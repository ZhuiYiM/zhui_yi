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

        <!-- 话题内容组件 -->
        <TopicArticle
            :id="topic.id"
            :content="topic.content"
            :images="topic.images"
            :tags="{ topicTags: topic.topicTags, locationTags: topic.locationTags }"
            :author="topic.author"
            :statistics="topic.statistics"
            :interactions="topic.interactions"
            :is-forwarded="topic.isForwarded"
            :forwarded-from-topic-id="topic.forwardedFromTopicId"
            :forwarded-from-product-id="topic.forwardedFromProductId"
            :created-at="topic.createdAt"
            :author-public-info="authorPublicInfo"
            :current-user-id="currentUser?.id"
            @like="toggleLike"
            @collect="toggleCollect"
            @share="handleShare"
            @edit="editTopic"
            @delete="deleteTopic"
            @report="reportTopic"
            @preview-image="previewImage"
            @view-forwarded="viewForwardedTopic"
            @view-forwarded-product="viewForwardedProduct"
            @view-user="viewUserProfile"
        />

        <!-- 评论区组件 -->
        <CommentsSection
            :comments="comments"
            @submit="submitComment"
            @like-comment="likeComment"
            @reply-comment="replyComment"
            @validation-error="handleValidationError"
        />
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !topic" class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>话题不存在</h3>
        <p>该话题可能已被删除或不存在</p>
        <button @click="goBack" class="primary-btn">返回话题墙</button>
      </div>
    </main>
    
    <!-- 分享弹窗 -->
    <ShareModal
      v-model:visible="showShareModal"
      :topic-id="route.params.id"
      :topic-url="currentTopicUrl"
      @repost="handleRepost"
      @copy="handleCopyLink"
    />
    
    <!-- 举报弹窗 -->
    <ReportModal
      v-model="showReportModal"
      target-type="topic"
      :target-id="route.params.id"
      @success="handleReportSuccess"
    />
    
    <!-- 图片预览弹窗 -->
    <div v-if="showImagePreview" class="modal-overlay" @click="closeImagePreview">
      <div class="modal-content" @click.stop>
        <button @click="closeImagePreview" class="close-btn">×</button>
        <img :src="previewImageUrl" alt="预览图片" class="preview-large-image">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import ShareModal from './ShareModal.vue';
import ReportModal from '../common/ReportModal.vue';
import TopicArticle from './TopicArticle.vue';
import CommentsSection from './CommentsSection.vue';
import { topicAPI } from '@/api/topic';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const topic = ref(null);
const comments = ref([]);
const authorPublicInfo = ref(null);
const currentUser = ref(null);
const isMobile = ref(window.innerWidth <= 768);
const showShareModal = ref(false);
const showImagePreview = ref(false);
const previewImageUrl = ref('');
const showReportModal = ref(false);

// 打开举报弹窗
const openReportModal = () => {
  console.log('[TopicDetail] 准备打开举报弹窗');
  console.log('[TopicDetail] 当前 topic:', topic.value);
  console.log('[TopicDetail] route.params.id:', route.params.id);
  if (!route.params.id) {
    console.error('[TopicDetail] 无法打开举报弹窗：route.params.id 为空');
    ElMessage.warning('话题信息未加载完成，请稍后再试');
    return;
  }
  showReportModal.value = true;
  console.log('[TopicDetail] 举报弹窗已打开，showReportModal:', showReportModal.value);
};

// 处理举报成功
const handleReportSuccess = (data) => {
  console.log('[TopicDetail] 举报成功:', data);
  showReportModal.value = false;
  ElMessage.success('举报成功，感谢你的反馈！');
};

// 计算属性 - 当前话题 URL
const currentTopicUrl = computed(() => {
  return `${window.location.origin}/topic/${route.params.id}`;
});

