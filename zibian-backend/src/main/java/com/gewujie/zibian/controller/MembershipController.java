package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.SubscriptionOrder;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    @PostMapping("/order")
    public ResponseEntity<SubscriptionOrder> createOrder(@RequestParam String type) {
        User.UserType userType;
        try {
            userType = User.UserType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(membershipService.createOrder(StpUtil.getLoginIdAsLong(), userType));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payOrder(@RequestParam String orderId, @RequestParam String paymentMethod) {
        try {
            User user = membershipService.processPayment(orderId, paymentMethod);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionOrder>> getOrderHistory() {
        return ResponseEntity.ok(membershipService.getOrderHistory(StpUtil.getLoginIdAsLong()));
    }
}
