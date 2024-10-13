package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class UserFavoriteController {
    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 新增用户收藏夹
     * **/
    @PostMapping("/user/fav/createFav")
    public CustomResponse createFav()  {
        return userFavoriteService.createFav();
    }



    /**
     * 更新用户的收藏夹
     * @param pid 文章的id fid 收藏夹的id
     * @return CustomResponse
     * **/
    @PostMapping("/user/fav/update")
    public CustomResponse updateFav(@RequestParam("pid") Integer pid, @RequestParam("fid")Integer fid){
       return  userFavoriteService.updateFav(pid,fid);
    }



    /**
     * 查找用户的所有收藏夹
     * @param
     * @return CustomResponse List<Integer> 收藏夹fid的set,不是list!
     * **/
    @GetMapping("/user/fav/searchAll")
    public CustomResponse searchAll(){
        return userFavoriteService.searchAll();
    }


}
