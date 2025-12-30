package com.gewujie.zibian.service;

import com.gewujie.zibian.model.SubscriptionOrder;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.repository.SubscriptionOrderRepository;
import com.gewujie.zibian.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MembershipService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionOrderRepository orderRepository;

    public SubscriptionOrder createOrder(Long userId, User.UserType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionOrder order = new SubscriptionOrder();
        order.setUserId(userId);
        order.setOrderId(UUID.randomUUID().toString());
        order.setType(type);
        order.setStatus(SubscriptionOrder.OrderStatus.PENDING);

        // Pricing
        if (type == User.UserType.MONTHLY_VIP) {
            order.setAmount(39.0);
        } else if (type == User.UserType.YEARLY_VIP) {
            order.setAmount(348.0);
        } else {
            throw new IllegalArgumentException("Invalid subscription type");
        }

        return orderRepository.save(order);
    }

    @Transactional
    public User processPayment(String orderId, String paymentMethod) {
        SubscriptionOrder order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (order.getStatus() == SubscriptionOrder.OrderStatus.PAID) {
            return userRepository.findById(order.getUserId()).orElseThrow(); // Already paid
        }

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update Order
        order.setStatus(SubscriptionOrder.OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        orderRepository.save(order);

        // Update User Membership
        updateUserMembership(user, order.getType());
        return userRepository.save(user);
    }

    private void updateUserMembership(User user, User.UserType newType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiration = user.getVipExpirationDate();

        // If user has no expiration or it's in the past, start from now
        if (currentExpiration == null || currentExpiration.isBefore(now)) {
            currentExpiration = now;
        }

        // Calculate new expiration
        if (newType == User.UserType.MONTHLY_VIP) {
            // Add 30 days
            user.setVipExpirationDate(currentExpiration.plusDays(30));
            // If user was NORMAL, set to MONTHLY. If YEARLY, stay YEARLY (upgrade extends
            // time, downgrade extends time but keeps high tier?)
            // Logic: If currently YEARLY, adding MONTHLY just extends time, keep YEARLY
            // status.
            if (user.getUserType() != User.UserType.YEARLY_VIP) {
                user.setUserType(User.UserType.MONTHLY_VIP);
            }
        } else if (newType == User.UserType.YEARLY_VIP) {
            // Add 1 year
            user.setVipExpirationDate(currentExpiration.plusYears(1));
            // Upgrade to YEARLY
            user.setUserType(User.UserType.YEARLY_VIP);
        }
    }

    public List<SubscriptionOrder> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void checkAndExpireMembership(User user) {
        if (user.getUserType() == User.UserType.NORMAL)
            return;

        if (user.getVipExpirationDate() != null && user.getVipExpirationDate().isBefore(LocalDateTime.now())) {
            user.setUserType(User.UserType.NORMAL);
            user.setVipExpirationDate(null);
            userRepository.save(user);
        }
    }
}
