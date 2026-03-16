<template>
  <div v-if="visible" class="publish-topic-modal-overlay" @click="handleClose">
    <div class="publish-topic-modal" @click.stop>
      <div class="modal-header">
        <h2>{{ isShareMode ? '分享并发布新话题' : '发布新话题' }}</h2>
        <button @click="handleClose" class="close-btn">×</button>
      </div>

      <div class="modal-body">
        <!-- 话题内容输入 -->
        <textarea
          v-model="localContent"
          :placeholder="isShareMode ? '补充一些说明后发布...' : '分享你的想法...'"
          class="post-textarea"
         rows="5"
          :readonly="isShareMode && readonlyContent"
        ></textarea>

        <!-- 图片上传 -->
        <div class="post-media">
          <ImageUploader
            v-model="localImages"
            :multiple="true"
            :limit="9"
            hint="最多可上传 9 张图片"
            :auto-upload="false"
            :disabled="isShareMode"
            @change="handleImageChange"
          />

          <!-- 四级标签选择器 -->
          <TagSelector
           ref="tagSelectorRef"
            v-model="selectedTags"
            :auto-select-level1="true"
            :read-only-level1="true"
            :is-share-mode="isShareMode"
            :userId="userId"
            @change="handleTagChange"
          />
        </div>

        <!-- 分享信息提示 -->
        <div v-if="isShareMode && shareInfo" class="share-info-box">
          <div class="info-icon">ℹ️</div>
          <div class="info-content">
            <p class="info-title">分享内容预览</p>
            <p class="info-text">来自用户：<strong>@{{ shareInfo.author }}</strong></p>
            <p class="info-hint">转发时会自动添加“转发”标签，无需手动选择</p>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="handleClose" class="cancel-btn">取消</button>
        <button @click="handlePublish" class="submit-btn" :disabled="!canPublish || publishing">
          {{ publishing ? '发布中...' : (isShareMode ? '发布分享' : '发布') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import TagSelector from '../tag/TagSelector.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { topicAPI } from '@/api/topic';
import { uploadAPI } from '@/api/upload';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  // 初始内容
  initialContent: {
    type: String,
    default: ''
  },
  // 当前用户 ID
  userId: {
    type: [Number, String],
    default: null
  },
  // 是否为转发模式
  isShareMode: {
    type: Boolean,
    default: false
  },
  // 分享信息
  shareInfo: {
    type: Object,
    default: null
  },
  // 是否只读内容
  readonlyContent: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['update:visible', 'published', 'closed']);

// 本地数据
const localContent = ref('');
const localImages = ref([]);
const uploadedImageUrls = ref([]); // 存储已上传的 URL
const selectedTags = ref({
  level1: null,
  level2: [],
  level3: [],
  level4: []
});
const publishing = ref(false);
const canPublish = ref(true);
const tagSelectorRef = ref(null);

// 监听初始内容变化
watch(() => props.initialContent, (newVal) => {
  if (newVal) {
    localContent.value = newVal;
  }
}, { immediate: true });

// 监听 visible 变化，关闭弹窗时清空内容
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    localContent.value = '';
  }
});

// 处理标签变化
const handleTagChange = (tagsData) => {
  console.log('标签已选择:', tagsData);
  // 验证是否可以发布
  canPublish.value = !!(tagsData.level1 && tagsData.level2?.length > 0);
};

// 处理图片变化（从 ImageUploader）
const handleImageChange = (data) => {
  console.log('🖼️ 图片变化:', data);
  
  if (data && typeof data === 'object' && data.files) {
    // 保存文件对象用于后续上传
    localImages.value = data.files;
    // 不要保存 urls，因为我们还没上传，urls 是空的或者是 blob URL
    uploadedImageUrls.value = [];
    console.log('💾 保存文件对象:', localImages.value.length, '个，准备发布时上传');
  } else if (Array.isArray(data)) {
    // 如果只是 URLs 数组（可能是 blob URL）
    uploadedImageUrls.value = [];
    console.log('🔗 忽略 blob URLs，等待发布时上传');
  }
};

