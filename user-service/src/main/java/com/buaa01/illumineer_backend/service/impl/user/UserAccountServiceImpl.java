package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.*;
import com.buaa01.illumineer_backend.entity.DTO.UserDTO;
import com.buaa01.illumineer_backend.entity.singleton.FidnumSingleton;
import com.buaa01.illumineer_backend.im.IMServer;
import com.buaa01.illumineer_backend.mapper.FavoriteMapper;
import com.buaa01.illumineer_backend.mapper.HistoryMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.mapper.UserRelationMapper;
import com.buaa01.illumineer_backend.service.user.UserAccountService;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.ESTool;
import com.buaa01.illumineer_backend.tool.JsonWebTokenTool;
import com.buaa01.illumineer_backend.tool.RedisTool;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
    private CurrentUser currentUser;
    @Autowired
    private UserService userService;
    @Autowired
    private HistoryMapper historyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private JsonWebTokenTool jsonWebTokenTool;
    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;
    @Autowired
    private ESTool esTool;
    @Autowired
    private UserRelationMapper userRelationMapper;

    /**
     * 用户注册
     *
     * @param username          账号
     * @param password          密码
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
        if (password.length() > 50 || confirmedPassword.length() > 50) {
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
        queryWrapper.eq("nick_name", username);
        queryWrapper.ne("stats", 2);
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
        User newUser = User.setNewUser(encodedPassword, username, email);
        //更新数据库
        userMapper.insert(newUser);


        //创建收藏夹 Redis中和数据库中
        String fidsKey = "uForFav:" + newUser.getUid();
        redisTool.storeZSetByTime(fidsKey, newUser.getUid());
        favoriteMapper.insert(new Favorite(newUser.getUid(), newUser.getUid(), 1, "默认收藏夹", 0, 0));


        //创建历史记录 hid和uid一样
        String hidsKey = "uForHis" + newUser.getUid();
        historyMapper.insert(new History(newUser.getUid(), newUser.getUid(), 0));

        //创建关系网，并添加到redis和数据库
        UserRelation userRelation = new UserRelation(newUserUid, new ArrayList<>());
        redisTool.setObjectValue("user_relation:" + newUser.getUid(), userRelation);
        userRelationMapper.insert(userRelation);
        esTool.addUser(newUser);
        customResponse.setMessage("注册成功！欢迎加入Illumineer");
        return customResponse;
    }


    /**
     * 用户登录
     */
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
        redisTool.setExObjectValue("security:user:" + user.getUid(), user); // 默认存活1小时

        // 检查账号状态，1 表示封禁中，不允许登录
        if (user.getStats() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }


        //将uid封装成一个jwttoken，同时token也会被缓存到redis中
        String token = jsonWebTokenTool.createToken(user.getUid().toString(), "user");
        try {
            // 把完整的用户信息存入redis，时间跟token一样，注意单位
            // 这里缓存的user信息建议只供读取uid用，其中的状态等非静态数据可能不准，所以 redis另外存值
            redisTool.setExObjectValue("securityUid:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);
            // 将该用户放到redis中在线集合(需要吗)
            redisTool.addSetMember("login_member", user.getUid());
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
        userDTO.setUsername(user.getNickName());
        userDTO.setIsVerify(user.getIsVerify());
        userDTO.setStats(user.getStats());

        Map<String, Object> final_map = new HashMap<>();
        final_map.put("token", token);
        final_map.put("user", userDTO);
        customResponse.setMessage("登录成功");
        customResponse.setData(final_map);
        return customResponse;
    }

    /**
     * 管理员登录
     **/
    @Override
    public CustomResponse adminLogin(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationProvider.authenticate(authenticationToken);
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();
        CustomResponse customResponse = new CustomResponse();
        // 普通用户无权访问
        if (user.getStatus() != 0) {
            customResponse.setCode(403);
            customResponse.setMessage("您不是管理员，无权访问");
            return customResponse;
        }

        // 顺便更新redis中的数据,默认存活一小时
        redisTool.setExObjectValue("logUid:" + user.getUid(), user); // 默认存活1小时

        // 检查账号状态，1 表示封禁中，不允许登录
        if (user.getStats() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }
        //将uid封装成一个jwttoken，同时token也会被缓存到redis中
        String token = jsonWebTokenTool.createToken(user.getUid().toString(), "admin");
        try {
            redisTool.setExObjectValue("securityUid:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);
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
        userDTO.setUsername(user.getNickName());
        userDTO.setIsVerify(user.getIsVerify());
        userDTO.setStats(user.getStats());

        Map<String, Object> final_map = new HashMap<>();
        final_map.put("token", token);
        final_map.put("user", userDTO);
        customResponse.setMessage("欢迎回来，管理员");
        customResponse.setData(final_map);
        return customResponse;
    }


    /**
     * 获取用户个人信息
     *
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse personalInfo() {
        Integer loginUserId = currentUser.getUserId();
        User userDTO = userService.getUserByUId(loginUserId);

        // 从redis中获取最新数据
        User user = redisTool.getObjectByClass("user" + loginUserId, User.class);
        // 如果redis中没有user数据，就从mysql中获取并更新到redis
        if (user == null) {
            user = userMapper.selectById(loginUserId);
            User finalUser = user;
            CompletableFuture.runAsync(() -> {
                redisTool.setExObjectValue("user:" + finalUser.getUid(), finalUser);  // 默认存活1小时
            }, taskExecutor);
        }


        CustomResponse customResponse = new CustomResponse();
        // 检查账号状态，1 表示封禁中，不允许登录，2表示账号注销了
        if (userDTO.getStats() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("账号已注销");
            return customResponse;
        }
        if (userDTO.getStats() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }

        customResponse.setData(userDTO);
        return customResponse;
    }

    /**
     * 获取管理员个人信息
     *
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse adminPersonalInfo() {
        Integer LoginUserId = currentUser.getUserId();

        // 从redis中获取最新数据
        User user = redisTool.getObjectByClass("user" + LoginUserId, User.class);
        // 如果redis中没有user数据，就从mysql中获取并更新到redis
        if (user == null) {
            user = userMapper.selectById(LoginUserId);
            User finalUser = user;
            CompletableFuture.runAsync(() -> {
                redisTool.setExObjectValue("user:" + finalUser.getUid(), finalUser);  // 默认存活1小时
            }, taskExecutor);
        }

        CustomResponse customResponse = new CustomResponse();

        // 普通用户无权访问
        if (user.getStatus() != 0) {
            customResponse.setCode(403);
            customResponse.setMessage("您不是管理员，无权访问");
            return customResponse;
        }
        // 检查账号状态，1 表示封禁中，不允许登录，2表示已注销
        if (user.getStats() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("账号已注销");
            return customResponse;
        }
        if (user.getStats() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setEmail(user.getEmail());
        userDTO.setInstitution(user.getInstitution());
        userDTO.setStatus(user.getStatus());
        userDTO.setUsername(user.getNickName());
        userDTO.setIsVerify(user.getIsVerify());
        userDTO.setStats(user.getStats());


        customResponse.setData(userDTO);
        return customResponse;
    }


    /**
     * 退出登录，清空redis中相关用户登录认证
     */
    @Override
    public void logout() {
        Integer LoginUserId = currentUser.getUserId();
        // 清除redis中该用户的登录认证数据

        redisTool.deleteValue("token:user:" + LoginUserId);
        redisTool.deleteValue("security:user:" + LoginUserId);
        //FIXME : 这里在线用户集合是set还是zset?
        redisTool.deleteSetMember("login_member", LoginUserId);   // 从在线用户集合中移除
        redisTool.deleteByPrefix("whisper" + LoginUserId + ":"); // 清除全部在聊天窗口的状态

        // 断开全部该用户的channel 并从 userChannel 移除该用户
        Set<Channel> userChannels = IMServer.userChannel.get(LoginUserId);
        if (userChannels != null) {
            for (Channel channel : userChannels) {
                try {
                    channel.close().sync(); // 等待通道关闭完成
                } catch (InterruptedException e) {
                    // 处理异常，如果有必要的话
                    e.printStackTrace();
                }
            }
            IMServer.userChannel.remove(LoginUserId);
        }
    }

    /**
     * 管理员退出登录，清空redis中相关管理员登录认证
     */
    @Override
    public void adminLogout() {
        Integer LoginUserId = currentUser.getUserId();
        // 清除redis中该用户的登录认证数据
        redisTool.deleteValue("token:admin:" + LoginUserId);
        redisTool.deleteValue("security:admin:" + LoginUserId);
    }

    /**
     * 更新密码
     */

    @Override
    public CustomResponse updatePassword(String pw, String npw) {
        CustomResponse customResponse = new CustomResponse();
        if (npw == null || npw.length() == 0) {
            customResponse.setCode(500);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }

        // 取出当前登录的用户
        UsernamePasswordAuthenticationToken authenticationToken1 =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails1 = (UserDetailsImpl) authenticationToken1.getPrincipal();
        User user = userDetails1.getUser();

        // 验证旧密码
        UsernamePasswordAuthenticationToken authenticationToken2 =
                new UsernamePasswordAuthenticationToken(user.getNickName(), pw);
        try {
            authenticationProvider.authenticate(authenticationToken2);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不正确");
            return customResponse;
        }

        if (Objects.equals(pw, npw)) {
            customResponse.setCode(500);
            customResponse.setMessage("新密码不能与旧密码相同");
            return customResponse;
        }

        String encodedPassword = passwordEncoder.encode(npw);  // 密文存储

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", user.getUid()).set("password", encodedPassword);
        userMapper.update(null, updateWrapper);

        logout();
        adminLogout();
        return customResponse;
    }
}

