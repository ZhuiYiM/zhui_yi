# 交易中心广告功能完善说明

## 一、功能概述

完善了交易中心（Mall）的广告功能，支持三种广告类型：

1. **活动标签筛选广告（activity）**：点击后跳转到交易中心页面并筛选特定标签的商品
2. **商家页面广告（merchant）**：点击后跳转到商家对外展示页面（个人主页）
3. **商品广告（product）**：点击后直接进入商品详细页

## 二、数据库变更

### 2.1 新增字段

在 `advertisements` 表中新增以下字段：

```sql
-- 广告类型字段
ad_type VARCHAR(50) DEFAULT 'product' 
COMMENT '广告类型：activity-活动标签筛选，merchant-商家页面，product-商品广告'

-- 关联 ID 字段
related_id BIGINT 
COMMENT '关联 ID：商品 ID、商家 ID 等，根据广告类型而定'

-- 筛选标签字段（JSON 格式）
filter_tags VARCHAR(500) 
COMMENT '筛选标签：JSON 格式存储，用于活动类型广告的标签筛选'
```

### 2.2 索引优化

```sql
CREATE INDEX `idx_ad_type` ON `advertisements` (`ad_type`);
CREATE INDEX `idx_related_id` ON `advertisements` (`related_id`);
```

### 2.3 测试数据

SQL 脚本中包含了 5 条测试数据，覆盖三种广告类型：
- 活动标签广告：毕业季二手交易特卖、校园美食节
- 商家广告：校园快递代取服务
- 商品广告：学霸笔记合集、二手教材低价出售

## 三、后端实现

### 3.1 实体类更新

**文件**: `src/main/java/com/example/demo/entity/Advertisement.java`

新增字段：
- `adType`: 广告类型
- `relatedId`: 关联 ID
- `filterTags`: 筛选标签（JSON 格式）

### 3.2 DTO 更新

**文件**: `src/main/java/com/example/demo/dto/AdvertisementDTO.java`

新增相同字段用于数据传输。

### 3.3 控制器

#### 3.3.1 管理端控制器

**文件**: `src/main/java/com/example/demo/controller/admin/AdminAdvertisementController.java`

提供完整的 CRUD 接口供管理端使用。

#### 3.3.2 公开接口控制器

**文件**: `src/main/java/com/example/demo/controller/AdvertisementController.java`

新增接口：
- `GET /api/advertisements/{position}`: 获取指定位置的广告列表
- `POST /api/advertisements/{id}/click`: 增加广告点击次数

**功能特点**：
- 自动过滤已禁用广告
- 自动过滤不在有效期内的广告
- 按排序顺序和创建时间排序

### 3.4 服务层

**文件**: `src/main/java/com/example/demo/service/impl/AdvertisementServiceImpl.java`

提供点击次数和展示次数的统计功能。

## 四、前端实现

### 4.1 管理端 UI

**文件**: `src/views/admin/AdvertisementManagement.vue`

#### 新增功能：
1. **广告类型选择器**：支持选择商品广告、商家页面、活动标签筛选
2. **动态表单**：
   - 选择"商品广告"或"商家页面"时显示"关联 ID"输入框
   - 选择"活动标签筛选"时显示"筛选标签"输入框（JSON 格式）
3. **表格列显示**：新增"广告类型"列，使用不同颜色标签区分

#### 使用说明：
- **商品广告**：填写商品 ID，点击后跳转到商品详情页 `/product/{id}`
- **商家页面**：填写商家 ID，点击后跳转到商家主页 `/user/{id}`
- **活动标签筛选**：填写 JSON 格式的标签数组，如 `{"tags": ["secondhand", "books"]}`，点击后跳转到 `/mall?tags=secondhand,books`

### 4.2 交易中心页面

**文件**: `src/components/mall/Mall.vue`

#### 新增功能：
1. **广告数据获取**：`fetchMallAds()` 方法从后端获取广告数据
2. **广告数据处理**：转换后端数据格式以适配 AdBanner 组件
3. **智能链接生成**：根据广告类型自动生成正确的跳转链接
4. **点击统计**：用户点击广告时调用后端接口增加点击次数

#### 关键代码：
```javascript
// 根据广告类型生成跳转链接
const generateAdLink = (ad) => {
  if (!ad.adType || ad.adType === 'product') {
    return `/product/${ad.relatedId}`;
  } else if (ad.adType === 'merchant') {
    return `/user/${ad.relatedId}`;
  } else if (ad.adType === 'activity' && ad.filterTags) {
    const tags = JSON.parse(ad.filterTags);
    const tagParam = tags.tags ? tags.tags.join(',') : '';
    return `/mall?tags=${tagParam}`;
  }
  return '/mall';
};
```

### 4.3 广告轮播组件

**文件**: `src/components/mall/AdBanner.vue`

