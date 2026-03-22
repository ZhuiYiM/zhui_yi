<template>
  <div class="change-password">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Lock /></el-icon>
          修改密码
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">修改密码</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>密码修改</span>
        </div>
      </template>

      <el-form 
        ref="passwordFormRef"
        :model="passwordForm" 
        :rules="passwordRules"
        label-width="120px"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
            clearable
          />
          <div class="form-tip">密码长度至少 6 位，包含字母和数字</div>
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 200px;">
            <el-icon><Check /></el-icon>
            确认修改
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { Lock, HomeFilled, Check, Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const loading = ref(false);
const passwordFormRef = ref(null);

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度至少 6 位'));
  } else {
    // 检查是否包含字母和数字
    const hasLetter = /[a-zA-Z]/.test(value);
    const hasNumber = /\d/.test(value);
    if (!hasLetter || !hasNumber) {
      callback(new Error('密码需包含字母和数字'));
    } else {
      callback();
    }
  }
};

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 提交修改
const handleSubmit = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await adminAPI.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        
        console.log('🔑 修改密码响应:', response);
        // 响应拦截器返回的是 responseData.data，即 "密码修改成功" 字符串
        if (response === '密码修改成功' || typeof response === 'string') {
          ElMessage.success('密码修改成功，请重新登录');
          // 清空表单
          handleReset();
          // 延迟跳转登录页
          setTimeout(() => {
            localStorage.removeItem('adminToken');
            localStorage.removeItem('adminInfo');
            window.location.href = '/admin/login';
          }, 1500);
        } else {
          ElMessage.error(response?.message || '修改失败');
        }
      } catch (error) {
        console.error('❌ 修改密码失败:', error);
        ElMessage.error(error.response?.data?.message || '修改失败，请稍后重试');
      } finally {
        loading.value = false;
      }
      }
  });
};

// 重置表单
const handleReset = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};
</script>

<style scoped>
.change-password {
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

.password-card {
  border-radius: 12px;
  max-width: 600px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
