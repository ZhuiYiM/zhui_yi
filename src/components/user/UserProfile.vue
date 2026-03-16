<template>
  <div class="user-profile-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>用户信息加载失败</h2>
      <p>{{ error }}</p>
      <button @click="loadUserInfo" class="retry-btn">重试</button>
    </div>

    <!-- 用户信息内容 -->
    <div v-else-if="userInfo" class="user-content">
      <!-- 用户头部信息 -->
      <UserHeader 
        :basic-info="userInfo.basicInfo"
        :identity="userInfo.identity"
        @back="goBack"
      />
      
      <!-- 学术信息 -->
      <AcademicInfo 
        :academic-info="userInfo.academicInfo"
        :show-student-id="userInfo.privacySettings.showStudentId !== false"
      />
      
      <!-- 统计数据 -->
      <UserStats :statistics="userInfo.statistics" />
      
      <!-- 隐私设置 -->
      <PrivacySettings 
        v-if="isCurrentUser"
        :public-visible="privacySettings.publicVisible"
        @update="updatePrivacySettings"
      />
      
      <!-- 操作按钮 -->
      <UserActions 
        v-if="!isCurrentUser"
        :is-following="isFollowing"
        :can-message="userInfo.canMessage"
        @follow="handleFollow"
        @message="handleMessage"
        @report="showReportModal = true"
      />
      
      <!-- 隐私受限提示 -->
      <div v-if="!canViewContent" class="privacy-restricted">
        <div class="restricted-icon">🔒</div>
        <h3>该用户设置了隐私保护</h3>
        <p>暂时无法查看其发布和参与的话题</p>
      </div>
      
      <!-- 话题和商品模块 -->
      <div v-else class="content-sections">
        <!-- 主标签切换：话题 vs 商品 -->
        <div class="main-tabs">
          <button 
            @click="showProducts = false"
            class="main-tab-btn"
            :class="{ active: !showProducts }"
          >
            💬 话题
          </button>
          <button 
            @click="showProducts = true"
            class="main-tab-btn"
            :class="{ active: showProducts }"
          >
            🛒 商品
          </button>
        </div>
        
        <!-- 商品模块 -->
        <ProductSection 
          v-if="showProducts"
          :products="productsList"
          :current-tab="productTab"
          :is-current-user="isCurrentUser"
          :loading="productsLoading"
          @tab-change="productTab = $event"
          @view-product="viewProduct"
        />
        
        <!-- 话题模块 -->
        <TopicSection 
          v-else
          :topics="getTopicsList"
          :current-tab="currentTab"
          :loading="topicsLoading"
          @tab-change="currentTab = $event"
          @view-topic="viewTopicDetail"
        />
      </div>
    </div>

    <!-- 举报弹窗 -->
    <ReportModal 
      v-if="showReportModal"
      @close="showReportModal = false"
      @submit="submitReport"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { topicAPI } from '@/api/topic';
import { productAPI } from '@/api/product';
import axios from 'axios';

// 导入子组件
import UserHeader from './UserHeader.vue';
import AcademicInfo from './AcademicInfo.vue';
import UserStats from './UserStats.vue';
import PrivacySettings from './PrivacySettings.vue';
import UserActions from './UserActions.vue';
import ProductSection from './ProductSection.vue';
import TopicSection from './TopicSection.vue';
import ReportModal from './ReportModal.vue';

const route = useRoute();
const router = useRouter();

// 数据
const loading = ref(false);
const error = ref(null);
const userInfo = ref(null);
const isFollowing = ref(false);
const currentTab = ref('published');
const topicsLoading = ref(false);
const publishedTopics = ref([]);
const participatedTopics = ref([]);
const likedTopics = ref([]);
const privacySettings = ref({ publicVisible: true });
const showReportModal = ref(false);

// 商品相关
const showProducts = ref(false);
const productTab = ref('published');
const productsLoading = ref(false);
const userPublishedProducts = ref([]);
const userFavoriteProducts = ref([]);

// 计算属性
const currentUserId = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.id || null;
});

const isCurrentUser = computed(() => {
  return currentUserId.value === parseInt(route.params.userId);
});

const canViewContent = computed(() => {
  if (isCurrentUser.value) return true;
  return privacySettings.value.publicVisible !== false;
});

const getTopicsList = computed(() => {
  if (currentTab.value === 'published') return publishedTopics.value;
  if (currentTab.value === 'liked') return likedTopics.value;
  return participatedTopics.value;
});

const productsList = computed(() => {
  if (productTab.value === 'published') return userPublishedProducts.value;
  if (productTab.value === 'favorites') return userFavoriteProducts.value;
  return [];
});

