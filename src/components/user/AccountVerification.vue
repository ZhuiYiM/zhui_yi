<template>
  <div :class="['account-verification', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="account-verification"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="userInfo"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 -->
      <header class="header">
        <h1 v-if="isMobile">账号管理</h1>
      </header>

      <!-- 账号管理内容 -->
      <div class="verification-container">
        <!-- 身份证实名认证 -->
        <section class="verification-section">
          <div class="section-header">
            <h2>身份证实名认证</h2>
            <div class="verification-status" :class="{ verified: userInfo.isIdVerified }">
              <span class="status-dot"></span>
              <span class="status-text">{{ userInfo.isIdVerified ? '已认证' : '未认证' }}</span>
            </div>
          </div>
          
          <div class="verification-content">
            <div v-if="!userInfo.isIdVerified" class="verification-form">
              <div class="form-group">
                <label>真实姓名</label>
                <input 
                  type="text" 
                  v-model="verificationForm.realName" 
                  placeholder="请输入真实姓名"
                  :disabled="isVerifying"
                />
              </div>
              
              <div class="form-group">
                <label>身份证号码</label>
                <input 
                  type="text" 
                  v-model="verificationForm.idCard" 
                  placeholder="请输入18位身份证号码"
                  :disabled="isVerifying"
                  maxlength="18"
                />
              </div>
              
              <button 
                class="verify-btn" 
                @click="submitIdVerification"
                :disabled="isVerifying || !canSubmitIdVerification"
              >
                {{ isVerifying ? '认证中...' : '提交认证' }}
              </button>
            </div>
            
            <div v-else class="verification-info">
              <div class="info-item">
                <span class="label">姓名:</span>
                <span class="value">{{ userInfo.realName }}</span>
              </div>
              <div class="info-item">
                <span class="label">身份证号:</span>
                <span class="value">{{ formatIdCard(userInfo.idCard) }}</span>
              </div>
              <button class="reverify-btn" @click="reverifyId">重新认证</button>
            </div>
          </div>
        </section>

        <!-- 手机号绑定 -->
        <section class="binding-section">
          <div class="section-header">
            <h2>手机号绑定</h2>
            <div class="binding-status" :class="{ bound: userInfo.phone }">
              <span class="status-dot"></span>
              <span class="status-text">{{ userInfo.phone ? '已绑定' : '未绑定' }}</span>
            </div>
          </div>
          
          <div class="binding-content">
            <div v-if="!userInfo.phone" class="binding-form">
              <div class="form-group">
                <label>手机号</label>
                <input 
                  type="tel" 
                  v-model="phoneForm.phone" 
                  placeholder="请输入手机号"
                  maxlength="11"
                  :disabled="isSendingPhoneCode"
                />
              </div>
              
              <div class="form-group verification-group">
                <label>验证码</label>
                <div class="verification-input">
                  <input 
                    type="text" 
                    v-model="phoneForm.code" 
                    placeholder="请输入验证码"
                    maxlength="6"
                    :disabled="isSendingPhoneCode"
                  />
                  <button 
                    class="send-code-btn"
                    @click="sendPhoneVerificationCode"
                    :disabled="isSendingPhoneCode || !isValidPhone"
                  >
                    {{ isSendingPhoneCode ? `${phoneCountdown}s` : '发送验证码' }}
                  </button>
                </div>
              </div>
              
              <button 
                class="bind-btn" 
                @click="bindPhone"
                :disabled="isBindingPhone || !canBindPhone"
              >
                {{ isBindingPhone ? '绑定中...' : '绑定手机号' }}
              </button>
            </div>
            
            <div v-else class="binding-info">
              <div class="info-item">
                <span class="label">已绑定手机号:</span>
                <span class="value">{{ formatPhone(userInfo.phone) }}</span>
              </div>
              <div class="action-buttons">
                <button class="change-btn" @click="showChangePhone">更换手机号</button>
                <button class="unbind-btn" @click="unbindPhone">解除绑定</button>
              </div>
            </div>
          </div>
        </section>

        <!-- 邮箱绑定 -->
        <section class="binding-section">
          <div class="section-header">
            <h2>邮箱绑定</h2>
            <div class="binding-status" :class="{ bound: userInfo.email }">
              <span class="status-dot"></span>
              <span class="status-text">{{ userInfo.email ? '已绑定' : '未绑定' }}</span>
            </div>
          </div>
          
          <div class="binding-content">
            <div v-if="!userInfo.email" class="binding-form">
              <div class="form-group">
                <label>邮箱地址</label>
                <input 
                  type="email" 
                  v-model="emailForm.email" 
                  placeholder="请输入邮箱地址"
                  :disabled="isSendingEmailCode"
                />
              </div>
              
              <div class="form-group verification-group">
                <label>验证码</label>
                <div class="verification-input">
                  <input 
                    type="text" 
                    v-model="emailForm.code" 
                    placeholder="请输入验证码"
                    maxlength="6"
                    :disabled="isSendingEmailCode"
                  />
                  <button 
                    class="send-code-btn"
                    @click="sendEmailVerificationCode"
                    :disabled="isSendingEmailCode || !isValidEmail"
                  >
                    {{ isSendingEmailCode ? `${emailCountdown}s` : '发送验证码' }}
                  </button>
                </div>
              </div>
              
              <button 
                class="bind-btn" 
                @click="bindEmail"
                :disabled="isBindingEmail || !canBindEmail"
              >
                {{ isBindingEmail ? '绑定中...' : '绑定邮箱' }}
              </button>
            </div>
            
            <div v-else class="binding-info">
              <div class="info-item">
                <span class="label">已绑定邮箱:</span>
                <span class="value">{{ formatEmail(userInfo.email) }}</span>
              </div>
              <div class="action-buttons">
                <button class="change-btn" @click="showChangeEmail">更换邮箱</button>
                <button class="unbind-btn" @click="unbindEmail">解除绑定</button>
              </div>
            </div>
          </div>
        </section>

        <!-- 微信绑定 -->
        <section class="binding-section">
          <div class="section-header">
            <h2>微信绑定</h2>
            <div class="binding-status" :class="{ bound: userInfo.wechatBound }">
              <span class="status-dot"></span>
              <span class="status-text">{{ userInfo.wechatBound ? '已绑定' : '未绑定' }}</span>
            </div>
          </div>
          
          <div class="binding-content">
            <div v-if="!userInfo.wechatBound" class="wechat-binding">
              <div class="wechat-qr-container">
                <div v-if="wechatQRCode" class="qr-display">
                  <div class="qr-placeholder">
                    <div class="qr-pattern"></div>
                    <p>微信二维码示意图</p>
                  </div>
                  <p class="qr-tip">请使用微信扫描上方二维码绑定账号</p>
                </div>
                <div v-else class="qr-loading">
                  <div class="loading-spinner"></div>
                  <p>正在生成二维码...</p>
                </div>
              </div>
              
              <div class="wechat-status">
                <div :class="['status-indicator', wechatBindStatus]"></div>
                <span v-if="wechatBindStatus === 'waiting'">等待扫码绑定</span>
                <span v-else-if="wechatBindStatus === 'scanned'">已扫码，等待确认</span>
                <span v-else-if="wechatBindStatus === 'bound'">绑定成功</span>
              </div>
              
              <div class="wechat-actions">
                <button 
                  class="refresh-qr-btn"
                  @click="refreshWechatQR"
                  :disabled="wechatBindStatus === 'bound'"
                >
                  🔄 刷新二维码
                </button>
              </div>
            </div>
            
            <div v-else class="binding-info">
              <div class="info-item">
                <span class="label">已绑定微信:</span>
                <span class="value">微信用户</span>
              </div>
              <button class="unbind-btn" @click="unbindWechat">解除绑定</button>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import { userAPI } from '@/api/user';
