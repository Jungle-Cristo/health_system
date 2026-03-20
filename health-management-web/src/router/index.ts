import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/user/Home.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import AiChat from '../views/AiChat.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: AiChat
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
