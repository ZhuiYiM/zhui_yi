<template>
  <div class="action-buttons">
    <button 
      class="btn btn-primary" 
      @click="$emit('buy')"
      :disabled="productStatus !== 1 && !isOwner"
    >
      {{ buttonText }}
    </button>
    <button 
      class="btn btn-secondary" 
      @click="$emit('favorite')"
      :class="{ active: isFavorite }"
    >
      {{ isFavorite ? '⭐ 已收藏' : '☆ 收藏' }}
    </button>
    <button class="btn btn-share" @click="$emit('share')">
      🔗 分享
    </button>
    <button class="btn btn-report" @click="$emit('report')">
      ⚠️ 举报
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  productStatus: {
    type: Number,
    default: 1
  },
  isFavorite: {
    type: Boolean,
    default: false
  },
  isOwner: {
    type: Boolean,
    default: false
  }
});

// 根据商品状态和是否物主显示不同的按钮文字
const buttonText = computed(() => {
  if (props.isOwner) {
    return props.productStatus === 1 ? '下架' : '重新上架';
  }
  return props.productStatus === 2 ? '已售出' : props.productStatus === 0 ? '已下架' : '立即购买';
});

defineEmits(['buy', 'favorite', 'share', 'report']);
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f2f5;
}

.btn {
  flex: 1;
  padding: 15px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #e74c3c;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #c0392b;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: white;
  color: #f39c12;
  border: 2px solid #f39c12;
}

.btn-secondary.active {
  background-color: #f39c12;
  color: white;
}

.btn-secondary:hover {
  background-color: #f39c12;
  color: white;
}

.btn-share {
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
}

.btn-share:hover {
  background-color: #4A90E2;
  color: white;
}

.btn-report {
  background-color: white;
  color: #f56c6c;
  border: 2px solid #f56c6c;
}

.btn-report:hover {
  background-color: #f56c6c;
  color: white;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }
}
</style>
