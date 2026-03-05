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
            <!-- 搜索框和刷新按钮在同一行 -->
            <div class="search-row">
              <div class="search-box-wrapper">
                <div class="search-box">
                  <input
                    v-model="searchQuery"
                    type="text"
                    placeholder="搜索话题、标签或用户..."
                    @keyup.enter="handleSearch"
                    @focus="handleSearchFocus"
                    @blur="handleSearchBlur"
                    class="search-input"
                    ref="searchInputRef"
                  >
                  <button @click="handleSearch" class="search-btn">🔍</button>
                </div>
                
                <!-- 搜索标签选择器（点击搜索框时显示） -->
                <div v-if="showSearchTagSelector" class="search-tag-selector">
                  <div class="tag-selector-header">
                    <span>选择标签进行搜索</span>
                    <button @click="closeSearchTagSelector" class="close-selector-btn">✕</button>
                  </div>
                  <div class="search-tags-grid">
                    <div
                      v-for="tag in allTagsForSearch"
                      :key="tag.code"
                      @click="selectTagForSearch(tag)"
                      class="search-tag-item"
                      :class="{ active: selectedSearchTags.includes(tag.code) }"
                    >
                      <span class="tag-icon">{{ tag.icon || '🏷️' }}</span>
                      <span class="tag-name">{{ tag.name }}</span>
                      <span v-if="selectedSearchTags.includes(tag.code)" class="check-mark">✓</span>
                    </div>
                  </div>
                  <div class="search-tag-actions">
                    <button @click="clearSearchTags" class="clear-tags-btn">清空</button>
                    <button @click="applySearchTags" class="apply-tags-btn">应用筛选</button>
                  </div>
                </div>
              </div>
              
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
          <!-- 标签选择器 -->
          <section class="tag-selector-section">
            <!-- 二级标签 -->
            <div class="tag-level level-2">
              <div class="tags-container multi-select">
                <span
                  v-for="tag in displayLevel2Tags"
                  :key="tag.code"
                  @click="toggleLevel2Tag(tag)"
                  class="tag-item chip"
                  :class="{ active: selectedLevel2.includes(tag.code) }"
                  :style="isSelectedLevel2(tag) ? { backgroundColor: tag.color + '20', borderColor: tag.color } : {}"
                >
                  {{ tag.name }}
                </span>
              </div>
            </div>
            
            <!-- 三级标签 -->
            <div class="tag-level level-3">
              <div class="tags-container multi-select">
                <span
                  v-for="tag in displayLevel3Tags"
                  :key="tag.code"
                  @click="toggleLevel3Tag(tag)"
                  class="tag-item chip location"
                  :class="{ active: selectedLevel3.includes(tag.code) }"
                >
                  📍 {{ tag.name }}
                </span>
              </div>
            </div>
            
            <!-- 清除筛选 -->
            <div class="clear-filters" v-if="hasActiveFilters">
              <button @click="clearAllFilters" class="clear-btn">
                ✕ 清除所有筛选
              </button>
            </div>
          </section>

          <!-- 话题列表 -->
          <section class="topics-section">
            <!-- 发布按钮区域 -->
            <div class="publish-action-area">
              <button @click="showPublishModal = true" class="main-publish-btn">
                📝 发布新话题
              </button>
            </div>
            
            <div class="section-header">
              <!-- 一级标签选择器 -->
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

            <!-- 话题列表 -->
            <div v-else class="topics-list">
              <div
                v-for="post in posts"
                :key="post.id"
                class="topic-card"
                @click="showTopicDetail(post)"
              >
                <div class="post-header">
                  <div class="author-info">
                    <img 
                      :src="post.author.avatar || defaultAvatar" 
                      :alt="post.author.name" 
                      class="author-avatar"
                      @click.stop="viewUserProfile(post.author.id)"
                      @mouseenter="handleAvatarHover($event, post.author.id)"
                      @mouseleave="hidePopover"
                    >
                    <div class="author-details">
                      <span class="author-name" @click.stop="viewUserProfile(post.author.id)" @mouseenter="handleAvatarHover($event, post.author.id)" @mouseleave="hidePopover">{{ post.author.name }}</span>
                      <div class="author-meta">
                        <span v-if="post.author.identityTag" class="identity-tag" :class="post.author.identityTag">
                          {{ getIdentityTagName(post.author.identityTag) }}
                        </span>
                        <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                      </div>
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
                  <div class="topic-tags-container">
                    <!-- 一级标签（用户类型）- 始终显示 -->
                    <span v-if="getPostLevel1Tag(post)" class="topic-tag level1-tag">
                      {{ getIdentityTagName(getPostLevel1Tag(post)) }}
                    </span>
                    
                    <!-- 二级标签（最多 3 个） -->
                    <span
                      v-for="tag in (post.level2Tags || []).slice(0, 3)"
                      :key="'l2-' + (tag.code || tag.name || '')"
                      class="topic-tag level2-tag"
                      :style="{ backgroundColor: tag.color + '20', borderColor: tag.color }"
                    >
                      {{ tag.name || tag }}
                    </span>
                    
                    <!-- 三级标签（最多 3 个） -->
                    <span
                      v-for="tag in (post.level3Tags || []).slice(0, 3)"
                      :key="'l3-' + (tag.code || tag.name || '')"
                      class="topic-tag level3-tag"
                    >
                      📍{{ tag.name || tag }}
                    </span>
                    
                    <!-- 四级标签（自定义标签，最多 3 个） -->
                    <span
                      v-for="tag in (post.level4Tags || []).slice(0, 3)"
                      :key="'l4-' + (tag.code || tag.name || '')"
                      class="topic-tag level4-tag"
                    >
                      #{{ tag.name || tag }}
                    </span>
                    
                    <!-- 兼容后端返回的 tags 数组 -->
                    <template v-if="(!post.level2Tags || post.level2Tags.length === 0) && (!post.level3Tags || post.level3Tags.length === 0) && (!post.level4Tags || post.level4Tags.length === 0) && post.tags && post.tags.length > 0">
                      <span
                        v-for="(tag, index) in post.tags.slice(0, 3)"
                        :key="'tag-' + index"
                        class="topic-tag simple-tag"
                      >
                        #{{ tag }}
                      </span>
                    </template>
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

          <!-- 图片上传 -->
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

            <!-- 四级标签选择器 -->
            <TagSelector
              ref="tagSelectorRef"
              v-model="selectedPostTags"
              :auto-select-level1="true"
              :read-only-level1="true"
              :userId="currentUser?.id"
              @change="handleTagChange"
            />
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button @click="publishPost" class="submit-btn" :disabled="!canPublish">
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>

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
import TagSelector from '@/components/topic/TagSelector.vue';
import UserPopoverCard from '@/components/user/UserPopoverCard.vue';
import { topicAPI, tagAPI } from '@/api/topic';
import { isMobile as isMobileDevice } from '@/utils/device';

