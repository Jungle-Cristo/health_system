<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Trash2, TrendingUp, Calendar } from 'lucide-vue-next';
import { ElMessage, ElMessageBox, ElSelect, ElOption } from 'element-plus';
import { 
  getHealthDataList, 
  deleteHealthData, 
  healthDataTypes,
  getLabelByType,
  type HealthDataResponse 
} from '../../api/health';

const props = defineProps<{
  refreshTrigger?: number;
}>();

const emit = defineEmits<{
  deleted: [];
}>();

const dataList = ref<HealthDataResponse[]>([]);
const loading = ref(false);
const selectedType = ref('steps');

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    console.log('加载健康数据，类型:', selectedType.value);
    const res = await getHealthDataList(
      selectedType.value,
      undefined,
      undefined
    );
    console.log('健康数据响应:', res);
    dataList.value = res.data || [];
    console.log('健康数据列表:', dataList.value);
  } catch (error) {
    console.error('加载健康数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 删除数据
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条健康数据吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deleteHealthData(id);
    ElMessage.success('删除成功');
    
    // 重新加载数据
    await loadData();
    emit('deleted');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

import { formatDate, formatTime } from '../../lib/utils';

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    steps: 'bg-blue-500',
    heart_rate: 'bg-red-500',
    sleep: 'bg-purple-500',
    weight: 'bg-green-500',
    blood_pressure: 'bg-yellow-500',
    blood_sugar: 'bg-orange-500'
  };
  return colors[type] || 'bg-gray-500';
};

// 监听刷新触发器和类型变化
watch(() => props.refreshTrigger, loadData, { immediate: true });
watch(selectedType, loadData);

onMounted(loadData);
</script>

<template>
  <div class="data-list">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-white flex items-center gap-2">
        <TrendingUp class="w-5 h-5 text-indigo-400" />
        健康数据记录
      </h3>
      
      <!-- 类型筛选 -->
      <el-select
        v-model="selectedType"
        class="text-sm"
        style="--el-select-bg-color: rgba(255, 255, 255, 0.05); --el-select-border-color: rgba(255, 255, 255, 0.1); --el-select-text-color: white; --el-select-hover-border-color: rgba(255, 255, 255, 0.3);"
      >
        <el-option 
          v-for="t in healthDataTypes" 
          :key="t.value" 
          :label="t.label" 
          :value="t.value"
          style="--el-option-bg-color: #1e293b; --el-option-text-color: white; --el-option-hover-bg-color: rgba(255, 255, 255, 0.1);"
        />
      </el-select>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-8">
      <div class="animate-spin w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full mx-auto"></div>
      <p class="text-gray-400 mt-2">加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="dataList.length === 0" class="text-center py-8">
      <div class="w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
        <Calendar class="w-8 h-8 text-gray-400" />
      </div>
      <p class="text-gray-400">暂无健康数据</p>
      <p class="text-gray-500 text-sm mt-1">开始记录您的第一条健康数据吧</p>
    </div>

    <!-- 数据列表 -->
    <div v-else class="space-y-3 max-h-96 overflow-y-auto">
      <div
        v-for="item in dataList"
        :key="item.id"
        class="glass-card p-4 rounded-xl hover:bg-white/5 transition-colors group"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <!-- 类型标签 -->
            <div 
              class="w-10 h-10 rounded-lg flex items-center justify-center text-white text-sm font-bold"
              :class="getTypeColor(item.type)"
            >
              {{ getLabelByType(item.type).charAt(0) }}
            </div>
            
            <div>
              <div class="flex items-center gap-2">
                <span class="text-white font-medium">
                  {{ getLabelByType(item.type) }}
                </span>
                <span class="text-2xl font-bold text-white">
                  {{ item.value }}
                  <span class="text-sm text-gray-400 font-normal">{{ item.unit }}</span>
                </span>
              </div>
              <div class="flex items-center gap-2 text-sm text-gray-400 mt-1">
                <Calendar class="w-3 h-3" />
                <span>{{ formatDate(item.recordDate) }}</span>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 删除按钮 -->
          <button
            @click="handleDelete(item.id)"
            class="p-2 text-gray-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all opacity-0 group-hover:opacity-100"
          >
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
        
        <!-- 备注 -->
        <p v-if="item.notes" class="mt-2 text-sm text-gray-400 pl-13">
          {{ item.notes }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.data-list {
  width: 100%;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

/* 滚动条样式 */
.max-h-96::-webkit-scrollbar {
  width: 6px;
}

.max-h-96::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.max-h-96::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.max-h-96::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.pl-13 {
  padding-left: 3.25rem;
}
</style>
