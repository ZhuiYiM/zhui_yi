import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { orderAPI } from '@/api/order';

/**
 * 用户订单管理 composable
 * @returns {Object} 订单状态和方法
 */
export function useUserOrders() {
  const recentOrders = ref([]);
  const ordersLoading = ref(false);

  /**
   * 加载最近订单（从 API 获取）- 仅包含买家订单
   */
  const loadRecentOrders = async () => {
    try {
      ordersLoading.value = true;
      
      // 从 API 获取订单列表（买家视角）
      const response = await orderAPI.getMyOrders({ page: 1, size: 6 });
      
      if (response && response.records && response.records.length > 0) {
        // 转换数据格式以适配 OrderSection 组件
        recentOrders.value = response.records.map(order => ({
          id: order.id,
          title: order.productTitle || '商品',
          price: order.totalAmount || 0,
          date: order.createdAt,
          status: getOrderStatus(order.orderStatus),
          statusText: getOrderStatusText(order.orderStatus),
          image: order.productImage || 'https://placehold.co/100x100/e0e0e0/999999?text=商品'
        }));
      } else {
        recentOrders.value = [];
      }
    } catch (error) {
      console.error('❌ 加载订单失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '加载订单失败');
      recentOrders.value = [];
    } finally {
      ordersLoading.value = false;
    }
  };
  
  /**
   * 获取订单状态映射
   */
  const getOrderStatus = (status) => {
    const statusMap = {
      0: 'pending',      // 待付款
      1: 'shipping',     // 待发货
      2: 'shipped',      // 已发货
      3: 'completed',    // 已完成
      4: 'cancelled'     // 已取消
    };
    return statusMap[status] || 'completed';
  };
  
  /**
   * 获取订单状态文本
   */
  const getOrderStatusText = (status) => {
    const statusTextMap = {
      0: '待付款',
      1: '待发货',
      2: '已发货',
      3: '已完成',
      4: '已取消'
    };
    return statusTextMap[status] || '未知状态';
  };

  /**
   * 查看订单详情
   * @param {string} orderId - 订单 ID
   */
  const viewOrderDetail = (orderId) => {
    // 实际项目中跳转到订单详情页
    return orderId;
  };

  /**
   * 查看全部订单
   */
  const viewAllOrders = () => {
    // 实际项目中跳转到订单列表页
  };

  return {
    recentOrders,
    ordersLoading,
    loadRecentOrders,
    viewOrderDetail,
    viewAllOrders
  };
}
