package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.User;

/**
 * @author Yees Tiew
 * @date 9/22/2024 10:09 PM
 */
public interface UserService {
    /**
     * 根据uid查询用户信息
     * @param id 用户ID
     * @return 用户实体类 User
     */
    User getUserByUId(Integer id);
}