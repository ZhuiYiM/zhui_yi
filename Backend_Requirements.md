# 校园信息平台后端功能需求文档

## 项目概述
本文档基于前端已实现的功能，整理出后端需要实现的接口和功能需求。

**涉及模块**:
- ✅ 用户认证模块 (登录、注册、找回密码)
- ✅ 用户个人中心模块 (资料管理、隐私设置)
- ✅ 身份认证模块 (学生认证、实名认证)
- ✅ 话题墙模块 (发布、浏览、点赞、评论)
- ✅ 标签系统模块 (四级标签体系)

---

## 一、用户认证模块

### 1.1 用户登录
**接口**: `POST /user/login`

**请求体**:
```json
{
    "username": "student123",
    "password": "password123"
}
```

**响应**:
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "userId": 1,
        "username": "student123",
        "email": "student@example.com",
        "realName": "张三",
        "avatarUrl": "https://cdn.example.com/avatar/1.jpg"
    }
}
```

**业务逻辑**:
- ✅ 验证用户名和密码
- ✅ 生成 JWT Token(有效期 7 天)
- ✅ 返回用户基本信息
- ⚠️ **注意**: 登录接口不需要认证头

---

### 1.2 用户注册
**接口**: `POST /user/register`

**请求体**:
```json
{
    "username": "newuser",
    "password": "password123",
    "email": "user@example.com",
    "verificationCode": "123456"
}
```

**业务逻辑**:
- ✅ 验证邮箱格式
- ✅ 验证验证码有效性
- ✅ 检查用户名和邮箱是否已存在
- ✅ 密码加密存储 (BCrypt)
- ✅ 自动创建用户记录

---

### 1.3 发送邮箱验证码
**接口**: `POST /user/send-verification-code`

**查询参数**:
```
email: user@example.com
```

**业务逻辑**:
- ✅ 生成 6 位数字验证码
- ✅ 有效期 10 分钟
- ✅ 同一邮箱 60 秒内只能发送一次
- ✅ 发送邮件到指定邮箱

---

### 1.4 手机号验证码登录
**接口**: `POST /user/login/phone`

**请求体**:
```json
{
    "phone": "13800138000",
    "code": "123456"
}
```

**业务逻辑**:
- ✅ 验证手机号格式 (^1[3-9]\d{9}$)
- ✅ 验证验证码
- ✅ 支持自动注册新用户
- ✅ 返回 token 和用户信息

---

### 1.5 发送手机验证码
**接口**: `POST /user/send-phone-code`

**请求体**:
```json
{
    "phone": "13800138000"
}
```

**业务逻辑**:
- ✅ 生成 6 位数字验证码
- ✅ 有效期 10 分钟
- ✅ 同一手机号 60 秒内只能发送一次

---

### 1.6 微信扫码登录
**接口 1**: `GET /user/wechat/qrcode`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "qrCodeId": "abc123def456",
        "qrCodeUrl": "https://open.weixin.qq.com/connect/qrconnect?...",
        "expireTime": 1704067200000
    }
}
```

