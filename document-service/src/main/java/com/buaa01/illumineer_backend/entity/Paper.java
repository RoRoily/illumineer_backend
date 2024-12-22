package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long pid;
    /** 文章题目 */
    private String title;
    /** 文章主题 */
    private String theme;
    /** 内容摘要 */
    private String essAbs;
    /** 关键词 */
    private List<String> keywords;
    /** 文章作者 */
    private Map<String, Integer> auths;
    /** 文章来源 */
    private String derivation;
    /** 文章类型 */
    private String type;
    /** 发布时间 */
    private Date publishDate;
    /** 相关领域 */
    private String category;
    /** 收藏次数 */
    private Integer favTimes;
    /** 被引用次数 */
    private Integer refTimes;
    /** 引用文献 */
    private List<Long> refs;
    /** 文章链接 */
    private String contentUrl;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    private Integer stats;
}