package com.buaa01.illumineer_backend.service;

import com.alibaba.fastjson.JSONObject;
import com.buaa01.illumineer_backend.aiUtil.dto.MsgDTO;
import com.buaa01.illumineer_backend.aiUtil.dto.RequestDTO;
import com.buaa01.illumineer_backend.aiUtil.dto.ResponseDTO;
import com.buaa01.illumineer_backend.aiUtil.session.Configuration;
import com.buaa01.illumineer_backend.aiUtil.session.OpenAiSession;
import com.buaa01.illumineer_backend.aiUtil.session.OpenAiSessionFactory;
import com.buaa01.illumineer_backend.aiUtil.session.defaults.DefaultOpenAiSessionFactory;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class AIAssistantService {
    private final String appId;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public AIAssistantService(
                              @Value("${ai-assistant.appId}") String appId,
                              @Value("${ai-assistant.apiKey}") String apiKey,
                              @Value("${ai-assistant.apiSecret}") String apiSecret) {
        this.appId = appId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        OpenAiSessionFactory();
    }
    private OpenAiSession openAiSession;
    private final CountDownLatch latch = new CountDownLatch(1); // 用于等待

    public void OpenAiSessionFactory() {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setAppId(appId);
        configuration.setApiHost("https://spark-api.xf-yun.com/");
        configuration.setApiSecret(apiKey);
        configuration.setApiKey(apiSecret);
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }

    public RequestDTO createRequest(String question) {
        return RequestDTO
                .builder()
                .header(RequestDTO.HeaderDTO.builder().appId("c3263c59").uid("c3263c59").build())
                .parameter(RequestDTO.ParameterDTO.builder().chat(RequestDTO.ParameterDTO.ChatDTO.builder().domain("4.0Ultra").maxTokens(2048).temperature(0.5F).build()).build())
                .payload(RequestDTO.PayloadDTO.builder().message(RequestDTO.PayloadDTO.MessageDTO.builder()
                                .text(Collections
                                        .singletonList(MsgDTO.builder().role("user").content(question).index(1).build())
                                ).build()
                        ).build()
                ).build();
    }

    /**
     * 【常用对话模式，推荐使用此模型进行测试】
     * 此对话模型 V4.0 接近于官网体验 & 流式应答
     */

    public String StartChat(String content) throws Exception {
        RequestDTO chatCompletion = createRequest(content); // 例如："帮我找一下人工智能相关领域，100字以内回答，请仅提取关键词并用逗号分割，不要输出其余信息。"

        AtomicReference<String> allResponse = new AtomicReference<>("");

        // 3. 发起请求
        WebSocket webSocket = openAiSession.completions(chatCompletion, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                // 将大模型回复的 JSON 文本转为 ResponseDTO 对象
                ResponseDTO responseData = JSONObject.parseObject(text, ResponseDTO.class);
                // 如果响应数据中的 header 的 code 值不为 0，则表示响应错误
                if (responseData.getHeader().getCode() != 0) {
                    return;
                }
                // 将回答进行拼接
                for (MsgDTO msgDTO : responseData.getPayload().getChoices().getText()) {
                    String content = msgDTO.getContent();
                    allResponse.getAndAccumulate(content, (acc, newContent) -> acc + newContent);
                }
                // 检查是否是最后一条消息，如果是，则关闭WebSocket
                if (responseData.getHeader().getStatus() == 2) {
                    webSocket.close(1000, "Complete");
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
            }
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                latch.countDown();
            }
        });
        latch.await();
        // 对字符串进行处理...
        return allResponse.get();
    }
}
//
//package com.buaa01.illumineer_backend.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class AIAssistantService {
//
//    private final RestTemplate restTemplate;
//    private final String appId;
//    private final String apiKey;
//    private final String apiSecret;
//
//    @Autowired
//    public AIAssistantService(RestTemplate restTemplate,
//                              @Value("${ai-assistant.appId}") String appId,
//                              @Value("${ai-assistant.apiKey}") String apiKey,
//                              @Value("${ai-assistant.apiSecret}") String apiSecret) {
//        this.restTemplate = restTemplate;
//        this.appId = appId;
//        this.apiKey = apiKey;
//        this.apiSecret = apiSecret;
//    }
//
//    public String generateKeywords(String userQuery) {
//        // 构建请求头
//        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//        headers.add("Content-Type", "application/json");
//        headers.add("Authorization", "api_key=" + apiKey + ":" + apiSecret);
//
//        // 构建请求体
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("header", Collections.singletonMap("app_id", appId));
//        Map<String, Object> parameter = new HashMap<>();
//        parameter.put("domain", "generalv3");
//        parameter.put("temperature", 0.5);
//        parameter.put("max_tokens", 4096);
//        payload.put("parameter", Collections.singletonMap("chat", parameter));
//        Map<String, String> message = new HashMap<>();
//        message.put("role", "user");
//        message.put("content", userQuery);
//        payload.put("payload", Collections.singletonMap("message", Collections.singletonMap("text", message)));
//
//        // 发送请求
//        String url = "https://api.xfyun.cn/v1/service/v1/ai_model/spark_4_0_ultra";
//        HttpHeaders httpHeaders = new HttpHeaders(headers);
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, httpHeaders);
//        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//
//        // 解析响应数据
//        if (response.getStatusCode() == HttpStatus.OK) {
//            // 提取AI生成的关键词
//            return parseKeywordsFromResponse(response.getBody());
//        } else {
//            return "Error: " + response.getStatusCode();
//        }
//    }
//
//    private String parseKeywordsFromResponse(String responseBody) {
//        // 解析JSON结构
//        return "parsed_keywords";
//    }
//}
