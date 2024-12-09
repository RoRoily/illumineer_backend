package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;

public interface UserFavoriteService {
    /**
     * 新建一个收藏夹
     */
    public CustomResponse createFav(String favName);

    /**
     * 将文章收藏至收藏夹
     **/
    public CustomResponse updateFav(Integer fid, Integer pid);

    /**
     * 在收藏夹中移除文章
     *
     **/
    public CustomResponse updateFav_Remove(Integer pid, Integer fid);

    /**
     * 查找用户的所有文件夹
     *
     **/
    public CustomResponse searchAll();
}
