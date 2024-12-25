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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.*;
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
    private CountDownLatch latch = new CountDownLatch(1); // 用于等待

    public void OpenAiSessionFactory() {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setAppId("c3263c59");
        configuration.setApiHost("https://spark-api.xf-yun.com/");
        configuration.setApiSecret("YTYyZTQ3NTI0MmRiODZjZDljMmY1NzZi");
        configuration.setApiKey("9388b96655dc10c634a1b49751c3e950");
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
                    ).build()).build()).build();
    }

    /**
     * 【常用对话模式，推荐使用此模型进行测试】
     * 此对话模型 V4.0 接近于官网体验 & 流式应答
     */

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Async  // 标记该方法为异步执行
    public CompletableFuture<String> StartChat(String content) throws Exception {

        RequestDTO chatCompletion = createRequest(content);
        AtomicReference<String> allResponse = new AtomicReference<>("");

        // 创建并启动 WebSocket
        WebSocket webSocket = openAiSession.completions(chatCompletion, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                // 10秒后检查并关闭 WebSocket
                scheduler.schedule(() -> {
                    latch.countDown();
                }, 10, TimeUnit.SECONDS); // 10秒后执行定时任务
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                // 将大模型回复的 JSON 文本转为 ResponseDTO 对象
                ResponseDTO responseData = JSONObject.parseObject(text, ResponseDTO.class);
                if (responseData.getHeader().getCode() != 0) {
                    webSocket.close(1400, "Failure");
                    return;
                }
                // 拼接回复内容
                for (MsgDTO msgDTO : responseData.getPayload().getChoices().getText()) {
                    String content = msgDTO.getContent();
                    allResponse.getAndAccumulate(content, (acc, newContent) -> acc + newContent);
                }

                // 如果是最后一条消息或接收到空内容，关闭 WebSocket
                if (responseData.getHeader().getStatus() == 2 ||
                        responseData.getPayload().getChoices().getText().isEmpty()) {
                    webSocket.close(1000, "Complete");
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                webSocket.close(1400, "Failure");
                latch.countDown();
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                latch.countDown();
            }
        });

        // 创建 CountDownLatch 等待 WebSocket 关闭
        latch = new CountDownLatch(1);

        // 等待 WebSocket 完成或者超时
        latch.await();
        // 处理响应内容
        return CompletableFuture.completedFuture(allResponse.get());
    }
}