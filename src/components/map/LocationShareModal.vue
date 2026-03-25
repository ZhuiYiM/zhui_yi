<template>
  <div v-if="visible" class="share-modal-overlay" @click="handleClose">
    <div class="share-modal" @click.stop>
      <div class="share-header">
        <h3>分享地点</h3>
        <button class="close-btn" @click="handleClose">×</button>
      </div>
      
      <div class="share-body">
        <div class="share-option" @click="copyLink">
          <div class="option-icon">🔗</div>
          <div class="option-content">
            <div class="option-title">复制链接</div>
            <div class="option-desc">复制地点链接到剪贴板</div>
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
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  locationId: {
    type: [Number, String],
    required: true
  },
  locationName: {
    type: String,
    required: true
  },
  locationUrl: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['copy', 'repost', 'close']);

// 关闭弹窗
const handleClose = () => {
  emit('close');
};

// 复制链接
const copyLink = () => {
  if (!props.locationUrl) {
    ElMessage.warning('地点链接不可用');
    return;
  }
  
  navigator.clipboard.writeText(props.locationUrl).then(() => {
    ElMessage.success('地点链接已复制到剪贴板');
    emit('copy', props.locationUrl);
    emit('close');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};

// 转发到话题墙
const repost = () => {
  if (!props.locationId) {
    ElMessage.warning('地点信息不完整');
    return;
  }
  
  // 生成分享数据
  const shareData = {
    sourceType: 'location',
    sourceId: props.locationId,
    content: `我发现了一个好地方：${props.locationName}`,
    author: '匿名用户', // 实际作者信息应该由父组件传入
    url: props.locationUrl
  };
  
  // 将分享数据存储到 sessionStorage
  sessionStorage.setItem('shareData', JSON.stringify(shareData));
  
  // 跳转到话题墙页面，并传递参数
  router.push({
    path: '/topicwall',
    query: {
      from: 'share',
      sourceType: 'location',
      sourceId: props.locationId
    }
  });
  
  ElMessage.success('正在跳转到发布页面...');
  emit('repost', shareData);
  emit('close');
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
  z-index: 3000;
}

.share-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #e0e0e0;
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
