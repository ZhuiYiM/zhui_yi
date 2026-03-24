<template>
  <div class="topic-card" @click="$emit('click', post)">
    <div class="post-header">
      <div class="author-info">
        <img 
          :src="getAvatarUrl(post.author)" 
          :alt="post.author.name || post.author.username" 
          class="author-avatar"
          @click.stop="$emit('view-user', post.author.id)"
          @mouseenter="handleAvatarHover($event, post.author.id)"
          @mouseleave="$emit('hide-popover')"
        >
        <div class="author-details">
          <span class="author-name" @click.stop="$emit('view-user', post.author.id)" @mouseenter="handleAvatarHover($event, post.author.id)" @mouseleave="$emit('hide-popover')">
            {{ post.author.name }}
          </span>
          <div class="author-meta">
            <span v-if="post.author.identityTag" class="identity-tag" :class="post.author.identityTag">
              {{ getIdentityTagName(post.author.identityTag) }}
            </span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div class="post-actions">
        <button @click.stop="handleLike" class="action-btn like-btn" :class="{ liked: isLiked }" :disabled="isOperating">
          👍 {{ likesCount }}
        </button>
        <button @click.stop="$emit('comment', post.id)" class="action-btn">
          💬 {{ post.comments }}
        </button>
      </div>
    </div>

    <div class="post-content">
      <p>{{ post.content }}</p>

      <!-- 被转发的话题引用（使用新组件） -->
      <ForwardedTopicCard
        v-if="post.isForwarded && post.forwardedFromTopicId"
        :forwarded-from-topic-id="post.forwardedFromTopicId"
        @click="(topicId) => emit('view-forwarded-topic', topicId)"
      />
      
      <!-- 分享的商品引用（使用新组件） -->
      <ForwardedProductCard
        v-if="post.isForwarded && post.forwardedFromProductId"
        :forwarded-from-product-id="post.forwardedFromProductId"
        @click="(productId) => emit('view-forwarded-product', productId)"
      />

      <!-- 图片展示 -->
      <div v-if="post.images && post.images.length > 0" class="post-images">
        <img
          v-for="(image, index) in post.images.slice(0, 4)"
          :key="index"
          :src="getImageUrl(image)"
          :alt="`图片${index + 1}`"
          class="post-image"
          @click.stop="$emit('preview-image', image)"
        >
        <div
          v-if="post.images.length > 4"
          class="more-images"
          @click.stop="$emit('preview-all-images', post.images)"
        >
          +{{ post.images.length - 4 }}
        </div>
      </div>
      
      <!-- 调试信息：如果没有图片但应该有 -->
      <div v-else-if="!post.images && post.imageCount > 0" class="debug-info" style="color: #999; font-size: 12px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
        ⚠️ 数据异常：imageCount={{ post.imageCount }} 但 images 数组为空
        <br>完整数据：{{ JSON.stringify(post, null, 2) }}
      </div>

      <!-- 标签 -->
      <div class="topic-tags-container">
        <!-- 身份标签（话题本身的身份标签） -->
        <span v-if="post.identityTag" class="topic-tag level1-tag">
          {{ getIdentityTagName(post.identityTag) }}
        </span>
        
        <!-- 话题标签（最多 3 个） -->
        <span
          v-for="(tag, index) in (Array.isArray(post.topicTags) ? post.topicTags : [post.topicTags]).filter(t => t).slice(0, 3)"
          :key="'topic-' + index"
          class="topic-tag level2-tag"
        >
          {{ typeof tag === 'string' ? tag : (tag.name || tag.code || '') }}
        </span>
        
        <!-- 地点标签（最多 3 个） -->
        <span
          v-for="(tag, index) in (Array.isArray(post.locationTags) ? post.locationTags : [post.locationTags]).filter(t => t).slice(0, 3)"
          :key="'location-' + index"
          class="topic-tag level3-tag"
        >
          📍{{ typeof tag === 'string' ? tag : (tag.name || tag.code || '') }}
        </span>
        
        <!-- 兼容后端返回的 tags 数组 -->
        <template v-if="(!post.topicTags || post.topicTags.length === 0) && (!post.locationTags || post.locationTags.length === 0) && post.tags && post.tags.length > 0">
          <span
            v-for="(tag, index) in post.tags.slice(0, 3)"
            :key="'tag-' + index"
            class="topic-tag simple-tag"
          >
            #{{ tag }}
          </span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { topicAPI } from '@/api/topic';
import { ElMessage } from 'element-plus';
import { API_CONFIG } from '@/config/api';
import ForwardedTopicCard from './ForwardedTopicCard.vue';
import ForwardedProductCard from './ForwardedProductCard.vue';

// Props
const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

// 调试：监听 post 变化
watch(() => props.post, (newVal) => {
  if (newVal && newVal.images) {
    console.log('🖼️ 图片数据:', newVal.images);
    console.log('🖼️ 图片数量:', newVal.images.length);
  }
}, { immediate: true, deep: true });

// Emits
const emit = defineEmits([
  'click',
  'view-user',
  'comment',
  'preview-image',
  'preview-all-images',
  'avatar-hover',
  'hide-popover',
  'like-change', // 新增：点赞状态变化事件
  'view-forwarded-topic', // 查看被转发的话题
  'view-forwarded-product' // 查看被分享的商品
]);

