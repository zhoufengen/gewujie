package com.gewujie.zibian.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_auths")
@Data
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String identityType; // PHONE, WECHAT, ALIPAY

    @Column(nullable = false)
    private String identifier; // The phone number, openId, etc.

    private String credential; // Password hash (if any), or null

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLoginTime;
}
