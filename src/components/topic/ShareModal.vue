<template>
  <div v-if="visible" class="share-modal-overlay" @click="handleClose">
    <div class="share-modal" @click.stop>
      <div class="share-header">
        <h3>{{ sourceType === 'product' ? '分享商品' : '分享话题' }}</h3>
        <button class="close-btn" @click="handleClose">×</button>
      </div>
      
      <div class="share-body">
        <div class="share-option" @click="copyLink">
          <div class="option-icon">🔗</div>
          <div class="option-content">
            <div class="option-title">复制链接</div>
            <div class="option-desc">复制{{ sourceType === 'product' ? '商品' : '话题' }}链接到剪贴板</div>
          </div>
        </div>
        
        <div class="share-option" @click="repost">
          <div class="option-icon">🔁</div>
          <div class="option-content">
            <div class="option-title">转发</div>
            <div class="option-desc">带上你的评论转发到话题墙</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  topicId: {
    type: [Number, String],
    required: true
  },
  topicUrl: {
    type: String,
    required: true
  },
  sourceType: {
    type: String,
    default: 'topic', // 'topic' 或 'product'
    validator: (value) => ['topic', 'product'].includes(value)
  }
});

const emit = defineEmits(['update:visible', 'copy', 'repost']);

const handleClose = () => {
  emit('update:visible', false);
};

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.topicUrl);
    ElMessage.success('链接已复制到剪贴板');
    emit('copy', props.topicUrl);
    handleClose();
  } catch (error) {
    console.error('复制失败:', error);
    ElMessage.error('复制失败，请手动复制');
  }
};

const repost = () => {
  emit('repost', props.topicId);
  handleClose();
};
</script>

<style scoped>
.share-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.share-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
}

.share-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
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

.share-body {
  padding: 20px 25px;
}

.share-option {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.share-option:last-child {
  margin-bottom: 0;
}

.share-option:hover {
  background: #f5f7fa;
  transform: translateX(5px);
}

.option-icon {
  font-size: 2rem;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f7ff;
  border-radius: 50%;
}

.option-content {
  flex: 1;
}

.option-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  font-size: 1rem;
}

.option-desc {
  color: #999;
  font-size: 0.85rem;
}
</style>
