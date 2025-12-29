<template>
  <div class="home-page page-padding">
    <!-- Header: User Info & Date -->
    <header class="clay-header">
      <div class="user-info" @click="$router.push('/profile')">
        <div class="avatar-ring">
          <span v-if="!userStore.isLoggedIn">G</span>
          <span v-else>{{ userStore.username.charAt(0).toUpperCase() }}</span>
        </div>
        <div class="user-texts">
          <div class="username">{{ userStore.isLoggedIn ? userStore.username : '游客' }}</div>
          <div class="user-status" :class="{ vip: userStore.isVip }">
            {{ userStore.isVip ? '👑 VIP会员' : '✨ 普通用户' }}
          </div>
        </div>
      </div>
      <div class="date-badge">
        <span class="day">{{ new Date().getDate() }}</span>
        <span class="month">{{ new Date().getMonth() + 1 }}月</span>
      </div>
    </header>

    <!-- Sign In (Main Action) -->
    <section class="sign-in-section">
      <div 
        class="sign-in-circle float-anim" 
        :class="{ 'signed-in': learningStore.hasSignedIn }"
        @click="handleSignIn"
      >
        <div class="emoji">{{ learningStore.hasSignedIn ? '✅' : '🌟' }}</div>
        <div class="label">{{ learningStore.hasSignedIn ? '已打卡' : '打卡' }}</div>
      </div>
    </section>

    <!-- Wordbook Selection -->
    <section class="wordbook-section">
      <div 
        class="clay-card-mini book-select" 
        @click="cycleBook"
      >
        <span class="book-icon">📖</span>
        <span class="current-book">{{ currentBookName }}</span>
        <span class="switch-hint">切换</span>
      </div>
    </section>

    <!-- VIP Unlock Banner (If not VIP) -->
    <section class="vip-banner clay-card" v-if="!userStore.isVip" @click="$router.push('/profile')">
      <div class="vip-content">
        <h3>解锁 VIP 权益</h3>
        <p>畅享无限汉字与词书</p>
      </div>
      <div class="vip-btn">Go ></div>
    </section>

    <!-- Game Entry (Large Card) -->
    <section class="game-entry">
      <div class="adventure-card clay-card" @click="$router.push('/gameplay')">
        <div class="card-bg-icon">⚔️</div>
        <div class="adventure-info">
          <h3>文字碑林大冒险</h3>
          <p>打败文盲兽，收集汉字碎片！</p>
          <button class="btn-primary mini">开始挑战</button>
        </div>
        <div class="monster-img">👾</div>
      </div>
    </section>

    <!-- Functional Grid -->
    <section class="grid-actions">
      <div class="clay-card action-item" @click="startLearning">
        <div class="icon">✏️</div>
        <div class="cnt">
          <h4>今日学习</h4>
          <span>{{ learningStore.dailyNewWords }} 个新字</span>
        </div>
      </div>
      <div class="clay-card action-item" @click="startReview">
          <div class="icon">🔄</div>
          <div class="cnt">
            <h4>复习</h4>
            <span>{{ learningStore.pendingReviewsCount }} 个待复习</span>
          </div>
        </div>
    </section>

    <NiceModal 
        v-model:visible="showLoginModal"
        title="需要登录"
        message="为了保存您的学习进度，请先登录账号。"
        confirmText="去登录"
        cancelText="暂不"
        @confirm="handleLoginConfirm"
    />
    <BottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { useLearningStore } from '../stores/learningStore'
import NiceModal from '../components/NiceModal.vue'
import BottomNav from '../components/BottomNav.vue'

const router = useRouter()
const userStore = useUserStore()
const learningStore = useLearningStore()
const showLoginModal = ref(false)

const books = ['启蒙词本', '小学词本', '中学词本']
const currentBookIdx = ref(0)
const currentBookName = computed(() => books[currentBookIdx.value])

// Fetch stats when component mounts
onMounted(async () => {
  if (userStore.userId) {
    await learningStore.fetchStats(userStore.userId)
    await learningStore.fetchSignInStatus(userStore.userId) // 获取当前打卡状态
  }
})

const cycleBook = () => {
  currentBookIdx.value = (currentBookIdx.value + 1) % books.length
}

const handleSignIn = async () => {
  if (userStore.userId) {
    await learningStore.checkIn(userStore.userId)
    await learningStore.fetchLearningDates(userStore.userId)
  }
}

const startLearning = () => {
  if (!userStore.isLoggedIn) {
     showLoginModal.value = true
     return
  }
  router.push({ path: '/learning', query: { textbook: currentBookName.value } })
}

