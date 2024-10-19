package com.buaa01.illumineer_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIAssistantService {

    private final RestTemplate restTemplate;
    private final String appId;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public AIAssistantService(RestTemplate restTemplate,
                              @Value("${ai-assistant.appId}") String appId,
                              @Value("${ai-assistant.apiKey}") String apiKey,
                              @Value("${ai-assistant.apiSecret}") String apiSecret) {
        this.restTemplate = restTemplate;
        this.appId = appId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String generateKeywords(String userQuery) {
        // 构建请求头
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "api_key=" + apiKey + ":" + apiSecret);

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("header", Collections.singletonMap("app_id", appId));
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("domain", "Ultra4.0");
        parameter.put("temperature", 0.5);
        parameter.put("max_tokens", 4096);
        payload.put("parameter", Collections.singletonMap("chat", parameter));
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userQuery);
        payload.put("payload", Collections.singletonMap("message", Collections.singletonMap("text", message)));

        // 发送请求
        String url = "https://api.xfyun.cn/v1/service/v1/ai_model/spark_4_0_ultra";
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // 解析响应数据
        if (response.getStatusCode() == HttpStatus.OK) {
            // 提取AI生成的关键词
            return parseKeywordsFromResponse(response.getBody());
        } else {
            return "Error: " + response.getStatusCode();
        }
    }

    private String parseKeywordsFromResponse(String responseBody) {
        // 解析JSON结构
        return "parsed_keywords";
    }
}
