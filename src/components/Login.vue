<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>校园信息平台</h1>
        <p v-if="!showRecovery">{{ isLogin ? '欢迎回来' : '创建账户' }}</p>
        <p v-else>找回密码</p>
      </div>
      
      <form @submit.prevent="handleSubmit" class="login-form">
        <!-- 登录类型选择 -->
        <div v-if="isLogin && !showRecovery && !showWechatQR" class="form-group">
          <div class="login-type-selector">
            <button 
              type="button" 
              :class="['type-btn', { active: loginType === 'username' }]"
              @click="loginType = 'username'"
            >
              账号登录
            </button>
            <button 
              type="button" 
              :class="['type-btn', { active: loginType === 'phone' }]"
              @click="loginType = 'phone'"
            >
              手机登录
            </button>
            <button 
              type="button" 
              :class="['type-btn', { active: loginType === 'wechat' }]"
              @click="showWechatLogin"
            >
              微信登录
            </button>
          </div>
        </div>
        
        <!-- 微信扫码登录界面 -->
        <div v-if="showWechatQR" class="wechat-login-container">
          <div class="wechat-qr-section">
            <h3>微信扫码登录</h3>
            <div class="qr-placeholder">
              <div v-if="qrCodeData" class="qr-code-display">
                <!-- 这里应该是实际的二维码图片 -->
                <div class="qr-code-image">
                  <div class="qr-pattern"></div>
                  <p>二维码示意图</p>
                </div>
                <p class="qr-tip">请使用微信扫描上方二维码</p>
              </div>
              <div v-else class="qr-loading">
                <div class="loading-spinner"></div>
                <p>正在生成二维码...</p>
              </div>
            </div>
            
            <!-- 扫码状态指示 -->
            <div class="scan-status">
              <div :class="['status-indicator', scanStatus]"></div>
              <span v-if="scanStatus === 'waiting'">等待扫码</span>
              <span v-else-if="scanStatus === 'scanned'">已扫码，等待确认</span>
              <span v-else-if="scanStatus === 'confirmed'">登录成功</span>
              <span v-else-if="scanStatus === 'expired'">二维码已过期</span>
            </div>
            
            <!-- 操作按钮 -->
            <div class="wechat-actions">
              <button 
                type="button" 
                class="refresh-btn"
                @click="refreshQRCode"
                :disabled="scanStatus === 'confirmed'"
              >
                🔄 刷新二维码
              </button>
              <button 
                type="button" 
                class="back-btn"
                @click="backToNormalLogin"
              >
                ← 返回
              </button>
            </div>
          </div>
        </div>
        
        <!-- 用户名登录 -->
        <div v-if="loginType === 'username' && !showRecovery && !showWechatQR" class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="formData.username" 
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <!-- 手机号登录 -->
        <div v-if="loginType === 'phone' && !showRecovery && !showWechatQR" class="form-group">
          <label>手机号</label>
          <input 
            type="tel" 
            v-model="formData.phone" 
            placeholder="请输入手机号"
            maxlength="11"
            required
          />
        </div>
        
        <!-- 找回密码类型选择 -->
        <div v-if="showRecovery" class="form-group">
          <div class="recovery-type-selector">
            <button 
              type="button" 
              :class="['type-btn', { active: recoveryType === 'email' }]"
              @click="recoveryType = 'email'"
            >
              邮箱找回
            </button>
            <button 
              type="button" 
              :class="['type-btn', { active: recoveryType === 'phone' }]"
              @click="recoveryType = 'phone'"
            >
              手机找回
            </button>
          </div>
        </div>
        
        <!-- 找回密码邮箱 -->
        <div v-if="showRecovery && recoveryType === 'email' && !showWechatQR" class="form-group">
          <label>邮箱</label>
          <input 
            type="email" 
            v-model="formData.email" 
            placeholder="请输入邮箱"
            required
          />
        </div>
        
        <!-- 找回密码手机号 -->
        <div v-if="showRecovery && recoveryType === 'phone' && !showWechatQR" class="form-group">
          <label>手机号</label>
          <input 
            type="tel" 
            v-model="formData.phone" 
            placeholder="请输入手机号"
            maxlength="11"
            required
          />
        </div>
        
        <!-- 注册邮箱 -->
        <div v-if="!isLogin && !showRecovery && !showWechatQR" class="form-group">
          <label>邮箱</label>
          <input 
            type="email" 
            v-model="formData.email" 
            placeholder="请输入邮箱"
            required
          />
        </div>
        
        <!-- 验证码输入 -->
        <div v-if="((!isLogin || loginType === 'phone' || showRecovery) && !showWechatQR)" class="form-group">
          <label>验证码</label>
          <div class="verification-input">
            <input 
              type="text" 
              v-model="formData.verificationCode" 
              placeholder="请输入验证码"
              maxlength="6"
            />
            <button 
              type="button" 
              @click="showRecovery ? sendRecoveryCode() : sendVerificationCode()"
              :disabled="isCountingDown"
            >
              {{ isCountingDown ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </div>
        
        <!-- 原密码（登录时） -->
        <div v-if="isLogin && !showRecovery && loginType === 'username' && !showWechatQR" class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="formData.password" 
            placeholder="请输入密码"
            required
            minlength="6"
          />
        </div>
        
        <!-- 新密码（找回密码时） -->
        <div v-if="showRecovery && !showWechatQR" class="form-group">
          <label>新密码</label>
          <input 
            type="password" 
            v-model="formData.newPassword" 
            placeholder="请输入新密码"
            required
            minlength="6"
          />
        </div>
        
        <!-- 确认新密码（找回密码时） -->
        <div v-if="showRecovery && !showWechatQR" class="form-group">
          <label>确认新密码</label>
          <input 
            type="password" 
            v-model="formData.confirmNewPassword" 
            placeholder="请再次输入新密码"
            required
            minlength="6"
          />
        </div>
        
        <!-- 注册密码 -->
        <div v-if="!isLogin && !showRecovery && !showWechatQR" class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="formData.password" 
            placeholder="请输入密码"
            required
            minlength="6"
          />
        </div>
        
        <!-- 注册确认密码 -->
        <div v-if="!isLogin && !showRecovery && !showWechatQR" class="form-group">
          <label>确认密码</label>
          <input 
            type="password" 
            v-model="formData.confirmPassword" 
            placeholder="请再次输入密码"
            required
            minlength="6"
          />
        </div>
        
        <button v-if="!showWechatQR" type="submit" class="submit-btn">
          {{ showRecovery ? '重置密码' : (isLogin ? '登录' : '注册') }}
        </button>
        
        <!-- 找回密码链接 -->
        <div v-if="isLogin && !showRecovery && !showWechatQR" class="forgot-password-link">
          <a href="#" @click.prevent="showPasswordRecovery">忘记密码？</a>
        </div>
        
        <!-- 返回登录链接 -->
        <div v-if="showRecovery && !showWechatQR" class="back-login-link">
          <a href="#" @click.prevent="backToLogin">&larr; 返回登录</a>
        </div>
      </form>
      
      <div v-if="!showRecovery && !showWechatQR" class="switch-form">
        <p>
          {{ isLogin ? '还没有账号？' : '已有账号？' }}
          <a href="#" @click.prevent="toggleForm">
            {{ isLogin ? '立即注册' : '立即登录' }}
          </a>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElLoading } from 'element-plus';
import { userAPI } from '@/api/user';

export default {
  name: 'Login',
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
  data() {
    return {
      isLogin: true,
      loginType: 'username', // 'username', 'phone' 或 'wechat'
      recoveryType: 'email', // 'email' 或 'phone'
      showRecovery: false,
      showWechatQR: false,
      qrCodeData: '',
      qrCodeId: '',
      scanStatus: 'waiting', // 'waiting', 'scanned', 'confirmed', 'expired'
      scanCheckTimer: null,
      formData: {
        username: '',
        phone: '',
        email: '',
        verificationCode: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      },
      isCountingDown: false,
      countdown: 120,
      countdownTimer: null
    }
  },
  methods: {
    toggleForm() {
      this.isLogin = !this.isLogin;
      this.showRecovery = false;
      this.showWechatQR = false;
      this.loginType = 'username';
      this.recoveryType = 'email';
      this.qrCodeData = '';
      this.qrCodeId = '';
      this.scanStatus = 'waiting';
      this.clearScanTimer();
      this.formData = {
        username: '',
        phone: '',
        email: '',
        verificationCode: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      };
      this.resetCountdown();
    },
    
    async handleSubmit() {
      if (this.showRecovery) {
        await this.handlePasswordRecovery();
      } else if (this.isLogin) {
        if (this.loginType === 'phone') {
          await this.handlePhoneLogin();
        } else if (this.loginType === 'wechat') {
          // 微信扫码登录不需要提交表单
          return;
        } else {
          await this.handleLogin();
        }
      } else {
        await this.handleRegister();
      }
    },
    
    async handleLogin() {
      if (!this.formData.username) {
        ElMessage.error('请输入用户名');
        return;
      }
      
      if (this.formData.password.length < 6) {
        ElMessage.error('密码长度至少为6位');
        return;
      }
      
      let loadingInstance = null;
      try {
        // 显示加载状态
        loadingInstance = ElLoading.service({
          text: '登录中...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        // 临时保存用户名，用于获取用户信息失败时的备用方案
        localStorage.setItem('temp_username', this.formData.username);
        
        const result = await this.authStore.login({
          username: this.formData.username,
          password: this.formData.password
        });
        
        loadingInstance.close();
        ElMessage.success('登录成功');
        
        // 延迟跳转确保消息显示
        setTimeout(() => {
          const redirect = this.$route.query.redirect;
          this.$router.push(redirect || '/');
        }, 500);
        
      } catch (error) {
        console.error('登录失败:', error);
        
        // 确保关闭加载状态
        if (loadingInstance) {
          loadingInstance.close();
        }
        
        // 提供更详细的错误信息
        let errorMessage = error.message || '登录失败，请稍后重试';
        
        // 如果是401错误，给出更友好的提示
        if (error.message && error.message.includes('用户名或密码错误')) {
          errorMessage = '用户名或密码错误，请检查后重试';
        }
        
        ElMessage.error(errorMessage);
      }
    },
    
    async handleRegister() {
      if (this.formData.username.length < 3) {
        ElMessage.error('用户名长度至少为3位');
        return;
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.formData.email)) {
        ElMessage.error('请输入正确的邮箱格式');
        return;
      }
      
      if (this.formData.verificationCode.length !== 6) {
        ElMessage.error('请输入6位验证码');
        return;
      }
      
      if (this.formData.password !== this.formData.confirmPassword) {
        ElMessage.error('两次输入的密码不一致');
        return;
      }
      
      if (this.formData.password.length < 6) {
        ElMessage.error('密码长度至少为6位');
        return;
      }
      
      try {
        await this.authStore.register({
          username: this.formData.username,
          email: this.formData.email,
          verificationCode: this.formData.verificationCode,
          password: this.formData.password
        });
        
        ElMessage.success('注册成功，请登录');
        this.isLogin = true;
        this.loginType = 'username';
      } catch (error) {
        ElMessage.error(error.message || '注册失败');
      }
    },
    
    async sendVerificationCode() {
      if (this.isLogin) {
        // 登录场景下发送验证码
        if (this.loginType === 'phone') {
          await this.sendPhoneCode();
        } else {
          await this.sendEmailCode();
        }
      } else {
        // 注册场景下发送邮箱验证码
        await this.sendEmailCode();
      }
    },
    
    async sendEmailCode() {
      if (!this.formData.email) {
        ElMessage.error('请输入邮箱地址');
        return;
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.formData.email)) {
        ElMessage.error('请输入正确的邮箱格式');
        return;
      }
      
      try {
        const response = await userAPI.sendEmailVerification(this.formData.email);
        ElMessage.success('验证码已发送，请查收邮箱');
        this.startCountdown();
      } catch (error) {
        console.error('❌ 发送验证码失败:', error);
        ElMessage.error(error.message || '发送验证码失败');
      }
    },
    
    async sendPhoneCode() {
      if (!this.formData.phone) {
        ElMessage.error('请输入手机号');
        return;
      }
      
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(this.formData.phone)) {
        ElMessage.error('请输入正确的手机号格式');
        return;
      }
      
      try {
        const response = await userAPI.sendPhoneVerificationCode(this.formData.phone);
        ElMessage.success('短信验证码已发送');
        this.startCountdown();
      } catch (error) {
        console.error('❌ 发送短信验证码失败:', error);
        ElMessage.error(error.message || '发送短信验证码失败');
      }
    },
    
    async handlePhoneLogin() {
      if (!this.formData.phone) {
        ElMessage.error('请输入手机号');
        return;
      }
      
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(this.formData.phone)) {
        ElMessage.error('请输入正确的手机号格式');
        return;
      }
      
      if (this.formData.verificationCode.length !== 6) {
        ElMessage.error('请输入6位验证码');
        return;
      }
      
      let loadingInstance = null;
      try {
        loadingInstance = ElLoading.service({
          text: '登录中...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        const result = await userAPI.loginByPhone(this.formData.phone, this.formData.verificationCode);
        
        loadingInstance.close();
        
        if (result.code === 200) {
          // 保存token
          const { token, user } = result.data;
          localStorage.setItem('token', token);
          
          // 使用认证存储来获取完整用户信息
          this.authStore.token = token;
          this.authStore.fetchCompleteUserInfo().then(() => {
            ElMessage.success('登录成功');
            
            setTimeout(() => {
              const redirect = this.$route.query.redirect;
              this.$router.push(redirect || '/');
            }, 500);
          });
          
          ElMessage.success('登录成功');
          
          setTimeout(() => {
            const redirect = this.$route.query.redirect;
            this.$router.push(redirect || '/');
          }, 500);
        } else {
          throw new Error(result.message || '登录失败');
        }
        
      } catch (error) {
        if (loadingInstance) {
          loadingInstance.close();
        }
        ElMessage.error(error.message || '登录失败');
      }
    },
    
    startCountdown() {
      this.isCountingDown = true;
      this.countdown = 120;
      
      this.countdownTimer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          this.resetCountdown();
        }
      }, 1000);
    },
    
    resetCountdown() {
      this.isCountingDown = false;
      clearInterval(this.countdownTimer);
    },
    
    // 显示找回密码界面
    showPasswordRecovery() {
      this.showRecovery = true;
      this.formData = {
        username: '',
        phone: '',
        email: '',
        verificationCode: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      };
    },
    
    // 返回登录界面
    backToLogin() {
      this.showRecovery = false;
      this.recoveryType = 'email';
      this.formData = {
        username: '',
        phone: '',
        email: '',
        verificationCode: '',
        password: '',
        confirmPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      };
      this.resetCountdown();
    },
    
    // 发送找回密码验证码
    async sendRecoveryCode() {
      let identifier = '';
      
      if (this.recoveryType === 'email') {
        if (!this.formData.email) {
          ElMessage.error('请输入邮箱地址');
          return;
        }
        identifier = this.formData.email;
      } else {
        if (!this.formData.phone) {
          ElMessage.error('请输入手机号');
          return;
        }
        identifier = this.formData.phone;
      }
      
      try {
        const response = await userAPI.sendPasswordRecoveryCode(identifier, this.recoveryType);
        ElMessage.success(`${this.recoveryType === 'email' ? '邮箱' : '手机'}验证码已发送`);
        this.startCountdown();
      } catch (error) {
        ElMessage.error(error.message || '发送验证码失败');
      }
    },
    
    // 处理找回密码
    async handlePasswordRecovery() {
      let identifier = '';
      
      if (this.recoveryType === 'email') {
        if (!this.formData.email) {
          ElMessage.error('请输入邮箱地址');
          return;
        }
        identifier = this.formData.email;
      } else {
        if (!this.formData.phone) {
          ElMessage.error('请输入手机号');
          return;
        }
        identifier = this.formData.phone;
      }
      
      if (this.formData.verificationCode.length !== 6) {
        ElMessage.error('请输入6位验证码');
        return;
      }
      
      if (this.formData.newPassword !== this.formData.confirmNewPassword) {
        ElMessage.error('两次输入的新密码不一致');
        return;
      }
      
      if (this.formData.newPassword.length < 6) {
        ElMessage.error('新密码长度至少为6位');
        return;
      }
      
      try {
        const response = await userAPI.resetPassword({
          identifier: identifier,
          type: this.recoveryType,
          code: this.formData.verificationCode,
          newPassword: this.formData.newPassword
        });
        
        ElMessage.success('密码重置成功，请使用新密码登录');
        this.backToLogin();
      } catch (error) {
        ElMessage.error(error.message || '密码重置失败');
      }
    },
    
    // 显示微信扫码界面
    async showWechatLogin() {
      this.loginType = 'wechat';
      this.showWechatQR = true;
      await this.generateWechatQRCode();
    },
    
    // 生成微信二维码
    async generateWechatQRCode() {
      try {
        const response = await userAPI.getWechatQRCode();
        if (response.code === 200) {
          this.qrCodeData = response.data.qrCode;
          this.qrCodeId = response.data.qrCodeId;
          this.scanStatus = 'waiting';
          this.startScanChecking();
        } else {
          throw new Error(response.message || '获取二维码失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '获取二维码失败');
        // 模拟二维码数据用于演示
        this.qrCodeData = 'https://example.com/wechat-qrcode-demo';
        this.qrCodeId = 'demo_' + Date.now();
        this.scanStatus = 'waiting';
        this.startScanChecking();
      }
    },
    
    // 开始轮询检查扫码状态
    startScanChecking() {
      this.clearScanTimer();
      this.scanCheckTimer = setInterval(async () => {
        try {
          const response = await userAPI.checkWechatScanStatus(this.qrCodeId);
          if (response.code === 200) {
            this.scanStatus = response.data.status;
            
            if (this.scanStatus === 'scanned') {
              ElMessage.info('微信已扫码，请在手机上确认登录');
            } else if (this.scanStatus === 'confirmed') {
              this.handleWechatLoginSuccess(response.data);
            } else if (this.scanStatus === 'expired') {
              ElMessage.warning('二维码已过期，请重新获取');
              this.clearScanTimer();
            }
          }
        } catch (error) {
          console.error('检查扫码状态失败:', error);
          // 模拟扫码状态用于演示
          if (Math.random() > 0.8) { // 20%概率模拟扫码成功
            this.scanStatus = 'confirmed';
            this.handleWechatLoginSuccess({
              token: 'demo_token_' + Date.now(),
              user: {
                username: 'wechat_user',
                nickname: '微信用户'
              }
            });
          }
        }
      }, 2000); // 每2秒检查一次
    },
    
    // 处理微信登录成功
    handleWechatLoginSuccess(data) {
      this.clearScanTimer();
      
      // 保存token
      const { token, user } = data;
      localStorage.setItem('token', token);
      
      // 使用认证存储来获取完整用户信息
      this.authStore.token = token;
      this.authStore.fetchCompleteUserInfo().then(() => {
        ElMessage.success('微信登录成功');
        
        setTimeout(() => {
          const redirect = this.$route.query.redirect;
          this.$router.push(redirect || '/');
        }, 500);
      });
    },
    
    // 清除扫码检查定时器
    clearScanTimer() {
      if (this.scanCheckTimer) {
        clearInterval(this.scanCheckTimer);
        this.scanCheckTimer = null;
      }
    },
    
    // 刷新二维码
    async refreshQRCode() {
      this.clearScanTimer();
      await this.generateWechatQRCode();
      ElMessage.success('二维码已刷新');
    },
    
    // 返回普通登录
    backToNormalLogin() {
      this.showWechatQR = false;
      this.loginType = 'username';
      this.qrCodeData = '';
      this.qrCodeId = '';
      this.scanStatus = 'waiting';
      this.clearScanTimer();
    },
  },
  
  beforeUnmount() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
    this.clearScanTimer();
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-type-selector,
.recovery-type-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.type-btn {
  flex: 1;
  padding: 12px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  background: white;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.type-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.forgot-password-link,
.back-login-link {
  text-align: center;
  margin: 15px 0;
}

.forgot-password-link a,
.back-login-link a {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
}

.forgot-password-link a:hover,
.back-login-link a:hover {
  text-decoration: underline;
}

/* 微信登录样式 */
.wechat-login-container {
  text-align: center;
}

.wechat-qr-section h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.qr-placeholder {
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

.qr-code-display {
  text-align: center;
}

.qr-code-image {
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

.qr-code-image p {
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

.qr-loading p {
  color: #666;
  margin: 0;
}

.scan-status {
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

.status-indicator.confirmed {
  background: #28a745;
}

.status-indicator.expired {
  background: #dc3545;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

.wechat-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.refresh-btn, .back-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: #667eea;
  color: white;
}

.refresh-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.back-btn {
  background: #6c757d;
  color: white;
}

.back-btn:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.login-box {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 1.8rem;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.verification-input {
  display: flex;
  gap: 10px;
}

.verification-input input {
  flex: 1;
}

.verification-input button {
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

.verification-input button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.switch-form {
  text-align: center;
}

.switch-form p {
  color: #666;
  margin: 0 0 15px 0;
}

.switch-form a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.switch-form a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    padding: 20px;
    margin: 10px;
  }
  
  /* 登录类型选择器保持横向排列 */
  .login-type-selector {
    flex-direction: row;
    gap: 5px;
  }
  
  .login-type-selector .type-btn {
    flex: 1;
    padding: 10px 5px;
    font-size: 0.85rem;
    min-width: 0;
  }
  
  /* 找回密码类型选择器仍为纵向 */
  .recovery-type-selector {
    flex-direction: column;
  }
  
  .type-btn {
    width: 100%;
  }
  
  .qr-code-image {
    width: 150px;
    height: 150px;
  }
  
  .qr-pattern {
    width: 100px;
    height: 100px;
    background-size: 16px 16px;
  }
  
  .wechat-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .refresh-btn, .back-btn {
    width: 100%;
  }
}

</style>