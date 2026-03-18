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
    }
};
