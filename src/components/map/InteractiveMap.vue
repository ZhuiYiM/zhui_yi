<template>
  <div class="interactive-map-container">
    <!-- 地图容器 - 支持拖动 -->
    <div 
      class="map-wrapper" 
      ref="mapWrapper"
      @mousedown="handleDragStart"
      @mousemove="handleDragMove"
      @mouseup="handleDragEnd"
      @mouseleave="handleDragEnd"
      :class="{ 'is-dragging': isDragging }"
    >
      <div class="map-content" :style="mapStyle">
        <!-- 地图底图 -->
        <img 
          src="@/assets/01.png" 
          alt="校园手绘地图" 
          class="map-base-image"
          @load="handleImageLoad"
          draggable="false"
        />
        
        <!-- 热点层 -->
        <div class="hotspot-layer">
          <div
            v-for="location in allLocations"
            :key="location.id"
            class="hotspot"
            :class="{ 
              'highlighted': highlightedIds.includes(location.id),
              'hidden': isLocationHidden(location)
            }"
            :style="getHotspotStyle(location)"
            @mouseenter="showTooltip(location)"
            @mouseleave="hideTooltip"
            @click="handleLocationClick(location)"
          >
            <!-- 热点标记 -->
            <div class="hotspot-marker">
              <span class="hotspot-icon">{{ location.icon }}</span>
            </div>
            
            <!-- 地点名称 (默认显示) -->
            <div class="hotspot-label">
              {{ location.name }}
            </div>
            
            <!-- 悬停提示框 -->
            <div v-if="tooltipLocation?.id === location.id" class="hotspot-tooltip">
              <div class="tooltip-name">{{ location.name }}</div>
              <div class="tooltip-desc">{{ location.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 缩放控制 -->
    <div class="zoom-controls">
      <el-button @click="zoomIn" title="放大">+</el-button>
      <el-button @click="zoomOut" title="缩小">-</el-button>
      <el-button @click="resetZoom" title="重置">⟲</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import handdrawnMapLocations from '@/data/handdrawn-map-locations.json';

// 数据
const allLocations = ref(handdrawnMapLocations);
const tooltipLocation = ref(null);
const highlightedIds = ref([]);
const currentZoom = ref(1);
const mapWrapper = ref(null);

// 拖动相关
const isDragging = ref(false);
const startPos = { x: 0, y: 0 };
const scrollPos = { left: 0, top: 0 };

// 方法：从外部更新地点数据
const updateLocations = (locations) => {
  if (locations && locations.length > 0) {
    // 这里可以将数据库的 locations 转换为 JSON 格式
    // 但目前我们直接使用 handdrawn-map-locations.json
    console.log('更新地点数据:', locations.length, '个');
  }
};

// 计算属性：过滤后的地点
const filteredLocations = computed(() => {
  if (!searchQuery.value.trim()) {
    return allLocations.value;
  }
  
  const query = searchQuery.value.toLowerCase();
  return allLocations.value.filter(loc =>
    loc.name.toLowerCase().includes(query) ||
    loc.description.toLowerCase().includes(query) ||
    loc.category.toLowerCase().includes(query)
  );
});

// 计算属性：地图样式
const mapStyle = computed(() => ({
  transform: `scale(${currentZoom.value})`,
  transformOrigin: 'center center'
}));

// 方法：获取热点样式
const getHotspotStyle = (location) => ({
  left: location.x + '%',
  top: location.y + '%',
  width: location.width + '%',
  height: location.height + '%'
});

// 方法：判断地点是否应该隐藏 (用于聚合)
const isLocationHidden = (location) => {
  // 如果缩放级别小于 1.2 且地点是宿舍楼，则隐藏部分
  if (currentZoom.value < 1.2 && location.category === 'dormitory') {
    // 只显示每栋楼的第一个
    const firstDorm = allLocations.value.find(l => 
      l.category === 'dormitory' && 
      l.name.startsWith(location.name.substring(0, 3))
    );
    return firstDorm && firstDorm.id !== location.id;
  }
  return false;
};

// 方法：显示提示
const showTooltip = (location) => {
  tooltipLocation.value = location;
};

// 方法：隐藏提示
const hideTooltip = () => {
  tooltipLocation.value = null;
};

// 方法：处理地点点击
const handleLocationClick = (location) => {
  console.log('点击地点:', location);
  ElMessage.info(`点击了：${location.name}`);
  // 可以在这里跳转到详情页
  // router.push(`/location/${location.id}`);
};

// 方法：处理搜索输入
const handleSearch = (value) => {
  if (!value.trim()) {
    highlightedIds.value = [];
  }
};

// 方法：处理滚动
const handleScroll = () => {
  // 可以在这里添加滚动时的优化逻辑
};

// 拖动相关方法
const handleDragStart = (e) => {
  isDragging.value = true;
  startPos.x = e.clientX;
  startPos.y = e.clientY;
  if (mapWrapper.value) {
    scrollPos.left = mapWrapper.value.scrollLeft;
    scrollPos.top = mapWrapper.value.scrollTop;
  }
};

const handleDragMove = (e) => {
  if (!isDragging.value || !mapWrapper.value) return;
  e.preventDefault();
  const dx = e.clientX - startPos.x;
  const dy = e.clientY - startPos.y;
  mapWrapper.value.scrollLeft = scrollPos.left - dx;
  mapWrapper.value.scrollTop = scrollPos.top - dy;
};

const handleDragEnd = () => {
  isDragging.value = false;
};

// 方法：滚动到指定地点
const scrollToLocation = (location) => {
  if (!mapWrapper.value) return;
  
  const wrapper = mapWrapper.value;
  const content = wrapper.querySelector('.map-content');
  
  // 计算目标位置 (将百分比转换为像素)
  const targetX = (location.x / 100) * content.offsetWidth;
  const targetY = (location.y / 100) * content.offsetHeight;
  
  // 滚动到目标位置 (居中显示)
  wrapper.scrollLeft = targetX - wrapper.offsetWidth / 2;
  wrapper.scrollTop = targetY - wrapper.offsetHeight / 2;
};

// 方法：按分类搜索 (供外部调用)
const searchByCategory = (categoryId, locations) => {
  if (!locations || locations.length === 0) {
    highlightedIds.value = [];
    ElMessage.warning('未找到相关地点');
    return;
  }
  
  // 高亮所有匹配的地点
  const matchedIds = locations.map(loc => loc.id);
  highlightedIds.value = matchedIds;
  
  // 定位到第一个地点
  if (locations.length > 0) {
    scrollToLocation(locations[0]);
  }
  
  ElMessage.success(`找到 ${locations.length} 个${categoryId}地点`);
};

// 方法：放大
const zoomIn = () => {
  if (currentZoom.value < 3) {
    currentZoom.value += 0.2;
  }
};

// 方法：缩小
const zoomOut = () => {
  if (currentZoom.value > 0.5) {
    currentZoom.value -= 0.2;
  }
};

// 方法：重置缩放
const resetZoom = () => {
  currentZoom.value = 1;
  if (mapWrapper.value) {
    mapWrapper.value.scrollLeft = 0;
    mapWrapper.value.scrollTop = 0;
  }
};

// 生命周期
onMounted(() => {
  console.log('交互式地图组件已挂载');
  console.log('地点数据:', allLocations.value.length, '个');
});

// 暴露给外部的方法
defineExpose({
  searchByCategory,
  updateLocations
});
</script>

<style scoped>
.interactive-map-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 80px);  /* 增加 y 值，使用视口高度计算 */
  min-height: 600px;            /* 最小高度确保可见 */
  background: #f5f7fa;
  overflow: hidden;  /* 外层容器保持 hidden */
}

