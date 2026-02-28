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
          <div v-if="isMobile" class="mobile-search-header">
            <div class="user-avatar-mini">
              <img :src="currentUser.avatar || 'https://placehold.co/24x24/4A90E2/FFFFFF?text=' + (currentUser.name?.charAt(0) || 'U')" :alt="currentUser.name">
            </div>
            <input
                type="text"
                v-model="searchQuery"
                :placeholder="`搜索${selectedCampusName}地点、建筑...`"
                @keyup.enter="performSearch"
            >
          </div>
          <div v-else class="desktop-search">
            <input
                type="text"
                v-model="searchQuery"
                :placeholder="`搜索${selectedCampusName}地点、建筑...`"
                @keyup.enter="performSearch"
            >
            <button @click="performSearch">搜索</button>
          </div>
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
          <button v-if="!isMobile" class="view-more-btn" @click="goToPage('full-map')">
            查看全图
          </button>
          <span v-else class="view-more" @click="goToPage('full-map')">
            查看全图 >
          </span>
        </div>

        <!-- 地图容器 -->
        <div class="map-container">
          <div class="map-placeholder">
            <p>{{ selectedCampusName }}地图展示区</p>
            <p>支持缩放、拖拽、地点搜索等功能</p>
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
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { campusAPI } from '@/api/campus';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';

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

// 获取校区列表
const fetchCampuses = async () => {
  try {
    const response = await campusAPI.getCampuses();
    campuses.value = response;
  } catch (error) {
    console.error('获取校区列表失败:', error);
    ElMessage.error('获取校区信息失败');
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

// 用户默认校区
const userDefaultCampus = ref(null);
const selectedCampus = ref('main'); // 默认选中本部校区
const showCampusModal = ref(false);
const tempSelectedCampus = ref('');

// 获取选中校区的名称
const selectedCampusName = computed(() => {
  const campus = campuses.value.find(c => c.id === selectedCampus.value);
  return campus ? campus.name : '校园';
});

// 合并后的导航项目
const combinedItems = computed(() => {
  // 根据当前校区显示不同的热门地点
  const campusHotLocations = getCampusHotLocations(selectedCampus.value);
  const quickNavItems = getQuickNavItems();

  return [...campusHotLocations, ...quickNavItems];
});

// 搜索相关
const searchQuery = ref('');
const performSearch = () => {
  console.log('搜索:', searchQuery.value);
  // 实际项目中可以调用地图搜索API
};

// 检查用户校区信息
const checkUserCampusInfo = () => {
  // 从本地存储获取用户信息
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // 从用户信息中获取默认校区（实际项目中可能需要从后端获取）
  const storedCampus = localStorage.getItem('defaultCampus');

  if (storedCampus) {
    selectedCampus.value = storedCampus;
  } else {
    // 如果没有默认校区信息，显示校区选择模态框
    showCampusModal.value = true;
    tempSelectedCampus.value = 'main'; // 默认临时选择本部校区
  }
};

// 获取当前校区的热门地点
const getCampusHotLocations = (campusId) => {
  const locations = {
    main: [
      { id: 'lib-main', name: "图书馆", description: "校园最大图书资源中心", icon: '📚', type: 'location' },
      { id: 'teach-a', name: "教学楼A栋", description: "主要教学区域", icon: '🏫', type: 'location' },
      { id: 'canteen-1', name: "食堂一楼", description: "学生用餐区域", icon: '🍽️', type: 'location' },
      { id: 'sports', name: "体育馆", description: "运动健身场所", icon: '⚽', type: 'location' }
    ],
    pingyuan: [
      { id: 'lib-pingyuan', name: "平原湖图书馆", description: "平原湖校区图书中心", icon: '📚', type: 'location' },
      { id: 'teach-pingyuan', name: "教学楼", description: "平原湖校区教学区", icon: '🏫', type: 'location' },
      { id: 'canteen-pingyuan', name: "学生餐厅", description: "平原湖校区用餐区", icon: '🍽️', type: 'location' },
      { id: 'dorm-pingyuan', name: "学生宿舍", description: "平原湖校区住宿区", icon: '🏠', type: 'location' }
    ],
    innovation: [
      { id: 'lib-innovation', name: "创新港图书馆", description: "创新港图书资源中心", icon: '📚', type: 'location' },
      { id: 'teach-innovation', name: "实验楼", description: "创新港实验教学区", icon: '🔬', type: 'location' },
      { id: 'canteen-innovation', name: "创新港餐厅", description: "创新港用餐区", icon: '🍽️', type: 'location' },
      { id: 'innovation-center', name: "创新中心", description: "科研创新区域", icon: '💡', type: 'location' }
    ]
  };

  return locations[campusId] || locations.main;
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
    selectedCampus.value = 'main';
    localStorage.setItem('defaultCampus', 'main');
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
  console.log(`跳转到地点: ${locationId}`);
  // 实际项目中可以高亮地图上的特定位置
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
  margin-left: 20px; /* 与侧边栏宽度一致 */
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
}

.map-placeholder {
  text-align: center;
  color: #666;
}

.map-placeholder p {
  margin: 10px 0;
  font-size: 1.1rem;
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