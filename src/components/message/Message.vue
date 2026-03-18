<template>
  <div :class="['message-page', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="message"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 - 移动端显示标题在搜索栏上方 -->
      <header class="header">
        <h1 v-if="isMobile">消息中心</h1>
        <div class="search-bar">
          <!-- 使用统一搜索组件 -->
          <UnifiedSearch
            v-model="searchQuery"
            :available-tags="messageTags"
            :enable-result-page="true"
            :default-search-type="'user'"
            placeholder="搜索联系人、群聊、消息..."
            tag-selector-title="选择消息类型"
            :show-quick-filters="false"
            :show-clear-button="false"
            @search="handleSearch"
          />
        </div>
      </header>

      <!-- 消息类型导航 -->
      <section class="message-nav-section">
        <MessageTabs
          v-model="activeTab"
          :unread-count="unreadCount"
          @tab-change="switchTab"
        />
      </section>

      <!-- 消息列表区域 -->
      <section class="messages-section">
        <div class="section-header">
          <h2>{{ activeTabTitle }}</h2>
          <button v-if="!isMobile" class="view-more-btn" @click="viewAllMessages">
            查看全部
          </button>
          <span v-else class="view-more" @click="viewAllMessages">
            查看全部 >
          </span>
        </div>

        <!-- 根据标签类型显示不同的列表 -->
        <template v-if="activeTab === 'personal' || activeTab === 'all'">
          <PrivateMessageList
            :conversations="displayedConversations"
            @conversation-click="openPrivateChat"
            @conversation-delete="confirmDeleteConversation"
          />
        </template>
        <template v-else>
          <MessageList
            :messages="displayedMessages"
            @message-click="openMessage"
          />
        </template>
      </section>

      <!-- 移动端使用统一导航组件 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>

    <!-- 私信聊天对话框 -->
    <PrivateChatDialog
      v-model="showChatDialog"
      :target-user-id="chatTargetUserId"
      :target-user-info="chatTargetUserInfo"
      @close="handleChatClose"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { messageAPI } from '@/api/message';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import UnifiedSearch from '@/components/common/UnifiedSearch.vue';
import MessageTabs from './MessageTabs.vue';
import MessageList from './MessageList.vue';
import PrivateMessageList from './PrivateMessageList.vue';
import PrivateChatDialog from './PrivateChatDialog.vue';

const router = useRouter(); // 创建路由实例

// 用户信息
const currentUser = ref({
  name: '',
  studentId: '',
  avatar: ''
});

// 获取用户信息
const getUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  currentUser.value = {
    id: user.id,
    name: user.name || user.username || '',
    studentId: user.studentId || user.student_id || '',
    avatar: user.avatar || ''
  };
};

// 设备检测相关
const isMobile = ref(false);

// 更新设备检测状态
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  updateDeviceDetection();
  getUserInfo();
  window.addEventListener('resize', updateDeviceDetection);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});

// 消息类型标签
const messageTabs = ref([
  { id: 'all', name: '全部', icon: '✉️', count: 12 },
  { id: 'system', name: '系统通知', icon: '📢', count: 3 },
  { id: 'personal', name: '私信', icon: '👤', count: 5 },
  { id: 'group', name: '群聊', icon: '👥', count: 4 }
]);

// 消息数据
const messages = ref([]);
const loading = ref(false);
const unreadCount = ref(0);
const activeTab = ref('all');
const currentPage = ref(1);
const pageSize = ref(20);
const searchQuery = ref(''); // 搜索关键词

// 消息标签（用于搜索筛选）
const messageTags = computed(() => {
  return [
    { code: 'personal', name: '私信', color: '#4A90E2' },
    { code: 'system', name: '系统通知', color: '#7B68EE' },
    { code: 'group', name: '群聊', color: '#FFA500' },
    { code: 'order', name: '订单通知', color: '#FF6347' },
    { code: 'topic', name: '话题互动', color: '#32CD32' }
  ];
});

// 私信相关数据
const privateMessages = ref([]);
const privateConversations = ref([]); // 聚合后的私信对话

// 私信对话框相关
const showChatDialog = ref(false);
const chatTargetUserId = ref(null);
const chatTargetUserInfo = ref({});

