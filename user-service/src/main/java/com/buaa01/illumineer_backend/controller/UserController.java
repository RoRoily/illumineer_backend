package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 获取用户所有信息
     *
     * @param uid 用户ID
     * @return 用户信息
     */
    @GetMapping("/personal/allInfo")
    public CustomResponse getUserAllInfo(@RequestParam("uid") Integer uid) {
        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(userService.getUserByUId(uid));
        return response;
    }

    /**
     * 获取用户主页信息
     *
     * @param uid 用户ID
     * @return 用户主页信息
     */

    @GetMapping("/personal/homeInfo")
    public CustomResponse getUserHomeInfo(@RequestParam("uid") Integer uid) {
        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(userService.getUserHomeInfo(uid));
        return response;
    }

    /**
     * 获取用户简历信息
     *
     * @param uid 用户ID
     * @return 用户简历信息
     */

    @GetMapping("/personal/getResume")
    public CustomResponse getUserResume(@RequestParam("uid") Integer uid) {
        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(userService.getUserResume(uid));
        return response;
    }

    /**
     * 更新用户信息
     *
     * @param map 新的用户信息
     * @return 更新结果
     */

    @PutMapping("/personal/updateInfo")
    public CustomResponse updateUserResume(@RequestBody Map<String, Object> map) {
        int num = userService.updateUserInfo(map);
        if (num == 0)
            return new CustomResponse(200, "OK", null);
        else if (num == -1)
            return new CustomResponse(400, "昵称已存在", null);
        else if (num == -2)
            return new CustomResponse(400, "邮箱已存在", null);
        return new CustomResponse(400, "未知错误", null);
    }

    /**
     * 用户注销
     *
     * @return 删除结果
     */
    @PutMapping("/personal/delete")
    public CustomResponse deleteUser() {
        CustomResponse customResponse = new CustomResponse();
        int code = userService.deleteUser();
        if (code == 1) {
            customResponse.setCode(200);
            customResponse.setMessage("OK");
        } else {
            customResponse.setCode(400);
            customResponse.setMessage("No such user");
        }
        return customResponse;
    }

    /**
     * 管理员封禁用户
     *
     * @param uid 用户ID
     * @return 封禁结果
     */
    @PutMapping("/admin/disable")
    public CustomResponse disableUser(@RequestParam Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        int code = userService.disableUser(uid);
        if (code == 1) {
            customResponse.setCode(200);
            customResponse.setMessage("OK");
        } else {
            customResponse.setCode(400);
            customResponse.setMessage("No such user");
        }
        return customResponse;
    }

    /**
     * 管理员解封用户
     *
     * @param uid 用户ID
     * @return 解封结果
     */
    @PutMapping("/admin/able")
    public CustomResponse ableUser(@RequestParam Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        int code = userService.ableUser(uid);
        if (code == 1) {
            customResponse.setCode(200);
            customResponse.setMessage("OK");
        } else {
            customResponse.setCode(400);
            customResponse.setMessage("No such user");
        }
        return customResponse;
    }
}
