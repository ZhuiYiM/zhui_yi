<template>
  <div v-if="modelValue" class="custom-tag-modal-overlay" @click="handleClose">
    <div class="custom-tag-modal" @click.stop>
      <div class="modal-header">
        <h2>🏷️ 提交自定义商品标签</h2>
        <button @click="handleClose" class="close-btn">×</button>
      </div>

      <div class="modal-body">
        <!-- 提示信息 -->
        <div class="info-box">
          <div class="info-icon">ℹ️</div>
          <div class="info-content">
            <p class="info-title">提交说明</p>
            <p class="info-text">
              提交您想要的自定义商品标签，管理员审核通过后将会添加到标签池中。<br/>
              标签名称应简洁明了，便于其他用户理解和搜索。
            </p>
          </div>
        </div>

        <!-- 表单 -->
        <div class="tag-form">
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span> 标签名称
            </label>
            <input
              v-model="formData.tagName"
              type="text"
              placeholder="请输入标签名称（2-20 个字符）"
              class="form-input"
              maxlength="20"
            />
          </div>

          <div class="form-item">
            <label class="form-label">
              补充说明
              <span class="hint">（选填，帮助管理员更好地理解标签含义）</span>
            </label>
            <textarea
              v-model="formData.description"
              placeholder="请简要描述该标签的使用场景或含义（最多 200 字）"
              class="form-textarea"
              maxlength="200"
              rows="4"
            ></textarea>
          </div>
        </div>

        <!-- 提交记录提示 -->
        <div class="submission-hint">
          <p>💡 每个标签提交后需要管理员审核，审核结果将通过消息中心通知您。</p>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="handleClose" class="cancel-btn">取消</button>
        <button @click="handleSubmit" class="submit-btn" :disabled="submitting || !canSubmit">
          {{ submitting ? '提交中...' : '提交' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { productAPI } from '@/api/product';

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['update:modelValue', 'submitted']);

// 表单数据
const formData = ref({
  tagName: '',
  description: ''
});

const submitting = ref(false);

// 计算是否可以提交
const canSubmit = computed(() => {
  return formData.value.tagName.trim().length >= 2 && 
         formData.value.tagName.trim().length <= 20;
});

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  if (!newVal) {
    // 关闭时重置表单
    resetForm();
  }
});

// 重置表单
const resetForm = () => {
  formData.value = {
    tagName: '',
    description: ''
  };
};

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false);
};

// 提交表单
const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请填写完整的标签信息');
    return;
  }

  submitting.value = true;

  try {
    // 准备提交数据
    const submitData = {
      name: formData.value.tagName.trim(),
      description: formData.value.description.trim() || null
    };

    console.log('📝 提交自定义商品标签:', submitData);

    // 调用 API
    const response = await productAPI.submitCustomTag(submitData);

    if (response) {
      ElMessage.success('标签提交成功，请等待管理员审核！');
      emit('submitted', response.data || response);
      handleClose();
    }
  } catch (error) {
    console.error('❌ 提交失败:', error);
    
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else if (error.message) {
      ElMessage.error(error.message);
    } else {
      ElMessage.error('提交失败，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.custom-tag-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.custom-tag-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f7fa;
  color: #606266;
}

.modal-body {
  padding: 24px;
}

.info-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #ecf5ff;
  border-radius: 8px;
  border: 1px solid #d9ecff;
  margin-bottom: 24px;
}

.info-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-title {
  margin: 0 0 8px 0;
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.info-text {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.tag-form {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.required {
  color: #f56c6c;
  margin-right: 4px;
}

.hint {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-input::placeholder {
  color: #c0c4cc;
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
  box-sizing: border-box;
  resize: vertical;
  font-family: inherit;
}

.form-textarea:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-textarea::placeholder {
  color: #c0c4cc;
}

.submission-hint {
  padding: 12px 16px;
  background: #fdf6ec;
  border-left: 3px solid #e6a23c;
  border-radius: 4px;
  margin-top: 16px;
}

.submission-hint p {
  margin: 0;
  font-size: 13px;
  color: #e6a23c;
  line-height: 1.6;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

.cancel-btn {
  padding: 8px 20px;
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.submit-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .custom-tag-modal-overlay {
    padding: 10px;
  }
  
  .custom-tag-modal {
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 16px 16px 0 16px;
  }
  
  .modal-body {
    padding: 16px;
  }
  
  .modal-footer {
    padding: 12px 16px;
  }
}
</style>
