package com.gewujie.zibian.controller;

import com.gewujie.zibian.dto.LoginRequest;
import com.gewujie.zibian.dto.AuthRequests;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.service.UserService;
import com.gewujie.zibian.service.ThirdPartyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gewujie.zibian.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private ThirdPartyAuthService thirdPartyAuthService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/send-code")
    public void sendCode(@RequestParam String phone) {
        smsService.sendVerificationCode(phone);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        String terminal = "H5"; // In real app, get from User-Agent or header

        User user = userService.loginByPhone(request.getPhone(), request.getCode(), terminal, ip);
        StpUtil.login(user.getId());
        user.setToken(StpUtil.getTokenInfo().tokenValue);
        user.setRefreshToken(generateAndStoreRefreshToken(user.getId()));
        return user;
    }

    // Stub for WeChat Login
    @GetMapping("/callback/wechat")
    public User wechatCallback(@RequestParam String code, HttpServletRequest servletRequest) {
        // Exchange code for openId (mock)
        String openId = "wx_" + code;
        User user = userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.WECHAT, openId, "H5",
                servletRequest.getRemoteAddr());
        StpUtil.login(user.getId());
        user.setToken(StpUtil.getTokenInfo().tokenValue);
        user.setRefreshToken(generateAndStoreRefreshToken(user.getId()));
        return user;
    }

    @PostMapping("/login/wechat")
    public User loginWeChat(@RequestBody AuthRequests.WeChatLoginRequest request,
            HttpServletRequest servletRequest) {
        String openId = thirdPartyAuthService.getWeChatOpenId(request.getCode());
        User user = userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.WECHAT, openId, "H5",
                servletRequest.getRemoteAddr());
        StpUtil.login(user.getId());
        user.setToken(StpUtil.getTokenInfo().tokenValue);
        user.setRefreshToken(generateAndStoreRefreshToken(user.getId()));
        return user;
    }

    @PostMapping("/login/alipay")
    public User loginAlipay(@RequestBody AuthRequests.AlipayLoginRequest request,
            HttpServletRequest servletRequest) {
        String openId = thirdPartyAuthService.getAlipayOpenId(request.getAuthCode());
        User user = userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.ALIPAY, openId, "H5",
                servletRequest.getRemoteAddr());
        StpUtil.login(user.getId());
        user.setToken(StpUtil.getTokenInfo().tokenValue);
        user.setRefreshToken(generateAndStoreRefreshToken(user.getId()));
        return user;
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestParam String plan) {
        userService.subscribe(StpUtil.getLoginIdAsLong(), plan);
    }

    @PostMapping("/update-nickname")
    public User updateNickname(@RequestParam String nickname) {
        return userService.updateNickname(StpUtil.getLoginIdAsLong(), nickname);
    }

    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token is required");
        }

        // Find userId by refresh token (scan Redis keys)
        // Better: store mapping in Redis as "refresh:token:{token}" -> userId
        String redisKey = "refresh:token:" + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(redisKey);

        if (userIdStr == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        Long userId = Long.parseLong(userIdStr);

        // Generate new access token
        StpUtil.login(userId);
        String newAccessToken = StpUtil.getTokenInfo().tokenValue;

        return Map.of("token", newAccessToken);
    }

    // Helper method to generate and store refresh token
    private String generateAndStoreRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString();
        // Store refresh token in Redis with 30 days TTL
        String userKey = "refresh:user:" + userId;
        String tokenKey = "refresh:token:" + refreshToken;

        redisTemplate.opsForValue().set(userKey, refreshToken, 30, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), 30, TimeUnit.DAYS);

        return refreshToken;
    }
}
