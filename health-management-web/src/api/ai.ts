import request from '../utils/request';

export interface ChatMessage {
  role: string;
  content: string;
}

export interface AiChatRequest {
  message: string;
  history?: ChatMessage[];
}

export interface AiChatResponse {
  answer: string;
  hasDisclaimer: boolean;
  disclaimer: string;
}

export function sendChatMessage(data: AiChatRequest) {
  return request.post<any, { data: AiChatResponse }>('/ai/chat', data);
}
