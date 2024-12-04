package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paper {
    @TableId(type = IdType.INPUT)
    private Long pid;
    // 文章的相关信息
    private String title; // 文章题目
    private List<String> keywords;
    private String contentUrl;
    private Map<String, Integer> auths; // 文章作者
    private String category; // 相关领域
    private String type; // 文章类型 (期刊、论文、会议、报纸) // sql add
    private String theme; // 文章主题 // sql add
    private Date publishDate; // 出版时间
    private String derivation;// 来源
    private Integer refTimes; // 引用次数
    private Integer favTimes; // 收藏次数
    private List<Long> refs; // 引用文献
    private Integer stats; // 0 正常 1 已删除
    private String essAbs; // 文章摘要
    /** 类别的id*/
    private Integer CategoryId;
}