// 获取消息列表
const fetchMessages = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page,
      size: pageSize.value,
      type: activeTab.value === 'all' ? '' : activeTab.value
    };
    
    const response = await messageAPI.getMessages(params);
    
    // 正确解析后端返回的数据
    const responseData = response.data || response;
    const records = responseData.records || responseData.data?.records || [];
    
    messages.value = records.map(msg => ({
      ...msg,
      read: msg.isRead === 1 || msg.isRead === true,
      sender: msg.senderName || '未知用户',
      avatar: msg.senderAvatar || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=' + (msg.senderName?.charAt(0) || 'U'),
      preview: msg.content || '',
      timestamp: new Date(msg.createdAt || msg.timestamp),
      type: msg.messageType === 'system' ? '系统通知' :
            msg.messageType === 'private_message' ? '私信' :
            msg.messageType === 'order_notification' ? '订单通知' : msg.messageType
    }));
    
    currentPage.value = page;
    
    // 如果是私信标签，获取私信列表
    if (activeTab.value === 'personal' || activeTab.value === 'all') {
      await fetchPrivateMessages();
    }
  } catch (error) {
    console.error('获取消息列表失败:', error);
    ElMessage.error('获取消息列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取私信列表
const fetchPrivateMessages = async () => {
  try {
    const response = await messageAPI.getMessages({ type: 'private_message' });
    
    const responseData = response.data || response;
    privateMessages.value = responseData.records || responseData.data?.records || [];
    
    // 聚合私信为对话
    aggregateConversations();
  } catch (error) {
    console.error('获取私信列表失败:', error);
  }
};

// 聚合私信为对话
const aggregateConversations = () => {
  const currentUserId = currentUser.value.id;
  const conversationMap = new Map();
  
  privateMessages.value.forEach(msg => {
    // 对于私信，按对话的另一方聚合（对方 ID）
    let otherUserId, otherUserName, otherUserAvatar;
    
    // 如果是自己发送的消息，对方是接收者；如果是别人发来的，对方是发送者
    if (msg.senderId === currentUserId) {
      // 自己发送的消息，对方是接收者
      otherUserId = msg.receiverId;
      otherUserName = msg.receiverName || '未知用户';
      otherUserAvatar = msg.receiverAvatar || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=U';
    } else {
      // 别人发来的消息，对方是发送者
      otherUserId = msg.senderId;
      otherUserName = msg.senderName || msg.sender?.username || '未知用户';
      otherUserAvatar = msg.senderAvatar || msg.sender?.avatarUrl || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=' + (msg.senderName?.charAt(0) || 'U');
    }
    
    if (!conversationMap.has(otherUserId)) {
      conversationMap.set(otherUserId, {
        senderId: otherUserId,
        senderName: otherUserName,
        senderAvatar: otherUserAvatar,
        lastMessage: msg.content,
        lastMessageTime: new Date(msg.createdAt || msg.timestamp),
        unreadCount: msg.senderId !== currentUserId && (msg.isRead === 0 || msg.isRead === false) ? 1 : 0,
        messages: [msg]
      });
    } else {
      const conv = conversationMap.get(otherUserId);
      // 更新最新消息
      const msgTime = new Date(msg.createdAt || msg.timestamp);
      if (msgTime > conv.lastMessageTime) {
        conv.lastMessage = msg.content;
        conv.lastMessageTime = msgTime;
      }
      // 累加未读数（只有对方发来的未读消息才计数）
      if (msg.senderId !== currentUserId && (msg.isRead === 0 || msg.isRead === false)) {
        conv.unreadCount++;
      }
      conv.messages.push(msg);
    }
  });
  
  // 转换为数组并按最后消息时间排序
  privateConversations.value = Array.from(conversationMap.values())
    .sort((a, b) => b.lastMessageTime - a.lastMessageTime);
};

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const response = await messageAPI.getUnreadCount();
    unreadCount.value = response.data || response.count || 0;
  } catch (error) {
    console.error('获取未读消息数失败:', error);
  }
};

