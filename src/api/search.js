import request from './request';

export const searchAPI = {
    // 统一搜索接口
    search(params) {
        return request.get('/search', {
            params: {
                q: params.query,
                type: params.type || 'all',
                tags: params.tags?.join(','),
                quickFilters: params.quickFilters?.join(',')
            }
        });
    },

    // 搜索地点
    searchLocations(keyword, campusId) {
        return request.get('/campuses/locations/search', {
            params: { keyword, campusId }
        });
    },

    // 搜索商品
    searchProducts(keyword) {
        return request.get('/products/search', {
            params: { keyword }
        });
    },

    // 搜索话题
    searchTopics(keyword) {
        return request.get('/topics/search', {
            params: { keyword }
        });
    },

    // 搜索用户
    searchUsers(keyword) {
        return request.get('/users/search', {
            params: { keyword }
        });
    }
};
