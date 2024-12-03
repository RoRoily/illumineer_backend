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
public class SearchResultPaper {
    private Long pid;
    /** 文章题目 */
    private String title;
    /** 关键词 */
    private List<String> keywords;
    /** 文章作者 */
    private Map<String, Integer> auths;
    /** 相关领域 */
    private String field;
    /** 文章类型 */
    private String type;
    /** 文章主题 */
    private String theme;
    /** 发布时间 */
    private Date publishDate;
    /** 文章来源 */
    private String derivation;
    /** 被引用次数 */
    private Integer ref_times;
    /** 收藏次数 */
    private Integer fav_time;

    // ————————以下字段不需要缓存—————————————
    /** 内容摘要 */
    // private String essAbs;
    /** 引用文献 */
    // private List<Integer> refs;
    /** 文章链接 */
    // private String contentUrl;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    // private Integer stats;
}
