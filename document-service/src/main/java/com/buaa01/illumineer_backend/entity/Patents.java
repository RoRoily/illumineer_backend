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
public class Patents extends Achievements {
    @TableId(type = IdType.AUTO)
    private Integer pid;
    /** 申请人 */
    private Map<String, Integer> applicants;
    /** 专利名 */
    private String name;
    /** 专利编号 */
    private String number;
    /** ipc分类号 */
    private String ipcClassification;
    /** 授权时间 */
    private LocalDate grantDate;
}
