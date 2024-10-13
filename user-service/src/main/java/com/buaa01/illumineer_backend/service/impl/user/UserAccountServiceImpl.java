package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.service.user.UserAccountService;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

public class UserAccountServiceImpl implements UserAccountService {


    /**
     * 用户注册
     * @param username 账号
     * @param password 密码
     * @param confirmedPassword 确认密码
     * @return CustomResponse对象
     * @Transactional 可以确保方法在一个数据库事务内执行。
     * 它保证方法的执行要么全部成功，要么全部回滚（失败），从而确保数据的一致性。
     */
    @Override
    @Transactional
    public CustomResponse register(String username, String password, String confirmedPassword) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        if (username == null) {
            customResponse.setCode(403);
            customResponse.setMessage("账号不能为空");
            return customResponse;
        }
        if (password == null || confirmedPassword == null) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        username = username.trim();   //删掉用户名的空白符
        if (username.length() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("账号不能为空");
            return customResponse;
        }
        if (username.length() > 50) {
            customResponse.setCode(403);
            customResponse.setMessage("账号长度不能大于50");
            return customResponse;
        }
        if (password.length() == 0 || confirmedPassword.length() == 0 ) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        if (password.length() > 50 || confirmedPassword.length() > 20 ) {
            customResponse.setCode(403);
            customResponse.setMessage("密码长度不能大于20");
            return customResponse;
        }
        if (!password.equals(confirmedPassword)) {
            customResponse.setCode(403);
            customResponse.setMessage("两次输入的密码不一致");
            return customResponse;
        }
        //检查是否有昵称重名的情况
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
        User last_user = userMapper.selectOne(queryWrapper1);
        int new_user_uid;
        if (last_user == null) {
            new_user_uid = 1;
        } else {
            new_user_uid = last_user.getUid() + 1;
        }
        String encodedPassword = passwordEncoder.encode(password);  // 密文存储
        String avatar_url = "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png";
        String bg_url = "https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png";
        Date now = new Date();
        User new_user = new User(
                null,
                username,
                encodedPassword,
                "用户_" + new_user_uid,
                avatar_url,
                bg_url,
                2,
                "这个人很懒，什么都没留下~",
                0,
                (double) 0,
                0,
                0,
                0,
                0,
                null,
                now,
                null
        );
        userMapper.insert(new_user);
        msgUnreadMapper.insert(new MsgUnread(new_user.getUid(),0,0,0,0,0,0));
        favoriteMapper.insert(new Favorite(null, new_user.getUid(), 1, 1, null, "默认收藏夹", "", 0, null));
        esUtil.addUser(new_user);
        customResponse.setMessage("注册成功！欢迎加入T站");
        return customResponse;
    }
}
