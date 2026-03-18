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
            :class="msg.senderId === currentUserId ? 'message-item self' : 'message-item other'"
          >
            <template v-if="msg.senderId !== currentUserId">
              <div class="message-avatar">
                <img :src="otherUser.avatar" :alt="otherUser.name">
              </div>
              <div class="message-bubble">
                <div class="message-content">
                  <img 
                    v-if="isImageMessage(msg)" 
                    :src="getImageUrl(msg)" 
                    alt="图片" 
                    class="message-image"
                    @click="handleImagePreview(getImageUrl(msg))"
                  />
                  <template v-else>{{ msg.content }}</template>
                </div>
                <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              </div>
            </template>
            
            <template v-else>
              <div class="message-bubble self">
                <div class="message-content">
                  <img 
                    v-if="isImageMessage(msg)" 
                    :src="getImageUrl(msg)" 
                    alt="图片" 
                    class="message-image"
                    @click="handleImagePreview(getImageUrl(msg))"
                  />
                  <template v-else>{{ msg.content }}</template>
                </div>
                <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              </div>
              <div class="message-avatar">
                <img :src="currentUser.avatar" :alt="currentUser.name">
              </div>
            </template>
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
          <!-- 功能按钮栏 -->
          <div class="toolbar">
            <el-button 
              :icon="Picture" 
              circle 
              size="small"
              @click="selectImage"
              title="发送图片"
            />
            <el-button 
              :icon="Folder" 
              circle 
              size="small"
              @click="selectFile"
              title="发送文件"
            />
            <el-button 
              :icon="Microphone" 
              circle 
              size="small"
              @click="toggleVoiceRecording"
              :loading="isRecording"
              title="语音消息"
            />
            <el-button 
              :icon="VideoCamera" 
              circle 
              size="small"
              @click="startVideoCall"
              title="视频通话 (todo)"
            />
            <el-button 
              :icon="PictureFilled" 
              circle 
              size="small"
              @click="showEmojiPicker = !showEmojiPicker"
              title="表情包"
            />
          </div>
          
          <!-- 表情包选择器 -->
          <div v-if="showEmojiPicker" class="emoji-picker">
            <div class="emoji-grid">
              <span 
                v-for="emoji in emojis" 
                :key="emoji"
                class="emoji-item"
                @click="insertEmoji(emoji)"
              >{{ emoji }}</span>
            </div>
          </div>
          
          <!-- 输入框 -->
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息... (Ctrl+Enter 发送)"
              @keydown.ctrl.enter="sendMessage"
              resize="none"
            />
          </div>
          
          <!-- 录音状态 -->
          <div v-if="isRecording" class="recording-status">
            <el-icon class="is-recording"><Microphone /></el-icon>
            <span>正在录音... {{ recordingTime }}s</span>
            <el-button size="small" @click="stopRecording">停止</el-button>
          </div>
          
          <!-- 已选择的文件 -->
          <div v-if="selectedFile" class="selected-file">
            <span class="file-name">{{ selectedFile.name }}</span>
            <el-button size="small" @click="cancelFile">取消</el-button>
          </div>
          
          <!-- 操作按钮 -->
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
import { Loading, Picture, Folder, Microphone, VideoCamera, PictureFilled } from '@element-plus/icons-vue';
import { messageAPI } from '@/api/message';
import { uploadAPI } from '@/api/upload';

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

// 新增功能相关
const showEmojiPicker = ref(false);
const isRecording = ref(false);
const recordingTime = ref(0);
const recordingTimer = ref(null);
const selectedFile = ref(null);
const mediaRecorder = ref(null);
const audioChunks = ref([]);

// 表情包列表
const emojis = ['😀', '😂', '😍', '🤔', '👍', '👎', '👌', '✌️', '🎉', '❤️', '🔥', '⭐', '💯', '🙏', '😊', '🥳', '😎', '🤗', '😭', '😡'];

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
    const response = await messageAPI.sendMessage({
      toUserId: props.targetUserId,
      content: inputMessage.value.trim(),
      type: 'private_message'
    });
    
    console.log('发送消息响应:', response);
    
    // 重新加载消息列表，确保显示最新消息
    await loadMessages();
    
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

// 判断是否是图片消息
const isImageMessage = (msg) => {
  try {
    if (!msg.content) return false;
    // 只有当 content 看起来像 JSON 时才尝试解析（以 { 开头）
    if (typeof msg.content === 'string' && !msg.content.trim().startsWith('{')) {
      return false;
    }
    const content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content;
    return content.type === 'image';
  } catch (e) {
    // 解析失败说明不是 JSON，直接返回 false
    return false;
  }
};

