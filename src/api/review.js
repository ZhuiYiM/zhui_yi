import request from './request';

export const reviewAPI = {
    // 创建评价
    createReview(data) {
        return request.post('/reviews', data);
    },

    // 获取商品评价列表
    getProductReviews(productId, page = 1, size = 10) {
        return request.get(`/reviews/product/${productId}`, {
            params: { page, size }
        });
    },

    // 获取评价统计
    getReviewStats(productId) {
        return request.get(`/reviews/stats/product/${productId}`);
    },

    // 卖家回复评价
    replyReview(reviewId, data) {
        return request.put(`/reviews/${reviewId}/reply`, data);
    },

    // 获取卖家评价列表（商户评价）
    getSellerReviews(sellerId, page = 1, size = 10) {
        return request.get(`/reviews/seller/${sellerId}`, {
            params: { page, size }
        });
    },

    // 获取用户可评价的订单列表
    getReviewableOrders() {
        return request.get('/reviews/reviewable');
    }
};
