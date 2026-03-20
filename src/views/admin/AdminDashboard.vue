<template>
  <div class="admin-dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-aside">
        <div class="logo">
          <h2>管理后台</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/admin/dashboard/home">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/dashboard/users">用户列表</el-menu-item>
            <el-menu-item index="/admin/dashboard/verifications">认证审核</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/admin/dashboard/topics">话题管理</el-menu-item>
            <el-menu-item index="/admin/dashboard/products">商品管理</el-menu-item>
            <el-menu-item index="/admin/dashboard/locations">地点管理</el-menu-item>
            <el-menu-item index="/admin/dashboard/comments">评论管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="report">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>举报管理</span>
            </template>
            <el-menu-item index="/admin/dashboard/reports">举报列表</el-menu-item>
            <el-menu-item index="/admin/dashboard/report-stats">举报统计</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/dashboard/logs">
            <el-icon><List /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/dashboard/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="admin-header">
          <div class="header-left">
            <h3>校园信息平台管理系统</h3>
            <div class="breadcrumb-nav">
              <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
                <el-icon><HomeFilled /></el-icon>
                首页
              </el-button>
              <span class="separator">/</span>
              <span class="current-page" v-if="currentRouteName !== 'DashboardHome'">
                {{ getPageTitle() }}
              </span>
            </div>
          </div>
          <div class="header-right">
            <el-button text @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              返回主页
            </el-button>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="adminInfo.avatar" />
                <span class="username">{{ adminInfo.realName || adminInfo.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="password">修改密码</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { DataAnalysis, User, Document, Warning, List, Setting, HomeFilled } from '@element-plus/icons-vue';
import { adminAPI } from '@/api/admin';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => route.name);

// 获取页面标题
const getPageTitle = () => {
  const titleMap = {
    'DashboardHome': '数据统计',
    'AdminUsers': '用户管理',
    'AdminVerifications': '认证审核',
    'AdminTopics': '话题管理',
    'AdminProducts': '商品管理',
    'AdminLocations': '地点管理',
    'AdminComments': '评论管理',
    'AdminReports': '举报管理',
    'AdminReportStats': '举报统计',
    'AdminLogs': '操作日志',
    'AdminSettings': '系统设置'
  };
  return titleMap[route.name] || '管理页面';
};

const adminInfo = ref({
  id: null,
  username: '',
  realName: '',
  avatar: '',
  roleId: null
});

// 加载管理员信息
onMounted(async () => {
  const info = localStorage.getItem('admin_info');
  if (info) {
    adminInfo.value = JSON.parse(info);
  }
});

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile');
      break;
    case 'password':
      router.push('/admin/change-password');
      break;
    case 'logout':
      await handleLogout();
      break;
  }
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await adminAPI.logout();
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_info');
    ElMessage.success('已退出登录');
    router.push('/admin/login');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出失败:', error);
    }
  }
};
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-aside {
  background: linear-gradient(180deg, #2d3748 0%, #1a202c 100%);
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  color: white;
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

:deep(.el-menu) {
  background: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin: 4px 8px;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.3) 0%, rgba(67, 56, 202, 0.3) 100%);
  color: #fff;
}

:deep(.el-sub-menu__title) {
  border-radius: 8px;
  margin: 4px 8px;
  transition: all 0.3s ease;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.admin-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.header-left h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.breadcrumb-nav .separator {
  color: #c0c4cc;
  margin: 0 4px;
}

.breadcrumb-nav .current-page {
  color: #303133;
  font-weight: 500;
}

.breadcrumb-nav .back-home-btn {
  color: #409EFF;
  padding: 0;
  font-size: 13px;
}

.breadcrumb-nav .back-home-btn:hover {
  color: #66b1ff;
}

.breadcrumb .el-breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  color: #409EFF;
  background: #f5f7fa;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f7fafc;
}

.username {
  margin-left: 12px;
  color: #2d3748;
  font-size: 14px;
  font-weight: 500;
}

.admin-main {
  background: #f5f7fa;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .admin-aside {
    display: none;
  }
  
  .admin-header {
    padding: 0 16px;
  }
  
  .header-left h3 {
    font-size: 16px;
  }
  
  .admin-main {
    padding: 16px;
  }
}
</style>
