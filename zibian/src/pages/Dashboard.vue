<template>
  <div class="dashboard-page page-padding">
    <h1>仪表盘</h1>
    
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-val">{{ learningStore.dailyNewWords }}</div>
        <div class="stat-label">今日新词</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">{{ learningStore.collectedWords.length || 0 }}</div>
        <div class="stat-label">已学汉字</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">{{ currentAchievement.name }}</div>
        <div class="stat-label">当前成就</div>
      </div>
    </div>

    <!-- Calendar Heatmap -->
    <div class="section calendar-section">
      <div class="cal-header-row">
        <button class="nav-btn" @click="changeMonth(-1)">◀</button>
        <h3>📅 {{ displayYear }}年 {{ displayMonth }}月</h3>
        <button class="nav-btn" @click="changeMonth(1)" :disabled="isCurrentMonth">▶</button>
      </div>
      
      <!-- Weekday Headers -->
      <div class="calendar-header">
        <div v-for="w in ['日','一','二','三','四','五','六']" :key="w" class="weekday">{{ w }}</div>
      </div>

      <!-- Days Grid -->
      <div class="calendar-grid">
        <!-- Empty start filler -->
        <div v-for="n in startOffset" :key="`empty-${n}`" class="day-cell empty"></div>
        
        <!-- Actual Days -->
        <div 
          v-for="day in daysInMonth" 
          :key="day"
          class="day-cell"
          :class="getDayStatus(day)"
        >
          <span class="day-num">{{ day }}</span>
          <span v-if="getDayStatus(day) === 'learned' || getDayStatus(day) === 'today-learned'" class="check">✔</span>
          <span v-else-if="getDayStatus(day) === 'checkedin' || getDayStatus(day) === 'today-checkedin'" class="circle">○</span>
          <span v-else-if="getDayStatus(day) === 'missed'" class="cross">✕</span>
        </div>
      </div>
    </div>

    <!-- Learning Curve -->
    <div class="section chart-section">
      <h3>遗忘曲线 / 学习趋势</h3>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- Login Call-to-Action for Guests -->
    <div v-if="!userStore.isLoggedIn" class="guest-banner clay-card" @click="showLoginModal = true">
      <div class="banner-content">
        <h3>登录查看详细数据</h3>
        <p>同步您的学习进度与成就</p>
      </div>
      <button class="btn-primary mini">去登录</button>
    </div>

    <NiceModal 
        v-model:visible="showLoginModal"
        title="需要登录"
        message="为了保存您的学习进度，请先登录账号。"
        confirmText="去登录"
        cancelText="暂不"
        @confirm="handleLoginConfirm"
    />

    <BottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import BottomNav from '../components/BottomNav.vue'
import NiceModal from '../components/NiceModal.vue'
import { useLearningStore } from '../stores/learningStore'
import { useUserStore } from '../stores/userStore'
import { getCurrentAchievement } from '../utils/achievementUtils'

const learningStore = useLearningStore()
const userStore = useUserStore()
const router = useRouter()
const showLoginModal = ref(false)

const handleLoginConfirm = () => {
    router.push('/login')
}

const chartRef = ref<HTMLDivElement | null>(null)

// Calculate current achievement based on learned words count
const currentAchievement = computed(() => {
  const learnedWordsCount = learningStore.collectedWords.length || 0
  return getCurrentAchievement(learnedWordsCount)
})

// Calendar Logic
const today = new Date()
const displayDate = ref(new Date(today.getFullYear(), today.getMonth(), 1))

const displayYear = computed(() => displayDate.value.getFullYear())
const displayMonth = computed(() => displayDate.value.getMonth() + 1)

const isCurrentMonth = computed(() => {
  return displayDate.value.getFullYear() === today.getFullYear() && 
         displayDate.value.getMonth() === today.getMonth()
})

const daysInMonth = computed(() => {
  return new Date(displayYear.value, displayMonth.value, 0).getDate()
})

const startOffset = computed(() => {
  return new Date(displayYear.value, displayMonth.value - 1, 1).getDay()
})

const changeMonth = (delta: number) => {
  const newDate = new Date(displayDate.value)
  newDate.setMonth(newDate.getMonth() + delta)
  displayDate.value = newDate
}

// Get day status based on actual learning records
const getDayStatus = (day: number) => {
      const checkDate = new Date(displayYear.value, displayMonth.value - 1, day)
      // Fix: Use local date string instead of toISOString() which converts to UTC
      const year = checkDate.getFullYear()
      const month = String(checkDate.getMonth() + 1).padStart(2, '0')
      const d = String(checkDate.getDate()).padStart(2, '0')
      const dateString = `${year}-${month}-${d}`
      
      // Debug: Show today's date string
      const todayString = today.toISOString().split('T')[0]
      console.log('Today string:', todayString)
      console.log('Checking day:', day, 'dateString:', dateString)
      
      // Debug: Show all learning dates
      console.log('All learning dates:', learningStore.learningDates)
      
      const dateStatus = learningStore.learningDates[dateString]
      console.log('Date status for', dateString, ':', dateStatus)
      
      if (checkDate > today) return 'future'
      
      const isToday = checkDate.toDateString() === today.toDateString()
      console.log('Is today?', isToday)
      
      // Check if date has learning records first
      if (dateStatus === 'learned') {
        console.log('Returning', isToday ? 'today-learned' : 'learned')
        return isToday ? 'today-learned' : 'learned'
      } else if (dateStatus === 'checkedin') {
        console.log('Returning', isToday ? 'today-checkedin' : 'checkedin')
        return isToday ? 'today-checkedin' : 'checkedin'
      }
      
      // If same day as today and no records, show today status
      if (isToday) {
        console.log('Returning today')
        return 'today'
      }
      
      // Past dates with no records
      console.log('Returning missed')
      return 'missed'
    }

