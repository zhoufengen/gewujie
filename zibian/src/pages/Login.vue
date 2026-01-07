<template>
  <div class="login-page">
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
    </div>

    <div class="login-container container">
      <!-- Logo Area -->
      <div class="logo-area">
        <h1>字变</h1>
        <p>让识字像个游戏</p>
      </div>

      <!-- Clay Form -->
      <div class="clay-card login-form">
        <h3 class="form-title">欢迎回来!</h3>
        
        <div class="form-item">
          <input v-model="phone" type="tel" placeholder="手机号" maxlength="11" />
        </div>

        <div class="form-item row">
          <input v-model="code" type="text" placeholder="验证码" maxlength="6" inputmode="numeric" />
          <button class="code-btn" :disabled="countdown > 0" @click="sendCode">
            {{ countdown > 0 ? `${countdown}s` : '获取' }}
          </button>
        </div>

        <button class="btn-primary block-btn" @click="handleLogin" :disabled="!isValid">
          🚀 一键登录
        </button>

        <div class="social-links">
          <span>第三方登录</span>
          <div class="icons">
            <div class="clay-icon alipay">支</div>
            <div class="clay-icon wechat">微</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { useToast } from '../utils/toast'

const router = useRouter()
const userStore = useUserStore()
const toast = useToast()

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const isValid = true // Always allow click

const sendCode = async () => {
  if (phone.value.length === 0) return toast.warning('请输入手机号')
  
  const success = await userStore.sendCode(phone.value)
  if (success) {
      toast.success('验证码已发送')
      countdown.value = 60
      const t = setInterval(() => {
        countdown.value--
        if(countdown.value<=0) clearInterval(t)
      }, 1000)
  } else {
      toast.error('验证码发送失败，请稍后再试')
  }
}

const handleLogin = async () => {
  const success = await userStore.login(phone.value, code.value)
  if (success) {
    toast.success('登录成功')
    router.push('/home')
  } else {
    toast.error('登录失败，请检查网络或验证码')
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--c-bg);
  position: relative;
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}

.bg-shapes .shape {
  position: absolute; border-radius: 50%; opacity: 0.5;
  filter: blur(40px);
  z-index: 0;
}
.shape-1 { background: var(--c-secondary); width: 300px; height: 300px; top: -100px; left: -100px; }
.shape-2 { background: var(--c-primary); width: 250px; height: 250px; bottom: -50px; right: -50px; }

.login-container {
  width: 100%; max-width: 400px; padding: 24px; position: relative; z-index: 10;
  background: transparent; border: none;
}

.logo-area { text-align: center; margin-bottom: 40px; }
.logo-area h1 { font-size: 3.5rem; color: var(--c-primary); text-shadow: 4px 4px 8px rgba(0,0,0,0.1); }
.logo-area p { font-size: 1.2rem; color: var(--c-text-light); letter-spacing: 2px; }

.login-form {
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(10px);
}

.form-title { text-align: center; margin-bottom: 24px; }

.form-item { margin-bottom: 16px; }
.form-item.row { display: flex; gap: 12px; }

.code-btn {
  white-space: nowrap; 
  padding: 0 16px; 
  border-radius: var(--radius-sm);
  color: var(--c-primary); font-weight: bold;
}

.block-btn { width: 100%; margin-top: 16px; }

.social-links { margin-top: 32px; text-align: center; }
.social-links span { font-size: 0.8rem; color: #bbb; display: block; margin-bottom: 12px; }

.icons { display: flex; justify-content: center; gap: 24px; }
.clay-icon {
  width: 48px; height: 48px; border-radius: 50%;
  background: #F0F4F8; 
  box-shadow: var(--shadow-clay);
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 1.2rem;
  cursor: pointer;
}
.clay-icon:active { box-shadow: var(--shadow-clay-sm); transform: scale(0.95); }
.alipay { color: #1677ff; }
.wechat { color: #07c160; }
</style>
