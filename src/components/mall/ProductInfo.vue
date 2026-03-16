<template>
  <div class="product-info-section">
    <h1 class="product-title">{{ product.title }}</h1>
    
    <div class="product-meta-top">
      <span class="view-count">👁️ {{ product.viewCount || 0 }}次浏览</span>
      <span class="like-count">👍 {{ product.likeCount || 0 }}人想要</span>
      <span class="time">{{ formatTime(product.createdAt) }}</span>
    </div>

    <div class="price-section">
      <span class="current-price">¥{{ product.price }}</span>
      <span v-if="product.originalPrice" class="original-price">原价 ¥{{ product.originalPrice }}</span>
    </div>

    <div class="description-section">
      <h3>商品描述</h3>
      <p>{{ product.description }}</p>
    </div>

    <!-- 分类信息 -->
    <div v-if="product.categoryName" class="category-info">
      <span class="label">分类：</span>
      <span class="value">{{ product.categoryName }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  product: {
    type: Object,
    required: true
  }
});

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  
  const now = new Date();
  const time = new Date(timeStr);
  const diff = now - time;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  if (diff < minute) return '刚刚';
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)}小时前`;
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`;
  return time.toLocaleDateString('zh-CN');
};
</script>

<style scoped>
.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
}

.product-meta-top {
  display: flex;
  gap: 20px;
  font-size: 0.9rem;
  color: #777;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 2px solid #f0f2f5;
}

.current-price {
  font-size: 2rem;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 1rem;
  color: #999;
  text-decoration: line-through;
}

.description-section h3 {
  margin: 0 0 10px;
  font-size: 1.1rem;
  color: #333;
}

.description-section p {
  margin: 0;
  font-size: 0.95rem;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.category-info {
  display: flex;
  gap: 10px;
  font-size: 0.9rem;
}

.category-info .label {
  color: #777;
}

.category-info .value {
  color: #333;
  font-weight: 600;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .product-title {
    font-size: 1.4rem;
  }
  
  .current-price {
    font-size: 1.6rem;
  }
}
</style>
