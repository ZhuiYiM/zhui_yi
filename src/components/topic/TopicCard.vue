<template>
  <div class="topic-card" @click="$emit('click', post)">
    <div class="post-header">
      <div class="author-info">
        <img 
          :src="post.author.avatar || defaultAvatar" 
          :alt="post.author.name" 
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

      <!-- 被转发的话题引用 -->
      <div v-if="forwardedTopic" class="forwarded-topic-card" @click.stop="viewForwardedTopic">
        <div class="forwarded-topic-header">
          <span class="forwarded-icon">📝</span>
          <span class="forwarded-label">转发自</span>
          <span class="forwarded-author">@{{ forwardedTopic.authorName }}</span>
        </div>
        <p class="forwarded-topic-content">{{ forwardedTopic.content }}</p>
        <div class="forwarded-topic-footer">
          <span class="forwarded-topic-time">{{ formatTime(forwardedTopic.createdAt) }}</span>
          <span class="forwarded-topic-link">点击查看原话题 →</span>
        </div>
      </div>

      <!-- 图片展示 -->
      <div v-if="post.images && post.images.length > 0" class="post-images">
        <img
          v-for="(image, index) in post.images.slice(0, 4)"
          :key="index"
          :src="image"
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

      <!-- 标签 -->
      <div class="topic-tags-container">
        <!-- 一级标签（用户类型）- 始终显示 -->
        <span v-if="getPostLevel1Tag(post)" class="topic-tag level1-tag">
          {{ getIdentityTagName(getPostLevel1Tag(post)) }}
        </span>
        
        <!-- 二级标签（最多 3 个） -->
        <span
          v-for="tag in (post.level2Tags || []).slice(0, 3)"
          :key="'l2-' + (tag.code || tag.name || '')"
          class="topic-tag level2-tag"
          :style="{ backgroundColor: tag.color + '20', borderColor: tag.color }"
        >
          {{ tag.name || tag }}
        </span>
        
        <!-- 三级标签（最多 3 个） -->
        <span
          v-for="tag in (post.level3Tags || []).slice(0, 3)"
          :key="'l3-' + (tag.code || tag.name || '')"
          class="topic-tag level3-tag"
        >
          📍{{ tag.name || tag }}
        </span>
        
        <!-- 四级标签（自定义标签，最多 3 个） -->
        <span
          v-for="tag in (post.level4Tags || []).slice(0, 3)"
          :key="'l4-' + (tag.code || tag.name || '')"
          class="topic-tag level4-tag"
        >
          #{{ tag.name || tag }}
        </span>
        
        <!-- 兼容后端返回的 tags 数组 -->
        <template v-if="(!post.level2Tags || post.level2Tags.length === 0) && (!post.level3Tags || post.level3Tags.length === 0) && (!post.level4Tags || post.level4Tags.length === 0) && post.tags && post.tags.length > 0">
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
import { computed, ref } from 'vue';
import { topicAPI } from '@/api/topic';
import { ElMessage } from 'element-plus';

// Props
const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

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
  'view-forwarded-topic' // 查看被转发的话题
]);

// 默认头像
const defaultAvatar = 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U';

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
    'society': '社会'
  };
 return tagMap[tagCode] || '';
};

// 获取帖子的一级标签（用户类型）
const getPostLevel1Tag = (post) => {
  // 优先从 author 对象中读取
  if (post.author?.level1Tag) {
    return post.author.level1Tag;
  }
  // 兼容旧的 identityTag 字段
  if (post.author?.identityTag) {
    return post.author.identityTag;
  }
  // 最后从 post 本身读取
 return post.level1Tag || null;
};

// 处理头像悬浮
const handleAvatarHover= (event, userId) => {
  emit('avatar-hover', event, userId);
};

