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

export async function getAIStreamResponse(
  req: AIChatRequest,
  onChunk: (chunk: string) => void,
  onComplete?: () => void,
  onError?: (error: Error) => void
): Promise<void> {
  try {
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(req)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('No response body');
    }

    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let currentEvent = '';

    while (true) {
      const { done, value } = await reader.read();
      
      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.replace('event:', '').trim();
        } else if (line.startsWith('data:')) {
          const data = line.replace('data:', '').trim();
          if (currentEvent === 'message' && data) {
            onChunk(data);
          } else if (currentEvent === 'complete') {
            onComplete?.();
          }
        }
      }
    }

    onComplete?.();
  } catch (error) {
    onError?.(error as Error);
    throw error;
  }
}
