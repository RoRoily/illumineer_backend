package com.buaa01.illumineer_backend.aiUtil.session;

import com.buaa01.illumineer_backend.aiUtil.IOpenAiApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;



/**

 * @author 云深不知处
 * @description 配置信息
 */
@Getter
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    @Setter
    private IOpenAiApi openAiApi;

    @Setter
    private OkHttpClient okHttpClient;

    @NotNull
    private String appId;

    @NotNull
    private String apiKey;

    private String apiHost;

    //    @NotNull
    private String apiSecret;

    public EventSource.Factory createRequestFactory() {
        return EventSources.createFactory(okHttpClient);
    }

}
