<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="report-modal" @click.stop>
      <h3>⚠️ 举报该用户</h3>
      <p class="modal-desc">请说明举报原因，我们会认真核实</p>
      
      <div class="form-group">
        <label>举报类型</label>
        <select v-model="reportType" class="form-select">
          <option value="spam">垃圾广告</option>
          <option value="fraud">诈骗信息</option>
          <option value="harassment">骚扰行为</option>
          <option value="illegal">违法违规</option>
          <option value="other">其他</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>举报原因描述 <span class="required">*</span></label>
        <textarea 
          v-model="reportReason" 
          class="form-textarea"
          placeholder="请详细描述举报原因..."
          rows="4"
        ></textarea>
      </div>
      
      <div class="modal-actions">
        <button @click="$emit('close')" class="cancel-btn">取消</button>
        <button @click="$emit('submit', { type: reportType, reason: reportReason })" class="submit-btn">提交举报</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['close', 'submit']);

const reportReason = ref('');
const reportType = ref('other');
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.3s;
}

.report-modal {
  background: white;
  border-radius: 16px;
  padding: 30px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s;
}

.report-modal h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}

.modal-desc {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 14px;
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

.form-group label .required {
  color: #f56c6c;
  margin-left: 4px;
}

.form-select,
.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.3s;
}

.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.cancel-btn {
  background: #f5f7fa;
  color: #606266;
}

.cancel-btn:hover {
  background: #e6e8eb;
}

.submit-btn {
  background: #f56c6c;
  color: white;
}

.submit-btn:hover {
  background: #f78989;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
