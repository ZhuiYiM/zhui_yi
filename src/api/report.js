import request from './request';

/**
 * 举报相关 API
 */
export const reportAPI = {
    /**
     * 提交举报
     * @param {Object} data - 举报数据
     * @param {string} data.targetType - 举报类型：'location' | 'product' | 'topic' | 'review'
     * @param {number} data.targetId - 举报目标 ID
     * @param {string} data.reason - 举报原因
     * @param {string} data.description - 举报详细描述
     * @param {Array} data.images - 举报图片（可选）
     */
    submitReport(data) {
        return request.post('/reports', data);
    },

    /**
     * 获取举报原因选项
     */
    getReportReasons() {
        return request.get('/reports/reasons');
    },

    /**
     * 获取我的举报列表
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码
     * @param {number} params.size - 每页数量
     */
    getMyReports(params) {
        return request.get('/reports/my', { params });
    }
};
