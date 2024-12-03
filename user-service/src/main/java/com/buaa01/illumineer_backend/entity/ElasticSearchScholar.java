package com.buaa01.illumineer_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
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
    private Integer gender;
    private List<String> field ;// 相关领域
    private String institution;
}