import { isMobile } from '@/utils/device';

export default {
  name: 'AccountVerification',
  components: {
    UnifiedNav
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    // 设备检测
    const isMobile = ref(false);
    const updateDeviceDetection = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    // 用户信息
    const userInfo = ref({
      username: '',
      studentId: '',
      realName: '',
      college: '',
      phone: '',
      email: '',
      idCard: '',
      isIdVerified: false,
      wechatBound: false
    });

    // 表单数据
    const verificationForm = reactive({
      studentId: '',
      realName: '',
      college: '',
      idCard: ''
    });

    const phoneForm = reactive({
      phone: '',
      code: ''
    });

    const emailForm = reactive({
      email: '',
      code: ''
    });

    // 状态管理
    const isVerifying = ref(false);
    const isSendingPhoneCode = ref(false);
    const isBindingPhone = ref(false);
    const isSendingEmailCode = ref(false);
    const isBindingEmail = ref(false);
    const phoneCountdown = ref(0);
    const emailCountdown = ref(0);
    const phoneTimer = ref(null);
    const emailTimer = ref(null);
    const wechatQRCode = ref('');
    const wechatBindStatus = ref('waiting');
    const wechatCheckTimer = ref(null);

    // 计算属性
    const canSubmitIdVerification = computed(() => {
      const idCardRegex = /(^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$)/;
      return verificationForm.realName && 
             verificationForm.idCard && 
             idCardRegex.test(verificationForm.idCard);
    });

    const isValidPhone = computed(() => {
      const phoneRegex = /^1[3-9]\d{9}$/;
      return phoneRegex.test(phoneForm.phone);
    });

    const canBindPhone = computed(() => {
      return isValidPhone.value && phoneForm.code.length === 6;
    });

    const isValidEmail = computed(() => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(emailForm.email);
    });

    const canBindEmail = computed(() => {
      return isValidEmail.value && emailForm.code.length === 6;
    });

    // 格式化显示
    const formatPhone = (phone) => {
      if (!phone) return '';
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
    };

    const formatEmail = (email) => {
      if (!email) return '';
      return email.replace(/(.{2}).*(@.*)/, '$1***$2');
    };

    const formatIdCard = (idCard) => {
      if (!idCard) return '';
      return idCard.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2');
    };

    // 获取用户信息
    const getUserInfo = () => {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      userInfo.value = {
        ...userInfo.value,
        ...user,
        username: user.username || user.name || '',
        isIdVerified: user.isIdVerified || false,
        wechatBound: user.wechatBound || false
      };
    };

    // 身份证认证相关
    const submitIdVerification = async () => {
      if (!canSubmitIdVerification.value) {
        ElMessage.error('请输入正确的姓名和身份证号码');
        return;
      }
      
      isVerifying.value = true;
      try {
        const response = await userAPI.applyRealNameVerification({
          realName: verificationForm.realName,
          idCard: verificationForm.idCard,
          type: 'idCard'
        });
        
        if (response.code === 200) {
          ElMessage.success('身份证认证申请已提交，请等待审核');
          // 更新用户信息
          userInfo.value.isIdVerified = true;
          userInfo.value.realName = verificationForm.realName;
          userInfo.value.idCard = verificationForm.idCard;
          localStorage.setItem('user', JSON.stringify(userInfo.value));
        } else {
          throw new Error(response.message || '认证失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '认证失败');
      } finally {
        isVerifying.value = false;
      }
    };

    const reverifyId = () => {
      userInfo.value.isIdVerified = false;
      verificationForm.realName = '';
      verificationForm.idCard = '';
    };

    // 手机号绑定相关
    const sendPhoneVerificationCode = async () => {
      if (!isValidPhone.value) {
        ElMessage.error('请输入正确的手机号');
        return;
      }

      isSendingPhoneCode.value = true;
      try {
        const response = await userAPI.sendPhoneVerificationCode(phoneForm.phone);
        if (response.code === 200) {
          ElMessage.success('验证码已发送');
          startPhoneCountdown();
        } else {
          throw new Error(response.message || '发送失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '发送验证码失败');
      } finally {
        isSendingPhoneCode.value = false;
      }
    };

    const bindPhone = async () => {
      if (!canBindPhone.value) return;
      
      isBindingPhone.value = true;
      try {
        const response = await userAPI.bindPhone({
          phone: phoneForm.phone,
          code: phoneForm.code
        });
        
        if (response.code === 200) {
          ElMessage.success('手机号绑定成功');
          userInfo.value.phone = phoneForm.phone;
          localStorage.setItem('user', JSON.stringify(userInfo.value));
          phoneForm.phone = '';
          phoneForm.code = '';
        } else {
          throw new Error(response.message || '绑定失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '绑定失败');
      } finally {
        isBindingPhone.value = false;
      }
    };

    const unbindPhone = async () => {
      try {
        await ElMessageBox.confirm('确定要解除手机号绑定吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        });
        
        const response = await userAPI.unbindSocial('phone');
        if (response.code === 200) {
          ElMessage.success('手机号解绑成功');
          userInfo.value.phone = '';
          localStorage.setItem('user', JSON.stringify(userInfo.value));
        } else {
          throw new Error(response.message || '解绑失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '解绑失败');
        }
      }
    };

    const showChangePhone = () => {
      ElMessage.info('更换手机号功能开发中');
    };

    // 邮箱绑定相关
    const sendEmailVerificationCode = async () => {
      if (!isValidEmail.value) {
        ElMessage.error('请输入正确的邮箱地址');
        return;
      }

      isSendingEmailCode.value = true;
      try {
        const response = await userAPI.sendEmailVerification(emailForm.email);
        if (response.code === 200) {
          ElMessage.success('验证码已发送，请查收邮箱');
          startEmailCountdown();
        } else {
          throw new Error(response.message || '发送失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '发送验证码失败');
      } finally {
        isSendingEmailCode.value = false;
      }
    };

    const bindEmail = async () => {
      if (!canBindEmail.value) return;
      
      isBindingEmail.value = true;
      try {
        const response = await userAPI.bindEmail({
          email: emailForm.email,
          code: emailForm.code
        });
        
        if (response.code === 200) {
          ElMessage.success('邮箱绑定成功');
          userInfo.value.email = emailForm.email;
          localStorage.setItem('user', JSON.stringify(userInfo.value));
          emailForm.email = '';
          emailForm.code = '';
        } else {
          throw new Error(response.message || '绑定失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '绑定失败');
      } finally {
        isBindingEmail.value = false;
      }
    };

    const unbindEmail = async () => {
      try {
        await ElMessageBox.confirm('确定要解除邮箱绑定吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        });
        
        const response = await userAPI.unbindSocial('email');
        if (response.code === 200) {
          ElMessage.success('邮箱解绑成功');
          userInfo.value.email = '';
          localStorage.setItem('user', JSON.stringify(userInfo.value));
        } else {
          throw new Error(response.message || '解绑失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '解绑失败');
        }
      }
    };

    const showChangeEmail = () => {
      ElMessage.info('更换邮箱功能开发中');
    };

    // 微信绑定相关
    const generateWechatQR = async () => {
      try {
        // 模拟生成二维码
        wechatQRCode.value = 'wechat_qr_' + Date.now();
        wechatBindStatus.value = 'waiting';
        startWechatChecking();
      } catch (error) {
        ElMessage.error('生成二维码失败');
      }
    };

    const refreshWechatQR = async () => {
      clearWechatTimer();
      await generateWechatQR();
      ElMessage.success('二维码已刷新');
    };

    const startWechatChecking = () => {
      clearWechatTimer();
      wechatCheckTimer.value = setInterval(() => {
        // 模拟扫码状态变化
        if (Math.random() > 0.7) {
          if (wechatBindStatus.value === 'waiting') {
            wechatBindStatus.value = 'scanned';
            ElMessage.info('微信已扫码，请在手机上确认');
          } else if (wechatBindStatus.value === 'scanned') {
            wechatBindStatus.value = 'bound';
            userInfo.value.wechatBound = true;
            localStorage.setItem('user', JSON.stringify(userInfo.value));
            ElMessage.success('微信绑定成功');
            clearWechatTimer();
          }
        }
      }, 3000);
    };

    const unbindWechat = async () => {
      try {
        await ElMessageBox.confirm('确定要解除微信绑定吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        });
        
        const response = await userAPI.unbindSocial('wechat');
        if (response.code === 200) {
          ElMessage.success('微信解绑成功');
          userInfo.value.wechatBound = false;
          localStorage.setItem('user', JSON.stringify(userInfo.value));
        } else {
          throw new Error(response.message || '解绑失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '解绑失败');
        }
      }
    };

    // 倒计时相关
    const startPhoneCountdown = () => {
      phoneCountdown.value = 120;
      phoneTimer.value = setInterval(() => {
        phoneCountdown.value--;
        if (phoneCountdown.value <= 0) {
          clearInterval(phoneTimer.value);
          phoneTimer.value = null;
        }
      }, 1000);
    };

    const startEmailCountdown = () => {
      emailCountdown.value = 120;
      emailTimer.value = setInterval(() => {
        emailCountdown.value--;
        if (emailCountdown.value <= 0) {
          clearInterval(emailTimer.value);
          emailTimer.value = null;
        }
      }, 1000);
    };

    const clearTimers = () => {
      if (phoneTimer.value) {
        clearInterval(phoneTimer.value);
        phoneTimer.value = null;
      }
      if (emailTimer.value) {
        clearInterval(emailTimer.value);
        emailTimer.value = null;
      }
      clearWechatTimer();
    };

    const clearWechatTimer = () => {
      if (wechatCheckTimer.value) {
        clearInterval(wechatCheckTimer.value);
        wechatCheckTimer.value = null;
      }
    };

    // 生命周期
    onMounted(() => {
      updateDeviceDetection();
      window.addEventListener('resize', updateDeviceDetection);
      getUserInfo();
      generateWechatQR();
    });

    onUnmounted(() => {
      window.removeEventListener('resize', updateDeviceDetection);
      clearTimers();
    });

    return {
      // 响应式数据
      isMobile,
      userInfo,
      verificationForm,
      phoneForm,
      emailForm,
      isVerifying,
      isSendingPhoneCode,
      isBindingPhone,
      isSendingEmailCode,
      isBindingEmail,
      phoneCountdown,
      emailCountdown,
      wechatQRCode,
      wechatBindStatus,
      
      // 计算属性
      canSubmitIdVerification,
      isValidPhone,
      canBindPhone,
      isValidEmail,
      canBindEmail,
      
      // 方法
      formatPhone,
      formatEmail,
      formatIdCard,
      submitIdVerification,
      reverifyId,
      sendPhoneVerificationCode,
      bindPhone,
      unbindPhone,
      showChangePhone,
      sendEmailVerificationCode,
      bindEmail,
      unbindEmail,
      showChangeEmail,
      refreshWechatQR,
      unbindWechat
    };
  }
};
</script>

<style scoped>
.account-verification {
  display: flex;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.full-width {
  margin-left: 0;
}

.header {
  margin-bottom: 30px;
}

.header h1 {
  color: #333;
  font-size: 1.8rem;
  margin: 0;
}

.verification-container {
  max-width: 800px;
  margin: 0 auto;
}

.verification-section,
.binding-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  color: #333;
  margin: 0;
  font-size: 1.3rem;
}

.verification-status,
.binding-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #dc3545;
}

.verification-status.verified .status-dot,
.binding-status.bound .status-dot {
  background-color: #28a745;
}

.status-text {
  font-size: 0.9rem;
  color: #666;
}

.verification-form,
.binding-form {
  display: grid;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #555;
}

.form-group input,
.form-group select {
  padding: 12px 15px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.verify-btn,
.bind-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.verify-btn:hover:not(:disabled),
.bind-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.verify-btn:disabled,
.bind-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.verification-info,
.binding-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.label {
  font-weight: 500;
  color: #555;
}

.value {
  color: #333;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.change-btn,
.unbind-btn,
.reverify-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.change-btn {
  background: #667eea;
  color: white;
}

.change-btn:hover {
  background: #5a6fd8;
}

.unbind-btn,
.reverify-btn {
  background: #dc3545;
  color: white;
}

.unbind-btn:hover,
.reverify-btn:hover {
  background: #c82333;
}

.verification-group {
  position: relative;
}

.verification-input {
  display: flex;
  gap: 10px;
}

.verification-input input {
  flex: 1;
}

.send-code-btn {
  padding: 12px 15px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.send-code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 微信绑定样式 */
.wechat-binding {
  text-align: center;
}

.wechat-qr-container {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.qr-display {
  text-align: center;
}

.qr-placeholder {
  width: 180px;
  height: 180px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto 15px;
}

.qr-pattern {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #667eea 25%, transparent 25%), 
              linear-gradient(-45deg, #667eea 25%, transparent 25%), 
              linear-gradient(45deg, transparent 75%, #667eea 75%), 
              linear-gradient(-45deg, transparent 75%, #667eea 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.qr-placeholder p {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.qr-tip {
  color: #667eea;
  font-size: 0.95rem;
  margin: 10px 0;
}

.qr-loading {
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e1e5f2;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.wechat-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 20px 0;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e1e5f2;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-indicator.waiting {
  background: #ffc107;
  animation: pulse 1.5s infinite;
}

.status-indicator.scanned {
  background: #17a2b8;
  animation: pulse 1.5s infinite;
}

.status-indicator.bound {
  background: #28a745;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

.wechat-actions {
  margin-top: 20px;
}

.refresh-qr-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.refresh-qr-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-2px);
}

.refresh-qr-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .verification-section,
  .binding-section {
    padding: 20px;
    margin-bottom: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .change-btn,
  .unbind-btn,
  .reverify-btn {
    width: 100%;
  }
  
  .verification-input {
    flex-direction: column;
  }
  
  .send-code-btn {
    width: 100%;
  }
  
  .wechat-qr-container {
    padding: 15px;
    min-height: 180px;
  }
  
  .qr-placeholder {
    width: 150px;
    height: 150px;
  }
  
  .qr-pattern {
    width: 100px;
    height: 100px;
    background-size: 16px 16px;
  }
}
</style>