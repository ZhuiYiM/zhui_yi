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
    }
};