// 标记消息为已读
const markMessageAsRead = async (messageId) => {
  try {
    await messageAPI.markAsRead(messageId);
    // 更新本地数据
    const message = messages.value.find(m => m.id === messageId);
    if (message) {
      message.isRead = true;
    }
    // 更新未读数
    await fetchUnreadCount();
  } catch (error) {
    console.error('标记已读失败:', error);
  }
};

// 切换标签
const switchTab = (tabId) => {
  activeTab.value = tabId;
  fetchMessages(1);
};

// 页面初始化
onMounted(() => {
  fetchMessages(1);
  fetchUnreadCount();
});

// 计算属性：当前标签标题
const activeTabTitle = computed(() => {
  const tab = messageTabs.value.find(t => t.id === activeTab.value);
  return tab ? tab.name : '全部';
});

// 计算属性：根据当前标签过滤消息
const displayedMessages = computed(() => {
  // 全部和私信标签都显示会话列表
  if (activeTab.value === 'all' || activeTab.value === 'personal') {
    return [];
  }
  
  // 系统通知和群聊显示消息列表
  if (!messages.value) return [];
  
  return messages.value.filter(msg => {
    if (!msg) return false;
    const msgType = msg.messageType || msg.type;
    return activeTab.value === 'system' ? msgType === 'system' :
        activeTab.value === 'group' ? msgType === 'group' : false;
  });
});

// 计算属性：聚合后的会话列表（用于全部和私信标签）
const displayedConversations = computed(() => {
  const conversationMap = new Map();
  const currentMessages = privateMessages.value;
  const currentUserId = currentUser.value.id;
  
  if (!currentMessages || currentMessages.length === 0) return [];
  
  currentMessages.forEach(msg => {
    // 对于私信，按对话的另一方聚合（对方 ID）
    let otherUserId, otherUserName, otherUserAvatar;
    
    // 如果是自己发送的消息，对方是接收者；如果是别人发来的，对方是发送者
    if (msg.senderId === currentUserId) {
      otherUserId = msg.receiverId;
      otherUserName = msg.receiverName || '未知用户';
      otherUserAvatar = msg.receiverAvatar || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=U';
    } else {
      otherUserId = msg.senderId;
      otherUserName = msg.senderName || msg.sender?.username || '未知用户';
      otherUserAvatar = msg.senderAvatar || msg.sender?.avatarUrl || 'https://placehold.co/50x50/4A90E2/FFFFFF?text=' + (msg.senderName?.charAt(0) || 'U');
    }
    
    const msgTime = new Date(msg.createdAt || msg.timestamp);
    
    if (!conversationMap.has(otherUserId)) {
      conversationMap.set(otherUserId, {
        senderId: otherUserId,
        senderName: otherUserName,
        senderAvatar: otherUserAvatar,
        lastMessage: msg.content,
        lastMessageTime: msgTime,
        unreadCount: msg.senderId !== currentUserId && (msg.isRead === 0 || msg.isRead === false) ? 1 : 0,
        type: '私信'
      });
    } else {
      const conv = conversationMap.get(otherUserId);
      // 更新最新消息
      if (msgTime > conv.lastMessageTime) {
        conv.lastMessage = msg.content;
        conv.lastMessageTime = msgTime;
      }
      // 累加未读数
      if (msg.senderId !== currentUserId && (msg.isRead === 0 || msg.isRead === false)) {
        conv.unreadCount++;
      }
    }
  });
  
  // 转换为数组并按最后消息时间排序
  return Array.from(conversationMap.values())
    .sort((a, b) => b.lastMessageTime - a.lastMessageTime);
});

// 打开消息详情
const openMessage = (message) => {
  // 标记为已读
  message.read = true;
  // 实际项目中可以跳转到消息详情页面
};

// 打开私信对话框
const openPrivateChat = (conversation) => {
  // 标记该对话的所有消息为已读
  markConversationAsRead(conversation.senderId);
  
  // 打开聊天对话框
  chatTargetUserId.value = conversation.senderId;
  chatTargetUserInfo.value = {
    senderName: conversation.senderName,
    senderAvatar: conversation.senderAvatar
  };
  showChatDialog.value = true;
  
  ElMessage.info(`正在加载与 ${conversation.senderName} 的对话`);
};

