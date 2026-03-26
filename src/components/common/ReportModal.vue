<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="举报"
    width="500px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form :model="form" label-width="80px" size="default">
      <el-alert
        title="举报说明"
        description="请如实填写举报信息，恶意举报将被处理"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <!-- 举报类型 -->
      <el-form-item label="类型">
        <el-tag :type="targetType === 'location' ? 'success' : 'primary'">
          {{ targetTypeText }}
        </el-tag>
        <span style="margin-left: 10px; color: #999;">ID: {{ targetId }}</span>
      </el-form-item>

      <!-- 举报原因 -->
      <el-form-item label="举报原因" required>
        <el-select 
          v-model="form.reason" 
          placeholder="请选择举报原因"
          style="width: 100%"
        >
          <el-option 
            v-for="item in reportReasons" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" 
          />
        </el-select>
      </el-form-item>

      <!-- 举报描述 -->
      <el-form-item label="详细描述" required>
        <el-input 
          v-model="form.description" 
          type="textarea"
          :rows="4"
          placeholder="请详细描述举报原因，便于我们核实处理"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 证据图片 -->
      <el-form-item label="证据图片">
        <ImageUploader 
          v-model="form.images"
          :multiple="true"
          :limit="3"
          :auto-upload="false"
          tip="最多上传 3 张图片作为证据（可选）"
        />
      </el-form-item>

      <!-- 联系方式 -->
      <el-form-item label="联系方式">
        <el-input 
          v-model="form.contactInfo" 
          placeholder="手机号或微信号（可选，便于我们联系您）"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { reportAPI } from '@/api/report';

const props = defineProps({
  modelValue: Boolean,
  targetType: {
    type: String,
    required: true,
    validator: (value) => {
      const valid = ['location', 'product', 'topic', 'review', 'user'].includes(value);
      return valid;
    }
  },
  targetId: {
    type: [Number, String],
    required: true,
    validator: (value) => {
      // 允许数字或字符串，但不能为 null、undefined 或空字符串
      if (value === null || value === undefined) {
        return false;
      }
      if (typeof value === 'number') {
        return true; // 数字都允许（包括 0）
      }
      if (typeof value === 'string') {
        return value.trim() !== '';
      }
      return false;
    }
  }
});

console.log('[ReportModal] Props 定义完成，当前 props:', props);

const emit = defineEmits(['update:modelValue', 'success']);

const dialogVisible = computed({
  get: () => {
    console.log('[ReportModal] dialogVisible get:', props.modelValue);
    return props.modelValue;
  },
  set: (val) => {
    console.log('[ReportModal] dialogVisible set:', val);
    emit('update:modelValue', val);
  }
});

const submitting = ref(false);
const reportReasons = ref([
  { label: '色情低俗', value: 'pornography' },
  { label: '违法犯罪', value: 'illegal' },
  { label: '政治敏感', value: 'political' },
  { label: '垃圾广告', value: 'spam' },
  { label: '虚假信息', value: 'fake_info' },
  { label: '侵权内容', value: 'copyright' },
  { label: '人身攻击', value: 'harassment' },
  { label: '其他违规', value: 'other' }
]);

const form = ref({
  reason: '',
  description: '',
  images: [],
  contactInfo: ''
});

// 加载举报原因
const loadReportReasons = async () => {
  try {
    // const data = await reportAPI.getReportReasons();
    // reportReasons.value = data || reportReasons.value;
  } catch (error) {
    // 静默失败，不影响用户体验
  }
};

// 举报原因文本映射
const targetTypeText = computed(() => {
  const mapping = {
    location: '地点',
    product: '商品',
    topic: '话题',
    review: '评价'
  };
  return mapping[props.targetType] || '未知';
});

// 提交举报
const handleSubmit = async () => {
  // 验证 targetId
  if (!props.targetId) {
    ElMessage.warning('举报目标 ID 无效');
    return;
  }
  
  // 验证表单
  if (!form.value.reason) {
    ElMessage.warning('请选择举报原因');
    return;
  }
  if (!form.value.description.trim()) {
    ElMessage.warning('请填写详细描述');
    return;
  }

  submitting.value = true;

  try {
    // 确保 targetId 是数字或字符串
    const targetId = typeof props.targetId === 'number' 
      ? props.targetId 
      : String(props.targetId).trim();
    
    const data = {
      targetType: props.targetType,
      targetId: targetId,
      reportType: form.value.reason, // 举报原因类型（pornography、spam 等）
      reason: form.value.description.trim(), // 详细描述
      description: form.value.description.trim(),
      images: form.value.images,
      contactInfo: form.value.contactInfo
    };
    
    // 调用后端 API 提交举报
    await reportAPI.submitReport(data);
    
    ElMessage.success('举报提交成功，我们会尽快处理');
    emit('success', data);
    
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 弹窗关闭后重置表单
const handleClosed = () => {
  form.value = {
    reason: '',
    description: '',
    images: [],
    contactInfo: ''
  };
};

// 监听弹窗显示状态
watch(() => props.modelValue, (newVal, oldVal) => {
  if (newVal) {
    loadReportReasons();
  } else {
    handleClosed();
  }
}, { immediate: true });

console.log('[ReportModal] 组件加载完成');
</script>

<style scoped>
.report-reasons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.reason-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.reason-tag:hover {
  transform: translateY(-2px);
}
</style>
