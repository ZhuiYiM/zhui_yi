<template>
  <div :class="['topic-detail-container', { 'mobile': isMobile }]">
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
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 话题详情 -->
      <div v-else-if="topic" class="topic-detail">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          ← 返回话题墙
        </button>

        <!-- 话题内容 -->
        <article class="topic-article">
          <!-- 作者信息 -->
          <header class="article-header">
            <div class="author-info">
              <img
                  :src="topic.author.avatarUrl || defaultAvatar"
                  :alt="topic.author.username"
                  class="author-avatar"
                  @mouseenter="showAuthorTooltip = true"
                  @mouseleave="showAuthorTooltip = false"
              >
              <div class="author-details">
                <span class="author-name">{{ topic.author.username || '匿名用户' }}</span>
                <!-- 显示身份标签 -->
                <span v-if="topic.author.level1Tag" class="identity-tag" :class="topic.author.level1Tag">
                  {{ getIdentityTagName(topic.author.level1Tag) }}
                </span>
                <span class="post-time">{{ formatDate(topic.createdAt) }}</span>
              </div>
            </div>

            <!-- 作者信息悬停提示 -->
            <div v-if="showAuthorTooltip && authorPublicInfo" class="author-tooltip">
              <div class="tooltip-content">
                <img :src="authorPublicInfo.basicInfo.avatarUrl || defaultAvatar" class="tooltip-avatar">
                <div class="tooltip-info">
                  <h4>{{ authorPublicInfo.basicInfo.username || '匿名用户' }}</h4>
                  <p v-if="authorPublicInfo.basicInfo.college">{{ authorPublicInfo.basicInfo.college }}</p>
                  <p v-if="authorPublicInfo.academicInfo.major">{{ authorPublicInfo.academicInfo.major }}</p>
                  <div class="tooltip-stats">
                    <span>{{ authorPublicInfo.statistics.postCount }} 话题</span>
                    <span>{{ authorPublicInfo.statistics.likesReceived }} 点赞</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="header-actions" v-if="isAuthor">
              <button @click="editTopic" class="edit-btn">✏️ 编辑</button>
              <button @click="deleteTopic" class="delete-btn">🗑️ 删除</button>
            </div>
          </header>

          <!-- 话题正文 -->
          <div class="article-body">
            <p class="content-text">{{ topic.content }}</p>

            <!-- 图片展示 -->
            <div v-if="topic.images && topic.images.length > 0" class="content-images">
              <img
                  v-for="(image, index) in topic.images"
                  :key="index"
                  :src="image"
                  :alt="`图片${index + 1}`"
                  class="content-image"
                  @click="previewImage(image)"
              >
            </div>

            <!-- 标签展示 -->
            <div v-if="hasTags" class="content-tags">
              <!-- 二级标签 -->
              <template v-if="topic.tags.level2 && topic.tags.level2.length > 0">
                <span
                    v-for="tag in topic.tags.level2"
                    :key="'l2-' + (tag.code || tag.name || tag)"
                    class="hashtag level2"
                    :style="getTagStyle(tag)"
                >
                  {{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 三级标签 -->
              <template v-if="topic.tags.level3 && topic.tags.level3.length > 0">
                <span
                    v-for="tag in topic.tags.level3"
                    :key="'l3-' + (tag.code || tag.name || tag)"
                    class="hashtag level3"
                >
                  📍{{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 四级标签 -->
              <template v-if="topic.tags.level4 && topic.tags.level4.length > 0">
                <span
                    v-for="tag in topic.tags.level4"
                    :key="'l4-' + (tag.code || tag.name || tag)"
                    class="hashtag level4"
                >
                  #{{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 兼容简单数组格式的标签 -->
              <template v-if="Array.isArray(topic.tags) && topic.tags.length > 0">
                <span
                    v-for="(tag, index) in topic.tags"
                    :key="'tag-' + index"
                    class="hashtag simple"
                >
                  #{{ tag.name || tag }}
                </span>
              </template>
            </div>
          </div>

          <!-- 统计信息 -->
          <footer class="article-footer">
            <div class="statistics">
              <span class="stat-item">👁️ {{ topic.statistics.viewsCount }} 浏览</span>
              <span class="stat-item">👍 {{ topic.statistics.likesCount }} 点赞</span>
              <span class="stat-item">💬 {{ topic.statistics.commentsCount }} 评论</span>
              <span class="stat-item">⭐ {{ topic.statistics.collectionsCount }} 收藏</span>
            </div>

            <!-- 互动按钮 -->
            <div class="interaction-buttons">
              <button
                  @click="toggleLike"
                  class="interact-btn like"
                  :class="{ active: topic.interactions.isLiked }"
              >
                {{ topic.interactions.isLiked ? '❤️' : '🤍' }} 点赞
              </button>
              <button
                  @click="toggleCollect"
                  class="interact-btn collect"
                  :class="{ active: topic.interactions.isCollected }"
              >
                {{ topic.interactions.isCollected ? '⭐' : '☆' }} 收藏
              </button>
              <button @click="shareTopic" class="interact-btn share">
                ↗️ 分享
              </button>
            </div>
          </footer>
        </article>

        <!-- 评论区 -->
        <section class="comments-section">
          <h3>评论 ({{ comments.length }})</h3>

          <!-- 发表评论 -->
          <div class="comment-input-box">
            <textarea
                v-model="newComment"
                placeholder="写下你的评论..."
                class="comment-textarea"
                rows="3"
            ></textarea>
            <button @click="submitComment" class="submit-comment-btn">发表评论</button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
            >
              <img
                  :src="comment.author.avatarUrl || defaultAvatar"
                  class="comment-avatar"
              >
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.author.username }}</span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
                <div class="comment-actions">
                  <button @click="likeComment(comment)" class="comment-like-btn">
                    👍 {{ comment.likesCount }}
                  </button>
                  <button @click="replyComment(comment)" class="comment-reply-btn">
                    💬 回复
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !topic" class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>话题不存在</h3>
        <p>该话题可能已被删除或不存在</p>
        <button @click="goBack" class="primary-btn">返回话题墙</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import { topicAPI } from '@/api/topic';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const topic = ref(null);
const comments = ref([]);
const newComment = ref('');
const isMobile = ref(window.innerWidth <= 768);
const showAuthorTooltip = ref(false);
const authorPublicInfo = ref(null);
const currentUser = ref(null);

// 计算属性
const isAuthor = computed(() => {
  return currentUser.value && topic.value && currentUser.value.id === topic.value.author.id;
});

// 是否有标签
const hasTags = computed(() => {
  if (!topic.value || !topic.value.tags) {
    return false;
  }
  
  const tags = topic.value.tags;
  // 检查各种可能的标签格式
  return (
    (tags.level2 && tags.level2.length > 0) ||
    (tags.level3 && tags.level3.length > 0) ||
    (tags.level4 && tags.level4.length > 0) ||
    // 兼容简单数组格式
    (Array.isArray(tags) && tags.length > 0)
  );
});

const defaultAvatar = 'https://placehold.co/100x100/CCCCCC/FFFFFF?text=默认头像';

// 获取话题详情
const fetchTopicDetail = async () => {
  try {
    console.log('📡 正在获取话题详情，topicId:', route.params.id);
    const response = await topicAPI.getTopicDetail(route.params.id);
    
    console.log('✅ 话题详情响应:', response);
    console.log('📋 响应数据结构:', JSON.stringify(response, null, 2));

    if (response) {
      // 适配后端实际返回的数据结构（平铺字段）
      topic.value = {
        id: response.id,
        content: response.content,
        images: response.images || [],
        tags: response.tags || {},
        author: {
          id: response.author?.id || response.authorId,
          username: response.author?.username || response.authorUsername || response.author?.realName,
          realName: response.author?.realName || response.authorRealName,
          avatarUrl: response.author?.avatarUrl || response.authorAvatarUrl,
          studentId: response.author?.studentId || response.authorStudentId,
          level1Tag: response.author?.level1Tag || response.level1Tag // 添加身份标签
        },
        statistics: {
          viewsCount: response.statistics?.viewsCount || response.viewsCount || 0,
          likesCount: response.statistics?.likesCount || response.likesCount || 0,
          commentsCount: response.statistics?.commentsCount || response.commentsCount || 0,
          collectionsCount: response.statistics?.collectionsCount || response.collectionsCount || 0
        },
        interactions: {
          isLiked: response.interactions?.isLiked || response.isLiked || false,
          isCollected: response.interactions?.isCollected || response.isCollected || false
        },
        isEssence: response.isEssence || 0,
        createdAt: response.createdAt
      };

      console.log('👤 话题作者信息:', topic.value.author);
      console.log('🏷️ 作者身份标签:', topic.value.author.level1Tag);

      // 获取作者公开信息
      if (topic.value.author && topic.value.author.id) {
        console.log('📡 正在获取作者公开信息，authorId:', topic.value.author.id);
        await fetchAuthorPublicInfo(topic.value.author.id);
      }

      // 获取评论列表
      await fetchComments();
    } else {
      throw new Error('话题详情数据为空');
    }
  } catch (error) {
    console.error('❌ 获取话题详情失败:', error);
    ElMessage.error('获取话题详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取作者公开信息
const fetchAuthorPublicInfo = async (userId) => {
  try {
    console.log('📡 正在获取作者公开信息，userId:', userId);
    const response = await topicAPI.getUserPublicInfo(userId);
    
    console.log('✅ 作者公开信息响应:', response);

    if (response) {
      // 适配后端返回的数据结构
      authorPublicInfo.value = {
        basicInfo: {
          id: response.basicInfo?.id || response.id,
          username: response.basicInfo?.username || response.username,
          realName: response.basicInfo?.realName || response.realName,
          avatarUrl: response.basicInfo?.avatarUrl || response.avatarUrl,
          bio: response.basicInfo?.bio || response.bio,
          college: response.basicInfo?.college || response.college
        },
        academicInfo: response.academicInfo || {},
        identity: response.identity || {},
        privacySettings: response.privacySettings || {},
        statistics: {
          postCount: response.statistics?.postCount || 0,
          likesReceived: response.statistics?.likesReceived || 0,
          followerCount: response.statistics?.followerCount || 0,
          followingCount: response.statistics?.followingCount || 0
        },
        canMessage: response.canMessage || false
      };
    }
  } catch (error) {
    console.error('❌ 获取作者信息失败:', error);
  }
};

// 获取评论列表
const fetchComments = async () => {
  try {
    console.log('📡 正在获取评论列表，topicId:', route.params.id);
    const response = await topicAPI.getTopicComments(route.params.id, { page: 1, size: 20, sort: 'latest' });
    
    console.log('✅ 评论列表响应:', response);

    if (response) {
      // 适配后端返回的数据结构
      comments.value = (response.comments || response.data || []).map(comment => ({
        id: comment.id,
        content: comment.content,
        author: {
          id: comment.author?.id,
          username: comment.author?.username || comment.author?.realName,
          avatarUrl: comment.author?.avatarUrl
        },
        likesCount: comment.likesCount || 0,
        createdAt: comment.createdAt,
        replies: comment.replies || []
      }));
    }
  } catch (error) {
    console.error('❌ 获取评论失败:', error);
  }
};

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  try {
    const response = await topicAPI.createComment(route.params.id, { content: newComment.value });
    
    ElMessage.success('评论成功');
    newComment.value = '';
    await fetchComments();

    // 重新获取话题详情以更新评论数
    await fetchTopicDetail();
  } catch (error) {
    console.error('评论失败:', error);
    ElMessage.error('评论失败');
  }
};

// 点赞话题
const toggleLike = async () => {
  try {
   const action = topic.value.interactions.isLiked ? 'unlike' : 'like';
   const responseData = await topicAPI.likeTopic(route.params.id, action);
    
    // 更新为后端返回的最新状态
    topic.value.interactions.isLiked = !topic.value.interactions.isLiked;
    topic.value.statistics.likesCount += topic.value.interactions.isLiked ? 1 : -1;
    
    if (responseData) {
      topic.value.statistics.likesCount = responseData.likesCount || topic.value.statistics.likesCount;
      topic.value.interactions.isLiked = responseData.isLiked !== undefined ? responseData.isLiked : topic.value.interactions.isLiked;
    }
    
    ElMessage.success(topic.value.interactions.isLiked ? '点赞成功' : '已取消点赞');
  } catch (error) {
   console.error('点赞失败:', error);
    // 409 表示重复点赞或取消点赞（状态冲突）
    if (error.response?.status === 409) {
     const msg = error.response.data?.message || (topic.value.interactions.isLiked ? '您已点过赞了' : '您还未点过赞');
      ElMessage.warning(msg);
    } else {
      ElMessage.error('点赞失败，请稍后重试');
    }
  }
};

// 收藏话题
const toggleCollect = async () => {
  try {
  const action = topic.value.interactions.isCollected ? '取消收藏' : '收藏';
    
    // 乐观更新 UI
  const originalState = topic.value.interactions.isCollected;
    topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    topic.value.statistics.collectionsCount += topic.value.interactions.isCollected ? 1 : -1;
    
    // 调用 API
  const responseData = await topicAPI.collectTopic(route.params.id);
    
    // 更新为后端返回的最新状态
    if (responseData) {
      topic.value.statistics.collectionsCount = responseData.collectionsCount || topic.value.statistics.collectionsCount;
      topic.value.interactions.isCollected = responseData.isCollected !== undefined ? responseData.isCollected: topic.value.interactions.isCollected;
    }
    
    ElMessage.success(action === '收藏' ? '收藏成功' : '已取消收藏');
  } catch (error) {
  console.error('收藏失败:', error);
    // 恢复原状态
    topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    topic.value.statistics.collectionsCount += topic.value.interactions.isCollected ? 1 : -1;
    
    // 409 表示重复操作（状态冲突）
    if (error.response?.status === 409) {
   const msg = error.response.data?.message || '操作失败';
      ElMessage.warning(msg);
    } else {
      ElMessage.error('收藏失败，请稍后重试');
    }
  }
};

// 分享话题
const shareTopic = () => {
  const shareText = `${topic.value.content}\n\n来自 @${topic.value.author.username} 的话题`;
  if (navigator.share) {
    navigator.share({
      title: '校园话题分享',
      text: shareText
    });
  } else {
    navigator.clipboard.writeText(shareText);
    ElMessage.success('分享链接已复制');
  }
};

// 编辑话题
const editTopic = () => {
  ElMessage.info('编辑功能开发中...');
};

// 删除话题
const deleteTopic = async () => {
  try {
    const confirmed = await ElMessageBox.confirm('确定要删除这个话题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    if (confirmed) {
      const response = await axios.delete(
          `/api/topic/${route.params.id}`,
          { headers: getAuthHeaders() }
      );

      if (response.data.code === 200) {
        ElMessage.success('删除成功');
        goBack();
      } else {
        ElMessage.error(response.data.message || '删除失败');
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 点赞评论
const likeComment = async (comment) => {
  try {
    const response = await axios.post(
        `/api/topic/comments/${comment.id}/like`,
        { action: 'like' },
        { headers: getAuthHeaders() }
    );

    if (response.data.code === 200) {
      comment.likesCount++;
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('点赞评论失败:', error);
  }
};

// 回复评论
const replyComment = (comment) => {
  ElMessage.info('回复功能开发中...');
};

// 预览图片
const previewImage = (imageUrl) => {
  // 可以使用 Element Plus 的 ImagePreview 组件
  window.open(imageUrl, '_blank');
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`;
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`;
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`;
  } else {
    return date.toLocaleDateString('zh-CN');
  }
};

// 获取身份标签名称
const getIdentityTagName = (tagCode) => {
  const identityMap = {
    'student': '学生',
    'merchant': '商户',
    'organization': '团体',
    'individual': '个人',
    'admin': '管理员'
  };
  return identityMap[tagCode] || tagCode;
};

// 获取标签样式
const getTagStyle = (tag) => {
  if (tag && tag.color) {
    return {
      backgroundColor: tag.color + '20',
      borderColor: tag.color
    };
  }
  return {};
};

// 返回上一页
const goBack = () => {
  router.push('/topicwall');
};

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(async () => {
  window.addEventListener('resize', handleResize);

  // 获取当前用户信息
  try {
    console.log('📡 正在获取当前用户信息');
    const response = await topicAPI.getUserProfile();
    
    if (response) {
      currentUser.value = response;
      console.log('✅ 当前用户信息:', currentUser.value);
    }
  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
  }

  await fetchTopicDetail();
});
</script>

<style scoped>
.topic-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e0e0e0;
  border-top-color: #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.back-btn {
  background: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
  font-size: 14px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #409EFF;
  color: #fff;
}

.topic-article {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  position: relative;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
}

/* 身份标签 */
.identity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
  transition: all 0.3s;
}

.identity-tag.student {
  background: #e3f2fd;
  color: #1976d2;
}

.identity-tag.merchant {
  background: #fff3e0;
  color: #f57c00;
}

.identity-tag.organization {
  background: #e8f5e9;
  color: #388e3c;
}

.identity-tag.individual {
  background: #f3e5f5;
  color: #7b1fa2;
}

.identity-tag.admin {
  background: #ffebee;
  color: #d32f2f;
}

/* 作者信息悬停提示 */
.author-tooltip {
  position: absolute;
  top: 60px;
  left: 0;
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 250px;
}

.tooltip-content {
  display: flex;
  gap: 12px;
}

.tooltip-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.tooltip-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.tooltip-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}

.tooltip-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.edit-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.edit-btn {
  background: #ecf5ff;
  color: #409EFF;
}

.edit-btn:hover {
  background: #409EFF;
  color: #fff;
}

.delete-btn {
  background: #fef0f0;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #f56c6c;
  color: #fff;
}

.article-body {
  margin-bottom: 25px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.content-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.content-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.content-image:hover {
  transform: scale(1.02);
}

.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hashtag {
  background: #f0f2f5;
  color: #606266;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.hashtag:hover {
  background: #409EFF;
  color: #fff;
}

.article-footer {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.statistics {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
}

.interact-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.interact-btn:hover {
  transform: translateY(-2px);
}

.interact-btn.like.active {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.interact-btn.collect.active {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}

/* 评论区 */
.comments-section {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.comments-section h3 {
  margin-bottom: 20px;
  color: #333;
}

.comment-input-box {
  margin-bottom: 30px;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  margin-bottom: 10px;
}

.submit-comment-btn {
  background: #409EFF;
  color: #fff;
  border: none;
  padding: 10px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.submit-comment-btn:hover {
  background: #66b1ff;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.comment-like-btn, .comment-reply-btn {
  background: none;
  border: none;
  padding: 0;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.comment-like-btn:hover, .comment-reply-btn:hover {
  color: #409EFF;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  color: #999;
  margin-bottom: 20px;
}

.primary-btn {
  background: #409EFF;
  color: #fff;
  border: none;
  padding: 12px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.primary-btn:hover {
  background: #66b1ff;
}

/* 标签样式 */
.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
}

.hashtag {
  background: #f0f2f5;
  color: #606266;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.hashtag:hover {
  background: #409EFF;
  color: #fff;
  transform: translateY(-2px);
}

.hashtag.level2 {
  font-weight: 500;
}

.hashtag.level3 {
  background: #ecf5ff;
  color: #409EFF;
}

.hashtag.level4 {
  background: #f4f4f5;
  color: #909399;
}

.hashtag.simple {
  background: #f0f2f5;
  color: #606266;
}

@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }

  .topic-article {
    padding: 20px;
  }

  .article-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .author-tooltip {
    left: auto;
    right: 0;
  }

  .statistics {
    flex-wrap: wrap;
    gap: 10px;
  }

  .interaction-buttons {
    flex-direction: column;
  }
}
</style>
