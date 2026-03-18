<template>
  <div :class="['map-page', { 'mobile': isMobile }]">
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
      <!-- 头部 - 移动端显示标题在搜索栏上方 -->
      <header class="header">
        <h1 v-if="isMobile">地图导引</h1>
        <div class="search-bar">
          <!-- 使用统一搜索组件 -->
          <UnifiedSearch
            v-model="searchQuery"
            :available-tags="locationTags"
            :enable-result-page="true"
            :default-search-type="'location'"
            placeholder="搜索地点、建筑..."
            tag-selector-title="选择地点类型"
            :show-quick-filters="false"
            :show-clear-button="false"
            @search="handleSearch"
          />
        </div>
      </header>

      <!-- 地图展示区域 -->
      <section class="map-section">
        <div class="section-header">
          <div class="campus-selector-header">
            <h2>{{ selectedCampusName }}地图</h2>
            <div class="campus-options-inline">
              <div
                  v-for="campus in campuses"
                  :key="campus.id"
                  :class="['campus-option-inline', { active: selectedCampus === campus.id }]"
                  @click="selectCampus(campus.id)"
              >
                {{ campus.name }}
              </div>
            </div>
          </div>
          <div v-if="!isMobile" class="map-controls">
            <select v-model="selectedMapProvider" @change="onMapProviderChange" class="map-provider-select">
              <option value="baidu">百度地图</option>
              <option value="gaode">高德地图</option>
              <option value="tencent">腾讯地图</option>
            </select>
            <button class="view-more-btn" @click="goToPage('full-map')">
              查看全图
            </button>
          </div>
          <span v-else class="view-more" @click="goToPage('full-map')">
            查看全图 >
          </span>
        </div>

        <!-- 地图容器 -->
        <div class="map-container" ref="mapContainer">
          <div v-if="mapLoading" class="map-loading">
            <div class="loading-spinner"></div>
            <p>正在加载{{ currentMapProviderName }}...</p>
          </div>
          <div v-else-if="mapError" class="map-error">
            <p>{{ mapError }}</p>
            <button @click="initMap">重新加载</button>
          </div>
          <div v-else class="map-content">
            <div class="map-placeholder">
              <p>{{ selectedCampusName }}地图展示区</p>
              <p>支持缩放、拖拽、地点搜索等功能</p>
              <p class="map-provider-info">当前使用：{{ currentMapProviderName }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门地点和快速导航合并区域 -->
      <section class="combined-nav-section">
        <div class="section-header">
          <h2>校园导航</h2>
        </div>

        <div class="combined-grid">
          <div
              v-for="(item, index) in combinedItems"
              :key="index"
              class="nav-item-card"
              @click="handleItemClick(item)"
          >
            <div class="nav-icon-large">{{ item.icon }}</div>
            <div class="nav-text">{{ item.name }}</div>
          </div>
        </div>
      </section>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>

    <!-- 校区选择模态框（当用户没有默认校区时显示） -->
    <div v-if="showCampusModal" class="modal-overlay" @click="closeCampusModal">
      <div class="campus-modal" @click.stop>
        <h2>请选择校区</h2>
        <div class="campus-options-large">
          <div
              v-for="campus in campuses"
              :key="campus.id"
              :class="['campus-option-large', { active: tempSelectedCampus === campus.id }]"
              @click="tempSelectCampus(campus.id)"
          >
            {{ campus.name }}
          </div>
        </div>
        <button class="confirm-campus-btn" @click="confirmCampusSelection">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { campusAPI } from '@/api/campus';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import UnifiedSearch from '@/components/common/UnifiedSearch.vue';
import { MAP_PROVIDERS, loadMapScript, formatLocationData } from '@/utils/mapHelper';

const router = useRouter(); // 创建路由实例

// 用户信息
const currentUser = ref({
  name: '',
  studentId: '',
  avatar: ''
});

// 获取用户信息
const getUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  currentUser.value = {
    name: user.name || user.username || '',
    studentId: user.studentId || user.student_id || '',
    avatar: user.avatar || ''
  };
};

// 设备检测相关
const isMobile = ref(false);

