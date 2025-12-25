package com.gewujie.zibian.service;

import com.gewujie.zibian.model.User;
import com.gewujie.zibian.model.UserAuth;
import com.gewujie.zibian.repository.LoginLogRepository;
import com.gewujie.zibian.repository.UserAuthRepository;
import com.gewujie.zibian.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private UserService userService;

    /**
     * Test registerUser creates user with UUID
     * Validates: Requirements 1.1, 5.1
     */
    @Test
    public void testRegisterUserCreatesUserWithUuid() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            // Simulate @PrePersist
            if (user.getUuid() == null) {
                user.onCreate();
            }
            return user;
        });
        
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
            UserAuth auth = invocation.getArgument(0);
            auth.setId(1L);
            return auth;
        });
        
        UserAuth auth = userService.registerUser("PHONE", "13800138000", "TestUser");
        
        assertNotNull("User should be created", auth.getUser());
        assertNotNull("UUID should be generated", auth.getUser().getUuid());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test registerUser sets phone for PHONE identity
     * Validates: Requirements 2.1
     */
    @Test
    public void testRegisterUserSetsPhoneForPhoneIdentity() {
        String phone = "13800138000";
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.onCreate();
            return user;
        });
        
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
            UserAuth auth = invocation.getArgument(0);
            auth.setId(1L);
            return auth;
        });
        
        UserAuth auth = userService.registerUser("PHONE", phone, "TestUser");
        
        assertEquals("Phone should be set", phone, auth.getUser().getPhone());
    }

    /**
     * Test loginByPhone updates phone correctly
     * Validates: Requirements 2.2
     */
    @Test
    public void testLoginByPhoneUpdatesPhone() {
        String oldPhone = "13800138000";
        String newPhone = "13900139000";
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("TestUser");
        existingUser.setPhone(oldPhone);
        existingUser.onCreate();
        
        UserAuth existingAuth = new UserAuth();
        existingAuth.setId(1L);
        existingAuth.setUser(existingUser);
        existingAuth.setIdentityType("PHONE");
        existingAuth.setIdentifier(newPhone);
        
        when(smsService.verifyCode(newPhone, "123456")).thenReturn(true);
        when(userAuthRepository.findByIdentityTypeAndIdentifier("PHONE", newPhone))
            .thenReturn(Optional.of(existingAuth));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User result = userService.loginByPhone(newPhone, "123456", "H5", "127.0.0.1");
        
        assertEquals("Phone should be updated", newPhone, result.getPhone());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test loginByThirdParty allows null phone
     * Validates: Requirements 2.4
     */
    @Test
    public void testLoginByThirdPartyAllowsNullPhone() {
        String openId = "wx_test123";
        
        when(userAuthRepository.findByIdentityTypeAndIdentifier("WECHAT", openId))
            .thenReturn(Optional.empty());
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.onCreate();
            return user;
        });
        
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
            UserAuth auth = invocation.getArgument(0);
            auth.setId(1L);
            return auth;
        });
        
        User result = userService.loginByThirdParty("WECHAT", openId, "H5", "127.0.0.1");
        
        assertNotNull("User should be created", result);
        assertNull("Phone should be null for third-party auth", result.getPhone());
        assertNotNull("UUID should still be generated", result.getUuid());
    }

    /**
     * Test loginByPhone throws exception for invalid code
     */
    @Test
    public void testLoginByPhoneThrowsExceptionForInvalidCode() {
        String phone = "13800138000";
        String code = "invalid";
        
        when(smsService.verifyCode(phone, code)).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.loginByPhone(phone, code, "H5", "127.0.0.1");
        });
    }

    /**
     * Test loginByPhone creates new user if not exists
     */
    @Test
    public void testLoginByPhoneCreatesNewUserIfNotExists() {
        String phone = "13800138000";
        String code = "123456";
        
        when(smsService.verifyCode(phone, code)).thenReturn(true);
        when(userAuthRepository.findByIdentityTypeAndIdentifier("PHONE", phone))
            .thenReturn(Optional.empty());
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.onCreate();
            return user;
        });
        
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
            UserAuth auth = invocation.getArgument(0);
            auth.setId(1L);
            return auth;
        });
        
        User result = userService.loginByPhone(phone, code, "H5", "127.0.0.1");
        
        assertNotNull("User should be created", result);
        assertEquals("Phone should be set", phone, result.getPhone());
        assertNotNull("UUID should be generated", result.getUuid());
    }

    /**
     * Test loginByPhone does not update phone if same
     */
    @Test
    public void testLoginByPhoneDoesNotUpdatePhoneIfSame() {
        String phone = "13800138000";
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("TestUser");
        existingUser.setPhone(phone);
        existingUser.onCreate();
        
        UserAuth existingAuth = new UserAuth();
        existingAuth.setId(1L);
        existingAuth.setUser(existingUser);
        existingAuth.setIdentityType("PHONE");
        existingAuth.setIdentifier(phone);
        
        when(smsService.verifyCode(phone, "123456")).thenReturn(true);
        when(userAuthRepository.findByIdentityTypeAndIdentifier("PHONE", phone))
            .thenReturn(Optional.of(existingAuth));
        
        User result = userService.loginByPhone(phone, "123456", "H5", "127.0.0.1");
        
        assertEquals("Phone should remain same", phone, result.getPhone());
        verify(userRepository, never()).save(any(User.class));
    }
}
