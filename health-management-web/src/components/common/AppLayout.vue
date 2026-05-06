<script setup lang="ts">
import { useRouter } from 'vue-router';
import { LogOut } from 'lucide-vue-next';
import { clearAuth, logout } from '../../api/auth';

const props = withDefaults(defineProps<{
  title: string;
  subtitle?: string;
  showBack?: boolean;
  backPath?: string;
}>(), {
  subtitle: '',
  showBack: false,
  backPath: '/',
});

const router = useRouter();

const handleLogout = async () => {
  try { await logout(); } catch { /* ignore */ }
  clearAuth();
  router.push('/login');
};
</script>

<template>
  <div class="app-layout p-6 md:p-8">
    <header class="mb-8">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <slot name="header-left">
            <div>
              <h1 class="text-3xl md:text-4xl font-bold">{{ title }}</h1>
              <p v-if="subtitle" class="text-gray-400">{{ subtitle }}</p>
            </div>
          </slot>
        </div>
        <button
          @click="handleLogout"
          class="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 border border-red-500/30 text-red-400 transition-colors flex-shrink-0"
        >
          <LogOut class="w-4 h-4" />
          <span class="hidden sm:inline">退出登录</span>
        </button>
      </div>
    </header>
    <slot />
  </div>
</template>

<style scoped>
.app-layout {
  max-width: 1440px;
  margin: 0 auto;
  min-height: 100vh;
}
</style>
