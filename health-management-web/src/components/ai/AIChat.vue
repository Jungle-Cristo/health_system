<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { MessageSquare, Send, Loader2, Trash2, Clock, User, Bot, HelpCircle } from 'lucide-vue-next';
import GlassCard from '../common/GlassCard.vue';
import { getAIResponse, getChatHistory, type AIChatMessage, type AIChatResponse } from '../../api/ai';

const props = defineProps<{
  userId?: number;
}>();

const messages = ref<AIChatMessage[]>([]);
const inputMessage = ref('');
const loading = ref(false);
const chatId = ref<string>(`chat_${Date.now()}`);

// 处理外部传入的问题（用于常见问题点击）
const handleExternalQuestion = async (question: string) => {
  inputMessage.value = question;
  await nextTick();
  await sendMessage();
};

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory();
    // 确保messages.value始终是数组
    if (Array.isArray(res.data)) {
      messages.value = res.data;
    } else {
      console.error('聊天历史数据格式错误:', res.data);
      messages.value = [];
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error);
    // 出错时确保messages.value是数组
    messages.value = [];
  }
};

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return;
  
  // 确保messages.value是数组
  if (!Array.isArray(messages.value)) {
    messages.value = [];
  }
  
  const message = inputMessage.value.trim();
  inputMessage.value = '';
  
  // 添加用户消息
  const userMessage: AIChatMessage = {
    id: `msg_${Date.now()}`,
    role: 'user',
    content: message,
    timestamp: new Date().toISOString(),
    chatId: chatId.value
  };
  
  messages.value.push(userMessage);
  loading.value = true;
  
  try {
    // 调用AI接口获取回复
    const response = await getAIResponse({
      message,
      chatId: chatId.value,
      context: messages.value.slice(-5) // 发送最近5条消息作为上下文
    });
    
    // 检查是否有错误
    if (response.data.error) {
      // 处理后端返回的错误
      let errorContent = '抱歉，处理您的请求时出现错误。';
      
      switch (response.data.error) {
        case 'INVALID_INPUT':
          errorContent = '输入内容无效，请检查您的输入。';
          break;
        case 'RATE_LIMITED':
          errorContent = '请求过于频繁，请稍后再试。';
          break;
        case 'INVALID_API_KEY':
          errorContent = 'AI服务配置错误，请联系管理员。';
          break;
        case 'CONTENT_FILTERED':
          errorContent = '您的请求内容不符合安全规范。';
          break;
        case 'TIMEOUT':
          errorContent = '请求超时，请稍后再试。';
          break;
        case 'SERVICE_UNAVAILABLE':
          errorContent = 'AI服务暂时不可用，请稍后再试。';
          break;
        default:
          errorContent = response.data.message || '抱歉，我暂时无法回答您的问题。';
      }
      
      // 添加错误消息
      const errorMessage: AIChatMessage = {
        id: `msg_${Date.now() + 1}`,
        role: 'assistant',
        content: errorContent,
        timestamp: new Date().toISOString(),
        chatId: chatId.value
      };
      
      messages.value.push(errorMessage);
    } else {
      // 添加AI回复
      const aiMessage: AIChatMessage = {
        id: `msg_${Date.now() + 1}`,
        role: 'assistant',
        content: response.data.response,
        timestamp: new Date().toISOString(),
        chatId: chatId.value
      };
      
      messages.value.push(aiMessage);
    }
  } catch (error) {
    console.error('获取AI回复失败:', error);
    
    // 添加错误消息
    const errorMessage: AIChatMessage = {
      id: `msg_${Date.now() + 1}`,
      role: 'assistant',
      content: '网络错误，无法连接到服务器。请检查网络连接后重试。',
      timestamp: new Date().toISOString(),
      chatId: chatId.value
    };
    
    messages.value.push(errorMessage);
  } finally {
    loading.value = false;
  }
};

// 清空聊天记录
const clearChat = () => {
  messages.value = [];
  chatId.value = `chat_${Date.now()}`;
};

// 按Enter键发送消息
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
};

// 时间格式化
const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadChatHistory();
});

// 暴露方法给父组件调用
defineExpose({
  handleExternalQuestion
});
</script>

<template>
  <GlassCard class="h-full flex flex-col">
    <!-- 聊天头部 -->
    <div class="flex items-center justify-between mb-4 pb-4 border-b border-white/10">
      <div class="flex items-center gap-2">
        <MessageSquare class="w-5 h-5 text-indigo-400" />
        <h3 class="text-lg font-semibold">智能健康助手</h3>
      </div>
      <button 
        @click="clearChat"
        class="text-gray-400 hover:text-white transition-colors p-2 rounded-full hover:bg-white/10"
        title="清空聊天记录"
      >
        <Trash2 class="w-4 h-4" />
      </button>
    </div>
    
    <!-- 聊天内容区域 -->
    <div class="flex-1 overflow-y-auto mb-4 space-y-4 pr-2">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="text-center py-8 text-gray-400">
        <HelpCircle class="w-12 h-12 mx-auto mb-4 text-indigo-400/50" />
        <p>您好！我是您的智能健康助手，有什么可以帮您的吗？</p>
        <p class="text-sm mt-2">您可以询问健康问题、获取运动建议或了解营养知识</p>
      </div>
      
      <!-- 消息列表 -->
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="[
          'flex',
          message.role === 'user' ? 'justify-end' : 'justify-start'
        ]"
      >
        <div 
          :class="[
            'max-w-[80%] p-3 rounded-lg',
            message.role === 'user' 
              ? 'bg-indigo-500/20 text-white rounded-tr-none' 
              : 'bg-white/10 text-gray-200 rounded-tl-none'
          ]"
        >
          <div class="flex items-start gap-2">
            <div 
              :class="[
                'w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0',
                message.role === 'user' ? 'bg-indigo-500' : 'bg-purple-500'
              ]"
            >
              <component :is="message.role === 'user' ? User : Bot" class="w-4 h-4 text-white" />
            </div>
            <div class="flex-1">
              <p class="whitespace-pre-wrap">{{ message.content }}</p>
              <div class="flex items-center gap-1 mt-2 text-xs text-gray-400">
                <Clock class="w-3 h-3" />
                <span>{{ formatTime(message.timestamp) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center py-2">
        <Loader2 class="w-4 h-4 text-indigo-400 animate-spin" />
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="relative">
      <textarea
        v-model="inputMessage"
        @keydown="handleKeyDown"
        class="w-full p-4 pr-12 bg-white/5 border border-white/10 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/50 resize-none min-h-[80px] text-white"
        placeholder="请输入您的健康问题..."
        :disabled="loading"
      ></textarea>
      <button
        @click="sendMessage"
        class="absolute right-2 bottom-2 w-8 h-8 rounded-full bg-indigo-500 hover:bg-indigo-600 flex items-center justify-center transition-colors"
        :disabled="loading || !inputMessage.trim()"
        :class="{ 'opacity-50 cursor-not-allowed': loading || !inputMessage.trim() }"
      >
        <Send class="w-4 h-4 text-white" />
      </button>
    </div>
  </GlassCard>
</template>

<style scoped>
/* 自定义滚动条 */
div::-webkit-scrollbar {
  width: 4px;
}

div::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
}

div::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

div::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