// 解析被转发的话题信息
const forwardedTopic = computed(() => {
  if (!props.post || !props.post.content) {
    return null;
  }
  
  // 检查是否包含分享标记
  const content = props.post.content;
  if (!content.includes('【分享自话题】')) {
    return null;
  }
  
  console.log('🔍 [TopicCard] 检测到转发内容，开始解析...');
  console.log('📝 话题内容:', content);
  console.log('🏷️ 四级标签:', props.post.level4Tags);
  
  // 提取作者名（改进正则，匹配到冒号为止）
  const authorMatch = content.match(/@([^:：]+)/);
  const authorName = authorMatch ? authorMatch[1].trim() : '匿名用户';
  
  // 提取原话题内容（第一行）
  const lines = content.split('\n');
  let originalContent = '';
  for (const line of lines) {
    if (line.includes('【分享自话题】') && line.includes('@')) {
      // 匹配 @xxx:后面的内容
      const contentMatch = line.match(/@[\u4e00-\u9fa5a-zA-Z0-9_]+[:：](.+)$/);
      if (contentMatch) {
        originalContent = contentMatch[1].trim();
        break;
      }
    }
  }
  
  // 尝试从四级标签中查找原话题 ID（如果有 share 标签且包含 topicId）
  let topicId = null;
  if (props.post.level4Tags && Array.isArray(props.post.level4Tags)) {
    console.log('🔍 检查四级标签:', props.post.level4Tags);
    const shareTag = props.post.level4Tags.find(tag => tag.code === 'share' || tag.name === 'share');
    if (shareTag) {
      console.log('✅ 找到分享标签:', shareTag);
      if (shareTag.topicId) {
        topicId = shareTag.topicId;
        console.log('🎯 从标签中提取到 topicId:', topicId);
      } else {
        console.warn('⚠️ 分享标签没有 topicId 字段');
      }
    } else {
      console.warn('⚠️ 在 level4Tags 中未找到 share 标签');
    }
  } else {
    console.warn('⚠️ tags.level4Tags 不是数组或不存在');
  }
  
  // 方式 2: 如果标签中没有 topicId，尝试从内容中提取
  if (!topicId) {
    console.log('🔍 [TopicCard] 尝试从内容中提取话题 ID...');
    console.log('📝 [TopicCard] 完整内容:', content);
    const forwardMatch = content.match(/<!--FORWARD_FROM:(\d+)-->/);
    if (forwardMatch) {
      topicId = parseInt(forwardMatch[1]);
      console.log('🎯 [TopicCard] 从内容标记中提取到 topicId:', topicId);
    } else {
      // 使用当前帖子 ID 作为备选
      topicId = props.post.id;
      console.log('⚠️ [TopicCard] 未找到 FORWARD_FROM 标记，使用当前帖子 ID 作为备选:', topicId);
      console.log('⚠️ [TopicCard] 请检查后端返回的内容是否包含 <!--FORWARD_FROM:ID--> 格式');
    }
  }
  
  const result = {
    id: parseInt(topicId),
    authorName,
    content: originalContent,
    createdAt: props.post.forwardedFromCreatedAt || props.post.createdAt || new Date().toISOString()
  };
  
  console.log('✅ 解析结果:', result);
  return result;
});

// 查看被转发的话题
const viewForwardedTopic = () => {
  console.log('🖱️ [TopicCard] 点击被转发话题卡片');
  console.log('forwardedTopic:', forwardedTopic.value);
  
  if (forwardedTopic.value && forwardedTopic.value.id) {
    const targetId = forwardedTopic.value.id;
    console.log('🚀 准备跳转到话题 ID:', targetId);
    console.log('当前帖子 ID:', props.post.id);
    
    // 检查是否是当前帖子本身（避免死循环）
    if (targetId.toString() === props.post.id.toString()) {
      console.warn('⚠️ 目标话题就是当前话题，无需跳转');
      // 不弹出提示，直接返回
      return;
    }
    
    emit('view-forwarded-topic', targetId);
    console.log('✅ 已触发跳转事件');
  } else {
    console.error('❌ 无法跳转：forwardedTopic 或 id 不存在');
  }
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

/* 被转发的话题卡片 */
.forwarded-topic-card {
  margin: 15px 0;
  padding: 15px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border-left: 4px solid #4A90E2;
  cursor: pointer;
  transition: all 0.3s;
}

.forwarded-topic-card:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.2);
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
}

.forwarded-topic-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #666;
}

.forwarded-icon {
  font-size: 16px;
}

.forwarded-label {
  font-weight: 500;
}

.forwarded-author {
  color: #4A90E2;
  font-weight: 600;
}

.forwarded-topic-content {
  margin: 10px 0;
  padding: 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.forwarded-topic-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.forwarded-topic-time {
  font-style: italic;
}

.forwarded-topic-link {
  color: #4A90E2;
  font-weight: 600;
  transition: color 0.3s;
}

.forwarded-topic-link:hover {
  color: #357ABD;
}
</style>
