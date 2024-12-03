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
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MessageMapper messageMapper;

    // 存储已连接的 WebSocket 会话
    private Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @KafkaListener(id = "user-group", topics = "topic")
    public void consume(String message,
                        @Header("userId") Integer userId,
                        Acknowledgment acknowledgment) throws JsonProcessingException {
        // 解析消息
        Message message1 = objectMapper.readValue(message, Message.class);
        messageMapper.insertMsg(message1);  // 存储消息到数据库

        // 处理消息并通过 WebSocket 推送
        WebSocketSession session = userSessions.get(userId); // 获取与用户关联的 WebSocket 会话
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message)); // 通过 WebSocket 发送消息
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 手动提交偏移量
        // acknowledgment.acknowledge();
    }

    // 添加用户 WebSocket 会话
    public void addUserSession(Integer userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    // 移除用户 WebSocket 会话
    public void removeUserSession(Integer userId) {
        userSessions.remove(userId);
    }
}
