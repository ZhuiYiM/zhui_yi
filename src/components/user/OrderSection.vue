<template>
  <div class="orders-section">
    <div class="section-header">
      <h2>我的订单</h2>
      <button v-if="!isMobile" class="view-more-btn" @click="$emit('view-all')">查看全部</button>
      <span v-else class="view-more" @click="$emit('view-all')">查看更多 ></span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner-small"></div>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="orders.length === 0" class="empty-orders">
      <div class="empty-icon">📦</div>
      <p>暂无订单记录</p>
    </div>

    <!-- 订单列表 -->
    <div v-else class="orders-grid">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-item"
        @click="$emit('view-detail', order.id)"
      >
        <div class="order-header">
          <span class="order-id">#{{ order.id }}</span>
          <span class="order-status" :class="order.status">{{ order.statusText }}</span>
        </div>
        <div class="order-content">
          <img :src="order.image" :alt="order.title" class="order-image">
          <div class="order-info">
            <h3>{{ order.title }}</h3>
            <p class="order-price">¥{{ order.price }}</p>
            <p class="order-date">{{ order.date }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  orders: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  isMobile: {
    type: Boolean,
    default: false
  }
});

defineEmits(['view-all', 'view-detail']);
</script>

<style scoped>
.orders-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.orders-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.orders-section h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.view-more {
  color: #4A90E2;
  font-size: 0.9rem;
  cursor: pointer;
}

.view-more-btn {
  background-color: #4A90E2;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.view-more-btn:hover {
  background-color: #4578d9;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px;
}

.spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-orders {
  text-align: center;
  padding: 60px 20px;
}

.empty-orders .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-orders p {
  color: #999;
  font-size: 14px;
}

/* 订单网格 */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.order-item {
  border: 1px solid #e1e5f2;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
  height: fit-content;
}

.order-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f8f9fa;
}

.order-id {
  font-weight: bold;
  color: #333;
  font-size: 0.9rem;
}

.order-status {
  padding: 3px 6px;
  border-radius: 12px;
  font-size: 0.7rem;
}

.order-status.completed {
  background-color: #d4edda;
  color: #155724;
}

.order-status.pending {
  background-color: #fff3cd;
  color: #856404;
}

.order-status.canceled {
  background-color: #f8d7da;
  color: #721c24;
}

.order-status.shipping {
  background-color: #d1ecf1;
  color: #0c5460;
}

.order-status.processing {
  background-color: #e2e3e5;
  color: #383d41;
}

.order-content {
  display: flex;
  padding: 12px;
  gap: 10px;
}

.order-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
}

.order-info h3 {
  margin: 0 0 5px 0;
  font-size: 0.9rem;
  color: #333;
}

.order-price {
  margin: 0 0 3px 0;
  font-size: 1rem;
  font-weight: bold;
  color: #e74c3c;
}

.order-date {
  margin: 0;
  font-size: 0.7rem;
  color: #777;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .orders-section {
    margin: 10px;
    padding: 15px;
  }

  .orders-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .order-item {
    padding: 15px;
  }
}
</style>
