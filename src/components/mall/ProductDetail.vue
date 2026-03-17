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
          ← 返回
        </button>

        <div :class="['detail-content', { 'mobile-layout': isMobile }]">
          <!-- 左侧：商品图片 -->
          <ProductGallery 
            :images="product.images" 
            :current-index="currentImageIndex"
            :title="product.title"
            @update:index="currentImageIndex = $event"
            @preview-image="previewImage"
          />

          <!-- 右侧：商品信息 -->
          <div class="product-info-section">
            <ProductInfo :product="product" />
            
            <!-- 卖家信息 -->
            <div v-if="sellerInfo" class="seller-info-in-detail">
              <SellerInfo 
                :seller="sellerInfo"
                @view-seller="navigateToUserProfile"
                @contact="contactSeller"
              />
            </div>
            
            <!-- 操作按钮 -->
            <div class="product-actions-in-detail">
              <ProductActions 
                :product-status="product.status"
                :is-favorite="isFavorite"
                :is-owner="isCurrentProductOwner"
                @buy="handleProductAction"
                @favorite="toggleFavorite"
                @share="shareProduct"
              />
            </div>
          </div>
        </div>

        <!-- 购买确认弹窗 -->
        <BuyConfirmModal
          v-model:visible="showBuyModal"
          :loading="buyingLoading"
          :product-image="product.images && product.images.length > 0 ? product.images[0] : ''"
          :product-title="product.title"
          :product-price="product.price"
          :seller-name="sellerInfo?.name || '未知卖家'"
          :contact-info="product.contact_info"
          :trade-methods="product.trade_methods || []"
          :trade-location="product.location"
          :max-quantity="5"
          :specifications="productSpecifications"
          @close="closeBuyModal"
          @confirm="confirmBuy"
        />

        <!-- 商家评价区域 -->
        <ReviewsSection 
          :reviews="reviews"
          :average-score="averageScore"
          @write-review="writeReview"
          @view-product="viewProduct"
          @view-image="viewImage"
          @like="toggleReviewLike"
          @reply="replyReview"
        />

        <!-- 猜你喜欢 -->
        <RecommendedProducts 
          :products="recommendedProducts"
          :is-mobile="isMobile"
          @product-click="goToProduct"
        />
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !product" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>商品不存在</h3>
        <p>该商品可能已被删除或不存在</p>
        <button @click="goBack" class="primary-btn">返回</button>
      </div>
    </main>
    
    <!-- 图片预览弹窗 -->
    <div v-if="showImagePreview" class="modal-overlay" @click="closeImagePreview">
      <div class="modal-content" @click.stop>
        <button @click="closeImagePreview" class="close-btn">×</button>
        <img :src="previewImageUrl" alt="预览图片" class="preview-large-image">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import ProductGallery from './ProductGallery.vue';
import ProductInfo from './ProductInfo.vue';
import SellerInfo from './SellerInfo.vue';
import ProductActions from './ProductActions.vue';
import ReviewsSection from './ReviewsSection.vue';
import RecommendedProducts from './RecommendedProducts.vue';
import BuyConfirmModal from './BuyConfirmModal.vue';
import { productAPI } from '@/api/product';
import { orderAPI } from '@/api/order';

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
const reviews = ref([]);
const averageScore = ref(5.0);
const showImagePreview = ref(false);
const previewImageUrl = ref('');
const showBuyModal = ref(false);
const buyingLoading = ref(false);
const productSpecifications = ref({}); // 商品规格数据

// 判断是否是当前用户发布的商品
const isCurrentProductOwner = computed(() => {
  if (!currentUser.value || !product.value) return false;
  const userId = JSON.parse(localStorage.getItem('user') || '{}').id;
  return product.value.sellerId === userId;
});

