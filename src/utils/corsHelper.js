// CORS配置助手
export const corsConfig = {
    // 开发环境CORS配置
    development: {
        origin: 'http://localhost:5179', // 前端开发服务器地址
        methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
        allowedHeaders: [
            'Content-Type',
            'Authorization',
            'X-Requested-With',
            'Accept',
            'Origin',
            'Access-Control-Request-Method',
            'Access-Control-Request-Headers'
        ],
        credentials: true
    },
    
    // 生产环境CORS配置
    production: {
        origin: 'https://your-domain.com', // 实际域名
        methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
        allowedHeaders: [
            'Content-Type',
            'Authorization',
            'X-Requested-With',
            'Accept'
        ],
        credentials: true
    }
};

// 请求配置工厂函数
export const createRequestConfig = (isLoginRequest = false) => {
    const config = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };
    
    // 登录请求不需要Authorization头
    if (!isLoginRequest) {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    }
    
    return config;
};

// 处理CORS预检请求的响应
export const handlePreflightResponse = (req, res) => {
    res.header('Access-Control-Allow-Origin', req.headers.origin || '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,PATCH,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With, Accept');
    res.header('Access-Control-Allow-Credentials', 'true');
    res.header('Access-Control-Max-Age', '86400'); // 24小时缓存
    
    if (req.method === 'OPTIONS') {
        res.status(200).end();
        return true;
    }
    return false;
};