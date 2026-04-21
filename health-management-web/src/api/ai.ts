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
  };
}

// AI聊天历史响应类型
export interface AIChatHistoryResponse {
  data: AIChatMessage[];
}

// 基础URL
const BASE_URL = '/api/ai';

// 获取AI回复
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
    
    const response = await axios.post(`${BASE_URL}/chat`, request, config);
    return response;
  } catch (error) {
    console.error('获取AI回复失败:', error);
    throw error;
  }
};

// 获取聊天历史
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

// 清空聊天历史
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
