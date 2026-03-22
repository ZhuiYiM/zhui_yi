<template>
  <div :class="['orders-container', { 'mobile': isMobile }]">
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
      <div class="orders-wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">我的订单</h1>
          <div class="header-actions">
            <el-button @click="showCancelReasonStats" size="small">
              📊 取消原因统计
            </el-button>
            <el-button @click="refreshOrders" :loading="loading" size="small">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>

        <!-- 状态筛选 -->
        <div class="status-filter">
          <el-radio-group v-model="statusFilter" @change="filterOrders" size="default">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待付款</el-radio-button>
            <el-radio-button label="shipping">待发货</el-radio-button>
            <el-radio-button label="shipped">已发货</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 订单列表 -->
        <div v-else-if="filteredOrders.length > 0" class="orders-list">
          <div 
            v-for="order in filteredOrders" 
            :key="order.id" 
            class="order-item"
          >
            <!-- 订单头部 -->
            <div class="order-header">
              <span class="order-number">订单编号：{{ order.id }}</span>
              <span class="order-date">{{ formatTime(order.createdAt) }}</span>
              <el-tag :type="getStatusType(order.orderStatus)" size="small">
                {{ getStatusText(order.orderStatus) }}
              </el-tag>
            </div>

            <!-- 商品信息 -->
            <div class="order-product" @click="goToProduct(order.productId)">
              <img :src="order.productImage || 'https://placehold.co/100x100/e0e0e0/999999?text=商品'" alt="商品" class="product-image">
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
                <el-button 
                  v-if="order.orderStatus === 0" 
                  type="primary" 
                  size="small"
                  @click="goToPayment(order.id)"
                >
                  立即支付
                </el-button>
                <el-button 
                  v-if="order.orderStatus === 0" 
                  size="small"
                  @click="cancelOrder(order.id)"
                >
                  取消订单
                </el-button>
                <el-button 
                  v-if="order.orderStatus === 1 || order.orderStatus === 2" 
                  size="small"
                  @click="confirmReceipt(order.id)"
                >
                  确认收货
                </el-button>
                <el-button 
                  v-if="order.orderStatus === 3" 
                  type="primary" 
                  size="small"
                  @click="writeReview(order.id)"
                >
                  写评论
                </el-button>
                <el-button 
                  size="small"
                  @click="viewOrderDetail(order.id)"
                >
                  查看详情
                </el-button>
                <el-button 
                  v-if="order.orderStatus === 3 || order.orderStatus === 4" 
                  size="small"
                  @click="deleteOrder(order.id)"
                >
                  删除订单
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-icon class="empty-icon"><ShoppingCart /></el-icon>
          <h3>暂无订单</h3>
          <p>快去挑选心仪的商品吧</p>
          <el-button type="primary" @click="goToMall">去逛逛</el-button>
        </div>
      </div>
      
      <!-- 评价弹窗 -->
      <WriteReviewModal
        v-if="showReviewModal && currentReviewOrder"
        :visible="showReviewModal"
        :order-id="currentReviewOrder.id"
        :product="reviewProductInfo"
        @close="showReviewModal = false"
        @success="handleReviewSuccess"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, ShoppingCart } from '@element-plus/icons-vue';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { orderAPI } from '@/api/order';
import { reviewAPI } from '@/api/review';
import WriteReviewModal from '@/components/mall/WriteReviewModal.vue';

const router = useRouter();

// 状态
const loading = ref(true);
const isMobile = ref(window.innerWidth <= 768);
const currentUser = ref(null);
const orders = ref([]);
const statusFilter = ref('all');
const showReviewModal = ref(false);
const currentReviewOrder = ref(null);
const reviewProductInfo = ref(null);

