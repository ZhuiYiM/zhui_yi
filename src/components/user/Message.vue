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
          <div v-if="isMobile" class="mobile-search-header">
            <div class="user-avatar-mini">
              <img :src="currentUser.avatar || 'https://placehold.co/24x24/4A90E2/FFFFFF?text=' + (currentUser.name?.charAt(0) || 'U')" :alt="currentUser.name">
            </div>
            <input
                type="text"
                v-model="searchQuery"
                placeholder="搜索联系人、群聊、消息..."
                @keyup.enter="performSearch"
            >
          </div>
          <div v-else class="desktop-search">
            <input
                type="text"
                v-model="searchQuery"
                placeholder="搜索联系人、群聊、消息..."
                @keyup.enter="performSearch"
            >
            <button @click="performSearch">搜索</button>
          </div>
        </div>
      </header>

      <!-- 消息类型导航 -->
      <section class="message-nav-section">
        <div class="section-header">
          <h2>消息分类</h2>
        </div>
        <div class="message-type-tabs">
          <div
              v-for="tab in messageTabs"
              :key="tab.id"
              :class="['tab-item', { active: activeTab === tab.id }]"
              @click="switchTab(tab.id)"
          >
            <span class="tab-icon">{{ tab.icon }}</span>
            <span class="tab-name">{{ tab.name }}</span>
            <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
          </div>
        </div>
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

        <!-- 消息列表 -->
        <div class="messages-list">
          <div
              v-for="message in displayedMessages"
              :key="message.id"
              :class="['message-item', { unread: !message.read }]"
              @click="openMessage(message)"
          >
            <div class="message-avatar">
              <img :src="message.avatar" :alt="message.sender">
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="sender-name">{{ message.sender }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
              <div class="message-preview">
                <span class="message-text">{{ message.preview }}</span>
                <span v-if="!message.read" class="unread-badge">•</span>
              </div>
              <div class="message-actions">
                <span class="message-type">{{ message.type }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 移动端使用统一导航组件 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { messageAPI } from '@/api/message';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';

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
    messages.value = response.data;
    currentPage.value = page;
  } catch (error) {
    console.error('获取消息列表失败:', error);
    ElMessage.error('获取消息列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const response = await messageAPI.getUnreadCount();
    unreadCount.value = response.count;
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

// 计算属性：根据当前标签过滤消息
const displayedMessages = computed(() => {
  if (activeTab.value === 'all') {
    return messages.value;
  }
  return messages.value.filter(msg =>
      activeTab.value === 'system' ? msg.type === '系统通知' :
          activeTab.value === 'personal' ? msg.type === '私信' :
              activeTab.value === 'group' ? msg.type === '群聊' : []
  );
});

// 计算属性：当前标签标题
const activeTabTitle = computed(() => {
  const tab = messageTabs.value.find(t => t.id === activeTab.value);
  return tab ? tab.name : '全部';
});

// 执行搜索
const performSearch = () => {
  console.log('搜索消息:', searchQuery.value);
  // 实际项目中可以调用搜索API
};

// 打开消息详情
const openMessage = (message) => {
  console.log('打开消息:', message.id);
  // 标记为已读
  message.read = true;
  // 实际项目中可以跳转到消息详情页面
};

// 查看全部消息
const viewAllMessages = () => {
  console.log('查看全部消息');
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
  console.log(`跳转到${page}页面`);
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
      console.log(`未知页面: ${page}`);
  }
};
</script>


<style scoped>
.message-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: grid;
  grid-template-columns: 200px 1fr; /* 侧边栏宽度 */
  grid-template-areas: "sidebar main";
}

.message-page.mobile {
  grid-template-columns: 1fr;
  grid-template-areas: "main";
  padding-bottom: 80px; /* 为移动底部导航留出空间 */
}

/* 统一导航栏样式 */
.nav-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-menu li {
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  color: #666;
  font-size: 0.95rem;
  font-weight: 500;
}

.nav-menu li:hover {
  background-color: #f0f7ff;
  color: #4A90E2;
  transform: translateX(5px);
}

.nav-menu li.active {
  background-color: #4A90E2;
  color: white;
  font-weight: 600;
}

.nav-menu span:first-child {
  font-size: 1.2rem;
  min-width: 24px;
  text-align: center;
}

/* 侧边栏样式优化 */
.sidebar {
  grid-area: sidebar;
  background-color: white;
  padding: 20px;
  border-right: 1px solid #e0e0e0;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1001;
  width: 200px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
}

.logo h1 {
  color: #4A90E2;
  margin: 0 0 25px 0;
  font-size: 1.3rem;
  font-weight: 600;
  text-align: center;
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

/* 移动端布局调整 */
@media (max-width: 767px) {
  .message-page {
    padding-bottom: 70px;
  }
  
  .sidebar {
    display: none;
  }
  
  .main-content {
    margin-left: 0;
    padding: 15px;
  }
  
  .header {
    padding: 15px 20px;
    align-items: stretch;
  }
  
  .header h1 {
    font-size: 1.5rem;
    margin-bottom: 10px;
  }
  
  .section {
    margin: 15px 10px;
    padding: 15px;
  }
  
  .message-type-tabs {
    gap: 8px;
  }
  
  .tab-item {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
  
  .message-item {
    padding: 12px;
  }
  
  .message-header {
    flex-direction: column;
    gap: 5px;
  }
  
  .message-time {
    align-self: flex-end;
  }
  
  .message-preview {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>

/* 桌面端底部样式 - 放置在main-content内部底部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

/* 响应式设计 */
/* 移动端顶部用户信息样式 */
.mobile-user-header {
  background-color: white;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.user-info-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-small img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.user-text-top {
  flex: 1;
  min-width: 0;
}

.username-small {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-id-small {
  color: #666;
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 移动端搜索框样式 */
.mobile-search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.user-avatar-mini img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #4A90E2;
}

.mobile-search-header input {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #e1e5f2;
  border-radius: 20px;
  font-size: 0.9rem;
}

/* 移动端布局调整 */
@media (max-width: 767px) {
  .message-page {
    padding-bottom: 70px; /* 为移动底部导航留出空间 */
  }
  
  .sidebar {
    display: none;
  }
  
  .main-content {
    margin-left: 0;
    padding: 15px;
  }
  
  .header {
    padding: 15px 20px;
    align-items: stretch;
  }
  
  .header h1 {
    font-size: 1.5rem;
    margin-bottom: 10px;
  }
  
  .section {
    margin: 15px 10px;
    padding: 15px;
  }
  
  .message-type-tabs {
    gap: 8px;
  }
  
  .tab-item {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
  
  .message-item {
    padding: 12px;
  }
  
  .message-header {
    flex-direction: column;
    gap: 5px;
  }
  
  .message-time {
    align-self: flex-end;
  }
  
  .message-preview {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}