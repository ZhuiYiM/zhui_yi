<template>
  <div class="user-popover-card" :style="positionStyle">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner-small"></div>
      <span>加载中...</span>
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <span>⚠️ {{ error }}</span>
    </div>
    
    <!-- 用户信息 -->
    <div v-else-if="userInfo" class="user-info">
      <div class="user-header">
        <img 
          :src="userInfo.basicInfo.avatarUrl || defaultAvatar" 
          :alt="userInfo.basicInfo.username"
          class="avatar"
        >
        <div class="user-details">
          <h4 class="username">{{ userInfo.basicInfo.realName || userInfo.basicInfo.username }}</h4>
          <p class="identity">
            <span class="identity-icon">{{ identityIcon }}</span>
            {{ identityName }}
          </p>
        </div>
      </div>
      
      <div class="user-bio" v-if="userInfo.basicInfo.bio">
        {{ userInfo.basicInfo.bio }}
      </div>
      
      <div class="user-stats">
        <div class="stat-item">
          <span class="value">{{ userInfo.statistics.postCount || 0 }}</span>
          <span class="label">话题</span>
        </div>
        <div class="stat-item">
          <span class="value">{{ userInfo.statistics.followerCount || 0 }}</span>
          <span class="label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="value">{{ userInfo.statistics.followingCount || 0 }}</span>
          <span class="label">关注</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { topicAPI } from '@/api/topic';

const props = defineProps({
  userId: { type: Number, required: true },
  position: { 
    type: Object, 
    default: () => ({ top: 0, left: 0 }) 
  }
});

const router = useRouter();
const loading = ref(false);
const error = ref(null);
const userInfo = ref(null);

const defaultAvatar = 'https://placehold.co/200x200/4A90E2/FFFFFF?text=U';

const positionStyle = computed(() => ({
  top: `${props.position.top}px`,
  left: `${props.position.left}px`
}));

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

onMounted(() => {
  loadUserInfo();
});

const loadUserInfo = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await topicAPI.getUserPublicInfo(props.userId);
    
    if (response.data) {
      userInfo.value = response.data;
    } else {
      throw new Error('用户信息为空');
    }
  } catch (err) {
    console.error('加载用户信息失败:', err);
    error.value = '加载失败';
  } finally {
    loading.value = false;
  };
};

const viewProfile = () => {
  // 方法已移除，按钮已从模板中删除
};
</script>

<style scoped>
.user-popover-card {
  position: fixed;
  width: 320px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 10000;
  overflow: hidden;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.loading-state, .error-state {
  padding: 30px;
  text-align: center;
  color: #999;
}

.spinner-small {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.user-info {
  padding: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #409eff;
}

.user-details {
  margin-left: 12px;
  flex: 1;
  
  .username {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0 0 6px 0;
  }
  
  .identity {
    font-size: 13px;
    color: #666;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 6px;
    
    .identity-icon {
      font-size: 16px;
    }
  }
}

.user-bio {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  margin-bottom: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  
  .stat-item {
    text-align: center;
    
    .value {
      display: block;
      font-size: 20px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 4px;
    }
    
    .label {
      font-size: 12px;
      color: #999;
    }
  }
}

</style>
