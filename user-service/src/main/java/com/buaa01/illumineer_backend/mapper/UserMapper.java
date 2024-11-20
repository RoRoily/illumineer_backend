package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Papers;
import com.buaa01.illumineer_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 更新category
    @Update("UPDATE user SET category = #{category} WHERE uid = #{uid}")
    void updateCategory(Integer uid, Map<Integer, Integer> category);
}
