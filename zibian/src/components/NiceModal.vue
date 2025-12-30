<template>
  <transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click.self="onCancel">
      <div class="modal-content clay-card">
        <div class="modal-header">
          <h3>{{ title }}</h3>
        </div>
        <div class="modal-body">
          <p v-if="message">{{ message }}</p>
          <slot name="custom"></slot>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="onCancel">{{ cancelText }}</button>
          <button class="btn-confirm" @click="onConfirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
defineProps({
  visible: Boolean,
  title: { type: String, default: '提示' },
  message: { type: String, default: '内容' },
  confirmText: { type: String, default: '确认' },
  cancelText: { type: String, default: '取消' }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

const onCancel = () => {
  emit('update:visible', false)
  emit('cancel')
}

const onConfirm = () => {
  emit('update:visible', false)
  emit('confirm')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #F0F4F8;
  padding: 24px;
  border-radius: 24px;
  width: 80%;
  max-width: 320px;
  box-shadow: 
    8px 8px 16px rgba(163, 177, 198, 0.3),
    -8px -8px 16px rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-header h3 {
  margin: 0;
  color: var(--c-text);
  font-size: 1.2rem;
  font-weight: 800;
  text-align: center;
}

.modal-body p {
  color: var(--c-text-light);
  line-height: 1.5;
  text-align: center;
  font-size: 0.95rem;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: stretch;
}

.modal-footer button {
  flex: 1;
  padding: 12px;
  border-radius: 99px;
  font-weight: bold;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  transition: transform 0.1s;
}

.modal-footer button:active {
  transform: scale(0.95);
}

.btn-cancel {
  background: #e0e5ec;
  color: #888;
  box-shadow: 
    4px 4px 8px rgba(163, 177, 198, 0.2),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 
    4px 4px 10px rgba(118, 75, 162, 0.3),
    -4px -4px 10px rgba(255, 255, 255, 0.1);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
