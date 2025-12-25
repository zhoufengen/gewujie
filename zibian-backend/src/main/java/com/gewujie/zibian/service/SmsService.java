package com.gewujie.zibian.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class SmsService {

    @Value("${sms.gotone.url:https://app.gotonesms.net/api/v3/sms/send}")
    private String apiUrl;

    @Value("${sms.gotone.token:}")
    private String apiToken;

    @Value("${sms.gotone.senderId:ZiBian}")
    private String senderId;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendVerificationCode(String phone) {
        String code = generateCode();

        // Save to Redis: 5 minutes TTL
        redisTemplate.opsForValue().set("SMS:" + phone, code, Duration.ofMinutes(5));

        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("[SMS DEBUG] Token is empty, using Mock Mode.");
            System.out.println("[MOCK SMS] To: " + phone + ", Code: " + code);
            return true;
        }

        System.out
                .println("[SMS DEBUG] Token present: " + apiToken.substring(0, Math.min(5, apiToken.length())) + "***");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            Map<String, String> body = new HashMap<>();
            body.put("recipient", phone);
            body.put("sender_id", senderId);
            body.put("type", "plain");
            body.put("message", "您的验证码是：" + code);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            System.out.println("[SMS DEBUG] Sending request to: " + apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            System.out.println("[SMS DEBUG] Response Code: " + response.getStatusCode());
            System.out.println("[SMS DEBUG] Response Body: " + response.getBody());

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.out.println("[SMS DEBUG] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyCode(String phone, String code) {
        // Backdoor for testing
        if ("1234".equals(code))
            return true;

        String cachedCode = redisTemplate.opsForValue().get("SMS:" + phone);
        return code != null && code.equals(cachedCode);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
