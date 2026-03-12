<template>
  <div v-if="forwardedTopic || loading" class="forwarded-topic-card" @click="handleClick">
    <!-- 加载中 -->
    <div v-if="loading && !forwardedTopic" class="loading-forwarded">
      <span class="loading-icon">🔄</span>
      <span class="loading-text">正在加载原话题...</span>
    </div>
    
    <!-- 已加载内容 -->
    <div v-else-if="forwardedTopic">
      <div class="forwarded-header">
        <span class="forwarded-icon">🔁</span>
        <span class="forwarded-label">转发自</span>
        <span class="forwarded-author" :class="{ 'deleted': isDeleted }">
          @{{ forwardedTopic.authorName }}
        </span>
      </div>
      
      <div class="forwarded-content" :class="{ 'deleted': isDeleted }">
        {{ forwardedTopic.content }}
      </div>
      
      <div class="forwarded-footer">
        <span class="forwarded-time">{{ formatDate(forwardedTopic.createdAt) }}</span>
        <span class="forwarded-hint" v-if="!isDeleted">点击查看原话题</span>
        <span class="forwarded-hint deleted" v-else>原话题已删除</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { topicAPI } from '@/api/topic';

const props = defineProps({
  forwardedFromTopicId: {
    type: [Number, String],
    required: true
  }
});

const emit = defineEmits(['click']);

const forwardedTopic = ref(null);
const loading = ref(false);
const isDeleted = ref(false); // 标记原话题是否已删除

// 加载被转发话题信息
const loadForwardedTopic = async () => {
  loading.value = true;
  try {
    // 使用自定义配置，不在拦截器中显示 404 错误
    const response = await topicAPI.getTopicDetail(props.forwardedFromTopicId, { 
      noGlobalError: true 
    });
    
    // ✅ request.js 拦截器已经返回了 responseData.data，直接使用 response
    const topicData = response;
    
    if (topicData && topicData.id) {
      forwardedTopic.value = {
        id: topicData.id,
        authorName: topicData.author?.username || topicData.author?.realName || '匿名用户',
        content: topicData.content?.substring(0, 100) || '内容已删除',
        createdAt: topicData.createdAt
      };
      isDeleted.value = false;
    } else {
      throw new Error('响应数据缺少 id 字段');
    }
  } catch (error) {
    // 如果加载失败，使用默认信息
    forwardedTopic.value = {
      id: props.forwardedFromTopicId,
      authorName: '未知用户',
      content: '⚠️ 该话题已被删除或无法查看',
      createdAt: new Date().toISOString()
    };
    isDeleted.value = true;
  } finally {
    loading.value = false;
  }
};

const handleClick = () => {
  if (forwardedTopic.value && forwardedTopic.value.id) {
    emit('click', forwardedTopic.value.id);
  }
};

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

onMounted(() => {
  loadForwardedTopic();
});
</script>

<style scoped>
.forwarded-topic-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-left: 4px solid #4A90E2;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
  cursor: pointer;
  transition: all 0.3s;
}

/* 加载状态 */
.loading-forwarded {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  color: #999;
  font-size: 0.9rem;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.forwarded-topic-card:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.15);
}

.forwarded-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.forwarded-icon {
  font-size: 1.2rem;
}

.forwarded-label {
  color: #666;
  font-weight: 500;
}

.forwarded-author {
  color: #4A90E2;
  font-weight: 600;
}

.forwarded-author.deleted {
  color: #999;
  text-decoration: line-through;
}

.forwarded-content {
  color: #555;
  line-height: 1.6;
  font-size: 0.95rem;
  margin-bottom: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.forwarded-content.deleted {
  color: #999;
  background: rgba(0, 0, 0, 0.03);
  font-style: italic;
}

.forwarded-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: #999;
}

.forwarded-time {
  color: #999;
}

.forwarded-hint {
  color: #4A90E2;
  font-style: italic;
}

.forwarded-hint.deleted {
  color: #999;
  font-style: italic;
  font-size: 0.8rem;
}
</style>
