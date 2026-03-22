<template>
  <div class="admin-profile">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          个人信息
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">个人信息</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" @click="handleEdit" :loading="saving">
            <el-icon><Edit /></el-icon>
            保存修改
          </el-button>
        </div>
      </template>

      <el-form :model="profile" label-width="100px" size="large">
        <el-form-item label="用户名">
          <el-input v-model="profile.username" placeholder="请输入用户名" disabled />
        </el-form-item>
        
        <el-form-item label="姓名">
          <el-input v-model="profile.realName" placeholder="请输入姓名" />
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="profile.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="头像">
          <el-input v-model="profile.avatar" placeholder="请输入头像 URL" />
        </el-form-item>
        
        <el-form-item label="角色 ID">
          <el-input v-model="profile.roleId" placeholder="角色 ID" disabled />
        </el-form-item>
        
        <el-form-item label="创建时间">
          <span>{{ profile.createdAt ? new Date(profile.createdAt).toLocaleString() : '-' }}</span>
        </el-form-item>
        
        <el-form-item label="最后登录">
          <span>{{ profile.lastLoginTime ? new Date(profile.lastLoginTime).toLocaleString() : '-' }}</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, HomeFilled, Edit } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const loading = ref(false);
const saving = ref(false);

const profile = reactive({
  username: '',
  nickname: '',
  realName: '',
  avatar: '',
  roleId: null,
  createdAt: null,
  lastLoginTime: null
});

// 加载个人信息
const loadProfile = async () => {
  loading.value = true;
  try {
    const response = await adminAPI.getInfo();
    console.log('👤 个人信息响应:', response);
    
    if (response && response.username) {
      profile.username = response.username || '';
      profile.realName = response.realName || '';
      profile.nickname = response.nickname || response.realName || '';
      profile.avatar = response.avatar || '';
      profile.roleId = response.roleId || null;
      profile.createdAt = response.createdAt || null;
      profile.lastLoginTime = response.lastLoginTime || null;
      console.log('✅ 个人信息加载成功');
    } else {
      ElMessage.error('加载失败');
      console.error('数据格式错误:', response);
    }
  } catch (error) {
    console.error('❌ 加载个人信息失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 保存修改
const handleEdit = async () => {
  saving.value = true;
  try {
    const response = await adminAPI.updateProfile({
      realName: profile.realName,
      nickname: profile.nickname,
      avatar: profile.avatar
    });
    
    console.log('✏️ 更新个人信息响应:', response);
    if (response.code === 200 || response.message === '更新成功') {
      ElMessage.success('保存成功');
      // 重新加载最新信息
      loadProfile();
    } else {
      ElMessage.error(response.message || '保存失败');
    }
  } catch (error) {
    console.error('❌ 保存失败:', error);
    ElMessage.error('保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.admin-profile {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.profile-card {
  border-radius: 12px;
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
