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
            :tags="topic.tags"
            :author="topic.author"
            :statistics="topic.statistics"
            :interactions="topic.interactions"
            :is-forwarded="topic.isForwarded"
            :forwarded-from-topic-id="topic.forwardedFromTopicId"
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import ShareModal from './ShareModal.vue';
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
        level2Tags: data.level2TagCodes || [], // ✅ 使用字符串数组
        level3Tags: data.level3TagCodes || [], // ✅ 使用字符串数组
        level4Tags: data.level4TagCodes || [], // ✅ 使用字符串数组
        author: {
          id: data.author?.id || data.authorId,
          username: data.author?.username || data.authorUsername || data.author?.realName,
          realName: data.author?.realName || data.authorRealName,
          avatarUrl: data.author?.avatarUrl || data.authorAvatarUrl,
          studentId: data.author?.studentId || data.authorStudentId,
          level1Tag: data.author?.level1Tag || data.level1Tag
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
    
    sessionStorage.setItem('shareData', JSON.stringify(shareData));
    router.push('/topicwall?from=share&sourceType=topic&sourceId=' + topicId);
    
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
const viewForwardedTopic = (forwardedFromTopicId) => {
  if (forwardedFromTopicId) {
    const targetId = forwardedFromTopicId;
    const currentId = route.params.id;
    
    if (targetId.toString() === currentId.toString()) {
      return;
    }
    
    router.push(`/topic/${targetId}`);
  } else {
    ElMessage.warning('无法获取原话题信息');
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
  ElMessageBox.prompt('请输入举报原因（可选）', '举报话题', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.{0,200}/,
    inputErrorMessage: '原因不能超过 200 字',
    type: 'warning'
  }).then(({ value }) => {
    console.log('举报原因:', value);
    ElMessage.success('举报已提交，我们会尽快处理');
  }).catch(() => {
    console.log('用户取消了举报');
  });
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
  window.open(imageUrl, '_blank');
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
</style>
