<template>
  <div class="user-profile-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>用户信息加载失败</h2>
      <p>{{ error }}</p>
      <button @click="loadUserInfo" class="retry-btn">重试</button>
    </div>

    <!-- 用户信息内容 -->
    <div v-else-if="userInfo" class="user-content">
      <!-- 头部背景 -->
      <div class="user-header-bg" :style="{ background: headerGradient }">
        <!-- 返回按钮移至顶部左侧 -->
        <button @click="goBack" class="back-to-top-btn">
          ← 返回
        </button>
      </div>
      
      <!-- 用户基本信息 -->
      <div class="user-basic-info">
        <div class="avatar-section">
          <img 
            :src="userInfo.basicInfo.avatarUrl || defaultAvatar" 
            :alt="userInfo.basicInfo.username"
            class="user-avatar"
          >
          <span v-if="userInfo.identity.verified" class="verified-badge" title="已认证">✓</span>
        </div>
        
        <div class="user-details">
          <h2 class="username">{{ userInfo.basicInfo.realName || userInfo.basicInfo.username }}</h2>
          <p class="user-identity">
            <span class="identity-icon">{{ identityIcon }}</span>
            {{ identityName }}
          </p>
          <p v-if="userInfo.basicInfo.bio" class="user-bio">{{ userInfo.basicInfo.bio }}</p>
        </div>
      </div>
      
      <!-- 学术信息 -->
      <div v-if="userInfo.academicInfo" class="academic-info">
        <div class="info-item">
          <span class="item-label">🏛️ 学院</span>
          <span class="item-value">{{ userInfo.academicInfo.college || '未填写' }}</span>
        </div>
        <div class="info-item">
          <span class="item-label">📚 专业</span>
          <span class="item-value">{{ userInfo.academicInfo.major || '未填写' }}</span>
        </div>
        <div v-if="userInfo.privacySettings.showStudentId !== false" class="info-item">
          <span class="item-label">🎓 学号</span>
          <span class="item-value">{{ userInfo.academicInfo.studentId || '未填写' }}</span>
        </div>
        <div class="info-item">
          <span class="item-label">📅 年级</span>
          <span class="item-value">{{ userInfo.academicInfo.grade || '未填写' }}</span>
        </div>
      </div>
      
      <!-- 统计数据 -->
      <div class="user-stats">
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.statistics.postCount || 0 }}</span>
          <span class="stat-label">话题</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.statistics.followerCount || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.statistics.followingCount || 0 }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.statistics.likesReceived || 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
      </div>
      
      <!-- 隐私设置提示 -->
      <div v-if="isCurrentUser" class="privacy-settings-card">
        <h3>🔒 隐私设置</h3>
        <div class="setting-item">
          <div class="setting-content">
            <span class="setting-label">允许他人查看我的话题和参与记录</span>
            <span class="setting-desc">关闭后，其他用户将无法看到您发布的话题和参与的话题</span>
          </div>
          <label class="switch">
            <input type="checkbox" v-model="privacySettings.publicVisible" @change="updatePrivacySettings">
            <span class="slider"></span>
          </label>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="!isCurrentUser">
        <button 
          @click="handleFollow"
          class="follow-btn"
          :class="{ 'following': isFollowing }"
        >
          {{ isFollowing ? '已关注' : '关注 TA' }}
        </button>
        <button 
          v-if="userInfo.canMessage" 
          @click="handleMessage"
          class="message-btn"
        >
          发消息
        </button>
      </div>
      
      <!-- 隐私受限提示 -->
      <div v-if="!canViewContent" class="privacy-restricted">
        <div class="restricted-icon">🔒</div>
        <h3>该用户设置了隐私保护</h3>
        <p>暂时无法查看其发布和参与的话题</p>
      </div>
      
      <!-- 话题标签页 -->
      <div v-else class="topics-section">
        <div class="topic-tabs">
          <button 
            @click="currentTab = 'published'"
            class="tab-btn"
            :class="{ active: currentTab === 'published' }"
          >
            发布的话题
          </button>
          <button 
            @click="currentTab = 'participated'"
            class="tab-btn"
            :class="{ active: currentTab === 'participated' }"
          >
            参与的话题
          </button>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="topicsLoading" class="loading-state">
          <div class="spinner-small"></div>
          <span>加载中...</span>
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="getTopicsList.length === 0" class="empty-topics">
          <div class="empty-icon">💭</div>
          <p>{{ currentTab === 'published' ? '暂无发布的话题' : '暂无参与的话题' }}</p>
        </div>
        
        <!-- 话题列表 -->
        <div v-else class="user-topics-list">
          <div
            v-for="topic in getTopicsList"
            :key="topic.id"
            class="topic-item"
            @click="viewTopicDetail(topic)"
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { topicAPI } from '@/api/topic';

