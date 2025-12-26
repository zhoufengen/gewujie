package com.gewujie.zibian.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

class DictionaryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private ObjectMapper objectMapper;

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        dictionaryService = new DictionaryService(restTemplateBuilder, objectMapper);
    }

    @Test
    void testServiceCreation() {
        assertNotNull(dictionaryService, "DictionaryService should be created successfully");
    }

    // 注意：这个测试需要实际的API密钥才能运行
    // @Test
    // void testGetCharacterInfo() {
    //     // 测试一个简单的汉字
    //     String[] result = dictionaryService.getCharacterInfo("好");
    //     assertNotNull(result, "Result should not be null");
    //     assertArrayEquals(new String[]{}, result, "Result should be an array of length 3");
    // }
}
