<template>
  <div :class="['topic-wall-container', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="topicwall"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 头部区域 -->
      <header class="page-header">
        <div class="header-content">
          <h1 class="page-title">{{ isMobile ? '校园话题墙' : '校园话题墙' }}</h1>
          
          <!-- 搜索区域 -->
          <div class="search-section">
            <div class="search-box">
              <input 
                v-model="searchQuery"
                type="text" 
                placeholder="搜索话题、标签或用户..." 
                @keyup.enter="handleSearch"
                class="search-input"
              >
              <button @click="handleSearch" class="search-btn">🔍</button>
            </div>
            
            <!-- 操作按钮区域 -->
            <div class="action-buttons">
              <!-- 刷新按钮 (桌面端) -->
              <button 
                v-if="!isMobile" 
                @click="refreshAllData"
                class="refresh-btn"
                :disabled="loading"
              >
                <span v-if="loading" class="loading-spinner"></span>
                <span v-else>🔄 刷新</span>
              </button>
              
              <!-- 发布按钮 (桌面端) -->
              <button 
                v-if="!isMobile" 
                @click="showPublishModal = true"
                class="publish-btn"
              >
                📝 发布话题
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div 
        class="content-wrapper"
        @scroll="handleScroll"
        ref="contentWrapper"
      >
        <!-- 左侧内容区 -->
        <div 
          class="left-content"
          @touchstart="handleTouchStart"
          @touchmove="handleTouchMove"
          @touchend="handleTouchEnd"
          ref="topicsContainer"
        >
          <!-- 热门标签 -->
          <section class="popular-tags">
            <h2>热门标签</h2>
            <div class="tags-container">
              <span 
                v-for="tag in popularTags" 
                :key="tag"
                @click="filterByTag(tag)"
                class="tag-item"
                :class="{ active: activeTag === tag }"
              >
                #{{ tag }}
              </span>
            </div>
          </section>

          <!-- 话题列表 -->
          <section class="topics-section">
            <div class="section-header">
              <h2>{{ activeTag ? `#${activeTag}` : '最新话题' }}</h2>
              <div class="sort-options">
                <button 
                  v-for="option in sortOptions" 
                  :key="option.value"
                  @click="changeSort(option.value)"
                  class="sort-btn"
                  :class="{ active: currentSort === option.value }"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <div class="spinner"></div>
              <p>加载中...</p>
            </div>

            <!-- 空状态 -->
            <div v-else-if="posts.length === 0" class="empty-state">
              <div class="empty-icon">💭</div>
              <h3>暂无话题</h3>
              <p>成为第一个发布话题的人吧！</p>
              <button @click="showPublishModal = true" class="primary-btn">
                发布话题
              </button>
            </div>

            <!-- 话题列表 -->
            <div v-else class="topics-list">
              <div 
                v-for="post in posts" 
                :key="post.id"
                class="topic-card"
              >
                <div class="post-header">
                  <div class="author-info">
                    <img :src="post.author.avatar || defaultAvatar" :alt="post.author.name" class="author-avatar">
                    <div class="author-details">
                      <span class="author-name">{{ post.author.name }}</span>
                      <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                    </div>
                  </div>
                  
                  <div class="post-actions">
                    <button @click="toggleLike(post)" class="action-btn" :class="{ liked: post.isLiked }">
                      👍 {{ post.likes }}
                    </button>
                    <button @click="commentPost(post.id)" class="action-btn">
                      💬 {{ post.comments }}
                    </button>
                    <button @click="sharePost(post)" class="action-btn">
                      ↗️ 分享
                    </button>
                  </div>
                </div>

                <div class="post-content">
                  <p>{{ post.content }}</p>
                  
                  <!-- 图片展示 -->
                  <div v-if="post.images && post.images.length > 0" class="post-images">
                    <img 
                      v-for="(image, index) in post.images.slice(0, 4)" 
                      :key="index"
                      :src="image" 
                      :alt="`图片${index + 1}`"
                      class="post-image"
                      @click="previewImage(image)"
                    >
                    <div 
                      v-if="post.images.length > 4" 
                      class="more-images"
                      @click="previewAllImages(post.images)"
                    >
                      +{{ post.images.length - 4 }}
                    </div>
                  </div>

                  <!-- 标签 -->
                  <div v-if="post.tags && post.tags.length > 0" class="post-tags">
                    <span 
                      v-for="tag in post.tags" 
                      :key="tag"
                      @click="filterByTag(tag)"
                      class="post-tag"
                    >
                      #{{ tag }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 分页 -->
              <div v-if="totalPages > 1" class="pagination">
                <button 
                  @click="changePage(currentPage - 1)" 
                  :disabled="currentPage === 1"
                  class="page-btn"
                >
                  上一页
                </button>
                
                <span class="page-info">
                  第 {{ currentPage }} 页，共 {{ totalPages }} 页
                </span>
                
                <button 
                  @click="changePage(currentPage + 1)" 
                  :disabled="currentPage === totalPages"
                  class="page-btn"
                >
                  下一页
                </button>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧侧边栏 (桌面端) -->
        <div v-if="!isMobile" class="right-sidebar">
          <!-- 发布话题卡片 -->
          <div class="publish-card">
            <h3>发布新话题</h3>
            <textarea 
              v-model="quickPostContent"
              placeholder="分享你的校园生活..."
              class="quick-post-textarea"
              rows="3"
            ></textarea>
            <div class="quick-post-actions">
              <button @click="addQuickImage" class="icon-btn">📷</button>
              <button @click="addQuickTag" class="icon-btn">🏷️</button>
              <button @click="publishQuickPost" class="publish-btn">发布</button>
            </div>
          </div>

          <!-- 热门用户 -->
          <div class="hot-users">
            <h3>活跃用户</h3>
            <div class="users-list">
              <div 
                v-for="user in hotUsers" 
                :key="user.id"
                class="user-item"
              >
                <img :src="user.avatar || defaultAvatar" :alt="user.name" class="user-avatar">
                <div class="user-info">
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-posts">{{ user.postCount }} 篇</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="stats-card">
            <h3>社区统计</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-number">{{ stats.totalPosts }}</span>
                <span class="stat-label">总话题</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ stats.activeUsers }}</span>
                <span class="stat-label">活跃用户</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ stats.todayPosts }}</span>
                <span class="stat-label">今日新增</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 发布话题模态框 -->
    <div v-if="showPublishModal" class="modal-overlay" @click="closeModal">
      <div class="publish-modal" @click.stop>
        <div class="modal-header">
          <h2>发布新话题</h2>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <textarea 
            v-model="newPostContent"
            placeholder="分享你的想法..."
            class="post-textarea"
            rows="5"
          ></textarea>
          
          <div class="post-media">
            <div class="image-preview">
              <img 
                v-for="(image, index) in postImages" 
                :key="index"
                :src="image" 
                alt="预览图片"
                class="preview-image"
              >
              <button 
                v-if="postImages.length < 9"
                @click="addImageToPost"
                class="add-image-btn"
              >
                +
              </button>
            </div>
            
            <div class="tag-input">
              <input 
                v-model="tagInput"
                type="text" 
                placeholder="添加标签 (回车确认)"
                @keyup.enter="addTag"
              >
              <div class="selected-tags">
                <span 
                  v-for="(tag, index) in postTags" 
                  :key="index"
                  class="selected-tag"
                >
                  #{{ tag }}
                  <button @click="removeTag(index)" class="remove-tag">×</button>
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button @click="publishPost" class="submit-btn">发布</button>
        </div>
      </div>
    </div>

    <!-- 图片预览模态框 -->
    <div v-if="showImagePreview" class="modal-overlay" @click="closeImagePreview">
      <div class="image-preview-modal" @click.stop>
        <button @click="closeImagePreview" class="close-btn">×</button>
        <img :src="previewImageUrl" alt="预览图片" class="preview-large-image">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { topicAPI } from '@/api/topic';

