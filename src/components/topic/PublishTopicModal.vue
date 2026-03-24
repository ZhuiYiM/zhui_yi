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
            :hint="isShareMode ? '转发时可添加新图片（不继承原话题图片）' : '最多可上传 9 张图片'"
            :auto-upload="false"
            @change="handleImageChange"
          />

          <!-- 话题分类和地点标签选择器 -->
          <TagSelector
           ref="tagSelectorRef"
            v-model="selectedTags"
            :auto-select-level1="true"
            :read-only-level1="true"
            :is-share-mode="isShareMode"
            :userId="userId"
            :show-level4="false"
            @change="handleTagChange"
          />
        </div>

        <!-- 分享信息提示 -->
        <div v-if="isShareMode && shareInfo" :class="['share-info-box', { 'product-share': shareInfo.sourceType === 'product' }]">
          <div class="info-icon">{{ shareInfo.sourceType === 'product' ? '🛍️' : 'ℹ️' }}</div>
          <div class="info-content">
            <p class="info-title">{{ shareInfo.sourceType === 'product' ? '商品信息预览' : '分享内容预览' }}</p>
            <p class="info-text">来自用户：<strong>@{{ shareInfo.author }}</strong></p>
            <p v-if="shareInfo.sourceType === 'topic'" class="info-hint">转发时可以添加新图片，不会继承原话题的图片</p>
            <p v-else-if="shareInfo.sourceType === 'product'" class="info-hint">转发时会自动添加"商品分享"标签，无需手动选择</p>
            <p v-else class="info-hint">{{ shareInfo.sourceType === 'product' ? '转发时会自动添加"商品分享"标签，无需手动选择' : '转发时会自动添加"话题转发"标签，无需手动选择' }}</p>
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
  level3: []
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
  // 验证是否可以发布
  canPublish.value = !!(tagsData.level1 && tagsData.level2?.length > 0);
};

// 处理图片变化（从 ImageUploader）
const handleImageChange = (data) => {
  if (data && typeof data === 'object' && data.files) {
    // 保存文件对象用于后续上传
    localImages.value = data.files;
    // 不要保存 urls，因为我们还没上传，urls 是空的或者是 blob URL
    uploadedImageUrls.value = [];
  } else if (Array.isArray(data)) {
    // 如果只是 URLs 数组（可能是 blob URL）
    uploadedImageUrls.value = [];
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
  // 先重置表单（清理 blob URL）
  resetForm();
  
  // 直接发送关闭事件，不使用 nextTick 避免循环
  emit('update:visible', false);
  emit('closed');
};

// 重置表单
const resetForm = () => {
  // 清理本地图片的 blob URL
  if (localImages.value && Array.isArray(localImages.value)) {
    localImages.value.forEach(file => {
      if (file && file.url && typeof file.url === 'string' && file.url.startsWith('blob:')) {
        try {
          URL.revokeObjectURL(file.url);
        } catch (e) {
          console.warn('释放 blob URL 失败:', e);
        }
      }
      // 同时检查 raw 属性
      if (file && file.raw && file.raw.url && typeof file.raw.url === 'string' && file.raw.url.startsWith('blob:')) {
        try {
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
    
    if (uploadedImageUrls.value && uploadedImageUrls.value.length > 0) {
      // 使用已上传的图片 URL
      imageUrls = uploadedImageUrls.value;
    }

    // 获取选中的标签
    const selectedTagsData = tagSelectorRef.value?.getSelectedTags();
    
    // 构建请求数据
    const topicData = {
      content: localContent.value,
      images: imageUrls,  // 使用上传后的 URL
      level1TagCode: selectedTagsData.level1?.code || null,
      // 话题标签（level2）
      topicTagCodes: (selectedTagsData.level2 || []).map(t => t.code),
      // 地点标签（level3）
      locationTagCodes: (selectedTagsData.level3 || []).map(t => t.code),
      anonymous: false
    };
    
    // 如果有本地图片且未上传，现在上传图片
    if (localImages.value && localImages.value.length > 0) {
      const loading = ElLoading.service({ text: '正在上传图片...' });
      
      try {
        for (const fileItem of localImages.value) {
          try {
            // 优先使用 raw 属性，如果没有则直接使用 fileItem
            const file = fileItem.raw || fileItem;
            
            // 确保是有效的 File 对象
            if (!(file instanceof File) && !(file instanceof Blob)) {
              console.warn('⚠️ 文件不是有效的 File/Blob 对象:', file);
              continue;
            }
            
            try {
              // 直接传递 file 对象，而不是 formData
              const response = await uploadAPI.uploadImage(file);

              if (typeof response === 'string') {
                // 后端返回的是相对路径 /images/xxx.jpg，直接使用，不要拼接 /api
                imageUrls.push(response);
              } else if (response && response.url) {
                // 完整 URL 已经由后端返回
                imageUrls.push(response.url);
              }
            } catch (error) {
              console.error('❌ 图片上传失败:', error);

              if (error.response) {
                console.error('响应状态:', error.response.status);
                console.error('响应数据:', error.response.data);
              }
            }
          } catch (uploadError) {
            console.error('❌ 单张图片上传失败:', uploadError);
            if (uploadError.response) {
              console.error('响应状态:', uploadError.response.status);
              console.error('响应数据:', uploadError.response.data);
            }
          }
        }
        
        // 所有图片上传完成，更新 topicData
        if (imageUrls.length > 0) {
          topicData.images = imageUrls; // 直接传递数组，不要 stringify
        }
      } finally {
        loading.close();
      }
    }
    
    // 如果是转发模式，区分商品分享和话题转发
    if (props.isShareMode && props.shareInfo) {
      // 判断是商品分享还是话题转发
      if (props.shareInfo.sourceType === 'product') {
        // 商品分享：使用创建话题 API，添加 forwardedFromProductId
        const topicData = {
          content: localContent.value,
          images: imageUrls,  // 使用上面处理过的图片 URL
          level1TagCode: selectedTagsData.level1?.code || null,
          topicTagCodes: (selectedTagsData.level2 || []).map(t => t.code),
          locationTagCodes: (selectedTagsData.level3 || []).map(t => t.code),
          anonymous: false,
          isForwarded: true,
          forwardedFromProductId: parseInt(props.shareInfo.sourceId || props.shareInfo.id)
        };
        
        try {
          const response = await topicAPI.createTopic(topicData);
          ElMessage.success('发布成功!');
          emit('published', response.data || response);
          handleClose();
          return;
        } catch (error) {
          console.error('❌ 商品分享失败:', error.message);
          throw error;
        }
      } else {
        // 话题转发：使用转发 API
        const originalTopicId = parseInt(props.shareInfo.sourceId || props.shareInfo.id);
        
        if (!isNaN(originalTopicId)) {
          try {
            // 使用已上传的图片 URL（如果有本地图片，会在上面的上传逻辑中处理）
            const response = await topicAPI.forwardTopic(originalTopicId, localContent.value, imageUrls);
            ElMessage.success('转发成功!');
            emit('published', response.data || response);
            handleClose();
            return;
          } catch (error) {
            console.error('❌ 转发失败:', error.message);
            throw error;
          }
        } else {
          ElMessage.error('无效的话题 ID');
          publishing.value = false;
          return;
        }
      }
    }
    
    // 正常发布流程
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

/* 商品分享样式 */
.share-info-box.product-share {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  border-color: #FF6B6B;
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
