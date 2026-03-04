<template>
  <div class="topic-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>话题不存在或已被删除</h2>
      <p>{{ error }}</p>
      <button @click="goBack" class="back-btn">返回话题墙</button>
    </div>

    <!-- 话题详情内容 -->
    <div v-else-if="topic" class="topic-content">
      <!-- 返回按钮 (紧凑样式) -->
      <button @click="goBack" class="back-to-list">← 返回</button>

      <!-- 主体内容 -->
      <article class="topic-article">
        <!-- 作者信息栏 -->
        <header class="author-section">
          <div 
            class="author-info" 
            @click="showAuthorInfo"
            @mouseenter="handleAvatarHover($event, topic.author.id)"
            @mouseleave="hidePopover"
          >
            <img 
              :src="topic.author.avatarUrl || defaultAvatar" 
              :alt="topic.author.username"
              class="author-avatar"
            >
            <div class="author-details">
              <h3 class="author-name">{{ topic.author.realName || topic.author.username }}</h3>
              <p class="author-meta">
                <span class="identity-tag">{{ getIdentityName(topic.author.identity) }}</span>
                <span class="post-time">{{ formatTime(topic.createdAt) }}</span>
              </p>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="topic-actions">
            <button 
              v-if="canEdit" 
              @click="editTopic"
              class="action-btn edit"
            >
              ✏️ 编辑
            </button>
            <button 
              v-if="canDelete" 
              @click="deleteTopic"
              class="action-btn delete"
            >
              🗑️ 删除
            </button>
          </div>
        </header>

        <!-- 用户信息悬浮卡片 (桌面端) -->
        <UserPopoverCard
          v-if="showPopover && !isMobile"
          :userId="popoverUserId"
          :position="popoverPosition"
        />

        <!-- 话题内容 -->
        <div class="topic-body">
          <!-- 标签区域 -->
          <div v-if="topic.tags && (topic.tags.level2 || topic.tags.level3 || topic.tags.level4)" class="tags-section">
            <!-- 二级标签 -->
            <div v-for="tag in topic.tags.level2" :key="tag.code" class="tag-badge level2" :style="{ backgroundColor: tag.color }">
              {{ tag.name }}
            </div>
            
            <!-- 三级标签 -->
            <div v-for="tag in topic.tags.level3" :key="tag.code" class="tag-badge level3">
              📍 {{ tag.name }}
            </div>
            
            <!-- 四级标签 -->
            <div v-for="tag in topic.tags.level4" :key="tag.id" class="tag-badge level4">
              #{{ tag.name }}
            </div>
          </div>

          <!-- 正文内容 -->
          <div class="content-text" v-html="formatContent(topic.content)"></div>

          <!-- 图片展示 -->
          <div v-if="topic.images && topic.images.length > 0" class="images-gallery">
            <div 
              v-for="(image, index) in topic.images" 
              :key="index"
              class="image-item"
              @click="previewImage(image)"
            >
              <img :src="image" :alt="`图片${index + 1}`">
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="statistics-bar">
            <div class="stat-item">
              <span class="stat-icon">👍</span>
              <span class="stat-value">{{ topic.statistics.likesCount || 0 }}</span>
              <span class="stat-label">点赞</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">💬</span>
              <span class="stat-value">{{ topic.statistics.commentsCount || 0 }}</span>
              <span class="stat-label">评论</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">👁️</span>
              <span class="stat-value">{{ topic.statistics.viewsCount || 0 }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">⭐</span>
              <span class="stat-value">{{ topic.statistics.collectionsCount || 0 }}</span>
              <span class="stat-label">收藏</span>
            </div>
          </div>

          <!-- 互动按钮 -->
          <div class="interaction-buttons">
            <button 
              @click="toggleLike"
              class="interact-btn like"
              :class="{ active: isLiked }"
            >
              {{ isLiked ? '👍 已赞' : '👍 点赞' }}
            </button>
            <button @click="scrollToComments" class="interact-btn comment">
              💬 评论
            </button>
            <button @click="shareTopic" class="interact-btn share">
              ↗️ 分享
            </button>
            <button @click="toggleCollection" class="interact-btn collect" :class="{ active: isCollected }">
              {{ isCollected ? '⭐ 已收藏' : '⭐ 收藏' }}
            </button>
          </div>
        </div>

        <!-- 评论区 -->
        <section ref="commentsSection" class="comments-section">
          <h3 class="section-title">
            评论 ({{ comments.length }})
            <span v-if="hasHotComments" class="hot-badge">🔥 有热评</span>
          </h3>

          <!-- 发布评论 -->
          <div v-if="isLoggedIn" class="comment-input-box">
            <textarea
              v-model="newComment"
              placeholder="写下你的评论..."
              rows="3"
              class="comment-textarea"
            ></textarea>
            <div class="comment-actions">
              <button @click="submitComment" class="submit-btn" :disabled="!newComment.trim()">
                发表评论
              </button>
            </div>
          </div>

          <div v-else class="login-hint">
            <p>登录后才能发表评论哦~</p>
            <button @click="goToLogin" class="login-btn">去登录</button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <!-- 热评 -->
            <div v-if="hotComments.length > 0" class="hot-comments">
              <h4 class="subsection-title">🔥 热评</h4>
              <div
                v-for="comment in hotComments"
                :key="comment.id"
                class="comment-item hot"
              >
                <CommentItem 
                  :comment="comment" 
                  @user-click="showUserInfo"
                  @avatar-hover="handleAvatarHover"
                  @avatar-leave="hidePopover"
                />
              </div>
            </div>

            <!-- 全部评论 -->
            <div class="all-comments">
              <h4 class="subsection-title">全部评论</h4>
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
              >
                <CommentItem 
                  :comment="comment"
                  @user-click="showUserInfo"
                  @avatar-hover="handleAvatarHover"
                  @avatar-leave="hidePopover"
                />
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="commentPagination.totalPages > 1" class="comment-pagination">
              <button
                @click="loadComments(commentPagination.currentPage - 1)"
                :disabled="commentPagination.currentPage === 1"
                class="page-btn"
              >
                上一页
              </button>
              <span class="page-info">
                第 {{ commentPagination.currentPage }} 页，共 {{ commentPagination.totalPages }} 页
              </span>
              <button
                @click="loadComments(commentPagination.currentPage + 1)"
                :disabled="commentPagination.currentPage === commentPagination.totalPages"
                class="page-btn"
              >
                下一页
              </button>
            </div>
          </div>
        </section>
      </article>
    </div>

    <!-- 图片预览弹窗 -->
    <div v-if="showPreview" class="image-preview-overlay" @click="closePreview">
      <button class="close-btn">×</button>
      <img :src="previewImageUrl" alt="预览" class="preview-image">
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { topicAPI } from '@/api/topic';
import CommentItem from './CommentItem.vue';
import UserPopoverCard from '@/components/user/UserPopoverCard.vue';
import { isMobile as isMobileDevice } from '@/utils/device';

const route = useRoute();
const router = useRouter();

// 数据
const loading = ref(true);
const error = ref(null);
const topic = ref(null);
const comments = ref([]);
const hotComments = ref([]);
const relatedTopics = ref([]);
const isLiked = ref(false);
const isCollected = ref(false);
const newComment = ref('');
const showUserModal = ref(false);
const selectedUserId = ref(null);
const showPreview = ref(false);
const previewImageUrl = ref('');
const currentUserId = ref(null);

// 悬浮卡片相关
const showPopover = ref(false);
const popoverUserId = ref(null);
const popoverPosition = ref({ top: 0, left: 0 });
const isMobile = ref(isMobileDevice());

const defaultAvatar = 'https://placehold.co/200x200/4A90E2/FFFFFF?text=U';
const HOT_THRESHOLD = 10; // 热评阈值

// 计算属性
const isLoggedIn = computed(() => !!localStorage.getItem('token'));
const canEdit = computed(() => {
  return isLoggedIn.value && topic.value?.author.id === currentUserId.value;
});
const canDelete = computed(() => {
  return isLoggedIn.value && topic.value?.author.id === currentUserId.value;
});
const hasHotComments = computed(() => hotComments.value.length > 0);
const commentPagination = ref({
  currentPage: 1,
  totalPages: 1,
  total: 0
});

// 生命周期
onMounted(async () => {
  currentUserId.value = getCurrentUserId();
  await loadTopicDetail();
});

// 获取当前用户 ID
const getCurrentUserId = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.id || null;
};

