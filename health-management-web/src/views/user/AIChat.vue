<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Brain, MessageSquare, HelpCircle, Home, ChevronDown, RefreshCw, Check, Sparkles } from 'lucide-vue-next';
import AppLayout from '../../components/common/AppLayout.vue';
import AIChat from '../../components/ai/AIChat.vue';
import type AIChatComponent from '../../components/ai/AIChat.vue';
import { getCurrentProvider, getAvailableProviders, switchProvider, type AIProviderInfo } from '../../api/ai';
import { healthMetrics } from '../../config/healthMetrics';

const router = useRouter();
const aiChatRef = ref<InstanceType<typeof AIChatComponent> | null>(null);

const currentProvider = ref<AIProviderInfo | null>(null);
const availableProviders = ref<AIProviderInfo[]>([]);
const showProviderDropdown = ref(false);
const switchingProvider = ref(false);
const switchSuccessMessage = ref('');

const loadProviderInfo = async () => {
  try {
    currentProvider.value = await getCurrentProvider();
  } catch {
    currentProvider.value = { code: 'mock', name: '模拟模式' };
  }
};

const loadAvailableProviders = async () => {
  try {
    availableProviders.value = await getAvailableProviders();
  } catch {
    availableProviders.value = [{ code: 'mock', name: '模拟模式' }];
  }
};

const handleSwitchProvider = async (providerCode: string) => {
  if (switchingProvider.value) return;
  switchingProvider.value = true;
  try {
    const result = await switchProvider(providerCode);
    if (result.success) {
      await Promise.all([loadProviderInfo(), loadAvailableProviders()]);
      switchSuccessMessage.value = result.message;
    } else {
      switchSuccessMessage.value = result.error || '切换失败';
    }
  } catch (error: any) {
    switchSuccessMessage.value = error.response?.data?.message || error.message || '切换失败';
  } finally {
    switchingProvider.value = false;
    showProviderDropdown.value = false;
    setTimeout(() => { switchSuccessMessage.value = ''; }, 3000);
  }
};

const getStatusClass = (code: string) => {
  const map: Record<string, string> = { openai: 'bg-green-500', baidu_wenxin: 'bg-blue-500', deepseek: 'bg-indigo-500' };
  return map[code] || 'bg-gray-500';
};

const getDesc = (code: string) => {
  const map: Record<string, string> = { openai: 'OpenAI大语言模型', baidu_wenxin: '百度文心一言', deepseek: 'DeepSeek大语言模型' };
  return map[code] || '基于关键词匹配的模拟回复';
};

const isMock = () => currentProvider.value?.code === 'mock';

const sendQuickQuestion = async (question: string) => {
  aiChatRef.value?.handleExternalQuestion(question);
};

onMounted(() => {
  loadProviderInfo();
  loadAvailableProviders();
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement;
    if (!target.closest('.provider-dropdown-container')) showProviderDropdown.value = false;
  });
});

const quickQuestions = [
  '如何改善睡眠质量？', '适合我的运动计划是什么？',
  '如何控制血压？', '健康饮食建议',
  '如何减轻压力？', '心率异常怎么办？',
];
</script>

<template>
  <AppLayout title="智能健康助手" subtitle="随时为您提供健康建议和解答">
    <template #header-left>
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
            <Brain class="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 class="text-3xl md:text-4xl font-bold">智能健康助手</h1>
            <p class="text-gray-400">随时为您提供健康建议和解答</p>
          </div>
        </div>

        <div class="provider-dropdown-container relative">
          <button
            @click.stop="showProviderDropdown = !showProviderDropdown"
            class="flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-200 border text-sm font-medium"
            :class="isMock() ? 'bg-gray-500/20 text-gray-400 border-gray-500/30' : 'bg-gradient-to-r from-green-500/20 to-blue-500/20 text-green-400 border-green-500/30'"
          >
            <div :class="['w-2.5 h-2.5 rounded-full', getStatusClass(currentProvider?.code || 'mock')]" />
            {{ isMock() ? '模拟模式' : 'AI接入模式' }}
            <ChevronDown class="w-4 h-4 transition-transform" :class="{ 'rotate-180': showProviderDropdown }" />
          </button>

          <Transition enter-active-class="transition-all duration-200 ease-out" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100" leave-active-class="transition-all duration-150 ease-in" leave-to-class="opacity-0 scale-95">
            <div v-show="showProviderDropdown" class="absolute left-0 mt-2 w-64 rounded-xl bg-gray-800/98 backdrop-blur-lg border border-white/10 shadow-xl z-50">
              <div v-for="provider in availableProviders" :key="provider.code">
                <button @click="handleSwitchProvider(provider.code)" :disabled="switchingProvider"
                  class="w-full flex items-center gap-3 p-3 hover:bg-white/10 transition-colors text-left disabled:opacity-50"
                  :class="{ 'bg-indigo-500/20 border border-indigo-500/30': currentProvider?.code === provider.code }">
                  <div :class="['w-8 h-8 rounded-lg flex items-center justify-center', provider.code === 'mock' ? 'bg-gray-500/20' : 'bg-green-500/20']">
                    <Sparkles :class="['w-4 h-4', getStatusClass(provider.code) === 'bg-gray-500' ? 'text-gray-400' : 'text-green-400']" />
                  </div>
                  <div class="flex-1">
                    <div class="font-medium text-white text-sm">{{ provider.name }}</div>
                    <div class="text-xs text-gray-400">{{ getDesc(provider.code) }}</div>
                  </div>
                  <Check v-if="currentProvider?.code === provider.code" class="w-4 h-4 text-indigo-400" />
                  <RefreshCw v-if="switchingProvider && currentProvider?.code !== provider.code" class="w-4 h-4 animate-spin text-indigo-400" />
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </template>

    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0">
      <div v-if="switchSuccessMessage" class="fixed top-4 right-4 px-4 py-2 rounded-lg bg-green-500/20 border border-green-500/30 text-green-400 text-sm flex items-center gap-2 z-50">
        <Check class="w-4 h-4" /> {{ switchSuccessMessage }}
      </div>
    </Transition>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-1 space-y-4">
        <button @click="router.push('/')" class="w-full flex items-center gap-3 p-4 rounded-lg bg-white/5 hover:bg-white/10 transition-colors">
          <Home class="w-5 h-5 text-indigo-400" /><span>首页</span>
        </button>

        <div>
          <h3 class="text-lg font-semibold mb-3 flex items-center gap-2"><HelpCircle class="w-5 h-5 text-indigo-400" />常见问题</h3>
          <div class="space-y-2">
            <button v-for="(q, i) in quickQuestions" :key="i" @click="sendQuickQuestion(q)"
              class="w-full text-left p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors text-sm">{{ q }}</button>
          </div>
        </div>

        <div>
          <h3 class="text-lg font-semibold mb-3">健康指标</h3>
          <div class="grid grid-cols-2 gap-2">
            <button v-for="m in healthMetrics" :key="m.type"
              class="flex items-center gap-2 p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors text-sm">
              <component :is="m.icon" :class="['w-4 h-4', m.color]" />
              {{ m.label }}
            </button>
          </div>
        </div>
      </div>

      <div class="lg:col-span-2">
        <AIChat ref="aiChatRef" />
      </div>
    </div>
  </AppLayout>
</template>
