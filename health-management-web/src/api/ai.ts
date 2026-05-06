import request from '../utils/request';

export interface AIChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  chatId: string;
}

export interface AIChatRequest {
  message: string;
  chatId: string;
  context?: AIChatMessage[];
}

export interface AIProviderInfo {
  code: string;
  name: string;
}

export interface SwitchProviderResponse {
  success: boolean;
  provider: string;
  message: string;
  error?: string;
}

export async function getAIResponse(req: AIChatRequest): Promise<any> {
  return request.post('/ai/chat', req);
}

export async function getChatHistory(): Promise<AIChatMessage[]> {
  return request.get('/ai/history');
}

export async function clearChatHistory(): Promise<void> {
  return request.delete('/ai/history');
}

export async function getCurrentProvider(): Promise<AIProviderInfo> {
  return request.get('/ai/provider/current');
}

export async function getAvailableProviders(): Promise<AIProviderInfo[]> {
  return request.get('/ai/provider/available');
}

export async function switchProvider(provider: string): Promise<SwitchProviderResponse> {
  return request.post('/ai/provider/switch', { provider });
}
