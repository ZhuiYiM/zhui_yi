<template>
  <div :class="['account-management', { 'mobile': isMobile }]">
    <!-- 移动端底部导航栏 -->
    <div v-if="isMobile" class="mobile-nav">
      <div class="nav-grid">
        <div class="nav-item" @click="goToPage('map')">
          <div class="nav-icon">🗺️</div>
        </div>
        <div class="nav-item" @click="goToPage('trade')">
          <div class="nav-icon">🛒</div>
        </div>
        <div class="nav-item" @click="goToPage('topic')">
          <div class="nav-icon">💬</div>
        </div>
        <div class="nav-item" @click="goToPage('message')">
          <div class="nav-icon">✉️</div>
        </div>
        <div class="nav-item" @click="goToPage('profile')">
          <div class="nav-icon">👤</div>
        </div>
      </div>
    </div>



    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 -->
      <header class="header">
        <div class="header-content">
          <button v-if="!isMobile" class="back-btn" @click="goBack">← 返回个人中心</button>
          <h1>{{ isMobile ? '账号管理' : '账号管理' }}</h1>
        </div>
      </header>

      <!-- 账号管理内容区域 -->
      <section class="account-content">
        <div class="unified-management-container">
          <!-- 统一的标题 -->
          <div class="main-header">
            <h1>🔧 账号管理中心</h1>
            <p>一站式管理您的账号安全、隐私设置和绑定信息</p>
          </div>
          
          <!-- 统一的功能卡片容器 -->
          <div class="unified-cards-container">
            <!-- 账号安全相关卡片 -->
            <div class="function-group">
              <h2>🔐 账号安全</h2>
              <div class="cards-row">
                <!-- 密码管理 -->
                <div class="card compact-card">
                  <div class="card-header">
                    <h3>🔑 修改密码</h3>
                    <p>定期更换提高安全性</p>
                  </div>
                  <div class="card-content">
                    <button @click="showChangePasswordDialog" class="primary-btn small">修改密码</button>
                  </div>
                </div>
                
                <!-- 安全保护 -->
                <div class="card compact-card">
                  <div class="card-header">
                    <h3>🛡️ 二次验证</h3>
                    <p>增强账号安全</p>
                  </div>
                  <div class="card-content">
                    <div class="toggle-setting">
                      <label class="switch">
                        <input type="checkbox" v-model="securitySettings.twoFactorAuth">
                        <span class="slider"></span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 隐私设置 -->
            <div class="function-group">
              <h2>👁️ 隐私控制</h2>
              <div class="cards-row">
                <!-- 信息可见性 -->
                <div class="card compact-card">
                  <div class="card-header">
                    <h3>👤 信息可见性</h3>
                    <p>控制信息公开范围</p>
                  </div>
                  <div class="card-content">
                    <div class="privacy-settings compact">
                      <div class="setting-item">
                        <label>手机号</label>
                        <select v-model="privacySettings.phoneVisibility">
                          <option value="public">公开</option>
                          <option value="friends">好友可见</option>
                          <option value="private">仅自己</option>
                        </select>
                      </div>
                      <div class="setting-item">
                        <label>邮箱</label>
                        <select v-model="privacySettings.emailVisibility">
                          <option value="public">公开</option>
                          <option value="friends">好友可见</option>
                          <option value="private">仅自己</option>
                        </select>
                      </div>
                      <div class="setting-item">
                        <label>学号</label>
                        <select v-model="privacySettings.studentIdVisibility">
                          <option value="public">公开</option>
                          <option value="friends">好友可见</option>
                          <option value="private">仅自己</option>
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 数据管理 -->
                <div class="card compact-card">
                  <div class="card-header">
                    <h3>💾 数据操作</h3>
                    <p>管理个人数据</p>
                  </div>
                  <div class="card-content">
                    <div class="data-actions vertical">
                      <button @click="downloadUserData" class="secondary-btn small">📥 下载数据</button>
                      <button @click="showDeleteAccountDialog" class="danger-btn small">🗑️ 注销账号</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 账号绑定 -->
            <div class="function-group">
              <h2>🔗 账号绑定</h2>
              <div class="card full-width-card">
                <div class="card-content">
                  <div class="binding-list compact">
                    <div class="binding-item compact">
                      <div class="binding-info">
                        <div class="binding-icon">📱</div>
                        <div class="binding-details">
                          <h4>手机号</h4>
                          <p>{{ currentUser.phone || '未绑定' }}</p>
                        </div>
                      </div>
                      <button 
                        @click="goToVerification('phone')"
                        :class="['primary-btn small', { 'secondary': currentUser.phone }]"
                      >
                        {{ currentUser.phone ? '更换' : '绑定' }}
                      </button>
                    </div>

                    <div class="binding-item compact">
                      <div class="binding-info">
                        <div class="binding-icon">📧</div>
                        <div class="binding-details">
                          <h4>邮箱</h4>
                          <p>{{ currentUser.email || '未绑定' }}</p>
                        </div>
                      </div>
                      <button 
                        @click="goToVerification('email')"
                        :class="['primary-btn small', { 'secondary': currentUser.email }]"
                      >
                        {{ currentUser.email ? '更换' : '绑定' }}
                      </button>
                    </div>

                    <div class="binding-item compact">
                      <div class="binding-info">
                        <div class="binding-icon">🌐</div>
                        <div class="binding-details">
                          <h4>微信</h4>
                          <p>{{ socialBindings.wechat ? '已绑定' : '未绑定' }}</p>
                        </div>
                      </div>
                      <button 
                        @click="goToVerification('wechat')"
                        :class="['primary-btn small', { 'secondary': socialBindings.wechat }]"
                      >
                        {{ socialBindings.wechat ? '解绑' : '绑定' }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 退出登录 -->
            <div class="function-group">
              <h2>🚪 账号退出</h2>
              <div class="card full-width-card">
                <div class="card-content">
                  <div class="logout-section">
                    <div class="logout-info">
                      <h3>安全退出</h3>
                      <p>退出当前账号登录，保护您的账号安全</p>
                    </div>
                    <button @click="logout" class="danger-btn">🚪 退出登录</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>

    <!-- 修改密码对话框 -->
    <div v-if="showPasswordDialog" class="dialog-overlay" @click="closePasswordDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="closePasswordDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="changePassword">
            <div class="form-group">
              <label>原密码</label>
              <input 
                type="password" 
                v-model="passwordForm.oldPassword" 
                placeholder="请输入原密码"
                required
              />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input 
                type="password" 
                v-model="passwordForm.newPassword" 
                placeholder="请输入新密码"
                required
                minlength="6"
              />
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input 
                type="password" 
                v-model="passwordForm.confirmPassword" 
                placeholder="请再次输入新密码"
                required
              />
            </div>
            <div class="dialog-actions">
              <button type="button" @click="closePasswordDialog" class="cancel-btn">取消</button>
              <button type="submit" class="confirm-btn">确认修改</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 注销账号对话框 -->
    <div v-if="showDeleteDialog" class="dialog-overlay" @click="closeDeleteDialog">
      <div class="dialog danger" @click.stop>
        <div class="dialog-header">
          <h3>注销账号</h3>
          <button class="close-btn" @click="closeDeleteDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="warning-message">
            <p>⚠️ 警告：此操作将永久删除您的账号及所有相关数据，包括：</p>
            <ul>
              <li>个人资料和认证信息</li>
              <li>发布的所有话题和评论</li>
              <li>交易记录和订单信息</li>
              <li>消息记录和联系人</li>
            </ul>
            <p>此操作不可撤销，请谨慎操作。</p>
          </div>
          <div class="form-group">
            <label>请输入"DELETE ACCOUNT"确认</label>
            <input 
              type="text" 
              v-model="deleteConfirmText" 
              placeholder="DELETE ACCOUNT"
              @input="validateDeleteConfirm"
            />
          </div>
          <div class="dialog-actions">
            <button type="button" @click="closeDeleteDialog" class="cancel-btn">取消</button>
            <button 
              type="button" 
              @click="deleteAccount" 
              class="danger-btn"
              :disabled="!deleteConfirmed"
            >
              确认注销
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { userAPI } from '@/api/user';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();

// 设备检测
const isMobile = ref(false);
const currentPage = ref('account');

// 移除了标签页相关配置

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false
});

