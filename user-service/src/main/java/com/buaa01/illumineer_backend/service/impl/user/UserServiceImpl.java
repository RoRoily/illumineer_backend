package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.user.UserService;

public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    @Override
    public User getUserByUId(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }

        if (user.getStats() != 0) { // 不是正常状态
            return null;
        }

        return user;
    }
}