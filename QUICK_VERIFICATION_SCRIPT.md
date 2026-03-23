# 用户位置标记功能 - 快速验证脚本

## 1. 验证数据库表创建成功

```bash
mysql -u root -p123456 campus_db -e "DESC user_location_mark;"
mysql -u root -p123456 campus_db -e "SELECT COUNT(*) as total FROM user_location_mark;"
```

预期输出：
- 显示表结构（30 个字段）
- total: 4（4 条测试数据）

---

## 2. 启动后端服务

```bash
mvn spring-boot:run
```

等待看到以下日志：
```
Started DemoApplication in X.XXX seconds
```

---

## 3. 测试 API 接口（使用 curl）

### 3.1 登录获取 Token

```powershell
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/user/login" -Method Post -ContentType "application/json" -Body '{"username":"student1","password":"password123"}'
$token = $loginResponse.data.token
Write-Host "Token: $token"
```

### 3.2 创建位置标记

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$body = @{
    campusId = 1
    locationName = "测试约见地点"
    latitude = 35.308000
    longitude = 113.926000
    markType = "meeting_point"
    markCategory = "building"
    description = "这是一个测试的约见地点"
    visibility = "public"
    isPermanent = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks" -Method Post -Headers $headers -Body $body
```

预期响应：
```json
{
  "code": 200,
  "message": "标记创建成功，请等待管理员审核",
  "data": {
    "id": 5,
    "message": "..."
  }
}
```

### 3.3 获取我的标记列表

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/my?page=1&size=10" -Headers $headers
$response | ConvertTo-Json
```

预期响应：包含刚才创建的标记

### 3.4 获取校区的公开标记

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/campus/1"
$response | ConvertTo-Json
```

预期响应：包含所有已审核通过的公开标记

### 3.5 获取标记详情

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/1"
$response | ConvertTo-Json
```

### 3.6 更新标记

```powershell
$updateBody = @{
    locationName = "更新后的地点名称"
    description = "更新后的描述"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/1" -Method Put -Headers $headers -Body $updateBody
```

### 3.7 点赞标记

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/1/like" -Method Post -Headers $headers
```

预期响应：
```json
{
  "code": 200,
  "message": "点赞成功"
}
```

### 3.8 删除标记

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/user/location-marks/1" -Method Delete -Headers $headers
```

预期响应：
```json
{
  "code": 200,
  "message": "标记删除成功"
}
```

---

## 4. 前端测试步骤

### 4.1 启动前端开发服务器

```bash
npm run dev
```

访问：http://localhost:5173

### 4.2 登录系统

使用任意账号登录：
- username: `student1`
- password: `password123`

### 4.3 访问地图页面

导航到：http://localhost:5173/map

### 4.4 测试标记创建

1. 查看页面上是否显示"📍 用户标记位置"区域
2. 点击"+ 标记我的位置"按钮
3. 填写表单：
   - 位置名称：图书馆门口
   - 标记类型：选择"约见地点"
   - 地点分类：建筑物
   - 描述：测试描述
   - 可见性：公开
4. 点击"提交标记"
5. 查看是否显示成功提示

### 4.5 查看标记展示

- 检查页面上是否显示新创建的标记卡片
- 卡片应包含：图标、名称、类型、状态标签

---

## 5. 数据库验证

```sql
-- 查看所有标记
SELECT * FROM user_location_mark ORDER BY created_at DESC;

-- 查看公开的已审核标记
SELECT id, location_name, mark_type, verification_status, visibility
FROM user_location_mark
WHERE verification_status = 'approved' 
  AND visibility = 'public' 
  AND is_active = 1;

-- 按类型统计
SELECT mark_type, COUNT(*) as count 
FROM user_location_mark 
GROUP BY mark_type;

-- 查看某个用户的标记
SELECT * FROM user_location_mark WHERE user_id = 2;
```

---

## 6. 常见问题排查

### 问题 1: Token 获取失败
**原因**：用户名或密码错误  
**解决**：检查数据库中 users 表是否有对应记录

### 问题 2: 创建标记时提示"用户未登录"
**原因**：Token 未正确传递  
**解决**：检查 Authorization header 格式是否为 "Bearer {token}"

### 问题 3: 前端看不到标记区域
**原因**：userLocationMarks 数组为空  
**解决**：检查 fetchUserLocationMarks() 是否正确调用

### 问题 4: 编译错误
**原因**：缺少依赖或代码语法错误  
**解决**：
```bash
mvn clean compile -DskipTests
```
查看具体错误信息

---

## 7. 性能测试（可选）

### 批量创建测试数据

```sql
-- 插入 100 条测试数据
DELIMITER $$

CREATE PROCEDURE insert_test_marks()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 100 DO
    INSERT INTO user_location_mark (
      user_id, campus_id, location_name, latitude, longitude,
      mark_type, mark_category, user_identity_type,
      description, verification_status, visibility
    ) VALUES (
      2, 1, CONCAT('测试地点_', i),
      35.307000 + (i * 0.0001), 113.926000 + (i * 0.0001),
      'meeting_point', 'building', 'student',
      CONCAT('这是第', i, '个测试地点'),
      CASE WHEN i % 2 = 0 THEN 'approved' ELSE 'pending' END,
      CASE WHEN i % 3 = 0 THEN 'private' ELSE 'public' END
    );
    SET i = i + 1;
  END WHILE;
END$$

DELIMITER ;

CALL insert_test_marks();
```

### 测试分页查询

使用 Postman 或 curl 测试分页接口：
```
GET /api/user/location-marks/my?page=1&size=20
GET /api/user/location-marks/my?page=2&size=20
```

---

## 8. 验收标准

✅ **数据库**
- [ ] user_location_mark 表创建成功
- [ ] 至少有 4 条测试数据
- [ ] 索引创建成功

✅ **后端 API**
- [ ] 能够成功登录获取 Token
- [ ] 能够创建位置标记
- [ ] 能够获取我的标记列表
- [ ] 能够获取校区公开标记
- [ ] 能够更新自己的标记
- [ ] 能够删除自己的标记
- [ ] 能够点赞标记

✅ **前端界面**
- [ ] 地图页面显示用户标记区域
- [ ] 能够打开标记创建模态框
- [ ] 表单验证正常工作
- [ ] 创建成功后显示成功提示
- [ ] 标记卡片正常显示

✅ **集成测试**
- [ ] 创建标记后能在地图页面看到
- [ ] 不同状态的标记显示正确的标签
- [ ] 点击标记有响应

---

## 9. 下一步行动

完成基础验证后，建议继续实现：

1. **地图 SDK 集成**（优先级最高）
   - 在 LocationMarkModal.vue 中集成百度/高德地图
   - 实现真实的点击选点功能

2. **身份认证完善**
   - 创建 UserIdentity 实体类
   - 在 Service 中查询用户真实身份
   - 验证标记类型与身份的匹配

3. **管理员审核界面**
   - 创建 AdminLocationMarks.vue
   - 实现审核通过/拒绝功能

祝测试顺利！ 🎉
