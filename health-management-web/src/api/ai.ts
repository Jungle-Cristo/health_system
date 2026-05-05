import axios from 'axios';

// AI聊天消息类型
export interface AIChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  chatId: string;
}

// AI聊天请求类型
export interface AIChatRequest {
  message: string;
  chatId: string;
  context?: AIChatMessage[];
}

// AI聊天响应类型
export interface AIChatResponse {
  data: {
    response: string;
    chatId: string;
    provider?: string;
    error?: string;
    message?: string;
    retryable?: boolean;
  };
}

// AI聊天历史响应类型
export interface AIChatHistoryResponse {
  data: AIChatMessage[];
}

// AI服务提供商类型
export interface AIProviderInfo {
  code: string;
  name: string;
}

// AI服务提供商响应类型
export interface AIProviderResponse {
  code: string;
  name: string;
}

// AI服务提供商列表响应类型
export interface AIProviderListResponse {
  data: AIProviderInfo[];
}

// 切换提供商响应类型
export interface SwitchProviderResponse {
  success: boolean;
  provider: string;
  message: string;
  error?: string;
}

// 基础URL
const BASE_URL = '/ai';

/**
 * 获取AI回复
 * @param request 聊天请求
 * @returns AI响应
 */
export const getAIResponse = async (request: AIChatRequest): Promise<AIChatResponse> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    console.debug('发送AI请求:', {
      url: `${BASE_URL}/chat`,
      request: request,
      hasToken: !!token
    });
    
    const response = await axios.post(`${BASE_URL}/chat`, request, config);
    console.debug('AI响应:', response.data);
    return response;
  } catch (error: any) {
    // 增强错误日志
    let errorInfo = {
      url: `${BASE_URL}/chat`,
      status: error.response?.status || '未知',
      statusText: error.response?.statusText || '未知',
      requestData: request,
      responseData: error.response?.data || null,
      message: error.message
    };
    
    console.error('获取AI回复失败:', errorInfo);
    
    // 根据HTTP状态码提供更详细的错误信息
    if (error.response?.status === 401) {
      const errorData = error.response.data;
      throw new Error(`认证失败: ${errorData?.message || 'API Key无效或已过期'}`);
    } else if (error.response?.status === 403) {
      throw new Error('访问被拒绝: 权限不足');
    } else if (error.response?.status === 429) {
      throw new Error('请求过于频繁，请稍后重试');
    } else if (error.response?.status === 503) {
      throw new Error('服务暂时不可用，请稍后重试');
    } else if (error.response?.status === 400) {
      const errorData = error.response.data;
      throw new Error(`请求参数错误: ${errorData?.message || '请检查输入内容'}`);
    } else {
      throw new Error(`请求失败: ${error.message}`);
    }
  }
};

/**
 * 获取聊天历史
 * @returns 聊天消息列表
 */
export const getChatHistory = async (): Promise<AIChatHistoryResponse> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {};
    
    if (token) {
      config.headers = {
        'Authorization': `Bearer ${token}`
      };
    }
    
    const response = await axios.get(`${BASE_URL}/history`, config);
    return response;
  } catch (error) {
    console.error('获取聊天历史失败:', error);
    throw error;
  }
};

/**
 * 清空聊天历史
 */
export const clearChatHistory = async (): Promise<void> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {};
    
    if (token) {
      config.headers = {
        'Authorization': `Bearer ${token}`
      };
    }
    
    await axios.delete(`${BASE_URL}/history`, config);
  } catch (error) {
    console.error('清空聊天历史失败:', error);
    throw error;
  }
};

/**
 * 获取当前使用的AI服务提供商
 * @returns 当前提供商信息
 */
export const getCurrentProvider = async (): Promise<AIProviderResponse> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {};
    
    if (token) {
      config.headers = {
        'Authorization': `Bearer ${token}`
      };
    }
    
    const response = await axios.get(`${BASE_URL}/provider/current`, config);
    return response.data;
  } catch (error) {
    console.error('获取当前提供商失败:', error);
    throw error;
  }
};

/**
 * 获取所有可用的AI服务提供商
 * @returns 可用提供商列表
 */
export const getAvailableProviders = async (): Promise<AIProviderInfo[]> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {};
    
    if (token) {
      config.headers = {
        'Authorization': `Bearer ${token}`
      };
    }
    
    const response = await axios.get(`${BASE_URL}/provider/available`, config);
    return response.data;
  } catch (error) {
    console.error('获取可用提供商失败:', error);
    throw error;
  }
};

/**
 * 切换AI服务提供商
 * @param provider 提供商代码
 * @returns 切换结果
 */
export const switchProvider = async (provider: string): Promise<SwitchProviderResponse> => {
  try {
    const token = localStorage.getItem('token');
    const config: any = {
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await axios.post(`${BASE_URL}/provider/switch`, { provider }, config);
    return response.data;
  } catch (error) {
    console.error('切换提供商失败:', error);
    throw error;
  }
};