const router = useRouter();

// 响应式数据
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

// 搜索标签选择器
const showSearchTagSelector = ref(false);
const selectedSearchTags = ref([]);
const showQuickTagSelector = ref(false);
const quickSelectedTags = ref([]);
const searchInputRef = ref(null); // 搜索框引用

// 标签选择器相关
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

// 计算属性 - 是否有激活的筛选
const hasActiveFilters = computed(() => {
  return selectedLevel1.value || selectedLevel2.value.length > 0 || selectedLevel3.value.length > 0;
});

// 计算属性 - 显示标签（后端无数据时使用默认标签）
const displayLevel1Tags = computed(() => {
  if (level1Tags.value.length > 0) {
    return level1Tags.value;
  }
  // 默认一级标签
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
  // 默认二级标签
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
  // 默认三级标签
  return [
    { code: 'library', name: '图书馆' },
    { code: 'cafeteria', name: '食堂' },
    { code: 'dormitory', name: '宿舍' },
    { code: 'playground', name: '操场' },
    { code: 'teaching_building', name: '教学楼' },
    { code: 'laboratory', name: '实验室' }
  ];
});

// 所有标签合集（用于搜索和快速选择）
const allTagsForSearch = computed(() => {
  const tags = [];
  
  // 添加二级标签
  displayLevel2Tags.value.forEach(tag => {
    tags.push({ ...tag, type: 'level2' });
  });
  
  // 添加三级标签
  displayLevel3Tags.value.forEach(tag => {
    tags.push({ ...tag, type: 'level3', icon: '📍' });
  });
  
  return tags;
});

