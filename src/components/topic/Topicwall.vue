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
      <!-- 头部搜索区域 -->
      <header class="page-header">
        <UnifiedSearch
            v-model="searchQuery"
            :selected-tags-model="selectedSearchTags"
            :active-quick-filters="quickFilters"
            :available-tags="allTagsForSearch"
            :quick-filter-options="quickFilterTags"
            placeholder="搜索话题、标签或用户..."
           tag-selector-title="选择标签进行搜索"
            :show-quick-filters="true"
            :show-clear-button="true"
            :has-active-filters="hasActiveFilters"
            :enable-result-page="true"
            :default-search-type="'topic'"
            @search="handleSearch"
            @apply-tags="applySearchTags"
            @clear-tags="clearSearchTags"
            @clear-filters="clearAllFilters"
        />
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
          <!-- 标签筛选栏 -->
          <TopicFilter
            :display-level2-tags="displayLevel2Tags"
            :display-level3-tags="displayLevel3Tags"
            :selected-level2="selectedLevel2"
            :selected-level3="selectedLevel3"
            :has-active-filters="hasActiveFilters"
            @toggle-level2="toggleLevel2Tag"
            @toggle-level3="toggleLevel3Tag"
            @clear-all="clearAllFilters"
          />

          <!-- 话题列表 -->
          <section class="topics-section">
            <!-- 发布按钮 -->
            <div class="publish-action-area">
              <button @click="showPublishModal = true" class="main-publish-btn">
                📝 发布新话题
              </button>
            </div>
            
            <!-- 一级标签和排序 -->
            <div class="section-header">
              <div class="level1-tags-container">
                <span
                  @click="selectLevel1Tag(null)"
                  class="level1-tag-item"
                  :class="{ active: !selectedLevel1 }"
                >
                  全部
                </span>
                <span
                  v-for="tag in displayLevel1Tags"
                  :key="tag.code"
                  @click="selectLevel1Tag(tag.code)"
                  class="level1-tag-item"
                  :class="{ active: selectedLevel1 === tag.code }"
                >
                  {{ tag.icon }} {{ tag.name }}
                </span>
              </div>
              
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

            <!-- 话题列表 - 使用 TopicCard 组件 -->
            <div v-else class="topics-list">
              <TopicCard
                v-for="post in posts"
                :key="post.id"
                :post="post"
                @click="showTopicDetail"
                @view-user="viewUserProfile"
                @like-change="handleLikeChange"
                @comment="commentPost"
                @preview-image="previewImage"
                @preview-all-images="previewAllImages"
                @avatar-hover="handleAvatarHover"
                @hide-popover="hidePopover"
                @view-forwarded-topic="viewForwardedTopic"
              />

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
      </div>
    </main>

    <!-- 发布话题模态框 -->
    <PublishTopicModal
        v-model:visible="showPublishModal"
        :is-share-mode="shareInfo !== null"
        :share-info="shareInfo"
        :userId="currentUser?.id"
        @published="handlePublished"
        @closed="handleModalClosed"
    />

    <!-- 用户信息悬浮卡片 (桌面端) -->
    <UserPopoverCard
      v-if="showPopover && !isMobile"
      :userId="popoverUserId"
      :position="popoverPosition"
    />

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
import UserPopoverCard from '@/components/user/UserPopoverCard.vue';
import PublishTopicModal from '@/components/topic/PublishTopicModal.vue';
import TopicCard from '@/components/topic/TopicCard.vue';
import TopicFilter from '@/components/topic/TopicFilter.vue';
import { topicAPI, tagAPI } from '@/api/topic';
import { isMobile as isMobileDevice } from '@/utils/device';
import UnifiedSearch from '@/components/common/UnifiedSearch.vue';

const router = useRouter();

// ========== 响应式数据 ==========
const loading = ref(false);
const showPublishModal = ref(false);
const showImagePreview = ref(false);
const previewImageUrl = ref('');

// 分享相关
const shareInfo = ref(null);

// 刷新相关数据
const contentWrapper = ref(null);
const topicsContainer = ref(null);
const touchStartY = ref(0);
const isRefreshing = ref(false);
const pullDistance = ref(0);
const maxPullDistance = ref(80);

// 搜索相关
const searchQuery = ref('');
const selectedSearchTags = ref([]);
const quickFilters = ref([]);

// 用户信息
const currentUser = ref(null);

// 标签筛选
const currentSort = ref('latest');
const level1Tags = ref([]);
const level2Tags = ref([]);
const level3Tags = ref([]);
const selectedLevel1 = ref(null);
const selectedLevel2 = ref([]);
const selectedLevel3 = ref([]);

const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hot' },
  { label: '精华', value: 'essence' }
];

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const totalPosts = ref(0);

// 话题数据
const posts = ref([]);

