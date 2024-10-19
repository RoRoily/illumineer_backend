package com.buaa01.illumineer_backend.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    //账号的相关信息
    private String avatar;
    private String email;
    private String password;
    private String Username;
    private String description;
    private String background; //背景图
    //账号状态的相关信息
    private Integer status; // 0为管理员,1~9为不同权限的用户
    private Boolean isVerify; //是否已实名认证
    private Integer stats; // 0 正常 1 封禁 2 已注销
    //实际个人的相关信息
    private String name;
    private Integer authId;
    private Integer gender;
    private Map<Category,Integer> field;// 相关领域
    private String institution; //所在机构
    //需要在Redis中存储的信息
    //名下论文集合
    //合作者/关联者
    //收藏夹
    //下载记录

    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate;

    static public User setNewUser(String encodedPassword,String username,String email){
        Date now = new Date();
        User new_user = new User(
                null,
                "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png", //头像的url
                email,
                encodedPassword,
                username,
                null,
                "https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png", //背景的url
                1, //默认为最低权限的用户
                false, //未实名
                0, //账户状态正常
                null, //姓名
                null, //authId
                null, //性别
                null, //相关领域
                null, //所在机构
                now,
                null
        );
        return new_user;
    }

}