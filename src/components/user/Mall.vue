<template>
  <div :class="['mall-page', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="mall"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 - 移动端显示标题在搜索栏上方 -->
      <header class="header">
        <h1 v-if="isMobile">交易中心</h1>
        <div class="search-bar">
          <div v-if="isMobile" class="mobile-search-header">
            <div class="user-avatar-mini">
              <img :src="currentUser.avatar || 'https://placehold.co/24x24/4A90E2/FFFFFF?text=' + (currentUser.name?.charAt(0) || 'U')" :alt="currentUser.name">
            </div>
            <input
                type="text"
                v-model="searchQuery"
                placeholder="搜索商品、服务..."
                @keyup.enter="performSearch"
            >
          </div>
          <div v-else class="desktop-search">
            <input
                type="text"
                v-model="searchQuery"
                placeholder="搜索商品、服务..."
                @keyup.enter="performSearch"
            >
            <button @click="performSearch">搜索</button>
          </div>
        </div>
      </header>

      <!-- 分类导航区域 -->
      <section class="categories-section">
        <div class="section-header">
          <h2>交易分类</h2>
        </div>

        <!-- 移动端分类导航 -->
        <div v-if="isMobile" class="category-mobile-grid">
          <div
              v-for="(category, index) in categories"
              :key="'cat-' + index"
              class="category-mobile-item"
              @click="selectCategory(category.id)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>

        <!-- 桌面端分类导航 -->
        <div v-else class="category-grid">
          <div
              v-for="(category, index) in categories"
              :key="'cat-' + index"
              class="category-item"
              @click="selectCategory(category.id)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.description }}</p>
          </div>
        </div>
      </section>

      <!-- 热门商品/服务区域 -->
      <section class="hot-deals-section">
        <div class="section-header">
          <h2>热门交易</h2>
          <button v-if="!isMobile" class="view-more-btn" @click="goToPage('hotdeals')">查看更多</button>
          <span v-else class="view-more" @click="goToPage('hotdeals')">查看更多 ></span>
        </div>

        <!-- 移动端网格 -->
        <div v-if="isMobile" class="hot-deals-mobile-grid">
          <div
              v-for="(deal, index) in hotDeals"
              :key="'deal-' + index"
              class="hot-deal-mobile-item"
              @click="goToDeal(deal.id)"
          >
            <img :src="deal.image" :alt="deal.title" class="deal-image">
            <div class="deal-info">
              <h3>{{ deal.title }}</h3>
              <p class="deal-price">¥{{ deal.price }}</p>
              <div class="deal-meta">
                <span class="deal-location">{{ deal.location }}</span>
                <span class="deal-time">{{ deal.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 桌面端网格 -->
        <div v-else class="hot-deals-grid">
          <div
              v-for="(deal, index) in hotDeals"
              :key="'deal-' + index"
              class="hot-deal-item"
              @click="goToDeal(deal.id)"
          >
            <img :src="deal.image" :alt="deal.title" class="deal-image">
            <div class="deal-info">
              <h3>{{ deal.title }}</h3>
              <p class="deal-price">¥{{ deal.price }}</p>
              <div class="deal-meta">
                <span class="deal-location">{{ deal.location }}</span>
                <span class="deal-time">{{ deal.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 发布交易区域 -->
      <section class="post-trade-section">
        <div class="post-form">
          <div class="post-header">
            <img src="https://via.placeholder.com/40x40" alt="用户头像" class="avatar">
            <span class="username">当前用户</span>
          </div>
          <div class="post-actions">
            <button @click="createNewTrade('item')">📦 发布物品交易</button>
            <button @click="createNewTrade('service')">🚗 发布服务需求</button>
          </div>
        </div>
      </section>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { productAPI } from '@/api/product';
import { uploadAPI } from '@/api/upload';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';

const router = useRouter(); // 创建路由实例

// 用户信息
const currentUser = ref(null);

// 获取用户信息
const getUserInfo = () => {
  const token = localStorage.getItem('token');
  if (token) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    currentUser.value = {
      name: user.name || user.username || '',
      studentId: user.studentId || user.student_id || '',
      avatar: user.avatar || ''
    };
  } else {
    // 未登录时设置为 null
    currentUser.value = null;
  }
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
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});

// 搜索相关
const searchQuery = ref('');
const performSearch = () => {
  console.log('搜索:', searchQuery.value);
  // 实际项目中可以调用API进行搜索
};

// 分类数据
const categories = ref([
  {
    id: 'items',
    name: '二手交易',
    description: '书籍、电子设备、生活用品等',
    icon: '📚'
  },
  {
    id: 'jobs',
    name: '兼职平台',
    description: '校内外兼职机会',
    icon: '💼'
  },
  {
    id: 'services',
    name: '跑腿拼车',
    description: '代取快递、拼车出行',
    icon: '🚗'
  },
  {
    id: 'food',
    name: '美食外卖',
    description: '校内外美食分享',
    icon: '🍔'
  },
  {
    id: 'other',
    name: '其他服务',
    description: '各类生活服务',
    icon: '🔧'
  }
]);

// 热门交易数据
const hotDeals = ref([
  {
    id: 1,
    title: '二手MacBook Pro，9成新',
    price: 4500,
    location: '教学楼A',
    time: '2小时前',
    image: 'https://via.placeholder.com/300x200/4A90E2/FFFFFF?text=MacBook+Pro'
  },
  {
    id: 2,
    title: '寻找拼车去火车站',
    price: 30,
    location: '南门',
    time: '1小时前',
    image: 'https://via.placeholder.com/300x200/50C878/FFFFFF?text=拼车'
  },
  {
    id: 3,
    title: '代取快递，校内配送',
    price: 5,
    location: '宿舍区',
    time: '30分钟前',
    image: 'https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=快递代取'
  },
  {
    id: 4,
    title: '英语四六级资料转让',
    price: 25,
    location: '图书馆',
    time: '3小时前',
    image: 'https://via.placeholder.com/300x200/9B59B6/FFFFFF?text=学习资料'
  }
]);

// 商品数据
const products = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(12);
const totalProducts = ref(0);
const searchParams = ref({
  keyword: '',
  category: '',
  minPrice: '',
  maxPrice: ''
});

// 获取商品列表
const fetchProducts = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page,
      size: pageSize.value,
      ...searchParams.value
    };
    
    const response = await productAPI.getProducts(params);
    products.value = response.data;
    totalProducts.value = response.total;
    currentPage.value = page;
  } catch (error) {
    console.error('获取商品列表失败:', error);
    ElMessage.error('获取商品列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索商品
const searchProducts = () => {
  fetchProducts(1);
};

// 分类选择功能
const selectCategory = (categoryId) => {
  console.log(`选择分类: ${categoryId}`);
  // 实际项目中可以跳转到对应分类页面
};

// 发布商品
const createNewTrade = async (type) => {
  // 跳转到发布页面或打开模态框
  router.push(`/publish?type=${type}`);
};

// 商品详情跳转
const goToDeal = (productId) => {
  router.push(`/product/${productId}`);
};

// 页面初始化
onMounted(() => {
  fetchProducts(1);
});

// 导航相关
const goToPage = (page) => {
  console.log(`跳转到${page}页面`);
  switch(page) {
    case 'home':
      router.push('/');
      break;
    case 'map':
      router.push('/map');
      break;
    case 'trade':  // 这是当前页面，可以刷新或滚动到顶部
      router.push('/mall');
      break;
    case 'topic':
      router.push('/topicwall');
      break;
    case 'message':
      router.push('/message');
      break;
    case 'profile':
      router.push('/personalcenter');
      break;
    case 'hotdeals':
      // 可以滚动到热门交易部分或跳转到专门的页面
      document.querySelector('.hot-deals-section')?.scrollIntoView({ behavior: 'smooth' });
      break;
    default:
      console.log(`未知页面: ${page}`);
  }
};
</script>

<style scoped>
.mall-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: grid;
  grid-template-columns: 0px 1fr; /* 增加侧边栏宽度 */
  grid-template-areas: "sidebar main";
}

.mall-page.mobile {
  grid-template-columns: 1fr;
  grid-template-areas: "main";
  padding-bottom: 80px; /* 为移动底部导航留出空间 */
}

/* 桌面端布局 */
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

.main-content {
  grid-area: main;
  padding: 0px;
  overflow-y: auto;
  margin-left: 210px; /* 调整主内容区左边距以匹配新的侧边栏宽度 */
  display: flex;
  flex-direction: column;
}

.main-content.full-width {
  margin-left: 0;
  padding-bottom: 80px; /* 为移动底部导航留出空间 */
}

.header {
  background-color: #4A90E2;
  color: white;
  padding: 15px 30px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.header h1 {
  margin: 0 0 15px 0;
  font-size: 1.8rem;
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

/* 桌面端底部样式 - 现在位于main-content内部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  margin-top: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin: 15px;
  flex-shrink: 0;
}

/* 公共样式 - 搜索栏 */
.search-bar {
  display: flex;
  gap: 10px;
  width: 100%;
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

/* 分类区域样式 */
.categories-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.categories-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.categories-section h2 {
  margin: 0 0 15px 0;
  font-size: 1.2rem;
  color: #333;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
}

.category-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.category-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  background: #e9f7fe;
  border-color: #4A90E2;
}

.category-icon {
  font-size: 2rem;
  margin-bottom: 10px;
  display: block;
}

.category-item h3 {
  margin: 10px 0 5px;
  font-size: 1rem;
  color: #333;
}

.category-item p {
  margin: 0;
  font-size: 0.8rem;
  color: #666;
}

/* 移动端分类网格 - 修改为5列 */
.category-mobile-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.category-mobile-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px 5px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-mobile-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  background: #e9f7fe;
  border-color: #4A90E2;
}

.category-mobile-item .category-icon {
  font-size: 1.2rem;
  margin-bottom: 3px;
}

.category-mobile-item .category-name {
  font-size: 0.7rem;
  color: #333;
}

/* 热门交易区域样式 */
.hot-deals-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.hot-deals-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.hot-deals-section h2 {
  margin: 0 0 15px 0;
  font-size: 1.2rem;
  color: #333;
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
}

.hot-deals-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.hot-deal-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.3s;
  border: 1px solid #eee;
}

.hot-deal-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.deal-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.deal-info {
  padding: 15px;
}

.deal-info h3 {
  margin: 0 0 8px;
  font-size: 1rem;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.deal-price {
  margin: 0 0 10px;
  font-size: 1.2rem;
  font-weight: bold;
  color: #e74c3c;
}

.deal-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #777;
}

.deal-location {
  flex: 1;
}

.deal-time {
  flex: 1;
  text-align: right;
}

/* 移动端热门交易网格 */
.hot-deals-mobile-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.hot-deal-mobile-item {
  display: flex;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.3s;
  border: 1px solid #eee;
}

.hot-deal-mobile-item .deal-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  flex-shrink: 0;
}

