import request from './request';

export const orderAPI = {
    // 创建订单
    createOrder(data) {
        return request.post('/orders', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取我的订单列表
    getMyOrders(params = {}) {
        return request.get('/orders/my', { params });
    },

    // 获取订单详情
    getOrderDetail(id) {
        return request.get(`/orders/${id}`);
    },

    // 更新订单状态
    updateOrderStatus(id, status) {
        return request.put(`/orders/${id}/status`, null, {
            params: { status }
        });
    },

    // 取消订单
    cancelOrder(id, data = {}) {
        return request.put(`/orders/${id}/cancel`, data, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },

    // 获取取消原因统计
    getCancelReasonStats() {
        return request.get('/orders/cancel-stats');
    },

    // 支付订单
    payOrder(id, data) {
        return request.put(`/orders/${id}/pay`, data);
    },

    // 获取卖家订单列表
    getSellerOrders(params = {}) {
        return request.get('/orders/seller/my', { params });
    },

    // 卖家发货
    shipOrder(id, data) {
        return request.put(`/orders/${id}/ship`, data);
    },

    // 删除订单
    deleteOrder(id) {
        return request.delete(`/orders/${id}`);
    }
};
