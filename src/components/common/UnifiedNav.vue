<template>
  <!-- PC端侧边栏导航 -->
  <aside v-if="!isMobile && showSidebar" class="sidebar">
    <div class="logo">
      <h1>校园信息平台</h1>
    </div>
    
    <!-- 用户信息区域 -->
    <div v-if="showUserInfo && hasUserInfo" class="user-info">
      <div class="user-avatar">
        <img 
          :src="avatarUrl" 
          :alt="displayUserName"
          @error="handleAvatarError"
        >
      </div>
      <div class="user-details">
        <div class="username">{{ displayUserName }}</div>
        <div class="student-id">学号: {{ displayStudentId }}</div>
      </div>
    </div>
    
    <!-- 游客信息区域 -->
    <div v-else-if="showUserInfo" class="guest-info">
      <div class="guest-avatar">
        👤
      </div>
      <div class="guest-details">
        <div class="guest-text">请登录</div>
      </div>
    </div>
    
    <!-- 导航菜单 -->
    <div class="nav-menu">
      <ul>
        <li 
          v-for="item in navItems" 
          :key="item.id"
          :class="{ active: item.id === currentPage }"
          @click="navigateTo(item.id)"
        >
          <span>{{ item.icon }}</span>
          <span>{{ item.name }}</span>
        </li>
      </ul>
    </div>
  </aside>

  <!-- 移动端底部导航栏 -->
  <div v-if="isMobile && showMobileNav" class="mobile-nav">
    <div class="nav-grid">
      <div 
        v-for="item in mobileNavItems" 
        :key="item.id"
        :class="['nav-item', { active: item.id === currentPage }]"
        @click="navigateTo(item.id)"
      >
        <div class="nav-icon">{{ item.icon }}</div>
        <span class="nav-text">{{ item.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  isMobile: {
    type: Boolean,
    default: false
  },
  currentPage: {
    type: String,
    default: ''
  },
  showSidebar: {
    type: Boolean,
    default: true
  },
  showMobileNav: {
    type: Boolean,
    default: true
  },
  showUserInfo: {
    type: Boolean,
    default: true
  },
  userInfo: {
    type: Object,
    default: () => ({
      name: '',
      studentId: '',
      avatar: ''
    })
  }
})

const router = useRouter()

// 计算属性：判断是否有用户信息
const hasUserInfo = computed(() => {
  return props.userInfo && 
         (props.userInfo.name || props.userInfo.username || props.userInfo.nickname)
})

// 计算属性：显示用户名
const displayUserName = computed(() => {
  const user = props.userInfo
  return user?.name || user?.nickname || user?.username || '用户'
})

// 计算属性：显示学号
const displayStudentId = computed(() => {
  const user = props.userInfo
  return user?.studentId || user?.student_id || user?.schoolId || '未设置'
})

// 计算属性：头像URL
const avatarUrl = computed(() => {
  const user = props.userInfo
  const avatar = user?.avatar || user?.profilePicture
  
  if (avatar) {
    // 如果是完整URL
    if (avatar.startsWith('http')) {
      return avatar
    }
    // 如果是相对路径
    if (avatar.startsWith('/')) {
      return `http://localhost:8080${avatar}`
    }
    return avatar
  }
  
  // 默认头像
  const firstChar = displayUserName.value.charAt(0).toUpperCase()
  return `https://placehold.co/60x60/4A90E2/FFFFFF?text=${firstChar}`
})

// 处理头像加载错误
const handleAvatarError = (event) => {
  const firstChar = displayUserName.value.charAt(0).toUpperCase()
  event.target.src = `https://placehold.co/60x60/4A90E2/FFFFFF?text=${firstChar}`
}

// PC端导航项配置
const navItems = [
  { id: 'map', name: '地图导引', icon: '🗺️' },
  { id: 'mall', name: '交易中心', icon: '🛒' },
  { id: 'message', name: '消息中心', icon: '✉️' },
  { id: 'topicwall', name: '校园话题墙', icon: '💬' },
  { id: 'personalcenter', name: '个人中心', icon: '👤' }
]

// 移动端导航项配置（简化版）
const mobileNavItems = [
  { id: 'map', name: '地图', icon: '🗺️' },
  { id: 'mall', name: '交易', icon: '🛒' },
  { id: 'message', name: '消息', icon: '✉️' },
  { id: 'topicwall', name: '话题', icon: '💬' },
  { id: 'personalcenter', name: '我的', icon: '👤' }
]

// 导航函数
const navigateTo = (pageId) => {
  switch(pageId) {
    case 'home':
      router.push('/')
      break
    case 'map':
      router.push('/map')
      break
    case 'mall':
      router.push('/mall')
      break
    case 'message':
      router.push('/message')
      break
    case 'topicwall':
      router.push('/topicwall')
      break
    case 'personalcenter':
      router.push('/personalcenter')
      break
    default:
      router.push('/')
  }
}
</script>

<style scoped>
/* PC端侧边栏样式 */
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
  margin-bottom: 20px;
}

.guest-info {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f8f9fa;
  margin-bottom: 20px;
}

.user-avatar img,
.guest-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #4A90E2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  background-color: #e9ecef;
  color: #6c757d;
}

.user-details,
.guest-details {
  flex: 1;
  min-width: 0;
}

.username,
.guest-text {
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

.guest-text {
  color: #6c757d;
  font-weight: normal;
  margin-bottom: 0;
}

/* 导航菜单样式 */
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

/* 移动端底部导航样式 */
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
  transition: all 0.3s ease;
  padding: 5px 0;
}

.mobile-nav .nav-item:hover {
  transform: translateY(-2px);
}

.mobile-nav .nav-item.active {
  transform: translateY(-2px);
}

.mobile-nav .nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin-bottom: 2px;
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

/* 隐藏侧边栏时的主内容区域调整 */
.main-content.full-width {
  margin-left: 0;
}
</style>
