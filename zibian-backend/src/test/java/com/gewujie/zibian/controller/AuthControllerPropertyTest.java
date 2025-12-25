package com.gewujie.zibian.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.service.SmsService;
import com.gewujie.zibian.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based tests for AuthController API responses
 * Feature: phone-login-enhancement
 */
@WebMvcTest(AuthController.class)
public class AuthControllerPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SmsService smsService;

    /**
     * Feature: phone-login-enhancement, Property 7: Login response completeness
     * For any successful login, the API response should contain all user profile fields 
     * including id, uuid, nickname, phone (if available), avatar, isVip, createdAt, and updatedAt
     * Validates: Requirements 3.1, 3.2, 3.3
     */
    @Test
    public void testLoginResponseCompleteness() throws Exception {
        // Test with multiple user scenarios
        String[][] testCases = {
            {"13800138000", "User1", "550e8400-e29b-41d4-a716-446655440000", "true"},
            {"13900139000", "User2", "550e8400-e29b-41d4-a716-446655440001", "false"},
            {"15800158000", "User3", "550e8400-e29b-41d4-a716-446655440002", "true"}
        };
        
        for (String[] testCase : testCases) {
            String phone = testCase[0];
            String nickname = testCase[1];
            String uuid = testCase[2];
            boolean isVip = Boolean.parseBoolean(testCase[3]);
            
            // Create mock user
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUuid(uuid);
            mockUser.setNickname(nickname);
            mockUser.setPhone(phone);
            mockUser.setIsVip(isVip);
            mockUser.setCreatedAt(LocalDateTime.now());
            mockUser.setUpdatedAt(LocalDateTime.now());
            
            when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockUser);
            
            // Make login request
            String requestBody = String.format("{\"phone\":\"%s\",\"code\":\"123456\"}", phone);
            
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
            
            String responseBody = result.getResponse().getContentAsString();
            User responseUser = objectMapper.readValue(responseBody, User.class);
            
            // Verify all fields are present
            assertNotNull("Response should contain user", responseUser);
            assertNotNull("Response should contain id", responseUser.getId());
            assertNotNull("Response should contain uuid", responseUser.getUuid());
            assertNotNull("Response should contain nickname", responseUser.getNickname());
            assertNotNull("Response should contain phone", responseUser.getPhone());
            assertNotNull("Response should contain isVip", responseUser.getIsVip());
            assertNotNull("Response should contain createdAt", responseUser.getCreatedAt());
            assertNotNull("Response should contain updatedAt", responseUser.getUpdatedAt());
            
            // Verify field values
            assertEquals("UUID should match", uuid, responseUser.getUuid());
            assertEquals("Phone should match", phone, responseUser.getPhone());
            assertEquals("Nickname should match", nickname, responseUser.getNickname());
            assertEquals("VIP status should match", isVip, responseUser.getIsVip());
        }
    }

    /**
     * Feature: phone-login-enhancement, Property 8: JSON serialization completeness
     * For any User entity, serializing it to JSON should include all fields without omission
     * Validates: Requirements 3.4
     */
    @Test
    public void testJsonSerializationCompleteness() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUuid("550e8400-e29b-41d4-a716-446655440000");
        user.setNickname("TestUser");
        user.setPhone("13800138000");
        user.setAvatar("http://example.com/avatar.jpg");
        user.setIsVip(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(user);
        
        // Verify all fields are present in JSON
        assertTrue("JSON should contain id", json.contains("\"id\""));
        assertTrue("JSON should contain uuid", json.contains("\"uuid\""));
        assertTrue("JSON should contain nickname", json.contains("\"nickname\""));
        assertTrue("JSON should contain phone", json.contains("\"phone\""));
        assertTrue("JSON should contain avatar", json.contains("\"avatar\""));
        assertTrue("JSON should contain isVip", json.contains("\"isVip\""));
        assertTrue("JSON should contain createdAt", json.contains("\"createdAt\""));
        assertTrue("JSON should contain updatedAt", json.contains("\"updatedAt\""));
        
        // Deserialize and verify
        User deserializedUser = objectMapper.readValue(json, User.class);
        assertEquals("ID should match", user.getId(), deserializedUser.getId());
        assertEquals("UUID should match", user.getUuid(), deserializedUser.getUuid());
        assertEquals("Nickname should match", user.getNickname(), deserializedUser.getNickname());
        assertEquals("Phone should match", user.getPhone(), deserializedUser.getPhone());
        assertEquals("Avatar should match", user.getAvatar(), deserializedUser.getAvatar());
        assertEquals("VIP status should match", user.getIsVip(), deserializedUser.getIsVip());
    }

    /**
     * Test response includes phone when available
     */
    @Test
    public void testResponseIncludesPhoneWhenAvailable() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUuid("550e8400-e29b-41d4-a716-446655440000");
        mockUser.setNickname("TestUser");
        mockUser.setPhone("13800138000");
        mockUser.setIsVip(false);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
        
        when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockUser);
        
        String requestBody = "{\"phone\":\"13800138000\",\"code\":\"123456\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        
        assertTrue("Response should include phone field", responseBody.contains("\"phone\""));
        assertTrue("Response should include phone value", responseBody.contains("13800138000"));
    }

    /**
     * Test response handles null phone gracefully
     */
    @Test
    public void testResponseHandlesNullPhoneGracefully() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUuid("550e8400-e29b-41d4-a716-446655440000");
        mockUser.setNickname("WeChatUser");
        mockUser.setPhone(null);
        mockUser.setIsVip(false);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
        
        when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockUser);
        
        String requestBody = "{\"phone\":\"13800138000\",\"code\":\"123456\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        User responseUser = objectMapper.readValue(responseBody, User.class);
        
        assertNotNull("Response should contain user", responseUser);
        assertNull("Phone should be null", responseUser.getPhone());
        assertNotNull("UUID should still be present", responseUser.getUuid());
    }
}
