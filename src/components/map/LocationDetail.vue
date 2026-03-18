<template>
  <div :class="['location-detail-page', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="map"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 地点详情 -->
      <div v-else-if="location" class="location-detail">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          ← 返回
        </button>

        <div :class="['detail-content', { 'mobile-layout': isMobile }]">
          <!-- 左侧：地点图片 -->
          <div class="location-gallery">
            <div v-if="location.imageUrl" class="main-image">
              <img :src="location.imageUrl" :alt="location.name" @click="previewImage" />
            </div>
            <div v-else class="main-image placeholder">
              <div class="placeholder-icon">📍</div>
              <p>{{ location.name }}</p>
            </div>
          </div>

          <!-- 右侧：地点信息 -->
          <div class="location-info-section">
            <!-- 标题和分类 -->
            <div class="location-header">
              <div class="location-title-row">
                <h1 class="location-name">{{ location.name }}</h1>
                <span v-if="location.isPopular" class="popular-tag">热门</span>
              </div>
              <div class="location-category">
                <span class="category-icon">{{ getCategoryIcon(location.category) }}</span>
                <span class="category-name">{{ getCategoryName(location.category) }}</span>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="info-section">
              <div class="info-row">
                <span class="info-label">📍 地址：</span>
                <span class="info-value">{{ location.description || '暂无描述' }}</span>
              </div>
              <div v-if="location.openingHours" class="info-row">
                <span class="info-label">🕐 开放时间：</span>
                <span class="info-value">{{ location.openingHours }}</span>
              </div>
              <div v-if="location.contactInfo" class="info-row">
                <span class="info-label">📞 联系方式：</span>
                <span class="info-value">{{ location.contactInfo }}</span>
              </div>
            </div>

            <!-- 设施标签 -->
            <div v-if="facilitiesList && facilitiesList.length > 0" class="facilities-section">
              <h3 class="section-title">设施服务</h3>
              <div class="facilities-tags">
                <el-tag 
                  v-for="(facility, index) in facilitiesList" 
                  :key="index"
                  size="small"
                  class="facility-tag"
                >
                  {{ facility }}
                </el-tag>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="primary" @click="navigateToLocation" class="action-btn">
                🗺️ 导航到这里
              </el-button>
              <el-button @click="shareLocation" class="action-btn">
                📤 分享地点
              </el-button>
            </div>

            <!-- 地图预览 -->
            <div class="map-preview-section">
              <h3 class="section-title">位置预览</h3>
              <div class="mini-map" ref="miniMapContainer">
                <div v-if="!miniMapLoaded" class="map-loading-mini">
                  正在加载地图...
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 相关推荐 -->
        <div v-if="relatedLocations && relatedLocations.length > 0" class="related-section">
          <h2 class="section-title">附近地点</h2>
          <div class="related-grid">
            <div 
              v-for="loc in relatedLocations" 
              :key="loc.id"
              class="related-card"
              @click="goToLocation(loc.id)"
            >
              <div class="related-image">
                <img v-if="loc.imageUrl" :src="loc.imageUrl" :alt="loc.name" />
                <div v-else class="image-placeholder">📍</div>
              </div>
              <div class="related-info">
                <h3 class="related-name">{{ loc.name }}</h3>
                <p class="related-category">{{ getCategoryName(loc.category) }}</p>
                <p v-if="loc.distance" class="distance">距离：{{ loc.distance }}m</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">❌</div>
        <p class="error-message">{{ error }}</p>
        <el-button type="primary" @click="goBack">返回首页</el-button>
      </div>
    </main>

    <!-- 图片预览弹窗 -->
    <div v-if="showImagePreview" class="modal-overlay" @click="closeImagePreview">
      <div class="modal-content">
        <button class="close-btn" @click="closeImagePreview">×</button>
        <img :src="location.imageUrl" class="preview-large-image" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import { campusAPI } from '../../api/campus';
import { getLocationCategory } from '../../utils/mapHelper';

const route = useRoute();
const router = useRouter();

// 设备检测
const isMobile = ref(false);
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

// 用户信息
const currentUser = ref({
  username: 'User',
  avatar: ''
});

// 地点数据
const location = ref(null);
const loading = ref(true);
const error = ref(null);
const facilitiesList = computed(() => {
  if (!location.value?.facilities) return [];
  try {
    return JSON.parse(location.value.facilities);
  } catch {
    return [];
  }
});

// 相关地点
const relatedLocations = ref([]);

// 图片预览
const showImagePreview = ref(false);

// 地图相关
const miniMapLoaded = ref(false);
const miniMapContainer = ref(null);

// 获取地点详情
const fetchLocationDetail = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await campusAPI.getLocationDetail(route.params.id);
    location.value = response;
    
    // 获取相关地点（附近的地点）
    if (location.value && location.value.campusId) {
      fetchRelatedLocations(location.value.campusId);
    }
  } catch (err) {
    console.error('获取地点详情失败:', err);
    error.value = '获取地点信息失败，请稍后重试';
    ElMessage.error('获取地点详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取相关地点
const fetchRelatedLocations = async (campusId) => {
  try {
    const response = await campusAPI.getCampusLocations(campusId);
    // 过滤掉当前地点，取前 4 个作为推荐
    relatedLocations.value = response
      .filter(loc => loc.id !== location.value.id)
      .slice(0, 4);
  } catch (err) {
    console.error('获取相关地点失败:', err);
  }
};

// 获取分类名称
const getCategoryName = (categoryCode) => {
  const category = getLocationCategory(categoryCode);
  return category.name;
};

// 获取分类图标
const getCategoryIcon = (categoryCode) => {
  const category = getLocationCategory(categoryCode);
  return category.icon;
};

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/map');
  }
};

