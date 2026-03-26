<template>
  <div :class="['user-location-detail-page', { 'mobile': isMobile }]">
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
      <div v-else-if="locationMark" class="location-detail">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          ← 返回
        </button>

        <div :class="['detail-content', { 'mobile-layout': isMobile }]">
          <!-- 左侧：地点图片 -->
          <div class="location-gallery">
            <div v-if="locationMark.images && locationMark.images.length > 0" class="main-image">
              <img :src="locationMark.images[0]" :alt="locationMark.locationName" @click="previewImage" />
            </div>
            <div v-else class="main-image placeholder">
              <div class="placeholder-icon">📍</div>
              <p>{{ locationMark.locationName }}</p>
            </div>
          </div>

          <!-- 右侧：地点信息 -->
          <div class="location-info-section">
            <!-- 标题和分类 -->
            <div class="location-header">
              <div class="location-title-row">
                <h1 class="location-name">{{ locationMark.locationName }}</h1>
                <span v-if="locationMark.verificationStatus === 'approved'" class="approved-tag">✓ 已审核</span>
                <span v-else-if="locationMark.verificationStatus === 'pending'" class="pending-tag">审核中</span>
              </div>
              <div class="location-meta">
                <span class="mark-type">{{ getMarkTypeName(locationMark.markType) }}</span>
                <span class="category-tag">{{ getMarkCategoryName(locationMark.markCategory) }}</span>
              </div>
            </div>

            <!-- 用户信息 -->
            <div class="publisher-info">
              <div class="publisher-avatar" @click="goToUserProfile">
                <img v-if="publisherAvatar" :src="publisherAvatar" alt="发布者" />
                <div v-else class="avatar-placeholder">👤</div>
              </div>
              <div class="publisher-info-text">
                <div class="publisher-name">{{ publisherName || '匿名用户' }}</div>
                <div class="publish-time">{{ formatTime(locationMark.createdAt) }}</div>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="info-section">
              <div class="info-row">
                <span class="info-label">📍 地址：</span>
                <span class="info-value">{{ locationMark.addressDetail || '暂无详细地址' }}</span>
              </div>
              <div v-if="locationMark.description" class="info-row">
                <span class="info-label">📝 描述：</span>
                <span class="info-value">{{ locationMark.description }}</span>
              </div>
              <div v-if="locationMark.contactInfo" class="info-row">
                <span class="info-label">📞 联系方式：</span>
                <span class="info-value">{{ locationMark.contactInfo }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">👁️ 可见性：</span>
                <span class="info-value">
                  <span v-if="locationMark.visibility === 'public_active'" class="visibility-public">
                    公开（主动所有人可见）
                  </span>
                  <span v-else-if="locationMark.visibility === 'public_passive'" class="visibility-passive">
                    公开（被动可见）
                  </span>
                  <span v-else-if="locationMark.visibility === 'private'" class="visibility-private">
                    仅自己可见
                  </span>
                </span>
              </div>
            </div>

            <!-- 图片列表 -->
            <div v-if="locationMark.images && locationMark.images.length > 1" class="image-gallery">
              <h3 class="section-title">更多图片</h3>
              <div class="image-list">
                <img 
                  v-for="(img, index) in locationMark.images.slice(1)" 
                  :key="index"
                  :src="img"
                  :alt="`${locationMark.locationName} - ${index + 2}`"
                  @click="() => previewImage(img)"
                  class="thumbnail-image"
                />
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="primary" @click="navigateToLocation" class="action-btn">
                🗺️ 导航到这里
              </el-button>
              <el-button v-if="isOwner" @click="editLocationMark" class="action-btn">
                ✏️ 编辑
              </el-button>
              <el-button v-if="isOwner" @click="deleteLocationMark" class="action-btn" type="danger">
                🗑️ 删除
              </el-button>
            </div>

            <!-- 地图预览 -->
            <div class="map-preview-section">
              <h3 class="section-title">位置预览</h3>
              <div class="mini-map" ref="miniMapContainer">
                <div v-if="!miniMapLoaded" class="map-loading-mini">
                  正在加载地图...
                </div>
                <div v-else class="mini-map-content">
                  <!-- 手绘地图底图 -->
                  <img 
                    src="@/assets/01.png" 
                    alt="校园手绘地图" 
                    class="mini-map-image"
                    draggable="false"
                  />
                  <!-- 位置标记 -->
                  <div 
                    v-if="locationMark.latitude && locationMark.longitude" 
                    class="location-marker"
                    :style="markerStyle"
                  >
                    <span class="marker-icon">📍</span>
                    <span class="marker-label">{{ locationMark.locationName }}</span>
                  </div>
                </div>
              </div>
              <div class="map-actions">
                <el-button 
                  size="small" 
                  @click="openFullMap"
                  type="primary"
                >
                  🗺️ 查看完整地图
                </el-button>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import { campusAPI } from '@/api/campus';

const route = useRoute();
const router = useRouter();

// 设备检测
const isMobile = ref(window.innerWidth < 768);
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  updateDeviceDetection();
  window.addEventListener('resize', updateDeviceDetection);
  loadLocationMark();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});

// 数据
const loading = ref(false);
const error = ref(null);
const locationMark = ref(null);
const miniMapLoaded = ref(false);
const miniMapContainer = ref(null);
const publisherName = ref('');
const publisherAvatar = ref('');

// 计算属性
const currentUserId = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.id || null;
});

const isOwner = computed(() => {
  return currentUserId.value === locationMark.value?.userId;
});

