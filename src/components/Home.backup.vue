<template>
  <div :class="['homepage', { 'mobile-layout': isMobile, 'desktop-layout': !isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="home"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />
    
    <!-- 移动端头部 -->
    <header v-if="isMobile" class="header">
      <h1>校园信息平台</h1>
    </header>

    <!-- 滚动公告（移动端） -->
    <div v-if="isMobile" class="notice-banner">
      <div class="notice-content">
        <span>📢 最新公告：校园春季运动会即将开始，欢迎报名参加！</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 公告栏区域 -->
      <section class="announcements-section">
        <div class="section-header">
          <h2>校园公告</h2>
          <!-- 对于游客，点击查看更多会检查认证状态 -->
          <button
              v-if="!isMobile && isLoggedIn"
              class="view-more-btn"
              @click="goToPage('activities')"
          >
            查看更多
          </button>
          <span
              v-else-if="isMobile && isLoggedIn"
              class="view-more"
              @click="goToPage('activities')"
          >
            查看更多 >
          </span>
          <!-- 游客版本的"查看更多" -->
          <button
              v-else-if="!isMobile && !isLoggedIn"
              class="view-more-btn"
              @click="checkAuthAndNavigate('activities')"
          >
            查看更多
          </button>
          <span
              v-else-if="isMobile && !isLoggedIn"
              class="view-more"
              @click="checkAuthAndNavigate('activities')"
          >
            查看更多 >
          </span>
        </div>

        <!-- 移动端垂直布局 -->
        <div v-if="isMobile" class="vertical-grid">
          <div
              v-for="(activity, index) in activities"
              :key="'activity-' + index"
              class="vertical-card"
              @click="goToActivity(activity.id)"
          >
            <img :src="activity.image" :alt="activity.title" class="vertical-image">
            <div class="vertical-title">{{ activity.title }}</div>
          </div>
        </div>

        <!-- 桌面端网格布局 -->
        <div v-else class="announcements-grid">
          <div
              v-for="(activity, index) in activities"
              :key="'activity-' + index"
              class="announcement-card"
              @click="goToActivity(activity.id)"
          >
            <img :src="activity.image" :alt="activity.title" class="announcement-image">
            <h3>{{ activity.title }}</h3>
            <p>点击查看详情</p>
          </div>
        </div>
      </section>

      <!-- 商业广告区域 -->
      <section class="ads-section">
        <div class="section-header">
          <h2>商业广告</h2>
          <button
              v-if="!isMobile && isLoggedIn"
              class="view-more-btn"
              @click="goToPage('ads')"
          >
            查看更多
          </button>
          <span
              v-else-if="isMobile && isLoggedIn"
              class="view-more"
              @click="goToPage('ads')"
          >
            查看更多 >
          </span>
          <!-- 游客版本 -->
          <button
              v-else-if="!isMobile && !isLoggedIn"
              class="view-more-btn"
              @click="checkAuthAndNavigate('ads')"
          >
            查看更多
          </button>
          <span
              v-else-if="isMobile && !isLoggedIn"
              class="view-more"
              @click="checkAuthAndNavigate('ads')"
          >
            查看更多 >
          </span>
        </div>

        <!-- 移动端两列网格 -->
        <div v-if="isMobile" class="image-grid">
          <div
              v-for="(ad, index) in advertisements"
              :key="'ad-' + index"
              class="image-card"
              @click="goToAd(ad.id)"
          >
            <img :src="ad.image" :alt="ad.title" class="image-item">
            <div class="image-title">{{ ad.title }}</div>
          </div>
        </div>

        <!-- 桌面端六列网格 -->
        <div v-else class="ads-grid">
          <div
              v-for="(ad, index) in advertisements"
              :key="'ad-' + index"
              class="ad-card"
              @click="goToAd(ad.id)"
          >
            <img :src="ad.image" :alt="ad.title" class="ad-image">
            <h3>{{ ad.title }}</h3>
            <p>点击查看详情</p>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部 -->
    <footer v-if="isMobile" class="footer">
      <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
    </footer>



    <!-- 桌面端底部 -->
    <footer v-if="!isMobile" class="desktop-footer">
      <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
    </footer>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';

// 检测设备类型
const isMobile = ref(false);
const isLoggedIn = ref(false);
const router = useRouter();
const authStore = useAuthStore();

// 当前用户信息
const currentUser = ref({
  name: '',
  studentId: '',
  avatar: ''
});

