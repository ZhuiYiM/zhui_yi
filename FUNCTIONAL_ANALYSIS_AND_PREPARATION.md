# 功能完善准备报告

## 📋 概述

本报告基于对前后端代码和数据库结构的全面分析，为接下来两个待完善功能的实现提供详细的技术准备。

**数据库配置**：
- 数据库名：`campus_db`
- 用户名：`root`
- 密码：`123456`
- 端口：`3306`

---

## 一、用户身份及相关认证、权限功能

### 1.1 现有基础

#### 数据库表结构

**users 表**（用户主表）
```sql
核心字段:
- id, username, password, email, phone_number
- avatar_url, real_name, student_id
- status, is_verified, is_real_name_verified
- role (varchar(20), default 'user')  # 角色：admin, user
- isAdmin (tinyint, default 0)        # 是否管理员
- isMerchant (tinyint, default 0)     # 是否商户
- isOrganization (tinyint, default 0) # 是否团体
- gender, birth_date, college, major
- bio, hobbies (JSON)
- 隐私控制字段：phone_visibility, email_visibility 等
```

**user_identity 表**（用户身份表）
```sql
- id, user_id (bigint, unique)
- identity_type (varchar(50)): student, merchant, organization 等
- identity_name (varchar(100)): 显示名称
- verified (tinyint): 是否已认证
- verified_at, expires_at
- extra_info (JSON): 额外信息
- created_at, updated_at
```

**user_verifications 表**（认证申请表）
```sql
- id, userId, verificationType
- status (pending/approved/rejected)
- studentId, realName, idCard, college
- rejectionReason
- submittedAt, reviewedAt, reviewerId
```

**admin_user 表**（管理员表）
```sql
- id, username, password (BCrypt 加密)
- real_name, role_id
- status, created_at, updated_at
```

**admin_role 表**（管理员角色表）
```sql
- id, role_name, role_code
- permissions (JSON 数组，如 ["*"] 或 ["user:*", "content:*"])
- description, status
```

#### 实体类

**User.java**
- 包含完整的用户信息字段
- 包含 role, isAdmin, isMerchant, isOrganization 身份标识字段

**UserIdentity.java** ❌ **不存在**
- 需要创建该实体类对应 `user_identity` 表

**UserVerification.java** ✅
- 已有完整的认证申请实体

**AdminUser.java, AdminRole.java** ✅
- 已有完整的管理员和角色实体

#### Service 层

**UserService.java/impl** ✅
已实现方法：
- `login()`, `register()` - 登录注册
- `getUserProfile()`, `updateUserProfile()` - 用户资料管理
- `verifyStudent()` - 学生身份验证
- `getAuthStatus()` - 获取认证状态
- `applyIdentityVerification()` - 申请身份认证
- `applyRealNameVerification()` - 申请实名认证
- `getVerificationApplications()` - 获取认证申请
- `cancelVerificationApplication()` - 取消认证申请
- `reportUser()` - 举报用户

**缺少的方法**：
- 商户身份认证
- 团体身份认证
- 身份权限验证工具方法

#### Controller 层

**UserController.java** ✅
- `/api/user/login` - 用户登录
- `/api/user/register` - 用户注册
- `/api/user/profile` - 获取/更新个人资料
- `/api/user/account/*` - 账号管理相关
- `/api/user/verification/*` - 认证相关接口

**缺失的接口**：
- 商户认证专门接口
- 团体认证专门接口
- 身份权限检查接口

#### 前端组件

**认证相关组件** ✅
- `/src/components/auth/` 目录下有：
  - `PhoneLoginForm.vue` - 手机号登录
  - `UsernameLoginForm.vue` - 用户名登录
  - `RegisterForm.vue` - 注册表单
  - `PasswordRecoveryForm.vue` - 密码找回

**个人中心** ✅
- `Personalcenter.vue` - 个人中心主页
- `AccountManagement.vue` - 账号管理
- `Personalinformation.vue` - 个人信息编辑

**身份认证 UI** ⚠️
- 前端有身份选择逻辑（student/organization/merchant）
- 但缺少完整的认证流程 UI

#### 安全机制

**JwtUtil.java** ✅
- Token 生成和验证
- 支持 userId 和 username 双重验证
- Token 刷新机制
- 时钟偏差容忍