const router = useRouter();

// 响应式数据
const isMobile = ref(false);
const loading = ref(false);
const showPublishModal = ref(false);
const showImagePreview = ref(false);
const previewImageUrl = ref('');

// 刷新相关数据
const contentWrapper = ref(null);
const topicsContainer = ref(null);
const touchStartY = ref(0);
const isRefreshing = ref(false);
const pullDistance = ref(0);
const maxPullDistance = ref(80);

// 用户信息
const currentUser = ref(null);

const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

// 搜索和筛选
const searchQuery = ref('');
const activeTag = ref('');
const currentSort = ref('latest');

const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hot' },
  { label: '精华', value: 'essence' }
];

// 发布相关
const newPostContent = ref('');
const quickPostContent = ref('');
const postImages = ref([]);
const postTags = ref([]);
const tagInput = ref('');

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);
const totalPosts = ref(0);

// 热门标签数据
const popularTags = ref([]);

// 话题数据
const posts = ref([]);

// 活跃用户数据
const hotUsers = ref([]);

// 统计数据
const stats = ref({
  totalPosts: 0,
  activeUsers: 0,
  todayPosts: 0
});

// 计算属性
const totalPages = computed(() => Math.ceil(totalPosts.value / pageSize.value));

// API调用方法
const fetchTopics = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: currentSort.value,
      tag: activeTag.value,
      search: searchQuery.value
    };
    
    const response = await topicAPI.getTopics(params);
    if (response.data) {
      posts.value = response.data.topics || [];
      totalPosts.value = response.data.total || 0;
      stats.value.totalPosts = response.data.totalPosts || 0;
      stats.value.todayPosts = response.data.todayPosts || 0;
    }
  } catch (error) {
    console.error('获取话题列表失败:', error);
    ElMessage.error('获取话题失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const fetchPopularTags = async () => {
  try {
    const response = await topicAPI.getPopularTags();
    if (response.data) {
      popularTags.value = response.data.tags || [];
    }
  } catch (error) {
    console.error('获取热门标签失败:', error);
    // 检查是否是数据库表不存在的错误
    if (error.response?.data?.message?.includes('Table') && error.response?.data?.message?.includes("doesn't exist")) {
      console.warn('数据库标签表不存在，使用默认标签');
      ElMessage.warning('标签功能暂时不可用，使用默认标签');
    }
    // 使用默认标签作为后备
    popularTags.value = [
      '校园生活', '学习经验', '社团活动', '美食推荐',
      '考试心得', '实习分享', '就业信息', '旅行攻略',
      '技术交流', '生活感悟', '求助帖', '表白墙'
    ];
  }
};

const publishTopic = async (topicData) => {
  try {
    const response = await topicAPI.createTopic(topicData);
    if (response.data) {
      ElMessage.success('发布成功！');
      // 重新获取话题列表
      await fetchTopics();
      return response.data;
    }
  } catch (error) {
    console.error('发布话题失败:', error);
    ElMessage.error('发布失败，请稍后重试');
    throw error;
  }
};

const likeTopic = async (topicId) => {
  try {
    const response = await topicAPI.likeTopic({ topicId });
    if (response.data) {
      const topic = posts.value.find(p => p.id === topicId);
      if (topic) {
        topic.isLiked = !topic.isLiked;
        topic.likes += topic.isLiked ? 1 : -1;
      }
      ElMessage.success(topic.isLiked ? '点赞成功' : '取消点赞');
    }
  } catch (error) {
    console.error('点赞操作失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  }
};

// 方法
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

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

const formatTime = (date) => {
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

const handleSearch = async () => {
  if (searchQuery.value.trim()) {
    currentPage.value = 1;
    await fetchTopics();
  }
};

const filterByTag = async (tag) => {
  activeTag.value = activeTag.value === tag ? '' : tag;
  currentPage.value = 1;
  await fetchTopics();
};

const changeSort = async (sortType) => {
  currentSort.value = sortType;
  currentPage.value = 1;
  await fetchTopics();
};

const toggleLike = async (post) => {
  await likeTopic(post.id);
};

const commentPost = (postId) => {
  router.push(`/topic/${postId}`);
};

const sharePost = (post) => {
  const shareText = `${post.content}\n\n来自 @${post.author.name} 的话题`;
  if (navigator.share) {
    navigator.share({
      title: '校园话题分享',
      text: shareText
    });
  } else {
    navigator.clipboard.writeText(shareText);
    ElMessage.success('内容已复制到剪贴板');
  }
};

const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

const closeImagePreview = () => {
  showImagePreview.value = false;
  previewImageUrl.value = '';
};

const addImageToPost = () => {
  // 模拟图片上传
  const mockImages = [
    'https://placehold.co/200x200/FF6B6B/FFFFFF?text=图1',
    'https://placehold.co/200x200/4ECDC4/FFFFFF?text=图2',
    'https://placehold.co/200x200/FFE66D/333333?text=图3'
  ];
  
  if (postImages.value.length < 9) {
    postImages.value.push(mockImages[postImages.value.length % 3]);
    ElMessage.success('图片添加成功');
  }
};

const addTag = () => {
  const tag = tagInput.value.trim();
  if (tag && !postTags.value.includes(tag)) {
    postTags.value.push(tag);
    tagInput.value = '';
    ElMessage.success('标签添加成功');
  }
};

const removeTag = (index) => {
  postTags.value.splice(index, 1);
};

const publishPost = async () => {
  if (!newPostContent.value.trim()) {
    ElMessage.warning('请输入话题内容');
    return;
  }

  const topicData = {
    content: newPostContent.value,
    tags: [...postTags.value],
    images: [...postImages.value]
  };

  try {
    await publishTopic(topicData);
    closeModal();
  } catch (error) {
    console.error('发布失败:', error);
  }
};

const publishQuickPost = async () => {
  if (!quickPostContent.value.trim()) {
    ElMessage.warning('请输入话题内容');
    return;
  }

  const topicData = {
    content: quickPostContent.value,
    tags: [],
    images: []
  };

  try {
    await publishTopic(topicData);
    quickPostContent.value = '';
  } catch (error) {
    console.error('快速发布失败:', error);
  }
};

const addQuickImage = () => {
  ElMessage.info('图片上传功能开发中...');
};

const addQuickTag = () => {
  ElMessage.info('标签功能开发中...');
};

const changePage = async (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    await fetchTopics();
  }
};

const closeModal = () => {
  showPublishModal.value = false;
  newPostContent.value = '';
  postImages.value = [];
  postTags.value = [];
  tagInput.value = '';
};

// 刷新功能
const refreshAllData = async () => {
  if (loading.value) return;
  
  loading.value = true;
  try {
    // 重置分页
    currentPage.value = 1;
    activeTag.value = '';
    searchQuery.value = '';
    currentSort.value = 'latest';
    
    // 并行获取所有数据
    await Promise.all([
      fetchTopics(),
      fetchPopularTags()
    ]);
    
    ElMessage.success('刷新成功');
  } catch (error) {
    console.error('刷新失败:', error);
    ElMessage.error('刷新失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 移动端下拉刷新处理
const handleTouchStart = (e) => {
  if (isMobile.value && !loading.value) {
    touchStartY.value = e.touches[0].clientY;
    pullDistance.value = 0;
  }
};

const handleTouchMove = (e) => {
  if (isMobile.value && !loading.value && touchStartY.value > 0) {
    const currentY = e.touches[0].clientY;
    const deltaY = currentY - touchStartY.value;
    
    // 只有在页面顶部且向下拉动时才触发
    if (deltaY > 0 && contentWrapper.value.scrollTop === 0) {
      e.preventDefault();
      pullDistance.value = Math.min(deltaY * 0.5, maxPullDistance.value);
      
      // 更新容器样式
      if (topicsContainer.value) {
        topicsContainer.value.style.transform = `translateY(${pullDistance.value}px)`;
        topicsContainer.value.style.transition = 'none';
      }
    }
  }
};

const handleTouchEnd = async () => {
  if (isMobile.value && pullDistance.value > 0) {
    // 如果拉动距离足够大，则触发刷新
    if (pullDistance.value >= maxPullDistance.value * 0.6) {
      await refreshAllData();
    }
    
    // 重置样式
    if (topicsContainer.value) {
      topicsContainer.value.style.transform = 'translateY(0)';
      topicsContainer.value.style.transition = 'transform 0.3s ease';
    }
    
    touchStartY.value = 0;
    pullDistance.value = 0;
  }
};

// 滚动到底部检测（移动端自动加载更多）
const handleScroll = () => {
  if (isMobile.value && !loading.value) {
    const wrapper = contentWrapper.value;
    if (wrapper) {
      const { scrollTop, scrollHeight, clientHeight } = wrapper;
      // 当滚动到底部附近时触发加载更多
      if (scrollTop + clientHeight >= scrollHeight - 100) {
        loadMoreTopics();
      }
    }
  }
};

// 加载更多话题
const loadMoreTopics = async () => {
  if (currentPage.value < totalPages.value && !loading.value) {
    loading.value = true;
    try {
      currentPage.value += 1;
      const params = {
        page: currentPage.value,
        size: pageSize.value,
        sort: currentSort.value,
        tag: activeTag.value,
        search: searchQuery.value
      };
      
      const response = await topicAPI.getTopics(params);
      if (response.data && response.data.topics) {
        posts.value = [...posts.value, ...response.data.topics];
        totalPosts.value = response.data.total || 0;
      }
    } catch (error) {
      console.error('加载更多话题失败:', error);
      ElMessage.error('加载更多失败');
      currentPage.value -= 1; // 回退页码
    } finally {
      loading.value = false;
    }
  }
};

const goToPage = (page) => {
  switch(page) {
    case 'home':
      router.push('/');
      break;
    case 'map':
      router.push('/map');
      break;
    case 'trade':
      router.push('/mall');
      break;
    case 'topic':
      // 当前页面，刷新
      window.location.reload();
      break;
    case 'message':
      router.push('/message');
      break;
    case 'profile':
      router.push('/personalcenter');
      break;
  }
};

// 生命周期
onMounted(async () => {
  updateDeviceDetection();
  getUserInfo();
  window.addEventListener('resize', updateDeviceDetection);
  
  // 获取初始数据
  await Promise.all([
    fetchTopics(),
    fetchPopularTags()
  ]);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
/* 基础样式 */
.topic-wall-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: grid;
  grid-template-columns: 250px 1fr;
  grid-template-areas: "sidebar main";
}

.topic-wall-container.mobile {
  grid-template-columns: 1fr;
  grid-template-areas: "main";
  padding-bottom: 80px;
}

/* 侧边栏样式 */
.sidebar {
  grid-area: sidebar;
  background: white;
  padding: 20px;
  border-right: 1px solid #e1e5f2;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  width: 250px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
  z-index: 100;
}

.logo h1 {
  color: #4A90E2;
  margin: 0 0 25px 0;
  font-size: 1.4rem;
  font-weight: 600;
  text-align: center;
}

.user-info {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 20px;
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

/* 主内容区域 */
.main-content {
  grid-area: main;
  padding: 0;
  margin-left: 250px;
  overflow-y: auto;
}

.topic-wall-container.mobile .main-content {
  margin-left: 0;
}

.page-header {
  background: linear-gradient(135deg, #4A90E2 0%, #667eea 100%);
  padding: 30px 40px;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  margin: 0 0 20px 0;
}

.search-section {
  display: flex;
  gap: 15px;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-box {
  flex: 1;
  display: flex;
  background: white;
  border-radius: 50px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.search-input {
  flex: 1;
  padding: 15px 20px;
  border: none;
  outline: none;
  font-size: 1rem;
}

.search-btn {
  padding: 15px 25px;
  background: #4A90E2;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #357abd;
}

.publish-btn {
  background: white;
  color: #4A90E2;
  border: none;
  padding: 15px 25px;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
}

.refresh-btn {
  background: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  padding: 15px 25px;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn:hover:not(:disabled) {
  background: #4A90E2;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.3);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 内容区域 */
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 40px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 30px;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.topic-wall-container.mobile .content-wrapper {
  grid-template-columns: 1fr;
  padding: 20px 15px;
  height: calc(100vh - 180px);
}

.left-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
  transition: transform 0.3s ease;
}

.popular-tags {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.popular-tags h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.3rem;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  padding: 8px 16px;
  background: #e3f2fd;
  color: #4A90E2;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  background: #bbdefb;
  transform: translateY(-2px);
}

.tag-item.active {
  background: #4A90E2;
  color: white;
}

.topics-section {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.4rem;
}

.sort-options {
  display: flex;
  gap: 10px;
}

.sort-btn {
  padding: 8px 15px;
  background: #f5f7fa;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.sort-btn:hover {
  background: #e3f2fd;
}

.sort-btn.active {
  background: #4A90E2;
  color: white;
}

.loading-state {
  text-align: center;
  padding: 50px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 1.3rem;
}

.empty-state p {
  color: #999;
  margin-bottom: 25px;
}

.primary-btn {
  background: #4A90E2;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.primary-btn:hover {
  background: #357abd;
  transform: translateY(-2px);
}

/* 话题卡片 */
.topic-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  transition: all 0.3s;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.12);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e3f2fd;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.post-time {
  color: #999;
  font-size: 0.9rem;
}

.post-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  background: #f5f7fa;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #e3f2fd;
}

.action-btn.liked {
  background: #ffebee;
  color: #f44336;
}

.post-content p {
  margin: 0 0 20px 0;
  line-height: 1.6;
  color: #555;
  font-size: 1.05rem;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.post-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.post-image:hover {
  transform: scale(1.05);
}

.more-images {
  background: rgba(0,0,0,0.7);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.more-images:hover {
  background: rgba(0,0,0,0.9);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-tag {
  padding: 6px 12px;
  background: #e8f5e8;
  color: #4caf50;
  border-radius: 15px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.post-tag:hover {
  background: #c8e6c9;
  transform: translateY(-2px);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
}

.page-btn {
  padding: 10px 20px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #357abd;
  transform: translateY(-2px);
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  color: #666;
  font-weight: 500;
}

/* 右侧侧边栏 */
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.publish-card, .hot-users, .stats-card {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.publish-card h3, .hot-users h3, .stats-card h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.2rem;
}

.quick-post-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e1e5f2;
  border-radius: 12px;
  resize: none;
  font-family: inherit;
  margin-bottom: 15px;
  transition: border-color 0.3s;
}

.quick-post-textarea:focus {
  outline: none;
  border-color: #4A90E2;
}

.quick-post-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e1e5f2;
  background: white;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s;
}

.icon-btn:hover {
  border-color: #4A90E2;
  transform: scale(1.1);
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 12px;
  transition: background 0.3s;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  padding: 0;
  border: none;
  background: transparent;
  margin: 0;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 3px;
}

.user-posts {
  color: #999;
  font-size: 0.85rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 12px;
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #4A90E2;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 0.85rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.publish-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #999;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 25px;
}

.post-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e1e5f2;
  border-radius: 12px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 20px;
  transition: border-color 0.3s;
}

.post-textarea:focus {
  outline: none;
  border-color: #4A90E2;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.preview-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.add-image-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  background: #f9f9f9;
  cursor: pointer;
  font-size: 2rem;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.add-image-btn:hover {
  border-color: #4A90E2;
  color: #4A90E2;
  background: #f0f7ff;
}

.tag-input input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: border-color 0.3s;
}

.tag-input input:focus {
  outline: none;
  border-color: #4A90E2;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.selected-tag {
  padding: 6px 12px;
  background: #e3f2fd;
  color: #4A90E2;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
}

.remove-tag {
  background: none;
  border: none;
  color: #f44336;
  cursor: pointer;
  font-weight: bold;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.remove-tag:hover {
  background: #ffcdd2;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 25px;
  border-top: 1px solid #eee;
}

.cancel-btn, .submit-btn {
  padding: 12px 25px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.submit-btn {
  background: #4A90E2;
  color: white;
  border: none;
}

.submit-btn:hover {
  background: #357abd;
  transform: translateY(-2px);
}

/* 图片预览模态框 */
.image-preview-modal {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.preview-large-image {
  max-width: 100%;
  max-height: 90vh;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
}

/* 移动端底部导航 */
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
  transition: all 0.3s;
}

.nav-item:hover {
  background: #f0f7ff;
}

.nav-item.active {
  background: #e3f2fd;
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
}

.nav-item.active .nav-icon {
  background: #4A90E2;
  color: white;
}

.nav-text {
  font-size: 0.7rem;
  color: #666;
}

.nav-item.active .nav-text {
  color: #4A90E2;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .topic-wall-container {
    padding-bottom: 80px;
  }
  
  .sidebar {
    display: none;
  }
  
  .page-header {
    padding: 20px 15px;
  }
  
  .page-title {
    font-size: 1.5rem;
    text-align: center;
  }
  
  .search-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: center;
  }
  
  .content-wrapper {
    padding: 15px;
    gap: 20px;
    height: calc(100vh - 160px);
  }
  
  .popular-tags, .topics-section {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .sort-options {
    align-self: flex-end;
  }
  
  .topic-card {
    padding: 20px;
  }
  
  .post-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .post-actions {
    align-self: flex-end;
  }
  
  .publish-card, .hot-users, .stats-card {
    padding: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .modal-overlay {
    padding: 10px;
  }
  
  .publish-modal {
    max-height: 95vh;
  }
  
  .modal-header, .modal-body, .modal-footer {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 15px;
  }
  
  .content-wrapper {
    padding: 10px;
  }
  
  .topic-card {
    padding: 15px;
  }
  
  .post-images {
    grid-template-columns: repeat(2, 1fr);
  }
  
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
</style>