<template>
  <div class="search-results-page">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="search"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 搜索头部 -->
      <div class="search-header">
        <div class="search-header-content">
          <h1 class="search-title">🔍 搜索结果</h1>
          <div class="search-summary">
            <span v-if="searchQuery">关键词：<strong>"{{ searchQuery }}"</strong></span>
            <span v-if="searchTypeText" class="type-separator">|</span>
            <span v-if="searchTypeText">类型：<strong>{{ searchTypeText }}</strong></span>
            <span v-if="hasTags" class="type-separator">|</span>
            <span v-if="hasTags">标签：<strong>{{ selectedTagsText }}</strong></span>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在搜索中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">❌</div>
        <p class="error-message">{{ error }}</p>
        <el-button type="primary" @click="retrySearch">重试</el-button>
      </div>

      <!-- 空结果状态 -->
      <div v-else-if="!hasResults" class="empty-state">
        <div class="empty-icon">😕</div>
        <p class="empty-message">未找到相关结果</p>
        <p class="empty-hint">试试其他关键词或清除筛选条件</p>
        <el-button @click="clearFilters">清除所有筛选</el-button>
      </div>

      <!-- 搜索结果 -->
      <div v-else class="results-container">
        <!-- 结果统计 -->
        <div class="results-summary">
          <span>找到 <strong>{{ totalResults }}</strong> 个相关结果</span>
        </div>

        <!-- 分类结果 -->
        <div v-if="results.locations && results.locations.length > 0" class="result-section">
          <div class="section-header">
            <h2 class="section-title">📍 地点</h2>
            <span class="section-count">{{ results.locations.length }} 个结果</span>
          </div>
          <div class="results-grid">
            <div
              v-for="location in results.locations"
              :key="location.id"
              class="result-card location-card"
              @click="goToLocation(location.id)"
            >
              <div class="card-image">
                <img v-if="location.imageUrl" :src="location.imageUrl" :alt="location.name" />
                <div v-else class="image-placeholder">📍</div>
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ location.name }}</h3>
                <p class="card-desc">{{ location.description || '暂无描述' }}</p>
                <div class="card-meta">
                  <span class="meta-item">📍 {{ getCategoryName(location.category) }}</span>
                  <span v-if="location.isPopular" class="meta-tag popular">热门</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="results.products && results.products.length > 0" class="result-section">
          <div class="section-header">
            <h2 class="section-title">🛒 商品</h2>
            <span class="section-count">{{ results.products.length }} 个结果</span>
          </div>
          <div class="results-grid">
            <div
              v-for="product in results.products"
              :key="product.id"
              class="result-card product-card"
              @click="goToProduct(product.id)"
            >
              <div class="card-image">
                <img v-if="product.images && product.images.length > 0" :src="product.images[0]" :alt="product.title" />
                <div v-else class="image-placeholder">🛒</div>
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ product.title }}</h3>
                <p class="card-price">¥{{ product.price }}</p>
                <div class="card-meta">
                  <span class="meta-item">👤 {{ product.sellerName || '卖家' }}</span>
                  <span class="meta-item">📍 {{ product.location || '暂无' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="results.topics && results.topics.length > 0" class="result-section">
          <div class="section-header">
            <h2 class="section-title">💬 话题</h2>
            <span class="section-count">{{ results.topics.length }} 个结果</span>
          </div>
          <div class="results-list">
            <div
              v-for="topic in results.topics"
              :key="topic.id"
              class="result-item topic-item"
              @click="goToTopic(topic.id)"
            >
              <div class="item-icon">💬</div>
              <div class="item-content">
                <h3 class="item-title">{{ topic.title }}</h3>
                <p class="item-desc">{{ topic.content?.substring(0, 100) || '' }}...</p>
                <div class="item-meta">
                  <span class="meta-item">👤 {{ topic.authorName || '匿名用户' }}</span>
                  <span class="meta-item">🕐 {{ formatDate(topic.createdAt) }}</span>
                  <span class="meta-item">👁️ {{ topic.viewCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="results.topicInfos && results.topicInfos.length > 0" class="result-section">
          <div class="section-header">
            <h2 class="section-title">📰 话题信息</h2>
            <span class="section-count">{{ results.topicInfos.length }} 个结果</span>
          </div>
          <div class="results-list">
            <div
              v-for="info in results.topicInfos"
              :key="info.id"
              class="result-item info-item"
              @click="goToTopic(info.topicId)"
            >
              <div class="item-icon">📰</div>
              <div class="item-content">
                <h3 class="item-title">{{ info.title }}</h3>
                <p class="item-desc">{{ info.summary || '暂无摘要' }}</p>
                <div class="item-meta">
                  <span class="meta-item">🕐 {{ formatDate(info.publishTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="results.users && results.users.length > 0" class="result-section">
          <div class="section-header">
            <h2 class="section-title">👤 用户</h2>
            <span class="section-count">{{ results.users.length }} 个结果</span>
          </div>
          <div class="results-grid">
            <div
              v-for="user in results.users"
              :key="user.id"
              class="result-card user-card"
              @click="goToUserProfile(user.id)"
            >
              <div class="user-avatar">
                <img v-if="user.avatar" :src="user.avatar" :alt="user.username" />
                <div v-else class="avatar-placeholder">{{ user.username?.charAt(0) || 'U' }}</div>
              </div>
              <div class="user-info">
                <h3 class="user-name">{{ user.username }}</h3>
                <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写' }}</p>
                <div class="user-stats">
                  <span class="stat-item">📝 {{ user.topicCount || 0 }} 话题</span>
                  <span class="stat-item">👍 {{ user.followerCount || 0 }} 粉丝</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { searchAPI } from '@/api/search';

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

// 搜索参数
const searchQuery = ref('');
const searchType = ref('all');
const selectedTags = ref([]);
const quickFilters = ref([]);

// 搜索结果
const loading = ref(false);
const error = ref(null);
const results = ref({
  locations: [],
  products: [],
  topics: [],
  topicInfos: [],
  users: []
});
const totalResults = ref(0);

// 搜索类型文本映射
const searchTypeTextMap = {
  all: '全部',
  location: '地点',
  product: '商品',
  topic: '话题',
  topic_info: '话题信息',
  user: '用户'
};

const searchTypeText = computed(() => {
  return searchTypeTextMap[searchType.value] || '';
});

const hasTags = computed(() => {
  return selectedTags.value.length > 0;
});

const selectedTagsText = computed(() => {
  return selectedTags.value.join(', ');
});

const hasResults = computed(() => {
  return totalResults.value > 0;
});

// 获取地点分类名称
const getCategoryName = (category) => {
  const categoryMap = {
    teaching: '教学楼',
    library: '图书馆',
    cafeteria: '食堂',
    dormitory: '宿舍',
    sports: '体育设施',
    admin: '行政楼',
    other: '其他'
  };
  return categoryMap[category] || '其他';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

// 执行搜索
const executeSearch = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await searchAPI.search({
      query: searchQuery.value,
      type: searchType.value,
      tags: selectedTags.value,
      quickFilters: quickFilters.value
    });
    
    console.log('搜索响应:', response);
    
    // 解析响应数据 - 后端返回的标准格式
    const responseData = response || {};
    
    results.value = {
      locations: responseData.locations || [],
      products: responseData.products || [],
      topics: responseData.topics || [],
      topicInfos: responseData.topicInfos || [],
      users: responseData.users || []
    };
    
    totalResults.value = responseData.total || 0;
    
    console.log('解析后的结果:', results.value);
  } catch (err) {
    console.error('搜索失败:', err);
    error.value = '搜索失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

// 解析 URL 参数
const parseUrlParams = () => {
  searchQuery.value = route.query.q || '';
  searchType.value = route.query.type || 'all';
  
  if (route.query.tags) {
    selectedTags.value = route.query.tags.split(',').filter(tag => tag.trim());
  }
  
  if (route.query.quickFilters) {
    quickFilters.value = route.query.quickFilters.split(',').filter(filter => filter.trim());
  }
};

// 重试搜索
const retrySearch = () => {
  executeSearch();
};

// 清除筛选
const clearFilters = () => {
  router.push({
    path: '/search/results',
    query: {}
  });
};

// 跳转函数
const goToLocation = (locationId) => {
  router.push(`/location/${locationId}`);
};

const goToProduct = (productId) => {
  router.push(`/product/${productId}`);
};

const goToTopic = (topicId) => {
  router.push(`/topic/${topicId}`);
};

const goToUserProfile = (userId) => {
  router.push(`/user/profile/${userId}`);
};

onMounted(() => {
  updateDeviceDetection();
  parseUrlParams();
  executeSearch();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.search-results-page {
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

/* 搜索头部 */
.search-header {
  background: white;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.search-header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.search-title {
  margin: 0 0 15px 0;
  font-size: 2rem;
  color: #333;
}

.search-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: #666;
  font-size: 1rem;
}

.type-separator {
  color: #ccc;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4c6ef5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
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
  padding: 80px 20px;
}

.error-icon {
  font-size: 5rem;
  margin-bottom: 20px;
}

.error-message {
  color: #f56c6c;
  margin-bottom: 20px;
  font-size: 1.2rem;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 20px;
}

.empty-message {
  color: #999;
  margin-bottom: 10px;
  font-size: 1.2rem;
}

.empty-hint {
  color: #bbb;
  margin-bottom: 20px;
}

/* 结果容器 */
.results-container {
  padding: 20px 30px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
}

.results-summary {
  margin-bottom: 25px;
  color: #555;
  font-size: 1rem;
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.results-summary strong {
  font-size: 1.2rem;
  font-weight: 700;
}

/* 结果分区 */
.result-section {
  margin-bottom: 35px;
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 2px solid #f5f5f5;
}

.section-title {
  margin: 0;
  font-size: 1.4rem;
  color: #333;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-count {
  color: #888;
  font-size: 0.85rem;
  background: #f5f5f5;
  padding: 5px 12px;
  border-radius: 20px;
  font-weight: 500;
}

/* 结果网格 */
.results-grid {
  display: grid !important;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)) !important;
  gap: 18px;
  width: 100%;
}

/* 响应式网格 - 根据屏幕宽度调整列数 */
@media (min-width: 1200px) {
  .results-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)) !important;
  }
}

@media (min-width: 1400px) {
  .results-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)) !important;
  }
}

@media (min-width: 1600px) {
  .results-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)) !important;
  }
}

/* 结果列表 */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 结果卡片 */
.result-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  display: flex !important;
  flex-direction: row !important;
  border: 1px solid rgba(0, 0, 0, 0.04);
  width: 100%;
}

