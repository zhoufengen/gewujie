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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController login endpoint
 * Validates: Requirements 3.1, 3.2, 3.3
 */
@WebMvcTest(AuthController.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SmsService smsService;

    /**
     * Test login endpoint returns uuid
     * Validates: Requirements 3.1
     */
    @Test
    public void testLoginEndpointReturnsUuid() throws Exception {
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
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uuid").value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.uuid").isNotEmpty());
    }

    /**
     * Test login endpoint returns phone when available
     * Validates: Requirements 3.2
     */
    @Test
    public void testLoginEndpointReturnsPhoneWhenAvailable() throws Exception {
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
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.phone").value("13800138000"))
            .andExpect(jsonPath("$.phone").isNotEmpty());
    }

    /**
     * Test response format matches expected structure
     * Validates: Requirements 3.3
     */
    @Test
    public void testResponseFormatMatchesExpectedStructure() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUuid("550e8400-e29b-41d4-a716-446655440000");
        mockUser.setNickname("TestUser");
        mockUser.setPhone("13800138000");
        mockUser.setAvatar("http://example.com/avatar.jpg");
        mockUser.setIsVip(true);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
        
        when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockUser);
        
        String requestBody = "{\"phone\":\"13800138000\",\"code\":\"123456\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.uuid").exists())
            .andExpect(jsonPath("$.nickname").exists())
            .andExpect(jsonPath("$.phone").exists())
            .andExpect(jsonPath("$.avatar").exists())
            .andExpect(jsonPath("$.isVip").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        User responseUser = objectMapper.readValue(responseBody, User.class);
        
        // Verify structure
        assertEquals(1L, responseUser.getId());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", responseUser.getUuid());
        assertEquals("TestUser", responseUser.getNickname());
        assertEquals("13800138000", responseUser.getPhone());
        assertEquals("http://example.com/avatar.jpg", responseUser.getAvatar());
        assertTrue(responseUser.getIsVip());
        assertNotNull(responseUser.getCreatedAt());
        assertNotNull(responseUser.getUpdatedAt());
    }

    /**
     * Test login with null phone (third-party user)
     */
    @Test
    public void testLoginWithNullPhone() throws Exception {
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
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uuid").exists())
            .andExpect(jsonPath("$.phone").isEmpty());
    }

    /**
     * Test login returns all required fields for VIP user
     */
    @Test
    public void testLoginReturnsAllFieldsForVipUser() throws Exception {
        User mockUser = new User();
        mockUser.setId(2L);
        mockUser.setUuid("550e8400-e29b-41d4-a716-446655440001");
        mockUser.setNickname("VIPUser");
        mockUser.setPhone("13900139000");
        mockUser.setAvatar("http://example.com/vip-avatar.jpg");
        mockUser.setIsVip(true);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
        
        when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockUser);
        
        String requestBody = "{\"phone\":\"13900139000\",\"code\":\"123456\"}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.uuid").value("550e8400-e29b-41d4-a716-446655440001"))
            .andExpect(jsonPath("$.nickname").value("VIPUser"))
            .andExpect(jsonPath("$.phone").value("13900139000"))
            .andExpect(jsonPath("$.avatar").value("http://example.com/vip-avatar.jpg"))
            .andExpect(jsonPath("$.isVip").value(true));
    }

    /**
     * Test login with invalid request returns error
     */
    @Test
    public void testLoginWithInvalidRequest() throws Exception {
        when(userService.loginByPhone(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid or expired code"));
        
        String requestBody = "{\"phone\":\"13800138000\",\"code\":\"invalid\"}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().is4xxClientError());
    }
}
