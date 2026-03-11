<template>
  <article class="topic-article">
    <!-- 作者信息 -->
    <header class="article-header">
      <div class="author-info">
        <img
            :src="author.avatarUrl || defaultAvatar"
            :alt="author.username"
            class="author-avatar"
            @mouseenter="showAuthorTooltip = true"
            @mouseleave="showAuthorTooltip = false"
        >
        <div class="author-details">
          <span class="author-name">{{ author.username || '匿名用户' }}</span>
          <!-- 显示身份标签 -->
          <span v-if="author.level1Tag" class="identity-tag" :class="author.level1Tag">
            {{ getIdentityTagName(author.level1Tag) }}
          </span>
          <span class="post-time">{{ formatDate(createdAt) }}</span>
        </div>
      </div>

      <!-- 作者信息悬停提示 -->
      <div v-if="showAuthorTooltip && authorPublicInfo" class="author-tooltip">
        <div class="tooltip-content">
          <img :src="authorPublicInfo.basicInfo.avatarUrl || defaultAvatar" class="tooltip-avatar">
          <div class="tooltip-info">
            <h4>{{ authorPublicInfo.basicInfo.username || '匿名用户' }}</h4>
            <p v-if="authorPublicInfo.basicInfo.college">{{ authorPublicInfo.basicInfo.college }}</p>
            <p v-if="authorPublicInfo.academicInfo.major">{{ authorPublicInfo.academicInfo.major }}</p>
            <div class="tooltip-stats">
              <span>{{ authorPublicInfo.statistics.postCount }} 话题</span>
              <span>{{ authorPublicInfo.statistics.likesReceived }} 点赞</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="header-actions" v-if="isAuthor">
        <button @click="$emit('edit')" class="edit-btn">✏️ 编辑</button>
        <button @click="$emit('delete')" class="delete-btn">🗑️ 删除</button>
      </div>
      
      <!-- 非作者用户显示举报按钮 -->
      <div class="header-actions" v-else>
        <button @click="$emit('report')" class="report-btn">⚠️ 举报</button>
      </div>
    </header>

    <!-- 话题正文 -->
    <div class="article-body">
      <p class="content-text">{{ content }}</p>

      <!-- 被转发的话题引用 -->
      <ForwardedTopicCard
        v-if="isForwarded && forwardedFromTopicId"
        :forwarded-from-topic-id="forwardedFromTopicId"
        @click="$emit('view-forwarded', forwardedFromTopicId)"
      />

      <!-- 图片展示 -->
      <div v-if="images && images.length > 0" class="content-images">
        <img
            v-for="(image, index) in images"
            :key="index"
            :src="image"
            :alt="`图片${index + 1}`"
            class="content-image"
            @click="$emit('preview-image', image)"
        >
      </div>

      <!-- 标签展示 -->
      <div v-if="hasTags" class="content-tags">
        <!-- 二级标签 -->
        <template v-if="tags.level2 && tags.level2.length > 0">
          <span
              v-for="tag in tags.level2"
              :key="'l2-' + (tag.code || tag.name || tag)"
              class="hashtag level2"
              :style="getTagStyle(tag)"
          >
            {{ tag.name || tag }}
          </span>
        </template>
        
        <!-- 三级标签 -->
        <template v-if="tags.level3 && tags.level3.length > 0">
          <span
              v-for="tag in tags.level3"
              :key="'l3-' + (tag.code || tag.name || tag)"
              class="hashtag level3"
          >
            📍{{ tag.name || tag }}
          </span>
        </template>
        
        <!-- 四级标签 -->
        <template v-if="tags.level4 && tags.level4.length > 0">
          <span
              v-for="tag in tags.level4"
              :key="'l4-' + (tag.code || tag.name || tag)"
              class="hashtag level4"
          >
            #{{ tag.name || tag }}
          </span>
        </template>
        
        <!-- 兼容简单数组格式的标签 -->
        <template v-if="Array.isArray(tags) && tags.length > 0">
          <span
              v-for="(tag, index) in tags"
              :key="'tag-' + index"
              class="hashtag simple"
          >
            #{{ tag.name || tag }}
          </span>
        </template>
      </div>
    </div>

    <!-- 统计信息 -->
    <footer class="article-footer">
      <div class="statistics">
        <span class="stat-item">👁️ {{ statistics.viewsCount }} 浏览</span>
        <span class="stat-item">👍 {{ statistics.likesCount }} 点赞</span>
        <span class="stat-item">💬 {{ statistics.commentsCount }} 评论</span>
        <span class="stat-item">⭐ {{ statistics.collectionsCount }} 收藏</span>
      </div>

      <!-- 互动按钮 -->
      <div class="interaction-buttons">
        <button
            @click="$emit('like')"
            class="interact-btn like"
            :class="{ active: interactions.isLiked }"
        >
          {{ interactions.isLiked ? '❤️' : '🤍' }} 点赞
        </button>
        <button
            @click="$emit('collect')"
            class="interact-btn collect"
            :class="{ active: interactions.isCollected }"
        >
          {{ interactions.isCollected ? '⭐' : '☆' }} 收藏
        </button>
        <button @click="$emit('share')" class="interact-btn share">
          ↗️ 分享
        </button>
      </div>
    </footer>
  </article>
