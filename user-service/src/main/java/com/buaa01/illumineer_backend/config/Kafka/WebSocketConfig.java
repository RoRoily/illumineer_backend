package com.buaa01.illumineer_backend.config.Kafka;

import com.buaa01.illumineer_backend.service.impl.user.CustomWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private CustomWebSocketHandler customWebSocketHandler; // 使用自定义的 WebSocketHandler

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/ws") // 使用自定义处理器
                .setAllowedOrigins("*"); // 配置允许的跨域请求
    }
    /**
     * 仅在非测试环境下注册一个ServerEndpointExporter，该Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     */
    @Profile("!test")
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
