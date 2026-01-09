import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/userStore'

const routes = [
  {
    path: '/',
    redirect: () => {
      const userStore = useUserStore()
      return userStore.isLoggedIn ? '/home' : '/login'
    }
  },
  { path: '/login', component: () => import('../pages/Login.vue') },
  { path: '/home', component: () => import('../pages/Home.vue') },
  { path: '/learning', component: () => import('../pages/LearningFlow.vue'), meta: { requiresAuth: true } },
  { path: '/review', component: () => import('../pages/ReviewFlow.vue'), meta: { requiresAuth: true } },
  { path: '/gameplay', component: () => import('../pages/Gameplay.vue'), meta: { requiresAuth: true } },
  { path: '/dashboard', component: () => import('../pages/Dashboard.vue'), meta: { requiresAuth: false } },
  { path: '/my-content', component: () => import('../pages/MyContent.vue'), meta: { requiresAuth: false } },
  { path: '/profile', component: () => import('../pages/Profile.vue'), meta: { requiresAuth: false } },
  { path: '/vip', component: () => import('../pages/Profile.vue'), meta: { requiresAuth: false } },
  { path: '/order-history', component: () => import('../pages/OrderHistory.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
