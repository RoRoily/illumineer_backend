package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.user.UserFavoriteService;

import feign.Param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class UserFavoriteController {
    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 新增用户收藏夹
     **/
    @PostMapping("/user/fav/createFav")
    public CustomResponse createFav(@RequestParam("favName") String favName) {
        return userFavoriteService.createFav(favName);
    }

    /**
     * 删除收藏夹
     *
     * @param
     * @return CustomResponse
     **/
    @PostMapping("/user/fav/deleteFav")
    public CustomResponse deleteFav(@RequestParam("fid") Integer fid) {
        return userFavoriteService.deleteFav(fid);
    }

    /**
     * 修改收藏夹名称
     *
     * @param fid 收藏夹id newName 新的收藏夹名称
     * @return CustomResponse
     **/
    @PutMapping("/user/fav/changeName")
    public CustomResponse changeFavName(@RequestParam("fid") Integer fid, @RequestParam("name") String name) {
        return userFavoriteService.changeFavName(fid, name);
    }

    /**
     * 更新用户的收藏夹（新增收藏）
     *
     * @param pid 文章的id fid 收藏夹的id
     * @return CustomResponse
     **/
    @PostMapping("/user/fav/add")
    public CustomResponse addPapertoFav(@RequestParam("pid") Long pid, @RequestParam("fid") Integer fid) {
        return userFavoriteService.addPapertoFav(fid, pid);
    }

    /**
     * 更新用户的收藏夹（移除收藏）
     *
     * @param pid 文章的id fid 收藏夹的id
     * @return CustomResponse
     **/
    @PostMapping("/user/fav/remove")
    public CustomResponse removePaperfromFav(@RequestParam("pid") Long pid, @RequestParam("fid") Integer fid) {
        return userFavoriteService.removePaperfromFav(fid, pid);
    }

    /**
     * 查找用户的所有收藏夹
     *
     * @param
     * @return CustomResponse
     **/
    @GetMapping("/user/fav/searchAll")
    public CustomResponse searchAll() {
        return userFavoriteService.searchAll();
    }

    /**
     * 批量操作收藏夹
     *
     * @param pid 文章id fids 收藏夹id的集合
     * @return CustomResponse
     **/
    @PostMapping("/user/fav/Batch")
    public CustomResponse ProcessFavBatch(@RequestParam("pid") Long pid, @RequestParam("fids") List<Integer> fids) {
        return userFavoriteService.ProcessFavBatch(pid, fids);
    }

    /**
     * 查找收藏夹中的文章
     *
     * @param fid 收藏夹id
     * @return CustomResponse
     **/

    @PostMapping("/user/fav/papers")
    public CustomResponse getPapersByFid(@RequestParam("fid") Integer fid) {
        return userFavoriteService.getPapersByFid(fid);
    }
}
