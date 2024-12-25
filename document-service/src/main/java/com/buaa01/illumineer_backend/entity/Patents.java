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
public class Patents{
    @TableId(type = IdType.AUTO)
    private Long pid;
    /** 申请人 */
    private Map<String, Integer> applicants;
    /** 专利名 */
    private String name;
    /** 专利编号 */
    private String number;
    /** ipc分类号 */
    private String ipcClassification;
    /** 内容摘要 */
    private String abstracts;
    /** 授权时间 */
    private Date grantDate;
    /** 关键词 */
    private List<String> keywords;
    /** 发布时间 */
    private Date publishDate;
    /** 收藏次数 */
    private Integer fav_time;
    /** 状态: 0 正常 1 已删除 2 审核中 */
    private Integer stats;
}