// 更新设备检测状态
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  updateDeviceDetection();
  getUserInfo();
  window.addEventListener('resize', updateDeviceDetection);

  // 检查用户校区信息
  checkUserCampusInfo();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});

// 校区数据
const campuses = ref([]);
const locations = ref([]);
const loading = ref(false);

// 用户默认校区
const userDefaultCampus = ref(null);
const selectedCampus = ref(1); // 默认选中本部校区（ID=1）
const showCampusModal = ref(false);
const tempSelectedCampus = ref(null);

// 地图相关
const mapProvider = ref('gaode'); // 默认使用高德地图
const selectedMapProvider = ref('gaode');
const mapInstance = ref(null);
const mapLoading = ref(false);
const mapError = ref('');
const mapContainer = ref(null);

// 获取当前地图服务商名称
const currentMapProviderName = computed(() => {
    const provider = MAP_PROVIDERS[selectedMapProvider.value.toUpperCase()];
    return provider ? provider.name : '地图';
});

// 获取选中校区的名称
const selectedCampusName = computed(() => {
  const campus = campuses.value.find(c => c.id === selectedCampus.value);
  return campus ? campus.name : '校园';
});

// 获取校区列表
const fetchCampuses = async () => {
  try {
    const response = await campusAPI.getCampuses();
    campuses.value = response;
    
    // 如果有默认校区，加载地图配置
    if (selectedCampus.value) {
      await loadMapConfig(selectedCampus.value);
    }
  } catch (error) {
    console.error('获取校区列表失败:', error);
    ElMessage.error('获取校区信息失败');
  }
};

// 加载地图配置
const loadMapConfig = async (campusId) => {
  try {
    const config = await campusAPI.getMapConfig(campusId, selectedMapProvider.value);
    if (config && config.latitude && config.longitude) {
      await initMap(config);
    }
  } catch (error) {
    console.error('加载地图配置失败:', error);
    // 使用默认配置初始化地图
    await initMap();
  }
};

// 初始化地图
const initMap = async (config = null) => {
  mapLoading.value = true;
  mapError.value = '';
  
  try {
    // 加载地图脚本
    await loadMapScript(selectedMapProvider.value);
    
    // 根据地图服务商初始化地图
    const provider = selectedMapProvider.value.toLowerCase();
    const defaultLat = 35.307736;
    const defaultLng = 113.926765;
    const defaultZoom = 15;
    
    const lat = config?.latitude || defaultLat;
    const lng = config?.longitude || defaultLng;
    const zoom = config?.zoom || defaultZoom;
    
    if (provider === 'baidu' && window.BMap) {
      // 百度地图
      mapInstance.value = new window.BMap.Map(mapContainer.value);
      const point = new window.BMap.Point(lng, lat);
      mapInstance.value.centerAndZoom(point, zoom);
      mapInstance.value.enableScrollWheelZoom();
    } else if (provider === 'gaode' && window.AMap) {
      // 高德地图
      mapInstance.value = new window.AMap.Map(mapContainer.value, {
        zoom: zoom,
        center: [lng, lat],
        viewMode: '3D'
      });
    } else if (provider === 'tencent' && window.qq) {
      // 腾讯地图
      mapInstance.value = new window.qq.maps.Map(mapContainer.value, {
        zoom: zoom,
        center: { lat, lng }
      });
    } else {
      // 如果地图 SDK 未加载，显示提示信息
      console.warn(`${currentMapProviderName.value} SDK 未正确加载`);
    }
    
    mapLoading.value = false;
  } catch (error) {
    console.error('初始化地图失败:', error);
    mapError.value = `加载${currentMapProviderName.value}失败，请检查网络连接或 API Key 配置`;
    mapLoading.value = false;
  }
};

// 切换地图服务商
const onMapProviderChange = () => {
  mapInstance.value = null; // 清空地图实例
  if (selectedCampus.value) {
    loadMapConfig(selectedCampus.value); // 重新加载地图
  }
};

// 获取地点信息
const fetchLocations = async (campusId) => {
  loading.value = true;
  try {
    const response = await campusAPI.getCampusLocations(campusId);
    locations.value = response;
  } catch (error) {
    console.error('获取地点信息失败:', error);
    ElMessage.error('获取地点信息失败');
  } finally {
    loading.value = false;
  }
};