// 加载话题详情
const loadTopicDetail = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await topicAPI.getTopicDetail(route.params.id);
    
    if (response.data) {
      topic.value = response.data;
      
      // 检查是否已点赞
      // isLiked.value = checkIfLiked(response.data.interactions);
      
      // 检查是否已收藏
      // isCollected.value = checkIfCollected(response.data.interactions);
      
      // 加载评论
      await loadComments(1);
      
      // 加载相关话题
      await loadRelatedTopics();
    }
  } catch (err) {
    console.error('加载话题详情失败:', err);
    error.value = err.response?.data?.message || '加载失败，请稍后重试';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 加载评论
const loadComments = async (page = 1) => {
  try {
    const response = await topicAPI.getTopicComments(route.params.id, {
      page,
      size: 20,
      sort: 'hot'
    });
    
    if (response.data) {
      const data = response.data;
      comments.value = data.comments.filter(c => !c.isHot) || [];
      hotComments.value = data.comments.filter(c => c.isHot) || [];
      
      commentPagination.value = {
        currentPage: page,
        totalPages: Math.ceil(data.total / 20),
        total: data.total
      };
    }
  } catch (err) {
    console.error('加载评论失败:', err);
    // 不显示 Mock 数据，保持空列表
    comments.value = [];
    hotComments.value = [];
  };
};

// 加载相关话题
const loadRelatedTopics = async () => {
  // TODO: 调用后端接口获取相关话题
  relatedTopics.value = [];
};

// 格式化内容 (支持简单的 Markdown)
const formatContent = (content) => {
  if (!content) return '';
  
  // 换行转<br>
  return content.replace(/\n/g, '<br>');
};

// 获取身份名称
const getIdentityName = (identity) => {
  const map = {
    'student': '学生',
    'merchant': '商户',
    'organization': '团体',
    'individual': '个人',
    'followed': '关注'
  };
  return map[identity] || '用户';
};

// 格式化时间
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

// 点赞
const toggleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    return;
  }
  
  try {
    await topicAPI.likeTopic({ topicId: topic.value.id });
    isLiked.value = !isLiked.value;
    
    if (!topic.value.statistics) {
      topic.value.statistics = {};
    }
    topic.value.statistics.likesCount += isLiked.value ? 1 : -1;
    
    ElMessage.success(isLiked.value ? '点赞成功' : '已取消点赞');
  } catch (err) {
    console.error('点赞失败:', err);
    ElMessage.error('操作失败，请重试');
  }
};

