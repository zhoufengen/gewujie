<template>
  <nav class="bottom-nav">
    <router-link to="/home" class="nav-item" active-class="active">
      <School class="icon" />
    </router-link>
    <router-link to="/my-content" class="nav-item" active-class="active">
      <BookOpen class="icon" />
    </router-link>
    <router-link to="/dashboard" class="nav-item" active-class="active">
      <LayoutDashboard class="icon" />
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import { School, BookOpen, LayoutDashboard } from 'lucide-vue-next'
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 400px;
  height: 70px;
  background-color: rgba(255, 255, 255, 0.85); /* Light glass */
  backdrop-filter: blur(10px);
  border-radius: 99px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
  box-shadow: var(--shadow-clay); /* Use clay shadow */
  border: 2px solid rgba(255,255,255,0.5);
  padding: 0 1rem;
}

.nav-item {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-text-light); /* Dark icons for light bg */
  border-radius: 50%;
  transition: all 0.2s;
}

.nav-item.active {
  background: var(--c-primary);
  color: #fff; /* Ensure white icon on orange bg */
  opacity: 1;
  transform: scale(1.1);
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(255, 159, 67, 0.4);
}

.nav-item .icon {
  width: 28px;
  height: 28px;
}
</style>