const handleLoginConfirm = () => {
    router.push('/login')
}

const startReview = () => {
  router.push('/review')
}
</script>

<style scoped>
.clay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-weight: 800;
  color: var(--c-text);
  font-size: 0.95rem;
}

.user-status {
  font-size: 0.65rem;
  color: var(--c-text-light);
  background: rgba(0,0,0,0.05);
  padding: 2px 6px;
  border-radius: 99px;
}

.user-status.vip {
  background: linear-gradient(135deg, #ffd93d 0%, #ff9a3d 100%);
  color: #5d4d00;
  font-weight: bold;
}

.date-badge {
  background: linear-gradient(135deg, #fff 0%, #f0f4f8 100%);
  box-shadow: var(--shadow-clay-sm);
  padding: 6px 12px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
}

.date-badge .day { font-size: 1.2rem; font-weight: 900; color: var(--c-primary); }
.date-badge .month { font-size: 0.6rem; color: var(--c-text-light); margin-top: 2px; }

/* Sign In Circle - Ultra Compact */
.sign-in-section {
  display: flex;
  justify-content: center;
  margin: 0.8rem 0;
}

.sign-in-circle {
  width: 100px; height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff 0%, #f5f7fa 100%);
  box-shadow: 
    8px 8px 16px rgba(163, 177, 198, 0.3),
    -8px -8px 16px rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.sign-in-circle.signed-in {
  background: linear-gradient(135deg, #2ED573 0%, #26d0ce 100%);
}

.sign-in-circle .emoji { font-size: 2.5rem; margin-bottom: 4px; }
.sign-in-circle .label { font-weight: 800; color: var(--c-text); font-size: 0.9rem; }
.sign-in-circle.signed-in .label { color: #fff; }

@keyframes float-anim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.float-anim {
  animation: float-anim 3s ease-in-out infinite;
}

/* Wordbook */
.wordbook-section {
  display: flex; justify-content: center; margin-bottom: 0.8rem;
}

.clay-card-mini.book-select {
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  padding: 8px 16px;
  border-radius: 99px;
  box-shadow: var(--shadow-clay-sm);
  display: flex; align-items: center; gap: 8px;
  cursor: pointer;
}

.book-icon { font-size: 1rem; }
.current-book { font-weight: 800; color: var(--c-text); font-size: 0.8rem; }
.switch-hint { 
  font-size: 0.65rem; 
  color: var(--c-primary); 
  background: rgba(255, 159, 67, 0.1);
  padding: 2px 8px; 
  border-radius: 10px; 
  font-weight: 600;
}

/* VIP Banner - Compact */
.vip-banner {
  background: linear-gradient(135deg, #ffe8d6 0%, #ffd8a8 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 0.8rem;
  cursor: pointer;
}

.vip-content h3 { font-size: 0.95rem; color: #d35400; margin-bottom: 2px; font-weight: 800; }
.vip-content p { font-size: 0.7rem; color: #e67e22; }

.vip-btn {
  background: #fff;
  padding: 6px 14px;
  border-radius: 99px;
  color: #e67e22;
  font-weight: 800;
  font-size: 0.8rem;
}

/* Adventure Card - Compact */
.adventure-card {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
  cursor: pointer;
  min-height: 100px;
  box-shadow: 
    8px 8px 16px rgba(102, 126, 234, 0.3),
    -4px -4px 12px rgba(255, 255, 255, 0.1);
}

.card-bg-icon {
  position: absolute;
  right: -15px; bottom: -15px;
  font-size: 6rem;
  opacity: 0.08;
  transform: rotate(-15deg);
}

.adventure-info { z-index: 1; }
.adventure-info h3 { color: #fff; font-size: 1.1rem; margin-bottom: 4px; font-weight: 800; }
.adventure-info p { color: rgba(255,255,255,0.95); font-size: 0.75rem; margin-bottom: 10px; }

.btn-primary.mini {
  padding: 6px 16px;
  font-size: 0.8rem;
  background: #fff;
  color: #667eea;
  font-weight: 800;
}

.monster-img {
  font-size: 3rem;
  z-index: 1;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce { 
  0%, 100% { transform: translateY(0); } 
  50% { transform: translateY(-10px); } 
}

/* Grid Actions - Compact */
.grid-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  padding: 12px 10px;
}

.action-item .icon { font-size: 1.8rem; }

.action-item h4 { 
  font-size: 0.95rem; 
  margin-bottom: 2px; 
  font-weight: 800;
}

.action-item span { 
  font-size: 0.7rem; 
  color: var(--c-text-light); 
  font-weight: 600;
}
</style>