onMounted(async () => {
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    
    // Set initial empty data
    chart.setOption({
      grid: { top: 20, right: 20, bottom: 20, left: 40, containLabel: true },
      xAxis: {
        type: 'category',
        data: [],
        axisLine: { lineStyle: { color: '#999' } }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#ddd' } }
      },
      series: [
        {
          data: [],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#2ED573' }, // Success Green
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(46, 213, 115, 0.5)' },
              { offset: 1, color: 'rgba(46, 213, 115, 0)' }
            ])
          }
        }
      ]
    })
    
    // Fetch and update with real data if user is logged in
    if (userStore.userId) {
      const trendData = await learningStore.fetchLearningTrend()
      
      // Process data for chart
      const formattedDates = trendData.map((item: any) => {
        const date = new Date(item.date)
        return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      })
      const counts = trendData.map((item: any) => item.count)
      
      // Update chart with real data
      chart.setOption({
        xAxis: { data: formattedDates },
        series: [{ data: counts }]
      })
    }
    
    window.addEventListener('resize', () => chart.resize())
  }
  
  if (userStore.userId) {
    await learningStore.fetchStats()
    await learningStore.fetchLearningDates()
  }
})

// Watch userId changes to reload data when user switches accounts
watch(() => userStore.userId, async (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    await learningStore.fetchStats()
    await learningStore.fetchLearningDates()
  }
})
</script>

<style scoped>
.page-padding {
  padding: 1rem 1rem 90px 1rem;
  height: 100vh;
  overflow: hidden;
  background-color: var(--c-bg);
  display: flex;
  flex-direction: column;
}

h1 {
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}

.stat-card {
  /* Dashboard Update for Claymorphism */
  background: var(--c-bg);
}

.stat-card {
  background: #F0F4F8;
  padding: 0.5rem;
  border-radius: var(--radius-sm);
  text-align: center;
  box-shadow: var(--shadow-clay);
}
.stat-val { font-size: 1.2rem; font-weight: bold; }
.stat-label { font-size: 0.7rem; color: var(--c-text-light); }

.section {
  background: #F0F4F8;
  border-radius: var(--radius);
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  box-shadow: var(--shadow-clay);
}

h3 {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

/* Calendar Styles */
.calendar-section {
  background: #F0F4F8; 
  padding: 10px; 
  border-radius: 16px; 
  box-shadow: var(--shadow-clay);
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.cal-header-row {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
}
.cal-header-row h3 { margin: 0; font-size: 1.1rem; }

.nav-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: #fff; box-shadow: var(--shadow-clay-sm);
  color: var(--c-primary); font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.nav-btn:active { transform: scale(0.95); }
.nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.calendar-header {
  display: grid; 
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
}

.weekday { text-align: center; color: var(--c-text-light); font-size: 0.9rem; font-weight: bold; }

.calendar-grid {
  display: grid; grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  aspect-ratio: 1;
  border-radius: 8px;
  background: rgba(255,255,255,0.5);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: bold; color: var(--c-text-light);
  position: relative;
}

.day-cell.learned {
  background: var(--c-success); 
  color: #fff;
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
}

.day-cell.checkedin {
  background: var(--c-warning); 
  color: #fff;
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
}

.day-cell.today-learned {
  border: 2px solid var(--c-primary);
  background: #fff; /* Keep white bg to show checks clearly if desired, or green? User asked for "Green checkmark". */
  /* Actually, to match "Yellow border... green checkmark", likely implies white bg. */
  background: #fff; 
  color: var(--c-success);
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
}

.day-cell.today-checkedin {
  border: 2px solid var(--c-primary);
  background: #fff;
  color: var(--c-warning);
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
}

.day-cell.today {
  border: 2px solid var(--c-primary);
  background: #fff;
  color: var(--c-primary);
}

.day-cell.missed {
  opacity: 0.5;
}

.day-cell.future {
  opacity: 0.2;
}

.check { font-size: 0.6rem; position: absolute; bottom: 2px; }
.circle { font-size: 0.6rem; position: absolute; bottom: 2px; }
.cross { font-size: 0.6rem; position: absolute; bottom: 2px; }

.chart-section {
  flex-shrink: 0;
}
.chart-container {
  width: 100%; height: 120px;
}

.guest-banner {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 20px;
  cursor: pointer;
  animation: pulse 2s infinite;
}

.banner-content h3 { font-size: 1rem; color: #555; margin-bottom: 4px; font-weight: 800; }
.banner-content p { font-size: 0.8rem; color: #777; }

.btn-primary.mini {
  padding: 8px 16px;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 800;
  box-shadow: var(--shadow-clay-sm);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}
</style>
