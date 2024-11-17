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
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    //账号的相关信息
    private String avatar;
    private String account;
    private String password;
    private String nickName;
    private String description;
    //账号状态的相关信息
    private Integer status; // 0为管理员,1~9为不同权限的用户
    private Boolean isVerify; //是否已实名认证
    private Integer stats; // 0 正常 1 封禁 2 已注销
    //实际个人的相关信息
    private String name;
    private Integer gender;
    private String background;
    private List<String> field ;// 相关领域
    private String institution;

    private Map<String, Integer> intention; // <领域, 权重> 点击+1，下载+2，收藏+3
    //需要在Redis中存储的信息
    //名下论文集合 papers
    //合作者/关联者 collaborator
    //收藏夹 collections
    //下载记录 downloads

    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate;
}