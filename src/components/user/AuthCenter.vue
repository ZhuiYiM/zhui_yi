<template>
  <div class="auth-section">
    <div class="section-header">
      <h2>认证中心</h2>
    </div>

    <div class="auth-status">
      <div class="status-item-container">
        <!-- 身份认证 -->
        <div class="status-item" :class="{ 'verified': isVerified }">
          <div class="status-icon">
            <span v-if="isVerified">✅</span>
            <span v-else>❌</span>
          </div>
          <div class="status-info">
            <h3>身份认证</h3>
            <p v-if="isVerified">已通过</p>
            <p v-else>未通过</p>
            <p class="status-detail" v-if="!isVerified">(未设置身份信息)</p>
          </div>
          <button @click="openIdentityForm" class="auth-btn">
            {{ isVerified ? '重新认证' : '立即认证' }}
          </button>
        </div>
        
        <!-- 实名认证 -->
        <div class="status-item" :class="{ 'verified': isRealNameVerified }">
          <div class="status-icon">
            <span v-if="isRealNameVerified">✅</span>
            <span v-else>❌</span>
          </div>
          <div class="status-info">
            <h3>实名认证</h3>
            <p v-if="isRealNameVerified">已通过</p>
            <p v-else>未通过</p>
            <p class="status-detail" v-if="!isRealNameVerified">(真实姓名未设置)</p>
          </div>
          <button @click="handleVerification('realname')" class="auth-btn">
            {{ isRealNameVerified ? '重新认证' : '立即认证' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { userAPI } from '@/api/user';

const props = defineProps({
  isVerified: {
    type: Boolean,
    default: false
  },
  isRealNameVerified: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['verification-click']);

// 定义事件，用于打开身份认证表单
const openIdentityForm = () => {
  emit('verification-click', 'identity', false);
};

// 处理认证点击
const handleVerification = async (type) => {
  try {
    if (type === 'identity') {
      if (props.isVerified) {
        // 取消认证
        await userAPI.applyIdentityVerification({ action: 'cancel' });
        ElMessage.success('已取消身份认证');
        emit('verification-click', 'identity', false);
      } else {
        // 申请认证
        ElMessage.info('身份认证申请功能开发中');
      }
    } else if (type === 'realname') {
      if (props.isRealNameVerified) {
        // 取消实名认证
        await userAPI.applyRealNameVerification({ action: 'cancel' });
        ElMessage.success('已取消实名认证');
        emit('verification-click', 'realname', false);
      } else {
        // 申请实名认证
        ElMessage.info('实名认证申请功能开发中');
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};
</script>

<style scoped>
.auth-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.auth-status {
  margin-top: 15px;
}

.status-item-container {
  display: flex;
  gap: 15px;
}

.status-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
}

.status-item:hover {
  background-color: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.05);
}

.status-item.verified {
  background-color: #e8f5e9;
}

.status-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.status-info {
  flex: 1;
  min-width: 0;
}

.status-info h3 {
  margin: 0 0 5px 0;
  font-size: 1rem;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-info p {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
}

.status-detail {
  font-size: 0.75rem;
  color: #999;
  margin-top: 3px;
}

.auth-btn {
  padding: 8px 16px;
  border: 2px solid #e1e5f2;
  border-radius: 6px;
  background-color: white;
  color: #667eea;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  font-weight: 500;
}

.auth-btn:hover {
  background-color: #667eea;
  color: white;
  border-color: #667eea;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .auth-section {
    margin: 10px;
    padding: 15px;
  }

  .status-item-container {
    flex-direction: column;
    gap: 10px;
  }

  .status-item {
    flex-direction: row;
    padding: 15px;
  }

  .status-icon {
    margin-right: 10px;
  }
}
</style>
