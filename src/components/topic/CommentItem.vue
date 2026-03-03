<template>
  <div class="comment-item" :class="{ 'is-hot': comment.isHot }">
    <div class="comment-header">
      <img 
        :src="comment.author.avatarUrl || defaultAvatar" 
        :alt="comment.author.username"
        class="comment-avatar"
        @click="$emit('user-click', comment.author.id)"
      >
      <div class="comment-meta">
        <div class="author-row">
          <span 
            class="author-name"
            @click="$emit('user-click', comment.author.id)"
          >
            {{ comment.author.realName || comment.author.username }}
          </span>
          <span v-if="comment.isHot" class="hot-tag">🔥 热评</span>
        </div>
        <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
      </div>
    </div>
    
    <div class="comment-content">
      {{ comment.content }}
    </div>
    
    <div class="comment-actions">
      <button 
        @click="toggleLike"
        class="action-btn"
        :class="{ liked: isLiked }"
      >
        👍 {{ comment.likeCount || 0 }}
      </button>
      <button @click="replyComment" class="action-btn">
        💬 回复
      </button>
      <button @click="reportComment" class="action-btn">
        ⚠️ 举报
      </button>
    </div>
    
    <!-- 回复列表 (暂不实现) -->
    <div v-if="comment.replies && comment.replies.length > 0" class="replies-section">
      <div class="reply-count">
        {{ comment.replies.length }} 条回复
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['user-click']);

const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';
const isLiked = ref(false);

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

// 点赞评论
const toggleLike = () => {
  // TODO: 调用后端接口
  isLiked.value = !isLiked.value;
  ElMessage.info('点赞功能开发中...');
};

// 回复评论
const replyComment = () => {
  ElMessage.info('回复功能开发中...');
};

// 举报评论
const reportComment = () => {
  ElMessage.warning('举报功能开发中...');
};
</script>

<style scoped>
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.is-hot {
    background: #fff7e6;
    padding: 16px;
    margin: 0 -16px 16px;
    border-radius: 8px;
    border-left: 3px solid #e6a23c;
  }
}

.comment-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.3s;
  
  &:hover {
    opacity: 0.8;
  }
}

.comment-meta {
  flex: 1;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  
  &:hover {
    color: #409eff;
  }
}

.hot-tag {
  font-size: 12px;
  padding: 2px 6px;
  background: #f56c6c;
  color: white;
  border-radius: 4px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
  font-size: 14px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  color: #606266;
  cursor: pointer;
  font-size: 13px;
  border-radius: 4px;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
  
  &.liked {
    color: #409eff;
  }
}

.replies-section {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.reply-count {
  font-size: 13px;
  color: #999;
}
</style>
