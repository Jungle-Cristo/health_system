<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import GlassCard from '../components/common/GlassCard.vue';
import { User, Lock, Mail, ArrowRight, Phone } from 'lucide-vue-next';
import { register } from '../api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const form = ref({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

const errors = ref({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

const isLoading = ref(false);

const passwordStrength = computed(() => {
  const p = form.value.password;
  if (!p) return 0;
  let strength = 0;
  if (p.length >= 8) strength += 25;
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) strength += 25;
  if (/\d/.test(p)) strength += 25;
  if (/[@$!%*?&]/.test(p)) strength += 25;
  return strength;
});

const strengthColor = computed(() => {
  if (passwordStrength.value <= 33) return 'bg-red-500';
  if (passwordStrength.value <= 66) return 'bg-yellow-500';
  return 'bg-green-500';
});

const strengthLabel = computed(() => {
  if (!form.value.password) return '未输入';
  if (passwordStrength.value <= 33) return '弱';
  if (passwordStrength.value <= 66) return '中等';
  return '强';
});

const validateForm = () => {
  let isValid = true;
  errors.value = { username: '', email: '', phone: '', password: '', confirmPassword: '' };

  if (!form.value.username) {
    errors.value.username = '请输入用户名';
    isValid = false;
  } else if (form.value.username.trim().length > 50) {
    errors.value.username = '用户名长度不能超过50';
    isValid = false;
  }
  if (!form.value.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = '请输入正确的邮箱';
    isValid = false;
  } else if (form.value.email.trim().length > 100) {
    errors.value.email = '邮箱长度不能超过100';
    isValid = false;
  }
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    errors.value.phone = '手机号格式不正确';
    isValid = false;
  }
  if (!form.value.password) {
    errors.value.password = '请输入密码';
    isValid = false;
  } else if (form.value.password.length > 72) {
    errors.value.password = '密码长度不能超过72';
    isValid = false;
  }
  if (!form.value.confirmPassword) {
    errors.value.confirmPassword = '请确认密码';
    isValid = false;
  } else if (form.value.password !== form.value.confirmPassword) {
    errors.value.confirmPassword = '两次输入的密码不一致';
    isValid = false;
  }
  return isValid;
};

const handleRegister = async () => {
  if (!validateForm()) return;

  try {
    isLoading.value = true;
    const res = await register({
      username: form.value.username.trim(),
      email: form.value.email.trim(),
      password: form.value.password,
      phone: form.value.phone.trim() ? form.value.phone.trim() : undefined
    });
    
    ElMessage.success('Registration successful! Welcome aboard.');
    // Save token and auto login
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
  <div class="register-container flex items-center justify-center">
    <GlassCard class="w-full max-w-lg p-8" :blur="30">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold mb-2">Create Account</h1>
        <p class="text-gray-400">Join us for a healthier life</p>
      </div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <!-- Username -->
        <div class="space-y-1">
          <label class="block text-sm font-medium">Username</label>
          <div class="relative">
            <User class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="form.username"
              type="text"
              class="glass-input w-full pl-10 pr-4 py-2.5 rounded-xl outline-none"
              :class="{ 'border-red-500/50': errors.username }"
              placeholder="Pick a username"
            />
          </div>
          <p v-if="errors.username" class="text-xs text-red-400 mt-1 ml-1">{{ errors.username }}</p>
        </div>

        <!-- Email -->
        <div class="space-y-1">
          <label class="block text-sm font-medium">Email</label>
          <div class="relative">
            <Mail class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="form.email"
              type="email"
              class="glass-input w-full pl-10 pr-4 py-2.5 rounded-xl outline-none"
              :class="{ 'border-red-500/50': errors.email }"
              placeholder="Your email"
            />
          </div>
          <p v-if="errors.email" class="text-xs text-red-400 mt-1 ml-1">{{ errors.email }}</p>
        </div>

        <!-- Phone (Optional) -->
        <div class="space-y-1">
          <label class="block text-sm font-medium">Phone (Optional)</label>
          <div class="relative">
            <Phone class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="form.phone"
              type="text"
              class="glass-input w-full pl-10 pr-4 py-2.5 rounded-xl outline-none"
              :class="{ 'border-red-500/50': errors.phone }"
              placeholder="11-digit mobile number"
            />
          </div>
          <p v-if="errors.phone" class="text-xs text-red-400 mt-1 ml-1">{{ errors.phone }}</p>
        </div>

        <!-- Password -->
        <div class="space-y-1">
          <label class="block text-sm font-medium">Password</label>
          <div class="relative">
            <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="form.password"
              type="password"
              class="glass-input w-full pl-10 pr-4 py-2.5 rounded-xl outline-none"
              :class="{ 'border-red-500/50': errors.password }"
              placeholder="Enter your password"
            />
          </div>
          <!-- Strength indicator -->
          <div class="mt-2 space-y-1">
            <div class="flex justify-between items-center text-[10px] text-gray-400 uppercase tracking-wider">
              <span>强度：{{ strengthLabel }}</span>
              <span>{{ passwordStrength }}%</span>
            </div>
            <div class="h-1 w-full bg-white/10 rounded-full overflow-hidden">
              <div 
                class="h-full transition-all duration-500 ease-out"
                :class="strengthColor"
                :style="{ width: `${passwordStrength}%` }"
              ></div>
            </div>
            <div class="text-xs text-gray-400">建议使用更强的密码，但不会阻止提交。</div>
          </div>
          <p v-if="errors.password" class="text-xs text-red-400 mt-1 ml-1">{{ errors.password }}</p>
        </div>

        <!-- Confirm Password -->
        <div class="space-y-1">
          <label class="block text-sm font-medium">Confirm Password</label>
          <div class="relative">
            <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="form.confirmPassword"
              type="password"
              class="glass-input w-full pl-10 pr-4 py-2.5 rounded-xl outline-none"
              :class="{ 'border-red-500/50': errors.confirmPassword }"
              placeholder="Repeat password"
            />
          </div>
          <p v-if="errors.confirmPassword" class="text-xs text-red-400 mt-1 ml-1">{{ errors.confirmPassword }}</p>
        </div>

        <button
          type="submit"
          class="glass-button w-full py-3.5 rounded-xl flex items-center justify-center space-x-2 mt-6 group"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">Create Health Account</span>
          <span v-else class="animate-pulse">Registering...</span>
          <ArrowRight v-if="!isLoading" class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
        </button>
      </form>

      <div class="mt-8 pt-6 border-t border-white/10 text-center">
        <p class="text-gray-400">
          Already have an account?
          <router-link to="/login" class="text-indigo-400 hover:text-indigo-300 font-medium transition-colors">Sign in</router-link>
        </p>
      </div>
    </GlassCard>
  </div>
</template>

<style scoped>
.register-container {
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
