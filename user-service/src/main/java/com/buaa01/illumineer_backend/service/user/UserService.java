package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.DTO.UserDTO;
import com.buaa01.illumineer_backend.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface UserService {
    /**
     * 根据用户id获取用户所有信息
     *
     * @param uid 用户ID
     * @return 用户信息
     */
    User getUserByUId(Integer uid);

    /**
     * 获取用户展示信息
     *
     * @param uid 用户ID
     * @return 用户展示信息
     */

    UserDTO getUserHomeInfo(Integer uid);

    /**
     * 获取用户简历信息
     *
     * @param uid 用户ID
     * @return 用户简历信息
     */

    Map<String, Object> getUserResume(Integer uid);

    /**
     * 更新用户信息
     *
     * @param info 更新信息
     * @return 更新结果
     */

    int updateUserInfo(Map<String, Object> info);

    /**
     * 用户注销
     *
     * @return 删除结果
     */
    int deleteUser();

    /**
     * 封禁用户
     *
     * @param uid 用户ID
     * @return 封禁结果
     */
    int disableUser(Integer uid);

    /**
     * 解封用户
     *
     * @param uid 用户ID
     * @return 解封结果
     */
    int ableUser(Integer uid);
}