// 搜索地点
const searchLocations = async () => {
  if (!searchQuery.value.trim()) return;
  
  try {
    const response = await campusAPI.searchLocations(searchQuery.value, selectedCampus.value);
    locations.value = response;
  } catch (error) {
    console.error('搜索地点失败:', error);
    ElMessage.error('搜索失败');
  }
};

// 页面初始化
onMounted(() => {
  fetchCampuses();
  if (selectedCampus.value) {
    fetchLocations(selectedCampus.value);
  }
});

// 监听校区变化
watch(selectedCampus, (newCampusId) => {
  if (newCampusId) {
    fetchLocations(newCampusId);
    loadMapConfig(newCampusId);
  }
});

// 搜索相关
const searchQuery = ref('');

// 地点标签（用于搜索筛选）
const locationTags = computed(() => {
  return [
    { code: 'teaching', name: '教学楼', color: '#4A90E2' },
    { code: 'library', name: '图书馆', color: '#7B68EE' },
    { code: 'cafeteria', name: '食堂', color: '#FFA500' },
    { code: 'dormitory', name: '宿舍', color: '#32CD32' },
    { code: 'sports', name: '体育设施', color: '#FF6347' },
    { code: 'admin', name: '行政楼', color: '#9370DB' },
    { code: 'other', name: '其他', color: '#808080' }
  ];
});

// 处理搜索
const handleSearch = (searchData) => {
  console.log('执行搜索:', searchData);
  // 统一搜索组件会自动跳转到搜索结果页
};

// 合并后的导航项目
const combinedItems = computed(() => {
  // 根据当前校区显示不同的热门地点
  const campusHotLocations = getCampusHotLocations(selectedCampus.value);
  const quickNavItems = getQuickNavItems();

  return [...campusHotLocations, ...quickNavItems];
});

// 检查用户校区信息
const checkUserCampusInfo = () => {
  // 从本地存储获取用户信息
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // 从用户信息中获取默认校区（实际项目中可能需要从后端获取）
  const storedCampus = localStorage.getItem('defaultCampus');

  if (storedCampus) {
    // 如果是字符串编码，需要转换为数字 ID
    if (typeof storedCampus === 'string') {
      // 根据校区编码查找对应的校区 ID
      const campus = campuses.value.find(c => c.code === storedCampus);
      if (campus) {
        selectedCampus.value = campus.id;
      } else {
        selectedCampus.value = 1; // 默认本部校区
      }
    } else {
      selectedCampus.value = storedCampus;
    }
  } else {
    // 如果没有默认校区信息，显示校区选择模态框
    showCampusModal.value = true;
    tempSelectedCampus.value = 1; // 默认临时选择本部校区
  }
};

// 获取当前校区的热门地点
const getCampusHotLocations = (campusId) => {
  // 从 locations 数据中筛选热门地点
  const campusLocations = locations.value.filter(loc => loc.campusId === campusId);
  
  // 优先显示热门地点（isPopular=true），如果没有则取前 4 个
  const popularLocations = campusLocations.filter(loc => loc.isPopular);
  
  if (popularLocations.length > 0) {
    return popularLocations.slice(0, 4).map(loc => ({
      id: loc.id,  // 使用真实的数据库 ID
      name: loc.name,
      description: loc.description || '',
      icon: loc.icon || '📍',
      type: 'location'
    }));
  }
  
  // 如果没有热门地点，返回前 4 个地点
  return campusLocations.slice(0, 4).map(loc => ({
    id: loc.id,  // 使用真实的数据库 ID
    name: loc.name,
    description: loc.description || '',
    icon: loc.icon || '📍',
    type: 'location'
  }));
};

// 获取快速导航项目
const getQuickNavItems = () => {
  return [
    { id: 'dormitory', icon: '🏠', name: '宿舍区', type: 'nav' },
    { id: 'canteen', icon: '🍽️', name: '食堂', type: 'nav' },
    { id: 'library', icon: '📚', name: '图书馆', type: 'nav' },
    { id: 'teaching', icon: '🏫', name: '教学楼', type: 'nav' },
    { id: 'sports', icon: '⚽', name: '体育设施', type: 'nav' },
    { id: 'admin', icon: '🏢', name: '行政楼', type: 'nav' }
  ];
};

