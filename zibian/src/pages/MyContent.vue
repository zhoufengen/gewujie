<template>
  <div class="content-page page-padding">
    <h1>我的内容</h1>
    
    <div v-if="groupedData.length > 0" class="learning-history">
      <div v-for="dateGroup in groupedData" :key="dateGroup.date" class="date-section">
        <h2 class="date-header">{{ formatDisplayDate(dateGroup.date) }}</h2>
        
        <div v-for="tbGroup in dateGroup.textbooks" :key="tbGroup.name" class="tb-section">
          <div class="tb-header">
            <span class="tb-tag">📖 {{ tbGroup.name }}</span>
          </div>
          
          <div class="grid-list">
            <div 
              class="char-item" 
              v-for="(char, idx) in tbGroup.chars" 
              :key="idx"
            >
              {{ char }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">📭</div>
      <p>暂无已学汉字，快去学习吧！</p>
    </div>

    <BottomNav />
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import BottomNav from '../components/BottomNav.vue'
import { useLearningStore } from '../stores/learningStore'
import { useUserStore } from '../stores/userStore'

const learningStore = useLearningStore()
const userStore = useUserStore()

const groupedData = computed(() => {
  const groups: Record<string, Record<string, string[]>> = {}
  
  learningStore.learnedRecords.forEach(record => {
    // learnedAt might be ISO string or date string
    const date = record.learnedAt.split('T')[0]
    const tb = record.textbookCategory || '默认词本'
    
    if (!groups[date]) groups[date] = {}
    if (!groups[date][tb]) groups[date][tb] = []
    
    groups[date][tb].push(record.character)
  })
  
  // Sort dates descending, textbooks ascending
  return Object.keys(groups)
    .sort((a, b) => b.localeCompare(a))
    .map(date => {
      const textbooksObj = groups[date] || {}
      return {
        date,
        textbooks: Object.keys(textbooksObj)
          .sort((a, b) => a.localeCompare(b))
          .map(tb => ({
            name: tb,
            chars: textbooksObj[tb] || []
          }))
      }
    })
})

const formatDisplayDate = (dateStr: string) => {
  const today = new Date().toISOString().split('T')[0]
  if (dateStr === today) return '今天'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

onMounted(() => {
  if (userStore.userId) {
    learningStore.fetchLearnedRecords(userStore.userId)
  }
})
</script>

<style scoped>
.page-padding { 
  padding: 1.5rem 1rem 100px; 
  background: var(--c-bg); 
  min-height: 100vh; 
  display: flex;
  flex-direction: column;
}

h1 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  padding-left: 0.5rem;
}

.learning-history {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.date-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.date-header {
  font-size: 1.1rem;
  color: var(--c-text);
  font-weight: bold;
  padding: 0.5rem;
  background: rgba(255,255,255,0.5);
  border-radius: 8px;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.tb-section {
  padding-left: 0.5rem;
}

.tb-header {
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
}

.tb-tag {
  font-size: 0.8rem;
  background: #E0E7FF;
  color: #4F46E5;
  padding: 2px 8px;
  border-radius: 20px;
  font-weight: 600;
}

.grid-list { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr)); 
  gap: 0.75rem; 
}

.char-item { 
  aspect-ratio: 1; 
  background: #fff; 
  border-radius: 12px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  font-size: 1.5rem; 
  font-family: 'Ma Shan Zheng', serif; 
  box-shadow: var(--shadow-clay-sm); 
  transition: transform 0.2s;
}

.char-item:active {
  transform: scale(0.9);
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 1rem;
}

.empty-icon {
  font-size: 4rem;
  opacity: 0.5;
}
</style>