**TokenInterceptor.java** ✅
- 拦截需要认证的 API
- 放行公开接口（/auth/, /number/, /upload/）
- 排除静态资源和前端路由

**问题**：
1. ❌ **未将用户角色/身份信息嵌入 Token**
   - Token 只包含 userId 和 username
   - 每次请求需要查询数据库获取角色信息
   - 无法实现基于角色的权限控制

2. ❌ **缺少 RBAC 权限注解**
   - 没有类似 `@PreAuthorize("hasRole('ADMIN')")` 的注解
   - 权限检查分散在各业务方法中

3. ❌ **user_identity 表未被充分利用**
   - 数据库中存储了身份类型
   - 但代码中很少使用这个表

### 1.2 需要完善的功能

#### 1.2.1 数据库层面

**✅ 已有的表**：
- users (用户主表)
- user_identity (用户身份表)
- user_verifications (认证申请表)
- admin_user (管理员表)
- admin_role (角色表)

**❌ 缺失的表**：
1. **user_permission** (用户权限表) - 可选
   ```sql
   - id, user_id, permission_code
   - resource_type, resource_id
   - expires_at, created_at
   ```

2. **role_permission** (角色权限关联表) - 如果使用 RBAC
   ```sql
   - id, role_id, permission_code
   - created_at
   ```

3. **permission** (权限定义表) - 如果使用 RBAC
   ```sql
   - id, permission_code, permission_name
   - resource_type, description
   ```

#### 1.2.2 后端代码

**需要创建的实体类**：
1. ✅ **UserIdentity.java** - 对应 user_identity 表
2. ❌ **UserPermission.java** - 如果需要细粒度权限
3. ❌ **RolePermission.java** - 如果需要 RBAC

**需要增强的 Service**：
1. **UserIdentityService** - 新增
   - `getIdentityByUserId(Integer userId)`
   - `updateIdentity(Integer userId, String identityType)`
   - `verifyIdentity(Integer userId)`
   - `checkPermission(Integer userId, String permissionCode)`

2. **UserService** - 增强
   - `getMerchantVerificationStatus(Integer userId)`
   - `getOrganizationVerificationStatus(Integer userId)`
   - `applyMerchantVerification(...)` 
   - `applyOrganizationVerification(...)`

**需要增强的 Controller**：
1. **UserIdentityController** - 新增
   ```java
   GET /api/user/identity - 获取用户身份
   PUT /api/user/identity - 更新用户身份
   POST /api/user/identity/verify - 提交身份认证材料
   GET /api/user/permissions - 获取用户权限列表
   ```

2. **UserController** - 增强
   - 增加商户认证接口
   - 增加团体认证接口

#### 1.2.3 前端代码

**需要创建的组件**：
1. **MerchantVerificationForm.vue** - 商户认证表单
   - 营业执照上传
   - 店铺信息填写
   - 联系人信息

2. **OrganizationVerificationForm.vue** - 团体认证表单
   - 组织证明文件
   - 负责人信息
   - 组织简介

3. **IdentityBadge.vue** - 身份标识徽章
   - 根据用户身份显示不同图标
   - 学生👨‍🎓、商户🏪、团体👥、管理员🛡️

**需要修改的组件**：
1. **Personalcenter.vue** - 增加身份认证入口
2. **AccountManagement.vue** - 增加身份管理页面

#### 1.2.4 认证流程优化

**当前问题**：
1. Token 不包含角色信息，每次查询数据库
2. 身份认证和应用场景脱节
3. 缺少权限注解，权限检查分散

**建议方案**：

**方案 A：轻量级改进**（推荐短期实施）
```java
// 1. 在 Token 中加入角色信息
jwtUtil.generateToken(username, userId, role, identities);

// 2. 添加权限注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value(); // 允许的角色列表
}

// 3. 创建切面进行权限检查
@Aspect
@Component
public class PermissionAspect {
    @Around("@annotation(requireRole)")
    public Object checkPermission(ProceedingJoinPoint pjp, RequireRole requireRole) {
        // 从 Token 中提取角色并检查
    }
}
```

**方案 B：完整 RBAC**（推荐长期实施）
- 引入 Spring Security
- 配置完整的用户 - 角色 - 权限体系
- 使用方法级安全注解

