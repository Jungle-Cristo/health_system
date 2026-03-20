<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Send, Bot, User, Loader2, AlertCircle } from 'lucide-vue-next';
import GlassCard from '../components/common/GlassCard.vue';
import { sendChatMessage, type ChatMessage, type AiChatResponse } from '../api/ai';

const router = useRouter();
const messages = ref<ChatMessage[]>([]);
const inputMessage = ref('');
const isLoading = ref(false);
const errorMessage = ref('');
const messagesContainer = ref<HTMLElement>();

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const handleSend = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return;

  const userMessage = inputMessage.value.trim();
  inputMessage.value = '';
  errorMessage.value = '';

  messages.value.push({
    role: 'user',
    content: userMessage
  });

  scrollToBottom();
  isLoading.value = true;

  try {
    const response = await sendChatMessage({
      message: userMessage,
      history: messages.value.slice(0, -1)
    });

    const aiResponse: AiChatResponse = response.data;
    let aiContent = aiResponse.answer;

    if (aiResponse.hasDisclaimer && aiResponse.disclaimer) {
      aiContent += `\n\n---\n${aiResponse.disclaimer}`;
    }

    messages.value.push({
      role: 'assistant',
      content: aiContent
    });
  } catch (error) {
    errorMessage.value = '发送消息失败，请稍后重试';
    console.error('Chat error:', error);
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    handleSend();
  }
};

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '您好！我是健康助手，可以为您提供健康咨询服务。您可以询问关于头痛、失眠、感冒、运动、饮食、压力等方面的问题，我会尽力为您提供建议。'
  });
});
</script>

<template>
  <div class="ai-chat-container min-h-screen p-4 md:p-8">
    <div class="max-w-4xl mx-auto">
      <header class="mb-6">
        <button
          @click="router.back()"
          class="flex items-center gap-2 text-gray-400 hover:text-white transition-colors mb-4"
        >
          <ArrowLeft class="w-5 h-5" />
          <span>返回</span>
        </button>
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <Bot class="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 class="text-2xl font-bold text-white">健康助手</h1>
            <p class="text-gray-400 text-sm">AI 健康咨询</p>
          </div>
        </div>
      </header>

      <GlassCard class="chat-container flex flex-col h-[calc(100vh-200px)]">
        <div
          ref="messagesContainer"
          class="messages-container flex-1 overflow-y-auto space-y-4 pr-2"
        >
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message flex gap-3', message.role === 'user' ? 'justify-end' : 'justify-start']"
          >
            <div
              v-if="message.role === 'assistant'"
              class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center flex-shrink-0"
            >
              <Bot class="w-4 h-4 text-white" />
            </div>
            <div
              :class="[
                'message-bubble max-w-[80%] rounded-2xl px-4 py-3',
                message.role === 'user'
                  ? 'bg-gradient-to-br from-indigo-500 to-purple-600 text-white rounded-tr-md'
                  : 'bg-white/10 text-gray-200 rounded-tl-md'
              ]"
            >
              <p class="whitespace-pre-wrap">{{ message.content }}</p>
            </div>
            <div
              v-if="message.role === 'user'"
              class="w-8 h-8 rounded-full bg-gradient-to-br from-green-500 to-emerald-600 flex items-center justify-center flex-shrink-0"
            >
              <User class="w-4 h-4 text-white" />
            </div>
          </div>

          <div v-if="isLoading" class="message flex gap-3 justify-start">
            <div
              class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center flex-shrink-0"
            >
              <Bot class="w-4 h-4 text-white" />
            </div>
            <div class="message-bubble bg-white/10 text-gray-200 rounded-2xl rounded-tl-md px-4 py-3 flex items-center gap-2">
              <Loader2 class="w-4 h-4 animate-spin" />
              <span>正在思考...</span>
            </div>
          </div>

          <div v-if="errorMessage" class="message flex gap-3 justify-center">
            <div class="message-bubble bg-red-500/20 text-red-300 rounded-2xl px-4 py-3 flex items-center gap-2">
              <AlertCircle class="w-4 h-4" />
              <span>{{ errorMessage }}</span>
            </div>
          </div>
        </div>

        <div class="input-area mt-4 pt-4 border-t border-white/10">
          <div class="flex gap-3">
            <textarea
              v-model="inputMessage"
              @keydown="handleKeyPress"
              placeholder="输入您的健康问题..."
              class="flex-1 bg-white/10 border border-white/20 rounded-xl px-4 py-3 text-white placeholder-gray-400 focus:outline-none focus:border-indigo-500 resize-none"
              rows="1"
              :disabled="isLoading"
            />
            <button
              @click="handleSend"
              :disabled="!inputMessage.trim() || isLoading"
              class="px-6 py-3 bg-gradient-to-br from-indigo-500 to-purple-600 text-white rounded-xl hover:from-indigo-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center gap-2"
            >
              <Send class="w-4 h-4" />
              <span>发送</span>
            </button>
          </div>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.ai-chat-container {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #1e1b4b 100%);
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
