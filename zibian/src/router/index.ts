import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', component: () => import('../pages/Login.vue') },
  { path: '/home', component: () => import('../pages/Home.vue') },
  { path: '/learning', component: () => import('../pages/LearningFlow.vue') },
  { path: '/review', component: () => import('../pages/ReviewFlow.vue') },
  { path: '/gameplay', component: () => import('../pages/Gameplay.vue') },
  { path: '/dashboard', component: () => import('../pages/Dashboard.vue') },
  { path: '/my-content', component: () => import('../pages/MyContent.vue') },
  { path: '/profile', component: () => import('../pages/Profile.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
