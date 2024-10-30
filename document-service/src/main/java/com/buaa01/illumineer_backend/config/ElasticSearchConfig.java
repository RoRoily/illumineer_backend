package com.buaa01.illumineer_backend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ElasticSearchConfig {
    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private Integer port;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    private final int MaxConnectionTotal = 100;
    private final int MaxConnectionPerRoute = 50;

    @Bean(destroyMethod = "close")
    //@Deprecated
    public RestClient restClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        //用于设置Elasticsearch的身份验证，使用了用户名和密码
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return RestClient.builder(new HttpHost(host,port,"http"))
                .setHttpClientConfigCallback(httpClientBuilder->httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider) // 身份验证
                        .setMaxConnTotal(MaxConnectionTotal)//设置最大连接数
                        .setMaxConnPerRoute(MaxConnectionPerRoute)//设置每个路由的最大连接数
                        .setKeepAliveStrategy((response, context) -> Duration.ofMinutes(5).toMillis())//http保活策略，将连接保持活跃的时间设置为5分钟。
                        .setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build())// 开启tcp keepalive,确保长时间未使用的连接不会被服务器关闭。
                ).build();
    }

    @Bean(destroyMethod = "close")
    //这是基于RestClient的Elasticsearch传输客户端，用于与Elasticsearch集群进行交互。
    public ElasticsearchTransport transport() {
        //使用Jackson作为JSON解析库，将对象映射为JSON格式，这是Elasticsearch与客户端之间通信的主要数据格式。
        return new RestClientTransport(restClient(), new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        //这是Elasticsearch Java客户端，提供了与Elasticsearch服务器交互的高级API，应用程序通过这个客户端发送搜索、索引等请求。
        return new ElasticsearchClient(transport());
    }
}
