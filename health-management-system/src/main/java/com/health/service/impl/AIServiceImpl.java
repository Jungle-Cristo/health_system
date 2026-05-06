package com.health.service.impl;

import com.health.ai.AIProvider;
import com.health.ai.AIServiceAdapter;
import com.health.ai.AIServiceFactory;
import com.health.ai.RateLimiter;
import com.health.ai.impl.WenxinAdapter;
import com.health.dto.HealthDataResponse;
import com.health.entity.AIChatMessage;
import com.health.exception.AIServiceException;
import com.health.repository.AIChatMessageRepository;
import com.health.service.AIService;
import com.health.service.HealthDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI服务实现类
 * 支持多种AI服务提供商，集成速率限制和输入验证
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private AIChatMessageRepository aiChatMessageRepository;

    @Autowired
    private AIServiceFactory aiServiceFactory;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private HealthDataService healthDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 当前使用的AI服务提供商（支持运行时切换）
     */
    private final AtomicReference<AIProvider> currentProvider = new AtomicReference<>();

    @Override
    public Map<String, Object> handleChatRequest(Long userId, Map<String, Object> request) {
        String message = (String) request.get("message");
        String chatId = (String) request.get("chatId");
        List<Map<String, Object>> context = (List<Map<String, Object>>) request.get("context");

        try {
            // 1. 输入验证
            rateLimiter.validateInput(message);

            // 2. 速率限制检查
            String clientIp = getClientIp();
            rateLimiter.checkRateLimit(userId, clientIp);

            // 3. 保存用户消息（保存原始消息，保持聊天记录简洁）
            AIChatMessage userMessage = new AIChatMessage();
            userMessage.setUserId(userId);
            userMessage.setChatId(chatId);
            userMessage.setRole("user");
            userMessage.setContent(message);
            userMessage.setTimestamp(LocalDateTime.now());
            aiChatMessageRepository.save(userMessage);

            // 4. 注入用户健康数据上下文后生成AI回复
            String enrichedMessage = enrichWithHealthData(userId, message);
            String aiResponse = generateAIResponseWithRetry(enrichedMessage, context);

            // 5. 保存AI回复
            AIChatMessage aiMessage = new AIChatMessage();
            aiMessage.setUserId(userId);
            aiMessage.setChatId(chatId);
            aiMessage.setRole("assistant");
            aiMessage.setContent(aiResponse);
            aiMessage.setTimestamp(LocalDateTime.now());
            aiChatMessageRepository.save(aiMessage);

            // 6. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("response", aiResponse);
            response.put("chatId", chatId);
            response.put("provider", getCurrentProvider().getName());

            return response;

        } catch (AIServiceException e) {
            log.error("AI服务处理失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getErrorCode().name());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("retryable", e.isRetryable());
            return errorResponse;
        } catch (Exception e) {
            log.error("AI服务处理异常: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "UNKNOWN");
            errorResponse.put("message", "服务器内部错误，请稍后重试");
            errorResponse.put("retryable", true);
            return errorResponse;
        }
    }

    /**
     * 生成AI回复（带重试机制）
     */
    private String generateAIResponseWithRetry(String message, List<Map<String, Object>> context) {
        AIProvider current = getCurrentProvider();
        AIServiceAdapter adapter = aiServiceFactory.getAdapter(current);
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                String contextJson = null;
                if (context != null && !context.isEmpty()) {
                    try {
                        contextJson = objectMapper.writeValueAsString(context);
                    } catch (Exception e) {
                        log.warn("序列化上下文失败: {}", e.getMessage());
                    }
                }

                log.info("正在调用{}处理请求，消息长度: {} 字符", current.getName(), message.length());
                String response = adapter.generateResponse(message, contextJson);
                log.info("{}调用成功，响应长度: {} 字符", current.getName(), response.length());
                return response;

            } catch (AIServiceException e) {
                attempts++;
                log.warn("AI服务调用失败（第{}次尝试），提供商: {}, 错误: {}", attempts, current.getName(), e.getMessage());

                if (!e.isRetryable() || attempts >= maxRetries) {
                    // 如果是API Key无效或认证失败，不进行降级，直接抛出错误提示用户
                    if (e.getErrorCode() == AIServiceException.ErrorCode.INVALID_API_KEY) {
                        log.error("{} API Key无效或已过期，请检查配置", current.getName());
                        throw new AIServiceException(AIServiceException.ErrorCode.INVALID_API_KEY, 
                            current.getName() + " API Key无效或已过期，请检查配置文件中的API Key");
                    }
                    
                    // 其他可降级的错误，降级到模拟模式并给出提示
                    log.warn("降级到模拟模式");
                    adapter = aiServiceFactory.getAdapter(AIProvider.MOCK);
                    try {
                        String mockResponse = adapter.generateResponse(message, null);
                        // 在响应前添加降级提示
                        return "[当前" + current.getName() + "服务暂时不可用，已自动切换到模拟模式]\n\n" + mockResponse;
                    } catch (Exception ex) {
                        throw new AIServiceException(AIServiceException.ErrorCode.SERVER_ERROR, ex);
                    }
                }

                try {
                    Thread.sleep((long) Math.pow(2, attempts) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AIServiceException(AIServiceException.ErrorCode.SERVER_ERROR, ie);
                }
            }
        }

        throw new AIServiceException(AIServiceException.ErrorCode.SERVER_ERROR);
    }

    private String enrichWithHealthData(Long userId, String originalMessage) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            List<HealthDataResponse> allData = healthDataService.getHealthDataList(
                    null, startDate.format(fmt), endDate.format(fmt));

            if (allData == null || allData.isEmpty()) {
                return originalMessage;
            }

            String[] orderTypes = {"steps", "heart_rate", "sleep",
                    "weight", "blood_pressure", "blood_sugar"};
            Map<String, List<Double>> typeValues = new LinkedHashMap<>();
            for (String t : orderTypes) {
                typeValues.put(t, new ArrayList<>());
            }

            for (HealthDataResponse hd : allData) {
                List<Double> values = typeValues.get(hd.getType());
                if (values != null) {
                    values.add(hd.getValue());
                }
            }

            Map<String, String> typeLabels = Map.of(
                    "steps", "步数", "heart_rate", "心率", "sleep", "睡眠时长",
                    "weight", "体重", "blood_pressure", "血压", "blood_sugar", "血糖"
            );
            Map<String, String> typeUnits = Map.of(
                    "steps", "步", "heart_rate", "bpm", "sleep", "小时",
                    "weight", "kg", "blood_pressure", "mmHg", "blood_sugar", "mmol/L"
            );

            StringBuilder sb = new StringBuilder();
            sb.append("[用户近期健康数据摘要]\n");
            sb.append("以下是该用户过去30天的真实健康数据统计：\n");

            for (String t : orderTypes) {
                List<Double> values = typeValues.get(t);
                if (values.isEmpty()) continue;

                DoubleSummaryStatistics stats = values.stream()
                        .mapToDouble(Double::doubleValue).summaryStatistics();
                double latest = values.get(0);

                sb.append("- ").append(typeLabels.getOrDefault(t, t))
                        .append(": 最新=").append(String.format("%.1f", latest))
                        .append(typeUnits.getOrDefault(t, ""))
                        .append(", 平均=").append(String.format("%.1f", stats.getAverage()))
                        .append(typeUnits.getOrDefault(t, ""))
                        .append(", 最高=").append(String.format("%.1f", stats.getMax()))
                        .append(", 最低=").append(String.format("%.1f", stats.getMin()))
                        .append("\n");
            }

            sb.append("\n请基于以上用户的真实健康数据，分析其健康状况，识别潜在风险，");
            sb.append("并针对异常指标提供具体的改善建议。如果用户的问题与数据相关，请结合数据回答。\n\n");
            sb.append("[用户问题]\n");
            sb.append(originalMessage);

            return sb.toString();
        } catch (Exception e) {
            log.warn("注入健康数据失败，使用原始消息: {}", e.getMessage());
            return originalMessage;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String ip = req.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.getRemoteAddr();
                }
                return ip != null ? ip : "127.0.0.1";
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    @Override
    public List<AIChatMessage> getChatHistory(Long userId) {
        return aiChatMessageRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    public void clearChatHistory(Long userId) {
        aiChatMessageRepository.deleteByUserId(userId);
    }

    @Override
    public String generateAIResponse(String message, List<Map<String, Object>> context) {
        try {
            AIServiceAdapter adapter = aiServiceFactory.getAdapter(getCurrentProvider());
            String contextJson = null;
            if (context != null && !context.isEmpty()) {
                contextJson = objectMapper.writeValueAsString(context);
            }
            return adapter.generateResponse(message, contextJson);
        } catch (AIServiceException e) {
            log.error("AI回复生成失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI回复生成异常: {}", e.getMessage(), e);
            throw new AIServiceException(AIServiceException.ErrorCode.UNKNOWN, e);
        }
    }

    @Override
    public AIProvider getCurrentProvider() {
        AIProvider provider = currentProvider.get();
        if (provider == null) {
            provider = aiServiceFactory.getDefaultProvider();
            currentProvider.set(provider);
        }

        if (!aiServiceFactory.isProviderAvailable(provider)) {
            log.warn("配置的提供商 {} 不可用，降级到MOCK模式", provider.getName());
            return AIProvider.MOCK;
        }

        return provider;
    }

    @Override
    public void switchProvider(AIProvider provider) {
        AIServiceAdapter adapter = aiServiceFactory.getAdapter(provider);
        
        // 检查适配器是否可用
        if (!adapter.isAvailable()) {
            log.warn("AI服务提供商 {} 不可用，保持当前提供商", provider.getName());
            throw new IllegalArgumentException("AI服务提供商 " + provider.getName() + " 不可用");
        }
        
        // 对于文心一言，检查配置是否完整（根据千帆API文档，只需要API Key）
        if (provider == AIProvider.BAIDU_WENXIN) {
            WenxinAdapter wenxinAdapter = (WenxinAdapter) adapter;
            if (!wenxinAdapter.hasCompleteConfig()) {
                log.warn("文心一言API Key未配置");
                throw new IllegalArgumentException("文心一言API Key未配置，请在配置文件中设置WENXIN_API_KEY环境变量");
            }
        }
        
        currentProvider.set(provider);
        log.info("AI服务提供商已切换为: {}", provider.getName());
    }

    @Override
    public List<AIProvider> getAvailableProviders() {
        return aiServiceFactory.getAvailableProviders();
    }
}