// 隐私设置
const privacySettings = reactive({
  phoneVisibility: 'private',
  emailVisibility: 'private',
  studentIdVisibility: 'private'
});

// 当前用户信息
const currentUser = reactive({
  phone: '',
  email: ''
});

// 社交绑定状态
const socialBindings = reactive({
  wechat: false
});

// 对话框状态
const showPasswordDialog = ref(false);
const showDeleteDialog = ref(false);

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 删除确认
const deleteConfirmText = ref('');
const deleteConfirmed = ref(false);

// 更新设备检测状态
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

// 移除了标签页切换方法

// 返回个人中心
const goBack = () => {
  router.push('/personalcenter');
};

// 导航到其他页面
const goToPage = (page) => {
  switch(page) {
    case 'map':
      router.push('/map');
      break;
    case 'trade':
      router.push('/mall');
      break;
    case 'topic':
      router.push('/topicwall');
      break;
    case 'message':
      router.push('/message');
      break;
    case 'profile':
      router.push('/personalcenter');
      break;
  }
};

// 跳转到认证页面
const goToVerification = (type) => {
  console.log(`跳转到${type}认证页面`);
  router.push({
    path: '/account/verification',
    query: { type: type }
  });
};

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  showPasswordDialog.value = true;
};

// 关闭修改密码对话框
const closePasswordDialog = () => {
  showPasswordDialog.value = false;
  // 重置表单
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

// 修改密码
const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致');
    return;
  }

  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('新密码长度至少为6位');
    return;
  }

  try {
    await userAPI.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    ElMessage.success('密码修改成功');
    closePasswordDialog();
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败');
  }
};

