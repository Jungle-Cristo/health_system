<script setup lang="ts">
interface Props {
  blur?: number;
  transparency?: number;
  borderRadius?: string;
  className?: string;
  hoverEffect?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  blur: 20,
  transparency: 0.1,
  borderRadius: '16px',
  className: '',
  hoverEffect: true
});
</script>

<template>
  <div
    :class="[
      'glass-card',
      hoverEffect ? 'hover-effect' : '',
      className
    ]"
    :style="{
      '--blur': `${blur}px`,
      '--transparency': transparency,
      '--border-radius': borderRadius
    }"
  >
    <slot></slot>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, var(--transparency));
  backdrop-filter: blur(var(--blur));
  -webkit-backdrop-filter: blur(var(--blur));
  border-radius: var(--border-radius);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 1.5rem;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.glass-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.hover-effect:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.hover-effect:hover::before {
  left: 100%;
}
</style>
