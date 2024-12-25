package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 科研项目类 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @TableId(type = IdType.AUTO)
    private Long pid;
    /** 主持人 */
    private Map<String, Integer> presider;
    /** 所属机构 */
    private Integer affiliation; // 机构id
    /** 关键词 */
    private List<String> keywords;
    /** 内容摘要 */
    private String essabs;
    /** 发布时间 */
    private Date publishDate;
    /** 收藏次数 */
    private Integer fav_time;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    private Integer stats;
}