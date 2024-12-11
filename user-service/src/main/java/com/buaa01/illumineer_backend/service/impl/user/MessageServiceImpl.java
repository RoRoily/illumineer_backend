package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.Message;
import com.buaa01.illumineer_backend.mapper.MessageMapper;
import com.buaa01.illumineer_backend.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ArrayList<Message> sendMsg(Integer receiver1, Integer receiver2, Integer type) throws JsonProcessingException {
        ArrayList<Message> messages = new ArrayList<>();
        Message message1 = new Message(); // 发给原告
        Message message2 = new Message(); // 发给被告
        message1.setSender(1);
        message1.setReceiver(receiver1);
        message1.setContent("请向被告申请调解" + LocalDateTime.now());
        message1.setType(type);
        message1.setStatus(0);
        message2.setSender(1);
        message2.setReceiver(receiver2);
        message2.setContent("请自己申请调解" + LocalDateTime.now());
        message2.setType(type);
        message2.setStatus(0);
        // 发送消息到 Kafka
        kafkaTemplate.send(MessageBuilder
                .withPayload(objectMapper.writeValueAsString(message1))
                .setHeader(KafkaHeaders.TOPIC, "topic")
                .setHeader(KafkaHeaders.PARTITION_ID, 0)
                .setHeader("userId", receiver1)  // 添加用户 ID 作为消息头
                .build());

        kafkaTemplate.send(MessageBuilder
                .withPayload(objectMapper.writeValueAsString(message2))
                .setHeader(KafkaHeaders.TOPIC, "topic")
                .setHeader(KafkaHeaders.PARTITION_ID, 1)
                .setHeader("userId", receiver2)  // 添加用户 ID 作为消息头
                .build());
        messages.add(message1);
        messages.add(message2);
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
