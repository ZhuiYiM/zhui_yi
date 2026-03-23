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
              <option value="handdrawn">手绘地图</option>
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
            <!-- 手绘地图 -->
            <div v-if="selectedMapProvider === 'handdrawn'" class="handdrawn-map-wrapper">
              <InteractiveMap ref="interactiveMapRef" />
            </div>
            <!-- 其他地图 -->
            <div v-else>
              <div class="map-placeholder">
                <p>{{ selectedCampusName }}地图展示区</p>
                <p>支持缩放、拖拽、地点搜索等功能</p>
                <p class="map-provider-info">当前使用：{{ currentMapProviderName }}</p>
              </div>
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

      <!-- 用户位置标记区域 -->
      <section class="user-marks-section" v-if="userLocationMarks.length > 0">
        <div class="section-header">
          <h2>📍 用户标记位置</h2>
          <el-button type="primary" size="small" @click="showMarkModal = true">
            + 标记我的位置
          </el-button>
        </div>
        
        <div class="user-marks-grid">
          <div
            v-for="mark in userLocationMarks"
            :key="mark.id"
            class="user-mark-card"
            @click="handleUserMarkClick(mark)"
          >
            <div class="mark-icon">{{ getMarkIcon(mark.markType) }}</div>
            <div class="mark-info">
              <div class="mark-name">{{ mark.locationName }}</div>
              <div class="mark-type">{{ getMarkTypeName(mark.markType) }}</div>
              <div class="mark-meta">
                <span v-if="mark.verificationStatus === 'pending'" class="status-pending">审核中</span>
                <span v-else-if="mark.visibility === 'private'" class="status-private">私密</span>
                <span v-else class="status-public">公开</span>
              </div>
            </div>
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

    <!-- 位置标记模态框 -->
    <LocationMarkModal 
      v-model="showMarkModal" 
      :default-campus-id="selectedCampus"
      @success="handleMarkSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { campusAPI } from '@/api/campus';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import UnifiedSearch from '@/components/common/UnifiedSearch.vue';
import InteractiveMap from '@/components/map/InteractiveMap.vue';
import LocationMarkModal from '@/components/map/LocationMarkModal.vue';
import { MAP_PROVIDERS, loadMapScript, formatLocationData } from '@/utils/mapHelper';
import handdrawnMapLocations from '@/data/handdrawn-map-locations.json';

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
  
  // 获取用户位置标记
  fetchUserLocationMarks();
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
const mapProvider = ref('handdrawn'); // 默认使用手绘地图
const selectedMapProvider = ref('handdrawn'); // 默认使用手绘地图
const mapInstance = ref(null);
const mapLoading = ref(false);
const mapError = ref('');
const mapContainer = ref(null);

// 手绘地图相关
const hoveredZoneId = ref(null);
const handDrawnLocations = ref([]);
const interactiveMapRef = ref(null);

// 用户位置标记相关
const userLocationMarks = ref([]);
const showMarkModal = ref(false);

// 获取当前地图服务商名称
const currentMapProviderName = computed(() => {
    const provider = MAP_PROVIDERS[selectedMapProvider.value.toUpperCase()];
    return provider ? provider.name : selectedMapProvider.value === 'handdrawn' ? '手绘地图' : '地图';
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
  // 如果是手绘地图，不需要加载地图 SDK
  if (selectedMapProvider.value === 'handdrawn') {
    mapLoading.value = false;
    return;
  }
  
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
    // 加载手绘地图标注点
    loadHandDrawnLocations(response);
  } catch (error) {
    console.error('获取地点信息失败:', error);
    ElMessage.error('获取地点信息失败');
  } finally {
    loading.value = false;
  }
};

// 加载手绘地图地点数据 - 从数据库 locations 转换为 InteractiveMap 可用的格式
const loadHandDrawnLocations = (locations) => {
  if (!locations || locations.length === 0) {
    handDrawnLocations.value = [];
    return;
  }
  
  // 将数据库地点数据转换为 InteractiveMap 可用的格式
  // 注意：这里需要从数据库的经纬度转换为百分比坐标
  // 但由于我们已经有 handdrawn-map-locations.json，暂时不使用这个转换
  handDrawnLocations.value = [];
};

