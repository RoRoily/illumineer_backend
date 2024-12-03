package com.buaa01.illumineer_backend.service.impl.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Service;

@Service
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final KafkaConsumer kafkaConsumer;

    @Autowired
    public CustomWebSocketHandler(KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userIdParam = session.getUri().getQuery(); // 获取连接 URL 的查询参数
        Integer userId = Integer.parseInt(userIdParam.split("=")[1]); // 获取 userId
        kafkaConsumer.addUserSession(userId, session); // 存储会话
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理接收到的消息
        System.out.println("Received: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Integer userId = getUserIdFromSession(session);
        kafkaConsumer.removeUserSession(userId); // 移除会话
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    private Integer getUserIdFromSession(WebSocketSession session) {
        // 获取用户 ID（可以根据实际情况修改）
        return (Integer) session.getAttributes().get("userId");
    }
}
