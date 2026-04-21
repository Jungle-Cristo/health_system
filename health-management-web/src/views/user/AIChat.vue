<script setup lang="ts">
import AIChat from '../../components/ai/AIChat.vue';
import { Brain, MessageSquare, HelpCircle, Activity, Heart, Moon, Scale, Gauge, Droplets, Home } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

const router = useRouter();

// 导航到其他页面
const navigateTo = (path: string) => {
  router.push(path);
};

// 快捷问题
const quickQuestions = [
  { id: 1, text: '如何改善睡眠质量？' },
  { id: 2, text: '适合我的运动计划是什么？' },
  { id: 3, text: '如何控制血压？' },
  { id: 4, text: '健康饮食建议' },
  { id: 5, text: '如何减轻压力？' },
  { id: 6, text: '心率异常怎么办？' }
];

// 发送快捷问题
const sendQuickQuestion = (question: string) => {
  // 这里需要与AIChat组件通信，将问题传递给它
  // 暂时留空，后续通过事件或props实现
};
</script>

<template>
  <div class="ai-chat-container p-6 md:p-8">
    <!-- 页面标题 -->
    <header class="mb-8">
      <div class="flex items-center gap-3 mb-2">
        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
          <Brain class="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 class="text-3xl md:text-4xl font-bold">智能健康助手</h1>
          <p class="text-gray-400">随时为您提供健康建议和解答</p>
        </div>
      </div>
    </header>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：快捷功能 -->
      <div class="lg:col-span-1 space-y-6">
        <!-- 导航菜单 -->
        <div class="space-y-2">
          <button 
            @click="navigateTo('/')"
            class="w-full flex items-center gap-3 p-4 rounded-lg bg-white/5 hover:bg-white/10 transition-colors"
          >
            <Home class="w-5 h-5 text-indigo-400" />
            <span>首页</span>
          </button>
          <button 
            class="w-full flex items-center gap-3 p-4 rounded-lg bg-indigo-500/20 border border-indigo-500/30"
          >
            <MessageSquare class="w-5 h-5 text-indigo-400" />
            <span>智能助手</span>
          </button>
        </div>

        <!-- 快捷问题 -->
        <div>
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <HelpCircle class="w-5 h-5 text-indigo-400" />
            常见问题
          </h3>
          <div class="space-y-2">
            <button 
              v-for="question in quickQuestions" 
              :key="question.id"
              @click="sendQuickQuestion(question.text)"
              class="w-full text-left p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors text-sm"
            >
              {{ question.text }}
            </button>
          </div>
        </div>

        <!-- 健康指标快速入口 -->
        <div>
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <Activity class="w-5 h-5 text-indigo-400" />
            健康指标
          </h3>
          <div class="grid grid-cols-2 gap-3">
            <button class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
              <Heart class="w-4 h-4 text-red-400" />
              <span class="text-sm">心率</span>
            </button>
            <button class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
              <Moon class="w-4 h-4 text-purple-400" />
              <span class="text-sm">睡眠</span>
            </button>
            <button class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
              <Scale class="w-4 h-4 text-green-400" />
              <span class="text-sm">体重</span>
            </button>
            <button class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
              <Gauge class="w-4 h-4 text-yellow-400" />
              <span class="text-sm">血压</span>
            </button>
            <button class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
              <Droplets class="w-4 h-4 text-orange-400" />
              <span class="text-sm">血糖</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天界面 -->
      <div class="lg:col-span-2">
        <div class="h-[700px]">
          <AIChat />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-chat-container {
  max-width: 1440px;
  margin: 0 auto;
  min-height: 100vh;
}
</style>
