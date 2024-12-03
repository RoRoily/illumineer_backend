package com.buaa01.illumineer_backend.service.paper;

import java.util.List;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;

public interface PaperAdoptionService {

    /***
     * 根据作者姓名返回包含该姓名的认领条目列表
     * 
     * @param name 姓名
     **/
    CustomResponse getPaperAdoptionsByName(String name);

}
