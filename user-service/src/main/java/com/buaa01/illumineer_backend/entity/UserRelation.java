package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.buaa01.illumineer_backend.handler.IntegerListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.IntegerTypeHandler;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelation {
    private Integer uid;
    @TableField(typeHandler = IntegerListTypeHandler.class)
    private List<Integer> relevant;
}
