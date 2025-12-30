<template>
  <div class="gameplay-page">
    <!-- Game UI Overlay -->
    <div class="game-ui">
      <div class="header-bar">
        <div class="hp-clay">
          <div class="hp-fill" :style="{ width: `${player.hp}%` }"></div>
        </div>
        <div class="clay-card-mini score-card">
          <span class="icon">🧩</span> {{ score }}
        </div>
        <button class="exit-btn-clay" @click="$router.back()">✕</button>
      </div>

      <!-- Dialogue/Intro -->
      <div class="clay-dialogue" v-if="gameState === 'intro'">
        <h3>文盲兽出现!</h3>
        <p>打败它夺回汉字碎片！</p>
        <button class="btn-primary" @click="startGame">开始战斗</button>
      </div>

      <!-- Controls -->
      <div class="controls-overlay" v-if="gameState === 'playing'">
        <!-- Expanded Joystick Area (Left Half Screen) -->
        <div class="joystick-area" ref="joystickZone" 
             @touchstart.prevent="startMove" 
             @touchmove.prevent="onMove" 
             @touchend.prevent="endMove"
             @mousedown.prevent="startMoveMouse"
             @mousemove.prevent="onMoveMouse"
             @mouseup.prevent="endMoveMouse"
             @mouseleave.prevent="endMoveMouse">
          <div class="joystick-base-clay" :style="joystickBaseStyle" v-if="touchId !== null">
            <div class="joystick-handle-clay" :style="joystickHandleStyle"></div>
          </div>
          <div class="placeholder-text" v-else>
            <div class="hand-icon">👆</div>按住移动
          </div>
        </div>
        
        <div class="action-btn-area">
          <button class="attack-btn-clay" @touchstart.prevent="attack">
            ⚔️
          </button>
        </div>
      </div>

      <!-- Victory/Defeat/Limit -->
      <div class="clay-dialogue" v-if="gameState === 'victory'">
        <h2>🎉 胜利!</h2>
        <p>获得碎片: {{ lootChars[0] }}</p>
        <button class="btn-primary" @click="$router.push(`/learning?mode=game&textbook=${currentTextbook}`)">去学习</button>
      </div>
      <div class="clay-dialogue" v-if="gameState === 'defeat'">
        <h2>💔 挑战失败</h2>
        <p>体力耗尽，被文盲兽抓走了！</p>
        <button class="btn-primary" @click="restartGame">重新挑战</button>
        <button class="btn-secondary" @click="$router.back()">返回</button>
      </div>
      <div class="clay-dialogue" v-if="gameState === 'limit-reached'">
        <h2>📅 今日学习已达上限</h2>
        <p>每日通过游戏只能获得1个新字，已达上限！</p>
        <div class="dialog-actions">
          <button class="btn-primary" @click="$router.push('/learning')">去学习</button>
          <button class="btn-secondary" @click="$router.back()">返回首页</button>
        </div>
      </div>
    </div>

    <!-- Canvas Layer -->
    <canvas ref="canvasRef" class="game-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '../stores/userStore'
import { useLearningStore } from '../stores/learningStore'
import { useRoute } from 'vue-router'

const userStore = useUserStore()
const learningStore = useLearningStore()
const route = useRoute()
const currentTextbook = ref<string>('启蒙词本')

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let animationFrameId: number

// Game State
const gameState = ref<'intro' | 'playing' | 'victory' | 'defeat' | 'limit-reached'>('intro')
const score = ref(0)
const player = ref({ x: 0, y: 0, hp: 100, speed: 4, angle: 0 })
const enemies = ref<any[]>([])
const bullets = ref<any[]>([])
const effects = ref<any[]>([])

// Loot Logic
// lootChars declared below with spawnLoot
// Joystick State
const touchId = ref<number | null>(null)
const stickPos = ref({ x: 0, y: 0 }) // Relative to base
const basePos = ref({ x: 0, y: 0 }) // Screen coords