// 退出登录
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 执行退出登录
    await authStore.logout();
    ElMessage.success('已退出登录');
    
    // 跳转到首页
    router.push('/');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败');
    }
  }
};

// 下载用户数据
const downloadUserData = async () => {
  try {
    // 这里应该调用API下载用户数据
    // const response = await userAPI.downloadUserData();
    
    ElMessage.success('数据下载请求已提交，请稍后查看邮箱');
  } catch (error) {
    ElMessage.error(error.message || '数据下载失败');
  }
};

// 显示注销账号对话框
const showDeleteAccountDialog = () => {
  showDeleteDialog.value = true;
};

// 关闭注销账号对话框
const closeDeleteDialog = () => {
  showDeleteDialog.value = false;
  deleteConfirmText.value = '';
  deleteConfirmed.value = false;
};

// 验证删除确认文本
const validateDeleteConfirm = () => {
  deleteConfirmed.value = deleteConfirmText.value === 'DELETE ACCOUNT';
};

// 注销账号
const deleteAccount = async () => {
  if (!deleteConfirmed.value) {
    ElMessage.error('请输入正确的确认文本');
    return;
  }

  try {
    await ElMessageBox.confirm('确定要永久注销账号吗？此操作不可撤销！', '严重警告', {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'error'
    });

    // 这里应该调用API注销账号
    // await userAPI.deleteAccount();
    
    ElMessage.success('账号注销成功，正在退出...');
    
    // 清除本地存储并跳转到登录页
    setTimeout(() => {
      authStore.logout();
      router.push('/login');
    }, 1500);
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('账号注销失败');
    }
  }
};

// 显示手机绑定对话框
const showPhoneBindingDialog = () => {
  ElMessage.info('手机绑定功能开发中');
};

// 显示邮箱绑定对话框
const showEmailBindingDialog = () => {
  ElMessage.info('邮箱绑定功能开发中');
};

