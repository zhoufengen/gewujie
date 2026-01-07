package com.gewujie.zibian.service;

import com.gewujie.zibian.model.User;
import com.gewujie.zibian.model.UserAuth;
import com.gewujie.zibian.model.IdentityType;
import com.gewujie.zibian.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.gewujie.zibian.repository.UserAuthRepository userAuthRepository;
    @Autowired
    private com.gewujie.zibian.repository.LoginLogRepository loginLogRepository;
    @Autowired
    private SmsService smsService;

    // Login with Phone Code
    @Transactional
    public User loginByPhone(String phone, String code, String terminal, String ip) {
        if (!smsService.verifyCode(phone, code)) {
            throw new IllegalArgumentException("Invalid or expired code");
        }

        System.out.println("Attempting login with phone: " + phone);

        Optional<UserAuth> existingAuth = userAuthRepository.findByIdentityTypeAndIdentifier(IdentityType.PHONE, phone);

        UserAuth auth;
        if (existingAuth.isPresent()) {
            System.out.println("Found existing user for phone: " + phone);
            auth = existingAuth.get();
        } else {
            System.out.println("Creating new user for phone: " + phone);
            auth = registerUser(IdentityType.PHONE, phone, "User_" + phone.substring(phone.length() - 4));
        }

        User user = auth.getUser();
        System.out.println("User ID: " + user.getId() + ", UUID: " + user.getUuid() + ", Phone: " + user.getPhone());

        boolean needsSave = false;

        // Fix: Generate UUID for existing users without one
        if (user.getUuid() == null) {
            user.setUuid(java.util.UUID.randomUUID().toString());
            System.out.println("Generated UUID for existing user: " + user.getUuid());
            needsSave = true;
        }

        // Update phone number if not set or changed
        if (user.getPhone() == null || !user.getPhone().equals(phone)) {
            user.setPhone(phone);
            needsSave = true;
        }

        // Save if any changes were made
        if (needsSave) {
            user = userRepository.save(user);
            System.out.println(
                    "Saved user - ID: " + user.getId() + ", UUID: " + user.getUuid() + ", Phone: " + user.getPhone());
        }

        recordLogin(user.getId(), IdentityType.PHONE, phone, terminal, ip);
        return user;
    }

    // Login with Third Party (Mock)
    @Transactional
    public User loginByThirdParty(IdentityType type, String openId, String terminal, String ip) {
        UserAuth auth = userAuthRepository.findByIdentityTypeAndIdentifier(type, openId)
                .orElseGet(() -> registerUser(type, openId, type + "_User"));

        User user = auth.getUser();

        // Fix: Generate UUID for existing users without one
        if (user.getUuid() == null) {
            user.setUuid(java.util.UUID.randomUUID().toString());
            user = userRepository.save(user);
            System.out.println("Generated and saved UUID for existing user: " + user.getUuid());
        }

        recordLogin(user.getId(), type, openId, terminal, ip);
        return user;
    }

    @Transactional
    protected UserAuth registerUser(IdentityType type, String identifier, String defaultName) {
        System.out.println("Registering new user - Type: " + type + ", Identifier: " + identifier);

        // Create User
        User user = new User();
        user.setNickname(defaultName);

        // Set phone number if registering via PHONE
        if (IdentityType.PHONE == type) {
            user.setPhone(identifier);
        }

        System.out.println("Before save - UUID: " + user.getUuid());
        userRepository.save(user);
        System.out.println("After save - User ID: " + user.getId() + ", UUID: " + user.getUuid());

        // Create Auth
        UserAuth auth = new UserAuth();
        auth.setUser(user);
        auth.setIdentityType(type);
        auth.setIdentifier(identifier);
        userAuthRepository.save(auth);

        return auth;
    }

    @Transactional
    protected void recordLogin(Long userId, IdentityType type, String identifier, String terminal, String ip) {
        com.gewujie.zibian.model.LoginLog log = new com.gewujie.zibian.model.LoginLog();
        log.setUserId(userId);
        log.setLoginType(type.name());
        log.setIdentifier(identifier);
        log.setTerminal(terminal);
        log.setIp(ip);
        loginLogRepository.save(log);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void subscribe(Long userId, String plan) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsVip(true);
            // user.setPlan(plan); // If we had a plan field
            userRepository.save(user);
        });
    }

    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user has already used their free nickname change
        if (Boolean.TRUE.equals(user.getHasChangedNickname())) {
            throw new RuntimeException("您已使用过免费改名机会");
        }

        // Validate nickname
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }
        if (newNickname.length() > 20) {
            throw new RuntimeException("昵称不能超过20个字符");
        }

        user.setNickname(newNickname.trim());
        user.setHasChangedNickname(true);
        return userRepository.save(user);
    }
}
