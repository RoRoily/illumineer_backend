package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserRelationService;
import com.buaa01.illumineer_backend.tool.JsonWebTokenTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRelationController {

    @Autowired
    private UserRelationService userRelationService;

    /**
     * 查询某学者的关系网络
     */
    @GetMapping("/relation/get")
    public CustomResponse searchRelationByUid(@RequestParam("uid")Integer uid){
        return userRelationService.searchRelationByUid(uid);
    }

    /**
     * 更新学者的关系网络
     */
    @PostMapping("/relation/update")
    public CustomResponse updateRelationByUid(@RequestParam("uid")Integer uid){
        return userRelationService.updateRelationByUid(uid);
    }
}
