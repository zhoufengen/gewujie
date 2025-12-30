package com.gewujie.zibian.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", unique = true, nullable = true, length = 36)
    private String uuid;

    // Basic Profile Info
    private String nickname;

    @Column(length = 20)
    private String phone;

    private String avatar;

    // VIP Type: NORMAL, MONTHLY_VIP, YEARLY_VIP
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.NORMAL;

    private LocalDateTime vipExpirationDate;

    // Deprecated: Use userType instead
    private Boolean isVip = false;

    // User types enum
    public enum UserType {
        NORMAL, // 普通用户
        MONTHLY_VIP, // 包月VIP
        YEARLY_VIP // 包年VIP
    }

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            System.out.println("Generated UUID: " + uuid + " for user: " + nickname);
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
        // Update isVip for backward compatibility
        this.isVip = userType != UserType.NORMAL;
    }

    public Boolean getIsVip() {
        // Deprecated: Use getUserType() instead
        return isVip;
    }

    public void setIsVip(Boolean isVip) {
        // Deprecated: Use setUserType() instead
        this.isVip = isVip;
        // Only update userType if changing from non-VIP to VIP
        if (isVip && this.userType == UserType.NORMAL) {
            this.userType = UserType.MONTHLY_VIP;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getVipExpirationDate() {
        return vipExpirationDate;
    }

    public void setVipExpirationDate(LocalDateTime vipExpirationDate) {
        this.vipExpirationDate = vipExpirationDate;
    }
}