const joystickBaseStyle = computed(() => ({
  left: `${basePos.value.x}px`,
  top: `${basePos.value.y}px`
}))

const joystickHandleStyle = computed(() => ({
  transform: `translate(${stickPos.value.x}px, ${stickPos.value.y}px)`
}))

const startGame = async () => {
      // Get textbook from route query or use default
      const textbook = route.query.textbook as string || '启蒙词本'
      currentTextbook.value = textbook
      
      // Fetch lesson by current textbook instead of default, with isGame=true to check limit
      await learningStore.fetchCurrentLessonByTextbook(textbook, userStore.userId || undefined, true)
      
      if (learningStore.currentLesson) {
          // Extract chars from words or single char
         const l = learningStore.currentLesson
         // words is a comma-separated string, not an array
         const wordsStr = l.words || ''
         const wordsChars = wordsStr.split(',').join('').split('')
         lootChars.value = [l.character, ...wordsChars]
         
         gameState.value = 'playing'
         spawnObstacles()
         spawnEnemy()
      } else {
         // If no lesson returned, it means daily limit reached (or error)
         gameState.value = 'limit-reached'
      }
    }

// Game Loop
const initGame = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  ctx = canvas.getContext('2d')
  
  player.value.x = canvas.width / 2
  player.value.y = canvas.height / 2

  loop()
}

const spawnEnemy = () => {
  enemies.value.push({
    x: Math.random() * window.innerWidth,
    y: Math.random() * window.innerHeight,
    hp: 50,
    type: 'beast',
    color: '#263238' // Dark Clay
  })
}

// Loot Logic
const lootChars = ref(['永', '福', '寿', '喜', '乐', '安', '康', '吉', '祥'])

const spawnLoot = (x: number, y: number) => {
  const chars = lootChars.value
  const char = chars[Math.floor(Math.random() * chars.length)]
  // Add clear drop effect
  effects.value.push({ 
    x: x, 
    y: y, 
    life: 60, // Lasts 1s
    text: `+${char}`, 
    color: '#FF9F43', // Orange
    scale: 1.5
  })
}

// State for facing
const lastFacingAngle = ref(0)
const isHurting = ref(false)

const obstacles = ref<any[]>([])

// Spawn Obstacles - Keep minimal or empty
const spawnObstacles = () => {
    obstacles.value = []
    // No walls - open battlefield
}

