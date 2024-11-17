package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.UserRelation;
import com.buaa01.illumineer_backend.mapper.UserRelationMapper;
import com.buaa01.illumineer_backend.service.UserService;
import com.buaa01.illumineer_backend.service.user.UserRelationService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.buaa01.illumineer_backend.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Autowired
    private UserRelationMapper userRelationMapper;

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
        UserRelation userRelation = userRelationMapper.selectOne(wrapper.eq("uid", uid));
        CustomResponse customResponse = new CustomResponse();
        Map<Integer,Integer> relationNet = new HashMap<>();
        User user = userService.getUserByUId(uid);
        //查询已经被作者认领的文章的pid集合
        String paperList = "property:" + uid;
        Set<Object> paperSet = redisTool.getSetMembers(paperList);
        for(Object paper : paperSet){
            //获取文章所认领作者的集合
            String AuthList = "paperBelonged:" + (String)paper;
            Set<Object> userSet =   redisTool.getSetMembers(AuthList);
            for(Object auth : userSet){
                if(!relationNet.containsKey((Integer) auth)){
                    relationNet.put((Integer) auth, 1);
                }else{
                    Integer coNum = relationNet.get((Integer) auth) + 1;
                    relationNet.put((Integer) auth, coNum);
                }
            }
        }
        //更新用户的relationNet
        for (Map.Entry<Integer, Integer> entry : relationNet.entrySet()) {
            if (entry.getValue() >= 5) {
                // 这里是值 >= 5 时的处理逻辑
                if(!userRelation.getRelevant().contains(entry.getKey())){
                    userRelation.getRelevant().add(entry.getKey());
                }
            }
        }
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
        if(userRelationMapper.selectCount(wrapper.eq("uid", uid)) == 0){
            customResponse.setCode(500);
            customResponse.setMessage("该用户不存在userRelation实体类");
            return customResponse;
        }
        UserRelation userRelation = userRelationMapper.selectOne(wrapper.eq("uid", uid));
        customResponse.setData(userRelation.getRelevant());
        customResponse.setMessage("成功返回用户的关系网络");
        return customResponse;
    }
}