// 获取图片 URL
const getImageUrl = (msg) => {
  try {
    if (!msg.content) return '';
    // 只有当 content 看起来像 JSON 时才尝试解析
    if (typeof msg.content === 'string' && !msg.content.trim().startsWith('{')) {
      return '';
    }
    const content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content;
    // 优先使用 url 字段，如果没有则使用 fileName 字段
    let url = content.url || content.fileName || '';
    
    // 确保 URL 包含 /images/ 路径
    if (url && !url.startsWith('/images/')) {
      url = '/images/' + url;
    }
    
    // 如果 URL 不以 http 开头，需要添加后端地址
    if (url && !url.startsWith('http://') && !url.startsWith('https://')) {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
      url = baseUrl + url;
    }
    
    return url;
  } catch (e) {
    return '';
  }
};

// 图片预览
const handleImagePreview = (url) => {
  window.open(url, '_blank');
};

// 插入表情
const insertEmoji = (emoji) => {
  inputMessage.value += emoji;
  showEmojiPicker.value = false;
};

// 选择图片
const selectImage = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      await uploadFile(file, 'image');
    }
  };
  input.click();
};

// 选择文件
const selectFile = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.onchange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      selectedFile.value = file;
    }
  };
  input.click();
};

// 上传文件
const uploadFile = async (file, type) => {
  try {
    const response = await uploadAPI.uploadImage(file);
    console.log('📦 上传完整响应:', response);
    
    // 响应拦截器已经返回了 responseData.data，所以 response 就是 fileUrl
    let fileUrl = response;
    
    // 兼容不同的响应格式
    if (response && typeof response === 'object') {
      fileUrl = response.url || response.data || response.fileUrl;
    }
    
    console.log('📤 上传返回的 URL:', fileUrl);
    
    if (!fileUrl) {
      throw new Error('上传未返回 URL');
    }
    
    // 从 URL 中提取文件名（去掉 /images/ 前缀）
    const fileName = fileUrl.replace('/images/', '');
    
    // 发送文件消息，使用服务器返回的文件名
    await sendFileMessage(fileUrl, type, fileName);
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败：' + (error.message || '请稍后重试'));
  }
};

// 发送文件消息
const sendFileMessage = async (url, type, fileName) => {
  try {
    console.log('📥 准备发送的文件信息:', { url, type, fileName });
    
    const content = JSON.stringify({
      type: type,
      url: url,
      fileName: fileName
    });
    
    console.log('📝 消息内容:', content);
    
    await messageAPI.sendMessage({
      toUserId: props.targetUserId,
      content: content,
      type: 'private_message'
    });
    
    // 重新加载消息列表，确保显示最新消息
    await loadMessages();
    
    scrollToBottom();
    ElMessage.success('发送成功');
  } catch (error) {
    console.error('发送失败:', error);
    ElMessage.error('发送失败');
  }
};

// 切换录音状态
const toggleVoiceRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
};

// 开始录音
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder.value = new MediaRecorder(stream);
    audioChunks.value = [];
    
    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data);
    };
    
    mediaRecorder.value.onstop = async () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' });
      const audioFile = new File([audioBlob], 'voice.webm', { type: 'audio/webm' });
      await uploadFile(audioFile, 'voice');
    };
    
    mediaRecorder.value.start();
    isRecording.value = true;
    recordingTime.value = 0;
    
    // 计时器
    recordingTimer.value = setInterval(() => {
      recordingTime.value++;
    }, 1000);
    
    ElMessage.info('开始录音');
  } catch (error) {
    console.error('录音失败:', error);
    ElMessage.error('无法访问麦克风');
  }
};

// 停止录音
const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop();
    isRecording.value = false;
    clearInterval(recordingTimer.value);
    ElMessage.success('录音已发送');
  }
};

// 取消文件
const cancelFile = () => {
  selectedFile.value = null;
};

// 视频通话 (todo)
const startVideoCall = () => {
  ElMessage.info('视频通话功能开发中...');
  // TODO: 实现视频通话功能
  // 可以使用 WebRTC 或第三方 SDK（如 Agora、腾讯云等）
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

/* 对方消息：整体靠左 */
.message-item.other {
  justify-content: flex-start;
}

/* 自己消息：整体靠右 */
.message-item.self {
  justify-content: flex-end;
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

/* 对方头像边框为绿色 */
.message-item.other .message-avatar img {
  border-color: #67C23A;
}

/* 自己头像边框为蓝色 */
.message-item.self .message-avatar img {
  border-color: #4A90E2;
}

.message-bubble {
  max-width: 60%;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 自己消息气泡为蓝色 */
.message-bubble.self {
  background: #4A90E2;
  color: white;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-image {
  max-width: 300px;
  max-height: 300px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: contain;
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

.toolbar {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
}

.emoji-picker {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.2s;
  text-align: center;
}

.emoji-item:hover {
  transform: scale(1.2);
}

.input-wrapper {
  margin-bottom: 12px;
}

.recording-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  margin-bottom: 12px;
  color: #f56c6c;
}

.selected-file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 12px;
}

.file-name {
  font-size: 14px;
  color: #666;
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
