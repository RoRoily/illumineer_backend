package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    // 获取指定文献ID的详细信息
    @Select("select * from paper where pid = #{pid} and status = 0")
    Paper getPaperByPid(Integer pid);
}
