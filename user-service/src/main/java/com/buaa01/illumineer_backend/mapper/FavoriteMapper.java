package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.Favorite;
import com.buaa01.illumineer_backend.entity.User;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorite WHERE fid = ${fid}")
    Favorite selectById(String fid);

    @Delete("DELETE FROM favorite WHERE fid = ${fid}")
    void deleteById(Integer fid);

    // @Update("UPDATE favorite SET count = count + 1 WHERE fid = ${fid}")
    // void updateFavCount(Integer fid);
}