// 显示地点提示
const showLocationTooltip = (location) => {
  hoveredZoneId.value = location;
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

// 获取用户位置标记
const fetchUserLocationMarks = async () => {
  try {
    // 获取当前校区的公开标记
    const response = await campusAPI.getCampusLocationMarks(selectedCampus.value);
    if (response && Array.isArray(response)) {
      userLocationMarks.value = response.filter(mark => mark.verificationStatus === 'approved');
    }
  } catch (error) {
    console.error('获取用户标记失败:', error);
  }
};

// 处理用户标记点击
const handleUserMarkClick = (mark) => {
  console.log('点击用户标记:', mark);
  // TODO: 显示标记详情或跳转到详情页
  ElMessage.info(`查看：${mark.locationName}`);
};

// 获取标记类型图标
const getMarkIcon = (markType) => {
  const icons = {
    meeting_point: '🤝',
    merchant_shop: '🏪',
    organization_activity: '👥'
  };
  return icons[markType] || '📍';
};

// 获取标记类型名称
const getMarkTypeName = (markType) => {
  const names = {
    meeting_point: '约见地点',
    merchant_shop: '店铺位置',
    organization_activity: '活动地点'
  };
  return names[markType] || '位置标记';
};

// 处理标记成功
const handleMarkSuccess = (data) => {
  console.log('标记创建成功:', data);
  // 重新加载用户标记
  fetchUserLocationMarks();
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
  // 从 locations 数据中筛选热门地点 (注意：后端返回的字段是 campus_id)
  const campusLocations = locations.value.filter(loc => {
    // 兼容两种字段命名：campus_id 或 campusId
    return (loc.campus_id || loc.campusId) === campusId;
  });
  
  // 优先显示热门地点（isPopular=true），如果没有则取前 4 个
  const popularLocations = campusLocations.filter(loc => loc.is_popular || loc.isPopular);
  
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

// 获取快速导航项目 - 显示具体建筑位置而不是分类
const getQuickNavItems = () => {
  // 优先从 handdrawn-map-locations.json 中选取代表性建筑
  // 因为 JSON 中包含了所有在地图上标注的具体位置（如西寓 2、3、4、5 等）
  const selectedBuildings = [];
  const categories = new Set();
  
  // 按顺序选取不同类型的建筑（每个类型最多 1 个）
  for (const loc of handdrawnMapLocations) {
    const category = loc.category || '';
    
    // 跳过没有分类或已选过的分类
    if (!category || categories.has(category)) {
      continue;
    }
    
    // 添加该分类的代表性建筑
    categories.add(category);
    selectedBuildings.push({
      id: loc.id,
      name: loc.name, // 使用具体建筑名称（如：西寓 2、图书馆等）
      description: loc.description || '',
      icon: loc.icon || '📍',
      type: 'location', // 改为 location 类型，点击跳转到具体地点详情
      category: category,
      x: loc.x,
      y: loc.y
    });
    
    // 限制最多显示 6 个建筑
    if (selectedBuildings.length >= 6) {
      break;
    }
  }
  
  // 如果 JSON 数据不足 6 个，从数据库 locations 中补充
  if (selectedBuildings.length < 6) {
    const campusLocations = locations.value.filter(loc => {
      return (loc.campus_id || loc.campusId) === selectedCampus.value;
    });
    
    for (const loc of campusLocations) {
      const category = loc.category || '';
      
      // 跳过已选过的分类
      if (!category || categories.has(category)) {
        continue;
      }
      
      categories.add(category);
      selectedBuildings.push({
        id: loc.id,
        name: loc.name,
        description: loc.description || '',
        icon: loc.icon || '📍',
        type: 'location',
        category: category
      });
      
      // 达到 6 个后退出
      if (selectedBuildings.length >= 6) {
        break;
      }
    }
  }
  
  return selectedBuildings;
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
    // 传递 category 参数（如果有）
    goToNav(item.id, item.category);
  }
};

// 地点跳转功能
const goToLocation = (locationId) => {
  console.log(`跳转到地点：${locationId}`);
  // 跳转到地点详情页
  router.push(`/location/${locationId}`);
};

// 导航跳转功能
const goToNav = (navId, category) => {
  console.log(`导航到：${navId}, 分类：${category}`);
  
  // 使用传入的 category 参数，如果没有则使用 navId
  const targetCategory = category || navId;
  
  // 根据导航分类筛选地点
  const filteredLocations = locations.value.filter(loc => {
    // 按分类筛选，兼容 campus_id 和 campusId
    const locCategory = loc.category || '';
    return locCategory === targetCategory;
  });
  
  if (filteredLocations.length === 0) {
    ElMessage.warning(`未找到${getNavName(navId)}相关地点`);
    return;
  }
  
  // 切换到手绘地图
  if (selectedMapProvider.value !== 'handdrawn') {
    selectedMapProvider.value = 'handdrawn';
  }
  
  // 滚动到地图区域
  const mapSection = document.querySelector('.map-section');
  if (mapSection) {
    mapSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
  
  // 显示提示消息
  ElMessage.success(`找到 ${filteredLocations.length} 个${getNavName(navId)}地点`);
  
  // 调用 InteractiveMap 组件的搜索方法
  if (interactiveMapRef.value && interactiveMapRef.value.searchByCategory) {
    // 等待下一帧，确保组件已经切换到手绘地图
    setTimeout(() => {
      interactiveMapRef.value.searchByCategory(targetCategory, filteredLocations);
    }, 100);
  }
};

// 获取导航分类名称
const getNavName = (navId) => {
  const navNames = {
    'teaching': '教学楼',
    'library': '图书馆',
    'cafeteria': '食堂',
    'dormitory': '宿舍',
    'sports': '体育设施',
    'admin': '行政楼',
    'other': '其他'
  };
  return navNames[navId] || navId;
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
  margin-left: 40px; /* 与侧边栏宽度一致，避免内容被覆盖 */
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
  height: 750px;  /* 增加高度，使用视口高度计算 */

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

/* 手绘地图样式 */
.handdrawn-map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: visible;  /* 改为 visible，允许缩放控制按钮显示 */
}

.handdrawn-map-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #f0f9ff;
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

/* 用户位置标记区域样式 */
.user-marks-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.user-marks-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.user-marks-section h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0;
}

.user-marks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.user-mark-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-mark-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.mark-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.mark-info {
  flex: 1;
  min-width: 0;
}

.mark-name {
  font-size: 1rem;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mark-type {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 6px;
}

.mark-meta {
  display: flex;
  gap: 6px;
}

.status-pending,
.status-private,
.status-public {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.status-pending {
  background: rgba(255, 193, 7, 0.3);
}

.status-private {
  background: rgba(158, 158, 158, 0.3);
}

.status-public {
  background: rgba(76, 175, 80, 0.3);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .user-marks-section {
    padding: 16px;
    margin: 10px;
  }
  
  .user-marks-grid {
    grid-template-columns: 1fr;
  }
  
  .user-marks-section .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>