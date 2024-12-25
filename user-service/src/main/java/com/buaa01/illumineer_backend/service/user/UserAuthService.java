package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;

import java.util.List;

public interface UserAuthService {
    /**
     * 完成对用户实名下文章的更新
     * v1.0 对redis和mysql进行更新
     * **/
    CustomResponse claim(Integer add, List<Long> pids);

    CustomResponse authentation(String name,String Institution,Integer gender);
}
