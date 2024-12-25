package com.buaa01.illumineer_backend.service.user;
import com.buaa01.illumineer_backend.entity.CustomResponse;

public interface UserHistoryService {

    /**将文章保存至历史记录
     * **/
    public CustomResponse updateFav(Integer fid,Long pid);

    /**查找用户的所有文件夹
     *
     * **/
    public CustomResponse searchAll();
}

