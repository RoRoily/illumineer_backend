package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Favorite;
import com.buaa01.illumineer_backend.entity.History;
import com.buaa01.illumineer_backend.entity.singleton.FidnumSingleton;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.FavoriteMapper;
import com.buaa01.illumineer_backend.mapper.HistoryMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.user.UserAccountService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.buaa01.illumineer_backend.entity.DTO.UserDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {

    FidnumSingleton fidnumInstance = FidnumSingleton.getInstance();

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private HistoryMapper historyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private FavoriteMapper favoriteMapper;


    /**
     * 用户注册
     * @param username 账号
     * @param password 密码
     * @param confirmedPassword 确认密码
     * @return customResponse对象
     * @Transactional 可以确保方法在一个数据库事务内执行。
     * 它保证方法的执行要么全部成功，要么全部回滚（失败），从而确保数据的一致性。
     */
    @Override
    @Transactional
    public CustomResponse register(String username, String password, String confirmedPassword, String email) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        if (username == null) {
            customResponse.setCode(403);
            customResponse.setMessage("用户名不能为空");
            return customResponse;
        }
        if (password == null || confirmedPassword == null) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        username = username.trim();   //删掉用户名的空白符
        if (username.isEmpty()) {
            customResponse.setCode(403);
            customResponse.setMessage("用户名不能为空");
            return customResponse;
        }
        if (username.length() > 12) {
            customResponse.setCode(403);
            customResponse.setMessage("用户名长度不能大于12");
            return customResponse;
        }
        if (password.isEmpty() || confirmedPassword.isEmpty()) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        if (password.length() > 50 || confirmedPassword.length() > 50 ) {
            customResponse.setCode(403);
            customResponse.setMessage("密码长度不能大于50");
            return customResponse;
        }
        if (!password.equals(confirmedPassword)) {
            customResponse.setCode(403);
            customResponse.setMessage("两次输入的密码不一致");
            return customResponse;
        }
        //判断账号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.ne("state", 2);
        User user = userMapper.selectOne(queryWrapper);   //查询数据库里值等于username并且没有注销的数据
        if (user != null) {
            customResponse.setCode(403);
            customResponse.setMessage("账号已存在");
            return customResponse;
        }

        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.orderByDesc("uid").last("limit 1");    // 降序选第一个
        User lastUser = userMapper.selectOne(queryWrapper1);
        int newUserUid;
        if (lastUser == null) {
            newUserUid = 1;
        } else {
            newUserUid = lastUser.getUid() + 1;
        }
        //更新用户密码
        String encodedPassword = passwordEncoder.encode(password);  // 密文存储
        //生成新的用户实体
        User newUser = User.setNewUser(encodedPassword,username,email);
        //更新数据库
        userMapper.insert(newUser);


        //创建收藏夹 Redis中和数据库中
        String fidsKey = "uForFav:" + newUser.getUid();
        redisTool.storeZSetByTime(fidsKey,fidnumInstance.addFidnum());
        favoriteMapper.insert(new Favorite(newUser.getUid(),newUser.getUid(),1,"默认收藏夹",0,0));

        //创建历史记录 hid和uid一样
        String hidsKey = "uForHis" + newUser.getUid();
        historyMapper.insert(new History(newUser.getUid(),newUser.getUid(),0));

        esTool.addUser(newUser);
        customResponse.setMessage("注册成功！欢迎加入Illumineer");
        return customResponse;
    }


/**
 * 用户登录
 * */
    @Override
    public CustomResponse login(String username, String password) {
        CustomResponse customResponse = new CustomResponse();

        //验证是否能正常登录
        //将用户名和密码封装成一个类，这个类不会存明文了，将是加密后的字符串
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // 处理用户名与密码是否争取
        Authentication authenticate;
        try {
            authenticate = authenticationProvider.authenticate(authenticationToken);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("账号或密码不正确");
            return customResponse;
        }

        //将用户取出来
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();

        // 更新redis中的数据
        redisTool.setExObjectValue("logUid:"+ user.getUid(),user); // 默认存活1小时

        // 检查账号状态，1 表示封禁中，不允许登录
        if (user.getStats() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }


        //将uid封装成一个jwttoken，同时token也会被缓存到redis中
        String token = jwtUtil.createToken(user.getUid().toString(), "user");
        try {
            // 把完整的用户信息存入redis，时间跟token一样，注意单位
            // 这里缓存的user信息建议只供读取uid用，其中的状态等非静态数据可能不准，所以 redis另外存值
            redisTool.setExObjectValue("securityUid:" + user.getUid(),user,60L*60*24*2, TimeUnit.SECONDS);
            // 将该用户放到redis中在线集合(需要吗)
            redisUtil.addMember("login_member", user.getUid());
        } catch (Exception e) {
            log.error("存储redis数据失败");
            throw e;
        }


        // 每次登录顺便返回user信息，就省去再次发送一次获取用户个人信息的请求
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setEmail(user.getEmail());
        userDTO.setInstitution(user.getInstitution());
        userDTO.setStatus(user.getStatus());
        userDTO.setUsername(user.getUsername());
        userDTO.setIsVerify(user.getIsVerify());

        Map<String, Object> final_map = new HashMap<>();
        final_map.put("token", token);
        final_map.put("user", userDTO);
        customResponse.setMessage("登录成功");
        customResponse.setData(final_map);
        return customResponse;
    }

    /**
     * 管理员登录
     * **/
    @Override
    public CustomResponse adminLogin(String username, String password) {
        return null;
    }

    @Override
    public CustomResponse personalInfo() {
        return null;
    }

    @Override
    public CustomResponse adminPersonalInfo() {
        return null;
    }

    @Override
    public void logout() {

    }

    @Override
    public void adminLogout() {

    }

    @Override
    public CustomResponse updatePassword(String pw, String npw) {
        return null;
    }
}
