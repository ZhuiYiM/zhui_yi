import request from './request';

export const productAPI = {
    // 获取商品列表
    getProducts(params = {}) {
        return request.get('/products', { params });
    },

    // 发布新商品
    createProduct(data) {
        return request.post('/products', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取商品详情
    getProductDetail(id) {
        return request.get(`/products/${id}`);
    },

    // 更新商品
    updateProduct(id, data) {
        return request.put(`/products/${id}`, data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 删除商品
    deleteProduct(id) {
        return request.delete(`/products/${id}`);
    },

    // 收藏/取消收藏商品
    toggleFavorite(id) {
        return request.post(`/products/${id}/favorite`);
    },

    // 获取我的收藏
    getMyFavorites(params = {}) {
        return request.get('/products/favorites', { params });
    },

    // 获取商品分类
    getCategories() {
        return request.get('/products/categories');
    },

    // 下架/上架商品
    updateProductStatus(id, status) {
        return request.put(`/products/${id}/status`, null, {
            params: { status }
        });
    },

    // 获取我发布的商品
    getMyPublishedProducts(params = {}) {
        return request.get('/products/my', { params });
    }
};