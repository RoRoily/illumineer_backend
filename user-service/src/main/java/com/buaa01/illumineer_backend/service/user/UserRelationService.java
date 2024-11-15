package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;

//更新用之间关系
public interface UserRelationService {

    /**
     * 更新某学者的关系网络
     * */
    public CustomResponse updateRelationByUid(Integer uid);


    /**
     * 查询某学者的关系网络
     * **/
    public CustomResponse searchRelationByUid(Integer uid);
}