**接口 2**: `GET /user/wechat/scan-status/{qrCodeId}`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "status": "pending", // pending | scanned | confirmed | expired
        "userInfo": null
    }
}
```

**业务逻辑**:
- ✅ 生成微信二维码
- ✅ 轮询扫码状态 (每 3 秒一次)
- ✅ 用户确认后自动登录并返回 token

---

### 1.7 找回密码
**接口 1**: `POST /user/password/recovery/code`

**查询参数**:
```
identifier: user@example.com  // 邮箱或手机号
type: email  // 或 phone
```

**接口 2**: `POST /user/password/reset`

**请求体**:
```json
{
    "identifier": "user@example.com",
    "code": "123456",
    "newPassword": "newpassword123"
}
```

**业务逻辑**:
- ✅ 验证标识符 (邮箱或手机号)
- ✅ 发送验证码
- ✅ 验证验证码和新密码
- ✅ 更新用户密码

---

### 1.8 退出登录
**接口**: `POST /user/logout`

**业务逻辑**:
- ✅ 使当前 token 失效 (可选：加入黑名单)
- ✅ 清除登录状态

---

### 1.9 刷新 Token
**接口**: `POST /user/refresh`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 验证 token 有效性
- ✅ 刷新 token 有效期
- ✅ 返回新的 token

---

## 二、用户个人中心模块

### 2.1 获取当前用户信息
**接口**: `GET /user/profile`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "username": "student123",
        "email": "student@example.com",
        "phoneNumber": "13800138000",
        "avatarUrl": "https://cdn.example.com/avatar/1.jpg",
        "realName": "张三",
        "studentId": "2023001001",
        "gender": "male",
        "birthDate": "2000-01-01",
        "college": "计算机学院",
        "major": "计算机科学与技术",
        "bio": "个人简介",
        "hobbies": ["编程", "篮球", "音乐"],
        "privacySettings": {
            "phoneVisibility": "private",
            "emailVisibility": "friends",
            "studentIdVisibility": "public",
            "realNameVisibility": "private",
            "topicsPublicVisible": true
        },
        "isVerified": true,
        "isRealNameVerified": true,
        "identity": {
            "level1Tag": "student",
            "verified": true
        },
        "createTime": "2023-09-01T00:00:00"
    }
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 返回完整用户信息
- ✅ 包含隐私设置
- ✅ 包含身份认证信息

---

### 2.2 更新用户资料
**接口**: `PUT /user/profile`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "avatarUrl": "https://cdn.example.com/avatar/1.jpg",
    "gender": "male",
    "birthDate": "2000-01-01",
    "college": "计算机学院",
    "major": "计算机科学与技术",
    "bio": "个人简介",
    "hobbies": ["编程", "篮球"],
    "privacySettings": {
        "phoneVisibility": "private",
        "emailVisibility": "friends",
        "topicsPublicVisible": true
    }
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 真实姓名不允许直接修改 (需通过实名认证)
- ✅ 部分字段可更新
- ✅ 隐私设置单独保存

---

### 2.3 上传头像
**接口**: `POST /users/{userId}/avatar`

**Content-Type**: `multipart/form-data`

**请求头**: `Authorization: Bearer {token}`

**表单参数**:
```
file: [图片文件]  // 支持 jpg/png/gif，最大 5MB
```

**业务逻辑**:
- ✅ 验证图片格式和大小
- ✅ 上传图片到 CDN 或本地存储
- ✅ 更新用户 avatarUrl
- ✅ 返回图片 URL

---

### 2.4 获取隐私设置
**接口**: `GET /user/account/privacy`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "phoneVisibility": "private",
        "emailVisibility": "friends",
        "studentIdVisibility": "public",
        "realNameVisibility": "private",
        "topicsPublicVisible": true
    }
}
```

---

### 2.5 更新隐私设置
**接口**: `PUT /user/account/privacy`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "phoneVisibility": "private",
    "emailVisibility": "friends",
    "studentIdVisibility": "public",
    "realNameVisibility": "private",
    "topicsPublicVisible": false
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 可见性枚举值:`public`(公开)、`friends`(好友可见)、`private`(私密)
- ✅ `topicsPublicVisible`: 控制对外展示页面是否显示话题

---

## 三、身份认证模块

### 3.1 学生身份认证
**接口**: `POST /user/verification/identity`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "studentId": "2023001001",
    "realName": "张三",
    "college": "计算机学院",
    "level1Tag": "student" // student | organization | unverified | merchant
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 验证学号格式
- ✅ 创建认证申请记录
- ✅ 等待管理员审核
- ✅ 审核通过后更新用户 identity 字段

---

### 3.2 实名认证
**接口**: `POST /user/verification/realname`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "realName": "张三",
    "idCard": "110101199001011234"
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 身份证号码格式验证
- ✅ 姓名与身份证匹配校验 (接入公安系统 API 或离线验证)
- ✅ 数据双写：同时更新 `user_verifications` 和 `users` 表
- ✅ 设置 `isRealNameVerified = true`

---

