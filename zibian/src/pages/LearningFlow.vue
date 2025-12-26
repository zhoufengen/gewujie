<template>
  <div class="learning-flow container">
    <!-- Header: Progress & Exit -->
    <div class="flow-header">
      <button class="icon-btn" @click="$router.push('/home')">✕</button>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
      <div class="book-indicator">📖 {{ currentBookName }}</div>
      <span class="step-text">{{ currentStepIndex + 1 }}/4</span>
    </div>

    <!-- Main Content Area -->
    <div class="step-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载字课...</p>
      </div>
      <div v-else-if="!store.currentLesson" class="empty-state">
        <p style="color: #666; font-size: 1.2rem;">暂无课程内容</p>
        <p style="color: #999; font-size: 0.9rem;">服务器可能未启动或数据缺失</p>
        <button class="btn-secondary" @click="$router.push('/home')">返回首页</button>
      </div>
      <transition v-else name="slide-fade" mode="out-in">
        <component :is="currentStepComponent" :key="currentStepIndex" />
      </transition>
    </div>

    <!-- Bottom Actions -->
    <div class="flow-footer">
      <button class="btn-secondary" v-if="currentStepIndex > 0" @click="prevStep">上一步</button>
      <button class="btn-primary flex-1" @click="nextStep">
        {{ isLastStep ? '完成' : '下一步' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useLearningStore } from '../stores/learningStore'
import { useUserStore } from '../stores/userStore'
import Shape from '../components/learning/Shape.vue'
import Pronounce from '../components/learning/Pronounce.vue'
import Write from '../components/learning/Write.vue'
import Usage from '../components/learning/Usage.vue'

const router = useRouter()
const route = useRoute()
const steps = [Shape, Pronounce, Write, Usage]
const store = useLearningStore()
const userStore = useUserStore()
const isLoading = ref(true)
const currentStepIndex = ref(0)

// Get current textbook name from route query
const currentBookName = computed(() => {
  return route.query.textbook as string || '默认词本'
})

onMounted(async () => {
  const textbook = route.query.textbook as string
  if (textbook) {
    await store.fetchCurrentLessonByTextbook(textbook, userStore.userId || undefined)
  } else {
    await store.fetchCurrentLesson()
  }
  isLoading.value = false
})

const currentStepComponent = computed(() => steps[currentStepIndex.value])
const progress = computed(() => {
    if (!store.currentLesson || !store.currentLesson.styles) return 0
    return ((currentStepIndex.value + 1) / steps.length) * 100
})
const isLastStep = computed(() => currentStepIndex.value === steps.length - 1)

const nextStep = async () => {
  if (isLastStep.value) {
    // Record learning completion
    if (userStore.userId && store.currentLesson) {
      await store.recordLearning(userStore.userId)
    }
    // Finish lesson
    router.push('/home')
  } else {
    currentStepIndex.value++
  }
}

const prevStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value--
  }
}
</script>

<style scoped>
.learning-flow {
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

.icon-btn {
  font-size: 1.5rem;
  padding: 0 0.5rem;
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

.btn-secondary {
  padding: 12px 24px;
  border-radius: 99px;
  border: 1px solid var(--c-ink);
  color: var(--c-ink);
  font-weight: bold;
}

.flex-1 {
  flex: 1;
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
  width: 40px; height: 40px;
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
