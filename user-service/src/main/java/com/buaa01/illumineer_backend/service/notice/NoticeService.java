package com.buaa01.illumineer_backend.service.notice;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Notice;

import java.util.List;

public interface NoticeService {
    /**
     * 向指定用户发送消息
     * @param uid 用户id
     * @param title 消息标题
     * @param content 消息内容
     * @return 响应对象
     */
    CustomResponse sendNotice(Integer uid, String title, String content);

    /**
     * 获取指定User的消息
     * @param uid 用户id
     * @return Notice数组
     */
    List<Notice> getNotice(Integer uid);
}
