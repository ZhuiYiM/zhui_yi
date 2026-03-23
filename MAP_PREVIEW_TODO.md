# 地点详情页地图预览功能 TODO

## ✅ 已完成

1. **手绘地图底图显示**
   - 使用 `@/assets/01.png` 作为地图底图
   - 地图图片正常加载显示

2. **位置标记显示**
   - 实现了标记样式计算（`markerStyle`）
   - 标记包含图标（📍）和地点名称标签
   - 添加了弹跳动画效果

3. **查看完整地图按钮**
   - 点击可跳转到地图主页
   - 传递地点 ID 参数用于高亮显示

4. **样式优化**
   - 标记位置定位（基于经纬度偏移计算）
   - 响应式布局
   - 移动端适配

## ⚠️ 当前限制

### 1. 位置计算精度问题

**现状**：
```javascript
// 当前使用的简化计算
const centerLat = 35.307736;
const centerLng = 113.926765;
const latOffset = (location.value.latitude - centerLat) * 1000;
const lngOffset = (location.value.longitude - centerLng) * 1000;
const x = 50 + lngOffset * 50;
const y = 50 - latOffset * 50;
```

**问题**：
- 系数（1000, 50）是估算值，不够精确
- 没有考虑地图投影转换
- 不同校区的地图覆盖范围不同

**解决方案**：

#### 方案 A：使用 handdrawn-map-locations.json 的精确坐标（推荐）

1. 从 JSON 文件中读取地点的精确百分比坐标
2. 建立数据库地点 ID 与 JSON 地点的映射关系

```javascript
// 修改 markerStyle 计算逻辑
import handdrawnMapLocations from '@/data/handdrawn-map-locations.json';

const markerStyle = computed(() => {
  if (!location.value) return {};
  
  // 从 JSON 中查找匹配的地点
  const jsonLocation = handdrawnMapLocations.find(
    loc => loc.name === location.value.name || 
           loc.id === location.value.handdrawnMapId
  );
  
  if (jsonLocation) {
    return {
      left: jsonLocation.x + '%',
      top: jsonLocation.y + '%'
    };
  }
  
  // 如果找不到，使用经纬度估算
  return calculateFromLatLng();
});
```

**优点**：
- 位置精确
- 不需要复杂的坐标转换
- 与 InteractiveMap 组件使用同一套数据

**缺点**：
- 需要在数据库中添加 `handdrawn_map_id` 字段建立关联
- 需要手动维护 JSON 数据

#### 方案 B：精确的经纬度转百分比算法

1. 获取地图图片覆盖的经纬度范围
2. 使用线性插值计算百分比坐标

```javascript
// 需要知道地图的边界
const mapBounds = {
  minLat: 35.300000,  // 地图最南端的纬度
  maxLat: 35.315000,  // 地图最北端的纬度
  minLng: 113.920000, // 地图最西端的经度
  maxLng: 113.935000  // 地图最东端的经度
};

const markerStyle = computed(() => {
  if (!location.value) return {};
  
  const { latitude, longitude } = location.value;
  
  // 计算百分比（经度对应 x，纬度对应 y）
  const x = ((longitude - mapBounds.minLng) / 
             (mapBounds.maxLng - mapBounds.minLng)) * 100;
  
  const y = (1 - ((latitude - mapBounds.minLat) / 
                  (mapBounds.maxLat - mapBounds.minLat))) * 100;
  
  return {
    left: x + '%',
    top: y + '%'
  };
});
```

**优点**：
- 不需要额外的 JSON 数据
- 可以自动计算任意地点的位置
- 适合多校区（每个校区配置不同的 bounds）

**缺点**：
- 需要测量地图图片的经纬度覆盖范围
- 手绘地图可能不是严格的矩形投影

## 🔧 后续优化建议

### P1 - 高精度地图（推荐）

1. **使用 handdrawn-map-locations.json**
   - 修改数据库 locations 表，添加 `handdrawn_map_id` 字段
   - 更新 markerStyle 计算逻辑，优先从 JSON 读取
   - 在后台管理系统中添加地点关联功能

2. **改进标记显示**
   - 支持点击标记查看详情
   - 标记可以拖动调整位置
   - 支持多个标记同时显示（附近地点）

3. **添加地图控制**
   - 缩放控制（+/- 按钮）
   - 拖动功能（参考 InteractiveMap）
   - 复位按钮

### P2 - 多地图支持

1. **用户可选择默认地图类型**
   - 手绘地图
   - 百度地图
   - 高德地图
   - 腾讯地图

2. **实现方案**
```vue
<div class="map-preview-section">
  <div class="map-type-selector">
    <el-radio-group v-model="userPreferredMap" size="small">
      <el-radio-button label="handdrawn">手绘地图</el-radio-button>
      <el-radio-button label="baidu">百度地图</el-radio-button>
      <el-radio-button label="gaode">高德地图</el-radio-button>
    </el-radio-group>
  </div>
  
  <!-- 根据选择渲染不同的地图 -->
  <HandDrawnMap v-if="userPreferredMap === 'handdrawn'" />
  <BaiduMap v-else-if="userPreferredMap === 'baidu'" />
  <GaodeMap v-else-if="userPreferredMap === 'gaode'" />
</div>
```

3. **保存用户偏好**
   - 将偏好存储在 localStorage 或数据库
   - 下次访问自动使用偏好地图

### P3 - 真实地图 SDK 集成

1. **百度地图**
```javascript
// 需要百度地图 AK
const initBaiduMap = () => {
  const map = new BMap.Map(miniMapContainer.value);
  const point = new BMap.Point(location.value.longitude, location.value.latitude);
  map.centerAndZoom(point, 15);
  map.addOverlay(new BMap.Marker(point));
};
```

2. **高德地图**
```javascript
// 需要高德地图 Key
const initGaodeMap = () => {
  const map = new AMap.Map(miniMapContainer.value, {
    zoom: 15,
    center: [location.value.longitude, location.value.latitude]
  });
  new AMap.Marker({
    position: [location.value.longitude, location.value.latitude],
    map: map
  });
};
```

## 📝 数据库修改建议

### 添加 handdrawn_map_id 字段

```sql
ALTER TABLE locations 
ADD COLUMN handdrawn_map_id INT COMMENT '手绘地图 JSON 中的地点 ID',
ADD INDEX idx_handdrawn_map_id (handdrawn_map_id);

-- 更新现有数据（示例）
UPDATE locations 
SET handdrawn_map_id = 1 
WHERE name = '操场';
```

### 添加用户地图偏好字段

```sql
ALTER TABLE users 
ADD COLUMN preferred_map_type VARCHAR(20) DEFAULT 'handdrawn' COMMENT '首选地图类型：handdrawn/baidu/gaode/tencent';
```

## 🎯 实现优先级

1. **立即实现**（P0）：
   - ✅ 基础手绘地图显示
   - ✅ 位置标记显示
   - ⏳ 使用 handdrawn-map-locations.json 精确坐标

2. **短期优化**（P1）：
   - 地图拖动和缩放
   - 标记点击交互
   - 附近地点标记

3. **中期计划**（P2）：
   - 多地图类型支持
   - 用户偏好设置
   - 后台地点关联管理

4. **长期规划**（P3）：
   - 真实地图 SDK 集成
   - 导航路线规划
   - AR 实景导航

## 📚 相关资源

- 手绘地图数据：`/src/data/handdrawn-map-locations.json`
- 地图图片：`/src/assets/01.png`
- 交互式地图组件：`/src/components/map/InteractiveMap.vue`
- 地图工具函数：`/src/utils/mapHelper.js`
