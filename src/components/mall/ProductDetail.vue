<template>
  <div :class="['product-detail-container', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="mall"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 商品详情 -->
      <div v-else-if="product" class="product-detail">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          ← 返回交易中心
        </button>

        <div :class="['detail-content', { 'mobile-layout': isMobile }]">
          <!-- 左侧：商品图片 -->
          <div class="product-gallery">
            <div class="main-image">
              <img 
                :src="currentImage || (product.images && product.images[0]) || defaultImage" 
                :alt="product.title"
              >
            </div>
            
            <!-- 缩略图列表 -->
            <div v-if="product.images && product.images.length > 1" class="thumbnail-list">
              <div
                v-for="(image, index) in product.images"
                :key="index"
                :class="['thumbnail-item', { active: currentImageIndex === index }]"
                @click="currentImageIndex = index"
              >
                <img :src="image" :alt="`缩略图${index + 1}`">
              </div>
            </div>
          </div>

          <!-- 右侧：商品信息 -->
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

            <!-- 卖家信息 -->
            <div class="seller-section">
              <h3>卖家信息</h3>
              <div class="seller-info" @click="viewSeller">
                <img 
                  :src="sellerInfo.avatar || defaultAvatar" 
                  :alt="sellerInfo.name"
                  class="seller-avatar"
                >
                <div class="seller-details">
                  <span class="seller-name">{{ sellerInfo.name }}</span>
                  <span v-if="sellerInfo.identityTag" class="seller-tag" :class="sellerInfo.identityTag">
                    {{ getIdentityTagName(sellerInfo.identityTag) }}
                  </span>
                </div>
                <button class="contact-btn" @click.stop="contactSeller">联系卖家</button>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button 
                class="btn btn-primary" 
                @click="buyNow"
                :disabled="product.status !== 1"
              >
                {{ product.status === 2 ? '已售出' : product.status === 0 ? '已下架' : '立即购买' }}
              </button>
              <button 
                class="btn btn-secondary" 
                @click="toggleFavorite"
                :class="{ active: isFavorite }"
              >
                {{ isFavorite ? '⭐ 已收藏' : '☆ 收藏' }}
              </button>
              <button class="btn btn-share" @click="shareProduct">
                🔗 分享
              </button>
            </div>
          </div>
        </div>

        <!-- 猜你喜欢 -->
        <section class="recommended-section">
          <h3>猜你喜欢</h3>
          <ProductList 
            :products="recommendedProducts" 
            :is-mobile="isMobile"
            :columns="4"
            :mobile-columns="2"
            :show-load-more="false"
            @product-click="goToProduct"
          />
        </section>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !product" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>商品不存在</h3>
        <p>该商品可能已被删除或不存在</p>
        <button @click="goBack" class="primary-btn">返回交易中心</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import ProductList from './ProductList.vue';
import { productAPI } from '@/api/product';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const product = ref(null);
const currentUser = ref(null);
const isMobile = ref(window.innerWidth <= 768);
const currentImageIndex = ref(0);
const isFavorite = ref(false);
const recommendedProducts = ref([]);

// 默认图片和头像
const defaultImage = 'https://placehold.co/500x500/4A90E2/FFFFFF?text=商品图片';
const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

// 计算属性
const currentImage = computed(() => {
  if (product.value?.images && product.value.images.length > 0) {
    return product.value.images[currentImageIndex.value];
  }
  return null;
});

const sellerInfo = computed(() => {
  if (product.value?.seller) {
    return {
      name: product.value.seller.username || product.value.seller.name || '未知卖家',
      avatar: product.value.seller.avatarUrl || product.value.seller.avatar,
      identityTag: product.value.seller.level1Tag || product.value.seller.identityTag
    };
  }
  return { name: '未知卖家', avatar: defaultAvatar, identityTag: null };
});