// 添加图片（已移除，使用 ImageUploader 组件）
// const addImage = () => {
//   // TODO: 实现真实的图片上传逻辑
//   const mockImages = [
//     'https://placehold.co/200x200/FF6B6B/FFFFFF?text=图 1',
//     'https://placehold.co/200x200/4ECDC4/FFFFFF?text=图 2',
//     'https://placehold.co/200x200/FFE66D/333333?text=图 3'
//   ];
//
//   if (localImages.value.length < 9) {
//     localImages.value.push(mockImages[localImages.value.length % 3]);
//     ElMessage.success('图片添加成功');
//   }
// };

// 关闭弹窗
const handleClose = () => {
  console.log('🚪 关闭弹窗');
  
  // 先重置表单（清理 blob URL）
  resetForm();
  
  // 直接发送关闭事件，不使用 nextTick 避免循环
  emit('update:visible', false);
  emit('closed');
};

// 重置表单
const resetForm = () => {
  console.log('🔄 重置表单');
  
  // 清理本地图片的 blob URL
  if (localImages.value && Array.isArray(localImages.value)) {
    localImages.value.forEach(file => {
      if (file && file.url && typeof file.url === 'string' && file.url.startsWith('blob:')) {
        try {
          console.log('🔓 释放 blob URL:', file.url);
          URL.revokeObjectURL(file.url);
        } catch (e) {
          console.warn('释放 blob URL 失败:', e);
        }
      }
      // 同时检查 raw 属性
      if (file && file.raw && file.raw.url && typeof file.raw.url === 'string' && file.raw.url.startsWith('blob:')) {
        try {
          console.log('🔓 释放 raw blob URL:', file.raw.url);
          URL.revokeObjectURL(file.raw.url);
        } catch (e) {
          console.warn('释放 raw blob URL 失败:', e);
        }
      }
    });
  }
  
  localContent.value = '';
  localImages.value = [];
  uploadedImageUrls.value = [];
  selectedTags.value = {
    level1: null,
    level2: [],
    level3: [],
    level4: []
  };
  canPublish.value = true;
  publishing.value = false;
};