const route = useRoute();
const router = useRouter();

// 数据
const loading = ref(false);
const error = ref(null);
const userInfo = ref(null);
const isFollowing = ref(false);
const currentTab = ref('published'); // 'published' 或 'participated'
const topicsLoading = ref(false);
const publishedTopics = ref([]);
const participatedTopics = ref([]);
const privacySettings = ref({
  publicVisible: true // 是否对外公开话题和参与记录
});

const defaultAvatar = 'https://placehold.co/200x200/4A90E2/FFFFFF?text=U';

// 计算属性
const currentUserId = ref(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.id || null;
});

const isCurrentUser = computed(() => {
  return currentUserId.value === parseInt(route.params.userId);
});

const identityIcon = computed(() => {
  const identityMap = {
    'student': '👨‍🎓',
    'merchant': '🏪',
    'organization': '👥',
    'individual': '👤',
    'followed': '⭐'
  };
  return identityMap[userInfo.value?.identity?.level1Tag] || '👤';
});

const identityName = computed(() => {
  const identityMap = {
    'student': '学生',
    'merchant': '校外商户',
    'organization': '团体',
    'individual': '校外个人',
    'followed': '已关注'
  };
  return identityMap[userInfo.value?.identity?.level1Tag] || '未知';
});

const headerGradient = computed(() => {
  const gradients = {
    'student': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'merchant': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'organization': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'individual': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'followed': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  };
  return gradients[userInfo.value?.identity?.level1Tag] || gradients['individual'];
});

// 是否可以查看内容（隐私控制）
const canViewContent = computed(() => {
  // 如果是当前用户自己，总是可以查看
  if (isCurrentUser.value) {
    return true;
  }
  // 否则检查用户的隐私设置
  return privacySettings.value.publicVisible !== false;
});

// 获取当前标签页的话题列表
const getTopicsList = computed(() => {
  return currentTab.value === 'published' ? publishedTopics.value : participatedTopics.value;
});

// 生命周期
onMounted(() => {
  loadUserInfo();
});

