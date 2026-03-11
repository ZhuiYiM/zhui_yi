<template>
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
      <button @click="handleSubmit" class="submit-comment-btn">发表评论</button>
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
            <button @click="$emit('like-comment', comment)" class="comment-like-btn">
              👍 {{ comment.likesCount }}
            </button>
            <button @click="$emit('reply-comment', comment)" class="comment-reply-btn">
              💬 回复
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  comments: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['submit', 'like-comment', 'reply-comment']);

const newComment = ref('');
const defaultAvatar = 'https://placehold.co/100x100/CCCCCC/FFFFFF?text=默认头像';

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

// 提交评论
const handleSubmit = () => {
  if (!newComment.value.trim()) {
    emit('validation-error', '请输入评论内容');
    return;
  }
  
  emit('submit', newComment.value);
  newComment.value = '';
};
</script>

<style scoped>
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
</style>