// 更新设备类型检测
const updateDeviceType = () => {
  isMobile.value = window.innerWidth <= 768;
};

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token');
  isLoggedIn.value = !!token;
  
  if (isLoggedIn.value) {
    // 获取用户信息
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    currentUser.value = {
      name: user.name || user.nickname || user.username || '用户',
      studentId: user.studentId || user.student_id || user.schoolId || '未设置',
      avatar: user.avatar || user.profilePicture || ''
    };
    
    console.log('🏠 当前用户信息:', currentUser.value);
  }
};

// 监听认证存储的变化
const watchAuthChanges = () => {
  // 监听localStorage变化
  const handleStorageChange = (e) => {
    if (e.key === 'user' || e.key === 'token') {
      checkLoginStatus();
    }
  };
  
  window.addEventListener('storage', handleStorageChange);
  
  // 返回清理函数
  return () => {
    window.removeEventListener('storage', handleStorageChange);
  };
};

// 检查认证状态并导航
const checkAuthAndNavigate = (page) => {
  const token = localStorage.getItem('token');

  if (!token) {
    // 未登录，跳转到登录页，并保存目标页面
    router.push({
      path: '/login',
      query: { redirect: page }
    });
  } else {
    // 已登录，执行正常的页面跳转
    goToPage(page);
  }
};

// 登出功能
const logout = async () => {
  try {
    await authStore.logout();
    ElMessage.success('已退出登录');
    // 重新检查登录状态
    checkLoginStatus();
    // 刷新页面或跳转到首页
    router.push('/');
  } catch (error) {
    ElMessage.error('退出登录失败');
  }
};

// 跳转到登录页
const goToLogin = () => {
  router.push('/login');
};





// 校园活动数据
const activities = ref([
  {
    id: 1,
    title: "学校公告",
    image: "/src/assets/School Announcement.png"  // ✅ 对应第二张图
  },
  {
    id: 2,
    title: "社团公告",
    image: "/src/assets/Campus wall.webp"  // ✅ 对应第一张图
  },
  {
    id: 3,
    title: "校园话题公告",
    image: "/src/assets/School Social Federation.png"  // ✅ 对应第三张图
  }
]);

// 商业广告数据
const advertisements = ref([
  {
    id: 1,
    title: "二手书店",
    image: "https://placehold.co/300x200/9B59B6/FFFFFF?text=二手书店"
  },
  {
    id: 2,
    title: "校园快递",
    image: "https://placehold.co/300x200/1ABC9C/FFFFFF?text=校园快递"
  },
  {
    id: 3,
    title: "美食外卖",
    image: "https://placehold.co/300x200/E67E22/FFFFFF?text=美食外卖"
  },
  {
    id: 4,
    title: "数码配件",
    image: "https://placehold.co/300x200/34495E/FFFFFF?text=数码配件"
  },
  {
    id: 5,
    title: "教材出售",
    image: "https://placehold.co/300x200/95A5A6/FFFFFF?text=教材出售"
  },
  {
    id: 6,
    title: "生活用品",
    image: "https://placehold.co/300x200/F39C12/FFFFFF?text=生活用品"
  },
  {
    id: 7,
    title: "电子产品",
    image: "https://placehold.co/300x200/8E44AD/FFFFFF?text=电子产品"
  },
  {
    id: 8,
    title: "体育用品",
    image: "https://placehold.co/300x200/2ECC71/FFFFFF?text=体育用品"
  }
]);



// 页面跳转函数
const goToActivity = (id) => {
  console.log(`跳转到活动详情页，ID: ${id}`);
};

const goToAd = (id) => {
  console.log(`跳转到广告详情页，ID: ${id}`);
};

const goToPage = (page) => {
  console.log(`跳转到${page}页面`);
  switch(page) {
    case 'map':
      router.push('/map');
      break;
    case 'trade':
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
    case 'activities':
      // 跳转到话题墙页面
      router.push('/topicwall');
      break;
    case 'ads':
      // 跳转到商城页面
      router.push('/mall');
      break;
    default:
      console.log(`未知页面: ${page}`);
  }
};
// 生命周期钩子
onMounted(() => {
  updateDeviceType();
  checkLoginStatus();
  const cleanup = watchAuthChanges();
  window.addEventListener('resize', updateDeviceType);
  
  // 清理函数
  onUnmounted(() => {
    window.removeEventListener('resize', updateDeviceType);
    cleanup();
  });
});
</script>
<style scoped>
/* 基础样式 */
.homepage {
  min-height: 100vh;
  display: grid;
  background-color: #f0f2f5;
  position: relative;
}

/* 桌面端布局 */
.homepage.desktop-layout {
  grid-template-rows: auto 1fr auto;
  grid-template-columns: 250px 1fr;
  grid-template-areas:
    "sidebar main"
    "sidebar main"
    "sidebar footer";
}

