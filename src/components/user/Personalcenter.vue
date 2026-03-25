<template>
  <div :class="['personal-center', { 'mobile': isMobile }]">
    <!-- 统一组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="personalcenter"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="userInfo"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 -->
      <header class="header">
        <h1 v-if="isMobile">个人中心</h1>
      </header>

      <!-- 用户信息区域 - 使用独立组件 -->
      <UserInfoSection 
        :name="userInfo.name"
        :student-id="userInfo.studentId"
        :college="userInfo.college"
        :avatar="userInfo.avatar"
        @edit-profile="editProfile"
        @account-management="accountManagement"
      />

      <!-- 认证中心区域 - 使用独立组件 -->
      <AuthCenter 
        :is-verified="userInfo.isVerified"
        :is-real-name-verified="userInfo.isRealNameVerified"
        @verification-click="handleVerificationUpdate"
      />

      <!-- 我的订单区域 - 使用独立组件 -->
      <OrderSection 
        :orders="recentOrders"
        :loading="ordersLoading"
        :is-mobile="isMobile"
        @view-all="viewAllOrders"
        @view-detail="viewOrderDetail"
      />

      <!-- 我的商品区域 - 保持原有组件化 -->
      <section class="products-section">
        <div class="section-header">
          <h2>我的商品</h2>
          <button v-if="!isMobile" class="view-more-btn primary" @click="viewAllProducts">查看全部</button>
          <span v-else class="view-more" @click="viewAllProducts">查看更多 ></span>
        </div>

        <!-- 商品标签页 -->
        <div class="product-tabs">
          <button 
            @click="currentProductTab = 'published'"
            class="tab-btn"
            :class="{ active: currentProductTab === 'published' }"
          >
            🛒 我发布的
          </button>
          <button 
            @click="currentProductTab = 'favorites'"
            class="tab-btn"
            :class="{ active: currentProductTab === 'favorites' }"
          >
            ⭐ 我收藏的
          </button>
        </div>

        <!-- 加载状态 -->
        <div v-if="productsLoading" class="loading-state">
          <div class="spinner-small"></div>
          <span>加载中...</span>
        </div>

        <!-- 空状态 -->
        <div v-else-if="getCurrentProductList().length === 0" class="empty-products">
          <div class="empty-icon">📦</div>
          <p>{{ 
            currentProductTab === 'published' ? '暂无发布的商品' : '暂无收藏的商品'
          }}</p>
        </div>

        <!-- 商品列表 -->
        <div v-else class="products-grid">
          <div
              v-for="(product, index) in getCurrentProductList()"
              :key="'product-' + product.id + '-' + index"
              class="product-item"
              @click="viewProductDetail(product.id)"
          >
            <img :src="product.images && product.images.length > 0 ? product.images[0] : 'https://placehold.co/100x100/4A90E2/FFFFFF?text=商品'" :alt="product.title" class="product-image">
            <div class="product-info">
              <h3>{{ product.title }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <div class="product-meta">
                <span class="product-status" :class="product.status === 1 ? 'onsale' : 'sold'">
                  {{ product.status === 1 ? '在售' : product.status === 2 ? '已售出' : '已下架' }}
                </span>
                <span class="product-stats">👁️ {{ product.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 我的话题区域 -->
      <section class="topics-section">
        <div class="section-header">
          <h2 style="color: #000000;">我的话题</h2>
          <button v-if="!isMobile" class="view-more-btn" @click="viewAllTopics">查看全部</button>
          <span v-else class="view-more" @click="viewAllTopics">查看更多 ></span>
        </div>

        <!-- 话题标签页 -->
        <div class="topic-tabs">
          <button 
            @click="currentTopicTab = 'published'"
           class="tab-btn"
            :class="{ active: currentTopicTab === 'published' }"
          >
            📝 我发布的话题
          </button>
          <button 
            @click="currentTopicTab = 'participated'"
           class="tab-btn"
            :class="{ active: currentTopicTab === 'participated' }"
          >
            💬 我参与的话题
          </button>
          <button 
            @click="currentTopicTab = 'liked'"
           class="tab-btn"
            :class="{ active: currentTopicTab === 'liked' }"
          >
            👍 我点赞的话题
          </button>
          <button 
            @click="currentTopicTab = 'collected'"
           class="tab-btn"
            :class="{ active: currentTopicTab === 'collected' }"
          >
            ⭐ 我收藏的话题
          </button>
        </div>

        <!-- 加载状态 -->
        <div v-if="topicsLoading" class="loading-state">
          <div class="spinner-small"></div>
          <span>加载中...</span>
        </div>

        <!-- 空状态 -->
        <div v-else-if="getCurrentTopicList().length === 0" class="empty-topics">
          <div class="empty-icon">💭</div>
          <p>{{ 
            currentTopicTab === 'published' ? '暂无发布的话题' : 
            currentTopicTab === 'liked' ? '暂无点赞的话题' :
            currentTopicTab === 'collected' ? '暂无收藏的话题' :
            '暂无参与的话题' 
          }}</p>
        </div>

        <!-- 话题列表 -->
        <div v-else class="topics-list">
          <div
              v-for="(topic, index) in getCurrentTopicList()"
              :key="'topic-' + topic.id + '-' + index"
              class="topic-item"
              @click="viewTopicDetail(topic.id)"
          >
            <h3>{{ topic.title || topic.content.substring(0, 50) }}</h3>
            <p class="topic-content">{{ topic.content }}</p>
            <div class="topic-meta">
              <span class="topic-time">{{ formatTime(topic.createdAt || topic.time) }}</span>
              <span class="topic-stats">👍 {{ topic.likesCount || topic.likes }} | 💬 {{ topic.commentsCount || topic.comments }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>
    
    <!-- 身份认证表单对话框 -->
    <IdentityVerificationForm 
      v-model="showIdentityForm"
      @success="handleIdentityFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import UserInfoSection from '@/components/user/UserInfoSection.vue';
import AuthCenter from '@/components/user/AuthCenter.vue';
import OrderSection from '@/components/user/OrderSection.vue';
import IdentityVerificationForm from '@/components/user/IdentityVerificationForm.vue';
import { useUserInfo } from '@/composables/useUserInfo';
import { useUserOrders } from '@/composables/useUserOrders';
import { topicAPI } from '@/api/topic';
import { productAPI } from '@/api/product';

const router = useRouter(); // 创建路由实例
const authStore = useAuthStore();

// 使用 composables 管理用户信息和订单
const { userInfo, fetchUserInfo, updateVerificationStatus } = useUserInfo();
const { recentOrders, ordersLoading, loadRecentOrders } = useUserOrders();

// 设备检测相关
const isMobile = ref(false);

// 更新设备检测状态
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

// 用户信息已移至 composable 管理

// 订单数据已移至 composable 管理

// 用户话题数据
const userTopics = ref([]);
const publishedTopics = ref([]);
const participatedTopics = ref([]);
const likedTopics = ref([]); // 我点赞的话题
const collectedTopics = ref([]); // 我收藏的话题
const currentTopicTab = ref('published'); // 'published'、'participated'、'liked' 或 'collected'
const topicsLoading = ref(false);

// 用户商品数据
const userProducts = ref([]);
const publishedProducts = ref([]); // 我发布的商品
const favoriteProducts = ref([]); // 我收藏的商品
const currentProductTab = ref('published'); // 'published' 或 'favorites'
const productsLoading = ref(false);

// 身份认证表单
const showIdentityForm = ref(false);

// 获取当前标签页的商品列表
const getCurrentProductList = () => {
  if (currentProductTab.value === 'published') {
    return publishedProducts.value;
  } else {
    return favoriteProducts.value;
  }
};

// 获取当前标签页的话题列表
const getCurrentTopicList = () => {
  if (currentTopicTab.value === 'published') {
   return publishedTopics.value;
  } else if (currentTopicTab.value === 'liked') {
   return likedTopics.value;
  } else if (currentTopicTab.value === 'collected') {
   return collectedTopics.value;
  } else {
   return participatedTopics.value;
  }
};

// 编辑个人资料
const editProfile = () => {
  console.log('编辑个人资料');
  router.push('/personalinformation');
};

// 账号管理
const accountManagement = () => {
  console.log('跳转到账号管理页面');
  router.push('/account/settings');
};

// 处理认证状态更新（来自 AuthCenter 组件）
const handleVerificationUpdate = (type, status) => {
  if (type === 'identity') {
    // 打开身份认证表单
    showIdentityForm.value = true;
  } else {
    updateVerificationStatus(type, status);
  }
};

// 身份认证表单提交成功
const handleIdentityFormSuccess = () => {
  showIdentityForm.value = false;
  // 刷新用户信息
  fetchUserInfo();
};



// 登出功能
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await authStore.logout();
    ElMessage.success('已退出登录');
    router.push('/');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败');
    }
  }
};

// 身份认证和实名认证逻辑已移至 AuthCenter 组件
// 订单功能方法已移至 OrderSection 组件作为事件发射
const viewAllOrders = () => {
  console.log('查看全部订单');
  router.push('/personal/orders');
};

const viewOrderDetail = (orderId) => {
  console.log(`查看订单详情：${orderId}`);
  router.push(`/order/confirmation/${orderId}`);
};

// 话题功能
const viewAllTopics = () => {
  console.log('查看全部话题');
  // 实际项目中可以跳转到话题页面
};

const viewTopicDetail = (topicId) => {
  console.log(`查看话题详情：${topicId}`);
  // 跳转到话题详情页面
  router.push(`/topic/${topicId}`);
};

// 商品功能
const viewAllProducts = () => {
  console.log('查看全部商品');
  // 跳转到我的商品页面
  router.push('/personal/products');
};

const viewProductDetail = (productId) => {
  console.log(`查看商品详情：${productId}`);
  // 跳转到商品详情页面
  router.push(`/product/${productId}`);
};

// 加载用户商品
const loadUserProducts = async () => {
  try {
    productsLoading.value = true;
    
    // 先检查 token 是否存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.log('ℹ️ 用户未登录，跳过商品加载');
      return;
    }
    
    // 获取当前用户 ID
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = userData.id;
    
    if (!userId) {
      console.log('ℹ️ 用户未登录，跳过商品加载');
      return;
    }
    
    console.log('📡 正在加载用户商品，userId:', userId);
    
    // 加载用户发布的商品（只显示 4 个）
    const publishedRes = await productAPI.getProducts({ 
      sellerId: userId, 
      status: null, // 获取所有状态的商品
      page: 1, // 明确指定第一页
      size: 4 // 限制显示数量
    });
    
    console.log('✅ 发布的商品响应:', publishedRes);
    const publishedData = publishedRes.data || publishedRes;
    publishedProducts.value = publishedData.records || publishedData.data?.records || [];
    
    // 如果超过 4 个，截取前 4 个
    if (publishedProducts.value.length > 4) {
      publishedProducts.value = publishedProducts.value.slice(0, 4);
    }
    
    // 处理 images 字段
    publishedProducts.value.forEach(p => {
      if (typeof p.images === 'string') {
        try {
          p.images = JSON.parse(p.images);
        } catch (e) {
          console.error('商品 images 解析失败:', e);
          p.images = [];
        }
      }
    });
    
    // 加载用户收藏的商品（只显示 4 个）
    const favoritesRes = await productAPI.getMyFavorites({ 
      page: 1, // 明确指定第一页
      size: 4 // 限制显示数量
    });
    console.log('✅ 收藏的商品响应:', favoritesRes);
    const favoritesData = favoritesRes.data || favoritesRes;
    favoriteProducts.value = favoritesData || favoritesData.data || [];
    
    // 如果超过 4 个，截取前 4 个
    if (favoriteProducts.value.length > 4) {
      favoriteProducts.value = favoriteProducts.value.slice(0, 4);
    }
    
    // 处理 images 字段
    favoriteProducts.value.forEach(p => {
      if (typeof p.images === 'string') {
        try {
          p.images = JSON.parse(p.images);
        } catch (e) {
          console.error('商品 images 解析失败:', e);
          p.images = [];
        }
      }
    });
    
    console.log('📋 发布的商品数量:', publishedProducts.value.length);
    console.log('📋 收藏的商品数量:', favoriteProducts.value.length);
  } catch (error) {
    console.error('❌ 加载用户商品失败:', error);
    // 如果是 Token 无效或 401 错误，清除本地存储并跳转登录
    if ((error.message && error.message.includes('Token')) || error.response?.status === 401) {
      console.warn('⚠️ Token 无效，清除本地存储并跳转登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      ElMessage.error('登录已过期，请重新登录');
      setTimeout(() => {
        router.push('/login');
      }, 500);
    } else {
      ElMessage.error('加载失败：' + (error.response?.data?.message || error.message));
    }
    publishedProducts.value = [];
    favoriteProducts.value = [];
  } finally {
    productsLoading.value = false;
  }
};

// 加载用户话题
const loadUserTopics = async () => {
  try {
   topicsLoading.value = true;
    
    // 先检查 token 是否存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.log('ℹ️ 用户未登录，跳过话题加载');
      return;
    }
    
    // 获取当前用户 ID
   const userData = JSON.parse(localStorage.getItem('user') || '{}');
   const userId = userData.id;
    
   if (!userId) {
     console.log('ℹ️ 用户未登录，跳过话题加载');
     return;
    }
    
    // 并行加载发布、参与、点赞和收藏的话题
  const [publishedRes, participatedRes, likedRes, collectedRes] = await Promise.all([
     topicAPI.getUserPublishedTopics(userId),
     topicAPI.getUserParticipatedTopics(userId),
     topicAPI.getUserLikedTopics(userId),
     topicAPI.getCollections()
    ]);
    
  console.log('✅ 发布的话题响应:', publishedRes);
  console.log('✅ 参与的话题响应:', participatedRes);
  console.log('✅ 点赞的话题响应:', likedRes);
  console.log('✅ 收藏的话题响应:', collectedRes);
    
    // 处理后端返回的数据结构 - 支持多种格式
  const publishedData = publishedRes.data || publishedRes;
  const participatedData = participatedRes.data || participatedRes;
  const likedData = likedRes.data || likedRes;
  const collectedData = collectedRes.data || collectedRes;
    
   publishedTopics.value = (publishedData.topics || publishedData.data?.topics || []);
    participatedTopics.value = (participatedData.topics || participatedData.data?.topics || []);
    likedTopics.value = (likedData.topics || likedData.data?.topics || []);
    
    // 收藏的话题数据结构 - 直接是 topics 数组
 collectedTopics.value = (collectedData.topics || collectedData.data?.topics || []);
    
  console.log('📋 发布的话题数量:', publishedTopics.value.length);
  console.log('📋 参与的话题数量:', participatedTopics.value.length);
  console.log('📋 点赞的话题数量:', likedTopics.value.length);
  console.log('📋 收藏的话题数量:', collectedTopics.value.length);
  } catch (error) {
  console.error('❌ 加载话题失败:', error);
  // 如果是 Token 无效或 401 错误，清除本地存储并跳转登录
  if ((error.message && error.message.includes('Token')) || error.response?.status === 401) {
    console.warn('⚠️ Token 无效，清除本地存储并跳转登录');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    ElMessage.error('登录已过期，请重新登录');
    setTimeout(() => {
      router.push('/login');
    }, 500);
  } else {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message));
  }
  publishedTopics.value = [];
  participatedTopics.value = [];
  likedTopics.value = [];
  collectedTopics.value = [];
  } finally {
  topicsLoading.value = false;
  }
};

