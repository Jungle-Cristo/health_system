<script setup lang="ts">
import { ref } from 'vue';
import { Plus, Check } from 'lucide-vue-next';

const recordType = ref('steps');
const value = ref('');
const unit = ref('steps');
const isSubmitting = ref(false);
const isSuccess = ref(false);

const types = [
  { label: 'Steps', value: 'steps', unit: 'steps' },
  { label: 'Heart Rate', value: 'heart_rate', unit: 'bpm' },
  { label: 'Sleep', value: 'sleep', unit: 'hrs' },
  { label: 'Weight', value: 'weight', unit: 'kg' }
];

const updateUnit = () => {
  const selected = types.find(t => t.value === recordType.value);
  if (selected) unit.value = selected.unit;
};

const handleSubmit = () => {
  isSubmitting.value = true;
  // Mock API call
  setTimeout(() => {
    isSubmitting.value = false;
    isSuccess.value = true;
    value.value = '';
    setTimeout(() => (isSuccess.value = false), 2000);
  }, 1000);
};
</script>

<template>
  <div class="data-input-form space-y-6">
    <div class="space-y-2">
      <label class="block text-sm font-medium text-gray-400">Record Type</label>
      <select
        v-model="recordType"
        @change="updateUnit"
        class="glass-input w-full px-4 py-3 rounded-xl outline-none appearance-none cursor-pointer"
      >
        <option v-for="t in types" :key="t.value" :value="t.value">{{ t.label }}</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="block text-sm font-medium text-gray-400">Value</label>
      <div class="relative">
        <input
          v-model="value"
          type="number"
          class="glass-input w-full px-4 py-3 pr-16 rounded-xl outline-none"
          placeholder="Enter value"
          required
        />
        <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">
          {{ unit }}
        </span>
      </div>
    </div>

    <button
      @click="handleSubmit"
      class="glass-button w-full py-4 rounded-xl flex items-center justify-center space-x-2"
      :disabled="isSubmitting"
    >
      <Check v-if="isSuccess" class="w-5 h-5 text-green-400" />
      <Plus v-else-if="!isSubmitting" class="w-5 h-5" />
      <span v-if="isSubmitting" class="animate-pulse">Saving...</span>
      <span v-else-if="isSuccess">Saved Successfully!</span>
      <span v-else>Record Health Data</span>
    </button>
  </div>
</template>

<style scoped>
select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 1.5rem;
}

option {
  background-color: #1e293b;
  color: white;
}
</style>
