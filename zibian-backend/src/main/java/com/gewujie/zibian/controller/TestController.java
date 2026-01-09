package com.gewujie.zibian.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/token-info")
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", StpUtil.isLogin());
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", StpUtil.getTokenValue());
        result.put("loginId", StpUtil.isLogin() ? StpUtil.getLoginId() : null);
        return result;
    }

    @GetMapping("/login")
    public Map<String, Object> testLogin() {
        StpUtil.login(12345L);
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("loginId", StpUtil.getLoginId());
        result.put("tokenInfo", StpUtil.getTokenInfo());
        return result;
    }
}
