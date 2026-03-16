<template>
  <div class="privacy-settings-card">
    <h3>🔒 隐私设置</h3>
    <div class="setting-item">
      <div class="setting-content">
        <span class="setting-label">允许他人查看我的话题和参与记录</span>
        <span class="setting-desc">关闭后，其他用户将无法看到您发布的话题和参与的话题</span>
      </div>
      <label class="switch">
        <input type="checkbox" v-model="publicVisible" @change="$emit('update', publicVisible)">
        <span class="slider"></span>
      </label>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  publicVisible: {
    type: Boolean,
    default: true
  }
});

defineEmits(['update']);

const publicVisible = ref(props.publicVisible);

watch(() => props.publicVisible, (newVal) => {
  publicVisible.value = newVal;
});
</script>

<style scoped>
.privacy-settings-card {
  padding: 24px 40px;
  border-bottom: 1px solid #f0f0f0;
}

.privacy-settings-card h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.setting-content {
  flex: 1;
}

.setting-label {
  display: block;
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 6px;
}

.setting-desc {
  display: block;
  font-size: 13px;
  color: #999;
}

.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
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
  border-radius: 26px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #409eff;
}

input:focus + .slider {
  box-shadow: 0 0 1px #409eff;
}

input:not(:checked) + .slider {
  background-color: #dcdfe6;
}

input:checked + .slider:before {
  transform: translateX(24px);
}
</style>
