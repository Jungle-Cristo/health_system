<script setup lang="ts">
import AIChat from '../../components/ai/AIChat.vue';
import type AIChatComponent from '../../components/ai/AIChat.vue';
import { Brain, MessageSquare, HelpCircle, Activity, Heart, Moon, Scale, Gauge, Droplets, Home, ChevronDown, RefreshCw, Check, Sparkles } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { ref, onMounted, watch } from 'vue';
import { getCurrentProvider, getAvailableProviders, switchProvider, type AIProviderInfo } from '../../api/ai';

const router = useRouter();
const aiChatRef = ref<InstanceType<typeof AIChatComponent> | null>(null);

// AI模式相关状态
const currentProvider = ref<AIProviderInfo | null>(null);
const availableProviders = ref<AIProviderInfo[]>([]);
const showProviderDropdown = ref(false);
const switchingProvider = ref(false);
const switchSuccessMessage = ref('');

// 判断当前是否为AI接入模式
const isAIMode = ref(false);

// 加载当前提供商信息
const loadProviderInfo = async () => {
  try {
    const provider = await getCurrentProvider();
    currentProvider.value = provider;
    isAIMode.value = provider.code !== 'mock';
  } catch (error) {
    console.error('加载提供商信息失败:', error);
    currentProvider.value = { code: 'mock', name: '模拟模式' };
    isAIMode.value = false;
  }
};

// 加载可用提供商列表
const loadAvailableProviders = async () => {
  try {
    const providers = await getAvailableProviders();
    availableProviders.value = providers;
  } catch (error) {
    console.error('加载可用提供商失败:', error);
    availableProviders.value = [{ code: 'mock', name: '模拟模式' }];
  }
};

// 切换AI服务提供商
const handleSwitchProvider = async (providerCode: string) => {
  if (switchingProvider.value) return;
  
  switchingProvider.value = true;
  try {
    const result = await switchProvider(providerCode);
    if (result.success) {
      // 刷新提供商信息
      await loadProviderInfo();
      await loadAvailableProviders();
      switchSuccessMessage.value = result.message;
      setTimeout(() => {
        switchSuccessMessage.value = '';
      }, 3000);
    } else {
      console.error('切换提供商失败:', result.error);
      switchSuccessMessage.value = result.error || '切换提供商失败';
      setTimeout(() => {
        switchSuccessMessage.value = '';
      }, 3000);
    }
  } catch (error: any) {
    console.error('切换提供商异常:', error);
    // 解析axios错误响应
    let errorMessage = '切换提供商失败';
    if (error.response && error.response.data) {
      errorMessage = error.response.data.message || error.response.data.error || '切换提供商失败';
    } else if (error.message) {
      errorMessage = error.message;
    }
    switchSuccessMessage.value = errorMessage;
    setTimeout(() => {
      switchSuccessMessage.value = '';
    }, 3000);
  } finally {
    switchingProvider.value = false;
    showProviderDropdown.value = false;
  }
};

// 获取提供商显示名称
const getProviderDisplayName = (code: string): string => {
  const provider = availableProviders.value.find(p => p.code === code);
  return provider ? provider.name : code;
};

// 获取提供商状态样式
const getProviderStatusClass = (code: string): string => {
  switch (code) {
    case 'openai':
      return 'bg-green-500';
    case 'baidu_wenxin':
      return 'bg-blue-500';
    case 'mock':
    default:
      return 'bg-gray-500';
  }
};

// 获取提供商描述
const getProviderDescription = (code: string): string => {
  switch (code) {
    case 'openai':
      return '基于OpenAI大语言模型';
    case 'baidu_wenxin':
      return '基于百度文心一言';
    case 'mock':
    default:
      return '基于关键词匹配的模拟回复';
  }
};

// 获取模式类型标签
const getModeTypeLabel = (code: string): string => {
  switch (code) {
    case 'openai':
    case 'baidu_wenxin':
      return 'AI接入模式';
    case 'mock':
    default:
      return '模拟模式';
  }
};

