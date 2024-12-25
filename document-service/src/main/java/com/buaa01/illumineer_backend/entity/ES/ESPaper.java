package com.buaa01.illumineer_backend.entity.ES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESPaper {
    private Long pid;
    // 文章的相关信息
    private String title; // 文章题目
    //    private String essAbs; // 文章摘要
    private List<String> keywords;
    private Map<String, Integer> auths; // 文章作者
    private List<String> field; // 相关领域
    private String type; // 文章类型 (期刊、论文、会议、报纸) // sql add
    private String theme; // 文章主题 // sql add
    private Date publishDate; // 出版时间
    private String derivation;// 来源
    private Integer ref_times; // 引用次数
    private Integer fav_time; // 收藏次数
}
