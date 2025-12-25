package com.gewujie.zibian.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_logs")
@Data
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String identifier; // Login identifier (phone number, openId, etc.)

    private String terminal; // PC, H5, ANDROID, IOS
    private String ip;
    private String loginType; // PHONE, WECHAT, etc.

    private LocalDateTime loginTime = LocalDateTime.now();
}
