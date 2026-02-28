import request from './request';

export const campusAPI = {
    // 获取校区列表
    getCampuses() {
        return request.get('/campuses');
    },

    // 获取校区地点信息
    getCampusLocations(campusId) {
        return request.get(`/campuses/${campusId}/locations`);
    },

    // 搜索地点
    searchLocations(keyword, campusId) {
        return request.get('/campuses/locations/search', {
            params: { keyword, campusId }
        });
    }
};
