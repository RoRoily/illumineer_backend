package com.buaa01.illumineer_backend.aiUtil.session;

import com.buaa01.illumineer_backend.aiUtil.dto.RequestDTO;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public interface OpenAiSession {
    /**
     * 星火认知大模型
     * @param requestDTO
     * @param
     * @return
     */
    WebSocket completions(RequestDTO requestDTO,  WebSocketListener listener) throws Exception;


    /**
     * 星火认知大模型， 用自己的数据
     * @param requestDTO
     * @param
     * @return
     */
    WebSocket completions(String apiHost, String apiKey, RequestDTO requestDTO,  WebSocketListener listener) throws Exception;

}