/* 搜索栏样式 - 已移除 */
/* .search-bar { ... } */

/* 地图容器样式 */
.map-wrapper {
  width: 100%;
  height: 100%;
  overflow: scroll !important;  /* 强制显示滚动条 */
  cursor: grab;
  user-select: none;
  position: relative;
  display: block;
}

.map-wrapper.is-dragging {
  cursor: grabbing;
}

.map-content {
  position: relative;
  display: block;
  width: max-content !important;  /* 强制使用内容宽度 */
  height: max-content !important; /* 强制使用内容高度 */
  min-width: 100%;
  min-height: 100%;
  transition: transform 0.3s ease;
}

.map-base-image {
  display: block;
  width: auto;
  height: auto;
  max-width: none;
  user-select: none;
  pointer-events: none;
}

/* 热点层样式 */
.hotspot-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.hotspot {
  position: absolute;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.hotspot:hover {
  z-index: 50;
}

.hotspot.hidden {
  display: none;
}

.hotspot.highlighted .hotspot-marker {
  transform: scale(1.5);
  box-shadow: 0 0 20px rgba(255, 165, 0, 0.8);
  animation: pulse 1.5s infinite;
}

/* 热点标记样式 */
.hotspot-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A90E2 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.4);
  transition: all 0.3s ease;
  font-size: 18px;
}

.hotspot:hover .hotspot-marker {
  transform: scale(1.3);
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.6);
}

/* 地点标签样式 */
.hotspot-label {
  margin-top: 4px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  font-size: 12px;
  color: #333;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 悬停提示框样式 */
.hotspot-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(-8px);
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  min-width: 150px;
  max-width: 250px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 100;
  animation: tooltipFadeIn 0.2s ease;
}

.tooltip-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}

.tooltip-desc {
  font-size: 12px;
  opacity: 0.9;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(-8px);
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 165, 0, 0.8);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 165, 0, 1);
  }
}

/* 缩放控制样式 */
.zoom-controls {
  position: absolute;
  bottom: 150px;  /* 从 100px 增加到 150px，再向上移动 */
  right: 20px;
  z-index: 1000;  /* 增加 z-index 确保在最上层 */
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;  /* 确保按钮水平居中对齐 */
}

.zoom-controls .el-button {
  width: 40px;
  height: 40px;
  padding: 0;
  font-size: 20px;
  font-weight: bold;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1001;  /* 按钮更高的层级 */
  display: flex;
  align-items: center;  /* 垂直居中 */
  justify-content: center;  /* 水平居中 */
  margin: 0;  /* 移除默认边距 */
}

.zoom-controls .el-button:hover {
  background: #4A90E2;
  color: white;
}

/* 确保按钮内容可见 */
.zoom-controls button {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .search-bar {
    width: 90%;
    top: 10px;
  }
  
  .hotspot-label {
    font-size: 10px;
    padding: 1px 4px;
  }
  
  .hotspot-marker {
    width: 24px;
    height: 24px;
    font-size: 14px;
  }
}
</style>