.result-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-color: rgba(102, 126, 234, 0.3);
}

.card-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  overflow: hidden;
  background: #f8f9fa;
  position: relative;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.result-card:hover .card-image img {
  transform: scale(1.05);
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

.card-content {
  flex: 1;
  padding: 15px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  margin: 0;
  font-size: 1.05rem;
  color: #333;
  font-weight: 700;
  line-height: 1.3;
}

.card-desc {
  margin: 0;
  color: #777;
  font-size: 0.85rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-price {
  color: #f56c6c;
  font-size: 1.2rem;
  font-weight: 700;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.meta-item {
  color: #888;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-tag {
  padding: 3px 8px;
  border-radius: 5px;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.meta-tag.popular {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: white;
  box-shadow: 0 2px 6px rgba(255, 154, 158, 0.4);
}

/* 列表项 */
.result-item {
  display: flex;
  gap: 18px;
  padding: 22px;
  background: #fafafa;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.result-item:hover {
  background: linear-gradient(135deg, #f8f9ff 0%, #fff5f8 100%);
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: rgba(102, 126, 234, 0.2);
}

.item-icon {
  font-size: 3.2rem;
  flex-shrink: 0;
  line-height: 1;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-title {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
  font-weight: 700;
}

.item-desc {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
  line-height: 1.6;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: auto;
}

/* 用户卡片 */
.user-card {
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 25px;
}

.user-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 3px solid white;
}

.user-avatar img {
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 2.2rem;
  font-weight: bold;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-name {
  margin: 0;
  font-size: 1.15rem;
  color: #333;
  font-weight: 700;
}

.user-bio {
  margin: 0;
  color: #777;
  font-size: 0.9rem;
  flex: 1;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0;
  }

  .results-container {
    padding: 15px;
  }

  .results-grid {
    grid-template-columns: 1fr;
  }

  .result-card {
    flex-direction: column;
  }

  .card-image {
    width: 100%;
    height: 200px;
  }

  .search-header {
    padding: 20px 15px;
  }

  .search-title {
    font-size: 1.5rem;
  }
}
</style>
