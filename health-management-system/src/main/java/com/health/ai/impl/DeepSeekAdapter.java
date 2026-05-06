package com.health.ai.impl;

import com.health.ai.AIConfig;
import com.health.ai.AIProvider;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekAdapter extends AbstractOpenAICompatibleAdapter {

    private final AIConfig.DeepSeekConfig config;

    public DeepSeekAdapter(AIConfig aiConfig) {
        this.config = aiConfig.getDeepseek();
    }

    @Override public AIProvider getProvider() { return AIProvider.DEEPSEEK; }
    @Override protected String getApiKey() { return config.getApiKey(); }
    @Override protected String getBaseUrl() { return config.getBaseUrl(); }
    @Override protected String getModel() { return config.getModel(); }
    @Override protected double getTemperature() { return config.getTemperature(); }
    @Override
    protected String getSystemPrompt() {
        return "你是一个专业的健康顾问AI助手。请基于提供的用户健康数据，给出个性化的、具体的健康分析和建议。如果数据中存在异常指标，请明确指出并给出改善建议。";
    }
}
