package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Message;
import com.buaa01.illumineer_backend.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/msg/sendMsg")
    public CustomResponse sendMsg(@RequestBody Map<String, Integer> map) throws JsonProcessingException {
        CustomResponse customResponse = new CustomResponse();
        ArrayList<Message> messages = messageService.sendMsg(map.get("receiver1"), map.get("receiver2"), map.get("type"));
        if (!messages.isEmpty()) {
            customResponse.setCode(200);
            customResponse.setMessage("发送成功");
            customResponse.setData(messages);
        } else {
            customResponse.setCode(400);
            customResponse.setMessage("发送失败");
        }
        return customResponse;
    }

    @GetMapping("/msg/getMsg")
    public CustomResponse getMsg(@RequestParam("rid") Integer rid) {
        CustomResponse customResponse = new CustomResponse();
        ArrayList<Message> messages = messageService.getMsg(rid);
        customResponse.setCode(200);
        customResponse.setMessage("获取成功");
        customResponse.setData(messages);
        return customResponse;
    }
}