// 筛选后的订单列表
const filteredOrders = computed(() => {
  if (statusFilter.value === 'all') {
    return orders.value;
  }
  
  const statusMap = {
    'pending': 0,      // 待付款
    'shipping': 1,     // 待发货
    'shipped': 2,      // 已发货
    'completed': 3,    // 已完成
    'cancelled': 4     // 已取消
  };
  
  return orders.value.filter(order => {
    return order.orderStatus === statusMap[statusFilter.value];
  });
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

// 加载订单列表
const loadOrders = async () => {
  try {
    loading.value = true;
    const response = await orderAPI.getMyOrders();
    
    console.log('订单列表响应:', response);
    
    if (response && response.records) {
      orders.value = response.records;
      console.log('订单数量:', orders.value.length);
    } else {
      orders.value = [];
    }
  } catch (error) {
    console.error('加载订单失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '加载订单失败');
    orders.value = [];
  } finally {
    loading.value = false;
  }
};

// 刷新订单
const refreshOrders = () => {
  loadOrders();
  ElMessage.success('刷新成功');
};

// 筛选订单
const filterOrders = () => {
  console.log('筛选状态:', statusFilter.value);
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

// 跳转到支付页面
const goToPayment = (orderId) => {
  router.push(`/order/payment/${orderId}`);
};

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/order/confirmation/${orderId}`);
};

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`);
};

// 取消订单
const cancelOrder = async (orderId) => {
  // 使用 Promise 包装弹窗
  return new Promise((resolve, reject) => {
    ElMessageBox({
      title: '取消订单',
      message: `
        <div class="cancel-reason-dialog">
          <p style="margin-bottom: 15px; color: #666;">请选择取消订单的原因：</p>
          <div class="reason-options">
            <label class="reason-option">
              <input type="radio" name="cancelReason" value="不想买了" />
              <span>不想买了</span>
            </label>
            <label class="reason-option">
              <input type="radio" name="cancelReason" value="信息填写错误" />
              <span>信息填写错误</span>
            </label>
            <label class="reason-option">
              <input type="radio" name="cancelReason" value="卖家同意退款" />
              <span>卖家同意退款</span>
            </label>
            <label class="reason-option">
              <input type="radio" name="cancelReason" value="其他原因" />
              <span>其他原因</span>
            </label>
          </div>
          <textarea 
            class="cancel-note-textarea" 
            placeholder="请输入补充说明（选填）" 
            rows="3"
            style="width: 100%; margin-top: 15px; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 14px;"
          ></textarea>
        </div>
      `,
      dangerouslyUseHTMLString: true,
      showCancelButton: true,
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      confirmButtonClass: 'el-button--danger',
      customClass: 'cancel-order-dialog',
      beforeClose: async (action, instance, done) => {
        if (action === 'confirm') {
          // 获取选中的原因
          const selectedRadio = document.querySelector('input[name="cancelReason"]:checked');
          const reason = selectedRadio ? selectedRadio.value : '';
          const note = document.querySelector('.cancel-note-textarea').value;
          
          if (!reason) {
            ElMessage.warning('请选择取消原因');
            return;
          }
          
          try {
            // 调用取消订单 API
            await orderAPI.cancelOrder(orderId, {
              reason: reason,
              note: note
            });
            
            ElMessage.success('订单取消成功');
            done();
            
            // 刷新订单列表
            loadOrders();
            resolve();
          } catch (error) {
            console.error('取消订单失败:', error);
            ElMessage.error(error.response?.data?.message || '取消订单失败');
            reject(error);
          }
        } else {
          done();
          reject('cancel');
        }
      }
    });
  });
};

// 确认收货
const confirmReceipt = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定已收到商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    await orderAPI.updateOrderStatus(orderId, 3); // 更新为已完成
    ElMessage.success('确认收货成功');
    loadOrders();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '确认收货失败');
    }
  }
};

// 删除订单
const deleteOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await orderAPI.deleteOrder(orderId);
    ElMessage.success('订单删除成功');
    loadOrders();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '删除订单失败');
    }
  }
};

// 写评论
const writeReview = async (orderId) => {
  try {
    // 查找订单信息
    const order = orders.value.find(o => o.id === orderId);
    if (!order) {
      ElMessage.error('订单不存在');
      return;
    }
    
    // 准备商品信息
    reviewProductInfo.value = {
      id: order.productId,
      title: order.productTitle,
      price: order.totalAmount,
      image: order.productImage || 'https://placehold.co/100x100/e0e0e0/999999?text=商品'
    };
    
    currentReviewOrder.value = order;
    showReviewModal.value = true;
  } catch (error) {
    console.error('打开评价弹窗失败:', error);
    ElMessage.error('打开评价窗口失败');
  }
};

// 评价成功回调
const handleReviewSuccess = () => {
  showReviewModal.value = false;
  currentReviewOrder.value = null;
  ElMessage.success('评价成功');
  loadOrders();
};