const update = () => {
  // Always update effects so they don't freeze
  effects.value.forEach((ef, i) => {
    ef.life--
    ef.y -= 1
    if (ef.life <= 0) effects.value.splice(i, 1)
  })

  if (gameState.value !== 'playing') return

  // Move Player
  if (touchId.value !== null) {
    let nextX = player.value.x + (stickPos.value.x / 40) * player.value.speed
    let nextY = player.value.y + (stickPos.value.y / 40) * player.value.speed
    
    // Obstacle Collision (Player) - Rectangle collision
    let collided = false
    obstacles.value.forEach(wall => {
        // Rectangle collision detection
        if (nextX + 20 > wall.x && nextX - 20 < wall.x + wall.w &&
            nextY + 20 > wall.y && nextY - 20 < wall.y + wall.h) {
            collided = true
        }
    })

    if (!collided) {
        player.value.x = nextX
        player.value.y = nextY
    }
    
    // Update facing if moving
    if (Math.abs(stickPos.value.x) > 0.1 || Math.abs(stickPos.value.y) > 0.1) {
      lastFacingAngle.value = Math.atan2(stickPos.value.y, stickPos.value.x)
    }

    // Bounds check
    player.value.x = Math.max(20, Math.min(window.innerWidth - 20, player.value.x))
    player.value.y = Math.max(20, Math.min(window.innerHeight - 20, player.value.y))
  }

  // Update Bullets
  for(let i = bullets.value.length - 1; i >= 0; i--) {
    const b = bullets.value[i]
    b.x += Math.cos(b.angle) * b.speed
    b.y += Math.sin(b.angle) * b.speed
    b.life--
    
    // Bullet vs Obstacle (Rectangle)
    let hitWall = false
    obstacles.value.forEach(wall => {
        if (b.x > wall.x && b.x < wall.x + wall.w &&
            b.y > wall.y && b.y < wall.y + wall.h) {
            hitWall = true
        }
    })

    if (b.life <= 0 || hitWall) {
        if(hitWall) effects.value.push({ x: b.x, y: b.y, life: 10, text: '💥', color: '#fff' })
        bullets.value.splice(i, 1)
    }
  }

  // Update Enemies & Collision
  isHurting.value = false
  // Use for loop to allow safe splicing
  for (let i = enemies.value.length - 1; i >= 0; i--) {
    const e = enemies.value[i]
    
    // --- AI Logic Start ---
    const dx = player.value.x - e.x
    const dy = player.value.y - e.y
    const dist = Math.hypot(dx, dy)
    
    let moveX = 0
    let moveY = 0

    // 1. Basic Chase
    if (dist > 30) {
      moveX = (dx / dist) * 1.5
      moveY = (dy / dist) * 1.5
    }
    
    // 2. Dodge Bullets
    bullets.value.forEach(b => {
        const bDist = Math.hypot(b.x - e.x, b.y - e.y)
        if (bDist < 100) { // Danger Sensing
            // Calculate perpendicular vector to bullet trajectory
            const angleToBullet = Math.atan2(b.y - e.y, b.x - e.x)
            // Dodge perpendicular
            moveX -= Math.cos(angleToBullet + Math.PI/2) * 2
            moveY -= Math.sin(angleToBullet + Math.PI/2) * 2
            
            // Visual Fear
            if(Math.random() > 0.9) effects.value.push({ x: e.x, y: e.y-20, life: 10, text: '💦', scale:0.8 })
        }
    })
    
    // 3. Apply Move with Wall Collision (Separate X/Y for sliding)
    let nextEX = e.x + moveX
    let nextEY = e.y + moveY
    
    // Check X axis (Rectangle collision)
    let xBlocked = false
    obstacles.value.forEach(wall => {
        if (nextEX + 25 > wall.x && nextEX - 25 < wall.x + wall.w &&
            e.y + 25 > wall.y && e.y - 25 < wall.y + wall.h) {
            xBlocked = true
        }
    })
    
    // Check Y axis (Rectangle collision)
    let yBlocked = false
    obstacles.value.forEach(wall => {
        if (e.x + 25 > wall.x && e.x - 25 < wall.x + wall.w &&
            nextEY + 25 > wall.y && nextEY - 25 < wall.y + wall.h) {
            yBlocked = true
        }
    })
    
    // Apply movement
    if (!xBlocked) e.x = nextEX
    if (!yBlocked) e.y = nextEY
    
    // --- AI Logic End ---

    // 2. Player Collision (Beast catches player)
    if (dist < 40) { // Overlap
       isHurting.value = true
       if (Math.random() > 0.95) { // Lower damage frequency
         player.value.hp -= 1
         effects.value.push({ x: player.value.x, y: player.value.y - 30, life: 20, text: '😫 痛!', color: '#ff0000' })
         if (player.value.hp <= 0) {
            gameState.value = 'defeat'
         }
       }
    }

    // 3. Bullet Collision with Dodge Chance
    for (let bi = bullets.value.length - 1; bi >= 0; bi--) {
      const b = bullets.value[bi]
      const bDist = Math.hypot(b.x - e.x, b.y - e.y)
      if (bDist < 30) {
        bullets.value.splice(bi, 1) // Remove bullet
        
        // Dodge chance: 30% miss rate
        const dodgeChance = 0.3
        const dodged = Math.random() < dodgeChance
        
        if (dodged) {
          // Miss! Show dodge effect
          effects.value.push({ 
            x: e.x, 
            y: e.y - 20, 
            life: 20, 
            text: 'Miss!', 
            color: '#ffd700',
            scale: 1.2
          })
          // Small dodge animation (jump)
          effects.value.push({ 
            x: e.x, 
            y: e.y, 
            life: 5, 
            text: '💨', 
            color: '#fff',
            scale: 0.8
          })
        } else {
          // Hit! Apply damage
          e.hp -= 20
          effects.value.push({ x: e.x, y: e.y, life: 10, text: '-20', color: '#fff' })

          if (e.hp <= 0) {
            enemies.value.splice(i, 1) // Remove enemy
            score.value++
            
            spawnLoot(e.x, e.y)
            
            if (enemies.value.length === 0) {
               setTimeout(() => {
                   gameState.value = 'victory'
               }, 1500)
            }
            break
          }
        }
      }
    }
  }
}

