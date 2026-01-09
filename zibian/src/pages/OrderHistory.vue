<template>
  <div class="order-history-page page-padding">
    <div class="header-row">
      <button class="back-btn" @click="$router.back()">←</button>
      <h2>账单历史</h2>
    </div>

    <div v-if="orders.length === 0 && !isLoading" class="empty-state">
      <div class="empty-icon">🧾</div>
      <p>暂无消费记录</p>
    </div>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="clay-card order-item">
        <div class="order-header">
          <span class="order-type">{{ formatType(order.type) }}</span>
          <span class="order-amount">¥{{ order.amount }}</span>
        </div>
        <div class="order-info">
          <div class="info-row">
            <span>交易时间:</span>
            <span>{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="info-row">
            <span>状态:</span>
            <span class="status-badge" :class="order.status.toLowerCase()">
              {{ formatStatus(order.status) }}
            </span>
          </div>
          <div class="info-row" v-if="order.orderId">
            <span>订单号:</span>
            <span class="order-id">{{ order.orderId }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="isLoading" class="loading-state">
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/userStore'
import { api } from '../utils/request'

const userStore = useUserStore()
const orders = ref<any[]>([])
const isLoading = ref(true)

const fetchOrders = async () => {
  if (!userStore.userId) return
  isLoading.value = true
  try {
    const res = await api.get(`/api/membership/history?userId=${userStore.userId}`)
    if (res.ok) {
      orders.value = await res.json()
    }
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
}

const formatType = (type: string) => {
  return type === 'YEARLY_VIP' ? '包年VIP会员' : '包月VIP会员'
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待支付',
    'PAID': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.page-padding {
  padding: 1.5rem 1rem;
  background: var(--c-bg);
  min-height: 100vh;
}

.header-row {
  display: flex; align-items: center; gap: 16px; margin-bottom: 24px;
}
.back-btn { font-size: 1.5rem; color: var(--c-text-light); }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  margin-top: 100px; color: var(--c-text-light);
}
.empty-icon { font-size: 3rem; margin-bottom: 1rem; }

.order-list {
  display: flex; flex-direction: column; gap: 16px;
}

.order-item {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: var(--shadow-clay-sm);
}

.order-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.order-type { font-weight: bold; color: var(--c-text); }
.order-amount { font-weight: 900; font-size: 1.1rem; color: var(--c-primary); }

.order-info {
  font-size: 0.85rem; color: var(--c-text-light);
  display: flex; flex-direction: column; gap: 8px;
}

.info-row {
  display: flex; justify-content: space-between;
}

.status-badge {
  padding: 2px 8px; border-radius: 4px; font-size: 0.75rem;
}
.status-badge.paid { background: #E3FCEF; color: #00A65F; }
.status-badge.pending { background: #FFF4E5; color: #FF9F43; }
.status-badge.cancelled { background: #F1F2F6; color: #A4B0C0; }

.order-id { font-family: monospace; font-size: 0.75rem; word-break: break-all; }
</style>
