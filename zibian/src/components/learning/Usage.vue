<template>
  <div class="step-usage">
    <h2>组词应用</h2>
    <p class="instruction">学会了 "{{ currentLesson.character }}"，来看看怎么用</p>

    <div class="words-list">
      <div 
        v-for="word in wordsArray" 
        :key="word" 
        class="word-card"
        @click="speak(word)"
      >
        <div class="word-text">{{ word }}</div>
        <div class="speaker-icon">🔊</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLearningStore } from '../../stores/learningStore'

const store = useLearningStore()
const currentLesson = computed(() => store.currentLesson || {})

// Parse comma-separated words string into array
const wordsArray = computed(() => {
  const words = currentLesson.value.words
  if (!words) return ['暂无组词']
  return words.split(',').filter((w: string) => w.trim())
})

const speak = (text: string) => {
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  window.speechSynthesis.speak(utterance)
}
</script>

<style scoped>
.step-usage {
  text-align: center;
  height: 100%;
  padding-top: 2rem;
}

.instruction {
  color: var(--c-text-light);
  margin-bottom: 2rem;
}

.words-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 1rem;
}

.word-card {
  background: #fff;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: transform 0.1s;
}

.word-card:active {
  transform: scale(0.98);
}

.word-text {
  font-size: 1.5rem;
  font-weight: bold;
  font-family: 'Noto Serif SC', serif;
  color: var(--c-ink);
}

.speaker-icon {
  font-size: 1.2rem;
  opacity: 0.5;
}
</style>
