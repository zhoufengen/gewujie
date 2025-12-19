<template>
  <div class="review-page container">
    <div class="header">
      <button class="icon-btn" @click="$router.push('/home')">✕</button>
      <h2>温故知新</h2>
    </div>

    <!-- Review Content -->
    <div class="flashcard-container">
      <div 
        class="flashcard" 
        :class="{ flipped: isFlipped }"
        @click="flipCard"
      >
        <!-- Front: Character only -->
        <div class="card-face front">
          <span class="char">知</span>
        </div>
        <!-- Back: Pinyin & Definition -->
        <div class="card-face back">
          <div class="pinyin">zhī</div>
          <div class="def">To know; knowledge.</div>
          <div class="words">知识, 知道</div>
        </div>
      </div>
    </div>

    <!-- Controls -->
    <div class="review-controls">
      <button class="btn-action forgot" @click="nextCard('forgot')">忘记</button>
      <button class="btn-action hard" @click="nextCard('hard')">模糊</button>
      <button class="btn-action easy" @click="nextCard('easy')">掌握</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isFlipped = ref(false)

const flipCard = () => {
  isFlipped.value = !isFlipped.value
}

const nextCard = (rating: 'forgot' | 'hard' | 'easy') => {
  console.log('Rated:', rating)
  isFlipped.value = false
  // Logic to load next card based on Spaced Repetition
}
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--c-bg);
}

.header {
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.flashcard-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  perspective: 1000px;
  padding: 2rem;
}

.flashcard {
  width: 100%;
  max-width: 320px;
  height: 440px;
  position: relative;
  transition: transform 0.6s;
  transform-style: preserve-3d;
  cursor: pointer;
}

.flashcard.flipped {
  transform: rotateY(180deg);
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.front .char {
  font-size: 8rem;
  font-family: 'Ma Shan Zheng', serif;
  color: var(--c-ink);
}

.back {
  transform: rotateY(180deg);
  padding: 2rem;
}

.back .pinyin {
  font-size: 2rem;
  color: var(--c-text-light);
  margin-bottom: 1rem;
}

.back .def {
  font-size: 1.1rem;
  color: var(--c-ink);
  margin-bottom: 2rem;
}

.review-controls {
  padding: 2rem;
  display: flex;
  justify-content: space-around;
  gap: 1rem;
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
</style>