// 悬浮卡片
const showPopover = ref(false);
const popoverUserId = ref(null);
const popoverPosition = ref({ top: 0, left: 0 });
const isMobile = ref(isMobileDevice());

// ========== 计算属性 ==========
const hasActiveFilters = computed(() => {
 return selectedLevel1.value || selectedLevel2.value.length > 0 || selectedLevel3.value.length > 0;
});

const displayLevel1Tags = computed(() => {
  if (level1Tags.value.length > 0) {
    return level1Tags.value;
  }
 return [
    { code: 'student', name: '学生', icon: '👨‍🎓', enabled: true },
    { code: 'organization', name: '团体', icon: '👥', enabled: true },
    { code: 'followed', name: '关注', icon: '⭐', enabled: true },
    { code: 'society', name: '社会', icon: '🌍', enabled: true }
  ];
});

const displayLevel2Tags = computed(() => {
  if (level2Tags.value.length > 0) {
    return level2Tags.value.slice(0, 8);
  }
 return [
    { code: 'study_experience', name: '学习经验', color: '#50C878' },
    { code: 'tech_exchange', name: '技术交流', color: '#4169E1' },
    { code: 'campus_life', name: '校园生活', color: '#FF6B6B' },
    { code: 'food_recommend', name: '美食推荐', color: '#FFA07A' },
    { code: 'activity', name: '社团活动', color: '#DDA0DD' },
    { code: 'employment', name: '就业信息', color: '#98FB98' },
    { code: 'help', name: '求助帖', color: '#F0E68C' },
    { code: 'confession', name: '表白墙', color: '#FFB6C1' }
  ];
});

const displayLevel3Tags = computed(() => {
  if (level3Tags.value.length > 0) {
    return level3Tags.value.slice(0, 6);
  }
 return [
    { code: 'library', name: '图书馆' },
    { code: 'cafeteria', name: '食堂' },
    { code: 'dormitory', name: '宿舍' },
    { code: 'playground', name: '操场' },
    { code: 'teaching_building', name: '教学楼' },
    { code: 'laboratory', name: '实验室' }
  ];
});

const allTagsForSearch = computed(() => {
  const tags = [];
  displayLevel2Tags.value.forEach(tag => {
   tags.push({ ...tag, type: 'level2' });
  });
  displayLevel3Tags.value.forEach(tag => {
   tags.push({ ...tag, type: 'level3', icon: '📍' });
  });
 return tags;
});

const totalPages = computed(() => Math.ceil(totalPosts.value / pageSize.value));

// ========== 方法 - 数据获取 ==========
const fetchTopicsWithFilters = async () => {
  loading.value = true;
  try {
   const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: currentSort.value,
      search: searchQuery.value
    };
    
   if (selectedLevel1.value) {
      params.level1Tag = selectedLevel1.value;
    }
   if (selectedLevel2.value.length > 0) {
      params.level2Tags = selectedLevel2.value;
    }
   if (selectedLevel3.value.length > 0) {
      params.level3Tags = selectedLevel3.value;
    }
    
   const response = await topicAPI.getTopics(params);
    
   if (response) {
     const topicsData = response.data || response;
     const rawTopics = topicsData.topics || [];
      
      posts.value = rawTopics.map(topic => ({
        id: topic.id,
       content: topic.content,
        images: topic.images || [],
       tags: topic.tags || [],
        likes: topic.likesCount || 0,
       comments: topic.commentsCount || 0,
        shares: topic.viewsCount || 0,
        isLiked: false,
        isTop: topic.isTop || 0,
        isEssence: topic.isEssence || 0,
        isForwarded: topic.isForwarded || false, // 添加转发标识
        forwardedFromTopicId: topic.forwardedFromTopicId || null, // 添加被转发的话题 ID
        createdAt: topic.createdAt,
        author: {
          id: topic.author?.id || null,
          name: topic.author?.username || topic.author?.realName || '匿名用户',
          username: topic.author?.username || '',
          studentId: topic.author?.studentId || '',
          avatar: topic.author?.avatarUrl || '',
          level1Tag: topic.author?.level1Tag || topic.author?.identity?.level1Tag || null
        },
        level1Tag: topic.level1Tag || null,
        level2Tags: topic.level2Tags || [],
        level3Tags: topic.level3Tags || [],
        level4Tags: topic.level4Tags || []
      }));
      
      // 调试：打印转发相关字段
      if (posts.value.length > 0) {
        // 检查是否有转发话题
        const forwardedTopics = posts.value.filter(p => p.isForwarded);
      }
      
      totalPosts.value = topicsData.total || 0;
    }
  } catch (error) {
   ElMessage.error('获取话题失败，请稍后重试');
    posts.value = [];
  } finally {
    loading.value = false;
  }
};

