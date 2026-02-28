import axios from 'axios';
import router from '../router';

// 创建 axios 实例
const apiClient = axios.create({
    baseURL: 'http://localhost:8080', // 直接连接后端服务
    timeout: 15000, // 增加超时时间
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    }
});

// 请求拦截器
apiClient.interceptors.request.use(
    config => {
        // 精确判断不需要token的接口 - 使用完整路径匹配
        const noAuthUrls = [
            '/user/login',
            '/user/register',
            '/user/send-verification-code',
            '/user/verify-code',
            '/user/password/recovery/code',
            '/user/password/reset',
            '/user/login/phone',
            '/user/send-phone-code',
            '/user/wechat/qrcode',
            '/user/wechat/scan-status',
            '/user/wechat/confirm-login'
        ];
        
        // 更严格的路径匹配逻辑
        const isNoAuthEndpoint = noAuthUrls.some(url => 
            config.url === url || config.url.startsWith(url + '/')
        );
        
        // 检查是否明确指定不使用认证
        const explicitNoAuth = config.noAuth === true;
        
        const requiresAuth = !isNoAuthEndpoint && !explicitNoAuth;
        const token = localStorage.getItem('token');
        
        console.log('🔍 请求URL:', config.url);
        console.log('📋 是否需要认证:', requiresAuth);
        console.log('🔑 是否有Token:', !!token);
        console.log('🏷️ 明确免认证:', explicitNoAuth);
        
        // 只对需要认证的接口添加token
        if (token && requiresAuth) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('✅ 已添加Authorization头');
        } else {
            console.log('🚫 跳过Authorization头');
        }
        
        // 添加时间戳防止缓存
        if (config.method === 'get') {
            config.params = {
                ...config.params,
                _t: Date.now()
            };
        }
        
        console.log('🚀 发送请求:', config.method.toUpperCase(), config.url);
        return config;
    },
    error => {
        console.error('❌ 请求配置错误:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
apiClient.interceptors.response.use(
    response => {
        console.log('✅ 响应成功:', response.config.url, response.status);
        
        // 统一处理成功响应
        const responseData = response.data;
        
        // 如果后端返回标准Result结构
        if (responseData && responseData.code !== undefined) {
            if (responseData.code === 200) {
                return responseData.data; // 返回实际数据
            } else {
                // 业务错误
                const errorMsg = responseData.message || '请求失败';
                window.$message?.error(errorMsg) || console.error(errorMsg);
                return Promise.reject(new Error(errorMsg));
            }
        }
        
        // 如果没有code字段，直接返回原始数据
        return responseData;
    },
    error => {
        console.error('❌ 响应错误:', error.response?.status, error.response?.data);
        
        // 统一处理错误响应
        const { response } = error;
        
        if (response) {
            const { status, data } = response;
            
            switch (status) {
                case 400:
                    window.$message?.error(data?.message || '请求参数错误') || console.error(data?.message || '请求参数错误');
                    break;
                case 401:
                    // Token过期或无效
                    window.$message?.error('登录已过期，请重新登录') || console.error('登录已过期，请重新登录');
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    // 延迟跳转避免重复跳转
                    setTimeout(() => {
                        router.push('/login');
                    }, 100);
                    break;
                case 403:
                    window.$message?.error('权限不足或跨域请求被拒绝') || console.error('权限不足或跨域请求被拒绝');
                    break;
                case 404:
                    window.$message?.error('请求的资源不存在') || console.error('请求的资源不存在');
                    break;
                case 408:
                    window.$message?.error('请求超时') || console.error('请求超时');
                    break;
                case 500:
                    window.$message?.error('服务器内部错误') || console.error('服务器内部错误');
                    break;
                case 502:
                    window.$message?.error('网关错误') || console.error('网关错误');
                    break;
                case 503:
                    window.$message?.error('服务暂时不可用') || console.error('服务暂时不可用');
                    break;
                case 504:
                    window.$message?.error('网关超时') || console.error('网关超时');
                    break;
                default:
                    window.$message?.error(data?.message || `请求失败 (${status})`) || console.error(data?.message || `请求失败 (${status})`);
            }
        } else if (error.request) {
            // 网络错误
            window.$message?.error('网络连接异常，请检查网络设置') || console.error('网络连接异常，请检查网络设置');
        } else {
            // 其他错误
            window.$message?.error(error.message || '未知错误') || console.error(error.message || '未知错误');
        }
        
        return Promise.reject(error);
    }
);

export default apiClient;