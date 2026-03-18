<template>
  <div :class="['products-container', { 'mobile': isMobile }]">
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
      <div class="products-wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="header-left">
            <el-button @click="goBack" icon="ArrowLeft" size="small">返回</el-button>
            <h1 class="page-title">我的商品</h1>
          </div>
          <div class="header-actions">
            <el-button @click="refreshProducts" :loading="loading" size="small">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>

        <!-- 商品标签页 -->
        <div class="product-tabs">
          <el-radio-group v-model="currentTab" @change="switchTab" size="default">
            <el-radio-button label="published">我发布的</el-radio-button>
            <el-radio-button label="orders">相关订单</el-radio-button>
            <el-radio-button label="favorites">我收藏的</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 我发布的商品 -->
        <div v-else-if="currentTab === 'published'" class="products-list">
          <div v-if="publishedProducts.length === 0" class="empty-state">
            <el-icon class="empty-icon"><ShoppingBag /></el-icon>
            <h3>暂无发布的商品</h3>
            <p>快去发布你的第一个商品吧</p>
            <el-button type="primary" @click="goToPublish">去发布</el-button>
          </div>
          <div v-else class="products-grid">
            <div 
              v-for="product in publishedProducts" 
              :key="product.id" 
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <img 
                :src="getProductImage(product)" 
                :alt="product.title" 
                class="product-image"
              >
              <div class="product-info">
                <h4 class="product-title">{{ product.title }}</h4>
                <p class="product-price">¥{{ product.price }}</p>
                <div class="product-meta">
                  <span class="product-status" :class="getStatusClass(product.status)">
                    {{ getStatusText(product.status) }}
                  </span>
                  <span class="product-stats">👁️ {{ product.viewCount || 0 }}</span>
                </div>
                <div class="product-actions">
                  <el-button size="small" @click.stop="editProduct(product.id)">编辑</el-button>
                  <el-button 
                    size="small" 
                    :type="product.status === 1 ? 'warning' : 'success'"
                    @click.stop="toggleProductStatus(product)"
                  >
                    {{ product.status === 1 ? '下架' : '上架' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 相关订单 -->
        <div v-else-if="currentTab === 'orders'" class="orders-list">
          <div v-if="relatedOrders.length === 0" class="empty-state">
            <el-icon class="empty-icon"><ShoppingCart /></el-icon>
            <h3>暂无相关订单</h3>
            <p>还没有与你的商品相关的订单</p>
          </div>
          <div v-else>
            <div 
              v-for="order in relatedOrders" 
              :key="order.id" 
              class="order-item"
            >
              <!-- 订单头部 -->
              <div class="order-header">
                <span class="order-number">订单编号：{{ order.id }}</span>
                <span class="order-date">{{ formatTime(order.createdAt) }}</span>
                <el-tag :type="getOrderStatusType(order.orderStatus)" size="small">
                  {{ getOrderStatusText(order.orderStatus) }}
                </el-tag>
              </div>

              <!-- 商品信息 -->
              <div class="order-product" @click="goToProduct(order.productId)">
                <img 
                  :src="order.productImage || 'https://placehold.co/100x100/e0e0e0/999999?text=商品'" 
                  alt="商品" 
                  class="product-image"
                >
                <div class="product-info">
                  <div class="product-title">{{ order.productTitle }}</div>
                  <div class="product-specs" v-if="order.specifications && order.specifications.length > 0">
                    <span v-for="(spec, index) in order.specifications" :key="index" class="spec-tag">
                      {{ spec.specName }}: {{ spec.specValue }}
                    </span>
                  </div>
                  <div class="product-quantity">x{{ order.quantity || 1 }}</div>
                </div>
                <div class="product-price">
                  <span class="price-label">实付</span>
                  <span class="price-value">¥{{ order.totalAmount }}</span>
                </div>
              </div>

              <!-- 订单底部操作 -->
              <div class="order-footer">
                <div class="order-total">
                  共 {{ order.quantity || 1 }} 件商品，合计：
                  <span class="total-amount">¥{{ order.totalAmount }}</span>
                </div>
                <div class="order-actions">
                  <el-button size="small" @click="viewOrderDetail(order.id)">查看详情</el-button>
                  <el-button 
                    v-if="order.orderStatus === 1" 
                    size="small" 
                    type="primary"
                    @click="shipOrder(order.id)"
                  >
                    发货
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 我收藏的商品 -->
        <div v-else-if="currentTab === 'favorites'" class="products-list">
          <div v-if="favoriteProducts.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Star /></el-icon>
            <h3>暂无收藏的商品</h3>
            <p>去商城逛逛，收藏喜欢的商品吧</p>
            <el-button type="primary" @click="goToMall">去商城</el-button>
          </div>
          <div v-else class="products-grid">
            <div 
              v-for="product in favoriteProducts" 
              :key="product.id" 
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <img 
                :src="getProductImage(product)" 
                :alt="product.title" 
                class="product-image"
              >
              <div class="product-info">
                <h4 class="product-title">{{ product.title }}</h4>
                <p class="product-price">¥{{ product.price }}</p>
                <div class="product-meta">
                  <span class="product-status" :class="getStatusClass(product.status)">
                    {{ getStatusText(product.status) }}
                  </span>
                  <span class="product-stats">👁️ {{ product.viewCount || 0 }}</span>
                </div>
                <div class="product-actions">
                  <el-button size="small" type="danger" @click.stop="removeFavorite(product.id)">
                    取消收藏
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, ShoppingBag, ShoppingCart, Star, ArrowLeft } from '@element-plus/icons-vue';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { productAPI } from '@/api/product';
import { orderAPI } from '@/api/order';

const router = useRouter();

// 状态
const loading = ref(true);
const isMobile = ref(window.innerWidth <= 768);
const currentUser = ref(null);
const currentTab = ref('published'); // 'published' | 'orders' | 'favorites'

// 商品和订单数据
const publishedProducts = ref([]);
const favoriteProducts = ref([]);
const relatedOrders = ref([]);

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

// 获取商品图片
const getProductImage = (product) => {
  if (product.images) {
    if (Array.isArray(product.images) && product.images.length > 0) {
      return product.images[0];
    } else if (typeof product.images === 'string') {
      try {
        const parsed = JSON.parse(product.images);
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed[0];
        }
      } catch (e) {
        return product.images;
      }
    }
  }
  return 'https://placehold.co/300x200/4A90E2/FFFFFF?text=商品图片';
};

