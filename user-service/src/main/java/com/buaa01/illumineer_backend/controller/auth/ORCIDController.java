package com.buaa01.illumineer_backend.controller.auth;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.entity.UserInfo;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.tool.RedisTool;
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
import java.util.concurrent.TimeUnit;


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
    @Autowired
    private RedisTool redisTool;

    /**
         * 引导用户跳转到 ORCID 授权页面
         */
        @GetMapping("/orcid")
        public ResponseEntity<Void> redirectToOrcidAuth() {
            System.out.println("127.0.0.1:8091/auth/orcid");
            //redisTool.setExObjectValue("orcid",uid,120, TimeUnit.SECONDS);
            String url = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s",
                    authorizeUrl, clientId, redirectUri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        @PostMapping("/orcid/sendRedis")
        public void redirectToOrcidAuth1(@RequestParam("uid") Integer uid) {
            System.out.println("11111" + uid);
            //System.out.println("127.0.0.1:8091/auth/orcid");
            redisTool.setExObjectValue("orcid",uid,120, TimeUnit.SECONDS);
            //String url = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s",
                   // authorizeUrl, clientId, redirectUri);
            //return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }


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

                //userService.modifyAuthInfo(fullName,organizationName,address);
                userService.modifyAuthInfoWithRedis(fullName,organizationName,address);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get user info");
            }

            return ResponseEntity.ok(userInfoResponse.getBody());
        }


}