// 切换微信绑定
const toggleWechatBinding = async () => {
  try {
    if (socialBindings.wechat) {
      await ElMessageBox.confirm('确定要解除微信绑定吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      });
      
      await userAPI.unbindSocial('wechat');
      socialBindings.wechat = false;
      ElMessage.success('微信绑定已解除');
    } else {
      // 这里应该跳转到微信授权页面
      ElMessage.info('微信绑定功能开发中');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败');
    }
  }
};

// 保存安全设置
const saveSecuritySettings = async () => {
  try {
    await userAPI.updateSecuritySettings({
      twoFactorAuth: securitySettings.twoFactorAuth
    });
    ElMessage.success('安全设置保存成功');
  } catch (error) {
    ElMessage.error(error.message || '保存失败');
  }
};

// 保存隐私设置
const savePrivacySettings = async () => {
  try {
    await userAPI.updatePrivacySettings(privacySettings);
    ElMessage.success('隐私设置保存成功');
  } catch (error) {
    ElMessage.error(error.message || '保存失败');
  }
};

// 页面初始化
onMounted(async () => {
  updateDeviceDetection();
  window.addEventListener('resize', updateDeviceDetection);
  
  // 初始化用户数据
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  currentUser.phone = user.phone || '';
  currentUser.email = user.email || '';
  
  // 获取安全设置
  try {
    const securityResponse = await userAPI.getSecuritySettings();
    if (securityResponse.data) {
      securitySettings.twoFactorAuth = securityResponse.data.twoFactorAuth || false;
    }
  } catch (error) {
    console.log('获取安全设置失败:', error.message);
  }
  
  // 获取隐私设置
  try {
    const privacyResponse = await userAPI.getPrivacySettings();
    if (privacyResponse.data) {
      Object.assign(privacySettings, privacyResponse.data);
    }
  } catch (error) {
    console.log('获取隐私设置失败:', error.message);
  }
  
  // 获取登录设备列表（已移除设备管理功能）
  // 此处不再需要获取设备列表
  console.log('设备管理功能已移除');
});

// 监听安全设置变化
watch(() => securitySettings.twoFactorAuth, () => {
  saveSecuritySettings();
});

// 监听隐私设置变化
watch(privacySettings, () => {
  savePrivacySettings();
}, { deep: true });

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.account-management {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.account-management.mobile {
  padding-bottom: 80px;
}

/* 主内容区域 */
.main-content {
  padding: 0px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.main-content.full-width {
  padding-bottom: 80px;
}

.header {
  background-color: #4A90E2;
  color: white;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.back-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  margin-right: 20px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background-color: rgba(255,255,255,0.2);
}

.header h1 {
  margin: 0;
  font-size: 1.8rem;
}

/* 移动端底部导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 10px 15px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 4px rgba(0,0,0,0.1);
  z-index: 1000;
  flex-shrink: 0;
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
}

/* 账号内容区域 */
.account-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.unified-management-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.main-header {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #4A90E2 0%, #357abd 100%);
  color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.2);
}

.main-header h1 {
  margin: 0 0 10px 0;
  font-size: 1.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.main-header p {
  margin: 0;
  opacity: 0.9;
  font-size: 1rem;
}

.unified-cards-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.function-group {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.function-group h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 2px solid #4A90E2;
}

.cards-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 15px;
}

.full-width-card {
  grid-column: 1 / -1;
}

.card {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0,0,0,0.1);
}

.compact-card {
  padding: 12px;
}

.card-header {
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.card-header h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 0.85rem;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.card h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.2rem;
  border-bottom: 2px solid #4A90E2;
  padding-bottom: 10px;
  display: inline-block;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item.danger {
  border-left: 3px solid #ff4757;
  padding-left: 15px;
}

.toggle-setting {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.toggle-setting span {
  font-weight: 500;
  color: #333;
}

.primary-btn, .secondary-btn, .danger-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.primary-btn.small, .secondary-btn.small, .danger-btn.small {
  padding: 6px 12px;
  font-size: 0.85rem;
}

.primary-btn {
  background-color: #4A90E2;
  color: white;
}

.primary-btn:hover {
  background-color: #357abd;
  transform: translateY(-1px);
}

.primary-btn.secondary {
  background-color: #6c757d;
}

.primary-btn.secondary:hover {
  background-color: #5a6268;
}

.secondary-btn {
  background-color: #6c757d;
  color: white;
  padding: 12px 24px;
  font-size: 1rem;
  min-width: 120px;
}

.secondary-btn.small {
  padding: 10px 20px;
  font-size: 0.9rem;
  min-width: 100px;
}

.secondary-btn:hover {
  background-color: #5a6268;
  transform: translateY(-1px);
}

.danger-btn {
  background-color: #ff4757;
  color: white;
  padding: 12px 24px;
  font-size: 1rem;
  min-width: 120px;
}

.danger-btn.small {
  padding: 10px 20px;
  font-size: 0.9rem;
  min-width: 100px;
}

.danger-btn:hover:not(:disabled) {
  background-color: #ff2e42;
  transform: translateY(-1px);
}

.danger-outline {
  background-color: white;
  color: #ff4757;
  border: 1px solid #ff4757;
}

.danger-outline:hover {
  background-color: #ff4757;
  color: white;
}

/* 开关样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #4A90E2;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

/* 设备列表样式 */
.device-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border: 1px solid #eee;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.device-icon {
  font-size: 1.5rem;
}

.device-details h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 1rem;
}

.device-details p {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 0.9rem;
}

.device-status {
  font-size: 0.8rem;
  padding: 2px 8px;
  border-radius: 12px;
  background-color: #e9ecef;
  color: #666;
}

.device-status.current {
  background-color: #d4edda;
  color: #155724;
}

.logout-btn {
  padding: 6px 12px;
  border: 1px solid #dc3545;
  background-color: white;
  color: #dc3545;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #dc3545;
  color: white;
}

/* 隐私设置样式 */
.privacy-settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.privacy-settings.compact {
  gap: 10px;
}

.privacy-settings .setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.privacy-settings label {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
  min-width: 60px;
}

.privacy-settings select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  font-size: 0.85rem;
  min-width: 100px;
}

