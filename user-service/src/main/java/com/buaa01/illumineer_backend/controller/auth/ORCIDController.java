package com.buaa01.illumineer_backend.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class ORCIDController {

        private String clientId;

        private String clientSecret;


        private String redirectUri;

        private String authorizeUrl;

        private String tokenUrl;

        private String userInfoUrl;

        /**
         * 引导用户跳转到 ORCID 授权页面
         */
        @GetMapping("/orcid")
        public ResponseEntity<Void> redirectToOrcidAuth() {
            String url = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s",
                    authorizeUrl, clientId, redirectUri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        /**
         * ORCID 授权回调接口，处理用户授权后的返回
         * @param code 使用code去ORCID服务器换取Access Token，使用Access Token获取用户信息
         */
        @GetMapping("/orcid/callback")
        public ResponseEntity<String> orcidCallback(@RequestParam("code") String code) {
            // 1. 使用 Authorization Code 获取 Access Token
            //构建HTTPS请求的工具类
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            //构建POST请求的表单参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", "http://localhost:8080/auth/orcid/callback");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            //请求成功：解析Access Token和orcid ID
            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get access token");
            }

            String accessToken = (String) response.getBody().get("access_token");
            String orcid = (String) response.getBody().get("orcid");

            // 2. 使用 Access Token 获取用户的 ORCID 信息
            //构建新的请求
            String userInfoEndpoint = userInfoUrl.replace("{orcid}", orcid);
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);

            HttpEntity<String> userInfoRequest = new HttpEntity<>(authHeaders);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoEndpoint, HttpMethod.GET, userInfoRequest, String.class);

            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user info");
            }

            // 返回用户信息
            return ResponseEntity.ok(userInfoResponse.getBody());
        }
    }

