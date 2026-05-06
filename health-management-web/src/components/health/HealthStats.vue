<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import {
  TrendingUp, TrendingDown, Minus, Activity
} from 'lucide-vue-next';
import GlassCard from '../common/GlassCard.vue';
import { getHealthDataList, type HealthDataResponse } from '../../api/health';
import { healthMetrics as metricsConfig } from '../../config/healthMetrics';

const props = defineProps<{
  refreshTrigger?: number;
}>();

const loading = ref(false);
const allHealthData = ref<HealthDataResponse[]>([]);
const selectedPeriod = ref<'day' | 'week' | 'month'>('day');

// 加载所有健康数据
const loadAllData = async () => {
  loading.value = true;
  try {
    const res = await getHealthDataList('', undefined, undefined);
    allHealthData.value = res.data || [];
  } catch (error) {
    console.error('加载健康统计数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 根据时间段筛选数据
const getFilteredDataByPeriod = (data: HealthDataResponse[], period: 'day' | 'week' | 'month') => {
  const now = new Date();
  const startDate = new Date();
  
  switch (period) {
    case 'day':
      startDate.setHours(0, 0, 0, 0);
      break;
    case 'week':
      startDate.setDate(now.getDate() - 7);
      break;
    case 'month':
      startDate.setMonth(now.getMonth() - 1);
      break;
  }
  
  return data.filter(item => new Date(item.recordDate) >= startDate);
};

// 计算统计数据
const calculateStats = (type: string) => {
  const typeData = getFilteredDataByPeriod(
    allHealthData.value.filter(item => item.type === type),
    selectedPeriod.value
  );
  
  if (typeData.length === 0) {
    return { current: '-', average: '-', min: '-', max: '-', trend: 'stable' as const };
  }
  
  const values = typeData.map(item => item.value);
  const current = values[0];
  const average = values.reduce((a, b) => a + b, 0) / values.length;
  const min = Math.min(...values);
  const max = Math.max(...values);
  
  // 计算趋势
  let trend: 'up' | 'down' | 'stable' = 'stable';
  if (values.length >= 2) {
    const prev = values[1];
    const diff = ((current - prev) / prev) * 100;
    if (diff > 5) trend = 'up';
    else if (diff < -5) trend = 'down';
  }
  
  return {
    current: current.toFixed(1),
    average: average.toFixed(1),
    min: min.toFixed(1),
    max: max.toFixed(1),
    trend
  };
};

const healthMetrics = computed(() =>
  metricsConfig.map(m => ({
    ...m,
    bgColor: m.color.replace('text-', 'bg-'),
    stats: calculateStats(m.type),
    thresholds: {
      min: m.thresholds.low * 0.5,
      max: m.thresholds.high * 1.5,
      warningMin: m.thresholds.low,
      warningMax: m.thresholds.high,
    },
  }))
);

// 获取趋势图标
const getTrendIcon = (trend: 'up' | 'down' | 'stable') => {
  switch (trend) {
    case 'up': return TrendingUp;
    case 'down': return TrendingDown;
    default: return Minus;
  }
};

// 获取趋势颜色
const getTrendColor = (trend: 'up' | 'down' | 'stable', type: string) => {
  // 对于某些指标，上升趋势可能是好的，对于其他指标可能是坏的
  const goodUp = ['steps', 'sleep'];
  const badUp = ['heart_rate', 'blood_pressure', 'blood_sugar'];
  
  if (trend === 'stable') return 'text-gray-400';
  
  if (goodUp.includes(type)) {
    return trend === 'up' ? 'text-green-400' : 'text-red-400';
  } else if (badUp.includes(type)) {
    return trend === 'up' ? 'text-red-400' : 'text-green-400';
  }
  
  return trend === 'up' ? 'text-green-400' : 'text-red-400';
};

// 检查是否异常
const isAbnormal = (metric: any) => {
  if (metric.stats.current === '-') return false;
  const current = parseFloat(metric.stats.current);
  return current < metric.thresholds.warningMin || current > metric.thresholds.warningMax;
};

// 监听刷新触发器
watch(() => props.refreshTrigger, loadAllData, { immediate: true });

onMounted(loadAllData);
</script>

<template>
  <div class="health-stats">
    <!-- 时间段选择 -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-semibold text-white flex items-center gap-2">
        <Activity class="w-5 h-5 text-indigo-400" />
        健康指标统计
      </h3>
      
      <div class="flex gap-2">
        <button
          v-for="period in ['day', 'week', 'month']"
          :key="period"
          @click="selectedPeriod = period as any"
          class="px-3 py-1.5 text-sm rounded-lg transition-colors"
          :class="selectedPeriod === period ? 'bg-indigo-500 text-white' : 'bg-white/5 text-gray-400 hover:bg-white/10'"
        >
          {{ period === 'day' ? '今日' : period === 'week' ? '本周' : '本月' }}
        </button>
      </div>
    </div>

    <!-- 统计卡片网格 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <GlassCard
        v-for="metric in healthMetrics"
        :key="metric.type"
        class="p-4 hover:bg-white/10 transition-colors"
        :class="{ 'border-red-500/50': isAbnormal(metric) }"
      >
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div 
              class="w-10 h-10 rounded-lg flex items-center justify-center"
              :class="metric.bgColor"
            >
              <component :is="metric.icon" class="w-5 h-5 text-white" />
            </div>
            <div>
              <h4 class="text-sm text-gray-400">{{ metric.label }}</h4>
              <div class="flex items-baseline gap-1">
                <span class="text-2xl font-bold text-white">{{ metric.stats.current }}</span>
                <span class="text-sm text-gray-400">{{ metric.unit }}</span>
              </div>
            </div>
          </div>
          
          <!-- 趋势指示器 -->
          <div v-if="metric.stats.current !== '-'" class="flex items-center gap-1">
            <component 
              :is="getTrendIcon(metric.stats.trend)" 
              :class="['w-4 h-4', getTrendColor(metric.stats.trend, metric.type)]"
            />
          </div>
        </div>

        <!-- 详细统计 -->
        <div v-if="metric.stats.current !== '-'" class="mt-4 grid grid-cols-3 gap-2 text-center">
          <div class="bg-white/5 rounded-lg p-2">
            <div class="text-xs text-gray-400">平均</div>
            <div class="text-sm font-medium text-white">{{ metric.stats.average }}</div>
          </div>
          <div class="bg-white/5 rounded-lg p-2">
            <div class="text-xs text-gray-400">最低</div>
            <div class="text-sm font-medium text-white">{{ metric.stats.min }}</div>
          </div>
          <div class="bg-white/5 rounded-lg p-2">
            <div class="text-xs text-gray-400">最高</div>
            <div class="text-sm font-medium text-white">{{ metric.stats.max }}</div>
          </div>
        </div>

        <!-- 异常警告 -->
        <div v-if="isAbnormal(metric)" class="mt-3 flex items-center gap-2 text-red-400 text-sm">
          <span class="w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
          <span>数值异常，建议关注</span>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.health-stats {
  width: 100%;
}
</style>