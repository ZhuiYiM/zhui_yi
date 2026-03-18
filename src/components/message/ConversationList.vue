<template>
  <div class="conversation-list">
    <div
        v-for="conversation in conversations"
        :key="conversation.senderId"
        :class="['conversation-item', { unread: conversation.unreadCount > 0 }]"
        @click="handleClick(conversation)"
    >
      <div class="conversation-avatar">
        <img :src="conversation.senderAvatar" :alt="conversation.senderName">
        <span v-if="conversation.unreadCount > 0" class="unread-dot"></span>
      </div>
      <div class="conversation-content">
        <div class="conversation-header">
          <span class="sender-name">{{ conversation.senderName }}</span>
          <span class="message-time">{{ formatTime(conversation.lastMessageTime) }}</span>
        </div>
        <div class="conversation-preview">
          <span class="message-text">{{ conversation.lastMessage }}</span>
          <span v-if="conversation.unreadCount > 0" class="unread-count-badge">{{ conversation.unreadCount }}</span>
        </div>
        <div class="conversation-type">
          <span class="type-tag">{{ conversation.type }}</span>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="conversations.length === 0" class="empty-state">
      <el-empty :description="emptyText" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  conversations: {
    type: Array,
    required: true
  },
  emptyText: {
    type: String,
    default: '暂无消息'
  }
});

const emit = defineEmits(['conversation-click']);

// 处理点击事件
const handleClick = (conversation) => {
  emit('conversation-click', conversation);
};

// 格式化时间
const formatTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const now = new Date();
  const diffMs = now - d;
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return d.toLocaleDateString();
  }
};
</script>

<style scoped>
.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.conversation-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  pointer-events: auto; /* 确保可以点击 */
}

.conversation-item * {
  pointer-events: none; /* 防止子元素阻止事件冒泡 */
}

.conversation-item:hover {
  background: #f0f7ff;
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.15);
}

.conversation-item.unread {
  background: #e9f7fe;
  border-left: 3px solid #4A90E2;
}

.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.conversation-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e0e0e0;
}

.conversation-avatar .unread-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #ff6b6b;
  border-radius: 50%;
  border: 2px solid white;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.conversation-preview .message-text {
  color: #666;
  font-size: 14px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.conversation-type {
  display: flex;
  gap: 8px;
}

.type-tag {
  font-size: 12px;
  color: #999;
  background-color: #f5f7fa;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}
</style>
