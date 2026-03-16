<template>
  <div class="product-gallery">
    <div class="main-image">
      <img 
        :src="currentImage || (images && images[0]) || defaultImage" 
        :alt="title"
        @click="$emit('preview-image', currentImage || (images && images[0]))"
      >
    </div>
    
    <!-- 缩略图列表 -->
    <div v-if="images && images.length > 1" class="thumbnail-list">
      <div
        v-for="(image, index) in images"
        :key="index"
        :class="['thumbnail-item', { active: currentIndex === index }]"
        @click="$emit('update:index', index)"
      >
        <img :src="image" :alt="`缩略图${index + 1}`">
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: 0
  },
  title: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:index', 'preview-image']);

const defaultImage = 'https://placehold.co/500x500/4A90E2/FFFFFF?text=商品图片';

const currentImage = computed(() => {
  if (props.images && props.images.length > 0) {
    return props.images[props.currentIndex];
  }
  return null;
});
</script>

<style scoped>
.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 100%;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f0f2f5;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  max-height: 100px;
  padding: 5px 0;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active {
  border-color: #4A90E2;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-image {
    height: 300px;
  }
  
  .thumbnail-item {
    width: 60px;
    height: 60px;
  }
}
</style>
