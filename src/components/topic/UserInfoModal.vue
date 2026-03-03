<template>
  <div class="user-info-modal-overlay" @click="$emit('close')" v-if="visible">
    <div class="user-info-modal" @click.stop :class="{ 'loading': loading }">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="$emit('close')">×</button>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-content">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-content">
        <div class="error-icon">⚠️</div>
        <p>{{ error }}</p>
        <button @click="loadUserInfo" class="retry-btn">重试</button>
      </div>
      
      <!-- 用户信息内容 -->
      <div v-else-if="userInfo" class="user-content">
        <!-- 头部背景 -->
        <div class="user-header-bg" :style="{ background: headerGradient }"></div>
        
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
          <div class="stat-item" @click="$emit('action', 'posts')">
            <span class="stat-value">{{ userInfo.statistics.postCount || 0 }}</span>
            <span class="stat-label">话题</span>
          </div>
          <div class="stat-item" @click="$emit('action', 'followers')">
            <span class="stat-value">{{ userInfo.statistics.followerCount || 0 }}</span>
            <span class="stat-label">粉丝</span>
          </div>
          <div class="stat-item" @click="$emit('action', 'following')">
            <span class="stat-value">{{ userInfo.statistics.followingCount || 0 }}</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ userInfo.statistics.likesReceived || 0 }}</span>
            <span class="stat-label">获赞</span>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button 
            v-if="!isCurrentUser" 
            @click="$emit('action', 'follow')"
            class="follow-btn"
            :class="{ 'following': isFollowing }"
          >
            {{ isFollowing ? '已关注' : '关注 TA' }}
          </button>
          <button 
            v-if="!isCurrentUser && userInfo.canMessage" 
            @click="$emit('action', 'message')"
            class="message-btn"
          >
            发消息
          </button>
          <button @click="$emit('action', 'view-profile')" class="profile-btn">
            查看主页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { topicAPI } from '@/api/topic';
import { ElMessage } from 'element-plus';

// Props
const props = defineProps({
  visible: { type: Boolean, default: false },
  userId: { type: Number, required: true },
  currentUserId: { type: Number, default: null }
});

// Emit
const emit = defineEmits(['close', 'action']);

// 数据
const loading = ref(false);
const error = ref(null);
const userInfo = ref(null);
const isFollowing = ref(false);

const defaultAvatar = 'https://placehold.co/200x200/4A90E2/FFFFFF?text=U';

// 计算属性
const isCurrentUser = computed(() => {
  return props.currentUserId === props.userId;
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

// 监听 userId 变化
watch(() => props.userId, (newVal) => {
  if (newVal && props.visible) {
    loadUserInfo();
  }
}, { immediate: true });

watch(() => props.visible, (newVal) => {
  if (newVal && props.userId) {
    loadUserInfo();
  }
});

// 加载用户信息
const loadUserInfo = async () => {
  if (!props.userId) return;
  
  loading.value = true;
  error.value = null;
  
  try {
    const response = await topicAPI.getUserPublicInfo(props.userId);
    
    if (response.data) {
      userInfo.value = response.data;
      
      // TODO: 检查是否已关注
      // const followStatus = await checkFollowStatus(props.userId);
      // isFollowing.value = followStatus;
    } else {
      throw new Error('用户信息为空');
    }
  } catch (err) {
    console.error('加载用户信息失败:', err);
    error.value = err.response?.data?.message || '加载用户信息失败，请重试';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 暴露方法给父组件
defineExpose({
  refresh: loadUserInfo
});
</script>

<style scoped lang="scss">
.user-info-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

.user-info-modal {
  position: relative;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
  
  &.loading {
    min-height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  z-index: 10;
  transition: all 0.3s;
  
  &:hover {
    background: white;
    color: #333;
    transform: rotate(90deg);
  }
}

.loading-content {
  text-align: center;
  padding: 40px;
  
  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #409eff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.error-content {
  text-align: center;
  padding: 40px;
  
  .error-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  p {
    color: #999;
    margin-bottom: 20px;
  }
  
  .retry-btn {
    padding: 10px 24px;
    background: #409eff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    
    &:hover {
      background: #66b1ff;
    }
  }
}

.user-content {
  .user-header-bg {
    height: 120px;
    border-radius: 16px 16px 0 0;
  }
  
  .user-basic-info {
    position: relative;
    padding: 0 24px;
    margin-top: -60px;
    display: flex;
    align-items: flex-start;
    
    .avatar-section {
      position: relative;
      flex-shrink: 0;
      
      .user-avatar {
        width: 100px;
        height: 100px;
        border-radius: 50%;
        border: 4px solid white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        object-fit: cover;
        background: white;
      }
      
      .verified-badge {
        position: absolute;
        bottom: 8px;
        right: 8px;
        width: 28px;
        height: 28px;
        background: #409eff;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        border: 3px solid white;
      }
    }
    
    .user-details {
      margin-left: 16px;
      padding-top: 48px;
      flex: 1;
      
      .username {
        font-size: 24px;
        font-weight: bold;
        color: #333;
        margin: 0 0 8px 0;
      }
      
      .user-identity {
        font-size: 14px;
        color: #666;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 6px;
        
        .identity-icon {
          font-size: 16px;
        }
      }
      
      .user-bio {
        font-size: 13px;
        color: #999;
        margin: 0;
        line-height: 1.5;
      }
    }
  }
  
  .academic-info {
    padding: 20px 24px;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;
      
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
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    
    .stat-item {
      flex: 1;
      text-align: center;
      cursor: pointer;
      padding: 8px 0;
      transition: all 0.3s;
      
      &:hover {
        background: #f5f7fa;
        border-radius: 8px;
      }
      
      .stat-value {
        display: block;
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 13px;
        color: #999;
      }
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 12px;
    padding: 20px 24px;
    
    button {
      flex: 1;
      padding: 12px 20px;
      border: none;
      border-radius: 8px;
      font-size: 14px;
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
      
      &.profile-btn {
        background: #f5f7fa;
        color: #606266;
        
        &:hover {
          background: #e6e8eb;
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