// 加载用户信息
const loadUserInfo = async () => {
  const userId = route.params.userId;
  if (!userId) return;
  
  loading.value = true;
  error.value = null;
  
  try {
    const response = await topicAPI.getUserPublicInfo(userId);
    
    if (response) {
      userInfo.value = {
        basicInfo: {
          id: response.id || response.basicInfo?.id,
          username: response.username || response.basicInfo?.username,
          avatarUrl: response.avatarUrl || response.basicInfo?.avatarUrl,
          bio: response.bio || response.basicInfo?.bio,
          college: response.college || response.basicInfo?.college
        },
        academicInfo: response.academicInfo || {},
        identity: {
          verified: response.verified || response.identity?.verified || false,
          level1Tag: response.level1Tag || response.identity?.level1Tag
        },
        privacySettings: response.privacySettings || {},
        statistics: {
          postCount: response.postCount || response.statistics?.postCount || 0,
          likesReceived: response.likesReceived || response.statistics?.likesReceived || 0,
          followerCount: response.followerCount || response.statistics?.followerCount || 0,
          followingCount: response.followingCount || response.statistics?.followingCount || 0
        },
        canMessage: response.canMessage || false
      };
      
      privacySettings.value = userInfo.value.privacySettings;
      
      // 加载话题和商品
      loadUserTopics();
      loadUserProducts();
    } else {
      throw new Error('用户信息为空');
    }
  } catch (err) {
    console.error('❌ 加载用户信息失败:', err);
    error.value = err.response?.data?.message || '加载用户信息失败';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 加载用户话题
const loadUserTopics = async () => {
  if (!canViewContent.value && !isCurrentUser.value) return;
  
  topicsLoading.value = true;
  const userId = route.params.userId;
  
  try {
    const [publishedRes, participatedRes, likedRes] = await Promise.all([
      topicAPI.getUserPublishedTopics(userId),
      topicAPI.getUserParticipatedTopics(userId),
      topicAPI.getUserLikedTopics(userId)
    ]);
    
    const publishedData = publishedRes.data || publishedRes;
    const participatedData = participatedRes.data || participatedRes;
    const likedData = likedRes.data || likedRes;
    
    publishedTopics.value = publishedData.topics || publishedData.data?.topics || [];
    participatedTopics.value = participatedData.topics || participatedData.data?.topics || [];
    likedTopics.value = likedData.topics || likedData.data?.topics || [];
  } catch (error) {
    console.error('❌ 加载用户话题失败:', error);
    publishedTopics.value = [];
    participatedTopics.value = [];
    likedTopics.value = [];
  } finally {
    topicsLoading.value = false;
  }
};

// 加载用户商品
const loadUserProducts = async () => {
  if (!canViewContent.value && !isCurrentUser.value) return;
  
  productsLoading.value = true;
  const userId = route.params.userId;
  
  try {
    const publishedRes = await productAPI.getProducts({ sellerId: userId, status: null, size: 20 });
    userPublishedProducts.value = publishedRes?.records || publishedRes?.data?.records || [];
    
    if (isCurrentUser.value) {
      const favoritesRes = await productAPI.getMyFavorites({ size: 20 });
      userFavoriteProducts.value = favoritesRes || favoritesRes?.data || [];
    }
  } catch (error) {
    console.error('❌ 加载用户商品失败:', error);
    userPublishedProducts.value = [];
    userFavoriteProducts.value = [];
  } finally {
    productsLoading.value = false;
  }
};

// 操作处理
const viewProduct = (product) => {
  router.push(`/product/${product.id}`);
};

const handleFollow = () => {
  isFollowing.value = !isFollowing.value;
  ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注');
};

const handleMessage = () => {
  ElMessage.info('消息功能开发中...');
};

const submitReport = async ({ type, reason }) => {
  if (!reason.trim()) {
    ElMessage.warning('请输入举报原因');
    return;
  }
  
  try {
    await axios.post('/api/reports', {
      targetId: route.params.userId,
      targetType: 'user',
      reportType: type,
      reason: reason
    });
    
    ElMessage.success('举报成功，我们会尽快处理');
    showReportModal.value = false;
  } catch (error) {
    ElMessage.error('举报失败，请稍后重试');
  }
};

const goBack = () => {
  router.back();
};

const updatePrivacySettings = async (value) => {
  try {
    privacySettings.value.publicVisible = value;
    ElMessage.success('隐私设置已保存');
  } catch (error) {
    ElMessage.error('更新失败，请稍后重试');
    privacySettings.value.publicVisible = !value;
  }
};

const viewTopicDetail = (topic) => {
  router.push(`/topic/${topic.id}`);
};

// 监听路由参数变化
watch(
  () => route.params.userId,
  (newUserId) => {
    if (newUserId) {
      const userId = parseInt(newUserId);
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      const currentUserId = userData.id;
      
      // 如果是访问自己的主页，重定向到个人中心
      if (userId === currentUserId) {
        router.replace('/personalcenter');
        return;
      }
      
      loadUserInfo();
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.loading-container, .error-container {
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
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.retry-btn {
  margin-top: 20px;
  padding: 12px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.user-content {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.content-sections {
  padding: 30px 40px;
}

.main-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0;
}

.main-tab-btn {
  padding: 14px 32px;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
  font-weight: 500;
}

.main-tab-btn:hover {
  color: #409eff;
}

.main-tab-btn.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 600;
}

.privacy-restricted {
  text-align: center;
  padding: 60px 20px;
}

.privacy-restricted .restricted-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.privacy-restricted h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #666;
}

.privacy-restricted p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .user-profile-page {
    padding: 0;
  }
  
  .user-content {
    border-radius: 0;
  }
  
  .content-sections {
    padding: 20px;
  }
}
</style>
