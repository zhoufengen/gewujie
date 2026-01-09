<template>
  <div class="profile-page page-padding">
    <div class="header-row">
      <button class="back-btn" @click="$router.push('/home')">←</button>
      <h2>会员中心</h2>
      <button class="bill-btn" @click="$router.push('/order-history')">
        <span>📜</span> 账单
      </button>
    </div>

    <!-- User Clay Card -->
    <div class="clay-card user-profile">
      <div class="avatar-large">
        <span>{{ userStore.username.charAt(0).toUpperCase() }}</span>
      </div>
      <div class="info">
        <h3>{{ userStore.username }}</h3>
        <!-- Hanlin Scholar Medal -->
        <div v-if="userStore.userType === 'YEARLY_VIP'" class="medal-container">
          <div class="medal hanlin">🏆</div>
          <span class="medal-text">翰林学士</span>
        </div>
        <div class="uid-container">
          <p 
            class="uid-text" 
            :class="{ expanded: isUuidExpanded }"
            @click="toggleUuid"
          >
            UID: {{ displayedUuid }}
          </p>
          <button 
            v-if="userStore.uuid" 
            class="copy-btn" 
            @click.stop="copyUuid"
            title="复制 UID"
          >
            📋
          </button>
        </div>
        <div class="status-pill" :class="{ vip: userStore.isVip }">
          {{ userStore.userType === 'YEARLY_VIP' ? '👑 包年VIP' : userStore.isVip ? '👑 包月VIP' : '普通用户' }}
          <span v-if="userStore.isVip && userStore.vipExpirationDate" class="expiry-badge">
            至 {{ formatDate(userStore.vipExpirationDate) }}
          </span>
        </div>
      </div>
    </div>

    <!-- Payment Section -->
    <div class="payment-section">
      <h3>订阅方案</h3>
      <div class="plans-row">
        <!-- Monthly -->
        <div 
          class="clay-card plan" 
          :class="{ active: selectedPlan === 'monthly' }"
          @click="selectedPlan = 'monthly'"
        >
          <div class="plan-name">月卡</div>
          <div class="plan-price">¥39</div>
          <div class="plan-desc">灵活订阅 随时取消</div>
        </div>

        <!-- Yearly (Recommended) -->
        <div 
          class="clay-card plan recommended" 
          :class="{ active: selectedPlan === 'yearly' }"
          @click="selectedPlan = 'yearly'"
        >
          <div class="rec-badge">推荐</div>
          <div class="plan-name">年卡</div>
          <div class="plan-price">¥348</div>
          <div class="plan-desc">每天不到1块钱</div>
        </div>
      </div>

      <div class="clay-card features-list">
        <div 
          class="feature-item" 
          v-for="(feature, idx) in currentFeatures" 
          :key="idx"
        >
          {{ feature }}
        </div>
      </div>

      <button class="btn-primary pay-btn" @click="handleBuy">
        立即开通 {{ selectedPlan === 'monthly' ? '¥39' : '¥348' }}
      </button>

      <!-- Logout Button (for logged-in users) -->
      <button v-if="userStore.isLoggedIn" class="btn-outline logout-page-btn" @click="handleLogout">
        退出登录
      </button>
    </div>
    <NiceModal 
        v-model:visible="showLoginModal"
        title="需要登录"
        message="为了保存您的学习进度，请先登录账号。"
        confirmText="去登录"
        cancelText="暂不"
        @confirm="handleLoginConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { API_BASE_URL } from '../config'
import { useToast } from '../utils/toast'
import NiceModal from '../components/NiceModal.vue'

const router = useRouter()
const userStore = useUserStore()
const toast = useToast()
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}
const selectedPlan = ref<'monthly' | 'yearly'>('yearly')
const isUuidExpanded = ref(false)
const showLoginModal = ref(false)

// Toggle UUID display between truncated and full
const toggleUuid = () => {
  isUuidExpanded.value = !isUuidExpanded.value
}

// Display UUID based on expanded state
const displayedUuid = computed(() => {
  if (!userStore.isLoggedIn) return '未登录'
  if (!userStore.uuid) return '加载中...'
  if (isUuidExpanded.value || userStore.uuid.length <= 12) {
    return userStore.uuid
  }
  return userStore.uuid.substring(0, 8) + '...'
})

// Copy UUID to clipboard
const copyUuid = async () => {
  if (!userStore.uuid) return
  
  try {
    // Check if clipboard API is available
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(userStore.uuid)
      toast.success('UID 已复制到剪贴板')
    } else {
      // Fallback for browsers that don't support clipboard API
      const textArea = document.createElement('textarea')
      textArea.value = userStore.uuid
      textArea.style.position = 'fixed'
      textArea.style.left = '-9999px'
      textArea.style.top = '0'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      
      try {
        const successful = document.execCommand('copy')
        if (successful) {
          toast.success('UID 已复制到剪贴板')
        } else {
          toast.error('复制失败，请手动复制')
        }
      } catch (err) {
        console.error('Fallback copy failed:', err)
        toast.error('复制失败，请手动复制')
      }
      
      document.body.removeChild(textArea)
    }
  } catch (e) {
    console.error('Failed to copy:', e)
    toast.error('复制失败，请手动复制')
  }
}

const currentFeatures = computed(() => {
  if (selectedPlan.value === 'monthly') {
    return [
      '✅ 解锁每日学习上限',
      '✅ 畅读所有词书',
      '✅ 纯净无广告体验',
      '✅ 点亮 VIP 身份标识'
    ]
  } else {
    return [
      '🔥 包含所有月卡权益',
      '💰 立省 ¥120 (相当于7折)',
      '🏆 获赠限定 "翰林学士" 勋章',
      '🔒 离线下载复习包 (即将上线)'
    ]
  }
})

