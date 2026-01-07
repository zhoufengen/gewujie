package com.gewujie.zibian.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ThirdPartyAuthService {

    @Value("${app.wechat.app-id}")
    private String wechatAppId;

    @Value("${app.wechat.app-secret}")
    private String wechatAppSecret;

    @Value("${app.alipay.app-id}")
    private String alipayAppId;

    @Value("${app.alipay.private-key}")
    private String alipayPrivateKey;

    @Value("${app.alipay.public-key}")
    private String alipayPublicKey;

    @Value("${app.alipay.gateway-url}")
    private String alipayGatewayUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ThirdPartyAuthService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public String getWeChatOpenId(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                wechatAppId, wechatAppSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("errcode")) {
                throw new RuntimeException("WeChat Login Failed: " + root.get("errmsg").asText());
            }

            return root.get("openid").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get WeChat OpenID", e);
        }
    }

    public String getAlipayOpenId(String authCode) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayGatewayUrl,
                    alipayAppId,
                    alipayPrivateKey,
                    "json",
                    "UTF-8",
                    alipayPublicKey,
                    "RSA2");

            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            request.setCode(authCode);
            request.setGrantType("authorization_code");

            AlipaySystemOauthTokenResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                return response.getUserId();
            } else {
                throw new RuntimeException("Alipay Login Failed: " + response.getSubMsg());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Alipay OpenID", e);
        }
    }
}