// Drawing function with Emojis
const draw = () => {
  if (!ctx || !canvasRef.value) return
  const W = canvasRef.value.width
  const H = canvasRef.value.height

  // Clear canvas
  ctx.clearRect(0, 0, W, H)

  // Draw Player (Ninja Emoji)
  ctx.font = '40px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  // Rotate player based on movement if needed, but simple for now
  ctx.save()
  ctx.translate(player.value.x, player.value.y)
  // Hurt flash
  if (isHurting.value) {
    ctx.globalAlpha = 0.5 + Math.sin(Date.now() / 50) * 0.5
  }
  ctx.fillText('🥷', 0, 0)
  
  // Label "我"
  ctx.font = 'bold 12px Nunito'
  ctx.fillStyle = '#666'
  ctx.fillText('我', 0, 30)
  
  ctx.restore()

  // Draw Enemies (Beast Emoji)
  enemies.value.forEach(e => {
    if (!ctx) return
    ctx.save()
    ctx.translate(e.x, e.y)
    ctx.fillText('👾', 0, 0)
    
    // Label
    ctx.font = '12px Nunito'
    ctx.fillStyle = '#666'
    ctx.fillText('文盲兽', 0, 25)
    ctx.restore()
  })

  // Draw Bullets (Fireball)
  bullets.value.forEach(b => {
    if (!ctx) return
    ctx.font = '20px Arial'
    ctx.fillText('🔥', b.x, b.y)
  })

  // Draw Maze Walls
  obstacles.value.forEach(wall => {
    if (!ctx) return
    ctx.fillStyle = '#7f8c8d' // Gray wall
    ctx.shadowColor = 'rgba(0,0,0,0.3)'
    ctx.shadowBlur = 8
    ctx.shadowOffsetY = 4
    ctx.fillRect(wall.x, wall.y, wall.w, wall.h)
    
    // Brick texture effect
    ctx.strokeStyle = 'rgba(0,0,0,0.2)'
    ctx.lineWidth = 2
    ctx.strokeRect(wall.x, wall.y, wall.w, wall.h)
    
    // Inner highlight
    ctx.fillStyle = 'rgba(255,255,255,0.1)'
    ctx.fillRect(wall.x + 2, wall.y + 2, wall.w - 4, wall.h / 3)
  })

  // Draw Effects
  if(ctx) {
      ctx.textAlign = 'center'
      ctx.textBaseline = 'bottom'
  }
  effects.value.forEach(ef => {
    if(ctx) {
      ctx.fillStyle = ef.color || '#FF6B81'
      const fontSize = ef.scale ? 20 * ef.scale : 20
      ctx.font = `bold ${fontSize}px Zcool KuaiLe`
      ctx.fillText(ef.text, ef.x, ef.y)
    }
  })
}

// Main game loop
const loop = () => {
  update()
  draw()
  animationFrameId = requestAnimationFrame(loop)
}

// ... drawClaySphere ... (Keep helper)