### 3.3 获取认证状态
**接口**: `GET /user/verification/status`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1,
        "isVerified": true,
        "isIdVerified": true,
        "isRealNameVerified": true,
        "verificationStats": {
            "pending": 0,
            "approved": 2,
            "rejected": 0
        },
        "recentVerifications": [],
        "identity": {
            "level1Tag": "student",
            "verified": true
        }
    }
}
```

---

### 3.4 认证申请历史
**接口**: `GET /user/verification/applications?page=1&size=10`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 分页查询用户的认证申请记录
- ✅ 包含审核状态 (pending | approved | rejected)

---

### 3.5 取消认证申请
**接口**: `DELETE /user/verification/applications/{applicationId}`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 仅能取消待审核状态的申请
- ✅ 删除或标记为 cancelled

---

## 四、话题墙模块

### 4.1 发布话题
**接口**: `POST /topic/publish`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "title": "期末复习经验分享",
    "content": "马上就要期末考试了，分享一些复习经验和技巧...",
    "images": [
        "https://cdn.example.com/temp/1.jpg",
        "https://cdn.example.com/temp/2.jpg"
    ],
    "level1Tag": "student",
    "level2Tags": [
        {"code": "study_experience", "name": "学习经验", "color": "#50C878"}
    ],
    "level3Tags": [
        {"code": "library", "name": "图书馆"}
    ],
    "level4Tags": [
        {"code": "programming", "name": "编程"}
    ]
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 内容长度验证:1-1000 字符
- ✅ 图片数量验证：最多 9 张
- ✅ 标签验证：一级标签必填，二、三级最多各 3 个，四级最多 3 个
- ✅ 标题可选，如不提供则自动从内容提取前 20 字符
- ✅ 关联用户的 identity.level1Tag
- ✅ 返回话题 ID

---

### 4.2 获取话题列表
**接口**: `GET /topics`

**查询参数**:
```
page=1&size=10&sort=latest&tag=学习经验&search=关键词&userId=1&level1Tag=student&level2Tags=study_experience&level3Tags=library
```

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "topics": [
            {
                "id": 1,
                "content": "期末复习经验分享...",
                "images": ["url1", "url2"],
                "tags": ["学习经验", "期末考试"],
                "likesCount": 15,
                "commentsCount": 8,
                "viewsCount": 120,
                "isEssence": 1,
                "isTop": 0,
                "createdAt": "2024-01-01T12:00:00",
                "author": {
                    "id": 1,
                    "username": "student123",
                    "realName": "张三",
                    "avatarUrl": "url",
                    "studentId": "2023001001",
                    "identity": {
                        "level1Tag": "student"
                    }
                },
                "level1Tag": "student",
                "level2Tags": [...],
                "level3Tags": [...],
                "level4Tags": [...]
            }
        ],
        "total": 100,
        "page": 1,
        "size": 10,
        "totalPages": 10
    }
}
```

**业务逻辑**:
- ✅ 分页查询 (默认 10 条，最大 50 条)
- ✅ 排序方式:`latest`(最新)、`hot`(热门)、`essence`(精华)
- ✅ 支持标签筛选 (一级、二级、三级)
- ✅ 支持关键词搜索 (内容、标题)
- ✅ 支持用户 ID 筛选
- ✅ 统计点赞数、评论数、浏览数
- ✅ 包含作者信息和完整标签体系

---

### 4.3 获取话题详情
**接口**: `GET /topic/{id}`

**查询参数**: `userId={currentUserId}` (用于判断是否点赞)

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "content": "期末复习经验分享...",
        "images": ["url1", "url2"],
        "tags": ["学习经验", "期末考试"],
        "likesCount": 15,
        "commentsCount": 8,
        "viewsCount": 121,
        "isEssence": 1,
        "isTop": 0,
        "createdAt": "2024-01-01T12:00:00",
        "isLiked": false,
        "author": {
            "id": 1,
            "username": "student123",
            "realName": "张三",
            "avatarUrl": "url",
            "identity": {
                "level1Tag": "student"
            }
        },
        "level1Tag": "student",
        "level2Tags": [...],
        "level3Tags": [...],
        "level4Tags": [...],
        "comments": [
            {
                "id": 1,
                "content": "很有帮助的分享!",
                "likesCount": 5,
                "createdAt": "2024-01-01T13:00:00",
                "parentId": null,
                "author": {
                    "id": 2,
                    "username": "user456",
                    "realName": "李四",
                    "avatarUrl": "url"
                }
            }
        ]
    }
}
```

**业务逻辑**:
- ✅ 增加浏览次数 (+1)
- ✅ 包含完整标签体系
- ✅ 包含热评 (评论数>10 时，按点赞数降序取前 20%)
- ✅ 标记当前用户是否点赞

---

### 4.4 编辑话题
**接口**: `PUT /api/topics/{id}`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 需要认证
- ✅ 仅话题作者可编辑
- ✅ 验证权限
- ✅ 更新话题内容和标签

---

### 4.5 删除话题
**接口**: `DELETE /topic/{id}`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 需要认证
- ✅ 仅话题作者可删除
- ✅ 逻辑删除 (设置 status=0)
- ✅ 保留评论数据

---

### 4.6 点赞/取消点赞话题
**接口**: `POST /topics/like`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "topicId": 1,
    "action": "like"  // 或 "unlike"
}
```

