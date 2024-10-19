package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paper {
    @TableId(type = IdType.AUTO)
    private Integer pid;
    // 文章的相关信息
    private String title; // 文章题目
    private String essAbs; // 文章摘要
    private List<String> keywords;
    private String contentUrl;
    private Map<String, Integer> auths; // 文章作者
    private List<String> field; // 相关领域
    private String Type; // 文章类型 (期刊、论文、会议、报纸) // sql add
    private String Theme; // 文章主题 // sql add
    private Date publishDate; // 出版时间
    private String derivation;// 来源
    private Integer ref_times; // 引用次数
    private Integer fav_time; // 收藏次数
    private List<Integer> refs; // 引用文献
    private Integer stats; // 0 正常 1 已删除
}