// 默认头像
const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

// 获取头像 URL（优先使用 avatarUrl，兼容 avatar 字段）
const getAvatarUrl = (author) => {
  if (!author) return defaultAvatar;
  const url = author.avatarUrl || author.avatar;
  if (!url || url.trim() === '') return defaultAvatar;
  return url;
};

// 获取完整的图片 URL（处理相对路径）
const getImageUrl = (imagePath) => {
  if (!imagePath) return '';
  
  // 如果已经是完整 URL（http 开头），直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }
  
  // 如果是相对路径（/images/开头），拼接后端基础地址（不要 /api）
  if (imagePath.startsWith('/images/')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
    // 去掉 /api 后缀，因为 /images 是静态资源路径
    return `${baseUrl.replace('/api', '')}${imagePath}`;
  }
  
  // 其他情况，直接返回
  return imagePath;
};

// 本地点赞状态
const isLocalLiked = ref(props.post.isLiked || false);
const localLikes = ref(props.post.likes || 0);
const isOperating = ref(false); // 防止重复点击

// 计算属性：是否已点赞
const isLiked = computed({
  get() {
    return isLocalLiked.value;
  },
  set(value) {
    isLocalLiked.value = value;
  }
});

// 点赞数量
const likesCount = computed(() => {
  return localLikes.value;
});

// 处理点赞
const handleLike = async () => {
  if (isOperating.value) return; // 防止重复点击
  
  const action = isLiked.value ? 'unlike' : 'like';
  const originalState = { liked: isLiked.value, likes: localLikes.value };
  
  try {
    isOperating.value = true;
    
    // 乐观更新 UI
    isLiked.value = !isLiked.value;
    localLikes.value += isLiked.value ? 1 : -1;
    
    // 调用 API
   const responseData = await topicAPI.likeTopic(props.post.id, action);
    
    // 成功提示
    ElMessage.success(action === 'like' ? '点赞成功' : '取消点赞');
    
    // 同步后端返回的状态
    if (responseData) {
      localLikes.value = responseData.likesCount || localLikes.value;
      isLiked.value = responseData.isLiked !== undefined ? responseData.isLiked : isLiked.value;
    }
    
    // 触发事件通知父组件（可选，用于同步其他地方的状态）
    emit('like-change', {
      topicId: props.post.id,
      isLiked: isLiked.value,
      likesCount: localLikes.value
    });
    
  } catch (error) {
   console.error('点赞操作失败:', error);
    
    // 恢复原状态
    isLiked.value = originalState.liked;
    localLikes.value = originalState.likes;
    
    // 错误处理
    if (error.response?.status === 409) {
     const msg = error.response.data?.message || (action === 'like' ? '您已点过赞了' : '您还未点过赞');
      ElMessage.warning(msg);
    } else {
      ElMessage.error(error.message || '操作失败，请稍后重试');
    }
  } finally {
    isOperating.value = false;
  }
};

// 格式化时间
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

// 获取身份标签名称
const getIdentityTagName = (tagCode) => {
  const tagMap = {
    'student': '认证学生',
    'organization': '认证团体',
    'followed': '已关注',
    'society': '社会',
    'admin': '管理员',
    'merchant': '商户'
  };
  return tagMap[tagCode] || tagCode;
};

// 处理头像悬浮
const handleAvatarHover= (event, userId) => {
  emit('avatar-hover', event, userId);
};
</script>

<style scoped>
/* 话题卡片 */
.topic-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.12);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e3f2fd;
  cursor: pointer;
  transition: opacity 0.3s;
}

.author-avatar:hover {
  opacity: 0.8;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.author-name:hover {
  opacity: 0.8;
}

.post-time {
  color: #999;
  font-size: 0.9rem;
}

.post-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  background: #f5f7fa;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #e3f2fd;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 点赞按钮特殊样式 - 已点赞状态 */
.action-btn.like-btn.liked {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  transform: translateY(-2px);
}

.action-btn.like-btn.liked:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff5252 0%, #e04563 100%);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.5);
}

.post-content p {
  margin: 0 0 20px 0;
  line-height: 1.6;
  color: #555;
  font-size: 1.05rem;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.post-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.post-image:hover {
  transform: scale(1.05);
}

.more-images {
  background: rgba(0,0,0,0.7);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.more-images:hover {
  background: rgba(0,0,0,0.9);
}

/* 话题标签容器 */
.topic-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
}

/* 通用标签样式 */
.topic-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
  cursor: default;
}

/* 一级标签（用户类型） */
.level1-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 二级标签（话题分类） */
.level2-tag {
  background: rgba(80, 200, 120, 0.15);
  color: #50C878;
  border: 1px solid #50C878;
}

/* 三级标签（地点） */
.level3-tag {
  background: #e0f7fa;
  color: #00acc1;
  border: 1px solid #b2ebf2;
}

/* 四级标签（自定义标签） */
.level4-tag {
  background: #fff3e0;
  color: #ff9800;
  border: 1px solid #ffe0b2;
}

/* 简单标签（兼容后端 tags 数组） */
.simple-tag {
  background: #f5f7fa;
  color: #666;
  border: 1px solid #e4e7ed;
}

</style>
