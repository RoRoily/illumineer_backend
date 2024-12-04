package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("select * from category where name = #{name}")
    Category getCategoryByName(String name);

    @Insert("insert into category(name) values(#{name})")
    void insertCategory(String name);

    @Select("select * from category where scid = #{scid} and cid = #{cid}")
    Category getCategoryBy2ID(String  scid, String cid);

    @Insert("insert into category(scid, cid, sname, name) values(#{scid}, #{cid}, #{sname}, #{name})")
    void insertCategory(String scid, String cid, String sname, String name);
}
