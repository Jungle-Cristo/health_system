<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import GlassCard from '../../components/common/GlassCard.vue';
import TrendChart from '../../components/health/TrendChart.vue';
import DataInputForm from '../../components/health/DataInputForm.vue';
import DataList from '../../components/health/DataList.vue';
import HealthStats from '../../components/health/HealthStats.vue';
import HealthAlerts from '../../components/health/HealthAlerts.vue';
import { Activity, Heart, Moon, Footprints, Scale, Gauge, Droplets, User } from 'lucide-vue-next';
import { getHealthDataList, getLabelByType, getUnitByType, type HealthDataResponse } from '../../api/health';

// 刷新触发器
const refreshTrigger = ref(0);
const todayHealthData = ref<HealthDataResponse[]>([]);

// 获取用户名
const username = ref('User');
onMounted(() => {
  const storedUsername = localStorage.getItem('username');
  if (storedUsername) {
    username.value = storedUsername;
  }
  loadTodayData();
});

// 加载今日数据
const loadTodayData = async () => {
  try {
    const today = new Date().toISOString().split('T')[0];
    const res = await getHealthDataList('', today, today);
    todayHealthData.value = res.data || [];
  } catch (error) {
    console.error('加载今日数据失败:', error);
  }
};

// 刷新数据
const handleDataChange = () => {
  refreshTrigger.value++;
  loadTodayData();
};



import { Brain, MessageSquare } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

const router = useRouter();

// 导航到智能助手
const navigateToAIChat = () => {
  router.push('/ai-chat');
};

// 所有健康指标
const allHealthMetrics = [
  { type: 'steps', label: '步数', icon: Footprints, color: 'text-blue-400', bgColor: 'bg-blue-500' },
  { type: 'heart_rate', label: '心率', icon: Heart, color: 'text-red-400', bgColor: 'bg-red-500' },
  { type: 'sleep', label: '睡眠', icon: Moon, color: 'text-purple-400', bgColor: 'bg-purple-500' },
  { type: 'weight', label: '体重', icon: Scale, color: 'text-green-400', bgColor: 'bg-green-500' },
  { type: 'blood_pressure', label: '血压', icon: Gauge, color: 'text-yellow-400', bgColor: 'bg-yellow-500' },
  { type: 'blood_sugar', label: '血糖', icon: Droplets, color: 'text-orange-400', bgColor: 'bg-orange-500' }
];
</script>

<template>
  <div class="home-container p-6 md:p-8">
    <!-- 欢迎标题 -->
    <header class="mb-8">
      <div class="flex items-center gap-3 mb-2">
        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
          <User class="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 class="text-3xl md:text-4xl font-bold">你好, {{ username }}!</h1>
          <p class="text-gray-400">这是您今天的健康概览</p>
        </div>
      </div>
    </header>

    <!-- 健康提醒 -->
    <HealthAlerts :refresh-trigger="refreshTrigger" />



    <!-- 健康指标统计 -->
    <GlassCard class="mb-6 p-6">
      <HealthStats :refresh-trigger="refreshTrigger" />
    </GlassCard>

    <!-- 智能健康助手卡片 -->
    <GlassCard class="mb-6 p-6 hover:bg-white/10 transition-colors cursor-pointer" @click="navigateToAIChat">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
          <Brain class="w-6 h-6 text-white" />
        </div>
        <div class="flex-1">
          <h3 class="text-lg font-semibold text-white mb-1">智能健康助手</h3>
          <p class="text-gray-400 text-sm">有健康问题？随时咨询AI助手，获取专业建议</p>
        </div>
        <div class="text-indigo-400">
          <MessageSquare class="w-5 h-5" />
        </div>
      </div>
    </GlassCard>

    <!-- 主内容区 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：趋势图表 -->
      <GlassCard class="lg:col-span-2 p-6">
        <TrendChart :refresh-trigger="refreshTrigger" />
      </GlassCard>

      <!-- 右侧：数据录入 -->
      <GlassCard class="p-6">
        <DataInputForm @success="handleDataChange" />
      </GlassCard>
    </div>

    <!-- 底部：数据列表 -->
    <GlassCard class="mt-6 p-6">
      <DataList 
        :refresh-trigger="refreshTrigger" 
        @deleted="handleDataChange"
      />
    </GlassCard>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1440px;
  margin: 0 auto;
  min-height: 100vh;
}
</style>
