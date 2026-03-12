<template>
  <div :class="['product-list', { 'mobile': isMobile }]">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="products.length === 0" class="empty-state">
      <div class="empty-icon">📦</div>
      <h3>暂无商品</h3>
      <p>{{ emptyText || '暂时没有相关商品' }}</p>
    </div>
    
    <!-- 商品列表 -->
    <div v-else :class="['product-grid', gridClass]">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
        :is-mobile="isMobile"
        :show-seller-info="showSellerInfo"
        @click="handleProductClick"
        @favorite-change="handleFavoriteChange"
        @view-seller="handleViewSeller"
      />
    </div>
    
    <!-- 加载更多 -->
    <div v-if="showLoadMore && !loading && hasMore" class="load-more">
      <button @click="handleLoadMore" class="load-more-btn">
        {{ loadMoreText || '加载更多' }}
      </button>
    </div>
    
    <!-- 无更多数据 -->
    <div v-if="!hasMore && products.length > 0" class="no-more">
      <span>{{ noMoreText || '没有更多了' }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import ProductCard from './ProductCard.vue';

const props = defineProps({
  products: {
    type: Array,
    required: true,
    default: () => []
  },
  isMobile: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  showSellerInfo: {
    type: Boolean,
    default: false
  },
  columns: {
    type: Number,
    default: 4 // 桌面端默认 4 列
  },
  mobileColumns: {
    type: Number,
    default: 1 // 移动端默认 1 列
  },
  showLoadMore: {
    type: Boolean,
    default: true
  },
  hasMore: {
    type: Boolean,
    default: true
  },
  emptyText: {
    type: String,
    default: ''
  },
  loadMoreText: {
    type: String,
    default: ''
  },
  noMoreText: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['product-click', 'favorite-change', 'view-seller', 'load-more']);

// 计算网格布局类名
const gridClass = computed(() => {
  if (props.isMobile) {
    return `mobile-grid-${props.mobileColumns}`;
  }
  return `desktop-grid-${props.columns}`;
});

// 处理商品点击
const handleProductClick = (product) => {
  emit('product-click', product);
};

// 处理收藏变化
const handleFavoriteChange = (data) => {
  emit('favorite-change', data);
};

// 处理查看卖家
const handleViewSeller = (seller) => {
  emit('view-seller', seller);
};

// 加载更多
const handleLoadMore = () => {
  emit('load-more');
};
</script>

<style scoped>
.product-list {
  width: 100%;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px;
  font-size: 1.2rem;
  color: #666;
}

.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}

/* 商品网格 - 桌面端 */
.product-grid {
  display: grid;
  gap: 20px;
  padding: 15px;
}

.product-grid.desktop-grid-1 {
  grid-template-columns: repeat(1, 1fr);
}

.product-grid.desktop-grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.product-grid.desktop-grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.product-grid.desktop-grid-4 {
  grid-template-columns: repeat(4, 1fr);
}

.product-grid.desktop-grid-5 {
  grid-template-columns: repeat(5, 1fr);
}

/* 商品网格 - 移动端 */
.product-grid.mobile-grid-1 {
  grid-template-columns: repeat(1, 1fr);
  gap: 15px;
  padding: 10px;
}

.product-grid.mobile-grid-2 {
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.load-more-btn {
  padding: 12px 40px;
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background-color: #4A90E2;
  color: white;
}

/* 无更多数据 */
.no-more {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 0.9rem;
}

/* 移动端适配 */
.product-list.mobile .product-grid {
  padding: 10px;
}

.product-list.mobile .load-more-btn {
  padding: 10px 30px;
  font-size: 0.95rem;
}
</style>
