# 用户位置标记可见性选项调整

## 📋 需求说明

### 原有可见性选项（已废弃）
- ❌ `public` - 公开（所有人可见）
- ❌ `friends` - 好友可见
- ❌ `private` - 仅自己可见

### 新可见性选项（2026-03-26 起）
- ✅ `public_active` - **公开（主动所有人可见）**
- ✅ `public_passive` - **公开（被动可见）**
- ✅ `private` - **仅自己可见**

---

## 🎯 可见性规则详解

### 1. public_active（主动所有人可见）

**可见范围：**
- ✅ 地图导引页面（所有用户可见）
- ✅ 用户对外展示页面（待完善）
- ✅ 个人中心页面（待完善）

**使用场景：**
- 学生主动分享的约见地点
- 商户公开的店铺位置
- 组织公开的活动地点
- 任何希望主动展示给他人的位置

**数据库查询：**
```sql
-- 地图导引页面显示的标记
SELECT * FROM user_location_mark 
WHERE visibility = 'public_active' 
  AND verification_status = 'approved' 
  AND is_active = 1;
```

---

### 2. public_passive（被动可见）

**可见范围：**
- ❌ 地图导引页面（**不显示**）
- ✅ 用户对外展示页面（待完善）
- ✅ 个人中心页面（待完善）

**使用场景：**
- 用户不希望主动展示在地图上，但可以在个人主页看到
- 相对私密但仍愿意对特定人公开的位置
- 临时性的位置标记

**数据库查询：**
```sql
-- 用户对外展示页面显示的标记
SELECT * FROM user_location_mark 
WHERE visibility IN ('public_active', 'public_passive') 
  AND verification_status = 'approved' 
  AND is_active = 1;
```

---

### 3. private（仅自己可见）

**可见范围：**
- ❌ 地图导引页面（**不显示**）
- ❌ 用户对外展示页面（**不显示**）
- ✅ 个人中心页面（待完善）

**使用场景：**
- 完全私密的位置
- 个人收藏的地点
- 未准备好公开的位置

**数据库查询：**
```sql
-- 个人中心显示的标记（当前用户的所有标记）
SELECT * FROM user_location_mark 
WHERE user_id = ?; -- 当前用户 ID
```

---

## 🔧 修改内容

### 1. 前端修改

**文件：** `src/components/map/LocationMarkModal.vue`

**变更：**
- 更新可见性下拉选项的文案和值
- 添加可见性说明提示
- 默认值改为 `public_active`

**代码示例：**
```vue
<el-select v-model="form.visibility" placeholder="请选择可见性">
  <el-option 
    label="公开（主动所有人可见）" 
    value="public_active" 
  />
  <el-option 
    label="公开（被动可见）" 
    value="public_passive" 
  />
  <el-option 
    label="仅自己可见" 
    value="private" 
  />
</el-select>
```

---

### 2. 后端修改

**文件：** `src/main/java/com/example/demo/entity/UserLocationMark.java`

**变更：**
- 更新 `visibility` 字段的注释

**代码：**
```java
/**
 * 可见性：public_active（主动所有人可见）/public_passive（被动可见）/private（仅自己可见）
 */
private String visibility;
```

---

### 3. 数据库修改

**文件：** `database/migration/database_user_location_mark.sql`

**变更：**
- 更新 `visibility` 字段的默认值和注释

**SQL：**
```sql
`visibility` VARCHAR(20) DEFAULT 'public_active' 
COMMENT '可见性：public_active（主动所有人可见）/public_passive（被动可见）/private（仅自己可见）',
```

---

### 4. 数据迁移

**文件：** `database/migration/database_user_location_mark_visibility_upgrade.sql`

**变更：**
- 将原有的 `public` 转换为 `public_active`
- 将原有的 `friends` 转换为 `public_passive`
- `private` 保持不变

**SQL：**
```sql
-- 更新现有数据
UPDATE user_location_mark 
SET visibility = 'public_active' 
WHERE visibility = 'public';

UPDATE user_location_mark 
SET visibility = 'public_passive' 
WHERE visibility = 'friends';
```

