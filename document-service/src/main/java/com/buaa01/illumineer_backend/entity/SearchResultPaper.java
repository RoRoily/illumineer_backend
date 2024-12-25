package com.buaa01.illumineer_backend.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private String keywords;
    /** 文章作者 */
    private String auths;
    /** 相关领域 */
    private String category;
    /** 文章类型 */
    private String type;
    /** 文章主题 */
    private String theme;
    /** 发布时间 */
    private Date publishDate;
    /** 文章来源 */
    private String derivation;
    /** 被引用次数 */
    private Integer refTimes;
    /** 收藏次数 */
    private Integer favTime;
    /** 文章链接 */
     private String contentUrl;

    // ————————以下字段不需要缓存—————————————
    /** 内容摘要 */
    // private String essAbs;
    /** 引用文献 */
    // private List<Integer> refs;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    // private Integer stats;

    public List<String> getKeywords() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> keywords = null;
        try {
            if (!this.keywords.isEmpty()) {
                keywords = objectMapper.readValue(this.keywords, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keywords;
    }

    public Map<String, Integer> getAuths() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Integer> auths = null;
        try {
            if (!this.auths.isEmpty()) {
                auths = objectMapper.readValue(this.auths, new TypeReference<Map<String, Integer>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auths;
    }
}