// 地图标记样式
const markerStyle = computed(() => {
  if (!locationMark.value?.latitude || !locationMark.value?.longitude) return {};
  
  // 简化的坐标转换（实际项目需要更精确的计算）
  const centerLat = 35.307736;
  const centerLng = 113.926765;
  const latRange = 0.01;
  const lngRange = 0.01;
  
  const x = 50 + ((locationMark.value.longitude - centerLng) / lngRange) * 100;
  const y = 50 - ((locationMark.value.latitude - centerLat) / latRange) * 100;
  
  return {
    left: `${Math.max(0, Math.min(100, x))}%`,
    top: `${Math.max(0, Math.min(100, y))}%`
  };
});

// 加载地点标记详情
const loadLocationMark = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await campusAPI.getLocationMarkDetail(route.params.id);
    console.log('地点标记详情响应:', response);
    
    // 后端返回的数据结构是 { mark: {...}, publisher: {...} }
    if (response && response.mark) {
      locationMark.value = response.mark;
      
      // 加载发布者信息
      if (response.publisher) {
        publisherName.value = response.publisher.username || '匿名用户';
        publisherAvatar.value = response.publisher.avatarUrl || '';
        
        // 判断是否是系统管理员（通过 userId 是否为 1 或者特殊标识）
        if (response.publisher.userId === 1) {
          publisherName.value = '系统管理员';
        }
      } else {
        // 如果没有发布者信息，显示系统管理员
        publisherName.value = '系统管理员';
        publisherAvatar.value = '';
      }
    } else {
      locationMark.value = response;
      // 兼容旧的数据结构
      await loadPublisherInfo();
    }
    
    // 初始化地图
    initMiniMap();
  } catch (err) {
    console.error('加载地点标记失败:', err);
    error.value = err.response?.data?.message || '加载失败';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 初始化小地图
const initMiniMap = () => {
  if (miniMapContainer.value) {
    miniMapLoaded.value = true;
  }
};

// 获取标记类型名称
const getMarkTypeName = (markType) => {
  const names = {
    'meeting_point': '约见地点',
    'merchant_shop': '店铺位置',
    'organization_activity': '活动地点'
  };
  return names[markType] || '位置标记';
};

// 获取地点分类名称
const getMarkCategoryName = (category) => {
  const names = {
    'building': '建筑物',
    'area': '区域',
    'facility': '设施',
    'other': '其他'
  };
  return names[category] || '';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN');
};

// 操作处理
const goBack = () => {
  router.back();
};

const previewImage = (imageUrl) => {
  // 可以添加图片预览弹窗
  console.log('预览图片:', imageUrl || locationMark.value?.images?.[0]);
};

const navigateToLocation = () => {
  if (locationMark.value?.latitude && locationMark.value?.longitude) {
    const url = `https://uri.amap.com/marker?position=${locationMark.value.longitude},${locationMark.value.latitude}&name=${encodeURIComponent(locationMark.value.locationName)}`;
    window.open(url, '_blank');
  } else {
    ElMessage.warning('该地点暂无坐标信息');
  }
};

const editLocationMark = () => {
  // TODO: 打开编辑弹窗
  ElMessage.info('编辑功能开发中...');
};

const deleteLocationMark = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个地点标记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await campusAPI.deleteLocationMark(locationMark.value.id);
    ElMessage.success('删除成功');
    goBack();
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err);
      ElMessage.error('删除失败，请稍后重试');
    }
  }
};

const goToUserProfile = () => {
  if (locationMark.value?.userId) {
    router.push(`/user/profile/${locationMark.value.userId}`);
  }
};

const openFullMap = () => {
  router.push('/map');
};
</script>

<style scoped>
.user-location-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.back-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #f5f5f5;
}

.detail-content {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.location-gallery .main-image {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
}

.location-gallery .main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.location-gallery .placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.placeholder-icon {
  font-size: 80px;
  color: #ccc;
}

.location-header {
  margin-bottom: 24px;
}

.location-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.location-name {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.approved-tag {
  padding: 4px 12px;
  background: #e8f5e9;
  color: #4caf50;
  border-radius: 20px;
  font-size: 14px;
}

.pending-tag {
  padding: 4px 12px;
  background: #fff3e0;
  color: #ff9800;
  border-radius: 20px;
  font-size: 14px;
}

.location-meta {
  display: flex;
  gap: 8px;
}

.mark-type, .category-tag {
  padding: 6px 12px;
  background: #f0f0f0;
  border-radius: 16px;
  font-size: 14px;
  color: #666;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 12px;
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s;
}

.publisher-info:hover {
  background: #f0f0f0;
}

.publisher-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.publisher-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e0e0e0;
  font-size: 24px;
}

.publisher-info-text {
  flex: 1;
}

.publisher-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.publish-time {
  font-size: 14px;
  color: #999;
}

.info-section {
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.info-label {
  font-weight: 600;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  flex: 1;
}

.visibility-public {
  color: #4caf50;
  font-weight: 600;
}

.visibility-passive {
  color: #2196f3;
  font-weight: 600;
}

.visibility-private {
  color: #9e9e9e;
  font-weight: 600;
}

.image-gallery {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.thumbnail-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-image:hover {
  transform: scale(1.05);
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  flex: 1;
}

.map-preview-section {
  margin-top: 24px;
}

.mini-map {
  width: 100%;
  height: 300px;
  background: #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.mini-map-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  user-select: none;
}

.location-marker {
  position: absolute;
  transform: translate(-50%, -100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  pointer-events: none;
}

.marker-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}

.marker-label {
  font-size: 12px;
  color: #333;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
}

.map-actions {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0;
  }
  
  .detail-content {
    grid-template-columns: 1fr;
    border-radius: 0;
    padding: 16px;
  }
  
  .location-gallery .main-image {
    height: 250px;
  }
  
  .location-name {
    font-size: 22px;
  }
}
</style>