.hot-deal-mobile-item .deal-info {
  padding: 10px;
  flex: 1;
}

/* 发布交易区域 */
.post-trade-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.post-form {
  border: 1px solid #e1e5f2;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9fafc;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 600;
  color: #333;
}

.post-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.post-actions button {
  flex: 1;
  padding: 12px 15px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  background-color: white;
  color: #667eea;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.post-actions button:hover {
  background-color: #667eea;
  color: white;
}

/* 移动端顶部用户信息样式 */
.mobile-user-header {
  background-color: white;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.user-info-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-small img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.user-text-top {
  flex: 1;
  min-width: 0;
}

.username-small {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-id-small {
  color: #666;
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  .mall-page {
    padding-top: 0;
    padding-bottom: 70px;
  }
  
  .mobile-user-header {
    display: none;
  }
  
  .sidebar {
    display: none;
  }
  
  .main-content {
    margin-top: 0;
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
  
  .category-mobile-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
  
  .category-mobile-item {
    padding: 12px 8px;
    font-size: 0.8rem;
  }
  
  .hot-deals-mobile-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .hot-deal-mobile-item {
    padding: 15px;
  }
  
  .post-form {
    padding: 15px;
  }
  
  .post-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .post-actions button {
    width: 100%;
  }
}
</style>

/* 桌面端样式 */
@media (min-width: 768px) {
  .mobile-nav {
    display: none;
  }
}