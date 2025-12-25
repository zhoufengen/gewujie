<template>
  <transition-group name="toast-fade" tag="div" class="toast-container">
    <div 
      v-for="toast in toasts" 
      :key="toast.id" 
      class="toast-item clay-card"
      :class="toast.type"
    >
      <span class="icon">{{ getIcon(toast.type) }}</span>
      <span class="message">{{ toast.message }}</span>
    </div>
  </transition-group>
</template>

<script setup lang="ts">
import { useToast } from '../utils/toast'

const { toasts } = useToast()

const getIcon = (type: string) => {
  switch(type) {
    case 'success': return '✅'
    case 'error': return '❌'
    case 'warning': return '⚠️'
    default: return 'ℹ️'
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  pointer-events: auto;
  min-width: 300px;
  padding: 12px 20px;
  border-radius: 50px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 
    4px 4px 10px rgba(163, 177, 198, 0.4),
    -4px -4px 10px rgba(255, 255, 255, 0.9);
}

.toast-item.error { border-left: 5px solid #ff4757; }
.toast-item.success { border-left: 5px solid #2ed573; }
.toast-item.warning { border-left: 5px solid #ffa502; }

.message {
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--c-text);
}

/* Transitions */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}
.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
