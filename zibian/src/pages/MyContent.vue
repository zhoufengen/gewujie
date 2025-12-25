<template>
  <div class="content-page page-padding">
    <h1>我的内容</h1>
    <!-- List of collected chars -->
    <div class="grid-list">
      <div 
        class="char-item" 
        v-for="(char, idx) in learningStore.collectedWords" 
        :key="idx"
      >
        {{ char }}
      </div>
      <div v-if="learningStore.collectedWords.length === 0" class="empty-hint">
        暂无已学汉字，快去学习吧！
      </div>
    </div>
    <BottomNav />
  </div>
</template>
<script setup lang="ts">
import { onMounted } from 'vue'
import BottomNav from '../components/BottomNav.vue'
import { useLearningStore } from '../stores/learningStore'
import { useUserStore } from '../stores/userStore'

const learningStore = useLearningStore()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.userId) {
    learningStore.fetchStats(userStore.userId)
  }
})
</script>
<style scoped>
.page-padding { padding: 2rem 1.5rem 100px; background: var(--c-bg); min-height: 100vh; }
.grid-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; }
.char-item { aspect-ratio: 1; background: #fff; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-family: 'Ma Shan Zheng', serif; box-shadow: 0 4px 8px rgba(0,0,0,0.05); }
.empty-hint { grid-column: span 4; text-align: center; color: #999; margin-top: 2rem; font-size: 0.9rem; }
</style>