// 选择校区
const selectCampus = (campusId) => {
  selectedCampus.value = campusId;
  localStorage.setItem('defaultCampus', campusId);
};

// 临时选择校区（用于模态框）
const tempSelectCampus = (campusId) => {
  tempSelectedCampus.value = campusId;
};

// 确认校区选择
const confirmCampusSelection = () => {
  if (tempSelectedCampus.value) {
    selectedCampus.value = tempSelectedCampus.value;
    localStorage.setItem('defaultCampus', tempSelectedCampus.value);
    showCampusModal.value = false;
  }
};

// 关闭校区选择模态框
const closeCampusModal = () => {
  showCampusModal.value = false;
  // 如果用户没有选择校区，默认选中本部校区
  if (!selectedCampus.value) {
    selectedCampus.value = 1;
    localStorage.setItem('defaultCampus', 1);
  }
};

// 处理导航项目点击
const handleItemClick = (item) => {
  if (item.type === 'location') {
    goToLocation(item.id);
  } else if (item.type === 'nav') {
    goToNav(item.id);
  }
};

// 地点跳转功能
const goToLocation = (locationId) => {
  console.log(`跳转到地点：${locationId}`);
  // 跳转到地点详情页
  router.push(`/location/${locationId}`);
};

// 导航跳转功能
const goToNav = (navId) => {
  console.log(`导航到: ${navId}`);
  // 实际项目中可以导航到特定类别区域
};

// 导航相关
const goToPage = (page) => {
  console.log(`跳转到${page}页面`);
  switch(page) {
    case 'home':
      router.push('/');
      break;
    case 'map':  // 这是当前页面，可以刷新或滚动到顶部
      router.push('/map');
      break;
    case 'trade':  // 跳转到交易中心
      router.push('/mall');
      break;
    case 'topic':  // 跳转到话题墙
      router.push('/topicwall');
      break;
    case 'message':  // 跳转到消息中心
      router.push('/message');
      break;
    case 'profile':  // 跳转到个人中心
      router.push('/personalcenter');
      break;
    case 'full-map':  // 查看全图功能
      // 可以实现地图全屏查看功能
      console.log('查看全图');
      break;
    default:
      console.log(`未知页面: ${page}`);
  }
};
</script>

<style scoped>
.map-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: grid;
  grid-template-columns: 180px 1fr; /* 缩窄侧边栏宽度 */
  grid-template-areas: "sidebar main";
}

.map-page.mobile {
  grid-template-columns: 1fr;
  grid-template-areas: "main";
  padding-bottom: 80px; /* 为移动底部导航留出空间 */
}

/* 统一导航栏样式 */
.nav-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-menu li {
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  color: #666;
  font-size: 0.95rem;
  font-weight: 500;
}

.nav-menu li:hover {
  background-color: #f0f7ff;
  color: #4A90E2;
  transform: translateX(5px);
}

.nav-menu li.active {
  background-color: #4A90E2;
  color: white;
  font-weight: 600;
}

.nav-menu span:first-child {
  font-size: 1.2rem;
  min-width: 24px;
  text-align: center;
}

/* 侧边栏样式优化 */
.sidebar {
  grid-area: sidebar;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e0e0e0;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1001;
  width: 200px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
}

.logo h1 {
  color: #4A90E2;
  margin: 0 0 25px 0;
  font-size: 1.3rem;
  font-weight: 600;
  text-align: center;
}

/* 用户信息区域样式 */
.user-info {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f8f9fa;
}

.user-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #4A90E2;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-id {
  color: #666;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 主内容区域 */
.main-content {
  grid-area: main;
  padding: 20px;
  overflow-y: auto;
  margin-left: 200px; /* 与侧边栏宽度一致，避免内容被覆盖 */
}

/* 通用部分样式 */
.section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

