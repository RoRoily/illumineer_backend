package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("select * from category where sub_class_id = #{scid}")
    Category getCategoryByID(String scid);

    @Insert("insert into category(sub_class_id, main_class_id, sub_class_name, main_class_name) values(#{scid}, #{cid}, #{sname}, #{name})")
    void insertCategory(String scid, String cid, String sname, String name);
}
