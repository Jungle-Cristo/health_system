package com.health.ai;

import com.health.exception.AIServiceException;

/**
 * AI服务适配器接口
 * 定义所有AI服务提供商必须实现的方法
 */
public interface AIServiceAdapter {
    
    /**
     * 获取服务提供商类型
     * @return AIProvider枚举值
     */
    AIProvider getProvider();
    
    /**
     * 生成AI回复
     * @param message 用户输入消息
     * @param context 对话上下文（可选）
     * @return AI生成的回复内容
     * @throws AIServiceException 当AI服务调用失败时抛出
     */
    String generateResponse(String message, String context) throws AIServiceException;
    
    /**
     * 检查服务是否可用
     * @return true表示服务可用，false表示不可用
     */
    boolean isAvailable();
    
    /**
     * 获取服务名称
     * @return 服务提供商名称
     */
    default String getServiceName() {
        return getProvider().getName();
    }
}