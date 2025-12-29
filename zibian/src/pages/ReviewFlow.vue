<template>
  <div class="review-flow container">
    <!-- Header: Progress & Exit -->
    <div class="flow-header" v-if="reviewQueue.length > 0 || allReviewsCompleted">
      <button class="icon-btn" @click="backToHome">✕</button>
      <div class="progress-bar" v-if="reviewQueue.length > 0">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
      <div class="book-indicator" v-if="currentBookName">📖 {{ currentBookName }}</div>
      <span class="step-text" v-if="reviewQueue.length > 0 && !allReviewsCompleted">{{ currentStepIndex + 1 }}/4</span>
    </div>

    <!-- Main Content Area -->
    <div class="step-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载复习内容...</p>
      </div>
      <div v-else-if="allReviewsCompleted" class="complete-state">
        <div class="complete-icon">📚</div>
        <h3>复习完成！</h3>
        <p>恭喜你完成了今天的复习</p>
        <p>保持学习，继续加油！</p>
        <button class="btn-primary" @click="backToHome">返回首页</button>
      </div>
      <div v-else-if="reviewQueue.length === 0" class="empty-state">
        <p style="color: #666; font-size: 1.2rem;">暂无复习内容</p>
        <button class="btn-secondary" @click="backToHome">返回首页</button>
      </div>
      <div v-else-if="showReviewButtonsFirst">
        <div class="review-question">
          <div class="review-character" v-if="currentCard">
            <div class="big-character">{{ currentCard.character }}</div>
            <div class="character-info">
              <span class="pinyin">{{ currentCard.pinyin }}</span>
              <span class="definition">{{ currentCard.definition }}</span>
            </div>
          </div>
          <p class="question-text">你对这个字的掌握程度是？</p>
        </div>
      </div>
      <transition v-else name="slide-fade" mode="out-in">
        <component :is="currentStepComponent" :key="currentStepIndex" />
      </transition>
    </div>

    <!-- Bottom Actions: Initial Review Buttons -->
    <div class="review-controls" v-if="!isLoading && reviewQueue.length > 0 && !allReviewsCompleted && showReviewButtonsFirst">
      <button class="btn-action forgot" @click="handleInitialReview('forgot')">忘记</button>
      <button class="btn-action hard" @click="handleInitialReview('hard')">模糊</button>
      <button class="btn-action easy" @click="handleInitialReview('easy')">掌握</button>
    </div>

    <!-- Bottom Actions: Learning Steps -->
    <div class="flow-footer" v-else-if="!isReviewCompleted && !allReviewsCompleted && reviewQueue.length > 0">
      <button class="btn-secondary" v-if="currentStepIndex > 0" @click="prevStep">上一步</button>
      <button class="btn-primary flex-1" @click="nextStep">
        {{ isLastStep ? '完成学习' : '下一步' }}
      </button>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { useLearningStore } from '../stores/learningStore'
import { API_BASE_URL } from '../config'
import Shape from '../components/learning/Shape.vue'
import Pronounce from '../components/learning/Pronounce.vue'
import Write from '../components/learning/Write.vue'
import Usage from '../components/learning/Usage.vue'

const router = useRouter()
const userStore = useUserStore()
const learningStore = useLearningStore()

// Learning steps
const steps = [Shape, Pronounce, Write, Usage]
const currentStepIndex = ref(0)
const isLoading = ref(true)
const isReviewCompleted = ref(false)
const reviewQueue = ref<any[]>([])
const currentCardIndex = ref(0)
// Track if all reviews are completed
const allReviewsCompleted = ref(false)

// Track if we should show initial review buttons first
const showReviewButtonsFirst = ref(true)

// Store initial rating when user selects "忘记" or "模糊"
const initialRating = ref<'forgot' | 'hard' | 'easy' | null>(null)

// Current textbook name from current review card
const currentBookName = computed(() => {
    const currentCard = reviewQueue.value[currentCardIndex.value]
    return currentCard?.textbookCategory || ''
})

// Current review card data
const currentCard = computed(() => {
    return reviewQueue.value[currentCardIndex.value] || null
})

