package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 更新category
    @Update("UPDATE user SET category = #{category} WHERE uid = #{uid}")
    void updateCategory(Integer uid, Map<Category, Integer> category);
    @Select("SELECT * FROM user WHERE nick_name = #{nickName}")
    User getUserByNickName(String nickName);
    @Select("SELECT * FROM user WHERE email= #{email}")
    User getUserByEmail(String email);
}
