<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { User, Brain, MessageSquare } from 'lucide-vue-next';
import AppLayout from '../../components/common/AppLayout.vue';
import GlassCard from '../../components/common/GlassCard.vue';
import TrendChart from '../../components/health/TrendChart.vue';
import DataInputForm from '../../components/health/DataInputForm.vue';
import DataList from '../../components/health/DataList.vue';
import HealthStats from '../../components/health/HealthStats.vue';
import HealthAlerts from '../../components/health/HealthAlerts.vue';
import { getHealthDataList, type HealthDataResponse } from '../../api/health';

const router = useRouter();
const refreshTrigger = ref(0);
const todayHealthData = ref<HealthDataResponse[]>([]);
const username = ref('User');

onMounted(() => {
  username.value = localStorage.getItem('username') || 'User';
  loadTodayData();
});

const loadTodayData = async () => {
  try {
    const today = new Date().toISOString().split('T')[0];
    const res = await getHealthDataList('', today, today);
    todayHealthData.value = res.data || [];
  } catch {
    // 静默失败
  }
};

const handleDataChange = () => {
  refreshTrigger.value++;
  loadTodayData();
};
</script>

<template>
  <AppLayout :title="`你好, ${username}!`" subtitle="这是您今天的健康概览">
    <template #header-left>
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
          <User class="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 class="text-3xl md:text-4xl font-bold">你好, {{ username }}!</h1>
          <p class="text-gray-400">这是您今天的健康概览</p>
        </div>
      </div>
    </template>

    <HealthAlerts :refresh-trigger="refreshTrigger" />

    <GlassCard class="mb-6 p-6">
      <HealthStats :refresh-trigger="refreshTrigger" />
    </GlassCard>

    <GlassCard class="mb-6 p-6 hover:bg-white/10 transition-colors cursor-pointer" @click="router.push('/ai-chat')">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
          <Brain class="w-6 h-6 text-white" />
        </div>
        <div class="flex-1">
          <h3 class="text-lg font-semibold text-white mb-1">智能健康助手</h3>
          <p class="text-gray-400 text-sm">有健康问题？随时咨询AI助手，获取专业建议</p>
        </div>
        <MessageSquare class="w-5 h-5 text-indigo-400" />
      </div>
    </GlassCard>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <GlassCard class="lg:col-span-2 p-6">
        <TrendChart :refresh-trigger="refreshTrigger" />
      </GlassCard>
      <GlassCard class="p-6">
        <DataInputForm @success="handleDataChange" />
      </GlassCard>
    </div>

    <GlassCard class="mt-6 p-6">
      <DataList :refresh-trigger="refreshTrigger" @deleted="handleDataChange" />
    </GlassCard>
  </AppLayout>
</template>
