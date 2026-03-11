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

        <!-- 话题内容 -->
        <article class="topic-article">
          <!-- 作者信息 -->
          <header class="article-header">
            <div class="author-info">
              <img
                  :src="topic.author.avatarUrl || defaultAvatar"
                  :alt="topic.author.username"
                  class="author-avatar"
                  @mouseenter="showAuthorTooltip = true"
                  @mouseleave="showAuthorTooltip = false"
              >
              <div class="author-details">
                <span class="author-name">{{ topic.author.username || '匿名用户' }}</span>
                <!-- 显示身份标签 -->
                <span v-if="topic.author.level1Tag" class="identity-tag" :class="topic.author.level1Tag">
                  {{ getIdentityTagName(topic.author.level1Tag) }}
                </span>
                <span class="post-time">{{ formatDate(topic.createdAt) }}</span>
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
              <button @click="editTopic" class="edit-btn">✏️ 编辑</button>
              <button @click="deleteTopic" class="delete-btn">🗑️ 删除</button>
            </div>
            
            <!-- 非作者用户显示举报按钮 -->
            <div class="header-actions" v-else>
              <button @click="reportTopic" class="report-btn">⚠️ 举报</button>
            </div>
          </header>

          <!-- 话题正文 -->
          <div class="article-body">
            <p class="content-text">{{ topic.content }}</p>

            <!-- 被转发的话题引用 -->
            <div v-if="forwardedTopic" class="forwarded-topic-card" @click="viewForwardedTopic">
              <div class="forwarded-topic-header">
                <span class="forwarded-icon">📝</span>
                <span class="forwarded-label">转发自</span>
                <span class="forwarded-author">@{{ forwardedTopic.authorName }}</span>
              </div>
              <p class="forwarded-topic-content">{{ forwardedTopic.content }}</p>
              <div class="forwarded-topic-footer">
                <span class="forwarded-topic-time">{{ formatDate(forwardedTopic.createdAt) }}</span>
                <span class="forwarded-topic-link" @click.stop="viewForwardedTopic">点击查看原话题 →</span>
              </div>
            </div>

            <!-- 图片展示 -->
            <div v-if="topic.images && topic.images.length > 0" class="content-images">
              <img
                  v-for="(image, index) in topic.images"
                  :key="index"
                  :src="image"
                  :alt="`图片${index + 1}`"
                  class="content-image"
                  @click="previewImage(image)"
              >
            </div>

            <!-- 标签展示 -->
            <div v-if="hasTags" class="content-tags">
              <!-- 二级标签 -->
              <template v-if="topic.tags.level2 && topic.tags.level2.length > 0">
                <span
                    v-for="tag in topic.tags.level2"
                    :key="'l2-' + (tag.code || tag.name || tag)"
                    class="hashtag level2"
                    :style="getTagStyle(tag)"
                >
                  {{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 三级标签 -->
              <template v-if="topic.tags.level3 && topic.tags.level3.length > 0">
                <span
                    v-for="tag in topic.tags.level3"
                    :key="'l3-' + (tag.code || tag.name || tag)"
                    class="hashtag level3"
                >
                  📍{{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 四级标签 -->
              <template v-if="topic.tags.level4 && topic.tags.level4.length > 0">
                <span
                    v-for="tag in topic.tags.level4"
                    :key="'l4-' + (tag.code || tag.name || tag)"
                    class="hashtag level4"
                >
                  #{{ tag.name || tag }}
                </span>
              </template>
              
              <!-- 兼容简单数组格式的标签 -->
              <template v-if="Array.isArray(topic.tags) && topic.tags.length > 0">
                <span
                    v-for="(tag, index) in topic.tags"
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
              <span class="stat-item">👁️ {{ topic.statistics.viewsCount }} 浏览</span>
              <span class="stat-item">👍 {{ topic.statistics.likesCount }} 点赞</span>
              <span class="stat-item">💬 {{ topic.statistics.commentsCount }} 评论</span>
              <span class="stat-item">⭐ {{ topic.statistics.collectionsCount }} 收藏</span>
            </div>

            <!-- 互动按钮 -->
            <div class="interaction-buttons">
              <button
                  @click="toggleLike"
                  class="interact-btn like"
                  :class="{ active: topic.interactions.isLiked }"
              >
                {{ topic.interactions.isLiked ? '❤️' : '🤍' }} 点赞
              </button>
              <button
                  @click="toggleCollect"
                  class="interact-btn collect"
                  :class="{ active: topic.interactions.isCollected }"
              >
                {{ topic.interactions.isCollected ? '⭐' : '☆' }} 收藏
              </button>
              <button @click="shareTopic" class="interact-btn share">
                ↗️ 分享
              </button>
            </div>
          </footer>
        </article>

        <!-- 评论区 -->
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
            <button @click="submitComment" class="submit-comment-btn">发表评论</button>
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
                  <button @click="likeComment(comment)" class="comment-like-btn">
                    👍 {{ comment.likesCount }}
                  </button>
                  <button @click="replyComment(comment)" class="comment-reply-btn">
                    💬 回复
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !topic" class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>话题不存在</h3>
        <p>该话题可能已被删除或不存在</p>
        <button @click="goBack" class="primary-btn">返回话题墙</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '../common/UnifiedNav.vue';
import { topicAPI } from '@/api/topic';

const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(true);
const topic = ref(null);
const comments = ref([]);
const newComment = ref('');
const isMobile = ref(window.innerWidth <= 768);
const showAuthorTooltip = ref(false);
const authorPublicInfo = ref(null);
const currentUser = ref(null);

// 计算属性
const isAuthor = computed(() => {
  return currentUser.value && topic.value && currentUser.value.id === topic.value.author.id;
});

// 是否有标签
const hasTags = computed(() => {
  if (!topic.value || !topic.value.tags) {
    return false;
  }
  
  const tags = topic.value.tags;
  // 检查各种可能的标签格式
  return (
    (tags.level2 && tags.level2.length > 0) ||
    (tags.level3 && tags.level3.length > 0) ||
    (tags.level4 && tags.level4.length > 0) ||
    // 兼容简单数组格式
    (Array.isArray(tags) && tags.length > 0)
  );
});

// 解析被转发的话题信息
const forwardedTopic = computed(() => {
  console.log('🔍 [forwardedTopic] 开始计算...');
  console.log('🔍 topic.value:', topic.value);
  
  if (!topic.value || !topic.value.content) {
    console.log('⚠️ topic 或 content 不存在');
    return null;
  }
  
  const content = topic.value.content;
  console.log('📝 检查内容是否包含转发标记:', content.substring(0, 50));
  
  if (!content.includes('【分享自话题】')) {
    console.log('⚠️ 内容不包含【分享自话题】标记');
    return null;
  }
  
  console.log('✅ 检测到转发内容，开始解析...');
  console.log('📝 完整话题内容:', content);
  console.log('🏷️ 话题标签:', topic.value.tags);
  console.log('🏷️ level4Tags:', topic.value.level4Tags);
  console.log('🏷️ tagsObject:', topic.value.tagsObject);
  console.log('🏷️ 标签类型:', typeof topic.value.tags);
  console.log('🏷️ 标签是否为数组:', Array.isArray(topic.value.tags));
  
  // 提取作者名（改进正则，匹配到冒号为止）
  const authorMatch = content.match(/@([^:：]+)/);
  const authorName = authorMatch ? authorMatch[1].trim() : '匿名用户';
  console.log('👤 提取到作者:', authorName);
  
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
  console.log('📄 提取到原内容:', originalContent ? originalContent.substring(0, 50) + '...' : '(空)');
  
  // 尝试从四级标签中查找原话题 ID（支持多种数据结构）
  let topicId = null;
  
  // 方式 1: 检查 tagsObject.level4Tags
  if (topic.value.tagsObject && Array.isArray(topic.value.tagsObject.level4Tags)) {
    console.log('🔍 检查 tagsObject.level4Tags:', topic.value.tagsObject.level4Tags);
    const shareTag = topic.value.tagsObject.level4Tags.find(tag => tag.code === 'share' || tag.name === '分享');
    if (shareTag) {
      console.log('✅ 找到分享标签:', shareTag);
      if (shareTag.topicId) {
        topicId = shareTag.topicId;
        console.log('🎯 从 tagsObject.level4Tags 中提取到 topicId:', topicId);
      } else {
        console.warn('⚠️ 分享标签没有 topicId 字段');
      }
    } else {
      console.warn('⚠️ 在 tagsObject.level4Tags 中未找到 share 标签');
    }
  } else {
    console.warn('⚠️ tagsObject.level4Tags 不是数组或不存在');
  }
  
  // 方式 2: 检查 level4Tags 数组格式
  if (!topicId && topic.value.level4Tags && Array.isArray(topic.value.level4Tags)) {
    console.log('🔍 检查 level4Tags:', topic.value.level4Tags);
    // 这里 level4Tags 可能是字符串数组，需要特殊处理
    if (topic.value.level4Tags.includes('share')) {
      console.log('✅ 发现 share 标签，但没有 topicId 信息');
    }
  }
  
  // 方式 3: 检查 tags 是否为对象且有 level4Tags 属性
  if (!topicId && topic.value.tags && !Array.isArray(topic.value.tags) && topic.value.tags.level4Tags) {
    console.log('🔍 检查 tags.level4Tags:', topic.value.tags.level4Tags);
    if (Array.isArray(topic.value.tags.level4Tags)) {
      const shareTag = topic.value.tags.level4Tags.find(tag => tag.code === 'share' || tag.name === 'share');
      if (shareTag) {
        console.log('✅ 找到分享标签:', shareTag);
        if (shareTag.topicId) {
          topicId = shareTag.topicId;
          console.log('🎯 从 tags.level4Tags 中提取到 topicId:', topicId);
        } else {
          console.warn('⚠️ 分享标签没有 topicId 字段');
        }
      } else {
        console.warn('⚠️ 在 tags.level4Tags 中未找到 share 标签');
      }
    } else {
      console.warn('⚠️ level4Tags 不是数组');
    }
  } else if (!topicId) {
    console.warn('⚠️ tags 不是对象或没有 level4Tags 属性');
  }
  
  // 方式 4: 检查 tags 是否为对象且有 level4 属性
  if (!topicId && topic.value.tags && !Array.isArray(topic.value.tags) && topic.value.tags.level4) {
    console.log('🔍 检查 tags.level4:', topic.value.tags.level4);
    if (Array.isArray(topic.value.tags.level4)) {
      const shareTag = topic.value.tags.level4.find(tag => tag.code === 'share' || tag.name === 'share');
      if (shareTag) {
        console.log('✅ 找到分享标签:', shareTag);
        if (shareTag.topicId) {
          topicId = shareTag.topicId;
          console.log('🎯 从 tags.level4 中提取到 topicId:', topicId);
        } else {
          console.warn('⚠️ 分享标签没有 topicId 字段');
        }
      } else {
        console.warn('⚠️ 在 tags.level4 中未找到 share 标签');
      }
    } else {
      console.warn('⚠️ level4 不是数组');
    }
  } else if (!topicId) {
    console.warn('⚠️ tags 不是对象或没有 level4 属性');
  }
  
  // 方式 3: 如果标签中没有 topicId，尝试从内容中提取（最后的手段）
  if (!topicId) {
    console.log('🔍 尝试从内容中提取话题 ID...');
    const forwardMatch = content.match(/<!--FORWARD_FROM:(\d+)-->/);
    if (forwardMatch) {
      topicId = parseInt(forwardMatch[1]);
      console.log('🎯 从内容标记中提取到 topicId:', topicId);
    } else {
      // 使用当前话题 ID 作为备选
      topicId = topic.value.id;
      console.log('⚠️ 未找到 FORWARD_FROM 标记，使用当前话题 ID 作为备选:', topicId);
    }
  }
  
  const result = {
    id: parseInt(topicId),
    authorName,
    content: originalContent,
    createdAt: topic.value.forwardedFromCreatedAt || topic.value.createdAt
  };
  
  console.log('✅ 解析结果:', result);
  console.log('🔍 [forwardedTopic] 计算完成，返回:', result);
  return result;
});

// 查看被转发的话题
const viewForwardedTopic = () => {
  console.log('🖱️ 点击被转发话题卡片');
  console.log('forwardedTopic:', forwardedTopic.value);
  
  if (forwardedTopic.value && forwardedTopic.value.id) {
    const targetId = forwardedTopic.value.id;
    console.log('🚀 准备跳转到话题 ID:', targetId);
    
    // 检查是否是当前话题本身（避免死循环）
    const currentId = route.params.id;
    if (targetId.toString() === currentId) {
      console.warn('⚠️ 目标话题就是当前话题，无需跳转');
      // 不弹出提示，直接返回
      return;
    }
    
    router.push(`/topic/${targetId}`);
    console.log('✅ 已触发路由跳转');
  } else {
    console.error('❌ 无法跳转：forwardedTopic 或 id 不存在');
    ElMessage.warning('无法获取原话题信息');
  }
};

const defaultAvatar = 'https://placehold.co/100x100/CCCCCC/FFFFFF?text=默认头像';

// 获取话题详情
const fetchTopicDetail = async () => {
  try {
    console.log('📡 正在获取话题详情，topicId:', route.params.id);
    const response = await topicAPI.getTopicDetail(route.params.id);
    
    console.log('✅ 话题详情响应:', response);
    console.log('📋 响应数据结构:', JSON.stringify(response, null, 2));

    if (response) {
      // 适配后端实际返回的数据结构（平铺字段）
      topic.value = {
        id: response.id || response.data?.id,
        content: response.content || response.data?.content,
        images: response.images || response.data?.images || [],
        tags: response.tags || response.data?.tags || {},
        level4Tags: response.level4TagCodes || response.data?.level4TagCodes || [],
        tagsObject: response.tagsObject || response.data?.tagsObject || {},
        author: {
          id: response.author?.id || response.authorId || response.data?.author?.id,
          username: response.author?.username || response.authorUsername || response.author?.realName || response.data?.author?.username,
        realName: response.author?.realName || response.authorRealName || response.data?.author?.realName,
          avatarUrl: response.author?.avatarUrl || response.authorAvatarUrl || response.data?.author?.avatarUrl,
          studentId: response.author?.studentId || response.authorStudentId || response.data?.author?.studentId,
          level1Tag: response.author?.level1Tag || response.level1Tag || response.data?.author?.level1Tag // 添加身份标签
        },
        statistics: {
          viewsCount: response.statistics?.viewsCount || response.viewsCount || response.data?.statistics?.viewsCount || response.data?.viewsCount || 0,
          likesCount: response.statistics?.likesCount || response.likesCount || response.data?.statistics?.likesCount || response.data?.likesCount || 0,
        commentsCount: response.statistics?.commentsCount || response.commentsCount || response.data?.statistics?.commentsCount || response.data?.commentsCount || 0,
        collectionsCount: response.statistics?.collectionsCount || response.collectionsCount || response.data?.statistics?.collectionsCount || response.data?.collectionsCount || 0
        },
        interactions: {
          isLiked: response.interactions?.isLiked || response.isLiked || response.data?.interactions?.isLiked || response.data?.isLiked || false,
          isCollected: response.interactions?.isCollected || response.isCollected || response.data?.interactions?.isCollected || response.data?.isCollected || false
        },
        isEssence: response.isEssence || response.data?.isEssence || 0,
      createdAt: response.createdAt || response.data?.createdAt
      };

      console.log('👤 话题作者信息:', topic.value.author);
      console.log('🏷️ 作者身份标签:', topic.value.author.level1Tag);
      console.log('📦 话题标签数据:', topic.value.tags);

      // 获取作者公开信息
      if (topic.value.author && topic.value.author.id) {
        console.log('📡 正在获取作者公开信息，authorId:', topic.value.author.id);
        await fetchAuthorPublicInfo(topic.value.author.id);
      }

      // 获取评论列表
      await fetchComments();
    } else {
      throw new Error('话题详情数据为空');
    }
  } catch (error) {
    console.error('❌ 获取话题详情失败:', error);
    ElMessage.error('获取话题详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取作者公开信息
const fetchAuthorPublicInfo = async (userId) => {
  try {
    console.log('📡 正在获取作者公开信息，userId:', userId);
    const response = await topicAPI.getUserPublicInfo(userId);
    
    console.log('✅ 作者公开信息响应:', response);

    if (response) {
      // 适配后端返回的数据结构
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
    console.error('❌ 获取作者信息失败:', error);
  }
};

// 获取评论列表
const fetchComments = async () => {
  try {
    console.log('📡 正在获取评论列表，topicId:', route.params.id);
    const response = await topicAPI.getTopicComments(route.params.id, { page: 1, size: 20, sort: 'latest' });
    
    console.log('✅ 评论列表响应:', response);

    if (response) {
      // 适配后端返回的数据结构
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
    console.error('❌ 获取评论失败:', error);
  }
};

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  try {
    const response = await topicAPI.createComment(route.params.id, { content: newComment.value });
    
    ElMessage.success('评论成功');
    newComment.value = '';
    await fetchComments();

    // 重新获取话题详情以更新评论数
    await fetchTopicDetail();
  } catch (error) {
    console.error('评论失败:', error);
    ElMessage.error('评论失败');
  }
};

// 点赞话题
const toggleLike = async () => {
  try {
   const action = topic.value.interactions.isLiked ? 'unlike' : 'like';
   const responseData = await topicAPI.likeTopic(route.params.id, action);
    
    // 更新为后端返回的最新状态
    topic.value.interactions.isLiked = !topic.value.interactions.isLiked;
    topic.value.statistics.likesCount += topic.value.interactions.isLiked ? 1 : -1;
    
    if (responseData) {
      topic.value.statistics.likesCount = responseData.likesCount || topic.value.statistics.likesCount;
      topic.value.interactions.isLiked = responseData.isLiked !== undefined ? responseData.isLiked : topic.value.interactions.isLiked;
    }
    
    ElMessage.success(topic.value.interactions.isLiked ? '点赞成功' : '已取消点赞');
  } catch (error) {
   console.error('点赞失败:', error);
    // 409 表示重复点赞或取消点赞（状态冲突）
    if (error.response?.status === 409 || error.code === 409) {
      // 自动重新获取话题详情以同步状态
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
 const action = topic.value.interactions.isCollected ? '取消收藏' : '收藏';
    
    // 乐观更新 UI
 const originalState = topic.value.interactions.isCollected;
  topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    // 修正：根据原始状态来增减，而不是新状态
    if (originalState) {
     // 原来是收藏状态，现在取消，应该减 1
   topic.value.statistics.collectionsCount = Math.max(0, topic.value.statistics.collectionsCount- 1);
    } else {
     // 原来是未收藏状态，现在收藏，应该加 1
   topic.value.statistics.collectionsCount += 1;
    }
    
    // 调用 API
 const responseData = await topicAPI.collectTopic(route.params.id);
    
    // 更新为后端返回的最新状态
   if (responseData && responseData.data) {
   topic.value.statistics.collectionsCount = responseData.data.collectionsCount || topic.value.statistics.collectionsCount;
   topic.value.interactions.isCollected = responseData.data.collected !== undefined ? responseData.data.collected: topic.value.interactions.isCollected;
   }
    
  ElMessage.success(action === '收藏' ? '收藏成功' : '已取消收藏');
  } catch (error) {
 console.error('收藏失败:', error);
    // 恢复原状态
  topic.value.interactions.isCollected = !topic.value.interactions.isCollected;
    // 修正：恢复时也要正确计算
    if (topic.value.interactions.isCollected) {
     // 恢复到收藏状态，应该加回来
   topic.value.statistics.collectionsCount += 1;
    } else {
     // 恢复到未收藏状态，应该减回去
   topic.value.statistics.collectionsCount = Math.max(0, topic.value.statistics.collectionsCount- 1);
    }
    
    // 409 表示重复操作（状态冲突）
   if (error.response?.status === 409) {
  const msg = error.response.data?.message || '操作失败';
   ElMessage.warning(msg);
    } else {
   ElMessage.error('收藏失败，请稍后重试');
    }
  }
};

// 分享话题
const shareTopic = () => {
  const topicId = route.params.id;
  
  // 显示分享选项弹窗
  ElMessageBox.confirm(
    `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <p style="margin-bottom: 8px;">选择分享方式：</p>
        <button class="share-option-btn" id="share-publish" style="width: 100%; padding: 12px; border: 2px solid #409EFF; border-radius: 8px; background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%); color: #FFFFFF; font-size: 14px; font-weight: 500; cursor: pointer; text-align: left; display: flex; align-items: center; gap: 8px; margin-bottom: 10px; transition: all 0.3s;" onmouseover="this.style.background='linear-gradient(135deg, #66b1ff 0%, #79bbff 100%)'; this.style.boxShadow='0 4px 12px rgba(64, 158, 255, 0.3)'; this.style.transform='translateX(5px)'" onmouseout="this.style.background='linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)'; this.style.boxShadow='none'; this.style.transform='translateX(0)'">
          📝 转发为话题
        </button>
        <button class="share-option-btn" id="share-link" style="width: 100%; padding: 12px; border: 2px solid #409EFF; border-radius: 8px; background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%); color: #FFFFFF; font-size: 14px; font-weight: 500; cursor: pointer; text-align: left; display: flex; align-items: center; gap: 8px; margin-bottom: 10px; transition: all 0.3s;" onmouseover="this.style.background='linear-gradient(135deg, #66b1ff 0%, #79bbff 100%)'; this.style.boxShadow='0 4px 12px rgba(64, 158, 255, 0.3)'; this.style.transform='translateX(5px)'" onmouseout="this.style.background='linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)'; this.style.boxShadow='none'; this.style.transform='translateX(0)'">
          🔗 复制链接
        </button>
      </div>
    `,
    '分享话题',
    {
      showConfirmButton: false,
      showCancelButton: true,
      cancelButtonText: '取消',
      dangerouslyUseHTMLString: true,
      customClass: 'share-message-box'
    }
  ).catch((action) => {
    // 用户取消或关闭弹窗，不做任何处理
    console.log('用户取消了分享操作');
  });
  
  // 绑定自定义按钮事件（需要等待 DOM 渲染）
  setTimeout(() => {
    const publishBtn = document.getElementById('share-publish');
    const linkBtn = document.getElementById('share-link');
    
    if (publishBtn) {
      publishBtn.addEventListener('click', () => {
        // 关闭弹窗
        document.querySelector('.share-message-box')?.querySelector('.el-message-box__headerbtn')?.click();
        // 跳转到发布页面
        goToPublish(topicId);
      });
    }
    
    if (linkBtn) {
      linkBtn.addEventListener('click', async () => {
        // 关闭弹窗
        document.querySelector('.share-message-box')?.querySelector('.el-message-box__headerbtn')?.click();
        // 复制链接
        await copyShareLink(topicId);
      });
    }
  }, 300);
};

// 跳转到发布页面
const goToPublish = (topicId) => {
  try {
    // 构建分享内容的引用信息
    const shareData = {
      sourceType: 'topic',
      sourceId: topicId,
      content: topic.value.content,
      author: topic.value.author.username || topic.value.author.realName || '匿名用户',
      url: `${window.location.origin}/topic/${topicId}`,
      originalTopicId: topicId // 保存原话题 ID
    };
    
    // 将分享数据存储到 sessionStorage
    sessionStorage.setItem('shareData', JSON.stringify(shareData));
    
    // 跳转到话题墙页面，打开分享弹窗
    router.push('/topicwall?from=share&sourceType=topic&sourceId=' + topicId);
    
    ElMessage.success('正在跳转到发布页面...');
  } catch (error) {
    console.error('跳转发布页面失败:', error);
    ElMessage.error('跳转失败，请稍后重试');
  }
};

// 复制分享链接
const copyShareLink = async (topicId) => {
  try {
    // 调用后端接口生成分享链接
    const response = await topicAPI.generateShareUrl(topicId);
    
    if (response.data && response.data.code === 200) {
      const shareUrl = response.data.data.shareUrl;
      
      // 复制到剪贴板
      await navigator.clipboard.writeText(shareUrl);
      
      ElMessage.success('分享链接已复制到剪贴板！');
    } else {
      throw new Error('生成分享链接失败');
    }
  } catch (error) {
    console.error('复制链接失败:', error);
    // 降级方案：使用当前 URL
    const shareUrl = `${window.location.origin}/topic/${topicId}`;
    
    try {
      await navigator.clipboard.writeText(shareUrl);
      ElMessage.success('分享链接已复制到剪贴板！');
    } catch (err) {
      // 如果 clipboard API 失败，使用传统方法
      const textArea = document.createElement('textarea');
      textArea.value = shareUrl;
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        ElMessage.success('分享链接已复制到剪贴板！');
      } catch (execErr) {
        ElMessage.error('复制失败，请手动复制链接');
      }
      document.body.removeChild(textArea);
    }
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
      // 使用 topicAPI 删除话题
      const result = await topicAPI.deleteTopic(route.params.id);
      
      console.log('✅ 删除响应:', result);
      console.log('📝 响应类型:', typeof result);
      
      // request.js 拦截器已经处理了标准响应，直接返回的是 data 字段
      // 如果是字符串，说明是成功的消息
      if (typeof result === 'string') {
        ElMessage.success(result || '删除成功');
        // 返回话题墙页面
        router.push('/topicwall');
      } else if (result && result.code === 200) {
        // 兼容旧的响应格式
        ElMessage.success(result.message || '删除成功');
        router.push('/topicwall');
      } else {
        ElMessage.error(result?.message || '删除失败');
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 删除失败:', error);
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
    // TODO: 调用后端举报接口
    console.log('举报原因:', value);
    ElMessage.success('举报已提交，我们会尽快处理');
  }).catch(() => {
    // 用户取消
    console.log('用户取消了举报');
  });
};

// 点赞评论
const likeComment = async (comment) => {
  try {
    const response = await axios.post(
        `/api/topic/comments/${comment.id}/like`,
        { action: 'like' },
        { headers: getAuthHeaders() }
    );

    if (response.data.code === 200) {
      comment.likesCount++;
      ElMessage.success('点赞成功');
    }
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
  // 可以使用 Element Plus 的 ImagePreview 组件
  window.open(imageUrl, '_blank');
};

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

  // 获取当前用户信息
  try {
    console.log('📡 正在获取当前用户信息');
    const response = await topicAPI.getUserProfile();
    
    if (response) {
      currentUser.value = response;
      console.log('✅ 当前用户信息:', currentUser.value);
    }
  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
  }

  await fetchTopicDetail();
});
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
}

.hashtag:hover {
  background: #409EFF;
  color: #fff;
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

/* 评论区 */
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

/* 标签样式 */
.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
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

/* 被转发的话题卡片 */
.forwarded-topic-card {
  margin: 20px 0;
  padding: 18px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border-left: 4px solid #4A90E2;
  cursor: pointer;
  transition: all 0.3s;
}

.forwarded-topic-card:hover {
  transform: translateX(8px);
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.25);
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
}

.forwarded-topic-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}

.forwarded-icon {
  font-size: 18px;
}

.forwarded-label {
  font-weight: 500;
}

.forwarded-author {
  color: #4A90E2;
  font-weight: 600;
}

.forwarded-topic-content {
  margin: 12px 0;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  max-height: 100px;
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
  margin-top: 12px;
  font-size: 13px;
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

@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }

  .topic-article {
    padding: 20px;
  }

  .article-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .author-tooltip {
    left: auto;
    right: 0;
  }

  .statistics {
    flex-wrap: wrap;
    gap: 10px;
  }

  .interaction-buttons {
    flex-direction: column;
  }
}

/* 分享弹窗样式 */
.share-message-box {
  .share-option-btn {
    width: 100% !important;
    padding: 12px !important;
    border: 2px solid #409EFF !important;
    border-radius: 8px !important;
    background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%) !important;
    cursor: pointer !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    color: #FFFFFF !important;
    transition: all 0.3s !important;
    text-align: left !important;
    margin-bottom: 10px !important;
    display: flex !important;
    align-items: center !important;
    gap: 8px !important;
    
    &:hover {
      border-color: #66b1ff !important;
      background: linear-gradient(135deg, #66b1ff 0%, #79bbff 100%) !important;
      transform: translateX(5px) !important;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
    }
  }
}

/* 分享信息提示框 */
.share-info-box {
  display: flex;
  gap: 12px;
  padding: 15px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
  border-radius: 8px;
  border-left: 4px solid #4A90E2;
  margin-top: 15px;
  
  .info-icon {
    font-size: 20px;
    flex-shrink: 0;
  }
  
  .info-content {
    flex: 1;
    
    .info-title {
      margin: 0 0 8px 0;
      font-weight: 600;
      color: #333;
      font-size: 14px;
    }
    
    .info-text {
      margin: 0 0 6px 0;
      color: #666;
      font-size: 13px;
      
      strong {
        color: #4A90E2;
      }
    }
    
    .info-hint {
      margin: 0;
      color: #999;
      font-size: 12px;
      font-style: italic;
    }
  }
}
</style>