// Controls Logic
const startMove = (e: TouchEvent) => {
  if (e.touches.length === 0) return
  const touch = e.touches[0]
  if (!touch) return
  touchId.value = touch.identifier
  basePos.value = { x: touch.clientX, y: touch.clientY }
  stickPos.value = { x: 0, y: 0 }
}

const onMove = (e: TouchEvent) => {
  const touch = Array.from(e.touches).find(t => t.identifier === touchId.value)
  if (!touch) return
  
  const dx = touch.clientX - basePos.value.x
  const dy = touch.clientY - basePos.value.y
  const maxDist = 40
  const dist = Math.hypot(dx, dy)
  const angle = Math.atan2(dy, dx)
  
  const limitedDist = Math.min(dist, maxDist)
  
  stickPos.value = {
    x: Math.cos(angle) * limitedDist,
    y: Math.sin(angle) * limitedDist
  }
}

const endMove = () => {
  touchId.value = null
  stickPos.value = { x: 0, y: 0 }
}

// Mouse Handlers for Desktop Testing
const isMouseDown = ref(false)

const startMoveMouse = (e: MouseEvent) => {
  isMouseDown.value = true
  basePos.value = { x: e.clientX, y: e.clientY }
  stickPos.value = { x: 0, y: 0 }
  touchId.value = 999 // Mock ID for v-if check
}

const onMoveMouse = (e: MouseEvent) => {
  if (!isMouseDown.value) return
  const dx = e.clientX - basePos.value.x
  const dy = e.clientY - basePos.value.y
  const maxDist = 40
  const dist = Math.hypot(dx, dy)
  const angle = Math.atan2(dy, dx)
  
  const limitedDist = Math.min(dist, maxDist)
  
  stickPos.value = {
    x: Math.cos(angle) * limitedDist,
    y: Math.sin(angle) * limitedDist
  }
}

const endMoveMouse = () => {
  isMouseDown.value = false
  touchId.value = null
  stickPos.value = { x: 0, y: 0 }
}

const restartGame = () => {
  // Reset game state
  gameState.value = 'intro'
  score.value = 0
  player.value.hp = 100
  player.value.x = window.innerWidth / 2
  player.value.y = window.innerHeight / 2
  enemies.value = []
  bullets.value = []
  effects.value = []
}


// Auto-Aim Attack
const attack = () => {
  // Find nearest enemy
  let targetAngle = lastFacingAngle.value
  let minDist = 9999
  
  if (enemies.value.length > 0) {
    enemies.value.forEach(e => {
      const dist = Math.hypot(e.x - player.value.x, e.y - player.value.y)
      if (dist < minDist) {
        minDist = dist
        targetAngle = Math.atan2(e.y - player.value.y, e.x - player.value.x)
      }
    })
  }

  bullets.value.push({
    x: player.value.x,
    y: player.value.y,
    angle: targetAngle,
    speed: 12,
    life: 60
  })
}

onMounted(() => {
  initGame()
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
})
</script>

<style scoped>
.gameplay-page {
  width: 100vw;
  height: 100vh;
  position: relative;
  background-color: var(--c-bg); /* Use global Jade White */
  overflow: hidden;
  /* Add subtle texture */
  background-image: radial-gradient(#E2E6EA 1px, transparent 1px);
  background-size: 20px 20px;
}

.game-canvas {
  position: absolute; top: 0; left: 0; z-index: 1;
}

.game-ui {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;
}
.game-ui > * { pointer-events: auto; }

.header-bar {
  position: absolute; top: 16px; left: 16px; right: 16px; 
  display: flex; justify-content: space-between; align-items: center;
}

/* Clay HP Bar */
.hp-clay {
  width: 120px; height: 16px;
  background: rgba(0,0,0,0.1);
  border-radius: 99px;
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.5);
}

.hp-fill {
  height: 100%; background: var(--c-accent);
  border-radius: 99px;
  transition: width 0.2s;
}

