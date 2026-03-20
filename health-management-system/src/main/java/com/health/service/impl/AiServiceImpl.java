package com.health.service.impl;

import com.health.dto.AiChatRequest;
import com.health.dto.AiChatResponse;
import com.health.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private static final String DISCLAIMER = "免责声明：本AI提供的健康建议仅供参考，不能替代专业医生的诊断和治疗。如有不适，请及时就医。";

    private static final Map<String, String> HEALTH_RESPONSES = new HashMap<>();

    static {
        HEALTH_RESPONSES.put("头痛", "根据您描述的头痛症状，建议您：1. 保持充足睡眠；2. 减少压力；3. 避免长时间使用电子设备；4. 如症状持续，请咨询医生。");
        HEALTH_RESPONSES.put("失眠", "改善睡眠的建议：1. 保持规律的作息时间；2. 睡前避免使用手机；3. 保持卧室安静、黑暗；4. 适当运动，但避免睡前剧烈运动。");
        HEALTH_RESPONSES.put("感冒", "感冒护理建议：1. 多休息；2. 多喝水；3. 注意保暖；4. 如症状加重或持续超过一周，请就医。");
        HEALTH_RESPONSES.put("运动", "运动建议：1. 每周至少进行150分钟中等强度有氧运动；2. 力量训练每周2-3次；3. 运动前热身，运动后拉伸；4. 循序渐进，避免过度运动。");
        HEALTH_RESPONSES.put("饮食", "健康饮食建议：1. 多吃蔬菜水果；2. 控制油盐糖摄入；3. 保证蛋白质摄入；4. 饮食多样化，营养均衡。");
        HEALTH_RESPONSES.put("压力", "缓解压力建议：1. 深呼吸和冥想；2. 规律运动；3. 保持社交联系；4. 合理安排时间，避免过度劳累。");
        HEALTH_RESPONSES.put("疲劳", "缓解疲劳的建议：1. 保证充足的睡眠（7-9小时）；2. 均衡饮食，确保摄入足够的营养；3. 适当运动，增强体质；4. 合理安排工作和休息时间，避免过度劳累。");
        HEALTH_RESPONSES.put("焦虑", "缓解焦虑的建议：1. 深呼吸练习；2. 冥想和放松训练；3. 规律运动；4. 保持社交联系，寻求支持；5. 如症状严重，建议咨询专业心理医生。");
        HEALTH_RESPONSES.put("消化", "改善消化的建议：1. 饮食规律，避免暴饮暴食；2. 多吃富含纤维的食物；3. 细嚼慢咽；4. 避免辛辣、油腻食物；5. 保持适量运动。");
        HEALTH_RESPONSES.put("血压", "血压管理建议：1. 定期监测血压；2. 低盐饮食；3. 规律运动；4. 保持健康体重；5. 避免吸烟和过量饮酒；6. 如血压持续异常，应就医并遵循医嘱。");
        HEALTH_RESPONSES.put("血糖", "血糖管理建议：1. 控制碳水化合物摄入；2. 规律饮食，避免暴饮暴食；3. 适量运动；4. 定期监测血糖；5. 如血糖异常，应就医并遵循医嘱。");
    }

    @Value("${glm.api-key}")
    private String glmApiKey;

    @Value("${glm.api-url}")
    private String glmApiUrl;

    @Value("${glm.model}")
    private String glmModel;

    @Value("${glm.temperature}")
    private double glmTemperature;

    @Value("${glm.max-tokens}")
    private int glmMaxTokens;

    @Value("${glm.timeout}")
    private int glmTimeout;

    @Value("${glm.retry-count}")
    private int glmRetryCount;

    @Value("${glm.retry-delay}")
    private int glmRetryDelay;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage().trim();
        log.info("Processing AI chat request: {}", message);
        log.debug("GLM API Key configured: {}", isGlmApiConfigured());
        log.debug("GLM API Key length: {}", glmApiKey != null ? glmApiKey.length() : 0);

        String answer;
        try {
            if (isGlmApiConfigured()) {
                log.info("Using GLM model for response generation");
                try {
                    answer = generateResponseWithGlm(message, request.getHistory());
                } catch (Exception e) {
                    log.error("GLM API error: {}", e.getMessage());
                    // Fallback to local response if GLM API fails
                    log.info("GLM API failed, using fallback response");
                    answer = generateResponse(message);
                }
            } else {
                log.info("GLM API not configured, using fallback response");
                answer = generateResponse(message);
            }
        } catch (Exception e) {
            log.error("Error processing AI chat request: {}", e.getMessage(), e);
            answer = "抱歉，AI服务暂时不可用，请稍后再试。\n\n错误详情：" + e.getMessage();
        }

        return AiChatResponse.builder()
                .answer(answer)
                .hasDisclaimer(true)
                .disclaimer(DISCLAIMER)
                .build();
    }

    private boolean isGlmApiConfigured() {
        boolean configured = glmApiKey != null && !glmApiKey.isEmpty();
        log.debug("GLM API configured: {}, key: {}", configured, glmApiKey != null ? "******" : "null");
        return configured;
    }

    private String generateResponseWithGlm(String message, List<AiChatRequest.ChatMessage> history) throws Exception {
        log.info("Using GLM model for response generation");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + glmApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", glmModel);
        requestBody.put("temperature", glmTemperature);
        requestBody.put("max_tokens", glmMaxTokens);

        List<Map<String, String>> messages = new ArrayList<>();

        // Add system prompt - updated to handle all types of questions
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个智能助手，可以回答各种类型的问题，包括健康、科学、技术、历史、文化等。请提供准确、相关、有帮助的回答，同时保持适当的内容标准，避免有害或不适当的信息。");
        messages.add(systemMessage);

        // Add history messages
        if (history != null && !history.isEmpty()) {
            for (AiChatRequest.ChatMessage chatMessage : history) {
                Map<String, String> historyMessage = new HashMap<>();
                historyMessage.put("role", chatMessage.getRole());
                historyMessage.put("content", chatMessage.getContent());
                messages.add(historyMessage);
            }
        }

        // Add current user message
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int retryCount = 0;
        while (true) {
            try {
                log.info("GLM API call attempt {}/{}", retryCount + 1, glmRetryCount + 1);
                ResponseEntity<String> response = restTemplate.exchange(
                        glmApiUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                String responseBody = response.getBody();
                log.debug("GLM API response: {}", responseBody);

                JsonNode root = objectMapper.readTree(responseBody);
                
                // Check for error response
                JsonNode success = root.path("success");
                if (!success.isMissingNode() && !success.asBoolean()) {
                    String msg = root.path("msg").asText("Unknown error");
                    throw new Exception("GLM API error: " + msg);
                }
                
                // Try standard OpenAI format
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode messageNode = choices.get(0).path("message");
                    return messageNode.path("content").asText();
                } else {
                    // Try alternative response format
                    JsonNode content = root.path("content");
                    if (!content.isMissingNode()) {
                        return content.asText();
                    }
                    throw new Exception("Invalid GLM API response format. Response: " + responseBody);
                }
            } catch (HttpClientErrorException e) {
                log.error("GLM API error: {}", e.getResponseBodyAsString());
                if (e.getStatusCode().value() == 401) {
                    throw new Exception("Invalid GLM API key. Please check your GLM_API_KEY environment variable.");
                } else if (e.getStatusCode().value() == 403) {
                    throw new Exception("GLM API access forbidden. Please check your API key permissions.");
                } else if (retryCount < glmRetryCount) {
                    retryCount++;
                    log.info("Retrying GLM API call in {}ms...", glmRetryDelay);
                    Thread.sleep(glmRetryDelay);
                } else {
                    throw new Exception("GLM API error: " + e.getMessage());
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                log.error("Error calling GLM API: {}", e.getMessage());
                if (retryCount < glmRetryCount) {
                    retryCount++;
                    log.info("Retrying GLM API call in {}ms...", glmRetryDelay);
                    Thread.sleep(glmRetryDelay);
                } else {
                    throw e;
                }
            }
        }
    }

    private String generateResponse(String message) {
        String lowerMessage = message.toLowerCase();

        // Check for health-related keywords first
        for (Map.Entry<String, String> entry : HEALTH_RESPONSES.entrySet()) {
            if (lowerMessage.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // General responses for non-health questions
        if (lowerMessage.contains("你好") || lowerMessage.contains("嗨") || lowerMessage.contains("哈喽") || lowerMessage.contains("hi") || lowerMessage.contains("hello")) {
            return "你好！我是智能助手，很高兴为您服务。请问有什么可以帮助您的吗？";
        } else if (lowerMessage.contains("名字") || lowerMessage.contains("叫什么")) {
            return "我是智能助手，可以回答各种类型的问题，包括健康、科学、技术、历史、文化等。";
        } else if (lowerMessage.contains("天气") || lowerMessage.contains("气温")) {
            return "抱歉，我无法实时获取天气信息。您可以通过天气预报应用或网站查看当前天气情况。";
        } else if (lowerMessage.contains("时间") || lowerMessage.contains("几点")) {
            return "抱歉，我无法实时获取当前时间。您可以查看设备上的时钟获取当前时间。";
        } else if (lowerMessage.contains("日期") || lowerMessage.contains("今天")) {
            return "抱歉，我无法实时获取当前日期。您可以查看设备上的日历获取当前日期。";
        } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("感谢")) {
            return "不客气！如果您有任何其他问题，随时告诉我。";
        } else if (lowerMessage.contains("再见") || lowerMessage.contains("拜拜")) {
            return "再见！祝您有愉快的一天！";
        } else if (lowerMessage.contains("天空") && lowerMessage.contains("蓝色")) {
            return "天空呈现蓝色是因为瑞利散射现象。当太阳光穿过大气层时，波长较短的蓝光比波长较长的红光更容易被大气中的气体分子散射，因此天空看起来是蓝色的。";
        } else if (lowerMessage.contains("地球") && (lowerMessage.contains("形状") || lowerMessage.contains("圆"))) {
            return "地球是一个两极稍扁、赤道略鼓的椭球体，也被称为'地球椭球体'。虽然从太空看地球几乎是完美的圆形，但实际上它的赤道半径比极半径约长21公里。";
        } else if (lowerMessage.contains("月亮") && (lowerMessage.contains("发光") || lowerMessage.contains("亮"))) {
            return "月亮本身并不发光，它是通过反射太阳光而发亮的。月球表面反射率约为7-12%，因此我们看到的月光实际上是太阳光被月球表面反射后到达地球的光。";
        } else if (lowerMessage.contains("电脑") || lowerMessage.contains("计算机")) {
            return "计算机是一种能够按照程序运行，自动、高速处理海量数据的现代化智能电子设备。它由硬件系统和软件系统组成，广泛应用于科学计算、数据处理、人工智能、娱乐等领域。";
        } else if (lowerMessage.contains("手机") || lowerMessage.contains("智能手机")) {
            return "智能手机是一种具有独立操作系统，可通过安装应用软件、游戏等程序来扩充功能的手机。它具有强大的处理能力，支持多种网络制式，是现代生活中不可或缺的通讯工具。";
        } else if (lowerMessage.contains("历史") || lowerMessage.contains("古代")) {
            return "历史是人类社会发展的记录，包括政治、经济、文化、科技等各个方面。学习历史可以帮助我们了解过去，认识现在，预见未来。如果您有具体的历史问题，请随时告诉我。";
        } else if (lowerMessage.contains("文化") || lowerMessage.contains("艺术")) {
            return "文化是人类在社会历史发展过程中所创造的物质财富和精神财富的总和，包括语言、文学、艺术、宗教、习俗等。不同国家和民族有各自独特的文化传统。";
        } else if (lowerMessage.contains("科学") || lowerMessage.contains("技术")) {
            return "科学是通过观察、实验、推理等方法探索自然规律的知识体系，技术则是应用科学知识解决实际问题的手段。科学技术的发展推动了人类社会的进步。";
        } else {
            return "感谢您的提问。我是一个智能助手，可以回答各种类型的问题，包括健康、科学、技术、历史、文化等。\n\n如果您有具体的问题，请随时告诉我，我会尽力为您提供准确、相关、有帮助的回答。";
        }
    }
}