// 切换标签页
const switchTab = () => {
  if (currentTab.value === 'published') {
    loadPublishedProducts();
  } else if (currentTab.value === 'orders') {
    loadRelatedOrders();
  } else if (currentTab.value === 'favorites') {
    loadFavoriteProducts();
  }
};

// 加载我发布的商品
const loadPublishedProducts = async () => {
  try {
    loading.value = true;
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = userData.id;
    
    if (!userId) {
      ElMessage.warning('请先登录');
      return;
    }
    
    const response = await productAPI.getProducts({ 
      sellerId: userId, 
      status: null,
      size: 50 
    });
    
    const data = response.data || response;
    publishedProducts.value = data.records || data.data?.records || [];
    
    // 处理 images 字段
    publishedProducts.value.forEach(p => {
      if (typeof p.images === 'string') {
        try {
          p.images = JSON.parse(p.images);
        } catch (e) {
          console.error('商品 images 解析失败:', e);
          p.images = [];
        }
      }
    });
  } catch (error) {
    console.error('加载发布的商品失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '加载失败');
    publishedProducts.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载相关订单（卖家订单）
const loadRelatedOrders = async () => {
  try {
    loading.value = true;
    const response = await orderAPI.getSellerOrders({ size: 50 });
    
    const data = response.data || response;
    relatedOrders.value = data.records || data.data?.records || [];
  } catch (error) {
    console.error('加载相关订单失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '加载失败');
    relatedOrders.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载收藏的商品
const loadFavoriteProducts = async () => {
  try {
    loading.value = true;
    const response = await productAPI.getMyFavorites({ size: 50 });
    
    const data = response.data || response;
    favoriteProducts.value = data || data.data || [];
    
    // 处理 images 字段
    favoriteProducts.value.forEach(p => {
      if (typeof p.images === 'string') {
        try {
          p.images = JSON.parse(p.images);
        } catch (e) {
          console.error('商品 images 解析失败:', e);
          p.images = [];
        }
      }
    });
  } catch (error) {
    console.error('加载收藏的商品失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '加载失败');
    favoriteProducts.value = [];
  } finally {
    loading.value = false;
  }
};

// 刷新商品
const refreshProducts = () => {
  switchTab();
  ElMessage.success('刷新成功');
};

// 获取商品状态样式
const getStatusClass = (status) => {
  const classes = {
    0: 'offline',      // 已下架
    1: 'onsale',       // 在售
    2: 'sold'          // 已售出
  };
  return classes[status] || 'offline';
};

// 获取商品状态文本
const getStatusText = (status) => {
  const texts = {
    0: '已下架',
    1: '在售',
    2: '已售出'
  };
  return texts[status] || '未知状态';
};

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'warning',      // 待付款
    1: 'success',      // 待发货
    2: 'primary',      // 已发货
    3: 'success',      // 已完成
    4: 'info'          // 已取消
  };
  return types[status] || 'info';
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  };
  return texts[status] || '未知状态';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 编辑商品
const editProduct = (productId) => {
  router.push(`/mall?edit=${productId}`);
};

// 切换商品状态
const toggleProductStatus = async (product) => {
  try {
    const newStatus = product.status === 1 ? 0 : 1;
    await productAPI.updateProductStatus(product.id, newStatus);
    
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功');
    product.status = newStatus;
  } catch (error) {
    console.error('切换商品状态失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '操作失败');
  }
};

// 取消收藏
const removeFavorite = async (productId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await productAPI.toggleFavorite(productId);
    ElMessage.success('取消收藏成功');
    loadFavoriteProducts();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '操作失败');
    }
  }
};

// 发货
const shipOrder = async (orderId) => {
  try {
    await ElMessageBox.prompt('请输入快递单号（选填）', '发货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[\w\d\s-]*$/,
      inputErrorMessage: '请输入有效的快递单号'
    }).then(async ({ value }) => {
      await orderAPI.shipOrder(orderId, {
        expressNo: value || ''
      });
      
      ElMessage.success('发货成功');
      loadRelatedOrders();
    });
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '操作失败');
    }
  }
};

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/order/confirmation/${orderId}`);
};

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`);
};

