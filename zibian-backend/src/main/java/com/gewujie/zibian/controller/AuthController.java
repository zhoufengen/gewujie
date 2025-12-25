package com.gewujie.zibian.controller;

import com.gewujie.zibian.dto.LoginRequest;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.service.UserService;
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
        return userService.loginByThirdParty("WECHAT", openId, "H5", servletRequest.getRemoteAddr());
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestParam Long userId, @RequestParam String plan) {
        userService.subscribe(userId, plan);
    }
}
