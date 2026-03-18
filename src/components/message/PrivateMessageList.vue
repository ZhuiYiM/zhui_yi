<template>
  <div class="private-message-list">
    <div v-if="conversations.length === 0" class="empty-state">
      <el-empty description="暂无私信" />
    </div>
    
    <div v-else class="conversation-items">
      <div
        v-for="conversation in conversations"
        :key="conversation.senderId"
        class="conversation-item"
        :class="{ 'is-unread': conversation.unreadCount > 0 }"
        @click="handleClick(conversation)"
      >
        <div class="conversation-avatar" @click.stop="goToUserProfile(conversation)">
          <el-avatar 
            :src="conversation.senderAvatar" 
            :size="50"
            style="cursor: pointer;"
          />
        </div>
        
        <div class="conversation-content">
          <div class="conversation-header">
            <span class="conversation-sender">{{ conversation.senderName }}</span>
            <div class="header-actions">
              <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
              <el-button
                type="danger"
                size="small"
                text
                icon="Delete"
                class="delete-btn"
                @click.stop="handleDelete(conversation)"
              >
              </el-button>
            </div>
          </div>
          
          <div class="conversation-preview">
            {{ conversation.lastMessage }}
          </div>
        </div>
        
        <div v-if="conversation.unreadCount > 0" class="unread-badge">
          {{ conversation.unreadCount }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
  conversations: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['conversation-click', 'conversation-delete']);

// 跳转到用户对外展示页面
const goToUserProfile = (conversation) => {
  const userId = conversation.senderId;
  router.push(`/user/profile/${userId}`);
};

const formatTime = (date) => {
  if (!date) return '';
  const dateObj = new Date(date);
  const now = new Date();
  const diffMs = now - dateObj;
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return dateObj.toLocaleDateString();
  }
};

const handleClick = (conversation) => {
  emit('conversation-click', conversation);
};

const handleDelete = (conversation) => {
  emit('conversation-delete', conversation);
};
</script>

<style scoped>
.private-message-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.empty-state {
  padding: 60px 20px;
}

.conversation-items {
  max-height: 600px;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.is-unread {
  background-color: #f0f9ff;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-avatar {
  margin-right: 15px;
  flex-shrink: 0;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversation-sender {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
  padding: 12px 16px !important;
  margin-left: 4px;
  min-width: auto !important;
  width: auto !important;
  height: auto !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 20px !important;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: #ffe6e6 !important;
  color: #f56c6c !important;
  transform: scale(1.2);
}

/* 移动端始终显示删除按钮 */
@media (max-width: 768px) {
  .delete-btn {
    opacity: 1 !important;
  }
}

.conversation-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  flex-shrink: 0;
  margin-left: 10px;
}
</style>
