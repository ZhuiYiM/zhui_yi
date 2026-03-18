import request from './request';

/**
 * 拉黑功能 API
 */
export const blockAPI = {
    // 拉黑用户
    blockUser(blockedId) {
        return request.post('/blocks', { blockedId }, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },

    // 取消拉黑用户
    unblockUser(blockedId) {
        return request.delete(`/blocks/${blockedId}`);
    },

    // 获取拉黑列表
    getBlockList(params = {}) {
        return request.get('/blocks/list', { params });
    },

    // 检查是否被拉黑
    isBlocked(userId) {
        return request.get('/blocks/check', { params: { userId } });
    }
};
