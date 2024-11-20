package com.buaa01.illumineer_backend.entity;

import co.elastic.clients.util.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer mid;
    private Integer sender;//发送者uid
    private Integer receiver;//接受者uid
    private Integer type;//消息类型
    private String title;//标题
    private String content;//内容
    private DateTime sendTime;//发送时间
    private Integer status;//状态(未读、已读等等)
}
