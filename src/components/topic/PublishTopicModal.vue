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
          <div class="image-preview">
            <img
              v-for="(image, index) in localImages"
              :key="index"
              :src="image"
              alt="预览图片"
              class="preview-image"
            >
            <button
              v-if="localImages.length < 9 && !isShareMode"
              @click="addImage"
              class="add-image-btn"
            >
              +
            </button>
          </div>

          <!-- 四级标签选择器 -->
          <TagSelector
           ref="tagSelectorRef"
            v-model="selectedTags"
            :auto-select-level1="true"
            :read-only-level1="true"
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
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import TagSelector from './TagSelector.vue';
import { topicAPI } from '@/api/topic';

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
  }
});

// Emits
const emit = defineEmits(['update:visible', 'published', 'closed']);

// 本地数据
const localContent = ref('');
const localImages = ref([]);
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

// 添加图片
const addImage = () => {
  // TODO: 实现真实的图片上传逻辑
  const mockImages = [
    'https://placehold.co/200x200/FF6B6B/FFFFFF?text=图1',
    'https://placehold.co/200x200/4ECDC4/FFFFFF?text=图2',
    'https://placehold.co/200x200/FFE66D/333333?text=图3'
  ];

  if (localImages.value.length < 9) {
    localImages.value.push(mockImages[localImages.value.length % 3]);
    ElMessage.success('图片添加成功');
  }
};

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false);
  emit('closed');
 resetForm();
};

// 重置表单
const resetForm = () => {
  localContent.value = '';
  localImages.value = [];
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
    // 获取选中的标签
    const selectedTagsData = tagSelectorRef.value?.getSelectedTags();
    
    // 构建请求数据
    const topicData = {
      content: localContent.value,
      images: localImages.value,
      level1TagCode: selectedTagsData.level1?.code || null,
      level2TagCodes: (selectedTagsData.level2 || []).map(t => t.code),
      level3TagCodes: (selectedTagsData.level3 || []).map(t => t.code),
      level4TagCodes: (selectedTagsData.level4 || []).map(t => t.code),
      anonymous: false
    };

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

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.preview-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.add-image-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  background: #f9f9f9;
  cursor: pointer;
  font-size: 2rem;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.add-image-btn:hover {
  border-color: #4A90E2;
  color: #4A90E2;
  background: #f0f7ff;
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
