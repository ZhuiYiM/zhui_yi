<template>
  <div class="write-review-modal" v-if="visible">
    <div class="modal-overlay" @click="close"></div>
    <div class="modal-content" :class="{ 'mobile': isMobile }">
      <!-- 头部 -->
      <div class="modal-header">
        <h3>写评价</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <!-- 商品信息 -->
      <div class="product-info">
        <img :src="product.image || 'https://placehold.co/100x100/e0e0e0/999999?text=商品图片'" 
             :alt="product.title" 
             class="product-image">
        <div class="product-details">
          <div class="product-title">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price || orderInfo?.totalAmount || '0.00' }}</div>
        </div>
      </div>
      
      <!-- 评分 -->
      <div class="rating-section">
        <label class="rating-label">总体评分</label>
        <div class="star-rating">
          <span 
            v-for="star in 5" 
            :key="star"
            class="star"
            :class="{ active: star <= rating }"
            @click="setRating(star)"
          >
            {{ star <= rating ? '⭐' : '☆' }}
          </span>
          <span class="rating-text">{{ ratingText }}</span>
        </div>
      </div>
      
      <!-- 评价内容 -->
      <div class="content-section">
        <label class="content-label">评价内容</label>
        <textarea
          v-model="reviewContent"
          class="content-input"
          placeholder="分享您的购物体验吧~"
          maxlength="500"
          rows="6"
        />
        <div class="word-count">{{ reviewContent.length }}/500</div>
      </div>
      
      <!-- 上传图片 -->
      <div class="upload-section">
        <label class="upload-label">上传照片（选填）</label>
        <div class="upload-area">
          <div 
            v-for="(image, index) in uploadedImages" 
            :key="index"
            class="uploaded-image"
          >
            <img :src="image" alt="已上传图片" class="image-preview">
            <button class="remove-image" @click="removeImage(index)">×</button>
          </div>
          <label v-if="uploadedImages.length < 9" class="upload-btn">
            <input 
              type="file" 
              accept="image/*" 
              multiple
              @change="handleImageUpload"
              class="file-input"
            >
            <div class="upload-icon">📷</div>
            <div class="upload-text">上传图片</div>
          </label>
        </div>
      </div>
      
      <!-- 底部按钮 -->
      <div class="modal-footer">
        <button class="cancel-btn" @click="close">取消</button>
        <button 
          class="submit-btn" 
          @click="submitReview"
          :disabled="!canSubmit"
          :loading="submitting"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { reviewAPI } from '@/api/review';
import { uploadAPI } from '@/api/upload';
import { API_CONFIG } from '@/config/api';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  orderId: {
    type: Number,
    required: true
  },
  product: {
    type: Object,
    default: () => ({})
  },
  orderInfo: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'success']);

// 状态
const isMobile = ref(window.innerWidth <= 768);
const rating = ref(5);
const reviewContent = ref('');
const uploadedImages = ref([]); // 存储已上传的图片 URL
const uploadedImageFiles = ref([]); // 存储待上传的图片 File 对象
const submitting = ref(false);

// 计算属性
const canSubmit = computed(() => {
  return rating.value >= 1 && reviewContent.value.trim().length > 0;
});

const ratingText = computed(() => {
  const texts = {
    1: '非常不满意',
    2: '不满意',
    3: '一般',
    4: '满意',
    5: '非常满意'
  };
  return texts[rating.value];
});

// 监听设备变化
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

// 设置评分
const setRating = (star) => {
  rating.value = star;
};

// 处理图片上传 - 仅保存到本地，不立即上传
const handleImageUpload = async (event) => {
  const files = event.target.files;
  if (files.length === 0) return;
  
  try {
    // 限制最多上传 9 张
    const remainingSlots = 9 - (uploadedImages.value.length + uploadedImageFiles.value.length);
    const filesToUpload = Array.from(files).slice(0, remainingSlots);
    
    for (const file of filesToUpload) {
      // 检查文件大小（最大 5MB）
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning(`文件 ${file.name} 超过 5MB，已跳过`);
        continue;
      }
      
      // 创建本地预览 URL
      const localUrl = URL.createObjectURL(file);
      uploadedImages.value.push(localUrl);
      uploadedImageFiles.value.push(file);
    }
  } catch (error) {
    console.error('图片选择失败:', error);
    ElMessage.error('图片选择失败，请稍后重试');
  }
};

