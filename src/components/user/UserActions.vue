<template>
  <div class="action-buttons">
    <button 
      @click="$emit('follow')"
      class="follow-btn"
      :class="{ 'following': isFollowing }"
    >
      {{ isFollowing ? '已关注' : '关注 TA' }}
    </button>
    <button 
      v-if="canMessage" 
      @click="$emit('message')"
      class="message-btn"
    >
      发消息
    </button>
    <button 
      @click="$emit('report')"
      class="report-btn"
    >
      ⚠️ 举报
    </button>
  </div>
</template>

<script setup>
defineProps({
  isFollowing: {
    type: Boolean,
    default: false
  },
  canMessage: {
    type: Boolean,
    default: true
  }
});

defineEmits(['follow', 'message', 'report']);
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 16px;
  padding: 30px 40px;
}

.action-buttons button {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.follow-btn {
  background: #409eff;
  color: white;
}

.follow-btn:hover {
  background: #66b1ff;
}

.follow-btn.following {
  background: #f0f0f0;
  color: #666;
}

.follow-btn.following:hover {
  background: #e6e6e6;
}

.message-btn {
  background: #67c23a;
  color: white;
}

.message-btn:hover {
  background: #85ce61;
}

.report-btn {
  background: #f56c6c;
  color: white;
}

.report-btn:hover {
  background: #f78989;
}

@media (max-width: 768px) {
  .action-buttons {
    padding: 20px;
  }
}
</style>
