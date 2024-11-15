package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.mapper.UserRelationMapper;
import com.buaa01.illumineer_backend.service.UserService;
import com.buaa01.illumineer_backend.service.user.UserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buaa01.illumineer_backend.entity.User;

@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Autowired
    private UserRelationMapper userRelationMapper;

    @Autowired
    private UserService userService;

    //更新学者的关系网络
    @Override
    public CustomResponse updateRelationByUid(Integer uid){
        CustomResponse customResponse = new CustomResponse();
        User user = userService.getUserByUId(uid);
        List

    }

    /**
     * 查询某学者的关系网络
     * **/
    @Override
    public CustomResponse searchRelationByUid(Integer uid){

    }
}