const handleBuy = async () => {
  if (userStore.isLoggedIn && userStore.userId) {
      try {
          // 1. Create Order
          const planType = selectedPlan.value === 'monthly' ? 'MONTHLY_VIP' : 'YEARLY_VIP'
          const orderRes = await fetch(`${API_BASE_URL}/api/membership/order?userId=${userStore.userId}&type=${planType}`, {
              method: 'POST'
          })
          
          if (!orderRes.ok) throw new Error('创建订单失败')
          const order = await orderRes.json()

          // 2. Pay Order (Mock Payment)
          const payRes = await fetch(`${API_BASE_URL}/api/membership/pay?orderId=${order.orderId}&paymentMethod=ALIPAY`, {
              method: 'POST'
          })

          if (!payRes.ok) throw new Error('支付失败')
          
          // 3. Update User State from response
          const updatedUser = await payRes.json()
          userStore.isVip = updatedUser.userType !== 'NORMAL'
          userStore.userType = updatedUser.userType
          userStore.vipExpirationDate = updatedUser.vipExpirationDate
          
          toast.success('支付成功！欢迎加入 VIP 大家庭！')
          // No need to redirect, stay on profile or go home? User was on profile.
      } catch(e) {
          toast.error('支付失败，请稍后重试')
          console.error(e)
      }
  } else {
      showLoginModal.value = true
  }
}

const handleLoginConfirm = () => {
  router.push('/login')
}

const handleLogout = () => {
  userStore.logout()
  toast.success('您已成功退出登录')
  router.push('/login')
}
</script>

<style scoped>
.header-row {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;
}
.back-btn { font-size: 1.5rem; color: var(--c-text-light); border: none; background: none; cursor: pointer; }
.bill-btn { 
  display: flex; align-items: center; gap: 4px;
  font-size: 0.9rem; color: var(--c-text-light); 
  background: rgba(255,255,255,0.5); border: 1px solid rgba(0,0,0,0.05);
  padding: 6px 12px; border-radius: 20px;
  cursor: pointer; transition: all 0.2s;
}
.bill-btn:hover { background: #fff; transform: translateY(-1px); box-shadow: var(--shadow-sm); color: var(--c-primary); }

.user-profile {
  display: flex; align-items: center; gap: 20px;
  margin-bottom: 32px;
  user-select: none; /* Prevent text selection on double click */
}

.avatar-large {
  width: 80px; height: 80px; border-radius: 50%;
  background: #F0F4F8; 
  box-shadow: var(--shadow-clay);
  display: flex; justify-content: center; align-items: center;
  font-size: 2rem; font-weight: 900; color: var(--c-primary);
  flex-shrink: 0; /* Prevent avatar from shrinking */
}

.info h3 { margin-bottom: 4px; color: var(--c-text); }

.uid-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.uid-text {
  font-size: 0.8rem;
  color: var(--c-text-light);
  cursor: pointer;
  transition: color 0.2s;
  word-break: break-all;
  flex: 1;
  user-select: text; /* Allow text selection for copying */
}

.uid-text:hover {
  color: var(--c-primary);
}

.uid-text.expanded {
  word-break: break-all;
}

.copy-btn {
  background: transparent;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.copy-btn:active {
  transform: scale(0.95);
}

.status-pill {
  display: inline-block; padding: 4px 12px; border-radius: 99px;
  font-size: 0.8rem; background: #e0e0e0; color: #888;
}
.status-pill.vip {
  background: var(--c-vip); color: #5d4d00; font-weight: bold;
}
.expiry-badge {
  margin-left: 8px;
  font-size: 0.7rem;
  color: #8b7a00;
  font-weight: normal;
}

/* Hanlin Scholar Medal */
.medal-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.medal {
  font-size: 1.5rem;
  animation: medal-glow 2s ease-in-out infinite;
}

.medal-text {
  font-size: 0.9rem;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

@keyframes medal-glow {
  0%, 100% { transform: scale(1); filter: brightness(1); }
  50% { transform: scale(1.1); filter: brightness(1.2); }
}

/* Plans */
.plans-row {
  display: flex; gap: 16px; margin-bottom: 24px;
}

.plan {
  flex: 1; text-align: center; cursor: pointer;
  border: 2px solid transparent;
  position: relative;
}

.plan.active {
  border-color: var(--c-primary);
  background: #fff;
}

.plan.recommended {
  transform: scale(1.05); /* Pop out */
  z-index: 1;
}

.rec-badge {
  position: absolute; top: -10px; right: -10px;
  background: var(--c-accent); color: #fff;
  padding: 4px 8px; border-radius: 8px; font-size: 0.7rem; font-weight: bold;
}

.plan-name { font-size: 1rem; color: var(--c-text-light); margin-bottom: 8px; }
.plan-price { font-size: 1.8rem; font-weight: 900; color: var(--c-text); margin-bottom: 4px; }
.plan-desc { font-size: 0.7rem; color: #999; }

/* Features */
.features-list {
  margin-bottom: 32px;
  display: flex; flex-direction: column; gap: 12px;
}
.feature-item { font-size: 1rem; color: var(--c-text); }

.pay-btn {
  width: 100%;
  margin-bottom: 16px;
}

.logout-page-btn {
  width: 100%;
  color: #ff4757;
  border-color: #ff4757;
  padding: 12px;
  border-radius: var(--radius-sm);
  font-weight: bold;
}

.logout-page-btn:hover {
  background: rgba(255, 71, 87, 0.05);
}
</style>