// 发布相关
const newPostContent = ref('');
const quickPostContent = ref('');
const postImages = ref([]);
const selectedPostTags = ref({
  level1: null,
  level2: [],
  level3: [],
  level4: []
});
const tagInput = ref('');
const publishing = ref(false);
const canPublish = ref(true);
const tagSelectorRef = ref(null);

// 用户信息弹窗
const showUserModal = ref(false);
const selectedUserId = ref(null);

// 悬浮卡片相关
const showPopover = ref(false);
const popoverUserId = ref(null);
const popoverPosition = ref({ top: 0, left: 0 });
const isMobile = ref(isMobileDevice());

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

// API 调用方法
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

    console.log('📡 请求话题列表，参数:', params);
    const response = await topicAPI.getTopics(params);
    console.log('📥 响应数据:', response);

    // 后端返回的数据已经在拦截器中解包到 data 层
    if (response) {
      // 兼容两种数据结构
      const topicsData = response.data || response;
      const rawTopics = topicsData.topics || [];

      // 转换字段名以匹配前端模板
      posts.value = rawTopics.map(topic => {
        // 处理标签数据 - 后端可能在 tags 数组或分级字段中
        const tags = topic.tags || [];
        
        return ({
          id: topic.id,
          content: topic.content,
          images: topic.images || [],
          tags: tags,
          likes: topic.likesCount || 0,
          comments: topic.commentsCount || 0,
          shares: topic.viewsCount || 0,
          isLiked: false, // 默认未点赞
          isTop: topic.isTop || 0,
          isEssence: topic.isEssence || 0,
          createdAt: topic.createdAt,
          author: {
            id: topic.author.id,
            name: topic.author.realName || topic.author.username || '匿名用户',
            username: topic.author.username,
            studentId: topic.author.studentId,
            avatar: topic.author.avatarUrl || '',
            identityTag: topic.author.identity?.level1Tag || null // 保存一级标签
          },
          // 保存话题的完整标签体系 - 需要转换为对象格式
          level1Tag: topic.level1Tag || null,
          level2Tags: (topic.level2Tags || []).map(tag => 
            typeof tag === 'string' || typeof tag === 'number' 
              ? { code: tag, name: tag } 
              : tag
          ),
          level3Tags: (topic.level3Tags || []).map(tag => 
            typeof tag === 'string' || typeof tag === 'number' 
              ? { code: tag, name: tag } 
              : tag
          ),
          level4Tags: (topic.level4Tags || []).map(tag => 
            typeof tag === 'string' || typeof tag === 'number' 
              ? { code: tag, name: tag } 
              : tag
          )
        });
      });

      totalPosts.value = topicsData.total || 0;
      stats.value.totalPosts = topicsData.totalPosts || 0;
      stats.value.todayPosts = topicsData.todayPosts || 0;

      console.log('✅ 话题数据已加载:', posts.value.length, '条');
    }
  } catch (error) {
    console.error('❌ 获取话题列表失败:', error);
    ElMessage.error('获取话题失败，请稍后重试');
    // 不显示 Mock 数据，保持空列表
    posts.value = [];
    totalPosts.value = 0;
  } finally {
    loading.value = false;
  }
};

// 加载标签数据
const loadTags = async () => {
  try {
    // 并行加载所有标签
    const [level1Res, level2Res, level3Res] = await Promise.all([
      tagAPI.getLevel1Tags(),
      tagAPI.getLevel2Tags(),
      tagAPI.getLevel3Tags()
    ]);
    
    console.log('📥 原始响应数据:', {
      level1: level1Res,
      level2: level2Res,
      level3: level3Res
    });
    
    // 处理一级标签：添加 enabled 字段和 emoji 图标映射
    level1Tags.value = (Array.isArray(level1Res) ? level1Res : []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true,
      icon: getIconEmoji(tag.code) || tag.icon || '👤'
    }));
    
    // 处理二级标签：添加 enabled 字段
    level2Tags.value = (Array.isArray(level2Res) ? level2Res : []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true
    }));
    
    // 处理三级标签：添加 enabled 字段
    level3Tags.value = (Array.isArray(level3Res) ? level3Res : []).map(tag => ({
      ...tag,
      enabled: tag.isActive !== undefined ? tag.isActive : true
    }));
    
    console.log('✅ 标签数据已加载:', {
      level1: level1Tags.value,
      level2: level2Tags.value,
      level3: level3Tags.value
    });
  } catch (error) {
    console.error('❌ 加载标签失败:', error);
    // 不显示错误提示，静默失败
  }
};

