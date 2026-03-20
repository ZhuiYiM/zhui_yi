<template>
  <div v-if="forwardedProduct || loading" class="forwarded-product-card" @click="handleClick">
    <!-- 加载中 -->
    <div v-if="loading && !forwardedProduct" class="loading-forwarded">
      <span class="loading-icon">🔄</span>
      <span class="loading-text">正在加载商品...</span>
    </div>
    
    <!-- 已加载内容 -->
    <div v-else-if="forwardedProduct">
      <div class="forwarded-header">
        <span class="forwarded-icon">🛍️</span>
        <span class="forwarded-label">分享自商品</span>
        <span class="forwarded-seller" :class="{ 'deleted': isDeleted }">
          @{{ forwardedProduct.sellerName }}
        </span>
      </div>
      
      <div class="forwarded-content" :class="{ 'deleted': isDeleted }">
        <div class="product-info-row">
          <img 
            v-if="forwardedProduct.image" 
            :src="forwardedProduct.image" 
            alt="商品图片" 
            class="product-thumbnail"
          >
          <div class="product-details">
            <h4 class="product-title">{{ forwardedProduct.title }}</h4>
            <div class="product-price">¥{{ forwardedProduct.price }}</div>
          </div>
        </div>
        <p class="product-desc">{{ forwardedProduct.description }}</p>
      </div>
      
      <div class="forwarded-footer">
        <span class="forwarded-time">{{ formatDate(forwardedProduct.createdAt) }}</span>
        <span class="forwarded-hint" v-if="!isDeleted">点击查看商品详情</span>
        <span class="forwarded-hint.deleted" v-else>商品已下架或删除</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { productAPI } from '@/api/product';

const props = defineProps({
  forwardedFromProductId: {
    type: [Number, String],
    required: true
  }
});

const emit = defineEmits(['click']);

const forwardedProduct = ref(null);
const loading = ref(false);
const isDeleted = ref(false); // 标记商品是否已删除/下架

// 加载被转发的商品信息
const loadForwardedProduct = async () => {
  loading.value = true;
  try {
    const response = await productAPI.getProductDetail(props.forwardedFromProductId);
    console.log('📦 商品详情响应:', response);
    
    // 后端返回的数据结构是：{ product: {...}, seller: {...}, categoryName: '...', isFavorite: false }
    // 需要从 response.product 中获取商品信息
    const productData = response.product || response;
    
    if (productData && (productData.id || productData.productId)) {
      forwardedProduct.value = {
        id: productData.id || productData.productId,
        sellerName: response.seller?.username || productData.sellerName || '未知卖家',
        title: productData.title,
        price: productData.price,
        description: productData.description?.substring(0, 100) || '暂无描述',
        image: productData.images && productData.images.length > 0 ? productData.images[0] : null,
        createdAt: productData.updatedAt || productData.createdAt
      };
      
      // 检查商品状态
      isDeleted.value = productData.status !== 1; // status !== 1 表示已下架或售出
      console.log('✅ 商品信息加载成功:', forwardedProduct.value);
    } else {
      console.error('❌ 响应数据缺少 id 字段:', response);
      throw new Error('响应数据缺少 id 字段');
    }
  } catch (error) {
    console.error('加载商品信息失败:', error);
    // 如果加载失败，使用默认信息
    forwardedProduct.value = {
      id: props.forwardedFromProductId,
      sellerName: '未知卖家',
      title: '⚠️ 该商品已被删除或无法查看',
      price: '0.00',
      description: '商品不存在或已下架',
      image: null,
      createdAt: new Date().toISOString()
    };
    isDeleted.value = true;
  } finally {
    loading.value = false;
  }
};

const handleClick = () => {
  if (forwardedProduct.value && forwardedProduct.value.id) {
    emit('click', forwardedProduct.value.id);
  }
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  
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
    return date.toLocaleDateString('zh-CN');
  }
};

onMounted(() => {
  loadForwardedProduct();
});
</script>

<style scoped>
.forwarded-product-card {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  border-left: 3px solid #FF6B6B;
  border-radius: 8px;
  padding: 12px 15px;
  margin: 15px 0;
  cursor: pointer;
  transition: all 0.3s;
}

/* 加载状态 */
.loading-forwarded {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  color: #999;
  font-size: 0.9rem;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.forwarded-product-card:hover {
  background: linear-gradient(135deg, #ffe6e6 0%, #ffd4d4 100%);
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.15);
}

.forwarded-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 0.85rem;
}

.forwarded-icon {
  font-size: 1rem;
}

.forwarded-label {
  color: #666;
  font-weight: 500;
}

.forwarded-seller {
  color: #FF6B6B;
  font-weight: 600;
}

.forwarded-seller.deleted {
  color: #999;
  text-decoration: line-through;
}

.forwarded-content {
  color: #555;
  line-height: 1.5;
  font-size: 0.9rem;
  margin-bottom: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 6px;
}

.forwarded-content.deleted {
  color: #999;
  background: rgba(0, 0, 0, 0.03);
  font-style: italic;
}

.product-info-row {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.product-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-title {
  margin: 0 0 6px;
  font-size: 0.9rem;
  color: #333;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 1.1rem;
  font-weight: bold;
  color: #FF6B6B;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.product-desc {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.forwarded-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
}

.forwarded-time {
  color: #999;
}

.forwarded-hint {
  color: #FF6B6B;
  font-style: italic;
}

.forwarded-hint.deleted {
  color: #999;
  font-style: italic;
  font-size: 0.8rem;
}
</style>
