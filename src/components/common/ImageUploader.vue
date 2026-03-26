<template>
  <div class="image-uploader">
    <!-- 单图上传 -->
    <div v-if="multiple === false" class="single-image-upload">
      <div 
        v-if="modelValue" 
        class="image-preview"
        @click="handlePreview"
      >
        <img :src="modelValue" alt="预览图片" class="preview-image">
        <div class="image-overlay">
          <el-icon class="icon" @click.stop="handleRemove"><CircleClose /></el-icon>
          <el-icon class="icon" @click.stop="triggerUpload"><Edit /></el-icon>
        </div>
      </div>
      <div 
        v-else 
        class="upload-placeholder"
        @click="triggerUpload"
      >
        <el-icon class="upload-icon"><Plus /></el-icon>
        <div class="upload-text">{{ placeholderText || '点击上传图片' }}</div>
        <div v-if="hint" class="upload-hint">{{ hint }}</div>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
      >
    </div>

    <!-- 多图上传 -->
    <div v-else class="multiple-images-upload">
      <el-upload
        v-model:file-list="fileList"
        list-type="picture-card"
        :auto-upload="autoUpload"
        :limit="limit"
        :on-change="handleChange"
        :on-remove="handleRemove"
        :on-exceed="handleExceed"
        :on-preview="handlePictureCardPreview"
        accept="image/*"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
      <div v-if="hint" class="upload-tip">{{ hint }}</div>
      
      <!-- 图片预览对话框 -->
      <el-dialog v-model="dialogVisible" title="图片预览" class="image-preview-dialog">
        <img :src="dialogImageUrl" alt="预览图片" class="preview-large-image">
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount, nextTick } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { Plus, CircleClose, Edit } from '@element-plus/icons-vue';
import { uploadAPI } from '@/api/upload';

const props = defineProps({
  // 绑定值 (v-model)
  modelValue: {
    type: [String, Array],
    default: ''
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 最大上传数量（多图时有效）
  limit: {
    type: Number,
    default: 9
  },
  // 占位符文本
  placeholderText: {
    type: String,
    default: ''
  },
  // 提示文本
  hint: {
    type: String,
    default: ''
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: true
  },
  // 文件大小限制（MB）
  maxSize: {
    type: Number,
    default: 2
  }
});

const emit = defineEmits(['update:modelValue', 'change', 'remove', 'exceed']);

const fileInput = ref(null);
const fileList = ref([]);
const dialogVisible = ref(false);
const dialogImageUrl = ref('');

// 触发文件选择
const triggerUpload = () => {
  fileInput.value?.click();
};

// 处理文件变化（单图）
const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件
  if (!validateFile(file)) return;

  // 如果需要自动上传
  if (props.autoUpload) {
    await uploadFile(file);
  } else {
    // 不自动上传，只保存预览
    const reader = new FileReader();
    reader.onload = (e) => {
      // 创建一个包含 file 对象的结构，和多图模式保持一致
      const fileObj = {
        raw: file,
        url: e.target.result // Base64 用于预览
      };
      emit('update:modelValue', fileObj);
      emit('change', fileObj);
    };
    reader.readAsDataURL(file);
  }

  // 清空 input，允许重复选择同一文件
  event.target.value = '';
};

// 处理文件变化（多图）
const handleChange = async (file, uploadFileList) => {
  // 验证文件
  if (!validateFile(file.raw)) {
    return;
  }

  // 如果不需要自动上传，直接使用 fileList 中的文件对象
  if (!props.autoUpload) {
    // 等待一下让 el-upload 更新 fileList
    await nextTick();
    
    // 提取 URLs 或文件对象
    const allUrls = fileList.value
      .filter(f => f.url || f.response?.data)
      .map(f => f.response?.data || f.url);
    
    // 发送数据给父组件（包含 file 对象）
    emit('update:modelValue', fileList.value);
    emit('change', { 
      urls: allUrls,
      files: fileList.value,
      allUrls: allUrls
    });
    return;
  }
  
  // 如果需要自动上传，等待上传完成
  if (props.autoUpload && file.raw) {
    try {
      await uploadFile(file.raw);
      // 上传完成后重新获取 fileList
      uploadFileList = fileList.value;
    } catch (error) {
      console.error('上传失败:', error);
      return;
    }
  }
  
  // 提取 URLs
  const uploadedUrls = uploadFileList
    .filter(f => f.response?.data)
    .map(f => f.response.data);
  
  const allUrls = uploadFileList
    .filter(f => f.url || f.response?.data)
    .map(f => f.response?.data || f.url);
  
  // 发送数据给父组件
  emit('update:modelValue', allUrls);
  emit('change', { 
    urls: uploadedUrls,
    files: uploadFileList,
    allUrls: allUrls
  });
};

