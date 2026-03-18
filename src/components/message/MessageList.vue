<template>
  <div class="message-list">
    <div v-if="messages.length === 0" class="empty-state">
      <el-empty description="暂无消息" />
    </div>
    
    <div v-else class="message-items">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="{ 'is-unread': !message.isRead && !message.read }"
        @click="handleClick(message)"
      >
        <div class="message-avatar">
          <el-avatar 
            :src="message.avatar || message.sender?.avatarUrl || 'https://placehold.co/100x100/4A90E2/FFFFFF?text=M'" 
            :size="50"
          />
        </div>
        
        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">{{ message.senderName || message.sender?.username || '未知用户' }}</span>
            <span class="message-time">{{ formatTime(message.createdAt || message.timestamp) }}</span>
          </div>
          
          <div class="message-preview">
            <span class="message-type">{{ getMessageType(message) }}:</span>
            {{ message.preview || message.content || '无内容' }}
          </div>
        </div>
        
        <div v-if="!message.isRead && !message.read" class="unread-dot" />
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['message-click']);

const getMessageType = (message) => {
  const type = message.messageType || message.type;
  const typeMap = {
    'system': '系统',
    'private_message': '私信',
    'group': '群聊',
    'order_notification': '订单'
  };
  return typeMap[type] || '消息';
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now - date;
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

const handleClick = (message) => {
  emit('message-click', message);
};
</script>

<style scoped>
.message-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.empty-state {
  padding: 60px 20px;
}

.message-items {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.is-unread {
  background-color: #f0f9ff;
}

.message-item:last-child {
  border-bottom: none;
}

.message-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-sender {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-type {
  color: #4A90E2;
  font-weight: 500;
  margin-right: 5px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #4A90E2;
  flex-shrink: 0;
  margin-left: 10px;
}
</style>
