<template>
  <div class="user-info-section">
    <div class="user-header">
      <img :src="avatar" alt="用户头像" class="user-avatar">
      <div class="user-details">
        <h2>{{ name }}</h2>
        <p>学号：{{ studentId }}</p>
        <p>部门：{{ college }}</p>
      </div>
    </div>

    <div class="user-actions">
      <button @click="$emit('edit-profile')" class="action-btn">编辑资料</button>
      <button @click="$emit('account-management')" class="action-btn">账号管理</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  },
  studentId: {
    type: String,
    required: true
  },
  college: {
    type: String,
    required: true
  },
  avatar: {
    type: String,
    required: true
  }
});

defineEmits(['edit-profile', 'account-management']);
</script>

<style scoped>
.user-info-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #4A90E2;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-details h2 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  color: #333;
  font-weight: 600;
}

.user-details p {
  margin: 5px 0;
  font-size: 0.9rem;
  color: #666;
}

.user-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn:hover {
  background-color: #4578d9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .user-info-section {
    margin: 10px;
    padding: 15px;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .user-avatar {
    width: 100px;
    height: 100px;
  }

  .user-details {
    width: 100%;
  }

  .user-details h2 {
    font-size: 1.2rem;
  }

  .user-actions {
    flex-direction: column;
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
