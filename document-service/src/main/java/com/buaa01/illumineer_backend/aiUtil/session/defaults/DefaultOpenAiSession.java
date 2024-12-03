package com.buaa01.illumineer_backend.aiUtil.session.defaults;

import com.alibaba.fastjson.JSONObject;
import com.buaa01.illumineer_backend.aiUtil.IOpenAiApi;
import com.buaa01.illumineer_backend.aiUtil.dto.RequestDTO;
import com.buaa01.illumineer_backend.aiUtil.session.Configuration;
import com.buaa01.illumineer_backend.aiUtil.session.OpenAiSession;
import com.buaa01.illumineer_backend.aiUtil.util.AuthUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.sse.EventSource;

/**
 * @author 云深不知处
 */
public class DefaultOpenAiSession implements OpenAiSession {

    /** 配置信息 */
    private final Configuration configuration;

    private final EventSource.Factory factory;

    private final IOpenAiApi openAiApi;

    private static final String V = "v4.0/chat";

    public DefaultOpenAiSession(Configuration configuration) {
        this.configuration = configuration;
        this.openAiApi = configuration.getOpenAiApi();
        this.factory = configuration.createRequestFactory();
    }

    @Override
    public WebSocket completions(RequestDTO chatCompletionRequest,  WebSocketListener listener) throws Exception {
        return this.completions(null, null, chatCompletionRequest,  listener);
    }

    @Override
    public WebSocket completions(String apiHostByUser, String apiKeyByUser, RequestDTO chatCompletionRequest,  WebSocketListener listener) throws Exception {
        // 动态设置 Host、Key，便于用户传递自己的信息
        String apiHost = apiHostByUser == null ? configuration.getApiHost() : apiHostByUser;
        String apiKey = apiKeyByUser == null ? configuration.getApiKey() : apiKeyByUser;
        // 构建请求信息
        String key = AuthUtil.getKey(apiKey, configuration);
        Request request = new Request.Builder().url(key).build();
        // 建立 wss 连接
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        // 发送请求
        webSocket.send(JSONObject.toJSONString(chatCompletionRequest));

        // 返回结果信息
        return webSocket;
    }
}
