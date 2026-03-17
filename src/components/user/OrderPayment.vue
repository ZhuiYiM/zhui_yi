<template>
  <div :class="['payment-container', { 'mobile': isMobile }]">
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
      <div class="payment-wrapper">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 支付页面 -->
        <div v-else-if="orderData" class="payment-card">
          <h1 class="page-title">订单支付</h1>

          <!-- 订单信息概览 -->
          <div class="order-summary">
            <h3>订单信息</h3>
            <div class="summary-row">
              <span class="label">订单编号：</span>
              <span class="value">{{ orderData.order.id }}</span>
            </div>
            <div class="summary-row">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(orderData.order.orderStatus)">
                {{ getStatusText(orderData.order.orderStatus) }}
              </el-tag>
            </div>
            <div class="summary-row">
              <span class="label">支付金额：</span>
              <span class="amount">¥{{ orderData.order.totalAmount || '0.00' }}</span>
            </div>
          </div>

          <!-- 支付方式选择 -->
          <div class="payment-methods">
            <h3>选择支付方式</h3>
            
            <!-- 站内支付 -->
            <div 
              :class="['payment-option', { selected: selectedMethod === 'station' }]"
              @click="selectedMethod = 'station'"
            >
              <div class="option-icon">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="option-info">
                <div class="option-title">站内支付</div>
                <div class="option-desc">使用账户余额支付（开发中）</div>
              </div>
              <el-radio v-model="selectedMethod" label="station"></el-radio>
            </div>

            <!-- 微信支付 -->
            <div 
              :class="['payment-option', { selected: selectedMethod === 'wechat' }]"
              @click="selectedMethod = 'wechat'"
            >
              <div class="option-icon wechat">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="option-info">
                <div class="option-title">微信支付</div>
                <div class="option-desc">扫码或使用微信 APP 支付（待开发）</div>
              </div>
              <el-radio v-model="selectedMethod" label="wechat"></el-radio>
            </div>

            <!-- 支付宝支付 -->
            <div 
              :class="['payment-option', { selected: selectedMethod === 'alipay' }]"
              @click="selectedMethod = 'alipay'"
            >
              <div class="option-icon alipay">
                <el-icon><Shop /></el-icon>
              </div>
              <div class="option-info">
                <div class="option-title">支付宝支付</div>
                <div class="option-desc">扫码或使用支付宝 APP 支付（待开发）</div>
              </div>
              <el-radio v-model="selectedMethod" label="alipay"></el-radio>
            </div>
          </div>

          <!-- 支付提示 -->
          <div class="payment-tips">
            <el-alert
              title="支付说明"
              type="info"
              :closable="false"
            >
              <template #default>
                <ul>
                  <li>请选择安全的支付方式</li>
                  <li>建议当面交易，确认商品质量后再付款</li>
                  <li>支付前请仔细核对订单信息</li>
                  <li>如有疑问，请联系卖家或平台客服</li>
                </ul>
              </template>
            </el-alert>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button @click="goBack" size="large">
              返回
            </el-button>
            <el-button 
              type="primary" 
              size="large"
              :loading="paying"
              @click="confirmPayment"
            >
              {{ paying ? '支付处理中...' : '确认支付' }}
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-icon class="empty-icon"><WarningFilled /></el-icon>
          <h3>订单不存在</h3>
          <p>该订单可能不存在或已被删除</p>
          <el-button type="primary" @click="goToOrderList">查看我的订单</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Wallet, ChatDotRound, Shop, WarningFilled } from '@element-plus/icons-vue';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { orderAPI } from '@/api/order';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const isMobile = ref(window.innerWidth <= 768);
