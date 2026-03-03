# 校园信息平台API接口文档（完整版）

## 重要变更说明

本文档基于最新实现进行了全面更新，主要变更包括：
1. 所有接口路径均已调整为标准RESTful格式
2. 响应格式已标准化，增加timestamp字段和错误详情
3. 新增用户认证、账户管理、实名认证等完整功能模块
4. 加强了参数验证机制和安全性控制
5. 修正了所有API路径，确保前后端一致

## 1. 接口基础配置

### Base URL
```
开发环境: http://localhost:8080
生产环境: https://api.campus-platform.com
```

### API前缀
所有接口均以 `/api` 开头，例如：`/api/topics`

### 特别注意
- 用户个人资料相关接口路径：`/api/users/profile`（不是`/user/profile`）
- 账户管理相关接口路径：`/account/*`
- 身份认证相关接口路径：`/api/auth/*`
- 请确保前端请求路径与后端控制器映射完全一致

### 公共请求头
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {JWT_TOKEN} (需要认证的接口)
```

### 统一响应格式
```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": "2023-01-01T12:00:00Z",
    "errors": [
        {
            "field": "username",
            "message": "用户名不能为空"
        }
    ]
}
```

### 响应字段说明
- `code`: 状态码 (200成功, 400参数错误, 401未授权, 403权限不足, 404资源不存在, 409资源冲突, 500服务器错误)
- `message`: 响应消息
- `data`: 响应数据
- `timestamp`: 响应时间戳 (ISO 8601格式)
- `errors`: 错误详情列表 (仅在错误时出现)

## 2. 用户认证模块

### 2.1 用户登录
```
POST /api/users/login
Content-Type: application/json

请求体:
{
    "username": "student123",
    "password": "password123"
}
```

### 2.2 用户注册
```
POST /api/users/register

请求体:
{
    "username": "newuser",
    "password": "password123",
    "email": "user@example.com",
    "verificationCode": "123456"  // 邮箱验证码
}
```

### 2.3 发送邮箱验证码
```
POST /api/users/send-verification-code

请求参数:
email: user@example.com
```

### 2.4 手机号登录
```
POST /api/users/login/phone

请求体:
{
    "phone": "13800138000",
    "code": "123456"
}
```

### 2.5 微信扫码登录
```
GET /api/users/wechat/qrcode
获取二维码信息

GET /api/users/wechat/scan-status/{qrCodeId}
查询扫码状态
```

### 2.6 找回密码
```
POST /api/users/password/recovery/code
发送找回密码验证码

参数: identifier=邮箱或手机号, type=email|phone

POST /api/users/password/reset
重置密码

请求体:
{
    "identifier": "user@example.com",
    "code": "123456",
    "newPassword": "newpassword123"
}
```

## 3. 话题管理模块

### 3.1 获取话题列表
```
GET /api/topics?page=1&size=10&sort=latest&tag=校园生活&search=关键词

查询参数:
- page: 页码，默认1
- size: 每页大小，默认10，最大50
- sort: 排序方式 (latest|hot|essence)
- tag: 标签筛选
- search: 搜索关键词
- userId: 用户ID筛选（可选）
```

### 3.2 发布新话题
```
POST /api/topics

请求体:
{
    "content": "期末复习经验分享，马上就要期末考试了，分享一些复习经验和技巧...",
    "images": [
        "https://cdn.example.com/temp/1.jpg",
        "https://cdn.example.com/temp/2.jpg"
    ],
    "tags": ["学习经验", "期末考试", "复习计划"]
}
```

### 3.3 获取话题详情
```
GET /api/topics/{id}?userId={currentUserId}
```

### 3.4 修改话题
```
PUT /api/topics/{id}

请求体:
{
    "content": "修改后的内容",
    "images": ["url1", "url2"],
    "tags": ["新标签1", "新标签2"]
}
```

### 3.5 删除话题
```
DELETE /api/topics/{id}
```

### 3.6 话题置顶操作
```
PUT /api/topics/{id}/top

请求参数:
isTop: true/false
```

### 3.7 话题精华操作
```
PUT /api/topics/{id}/essence

请求参数:
isEssence: true/false
```

## 4. 互动功能模块

### 4.1 点赞/取消点赞
```
POST /api/topics/{id}/like

请求体:
{
    "action": "like" 或 "unlike"
}
```

### 4.2 收藏/取消收藏
```
POST /api/topics/{id}/collect
```

### 4.3 获取用户收藏列表
```
GET /api/topics/collections?page=1&size=10
需要认证头: Authorization: Bearer {token}
```

### 4.4 关注用户
```
POST /api/users/{userId}/follow
需要认证
```

## 5. 评论系统模块

### 5.1 发布评论
```
POST /api/topics/{id}/comments

