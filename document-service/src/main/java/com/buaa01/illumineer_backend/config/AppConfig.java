package com.buaa01.illumineer_backend.config;

import com.buaa01.illumineer_backend.service.client.UserClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public UserClientService userClientService(){return new UserClientService();}
}