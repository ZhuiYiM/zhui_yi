<template>
  <div class="password-recovery">
    <div class="recovery-type-selector">
      <button 
        type="button" 
        :class="['type-btn', { active: recoveryType === 'email' }]"
        @click="$emit('update:recoveryType', 'email')"
      >
        邮箱找回
      </button>
      <button 
        type="button" 
        :class="['type-btn', { active: recoveryType === 'phone' }]"
        @click="$emit('update:recoveryType', 'phone')"
      >
        手机找回
      </button>
    </div>
    
    <div v-if="recoveryType === 'email'" class="form-group">
      <label>邮箱</label>
      <input 
        type="email" 
        :value="email" 
        @input="$emit('update:email', $event.target.value)"
        placeholder="请输入邮箱"
        required
      />
    </div>
    
    <div v-if="recoveryType === 'phone'" class="form-group">
      <label>手机号</label>
      <input 
        type="tel" 
        :value="phone" 
        @input="$emit('update:phone', $event.target.value)"
        placeholder="请输入手机号"
        maxlength="11"
        required
      />
    </div>
    
    <div class="form-group">
      <label>验证码</label>
      <div class="verification-input">
        <input 
          type="text" 
          :value="code" 
          @input="$emit('update:code', $event.target.value)"
          placeholder="请输入验证码"
          maxlength="6"
        />
        <button 
          type="button" 
          @click="$emit('send-code')"
          :disabled="isCountingDown"
        >
          {{ isCountingDown ? `${countdown}s` : '发送验证码' }}
        </button>
      </div>
    </div>
    
    <div class="form-group">
      <label>新密码</label>
      <input 
        type="password" 
        :value="newPassword" 
        @input="$emit('update:newPassword', $event.target.value)"
        placeholder="请输入新密码"
        required
        minlength="6"
      />
    </div>
    
    <div class="form-group">
      <label>确认新密码</label>
      <input 
        type="password" 
        :value="confirmNewPassword" 
        @input="$emit('update:confirmNewPassword', $event.target.value)"
        placeholder="请再次输入新密码"
        required
        minlength="6"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'PasswordRecoveryForm',
  props: {
    recoveryType: String,
    email: String,
    phone: String,
    code: String,
    newPassword: String,
    confirmNewPassword: String,
    isCountingDown: Boolean,
    countdown: Number
  },
  emits: [
    'update:recoveryType',
    'update:email',
    'update:phone',
    'update:code',
    'update:newPassword',
    'update:confirmNewPassword',
    'send-code'
  ]
}
</script>

<style scoped>
.recovery-type-selector {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.type-btn {
  flex: 1;
  padding: 12px;
  background: #f8f9fa;
  border: 2px solid #e1e5eb;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.type-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.type-btn:hover:not(.active) {
  border-color: #667eea;
  background: #f0f4ff;
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
  padding: 12px 16px;
  border: 2px solid #e1e5eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
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
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.verification-input button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.verification-input button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>
