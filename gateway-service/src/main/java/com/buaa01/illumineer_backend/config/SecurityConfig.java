package com.buaa01.illumineer_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableWebFluxSecurity
public class SecurityConfig implements WebMvcConfigurer {
    static final String[] ORIGINS = new String[]{"GET", "POST", "PUT", "DELETE","OPTIONS"};
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 保护
                .cors().and()    // 启用 CORS 支持
                .authorizeRequests(authorize -> authorize
                        // 请求放开接口
                        .antMatchers(
                                "/user/user/account/register"
                        )
                        .permitAll()
                        // 允许HTTP OPTIONS请求
                        .antMatchers(HttpMethod.OPTIONS).permitAll()
                        // 其他地址的访问均需验证权限
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许跨域发送cookie
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:8091","http://localhost:9090")); // 允许所有来源
        config.setAllowedHeaders(List.of("*")); // 允许所有请求头
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); // 允许所有方法
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}