### 1.3 实现路线图

**阶段一：基础完善**（1-2 天）
1. ✅ 创建 `UserIdentity` 实体类
2. ✅ 创建 `UserIdentityMapper` 和 `Service`
3. ✅ 实现基本的身份查询和更新
4. ✅ 在 Token 中加入角色信息

**阶段二：商户/团体认证**（2-3 天）
1. ✅ 实现商户认证流程
2. ✅ 实现团体认证流程
3. ✅ 创建前端认证表单组件
4. ✅ 增加身份标识徽章展示

**阶段三：权限控制**（3-5 天）
1. ✅ 实现权限注解和切面
2. ✅ 在关键接口添加权限检查
3. ✅ 实现基于身份的可见性控制
4. ✅ 编写权限测试用例

---

## 二、地图位置标记权限功能

### 2.1 现有基础

#### 数据库表结构

**tag_level_3 表**（三级标签 - 地点）
```sql
- id, code (unique), name
- location_type (area/building/facility)
- latitude (decimal(10,8)), longitude (decimal(11,8))
- address, icon, color
- is_active, sort_order
- created_at, updated_at

当前数据示例：
- library (图书馆) - building
- teaching_building (教学楼) - building
- dormitory (宿舍区) - area
- cafeteria (食堂) - facility
- playground (操场) - facility
```

**locations 表**（地点表）
```sql
- id, campus_id
- name, description, category
- latitude, longitude, icon
- image_url, opening_hours, contact_info
- facilities (JSON), is_popular, view_count
- sort_order, created_at, updated_at

说明：
- 这是系统预设的固定地点
- 已经有 80+ 个地点数据
```

**campuses 表**（校区表）
```sql
- id, name, code
- description, address
- center_latitude, center_longitude
- zoom_level, map_config (JSON)
- is_active, sort_order
```

**⚠️ 缺失的表**：
1. **user_location_mark** (用户位置标记表)
   ```sql
   - id, user_id (标记者)
   - location_name (位置名称)
   - latitude, longitude (精确坐标)
   - location_type (shop/activity/meeting/other)
   - description (描述)
   - related_identity (关联身份：merchant/organization/student)
   - verification_status (审核状态：pending/approved/rejected)
   - visibility (public/friends/private)
   - start_time, end_time (有效期)
   - view_count, like_count
   - created_at, updated_at, expires_at
   ```

2. **location_mark_application** (位置标记申请表)
   ```sql
   - id, user_id, campus_id
   - location_name, latitude, longitude
   - mark_type (merchant_shop/organization_activity/meeting_point)
   - reason, proof_images (JSON)
   - status, reviewer_id, review_comment
   - submitted_at, reviewed_at
   ```

#### 后端代码

**实体类** ✅
- `TagLevel3.java` - 三级标签地点实体
- `Location.java` - 地点实体
- `Campus.java` - 校区实体

**Service 层** ✅
- `CampusService.java/impl`
  - `getCampuses()` - 获取校区列表
  - `getLocationsByCampusId()` - 获取校区地点
  - `searchLocations()` - 搜索地点
  - `getPopularLocationsByCampusId()` - 获取热门地点
  - `getMapConfig()` - 获取地图配置

**❌ 缺失的 Service**：
- `UserLocationMarkService` - 用户位置标记管理
  - `createMark()` - 创建标记
  - `getMarksByUserId()` - 获取用户的标记
  - `getMarksByCampus()` - 获取校区的标记
  - `verifyMark()` - 审核标记
  - `deleteMark()` - 删除标记

**Controller 层** ✅
- `CampusController.java`
  - `GET /api/campuses` - 获取校区列表
  - `GET /api/campuses/{campusId}/locations` - 获取地点列表
  - `GET /api/campuses/locations/search` - 搜索地点
  - `GET /api/campuses/popular-locations` - 获取热门地点
  - `GET /api/campuses/map-config` - 获取地图配置

**❌ 缺失的 Controller**：
- `UserLocationMarkController` - 用户位置标记管理
  ```java
  POST /api/user/location-marks - 创建位置标记
  GET /api/user/location-marks - 获取我的标记列表
  GET /api/campuses/{campusId}/user-marks - 获取校区的用户标记
  PUT /api/user/location-marks/{id} - 更新标记
  DELETE /api/user/location-marks/{id} - 删除标记
  ```

