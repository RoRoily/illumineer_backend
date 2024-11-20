package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Institution;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface InstitutionMapper extends BaseMapper<Institution> {
}
