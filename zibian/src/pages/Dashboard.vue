<template>
  <div class="dashboard-page page-padding">
    <h1>仪表盘</h1>
    
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-val">12</div>
        <div class="stat-label">连续签到 (天)</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">48</div>
        <div class="stat-label">已学汉字</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">秀才</div>
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
          <span v-if="getDayStatus(day) === 'learned'" class="check">✔</span>
        </div>
      </div>
    </div>

    <!-- Learning Curve -->
    <div class="section chart-section">
      <h3>遗忘曲线 / 学习趋势</h3>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <BottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import BottomNav from '../components/BottomNav.vue'

const chartRef = ref<HTMLDivElement | null>(null)

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

// Mock logic: Learn status
const getDayStatus = (day: number) => {
  // Check if future relative to REAL today
  const checkDate = new Date(displayYear.value, displayMonth.value - 1, day)
  
  if (checkDate > today) return 'future'
  
  // If same day as today
  if (checkDate.toDateString() === today.toDateString()) return 'today' 
  
  // Random history (seeded by date for consistency)
  const seed = checkDate.getFullYear() * 1000 + checkDate.getMonth() * 100 + day
  return (seed % 3 !== 0) ? 'learned' : 'missed' 
}

onMounted(() => {
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      grid: { top: 20, right: 20, bottom: 20, left: 40, containLabel: true },
      xAxis: {
        type: 'category',
        data: ['11-01', '11-05', '11-10', '11-15', '11-20', '11-25', '11-30'],
        axisLine: { lineStyle: { color: '#999' } }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#ddd' } }
      },
      series: [
        {
          data: [2, 5, 8, 12, 15, 10, 20],
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
    window.addEventListener('resize', () => chart.resize())
  }
})
</script>

<style scoped>
.page-padding {
  padding: 2rem 1.5rem 100px 1.5rem;
  min-height: 100vh;
  background-color: var(--c-bg); /* Jade White */
}

h1 {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  /* Dashboard Update for Claymorphism */
  background: var(--c-bg);
}

.stat-card {
  background: #F0F4F8;
  padding: 1rem;
  border-radius: var(--radius-sm);
  text-align: center;
  box-shadow: var(--shadow-clay);
}

.section {
  background: #F0F4F8;
  border-radius: var(--radius);
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-clay);
}

h3 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

/* Calendar Styles */
.calendar-section {
  background: #F0F4F8; 
  padding: 20px; 
  border-radius: 24px; 
  box-shadow: var(--shadow-clay);
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
  gap: 8px;
}

.day-cell {
  aspect-ratio: 1;
  border-radius: 12px;
  background: rgba(255,255,255,0.5);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 0.9rem; font-weight: bold; color: var(--c-text-light);
  position: relative;
}

.day-cell.learned {
  background: var(--c-success); 
  color: #fff;
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

.chart-container {
  width: 100%; height: 200px;
}
</style>
