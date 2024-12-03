package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;

import java.util.List;

public interface PaperAdoptionService {

    /***
     * 根据作者姓名返回包含该姓名的认领条目列表
     * @param name 姓名
     * **/
    CustomResponse getPaperAdoptionsByName(String name);

    /***
     * 根据pids中的各个pid找到Paper，转换成PaperAdo并返回
     * @param pids
     * **/
    List<PaperAdo> getPaperAdoptionsByList(List<Long> pids);
}