// 收藏
const toggleCollection = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    const response = await topicAPI.collectTopic(topic.value.id);
    if (response.data) {
      isCollected.value = !isCollected.value;
      
      if (!topic.value.statistics) {
        topic.value.statistics = {};
      }
      topic.value.statistics.collectionsCount += isCollected.value ? 1 : -1;
      
      ElMessage.success(isCollected.value ? '收藏成功' : '取消收藏');
    }
  } catch (err) {
    console.error('收藏失败:', err);
    ElMessage.error(err.response?.data?.message || '操作失败，请稍后重试');
  }
};

// 分享
const shareTopic = () => {
  const shareText = `${topic.value.content.substring(0, 50)}...\n\n来自校园话题墙`;
  
  if (navigator.share) {
    navigator.share({
      title: topic.value.title || '校园话题',
      text: shareText,
      url: window.location.href
    });
  } else {
    navigator.clipboard.writeText(`${shareText}\n${window.location.href}`);
    ElMessage.success('链接已复制到剪贴板');
  }
};

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) return;
  
  try {
    await topicAPI.createComment(route.params.id, {
      content: newComment.value
    });
    
    ElMessage.success('评论成功');
    newComment.value = '';
    await loadComments(commentPagination.value.currentPage);
  } catch (err) {
    console.error('评论失败:', err);
    ElMessage.error('评论失败，请重试');
  }
};

// 显示作者信息
const showAuthorInfo = () => {
  if (topic.value?.author?.id) {
    router.push(`/user/${topic.value.author.id}`);
  }
};

