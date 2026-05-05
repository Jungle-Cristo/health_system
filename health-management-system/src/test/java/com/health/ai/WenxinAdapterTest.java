package com.health.ai;

import com.health.ai.impl.WenxinAdapter;
import com.health.exception.AIServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 百度千帆（文心一言）服务适配器单元测试
 */
class WenxinAdapterTest {
    
    private AIConfig aiConfig;
    private AIConfig.WenxinConfig wenxinConfig;
    
    @BeforeEach
    void setUp() {
        aiConfig = Mockito.mock(AIConfig.class);
        wenxinConfig = Mockito.mock(AIConfig.WenxinConfig.class);
        when(aiConfig.getWenxin()).thenReturn(wenxinConfig);
    }
    
    @Test
    @DisplayName("测试WenxinAdapter实现AIServiceAdapter接口")
    void testImplementsAIServiceAdapter() {
        when(wenxinConfig.getApiKey()).thenReturn("test-api-key");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertTrue(adapter instanceof AIServiceAdapter);
        assertEquals(AIProvider.BAIDU_WENXIN, adapter.getProvider());
    }
    
    @Test
    @DisplayName("测试isAvailable方法 - API Key已配置")
    void testIsAvailable_WithApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn("bce-v3/ALTAK-test-key");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertTrue(adapter.isAvailable());
    }
    
    @Test
    @DisplayName("测试isAvailable方法 - API Key为空")
    void testIsAvailable_WithEmptyApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn("");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertFalse(adapter.isAvailable());
    }
    
    @Test
    @DisplayName("测试isAvailable方法 - API Key为null")
    void testIsAvailable_WithNullApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn(null);
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertFalse(adapter.isAvailable());
    }
    
    @Test
    @DisplayName("测试hasCompleteConfig方法 - API Key已配置")
    void testHasCompleteConfig_WithApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn("bce-v3/ALTAK-test-key");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertTrue(adapter.hasCompleteConfig());
    }
    
    @Test
    @DisplayName("测试hasCompleteConfig方法 - API Key为空")
    void testHasCompleteConfig_WithEmptyApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn("");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertFalse(adapter.hasCompleteConfig());
    }
    
    @Test
    @DisplayName("测试generateResponse方法 - API Key未配置时抛出异常")
    void testGenerateResponse_WithoutApiKey() {
        when(wenxinConfig.getApiKey()).thenReturn(null);
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        AIServiceException exception = assertThrows(AIServiceException.class, () -> {
            adapter.generateResponse("你好", null);
        });
        
        assertEquals(AIServiceException.ErrorCode.INVALID_API_KEY, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("测试getProvider方法返回正确的提供商")
    void testGetProvider() {
        when(wenxinConfig.getApiKey()).thenReturn("test-api-key");
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertEquals(AIProvider.BAIDU_WENXIN, adapter.getProvider());
        assertEquals("baidu_wenxin", adapter.getProvider().getCode());
        assertEquals("百度文心一言", adapter.getProvider().getName());
    }
    
    @Test
    @DisplayName("测试千帆API Key格式验证 - 有效的API Key格式")
    void testValidApiKeyFormat() {
        String validApiKey = "bce-v3/ALTAK-cGVkqeUN6YcrcnMI3oM5n/6c4d749ca43b31848186c02d2fd4cd26a1ed1b43";
        
        when(wenxinConfig.getApiKey()).thenReturn(validApiKey);
        
        WenxinAdapter adapter = new WenxinAdapter(aiConfig);
        
        assertTrue(adapter.isAvailable());
        assertTrue(adapter.hasCompleteConfig());
    }
}
