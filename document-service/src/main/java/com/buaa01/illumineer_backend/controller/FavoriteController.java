package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    /**
     * 查找给定收藏夹下的所有文章
     * 
     * @param fid
     * @return CustomResponse
     **/
    @GetMapping("/fav/getByFid")
    public CustomResponse getPidsByFid(@RequestParam("fid") Integer fid) {
        return favoriteService.getPidsByFId(fid);
    }
}
