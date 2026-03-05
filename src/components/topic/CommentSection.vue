<template>
  <div class="comment-section">
    <h3 class="section-title">评论 ({{ comments.length }})</h3>
    
    <!-- 发表评论 -->
    <div v-if="isLoggedIn" class="comment-input-box">
      <textarea
        v-model="newComment"
        placeholder="写下你的评论..."
        class="comment-textarea"
        rows="3"
        maxlength="500"
      ></textarea>
      <div class="input-actions">
        <span class="char-count">{{ newComment.length }}/500</span>
        <button @click="submitComment" :disabled="!canSubmit" class="submit-btn">
          {{ submitting ? '发送中...' : '发表评论' }}
        </button>
      </div>
    </div>
    
    <!-- 未登录提示 -->
    <div v-else class="login-hint">
      <p>登录后才能发表评论哦~</p>
      <button @click="goToLogin" class="login-btn">去登录</button>
    </div>
    
    <!-- 评论列表 -->
    <div v-loading="loading" class="comments-list">
      <div v-if="comments.length === 0" class="empty-comments">
        <p>暂无评论，快来抢沙发吧~</p>
      </div>
      
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <img 
          :src="comment.author.avatarUrl || defaultAvatar" 
          :alt="comment.author.username"
          class="comment-avatar"
          @click.stop="viewUserProfile(comment.author.id)"
        >
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-author" @click.stop="viewUserProfile(comment.author.id)">
              {{ comment.author.realName || comment.author.username }}
            </span>
            <span v-if="comment.author.identity?.level1Tag" class="identity-tag" :class="comment.author.identity.level1Tag">
              {{ getIdentityTagName(comment.author.identity.level1Tag) }}
            </span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <div class="comment-actions">
            <button @click="likeCommentAction(comment)" class="action-btn like-btn">
              👍 {{ comment.likesCount || 0 }}
            </button>
            <button @click="replyComment(comment)" class="action-btn reply-btn">
              💬 回复
            </button>
            <button v-if="canDelete(comment)" @click="deleteCommentAction(comment)" class="action-btn delete-btn">
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { topicAPI } from '@/api/topic';

const props = defineProps({
  topicId: { type: Number, required: true }
});

const emit = defineEmits(['comment-added', 'comment-deleted']);

const router = useRouter();

// 响应式数据
const comments = ref([]);
const newComment = ref('');
const loading = ref(false);
const submitting = ref(false);
const isLoggedIn = ref(false);
const currentUserId = ref(null);

const defaultAvatar = 'https://placehold.co/40x40/4A90E2/FFFFFF?text=U';

// 计算属性
const canSubmit = computed(() => {
  return newComment.value.trim().length > 0 && !submitting.value;
});

// 生命周期
onMounted(async () => {
  checkLoginStatus();
  await loadComments();
});

// 监听话题 ID 变化
watch(() => props.topicId, async (newId) => {
  if (newId) {
    await loadComments();
  }
});

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  
  if (token && user.id) {
    isLoggedIn.value = true;
    currentUserId.value = user.id;
  } else {
    isLoggedIn.value = false;
    currentUserId.value = null;
  }
};

// 加载评论列表
const loadComments = async () => {
  if (!props.topicId) return;
  
  loading.value = true;
  try {
    const response = await topicAPI.getTopicComments(props.topicId, {
      page: 1,
      size: 50,
      sort: 'latest'
    });
    
    console.log('📥 评论响应:', response);
    
    // 适配后端返回的数据结构
    if (response) {
      const commentData = response.data || response;
      comments.value = Array.isArray(commentData) ? commentData : (commentData.records || []);
      console.log('✅ 评论已加载:', comments.value.length, '条');
    }
  } catch (error) {
    console.error('❌ 加载评论失败:', error);
    // 静默失败，不显示错误提示
    comments.value = [];
  } finally {
    loading.value = false;
  }
};

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  
  submitting.value = true;
  try {
    const response = await topicAPI.createComment(props.topicId, {
      content: newComment.value.trim()
    });
    
    console.log('📤 评论响应:', response);
    
    if (response) {
      ElMessage.success('评论成功');
      newComment.value = '';
      
      // 重新加载评论
      await loadComments();
      
      // 触发事件通知父组件
      emit('comment-added');
    }
  } catch (error) {
    console.error('❌ 发表评论失败:', error);
    const errorMsg = error.response?.data?.message || '评论失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

// 点赞评论
const likeCommentAction = async (comment) => {
  try {
    const response = await topicAPI.likeComment(comment.id, 'like');
    
    if (response) {
      // 更新本地评论的点赞数
      const targetComment = comments.value.find(c => c.id === comment.id);
      if (targetComment) {
        targetComment.likesCount = (targetComment.likesCount || 0) + 1;
      }
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('❌ 点赞评论失败:', error);
    ElMessage.error('操作失败');
  }
};

// 回复评论
const replyComment = (comment) => {
  ElMessage.info('回复功能开发中...');
  // TODO: 实现回复功能
};

// 删除评论
const deleteCommentAction = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await topicAPI.deleteComment(comment.id);
    
    if (response) {
      ElMessage.success('评论已删除');
      
      // 重新加载评论
      await loadComments();
      
      // 触发事件通知父组件
      emit('comment-deleted');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 删除评论失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 判断是否可以删除评论
const canDelete = (comment) => {
  // 只有评论作者或管理员可以删除
  return currentUserId.value && 
         (comment.author.id === currentUserId.value);
};

// 格式化时间
const formatTime = (date) => {
  if (!date) return '';
  
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
    'merchant': '校外商户',
    'organization': '认证团体',
    'society': '社会用户',
    'admin': '管理员'
  };
  return tagMap[tagCode] || '';
};

// 查看用户主页
const viewUserProfile = (userId) => {
  router.push(`/user/${userId}`);
};

// 跳转登录
const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

/* 评论输入框 */
.comment-input-box {
  margin-bottom: 25px;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.3s;
}

.comment-textarea:focus {
  outline: none;
  border-color: #4A90E2;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.char-count {
  font-size: 12px;
  color: #999;
}

.submit-btn {
  padding: 8px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 未登录提示 */
.login-hint {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.login-hint p {
  color: #666;
  margin-bottom: 15px;
}

.login-btn {
  padding: 8px 24px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.login-btn:hover {
  background: #357ABD;
}

/* 评论列表 */
.comments-list {
  min-height: 100px;
}

.empty-comments {
  text-align: center;
  padding: 40px;
  color: #999;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item:hover {
  background: #fafafa;
  margin: 0 -10px;
  padding: 15px 10px;
  border-radius: 8px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.3s;
}

.comment-avatar:hover {
  transform: scale(1.1);
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.comment-author {
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.comment-author:hover {
  color: #4A90E2;
}

.identity-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #e3f2fd;
  color: #1976d2;
}

.identity-tag.student {
  background: #e8f5e9;
  color: #388e3c;
}

.identity-tag.merchant {
  background: #fff3e0;
  color: #f57c00;
}

.identity-tag.organization {
  background: #f3e5f5;
  color: #7b1fa2;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.comment-text {
  color: #333;
  line-height: 1.6;
  word-break: break-word;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  padding: 4px 12px;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  border-color: #4A90E2;
  color: #4A90E2;
  background: #f0f7ff;
}

.like-btn:hover {
  border-color: #ff4444;
  color: #ff4444;
  background: #fff0f0;
}

.delete-btn:hover {
  border-color: #ff4444;
  color: #ff4444;
  background: #fff0f0;
}
</style>
