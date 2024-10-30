package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.MessageMapper.MessageMapper;
import com.buaa01.illumineer_backend.entity.Message;
import com.buaa01.illumineer_backend.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public ArrayList<Message> sendMsg(Integer receiver1, Integer receiver2, Integer type) {
        ArrayList<Message> messages = new ArrayList<>();
        Message message1 = new Message();//发给原告
        Message message2 = new Message();//发给被告
        message1.setSender(1);
        message1.setReceiver(receiver1);
        message1.setContent("请向被告申请调解");
        message1.setType(type);
        message1.setStatus(0);
        message2.setSender(1);
        message2.setReceiver(receiver2);
        message2.setContent("请 yourself to apply mediation");
        message2.setType(type);
        message2.setStatus(0);
        //messageMapper.insert(message1);
        //messageMapper.insert(message2);
        messageMapper.insertMsg(message1);
        messageMapper.insertMsg(message2);
        // 创建连接和频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            ObjectMapper objectMapper = new ObjectMapper();
            // 设置消息属性，包括持久化
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2) // 2 表示持久化
                    .build();
            // 声明队列（如果队列不存在则创建）
            channel.queueDeclare("msg" + receiver1, true, false, false, null);
            // 发送消息
            channel.basicPublish("", "msg" + receiver1, props, objectMapper.writeValueAsString(message1).getBytes(StandardCharsets.UTF_8));
            channel.queueDeclare("msg" + receiver2, true, false, false, null);
            channel.basicPublish("", "msg" + receiver2, props, objectMapper.writeValueAsString(message2).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            messages.add(message1);
            messages.add(message2);
        }
        return messages;
    }

    @Override
    public ArrayList<Message> getMsg(Integer rid) {
        ArrayList<Message> messages = new ArrayList<>();
        // 创建连接和频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明队列（确保队列存在）
            channel.queueDeclare("msg" + rid, true, false, false, null);
            // 创建回调对象，当消息被接收时调用
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // 反序列化 JSON 字符串为 Message 实体
                    Message message = objectMapper.readValue(delivery.getBody(), Message.class);
                    messages.add(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            // 消费消息
            channel.basicConsume("msg" + rid, false, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }
}
