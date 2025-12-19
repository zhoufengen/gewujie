<template>
  <div class="content-page page-padding">
    <h1>我的内容</h1>
    <!-- List of collected chars -->
    <div class="grid-list">
      <div class="char-item" v-for="i in 12" :key="i">
        {{ ['永', '字', '八', '法', '天', '地', '玄', '黄', '宇', '宙', '洪', '荒'][i-1] }}
      </div>
    </div>
    <BottomNav />
  </div>
</template>
<script setup lang="ts">
import BottomNav from '../components/BottomNav.vue'
</script>
<style scoped>
.page-padding { padding: 2rem 1.5rem 100px; background: var(--c-bg); min-height: 100vh; }
.grid-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; }
.char-item { aspect-ratio: 1; background: #fff; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-family: 'Ma Shan Zheng', serif; box-shadow: 0 4px 8px rgba(0,0,0,0.05); }
</style>
