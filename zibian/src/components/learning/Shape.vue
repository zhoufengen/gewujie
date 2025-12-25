<template>
  <div class="step-shape">
    <h2>认字形</h2>
    <p class="instruction">滑动查看汉字演变 (七体)</p>

    <!-- Main Display -->
    <div class="main-display">
      <div v-if="!currentLesson.styles || currentLesson.styles.length === 0" class="no-data">
         暂无演变数据
      </div>
      <div class="char-card" v-else-if="currentStyle">
        <div class="style-label">{{ currentStyle.name }}</div>
        <!-- Mock Image/Char for now -->
        <div class="char-content">
          <!-- Text always rendered (behind image) -->
          <span class="char-text">{{ currentLesson.character || '?' }}</span>
          
          <!-- Image overlays text. Hidden if error or missing. -->
          <img 
            v-if="currentStyle.img && !imgError"
            :src="currentStyle.img" 
            class="style-img glass-overlay" 
            @error="handleImgError"
          />
        </div>
      </div>
    </div>

    <!-- Thumbnails / Slider Controls -->
    <div class="slider-controls">
      <button class="nav-btn prev" @click="prevStyle" :disabled="currentIndex === 0">←</button>
      <div class="dots">
        <span 
          v-for="(style, index) in currentLesson.styles" 
          :key="style.name"
          class="dot"
          :class="{ active: index === currentIndex }"
          @click="currentIndex = index as number"
        ></span>
      </div>
      <button class="nav-btn next" @click="nextStyle" :disabled="currentIndex === count - 1">→</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useLearningStore } from '../../stores/learningStore'

const store = useLearningStore()
const currentLesson = computed(() => store.currentLesson || {})
const currentIndex = ref(0)
const count = computed(() => currentLesson.value.styles ? currentLesson.value.styles.length : 0)
const currentStyle = computed(() => {
    if (!currentLesson.value.styles || currentLesson.value.styles.length === 0) return null
    return currentLesson.value.styles[currentIndex.value]
})

const nextStyle = () => {
  if (currentIndex.value < count.value - 1) currentIndex.value++
}

const prevStyle = () => {
  if (currentIndex.value > 0) currentIndex.value--
}
const imgError = ref(false)

const handleImgError = () => {
  imgError.value = true
}

// Reset error state when style changes
import { watch } from 'vue'
watch(currentIndex, () => {
  imgError.value = false
})
</script>

<style scoped>
.step-shape {
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.instruction {
  color: var(--c-text-light);
  margin-bottom: 2rem;
}

.main-display {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.char-card {
  width: 280px;
  height: 360px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px solid rgba(0,0,0,0.05);
}

.style-label {
  position: absolute;
  top: 20px;
  left: 20px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 1.5rem;
  color: var(--c-red);
  writing-mode: vertical-rl;
}

.char-content {
  position: relative;
  width: 200px; height: 200px;
  display: flex; align-items: center; justify-content: center;
}

.char-text {
  font-size: 8rem;
  font-family: 'Ma Shan Zheng', serif;
  color: var(--c-ink);
  z-index: 1;
}

.style-img {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: contain;
  background: #fff; /* Cover the text */
  z-index: 2;
}

.slider-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--c-text-light);
  color: var(--c-text-light);
  font-size: 1.2rem;
}

.nav-btn:disabled {
  opacity: 0.3;
}

.dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0,0,0,0.2);
  transition: all 0.3s;
}

.dot.active {
  background: var(--c-ink);
  transform: scale(1.2);
}
</style>