---

## 📊 数据查询规则

### 地图导引页面
```sql
-- 显示所有公开（主动）且审核通过的位置标记
SELECT 
  m.id,
  m.location_name,
  m.mark_type,
  m.latitude,
  m.longitude,
  m.description,
  u.username as creator_name
FROM user_location_mark m
JOIN users u ON m.user_id = u.id
WHERE m.visibility = 'public_active' 
  AND m.verification_status = 'approved'
  AND m.is_active = 1;
```

### 用户对外展示页面
```sql
-- 显示所有公开（主动 + 被动）且审核通过的位置标记
SELECT 
  m.id,
  m.location_name,
  m.mark_type,
  m.latitude,
  m.longitude,
  m.description,
  m.visibility
FROM user_location_mark m
WHERE m.user_id = ?
  AND m.visibility IN ('public_active', 'public_passive')
  AND m.verification_status = 'approved'
  AND m.is_active = 1;
```

### 个人中心页面
```sql
-- 显示当前用户的所有标记（所有可见性）
SELECT 
  m.id,
  m.location_name,
  m.mark_type,
  m.visibility,
  m.verification_status,
  m.is_active,
  m.created_at
FROM user_location_mark m
WHERE m.user_id = ?
ORDER BY m.created_at DESC;
```

---

## ✅ 测试清单

### 前端测试
- [ ] 打开"标记我的位置"弹窗
- [ ] 可见性下拉框显示三个选项
- [ ] 选项文案正确：公开（主动所有人可见）、公开（被动可见）、仅自己可见
- [ ] 默认选中"公开（主动所有人可见）"
- [ ] 选择不同选项后提交，数据库存储正确

### 后端测试
- [ ] 创建标记时，visibility 字段正确存储
- [ ] 查询地图导引数据时，仅返回 `public_active` 的标记
- [ ] 查询用户对外展示数据时，返回 `public_active` 和 `public_passive` 的标记
- [ ] 查询个人中心数据时，返回所有标记

### 数据迁移测试
- [ ] 执行 `database_user_location_mark_visibility_upgrade.sql`
- [ ] 验证原有 `public` 数据变为 `public_active`
- [ ] 验证原有 `friends` 数据变为 `public_passive`
- [ ] 验证 `private` 数据保持不变

---

## 📝 注意事项

1. **向后兼容**：原有的 `friends` 选项已映射为 `public_passive`，但实际功能略有不同（原 `friends` 是好友可见，新 `public_passive` 是被动可见）

2. **前端展示**：地图导引页面需要修改查询逻辑，仅显示 `visibility = 'public_active'` 的标记

3. **用户对外展示页**：该功能待完善，需要确保查询时包含 `public_active` 和 `public_passive` 两种可见性

4. **个人中心**：该功能待完善，需要显示所有可见性的标记

5. **审核流程**：无论哪种可见性，都需要经过审核流程（`verification_status`）

---

##  相关文件

### 前端
- `src/components/map/LocationMarkModal.vue` - 位置标记表单

### 后端
- `src/main/java/com/example/demo/entity/UserLocationMark.java` - 位置标记实体
- `src/main/java/com/example/demo/mapper/UserLocationMarkMapper.java` - 数据访问层
- `src/main/java/com/example/demo/service/UserLocationMarkService.java` - 业务逻辑层
- `src/main/java/com/example/demo/controller/UserLocationMarkController.java` - 控制器

### 数据库
- `database/migration/database_user_location_mark.sql` - 初始建表脚本
- `database/migration/database_user_location_mark_visibility_upgrade.sql` - 可见性升级脚本

---

## 🎯 后续优化建议

1. **用户对外展示页面**：完善用户主页的位置展示功能
2. **个人中心**：完善个人中心的位置管理功能
3. **隐私控制**：考虑是否需要更细粒度的隐私控制（如指定好友可见）
4. **批量管理**：支持批量修改可见性
5. **可见性说明**：在表单中增加更详细的可见性说明或帮助文档

---

**更新时间：** 2026-03-26  
**版本：** v1.0