#### 前端代码

**地图组件** ✅
- `Map.vue` - 主地图页面
  - 校区选择
  - 地点搜索
  - 热门地点展示
  - 点击跳转到详情

- `LocationDetail.vue` - 地点详情页
  - 地点信息展示
  - 导航功能
  - 分享功能
  - 相关地点推荐

**❌ 缺失的组件**：
1. **LocationMarkModal.vue** - 位置标记模态框
   - 地图选点
   - 标记类型选择
   - 信息填写
   - 证明材料上传

2. **MyLocationMarks.vue** - 我的位置标记管理
   - 标记列表
   - 编辑/删除
   - 审核状态查看

3. **UserMarkLayer.vue** - 用户标记图层
   - 在地图上显示用户标记
   - 区分不同类型标记的图标
   - 点击查看详情

#### 标签系统

**TagLevel3 现状** ✅
- 当前三级标签是**系统预设**的地点类型
- 包含：图书馆、教学楼、宿舍、食堂、操场等
- 由管理员统一管理
- 用户只能选择，不能创建

**❌ 问题**：
- 现有的 tag_level_3 不适合做用户自定义位置标记
- 需要新建表来存储用户创建的位置标记

### 2.2 需求分析

#### 应用场景

1. **商户标记店铺位置** 🏪
   - 已通过身份认证的商户
   - 在地图上标记自己的店铺位置
   - 提交营业执照等证明材料
   - 审核后对其他用户可见
   - 显示特殊商户图标

2. **社团标记活动地点** 👥
   - 已通过认证的团体/社团
   - 标记临时或固定的活动地点
   - 设置活动时间范围
   - 可选择公开或仅成员可见
   - 显示团体活动图标

3. **学生约见地点** 🤝
   - 所有学生用户
   - 标记临时约见地点
   - 可设置私密性（公开/好友可见）
   - 可设置有效时间
   - 显示约见图标

#### 权限设计

**谁能标记位置？**
- ✅ 学生用户：可以标记约见地点（需审核）
- ✅ 商户用户：可以标记店铺位置（需营业执照）
- ✅ 团体用户：可以标记活动地点（需组织证明）

**审核机制**：
- 学生标记 → 需要审核（防止恶意标记）
- 商户标记 → 需要审核 + 营业执照
- 团体标记 → 需要审核 + 组织证明

**可见性控制**：
- 公开标记：所有人可见
- 好友标记：仅好友可见
- 私密标记：仅自己可见

### 2.3 实现方案

#### 2.3.1 数据库设计

**方案 A：独立表**（推荐）

```sql
CREATE TABLE `user_location_mark` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `campus_id` INT NOT NULL,
  
  -- 位置信息
  `location_name` VARCHAR(100) NOT NULL,
  `latitude` DECIMAL(10,8) NOT NULL,
  `longitude` DECIMAL(11,8) NOT NULL,
  `address_detail` VARCHAR(255),
  
  -- 标记类型
  `mark_type` VARCHAR(50) NOT NULL COMMENT 'merchant_shop/organization_activity/meeting_point',
  `mark_category` VARCHAR(50) COMMENT '与 tag_level_3.location_type 对应',
  
  -- 关联身份
  `user_identity_type` VARCHAR(50) COMMENT 'student/merchant/organization',
  `related_entity_id` BIGINT COMMENT '关联的商户 ID 或组织 ID',
  
  -- 描述信息
  `description` TEXT,
  `contact_info` VARCHAR(100),
  `images` JSON COMMENT '图片 URL 数组',
  
  -- 时间信息
  `start_time` DATETIME,
  `end_time` DATETIME,
  `is_permanent` TINYINT(1) DEFAULT 0 COMMENT '是否永久标记',
  
  -- 审核状态
  `verification_status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `verified_at` DATETIME,
  `reviewer_id` BIGINT,
  `review_comment` TEXT,
  
  -- 可见性
  `visibility` VARCHAR(20) DEFAULT 'public' COMMENT 'public/friends/private',
  
  -- 统计数据
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  
  -- 状态控制
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expires_at` DATETIME,
  
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_campus_id` (`campus_id`),
  INDEX `idx_mark_type` (`mark_type`),
  INDEX `idx_verification_status` (`verification_status`),
  INDEX `idx_visibility` (`visibility`),
  INDEX `idx_location` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**方案 B：扩展现有表**（不推荐）