请求体:
{
    "content": "很有帮助的分享，感谢！",
    "parentId": null  // 回复主话题，回复评论时传入父评论ID
}
```

### 5.2 获取话题评论列表
```
GET /api/topics/{id}/comments?page=1&size=20&sort=latest

查询参数:
- page: 页码
- size: 每页大小
- sort: 排序方式 (latest|hot)
```

### 5.3 删除评论
```
DELETE /api/comments/{id}
```

### 5.4 点赞评论
```
POST /api/comments/{id}/like

请求参数:
action: like/unlike
```

## 6. 标签管理模块

### 6.1 获取热门标签
```
GET /api/topics/tags/popular?limit=20
```

### 6.2 搜索标签
```
GET /api/topics/tags/search?keyword=学习&limit=10
```

## 7. 文件上传模块

### 7.1 上传图片
```
POST /api/upload/image
Content-Type: multipart/form-data

表单参数:
- file: 图片文件 (支持jpg,png,gif,最大5MB)
需要认证头: Authorization: Bearer {token}
```

## 8. 用户个人中心模块

### 8.1 获取个人资料
```
GET /api/users/profile
需要认证头: Authorization: Bearer {token}
```

### 8.2 更新个人资料
```
PUT /api/users/profile

请求体:
{
    "avatarUrl": "头像URL",
    "realName": "真实姓名",
    "gender": "性别",
    "birthDate": "1999-01-01",
    "college": "学院",
    "major": "专业班级",
    "bio": "个人简介",
    "hobbies": ["兴趣1", "兴趣2"]
}
需要认证
```

### 8.3 更新用户电话
```
PUT /api/users/profile/phone

请求参数:
phoneNumber: 13800138000
需要认证
```

### 8.4 更新用户邮箱
```
PUT /api/users/profile/email

请求参数:
email: newemail@example.com
需要认证
```

### 8.5 更新用户学号
```
PUT /api/users/profile/student-id

请求参数:
studentId: 2023001001
需要认证
```

### 8.6 上传头像
```
POST /api/users/{userId}/avatar
Content-Type: multipart/form-data

表单参数:
file: 图片文件
需要认证
```

### 8.7 获取用户隐私设置
```
GET /api/users/{userId}/privacy
需要认证
```

### 8.8 更新用户隐私设置
```
POST /api/users/{userId}/privacy

请求体:
{
    "privacy_settings": {
        "realName": true,
        "phone": false,
        "email": true
    },
    "default_privacy_level": "public"
}
需要认证
```

### 8.9 获取用户发布的话题
```
GET /api/users/{userId}/topics?page=1&size=10
```

## 9. 账户管理模块

### 9.1 修改密码
```
POST /account/password/change
需要认证

请求体:
{
    "oldPassword": "oldpassword123",
    "newPassword": "newpassword123"
}
```

### 9.2 获取登录设备列表
```
GET /account/devices
需要认证
```

### 9.3 退出指定设备
```
DELETE /account/devices/{deviceId}
需要认证
```

### 9.4 退出当前设备
```
DELETE /account/devices/current
需要认证
```

### 9.5 下载用户数据
```
GET /account/data/download
需要认证
```

### 9.6 注销账号
```
DELETE /account/delete
需要认证
```

### 9.7 绑定手机号
```
POST /account/phone/bind
需要认证

请求体:
{
    "phone": "13800138000"
}
```

### 9.8 绑定邮箱
```
POST /account/email/bind
需要认证

请求体:
{
    "email": "newemail@example.com"
}
```

### 9.9 解绑社交账号
```
DELETE /account/social/{platform}
需要认证
```

### 9.10 获取安全设置
```
GET /account/security/settings
需要认证
```

### 9.11 更新安全设置
```
PUT /account/security/settings
需要认证

请求体:
{
    "twoFactorAuth": true,
    "loginNotification": true
}
```

## 10. 身份认证模块

### 10.1 申请学生身份认证
```
POST /api/auth/student-verification
需要认证

请求体:
{
    "studentId": "2023001001",
    "realName": "张三",
    "college": "计算机学院"
}
```

### 10.2 申请身份证实名认证
```
POST /api/auth/idcard-verification
需要认证

请求体:
{
    "realName": "张三",
    "idCard": "110101199001011234"
}
```

### 10.3 获取认证状态
```
GET /api/auth/status
需要认证
```

### 10.4 获取认证申请历史
```
GET /api/auth/applications?page=1&size=10
需要认证
```

### 10.5 取消认证申请
```
DELETE /api/auth/application/{applicationId}
需要认证
```

### 10.6 验证身份证号码格式
```
POST /api/auth/validate-idcard

