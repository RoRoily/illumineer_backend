package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.UserRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {

    @Select("SELECT relevant FROM user_relation WHERE uid = ${uid}")
    String selectRelationByUid(Integer uid);
}
