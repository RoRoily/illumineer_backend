package com.buaa01.illumineer_backend.mapper;

import com.buaa01.illumineer_backend.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {
    @Select("select * from category where name = #{name}")
    Category getCategoryByName(String name);

    @Insert("insert into category(name) values(#{name})")
    void insertCategory(String name);
}
