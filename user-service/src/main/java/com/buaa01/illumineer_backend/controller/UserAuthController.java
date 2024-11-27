package com.buaa01.illumineer_backend.controller;

import com.aliyuncs.endpoint.UserCustomizedEndpointResolver;
import com.buaa01.illumineer_backend.service.user.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
/**
 * 进行实名的从
 * */
@RestController
public class UserAuthController {
/*
    @Autowired
    private UserAuthService userAuthService;

    /**
     *  认证成功后，更新用户的实名信息
     *  可能需要根据corcode返回的json格式进行修改
     * @param map 包含name institution gender 的map
     * @return CustomResponse实例
     *//*
    @PostMapping("/user/auth/authentation")
        public CustomResponse authentation(@RequestBody Map<String,String> map){
            String name = map.get("name");
            String institution = map.get("institution");
            String genderString = map.get("gender");
            Integer gender = Integer.parseInt(genderString);
        try {
            return userAuthService.authentation(name,institution,gender);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("实名认证出现错误");
            return customResponse;
            }
        }


    /**
     * 根据认证信息返回需要认领的文章信息
     * @param
     * @return CustomResponse实例
     * List中包含的是map属性 分别是文章的 name writer Date isClaimed pid
     *名称 作者 时间 是否被该用户实名认证的对象认领(被同名的认领了) 文章对应的pid（方便认领完成后返回给后端进行更新）
     * *//*
    @GetMapping("user/auth/getClaimList")
    public CustomResponse getClaimList(){
        try {
            return userAuthService.getClaimList();
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("获取带认领文章列表出现错误");
            return customResponse;
        }
    }

    /**
     * 完成文章的认领
     * @param pidList 被认领的pid的List
     * @return CustomResponse对象
     * **/
    /*
    @PostMapping("user/auth/claim")
    public CustomResponse claim(@RequestBody List<Integer> pidList){
        try {
            return userAuthService.claim(pidList);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("认领文章出现错误");
            return customResponse;
        }
    }
    */

}
