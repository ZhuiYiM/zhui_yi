import request from './request'

/**
 * 标签管理 API（基于 tag_level_2 表）
 */
export const tagAPI = {
    /**
     * 分页查询二级标签列表
     */
    getLevel2Tags(params) {
        return request({
            url: '/admin/tags',
            method: 'get',
            params
        })
    },

    /**
     * 创建二级标签
     */
    createLevel2Tag(data) {
        return request({
            url: '/admin/tags',
            method: 'post',
            data
        })
    },

    /**
     * 更新二级标签
     */
    updateLevel2Tag(id, data) {
        return request({
            url: `/admin/tags/${id}`,
            method: 'put',
            data: {
                name: data.name,
                icon: data.icon,
                color: data.color,
                sortOrder: data.sortOrder,
                isActive: data.isActive
            }
        })
    },

    /**
     * 更新三级标签
     */
    updateLevel3Tag(id, data) {
        return request({
            url: `/admin/tags/level-3/${id}`,
            method: 'put',
            data: {
                name: data.name,
                locationType: data.locationType,
                icon: data.icon,
                color: data.color,
                address: data.address,
                latitude: data.latitude,
                longitude: data.longitude,
                sortOrder: data.sortOrder,
                isActive: data.isActive
            }
        })
    },

    /**
     * 更新四级标签
     */
    updateLevel4Tag(id, data) {
        return request({
            url: `/admin/tags/level-4/${id}`,
            method: 'put',
            data: {
                name: data.name,
                status: data.status
            }
        })
    },

    /**
     * 更新五级标签
     */
    updateLevel5Tag(id, data) {
        return request({
            url: `/admin/tags/level-5/${id}`,
            method: 'put',
            data
        })
    },

    /**
     * 删除二级标签
     */
    deleteLevel2Tag(id) {
        return request({
            url: `/admin/tags/${id}`,
            method: 'delete'
        })
    },

    /**
     * 批量删除二级标签
     */
    batchDeleteLevel2Tags(ids) {
        return request({
            url: '/admin/tags/batch',
            method: 'delete',
            params: { ids }
        })
    },

    /**
     * 更新二级标签状态
     */
    updateLevel2TagStatus(id, isActive) {
        return request({
            url: `/admin/tags/${id}/status`,
            method: 'put',
            data: { isActive }
        })
    },

    /**
     * 删除三级标签
     */
    deleteLevel3Tag(id) {
        return request({
            url: `/admin/tags/level-3/${id}`,
            method: 'delete'
        })
    },

    /**
     * 批量删除三级标签
     */
    batchDeleteLevel3Tags(ids) {
        return request({
            url: '/admin/tags/level-3/batch',
            method: 'delete',
            params: { ids }
        })
    },

    /**
     * 更新三级标签状态
     */
    updateLevel3TagStatus(id, isActive) {
        return request({
            url: `/admin/tags/level-3/${id}/status`,
            method: 'put',
            data: { isActive }
        })
    },

    /**
     * 删除四级标签
     */
    deleteLevel4Tag(id) {
        return request({
            url: `/admin/tags/level-4/${id}`,
            method: 'delete'
        })
    },

    /**
     * 批量删除四级标签
     */
    batchDeleteLevel4Tags(ids) {
        return request({
            url: '/admin/tags/level-4/batch',
            method: 'delete',
            params: { ids }
        })
    },

    /**
     * 更新四级标签状态
     */
    updateLevel4TagStatus(id, status) {
        return request({
            url: `/admin/tags/level-4/${id}/status`,
            method: 'put',
            data: { status }
        })
    },

    /**
     * 创建三级标签
     */
    createLevel3Tag(data) {
        return request({
            url: '/admin/tags/level-3',
            method: 'post',
            data
        })
    },

    /**
     * 创建四级标签
     */
    createLevel4Tag(data) {
        return request({
            url: '/admin/tags/level-4',
            method: 'post',
            data
        })
    },

    /**
     * 创建五级标签
     */
    createLevel5Tag(data) {
        return request({
            url: '/admin/tags/level-5',
            method: 'post',
            data
        })
    },

    /**
     * 删除五级标签
     */
    deleteLevel5Tag(id) {
        return request({
            url: `/admin/tags/level-5/${id}`,
            method: 'delete'
        })
    },

    /**
     * 批量删除五级标签
     */
    batchDeleteLevel5Tags(ids) {
        return request({
            url: '/admin/tags/level-5/batch',
            method: 'delete',
            params: { ids }
        })
    },

    /**
     * 更新五级标签状态
     */
    updateLevel5TagStatus(id, isActive) {
        return request({
            url: `/admin/tags/level-5/${id}/status`,
            method: 'put',
            data: { 
                isActive,
                status: isActive ? 'active' : 'inactive'
            }
        })
    },

    /**
     * 获取三级标签列表
     */
    getLevel3Tags(params) {
        return request({
            url: '/admin/tags/level-3',
            method: 'get',
            params
        })
    },

    /**
     * 获取四级标签列表
     */
    getLevel4Tags(params) {
        return request({
            url: '/admin/tags/level-4',
            method: 'get',
            params
        })
    },

    /**
     * 获取五级标签列表
     */
    getLevel5TagsAdmin(params) {
        return request({
            url: '/admin/tags/level-5',
            method: 'get',
            params
        })
    }
}