.data-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.data-actions.vertical {
  flex-direction: column;
  gap: 8px;
}

/* 设备列表样式 */
.device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.device-list.compact {
  gap: 10px;
  margin-bottom: 15px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.device-item.compact {
  padding: 10px;
}

.device-item:hover {
  border-color: #4A90E2;
  box-shadow: 0 2px 6px rgba(74, 144, 226, 0.1);
}

.device-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.device-icon {
  font-size: 1.5rem;
}

.device-details h4 {
  margin: 0 0 3px 0;
  color: #333;
  font-size: 0.95rem;
  font-weight: 500;
}

.device-details p {
  margin: 0 0 3px 0;
  color: #666;
  font-size: 0.85rem;
}

.device-actions {
  display: flex;
  gap: 8px;
}

.current-device-logout {
  text-align: center;
  padding: 15px;
  background: #fff5f5;
  border-radius: 8px;
  border: 1px solid #ffe0e0;
}

.current-device-logout .danger-btn {
  margin-bottom: 8px;
}

.logout-hint {
  margin: 0;
  color: #ff4757;
  font-size: 0.85rem;
}

/* 绑定列表样式 */
.binding-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.binding-list.compact {
  gap: 10px;
}

.binding-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.binding-item.compact {
  padding: 10px;
}

.binding-item:hover {
  border-color: #4A90E2;
  box-shadow: 0 2px 6px rgba(74, 144, 226, 0.1);
}

.binding-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.binding-icon {
  font-size: 1.5rem;
}

.binding-details h4 {
  margin: 0 0 3px 0;
  color: #333;
  font-size: 0.95rem;
  font-weight: 500;
}

.binding-details p {
  margin: 0;
  color: #666;
  font-size: 0.85rem;
}

/* 退出登录样式 */
.logout-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff5f5;
  border-radius: 8px;
  border: 1px solid #ffe0e0;
}

.logout-info h3 {
  margin: 0 0 8px 0;
  color: #dc3545;
  font-size: 1.2rem;
}

.logout-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.dialog {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

.dialog.danger {
  border-top: 4px solid #ff4757;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.warning-message {
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
}

.warning-message p {
  margin: 0 0 10px 0;
  color: #856404;
  font-weight: 500;
}

.warning-message ul {
  margin: 10px 0;
  padding-left: 20px;
  color: #856404;
}

.warning-message li {
  margin-bottom: 5px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn,
.confirm-btn,
.danger-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.confirm-btn {
  background-color: #4A90E2;
  color: white;
}

.confirm-btn:hover {
  background-color: #357abd;
}

.danger-btn {
  background-color: #ff4757;
  color: white;
}

.danger-btn:hover:not(:disabled) {
  background-color: #ff2e42;
}

.danger-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 桌面端底部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  margin-top: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin: 15px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .account-management {
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
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .back-btn {
    margin-right: 0;
  }

  .account-content {
    padding: 15px 10px;
  }

  .unified-management-container {
    gap: 20px;
  }

  .main-header {
    padding: 15px;
  }

  .main-header h1 {
    font-size: 1.5rem;
  }

  .function-group {
    padding: 15px;
  }

  .function-group h2 {
    font-size: 1.2rem;
    margin-bottom: 15px;
  }

  .cards-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .card {
    padding: 12px;
  }

  .full-width-card {
    grid-column: 1;
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .binding-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .binding-info {
    width: 100%;
  }

  .privacy-settings .setting-item {
    flex-direction: row;
  }

  .data-actions {
    flex-direction: column;
  }

  .data-actions button {
    width: 100%;
  }

  .logout-section {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .logout-info {
    text-align: center;
  }

  .dialog {
    width: 95%;
    margin: 10px;
  }

  .dialog-actions {
    flex-direction: column;
  }

  .dialog-actions button {
    width: 100%;
  }
}

@media (min-width: 768px) {
  .mobile-nav {
    display: none;
  }
}
</style>