// 移除图片
const removeImage = (index) => {
  // 释放本地预览 URL
  if (uploadedImages.value[index] && uploadedImages.value[index].startsWith('blob:')) {
    URL.revokeObjectURL(uploadedImages.value[index]);
  }
  uploadedImages.value.splice(index, 1);
  uploadedImageFiles.value.splice(index, 1);
};

// 提交评价
const submitReview = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请填写完整评价信息');
    return;
  }
  
  try {
    submitting.value = true;
    
    const data = {
      orderId: props.orderId,
      rating: rating.value,
      content: reviewContent.value.trim(),
      images: uploadedImages.value.length > 0 
        ? uploadedImages.value.join(',') 
        : null
    };
    
    await reviewAPI.createReview(data);
    
    ElMessage.success('评价成功！');
    emit('success');
    close();
  } catch (error) {
    console.error('提交评价失败:', error);
    ElMessage.error(error.response?.data?.message || '评价失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 关闭弹窗
const close = () => {
  emit('close');
};

// 监听设备变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    window.addEventListener('resize', updateDeviceDetection);
  } else {
    window.removeEventListener('resize', updateDeviceDetection);
  }
});
</script>

<style scoped>
.write-review-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  max-height: 90vh;
  background-color: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-content.mobile {
  width: 100%;
  height: 100%;
  max-height: 100%;
  border-radius: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-content {
  overflow-y: auto;
}

/* 商品信息 */
.product-info {
  display: flex;
  gap: 15px;
  padding: 20px;
  background-color: #f8f9fa;
  margin: 0;
  border-bottom: 1px solid #e0e0e0;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-title {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}

.product-price {
  font-size: 1.2rem;
  color: #F56C6C;
  font-weight: bold;
}

/* 评分区域 */
.rating-section {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.rating-label {
  display: block;
  font-size: 0.95rem;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.star-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.star {
  font-size: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.5;
}

.star.active {
  opacity: 1;
  transform: scale(1.1);
}

.star:hover {
  transform: scale(1.2);
}

.rating-text {
  font-size: 0.95rem;
  color: #666;
  margin-left: 10px;
}

/* 评价内容 */
.content-section {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.content-label {
  display: block;
  font-size: 0.95rem;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.content-input {
  width: 100%;
  padding: 12px;
  font-size: 0.95rem;
  color: #333;
  border: 1px solid #d0d0d0;
  border-radius: 6px;
  resize: vertical;
  font-family: inherit;
}

.content-input:focus {
  outline: none;
  border-color: #4A90E2;
}

.word-count {
  text-align: right;
  font-size: 0.85rem;
  color: #999;
  margin-top: 8px;
}

/* 上传图片 */
.upload-section {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.upload-label {
  display: block;
  font-size: 0.95rem;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.upload-area {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.uploaded-image {
  position: relative;
  width: 100px;
  height: 100px;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
}

.remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.remove-image:hover {
  background-color: rgba(245, 108, 108, 0.8);
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #d0d0d0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.upload-btn:hover {
  border-color: #4A90E2;
  background-color: #f0f7ff;
}

.file-input {
  display: none;
}

.upload-icon {
  font-size: 2rem;
  margin-bottom: 5px;
}

.upload-text {
  font-size: 0.85rem;
  color: #999;
}

/* 底部按钮 */
.modal-footer {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding: 20px;
  background-color: #f8f9fa;
}

.cancel-btn {
  padding: 10px 24px;
  font-size: 0.95rem;
  color: #333;
  background-color: white;
  border: 1px solid #d0d0d0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background-color: #f5f5f5;
}

.submit-btn {
  padding: 10px 24px;
  font-size: 0.95rem;
  color: white;
  background-color: #4A90E2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  background-color: #3a80d2;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .modal-content {
    width: 100%;
    height: 100%;
    max-height: 100%;
    border-radius: 0;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-image {
    width: 60px;
    height: 60px;
  }
  
  .star {
    font-size: 1.5rem;
  }
  
  .modal-footer {
    flex-direction: column-reverse;
  }
  
  .modal-footer button {
    width: 100%;
  }
}
</style>