const loadTags = async () => {
  try {
   const [level1Res, level2Res, level3Res] = await Promise.all([
     tagAPI.getLevel1Tags(),
     tagAPI.getLevel2Tags(),
     tagAPI.getLevel3Tags()
    ]);
    
    level1Tags.value = (Array.isArray(level1Res) ? level1Res: []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true,
      icon: getIconEmoji(tag.code) || tag.icon || '👤'
    }));
    
    level2Tags.value = (Array.isArray(level2Res) ? level2Res: []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true
    }));
    
    level3Tags.value = (Array.isArray(level3Res) ? level3Res: []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true
    }));
  } catch (error) {
    // 静默失败
  }
};

const getIconEmoji = (code) => {
  const iconMap = {
    'student': '👨‍🎓',
    'merchant': '🏪',
    'organization': '👥',
    'individual': '👤',
    'followed': '⭐'
  };
 return iconMap[code] || null;
};

// ========== 方法 - 事件处理 ==========
const handleSearch = async () => {
  if (searchQuery.value.trim()) {
    currentPage.value = 1;
    await fetchTopicsWithFilters();
  }
};

const selectLevel1Tag = (code) => {
  selectedLevel1.value = selectedLevel1.value === code ? null : code;
  currentPage.value = 1;
  fetchTopicsWithFilters();
};

const toggleLevel2Tag = (tag) => {
  const index = selectedLevel2.value.indexOf(tag.code);
  if (index > -1) {
    selectedLevel2.value.splice(index, 1);
  } else {
    selectedLevel2.value.push(tag.code);
  }
  currentPage.value = 1;
  fetchTopicsWithFilters();
};

const toggleLevel3Tag = (tag) => {
  const index = selectedLevel3.value.indexOf(tag.code);
  if (index > -1) {
    selectedLevel3.value.splice(index, 1);
  } else {
    selectedLevel3.value.push(tag.code);
  }
  currentPage.value = 1;
  fetchTopicsWithFilters();
};

const clearAllFilters = () => {
  selectedLevel1.value = null;
  selectedLevel2.value = [];
  selectedLevel3.value = [];
  currentPage.value = 1;
  fetchTopicsWithFilters();
};

const changeSort = async (sortType) => {
  currentSort.value = sortType;
  currentPage.value = 1;
  await fetchTopicsWithFilters();
};

const changePage = async (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    await fetchTopicsWithFilters();
  }
};

const showTopicDetail = (post) => {
  if (!post || !post.id) {
   ElMessage.error('话题信息不完整');
    return;
  }
  router.push(`/topic/${post.id}`);
};

const commentPost = (postId) => {
  router.push(`/topic/${postId}`);
};

const viewForwardedTopic = async (topicId) => {
  try {
    // 先尝试获取话题详情
    const response = await topicAPI.getTopicDetail(topicId);
    
    if (response && response.id) {
      // 话题存在，跳转到详情页
      router.push(`/topic/${topicId}`);
    } else {
      throw new Error('话题不存在');
    }
  } catch (error) {
    console.error('查看转发话题失败:', error);
    // 话题不存在或已删除，显示提示框
    ElMessageBox.alert(
      '该话题已被删除或不存在',
      '话题不存在',
      {
        confirmButtonText: '确定',
        type: 'warning'
      }
    );
  }
};

const viewUserProfile = (userId) => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}');
  const currentUserId = userData.id;
  
  if (userId === currentUserId) {
    router.push('/personalcenter');
  } else {
    router.push(`/user/profile/${userId}`);
  }
};

const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

const previewAllImages = (images) => {
  if (images && images.length > 0) {
    previewImageUrl.value = images[0];
    showImagePreview.value = true;
  }
};

const closeImagePreview = () => {
  showImagePreview.value = false;
  previewImageUrl.value = '';
};

const handleLikeChange = (data) => {
  // TopicCard 已处理点赞逻辑，这里可以留空或用于同步其他状态
};

const handlePublished = async () => {
  await fetchTopicsWithFilters();
};

const handleModalClosed = () => {
  // 模态框关闭后的清理
};

// ========== 用户悬浮卡片 ==========
const handleAvatarHover= (event, userId) => {
  if (isMobile.value) return;
  
  const rect = event.currentTarget.getBoundingClientRect();
  popoverPosition.value = {
    top: rect.top + window.scrollY - 50,
    left: rect.left + window.scrollX - 340
  };
  popoverUserId.value = userId;
  showPopover.value = true;
};

const hidePopover = () => {
  showPopover.value = false;
};

// ========== 设备检测 ==========
const updateDeviceDetection= () => {
  isMobile.value = window.innerWidth < 768;
};