// 跳转到地点
const goToLocation = (locationId) => {
  router.push(`/location/${locationId}`);
  // 刷新页面数据
  setTimeout(() => {
    fetchLocationDetail();
  }, 100);
};

// 导航到地点
const navigateToLocation = () => {
  if (!location.value) return;
  
  const { latitude, longitude, name } = location.value;
  
  // 打开地图应用进行导航
  const baiduMapUrl = `http://api.map.baidu.com/marker?location=${latitude},${longitude}&title=${name}&content=${name}&output=html`;
  window.open(baiduMapUrl, '_blank');
  
  ElMessage.success('已在新窗口打开导航');
};

// 分享地点
const shareLocation = () => {
  if (!location.value) return;
  
  const shareText = `我发现了一个好地方：${location.value.name}，快来看看吧！`;
  
  // 复制到剪贴板
  navigator.clipboard.writeText(shareText).then(() => {
    ElMessage.success('分享信息已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};

// 图片预览
const previewImage = () => {
  if (!location.value?.imageUrl) return;
  showImagePreview.value = true;
};

const closeImagePreview = () => {
  showImagePreview.value = false;
};

// 初始化迷你地图
const initMiniMap = () => {
  if (!location.value || !miniMapContainer.value) return;
  
  // 这里可以集成真实的地图 SDK
  // 暂时显示一个占位图
  miniMapLoaded.value = true;
  
  // TODO: 集成百度地图/高德地图 SDK
  // 示例代码（需要配置 API Key）：
  /*
  const map = new BMap.Map(miniMapContainer.value);
  const point = new BMap.Point(location.value.longitude, location.value.latitude);
  map.centerAndZoom(point, 15);
  map.addControl(new BMap.NavigationControl());
  const marker = new BMap.Marker(point);
  map.addOverlay(marker);
  */
};

watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      location.value = null;
      relatedLocations.value = [];
      loading.value = true;
      error.value = null;
      fetchLocationDetail();
    }
  }
);

onMounted(() => {
  updateDeviceDetection();
  fetchLocationDetail();
  window.addEventListener('resize', updateDeviceDetection);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.location-detail-page {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.main-content {
  padding: 0px;
  margin-left: 210px;
  display: flex;
  flex-direction: column;
}

.main-content.full-width {
  margin-left: 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.error-message {
  color: #f56c6c;
  margin-bottom: 20px;
  font-size: 1.1rem;
}

/* 地点详情 */
.location-detail {
  padding: 20px;
}

.back-btn {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  margin-bottom: 20px;
  transition: all 0.3s;
  align-self: flex-start;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 30px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 图片展示区 */
.location-gallery {
  position: relative;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.main-image img:hover {
  transform: scale(1.05);
}

.main-image.placeholder {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
}

.placeholder-icon {
  font-size: 5rem;
  margin-bottom: 20px;
}

.placeholder p {
  font-size: 1.5rem;
  font-weight: 600;
}

/* 地点信息区 */
.location-info-section {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* 标题区域 */
.location-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 20px;
}

.location-title-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.location-name {
  margin: 0;
  font-size: 2rem;
  color: #333;
  font-weight: 700;
}

.popular-tag {
  background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.location-category {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 1.1rem;
}

.category-icon {
  font-size: 1.3rem;
}

.category-name {
  font-weight: 500;
}

/* 信息行 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.info-row {
  display: flex;
  gap: 10px;
  line-height: 1.6;
}

.info-label {
  font-weight: 600;
  color: #555;
  flex-shrink: 0;
}

.info-value {
  color: #333;
}

/* 设施标签 */
.facilities-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 1.2rem;
  color: #333;
  font-weight: 600;
}

.facilities-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.facility-tag {
  background: #e3f2fd;
  color: #1976d2;
  border: none;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
}

.action-btn {
  flex: 1;
  height: 45px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
}

/* 地图预览 */
.map-preview-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.mini-map {
  width: 100%;
  height: 250px;
  background: #e0e0e0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.map-loading-mini {
  color: #666;
  font-size: 0.9rem;
}

/* 相关地点 */
.related-section {
  margin-top: 30px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.related-card {
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.related-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #e0e0e0;
}

.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.related-info {
  padding: 15px;
}

.related-name {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  color: #333;
  font-weight: 600;
}

.related-category {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.distance {
  margin: 8px 0 0 0;
  color: #4A90E2;
  font-size: 0.85rem;
  font-weight: 500;
}

/* 图片预览弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  padding: 20px;
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 40px;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #4A90E2;
}

.preview-large-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0;
  }

  .detail-content {
    grid-template-columns: 1fr;
    margin: 15px;
    padding: 20px;
  }

  .back-btn {
    margin: 15px;
  }

  .main-image {
    height: 300px;
  }

  .location-name {
    font-size: 1.5rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .related-grid {
    grid-template-columns: 1fr;
  }
}
</style>