**响应**:
```json
{
    "code": 200,
    "message": "点赞成功",
    "data": {
        "likesCount": 16,
        "isLiked": true
    }
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 防止重复点赞 (使用唯一索引)
- ✅ 增加/减少点赞数
- ✅ 记录点赞关系

---

### 4.7 收藏/取消收藏话题
**接口**: `POST /api/topics/{id}/collect`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 需要认证
- ✅ 切换收藏状态
- ✅ 记录收藏关系

---

### 4.8 获取用户收藏列表
**接口**: `GET /api/topics/collections?page=1&size=10`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 分页查询用户收藏的话题
- ✅ 包含话题完整信息

---

### 4.9 获取用户发布的话题
**接口**: `GET /user/{userId}/published-topics?page=1&size=10`

**查询参数**:
```
page=1&size=10
```

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "topics": [...],
        "total": 15,
        "page": 1,
        "size": 10,
        "totalPages": 2
    }
}
```

**业务逻辑**:
- ✅ 查询指定用户发布的所有话题
- ✅ 受隐私设置控制 (topicsPublicVisible)
- ✅ 分页返回

---

### 4.10 获取用户参与的话题
**接口**: `GET /user/{userId}/participated-topics?page=1&size=10`

**业务逻辑**:
- ✅ 查询用户评论过的话题
- ✅ 去重并按最新评论时间排序
- ✅ 受隐私设置控制

---

### 4.11 获取用户公开信息
**接口**: `GET /user/{userId}/public-info`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "basicInfo": {
            "id": 1,
            "username": "student123",
            "realName": "张三",
            "avatarUrl": "url",
            "bio": "个人简介"
        },
        "academicInfo": {
            "college": "计算机学院",
            "major": "计算机科学与技术",
            "studentId": "2023001001",
            "grade": "2021 级"
        },
        "statistics": {
            "postCount": 15,
            "followerCount": 128,
            "followingCount": 56,
            "likesReceived": 342
        },
        "identity": {
            "level1Tag": "student",
            "verified": true
        },
        "privacySettings": {
            "topicsPublicVisible": true,
            "phoneVisibility": "private",
            "emailVisibility": "friends",
            "studentIdVisibility": "public",
            "realNameVisibility": "private"
        },
        "canMessage": false
    }
}
```

**业务逻辑**:
- ✅ 无需认证 (游客可访问)
- ✅ 根据隐私设置过滤敏感信息
- ✅ 如果 `topicsPublicVisible=false`,返回提示

---

## 五、评论系统模块

### 5.1 发布评论
**接口**: `POST /topic/{topicId}/comment`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "content": "很有帮助的分享，感谢!",
    "parentId": null  // 回复评论时传入父评论 ID
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 内容长度验证:1-500 字符
- ✅ 支持回复评论 (最多 3 层)
- ✅ 增加话题评论数

---

### 5.2 获取评论列表
**接口**: `GET /topic/{topicId}/comments?page=1&size=20&sort=latest`

**查询参数**:
- `page`: 页码，默认 1
- `size`: 每页大小，默认 20
- `sort`: 排序方式 (`latest`=最新，`hot`=最热)

**业务逻辑**:
- ✅ 分页查询
- ✅ 支持排序
- ✅ 包含评论者信息

---

### 5.3 删除评论
**接口**: `DELETE /api/comments/{commentId}`

**请求头**: `Authorization: Bearer {token}`

**业务逻辑**:
- ✅ 需要认证
- ✅ 仅评论作者可删除
- ✅ 逻辑删除
- ✅ 减少话题评论数

---

### 5.4 点赞/取消点赞评论
**接口**: `POST /api/comments/{commentId}/like`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "action": "like"  // 或 "unlike"
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 防止重复点赞
- ✅ 增加/减少点赞数

---

## 六、标签系统模块

### 6.1 获取一级标签 (用户身份)
**接口**: `GET /topic/tags/level1`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {"code": "student", "name": "学生", "icon": "👨‍🎓"},
        {"code": "organization", "name": "团体", "icon": "👥"},
        {"code": "followed", "name": "关注", "icon": "⭐"},
        {"code": "society", "name": "社会", "icon": "🌍"}
    ]
}
```