// 显示用户信息（点击）
const showUserInfo = (userId) => {
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
  }
};

// 预览图片
const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showPreview.value = true;
};

const closePreview = () => {
  showPreview.value = false;
  previewImageUrl.value = '';
};

// 滚动到评论区
const scrollToComments = () => {
  document.querySelector('.comments-section')?.scrollIntoView({
    behavior: 'smooth'
  });
};

// 编辑话题
const editTopic = async () => {
  const newContent = prompt('编辑话题内容:', topic.value.content);
  if (!newContent || newContent === topic.value.content) return;
  
  try {
    await topicAPI.editTopic(route.params.id, { content: newContent });
    ElMessage.success('编辑成功');
    // 重新加载话题
    await loadTopicDetail();
  } catch (err) {
    console.error('编辑失败:', err);
    ElMessage.error(err.response?.data?.message || '编辑失败，请重试');
  }
};

// 删除话题
const deleteTopic = async () => {
  if (!confirm('确定要删除这个话题吗？')) return;
  
  try {
    await topicAPI.deleteTopic(route.params.id);
    ElMessage.success('话题已删除');
    goBack();
  } catch (err) {
    console.error('删除失败:', err);
    ElMessage.error('删除失败，请重试');
  }
};

// 返回
const goBack = () => {
  router.push('/topicwall');
};

// 查看其他话题
const viewTopic = (id) => {
  router.push(`/topic/${id}`);
};

// 去登录
const goToLogin = () => {
  router.push('/login', { query: { redirect: route.fullPath } });
};
</script>

<style scoped>
.topic-detail-page {
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

.back-btn {
  margin-top: 20px;
  padding: 12px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.topic-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  padding-top: 20px;
}

.back-to-list {
  margin-bottom: 16px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
  font-size: 13px;
  align-self: flex-start;
  transition: all 0.3s;
  white-space: nowrap;
  width: fit-content;
}

.back-to-list:hover {
  background: #f5f7fa;
  border-color: #409eff;
  color: #409eff;
}

.topic-article {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  width: 100%;
}

/* 移动端返回按钮 (固定在顶部) */
.mobile-back-btn {
  display: none;
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  padding: 10px 16px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.3s;
}

.mobile-back-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.author-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.author-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #999;
}

.identity-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
}

.topic-actions {
  display: flex;
  gap: 8px;
}

.action-btn.edit {
  background: #67c23a;
  color: white;
}
  
.action-btn.delete {
  background: #f56c6c;
  color: white;
}

.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 20px 0;
}

.tag-badge.level2 {
  font-weight: 600;
}
  
.tag-badge.level3 {
  background: #909399;
}
  
.tag-badge.level4 {
  background: #e6a23c;
}

.content-text {
  line-height: 1.8;
  color: #333;
  margin: 20px 0;
}

.images-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin: 20px 0;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item img:hover {
  transform: scale(1.05);
}

.statistics-bar {
  display: flex;
  gap: 24px;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
  padding: 20px 0;
}

.interact-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.interact-btn:hover {
  background: #f5f7fa;
}

.interact-btn.like.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}
  
.interact-btn.collect.active {
  background: #e6a23c;
  color: white;
  border-color: #e6a23c;
}

.comments-section {
  margin-top: 40px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hot-badge {
  font-size: 12px;
  padding: 2px 8px;
  background: #f56c6c;
  color: white;
  border-radius: 4px;
}

.comment-input-box {
  margin-bottom: 24px;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 12px;
}

.submit-btn {
  padding: 10px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.login-hint {
  text-align: center;
  padding: 40px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.login-btn {
  margin-top: 12px;
  padding: 10px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.subsection-title {
  font-size: 14px;
  color: #666;
  margin: 20px 0 12px;
  padding-left: 12px;
  border-left: 3px solid #409eff;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}


.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
}

.preview-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

@media (max-width: 768px) {
  .topic-article {
    padding: 16px;
    margin-top: 60px;
  }
  
  .statistics-bar {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .interaction-buttons {
    flex-wrap: wrap;
  }
  
  .interact-btn {
    flex: 1 1 calc(50% - 6px);
  }
}
</style>
