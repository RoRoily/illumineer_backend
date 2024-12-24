package com.buaa01.illumineer_backend.controller.auth;

import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.entity.UserInfo;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

        @Value("${orcid.clientId}")
        private String clientId;

        @Value("${orcid.clientSecret}")
        private String clientSecret;

        @Value("${orcid.redirectUri}")
        private String redirectUri;

        private String authorizeUrl = "https://orcid.org/oauth/authorize";


        //用于从ORCID服务器获取访问令牌
        @Value("${orcid.tokenUrl}")
        private String tokenUrl ;

        @Value("${orcid.userInfoUrl}")
        private String userInfoUrl;

        @Autowired
        private UserService userService;

        /**
         * 引导用户跳转到 ORCID 授权页面
         */
        @GetMapping("/orcid")
        public ResponseEntity<Void> redirectToOrcidAuth() {
            System.out.println("127.0.0.1:8091/auth/orcid");
            String url = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s",
                    authorizeUrl, clientId, redirectUri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        /**
         * ORCID 授权回调接口，处理用户授权后的返回
         * @param code 使用code去ORCID服务器换取Access Token，使用Access Token获取用户信息
         */
        /**
        @GetMapping("/orcid/callback")
        public ResponseEntity<String> orcidCallback(@RequestParam("code") String code) {
            // 1. 使用 Authorization Code 获取 Access Token
            //构建HTTPS请求的工具类
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            System.out.println("Return to 127.0.0.1/auth/orcid code = " + code);
            //构建POST请求的表单参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", "http://127.0.0.1:8091/auth/orcid/callback");

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


            String userInfo = userInfoResponse.getBody();
            System.out.println(userInfo);
            //System.out.println(userInfoResponse);
            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user info");
            }

            // 返回用户信息
            return ResponseEntity.ok(userInfoResponse.getBody());
        }**/

        /**
    @GetMapping("/orcid/callback")
    public ResponseEntity<String> orcidCallback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: 获取 Access Token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get access token");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");
        String orcid = (String) tokenResponse.getBody().get("orcid");

        // Step 2: 获取用户信息
        String userInfoEndpoint = userInfoUrl.replace("{orcid}", orcid);
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        userInfoHeaders.set("Accept", "application/json");

        HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                userInfoEndpoint, HttpMethod.GET, userInfoRequest, String.class);

        if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user info");
        }

        // 打印用户信息
        System.out.println("User Info: " + userInfoResponse.getBody());

        return ResponseEntity.ok(userInfoResponse.getBody());
    }**/
        @GetMapping("/orcid/callback")
        public ResponseEntity<String> orcidCallback(@RequestParam("code") String code) {
            RestTemplate restTemplate = new RestTemplate();

            // Step 1: 获取 Access Token
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get access token");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");
            String orcid = (String) tokenResponse.getBody().get("orcid");

            // Step 2: 获取用户信息
            String userInfoEndpoint = "https://pub.orcid.org/v3.0/" + orcid;
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            userInfoHeaders.set("Accept", "application/json");

            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoEndpoint, HttpMethod.GET, userInfoRequest, String.class);

            System.out.println(userInfoResponse);

            // 获取响应体
            String responseBody = userInfoResponse.getBody();

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                UserInfo userInfo = objectMapper.readValue(responseBody, UserInfo.class);

                // 提取用户姓名
                String givenName = userInfo.getPerson().getName().getGivenNames().getValue();
                String familyName = userInfo.getPerson().getName().getFamilyName().getValue();
                String fullName = givenName + " " + familyName;

                // 提取所属机构
                UserInfo.EmploymentSummary employmentSummary = userInfo.getActivitiesSummary()
                        .getEmployments().getAffiliationGroups()[0].getSummaries()[0].getEmploymentSummary();
                String organizationName = employmentSummary.getOrganization().getName();

                // 提取所在地
                String city = employmentSummary.getOrganization().getAddress().getCity();
                String country = employmentSummary.getOrganization().getAddress().getCountry();

                // 输出结果
                System.out.println("用户姓名: " + fullName);
                System.out.println("所属机构: " + organizationName);
                System.out.println("所在地: " + city + ',' + country);
                String address = city + "," + country;

                userService.modifyAuthInfo(fullName,organizationName,address);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user info");
            }

            return ResponseEntity.ok(userInfoResponse.getBody());
        }


}

