<template>
  <div class="step-write">
    <h2>写字划</h2>
    <div class="mode-switch">
      <button 
        class="switch-btn" 
        :class="{ active: mode === 'stroke' }"
        @click="mode = 'stroke'"
      >笔顺</button>
      <button 
        class="switch-btn" 
        :class="{ active: mode === 'brush' }"
        @click="mode = 'brush'"
      >毛笔</button>
    </div>

    <!-- Stroke Mode (Hanzi Writer) -->
    <div v-show="mode === 'stroke'" class="writer-container">
      <!-- Debug Info -->
      <div class="debug-info" style="font-size: 12px; color: #666; margin-bottom: 5px;">
        当前汉字: {{ currentLesson.character }} (ID: {{ currentLesson.id }})
      </div>

      <div id="hanzi-writer-target" class="writer-target">
         <div v-if="writerLoading" class="writer-loading">加载笔顺中...</div>
         <div v-if="writerError" class="writer-error text-danger">{{ writerError }}</div>
         
         <div v-if="showStrokeOrder && !writerLoading" class="stroke-overlay">
            <div 
              v-for="(point, index) in strokePoints" 
              :key="index"
              class="stroke-num"
              :style="{ left: point.x + 'px', top: point.y + 'px' }"
            >
              {{ index + 1 }}
            </div>
         </div>
      </div>
      <div class="controls">
        <button class="btn-icon" @click="animateWriter">▶ 演示</button>
        <button class="btn-icon" @click="quizWriter">✍️ 临摹</button>
      </div>
    </div>

    <!-- Brush Mode (Canvas) -->
    <div v-show="mode === 'brush'" class="brush-container">
      <div class="canvas-wrapper">
        <!-- Background guide -->
        <div class="grid-bg"></div>
        <div class="char-guide">{{ currentLesson.character }}</div>
        <canvas 
          ref="canvasRef"
          width="300" 
          height="300"
          @mousedown="startStroke"
          @mousemove="drawStroke"
          @mouseup="endStroke"
          @mouseleave="endStroke"
          @touchstart.prevent="startStroke"
          @touchmove.prevent="drawStroke"
          @touchend.prevent="endStroke"
        ></canvas>
      </div>
      <div class="controls">
        <button class="btn-icon" @click="clearCanvas">🗑 清空</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import HanziWriter from 'hanzi-writer'
import { useLearningStore } from '../../stores/learningStore'

const store = useLearningStore()
const currentLesson = computed(() => store.currentLesson)
const mode = ref<'stroke' | 'brush'>('stroke')
const canvasRef = ref<HTMLCanvasElement | null>(null)
let writer: any = null
let ctx: CanvasRenderingContext2D | null = null
let isDrawing = false
let lastPoint = { x: 0, y: 0 }
let lastTime = 0

const writerLoading = ref(false)
const writerError = ref('')

onMounted(() => {
  // Init Hanzi Writer
  initWriter()

  // Init Canvas
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    if (ctx) {
      ctx.lineCap = 'round'
      ctx.lineJoin = 'round'
    }
  }
})

// Stroke Numbering Logic
const showStrokeOrder = ref(false)
const strokePoints = ref<{x: number, y: number}[]>([])

const parseStrokePoints = (data: any) => {
  try {
    const strokes = data.strokes || []
    const points = strokes.map((path: string) => {
      // Basic regex to find the first Move command (M x y)
      const match = path.match(/M\s*([0-9.]+)\s+([0-9.]+)/)
      if (match) {
        const rawX = parseFloat(match[1] as string)
        const rawY = parseFloat(match[2] as string)
        // HanziWriter uses 1024x1024 coord system by default
        const scale = (300 - 2 * 20) / 1024
        
        // Standard HanziWriter data: 0,0 is usually top-left.
        // Simple scaling from 1024 space to 300 space with padding:
        const x = rawX * scale + 20
        const y = rawY * scale + 20 
        
        return { x, y }
      }
      return null
    }).filter((p: any) => p !== null)
    strokePoints.value = points
    console.log('Parsed stroke points:', points)
  } catch (e) {
    console.error('Error parsing stroke points:', e)
  }
}

const initWriter = () => {
  if (writer || !currentLesson.value.character) {
      if (!currentLesson.value.character) console.warn('Write.vue: No char to init')
      return
  }
  
  console.log('Initializing HanziWriter for:', currentLesson.value.character)
  writerLoading.value = true
  writerError.value = ''

  try {
    writer = HanziWriter.create('hanzi-writer-target', currentLesson.value.character || '', {
        width: 300,
        height: 300,
        padding: 20,
        strokeColor: '#2c2c2c',
        radicalColor: '#c93d3d',
        showOutline: true,
        // Use local data 
        charDataLoader: (char: string | undefined, onComplete: (data: any) => void) => {
            if (!char) return
            console.log('Fetching local data for:', char)
            fetch(`/data/hanzi/${char}.json`)
            .then(res => {
                if (!res.ok) throw new Error(`Status ${res.status}: ${res.statusText}`);
                return res.json();
            })
            .then(data => {
                console.log('Data loaded successfully')
                onComplete(data)
                parseStrokePoints(data) // Parse for our overlay
                writerLoading.value = false
            })
            .catch(err => {
                console.error('Failed to load local stroke data:', err)
                writerError.value = '笔顺数据加载失败: ' + err.message
                writerLoading.value = false
            })
        }
    })
  } catch (e: any) {
    console.error('HanziWriter creation failed:', e)
    writerError.value = '初始化失败: ' + e.message
    writerLoading.value = false
  }
}

