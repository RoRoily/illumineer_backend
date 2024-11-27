package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paper {

    @TableId(type = IdType.AUTO)
    private Integer pid;
    /** 文章题目 */
    private String title;
    /** 内容摘要 */
    private String essAbs;
    /** 关键词 */
    private List<String> keywords;
    /** 文章链接 */
    private String contentUrl;
    /** 文章作者 */
    private Map<String, Integer> auths;
    // 相关领域
    private List<String> category;
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
    /** 引用文献 */
    private List<Integer> refs;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    private Integer stats;
}