const currentUser = ref(null);
const orderData = ref(null);
const selectedMethod = ref('station'); // 默认选中站内支付
const paying = ref(false);

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
    
    console.log('支付页订单详情响应:', response);
    
    if (response) {
      // 响应拦截器已经返回了实际数据，不需要再访问 .data
      orderData.value = response;
      
      console.log('订单数据:', orderData.value);
      console.log('订单状态:', orderData.value.order?.orderStatus);
      
      // 如果订单已支付，提示用户
      if (orderData.value.order.paymentStatus === 1) {
        ElMessageBox.alert(
          '该订单已完成支付，无需重复支付',
          '支付提示',
          {
            confirmButtonText: '查看订单',
            callback: () => {
              router.push('/personal/orders');
            }
          }
        );
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '获取订单详情失败');
  } finally {
    loading.value = false;
  }
};

// 确认支付
const confirmPayment = async () => {
  if (!orderData.value) {
    ElMessage.warning('订单信息不存在');
    return;
  }
  
  // 检查订单状态
  if (orderData.value.order.orderStatus !== 0) {
    ElMessage.warning('该订单状态无法支付');
    return;
  }
  
  try {
    paying.value = true;
    
    // 根据选择的支付方式进行处理
    switch (selectedMethod.value) {
      case 'station':
        await processStationPayment();
        break;
      case 'wechat':
        await processWechatPayment();
        break;
      case 'alipay':
        await processAlipayPayment();
        break;
      default:
        ElMessage.warning('请选择支付方式');
    }
  } catch (error) {
    console.error('支付失败:', error);
    ElMessage.error(error.message || '支付失败，请稍后重试');
  } finally {
    paying.value = false;
  }
};

// 处理站内支付
const processStationPayment = async () => {
  // 调用支付接口
  await orderAPI.payOrder(orderData.value.order.id, {
    paymentMethod: 'station'
  });
  
  ElMessage.success('支付成功！');
  setTimeout(() => {
    router.push(`/order/confirmation/${orderData.value.order.id}`);
  }, 1500);
};

// 处理微信支付
const processWechatPayment = async () => {
  // 调用支付接口
  await orderAPI.payOrder(orderData.value.order.id, {
    paymentMethod: 'wechat'
  });
  
  ElMessage.success('支付成功！');
  setTimeout(() => {
    router.push(`/order/confirmation/${orderData.value.order.id}`);
  }, 1500);
};

// 处理支付宝支付
const processAlipayPayment = async () => {
  // 调用支付接口
  await orderAPI.payOrder(orderData.value.order.id, {
    paymentMethod: 'alipay'
  });
  
  ElMessage.success('支付成功！');
  setTimeout(() => {
    router.push(`/order/confirmation/${orderData.value.order.id}`);
  }, 1500);
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

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/personal/orders');
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
.payment-container {
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

.payment-wrapper {
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

/* 支付卡片 */
.payment-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.page-title {
  margin: 0 0 30px;
  font-size: 1.8rem;
  color: #333;
  text-align: center;
}

/* 订单概览 */
.order-summary {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  border-left: 4px solid #ffc107;
}

.order-summary h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #856404;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 0.95rem;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-row .label {
  color: #856404;
}

.summary-row .value {
  color: #856404;
  font-weight: 600;
}

.summary-row .amount {
  color: #F56C6C;
  font-size: 1.5rem;
  font-weight: bold;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 支付方式选择 */
.payment-methods {
  margin-bottom: 25px;
}

.payment-methods h3 {
  margin: 0 0 15px;
  font-size: 1.1rem;
  color: #333;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #409EFF;
  background: #f5f7fa;
}

.payment-option.selected {
  border-color: #409EFF;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
}

.option-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 1.8rem;
  color: #409EFF;
}

.option-icon.wechat {
  background: #07c160;
  color: white;
}

.option-icon.alipay {
  background: #1677ff;
  color: white;
}

.option-info {
  flex: 1;
}

.option-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.option-desc {
  font-size: 0.85rem;
  color: #999;
}

/* 支付提示 */
.payment-tips {
  margin-bottom: 25px;
}

.payment-tips ul {
  margin: 10px 0 0;
  padding-left: 20px;
}

.payment-tips li {
  margin: 5px 0;
  font-size: 0.9rem;
  line-height: 1.6;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
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
  
  .payment-wrapper {
    padding: 15px;
  }
  
  .payment-card {
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