**业务逻辑**:
- ✅ 无需认证
- ✅ 返回所有一级标签

---

### 6.2 获取二级标签 (话题分类)
**接口**: `GET /topic/tags/level2`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {"code": "study_experience", "name": "学习经验", "color": "#50C878"},
        {"code": "tech_exchange", "name": "技术交流", "color": "#4169E1"},
        ...
    ]
}
```

**业务逻辑**:
- ✅ 无需认证
- ✅ 包含颜色属性

---

### 6.3 获取三级标签 (地点)
**接口**: `GET /topic/tags/level3`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {"code": "library", "name": "图书馆"},
        {"code": "cafeteria", "name": "食堂"},
        ...
    ]
}
```

---

### 6.4 获取四级标签 (自定义标签)
**接口**: `GET /topic/tags/level4?keyword=关键词&page=1&size=20`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "name": "编程",
                "usage_count": 150,
                "trend_score": 95.5,
                "status": "active"
            }
        ],
        "total": 100,
        "currentPage": 1,
        "pageSize": 20,
        "totalPages": 5
    }
}
```

**业务逻辑**:
- ✅ 支持关键词搜索
- ✅ 分页查询
- ✅ 统计使用次数和趋势分数

---

### 6.5 创建自定义标签
**接口**: `POST /topic/tags/level4`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
    "name": "编程",
    "description": "编程相关话题"
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 检查标签是否已存在
- ✅ 创建新标签

---

### 6.6 批量创建四级标签
**接口**: `POST /topic/tags/level4/batch`

**请求体**:
```json
{
    "tagNames": ["编程", "考研", "兼职"]
}
```

**业务逻辑**:
- ✅ 智能判断已存在标签
- ✅ 自动创建新标签
- ✅ 返回所有标签 ID

---

### 6.7 获取热门标签
**接口**: `GET /topic/tags/hot?limit=20`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": ["编程", "考研", "兼职", "学习经验", ...]
}
```

**业务逻辑**:
- ✅ 按使用频率排序
- ✅ 限制返回数量

---

## 七、社区统计模块

### 7.1 获取平台统计
**接口**: `GET /api/topics/stats`

**响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "totalPosts": 1000,
        "totalComments": 5000,
        "totalLikes": 15000,
        "todayPosts": 50,
        "activeUsers": 200
    }
}
```

**业务逻辑**:
- ✅ 统计总话题数
- ✅ 统计总评论数
- ✅ 统计总点赞数
- ✅ 统计今日话题数
- ✅ 统计活跃用户数 (近 30 天有活动)

---

## 八、文件上传模块

### 8.1 上传图片
**接口**: `POST /api/upload/image`

**Content-Type**: `multipart/form-data`

**请求头**: `Authorization: Bearer {token}`

**表单参数**:
```
file: [图片文件]  // 支持 jpg/png/gif，最大 5MB
```

**响应**:
```json
{
    "code": 200,
    "message": "上传成功",
    "data": {
        "url": "https://cdn.example.com/images/abc123.jpg",
        "filename": "image.jpg",
        "size": 102400
    }
}
```

**业务逻辑**:
- ✅ 需要认证
- ✅ 验证图片格式和大小
- ✅ 上传图片到 CDN 或本地存储
- ✅ 生成访问 URL

---

## 九、数据库表设计建议

### 9.1 用户表 (users)
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    avatar_url VARCHAR(255),
    real_name VARCHAR(50),
    student_id VARCHAR(20),
    gender ENUM('male', 'female', 'other'),
    birth_date DATE,
    college VARCHAR(100),
    major VARCHAR(100),
    bio TEXT,
    hobbies JSON,
    is_verified BOOLEAN DEFAULT FALSE,
    is_real_name_verified BOOLEAN DEFAULT FALSE,
    identity_level1_tag VARCHAR(50),
    identity_verified BOOLEAN DEFAULT FALSE,
    privacy_settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1
);
```

