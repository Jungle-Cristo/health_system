<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import GlassCard from '../components/common/GlassCard.vue';
import { User, Lock, ArrowRight } from 'lucide-vue-next';
import { login } from '../api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const account = ref('');
const password = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  if (!account.value || !password.value) {
    ElMessage.warning('请输入账号和密码');
    return;
  }

  try {
    isLoading.value = true;
    const res = await login({
      account: account.value,
      password: password.value
    });
    
    ElMessage.success('登录成功');
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('userId', res.data.userId.toString());
    localStorage.setItem('username', res.data.username);
    
    router.push('/');
  } catch (err) {
    // Error handled by interceptor
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-container flex items-center justify-center">
    <GlassCard class="w-full max-w-md p-8" :blur="30">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold mb-2">Welcome Back</h1>
        <p class="text-gray-400">Sign in to track your health</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div class="space-y-2">
          <label class="block text-sm font-medium">Account</label>
          <div class="relative">
            <User class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="account"
              type="text"
              class="glass-input w-full pl-10 pr-4 py-3 rounded-xl outline-none"
              placeholder="Username, email, or phone"
              required
            />
          </div>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-medium">Password</label>
          <div class="relative">
            <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="password"
              type="password"
              class="glass-input w-full pl-10 pr-4 py-3 rounded-xl outline-none"
              placeholder="Enter your password"
              required
            />
          </div>
        </div>

        <button
          type="submit"
          class="glass-button w-full py-4 rounded-xl flex items-center justify-center space-x-2 group"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">Sign In</span>
          <span v-else class="animate-pulse">Signing in...</span>
          <ArrowRight v-if="!isLoading" class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
        </button>
      </form>

      <div class="mt-8 text-center">
        <p class="text-gray-400">
          Don't have an account?
          <router-link to="/register" class="text-indigo-400 hover:text-indigo-300 transition-colors">Create one</router-link>
        </p>
      </div>
    </GlassCard>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  padding: 1.5rem;
  background: transparent;
}

.glass-input {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  transition: all 0.3s ease;
}

.glass-input:focus {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.2);
}

.glass-button {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(124, 58, 237, 0.8));
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.4);
  filter: brightness(1.1);
}

.glass-button:active:not(:disabled) {
  transform: translateY(0);
}

.glass-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
