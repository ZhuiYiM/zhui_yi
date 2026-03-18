# 地图导引功能使用说明

## 功能概述

校园信息平台地图导引功能，支持河南师范大学三个校区（本部校区、平原湖校区、创新港校区）的地图展示和导航，集成百度地图、高德地图、腾讯地图三大主流地图服务商。

## 主要特性

### 1. 多校区支持
- **本部校区**：河南省新乡市建设东路 46 号
- **平原湖校区**：河南省新乡市金穗大道东段
- **创新港校区**：河南省新乡市红旗区新二街

### 2. 多地图服务商切换
用户可以根据个人偏好选择不同的地图服务商：
- 🗺️ **百度地图** - 详细的 POI 信息和路线规划
- 🗺️ **高德地图** - 精准的导航和实时路况
- 🗺️ **腾讯地图** - 清晰的界面和社交功能

### 3. 地点分类展示
系统自动对校园地点进行分类管理：
- 🏫 教学楼
- 📚 图书馆
- 🍽️ 食堂
- 🏠 宿舍
- ⚽ 体育设施
- 🏢 行政楼
- 📍 其他

### 4. 热门地点推荐
每个校区展示热门地点，快速访问常用场所。

## 技术实现

### 后端技术栈
- **Spring Boot 3.2.0** - 核心框架
- **MyBatis-Plus** - ORM 框架
- **Jackson** - JSON 处理

### 前端技术栈
- **Vue 3** - 渐进式框架
- **Element Plus** - UI 组件库
- **地图 SDK** - 百度/高德/腾讯地图 JavaScript API

### 数据库设计

#### campuses 表（校区表）
```sql
CREATE TABLE `campuses` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,           -- 校区名称
    `code` VARCHAR(50) NOT NULL UNIQUE,     -- 校区编码
    `description` TEXT,                      -- 校区描述
    `address` VARCHAR(255),                  -- 校区地址
    `center_latitude` DECIMAL(10, 8),       -- 中心点纬度
    `center_longitude` DECIMAL(11, 8),      -- 中心点经度
    `zoom_level` INT,                        -- 默认缩放级别
    `map_config` JSON,                       -- 地图配置
    `is_active` TINYINT,                     -- 是否启用
    `sort_order` INT                         -- 排序顺序
);
```

#### locations 表（地点表）
```sql
CREATE TABLE `locations` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `campus_id` INT NOT NULL,               -- 校区 ID
    `name` VARCHAR(100) NOT NULL,           -- 地点名称
    `description` TEXT,                      -- 地点描述
    `category` VARCHAR(50),                  -- 地点分类
    `latitude` DECIMAL(10, 8),              -- 纬度
    `longitude` DECIMAL(11, 8),             -- 经度
    `icon` VARCHAR(50),                      -- 图标
    `is_popular` TINYINT,                    -- 是否热门
    `view_count` INT,                        -- 浏览次数
    `sort_order` INT                         -- 排序顺序
);
```

## API 接口说明

### 1. 获取校区列表
```http
GET /api/campuses
```

