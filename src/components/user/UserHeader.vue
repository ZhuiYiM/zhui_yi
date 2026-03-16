<template>
  <div class="user-header-section">
    <!-- 头部背景 -->
    <div class="user-header-bg" :style="{ background: headerGradient }">
      <button @click="$emit('back')" class="back-btn">
        ← 返回
      </button>
    </div>
    
    <!-- 用户基本信息 -->
    <div class="user-basic-info">
      <div class="avatar-section">
        <img 
          :src="basicInfo.avatarUrl || defaultAvatar" 
          :alt="basicInfo.username"
          class="user-avatar"
        >
        <span v-if="identity.verified" class="verified-badge" title="已认证">✓</span>
      </div>
      
      <div class="user-details">
        <h2 class="username">{{ basicInfo.username || '匿名用户' }}</h2>
        <p class="user-identity">
          <span class="identity-icon">{{ identityIcon }}</span>
          {{ identityName }}
        </p>
        <p v-if="basicInfo.bio" class="user-bio">{{ basicInfo.bio }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  basicInfo: {
    type: Object,
    required: true
  },
  identity: {
    type: Object,
    required: true
  }
});

defineEmits(['back']);

const defaultAvatar = 'https://placehold.co/200x200/4A90E2/FFFFFF?text=U';

const identityIcon = computed(() => {
  const identityMap = {
    'student': '👨‍🎓',
    'merchant': '🏪',
    'organization': '👥',
    'individual': '👤',
    'followed': '⭐'
  };
  return identityMap[props.identity?.level1Tag] || '👤';
});

const identityName = computed(() => {
  const identityMap = {
    'student': '学生',
    'merchant': '校外商户',
    'organization': '团体',
    'individual': '校外个人',
    'followed': '已关注'
  };
  return identityMap[props.identity?.level1Tag] || '未知';
});

const headerGradient = computed(() => {
  const gradients = {
    'student': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'merchant': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'organization': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'individual': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'followed': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  };
  return gradients[props.identity?.level1Tag] || gradients['individual'];
});
</script>

<style scoped>
.user-header-section {
  position: relative;
}

.user-header-bg {
  height: 200px;
  border-radius: 16px 16px 0 0;
  position: relative;
}

.back-btn {
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

.back-btn:hover {
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
}

.avatar-section {
  position: relative;
  flex-shrink: 0;
}

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

.user-details {
  margin-left: 24px;
  padding-top: 60px;
  flex: 1;
}

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
}

.user-identity .identity-icon {
  font-size: 20px;
}

.user-bio {
  font-size: 14px;
  color: #999;
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .user-basic-info {
    padding: 0 20px;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .avatar-section {
    margin-bottom: 16px;
  }
  
  .user-details {
    margin-left: 0;
    padding-top: 16px;
  }
}
</style>
