<script setup lang="ts">
import AnimatedBg from './components/common/AnimatedBg.vue';
import { RouterView } from 'vue-router';
</script>

<template>
  <AnimatedBg />
  <main class="app-container">
    <RouterView v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </RouterView>
  </main>
</template>

<style>
@import './assets/styles/glassmorphism.css';

* {
  margin: 0;
  padding: 0;
  box-shadow: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #f8fafc;
  background-color: #0f172a;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-container {
  position: relative;
  min-height: 100vh;
  z-index: 1;
}

/* Page transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
