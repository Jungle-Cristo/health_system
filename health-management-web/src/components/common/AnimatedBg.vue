<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import gsap from 'gsap';

const canvasRef = ref<HTMLCanvasElement | null>(null);

interface Particle {
  x: number;
  y: number;
  radius: number;
  color: string;
  vx: number;
  vy: number;
  alpha: number;
}

let particles: Particle[] = [];
let animationFrame: number;
const particleCount = 50;

const createParticles = (width: number, height: number) => {
  particles = [];
  const colors = ['#6366f1', '#a855f7', '#ec4899', '#3b82f6'];
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      radius: Math.random() * 2 + 1,
      color: colors[Math.floor(Math.random() * colors.length)],
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      alpha: Math.random() * 0.5 + 0.2
    });
  }
};

const draw = (ctx: CanvasRenderingContext2D, width: number, height: number) => {
  ctx.clearRect(0, 0, width, height);

  particles.forEach(p => {
    p.x += p.vx;
    p.y += p.vy;

    if (p.x < 0 || p.x > width) p.vx *= -1;
    if (p.y < 0 || p.y > height) p.vy *= -1;

    ctx.beginPath();
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
    ctx.fillStyle = p.color;
    ctx.globalAlpha = p.alpha;
    ctx.fill();

    // Draw lines between particles
    particles.forEach(p2 => {
      const dx = p.x - p2.x;
      const dy = p.y - p2.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 150) {
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(p2.x, p2.y);
        ctx.strokeStyle = p.color;
        ctx.globalAlpha = (1 - dist / 150) * 0.1;
        ctx.stroke();
      }
    });
  });

  animationFrame = requestAnimationFrame(() => draw(ctx, width, height));
};

const handleResize = () => {
  if (!canvasRef.value) return;
  canvasRef.value.width = window.innerWidth;
  canvasRef.value.height = window.innerHeight;
  createParticles(canvasRef.value.width, canvasRef.value.height);
};

onMounted(() => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  handleResize();
  window.addEventListener('resize', handleResize);
  draw(ctx, canvas.width, canvas.height);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  cancelAnimationFrame(animationFrame);
});
</script>

<template>
  <div class="animated-bg">
    <div class="gradient-bg"></div>
    <canvas ref="canvasRef" class="particles-canvas"></canvas>
  </div>
</template>

<style scoped>
.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  background-color: #0f172a;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 30%, rgba(79, 70, 229, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 80% 70%, rgba(124, 58, 237, 0.15) 0%, transparent 40%);
  filter: blur(80px);
}

.particles-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>
