<template>
  <div class="username-form">
    <div class="form-group">
      <label>用户名</label>
      <input 
        type="text" 
        :value="username" 
        @input="$emit('update:username', $event.target.value)"
        placeholder="请输入用户名"
        required
      />
    </div>
    
    <div class="form-group">
      <label>密码</label>
      <input 
        type="password" 
        :value="password" 
        @input="$emit('update:password', $event.target.value)"
        placeholder="请输入密码"
        required
        minlength="6"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'UsernameLoginForm',
  props: {
    username: {
      type: String,
      default: ''
    },
    password: {
      type: String,
      default: ''
    }
  },
  emits: ['update:username', 'update:password']
}
</script>

<style scoped>
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
</style>
