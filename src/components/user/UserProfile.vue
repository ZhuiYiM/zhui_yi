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
        :is-blocked="isBlocked"
        :can-message="userInfo.canMessage"
        @follow="handleFollow"
        @message="handleMessage"
        @block="showBlockModal = true"
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
        <!-- 主标签切换：发布、参与、点赞、商品、商户评价、地点 -->
        <div class="main-tabs">
          <button 
            @click="currentTab = 'published'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'published' }"
          >
            📝 发布的话题
          </button>
          <button 
            @click="currentTab = 'participated'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'participated' }"
          >
            💬 参与的话题
          </button>
          <button 
            @click="currentTab = 'liked'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'liked' }"
          >
            ❤️ 点赞的话题
          </button>
          <button 
            @click="currentTab = 'products'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'products' }"
          >
            🛒 TA 的商品
          </button>
          <button 
            @click="currentTab = 'reviews'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'reviews' }"
          >
            ⭐ 商户评价
          </button>
          <button 
            @click="currentTab = 'locations'"
            class="main-tab-btn"
            :class="{ active: currentTab === 'locations' }"
          >
            📍 TA 的地点
          </button>
        </div>
        
        <!-- 商品内容 -->
        <ProductSection 
          v-if="currentTab === 'products'"
          :products="productsList"
          :current-tab="'published'"
          :is-current-user="false"
          :loading="productsLoading"
          @tab-change="productTab = $event"
          @view-product="viewProduct"
        />
        
        <!-- 商户评价内容 -->
        <div v-else-if="currentTab === 'reviews'" class="seller-reviews-section">
          <div v-if="sellerReviewsLoading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-else-if="sellerReviews.length === 0" class="empty-state">
            <div class="empty-icon">⭐</div>
            <p>暂无商户评价</p>
          </div>
          <div v-else class="reviews-list">
            <ReviewItem
              v-for="review in sellerReviews"
              :key="review.id"
              :review="review"
              @view-product="viewProduct"
              @view-image="viewImage"
            />
          </div>
        </div>
        
        <!-- 用户地点标记内容 -->
        <div v-else-if="currentTab === 'locations'" class="user-locations-section">
          <div v-if="userLocationsLoading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-else-if="userLocations.length === 0" class="empty-state">
            <div class="empty-icon">📍</div>
            <p>暂无公开地点标记</p>
          </div>
          <div v-else class="locations-list">
            <div
              v-for="location in userLocations"
              :key="location.id"
              class="location-card"
              @click="viewLocation(location)"
            >
              <div class="location-image">
                <img v-if="location.images && location.images.length > 0" :src="location.images[0]" :alt="location.locationName" />
                <div v-else class="image-placeholder">📍</div>
              </div>
              <div class="location-info">
                <h3 class="location-name">{{ location.locationName }}</h3>
                <p class="location-type">{{ getMarkTypeName(location.markType) }}</p>
                <p class="location-category">{{ getMarkCategoryName(location.markCategory) }}</p>
                <p v-if="location.description" class="location-description">{{ location.description }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 话题内容 -->
        <TopicSection 
          v-else
          :topics="getTopicsList"
          :loading="topicsLoading"
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

    <!-- 发送消息弹窗 -->
    <el-dialog
      v-model="showMessageModal"
      title="发送私信"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="message-dialog-content">
        <div class="recipient-info" v-if="userInfo">
          <img :src="userInfo.basicInfo.avatarUrl" alt="头像" class="recipient-avatar">
          <div class="recipient-name">{{ userInfo.basicInfo.username }}</div>
        </div>
        <el-input
          v-model="messageContent"
          type="textarea"
          :rows="6"
          placeholder="请输入消息内容..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showMessageModal = false">取消</el-button>
          <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拉黑选项菜单 -->
    <el-dialog
      v-model="showBlockModal"
      :title="isBlocked ? '取消拉黑' : '拉黑用户'"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="block-dialog-content">
        <p v-if="isBlocked">
          确定要取消拉黑该用户吗？取消后可以继续接收来自该用户的消息。
        </p>
        <p v-else>
          确定要拉黑该用户吗？拉黑后将不会收到来自该用户的消息，但对方仍可以查看您的公开信息。
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBlockModal = false">取消</el-button>
          <el-button :type="isBlocked ? 'success' : 'danger'" @click="handleBlock" :loading="blocking">
            {{ isBlocked ? '取消拉黑' : '确认拉黑' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { topicAPI } from '@/api/topic';
import { productAPI } from '@/api/product';
import { messageAPI } from '@/api/message';
import { blockAPI } from '@/api/block';
import { reviewAPI } from '@/api/review';
import { campusAPI } from '@/api/campus';
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
import ReviewItem from '@/components/mall/ReviewItem.vue';

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
const showMessageModal = ref(false);
const showBlockModal = ref(false);
const isBlocked = ref(false); // 当前用户是否被拉黑
const messageContent = ref(''); // 发送的消息内容
const sending = ref(false); // 发送消息中
const blocking = ref(false); // 拉黑操作中

// 商品相关
const productsLoading = ref(false);
const userPublishedProducts = ref([]);

// 商户评价相关
const sellerReviewsLoading = ref(false);
const sellerReviews = ref([]);

// 用户地点标记相关
const userLocationsLoading = ref(false);
const userLocations = ref([]);

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
  if (currentTab.value === 'participated') return participatedTopics.value;
  if (currentTab.value === 'liked') return likedTopics.value;
  return []; // 商品标签时返回空数组
});

const productsList = computed(() => {
  return userPublishedProducts.value;
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
      
      // 检查拉黑状态
      checkBlockStatus();
      
      // 加载话题和商品
      loadUserTopics();
      loadUserProducts();
      loadSellerReviews();
      loadUserLocations();
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

// 加载卖家评价
const loadSellerReviews = async () => {
  const userId = route.params.userId;
  if (!userId) return;
  
  sellerReviewsLoading.value = true;
  
  try {
    const response = await reviewAPI.getSellerReviews(parseInt(userId), 1, 50);
    sellerReviews.value = response || response?.data || [];
    console.log('卖家评价数据:', sellerReviews.value);
  } catch (error) {
    console.error('加载卖家评价失败:', error);
    sellerReviews.value = [];
  } finally {
    sellerReviewsLoading.value = false;
  }
};

// 加载用户地点标记（public_active 的公开地点）
const loadUserLocations = async () => {
  const userId = route.params.userId;
  if (!userId) return;
  
  userLocationsLoading.value = true;
  
  try {
    // 获取该用户的公开地点标记
    const response = await campusAPI.getUserPublicLocationMarks(userId);
    const allMarks = response || response?.data || [];
    
    // 筛选出该用户的公开地点（public_active 或 public_passive，且审核通过）
    userLocations.value = allMarks.filter(mark => {
      return mark.userId === parseInt(userId) && 
             (mark.visibility === 'public_active' || mark.visibility === 'public_passive') && 
             mark.verificationStatus === 'approved';
    });
  } catch (error) {
    console.error('加载用户地点标记失败:', error);
    userLocations.value = [];
  } finally {
    userLocationsLoading.value = false;
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

// 检查拉黑状态
const checkBlockStatus = async () => {
  const userId = route.params.userId;
  if (!userId || isCurrentUser.value) return;
  
  try {
    const response = await blockAPI.isBlocked(parseInt(userId));
    isBlocked.value = response.data?.blocked || false;
  } catch (error) {
    console.error('检查拉黑状态失败:', error);
    isBlocked.value = false;
  }
};

// 操作处理
const viewProduct = (product) => {
  router.push(`/product/${product.id}`);
};

const viewLocation = (location) => {
  router.push(`/user-location/${location.id}`);
};

const viewImage = (imageUrl) => {
  // 查看图片逻辑（可以添加图片预览弹窗）
  console.log('查看图片:', imageUrl);
};

const handleFollow = () => {
  isFollowing.value = !isFollowing.value;
  ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注');
};

const handleMessage = () => {
  showMessageModal.value = true;
};

const handleBlock = async () => {
  const userId = route.params.userId;
  if (!userId) return;
  
  try {
    if (isBlocked.value) {
      // 取消拉黑
      await ElMessageBox.confirm('确定要取消拉黑该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      
      blocking.value = true;
      await blockAPI.unblockUser(parseInt(userId));
      isBlocked.value = false;
      ElMessage.success('已取消拉黑');
      showBlockModal.value = false;
    } else {
      // 拉黑用户
      await ElMessageBox.confirm('确定要拉黑该用户吗？拉黑后将不会收到来自该用户的消息。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      
      blocking.value = true;
      await blockAPI.blockUser(parseInt(userId));
      isBlocked.value = true;
      ElMessage.success('已成功拉黑该用户');
      showBlockModal.value = false;
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error);
      ElMessage.error(error.response?.data?.message || '操作失败，请稍后重试');
    }
  } finally {
    blocking.value = false;
  }
};

const sendMessage = async () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入消息内容');
    return;
  }
  
  const userId = route.params.userId;
  if (!userId) return;
  
  try {
    sending.value = true;
    await messageAPI.sendMessage({
      toUserId: parseInt(userId),
      content: messageContent.value,
      type: 'private_message'
    });
    
    ElMessage.success('消息发送成功');
    messageContent.value = '';
    showMessageModal.value = false;
    
    // 通知消息中心刷新（使用自定义事件或 localStorage）
    localStorage.setItem('refreshMessages', Date.now().toString());
    window.dispatchEvent(new Event('refreshMessages'));
    
    // 询问是否跳转到消息中心
    ElMessageBox.confirm('消息已发送，是否前往消息中心查看？', '提示', {
      confirmButtonText: '前往查看',
      cancelButtonText: '稍后再说',
      type: 'success'
    }).then(() => {
      router.push('/message');
    }).catch(() => {});
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error(error.response?.data?.message || '发送失败，请稍后重试');
  } finally {
    sending.value = false;
  }
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

.reviews-placeholder {
  text-align: center;
  padding: 60px 20px;
}

.reviews-placeholder .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.reviews-placeholder p {
  margin: 0;
  color: #999;
  font-size: 16px;
}

/* 卖家评价区域 */
.seller-reviews-section {
  margin-top: 20px;
}

.seller-reviews-section .loading-state,
.seller-reviews-section .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.seller-reviews-section .spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.seller-reviews-section .empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ccc;
}

.seller-reviews-section .empty-state p {
  margin: 0;
  color: #999;
  font-size: 16px;
}

.seller-reviews-section .reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 用户地点标记区域 */
.user-locations-section {
  margin-top: 20px;
}

.user-locations-section .loading-state,
.user-locations-section .empty-state {
  text-align: center;
  padding: 60px 20px;
}

.user-locations-section .spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

.user-locations-section .empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ccc;
}

.user-locations-section .empty-state p {
  margin: 0;
  color: #999;
  font-size: 16px;
}

.locations-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.location-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.location-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.location-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.location-image img {
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
  font-size: 48px;
  color: #ccc;
}

.location-info {
  padding: 16px;
}

.location-info .location-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-info .location-type,
.location-info .location-category {
  font-size: 14px;
  color: #666;
  margin: 0 0 6px 0;
}

.location-info .location-description {
  font-size: 14px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-dialog-content {
  padding: 10px 0;
}

.recipient-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.recipient-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.recipient-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.block-dialog-content {
  padding: 10px 0;
}

.block-dialog-content p {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
