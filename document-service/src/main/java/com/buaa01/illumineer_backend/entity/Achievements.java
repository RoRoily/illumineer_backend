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
public class Achievements {
    @TableId(type = IdType.AUTO)
    /** 成果id */
    private Integer aid;
    /** 关键词 */
    private List<String> keywords;
    /** 内容摘要 */
    private String essAbs;
    /** 发布时间 */
    private LocalDate publishDate;
    /** 收藏次数 */
    private Integer fav_time;
    /** 状态 0 正常 1 已删除 2 审核中 */
    private Integer stats; //
}