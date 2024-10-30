package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Papers;
import org.apache.ibatis.annotations.Select;

public interface PaperMapper extends BaseMapper<Papers> {

    // 获取指定文献ID的详细信息
    @Select("select * from paper where pid = #{pid} and status = 0")
    Papers getPaperByPid(Integer pid);
}
