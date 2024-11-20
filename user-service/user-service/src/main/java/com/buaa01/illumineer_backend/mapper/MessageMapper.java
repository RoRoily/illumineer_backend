package com.buaa01.illumineer_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa01.illumineer_backend.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    @Insert("insert into message(sender,receiver,status,content,type) values(#{sender},#{receiver},#{status},#{content},#{type})")
    void insertMsg(Message message);

    @Select("select * from message where receiver = #{rid}")
    ArrayList<Message> getMsg(Integer rid);
}
