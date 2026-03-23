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
            params: {
                code: data.code,
                name: data.name,
                icon: data.icon,
                color: data.color,
                sortOrder: data.sortOrder
            }
        })
    },

    /**
     * 更新二级标签
     */
    updateLevel2Tag(id, data) {
        return request({
            url: `/admin/tags/${id}`,
            method: 'put',
            params: {
                name: data.name,
                icon: data.icon,
                color: data.color,
                sortOrder: data.sortOrder,
                isActive: data.isActive
            }
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
            params: { isActive }
        })
    }
}
