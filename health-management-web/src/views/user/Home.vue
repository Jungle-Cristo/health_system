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

// 获取今日统计数据
const getTodayStat = (type: string) => {
  const typeData = todayHealthData.value.filter(item => item.type === type);
  if (typeData.length === 0) return '-';
  
  // 对于步数，返回总和；对于其他指标，返回平均值
  if (type === 'steps') {
    const total = typeData.reduce((sum, item) => sum + item.value, 0);
    return total.toLocaleString();
  } else {
    const avg = typeData.reduce((sum, item) => sum + item.value, 0) / typeData.length;
    return avg.toFixed(1);
  }
};

// 统计数据
const stats = computed(() => [
  { 
    label: '今日步数', 
    value: getTodayStat('steps'), 
    unit: '步', 
    icon: Footprints, 
    color: 'text-blue-400',
    bgColor: 'bg-blue-500'
  },
  { 
    label: '平均心率', 
    value: getTodayStat('heart_rate'), 
    unit: 'bpm', 
    icon: Heart, 
    color: 'text-red-400',
    bgColor: 'bg-red-500'
  },
  { 
    label: '睡眠时长', 
    value: getTodayStat('sleep'), 
    unit: '小时', 
    icon: Moon, 
    color: 'text-purple-400',
    bgColor: 'bg-purple-500'
  },
  { 
    label: '当前体重', 
    value: getTodayStat('weight'), 
    unit: 'kg', 
    icon: Scale, 
    color: 'text-green-400',
    bgColor: 'bg-green-500'
  }
]);

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

    <!-- 今日统计卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <GlassCard 
        v-for="stat in stats" 
        :key="stat.label" 
        class="flex flex-col items-center justify-center p-4 md:p-6 hover:bg-white/10 transition-colors group"
      >
        <div :class="['w-10 h-10 rounded-lg flex items-center justify-center mb-3', stat.bgColor]">
          <component :is="stat.icon" class="w-5 h-5 text-white" />
        </div>
        <div class="flex items-baseline gap-1">
          <span class="text-2xl md:text-3xl font-bold">{{ stat.value }}</span>
          <span class="text-sm text-gray-400">{{ stat.unit }}</span>
        </div>
        <span class="text-xs text-gray-400 mt-1">{{ stat.label }}</span>
      </GlassCard>
    </div>

    <!-- 健康指标统计 -->
    <GlassCard class="mb-6 p-6">
      <HealthStats :refresh-trigger="refreshTrigger" />
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