// 发布话题
const handlePublish = async () => {
  // 验证内容
  if (!localContent.value.trim()) {
    ElMessage.warning('请输入话题内容');
    return;
  }

  // 验证标签
  if (!tagSelectorRef.value?.validate()) {
    canPublish.value = false;
    return;
  }

  publishing.value = true;
  
  try {
    // 上传图片
    let imageUrls = [];
    
    console.log('🔍 检查图片数据...');
    console.log('   uploadedImageUrls:', uploadedImageUrls.value);
    console.log('   localImages:', localImages.value);
    
    // 使用已上传的 URL 或者上传本地文件
    if (uploadedImageUrls.value && uploadedImageUrls.value.length > 0) {
      // 已经有上传好的 URLs（自动上传模式）
      console.log('✅ 使用已上传的图片 URLs:', uploadedImageUrls.value);
      imageUrls = uploadedImageUrls.value;
    } else if (localImages.value && localImages.value.length > 0) {
      // 需要手动上传本地文件
      console.log('📷 准备上传', localImages.value.length, '张图片');
      const loading = ElLoading.service({ text: '正在上传图片...' });
      
      try {
        for (const fileItem of localImages.value) {
          try {
            // 优先使用 raw 属性，如果没有则直接使用 fileItem
            const file = fileItem.raw || fileItem;
            
            console.log('📝 检查文件对象:', file);
            console.log('   fileItem:', fileItem);
            console.log('   fileItem.raw:', fileItem.raw);
            
            // 确保是有效的 File 对象
            if (!(file instanceof File) && !(file instanceof Blob)) {
              console.warn('⚠️ 文件不是有效的 File/Blob 对象:', file);
              console.warn('   file type:', typeof file);
              console.warn('   file constructor:', file?.constructor?.name);
              continue;
            }
            
            console.log('📤 准备上传图片:', file.name, file.size, file.type);
            
            const response = await uploadAPI.uploadImage(file);
            console.log('✅ 图片上传响应:', response);
            
            // 处理不同的响应格式
            let imageUrl = '';
            if (response && typeof response === 'string') {
              // 直接返回字符串路径
              imageUrl = response;
            } else if (response && response.data) {
              // 返回对象包含 data 字段
              imageUrl = response.data;
            } else if (response && response.url) {
              // 返回对象包含 url 字段
              imageUrl = response.url;
            }
            
            if (imageUrl) {
              // 确保图片 URL 是完整的
              // 如果 imageUrl 已经是 http 或 https 开头，直接使用
              // 否则需要拼接完整的 URL
              let fullUrl = imageUrl;
              if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
                // 去除可能的前导斜杠
                const cleanPath = imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl;
                fullUrl = `http://localhost:8080${cleanPath}`;
              }
              console.log('🖼️ 图片 URL:', fullUrl);
              imageUrls.push(fullUrl);
            } else {
              console.error('❌ 图片上传失败：无法获取图片 URL', response);
            }
          } catch (uploadError) {
            console.error('❌ 单张图片上传失败:', uploadError);
            if (uploadError.response) {
              console.error('响应状态:', uploadError.response.status);
              console.error('响应数据:', uploadError.response.data);
            }
          }
        }
        
        console.log('🎉 所有图片上传完成，URLs:', imageUrls);
      } finally {
        loading.close();
      }
    } else {
      console.log('ℹ️ 没有图片');
    }
    
    // 获取选中的标签
    const selectedTagsData = tagSelectorRef.value?.getSelectedTags();
    
    // 构建请求数据
    const topicData = {
      content: localContent.value,
      images: imageUrls,  // 使用上传后的 URL
      level1TagCode: selectedTagsData.level1?.code || null,
      level2TagCodes: (selectedTagsData.level2 || []).map(t => t.code),
      level3TagCodes: (selectedTagsData.level3 || []).map(t => t.code),
      level4TagCodes: (selectedTagsData.level4 || []).map(t => t.code),
      anonymous: false
    };
    
    // 如果是转发模式，添加转发相关字段
    if (props.isShareMode && props.shareInfo) {
      topicData.isForwarded = true;
      topicData.forwardedFromTopicId = parseInt(props.shareInfo.sourceId);
    }

    const response = await topicAPI.createTopic(topicData);
    
    // 发布成功（request.js 拦截器已经处理了标准响应，直接返回的是 data 字段）
    ElMessage.success('发布成功!');
    emit('published', response.data || response);
    // 确保关闭弹窗
    handleClose();
  } catch(error) {
    console.error('❌ 发布失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error('发布失败，请稍后重试');
    }
  } finally {
    publishing.value = false;
  }
};
</script>

<style scoped>
.publish-topic-modal-overlay {
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

.publish-topic-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
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
  padding: 25px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.4rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #999;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 25px;
}

.post-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e1e5f2;
  border-radius: 12px;
 resize: vertical;
  font-family: inherit;
  margin-bottom: 20px;
  transition: border-color 0.3s;
  font-size: 14px;
  line-height: 1.6;
}

.post-textarea:focus {
  outline: none;
  border-color: #4A90E2;
}

.post-textarea[readonly] {
  background: #f5f7fa;
  cursor: not-allowed;
}

.post-media {
  margin-bottom: 20px;
}

/* 分享信息提示框 */
.share-info-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e3f2fd 100%);
  border: 2px solid #4A90E2;
  border-radius: 8px;
}

.info-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-title {
  font-weight: 600;
  color: #000000; /* 修改为黑色 */
  margin: 0 0 8px 0;
  font-size: 1rem;
}

.info-text {
  color: #000000; /* 修改为黑色 */
  margin: 0 0 6px 0;
  font-size: 0.95rem;
}

.info-text strong {
  color: #4A90E2;
  font-weight: 600;
}

.info-hint {
  color: #000000; /* 修改为黑色 */
  margin: 0;
  font-size: 0.85rem;
  font-style: italic;
  opacity: 0.8;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 25px;
  border-top: 1px solid #eee;
}

.cancel-btn,
.submit-btn {
  padding: 12px 25px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .publish-topic-modal-overlay {
    padding: 10px;
  }

  .publish-topic-modal {
    max-height: 95vh;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 20px;
  }

  .modal-header h2 {
    font-size: 1.2rem;
  }
}
</style>
