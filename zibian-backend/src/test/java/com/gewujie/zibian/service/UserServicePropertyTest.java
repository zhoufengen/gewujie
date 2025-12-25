package com.gewujie.zibian.service;

import com.gewujie.zibian.model.User;
import com.gewujie.zibian.model.UserAuth;
import com.gewujie.zibian.repository.LoginLogRepository;
import com.gewujie.zibian.repository.UserAuthRepository;
import com.gewujie.zibian.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
 * Property-based tests for UserService
 * Feature: phone-login-enhancement
 */
@ExtendWith(MockitoExtension.class)
public class UserServicePropertyTest {

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
     * Feature: phone-login-enhancement, Property 3: Phone number storage on registration
     * For any valid phone number used during registration, the resulting user record 
     * should have that phone number stored in the users table
     * Validates: Requirements 2.1
     */
    @Test
    public void testPhoneNumberStorageOnRegistration() {
        // Test with multiple phone numbers
        String[] testPhones = {
            "13800138000", "13900139000", "15800158000",
            "17800178000", "18800188000", "19900199000"
        };
        
        for (String phone : testPhones) {
            // Setup mocks
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            
            when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
                UserAuth auth = invocation.getArgument(0);
                auth.setId(1L);
                return auth;
            });
            
            // Register user with phone
            UserAuth auth = userService.registerUser("PHONE", phone, "TestUser");
            
            // Verify phone is set on user
            assertNotNull("User should be created", auth.getUser());
            assertEquals("Phone should be stored in user record", 
                        phone, auth.getUser().getPhone());
            
            // Verify user was saved
            verify(userRepository, atLeastOnce()).save(argThat(user -> 
                phone.equals(user.getPhone())
            ));
            
            // Reset mocks for next iteration
            reset(userRepository, userAuthRepository);
        }
    }

    /**
     * Feature: phone-login-enhancement, Property 4: Phone number update on login
     * For any existing user, if they login with a different phone number than 
     * currently stored, the stored phone number should be updated to the new value
     * Validates: Requirements 2.2
     */
    @Test
    public void testPhoneNumberUpdateOnLogin() {
        String originalPhone = "13800138000";
        String newPhone = "13900139000";
        
        // Create existing user with original phone
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("TestUser");
        existingUser.setPhone(originalPhone);
        existingUser.onCreate();
        
        UserAuth existingAuth = new UserAuth();
        existingAuth.setId(1L);
        existingAuth.setUser(existingUser);
        existingAuth.setIdentityType("PHONE");
        existingAuth.setIdentifier(newPhone);
        
        // Setup mocks
        when(smsService.verifyCode(newPhone, "123456")).thenReturn(true);
        when(userAuthRepository.findByIdentityTypeAndIdentifier("PHONE", newPhone))
            .thenReturn(Optional.of(existingAuth));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Login with new phone
        User result = userService.loginByPhone(newPhone, "123456", "H5", "127.0.0.1");
        
        // Verify phone was updated
        assertEquals("Phone should be updated to new value", 
                    newPhone, result.getPhone());
        verify(userRepository).save(argThat(user -> 
            newPhone.equals(user.getPhone())
        ));
    }

    /**
     * Feature: phone-login-enhancement, Property 6: Null phone for third-party auth
     * For any user registered via third-party authentication (WECHAT, ALIPAY), 
     * the phone field should be allowed to be null
     * Validates: Requirements 2.4
     */
    @Test
    public void testNullPhoneForThirdPartyAuth() {
        String[] thirdPartyTypes = {"WECHAT", "ALIPAY"};
        String[] openIds = {"wx_test123", "alipay_test456"};
        
        for (int i = 0; i < thirdPartyTypes.length; i++) {
            String type = thirdPartyTypes[i];
            String openId = openIds[i];
            
            // Setup mocks
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId((long) (i + 1));
                return user;
            });
            
            when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> {
                UserAuth auth = invocation.getArgument(0);
                auth.setId((long) (i + 1));
                return auth;
            });
            
            // Register user via third-party
            UserAuth auth = userService.registerUser(type, openId, type + "_User");
            
            // Verify phone is null
            assertNotNull("User should be created", auth.getUser());
            assertNull("Phone should be null for third-party auth", 
                      auth.getUser().getPhone());
            
            // Verify UUID is still generated
            assertNotNull("UUID should be generated even without phone", 
                         auth.getUser().getUuid());
            
            // Reset mocks for next iteration
            reset(userRepository, userAuthRepository);
        }
    }

    /**
     * Test that phone is not updated to null on login
     */
    @Test
    public void testPhoneNotUpdatedToNull() {
        String phone = "13800138000";
        
        // Create existing user with phone
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
        
        // Setup mocks
        when(smsService.verifyCode(phone, "123456")).thenReturn(true);
        when(userAuthRepository.findByIdentityTypeAndIdentifier("PHONE", phone))
            .thenReturn(Optional.of(existingAuth));
        
        // Login with same phone
        User result = userService.loginByPhone(phone, "123456", "H5", "127.0.0.1");
        
        // Verify phone is still set
        assertEquals("Phone should remain unchanged", phone, result.getPhone());
        
        // Verify save was not called (phone didn't change)
        verify(userRepository, never()).save(any(User.class));
    }
}
