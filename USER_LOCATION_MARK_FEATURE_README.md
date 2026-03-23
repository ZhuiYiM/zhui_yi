# 用户位置标记功能测试指南

## 功能说明

用户位置标记功能允许用户在校园地图上标记特定位置，包括：
- 🤝 **约见地点**：学生标记的见面集合点
- 🏪 **店铺位置**：商家标记的店铺位置
- 👥 **活动地点**：社团标记的活动举办地点

## 数据库表

### user_location_mark 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 ID |
| user_id | BIGINT | 标记者用户 ID |
| campus_id | INT | 校区 ID |
| location_name | VARCHAR(100) | 位置名称 |
| latitude | DECIMAL(10,8) | 纬度 |
| longitude | DECIMAL(11,8) | 经度 |
| mark_type | VARCHAR(50) | 标记类型 |
| verification_status | VARCHAR(20) | 审核状态：pending/approved/rejected |
| visibility | VARCHAR(20) | 可见性：public/friends/private |
| is_active | TINYINT(1) | 是否有效 |
| created_at | DATETIME | 创建时间 |

## API 接口列表

### 1. 创建位置标记
```http
POST /api/user/location-marks
Content-Type: application/json
Authorization: Bearer {token}

{
  "campusId": 1,
  "locationName": "图书馆门口",
  "latitude": 35.308123,
  "longitude": 113.926234,
  "markType": "meeting_point",
  "markCategory": "building",
  "description": "经常在图书馆门口集合",
  "visibility": "public",
  "isPermanent": false
}
```

### 2. 获取我的标记列表
```http
GET /api/user/location-marks/my?page=1&size=10
Authorization: Bearer {token}
```

### 3. 获取校区的公开标记
```http
GET /api/user/location-marks/campus/{campusId}?markType=meeting_point
```

### 4. 获取标记详情
```http
GET /api/user/location-marks/{markId}
```

### 5. 更新标记
```http
PUT /api/user/location-marks/{markId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "locationName": "新名称",
  "description": "新描述"
}
```

### 6. 删除标记
```http
DELETE /api/user/location-marks/{markId}
Authorization: Bearer {token}
```

### 7. 审核标记（管理员）
```http
PUT /api/user/location-marks/{markId}/verify?status=approved&comment=通过
Authorization: Bearer {token}
```

### 8. 点赞标记
```http
POST /api/user/location-marks/{markId}/like
Authorization: Bearer {token}
```

## 前端组件

### 1. LocationMarkModal.vue
位置标记模态框组件，用于创建新标记。

**使用示例：**
```vue
<LocationMarkModal 
  v-model="showMarkModal" 
  :default-campus-id="selectedCampus"
  @success="handleMarkSuccess"
/>
```

### 2. Map.vue 集成
地图页面已集成用户标记显示功能：
- 显示当前校区的公开标记
- 提供"标记我的位置"按钮
- 点击标记可查看基本信息

## 测试步骤

### 步骤 1：启动后端服务
```bash
mvn spring-boot:run
```

### 步骤 2：启动前端开发服务器
```bash
npm run dev
```

### 步骤 3：登录系统
访问 http://localhost:5173/login，使用以下测试账号登录：
- 学生账号：username: `student1`, password: `password123`
- 商家账号：username: `merchant1`, password: `password123`
- 社团账号：username: `org1`, password: `password123`

### 步骤 4：访问地图页面
访问 http://localhost:5173/map

### 步骤 5：创建位置标记
1. 点击"+ 标记我的位置"按钮
2. 填写表单信息：
   - 位置名称：如"图书馆门口"
   - 标记类型：选择约见地点/店铺位置/活动地点
   - 描述信息
   - 可见性设置
3. 点击"提交标记"

### 步骤 6：查看标记
- 创建成功后，标记会显示在地图页面的"用户标记位置"区域
- 待审核状态的标记仅自己可见
- 已审核通过的公开标记所有人可见

## 测试数据

数据库中已有 4 条测试数据：

```sql
-- 查询所有标记
SELECT * FROM user_location_mark;

-- 查询公开的已审核标记
SELECT * FROM user_location_mark 
WHERE verification_status = 'approved' 
  AND visibility = 'public' 
  AND is_active = 1;

-- 按类型统计
SELECT mark_type, COUNT(*) as count 
FROM user_location_mark 
GROUP BY mark_type;
```

## 预期结果

### ✅ 成功的标志
1. 能够成功创建位置标记
2. 创建后显示"请等待管理员审核"提示
3. 地图页面显示已审核通过的公开标记
4. 不同身份的用户可以创建对应类型的标记：
   - 学生 → 约见地点
   - 商家 → 店铺位置
   - 社团 → 活动地点

### ⚠️ 注意事项
1. 目前标记选点功能使用模拟坐标，实际项目需要集成真实地图 SDK
2. 身份验证逻辑简化处理，实际应该从数据库查询用户真实身份
3. 管理员审核功能需要完善权限检查

## 后续优化

### P0 - 核心功能
- [ ] 集成真实地图 SDK（百度/高德）实现点击选点
- [ ] 完善用户身份验证逻辑
- [ ] 实现管理员审核界面

### P1 - 增强功能
- [ ] 创建"我的标记"管理页面
- [ ] 支持标记图片上传
- [ ] 标记详情页展示

### P2 - 高级功能
- [ ] 标记点赞和评论
- [ ] 基于位置的推荐
- [ ] 标记分享功能

## 常见问题

### Q1: 创建标记时提示"只有认证商家才能标记店铺位置"
A: 当前简化处理，默认所有用户都是学生身份。需要根据实际业务完善身份认证逻辑。

### Q2: 地图选点无法使用
A: 当前版本使用模拟坐标，需要在 LocationMarkModal.vue 中集成真实地图 SDK。

### Q3: 标记创建后看不到
A: 新创建的标记默认为"pending"（待审核）状态，需要管理员审核通过后才会公开显示。