- 在 tag_level_3 表中添加 user_id 字段
- 缺点：会混淆系统标签和用户标签
- 不利于权限控制和审核

#### 2.3.2 后端实现

**创建实体类**：
```java
// UserLocationMark.java
@Data
@TableName("user_location_mark")
public class UserLocationMark {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer campusId;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String markType; // merchant_shop/organization_activity/meeting_point
    private String userIdentityType;
    private String verificationStatus;
    private String visibility;
    // ... 其他字段
}
```

**创建 Service**：
```java
@Service
public class UserLocationMarkService {
    
    // 创建标记
    public Result createMark(UserLocationMarkDTO dto, HttpServletRequest request) {
        // 1. 验证用户身份
        // 2. 检查是否在允许范围内
        // 3. 保存标记（默认待审核）
        // 4. 通知管理员审核
    }
    
    // 获取我的标记
    public Result getMyMarks(Integer userId, Integer page, Integer size) {
        // 分页查询用户的标记
    }
    
    // 获取校区的公开标记
    public Result getCampusMarks(Integer campusId, String markType) {
        // 只返回 verification_status='approved' AND visibility='public' 的标记
    }
    
    // 审核标记（管理员）
    public Result verifyMark(Long markId, String status, String comment, Integer reviewerId) {
        // 更新审核状态
        // 通知用户
    }
}
```

**创建 Controller**：
```java
@RestController
@RequestMapping("/api/user/location-marks")
public class UserLocationMarkController {
    
    @PostMapping
    public Result createMark(@RequestBody UserLocationMarkDTO dto, 
                             HttpServletRequest request) {
        return locationMarkService.createMark(dto, request);
    }
    
    @GetMapping("/my")
    public Result getMyMarks(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size,
                             HttpServletRequest request) {
        return locationMarkService.getMyMarks(userId, page, size);
    }
    
    @GetMapping("/campus/{campusId}")
    public Result getCampusMarks(@PathVariable Integer campusId,
                                 @RequestParam(required = false) String markType) {
        return locationMarkService.getCampusMarks(campusId, markType);
    }
}
```

#### 2.3.3 前端实现

**组件结构**：
```
src/components/map/
├── Map.vue                    # 原有地图页
├── LocationDetail.vue         # 原有地点详情
├── LocationMarkModal.vue      # 新增：标记模态框
├── MyLocationMarks.vue        # 新增：我的标记管理
└── UserMarkLayer.vue          # 新增：用户标记图层组件
```

**LocationMarkModal.vue 核心功能**：
```vue
<template>
  <el-dialog title="标记位置">
    <!-- 1. 地图选点 -->
    <div class="map-picker">
      <InteractiveMap 
        @location-selected="onLocationSelected"
      />
    </div>
    
    <!-- 2. 标记类型 -->
    <el-radio-group v-model="form.markType">
      <el-radio label="merchant_shop">🏪 店铺位置</el-radio>
      <el-radio label="organization_activity">👥 活动地点</el-radio>
      <el-radio label="meeting_point">🤝 约见地点</el-radio>
    </el-radio-group>
    
    <!-- 3. 信息填写 -->
    <el-form :model="form">
      <el-input v-model="form.locationName" placeholder="位置名称" />
      <el-input v-model="form.description" type="textarea" />
      <el-input v-model="form.contactInfo" placeholder="联系方式（可选）" />
    </el-form>
    
    <!-- 4. 证明材料上传 -->
    <ImageUploader 
      v-if="requiresProof()"
      v-model="form.proofImages"
      tip="请上传营业执照或组织证明"
    />
    
    <!-- 5. 可见性设置 -->
    <el-select v-model="form.visibility">
      <el-option label="公开" value="public" />
      <el-option label="好友可见" value="friends" />
      <el-option label="仅自己可见" value="private" />
    </el-select>
    
    <!-- 6. 有效期设置 -->
    <el-date-picker 
      v-model="form.timeRange"
      type="datetimerange"
      placeholder="选择有效时间范围"
    />
  </el-dialog>
</template>
```