// Watch for specific changes if lesson changes dynamically
watch(() => currentLesson.value.character, (newChar) => {
  if (newChar) {
    if (!writer) {
        initWriter()
    } else {
        writer.setCharacter(newChar)
    }
  }
})

const animateWriter = () => {
  writer?.animateCharacter()
}

const quizWriter = () => {
  writer?.quiz()
}

const clearCanvas = () => {
  if (ctx && canvasRef.value) {
    ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  }
}

// Brush Algorithm (Simple Velocity based)
const getPos = (e: MouseEvent | TouchEvent) => {
  const canvas = canvasRef.value
  if (!canvas) return { x: 0, y: 0 }
  const rect = canvas.getBoundingClientRect()
  let clientX, clientY
  if ('touches' in e && (e as TouchEvent).touches.length > 0) {
    const touch = (e as TouchEvent).touches[0]
    clientX = touch?.clientX ?? 0
    clientY = touch?.clientY ?? 0
  } else {
    clientX = (e as MouseEvent).clientX
    clientY = (e as MouseEvent).clientY
  }
  return {
    x: clientX - rect.left,
    y: clientY - rect.top
  }
}

const startStroke = (e: MouseEvent | TouchEvent) => {
  isDrawing = true
  const pos = getPos(e)
  lastPoint = pos
  lastTime = Date.now()
}

const drawStroke = (e: MouseEvent | TouchEvent) => {
  if (!isDrawing || !ctx) return
  
  const pos = getPos(e)
  const now = Date.now()
  const dist = Math.hypot(pos.x - lastPoint.x, pos.y - lastPoint.y)
  const time = now - lastTime
  
  // Velocity: pixels per ms
  const velocity = time > 0 ? dist / time : 0
  // Simulating brush width: faster = thinner
  const minWidth = 4
  const maxWidth = 12
  const width = Math.max(minWidth, maxWidth - velocity * 2)

  ctx.lineWidth = width
  ctx.strokeStyle = '#2c2c2c' // Ink color
  
  ctx.beginPath()
  ctx.moveTo(lastPoint.x, lastPoint.y)
  // Quadratic curve for smoothness? Or simply lineTo for now
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()

  lastPoint = pos
  lastTime = now
}

const endStroke = () => {
  isDrawing = false
}
</script>

<style scoped>
.step-write {
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.mode-switch {
  display: flex;
  background: rgba(0,0,0,0.05);
  border-radius: 99px;
  padding: 4px;
}

.switch-btn {
  padding: 8px 24px;
  border-radius: 99px;
  color: var(--c-text-light);
  font-size: 0.9rem;
}

.switch-btn.active {
  background: #fff;
  color: var(--c-ink);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.writer-container, .brush-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

#hanzi-writer-target {
  width: 300px;
  height: 300px;
  background: url('data:image/svg+xml;utf8,<svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg"><line x1="0" y1="0" x2="100%" y2="100%" stroke="%23eee" stroke-width="1" stroke-dasharray="5,5"/><line x1="100%" y1="0" x2="0" y2="100%" stroke="%23eee" stroke-width="1" stroke-dasharray="5,5"/><line x1="50%" y1="0" x2="50%" y2="100%" stroke="%23eee" stroke-width="1" stroke-dasharray="5,5"/><line x1="0" y1="50%" x2="100%" y2="50%" stroke="%23eee" stroke-width="1" stroke-dasharray="5,5"/></svg>') no-repeat center;
  background-size: contain;
  border: 1px solid rgba(0,0,0,0.1);
  margin-bottom: 1rem;
}

.canvas-wrapper {
  position: relative;
  width: 300px;
  height: 300px;
  border: 1px solid rgba(0,0,0,0.1);
  background: #fff;
}

.grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(to right, rgba(0,0,0,0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0,0,0,0.05) 1px, transparent 1px);
  background-size: 150px 150px;
  pointer-events: none;
}

.grid-bg::after { /* Diagonal lines */
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: 
    linear-gradient(45deg, transparent 49.5%, rgba(0,0,0,0.03) 49.5%, rgba(0,0,0,0.03) 50.5%, transparent 50.5%),
    linear-gradient(-45deg, transparent 49.5%, rgba(0,0,0,0.03) 49.5%, rgba(0,0,0,0.03) 50.5%, transparent 50.5%);
}

.char-guide {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 200px;
  opacity: 0.1;
  pointer-events: none;
  user-select: none;
}

canvas {
  touch-action: none;
  cursor: crosshair;
}

.controls {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.writer-target {
  position: relative; /* For absolute overlay */
}

.stroke-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none; /* Let clicks pass through to writer */
}

.stroke-num {
  position: absolute;
  width: 20px; height: 20px;
  background: #e74c3c; /* Red color */
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transform: translate(-50%, -50%); /* Center on point */
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.btn-icon {
  background: var(--c-paper);
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.1);
  font-size: 0.9rem;
}
</style>