// 获取模式类型样式
const getModeTypeClass = (code: string): string => {
  switch (code) {
    case 'openai':
    case 'baidu_wenxin':
      return 'bg-gradient-to-r from-green-500/20 to-blue-500/20 text-green-400 border-green-500/30';
    case 'mock':
    default:
      return 'bg-gray-500/20 text-gray-400 border-gray-500/30';
  }
};

// 监听点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const dropdown = document.querySelector('.provider-dropdown-container');
  if (dropdown && !dropdown.contains(target)) {
    showProviderDropdown.value = false;
  }
};

onMounted(() => {
  loadProviderInfo();
  loadAvailableProviders();
  document.addEventListener('click', handleClickOutside);
});

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
const sendQuickQuestion = async (question: string) => {
  if (aiChatRef.value) {
    await aiChatRef.value.handleExternalQuestion(question);
  }
};
</script>

<template>
  <div class="ai-chat-container p-6 md:p-8">
    <!-- 页面标题 -->
    <header class="mb-8">
      <div class="flex items-center justify-between mb-2">
        <div class="flex items-center gap-4">
          <!-- Logo和标题 -->
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
              <Brain class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-3xl md:text-4xl font-bold">智能健康助手</h1>
              <p class="text-gray-400">随时为您提供健康建议和解答</p>
            </div>
          </div>
          
          <!-- AI模式切换器（移到左上角） -->
          <div class="provider-dropdown-container relative">
            <button
              @click.stop="showProviderDropdown = !showProviderDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-200 border"
              :class="getModeTypeClass(currentProvider?.code || 'mock')"
            >
              <div :class="['w-2.5 h-2.5 rounded-full', getProviderStatusClass(currentProvider?.code || 'mock')]"></div>
              <span class="font-medium text-sm">{{ getModeTypeLabel(currentProvider?.code || 'mock') }}</span>
              <ChevronDown 
                class="w-4 h-4 transition-transform duration-200" 
                :class="{ 'rotate-180': showProviderDropdown }"
              />
            </button>
            
            <!-- 下拉菜单 -->
            <Transition
              enter-active-class="transition-all duration-200 ease-out"
              enter-from-class="opacity-0 transform scale-95 translate-y-2"
              enter-to-class="opacity-100 transform scale-100 translate-y-0"
              leave-active-class="transition-all duration-150 ease-in"
              leave-from-class="opacity-100 transform scale-100 translate-y-0"
              leave-to-class="opacity-0 transform scale-95 translate-y-2"
            >
              <div
                v-show="showProviderDropdown"
                class="absolute left-0 mt-2 w-72 rounded-xl bg-gray-800/98 backdrop-blur-lg border border-white/10 shadow-xl overflow-hidden z-50"
              >
                <!-- 菜单头部 -->
                <div class="p-4 border-b border-white/10 bg-gradient-to-r from-indigo-500/10 to-purple-500/10">
                  <div class="flex items-center gap-2 mb-1">
                    <Sparkles class="w-4 h-4 text-indigo-400" />
                    <span class="font-semibold text-white">选择AI服务模式</span>
                  </div>
                  <p class="text-xs text-gray-400">选择智能助手的工作模式</p>
                </div>
                
                <!-- 模式列表 -->
                <div class="p-2">
                  <!-- 模拟模式 -->
                  <button
                    @click="handleSwitchProvider('mock')"
                    :disabled="switchingProvider"
                    class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-white/10 transition-all duration-200 text-left disabled:opacity-50 disabled:cursor-not-allowed"
                    :class="{ 'bg-indigo-500/20 border border-indigo-500/30': currentProvider?.code === 'mock' }"
                  >
                    <div class="w-10 h-10 rounded-lg bg-gray-500/20 flex items-center justify-center">
                      <Brain class="w-5 h-5 text-gray-400" />
                    </div>
                    <div class="flex-1">
                      <div class="font-medium text-white">模拟模式</div>
                      <div class="text-xs text-gray-400">基于预设规则和关键词匹配的模拟回复</div>
                    </div>
                    <Check 
                      v-if="currentProvider?.code === 'mock'" 
                      class="w-5 h-5 text-indigo-400" 
                    />
                    <RefreshCw 
                      v-if="switchingProvider && currentProvider?.code !== 'mock'" 
                      class="w-5 h-5 text-indigo-400 animate-spin" 
                    />
                  </button>
                  
                  <!-- 分隔线 -->
                  <div class="my-2 border-t border-white/5"></div>
                  
                  <!-- AI接入模式 -->
                  <div class="mb-2">
                    <p class="px-2 text-xs text-gray-500 mb-2 flex items-center gap-1">
                      <Sparkles class="w-3 h-3" />
                      AI接入模式
                    </p>
                    <button
                      v-for="provider in availableProviders.filter(p => p.code !== 'mock')"
                      :key="provider.code"
                      @click="handleSwitchProvider(provider.code)"
                      :disabled="switchingProvider"
                      class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-white/10 transition-all duration-200 text-left disabled:opacity-50 disabled:cursor-not-allowed"
                      :class="{ 'bg-indigo-500/20 border border-indigo-500/30': currentProvider?.code === provider.code }"
                    >
                      <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', 
                        provider.code === 'openai' ? 'bg-green-500/20' : 'bg-blue-500/20']">
                        <Sparkles :class="['w-5 h-5', provider.code === 'openai' ? 'text-green-400' : 'text-blue-400']" />
                      </div>
                      <div class="flex-1">
                        <div class="font-medium text-white">{{ provider.name }}</div>
                        <div class="text-xs text-gray-400">{{ getProviderDescription(provider.code) }}</div>
                      </div>
                      <Check 
                        v-if="currentProvider?.code === provider.code" 
                        :class="['w-5 h-5', provider.code === 'openai' ? 'text-green-400' : 'text-blue-400']" 
                      />
                      <RefreshCw 
                        v-if="switchingProvider && currentProvider?.code !== provider.code" 
                        :class="['w-5 h-5 animate-spin', provider.code === 'openai' ? 'text-green-400' : 'text-blue-400']" 
                      />
                    </button>
                    
                    <!-- 无AI提供商时的提示 -->
                    <div 
                      v-if="availableProviders.filter(p => p.code !== 'mock').length === 0"
                      class="p-4 rounded-lg bg-yellow-500/10 border border-yellow-500/20"
                    >
                      <p class="text-sm text-yellow-400">
                        暂无可用的AI接入服务
                      </p>
                      <p class="text-xs text-gray-400 mt-1">
                        请在后端配置API密钥以启用AI接入模式
                      </p>
                    </div>
                  </div>
                </div>
                
                <!-- 菜单底部提示 -->
                <div class="border-t border-white/10 p-3">
                  <div class="flex items-center gap-2 text-xs text-gray-500">
                    <div :class="['w-2 h-2 rounded-full', getProviderStatusClass(currentProvider?.code || 'mock')]"></div>
                    <span>当前模式：{{ currentProvider?.name || '加载中...' }}</span>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
        
        <!-- 切换成功提示 -->
        <Transition
          enter-active-class="transition-all duration-300 ease-out"
          enter-from-class="opacity-0 transform translate-y-2"
          enter-to-class="opacity-100 transform translate-y-0"
          leave-active-class="transition-all duration-200 ease-in"
          leave-from-class="opacity-100 transform translate-y-0"
          leave-to-class="opacity-0 transform translate-y-2"
        >
          <div 
            v-if="switchSuccessMessage"
            class="absolute top-16 right-4 px-4 py-2 rounded-lg bg-green-500/20 border border-green-500/30 text-green-400 text-sm flex items-center gap-2"
          >
            <Check class="w-4 h-4" />
            {{ switchSuccessMessage }}
          </div>
        </Transition>
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
          <AIChat ref="aiChatRef" />
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

/* 下拉菜单动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 点击外部关闭下拉菜单 */
.dropdown-backdrop {
  position: fixed;
  inset: 0;
  z-index: 40;
}

/* 模式切换器按钮样式 */
.provider-dropdown-container button {
  min-width: 140px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .provider-dropdown-container {
    margin-top: 0.5rem;
  }
  
  .provider-dropdown-container button {
    min-width: 120px;
    padding: 0.5rem 1rem;
  }
  
  .ai-chat-container header {
    flex-wrap: wrap;
  }
}
</style>