// 验证文件
const validateFile = (file) => {
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return false;
  }

  // 检查文件大小
  const maxSizeBytes = props.maxSize * 1024 * 1024;
  if (file.size > maxSizeBytes) {
    ElMessage.error(`图片大小不能超过${props.maxSize}MB`);
    return false;
  }

  return true;
};

// 上传图片到服务器
const uploadFile = async (file) => {
  try {
    const loading = ElLoading.service({ text: '上传中...' });
    
    const response = await uploadAPI.uploadImage(file);
    
    loading.close();
    
    if (response.code === 200 && response.data) {
      // 返回的是相对路径，需要拼接完整 URL
      const fullUrl = `${import.meta.env.VITE_API_BASE_URL || ''}${response.data}`;
      emit('update:modelValue', fullUrl);
      emit('change', fullUrl);
      ElMessage.success('上传成功');
    } else {
      ElMessage.error(response.message || '上传失败');
    }
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败，请重试');
  }
};

// 删除图片
const handleRemove = async (file, uploadFileList) => {
  // 安全获取 url 字符串
  let urlStr = '';
  if (file.url) {
    urlStr = typeof file.url === 'string' ? file.url : String(file.url);
  }
  
  // 如果是本地预览 URL，需要释放
  if (urlStr && urlStr.startsWith('blob:')) {
    try {
      URL.revokeObjectURL(urlStr);
    } catch (e) {
      console.warn('释放 blob URL 失败:', e);
    }
  }
  
  // 确保 uploadFileList 有效
  if (!uploadFileList || !Array.isArray(uploadFileList)) {
    uploadFileList = fileList.value;
  }
  
  if (props.multiple) {
    const urls = uploadFileList
      .filter(f => {
        // 安全过滤
        if (!f) return false;
        const fUrl = typeof f.url === 'string' ? f.url : (f.response?.data || '');
        return fUrl || f.response?.data;
      })
      .map(f => f.response?.data || (typeof f.url === 'string' ? f.url : ''));
    
    // 使用 nextTick 确保 DOM 更新后再发送事件
    await nextTick();
    
    // 先更新 fileList，再发送事件
    fileList.value = uploadFileList;
    emit('update:modelValue', urls);
    emit('remove', urls);
  } else {
    emit('update:modelValue', '');
    emit('remove');
  }
  ElMessage.success('删除成功');
};

// 超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`);
  emit('exceed');
};

// 点击图片预览（Element Plus 内置功能）
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url;
  dialogVisible.value = true;
};

// 组件卸载时清理所有临时 URL
onBeforeUnmount(() => {
  fileList.value.forEach(file => {
    if (file.url && file.url.startsWith('blob:')) {
      URL.revokeObjectURL(file.url);
    }
  });
});
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

/* 单图上传样式 */
.single-image-upload {
  width: 100%;
}

.image-preview {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #e1e5f2;
  transition: all 0.3s;
}

.image-preview:hover {
  border-color: #4A90E2;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.2);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .image-overlay {
  opacity: 1;
}

.icon {
  font-size: 1.5rem;
  color: white;
  cursor: pointer;
  transition: transform 0.3s;
}

.icon:hover {
  transform: scale(1.2);
}

.upload-placeholder {
  width: 150px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.upload-placeholder:hover {
  border-color: #4A90E2;
  background-color: #f0f7ff;
}

.upload-icon {
  font-size: 2rem;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 0.9rem;
  color: #606266;
}

.upload-hint {
  font-size: 0.8rem;
  color: #909399;
  margin-top: 5px;
}

/* 多图上传样式 */
.multiple-images-upload {
  width: 100%;
}

.upload-tip {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 8px;
}

/* 图片预览对话框 */
.image-preview-dialog {
  text-align: center;
}

.preview-large-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}
</style>
