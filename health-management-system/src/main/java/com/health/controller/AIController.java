package com.health.controller;

import com.health.entity.AIChatMessage;
import com.health.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * 处理AI聊天请求
     * @param request 请求参数
     * @return AI回复
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            // 从请求头获取用户ID
            // 这里假设用户ID已经通过JWT解析并存储在请求上下文中
            Long userId = 1L; // 临时硬编码，实际应该从JWT中获取
            
            Map<String, Object> response = aiService.handleChatRequest(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取聊天历史
     * @return 聊天消息列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<AIChatMessage>> getChatHistory() {
        try {
            // 从请求头获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中获取
            
            List<AIChatMessage> history = aiService.getChatHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
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
            // 从请求头获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中获取
            
            aiService.clearChatHistory(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
