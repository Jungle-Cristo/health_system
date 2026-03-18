<script setup lang="ts">
import GlassCard from '../../components/common/GlassCard.vue';
import TrendChart from '../../components/health/TrendChart.vue';
import DataInputForm from '../../components/health/DataInputForm.vue';
import { Activity, Heart, Moon, Footprints } from 'lucide-vue-next';

const stats = [
  { label: 'Steps', value: '8,432', unit: 'steps', icon: Footprints, color: 'text-blue-400' },
  { label: 'Heart Rate', value: '72', unit: 'bpm', icon: Heart, color: 'text-red-400' },
  { label: 'Sleep', value: '7.5', unit: 'hrs', icon: Moon, color: 'text-purple-400' },
  { label: 'Activity', value: '45', unit: 'min', icon: Activity, color: 'text-green-400' }
];

const chartData = [1200, 1500, 1100, 1800, 2000, 1600, 2400];
const chartLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
</script>

<template>
  <div class="home-container p-8">
    <header class="mb-12">
      <h1 class="text-4xl font-bold mb-2">Hello, Jungle!</h1>
      <p class="text-gray-400">Here's your health summary for today.</p>
    </header>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
      <GlassCard v-for="stat in stats" :key="stat.label" class="flex flex-col items-center justify-center p-6">
        <component :is="stat.icon" :class="['w-8 h-8 mb-4', stat.color]" />
        <span class="text-3xl font-bold mb-1">{{ stat.value }}</span>
        <span class="text-sm text-gray-400 uppercase tracking-wider">{{ stat.label }}</span>
      </GlassCard>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <GlassCard class="lg:col-span-2 min-h-[400px]">
        <h2 class="text-xl font-bold mb-6 text-gray-200">Health Trends</h2>
        <TrendChart :data="chartData" :labels="chartLabels" title="Weekly Activity" color="#6366f1" />
      </GlassCard>

      <div class="space-y-8">
        <GlassCard>
          <h2 class="text-xl font-bold mb-6 text-gray-200">Quick Record</h2>
          <DataInputForm />
        </GlassCard>

        <GlassCard>
          <h2 class="text-xl font-bold mb-6 text-gray-200">Daily Goals</h2>
          <div class="space-y-6">
            <div v-for="i in 3" :key="i" class="space-y-2">
              <div class="flex justify-between text-sm text-gray-400">
                <span>Water Intake</span>
                <span>75%</span>
              </div>
              <div class="w-full bg-white/10 rounded-full h-2">
                <div class="bg-indigo-500 h-2 rounded-full w-3/4"></div>
              </div>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1440px;
  margin: 0 auto;
}
</style>
