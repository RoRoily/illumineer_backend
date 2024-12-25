package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.UserRelation;
import com.buaa01.illumineer_backend.mapper.UserRelationMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.user.UserRelationService;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buaa01.illumineer_backend.entity.User;

import java.util.*;

@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Autowired
    private UserRelationMapper userRelationMapper;

    @Autowired
    private PaperServiceClient paperServiceClient;

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTool redisTool;

    /**
     * 更新策略：在合作超过5篇的用户之间建立关系
     * */
    //更新学者的关系网络
    @Override
    public CustomResponse updateRelationByUid(Integer uid){
        QueryWrapper<UserRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid);
//        UserRelation userRelation = redisTool.getObjectByClass("user_relation:"+uid, UserRelation.class);
//        if(userRelation == null){
          UserRelation userRelation = userRelationMapper.selectOne(wrapper);
//        }
        CustomResponse customResponse = new CustomResponse();
        Map<Integer,Integer> relationNet = new HashMap<>();
        User user = userService.getUserByUId(uid);
        //查询已经被作者认领的文章的pid集合
        String paperList = "property:" + uid;
        Set<Object> paperSet = redisTool.getSetMembers(paperList);
        for(Object pid : paperSet){
            //获取文章所认领作者的集合
            Map<String, Object> paper = paperServiceClient.getPaperById(Long.parseLong(pid.toString()));
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Integer> auths = null;
            try {
                auths = objectMapper.readValue(paper.get("auths").toString(), new TypeReference<Map<String, Integer>>() {
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
//            String AuthList = "paperBelonged:" + pid;
//            Set<Object> userSet = redisTool.getSetMembers(AuthList);
            Collection<Integer> userSet = auths.values();
            for(Integer auth : userSet){
                if (auth != 0 && auth != uid) {
                    if (!relationNet.containsKey(auth)) {
                        relationNet.put(auth, 1);
                    } else {
                        Integer coNum = relationNet.get(auth) + 1;
                        relationNet.put(auth, coNum);
                    }
                }
            }
        }
        List<Integer> relevants = null;
        if (userRelation.getRelevant() == null) {
            relevants = new ArrayList<>();
        } else {
            relevants = userRelation.getRelevant();
        }
        //更新用户的relationNet
        for (Integer auth: relationNet.keySet()) {
            if (relationNet.get(auth) >= 1) {
                // 这里是值 >= 1 时的处理逻辑
                if(relevants != null && !relevants.contains(auth)){
                    relevants.add(auth);
                }
            }
        }
        userRelation.setRelevant(relevants);
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("uid", uid);
        userRelationMapper.update(userRelation, updateWrapper);
        redisTool.setObjectValue("user_relation:" + uid, userRelation);
        customResponse.setMessage("更新该用户的关系网络成功！");

        return customResponse;
    }

    /**
     * 查询某学者的关系网络
     * **/
    @Override
    public CustomResponse searchRelationByUid(Integer uid){
        CustomResponse customResponse = new CustomResponse();
        QueryWrapper<UserRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid);
        if(userRelationMapper.selectCount(wrapper) == 0){
            customResponse.setCode(500);
            customResponse.setMessage("该用户不存在userRelation实体类");
            return customResponse;
        }
        String relations = userRelationMapper.selectRelationByUid(uid);
        List<Integer> userRelation = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            userRelation = objectMapper.readValue(relations, new TypeReference<List<Integer>>() {
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryWrapper<UserRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", 11);
        userRelationMapper.delete(queryWrapper);
        customResponse.setData(userRelation);
        customResponse.setMessage("成功返回用户的关系网络");
        return customResponse;
    }
}
