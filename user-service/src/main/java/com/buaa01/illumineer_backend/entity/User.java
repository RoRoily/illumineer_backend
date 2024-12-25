package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.buaa01.illumineer_backend.handler.IntentionHandler;
import com.buaa01.illumineer_backend.handler.StringListTypeHandler;
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
    // 账号的相关信息
    private String avatar;
    private String email;
    private String password;
    private String nickName;
    private String description;
    // 账号状态的相关信息
    private Integer status; // 0为管理员,1~9为不同权限的用户
    private Boolean isVerify; // 是否已实名认证
    private Integer stats; // 0 正常 1 封禁 2 已注销
    @TableField(typeHandler = IntentionHandler.class)
    private Map<Category, Integer> intention;
    // 实际个人的相关信息
    private String name;
    private Integer gender;
    private String background;
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> field;// 相关领域
    private String institution;
    // 需要在Redis中存储的信息
    // 名下论文集合 papers
    // 合作者/关联者 collaborator
    // 收藏夹
    private Integer FavBias; // 收藏夹偏移，用以创建新的收藏夹
    // 下载记录 downloads

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate;

    public static User setNewUser(String encodedPassword, String username, String email) {
        String avatar_url = "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png";
        String bg_url = "https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png";
        Date now = new Date();
        User user = new User(
                null,
                avatar_url,
                email,
                encodedPassword,
                username,
                "该用户还未填写自我介绍",
                9,
                false,
                0,
                null, // intention
                null,
                null,
                bg_url,
                null,
                null,
                0,
                now,
                null);
        return user;
    }
}