</template>

<script setup>
import { ref, computed } from 'vue';
import ForwardedTopicCard from './ForwardedTopicCard.vue';

const props = defineProps({
  id: { type: [Number, String], required: true },
  content: { type: String, required: true },
  images: { type: Array, default: () => [] },
  tags: { type: Object, default: () => ({}) },
  author: { 
    type: Object, 
    required: true,
    default: () => ({
      id: null,
      username: '',
      avatarUrl: '',
      level1Tag: ''
    })
  },
  statistics: {
    type: Object,
    default: () => ({
      viewsCount: 0,
      likesCount: 0,
      commentsCount: 0,
      collectionsCount: 0
    })
  },
  interactions: {
    type: Object,
    default: () => ({
      isLiked: false,
      isCollected: false
    })
  },
  isForwarded: { type: Boolean, default: false },
  forwardedFromTopicId: { type: [Number, String], default: null },
  createdAt: { type: String, default: '' },
  authorPublicInfo: { type: Object, default: null },
  currentUserId: { type: [Number, String], default: null }
});

const emit = defineEmits(['like', 'collect', 'share', 'edit', 'delete', 'report', 'preview-image', 'view-forwarded']);

const showAuthorTooltip = ref(false);
const defaultAvatar = 'https://placehold.co/100x100/CCCCCC/FFFFFF?text=默认头像';

// 是否为作者
const isAuthor = computed(() => {
  return props.currentUserId && props.author.id && props.currentUserId === props.author.id;
});

// 是否有标签
const hasTags = computed(() => {
  if (!props.tags) {
    return false;
  }
  
  const tags = props.tags;
  return (
    (tags.level2 && tags.level2.length > 0) ||
    (tags.level3 && tags.level3.length > 0) ||
    (tags.level4 && tags.level4.length > 0) ||
    (Array.isArray(tags) && tags.length > 0)
  );
});

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

// 获取身份标签名称
const getIdentityTagName = (tagCode) => {
  const identityMap = {
    'student': '学生',
    'merchant': '商户',
    'organization': '团体',
    'individual': '个人',
    'admin': '管理员'
  };
  return identityMap[tagCode] || tagCode;
};

// 获取标签样式
const getTagStyle = (tag) => {
  if (tag && tag.color) {
    return {
      backgroundColor: tag.color + '20',
      borderColor: tag.color
    };
  }
  return {};
};
</script>

<style scoped>
.topic-article {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  position: relative;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
}

/* 身份标签 */
.identity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
  transition: all 0.3s;
}

.identity-tag.student {
  background: #e3f2fd;
  color: #1976d2;
}

.identity-tag.merchant {
  background: #fff3e0;
  color: #f57c00;
}

.identity-tag.organization {
  background: #e8f5e9;
  color: #388e3c;
}

.identity-tag.individual {
  background: #f3e5f5;
  color: #7b1fa2;
}

.identity-tag.admin {
  background: #ffebee;
  color: #d32f2f;
}

/* 作者信息悬停提示 */
.author-tooltip {
  position: absolute;
  top: 60px;
  left: 0;
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 250px;
}

.tooltip-content {
  display: flex;
  gap: 12px;
}

.tooltip-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.tooltip-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.tooltip-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}

.tooltip-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.edit-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.edit-btn {
  background: #ecf5ff;
  color: #409EFF;
}

.edit-btn:hover {
  background: #409EFF;
  color: #fff;
}

.delete-btn {
  background: #fef0f0;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #f56c6c;
  color: #fff;
}

.report-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  background: #fdf6ec;
  color: #e6a23c;
}

.report-btn:hover {
  background: #e6a23c;
  color: #fff;
}

.article-body {
  margin-bottom: 25px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.content-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.content-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.content-image:hover {
  transform: scale(1.02);
}

.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hashtag {
  background: #f0f2f5;
  color: #606266;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.hashtag:hover {
  background: #409EFF;
  color: #fff;
  transform: translateY(-2px);
}

.hashtag.level2 {
  font-weight: 500;
}

.hashtag.level3 {
  background: #ecf5ff;
  color: #409EFF;
}

.hashtag.level4 {
  background: #f4f4f5;
  color: #909399;
}

.hashtag.simple {
  background: #f0f2f5;
  color: #606266;
}

.article-footer {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.statistics {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
}

.interact-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.interact-btn:hover {
  transform: translateY(-2px);
}

.interact-btn.like.active {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.interact-btn.collect.active {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}
</style>
