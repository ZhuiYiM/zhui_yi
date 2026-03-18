import request from './request';

export const campusAPI = {
    // 获取校区列表
    getCampuses() {
        return request.get('/campuses');
    },

    // 获取校区地点信息
    getCampusLocations(campusId) {
        return request.get('/campuses/locations', {
            params: { campusId }
        });
    },

    // 搜索地点
    searchLocations(keyword, campusId) {
        return request.get('/campuses/locations/search', {
            params: { keyword, campusId }
        });
    },

    // 获取热门地点
    getPopularLocations(campusId) {
        return request.get('/campuses/popular-locations', {
            params: { campusId }
        });
    },

    // 获取地图配置
    getMapConfig(campusId, mapType) {
        return request.get('/campuses/map-config', {
            params: { campusId, mapType }
        });
    },

    // 获取地点详情
    getLocationDetail(locationId) {
        return request.get(`/campuses/locations/${locationId}`);
    }
};