响应示例：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "本部校区",
      "code": "main",
      "description": "河南师范大学主校区",
      "address": "河南省新乡市建设东路 46 号"
    }
  ]
}
```

### 2. 获取校区地点
```http
GET /api/campuses/{campusId}/locations
```

### 3. 获取热门地点
```http
GET /api/campuses/popular-locations?campusId=1
```

### 4. 获取地图配置
```http
GET /api/campuses/map-config?campusId=1&mapType=baidu
```

参数说明：
- `campusId`: 校区 ID（必填）
- `mapType`: 地图类型（可选，baidu/gaode/tencent）

响应示例：
```json
{
  "code": 200,
  "data": {
    "latitude": 35.307736,
    "longitude": 113.926765,
    "zoom": 16,
    "campusName": "本部校区",
    "campusCode": "main",
    "address": "河南省新乡市建设东路 46 号"
  }
}
```

## 部署配置

### 1. 数据库初始化

执行 SQL 脚本创建表和初始数据：
```bash
mysql -u root -p campus_db < database_campuses_locations.sql
```

### 2. 地图 API Key 配置

在 `src/utils/mapHelper.js` 中配置各地图服务商的 API Key：

```javascript
export const MAP_PROVIDERS = {
    BAIDU: {
        id: 'baidu',
        name: '百度地图',
        scriptUrl: 'https://api.map.baidu.com/api?v=3.0&ak=YOUR_BAIDU_AK'
    },
    GAODE: {
        id: 'gaode',
        name: '高德地图',
        scriptUrl: 'https://webapi.amap.com/maps?v=2.0&key=YOUR_GAODE_KEY'
    },
    TENCENT: {
        id: 'tencent',
        name: '腾讯地图',
        scriptUrl: 'https://map.qq.com/api/gljs?v=1.exp&key=YOUR_TENCENT_KEY'
    }
};
```

### 3. 获取 API Key

#### 百度地图
1. 访问 [百度地图开放平台](https://lbsyun.baidu.com/)
2. 注册账号并创建应用
3. 选择浏览器端，获取 AK

#### 高德地图
1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册账号并创建 Key
3. 选择 Web 端 (JS API)，获取 Key

#### 腾讯地图
1. 访问 [腾讯位置服务](https://lbs.qq.com/)
2. 注册账号并创建应用
3. 选择浏览器端，获取 key

### 4. 启动项目

后端启动：
```bash
cd F:\code\CampusInformationPlatform\demo
mvn spring-boot:run
```

前端启动：
```bash
npm install
npm run dev
```

## 使用指南

### 用户操作流程

1. **进入地图页面**
   - 点击导航栏的"地图"选项
   - 首次使用需选择默认校区

2. **切换校区**
   - 点击顶部的校区标签（本部校区/平原湖校区/创新港校区）
   - 地图会自动切换到对应校区

3. **切换地图服务商**
   - 使用地图商下拉框选择偏好的地图服务
   - 地图会重新加载所选服务商的地图

4. **查看热门地点**
   - 滚动到"校园导航"区域
   - 点击地点卡片可查看详情

5. **搜索地点**
   - 在搜索框输入地点名称
   - 按回车或点击搜索按钮

### 管理员操作

如需添加或修改地点信息：

1. 直接操作数据库添加数据
2. 开发后台管理界面进行管理（待实现）

## 坐标系统说明

不同地图服务商使用不同的坐标系：

- **WGS-84**：GPS 原始坐标
- **GCJ-02**：高德、腾讯使用的火星坐标系
- **BD-09**：百度坐标系

系统内置了坐标转换工具 `CoordinateConverter`，可自动处理坐标转换。

## 故障排查

### 地图无法加载

1. 检查 API Key 是否正确配置
2. 确认网络连接正常
3. 查看浏览器控制台错误信息
4. 验证域名是否在地图服务商白名单中

### 位置不准确

1. 检查数据库中经纬度是否正确
2. 确认坐标系转换是否正确
3. 尝试切换其他地图服务商

### 样式显示异常

1. 清除浏览器缓存
2. 检查 CSS 文件是否正确加载
3. 验证 Element Plus 版本兼容性

## 扩展功能建议

### 短期优化
- [ ] 添加地点详情弹窗
- [ ] 实现路径规划和导航
- [ ] 添加地点搜索建议
- [ ] 支持地点收藏功能

### 长期规划
- [ ] 室内地图支持
- [ ] AR 实景导航
- [ ] 人流密度展示
- [ ] 空闲教室查询
- [ ] 校车实时位置

## 技术参考

### 相关文档
- [百度地图 JavaScript API](https://lbsyun.baidu.com/index.php?title=jspopular3.0)
- [高德地图 JavaScript API](https://lbs.amap.com/api/javascript-api/summary)
- [腾讯地图 JavaScript API](https://lbs.qq.com/webDemoCenter/glAPI/glOverview/glIntroduction)

### 代码文件位置
- 数据库脚本：`database_campuses_locations.sql`
- 后端实体：`src/main/java/com/example/demo/entity/Campus.java`, `Location.java`
- 后端服务：`src/main/java/com/example/demo/service/CampusService.java`
- 前端组件：`src/components/user/Map.vue`
- 地图工具：`src/utils/mapHelper.js`
- API 接口：`src/api/campus.js`

## 更新日志

### v1.0.0 (2026-03-19)
- ✅ 创建校区和地点数据库表
- ✅ 添加三个校区的初始数据
- ✅ 实现多地图服务商切换
- ✅ 开发地点分类和热门推荐
- ✅ 提供完整的 API 接口
- ✅ 坐标转换工具实现

---

**开发者**: Campus Information Platform Team  
**最后更新**: 2026-03-19