/* Mini Clay Card for Score */
.clay-card-mini {
  background: #F0F4F8;
  padding: 8px 16px;
  border-radius: 99px;
  box-shadow: var(--shadow-clay-sm);
  font-weight: bold; color: var(--c-primary);
  display: flex; align-items: center; gap: 8px;
}

.exit-btn-clay {
  width: 40px; height: 40px; border-radius: 50%;
  background: #F0F4F8; color: var(--c-text-light);
  box-shadow: var(--shadow-clay-sm);
  font-size: 1.2rem;
}

/* Dialogues */
.clay-dialogue {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  background: #F5F7FA;
  padding: 32px; border-radius: 32px;
  box-shadow: var(--shadow-clay);
  text-align: center;
  border: 1px solid #fff;
  min-width: 300px; /* Ensure enough width */
}
.clay-dialogue h3 { 
  font-size: 1.5rem; color: var(--c-primary); margin-bottom: 8px; 
  white-space: nowrap; /* Prevent headline wrapping */
}
.clay-dialogue p { color: var(--c-text-light); margin-bottom: 24px; }

/* Joystick Clay */
.controls-overlay {
  position: absolute; bottom: 0; left: 0; width: 100%; height: 100%; pointer-events: none;
}

.joystick-area {
  position: absolute; bottom: 0; left: 0;
  width: 50%; height: 50%; /* Larger hit area */
  pointer-events: auto;
  z-index: 10;
}

.joystick-base-clay {
  position: fixed; /* Fix coordinate issue */
  width: 100px; height: 100px;
  background: rgba(240, 244, 248, 0.8); /* More opaque */
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 
    0 0 0 4px rgba(255, 159, 67, 0.6), /* Orange outer ring */
    inset 0 2px 8px rgba(0,0,0,0.1);
  pointer-events: none;
  border: 2px solid rgba(255,255,255,0.9);
}

.joystick-handle-clay {
  width: 50px; height: 50px; background: var(--c-primary);
  border-radius: 50%;
  position: absolute; top: 25px; left: 25px;
  box-shadow: 
    var(--shadow-clay-sm),
    0 0 0 3px rgba(255,255,255,0.5);
}

.placeholder-text {
  /* Visual guide */
  position: absolute; bottom: 80px; left: 80px;
  text-align: center; color: var(--c-text-light); opacity: 0.5; font-size: 0.8rem;
  pointer-events: none;
}

/* Attack Button Clay */
.action-btn-area {
  position: absolute; bottom: 60px; right: 60px; pointer-events: auto;
}

.attack-btn-clay {
  width: 80px; height: 80px; border-radius: 50%;
  background: var(--c-secondary); color: #fff;
  font-size: 2rem;
  box-shadow: 
    6px 6px 12px rgba(84, 160, 255, 0.4),
    -6px -6px 12px rgba(255, 255, 255, 0.6),
    inset 2px 2px 4px rgba(255,255,255,0.4);
}
.attack-btn-clay:active {
  box-shadow: inset 4px 4px 8px rgba(0,0,0,0.1);
  transform: scale(0.95);
}

/* Dialog Actions */
.dialog-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
  align-items: center;
}

.btn-primary, .btn-secondary {
  border: none;
  outline: none;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--c-primary);
  color: #fff;
  padding: 12px 32px;
  border-radius: 99px;
  font-size: 1.1rem;
  box-shadow: 
    4px 4px 10px rgba(255, 159, 67, 0.4),
    -4px -4px 10px rgba(255, 255, 255, 0.5),
    inset 2px 2px 4px rgba(255,255,255,0.2);
}
.btn-primary:active {
  transform: scale(0.95);
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.1);
}

.btn-secondary {
  background: transparent;
  color: var(--c-text-light);
  padding: 8px 24px;
  font-size: 1rem;
  text-decoration: underline;
  opacity: 0.8;
}
.btn-secondary:active {
  opacity: 0.6;
}
</style>
