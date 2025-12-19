<template>
  <div class="step-pronounce">
    <h2>读字音</h2>
    <p class="instruction">点击喇叭跟读</p>

    <div class="pronounce-card">
      <div class="pinyin-display">{{ currentLesson.pinyin }}</div>
      <div class="char-display">{{ currentLesson.char }}</div>

      <!-- Audio Button -->
      <div class="audio-btn-wrapper">
        <button class="audio-btn" @click="speak" :class="{ playing: isPlaying }">
          <Speaker v-if="!isPlaying" :size="32" />
          <Volume2 v-else :size="32" />
        </button>
        <!-- Wave Animation -->
        <div class="wave-ring" v-if="isPlaying"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useLearningStore } from '../../stores/learningStore'
import { Speaker, Volume2 } from 'lucide-vue-next'

const store = useLearningStore()
const currentLesson = computed(() => store.currentLesson)
const isPlaying = ref(false)

const speak = () => {
  if (isPlaying.value) return
  isPlaying.value = true

  // Web Speech API
  const utterance = new SpeechSynthesisUtterance(currentLesson.value.char)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.8
  
  utterance.onend = () => {
    isPlaying.value = false
  }

  window.speechSynthesis.speak(utterance)
}
</script>

<style scoped>
.step-pronounce {
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.instruction {
  color: var(--c-text-light);
  margin-bottom: 2rem;
}

.pronounce-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.pinyin-display {
  font-size: 3rem;
  font-family: 'Noto Serif SC', serif;
  color: var(--c-text-light);
  letter-spacing: 0.1em;
}

.char-display {
  font-size: 8rem;
  font-family: 'Ma Shan Zheng', serif;
  color: var(--c-ink);
  line-height: 1;
}

.audio-btn-wrapper {
  position: relative;
  margin-top: 2rem;
}

.audio-btn {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--c-ink);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  transition: transform 0.1s;
}

.audio-btn:active {
  transform: scale(0.95);
}

.wave-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid var(--c-ink);
  animation: ripple 1.5s infinite;
  z-index: 1;
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(2); opacity: 0; }
}
</style>