请求体:
{
    "idCard": "110101199001011234"
}
```

## 11. 消息通知模块

### 11.1 获取用户通知
```
GET /api/messages?type=all&page=1&size=20
需要认证
```

### 11.2 标记通知已读
```
PUT /api/messages/{id}/read
需要认证
```

### 11.3 获取未读消息数量
```
GET /api/messages/unread-count
需要认证
```

## 12. 数据统计模块

### 12.1 获取平台统计信息
```
GET /api/topics/stats
```

## 13. 搜索功能模块

### 13.1 综合搜索
```
GET /api/search/all?keyword=关键词&page=1&size=10

查询参数:
- keyword: 搜索关键词
- page: 页码
- size: 每页大小
```

## 14. 参数验证说明

### 常见验证规则
- 用户名：3-20字符，字母数字下划线
- 密码：6-20字符
- 邮箱：有效邮箱格式
- 手机号：11位数字
- 话题内容：必填，最长1000字符
- 图片数量：最多9张
- 标签数量：最多5个
- 页码：正整数
- 每页大小：1-50之间的正整数
- 身份证：18位有效格式

### 验证错误响应示例
```json
{
    "code": 400,
    "message": "请求参数验证失败",
    "data": null,
    "timestamp": "2023-12-01T12:00:00Z",
    "errors": [
        {
            "field": "username",
            "message": "用户名不能为空"
        },
        {
            "field": "email",
            "message": "邮箱格式不正确"
        },
        {
            "field": "content",
            "message": "话题内容不能为空"
        }
    ]
}
```

## 15. 错误码说明

- 200: 成功
- 400: 请求参数错误/验证失败
- 401: 未授权，需要登录
- 403: 权限不足
- 404: 资源不存在
- 409: 资源冲突（如重复点赞、已收藏等）
- 422: 请求体验证失败
- 429: 请求过于频繁
- 500: 服务器内部错误
- 503: 服务暂时不可用

## 17. 前端集成指南

### 17.1 基础配置
```javascript
// API基础配置
const API_BASE_URL = 'http://localhost:8080';
const API_CONFIG = {
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};
```

### 17.2 请求拦截器
```javascript
// 添加认证token
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);
```

### 17.3 响应拦截器
```javascript
// 统一处理响应
axios.interceptors.response.use(
    response => {
        return response.data;
    },
    error => {
        if (error.response?.status === 401) {
            // token过期，跳转登录
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error.response?.data || error.message);
    }
);
```

### 17.4 常用API调用示例
```javascript
// 用户登录
export const login = (credentials) => {
    return axios.post('/api/users/login', credentials);
};

// 获取用户资料
export const getUserProfile = () => {
    return axios.get('/api/users/profile');
};

// 更新用户资料
export const updateUserProfile = (profileData) => {
    return axios.put('/api/users/profile', profileData);
};

// 发布话题
export const createTopic = (topicData) => {
    return axios.post('/api/topics', topicData);
};

// 获取话题列表
export const getTopics = (params) => {
    return axios.get('/api/topics', { params });
};
```

### 17.5 重要路径对照表
| 功能模块 | 前端请求路径 | 后端控制器 |
|---------|-------------|-----------|
| 用户登录 | `/api/users/login` | UserController |
| 用户注册 | `/api/users/register` | UserController |
| 获取资料 | `/api/users/profile` | UserController |
| 更新资料 | `/api/users/profile` | UserController |
| 修改密码 | `/account/password/change` | AccountManagementController |
| 实名认证 | `/api/auth/idcard-verification` | AuthenticationController |
| 获取通知 | `/api/messages` | MessageController |
| 上传图片 | `/api/upload/image` | FileUploadController |

### 17.6 错误处理建议
```javascript
// 统一错误处理函数
const handleApiError = (error) => {
    const { code, message, errors } = error;
    
    switch(code) {
        case 400:
            console.error('参数错误:', errors);
            break;
        case 401:
            console.error('未授权，请重新登录');
            break;
        case 403:
            console.error('权限不足');
            break;
        case 404:
            console.error('资源不存在');
            break;
        case 500:
            console.error('服务器错误');
            break;
        default:
            console.error('未知错误:', message);
    }
    
    // 显示用户友好的错误提示
    showMessage(message, 'error');
};

## 18. 版本更新记录

### v2.0.0 (当前版本)
- 重构API文档结构，按功能模块分类
- 修正所有API路径，确保前后端一致性
- 新增账户管理、身份认证等完整功能模块
- 完善参数验证和错误处理说明
- 添加前端集成指南和最佳实践

### v1.0.0
- 初始版本，基础话题墙功能
- 基本的用户认证和话题管理接口