// 跳转到发布页面
const goToPublish = () => {
  router.push('/mall?publish=true');
};

// 跳转到商城
const goToMall = () => {
  router.push('/mall');
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 设备检测
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  getUserInfo();
  loadPublishedProducts();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.products-container {
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

.products-wrapper {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 1.8rem;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 商品标签页 */
.product-tabs {
  margin-bottom: 20px;
  padding: 15px 0;
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
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

/* 商品列表 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
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
  height: 200px;
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
  margin-bottom: 12px;
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

.product-status.offline {
  background: #f5f7fa;
  color: #999;
}

.product-stats {
  color: #999;
}

.product-actions {
  display: flex;
  gap: 8px;
}

/* 订单列表 */
.order-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 15px;
  transition: all 0.3s;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e0e0e0;
  font-size: 0.9rem;
}

.order-number {
  color: #666;
  font-weight: 500;
}

.order-date {
  color: #999;
}

/* 商品信息 */
.order-product {
  display: flex;
  gap: 15px;
  padding: 20px;
  cursor: pointer;
  transition: background 0.3s;
}

.order-product:hover {
  background: #fafafa;
}

.order-product .product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.order-product .product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0;
}

.order-product .product-title {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}

.order-product .product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.order-product .spec-tag {
  font-size: 0.8rem;
  color: #666;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.order-product .product-quantity {
  font-size: 0.8rem;
  color: #999;
}

.order-product .product-price {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 80px;
}

.order-product .price-label {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 5px;
}

.order-product .price-value {
  font-size: 1.3rem;
  color: #F56C6C;
  font-weight: bold;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  background: #fafafa;
}

.order-total {
  font-size: 0.9rem;
  color: #666;
}

.total-amount {
  font-size: 1.2rem;
  color: #F56C6C;
  font-weight: bold;
  margin-left: 5px;
}

.order-actions {
  display: flex;
  gap: 10px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  color: #999;
}

.empty-icon {
  font-size: 4rem;
  color: #C0C4CC;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px;
  font-size: 1.2rem;
  color: #666;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0;
  }
  
  .products-wrapper {
    padding: 15px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .order-product {
    flex-direction: column;
  }
  
  .order-product .product-image {
    width: 100%;
    height: 200px;
  }
  
  .order-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-end;
  }
  
  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
