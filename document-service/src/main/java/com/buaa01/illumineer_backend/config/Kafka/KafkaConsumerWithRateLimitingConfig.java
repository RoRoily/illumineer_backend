package com.buaa01.illumineer_backend.config.Kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@EnableKafka
//指示为配置类，sprig将从中读取bean定义
@Configuration
public class KafkaConsumerWithRateLimitingConfig {

    //每秒允许处理的最大消息树
    private static final int MAX_MESSAGES_PER_SECOND = 5;
    private static final int BUFFER_SIZE = 100;
    //用于线程安全地计数当前秒内已处理的消息数量。
    private AtomicInteger messageCount = new AtomicInteger(0);
    //记录上次重置计数器的时间。
    private long lastTimestamp = System.currentTimeMillis();
    private BlockingQueue<String> messageBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
    /**
     * 创建并配置Kafka消费者工厂
     * **/
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        //Kafka服务器地址
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
       //消费者组ID
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "your-group-id");
        //指定反序列化器
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }


    /**
     * 创建Kafka监听器容器工厂
     * 设置之前定义的消费者工厂。这允许应用程序并发处理来自Kafka的消息。
     * **/
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    /**
     * 消息监听器
     * 监听指定主题的消息
     * **/
    @KafkaListener(topics = "your-topic", groupId = "your-group-id")
    public void listen(String message) {
        long currentTime = System.currentTimeMillis();

        // 超过1秒时充值计数器和时间戳
        if (currentTime - lastTimestamp >= 1000) {
            messageCount.set(0);
            lastTimestamp = currentTime;
        }

        // 每秒接收消息不超过最大，可以立即处理
        if (messageCount.incrementAndGet() <= MAX_MESSAGES_PER_SECOND) {
            System.out.println("Received Message: " + message);
        } else {
            // 加入到buffer
            try {
                if (!messageBuffer.offer(message, 100, TimeUnit.MILLISECONDS)) {
                    System.out.println("Buffer is full. Message dropped: " + message);
                    messageCount.decrementAndGet(); // Adjust count back
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Process messages from the buffer in a separate thread
        processBufferedMessages();
    }

    /**
     * 启动一个新的线程从缓冲区中获取消息进行处理
     * 设置了1秒的超时
     */
    private void processBufferedMessages() {
        new Thread(() -> {
            while (true) {
                try {
                    String bufferedMessage = messageBuffer.poll(1, TimeUnit.SECONDS);
                    if (bufferedMessage != null) {
                        System.out.println("Processing buffered message: " + bufferedMessage);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}


