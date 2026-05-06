<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Home } from 'lucide-vue-next';

const router = useRouter();
</script>

<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <h1 class="text-8xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent mb-4">404</h1>
      <p class="text-xl text-gray-400 mb-8">页面未找到</p>
      <button
        @click="router.push('/')"
        class="inline-flex items-center gap-2 px-6 py-3 rounded-lg bg-indigo-500/20 hover:bg-indigo-500/30 border border-indigo-500/30 text-indigo-400 transition-colors"
      >
        <Home class="w-5 h-5" />
        返回首页
      </button>
    </div>
  </div>
</template>
