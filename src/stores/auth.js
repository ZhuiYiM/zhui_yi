import { ref, computed, onMounted } from 'vue';
import { defineStore } from 'pinia';
// authAPI 已合并到 userAPI 中
import { userAPI } from '@/api/user';
import router from '@/router';
import { ElMessage } from 'element-plus'; // 添加 ElMessage 导入

export const useAuthStore = defineStore('auth', () => {
    // 状态
    const token = ref(localStorage.getItem('token') || '');
    const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'));
    const isAuthenticated = computed(() => !!token.value);

    // Actions
    const login = async (loginData) => {
        try {
            // 清理可能存在的旧 Token，避免干扰登录请求
            const existingToken = localStorage.getItem('token');
            if (existingToken) {
                localStorage.removeItem('token');
            }
                
            const response = await userAPI.login(loginData);
                
            // 检查响应数据结构
            if (!response) {
                throw new Error('服务器无响应数据');
            }
                
            const { token: userToken, user } = response;
                
            // 验证必要字段
            if (!userToken) {
                throw new Error('服务器响应缺少 token');
            }
                
            // 保存 token
            token.value = userToken;
            localStorage.setItem('token', userToken);
                
            // 自动获取完整的用户信息
            await fetchCompleteUserInfo();
                
            ElMessage.success('登录成功');
            return response;
        } catch (error) {
            console.error('❌ 登录失败:', error);
                
            // 详细错误处理
            if (error.response) {
                const { status, data } = error.response;
                console.error('📡 错误响应:', status, data);
                switch (status) {
                    case 400:
                        throw new Error(data?.message || '用户名或密码错误');
                    case 401:
                        throw new Error('用户名或密码错误');
                    case 403:
                        throw new Error('账户已被禁用');
                    case 500:
                        throw new Error('服务器内部错误');
                    default:
                        throw new Error(data?.message || `登录失败 (${status})`);
                }
            } else if (error.request) {
                console.error('🌐 网络请求失败:', error.request);
                throw new Error('网络连接失败，请检查网络设置');
            } else {
                console.error('🐛 其他错误:', error.message);
                throw error;
            }
        }
    };

    const register = async (registerData) => {
        try {
            const response = await userAPI.register(registerData);
            return response;
        } catch (error) {
            throw error;
        }
    };

    const logout = async () => {
        try {
            await userAPI.logout();
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // 清除本地存储
            token.value = '';
            userInfo.value = {};
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            
            // 跳转到登录页
            router.push('/login');
        }
    };

    const refreshUserInfo = async () => {
        try {
            const user = await userAPI.getCurrentUser();
            userInfo.value = user;
            localStorage.setItem('user', JSON.stringify(user));
        } catch (error) {
            console.error('Refresh user info error:', error);
        }
    };
    
    // 获取完整的用户信息（包括头像、学号等）
    const fetchCompleteUserInfo = async () => {
        try {
            const user = await userAPI.getCurrentUser();
            
            // 处理用户信息，确保字段兼容性
            const processedUserInfo = {
                id: user.id || user.userId || '',
                username: user.username || user.name || '',
                name: user.name || user.nickname || user.username || '',
                studentId: user.studentId || user.student_id || user.schoolId || '',
                avatar: user.avatar || user.profilePicture || '',
                email: user.email || '',
                phone: user.phone || user.phoneNumber || '',
                role: user.role || 'user'
            };
            
            userInfo.value = processedUserInfo;
            localStorage.setItem('user', JSON.stringify(processedUserInfo));
            
            return processedUserInfo;
        } catch (error) {
            console.error('❌ 获取完整用户信息失败:', error);
            // 如果获取失败，使用登录时的基本信息
            const basicInfo = {
                username: localStorage.getItem('temp_username') || '',
                name: localStorage.getItem('temp_username') || '用户',
                studentId: '',
                avatar: ''
            };
            userInfo.value = basicInfo;
            localStorage.setItem('user', JSON.stringify(basicInfo));
            return basicInfo;
        }
    };
    
    // 初始化用户信息（应用启动时调用）
    const initUserInfo = async () => {
        const tokenValue = localStorage.getItem('token');
        if (tokenValue) {
            token.value = tokenValue;
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                userInfo.value = JSON.parse(storedUser);
            }
            // 尝试刷新用户信息
            await refreshUserInfo();
        }
    };

    const checkAuth = () => {
        return isAuthenticated.value;
    };

    return {
        // 状态
        token,
        userInfo,
        isAuthenticated,
        
        // 方法
        login,
        register,
        logout,
        refreshUserInfo,
        fetchCompleteUserInfo,
        initUserInfo,
        checkAuth
    };
});