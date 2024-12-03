package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Notice;
import com.buaa01.illumineer_backend.service.notice.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @PostMapping("user/send/notice")
    public CustomResponse sendNotice(@RequestBody Map<String, Object> noticeMap) {
        CustomResponse customResponse = new CustomResponse();
        Integer user_id = (Integer) noticeMap.get("user_id");
        String title = (String) noticeMap.get("title");
        String content = (String) noticeMap.get("content");
        if(user_id == null || title == null || content == null) {
            customResponse.setCode(400);
            customResponse.setMessage("发送消息的参数缺失");
            return customResponse;
        }
        noticeService.sendNotice(user_id, title, content);
        return customResponse;
    }

    @GetMapping("user/get/notice")
    public CustomResponse getNotice(@RequestParam("user_id") Integer user_id) {
        CustomResponse customResponse = new CustomResponse();
        List<Notice> noticeList = noticeService.getNotice(user_id);
        if(noticeList == null || noticeList.isEmpty()) {
            customResponse.setCode(400);
            customResponse.setMessage("没有找到该用户的消息");
            return customResponse;
        }
        customResponse.setCode(200);
        customResponse.setData(noticeList);
        return customResponse;
    }
}
