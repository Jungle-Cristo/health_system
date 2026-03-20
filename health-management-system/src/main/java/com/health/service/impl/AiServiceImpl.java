package com.health.service.impl;

import com.health.dto.AiChatRequest;
import com.health.dto.AiChatResponse;
import com.health.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage().trim();
        log.info("Processing AI chat request: {}", message);

        String answer = generateResponse(message);

        return AiChatResponse.builder()
                .answer(answer)
                .hasDisclaimer(true)
                .disclaimer(DISCLAIMER)
                .build();
    }

    private String generateResponse(String message) {
        String lowerMessage = message.toLowerCase();

        for (Map.Entry<String, String> entry : HEALTH_RESPONSES.entrySet()) {
            if (lowerMessage.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        return "您好！我是健康助手，可以为您提供健康咨询服务。您可以询问关于头痛、失眠、感冒、运动、饮食、压力等方面的问题，我会尽力为您提供建议。";
    }
}
