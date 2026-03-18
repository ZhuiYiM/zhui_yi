/**
 * 地图工具类
 * 提供地图坐标转换、地图服务商配置等功能
 */

// 地图服务商配置
export const MAP_PROVIDERS = {
    BAIDU: {
        id: 'baidu',
        name: '百度地图',
        icon: '🗺️',
        scriptUrl: 'https://api.map.baidu.com/api?v=3.0&ak=YOUR_BAIDU_AK', // TODO: 替换为真实的百度地图 AK
        defaultZoom: 15,
        minZoom: 3,
        maxZoom: 19
    },
    GAODE: {
        id: 'gaode',
        name: '高德地图',
        icon: '🗺️',
        scriptUrl: 'https://webapi.amap.com/maps?v=2.0&key=YOUR_GAODE_KEY', // TODO: 替换为真实的高德地图 Key
        defaultZoom: 15,
        minZoom: 3,
        maxZoom: 18
    },
    TENCENT: {
        id: 'tencent',
        name: '腾讯地图',
        icon: '🗺️',
        scriptUrl: 'https://map.qq.com/api/gljs?v=1.exp&key=YOUR_TENCENT_KEY', // TODO: 替换为真实的腾讯地图 Key
        defaultZoom: 15,
        minZoom: 4,
        maxZoom: 18
    }
};

// 地点分类配置
export const LOCATION_CATEGORIES = {
    teaching: { name: '教学楼', icon: '🏫' },
    library: { name: '图书馆', icon: '📚' },
    cafeteria: { name: '食堂', icon: '🍽️' },
    dormitory: { name: '宿舍', icon: '🏠' },
    sports: { name: '体育设施', icon: '⚽' },
    admin: { name: '行政楼', icon: '🏢' },
    other: { name: '其他', icon: '📍' }
};

/**
 * 加载地图脚本
 * @param {string} provider - 地图服务商 ID
 * @returns {Promise}
 */
export function loadMapScript(provider) {
    return new Promise((resolve, reject) => {
        const config = MAP_PROVIDERS[provider.toUpperCase()];
        if (!config) {
            reject(new Error(`不支持的地图服务商：${provider}`));
            return;
        }

        // 检查是否已经加载过
        if (document.querySelector(`script[src*="${config.scriptUrl.split('?')[0]}"]`)) {
            resolve();
            return;
        }

        const script = document.createElement('script');
        script.src = config.scriptUrl;
        script.async = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error(`加载${config.name}脚本失败`));
        document.head.appendChild(script);
    });
}

/**
 * 坐标转换工具
 * 注意：不同地图服务商使用不同的坐标系，实际项目中需要进行坐标转换
 * - 百度地图：BD-09 坐标系
 * - 高德/腾讯地图：GCJ-02 坐标系（火星坐标系）
 * - GPS 原始坐标：WGS-84 坐标系
 */
export const CoordinateConverter = {
    /**
     * WGS-84 转 GCJ-02
     */
    wgs84ToGcj02(lat, lng) {
        const PI = Math.PI;
        const a = 6378245.0;
        const ee = 0.00669342162296594323;
        
        if (this.outOfChina(lat, lng)) {
            return { lat, lng };
        }
        
        let dLat = this.transformLat(lng - 105.0, lat - 35.0);
        let dLng = this.transformLng(lng - 105.0, lat - 35.0);
        let radLat = lat / 180.0 * PI;
        let magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        let sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        
        return {
            lat: lat + dLat,
            lng: lng + dLng
        };
    },
    
    /**
     * GCJ-02 转 BD-09
     */
    gcj02ToBd09(lat, lng) {
        const xPi = Math.PI * 3000.0 / 180.0;
        const z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * xPi);
        const theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * xPi);
        
        return {
            lat: z * Math.sin(theta) + 0.006,
            lng: z * Math.cos(theta) + 0.0065
        };
    },
    
    /**
     * BD-09 转 GCJ-02
     */
    bd09ToGcj02(lat, lng) {
        const xPi = Math.PI * 3000.0 / 180.0;
        const x = lng - 0.0065;
        const y = lat - 0.006;
        const z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * xPi);
        const theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * xPi);
        
        return {
            lat: z * Math.sin(theta),
            lng: z * Math.cos(theta)
        };
    },
    
    /**
     * 判断是否在中国境内
     */
    outOfChina(lat, lng) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    },
    
    /**
     * 转换纬度
     */
    transformLat(lng, lat) {
        let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    },
    
    /**
     * 转换经度
     */
    transformLng(lng, lat) {
        let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
};

/**
 * 获取地点分类信息
 * @param {string} categoryCode - 分类代码
 * @returns {Object} 分类信息
 */
export function getLocationCategory(categoryCode) {
    return LOCATION_CATEGORIES[categoryCode] || LOCATION_CATEGORIES.other;
}

/**
 * 格式化地点数据
 * @param {Object} location - 地点数据
 * @returns {Object} 格式化后的地点数据
 */
export function formatLocationData(location) {
    const category = getLocationCategory(location.category);
    
    return {
        ...location,
        categoryName: category.name,
        categoryIcon: category.icon,
        coordinates: {
            latitude: parseFloat(location.latitude),
            longitude: parseFloat(location.longitude)
        }
    };
}

/**
 * 计算两点之间的距离（单位：米）
 * @param {number} lat1 - 第一点纬度
 * @param {number} lng1 - 第一点经度
 * @param {number} lat2 - 第二点纬度
 * @param {number} lng2 - 第二点经度
 * @returns {number} 距离（米）
 */
export function calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6378137; // 地球半径（米）
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function toRad(degrees) {
    return degrees * Math.PI / 180;
}

/**
 * 获取地图缩放级别建议
 * @param {number} distance - 距离（米）
 * @returns {number} 建议的缩放级别
 */
export function getSuggestZoomLevel(distance) {
    if (distance < 100) return 18;
    if (distance < 500) return 17;
    if (distance < 1000) return 16;
    if (distance < 2000) return 15;
    if (distance < 5000) return 14;
    if (distance < 10000) return 13;
    return 12;
}
