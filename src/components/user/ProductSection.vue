<template>
  <div class="products-section">
    <!-- 商品子标签 -->
    <div class="product-tabs">
      <button 
        @click="$emit('tab-change', 'published')"
        class="tab-btn"
        :class="{ active: currentTab === 'published' }"
      >
        {{ isCurrentUser ? '我的商品' : 'TA 的商品' }}
      </button>
      <button 
        @click="$emit('tab-change', 'favorites')"
        class="tab-btn"
        :class="{ active: currentTab === 'favorites' }"
        v-if="isCurrentUser"
      >
        我的收藏
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner-small"></div>
      <span>加载中...</span>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="products.length === 0" class="empty-products">
      <div class="empty-icon">📦</div>
      <p>{{ 
        currentTab === 'published' ? (isCurrentUser ? '暂无发布的商品' : 'TA 暂无发布的商品') : 
        '暂无收藏的商品'
      }}</p>
    </div>
    
    <!-- 商品列表 -->
    <div v-else class="user-products-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="$emit('view-product', product)"
      >
        <img 
          :src="product.images && product.images.length > 0 ? product.images[0] : defaultProductImage" 
          :alt="product.title"
          class="product-image"
        >
        <div class="product-info">
          <h4 class="product-title">{{ product.title }}</h4>
          <p class="product-price">¥{{ product.price }}</p>
          <div class="product-meta">
            <span class="product-status" :class="product.status === 1 ? 'onsale' : 'sold'">
              {{ product.status === 1 ? '在售' : product.status === 2 ? '已售出' : '已下架' }}
            </span>
            <span class="product-stats">👁️ {{ product.viewCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  products: {
    type: Array,
    default: () => []
  },
  currentTab: {
    type: String,
    default: 'published'
  },
  isCurrentUser: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
});

defineEmits(['tab-change', 'view-product']);

const defaultProductImage = 'https://placehold.co/300x200/4A90E2/FFFFFF?text=商品图片';
</script>

<style scoped>
.products-section {
  margin-bottom: 30px;
}

.product-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0;
}

.tab-btn {
  padding: 12px 24px;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #409eff;
}

.tab-btn.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 600;
}

.user-products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  background: #f5f7fa;
}

.product-info {
  padding: 16px;
}

.product-title {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #333;
  font-weight: 600;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #e74c3c;
  font-weight: bold;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.product-status {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.product-status.onsale {
  background: #e8f5e9;
  color: #4caf50;
}

.product-status.sold {
  background: #ffebee;
  color: #f44336;
}

.product-stats {
  color: #999;
}

.loading-state {
  text-align: center;
  padding: 40px;
}

.spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-products {
  text-align: center;
  padding: 60px 20px;
}

.empty-products .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-products p {
  color: #999;
  font-size: 14px;
}
</style>
