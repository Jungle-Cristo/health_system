<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { MessageSquare, Send, Loader2, Trash2, Clock, User, Bot, HelpCircle } from 'lucide-vue-next';
import { marked } from 'marked';
import GlassCard from '../common/GlassCard.vue';
import { getAIResponse, getAIStreamResponse, getChatHistory, type AIChatMessage } from '../../api/ai';
import { formatTime } from '../../lib/utils';

marked.setOptions({
  breaks: true,
  gfm: true
});

const renderMarkdown = (content: string): string => {
  try {
    return marked.parse(content) as string;
  } catch {
    return content;
  }
};

const STREAM_DELAY = 300; // 每行输出延迟约0.3秒

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
    if (Array.isArray(res)) {
      messages.value = res;
    } else {
      console.error('聊天历史数据格式错误:', res);
      messages.value = [];
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error);
    // 出错时确保messages.value是数组
    messages.value = [];
  }
};

const streamingMessageId = ref<string | null>(null);
const streamingContent = ref('');

const delay = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return;
  
  if (!Array.isArray(messages.value)) {
    messages.value = [];
  }
  
  const message = inputMessage.value.trim();
  inputMessage.value = '';
  
  const userMessage: AIChatMessage = {
    id: `msg_${Date.now()}`,
    role: 'user',
    content: message,
    timestamp: new Date().toISOString(),
    chatId: chatId.value
  };
  
  messages.value.push(userMessage);
  loading.value = true;
  
  const aiMessageId = `msg_${Date.now() + 1}`;
  streamingMessageId.value = aiMessageId;
  streamingContent.value = '';
  
  const aiMessage: AIChatMessage = {
    id: aiMessageId,
    role: 'assistant',
    content: '',
    timestamp: new Date().toISOString(),
    chatId: chatId.value
  };
  
  messages.value.push(aiMessage);
  
  try {
    await getAIStreamResponse({
      message,
      chatId: chatId.value,
      context: messages.value.slice(-6)
    }, async (chunk) => {
      streamingContent.value += chunk;
      
      const index = messages.value.findIndex(m => m.id === aiMessageId);
      if (index !== -1) {
        messages.value[index].content = streamingContent.value;
      }
      
      await delay(STREAM_DELAY);
    }, () => {
      streamingMessageId.value = null;
    }, (error) => {
      console.error('流式响应错误:', error);
      streamingMessageId.value = null;
    });
    
  } catch (error) {
    console.error('获取AI回复失败:', error);
    
    const index = messages.value.findIndex(m => m.id === aiMessageId);
    if (index !== -1) {
      messages.value[index].content = '网络错误，无法连接到服务器。请检查网络连接后重试。';
    } else {
      const errorMessage: AIChatMessage = {
        id: `msg_${Date.now() + 2}`,
        role: 'assistant',
        content: '网络错误，无法连接到服务器。请检查网络连接后重试。',
        timestamp: new Date().toISOString(),
        chatId: chatId.value
      };
      messages.value.push(errorMessage);
    }
  } finally {
    loading.value = false;
    streamingMessageId.value = null;
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

// formatTime 从 lib/utils 导入

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
              <p class="whitespace-pre-wrap" v-if="message.role === 'user'">{{ message.content }}</p>
              <div class="whitespace-pre-wrap markdown-content" v-else v-html="renderMarkdown(message.content)"></div>
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

/* Markdown样式 */
.markdown-content {
  line-height: 1.6;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  font-weight: 600;
  margin-top: 1em;
  margin-bottom: 0.5em;
  color: #e0e7ff;
}

.markdown-content h1 { font-size: 1.5em; }
.markdown-content h2 { font-size: 1.3em; }
.markdown-content h3 { font-size: 1.1em; }

.markdown-content p {
  margin-bottom: 0.8em;
}

.markdown-content ul,
.markdown-content ol {
  padding-left: 1.5em;
  margin-bottom: 0.8em;
}

.markdown-content li {
  margin-bottom: 0.3em;
}

.markdown-content strong {
  font-weight: 600;
  color: #c7d2fe;
}

.markdown-content em {
  font-style: italic;
}

.markdown-content code {
  background-color: rgba(139, 92, 246, 0.2);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-size: 0.9em;
  color: #c4b5fd;
}

.markdown-content blockquote {
  border-left: 3px solid #8b5cf6;
  padding-left: 1em;
  margin: 0.8em 0;
  color: #a5b4fc;
  font-style: italic;
}

.markdown-content hr {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin: 1em 0;
}
</style>