// 标记对话为已读
const markConversationAsRead = async (otherUserId) => {
  try {
    const currentUserId = currentUser.value.id;
    // 找到该对话的所有未读消息（包括发送和接收的）
    const unreadMessages = privateMessages.value.filter(
      msg => {
        const msgOtherUserId = msg.senderId === currentUserId ? msg.receiverId : msg.senderId;
        return msgOtherUserId === otherUserId && (msg.isRead === 0 || msg.isRead === false);
      }
    );
    
    // 批量标记已读
    if (unreadMessages.length > 0) {
      const messageIds = unreadMessages.map(msg => msg.id);
      await messageAPI.markAsReadBatch(messageIds);
      
      // 更新本地数据
      unreadMessages.forEach(msg => {
        msg.isRead = 1;
      });
      
      // 更新未读数
      await fetchUnreadCount();
      
      // 重新聚合对话
      await aggregateConversations();
    }
  } catch (error) {
    console.error('标记已读失败:', error);
  }
};

// 处理聊天对话框关闭
const handleChatClose = () => {
  showChatDialog.value = false;
  // 刷新消息列表
  fetchMessages(1);
};

// 确认删除对话
const confirmDeleteConversation = async (conversation) => {
  // 使用 Element Plus 的确认对话框
  ElMessageBox.confirm(
    `确定要删除与 ${conversation.senderName} 的对话吗？删除后无法恢复。`,
    '删除对话',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await deleteConversation(conversation);
  }).catch(() => {
    // 用户取消删除
    ElMessage.info('已取消删除');
  });
};

// 删除对话
const deleteConversation = async (conversation) => {
  try {
    const otherUserId = conversation.senderId;
    
    // 调用后端 API 删除对话
    // 注意：这里假设有批量删除消息的 API，如果没有需要实现
    const messagesToDelete = privateMessages.value
      .filter(msg => {
        const msgOtherUserId = msg.senderId === currentUser.value.id ? msg.receiverId : msg.senderId;
        return msgOtherUserId === otherUserId;
      })
      .map(msg => msg.id);
    
    if (messagesToDelete.length > 0) {
      // 批量删除消息
      await messageAPI.deleteMessagesBatch(messagesToDelete);
      
      // 从本地数据中移除
      privateMessages.value = privateMessages.value.filter(
        msg => {
          const msgOtherUserId = msg.senderId === currentUser.value.id ? msg.receiverId : msg.senderId;
          return msgOtherUserId !== otherUserId;
        }
      );
      
      // 重新聚合对话
      await aggregateConversations();
      
      // 刷新消息列表
      await fetchMessages(currentPage.value);
      
      // 更新未读数
      await fetchUnreadCount();
      
      ElMessage.success('对话已删除');
    } else {
      ElMessage.warning('没有找到要删除的消息');
    }
  } catch (error) {
    console.error('删除对话失败:', error);
    ElMessage.error('删除对话失败，请稍后重试');
  }
};

// 查看全部消息
const viewAllMessages = () => {
  // 实际项目中可以跳转到完整消息列表页面
};

// 格式化时间
const formatTime = (date) => {
  const now = new Date();
  const diffMs = now - date;
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    // 今天
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

// 导航相关
const goToPage = (page) => {
  switch(page) {
    case 'home':
      router.push('/');
      break;
    case 'map':
      router.push('/map');
      break;
    case 'trade':  // 跳转到交易中心
      router.push('/mall');
      break;
    case 'topic':  // 跳转到话题墙
      router.push('/topicwall');
      break;
    case 'message':  // 这是当前页面，可以刷新或滚动到顶部
      router.push('/message');
      break;
    case 'profile':
      router.push('/personalcenter');
      break;
    default:
      console.log(`未知页面：${page}`);
  }
};
</script>


<style scoped>
.message-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: grid;
  grid-template-columns: 200px 1fr;
  grid-template-areas: "sidebar main";
}

.message-page.mobile {
  grid-template-columns: 1fr;
  grid-template-areas: "main";
  padding-bottom: 80px;
}

