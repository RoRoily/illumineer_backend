package com.buaa01.illumineer_backend.aiUtil.session.defaults;

import com.buaa01.illumineer_backend.aiUtil.IOpenAiApi;
import com.buaa01.illumineer_backend.aiUtil.session.Configuration;
import com.buaa01.illumineer_backend.aiUtil.session.OpenAiSession;
import com.buaa01.illumineer_backend.aiUtil.session.OpenAiSessionFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 云深不知处
 */
public class DefaultOpenAiSessionFactory implements OpenAiSessionFactory {

    private final Configuration configuration;

    public DefaultOpenAiSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }


    @Override
    public OpenAiSession openSession() {
        // 日志配置
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        // 创建http客户端
        OpenAiInterceptor openAiInterceptor = new OpenAiInterceptor(configuration.getApiKey(), configuration);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
//                .addInterceptor(openAiInterceptor)
                .connectTimeout(450, TimeUnit.SECONDS)
                .writeTimeout(450, TimeUnit.SECONDS)
                .readTimeout(450, TimeUnit.SECONDS)
                .build();
        configuration.setOkHttpClient(okHttpClient);
        // 开启openai会话

        IOpenAiApi openAiApi = new Retrofit.Builder()
                .baseUrl(configuration.getApiHost())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build().create(IOpenAiApi.class);
        configuration.setOpenAiApi(openAiApi);

        return new DefaultOpenAiSession(configuration);
    }
}
