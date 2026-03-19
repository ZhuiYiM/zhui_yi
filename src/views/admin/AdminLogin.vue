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
              clearable
            >
              <template #prefix>
                <span style="margin-right: 8px;">👤</span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <span style="margin-right: 8px;">🔒</span>
              </template>
            </el-input>
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
        console.log('登录响应:', response);
        
        // request.js 拦截器已经处理了响应，直接返回 data 对象
        if (response && response.token) {
          // 保存 Token 和用户信息
          localStorage.setItem('admin_token', response.token);
          const adminInfo = {
            id: response.adminId,
            username: response.username,
            realName: response.realName,
            roleId: response.roleId
          };
          localStorage.setItem('admin_info', JSON.stringify(adminInfo));
          console.log('已保存 admin_token:', response.token);
          console.log('已保存 admin_info:', adminInfo);
          
          ElMessage.success('登录成功');
          
          // 直接跳转到 dashboard 首页
          console.log('准备跳转到 /admin/dashboard/home');
          router.push('/admin/dashboard/home');
        } else {
          console.error('登录响应缺少 token:', response);
          ElMessage.error(response?.message || '登录失败');
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
  position: relative;
  overflow: hidden;
}

.admin-login-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-container {
  width: 100%;
  max-width: 460px;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.login-box {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 48px 40px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #718096;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-top: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.login-tips {
  margin-top: 24px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  border-radius: 12px;
  font-size: 13px;
  color: #4a5568;
  line-height: 1.8;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.login-tips p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.login-tips p::before {
  content: '💡';
  font-size: 16px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
  
  .login-box {
    padding: 36px 28px;
    border-radius: 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-btn {
    height: 44px;
    font-size: 15px;
  }
}
</style>
