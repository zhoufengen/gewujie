<template>
  <div class="review-flow container">
    <!-- Header: Progress & Exit -->
    <div class="flow-header">
      <button class="icon-btn" @click="backToHome">✕</button>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
      <span class="step-text">{{ currentStepIndex + 1 }}/4</span>
    </div>

    <!-- Main Content Area -->
    <div class="step-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载复习内容...</p>
      </div>
      <div v-else-if="reviewQueue.length === 0" class="empty-state">
        <p style="color: #666; font-size: 1.2rem;">暂无复习内容</p>
        <button class="btn-secondary" @click="backToHome">返回首页</button>
      </div>
      <transition v-else name="slide-fade" mode="out-in">
        <component :is="currentStepComponent" :key="currentStepIndex" />
      </transition>
    </div>

    <!-- Bottom Actions: Learning Steps -->
    <div class="flow-footer" v-if="!isReviewCompleted">
      <button class="btn-secondary" v-if="currentStepIndex > 0" @click="prevStep">上一步</button>
      <button class="btn-primary flex-1" @click="nextStep">
        {{ isLastStep ? '完成学习' : '下一步' }}
      </button>
    </div>

    <!-- Bottom Actions: Review Buttons -->
    <div class="review-controls" v-else>
      <button class="btn-action forgot" @click="submitReview('forgot')">忘记</button>
      <button class="btn-action hard" @click="submitReview('hard')">模糊</button>
      <button class="btn-action easy" @click="submitReview('easy')">掌握</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { useLearningStore } from '../stores/learningStore'
import { API_BASE_URL } from '../config'
import { useToast } from '../utils/toast'
import Shape from '../components/learning/Shape.vue'
import Pronounce from '../components/learning/Pronounce.vue'
import Write from '../components/learning/Write.vue'
import Usage from '../components/learning/Usage.vue'

const router = useRouter()
const userStore = useUserStore()
const learningStore = useLearningStore()
const toast = useToast()

// Learning steps
const steps = [Shape, Pronounce, Write, Usage]
const currentStepIndex = ref(0)
const isLoading = ref(true)
const isReviewCompleted = ref(false)
const reviewQueue = ref<any[]>([])
const currentCardIndex = ref(0)

// Fetch reviews on mount
const fetchReviews = async () => {
    if (!userStore.userId) return
    try {
        const res = await fetch(`${API_BASE_URL}/api/review/list?userId=${userStore.userId}`)
        if (res.ok) {
            reviewQueue.value = await res.json()
            if (reviewQueue.value.length > 0) {
                // Set first card as current lesson in learning store
                learningStore.setCurrentLesson(reviewQueue.value[0])
            }
        }
    } catch (e) {
        console.error('获取复习内容失败:', e)
    } finally {
        isLoading.value = false
    }
}

// Get current components
const currentStepComponent = computed(() => steps[currentStepIndex.value])
const progress = computed(() => {
    return ((currentStepIndex.value + 1) / steps.length) * 100
})
const isLastStep = computed(() => currentStepIndex.value === steps.length - 1)

// Navigation functions
const nextStep = () => {
    if (isLastStep.value) {
        // Complete learning steps, show review buttons
        isReviewCompleted.value = true
    } else {
        currentStepIndex.value++
    }
}

const prevStep = () => {
    if (currentStepIndex.value > 0) {
        currentStepIndex.value--
    }
}

const backToHome = () => {
    router.push('/home')
}

// Review functions
const submitReview = async (rating: 'forgot' | 'hard' | 'easy') => {
    if (!reviewQueue.value[currentCardIndex.value] || !userStore.userId) return
    
    const currentCard = reviewQueue.value[currentCardIndex.value]
    
    // Submit rating
    try {
        await fetch(`${API_BASE_URL}/api/review/submit?userId=${userStore.userId}&lessonId=${currentCard.id}&rating=${rating}`, {
            method: 'POST'
        })
    } catch(e) {
        console.error('提交复习结果失败:', e)
        return
    }

    // Reset for next card
    isReviewCompleted.value = false
    currentStepIndex.value = 0
    currentCardIndex.value++
    
    // Check if all reviews completed
    if (currentCardIndex.value >= reviewQueue.value.length) {
        // All reviews completed
        toast.success('复习完成!')
        router.push('/home')
    } else {
        // Set next card as current lesson
        learningStore.setCurrentLesson(reviewQueue.value[currentCardIndex.value])
    }
}

onMounted(() => {
    fetchReviews()
})
</script>

<style scoped>
.review-flow {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--c-bg);
}

.flow-header {
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(0,0,0,0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--c-ink);
  transition: width 0.3s ease;
}

.step-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--c-text-light);
}

.step-content {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  position: relative;
}

.flow-footer {
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  background: var(--c-bg);
}

.review-controls {
  padding: 2rem;
  display: flex;
  justify-content: space-around;
  gap: 1rem;
}

.btn-secondary {
  padding: 12px 24px;
  border-radius: 99px;
  border: 1px solid var(--c-ink);
  color: var(--c-ink);
  font-weight: bold;
}

.btn-primary {
  padding: 12px 24px;
  border-radius: 99px;
  background: var(--c-ink);
  color: #fff;
  font-weight: bold;
}

.btn-action {
  padding: 14px 24px;
  border-radius: 99px;
  font-weight: bold;
  color: #fff;
}

.forgot { background: #ff4d4f; }
.hard { background: #faad14; }
.easy { background: #52c41a; }

.flex-1 {
  flex: 1;
}

.icon-btn {
  font-size: 1.5rem;
  padding: 0 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--c-ink);
}

/* Transitions */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.loading-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--c-text-light);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--c-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--c-text-light);
  gap: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
