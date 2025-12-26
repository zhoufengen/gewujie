package com.gewujie.zibian.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {

    // RollToolsApi 的基础 URL
    private static final String API_BASE_URL = "https://www.mxnzp.com/api/";
    // 这里需要替换为实际的 API 密钥
    private static final String API_KEY = "tocwp8hmvojnknzv";  // 需要替换为实际API密钥
    private static final String API_SECRET = "f8TXYc1iHrWTsDeoD3TWtAvW9KQ6u4z7";  // 需要替换为实际API密钥
    
    // 调试模式：当为true时，使用模拟数据而不是实际API调用
    private static final boolean DEBUG_MODE = false;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DictionaryService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 获取汉字的字典信息（拼音、释义等）
     * @param character 要查询的汉字
     * @return 包含拼音、释义、词语等信息的数组 [pinyin, definition, words]
     */
    public String[] getCharacterInfo(String character) {
        // 调试模式：使用模拟数据
        if (DEBUG_MODE) {
            System.out.println("DEBUG MODE: Using mock data for character: " + character);
            // 简单的模拟数据生成逻辑
            String mockPinyin = character + "-pinyin";
            String mockDefinition = character + " definition";
            String mockWords = character + "词1," + character + "词2," + character + "词3";
            return new String[]{mockPinyin, mockDefinition, mockWords};
        }
        
        // 生产模式：调用实际API
        try {
            // 构建请求 URL
            String url = API_BASE_URL + "chinese_dictionary/query?app_id={appId}&app_secret={appSecret}&word={word}";
            
            // 设置请求参数
            Map<String, String> params = new HashMap<>();
            params.put("appId", API_KEY);
            params.put("appSecret", API_SECRET);
            params.put("word", character);
            
            System.out.println("Calling API for character: " + character);
            // 发送请求并获取响应
            String response = restTemplate.getForObject(url, String.class, params);
            System.out.println("API Response: " + response);
            
            // 解析 JSON 响应
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            // 检查响应状态
            if (result.containsKey("code") && "1".equals(result.get("code").toString())) {
                // 获取数据部分
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                
                // 提取拼音
                String pinyin = data.getOrDefault("pinyin", "").toString();
                
                // 提取释义
                String definition = data.getOrDefault("explanation", "").toString();
                
                // 提取词语
                String words = "";
                if (data.containsKey("words")) {
                    Object wordsObj = data.get("words");
                    if (wordsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> wordsList = (List<String>) wordsObj;
                        words = String.join(",", wordsList);
                    } else if (wordsObj instanceof String) {
                        // 如果API返回的是字符串格式的词语列表
                        String wordsStr = (String) wordsObj;
                        // 假设格式为"词语1,词语2,词语3"
                        words = wordsStr;
                    }
                } else if (data.containsKey("word") || data.containsKey("ci")) {
                    // 兼容其他可能的词语字段名
                    Object wordsObj = data.containsKey("word") ? data.get("word") : data.get("ci");
                    if (wordsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> wordsList = (List<String>) wordsObj;
                        words = String.join(",", wordsList);
                    } else if (wordsObj instanceof String) {
                        words = (String) wordsObj;
                    }
                }
                
                System.out.println("API Data for " + character + ": [" + pinyin + ", " + definition + ", " + words + "]");
                return new String[]{pinyin, definition, words};
            } else {
                System.err.println("API returned error code: " + result.getOrDefault("code", "unknown"));
                if (result.containsKey("msg")) {
                    System.err.println("API error message: " + result.get("msg"));
                }
            }
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("获取汉字信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 如果出现异常或 API 调用失败，返回默认值
        return new String[]{"", "", ""};
    }
}