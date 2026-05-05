package com.health.ai.impl;

import com.health.ai.AIProvider;
import com.health.ai.AIServiceAdapter;
import com.health.exception.AIServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模拟AI服务适配器
 * 保持原有基于关键词匹配的回复逻辑，用于演示和测试
 */
@Slf4j
@Component
public class MockAIAdapter implements AIServiceAdapter {
    
    @Override
    public AIProvider getProvider() {
        return AIProvider.MOCK;
    }
    
    @Override
    public String generateResponse(String message, String context) throws AIServiceException {
        log.info("使用模拟AI服务处理请求: {}", message);
        
        // 简单的模拟AI回复逻辑（与原实现保持一致）
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("睡眠")) {
            return "改善睡眠质量的建议：\n1. 保持规律的作息时间\n2. 睡前避免使用电子设备\n3. 创造安静、舒适的睡眠环境\n4. 避免睡前饮用咖啡或茶\n5. 适当进行放松活动，如冥想或深呼吸\n\n如果您有严重的睡眠问题，建议咨询专业医生。";
        } else if (lowerMessage.contains("运动")) {
            return "适合您的运动计划建议：\n1. 每周至少进行150分钟中等强度有氧运动\n2. 每周进行2-3次力量训练\n3. 每天保持适当的身体活动，如步行\n4. 根据个人健康状况选择适合的运动方式\n5. 逐渐增加运动强度，避免过度训练\n\n请根据您的身体状况和医生建议调整运动计划。";
        } else if (lowerMessage.contains("血压")) {
            return "控制血压的建议：\n1. 保持健康的饮食习惯，减少盐的摄入\n2. 定期进行身体活动\n3. 保持健康的体重\n4. 限制酒精摄入\n5. 管理压力\n6. 按照医生建议服用降压药物\n\n请定期监测血压，并咨询医生获取个性化建议。";
        } else if (lowerMessage.contains("饮食")) {
            return "健康饮食建议：\n1. 多吃蔬菜水果，每天摄入5份以上\n2. 选择全谷物食品\n3. 适量摄入蛋白质，如瘦肉、鱼类、豆类\n4. 限制饱和脂肪和反式脂肪的摄入\n5. 控制糖分和盐分的摄入\n6. 保持充足的水分摄入\n\n请根据个人健康状况调整饮食计划。";
        } else if (lowerMessage.contains("压力")) {
            return "减轻压力的建议：\n1. 进行深呼吸和冥想练习\n2. 保持规律的运动\n3. 确保充足的睡眠\n4. 与朋友和家人保持联系\n5. 培养兴趣爱好\n6. 学习时间管理技巧\n7. 寻求专业帮助（如心理咨询）\n\n如果您感到持续的压力或焦虑，建议咨询专业人士。";
        } else if (lowerMessage.contains("心率")) {
            return "心率异常的建议：\n1. 休息并监测心率变化\n2. 避免咖啡因和其他兴奋剂\n3. 保持充足的水分摄入\n4. 避免过度运动\n5. 如果心率持续异常或伴有胸痛、呼吸困难等症状，请立即就医\n\n正常静息心率范围通常为60-100次/分钟，但这可能因年龄和健康状况而异。";
        } else if (lowerMessage.contains("体重")) {
            return "健康体重管理建议：\n1. 保持均衡的饮食，控制热量摄入\n2. 增加身体活动，每天至少30分钟中等强度运动\n3. 保持规律的作息时间\n4. 避免情绪化进食\n5. 设定合理的体重目标，每周减重0.5-1公斤为宜\n6. 定期监测体重变化\n\n请根据个人健康状况调整体重管理计划，如有需要咨询医生或营养师。";
        } else if (lowerMessage.contains("血糖")) {
            return "血糖管理建议：\n1. 控制碳水化合物的摄入，选择低GI食物\n2. 保持规律的饮食时间\n3. 适量进行有氧运动\n4. 按照医生建议监测血糖水平\n5. 遵循医生的药物治疗方案\n6. 保持健康的体重\n\n如果您有糖尿病或血糖异常，请定期咨询医生获取个性化建议。";
        } else if (lowerMessage.contains("你好") || lowerMessage.contains("嗨") || lowerMessage.contains("hello")) {
            return "你好！我是您的智能健康助手。有什么健康问题可以帮您解答吗？";
        } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("thank")) {
            return "不客气！如果您有任何其他健康问题，随时可以问我。";
        } else {
            return "感谢您的咨询。作为智能健康助手，我可以回答关于睡眠、运动、饮食、压力管理、血压、心率、体重和血糖等健康问题。请问您有什么具体的健康问题需要了解？\n\n免责声明：我的建议仅供参考，不能替代专业医疗建议。如有健康问题，请咨询专业医生。";
        }
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 模拟服务始终可用
    }
}