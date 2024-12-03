package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/user/personal/allInfo")
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

    @GetMapping("/user/personal/homeInfo")
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

    @GetMapping("/user/personal/getResume")
    public CustomResponse getUserResume(@RequestParam("uid") Integer uid) {
        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(userService.getUserResume(uid));
        return response;
    }

    /**
     * 更新用户简历信息
     *
     * @param description 新的用户简历信息
     * @return 更新结果
     */

    @PutMapping("/user/personal/updateResume")
    public CustomResponse updateUserResume(@RequestBody String description) {
        int num = userService.updateUserResume(description);
        if (num == 1)
            return new CustomResponse(200, "OK", null);
        else
            return new CustomResponse(400, "Bad Request", null);
    }

    /**
     * 用户注销
     *
     * @return 删除结果
     */
    @PutMapping("/user/personal/delete")
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
    @PutMapping("/user/admin/disable")
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
    @PutMapping("/user/admin/able")
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