// Fetch reviews on mount
const fetchReviews = async () => {
    if (!userStore.userId) return
    try {
        const res = await fetch(`${API_BASE_URL}/api/review/list?userId=${userStore.userId}`)
        if (res.ok) {
            reviewQueue.value = await res.json()
            console.log('获取到的复习内容:', reviewQueue.value)
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
const nextStep = async () => {
    if (isLastStep.value) {
        // When user completes all learning steps, submit review automatically with initial rating
        if (initialRating.value) {
            await submitReview(initialRating.value)
            // Reset initial rating for next card
            initialRating.value = null
        }
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

// Handle initial review choice
const handleInitialReview = async (rating: 'forgot' | 'hard' | 'easy') => {
    const localCurrentCard = reviewQueue.value[currentCardIndex.value]
    if (!localCurrentCard || !userStore.userId) return
    
    // If user selects "掌握" (easy), submit directly and move to next card
    if (rating === 'easy') {
        await submitReview(rating)
    } else {
        // If user selects "忘记" or "模糊", enter learning steps
        // Store the initial rating for later submission
        initialRating.value = rating
        showReviewButtonsFirst.value = false
    }
}

// Review functions
const submitReview = async (rating: 'forgot' | 'hard' | 'easy') => {
    if (!reviewQueue.value[currentCardIndex.value] || !userStore.userId) return
    
    const localCurrentCard = reviewQueue.value[currentCardIndex.value]
    
    // Submit rating
    try {
        await fetch(`${API_BASE_URL}/api/review/submit?userId=${userStore.userId}&lessonId=${localCurrentCard.id}&rating=${rating}`, {
            method: 'POST'
        })
    } catch(e) {
        console.error('提交复习结果失败:', e)
        return
    }

    console.log('=== 复习提交后状态 ===')
    console.log('当前卡片索引:', currentCardIndex.value)
    console.log('复习队列长度:', reviewQueue.value.length)
    console.log('当前卡片:', localCurrentCard)
    console.log('评分:', rating)

    // Increment card index first
    currentCardIndex.value++
    
    console.log('递增后卡片索引:', currentCardIndex.value)
    console.log('是否所有复习完成:', currentCardIndex.value >= reviewQueue.value.length)
    
    // Check if all reviews completed
    if (currentCardIndex.value >= reviewQueue.value.length) {
        // All reviews completed, show completion state
        allReviewsCompleted.value = true
        console.log('设置复习全部完成:', allReviewsCompleted.value)
        console.log('当前状态: showReviewButtonsFirst=', showReviewButtonsFirst.value, 'isReviewCompleted=', isReviewCompleted.value)
    } else if (reviewQueue.value.length > 0) {
        // Reset for next card
        isReviewCompleted.value = false
        currentStepIndex.value = 0
        showReviewButtonsFirst.value = true
        // Set next card as current lesson
        learningStore.setCurrentLesson(reviewQueue.value[currentCardIndex.value])
        console.log('重置状态为下一张卡片: showReviewButtonsFirst=', showReviewButtonsFirst.value)
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

.book-indicator {
  font-size: 0.8rem;
  font-weight: bold;
  color: var(--c-primary);
  padding: 0 0.5rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.complete-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--c-text);
  gap: 1.5rem;
  text-align: center;
}

.complete-icon {
  font-size: 5rem;
  margin-bottom: 1rem;
  animation: bounce 1s ease-in-out;
}

.complete-state h3 {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--c-ink);
  margin: 0;
}

.complete-state p {
  font-size: 1.1rem;
  color: var(--c-text-light);
  margin: 0;
}

.complete-state .btn-primary {
  margin-top: 2rem;
  padding: 12px 32px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-30px); }
  60% { transform: translateY(-15px); }
}

/* Initial Review Question Styles */
.review-question {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 2rem;
}

.review-character {
  margin-bottom: 3rem;
}

.big-character {
  font-size: 12rem;
  font-weight: bold;
  color: var(--c-ink);
  margin-bottom: 1rem;
  line-height: 1;
}

.character-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 1.5rem;
  color: var(--c-text);
}

.pinyin {
  font-family: 'Arial', sans-serif;
  color: var(--c-primary);
}

.definition {
  font-size: 1.2rem;
  color: var(--c-text-light);
  max-width: 400px;
  line-height: 1.4;
}

.question-text {
  font-size: 1.3rem;
  color: var(--c-ink);
  font-weight: 600;
  margin-top: 2rem;
}
</style>
