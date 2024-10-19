package com.buaa01.illumineer_backend.aiUtil.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 请求参数
 */

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class RequestDTO {

    @JsonProperty("header")
    private HeaderDTO header;
    @JsonProperty("parameter")
    private ParameterDTO parameter;
    @JsonProperty("payload")
    private PayloadDTO payload;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class HeaderDTO {
        /**
         * 应用appid，从开放平台控制台创建的应用中获取
         */
        @JSONField(name = "app_id")
        private String appId;
        /**
         * 每个用户的id，用于区分不同用户，最大长度32
         */
        @JSONField(name = "uid")
        private String uid;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ParameterDTO {
        private ChatDTO chat;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ChatDTO {
            /**
             * 指定访问的领域,4.0Ultra指向V4.0版本！
             */
            @JsonProperty("domain")
            private String domain = "4.0Ultra";
            /**
             * 核采样阈值。用于决定结果随机性，取值越高随机性越强即相同的问题得到的不同答案的可能性越高
             */
            @JsonProperty("temperature")
            private Float temperature = 0.5F;
            /**
             * 模型回答的tokens的最大长度
             */
            @JSONField(name = "max_tokens")
            private Integer maxTokens = 2048;
        }
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class PayloadDTO {
        @JsonProperty("message")
        private MessageDTO message;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class MessageDTO {
            @JsonProperty("text")
            private List<MsgDTO> text;
            // 这里应该是发送的信息（组成的一个列表）
        }
    }
}
