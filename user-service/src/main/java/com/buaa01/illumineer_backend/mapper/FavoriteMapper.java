package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import com.buaa01.illumineer_backend.entity.Favorite;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Delete("DELETE FROM favorite WHERE fid = ${fid}")
    void deleteById(Integer fid);
}
