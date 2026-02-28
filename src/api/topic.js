import request from './request';

export const topicAPI = {
    // 获取话题列表
    getTopics(params = {}) {
        return request.get('/topics', { params });
    },

    // 发布新话题
    createTopic(data) {
        return request.post('/topics', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 点赞话题
    likeTopic(data) {
        return request.post('/topics/like', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取热门标签
    getPopularTags() {
        return request.get('/topics/tags/popular');
    },

    // 搜索话题
    searchTopics(query, params = {}) {
        return request.get(`/topics/search?q=${encodeURIComponent(query)}`, { params });
    },

    // 根据标签获取话题
    getTopicsByTag(tag, params = {}) {
        return request.get(`/topics/tag/${encodeURIComponent(tag)}`, { params });
    },

    // 获取话题详情
    getTopicDetail(id) {
        return request.get(`/topics/${id}`);
    },

    // 删除话题
    deleteTopic(id) {
        return request.delete(`/topics/${id}`);
    },

    // 获取用户发布的所有话题
    getUserTopics(userId, params = {}) {
        return request.get(`/users/${userId}/topics`, { params });
    }
};