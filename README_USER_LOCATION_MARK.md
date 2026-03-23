# 用户位置标记功能 - 完整实现

## 📚 目录

1. [功能概述](#功能概述)
2. [快速开始](#快速开始)
3. [文件清单](#文件清单)
4. [API 接口文档](#api 接口文档)
5. [前端组件使用](#前端组件使用)
6. [测试指南](#测试指南)
7. [后续开发计划](#后续开发计划)

---

## 功能概述

### 什么是用户位置标记？

用户位置标记功能允许用户在校园地图上创建自定义位置标记，包括：

- 🤝 **约见地点** - 学生标记的见面集合点
- 🏪 **店铺位置** - 商家标记的店铺位置  
- 👥 **活动地点** - 社团标记的活动举办地点

### 核心特性

✅ **审核机制** - 所有标记需要管理员审核后才能公开显示  
✅ **可见性控制** - 支持公开、好友可见、私密三种可见性级别  
✅ **身份验证** - 不同身份用户可创建不同类型的标记  
✅ **地图集成** - 在地图页面直接展示用户标记  
✅ **时间控制** - 支持永久标记和临时标记  

---

## 快速开始

### 1. 数据库初始化

```bash
# 执行 SQL 脚本创建表和测试数据
Get-Content database_user_location_mark_simple.sql | mysql -u root -p123456 campus_db
```

### 2. 启动后端服务

```bash
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
npm run dev
```

### 4. 访问应用

- 前端地址：http://localhost:5173
- 后端地址：http://localhost:8080

---

## 文件清单

### 后端文件

```
src/main/java/com/example/demo/
├── entity/
│   ├── UserLocationMark.java              # 位置标记实体类
│   └── dto/
│       └── UserLocationMarkCreateDTO.java # 创建 DTO
├── mapper/
│   └── UserLocationMarkMapper.java        # Mapper 接口
├── service/
│   ├── UserLocationMarkService.java       # Service 接口
│   └── impl/
│       └── UserLocationMarkServiceImpl.java  # Service 实现
└── controller/
    └── UserLocationMarkController.java    # RESTful API 控制器
```

### 前端文件

```
src/
├── api/
│   └── campus.js                          # API 接口（新增 location-marks 方法）
├── components/map/
│   ├── LocationMarkModal.vue              # 标记创建模态框
│   └── Map.vue                            # 地图页面（已集成标记功能）
└── tests/
    └── user-location-marks.postman_collection.json  # Postman 测试集
```

### 数据库文件

```
database_user_location_mark.sql            # 完整版 SQL（含中文注释）
database_user_location_mark_simple.sql     # 简化版 SQL（无编码问题）
```

### 文档文件

```
USER_LOCATION_MARK_FEATURE_README.md       # 功能说明文档
USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md  # 实现总结
QUICK_VERIFICATION_SCRIPT.md               # 快速验证脚本
README_USER_LOCATION_MARK.md               # 本文件
```

---

## API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8080/api/user/location-marks`
- **认证方式**: Bearer Token (JWT)
- **请求格式**: JSON

### 接口列表

#### 1. 创建位置标记

```http
POST /api/user/location-marks
Authorization: Bearer {token}
Content-Type: application/json

{
  "campusId": 1,
  "locationName": "图书馆门口",
  "latitude": 35.308123,
  "longitude": 113.926234,
  "markType": "meeting_point",
  "markCategory": "building",
  "description": "经常在图书馆门口集合",
  "contactInfo": "13800138000",
  "images": ["image1.jpg", "image2.jpg"],
  "startTime": "2026-03-25T09:00:00",
  "endTime": "2026-03-25T17:00:00",
  "isPermanent": false,
  "visibility": "public"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "标记创建成功，请等待管理员审核",
  "data": {
    "id": 5
  }
}
```

#### 2. 获取我的标记列表

```http
GET /api/user/location-marks/my?page=1&size=10
Authorization: Bearer {token}
```

**响应示例：**
```json
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 10,
    "page": 1,
    "size": 10,
    "pages": 1
  }
}
```

#### 3. 获取校区的公开标记

```http
GET /api/user/location-marks/campus/{campusId}?markType=meeting_point
```

**响应示例：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "locationName": "图书馆门口",
      "markType": "meeting_point",
      "latitude": 35.308123,
      "longitude": 113.926234,
      "verificationStatus": "approved",
      "visibility": "public"
    }
  ]
}
```

#### 4. 获取标记详情

```http
GET /api/user/location-marks/{markId}
```

#### 5. 更新标记

```http
PUT /api/user/location-marks/{markId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "locationName": "新名称",
  "description": "新描述"
}
```

#### 6. 删除标记

```http
DELETE /api/user/location-marks/{markId}
Authorization: Bearer {token}
```

#### 7. 审核标记（管理员）

```http
PUT /api/user/location-marks/{markId}/verify?status=approved&comment=通过
Authorization: Bearer {token}
```

#### 8. 点赞标记

```http
POST /api/user/location-marks/{markId}/like
Authorization: Bearer {token}
```

---

## 前端组件使用

### LocationMarkModal 组件

**引入组件：**
```vue
<script setup>
import { ref } from 'vue';
import LocationMarkModal from '@/components/map/LocationMarkModal.vue';

const showMarkModal = ref(false);

const handleMarkSuccess = (data) => {
  console.log('标记创建成功:', data);
  // 刷新列表等操作
};
</script>

<template>
  <LocationMarkModal 
    v-model="showMarkModal" 
    :default-campus-id="1"
    @success="handleMarkSuccess"
  />
</template>
```

**组件事件：**
- `update:modelValue` - 模态框显示/隐藏
- `success` - 创建成功回调

### Map.vue 集成

地图页面已自动集成用户标记显示功能：
- 自动加载当前校区的公开标记
- 显示"标记我的位置"按钮
- 点击标记可查看基本信息

---

## 测试指南

### 使用 Postman 测试

1. 导入测试集合：
   ```
   tests/user-location-marks.postman_collection.json
   ```

2. 运行集合会自动：
   - 登录获取 Token
   - 依次测试所有接口
   - 自动保存 Token 到变量

### 手动测试步骤

详见：[QUICK_VERIFICATION_SCRIPT.md](./QUICK_VERIFICATION_SCRIPT.md)

### 数据库验证

```sql
-- 查看所有标记
SELECT * FROM user_location_mark;

-- 查看公开标记
SELECT id, location_name, mark_type, verification_status, visibility
FROM user_location_mark
WHERE verification_status = 'approved' 
  AND visibility = 'public';
```

---

## 后续开发计划

### P0 - 核心功能完善（必须完成）

#### 1. 集成真实地图 SDK ⭐⭐⭐
**工作内容：**
- 在 LocationMarkModal.vue 中集成百度/高德地图 SDK
- 实现点击地图获取经纬度
- 显示地图标记和标注

**参考代码：**
```javascript
// 在 LocationMarkModal.vue 的 handleMapClick 中
import { loadMapScript } from '@/utils/mapHelper';

const initMiniMap = async () => {
  await loadMapScript('baidu');
  const map = new window.BMap.Map(miniMapRef.value);
  map.centerAndZoom(new window.BMap.Point(113.926765, 35.307736), 15);
  
  map.addEventListener('click', (e) => {
    form.value.latitude = e.point.lat;
    form.value.longitude = e.point.lng;
    form.value.locationName = '已选择位置';
  });
};
```

#### 2. 完善身份认证 ⭐⭐⭐
**工作内容：**
- 创建 UserIdentity 实体类
- 在 Service 中查询用户真实身份
- 验证标记类型与身份的匹配关系

**参考代码：**
```java
// UserLocationMarkServiceImpl.java
@Autowired
private UserIdentityService identityService;

// 在 createMark() 方法中
UserIdentity identity = identityService.getIdentityByUserId(userId);
String identityType = identity.getIdentityType();

if ("merchant_shop".equals(dto.getMarkType()) && !"merchant".equals(identityType)) {
    return Result.error("只有认证商家才能标记店铺位置");
}
```

#### 3. 管理员审核界面 ⭐⭐
**工作内容：**
- 创建 AdminLocationMarks.vue 页面
- 显示待审核标记列表
- 实现审核通过/拒绝功能
- 添加批量审核功能

**路由配置：**
```javascript
{
  path: '/admin/location-marks',
  name: 'AdminLocationMarks',
  component: () => import('@/views/admin/AdminLocationMarks.vue'),
  meta: { requiresAuth: true, requiresAdmin: true }
}
```

### P1 - 增强功能（推荐完成）

#### 4. 我的标记管理页面 ⭐⭐
**工作内容：**
- 创建 MyLocationMarks.vue 页面
- 列表展示我的所有标记
- 支持编辑、删除操作
- 显示审核状态和历史

**路由配置：**
```javascript
{
  path: '/my-location-marks',
  name: 'MyLocationMarks',
  component: () => import('@/components/user/MyLocationMarks.vue'),
  meta: { requiresAuth: true }
}
```

#### 5. 标记详情页 ⭐⭐
**工作内容：**
- 创建 LocationMarkDetail.vue 页面
- 显示完整信息（图片画廊、描述等）
- 添加评论功能
- 分享功能

**路由配置：**
```javascript
{
  path: '/location-mark/:id',
  name: 'LocationMarkDetail',
  component: () => import('@/views/LocationMarkDetail.vue')
}
```

#### 6. 图片上传完善 ⭐
**工作内容：**
- 完善 ImageUploader 组件
- 后端添加图片上传接口
- 图片压缩和存储优化

### P2 - 高级功能（可选）

#### 7. 标记统计和分析
- 浏览次数统计
- 点赞数统计
- 用户行为分析图表

#### 8. 通知功能
- 审核结果通知
- 有人点赞通知
- 系统消息推送

#### 9. 基于位置的推荐
- 推荐附近的标记
- 热门标记排行
- 个性化推荐

---

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis-Plus
- MySQL 8.0
- JWT (io.jsonwebtoken.jwts)

### 前端
- Vue 3
- Vite
- Element Plus
- Vue Router 4
- Pinia

---

## 常见问题

### Q1: 创建标记时提示"用户未登录"
**A:** 检查 Authorization header 是否正确传递，格式应为 `Bearer {token}`

### Q2: 地图选点无法使用
**A:** 当前版本使用模拟坐标，需要集成真实地图 SDK（见 P0-1）

### Q3: 标记创建后看不到
**A:** 新创建的标记默认为"pending"状态，需要管理员审核通过后才会公开显示

### Q4: 如何区分不同身份的标记？
**A:** 通过 `mark_type` 字段区分：
- `meeting_point` - 约见地点（学生）
- `merchant_shop` - 店铺位置（商家）
- `organization_activity` - 活动地点（社团）

---

## 相关文档

- [功能详细说明](./USER_LOCATION_MARK_FEATURE_README.md)
- [实现总结](./USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md)
- [快速验证脚本](./QUICK_VERIFICATION_SCRIPT.md)

---

## 开发者

如有问题，请联系开发团队或提交 Issue。

**祝使用愉快！** 🎉