// 获取话题详情
const fetchTopicDetail = async () => {
  try {
    const response = await topicAPI.getTopicDetail(route.params.id);
    
    if (response) {
      // request.js 拦截器已经返回了 data 字段，直接使用 response
      const data = response;
      
      // 适配后端实际返回的数据结构（平铺字段）
      topic.value = {
        id: data.id,
        content: data.content,
        images: data.images || [],
        tags: data.tags || [],
        // ✅ 处理 topicTags：后端可能返回 topicTags 对象数组或 topicTagCodes 字符串数组
        topicTags: (() => {
          if (data.topicTags && Array.isArray(data.topicTags)) {
            return data.topicTags; // 已经是对象数组
          } else if (data.topicTagCodes) {
            // 字符串数组，需要转换为对象数组
            const codes = Array.isArray(data.topicTagCodes) ? data.topicTagCodes : [];
            return codes.map(code => typeof code === 'string' ? { code, name: code } : code);
          }
          return [];
        })(),
        // ✅ 处理 locationTags：后端可能返回 locationTags 对象数组或 locationTagCodes 字符串数组
        locationTags: (() => {
          if (data.locationTags && Array.isArray(data.locationTags)) {
            return data.locationTags; // 已经是对象数组
          } else if (data.locationTagCodes) {
            // 字符串数组，需要转换为对象数组
            const codes = Array.isArray(data.locationTagCodes) ? data.locationTagCodes : [];
            return codes.map(code => typeof code === 'string' ? { code, name: code } : code);
          }
          return [];
        })(),
        author: {
          id: data.author?.id || data.authorId,
          username: data.author?.username || data.authorUsername || data.author?.realName,
          realName: data.author?.realName || data.authorRealName,
          avatarUrl: data.author?.avatarUrl || data.authorAvatarUrl,
          studentId: data.author?.studentId || data.authorStudentId,
          identityTag: data.author?.identityTag || data.identityTag // ✅ 身份标签
        },
        statistics: {
          viewsCount: data.statistics?.viewsCount || data.viewsCount || 0,
          likesCount: data.statistics?.likesCount || data.likesCount || 0,
          commentsCount: data.statistics?.commentsCount || data.commentsCount || 0,
          collectionsCount: data.statistics?.collectionsCount || data.collectionsCount || 0
        },
        interactions: {
          isLiked: data.interactions?.isLiked || data.isLiked || false,
          isCollected: data.interactions?.isCollected || data.isCollected || false
        },
        isEssence: data.isEssence || 0,
        isForwarded: data.isForwarded || false,
        forwardedFromTopicId: data.forwardedFromTopicId || null,
        forwardedFromProductId: data.forwardedFromProductId || null,
        createdAt: data.createdAt
      };

      // 获取作者公开信息
      if (topic.value.author && topic.value.author.id) {
        await fetchAuthorPublicInfo(topic.value.author.id);
      }

      // 获取评论列表
      await fetchComments();
    } else {
      throw new Error('话题详情数据为空');
    }
  } catch (error) {
    console.error('获取话题详情失败:', error);
    ElMessage.error('获取话题详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取作者公开信息
const fetchAuthorPublicInfo = async (userId) => {
  try {
    const response = await topicAPI.getUserPublicInfo(userId);
    
    if (response) {
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
    console.error('获取作者信息失败:', error);
  }
};

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await topicAPI.getTopicComments(route.params.id, { page: 1, size: 20, sort: 'latest' });
    
    if (response) {
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
    console.error('获取评论失败:', error);
  }
};

// 发表评论
const submitComment = async (content) => {
  try {
    await topicAPI.createComment(route.params.id, { content });
    
    ElMessage.success('评论成功');
    await fetchComments();
    await fetchTopicDetail();
  } catch (error) {
    console.error('评论失败:', error);
    ElMessage.error('评论失败');
  }
};

// 处理验证错误
const handleValidationError = (message) => {
  ElMessage.warning(message);
};

// 点赞话题
const toggleLike = async () => {
  try {
    const action = topic.value.interactions.isLiked ? 'unlike' : 'like';
    const responseData = await topicAPI.likeTopic(route.params.id, action);
    
    topic.value.interactions.isLiked = !topic.value.interactions.isLiked;
    topic.value.statistics.likesCount += topic.value.interactions.isLiked ? 1 : -1;
    
    if (responseData) {
      topic.value.statistics.likesCount = responseData.likesCount || topic.value.statistics.likesCount;
      topic.value.interactions.isLiked = responseData.isLiked !== undefined ? responseData.isLiked : topic.value.interactions.isLiked;
    }
    
    ElMessage.success(topic.value.interactions.isLiked ? '点赞成功' : '已取消点赞');
  } catch (error) {
    if (error.response?.status === 409 || error.code === 409) {
      await fetchTopicDetail();
      const msg = error.response?.data?.message || error.message || '状态已同步';
      ElMessage.warning(msg);
    } else {
      ElMessage.error('点赞失败，请稍后重试');
    }
  }
};

// 收藏话题
const toggleCollect = async () => {
  try {
    const originalState = topic.value.interactions.isCollected;
    topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    
    if (originalState) {
      topic.value.statistics.collectionsCount = Math.max(0, topic.value.statistics.collectionsCount - 1);
    } else {
      topic.value.statistics.collectionsCount += 1;
    }
    
    const responseData = await topicAPI.collectTopic(route.params.id);
    
    if (responseData && responseData.data) {
      topic.value.statistics.collectionsCount = responseData.data.collectionsCount || topic.value.statistics.collectionsCount;
      topic.value.interactions.isCollected = responseData.data.collected !== undefined ? responseData.data.collected : topic.value.interactions.isCollected;
    }
    
    const action = topic.value.interactions.isCollected ? '收藏成功' : '已取消收藏';
    ElMessage.success(action);
  } catch (error) {
    topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    if (topic.value.interactions.isCollected) {
      topic.value.statistics.collectionsCount += 1;
    } else {
      topic.value.statistics.collectionsCount = Math.max(0, topic.value.statistics.collectionsCount - 1);
    }
    
    if (error.response?.status === 409) {
      const msg = error.response.data?.message || '操作失败';
      ElMessage.warning(msg);
    } else {
      ElMessage.error('收藏失败，请稍后重试');
    }
  }
};

// 分享话题
const handleShare = () => {
  showShareModal.value = true;
};

// 处理转发
const handleRepost = (topicId) => {
  try {
    const shareData = {
      sourceType: 'topic',
      sourceId: topicId,
      content: topic.value.content,
      author: topic.value.author.username || topic.value.author.realName || '匿名用户',
      url: currentTopicUrl.value,
      originalTopicId: topicId
    };
    
    // 将分享数据存储到 sessionStorage
    sessionStorage.setItem('shareData', JSON.stringify(shareData));
    
    // 跳转到话题墙页面，并传递参数
    router.push({
      path: '/topicwall',
      query: {
        from: 'share',
        sourceType: 'topic',
        sourceId: topicId
      }
    });
    
    ElMessage.success('正在跳转到发布页面...');
  } catch (error) {
    console.error('跳转发布页面失败:', error);
    ElMessage.error('跳转失败，请稍后重试');
  }
};

// 处理复制链接
const handleCopyLink = (url) => {
  console.log('已复制链接:', url);
};

// 查看被转发的话题
const viewForwardedTopic = async (forwardedFromTopicId) => {
  if (forwardedFromTopicId) {
    const targetId = forwardedFromTopicId;
    const currentId = route.params.id;
    
    if (targetId.toString() === currentId.toString()) {
      return;
    }
    
    try {
      // 先尝试获取话题详情
      const response = await topicAPI.getTopicDetail(targetId);
      
      if (response && response.id) {
        // 话题存在，跳转到详情页
        router.push(`/topic/${targetId}`);
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
  } else {
    ElMessage.warning('无法获取原话题信息');
  }
};

// 查看被分享的商品
const viewForwardedProduct = async (forwardedFromProductId) => {
  if (forwardedFromProductId) {
    try {
      // 跳转到商品详情页
      router.push(`/product/${forwardedFromProductId}`);
    } catch (error) {
      console.error('查看商品失败:', error);
      ElMessage.error('商品不存在或已下架');
    }
  } else {
    ElMessage.warning('无法获取商品信息');
  }
};

// 查看用户主页
const viewUserProfile = (userId) => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}');
  const currentUserId = userData.id;
  
  if (userId === currentUserId) {
    router.push('/personalcenter');
  } else {
    router.push(`/user/profile/${userId}`);
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
      const result = await topicAPI.deleteTopic(route.params.id);
      
      if (typeof result === 'string') {
        ElMessage.success(result || '删除成功');
        router.push('/topicwall');
      } else if (result && result.code === 200) {
        ElMessage.success(result.message || '删除成功');
        router.push('/topicwall');
      } else {
        ElMessage.error(result?.message || '删除失败');
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || error.message || '删除失败';
      ElMessage.error(errorMsg);
    }
  }
};

// 举报话题
const reportTopic = () => {
  showReportModal.value = true;
};

// 点赞评论
const likeComment = async (comment) => {
  try {
    // TODO: 调用点赞评论接口
    comment.likesCount++;
    ElMessage.success('点赞成功');
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
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false;
  previewImageUrl.value = '';
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

  try {
    const response = await topicAPI.getUserProfile();
    if (response) {
      currentUser.value = response;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }

  await fetchTopicDetail();
});

// 监听路由参数变化，当话题 ID 变化时重新加载数据
watch(
  () => route.params.id,
  async (newId, oldId) => {
    if (newId && newId !== oldId) {
      // 重置状态
      topic.value = null;
      comments.value = [];
      authorPublicInfo.value = null;
      loading.value = true;
      
      // 重新加载数据
      await fetchTopicDetail();
    }
  }
);
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

@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
}

/* 图片预览弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  padding: 20px;
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 40px;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #409EFF;
}

.preview-large-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>
