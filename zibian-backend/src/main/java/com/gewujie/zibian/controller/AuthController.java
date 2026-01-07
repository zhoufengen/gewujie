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

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private ThirdPartyAuthService thirdPartyAuthService;

    @PostMapping("/send-code")
    public void sendCode(@RequestParam String phone) {
        smsService.sendVerificationCode(phone);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        String terminal = "H5"; // In real app, get from User-Agent or header

        return userService.loginByPhone(request.getPhone(), request.getCode(), terminal, ip);
    }

    // Stub for WeChat Login
    @GetMapping("/callback/wechat")
    public User wechatCallback(@RequestParam String code, HttpServletRequest servletRequest) {
        // Exchange code for openId (mock)
        String openId = "wx_" + code;
        return userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.WECHAT, openId, "H5",
                servletRequest.getRemoteAddr());
    }

    @PostMapping("/login/wechat")
    public User loginWeChat(@RequestBody AuthRequests.WeChatLoginRequest request,
            HttpServletRequest servletRequest) {
        String openId = thirdPartyAuthService.getWeChatOpenId(request.getCode());
        return userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.WECHAT, openId, "H5",
                servletRequest.getRemoteAddr());
    }

    @PostMapping("/login/alipay")
    public User loginAlipay(@RequestBody AuthRequests.AlipayLoginRequest request,
            HttpServletRequest servletRequest) {
        String openId = thirdPartyAuthService.getAlipayOpenId(request.getAuthCode());
        return userService.loginByThirdParty(com.gewujie.zibian.model.IdentityType.ALIPAY, openId, "H5",
                servletRequest.getRemoteAddr());
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestParam Long userId, @RequestParam String plan) {
        userService.subscribe(userId, plan);
    }

    @PostMapping("/update-nickname")
    public User updateNickname(@RequestParam Long userId, @RequestParam String nickname) {
        return userService.updateNickname(userId, nickname);
    }
}