/* 校区选择头部样式 */
.campus-selector-header {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.map-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.map-provider-select {
  padding: 6px 12px;
  border: 2px solid #e1e5f2;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  background-color: white;
  color: #667eea;
}

.map-provider-select:hover {
  border-color: #667eea;
}

.map-provider-select:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.campus-options-inline {
  display: flex;
  gap: 10px;
  background-color: #f5f7fa;
  padding: 4px;
  border-radius: 6px;
}

.campus-option-inline {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s;
  color: #667eea; /* 蓝色字体 */
}

.campus-option-inline.active {
  background-color: #4A90E2;
  color: white;
  font-weight: bold;
}

.campus-option-inline:hover {
  background-color: #e3f2fd;
  color: #4A90E2;
}

.view-more {
  color: #4A90E2;
  font-size: 0.9rem;
  cursor: pointer;
}

.view-more-btn {
  background-color: #4A90E2;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

/* 地图容器样式 */
.map-container {
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.map-loading,
.map-error {
  text-align: center;
  color: #666;
  padding: 40px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.map-error button {
  margin-top: 15px;
  padding: 8px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.map-error button:hover {
  background-color: #3a7bc8;
}

.map-content {
  width: 100%;
  height: 100%;
}

.map-placeholder p {
  margin: 10px 0;
  font-size: 1.1rem;
}

.map-provider-info {
  color: #4A90E2;
  font-weight: 500;
  margin-top: 15px;
}

/* 合并导航网格样式 */
.combined-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.nav-item-card {
  background: white;
  border-radius: 8px;
  padding: 20px 10px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.nav-item-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.nav-icon-large {
  font-size: 2rem;
  margin-bottom: 10px;
}

.nav-text {
  font-size: 0.9rem;
  color: #333;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  gap: 10px;
  width: 100%;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-bar input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-bar button {
  padding: 10px 15px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.search-bar button:hover {
  background-color: #764ba2;
}

/* 移动端底部导航样式 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 10px 15px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 4px rgba(0,0,0,0.1);
  z-index: 1000;
  flex-shrink: 0;
}

.mobile-nav .nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.mobile-nav .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.mobile-nav .nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin-bottom: 2px;
}

/* 桌面端底部样式 - 放置在main-content内部底部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

/* 校区选择模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1002;
}

.campus-modal {
  background: white;
  padding: 30px;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.campus-modal h2 {
  margin-top: 0;
  color: #333;
  margin-bottom: 25px;
}

.campus-options-large {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 25px;
}

.campus-option-large {
  padding: 12px 20px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
}

.campus-option-large.active {
  border-color: #4A90E2;
  background-color: #e3f2fd;
  color: #4A90E2;
  font-weight: bold;
}

.campus-option-large:hover {
  border-color: #667eea;
}

.confirm-campus-btn {
  width: 100%;
  padding: 12px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.confirm-campus-btn:hover {
  background-color: #3a7bc8;
}

/* 移动端搜索框样式 */
.mobile-search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.user-avatar-mini img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.mobile-search-header input {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #e1e5f2;
  border-radius: 20px;
  font-size: 0.9rem;
}

/* 移动端布局调整 */
@media (max-width: 767px) {
  .map-page {
    padding-top: 0; /* 移除顶部用户信息空间 */
    padding-bottom: 70px; /* 为移动底部导航留出空间 */
  }
  
  .mobile-user-header {
    display: none; /* 隐藏移动端顶部用户信息 */
  }
  
  .sidebar {
    display: none;
  }
  
  .main-content {
    margin-top: 0; /* 移除顶部间距 */
    margin-left: 0;
    padding: 15px;
  }
  
  .header {
    padding: 15px 20px;
    align-items: stretch;
  }
  
  .header h1 {
    font-size: 1.5rem;
    margin-bottom: 10px;
  }
  
  .section {
    margin: 15px 10px;
    padding: 15px;
  }
  
  .map-container {
    height: 300px;
  }
  
  .map-placeholder {
    font-size: 0.9rem;
  }
  
  .combined-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
  
  .nav-item-card {
    padding: 12px 8px;
    font-size: 0.8rem;
  }
  
  .campus-selector-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .campus-options-inline {
    justify-content: center;
  }
  
  .map-controls {
    width: 100%;
    justify-content: center;
  }
  
  .map-provider-select {
    width: 100%;
    max-width: 200px;
  }
}

/* 移动端搜索框样式 */
.mobile-search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.user-avatar-mini img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.mobile-search-header input {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #e1e5f2;
  border-radius: 20px;
  font-size: 0.9rem;
}
</style>