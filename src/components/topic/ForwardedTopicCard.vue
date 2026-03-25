<template>
  <div v-if="forwardedTopic || forwardedLocation || loading" class="forwarded-topic-card" :class="locationType" @click="handleClick">
    <!-- 加载中 -->
    <div v-if="loading && !forwardedTopic && !forwardedLocation" class="loading-forwarded">
      <span class="loading-icon">🔄</span>
      <span class="loading-text">正在加载原内容...</span>
    </div>
    
    <!-- 已加载内容：话题类型 -->
    <div v-else-if="forwardedTopic && locationType === 'topic'">
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
        <span class="forwarded-hint" v-if="!isDeleted">📖 点击查看原话题</span>
        <span class="forwarded-hint deleted" v-else>原话题已删除</span>
      </div>
    </div>
    
    <!-- 已加载内容：地点类型 -->
    <div v-else-if="forwardedLocation">
      <div class="forwarded-header">
        <span class="forwarded-icon">📍</span>
        <span class="forwarded-label">分享地点</span>
        <span class="forwarded-name">
          {{ forwardedLocation.name }}
        </span>
      </div>
      
      <div class="forwarded-content">
        <div v-if="forwardedLocation.description" class="location-description">
          {{ forwardedLocation.description }}
        </div>
        <div v-if="forwardedLocation.category" class="location-detail">
          <span>📍 类型：{{ forwardedLocation.category }}</span>
        </div>
      </div>
      
      <div class="forwarded-footer">
        <span class="forwarded-hint">🗺️ 点击查看地点详情</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { topicAPI } from '@/api/topic';
import { campusAPI } from '@/api/campus';

const props = defineProps({
  forwardedFromTopicId: {
    type: [Number, String],
    required: true
  }
});

const emit = defineEmits(['click']);

const forwardedTopic = ref(null);
const forwardedLocation = ref(null);
const locationType = ref(null); // 'topic' or 'location'
const loading = ref(false);
const isDeleted = ref(false); // 标记原话题是否已删除

// 智能加载：先尝试作为话题加载，失败则尝试作为地点加载
const loadContent = async () => {
  loading.value = true;
  const sourceId = props.forwardedFromTopicId;
  
  try {
    // 第一步：尝试作为话题加载
    try {
      const response = await topicAPI.getTopicDetail(sourceId, { 
        noGlobalError: true 
      });
      
      if (response && response.id) {
        forwardedTopic.value = {
          id: response.id,
          authorName: response.author?.username || response.author?.realName || '匿名用户',
          content: response.content?.substring(0, 100) || '内容已删除',
          createdAt: response.createdAt
        };
        locationType.value = 'topic';
        isDeleted.value = false;
        loading.value = false;
        return;
      }
    } catch (topicError) {
      // 话题加载失败，尝试作为地点加载
    }
    
    // 第二步：尝试作为地点加载
    try {
      const response = await campusAPI.getLocationDetail(sourceId, { noGlobalError: true });
      
      if (response && response.id) {
        forwardedLocation.value = {
          id: response.id,
          name: response.name || '未知地点',
          description: response.description || '暂无描述',
          category: response.category
        };
        locationType.value = 'location';
      } else {
        throw new Error('响应数据缺少 id 字段');
      }
    } catch (locationError) {
      throw locationError;
    }
  } catch (error) {
    // 如果都加载失败，显示错误信息
    forwardedTopic.value = {
      id: sourceId,
      authorName: '未知用户',
      content: '⚠️ 该内容已被删除或无法查看',
      createdAt: new Date().toISOString()
    };
    locationType.value = 'topic';
    isDeleted.value = true;
  } finally {
    loading.value = false;
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

const handleClick = () => {
  const targetId = forwardedTopic.value?.id || forwardedLocation.value?.id;
  if (targetId) {
    emit('click', targetId);
  }
};

onMounted(() => {
  loadContent();
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

/* 地点类型样式 */
.forwarded-topic-card.location {
  background: linear-gradient(135deg, #f0fff4 0%, #e6f7e9 100%);
  border-left: 4px solid #28a745;
}

.forwarded-topic-card.location:hover {
  background: linear-gradient(135deg, #e6f7e9 0%, #d4edda 100%);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.15);
}

.forwarded-topic-card.location .forwarded-name {
  color: #28a745;
  font-weight: 600;
}

.location-description {
  margin-bottom: 8px;
}

.location-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #666;
  font-size: 0.9rem;
}
</style>
