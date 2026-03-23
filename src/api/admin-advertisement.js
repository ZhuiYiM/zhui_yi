import request from './request'

/**
 * 广告管理 API
 */
export const advertisementAPI = {
    /**
     * 分页查询广告列表
     */
    getAdvertisements(params) {
        return request({
            url: '/admin/advertisements',
            method: 'get',
            params
        })
    },

    /**
     * 根据 ID 获取广告详情
     */
    getAdvertisement(id) {
        return request({
            url: `/admin/advertisements/${id}`,
            method: 'get'
        })
    },

    /**
     * 创建广告
     */
    createAdvertisement(data) {
        return request({
            url: '/admin/advertisements',
            method: 'post',
            data
        })
    },

    /**
     * 更新广告
     */
    updateAdvertisement(id, data) {
        return request({
            url: `/admin/advertisements/${id}`,
            method: 'put',
            data
        })
    },

    /**
     * 删除广告
     */
    deleteAdvertisement(id) {
        return request({
            url: `/admin/advertisements/${id}`,
            method: 'delete'
        })
    },

    /**
     * 批量删除广告
     */
    batchDeleteAdvertisements(ids) {
        return request({
            url: '/admin/advertisements/batch',
            method: 'delete',
            params: { ids }
        })
    },

    /**
     * 更新广告状态
     */
    updateAdvertisementStatus(id, isActive) {
        return request({
            url: `/admin/advertisements/${id}/status`,
            method: 'put',
            params: { isActive }
        })
    }
}
