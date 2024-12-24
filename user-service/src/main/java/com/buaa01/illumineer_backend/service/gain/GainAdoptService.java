package com.buaa01.illumineer_backend.service.gain;

import java.util.List;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.PaperAdo;

public interface GainAdoptService {
    /**
     * 给出符合条件的文献
     * @param name 认领者的真实姓名
     * 需要将文章对象进行缓存，在完成认领的时候需要重新调用
     * **/
    public List<PaperAdo> getAllGain(String name);

    /**
     * 根据认领结果对数据库中的Paper的Auth进行修改
     * @param pids 被认领的文章集合 uid 被认领对象的id
     */
    public CustomResponse updateAdoption(List<Integer> pids,Integer uid);

    /**
     * 给出符合条件的文献
     * @param name 认领者的真实姓名
     * 需要将文章对象进行缓存，在完成认领的时候需要重新调用
     * **/
    public List<PaperAdo> getAllGainToClaim(String name);

    /**
     * 给出符合条件的文献
     * @param name 认领者的真实姓名
     * 需要将文章对象进行缓存，在完成认领的时候需要重新调用
     * **/
    public List<PaperAdo> getAllGainClaimed(String name);


    public CustomResponse claimAPaper(Integer uid,Long pid);

}