// 获取标签对应的 emoji 图标
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

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await topicAPI.getStats();
    if (response.data) {
      stats.value = {
        totalPosts: response.data.totalPosts || 0,
        activeUsers: response.data.activeUsers || 0,
        todayPosts: response.data.todayPosts || 0
      };
      console.log('✅ 统计信息已加载');
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
    // 不显示 Mock 数据，保持空列表
    stats.value = {
      totalPosts: 0,
      activeUsers: 0,
      todayPosts: 0
    };
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
    // 不显示默认标签，保持空列表
    popularTags.value = [];
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

// 获取身份标签名称
const getIdentityTagName = (tagCode) => {
  const tagMap = {
    'student': '认证学生',
    'organization': '认证团体',
    'followed': '已关注',
    'society': '社会'
  };
  return tagMap[tagCode] || '';
};

// 获取帖子的一级标签（用户类型）
const getPostLevel1Tag = (post) => {
  // 优先从 author 对象中读取
  if (post.author?.level1Tag) {
    return post.author.level1Tag;
  }
  // 兼容旧的 identityTag 字段
  if (post.author?.identityTag) {
    return post.author.identityTag;
  }
  // 最后从 post 本身读取
  return post.level1Tag || null;
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

// 标签选择器方法
const selectLevel1Tag = (code) => {
  // 如果点击的是已选中的标签，则取消选择（切换效果）
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

const isSelectedLevel2 = (tag) => {
  return selectedLevel2.value.includes(tag.code);
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

const fetchTopicsWithFilters = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: currentSort.value,
      search: searchQuery.value
    };
    
    // 添加标签筛选参数
    if (selectedLevel1.value) {
      params.level1Tag = selectedLevel1.value;
    }
    if (selectedLevel2.value.length > 0) {
      params.level2Tags = selectedLevel2.value;
    }
    if (selectedLevel3.value.length > 0) {
      params.level3Tags = selectedLevel3.value;
    }
    
    console.log('📡 请求话题列表，带标签筛选:', params);
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
        createdAt: topic.createdAt,
        author: {
          id: topic.author.id,
          name: topic.author.realName || topic.author.username || '匿名用户',
          username: topic.author.username,
          studentId: topic.author.studentId,
          avatar: topic.author.avatarUrl || '',
          level1Tag: topic.author.level1Tag || topic.author.identity?.level1Tag || null // 保存一级标签
        },
        // 保存话题的完整标签体系
        level1Tag: topic.level1Tag || null,
        level2Tags: topic.level2Tags || [],
        level3Tags: topic.level3Tags || [],
        level4Tags: topic.level4Tags || []
      }));
      
      totalPosts.value = topicsData.total || 0;
      console.log('✅ 话题数据已加载:', posts.value.length, '条');
    }
  } catch (error) {
    console.error('❌ 获取话题列表失败:', error);
    ElMessage.error('获取话题失败，请稍后重试');
    // 不显示 Mock 数据，保持空列表
    posts.value = [];
  } finally {
    loading.value = false;
  }
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

// 显示话题详情
const showTopicDetail = (post) => {
  console.log('📡 打开话题详情，postId:', post?.id);
  
  if (!post || !post.id) {
    console.error('❌ 话题信息不完整:', post);
    ElMessage.error('话题信息不完整');
    return;
  }
  
  router.push(`/topic/${post.id}`);
};

const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

