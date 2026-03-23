import request from './request';

export const adminAPI = {
    // 管理员登录
    login(username, password) {
        return request.post('/admin/login', { username, password });
    },

    // 管理员退出
    logout() {
        return request.post('/admin/logout');
    },

    // 获取管理员信息
    getInfo() {
        return request.get('/admin/info');
    },

    // 更新管理员信息
    updateProfile(data) {
        return request.put('/admin/profile', data);
    },

    // 修改密码
    changePassword(data) {
        return request.put('/admin/change-password', data);
    },

    // 获取首页统计数据
    getDashboardStats() {
        return request.get('/admin/dashboard/stats');
    },

    // ========== 用户管理 ==========
    // 获取用户列表
    getUserList(params) {
        return request.get('/admin/users', { params });
    },

    // 获取用户详情
    getUserDetail(userId) {
        return request.get(`/admin/users/${userId}`);
    },

    // 更新用户状态
    updateUserStatus(userId, status) {
        return request.put(`/admin/users/${userId}/status`, { status });
    },

    // 删除用户
    deleteUser(userId) {
        return request.delete(`/admin/users/${userId}`);
    },

    // 审核用户身份证认证
    verifyUserIdCard(userId, pass, reason) {
        return request.put(`/admin/users/${userId}/idcard-verify`, { pass, reason });
    },

    // 审核用户实名认证
    verifyUserRealName(userId, pass, reason) {
        return request.put(`/admin/users/${userId}/realname-verify`, { pass, reason });
    },

    // ========== 认证申请管理 ==========
    // 获取认证申请列表
    getVerificationApplications(params) {
        return request.get('/admin/verifications', { params });
    },

    // 获取认证申请详情
    getApplicationDetail(applicationId) {
        return request.get(`/admin/verifications/${applicationId}`);
    },

    // 通过认证申请
    approveApplication(applicationId, adminRemark) {
        return request.put(`/admin/verifications/${applicationId}/approve`, { adminRemark });
    },

    // 拒绝认证申请
    rejectApplication(applicationId, reason) {
        return request.put(`/admin/verifications/${applicationId}/reject`, { reason });
    },

    // ========== 话题管理 ==========
    // 获取话题列表
    getTopicList(params) {
        return request.get('/admin/topics', { params });
    },

    // 删除话题
    deleteTopic(topicId) {
        return request.delete(`/admin/topics/${topicId}`);
    },

    // 审核话题
    reviewTopic(topicId, status) {
        return request.put(`/admin/topics/${topicId}/review`, { status });
    },

    // ========== 商品管理 ==========
    // 获取商品列表
    getProductList(params) {
        return request.get('/admin/products', { params });
    },

    // 下架商品
    removeProduct(productId) {
        return request.put(`/admin/products/${productId}/remove`);
    },

    // 更新商品状态
    updateProductStatus(productId, status) {
        return request.put(`/admin/products/${productId}/status`, { status });
    },

    // 删除商品
    deleteProduct(productId) {
        return request.delete(`/admin/products/${productId}`);
    },

    // ========== 举报管理 ==========
    // 获取举报列表
    getReportList(params) {
        return request.get('/admin/reports', { params });
    },

    // 处理举报
    handleReport(reportId, status, result) {
        return request.put(`/admin/reports/${reportId}/handle`, { status, result });
    },

    // 获取举报统计
    getReportStats() {
        return request.get('/admin/reports/stats');
    },

    // ========== 评论管理 ==========
    // 获取评论列表
    getCommentList(params) {
        return request.get('/admin/comments', { params });
    },

    // 删除评论
    deleteComment(commentId) {
        return request.delete(`/admin/comments/${commentId}`);
    },

    // ========== 操作日志 ==========
    // 获取操作日志列表
    getOperationLogs(params) {
        return request.get('/admin/logs', { params });
    },

    // ========== 系统设置 ==========
    // 获取系统设置
    getSystemSettings() {
        return request.get('/admin/settings');
    },

    // 更新系统设置
    updateSystemSettings(data) {
        return request.put('/admin/settings', data);
    },

    // ========== 标签管理 ==========
    // 二级标签
    getLevel2Tags(params) {
        return request.get('/admin/tags', { params });
    },
    createLevel2Tag(data) {
        return request.post('/admin/tags', data);
    },
    updateLevel2Tag(id, data) {
        return request.put(`/admin/tags/${id}`, data);
    },
    deleteLevel2Tag(id) {
        return request.delete(`/admin/tags/${id}`);
    },
    batchDeleteLevel2Tags(ids) {
        return request.delete('/admin/tags/batch', { params: { ids } });
    },
    updateLevel2TagStatus(id, isActive) {
        return request.put(`/admin/tags/${id}/status`, { isActive });
    },

    // 三级标签
    getLevel3Tags(params) {
        return request.get('/admin/tags/level-3', { params });
    },
    createLevel3Tag(data) {
        return request.post('/admin/tags/level-3', data);
    },
    updateLevel3Tag(id, data) {
        return request.put(`/admin/tags/level-3/${id}`, data);
    },
    deleteLevel3Tag(id) {
        return request.delete(`/admin/tags/level-3/${id}`);
    },
    batchDeleteLevel3Tags(ids) {
        return request.delete('/admin/tags/level-3/batch', { params: { ids } });
    },
    updateLevel3TagStatus(id, isActive) {
        return request.put(`/admin/tags/level-3/${id}/status`, { isActive });
    },

    // 四级标签
    getLevel4Tags(params) {
        return request.get('/admin/tags/level-4', { params });
    },
    updateLevel4Tag(id, data) {
        return request.put(`/admin/tags/level-4/${id}`, data);
    },
    deleteLevel4Tag(id) {
        return request.delete(`/admin/tags/level-4/${id}`);
    },
    batchDeleteLevel4Tags(ids) {
        return request.delete('/admin/tags/level-4/batch', { params: { ids } });
    },
    updateLevel4TagStatus(id, status) {
        return request.put(`/admin/tags/level-4/${id}/status`, { status });
    }
};
