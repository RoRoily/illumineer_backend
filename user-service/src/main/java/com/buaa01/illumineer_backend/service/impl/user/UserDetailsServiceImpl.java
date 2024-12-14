package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 为springboot security提供自定义的用户认证逻辑
 * */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nick_name", username);
        queryWrapper.ne("stats", 2);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return null;
        }

        /**
         * serDetailsImpl 是一个自定义的类，应该实现了 UserDetails 接口，
         * Spring Security 依赖这个对象来获取用户的权限、账号状态等信息，来进行身份认证和授权。
         * */
        return new UserDetailsImpl(user);
    }
}