// 获取用户信息
const getUserInfo = () => {
  const token = localStorage.getItem('token');
  if (token) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    currentUser.value = {
      name: user.name || user.username || '',
      studentId: user.studentId || user.student_id || '',
      avatar: user.avatar || ''
    };
  } else {
    currentUser.value = null;
  }
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
  
  if (diff < minute) return '刚刚';
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)}小时前`;
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`;
  return time.toLocaleDateString('zh-CN');
};

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

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true;
    const response = await productAPI.getProductDetail(route.params.id);
    
    console.log('🔍 商品详情响应:', response); // 调试日志
    
    if (response) {
      product.value = response;
      
      // 模拟推荐商品（实际项目中应该调用 API）
      recommendedProducts.value = [
        {
          id: 999,
          title: '相关商品推荐 1',
          price: 50,
          images: ['https://placehold.co/300x200/FF6B6B/FFFFFF?text=推荐 1'],
          viewCount: 100,
          likeCount: 20
        },
        {
          id: 998,
          title: '相关商品推荐 2',
          price: 80,
          images: ['https://placehold.co/300x200/50C878/FFFFFF?text=推荐 2'],
          viewCount: 150,
          likeCount: 30
        }
      ];
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error('获取商品详情失败');
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到商品
const goToProduct = (productItem) => {
  if (productItem.id) {
    router.push(`/product/${productItem.id}`);
  }
};

// 查看卖家
const viewSeller = () => {
  if (product.value?.sellerId) {
    router.push(`/user/${product.value.sellerId}`);
  }
};

// 联系卖家
const contactSeller = () => {
  ElMessage.info('联系卖家功能开发中');
};

// 立即购买
const buyNow = () => {
  if (product.value?.status !== 1) {
    ElMessage.warning('该商品无法购买');
    return;
  }
  ElMessage.info('购买功能开发中');
};

// 切换收藏
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
  ElMessage.success(isFavorite.value ? '收藏成功' : '取消收藏');
};

// 分享商品
const shareProduct = () => {
  ElMessage.info('分享功能开发中');
};

// 设备检测
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  getUserInfo();
  fetchProductDetail();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.product-detail-container {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.main-content {
  padding: 0px;
  margin-left: 210px;
  display: flex;
  flex-direction: column;
}

.main-content.full-width {
  margin-left: 0;
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

.primary-btn {
  margin-top: 20px;
  padding: 12px 30px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
}

/* 返回按钮 */
.back-btn {
  margin: 20px;
  padding: 10px 20px;
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  align-self: flex-start;
}

.back-btn:hover {
  background-color: #4A90E2;
  color: white;
}

/* 商品详情内容 */
.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin: 20px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.detail-content.mobile-layout {
  grid-template-columns: 1fr;
  padding: 20px;
}

/* 商品图片画廊 */
.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f0f2f5;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active {
  border-color: #4A90E2;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息区域 */
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

/* 卖家信息 */
.seller-section h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #333;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.seller-info:hover {
  background-color: #e9f7fe;
}

.seller-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.seller-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.seller-tag {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
  align-self: flex-start;
}

.seller-tag.student {
  background-color: #4A90E2;
}

.seller-tag.merchant {
  background-color: #50C878;
}

.contact-btn {
  padding: 8px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.contact-btn:hover {
  background-color: #5a9fd6;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f2f5;
}

.btn {
  flex: 1;
  padding: 15px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #e74c3c;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #c0392b;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: white;
  color: #f39c12;
  border: 2px solid #f39c12;
}

.btn-secondary.active {
  background-color: #f39c12;
  color: white;
}

.btn-secondary:hover {
  background-color: #f39c12;
  color: white;
}

.btn-share {
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
}

.btn-share:hover {
  background-color: #4A90E2;
  color: white;
}

/* 推荐商品 */
.recommended-section {
  margin: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.recommended-section h3 {
  margin: 0 0 20px;
  font-size: 1.2rem;
  color: #333;
}

/* 移动端适配 */
.product-detail-container.mobile .detail-content {
  margin: 15px;
  padding: 20px;
}

.product-detail-container.mobile .main-image {
  height: 300px;
}

.product-detail-container.mobile .product-title {
  font-size: 1.4rem;
}

.product-detail-container.mobile .current-price {
  font-size: 1.6rem;
}

.product-detail-container.mobile .action-buttons {
  flex-direction: column;
}

.product-detail-container.mobile .thumbnail-item {
  width: 60px;
  height: 60px;
}
</style>
