package com.gewujie.zibian.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sign_in_record")
public class SignInRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDateTime signInTime;

    @PrePersist
    public void prePersist() {
        this.signInTime = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getSignInTime() {
        return signInTime;
    }
    
    public void setSignInTime(LocalDateTime signInTime) {
        this.signInTime = signInTime;
    }
}
