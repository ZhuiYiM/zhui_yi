<template>
  <div class="review-item">
    <div class="review-body">
      <!-- 左侧头像 -->
      <img 
        :src="review.userAvatar || defaultAvatar" 
        :alt="review.userName" 
        class="reviewer-avatar"
      >
      
      <!-- 右侧评价内容 -->
      <div class="review-content-wrapper">
        <div class="review-header">
          <div class="reviewer-info">
            <div class="reviewer-name">{{ review.userName }}</div>
            <div class="review-time">{{ formatTime(review.createdAt) }}</div>
          </div>
          <div class="review-rating">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= review.rating }">
              {{ i <= review.rating ? '⭐' : '☆' }}
            </span>
          </div>
        </div>
        
        <!-- 评价商品和评价内容同行 -->
        <div class="review-product-content">
          <!-- 评价商品 -->
          <div v-if="review.product" class="reviewed-product">
            <img 
              :src="review.product.image || defaultImage" 
              :alt="review.product.title" 
              class="product-image"
              @click="$emit('view-product', review.product.id)"
            >
          </div>
          
          <!-- 评价内容 -->
          <div class="review-content">
            {{ review.content }}
          </div>
        </div>
        
        <div v-if="review.images && review.images.length > 0" class="review-images">
          <img 
            v-for="(img, idx) in review.images" 
            :key="idx" 
            :src="img" 
            :alt="`评价图片${idx + 1}`"
            class="review-image"
            @click="$emit('view-image', img)"
          >
        </div>
        <div class="review-actions">
          <button class="like-btn" @click="$emit('like', review)">
            {{ review.isLiked ? '❤️' : '🤍' }} {{ review.likeCount || 0 }}
          </button>
          <button class="reply-btn" @click="$emit('reply', review)">
            💬 回复
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  review: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['view-product', 'view-image', 'like', 'reply']);

const defaultImage = 'https://placehold.co/500x500/4A90E2/FFFFFF?text=商品图片';
const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

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
.review-item {
  padding: 20px;
  border: 1px solid #e1e5f2;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.review-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.05);
}

.review-body {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.reviewer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

/* 评价内容包装器 */
.review-content-wrapper {
  flex: 1;
  min-width: 0;
  text-align: left;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reviewer-info {
  flex: 1;
  text-align: left;
}

.reviewer-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.review-time {
  font-size: 0.8rem;
  color: #777;
}

.review-rating {
  display: flex;
  gap: 5px;
}

.review-rating .star {
  font-size: 1rem;
}

/* 评价商品和评价内容同行布局 */
.review-product-content {
  display: flex;
  gap: 15px;
  align-items: flex-start;
  margin-bottom: 15px;
}

.review-product-content .reviewed-product {
  flex-shrink: 0;
}

.reviewed-product {
  margin-bottom: 0;
}

.reviewed-product .product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 2px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.3s ease;
  display: block;
}

.reviewed-product .product-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  border-color: #4A90E2;
}

.review-content {
  flex: 1;
  min-width: 0;
  line-height: 1.6;
  color: #333;
  text-align: left;
  margin: 0;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.review-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-actions {
  display: flex;
  gap: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f2f5;
  justify-content: flex-start;
}

.like-btn,
.reply-btn {
  padding: 6px 15px;
  background: none;
  border: 1px solid #e0e0e0;
  border-radius: 15px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  color: #666;
}

.like-btn:hover,
.reply-btn:hover {
  border-color: #4A90E2;
  color: #4A90E2;
  background-color: #f0f7ff;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .review-body {
    gap: 10px;
  }
  
  .reviewer-avatar {
    width: 40px;
    height: 40px;
  }
  
  .review-header {
    flex-wrap: wrap;
  }
}
</style>