**地图集成**：
```vue
<!-- 在 Map.vue 中添加用户标记图层 -->
<template>
  <div class="map-container">
    <!-- 原有地点图层 -->
    <div class="location-layer">
      <div v-for="loc in locations" :key="loc.id" class="location-marker">
        {{ loc.icon }} {{ loc.name }}
      </div>
    </div>
    
    <!-- 新增用户标记图层 -->
    <div class="user-mark-layer">
      <div 
        v-for="mark in userMarks" 
        :key="mark.id" 
        class="mark-marker"
        :class="mark.markType"
      >
        <span class="mark-icon">{{ getMarkIcon(mark.markType) }}</span>
        <span class="mark-name">{{ mark.locationName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
// 加载用户标记
const userMarks = ref([]);
const loadUserMarks = async () => {
  const res = await campusAPI.getCampusUserMarks(selectedCampus.value);
  userMarks.value = res.data;
};
</script>
```

### 2.4 实现路线图

**阶段一：数据库和基础架构**（1-2 天）
1. ✅ 创建 `user_location_mark` 表
2. ✅ 创建 `UserLocationMark` 实体类
3. ✅ 创建 Mapper 和 Service
4. ✅ 实现基础的 CRUD 接口

**阶段二：标记功能**（2-3 天）
1. ✅ 实现标记创建接口
2. ✅ 实现标记审核接口
3. ✅ 创建 LocationMarkModal 组件
4. ✅ 集成地图选点功能

**阶段三：展示和管理**（2-3 天）
1. ✅ 在地图页显示用户标记
2. ✅ 创建 MyLocationMarks 管理页
3. ✅ 实现标记筛选和搜索
4. ✅ 添加审核状态展示

**阶段四：权限和优化**（1-2 天）
1. ✅ 实现基于身份的标记权限
2. ✅ 实现可见性控制
3. ✅ 添加标记统计功能
4. ✅ 性能优化（缓存、索引）

---

## 三、两个功能的关联性

### 3.1 身份认证是位置标记的前提

**依赖关系**：
```
用户登录 
  ↓
身份认证（学生/商户/团体）
  ↓
获得相应权限
  ↓
创建对应类型的位置标记
```

**具体场景**：
1. 用户必须先完成**商户认证**，才能标记店铺位置
2. 用户必须先完成**团体认证**，才能标记活动地点
3. 学生身份可以直接标记约见地点（但需要审核）

### 3.2 统一的用户身份体系

**建议的数据流**：
```javascript
// 前端存储用户完整信息
const userInfo = {
  id: 1,
  username: '张三',
  role: 'user',
  identities: [
    { type: 'student', verified: true },
    { type: 'merchant', verified: false }
  ],
  permissions: ['create_meeting_mark', 'view_public_marks']
};

// 创建位置标记时检查身份
const canCreateMark = (markType) => {
  if (markType === 'merchant_shop') {
    return userInfo.identities.find(i => i.type === 'merchant' && i.verified);
  }
  if (markType === 'organization_activity') {
    return userInfo.identities.find(i => i.type === 'organization' && i.verified);
  }
  if (markType === 'meeting_point') {
    return userInfo.identities.find(i => i.type === 'student');
  }
  return false;
};
```

### 3.3 统一的权限控制框架

**建议实现的注解**：
```java
// 需要商户身份
@RequireIdentity("merchant")
@PostMapping("/marks/shop")
public Result createShopMark(...) {}

// 需要团体身份
@RequireIdentity("organization")
@PostMapping("/marks/activity")
public Result createActivityMark(...) {}

// 只需要是学生
@RequireIdentity("student")
@PostMapping("/marks/meeting")
public Result createMeetingMark(...) {}
```

---

## 四、技术栈总结

### 4.1 后端技术

- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **认证**: JWT (io.jsonwebtoken.jwts)
- **加密**: BCrypt (管理员密码)
- **验证**: Hibernate Validator

### 4.2 前端技术

- **框架**: Vue 3 + Vite
- **UI 库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP**: Fetch API
- **地图**: 百度地图/高德地图/腾讯地图（多服务商支持）

### 4.3 开发规范

- **命名规范**: 驼峰命名（camelCase）
- **注释语言**: 英文（特殊业务逻辑用中文）
- **日期格式**: yyyy-MM-dd HH:mm:ss
- **API 风格**: RESTful
- **响应格式**: `{ code, data, message }`