// 计算卖家信息
const sellerInfo = computed(() => {
  if (product.value?.seller) {
    return {
      id: product.value.seller.id,
      name: product.value.seller.username || product.value.seller.realName || '未知卖家',
      avatar: product.value.seller.avatarUrl || product.value.seller.avatar,
      identityTag: product.value.seller.level1Tag,
      college: product.value.seller.college,
      bio: product.value.seller.bio,
      productCount: product.value.seller.productCount
    };
  }
  if (product.value?.sellerId) {
    return { 
      id: product.value.sellerId,
      name: product.value.sellerName || '未知卖家',
      avatar: product.value.sellerAvatar || 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U'
    };
  }
  return null;
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

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true;
    const productId = route.params.id;
    
    if (!productId) {
      throw new Error('商品 ID 参数缺失');
    }
    
    const response = await productAPI.getProductDetail(productId);
    
    if (!response) {
      throw new Error('响应数据为空');
    }
    
    let data;
    if (response.code === 200 && response.data) {
      data = response.data;
    } else if (response.data) {
      data = response.data;
    } else {
      data = response;
    }
    
    product.value = data.product || data;
    
    // 将 seller 对象附加到 product 上
    if (data.seller && !product.value.seller) {
      product.value.seller = data.seller;
    }
    
    isFavorite.value = data.isFavorite ?? false;
    
    // 处理 images 字段
    if (product.value && typeof product.value.images === 'string') {
      try {
        product.value.images = JSON.parse(product.value.images);
      } catch (e) {
        product.value.images = [];
      }
    }
    
    // 处理规格数据
    if (data.specifications) {
      productSpecifications.value = data.specifications;
    }
    
    // 加载卖家商品和评价
    if (product.value.sellerId) {
      loadSellerProducts(product.value.sellerId);
      loadSellerReviews(product.value.sellerId);
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '获取商品详情失败');
    product.value = null;
  } finally {
    loading.value = false;
  }
};

// 加载卖家的其他商品
const loadSellerProducts = async (sellerId) => {
  try {
    const params = { sellerId, status: 1, size: 4 };
    const response = await productAPI.getProducts(params);
    
    if (response && response.records) {
      recommendedProducts.value = response.records
        .filter(p => p.id !== product.value.id)
        .slice(0, 4);
      
      // 处理 images 字段
      recommendedProducts.value.forEach(p => {
        if (typeof p.images === 'string') {
          try {
            p.images = JSON.parse(p.images);
          } catch (e) {
            p.images = [];
          }
        }
      });
    }
  } catch (error) {
    console.error('加载卖家商品失败:', error);
    recommendedProducts.value = [];
  }
};

