<template>
  <div class="learning-flow container">
    <!-- Header: Progress & Exit -->
    <div class="flow-header">
      <button class="icon-btn" @click="$router.push('/home')">✕</button>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
      <span class="step-text">{{ currentStepIndex + 1 }}/4</span>
    </div>

    <!-- Main Content Area -->
    <div class="step-content">
      <transition name="slide-fade" mode="out-in">
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import Shape from '../components/learning/Shape.vue'
import Pronounce from '../components/learning/Pronounce.vue'
import Write from '../components/learning/Write.vue'
import Usage from '../components/learning/Usage.vue'

const router = useRouter()
const steps = [Shape, Pronounce, Write, Usage]
const currentStepIndex = ref(0)

const currentStepComponent = computed(() => steps[currentStepIndex.value])
const progress = computed(() => ((currentStepIndex.value + 1) / steps.length) * 100)
const isLastStep = computed(() => currentStepIndex.value === steps.length - 1)

const nextStep = () => {
  if (isLastStep.value) {
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
</style>