const previewAllImages = (images) => {
  // 显示第一张图片或弹出图片列表
  if (images && images.length > 0) {
    previewImageUrl.value = images[0];
    showImagePreview.value = true;
  }
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

// 处理标签变化
const handleTagChange = (tagsData) => {
  console.log('标签已选择:', tagsData);
  // 验证是否可以发布
  canPublish.value = !!(tagsData.level1 && tagsData.level2?.length > 0);
};

// 发布话题
const publishPost = async () => {
  // 验证内容
  if (!newPostContent.value.trim()) {
    ElMessage.warning('请输入话题内容');
    return;
  }

  // 验证标签
  if (!tagSelectorRef.value?.validate()) {
    canPublish.value = false;
    return;
  }

  publishing.value = true;
  
  try {
    // 获取选中的标签
    const selectedTags = tagSelectorRef.value?.getSelectedTags();
    
    // 构建请求数据
    const topicData = {
      content: newPostContent.value,
      images: postImages.value,
      tags: {
        level1: selectedTags.level1?.code || null,
        level2: selectedTags.level2.map(t => t.code),
        level3: selectedTags.level3.map(t => t.code),
        level4: selectedTags.level4.map(t => t.code)
      },
      anonymous: false
    };

    console.log('📤 发布话题，数据:', topicData);
    
    const response = await topicAPI.createTopic(topicData);
    
    if (response.data) {
      ElMessage.success('发布成功!');
      closeModal();
      await fetchTopics();
    }
  } catch (error) {
    console.error('❌ 发布失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error('发布失败，请稍后重试');
    }
  } finally {
    publishing.value = false;
  }
};

// 查看用户主页
const viewUserProfile = (userId) => {
  router.push(`/user/${userId}`);
};

// 处理头像悬浮（桌面端）
const handleAvatarHover = (event, userId) => {
  if (isMobile.value) return;
  
  const rect = event.currentTarget.getBoundingClientRect();
  popoverPosition.value = {
    top: rect.top + window.scrollY - 50, // 向上偏移，使卡片顶部与头像对齐
    left: rect.left + window.scrollX - 340 // 向左偏移，显示在头像左侧（卡片宽度 320px + 间距 20px）
  };
  popoverUserId.value = userId;
  showPopover.value = true;
};

// 隐藏悬浮卡片
const hidePopover = () => {
  showPopover.value = false;
};

// 处理用户操作
const handleUserAction = (action) => {
  console.log('用户操作:', action);
  switch (action) {
    case 'follow':
      ElMessage.info('关注功能开发中...');
      break;
    case 'message':
      ElMessage.info('消息功能开发中...');
      break;
    case 'view-profile':
      ElMessage.info('个人主页开发中...');
      break;
    case 'posts':
      ElMessage.info('查看 TA 的话题开发中...');
      break;
    case 'followers':
      ElMessage.info('查看粉丝列表开发中...');
      break;
    case 'following':
      ElMessage.info('查看关注列表开发中...');
      break;
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

// 打开快速标签选择器
const openQuickTagSelector = () => {
  showQuickTagSelector.value = true;
};

// 关闭快速标签选择器
const closeQuickTagSelector = () => {
  showQuickTagSelector.value = false;
};

// 切换快速标签选择
const toggleQuickTag = (tag) => {
  const index = quickSelectedTags.value.indexOf(tag.code);
  if (index > -1) {
    quickSelectedTags.value.splice(index, 1);
  } else {
    // 最多选择 5 个标签
    if (quickSelectedTags.value.length >= 5) {
      ElMessage.warning('最多选择 5 个标签');
      return;
    }
    quickSelectedTags.value.push(tag.code);
  }
};

// 清空快速标签
const clearQuickTags = () => {
  quickSelectedTags.value = [];
};

// 确认快速标签选择
const confirmQuickTags = () => {
  // 将选中的标签应用到发布内容中（暂时只显示提示）
  const selectedNames = allTagsForSearch.value
    .filter(tag => quickSelectedTags.value.includes(tag.code))
    .map(tag => tag.name)
    .join(', ');
  
  ElMessage.success(`已选择标签：${selectedNames || '无'}`);
  closeQuickTagSelector();
};

// 关闭搜索标签选择器
const closeSearchTagSelector = () => {
  showSearchTagSelector.value = false;
};

// 处理搜索框点击
const handleSearchBoxClick = () => {
  showSearchTagSelector.value = true;
};

// 处理搜索框获得焦点
const handleSearchFocus = () => {
  // 延迟显示，避免与点击事件冲突
  setTimeout(() => {
    showSearchTagSelector.value = true;
  }, 100);
};

// 处理搜索框失去焦点 - 优化：增加延迟防止误关闭
const handleSearchBlur = (event) => {
  // 延迟关闭，给点击标签留出足够时间
  setTimeout(() => {
    // 如果点击的是标签选择器内部，不关闭
    const tagSelector = document.querySelector('.search-tag-selector');
    if (!tagSelector?.contains(event.relatedTarget)) {
      showSearchTagSelector.value = false;
    }
  }, 300); // ⚠️ 延迟增加到 300ms，给点击留出足够时间
};

// 处理点击外部关闭标签选择器
const handleClickOutside = (event) => {
  const searchBox = document.querySelector('.search-box');
  const tagSelector = document.querySelector('.search-tag-selector');
  const quickTagSelector = document.querySelector('.quick-tag-selector');
  
  // 如果点击的不是搜索框、标签选择器或快速标签选择器，则关闭所有选择器
  if (searchBox && !searchBox.contains(event.target) && 
      tagSelector && !tagSelector.contains(event.target) &&
      quickTagSelector && !quickTagSelector.contains(event.target)) {
    showSearchTagSelector.value = false;
    showQuickTagSelector.value = false;
  }
};

// 选择搜索标签 - 优化：不立即关闭选择器
const selectTagForSearch = (tag) => {
  const index = selectedSearchTags.value.indexOf(tag.code);
  if (index > -1) {
    selectedSearchTags.value.splice(index, 1);
  } else {
    // 最多选择 5 个标签
    if (selectedSearchTags.value.length >= 5) {
      ElMessage.warning('最多选择 5 个标签');
      return;
    }
    selectedSearchTags.value.push(tag.code);
  }
  // ✅ 关键修改：不关闭选择器，允许继续选择
};

// 清空搜索标签
const clearSearchTags = () => {
  selectedSearchTags.value = [];
};

// 应用搜索标签 - 优化：执行真实搜索
const applySearchTags = async () => {
  if (selectedSearchTags.value.length === 0) {
    ElMessage.info('请选择至少一个标签');
    return;
  }
  
  // 获取选中的标签名称
  const selectedNames = allTagsForSearch.value
    .filter(tag => selectedSearchTags.value.includes(tag.code))
    .map(tag => tag.name)
    .join(', ');
  
  searchQuery.value = selectedNames;
  ElMessage.success(`已选择 ${selectedSearchTags.value.length} 个标签`);
  
  // ✅ 执行搜索
  await handleSearch();
  
  // ✅ 最后关闭选择器
  closeSearchTagSelector();
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
  
  // 添加全局点击事件监听，用于关闭标签选择器
  document.addEventListener('click', handleClickOutside);

  // 获取初始数据
  await Promise.all([
    fetchTopics(),
    fetchPopularTags(),
    loadTags(),
    loadStats() // 加载统计信息
  ]);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
  // 移除全局点击事件监听
  document.removeEventListener('click', handleClickOutside);
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
  flex-direction: column; /* 整体垂直布局 */
  gap: 15px;
  align-items: center;
  position: relative; /* 确保相对定位 */
  width: 100%; /* 占满宽度 */
}

/* 搜索行 - 搜索框和刷新按钮在同一行 */
.search-row {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  position: relative;
}

.search-box-wrapper {
  flex: 1; /* 搜索框占据剩余空间 */
  position: relative; /* 为标签选择器提供相对定位 */
}

.search-box {
  width: 100%; /* 占满父容器宽度 */
  display: flex;
  background: white;
  border-radius: 50px;
  overflow: visible; /* 改为 visible 让下拉选择器可以显示 */
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  position: relative; /* 确保父容器有相对定位 */
}

.action-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%; /* 占满宽度 */
  justify-content: flex-end; /* 按钮靠右对齐 */
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

/* 搜索标签选择器 */
.search-tag-selector {
  position: absolute;
  top: calc(100% + 5px); /* 紧贴搜索框，只留 5px 缝隙 */
  left: 0;
  right: 0;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  z-index: 1000; /* 提高层级 */
  animation: slideDown 0.3s ease;
  border: 1px solid #e1e5f2; /* 添加边框 */
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tag-selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e1e5f2;
}

.tag-selector-header span {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.close-selector-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #999;
  cursor: pointer;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-selector-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.search-tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); /* 减小最小宽度 */
  gap: 8px; /* 减小间距 */
  margin-bottom: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.search-tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px; /* 减小内边距 */
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px; /* 减小圆角 */
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px; /* 减小字体 */
  position: relative;
}

.search-tag-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.search-tag-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.search-tag-item .tag-icon {
  margin-right: 4px; /* 减小间距 */
  font-size: 15px; /* 稍微增大图标 */
  color: #2c3e50; /* 图标颜色加深 */
}

.search-tag-item.active .tag-icon {
  color: white; /* 选中状态为白色 */
}

.search-tag-item .tag-name {
  flex: 1;
  font-weight: 600; /* 加粗字体 */
  color: #2c3e50; /* 深色字体便于观看 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-tag-item.active .tag-name {
  color: white; /* 选中状态为白色 */
}

.search-tag-item .check-mark {
  display: none;
  font-weight: bold;
  font-size: 14px; /* 减小对勾大小 */
}

.search-tag-item.active .check-mark {
  display: inline-block;
}

.search-tag-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px; /* 减小按钮间距 */
}

.clear-tags-btn,
.apply-tags-btn {
  padding: 6px 12px; /* 减小按钮内边距 */
  border-radius: 6px;
  font-size: 12px; /* 减小按钮字体 */
  cursor: pointer;
  transition: all 0.3s;
}

.clear-tags-btn {
  background: #f5f7fa;
  color: #666;
  border: 1px solid #dcdfe6;
}

.clear-tags-btn:hover {
  background: #e4e7ed;
}

.apply-tags-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.apply-tags-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 快速发布标签选择器 */
.quick-tag-selector {
  margin-top: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  animation: slideDown 0.3s ease;
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

/* 一级标签容器 */
.level1-tags-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* 一级标签项 */
.level1-tag-item {
  padding: 8px 16px;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  
  &:hover {
    background: #ecf5ff;
    border-color: #b3d8ff;
  }
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transform: translateY(-2px);
  }
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

/* 话题标签容器 */
.topic-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
}

/* 通用标签样式 */
.topic-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
  cursor: default;
}

