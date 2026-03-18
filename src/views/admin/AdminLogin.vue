<template>
  <div class="admin-login-page">
    <div class="login-container">
      <div class="login-box">
        <h1 class="login-title">校园信息平台 - 管理后台</h1>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-tips">
          <p>默认账号：admin</p>
          <p>默认密码：admin123</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await adminAPI.login(loginForm.username, loginForm.password);
        
        if (response.code === 200) {
          // 保存 Token 和用户信息
          localStorage.setItem('admin_token', response.data.token);
          localStorage.setItem('admin_info', JSON.stringify({
            id: response.data.adminId,
            username: response.data.username,
            realName: response.data.realName,
            roleId: response.data.roleId
          }));
          
          ElMessage.success('登录成功');
          router.push('/admin/dashboard');
        } else {
          ElMessage.error(response.message || '登录失败');
        }
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error('登录失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 30px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.login-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}

.login-tips p {
  margin: 0;
}
</style>
