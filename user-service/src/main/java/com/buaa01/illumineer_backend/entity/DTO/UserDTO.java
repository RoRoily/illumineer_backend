package com.buaa01.illumineer_backend.entity.DTO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.buaa01.illumineer_backend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Date;
import java.util.List;

//仅返回用于用户登录后界面的简单信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Integer uid;
    //账号的相关信息
    private String avatar;
    private String email;
    private String Username;
    //账号状态的相关信息
    private Integer status; // 0为管理员,1~9为不同权限的用户
    private Boolean isVerify; //是否已实名认证
    private String institution; //所在机构
}