/* 一级标签（用户类型） */
.level1-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 二级标签（话题分类） */
.level2-tag {
  background: rgba(80, 200, 120, 0.15);
  color: #50C878;
  border: 1px solid #50C878;
}

/* 三级标签（地点） */
.level3-tag {
  background: #e0f7fa;
  color: #00acc1;
  border: 1px solid #b2ebf2;
}

/* 四级标签（自定义标签） */
.level4-tag {
  background: #fff3e0;
  color: #ff9800;
  border: 1px solid #ffe0b2;
}

/* 简单标签（兼容后端 tags 数组） */
.simple-tag {
  background: #f5f7fa;
  color: #666;
  border: 1px solid #e4e7ed;
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

/* 话题卡片点击效果 */
.topic-card {
  cursor: pointer;
  transition: all 0.3s;
}

.topic-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 作者头像和姓名点击效果 */
.author-avatar,
.author-name {
  cursor: pointer;
  transition: opacity 0.3s;
}

.author-avatar:hover,
.author-name:hover {
  opacity: 0.8;
}

/* 标签选择器区域 */
.tag-selector-section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tag-selector-section h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #333;
}

.tag-level {
  margin-bottom: 20px;
}

.tag-level:last-child {
  margin-bottom: 0;
}

.tag-title {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 12px;
  font-weight: 600;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  
  &.multi-select {
    gap: 8px;
  }
}

.tag-item {
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
  color: #606266;
  
  &:hover:not(.reset) {
    background: #ecf5ff;
    border-color: #409eff;
  }
  
  &.active {
    background: #ecf5ff;
    border-color: #409eff;
    color: #409eff;
  }
  
  &.chip {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  &.location {
    background: #f0f9ff;
    border-color: #00bcd4;
    
    &.active {
      background: #e0f7fa;
      border-color: #00acc1;
    }
  }
}

.clear-filters {
  margin-top: 16px;
  text-align: center;
}

.clear-btn {
  padding: 8px 20px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f0f0;
    border-color: #c0c4cc;
  }
}

/* 发布模态框中的标签选择器 */
.publish-modal .tag-selector-container {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.publish-modal .tag-selector-container::-webkit-scrollbar {
  width: 6px;
}

.publish-modal .tag-selector-container::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.publish-modal .tag-selector-container::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 发布按钮禁用状态 */
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>