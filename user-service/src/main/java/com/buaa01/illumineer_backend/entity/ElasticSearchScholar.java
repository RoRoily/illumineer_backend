package com.buaa01.illumineer_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticSearchScholar {
    //es存储的学者类，查入驻学者
    private Integer uid;
    //账号的相关信息
    private String userName;
    private Integer stats; // 0 正常 1 封禁 2 已注销
    //实际个人的相关信息
    private String name;
    private Integer authId;
    private Integer gender;
    private Map<Category,Integer> field ;// 相关领域
    private String institution;
}