.main-content {
  grid-area: main;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.main-content.full-width {
  max-width: 100%;
}

.header {
  margin-bottom: 20px;
}

.header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 15px;
}

.search-bar {
  background: white;
  padding: 15px;
  border-radius: 12px;
}

.mobile-search-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-mini {
  flex-shrink: 0;
}

.user-avatar-mini img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.mobile-search-header input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.message-nav-section {
  margin-bottom: 20px;
}

.messages-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.view-more-btn {
  padding: 8px 16px;
  background-color: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.view-more-btn:hover {
  background-color: #e0e0e0;
}

.view-more {
  color: #4A90E2;
  font-size: 14px;
  cursor: pointer;
}

.desktop-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 用户信息区域样式 */
.user-info {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f8f9fa;
}

.user-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #4A90E2;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-id {
  color: #666;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 主内容区域 */
.main-content {
  grid-area: main;
  padding: 0;
  overflow-y: auto;
  margin-left: 40px; /* 与侧边栏宽度一致 */
}

/* 通用部分样式 */
.section {
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
  color: #333;
}

.view-more {
  color: #4A90E2;
  font-size: 0.9rem;
  cursor: pointer;
}

.view-more-btn {
  background-color: #4A90E2;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

/* 消息类型标签样式 */
.message-type-tabs {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
  margin-bottom: 20px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: #f5f7fa;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-item:hover {
  background-color: #e3f2fd;
}

.tab-item.active {
  background-color: #4A90E2;
  color: white;
}

.tab-count {
  background-color: #ff6b6b;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 0.7rem;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 消息列表样式 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 聚合后的会话列表样式 */
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

/* 私信对话列表样式（保留旧代码兼容） */
.private-messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.private-message-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  position: relative;
}

.private-message-item:hover {
  background: #f0f7ff;
  transform: translateX(3px);
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.15);
}

.private-message-item.unread {
  background: #e9f7fe;
  border-left: 3px solid #4A90E2;
}

.private-message-avatar {
  position: relative;
  flex-shrink: 0;
}

.private-message-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #4A90E2;
}

.private-message-avatar .unread-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #ff6b6b;
  border-radius: 50%;
  border: 2px solid white;
}

.private-message-content {
  flex: 1;
  min-width: 0;
}

.private-message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.private-message-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.private-message-preview .message-text {
  color: #666;
  font-size: 0.9rem;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.unread-count-badge {
  background: #ff6b6b;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 0.75rem;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.message-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.message-item:hover {
  background-color: #e9f7fe;
  transform: translateX(5px);
}

.message-item.unread {
  background-color: #e9f7fe;
  border-left: 3px solid #4A90E2;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.sender-name {
  font-weight: bold;
  color: #333;
}

.message-time {
  font-size: 0.8rem;
  color: #777;
}

.message-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.message-text {
  color: #666;
  font-size: 0.9rem;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  color: #4A90E2;
  font-size: 1.2rem;
  margin-left: 10px;
}

.message-actions {
  display: flex;
  justify-content: space-between;
}

.message-type {
  font-size: 0.8rem;
  color: #999;
  background-color: #e3f2fd;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  gap: 10px;
  width: 100%;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-bar input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-bar button {
  padding: 10px 15px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.search-bar button:hover {
  background-color: #764ba2;
}

/* 移动端底部导航样式统一 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 15px;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.15);
  z-index: 1000;
}

.mobile-nav .nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.mobile-nav .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 8px 0;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.mobile-nav .nav-item:hover {
  background: #f0f7ff;
}

.mobile-nav .nav-item.active {
  background: #e3f2fd;
}

.mobile-nav .nav-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.mobile-nav .nav-item.active .nav-icon {
  background: #4A90E2;
  color: white;
  transform: scale(1.1);
}

.mobile-nav .nav-text {
  font-size: 0.7rem;
  color: #666;
  transition: all 0.3s ease;
}

.mobile-nav .nav-item.active .nav-text {
  color: #4A90E2;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-nav .nav-grid {
    gap: 2px;
  }
  
  .mobile-nav .nav-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }
  
  .mobile-nav .nav-text {
    font-size: 0.6rem;
  }
}
</style>