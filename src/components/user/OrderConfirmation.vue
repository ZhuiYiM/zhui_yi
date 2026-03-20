<template>
  <div :class="['order-confirmation-container', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="mall"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 写评价弹窗 -->
    <WriteReviewModal
      v-if="showReviewModal"
      :visible="showReviewModal"
      :order-id="orderData?.order?.id"
      :product="reviewProductInfo"
      :order-info="orderData?.order"
      @close="closeReviewModal"
      @success="handleReviewSuccess"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <div class="confirmation-wrapper">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 订单确认信息 -->
        <div v-else-if="orderData" class="confirmation-card">
          <div class="success-header">
            <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
            <h1 class="success-title">订单创建成功！</h1>
          </div>

          <!-- 订单号 -->
          <div class="order-number">
            <span class="order-label">订单编号：</span>
            <span class="order-id">{{ orderData.order.id }}</span>
            <el-button @click="copyOrderNumber" size="small" type="primary" link>
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>

          <!-- 商品信息 -->
          <div class="product-section">
            <h3>商品信息</h3>
            <div class="product-item" @click="goToProductDetail(orderData.order.productId)">
              <img :src="productImage" alt="商品图片" class="product-image">
              <div class="product-info">
                <div class="product-title">{{ productData?.title || orderData.product?.title }}</div>
                <div class="product-price">¥{{ orderData.order.totalAmount || '0.00' }}</div>
                <div class="product-quantity">x{{ quantity }}</div>
              </div>
            </div>
            
            <!-- 选中的规格 -->
            <div v-if="selectedSpecs && selectedSpecs.length > 0" class="specs-section">
              <h4>已选规格</h4>
              <div class="specs-list">
                <el-tag 
                  v-for="spec in selectedSpecs" 
                  :key="spec.specId || spec.specValue"
                  size="small"
                  style="margin-right: 8px; margin-bottom: 8px;"
                >
                  {{ spec.specName }}: {{ spec.specValue }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 订单详情 -->
          <div class="order-details">
            <h3>订单详情</h3>
            <div class="detail-row">
              <span class="label">订单编号：</span>
              <span class="value">{{ orderData.order.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(orderData.order.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(orderData.order.orderStatus)">
                {{ getStatusText(orderData.order.orderStatus) }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPaymentMethodText(orderData.order.paymentMethod) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">买家联系方式：</span>
              <span class="value">{{ orderData.order.buyerContact || '-' }}</span>
            </div>
            <div v-if="orderData.order.buyerMessage" class="detail-row">
              <span class="label">买家留言：</span>
              <span class="value">{{ orderData.order.buyerMessage }}</span>
            </div>
          </div>

          <!-- 金额明细 -->
          <div class="amount-section">
            <h3>金额明细</h3>
            <div class="amount-row">
              <span class="label">商品总价：</span>
              <span class="value">¥{{ orderData.order.totalAmount || '0.00' }}</span>
            </div>
            <div class="amount-row total">
              <span class="label">实付金额：</span>
              <span class="total-amount">¥{{ orderData.order.totalAmount || '0.00' }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button @click="goToOrderList" size="large">
              查看我的订单
            </el-button>
            <el-button 
              v-if="orderData.order && orderData.order.orderStatus === 0" 
              type="danger" 
              size="large"
              @click="cancelOrder"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="orderData.order && orderData.order.orderStatus === 0" 
              type="primary" 
              size="large"
              @click="goToPayment"
            >
              立即支付
            </el-button>
            <!-- 评价按钮：只有已完成的订单可以评价 -->
            <el-button 
              v-if="orderData.order && orderData.order.orderStatus === 3" 
              type="success" 
              size="large"
              @click="openReviewModal"
            >
              ✍️ 写评价
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-icon class="empty-icon"><WarningFilled /></el-icon>
          <h3>订单不存在</h3>
          <p>该订单可能不存在或已被删除</p>
          <el-button type="primary" @click="goBack">返回上一页</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheckFilled, CopyDocument, WarningFilled } from '@element-plus/icons-vue';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import WriteReviewModal from '@/components/mall/WriteReviewModal.vue';
import { orderAPI } from '@/api/order';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const isMobile = ref(window.innerWidth <= 768);
const currentUser = ref(null);
const orderData = ref(null);
const productData = ref(null);
const selectedSpecs = ref([]);
const quantity = ref(1);
const productImage = ref('');
const showReviewModal = ref(false);
const reviewProductInfo = ref({});

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

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    loading.value = true;
    const orderId = route.params.id;
    
    if (!orderId) {
      throw new Error('订单 ID 参数缺失');
    }
    
    const response = await orderAPI.getOrderDetail(orderId);
    
    console.log('订单详情响应:', response);
    
    if (response) {
      // 响应拦截器已经返回了实际数据，不需要再访问 .data
      orderData.value = response;
      
      console.log('订单数据:', orderData.value);
      console.log('订单状态:', orderData.value.order?.orderStatus);
      
      // 从 URL 参数中获取额外信息
      if (route.query.productTitle) {
        productData.value = { title: route.query.productTitle };
      }
      if (route.query.quantity) {
        quantity.value = parseInt(route.query.quantity);
      }
      if (route.query.image) {
        productImage.value = route.query.image;
      } else {
        productImage.value = 'https://placehold.co/300x300/e0e0e0/999999?text=商品图片';
      }
      
      // 解析规格数据
      if (orderData.value.specifications) {
        selectedSpecs.value = orderData.value.specifications;
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '获取订单详情失败');
  } finally {
    loading.value = false;
  }
};

// 复制订单号
const copyOrderNumber = () => {
  const orderNumber = orderData.value.order.id.toString();
  navigator.clipboard.writeText(orderNumber).then(() => {
    ElMessage.success('订单号已复制');
  }).catch(() => {
    ElMessage.error('复制失败');
  });
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

// 获取订单状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'success',
    4: 'info'
  };
  return types[status] || 'info';
};

// 获取订单状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  };
  return texts[status] || '未知状态';
};

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const texts = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'station': '站内支付'
  };
  return texts[method] || '未选择';
};