### 9.2 话题表 (topics)
```sql
CREATE TABLE topics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    author_id INT NOT NULL,
    title VARCHAR(100),
    content TEXT NOT NULL,
    images JSON,
    level1_tag VARCHAR(50),
    level1_tag_name VARCHAR(50),
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    views_count INT DEFAULT 0,
    collections_count INT DEFAULT 0,
    is_essence TINYINT DEFAULT 0,
    is_top TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
```

### 9.3 话题标签关联表 (topic_tags)
```sql
CREATE TABLE topic_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    topic_id INT NOT NULL,
    tag_level TINYINT NOT NULL,
    tag_code VARCHAR(50) NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_tag_level (tag_level)
);
```

### 9.4 评论表 (comments)
```sql
CREATE TABLE comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    topic_id INT NOT NULL,
    author_id INT NOT NULL,
    parent_id INT,
    content TEXT NOT NULL,
    likes_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES comments(id)
);
```

### 9.5 标签表 (tags)
```sql
CREATE TABLE tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    level TINYINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    icon VARCHAR(50),
    description TEXT,
    usage_count INT DEFAULT 0,
    trend_score DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_level (name, level)
);
```

### 9.6 点赞表 (likes)
```sql
CREATE TABLE likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    target_type VARCHAR(20) NOT NULL, -- 'topic' | 'comment'
    target_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 9.7 收藏表 (collections)
```sql
CREATE TABLE collections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    topic_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_topic (user_id, topic_id),
    FOREIGN KEY (topic_id) REFERENCES topics(id)
);
```

### 9.8 认证申请表 (user_verifications)
```sql
CREATE TABLE user_verifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'identity' | 'realname'
    student_id VARCHAR(20),
    real_name VARCHAR(50),
    id_card VARCHAR(20),
    college VARCHAR(100),
    level1_tag VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending', -- 'pending' | 'approved' | 'rejected'
    reason TEXT,
    reviewed_by INT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 十、优先级说明

### P0 - 核心功能 (必须实现)
- ✅ 用户登录/注册
- ✅ 发布话题
- ✅ 获取话题列表
- ✅ 话题详情 (含评论)
- ✅ 点赞/取消点赞
- ✅ 发布评论
- ✅ 获取用户信息
- ✅ 身份认证

### P1 - 重要功能
- ✅ 手机号登录
- ✅ 找回密码
- ✅ 编辑/删除话题
- ✅ 删除评论
- ✅ 收藏功能
- ✅ 标签系统
- ✅ 隐私设置
- ✅ 用户对外展示页面

### P2 - 增强功能
- ⭕ 微信扫码登录
- ⭕ 话题置顶/精华
- ⭕ 评论点赞
- ⭕ 标签搜索
- ⭕ 数据统计

---

## 十一、技术栈建议

### 后端框架
- Spring Boot 2.7+
- MyBatis-Plus / JPA
- Spring Security + JWT

### 数据库
- MySQL 8.0+
- Redis(缓存、会话管理)

### 其他
- MinIO / 阿里云 OSS(文件存储)
- RabbitMQ / Kafka(异步任务、消息队列)
- Elasticsearch(全文搜索，可选)

---

## 十二、开发注意事项

### 1. 安全性
- ✅ 密码必须加密存储 (BCrypt)
- ✅ JWT Token 设置合理有效期
- ✅ 防止 SQL 注入
- ✅ 接口限流 (防刷)
- ✅ XSS 防护

### 2. 性能优化
- ✅ 热点数据缓存 (Redis)
- ✅ 分页查询优化
- ✅ 图片 CDN 加速
- ✅ 数据库索引优化

### 3. 数据一致性
- ✅ 事务管理
- ✅ 乐观锁/悲观锁
- ✅ 分布式锁 (Redis)

### 4. 错误处理
- ✅ 统一异常处理
- ✅ 全局错误码规范
- ✅ 日志记录

---

**文档版本**: v1.0  
**创建时间**: 2024-01-01  
**适用阶段**: 后端开发 Phase 1  
