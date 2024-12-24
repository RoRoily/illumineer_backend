package com.buaa01.illumineer_backend.service.impl.gain;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.PaperAdo;
import com.buaa01.illumineer_backend.service.gain.GainAdoptService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buaa01.illumineer_backend.service.client.PaperServiceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GainAdoptServiceImpl implements GainAdoptService {
    @Autowired
    private PaperServiceClient paperServiceClient;

    @Autowired
    private RedisTool redisTool;
    /**
     * 给出符合条件的文献
     * @param name 认领者的真实姓名
     * 需要将文章对象进行缓存，在完成认领的时候需要重新调用
     * **/
    @Override
    public List<PaperAdo> getAllGain(String name){
        String adoptionKey = "adoption :" + name;
        if(!redisTool.isExist(adoptionKey)){
        List<PaperAdo> paperAdoptions = paperServiceClient.getPaperAdoByName(name);
        //将带认领的文献列表存入redis中
        //在对应的document-service中将paper实体类放入redis
        for(PaperAdo paperAdo : paperAdoptions){
            redisTool.addSetMember(adoptionKey,paperAdo.getPid());
        }
        redisTool.setExpire(adoptionKey,300);
        return paperAdoptions;}
        else{
            List<Long> pids = redisTool.getAllList(adoptionKey,Long.class);
            return paperServiceClient.getPaperAdoByList(pids);
        }
    }

    /**
     * 根据认领结果对数据库中的Paper的Auth进行修改
     * @param pids 被认领的文章集合 uid 被认领对象的id
     */
    @Override
    public CustomResponse updateAdoption(List<Integer> pids, Integer uid){
        CustomResponse customResponse = new CustomResponse(200,"待修改",null);
        return customResponse;
    }
}