// 格式化时间
const formatTime = (date) => {
  if (!date) return '';
  
  const now = new Date();
  const diff = now - new Date(date);
  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return new Date(date).toLocaleDateString();
};

// 用户操作
const openSettings = () => {
  console.log('打开设置');
  // 实际项目中可以跳转到设置页面
};

// 导航相关
const goToPage = (page) => {
  console.log(`跳转到${page}页面`);
  switch(page) {
    case 'home':
      router.push('/');
      break;
    case 'map':  // 跳转到地图导引
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
    case 'profile':  // 这是当前页面，可以刷新或滚动到顶部
      router.push('/personalcenter');
      break;
    case 'settings':  // 跳转到设置页面
      console.log('跳转到设置页面');
      break;
    default:
      console.log(`未知页面: ${page}`);
  }
};

// 页面初始化
onMounted(async () => {
  console.log('个人中心页面已挂载');
  updateDeviceDetection();
  window.addEventListener('resize', updateDeviceDetection());
  
  // 检查登录状态
  const token = localStorage.getItem('token');
  if (!token) {
    console.warn('⚠️ 用户未登录，跳转到登录页');
    ElMessage.warning('请先登录');
    setTimeout(() => {
      router.push('/login');
    }, 500);
    return;
  }
  
  // 从后端获取最新的用户信息（包括身份认证字段）
  console.log('📡 正在获取最新用户信息...');
  await authStore.fetchCompleteUserInfo();
  console.log('✅ 用户信息获取成功');
  
  // 更新本地用户信息
  fetchUserInfo();
  console.log('用户信息初始化完成');
  
  loadRecentOrders();
  console.log('订单数据初始化完成');
  
  // 加载用户的话题
  await loadUserTopics();
  
  // 加载用户的商品
  await loadUserProducts();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.personal-center {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: grid;
  grid-template-columns: 210px 1fr; /* 增加侧边栏宽度 */
  grid-template-areas: "sidebar main";
}

.personal-center.mobile {
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

/* 用户信息区域样式已移至 AuthCenter 组件 */

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

/* 移动端底部导航样式统一 */
.mobile-nav {
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
  padding: 8px 0;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.mobile-nav .nav-item:hover {
  background: #f0f7ff;
}

.mobile-nav .nav-item.active {
  background: #e3f2fd;
}

.mobile-nav .nav-icon {
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

.mobile-nav .nav-item.active .nav-icon {
  background: #4A90E2;
  color: white;
  transform: scale(1.1);
}

.mobile-nav .nav-text {
  font-size: 0.7rem;
  color: #666;
  transition: all 0.3s ease;
}

.mobile-nav .nav-item.active .nav-text {
  color: #4A90E2;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-nav .nav-grid {
    gap: 2px;
  }
  
  .mobile-nav .nav-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }
  
  .mobile-nav .nav-text {
    font-size: 0.6rem;
  }
}

/* 移动端用户信息样式 */
.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
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

/* 用户信息区域样式 */
.user-info-section {
  background: white;
  margin: 15px;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  margin-bottom: 20px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e3f2fd;
}

.user-details {
  flex: 1;
}

.user-details h2 {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  color: #333;
}

.user-details p {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
}

.user-actions {
  display: flex;
  gap: 15px;
  width: 100%;
  justify-content: center;
}

.user-actions button {
  padding: 10px 20px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  background-color: white;
  color: #667eea;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  max-width: 150px;
}

.user-actions button:hover {
  background-color: #667eea;
  color: white;
}

/* 认证功能区域样式 */
.auth-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.auth-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.auth-section h2 {
  margin: 0 0 15px 0;
  font-size: 1.2rem;
  color: #333;
}

.refresh-btn {
  background-color: #4A90E2;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.refresh-btn:hover {
  background-color: #357abd;
  transform: scale(1.05);
}

.refresh-btn:active {
  transform: scale(0.95);
}

/* 认证中心区域样式已移至 AuthCenter 组件 */
/* 订单区域样式已移至 OrderSection 组件 */

/* 话题区域样式 */
.topics-section {
  background: white;
 margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

/* 话题标签页样式 - 优化字体和背景颜色 */
.topic-tabs {
  display: flex;
  gap: 15px;
 margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 10px 20px;
  background: #f5f7fa;
  color: #606266;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
 cursor: pointer;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: #e3f2fd;
  color: #4A90E2;
  transform: translateY(-2px);
}

.tab-btn.active {
  background: linear-gradient(135deg, #4A90E2 0%, #357abd 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);
  transform: translateY(-2px);
}

.tab-btn.active:hover {
  background: linear-gradient(135deg, #357abd 0%, #2c6aa8 100%);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.4);
}

.topics-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-item {
  padding: 15px;
  border: 1px solid #e1e5f2;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.topic-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.topic-item h3 {
  margin: 0 0 10px 0;
  font-size: 1.1rem;
  color: #333;
}

.topic-content {
  margin: 0 0 10px 0;
  color: #666;
  line-height: 1.5;
}

.topic-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #777;
}

/* 商品区域样式 */
.products-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.products-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.products-section h2 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
}

/* 商品标签页样式 */
.product-tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
}

/* 商品列表样式 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.product-item {
  background: white;
  border: 1px solid #e1e5f2;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.product-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  background-color: #f0f2f5;
}

.product-info {
  padding: 12px;
}

.product-info h3 {
  margin: 0 0 8px 0;
  font-size: 0.95rem;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  font-weight: bold;
  color: #e74c3c;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #777;
}

.product-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
}

.product-status.onsale {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.product-status.sold {
  background-color: #f8d7da;
  color: #721c24;
}

.product-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 空状态样式 */
.empty-products {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-products .empty-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.empty-products p {
  margin: 0;
  font-size: 0.9rem;
}

/* 响应式设计 - 移动端商品样式 */
@media (max-width: 1024px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .product-image {
    height: 120px;
  }
  
  .product-info h3 {
    font-size: 0.85rem;
  }
  
  .product-price {
    font-size: 1rem;
  }
}

/* 响应式设计 - 移动端样式 */
@media (max-width: 767px) {
  .personal-center {
    padding-bottom: 70px; /* 为移动底部导航留出空间 */
  }
  
  .sidebar {
    display: none;
  }
  
  .main-content {
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
  
  .user-header {
    flex-direction: column;
    text-align: center;
  }
  
  .user-avatar {
    width: 80px;
    height: 80px;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .user-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .user-actions button {
    width: 100%;
  }
  
  .status-item-container {
    flex-direction: column;
    gap: 15px;
  }
  
  .status-item {
    flex-direction: row;
    align-items: center;
    padding: 15px;
  }
  
  .status-icon {
    margin-right: 15px;
    margin-bottom: 0;
  }
  
  .orders-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .order-item {
    padding: 15px;
  }
  
  .topics-list {
    padding: 10px;
  }
  
  .topic-item {
    padding: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    align-self: flex-end;
  }
}

/* 桌面端样式 */
@media (min-width: 768px) {
  .mobile-nav {
    display: none;
  }
  
  .main-content {
    grid-area: main;
    padding: 0;
    overflow-y: auto;
    margin-left: 210px; /* 与侧边栏宽度一致 */
  }
}

/* 查看全部按钮样式 */
.view-more-btn {
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.view-more-btn:hover {
  opacity: 0.8;
}

/* 蓝色主题按钮 */
.view-more-btn.primary {
  background-color: #4A90E2;
  color: white;
  border: 1px solid #4A90E2;
}

.view-more-btn.primary:hover {
  background-color: #4578d9;
}
</style>
