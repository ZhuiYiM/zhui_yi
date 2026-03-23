<template>
  <div :class="['ad-banner', { 'mobile': isMobile }]">
    <!-- 轮播广告容器 -->
    <div class="banner-container">
      <div 
        v-for="(ad, index) in ads" 
        :key="ad.id"
        :class="['banner-item', { active: currentAdIndex === index }]"
        @click="handleAdClick(ad)"
      >
        <img :src="ad.image" :alt="ad.title" class="banner-image">
        <div class="banner-content">
          <h3 class="banner-title">{{ ad.title }}</h3>
          <p class="banner-desc">{{ ad.description }}</p>
          <button v-if="ad.actionText" class="banner-action">{{ ad.actionText }}</button>
        </div>
      </div>
      
      <!-- 轮播指示器 -->
      <div v-if="ads.length > 1" class="banner-indicators">
        <span 
          v-for="(ad, index) in ads" 
          :key="'indicator-' + index"
          :class="['indicator', { active: currentAdIndex === index }]"
          @click="goToAd(index)"
        ></span>
      </div>
      
      <!-- 左右箭头（桌面端） -->
      <button 
        v-if="!isMobile && ads.length > 1" 
        class="banner-arrow prev-arrow"
        @click="prevAd"
      >
        ‹
      </button>
      <button 
        v-if="!isMobile && ads.length > 1" 
        class="banner-arrow next-arrow"
        @click="nextAd"
      >
        ›
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  isMobile: {
    type: Boolean,
    default: false
  },
  // 允许父组件传入自定义广告数据
  customAds: {
    type: Array,
    default: null
  }
});

const emit = defineEmits(['ad-click']);

const router = useRouter();
const currentAdIndex = ref(0);
let autoPlayTimer = null;

// 预设广告数据
const defaultAds = [
  {
    id: 1,
    title: '毕业季二手书特卖',
    description: '低至 3 折，包邮到家',
    image: 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=毕业季特卖',
    actionText: '立即选购',
    link: '/mall?category=books'
  },
  {
    id: 2,
    title: '校园快递代取服务',
    description: '首单免费，快速便捷',
    image: 'https://placehold.co/800x300/4A90E2/FFFFFF?text=快递代取',
    actionText: '预约服务',
    link: '/mall?category=service'
  },
  {
    id: 3,
    title: '学霸笔记合集',
    description: '各科重点整理，考试必备',
    image: 'https://placehold.co/800x300/50C878/FFFFFF?text=学霸笔记',
    actionText: '查看详情',
    link: '/mall?keyword=笔记'
  }
];

// 使用自定义广告或默认广告
const ads = computed(() => props.customAds || defaultAds);

// 自动轮播
const startAutoPlay = () => {
  if (ads.value.length <= 1) return;
  
  autoPlayTimer = setInterval(() => {
    nextAd();
  }, 5000); // 5 秒切换一次
};

const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
};

const nextAd = () => {
  currentAdIndex.value = (currentAdIndex.value + 1) % ads.value.length;
  incrementViewCount(); // 切换广告时记录新的展示次数
};

const prevAd = () => {
  currentAdIndex.value = (currentAdIndex.value - 1 + ads.value.length) % ads.value.length;
};

const goToAd = (index) => {
  currentAdIndex.value = index;
};

const handleAdClick = (ad) => {
  emit('ad-click', ad);
  
  if (ad.link) {
    router.push(ad.link);
  }
};

// 增加展示次数的函数（使用防抖）
let viewCountTimer = null;
const incrementViewCount = () => {
  if (viewCountTimer) {
    clearTimeout(viewCountTimer);
  }
  
  viewCountTimer = setTimeout(() => {
    // TODO: 调用后端接口增加展示次数
    console.log('👁️ 广告展示次数 +1');
  }, 3000); // 3 秒后才计为有效展示
};

onMounted(() => {
  startAutoPlay();
  incrementViewCount(); // 首次加载时记录展示次数
});

onUnmounted(() => {
  stopAutoPlay();
});
</script>

<style scoped>
.ad-banner {
  margin: 15px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.banner-container {
  position: relative;
  width: 100%;
  height: 300px;
  background-color: #f0f2f5;
}

.banner-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.5s ease;
  cursor: pointer;
}

.banner-item.active {
  opacity: 1;
  z-index: 1;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 30px;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
  color: white;
}

.banner-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
}

.banner-desc {
  margin: 0 0 12px;
  font-size: 1rem;
  opacity: 0.9;
}

.banner-action {
  padding: 8px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.banner-action:hover {
  background-color: #5a9fd6;
  transform: translateY(-2px);
}

/* 轮播指示器 */
.banner-indicators {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 2;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background-color: white;
  width: 30px;
  border-radius: 5px;
}

/* 箭头按钮 */
.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(255,255,255,0.3);
  color: white;
  border: none;
  font-size: 2.5rem;
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  z-index: 2;
}

.banner-arrow:hover {
  background-color: rgba(255,255,255,0.6);
}

.prev-arrow {
  left: 20px;
}

.next-arrow {
  right: 20px;
}

/* 移动端适配 */
.ad-banner.mobile {
  margin: 10px;
}

.ad-banner.mobile .banner-container {
  height: 200px;
}

.ad-banner.mobile .banner-title {
  font-size: 1.1rem;
}

.ad-banner.mobile .banner-desc {
  font-size: 0.85rem;
}

.ad-banner.mobile .banner-action {
  padding: 6px 15px;
  font-size: 0.8rem;
}
</style>
