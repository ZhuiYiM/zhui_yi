<template>
  <div class="private-chat-dialog">
    <el-dialog
      v-model="dialogVisible"
      :title="chatTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="closeChat"
    >
      <!-- 聊天内容区域 -->
      <div class="chat-container">
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', msg.senderId === currentUserId ? 'self' : 'other']"
          >
            <div v-if="msg.senderId !== currentUserId" class="message-avatar">
              <img :src="otherUser.avatar" :alt="otherUser.name">
            </div>
            <div class="message-bubble">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
            <div v-if="msg.senderId === currentUserId" class="message-avatar self">
              <img :src="currentUser.avatar" :alt="currentUser.name">
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-messages">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
        </div>
      </div>

      <!-- 输入框区域 -->
      <template #footer>
        <div class="chat-input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息... (Ctrl+Enter 发送)"
            @keydown.ctrl.enter="sendMessage"
            resize="none"
          />
          <div class="input-actions">
            <el-button @click="closeChat">关闭</el-button>
            <el-button type="primary" @click="sendMessage" :loading="sending">
              发送
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { messageAPI } from '@/api/message';

const props = defineProps({
  modelValue: Boolean,
  targetUserId: {
    type: Number,
    default: null
  },
  targetUserInfo: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:modelValue', 'close']);

// 用户信息
const currentUser = ref({
  id: null,
  name: '',
  avatar: ''
});

const otherUser = ref({
  id: null,
  name: '',
  avatar: ''
});

// 聊天数据
const dialogVisible = ref(false);
const messages = ref([]);
const loading = ref(false);
const sending = ref(false);
const inputMessage = ref('');
const messageListRef = ref(null);

// 计算属性
const currentUserId = computed(() => currentUser.value.id);
const chatTitle = computed(() => `与 ${otherUser.value.name} 的聊天`);

// 监听对话框显示
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal && props.targetUserId) {
    initChat();
  }
});

watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal);
  if (!newVal) {
    emit('close');
  }
});

// 监听 targetUserId 变化，切换用户时重新加载
watch(() => props.targetUserId, (newUserId) => {
  if (newUserId && dialogVisible.value) {
    initChat();
  }
}, { immediate: true });

// 获取当前用户信息
const getCurrentUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  currentUser.value = {
    id: user.id,
    name: user.username || user.name,
    avatar: user.avatar || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=' + (user.username?.charAt(0) || 'U')
  };
};

// 初始化聊天
const initChat = async () => {
  getCurrentUserInfo();
  
  // 确保当前用户 ID 已获取
  if (!currentUserId.value) {
    console.error('当前用户 ID 未获取到');
    return;
  }
  
  otherUser.value = {
    id: props.targetUserId,
    name: props.targetUserInfo.senderName || '未知用户',
    avatar: props.targetUserInfo.senderAvatar || 'https://placehold.co/50x50/67C23A/FFFFFF?text=U'
  };
  
  console.log('初始化聊天:', {
    currentUserId: currentUserId.value,
    targetUserId: props.targetUserId,
    targetUserInfo: props.targetUserInfo
  });
  
  // 每次切换用户或第一次打开时都重新加载消息
  await loadMessages();
  scrollToBottom();
};

// 加载消息
const loadMessages = async () => {
  loading.value = true;
  try {
    const response = await messageAPI.getMessages({ 
      type: 'private_message',
      page: 1,
      size: 50
    });
    
    const responseData = response.data || response;
    const allMessages = responseData.records || responseData.data?.records || [];
    
    console.log('加载到的所有消息:', allMessages);
    console.log('过滤条件:', {
      targetUserId: props.targetUserId,
      currentUserId: currentUserId.value
    });
    
    // 过滤出与当前用户的对话并按时间升序排序
    messages.value = allMessages.filter(msg => 
      (msg.senderId === props.targetUserId && msg.receiverId === currentUserId.value) ||
      (msg.senderId === currentUserId.value && msg.receiverId === props.targetUserId)
    ).map(msg => {
      // 正确处理时间
      let timestamp;
      if (msg.createdAt) {
        // 如果是字符串，直接解析；如果是对象，转换为字符串
        timestamp = new Date(msg.createdAt.toString());
      } else if (msg.timestamp) {
        timestamp = new Date(msg.timestamp);
      } else {
        timestamp = new Date();
      }
      
      return {
        ...msg,
        timestamp: timestamp
      };
    }).sort((a, b) => a.timestamp - b.timestamp); // 按时间升序排序
    
    console.log('过滤后的消息:', messages.value);
    
    // 标记为已读
    const unreadMessages = messages.value.filter(
      msg => msg.senderId === props.targetUserId && msg.isRead === 0
    );
    
    if (unreadMessages.length > 0) {
      const messageIds = unreadMessages.map(msg => msg.id);
      await messageAPI.markAsReadBatch(messageIds);
      
      // 更新本地状态
      unreadMessages.forEach(msg => {
        msg.isRead = 1;
      });
    }
    
  } catch (error) {
    console.error('加载消息失败:', error);
    ElMessage.error('加载消息失败');
  } finally {
    loading.value = false;
  }
};

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容');
    return;
  }
  
  sending.value = true;
  try {
    await messageAPI.sendMessage({
      toUserId: props.targetUserId,
      content: inputMessage.value.trim(),
      type: 'private_message'
    });
    
    // 添加到消息列表
    messages.value.push({
      id: Date.now(), // 临时 ID
      senderId: currentUserId.value,
      receiverId: props.targetUserId,
      content: inputMessage.value.trim(),
      messageType: 'private_message',
      isRead: 1,
      timestamp: new Date()
    });
    
    inputMessage.value = '';
    scrollToBottom();
    
    ElMessage.success('发送成功');
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error(error.response?.data?.message || '发送失败，请稍后重试');
  } finally {
    sending.value = false;
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
};

// 格式化时间
const formatTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const hours = d.getHours().toString().padStart(2, '0');
  const minutes = d.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

// 关闭聊天
const closeChat = () => {
  dialogVisible.value = false;
  inputMessage.value = '';
  // 不清空 messages，保持历史记录
};

onMounted(() => {
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('keydown', handleKeyDown);
});

// 处理键盘事件
const handleKeyDown = (e) => {
  // Esc 关闭对话框
  if (e.key === 'Escape' && dialogVisible.value) {
    closeChat();
  }
};
</script>

<style scoped>
.private-chat-dialog {
  position: fixed;
  z-index: 9999;
}

.chat-container {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e0e0e0;
}

.message-avatar.self img {
  border-color: #4A90E2;
}

.message-bubble {
  max-width: 60%;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-item.self .message-bubble {
  background: #4A90E2;
  color: white;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.6;
  margin-top: 4px;
  text-align: right;
}

.loading-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #999;
}

.chat-input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
