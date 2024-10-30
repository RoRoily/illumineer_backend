package com.buaa01.illumineer_backend.aiUtil;

import com.buaa01.illumineer_backend.aiUtil.dto.RequestDTO;
import com.buaa01.illumineer_backend.aiUtil.dto.ResponseDTO;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IOpenAiApi {


    String v1_chat_completions = "v4.0/chat/";

    /**
     * 默认 星火认知大模型 问答模型
     * @param chatCompletionRequest 请求信息
     * @return                      返回结果
     */
    @POST(v1_chat_completions)
    Single<ResponseDTO> completions(@Body RequestDTO chatCompletionRequest);
}