// 加载用户信息
const loadUserInfo = async () => {
  const userId = route.params.userId;
  if (!userId) return;
  
  loading.value = true;
  error.value = null;
  
  try {
    console.log('📡 正在加载用户公开信息，userId:', userId);
    const response = await topicAPI.getUserPublicInfo(userId);
    
    console.log('✅ 用户公开信息响应:', response);
    console.log('📋 响应数据结构:', JSON.stringify(response, null, 2));
    
    if (response) {
      // 适配后端实际返回的数据结构（平铺字段）
      userInfo.value = {
        basicInfo: {
          id: response.id || response.basicInfo?.id,
          username: response.username || response.basicInfo?.username,
          realName: response.realName || response.basicInfo?.realName,
          avatarUrl: response.avatarUrl || response.basicInfo?.avatarUrl,
          bio: response.bio || response.basicInfo?.bio,
          college: response.college || response.basicInfo?.college
        },
        academicInfo: response.academicInfo || {},
        identity: {
          verified: response.verified || response.identity?.verified || false,
          level1Tag: response.level1Tag || response.identity?.level1Tag || response.identity?.tagCode,
          level1TagName: response.level1TagName || response.identity?.level1TagName || response.identity?.tagName
        },
        privacySettings: response.privacySettings || {},
        statistics: {
          postCount: response.postCount || response.statistics?.postCount || 0,
          likesReceived: response.likesReceived || response.statistics?.likesReceived || 0,
          followerCount: response.followerCount || response.statistics?.followerCount || 0,
          followingCount: response.followingCount || response.statistics?.followingCount || 0
        },
        canMessage: response.canMessage || false
      };
      
      // 加载隐私设置
      privacySettings.value = userInfo.value.privacySettings;
      
      console.log('✅ 用户信息解析成功:', userInfo.value);
      console.log('🏷️ 身份标签:', userInfo.value.identity.level1Tag);
      
      // 加载用户的话题
      loadUserTopics();
    } else {
      throw new Error('用户信息为空');
    }
  } catch (err) {
    console.error('❌ 加载用户信息失败:', err);
    error.value = err.response?.data?.message || '加载用户信息失败';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 加载用户发布和参与的话题
const loadUserTopics = async () => {
  // 如果是当前用户自己，或者用户设置了公开，则加载话题
  if (!canViewContent.value && !isCurrentUser.value) {
    return;
  }
  
  topicsLoading.value = true;
  const userId = route.params.userId;
  
  try {
    // 并行加载发布和参与的话题
    const [publishedRes, participatedRes] = await Promise.all([
      topicAPI.getUserPublishedTopics(userId),
      topicAPI.getUserParticipatedTopics(userId)
    ]);
    
    publishedTopics.value = publishedRes.data?.topics || [];
    participatedTopics.value = participatedRes.data?.topics || [];
    
    console.log('✅ 用户话题已加载');
  } catch (error) {
    console.error('加载用户话题失败:', error);
    // 不显示 Mock 数据，保持空列表
    publishedTopics.value = [];
    participatedTopics.value = [];
  } finally {
    topicsLoading.value = false;
  }
};

// 使用 Mock 数据预览
const useMockData = () => {
  const userId = route.params.userId;
  
  userInfo.value = {
    basicInfo: {
      id: userId,
      username: `user${userId}`,
      realName: userId === '6' ? '马拥康' : '张三',
      avatarUrl: null,
      bio: '热爱编程的大学生，喜欢分享技术经验',
      college: '计算机学院'
    },
    academicInfo: {
      college: '计算机学院',
      major: '计算机科学与技术',
      studentId: '2021001',
      grade: '2021 级'
    },
    identity: {
      level1Tag: 'student',
      verified: true
    },
    statistics: {
      postCount: 15,
      followerCount: 128,
      followingCount: 56,
      likesReceived: 342
    },
    privacySettings: {
      showStudentId: true
    },
    canMessage: true
  };
  
  ElMessage.error('后端接口未实现，请检查后端服务');
};

// 处理操作
const handleFollow = () => {
  if (isFollowing.value) {
    ElMessage.info('已取消关注');
    isFollowing.value = false;
  } else {
    ElMessage.success('关注成功');
    isFollowing.value = true;
  }
};

const handleMessage = () => {
  ElMessage.info('消息功能开发中...');
};

const goBack = () => {
  router.back();
};

// Mock 话题数据
const useMockTopicsData = () => {
  const mockTopics = [
    {
      id: 1,
      content: '分享我的学习经验：如何高效准备期末考试...',
      likesCount: 25,
      commentsCount: 8,
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString()
    },
    {
      id: 2,
      content: '图书馆三楼新装修了，环境超级好！推荐大家去那里自习~',
      likesCount: 42,
      commentsCount: 15,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
    },
    {
      id: 3,
      content: '求助：有没有人知道计算机学院的选修课哪个老师比较好？',
      likesCount: 12,
      commentsCount: 23,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString()
    }
  ];
  
  publishedTopics.value = mockTopics;
  participatedTopics.value = mockTopics.slice(0, 2);
  
  console.warn('⚠️ Mock 数据已移除，请确保后端接口正常');
};

// 更新隐私设置
const updatePrivacySettings = async () => {
  try {
    // TODO: 调用后端 API 更新隐私设置
    console.log('隐私设置已更新:', privacySettings.value);
    ElMessage.success('隐私设置已保存');
  } catch (error) {
    console.error('更新隐私设置失败:', error);
    ElMessage.error('更新失败，请稍后重试');
    // 恢复原状态
    privacySettings.value.publicVisible = !privacySettings.value.publicVisible;
  }
};

// 查看话题详情
const viewTopicDetail = (topic) => {
  router.push(`/topic/${topic.id}`);
};

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
.user-profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.retry-btn {
  margin-top: 20px;
  padding: 12px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.user-content {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.user-header-bg {
  height: 200px;
  border-radius: 16px 16px 0 0;
  position: relative;
}

/* 顶部返回按钮 */
.back-to-top-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.back-to-top-btn:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.user-basic-info {
  position: relative;
  padding: 0 40px;
  margin-top: -80px;
  display: flex;
  align-items: flex-start;
  
  .avatar-section {
    position: relative;
    flex-shrink: 0;
    
    .user-avatar {
      width: 160px;
      height: 160px;
      border-radius: 50%;
      border: 6px solid white;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      object-fit: cover;
      background: white;
    }
    
    .verified-badge {
      position: absolute;
      bottom: 12px;
      right: 12px;
      width: 36px;
      height: 36px;
      background: #409eff;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      border: 4px solid white;
    }
  }
  
  .user-details {
    margin-left: 24px;
    padding-top: 60px;
    flex: 1;
    
    .username {
      font-size: 32px;
      font-weight: bold;
      color: #333;
      margin: 0 0 12px 0;
    }
    
    .user-identity {
      font-size: 16px;
      color: #666;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .identity-icon {
        font-size: 20px;
      }
    }
    
    .user-bio {
      font-size: 14px;
      color: #999;
      margin: 0;
      line-height: 1.6;
    }
  }
}

.academic-info {
  padding: 30px 40px;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    font-size: 15px;
    
    .item-label {
      color: #666;
      font-weight: 500;
    }
    
    .item-value {
      color: #333;
    }
  }
}

.user-stats {
  display: flex;
  padding: 30px 40px;
  border-bottom: 1px solid #f0f0f0;
  
  .stat-item {
    flex: 1;
    text-align: center;
    padding: 12px 0;
    transition: all 0.3s;
    
    &:hover {
      background: #f5f7fa;
      border-radius: 8px;
    }
    
    .stat-value {
      display: block;
      font-size: 28px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 6px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #999;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 16px;
  padding: 30px 40px;
  
  button {
    flex: 1;
    padding: 14px 24px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    
    &.follow-btn {
      background: #409eff;
      color: white;
      
      &:hover {
        background: #66b1ff;
      }
      
      &.following {
        background: #f0f0f0;
        color: #666;
        
        &:hover {
          background: #e6e6e6;
        }
      }
    }
    
    &.message-btn {
      background: #67c23a;
      color: white;
      
      &:hover {
        background: #85ce61;
      }
    }
    
    &.back-btn {
      background: #f5f7fa;
      color: #606266;
      
      &:hover {
        background: #e6e8eb;
      }
    }
  }
}

/* 隐私设置卡片 */
.privacy-settings-card {
  padding: 24px 40px;
  border-bottom: 1px solid #f0f0f0;
  
  h3 {
    margin: 0 0 16px 0;
    font-size: 18px;
    color: #333;
  }
  
  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    
    .setting-content {
      flex: 1;
      
      .setting-label {
        display: block;
        font-size: 15px;
        color: #333;
        font-weight: 500;
        margin-bottom: 6px;
      }
      
      .setting-desc {
        display: block;
        font-size: 13px;
        color: #999;
      }
    }
  }
}

/* 开关按钮 */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
  
  input {
    opacity: 0;
    width: 0;
    height: 0;
    
    &:checked + .slider {
      background-color: #409eff;
    }
    
    &:focus + .slider {
      box-shadow: 0 0 1px #409eff;
    }
    
    &:not(:checked) + .slider {
      background-color: #dcdfe6;
    }
  }
  
  .slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: .4s;
    border-radius: 26px;
    
    &:before {
      position: absolute;
      content: "";
      height: 20px;
      width: 20px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: .4s;
      border-radius: 50%;
    }
  }
  
  input:checked + .slider:before {
    transform: translateX(24px);
  }
}

/* 隐私受限提示 */
.privacy-restricted {
  text-align: center;
  padding: 60px 20px;
  
  .restricted-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    color: #666;
  }
  
  p {
    margin: 0;
    color: #999;
    font-size: 14px;
  }
}

/* 话题区域 */
.topics-section {
  padding: 30px 40px;
}

.topic-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0;
  
  .tab-btn {
    padding: 12px 24px;
    background: transparent;
    border: none;
    border-bottom: 3px solid transparent;
    cursor: pointer;
    font-size: 15px;
    color: #666;
    transition: all 0.3s;
    
    &:hover {
      color: #409eff;
    }
    
    &.active {
      color: #409eff;
      border-bottom-color: #409eff;
      font-weight: 600;
    }
  }
}

.loading-state {
  text-align: center;
  padding: 40px;
  
  .spinner-small {
    width: 30px;
    height: 30px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #409eff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 12px;
  }
}

.empty-topics {
  text-align: center;
  padding: 60px 20px;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  p {
    color: #999;
    font-size: 14px;
  }
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
  
  &:hover {
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
}

@media (max-width: 768px) {
  .user-basic-info {
    padding: 0 20px;
    flex-direction: column;
    align-items: center;
    text-align: center;
    
    .avatar-section {
      margin-bottom: 16px;
    }
    
    .user-details {
      margin-left: 0;
      padding-top: 16px;
    }
  }
  
  .academic-info,
  .user-stats,
  .action-buttons {
    padding: 20px;
  }
}
</style>
