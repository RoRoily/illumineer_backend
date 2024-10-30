package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.*;

/** 科研项目类 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project extends Achievements {
    @TableId(type = IdType.AUTO)
    private Integer pid;
    /** 主持人 */
    private Map<String, Integer> presider;
    /** 所属机构 */
    private Integer affiliation; // 机构id
}