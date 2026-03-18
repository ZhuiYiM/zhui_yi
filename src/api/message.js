import request from './request';

export const messageAPI = {
    // 获取消息列表
    getMessages(params = {}) {
        return request.get('/messages', { params });
    },

    // 发送消息
    sendMessage(data) {
        return request.post('/messages', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 批量标记已读
    markAsReadBatch(ids) {
        return request.put('/messages/read-batch', { ids }, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取未读消息数
    getUnreadCount() {
        return request.get('/messages/unread-count');
    },

    // 标记消息为已读
    markAsRead(messageId) {
        return request.put(`/messages/${messageId}/read`);
    },

    // 删除消息
    deleteMessage(messageId) {
        return request.delete(`/messages/${messageId}`);
    }
};