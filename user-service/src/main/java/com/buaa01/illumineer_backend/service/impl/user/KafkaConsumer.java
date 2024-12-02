package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.Message;
import com.buaa01.illumineer_backend.mapper.MessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MessageMapper messageMapper;

    @KafkaListener(id = "user-group", topics = "topic")
    public void consume(String message,
                        @Header("userId") Integer userId,
                        Acknowledgment acknowledgment) throws JsonProcessingException {
        // 解析消息
        Message message1 = objectMapper.readValue(message, Message.class);
        messageMapper.insertMsg(message1);  // 存储消息到数据库
        // 手动提交偏移量
        // acknowledgment.acknowledge();
    }
}
