package com.health.controller;

import com.health.ai.AIProvider;
import com.health.entity.AIChatMessage;
import com.health.service.AIService;
import com.health.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private JwtUtils jwtUtils;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Long getCurrentUserId() {
        Long userId = jwtUtils.getCurrentUserId();
        return userId != null ? userId : 1L;
    }
    
    /**
     * 处理AI聊天请求
     * @param request 请求参数
     * @return AI回复
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            // 从请求头获取用户ID（实际应用中从JWT解析）
            Long userId = getCurrentUserId();
            
            Map<String, Object> response = aiService.handleChatRequest(userId, request);
            
            // 检查是否有错误
            if (response.containsKey("error")) {
                String errorCode = (String) response.get("error");
                HttpStatus status = HttpStatus.BAD_REQUEST;
                
                // 根据错误类型返回对应的HTTP状态码
                switch (errorCode) {
                    case "INVALID_API_KEY":
                        status = HttpStatus.UNAUTHORIZED;
                        break;
                    case "FORBIDDEN":
                        status = HttpStatus.FORBIDDEN;
                        break;
                    case "RATE_LIMITED":
                        status = HttpStatus.TOO_MANY_REQUESTS;
                        break;
                    case "SERVICE_UNAVAILABLE":
                        status = HttpStatus.SERVICE_UNAVAILABLE;
                        break;
                    case "CONTENT_FILTERED":
                        status = HttpStatus.BAD_REQUEST;
                        break;
                    case "INVALID_INPUT":
                        status = HttpStatus.BAD_REQUEST;
                        break;
                    default:
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                
                return ResponseEntity.status(status).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "SERVER_ERROR");
            errorResponse.put("message", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 处理AI聊天请求（流式模式）
     * @param request 请求参数
     * @return SSE流式响应
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时
        
        executorService.execute(() -> {
            try {
                Long userId = getCurrentUserId();
                
                aiService.handleStreamChatRequest(userId, request, chunk -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(chunk));
                    } catch (IOException e) {
                        log.warn("SSE发送失败: {}", e.getMessage());
                    }
                });
                
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data("done"));
                
                emitter.complete();
                
            } catch (Exception e) {
                log.error("AI流式聊天错误: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
    
    /**
     * 获取聊天历史
     * @return 聊天消息列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<AIChatMessage>> getChatHistory() {
        try {
            Long userId = getCurrentUserId();
            List<AIChatMessage> history = aiService.getChatHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 清空聊天历史
     * @return 响应状态
     */
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearChatHistory() {
        try {
            Long userId = getCurrentUserId();
            aiService.clearChatHistory(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取当前使用的AI服务提供商
     * @return 当前提供商信息
     */
    @GetMapping("/provider/current")
    public ResponseEntity<Map<String, Object>> getCurrentProvider() {
        try {
            AIProvider provider = aiService.getCurrentProvider();
            Map<String, Object> response = new HashMap<>();
            response.put("code", provider.getCode());
            response.put("name", provider.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取所有可用的AI服务提供商
     * @return 可用提供商列表
     */
    @GetMapping("/provider/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableProviders() {
        try {
            List<AIProvider> providers = aiService.getAvailableProviders();
            List<Map<String, Object>> result = providers.stream()
                    .map(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", p.getCode());
                        map.put("name", p.getName());
                        return map;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 切换AI服务提供商
     * @param request 包含provider字段的请求体
     * @return 切换结果
     */
    @PostMapping("/provider/switch")
    public ResponseEntity<Map<String, Object>> switchProvider(@RequestBody Map<String, String> request) {
        try {
            String providerCode = request.get("provider");
            if (providerCode == null || providerCode.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "INVALID_PROVIDER");
                error.put("message", "提供商代码不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            AIProvider provider = AIProvider.fromCode(providerCode);
            aiService.switchProvider(provider);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("provider", provider.getName());
            response.put("message", "AI服务提供商已切换为: " + provider.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI controller error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SWITCH_FAILED");
            error.put("message", "切换提供商失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}