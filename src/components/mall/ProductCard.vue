<template>
  <div :class="['product-card', { 'mobile': isMobile }]" @click="handleClick">
    <!-- 商品图片 -->
    <div class="product-image-container">
      <img 
        :src="product.images && product.images.length > 0 ? product.images[0] : defaultImage" 
        :alt="product.title" 
        class="product-image"
      >
      <!-- 状态标签 -->
      <span v-if="product.status === 0" class="status-tag off-shelf">已下架</span>
      <span v-else-if="product.status === 2" class="status-tag sold-out">已售出</span>
      <span v-else-if="product.isHot" class="status-tag hot">热门</span>
      
      <!-- 收藏按钮 -->
      <button 
        class="favorite-btn" 
        :class="{ active: isFavorite }"
        @click.stop="toggleFavorite"
      >
        {{ isFavorite ? '⭐' : '☆' }}
      </button>
    </div>
    
    <!-- 商品信息 -->
    <div class="product-info">
      <h3 class="product-title">{{ product.title }}</h3>
      <p class="product-desc">{{ product.description }}</p>
      
      <div class="product-price-row">
        <span class="product-price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
      </div>
      
      <div class="product-meta">
        <span class="product-location">📍 {{ product.location || '校内' }}</span>
        <span class="product-time">{{ formatTime(product.createdAt) }}</span>
      </div>
      
      <div class="product-stats">
        <span class="stat-item">👁️ {{ product.viewCount || 0 }}</span>
        <span class="stat-item">👍 {{ product.likeCount || 0 }}</span>
      </div>
      
      <!-- 卖家信息（可选） -->
      <div v-if="showSellerInfo && product.seller" class="seller-info">
        <img 
          :src="product.seller.avatar || defaultAvatar" 
          :alt="product.seller.name"
          class="seller-avatar"
          @click.stop="viewSeller"
        >
        <span class="seller-name" @click.stop="viewSeller">{{ product.seller.name }}</span>
        <span v-if="product.seller.identityTag" class="seller-tag" :class="product.seller.identityTag">
          {{ getIdentityTagName(product.seller.identityTag) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  isMobile: {
    type: Boolean,
    default: false
  },
  showSellerInfo: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click', 'favorite-change', 'view-seller']);

const router = useRouter();
const isFavorite = ref(props.product.isFavorite || false);

// 默认图片和头像
const defaultImage = 'https://placehold.co/300x200/4A90E2/FFFFFF?text=商品图片';
const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

// 身份标签名称映射
const getIdentityTagName = (tag) => {
  const tagMap = {
    'student': '学生',
    'merchant': '商家',
    'admin': '管理员',
    'organization': '团体',
    'society': '社会'
  };
  return tagMap[tag] || tag;
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  
  const now = new Date();
  const time = new Date(timeStr);
  const diff = now - time;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`;
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`;
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`;
  } else {
    return time.toLocaleDateString('zh-CN');
  }
};

// 处理点击
const handleClick = () => {
  emit('click', props.product);
  
  // 默认跳转到商品详情页
  if (props.product.id) {
    const targetPath = `/product/${props.product.id}`;
    router.push(targetPath);
  }
};

// 切换收藏状态
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
  emit('favorite-change', {
    productId: props.product.id,
    isFavorite: isFavorite.value
  });
};

// 查看卖家
const viewSeller = () => {
  if (props.product.seller && props.product.seller.id) {
    emit('view-seller', props.product.seller);
    router.push(`/user/${props.product.seller.id}`);
  }
};
</script>

<style scoped>
.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.product-image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f0f2f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

/* 状态标签 */
.status-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
  z-index: 1;
}

.status-tag.off-shelf {
  background-color: #999;
}

.status-tag.sold-out {
  background-color: #FF6B6B;
}

.status-tag.hot {
  background-color: #FF9500;
}

/* 收藏按钮 */
.favorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.9);
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.favorite-btn:hover {
  transform: scale(1.1);
}

.favorite-btn.active {
  background-color: #FFD700;
}

/* 商品信息 */
.product-info {
  padding: 15px;
}

.product-title {
  margin: 0 0 8px;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-desc {
  margin: 0 0 10px;
  font-size: 0.85rem;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.3;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.product-price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 0.9rem;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #777;
  margin-bottom: 8px;
}

.product-stats {
  display: flex;
  gap: 15px;
  font-size: 0.8rem;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
}

/* 卖家信息 */
.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.seller-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.seller-name {
  flex: 1;
  font-size: 0.85rem;
  color: #666;
  cursor: pointer;
}

.seller-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
  color: white;
}

.seller-tag.student {
  background-color: #4A90E2;
}

.seller-tag.merchant {
  background-color: #50C878;
}

.seller-tag.admin {
  background-color: #FF6B6B;
}

.seller-tag.organization {
  background-color: #9B59B6;
}

/* 移动端适配 */
.product-card.mobile {
  border-radius: 8px;
}

.product-card.mobile .product-image-container {
  height: 150px;
}

.product-card.mobile .product-info {
  padding: 10px;
}

.product-card.mobile .product-title {
  font-size: 0.95rem;
}

.product-card.mobile .product-desc {
  font-size: 0.8rem;
}

.product-card.mobile .product-price {
  font-size: 1.1rem;
}
</style>
