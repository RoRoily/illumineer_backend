package com.buaa01.illumineer_backend.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean(name = "feignBearerTokenRequestInterceptor")  // 重命名为 feignBearerTokenRequestInterceptor
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return new BearerTokenRequestInterceptor();
    }
}