package com.buaa01.illumineer_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserAccountService;

import java.util.Map;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * 注册接口
     * @param map 包含username password confirmedPassWord e-mail的 map
     * @return CustomResponse对象
     */

    @PostMapping("/account/register")
    public CustomResponse register(@RequestBody Map<String,String> map){
        String email = map.get("email");
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword =  map.get("confirmedPassWord");
        try {
            return userAccountService.register(username, password, confirmedPassword,email);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("注册出现错误！");
            return customResponse;
        }
    }


    /**
    * 登录接口
     * @param map 包含email password  的map
     * @return CustomResponse对象
     * */
    @PostMapping("/account/login")
    public CustomResponse login(@RequestBody Map<String,String> map){
        String email = map.get("username");
        String password = map.get("password");
        return userAccountService.login(email,password);
    }

    /**
     * 管理员登录接口
     * @param map 包含 email password 的 map
     * @return CustomResponse对象
     */
    @PostMapping("/admin/account/login")
    public CustomResponse adminLogin(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        String password = map.get("password");
        return userAccountService.adminLogin(email, password);
    }

    /**
     * 获取当前登录用户信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/personal/info")
    public CustomResponse personalInfo() {
        return userAccountService.personalInfo();
    }

    /**
     * 获取当前登录管理员信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/admin/personal/info")
    public CustomResponse adminPersonalInfo() {
        return userAccountService.adminPersonalInfo();
    }

    /**
     * 退出登录接口
     */
    @GetMapping("/user/account/logout")
    public void logout() {
        userAccountService.logout();
    }

    /**
     * 管理员退出登录接口
     */
    @GetMapping("/admin/account/logout")
    public void adminLogout() {
        userAccountService.adminLogout();
    }

    /**
     * 修改当前用户密码
     * @param pw    就密码
     * @param npw   新密码
     * @return  响应对象
     */
    @PostMapping("/user/password/update")
    public CustomResponse updatePassword(@RequestParam("pw") String pw, @RequestParam("npw") String npw) {
        return userAccountService.updatePassword(pw, npw);
    }
}
