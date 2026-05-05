package com.health.ai.impl;

import com.health.ai.AIConfig;
import com.health.ai.AIProvider;
import com.health.ai.AIServiceAdapter;
import com.health.exception.AIServiceException;
import com.health.exception.AIServiceException.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI服务适配器
 */
@Slf4j
@Component
public class OpenAIAdapter implements AIServiceAdapter {
    
    private final AIConfig.OpenAIConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OpenAIAdapter(AIConfig aiConfig) {
        this.config = aiConfig.getOpenai();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public AIProvider getProvider() {
        return AIProvider.OPENAI;
    }
    
    @Override
    public String generateResponse(String message, String context) throws AIServiceException {
        // 验证API密钥
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new AIServiceException(ErrorCode.INVALID_API_KEY);
        }
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("temperature", config.getTemperature());
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的健康顾问AI助手，请用简洁明了的语言回答用户的健康问题。");
            messages.add(systemMessage);
            
            // 添加上下文消息（如果有）
            if (context != null && !context.isEmpty()) {
                try {
                    List<Map<String, Object>> contextMessages = objectMapper.readValue(context, List.class);
                    for (Map<String, Object> ctxMsg : contextMessages) {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", (String) ctxMsg.get("role"));
                        msg.put("content", (String) ctxMsg.get("content"));
                        messages.add(msg);
                    }
                } catch (Exception e) {
                    log.warn("解析上下文失败，忽略上下文: {}", e.getMessage());
                }
            }
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            
            // 构建请求
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .timeout(java.time.Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 处理响应
            return parseResponse(response);
            
        } catch (AIServiceException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            log.error("OpenAI请求超时: {}", e.getMessage());
            throw new AIServiceException(ErrorCode.TIMEOUT, e);
        } catch (java.io.IOException e) {
            log.error("OpenAI请求IO异常: {}", e.getMessage());
            throw new AIServiceException(ErrorCode.SERVICE_UNAVAILABLE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("OpenAI请求被中断: {}", e.getMessage());
            throw new AIServiceException(ErrorCode.SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            log.error("OpenAI请求异常: {}", e.getMessage(), e);
            throw new AIServiceException(ErrorCode.UNKNOWN, e);
        }
    }
    
    /**
     * 解析OpenAI响应
     */
    private String parseResponse(HttpResponse<String> response) throws Exception {
        int statusCode = response.statusCode();
        
        if (statusCode == 200) {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    return message.get("content").trim();
                }
            }
            throw new AIServiceException(ErrorCode.UNKNOWN, "OpenAI返回空响应");
        } else if (statusCode == 401 || statusCode == 403) {
            throw new AIServiceException(ErrorCode.INVALID_API_KEY);
        } else if (statusCode == 429) {
            throw new AIServiceException(ErrorCode.RATE_LIMITED);
        } else if (statusCode >= 500) {
            throw new AIServiceException(ErrorCode.SERVICE_UNAVAILABLE);
        } else {
            // 尝试解析错误消息
            try {
                Map<String, Object> errorBody = objectMapper.readValue(response.body(), Map.class);
                String errorMessage = (String) errorBody.get("error");
                if (errorMessage != null && errorMessage.contains("content")) {
                    throw new AIServiceException(ErrorCode.CONTENT_FILTERED, errorMessage);
                }
            } catch (Exception ignored) {
            }
            throw new AIServiceException(ErrorCode.UNKNOWN, "OpenAI返回错误状态码: " + statusCode);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return config.getApiKey() != null && !config.getApiKey().isEmpty();
    }
}