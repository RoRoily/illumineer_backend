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
public class Papers extends Achievements {
    @TableId(type = IdType.AUTO)
    private Integer pid;
    /** 文章题目 */
    private String title;
    /** 文章主题 */
    private String theme;
    /** 文章类型 (期刊、论文、会议、报纸) */
    private String Type;
    /** 文章作者 */
    private Map<String, Integer> auths;
    /** 文章来源 */
    private String derivation;
    /** 相关领域 */
    private List<Category> field;
    /** 被引用次数 */
    private Integer ref_times;
    /** 引用文献 */
    private List<Integer> refs;
    /** 文章链接 */
    private String contentUrl;
}