// 加载卖家评价
const loadSellerReviews = async (sellerId) => {
  try {
    // 模拟数据 - 实际应调用 API
    reviews.value = [
      {
        id: 1,
        userId: 2,
        userName: '张三',
        userAvatar: 'https://placehold.co/50x50/4A90E2/FFFFFF?text=Z',
        content: '商品质量很好，卖家服务态度也很棒！',
        rating: 5,
        createdAt: new Date().toISOString(),
        likeCount: 3,
        isLiked: false,
        images: [],
        product: {
          id: 259,
          title: '电脑清灰换硅脂',
          price: 30,
          image: 'https://placehold.co/100x100/4A90E2/FFFFFF?text=服务'
        }
      }
    ];
    
    if (reviews.value.length > 0) {
      const total = reviews.value.reduce((sum, r) => sum + r.rating, 0);
      averageScore.value = total / reviews.value.length;
    }
  } catch (error) {
    console.error('加载评价失败:', error);
    reviews.value = [];
    averageScore.value = 5.0;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到用户主页
const navigateToUserProfile = (sellerId) => {
  if (sellerId) {
    router.push(`/user/profile/${sellerId}`);
  }
};

// 联系卖家
const contactSeller = () => {
  ElMessage.info('联系卖家功能开发中');
};

// 处理商品操作按钮点击（购买/下架）
const handleProductAction = () => {
  if (isCurrentProductOwner.value) {
    // 商家点击自己的商品 - 下架/上架操作
    toggleProductStatus();
  } else {
    // 买家点击 - 购买操作
    buyNow();
  }
};

// 切换商品状态（上架/下架）
const toggleProductStatus = async () => {
  try {
    const newStatus = product.value.status === 1 ? 3 : 1; // 1=在售，3=下架
    await productAPI.updateProductStatus(product.value.id, newStatus);
    product.value.status = newStatus;
    ElMessage.success(newStatus === 1 ? '商品已上架' : '商品已下架');
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

// 立即购买
const buyNow = () => {
  if (product.value?.status !== 1) {
    ElMessage.warning('该商品无法购买');
    return;
  }
  showBuyModal.value = true;
};

// 关闭购买弹窗
const closeBuyModal = () => {
  showBuyModal.value = false;
};

// 确认购买
const confirmBuy = async (orderInfo) => {
  try {
    buyingLoading.value = true;
    
    // 获取商品图片
    const productImage = product.value.images && product.value.images.length > 0 
      ? product.value.images[0] 
      : 'https://placehold.co/300x300/e0e0e0/999999?text=商品图片';
    
    // 创建订单数据
    const orderData = {
      productId: product.value.id,
      sellerId: product.value.sellerId,
      totalAmount: parseFloat(orderInfo.totalAmount),
      orderStatus: 0, // 待付款
      buyerContact: orderInfo.buyerContact,
      message: orderInfo.message,
      quantity: orderInfo.quantity,
      selectedSpecifications: orderInfo.selectedSpecifications || []
    };
    
    // 调用创建订单 API
    const response = await orderAPI.createOrder(orderData);
        
    // 获取订单 ID(从响应中)
    const orderId = response.id || response.data?.id;
    
    if (!orderId) {
      console.error('无法获取订单 ID, 响应结构:', JSON.stringify(response, null, 2));
      ElMessage.error('订单创建失败：无法获取订单 ID');
      return;
    }
    
    ElMessage.success('订单创建成功！即将跳转到支付页面');
    closeBuyModal();
    
    // 跳转到订单确认页面
    setTimeout(() => {
      router.push(`/order/confirmation/${orderId}?productTitle=${encodeURIComponent(product.value.title)}&quantity=${orderInfo.quantity}&image=${encodeURIComponent(productImage)}`);
    }, 1000);
  } catch (error) {
    console.error('购买失败:', error);
    ElMessage.error(error.response?.data?.message || '购买失败，请稍后重试');
  } finally {
    buyingLoading.value = false;
  }
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

// 写评价
const writeReview = () => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录后再评价');
    return;
  }
  ElMessage.info('写评价功能开发中');
};

// 查看商品
const viewProduct = (productId) => {
  if (productId) {
    router.push(`/product/${productId}`);
  }
};

// 查看图片
const viewImage = (imageUrl) => {
  // 查看图片逻辑
};

// 预览图片
const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false;
  previewImageUrl.value = '';
};

// 点赞评价
const toggleReviewLike = (review) => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录');
    return;
  }
  review.isLiked = !review.isLiked;
  review.likeCount = (review.likeCount || 0) + (review.isLiked ? 1 : -1);
};

// 回复评价
const replyReview = (review) => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录');
    return;
  }
  ElMessage.info('回复评价功能开发中');
};

// 跳转到商品
const goToProduct = (productItem) => {
  if (productItem && productItem.id) {
    router.push(`/product/${productItem.id}`);
  }
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

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      product.value = null;
      isFavorite.value = false;
      recommendedProducts.value = [];
      currentImageIndex.value = 0;
      loading.value = true;
      reviews.value = [];
      averageScore.value = 5.0;
      fetchProductDetail();
    }
  }
);

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
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
  min-height: 400px;
}

.detail-content.mobile-layout {
  grid-template-columns: 1fr;
  padding: 20px;
}

/* 商品信息区域 */
.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卖家信息在详情页中的样式 */
.seller-info-in-detail {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

/* 操作按钮在详情页中的样式 */
.product-actions-in-detail {
  margin-top: 20px;
}

/* 卖家信息包装器 */
.seller-info-wrapper {
  margin: 20px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0;
  }
  
  .detail-content {
    margin: 15px;
    padding: 20px;
  }
  
  .back-btn {
    margin: 15px;
  }
  
  .seller-info-wrapper,
  .reviews-section,
  .recommended-section {
    margin: 15px;
  }
}

/* 图片预览弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  padding: 20px;
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 40px;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #4A90E2;
}

.preview-large-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>
