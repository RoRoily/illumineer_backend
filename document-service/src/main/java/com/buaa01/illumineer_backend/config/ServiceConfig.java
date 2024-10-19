package com.buaa01.illumineer_backend.config;

import com.buaa01.illumineer_backend.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ServiceConfig {

    @Value("${ai-assistant.appId}")
    private String appId;

    @Value("${ai-assistant.apiKey}")
    private String apiKey;

    @Value("${ai-assistant.apiSecret}")
    private String apiSecret;

    @Bean
    public AIAssistantService aiAssistantService(RestTemplate restTemplate) {
        return new AIAssistantService(appId, apiKey, apiSecret);
    }
}