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
    },

    // ========== 用户位置标记相关接口 ==========
    
    // 创建位置标记
    createLocationMark(data) {
        return request.post('/user/location-marks', data);
    },

    // 获取我的标记列表
    getMyLocationMarks(page = 1, size = 10) {
        return request.get('/user/location-marks/my', {
            params: { page, size }
        });
    },

    // 获取校区的公开标记
    getCampusLocationMarks(campusId, markType = null) {
        return request.get(`/user/location-marks/campus/${campusId}`, {
            params: { markType }
        });
    },

    // 获取标记详情
    getLocationMarkDetail(markId) {
        return request.get(`/user/location-marks/${markId}`);
    },

    // 更新标记
    updateLocationMark(markId, data) {
        return request.put(`/user/location-marks/${markId}`, data);
    },

    // 删除标记
    deleteLocationMark(markId) {
        return request.delete(`/user/location-marks/${markId}`);
    },

    // 审核标记（管理员）
    verifyLocationMark(markId, status, comment = '') {
        return request.put(`/user/location-marks/${markId}/verify`, null, {
            params: { status, comment }
        });
    },

    // 点赞标记
    likeLocationMark(markId) {
        return request.post(`/user/location-marks/${markId}/like`);
    }
};