// 显示取消原因统计
const showCancelReasonStats = async () => {
  try {
    const response = await orderAPI.getCancelReasonStats();
    console.log('取消原因统计:', response);
    
    if (response && response.data && response.data.length > 0) {
      const stats = response.data;
      let statsHtml = '<div style="padding: 10px;">';
      stats.forEach(item => {
        statsHtml += `<div style="display: flex; justify-content: space-between; padding: 10px; border-bottom: 1px solid #eee;">`;
        statsHtml += `<span>${item.reason}</span>`;
        statsHtml += `<span style="font-weight: bold; color: #F56C6C;">${item.count}次</span>`;
        statsHtml += `</div>`;
      });
      statsHtml += '</div>';
      
      ElMessageBox({
        title: '取消原因统计',
        message: statsHtml,
        showConfirmButton: true,
        confirmButtonText: '关闭'
      });
    } else {
      ElMessageBox({
        title: '取消原因统计',
        message: '<div style="padding: 20px; text-align: center; color: #999;">暂无取消记录</div>',
        showConfirmButton: true,
        confirmButtonText: '关闭'
      });
    }
  } catch (error) {
    console.error('获取取消原因统计失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '获取统计失败');
  }
};

// 跳转到商城
const goToMall = () => {
  router.push('/mall');
};

// 设备检测
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  getUserInfo();
  loadOrders();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style>
/* 取消订单弹窗样式 - 全局样式 */
.cancel-order-dialog {
  width: 520px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.cancel-order-dialog .el-message-box__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.cancel-order-dialog .el-message-box__title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.cancel-order-dialog .el-message-box__content {
  padding: 24px;
}

.cancel-reason-dialog {
  padding: 5px 0;
}

.cancel-reason-dialog > p {
  margin: 0 0 16px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.reason-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reason-option {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: rgb(245, 247, 250);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  width: auto;
  min-width: 200px;
}

.reason-option:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.reason-option input[type="radio"] {
  margin-right: 8px;
  cursor: pointer;
}

.reason-option input[type="radio"]:checked + span {
  color: #409eff;
  font-weight: 500;
}

.reason-option span {
  color: #606266;
  font-size: 14px;
}

.cancel-note-textarea {
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
}

/* 确保输入框在弹窗中居中显示 */
.cancel-reason-dialog .cancel-note-textarea {
  max-width: 100%;
  margin: 15px 0 0 0;
}

.cancel-order-dialog .el-message-box__footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.cancel-order-dialog .el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.cancel-order-dialog .el-button--danger:hover {
  background-color: #f78989;
  border-color: #f78989;
}

.cancel-order-dialog .el-button {
  min-width: 100px;
  height: 36px;
  font-size: 14px;
}
</style>

<style scoped>
.orders-container {
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

.orders-wrapper {
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

.page-title {
  margin: 0;
  font-size: 1.8rem;
  color: #333;
}

/* 状态筛选 */
.status-filter {
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

/* 取消订单弹窗样式 - 使用:deep 穿透 scoped 样式 */
:deep(.cancel-order-dialog) {
  width: 520px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

:deep(.cancel-order-dialog .el-message-box__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.cancel-order-dialog .el-message-box__title) {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

:deep(.cancel-order-dialog .el-message-box__content) {
  padding: 24px;
}

:deep(.cancel-reason-dialog) {
  padding: 5px 0;
}

:deep(.cancel-reason-dialog > p) {
  margin: 0 0 16px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

:deep(.cancel-reason-dialog .el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start; /* 左对齐 */
}

:deep(.cancel-reason-dialog .el-radio) {
  margin: 0;
  width: auto;
  min-width: 200px;
  padding: 8px 12px;
  background: rgb(245, 247, 250);
  border-radius: 4px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

:deep(.cancel-reason-dialog .el-radio:hover) {
  background: #ecf5ff;
  border-color: #409eff;
}

:deep(.cancel-reason-dialog .el-radio.is-checked) {
  background: #ecf5ff;
  border-color: #409eff;
}

:deep(.cancel-reason-dialog .el-radio__input) {
  margin-right: 8px;
}

:deep(.cancel-reason-dialog .el-radio__label) {
  color: #606266;
  font-size: 14px;
}

:deep(.cancel-reason-dialog .el-input) {
  margin-top: 16px;
  width: 100%; /* 输入框占满宽度 */
}

:deep(.cancel-reason-dialog .el-textarea__inner) {
  border-radius: 6px;
  font-size: 14px;
}

:deep(.cancel-reason-dialog .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

:deep(.cancel-order-dialog .el-message-box__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center; /* 按钮居中 */
  gap: 12px;
}

:deep(.cancel-order-dialog .el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

:deep(.cancel-order-dialog .el-button--danger:hover) {
  background-color: #f78989;
  border-color: #f78989;
}

:deep(.cancel-order-dialog .el-button) {
  min-width: 80px; /* 按钮最小宽度 */
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
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

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
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
  margin-bottom: 8px;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.spec-tag {
  font-size: 0.8rem;
  color: #666;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.product-quantity {
  font-size: 0.85rem;
  color: #999;
}

.product-price {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 80px;
}

.price-label {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 5px;
}

.price-value {
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
  
  .orders-wrapper {
    padding: 15px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .order-product {
    flex-direction: column;
  }
  
  .product-image {
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