/* 移动端布局 */
.homepage.mobile-layout {
  display: flex;
  flex-direction: column;
}

/* 移动端头部 */
.header {
  background-color: #4A90E2;
  color: white;
  padding: 15px 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

/* 移动端公告栏 */
.notice-banner {
  background-color: #FFF9C4;
  padding: 10px 15px;
  border-left: 4px solid #FFEB3B;
  margin: 10px 15px;
  flex-shrink: 0;
}

.notice-content {
  display: flex;
  align-items: center;
}

.notice-content span {
  font-size: 0.9rem;
  color: #5D4037;
}

/* 桌面端侧边栏 */
.sidebar {
  grid-area: sidebar;
  background-color: white;
  padding: 0;
  border-right: 1px solid #e0e0e0;
  height: 100%;
}

.logo h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #4A90E2;
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.nav-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-menu li {
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #4A90E2;
}

.nav-menu span:first-child {
  font-size: 1.2rem;
}

/* 用户信息样式 */
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
}

.username {
  font-weight: bold;
  color: #333;
  font-size: 1.1rem;
  margin-bottom: 5px;
}

.student-id {
  color: #666;
  font-size: 0.9rem;
}

.guest-info {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f8f9fa;
}

.guest-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #6c757d;
}

.guest-text {
  color: #6c757d;
  font-size: 1rem;
}

.logout-item {
  background-color: #fff5f5;
  color: #dc3545;
  border-top: 1px solid #eee;
  margin-top: 10px;
}

.logout-item:hover {
  background-color: #f8d7da;
}

/* 移动端用户信息样式 */
.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-top: 1px solid #eee;
}

.user-avatar-small img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.user-text {
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

.logout-btn-small {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  white-space: nowrap;
}

.logout-btn-small:hover {
  background-color: #c82333;
}

.mobile-guest-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-top: 1px solid #eee;
}

.guest-text-small {
  color: #6c757d;
  font-size: 0.9rem;
}

.login-btn-small {
  background-color: #4A90E2;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
}

.login-btn-small:hover {
  background-color: #357abd;
}

/* 主内容区域 */
.main-content {
  grid-area: main;
  padding: 20px;
  overflow-y: auto;
}

/* 通用部分样式 */
.section {
  background: white;
  margin: 15px;
  padding: 15px;
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

/* 移动端网格 */
.image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.image-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.image-card:hover {
  transform: translateY(-2px);
}

.image-item {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.image-title {
  padding: 8px;
  font-size: 0.9rem;
  text-align: center;
  background-color: #f9f9f9;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 移动端垂直布局 */
.vertical-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.vertical-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s;
  display: flex;
  align-items: center;
}

.vertical-card:hover {
  transform: translateY(-2px);
}

.vertical-image {
  width: 150px;
  height: 150px;
  object-fit: cover;
  display: block;
  flex-shrink: 0;
  margin-right: 10px;
}

.vertical-title {
  padding: 8px;
  font-size: 0.9rem;
  color: #333;
  flex-grow: 1;
}

/* 桌面端网格 */
.announcements-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.ads-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}

.announcement-card,
.ad-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.3s;
}

.announcement-card:hover,
.ad-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.announcement-image,
.ad-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.announcement-card h3,
.ad-card h3 {
  padding: 15px;
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.announcement-card p,
.ad-card p {
  padding: 0 15px 15px;
  color: #777;
  margin: 0;
}

/* 移动端底部导航样式统一 */
.nav-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 15px;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.15);
  z-index: 1000;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 8px 0;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: #f0f7ff;
}

.nav-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.nav-text {
  font-size: 0.7rem;
  color: #666;
  transition: all 0.3s ease;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .nav-grid {
    gap: 2px;
  }
  
  .nav-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }
  
  .nav-text {
    font-size: 0.6rem;
  }
}

/* 底部 */
.footer {
  text-align: center;
  padding: 20px 10px 0;
  color: #777;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.desktop-footer {
  grid-area: footer;
  background-color: #333;
  color: white;
  text-align: center;
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .homepage.desktop-layout {
    display: flex;
    flex-direction: column;
  }

  .sidebar {
    display: none;
  }
}

@media (min-width: 769px) {
  .homepage.mobile-layout {
    grid-template-rows: auto 1fr auto;
    grid-template-columns: 250px 1fr;
    grid-template-areas:
      "sidebar main"
      "sidebar main"
      "sidebar footer";
  }

  .header, .notice-banner, .nav-section, .footer {
    display: none;
  }
}
</style>