---

## 五、风险和挑战

### 5.1 用户身份认证

**风险**：
1. ⚠️ 商户/团体认证需要线下审核，流程复杂
2. ⚠️ 身份证明材料的存储和隐私保护
3. ⚠️ 身份过期后的权限回收

**应对方案**：
- 设计清晰的审核流程 UI
- 敏感信息加密存储
- Token 中增加过期时间检查

### 5.2 位置标记

**风险**：
1. ⚠️ 恶意标记和虚假信息
2. ⚠️ 位置重叠和冲突
3. ⚠️ 大量标记影响地图性能

**应对方案**：
- 严格的审核机制
- 同一位置标记数量限制
- 前端分层次渲染（只显示可视区域内的标记）

### 5.3 数据一致性

**风险**：
1. ⚠️ 用户注销后标记的处理
2. ⚠️ 身份变更后历史标记的处理
3. ⚠️ 地点删除后的关联清理

**应对方案**：
- 设置软删除标志
- 级联更新或清空
- 定期清理过期数据

---

## 六、下一步行动清单

### 优先级 P0（必须完成）

**身份认证功能**：
- [ ] 创建 `UserIdentity` 实体类和 Service
- [ ] 实现商户认证流程
- [ ] 实现团体认证流程
- [ ] 在 Token 中加入身份信息
- [ ] 创建前端认证表单组件

**位置标记功能**：
- [ ] 创建 `user_location_mark` 表
- [ ] 创建 `UserLocationMark` 实体类和 Service
- [ ] 实现标记创建和审核接口
- [ ] 创建 LocationMarkModal 组件
- [ ] 在地图页显示用户标记

### 优先级 P1（重要）

- [ ] 实现权限注解和切面
- [ ] 添加身份标识徽章展示
- [ ] 实现标记可见性控制
- [ ] 创建我的标记管理页面
- [ ] 添加标记搜索和筛选

### 优先级 P2（优化）

- [ ] 实现 RBAC 权限体系
- [ ] 添加标记统计和热力图
- [ ] 优化地图渲染性能
- [ ] 实现标记推荐算法
- [ ] 添加消息通知功能

---

## 七、测试策略

### 7.1 单元测试

**身份认证**：
```java
@Test
public void testMerchantVerification() {
    // 测试商户认证流程
}

@Test
public void testIdentityPermissionCheck() {
    // 测试身份权限检查
}
```

**位置标记**：
```java
@Test
public void testCreateLocationMark() {
    // 测试创建标记
}

@Test
public void testMarkVerification() {
    // 测试标记审核
}
```

### 7.2 集成测试

**场景测试**：
1. 商户认证成功 → 创建店铺标记 → 审核通过 → 公开展示
2. 未认证商家 → 尝试创建店铺标记 → 拒绝访问
3. 学生创建约见点 → 审核 pending → 仅自己可见 → 审核通过 → 公开展示

### 7.3 E2E 测试

使用 Playwright 或 Cypress 进行端到端测试：
- 完整的用户注册和认证流程
- 完整的地图标记和展示流程

---

## 八、文档更新

完成功能后需要更新的文档：

1. **API 文档**: 新增接口的详细说明
2. **数据库文档**: 新增表的 ER 图
3. **用户手册**: 身份认证和位置标记的使用指南
4. **部署文档**: 环境配置和初始化脚本

---

## 九、总结

通过本次全面分析，我们明确了：

### 已完成的基础
✅ 用户系统和登录认证
✅ JWT Token 机制
✅ 三级标签地点系统
✅ 地图导引功能
✅ 基础的身份认证框架

### 需要完善的功能
❌ 商户/团体身份认证
❌ 基于身份的权限控制
❌ 用户自定义位置标记
❌ 位置标记审核和管理

### 实现策略
1. **先基础后应用**: 先完善身份认证，再实现位置标记
2. **先核心后优化**: 先实现基本功能，再添加权限控制
3. **小步快跑**: 分阶段实现，每个阶段都可独立测试

预计总工作量：**10-15 个工作日**

---

**创建时间**: 2026-03-24  
**最后更新**: 2026-03-24  
**版本**: 1.0