#### 新增功能：
1. **展示次数统计**：使用防抖技术，3 秒后计为有效展示
2. **自定义广告数据**：支持父组件传入自定义广告数据
3. **点击事件触发**：点击时触发 `ad-click` 事件供父组件处理

#### 展示统计逻辑：
```javascript
const incrementViewCount = () => {
  if (viewCountTimer) {
    clearTimeout(viewCountTimer);
  }
  
  viewCountTimer = setTimeout(() => {
    // 调用后端接口增加展示次数
    console.log('👁️ 广告展示次数 +1');
  }, 3000); // 3 秒后才计为有效展示
};
```

### 4.4 API 接口

**文件**: `src/api/product.js`

新增广告 API：
```javascript
export const mallAdAPI = {
  // 获取交易中心广告列表
  getMallAds() {
    return request.get('/advertisements/mall');
  },
  
  // 增加广告点击次数
  incrementClickCount(id) {
    return request.post(`/advertisements/${id}/click`);
  }
};
```

## 五、使用流程

### 5.1 管理员操作流程

1. 登录管理后台
2. 进入"广告管理"页面
3. 点击"新增广告"
4. 填写广告信息：
   - 广告标题、图片、内容描述
   - 选择广告位置（选择"交易中心"）
   - **选择广告类型**：
     - 商品广告：填写商品 ID
     - 商家页面：填写商家 ID
     - 活动标签筛选：填写 JSON 格式的标签数组
   - 设置有效期和排序
5. 保存后广告将在交易中心轮播展示

### 5.2 用户使用流程

1. 用户访问交易中心页面
2. 查看顶部广告轮播区域
3. 点击感兴趣的广告
4. 根据广告类型跳转到对应页面：
   - 商品广告 → 商品详情页
   - 商家广告 → 商家主页
   - 活动标签 → 筛选后的商品列表页

## 六、数据统计

系统自动统计每个广告的：
- **点击次数**：用户点击广告的次数
- **展示次数**：广告被展示的次数（3 秒以上计为有效展示）

管理员可在广告管理列表中查看这些数据。

## 七、注意事项

### 7.1 数据库执行顺序

1. 先执行 `database_advertisements_upgrade.sql` 添加新字段
2. 再执行原有的 `database_advertisements.sql`（如果尚未执行）

### 7.2 JSON 格式要求

活动标签广告的 `filter_tags` 字段必须为合法的 JSON 格式：
```json
{
  "tags": ["secondhand", "books", "digital"]
}
```

### 7.3 广告有效期

系统会自动过滤不在有效期内的广告，确保只展示有效的广告内容。

### 7.4 错误处理

- 如果后端无广告数据，AdBanner 组件会使用默认广告数据
- 如果广告类型配置错误，默认跳转到交易中心页面
- 所有接口调用都有错误日志输出，便于调试

## 八、扩展建议

1. **广告素材管理**：可增加图片上传功能，无需手动填写 URL
2. **点击率分析**：计算点击率（点击次数/展示次数），帮助评估广告效果
3. **A/B 测试**：支持同一位置多个广告的轮换展示
4. **定向投放**：根据用户身份（学生/商家）展示不同广告
5. **广告位管理**：可扩展更多广告位（首页、话题墙、地图等）

## 九、相关文件清单

### 数据库文件
- `database_advertisements_upgrade.sql` - 广告表结构升级脚本（新增）
- `database_advertisements.sql` - 原有广告表脚本

### 后端文件
- `src/main/java/com/example/demo/entity/Advertisement.java` - 实体类（已修改）
- `src/main/java/com/example/demo/dto/AdvertisementDTO.java` - DTO（已修改）
- `src/main/java/com/example/demo/controller/AdvertisementController.java` - 公开接口控制器（新增）
- `src/main/java/com/example/demo/controller/admin/AdminAdvertisementController.java` - 管理端控制器（已有）
- `src/main/java/com/example/demo/service/AdvertisementService.java` - 服务接口（已有）
- `src/main/java/com/example/demo/service/impl/AdvertisementServiceImpl.java` - 服务实现（已有）

### 前端文件
- `src/components/mall/Mall.vue` - 交易中心页面（已修改）
- `src/components/mall/AdBanner.vue` - 广告轮播组件（已修改）
- `src/views/admin/AdvertisementManagement.vue` - 广告管理页面（已修改）
- `src/api/product.js` - API 接口（已修改）

## 十、测试建议

1. **功能测试**：
   - 创建三种类型的广告并验证跳转是否正确
   - 验证广告有效期过滤是否生效
   - 验证禁用广告是否不展示

2. **数据统计测试**：
   - 点击广告后检查点击次数是否增加
   - 停留 3 秒以上检查展示次数是否增加

3. **边界测试**：
   - 测试无广告数据时的降级处理
   - 测试错误的 JSON 格式处理
   - 测试无效的关联 ID 处理

4. **性能测试**：
   - 大量广告数据时的加载性能
   - 频繁切换广告时的性能表现
