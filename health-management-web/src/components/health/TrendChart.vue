<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';

const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

const props = defineProps<{
  data: number[];
  labels: string[];
  title: string;
  color?: string;
}>();

const initChart = () => {
  if (!chartRef.value) return;
  
  chart = echarts.init(chartRef.value, 'dark');
  const option = {
    backgroundColor: 'transparent',
    title: {
      text: props.title,
      textStyle: { color: '#94a3b8', fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.8)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#f8fafc' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.labels,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.05)' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        name: props.title,
        type: 'line',
        smooth: true,
        data: props.data,
        itemStyle: { color: props.color || '#6366f1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: props.color || '#6366f1' + '88' },
            { offset: 1, color: props.color || '#6366f1' + '00' }
          ])
        },
        lineStyle: { width: 3 }
      }
    ]
  };
  
  chart.setOption(option);
};

const handleResize = () => {
  chart?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chart?.dispose();
});

watch(() => props.data, () => {
  chart?.setOption({
    series: [{ data: props.data }]
  });
});
</script>

<template>
  <div ref="chartRef" class="w-full h-full min-h-[300px]"></div>
</template>
