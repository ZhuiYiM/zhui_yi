import request from './request';

// ========== 标签管理接口 ==========
export const tagAPI = {
    // 获取一级标签 (用户身份) - 从 tag_level_1 表获取
    getLevel1Tags() {
        return request.get('/tags/level1');
    },

    // 获取二级标签 (话题分类) - 从 topic_tag 表获取
    getLevel2Tags(params = {}) {
        return request.get('/tags/topics', { params });
    },

    // 获取三级标签 (地点实体) - 使用地点标签接口
    getLevel3Tags(params = {}) {
        return request.get('/tags/locations', { params });
    },

    // 获取四级标签列表 (支持搜索和分页) - 使用话题标签接口
    getLevel4Tags(params = {}) {
        return request.get('/tags/topics', { params });
    },

    // 创建自定义标签 - 使用话题自定义标签接口
    createLevel4Tag(data) {
        return request.post('/tags/topics/custom', data);
    },

    // 用户提交自定义标签（待审核）
    submitCustomTag(data) {
        return request.post('/tags/custom/submit', data);
    },

    // 获取热门标签
    getHotTags(params = {}) {
        return request.get('/tags/topics/hot', { params });
    },

    // 获取热门标签组合
    getHotTagCombo(params = {}) {
        return request.get('/tags/topics/hot', { params });
    }
};

// ========== 话题管理接口 ==========
export const topicAPI = {
    // 获取话题列表
    getTopics(params = {}) {
        return request.get('/topics', { params });
    },

    // 发布新话题 (带多级标签)
    createTopic(data) {
        return request.post('/topics', data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取话题详情 (含热评)
    getTopicDetail(id, config = {}) {
        return request.get(`/topics/${id}`, config);
    },

    // 点赞话题
    likeTopic(topicId, action) {
        return request.post(`/topics/${topicId}/like`, { action }, {
            headers: {
                'Content-Type': 'application/json'
            },
            noAuth: false  // 明确需要认证
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

    // 获取话题评论列表
    getTopicComments(topicId, params = {}) {
        return request.get(`/topics/${topicId}/comments`, { params });
    },

    // 发布评论
    createComment(topicId, data) {
        return request.post(`/topics/${topicId}/comments`, data);
    },

    // 删除话题
    deleteTopic(id) {
        return request.delete(`/topics/${id}`);
    },

    // 获取用户公开信息
    getUserPublicInfo(userId) {
        return request.get(`/user/${userId}/public-info`);
    },

    // 获取当前用户资料
    getUserProfile() {
        return request.get('/user/profile');
    },

    // 获取用户发布的话题
    getUserPublishedTopics(userId, params = {}) {
        return request.get(`/user/${userId}/published-topics`, { params });
    },

    // 获取用户参与的话题
   getUserParticipatedTopics(userId, params = {}) {
       return request.get(`/user/${userId}/participated-topics`, { params });
    },

    // 获取用户点赞的话题
   getUserLikedTopics(userId, params = {}) {
       return request.get(`/topics/users/${userId}/liked-topics`, { params });
    },

    // 收藏话题
   collectTopic(topicId) {
        return request.post(`/topics/${topicId}/collect`);
    },

    // 获取用户收藏列表
    getCollections(params = {}) {
        return request.get('/topics/collections', { params });
    },

    // 编辑话题
    editTopic(id, data) {
        return request.put(`/topics/${id}`, data, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
    },

    // 获取平台统计
    getStats() {
        return request.get('/topics/stats');
    },
    
    // 生成分享链接
    generateShareUrl(topicId) {
        return request.post(`/topics/${topicId}/share-url`);
    },
    
    // 获取分享信息
    getShareInfo(topicId) {
        return request.get(`/topics/${topicId}/share-info`);
    },
    
    // 转发话题
    forwardTopic(topicId, content = null, images = null) {
        return request.post(`/topics/${topicId}/forward`, { content, images }, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }
};