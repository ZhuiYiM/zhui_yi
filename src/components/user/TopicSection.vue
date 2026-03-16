<template>
  <div class="topics-section">
    <!-- 话题标签 -->
    <div class="topic-tabs">
      <button 
        @click="$emit('tab-change', 'published')"
        class="tab-btn"
        :class="{ active: currentTab === 'published' }"
      >
        发布的话题
      </button>
      <button 
        @click="$emit('tab-change', 'participated')"
        class="tab-btn"
        :class="{ active: currentTab === 'participated' }"
      >
        参与的话题
      </button>
      <button 
        @click="$emit('tab-change', 'liked')"
        class="tab-btn"
        :class="{ active: currentTab === 'liked' }"
      >
        点赞的话题
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner-small"></div>
      <span>加载中...</span>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="topics.length === 0" class="empty-topics">
      <div class="empty-icon">💭</div>
      <p>{{ 
        currentTab === 'published' ? '暂无发布的话题' : 
        currentTab === 'liked' ? '暂无点赞的话题' : 
        '暂无参与的话题' 
      }}</p>
    </div>
    
    <!-- 话题列表 -->
    <div v-else class="user-topics-list">
      <div
        v-for="topic in topics"
        :key="topic.id"
        class="topic-item"
        @click="$emit('view-topic', topic)"
      >
        <div class="topic-content">
          <h4 class="topic-title">{{ topic.title || topic.content.substring(0, 50) }}</h4>
          <p class="topic-excerpt">{{ topic.content.substring(0, 100) }}...</p>
          <div class="topic-meta">
            <span class="topic-time">{{ formatTime(topic.createdAt) }}</span>
            <span class="topic-stats">
              👍 {{ topic.likesCount || 0 }} 💬 {{ topic.commentsCount || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  topics: {
    type: Array,
    default: () => []
  },
  currentTab: {
    type: String,
    default: 'published'
  },
  loading: {
    type: Boolean,
    default: false
  }
});

defineEmits(['tab-change', 'view-topic']);

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
</script>

<style scoped>
.topics-section {
  padding: 30px 40px;
}

.topic-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0;
}

.tab-btn {
  padding: 12px 24px;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #409eff;
}

.tab-btn.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 600;
}

.user-topics-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-item {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.topic-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.topic-content {
  .topic-title {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #333;
    font-weight: 600;
    line-height: 1.5;
  }
  
  .topic-excerpt {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #666;
    line-height: 1.6;
  }
  
  .topic-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .topic-time {
      font-size: 13px;
      color: #999;
    }
    
    .topic-stats {
      font-size: 13px;
      color: #999;
    }
  }
}

.loading-state {
  text-align: center;
  padding: 40px;
}

.spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-topics {
  text-align: center;
  padding: 60px 20px;
}

.empty-topics .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-topics p {
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .topics-section {
    padding: 20px;
  }
}
</style>
