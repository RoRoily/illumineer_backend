package com.buaa01.illumineer_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    @TableId(type = IdType.AUTO)
    private Integer nid;//消息编号
    private Integer uid;//给哪个用户发消息
    private String title;//通知标题
    private String content;//通知内容
}
