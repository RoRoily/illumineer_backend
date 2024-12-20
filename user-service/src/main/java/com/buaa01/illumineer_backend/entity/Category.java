package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    //mainClassId = 0 ,subClassId = 1时为默认的未知领域
    private String mainClassId; //主领域
    private String subClassId; //细分领域
    private String mainClassName;
    private String subClassName;

    public Category Category_initial() {
        Category tempCategory = new Category();
        tempCategory.setMainClassId("0");
        tempCategory.setSubClassId("1");
        tempCategory.setMainClassName("unknown");
        tempCategory.setSubClassName("unknown");
        return tempCategory;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 将 Category 对象转换为 JSON 字符串
    public String toJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    // 将 JSON 字符串转换回 Category 对象
    public static Category fromJsonString(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Category.class);
    }
}
