<template>
  <div class="image-preview-container">
    <!-- 图片列表 -->
    <div v-if="images && images.length > 0" :class="['image-list', layout]">
      <div 
        v-for="(img, index) in images" 
        :key="index"
        class="image-item-wrapper"
      >
        <div class="image-item" @click="handlePreview(index)">
          <img :src="img" :alt="`图片${index + 1}`" class="image-thumb" loading="lazy">
          
          <!-- 删除按钮（可配置显示） -->
          <button 
            v-if="showDelete" 
            class="delete-btn"
            @click.stop="handleDelete(index)"
          >
            <el-icon><Close /></el-icon>
          </button>
          
          <!-- 序号标记 -->
          <div v-if="showIndex" class="index-badge">{{ index + 1 }}</div>
        </div>
      </div>
      
      <!-- 添加按钮（多图模式） -->
      <div 
        v-if="multiple && images.length < maxCount" 
        class="add-image-item"
        @click="$emit('add')"
      >
        <el-icon class="add-icon"><Plus /></el-icon>
        <span class="add-text">添加</span>
      </div>
    </div>

    <!-- 空状态提示 -->
    <div v-else-if="emptyText" class="empty-state">
      {{ emptyText }}
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog 
      v-model="previewVisible" 
      :title="`图片预览 (${currentIndex + 1}/${images.length})`"
      class="image-preview-dialog"
      :close-on-click-modal="true"
    >
      <div class="preview-content">
        <img :src="currentImageUrl" alt="预览图片" class="preview-image">
        
        <!-- 预览操作栏 -->
        <div class="preview-actions">
          <button class="action-btn" @click="downloadImage" title="下载">
            <el-icon><Download /></el-icon>
          </button>
          <button 
            v-if="showDelete" 
            class="action-btn danger" 
            @click="handleDeleteFromPreview" 
            title="删除"
          >
            <el-icon><Delete /></el-icon>
          </button>
        </div>
        
        <!-- 左右切换按钮 -->
        <button class="nav-btn prev" @click="navigate(-1)" :disabled="currentIndex === 0">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button class="nav-btn next" @click="navigate(1)" :disabled="currentIndex === images.length - 1">
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Close, Download, Delete, ArrowLeft, ArrowRight } from '@element-plus/icons-vue';

const props = defineProps({
  // 图片数组
  images: {
    type: Array,
    default: () => []
  },
  // 是否支持多选/添加
  multiple: {
    type: Boolean,
    default: false
  },
  // 最大数量
  maxCount: {
    type: Number,
    default: 9
  },
  // 布局方式：'grid' | 'list'
  layout: {
    type: String,
    default: 'grid',
    validator: (value) => ['grid', 'list'].includes(value)
  },
  // 显示删除按钮
  showDelete: {
    type: Boolean,
    default: false
  },
  // 显示序号
  showIndex: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['add', 'remove', 'delete', 'preview']);

const previewVisible = ref(false);
const currentIndex = ref(0);

// 当前预览的图片 URL
const currentImageUrl = computed(() => {
  if (props.images && props.images.length > 0) {
    return props.images[currentIndex.value];
  }
  return '';
});

// 打开预览
const handlePreview = (index) => {
  currentIndex.value = index;
  previewVisible.value = true;
  emit('preview', { index, url: props.images[index] });
};

// 导航（上一张/下一张）
const navigate = (direction) => {
  const newIndex = currentIndex.value + direction;
  if (newIndex >= 0 && newIndex < props.images.length) {
    currentIndex.value = newIndex;
  }
};

// 删除图片
const handleDelete = (index) => {
  emit('delete', index);
  emit('remove', props.images[index]);
  ElMessage.success('删除成功');
};

// 从预览中删除
const handleDeleteFromPreview = () => {
  handleDelete(currentIndex.value);
  if (currentIndex.value >= props.images.length - 1) {
    currentIndex.value = Math.max(0, props.images.length - 2);
  }
  if (props.images.length <= 1) {
    previewVisible.value = false;
  }
};

// 下载图片
const downloadImage = async () => {
  try {
    const url = currentImageUrl.value;
    const response = await fetch(url);
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `image_${Date.now()}.jpg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
    
    ElMessage.success('下载成功');
  } catch (error) {
    console.error('下载失败:', error);
    ElMessage.error('下载失败，请尝试右键保存图片');
  }
};
</script>

<style scoped>
.image-preview-container {
  width: 100%;
}

/* 图片列表样式 */
.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.image-list.grid {
  /* 网格布局 */
}

.image-list.list {
  flex-direction: column;
}

.image-item-wrapper {
  position: relative;
}

.image-item {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #e1e5f2;
  transition: all 0.3s;
  background-color: #fafafa;
}

.image-item:hover {
  border-color: #4A90E2;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
  transform: translateY(-2px);
}

.image-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item:hover .image-thumb {
  transform: scale(1.05);
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 10;
}

.image-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(255, 0, 0, 0.8);
}

/* 序号标记 */
.index-badge {
  position: absolute;
  top: 5px;
  left: 5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
}

/* 添加按钮 */
.add-image-item {
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

.add-image-item:hover {
  border-color: #4A90E2;
  background-color: #f0f7ff;
}

.add-icon {
  font-size: 2rem;
  color: #8c939d;
  margin-bottom: 5px;
}

.add-text {
  font-size: 0.9rem;
  color: #606266;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 30px;
  color: #909399;
  font-size: 0.9rem;
}

/* 预览对话框 */
.image-preview-dialog {
  max-width: 90vw;
}

.preview-content {
  position: relative;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

/* 预览操作栏 */
.preview-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #4A90E2;
  color: white;
  transform: scale(1.1);
}

.action-btn.danger:hover {
  background: #F56C6C;
}

/* 导航按钮 */
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 10;
}

.nav-btn.prev {
  left: 20px;
}

.nav-btn.next {
  right: 20px;
}

.nav-btn:hover {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .image-item,
  .add-image-item {
    width: 100px;
    height: 100px;
  }
  
  .nav-btn {
    width: 35px;
    height: 35px;
  }
  
  .nav-btn.prev {
    left: 10px;
  }
  
  .nav-btn.next {
    right: 10px;
  }
}
</style>
