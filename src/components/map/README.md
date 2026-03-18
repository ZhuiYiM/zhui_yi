# 地图功能组件说明

## 📁 目录结构

```
src/components/map/
├── Map.vue              # 主地图页面组件
├── LocationDetail.vue   # 地点详情页面组件（新增）
└── README.md           # 本说明文档
```

## 🎯 组件功能

### 1. Map.vue - 主地图页面
**路径**: `/map`

**主要功能**:
- 多校区地图展示（本部、平原湖、创新港）
- 多地图服务商切换（百度、高德、腾讯）
- 地点搜索与筛选
- 热门地点推荐
- 校园导航分类
- 点击地点卡片跳转到详情页

**核心方法**:
```javascript
// 跳转到地点详情页
goToLocation(locationId) {
  router.push(`/location/${locationId}`);
}
```

### 2. LocationDetail.vue - 地点详情页（新增）
**路径**: `/location/:id`

**主要功能**:
- 地点详细信息展示
  - 地点名称、分类、描述
  - 开放时间、联系方式
  - 设施标签展示
  - 地点图片预览
- 交互功能
  - 🗺️ 导航到这里（调用百度地图）
  - 📤 分享地点（复制到剪贴板）
  - 位置预览（迷你地图）
- 相关地点推荐
  - 附近地点
  - 同类地点

**响应式布局**:
- 支持桌面端和移动端自适应
- 移动端优化显示

## 🔧 API 接口

### 已添加的新接口

```javascript
// src/api/campus.js

// 获取地点详情
getLocationDetail(locationId) {
    return request.get(`/campuses/locations/${locationId}`);
}
```

### 完整的 API 列表

```javascript
campusAPI = {
    // 获取校区列表
    getCampuses(),
    
    // 获取校区地点信息
    getCampusLocations(campusId),
    
    // 搜索地点
    searchLocations(keyword, campusId),
    
    // 获取热门地点
    getPopularLocations(campusId),
    
    // 获取地图配置
    getMapConfig(campusId, mapType),
    
    // 【新增】获取地点详情
    getLocationDetail(locationId)
}
```

## 🛣️ 路由配置

已在 `src/router/index.js` 中添加:

```javascript
{
    path: '/location/:id',
    name: 'LocationDetail',
    component: LocationDetail,
    meta: { requiresAuth: false }  // 游客也可访问
}
```

## 📊 数据流

1. **用户在地图页点击地点**
   ```
   Map.vue (点击地点卡片) 
   → goToLocation(locationId) 
   → router.push(`/location/${locationId}`)
   ```

2. **地点详情页加载数据**
   ```
   LocationDetail.vue (onMounted)
   → fetchLocationDetail()
   → campusAPI.getLocationDetail(id)
   → 渲染页面
   ```

3. **获取相关地点**
   ```
   fetchRelatedLocations(campusId)
   → campusAPI.getCampusLocations(campusId)
   → 过滤并展示前 4 个地点
   ```

## 🎨 UI 特性

### LocationDetail.vue 设计亮点

1. **双栏布局**（桌面端）
   - 左侧：地点图片画廊
   - 右侧：地点信息区域

2. **信息分组展示**
   - 标题和分类标签
   - 基本信息（地址、时间、联系方式）
   - 设施服务标签
   - 操作按钮区
   - 地图预览区

3. **相关推荐**
   - 网格布局展示附近地点
   - 悬停效果增强交互体验

4. **图片预览**
   - 点击图片放大查看
   - 支持 ESC 关闭

5. **移动端适配**
   - 单栏布局
   - 触摸友好的按钮尺寸
   - 优化的字体大小

## 🔍 使用示例

### 从地图页跳转
```vue
<!-- Map.vue 中 -->
<div 
  v-for="item in combinedItems"
  :key="item.id"
  class="nav-item-card"
  @click="handleItemClick(item)"
>
  {{ item.name }}
</div>

<script>
const handleItemClick = (item) => {
  if (item.type === 'location') {
    goToLocation(item.id);  // 跳转到详情页
  }
};
</script>
```

### 直接访问详情页
```javascript
// 编程式导航
router.push('/location/123');

// 或在模板中
<router-link to="/location/123">查看详情</router-link>
```

## 📝 待办事项

### 功能完善
- [ ] 集成真实的地图 SDK 到迷你地图区域
- [ ] 添加地点收藏功能
- [ ] 实现用户评价系统
- [ ] 添加地点间的路线规划
- [ ] 支持 AR 实景导航

### 性能优化
- [ ] 图片懒加载
- [ ] 地点数据缓存
- [ ] 虚拟滚动优化大量地点展示

### 用户体验
- [ ] 添加骨架屏加载动画
- [ ] 离线地图支持
- [ ] 语音导航功能

## 🐛 故障排查

### 地点详情无法加载

1. **检查后端接口**
   ```bash
   # 测试 API 是否正常
   GET http://localhost:8080/api/campuses/locations/1
   ```

2. **检查前端路由**
   ```javascript
   // 确认路由配置正确
   console.log($route.params.id);
   ```

3. **查看浏览器控制台**
   - 打开开发者工具
   - 查看 Network 标签的请求状态
   - 检查 Console 标签的错误信息

### 图片无法显示

1. 检查 `imageUrl` 字段是否为有效 URL
2. 确认图片服务器可访问
3. 检查 CORS 配置

## 📚 相关文档

- [地图功能快速启动指南](../../MAP_QUICKSTART.md)
- [地图功能详细说明](../../MAP_FEATURE_README.md)
- [地点数据结构](../../database_campuses_locations.sql)

## 🎯 下一步计划

1. **短期优化**
   - 完善地图 SDK 集成
   - 添加更多地点交互功能
   
2. **长期规划**
   - 室内地图支持
   - 实时人流密度展示
   - 校车位置追踪
   - 空闲教室查询

---

**创建时间**: 2026-03-19  
**最后更新**: 2026-03-19  
**维护者**: Campus Information Platform Team
