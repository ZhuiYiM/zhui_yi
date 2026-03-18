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
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users">用户列表</el-menu-item>
            <el-menu-item index="/admin/roles">角色管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/admin/topics">话题管理</el-menu-item>
            <el-menu-item index="/admin/products">商品管理</el-menu-item>
            <el-menu-item index="/admin/comments">评论管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="report">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>举报管理</span>
            </template>
            <el-menu-item index="/admin/reports">举报列表</el-menu-item>
            <el-menu-item index="/admin/report-stats">举报统计</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/logs">
            <el-icon><List /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
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
          </div>
          <div class="header-right">
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
import { DataAnalysis, User, Document, Warning, List, Setting } from '@element-plus/icons-vue';
import { adminAPI } from '@/api/admin';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => route.path);

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
}

.admin-aside {
  background-color: #304156;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
}

.logo h2 {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.admin-header {
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 10px;
  color: #606266;
  font-size: 14px;
}

.admin-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