const getUserInfo = () => {
  const token = localStorage.getItem('token');
  if (token) {
   const user = JSON.parse(localStorage.getItem('user') || '{}');
    currentUser.value = {
      id: user.id,
      name: user.name || user.username || '',
      studentId: user.studentId || user.student_id || '',
      avatar: user.avatar || ''
    };
  } else {
    currentUser.value = null;
  }
};

// ========== 下拉刷新（移动端）==========
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

   if (deltaY > 0 && contentWrapper.value.scrollTop === 0) {
      e.preventDefault();
      pullDistance.value = Math.min(deltaY * 0.5, maxPullDistance.value);

     if (topicsContainer.value) {
        topicsContainer.value.style.transform = `translateY(${pullDistance.value}px)`;
        topicsContainer.value.style.transition = 'none';
      }
    }
  }
};

const handleTouchEnd = async () => {
  if (isMobile.value && pullDistance.value > 0) {
   if (pullDistance.value >= maxPullDistance.value* 0.6) {
      await refreshAllData();
    }

   if (topicsContainer.value) {
      topicsContainer.value.style.transform = 'translateY(0)';
      topicsContainer.value.style.transition= 'transform 0.3s ease';
    }

    touchStartY.value = 0;
    pullDistance.value = 0;
  }
};

const refreshAllData = async () => {
  if (loading.value) return;

  loading.value = true;
  try {
    currentPage.value = 1;
    searchQuery.value = '';
    currentSort.value = 'latest';
    await fetchTopicsWithFilters();
   ElMessage.success('刷新成功');
  } catch (error) {
   ElMessage.error('刷新失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleScroll = () => {
  if (isMobile.value && !loading.value) {
   const wrapper= contentWrapper.value;
   if (wrapper) {
     const { scrollTop, scrollHeight, clientHeight } = wrapper;
     if (scrollTop + clientHeight >= scrollHeight -100) {
        loadMoreTopics();
      }
    }
  }
};

const loadMoreTopics = async () => {
  if (currentPage.value < totalPages.value && !loading.value) {
    loading.value = true;
    try {
      currentPage.value += 1;
      await fetchTopicsWithFilters();
    } catch (error) {
     ElMessage.error('加载更多失败');
      currentPage.value -= 1;
    } finally {
      loading.value = false;
    }
  }
};

// 处理分享参数
const handleShareFromQuery = async () => {
  const routeParams = router.currentRoute.value.query;
  if (routeParams.from === 'share' && routeParams.sourceType === 'topic' && routeParams.sourceId) {
    // 从 sessionStorage 获取分享数据
    const shareData = sessionStorage.getItem('shareData');
    if (shareData) {
      try {
        const parsedData = JSON.parse(shareData);
        shareInfo.value = parsedData;
        
        // 打开分享弹窗
        setTimeout(() => {
          showPublishModal.value = true;
          // 清理 URL 参数
          router.replace({ query: {} });
        }, 500);
      } catch (error) {
        console.error('解析分享数据失败:', error);
        // 不显示错误提示，静默处理
      }
    } else {
      // 如果没有分享数据，也静默处理，不显示错误
      console.log('未找到分享数据，可能已被清除');
    }
    // 清理 URL 参数，避免刷新时重复处理
    router.replace({ query: {} });
  }
};

// ========== 生命周期 ==========
onMounted(async () => {
  updateDeviceDetection();
  getUserInfo();
  window.addEventListener('resize', updateDeviceDetection);
  
  await Promise.all([
    fetchTopicsWithFilters(),
    loadTags()
  ]);
  
  // 检查是否从分享跳转而来
  handleShareFromQuery();
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

/* 发布按钮区域 */
.publish-action-area {
  margin-bottom: 25px;
  display: flex;
  justify-content: center;
}

.main-publish-btn {
  padding: 14px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.main-publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Section Header */
.section-header {
  margin-bottom: 20px;
}

.level1-tags-container {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.level1-tag-item {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
  font-size: 14px;
  color: #2c3e50;
}

.level1-tag-item:hover:not(.active) {
  background: #ecf5ff;
  border-color: #409eff;
  color: #2c3e50;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.level1-tag-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.sort-options {
  display: flex;
  gap: 10px;
}

.sort-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #606266;
}

.sort-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.sort-btn.active {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 10px 0;
  color: #606266;
}

.empty-state p {
  color: #909399;
  margin-bottom: 20px;
}

.primary-btn {
  padding: 12px 30px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.primary-btn:hover {
  background: #66b1ff;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #606266;
}

.page-btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #606266;
  font-size: 14px;
}

/* 图片预览 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.image-preview-modal {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.preview-large-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .page-header {
    padding: 20px 15px;
  }

  .level1-tags-container {
    gap: 8px;
  }

  .level1-tag-item {
    padding: 6px 12px;
    font-size: 13px;
  }

  .sort-options {
    gap: 8px;
  }

  .sort-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>