// 跳转到支付页面
const goToPayment = () => {
  router.push(`/order/payment/${orderData.value.order.id}`);
};

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/personal/orders');
};

// 取消订单
const cancelOrder = async () => {
  ElMessageBox.confirm(
    '确定要取消该订单吗？取消后无法恢复。',
    '取消订单',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    try {
      // 调用取消订单 API
      await orderAPI.cancelOrder(orderData.value.order.id, {
        reason: '其他原因',
        note: ''
      });
      
      ElMessage.success('订单取消成功');
      
      // 刷新订单详情
      fetchOrderDetail();
    } catch (error) {
      console.error('取消订单失败:', error);
      ElMessage.error(error.response?.data?.message || '取消订单失败');
    }
  }).catch(() => {
    // 用户取消
  });
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  if (!productId) {
    ElMessage.warning('商品信息不完整');
    return;
  }
  router.push(`/product/${productId}`);
};

// 打开评价弹窗
const openReviewModal = () => {
  if (!orderData.value?.order) return;
  
  // 准备商品信息
  reviewProductInfo.value = {
    id: orderData.value.order.productId,
    title: productData.value?.title || '商品',
    price: orderData.value.order.totalAmount,
    image: productImage.value
  };
  
  showReviewModal.value = true;
};

// 关闭评价弹窗
const closeReviewModal = () => {
  showReviewModal.value = false;
};

// 评价成功
const handleReviewSuccess = () => {
  // 刷新订单详情
  fetchOrderDetail();
};

// 设备检测
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  getUserInfo();
  fetchOrderDetail();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.order-confirmation-container {
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

.confirmation-wrapper {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
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

/* 确认卡片 */
.confirmation-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.success-header {
  text-align: center;
  margin-bottom: 30px;
}

.success-icon {
  font-size: 4rem;
  color: #67C23A;
  margin-bottom: 10px;
}

.success-title {
  margin: 0;
  font-size: 1.8rem;
  color: #333;
}

.order-number {
  background: #f5f7fa;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #333;
}

.order-label {
  color: #333;
  font-weight: 500;
}

.order-id {
  color: #333;
  font-size: 1.1rem;
  font-weight: 600;
}

/* 商品区域 */
.product-section {
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 1px solid #e0e0e0;
}

.product-section h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #333;
}

.product-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:hover {
  background: #f0f0f0;
  transform: translateX(5px);
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}

.product-price {
  font-size: 1.3rem;
  color: #F56C6C;
  font-weight: bold;
}

.product-quantity {
  font-size: 0.9rem;
  color: #999;
}

/* 规格区域 */
.specs-section {
  margin-top: 15px;
}

.specs-section h4 {
  margin: 0 0 10px;
  font-size: 0.95rem;
  color: #666;
}

.specs-list {
  display: flex;
  flex-wrap: wrap;
}

/* 订单详情 */
.order-details {
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 1px solid #e0e0e0;
}

.order-details h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #333;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 0.95rem;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-row .label {
  color: #333;
}

.detail-row .value {
  color: #333;
}

/* 金额区域 */
.amount-section {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
}

.amount-section h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #856404;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 0.95rem;
}

.amount-row:last-child {
  margin-bottom: 0;
}

.amount-row .label {
  color: #856404;
}

.amount-row .value {
  color: #856404;
  font-weight: 600;
}

.amount-row.total {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ffc107;
  font-size: 1.1rem;
}

.total-amount {
  color: #F56C6C !important;
  font-size: 1.5rem;
  font-weight: bold;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
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
  color: #F56C6C;
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
  
  .confirmation-wrapper {
    padding: 15px;
  }
  
  .confirmation-card {
    padding: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
