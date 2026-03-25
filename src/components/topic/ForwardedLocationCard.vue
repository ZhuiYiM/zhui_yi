<template>
  <div v-if="location || loading" class="forwarded-location-card" @click="handleClick">
    <!-- 加载中 -->
    <div v-if="loading && !location" class="loading-location">
      <span class="loading-icon">🔄</span>
      <span class="loading-text">正在加载地点信息...</span>
    </div>
    
    <!-- 已加载内容 -->
    <div v-else-if="location">
      <div class="forwarded-header">
        <span class="forwarded-icon">📍</span>
        <span class="forwarded-label">分享地点</span>
        <span class="forwarded-name">
          {{ location.name }}
        </span>
      </div>
      
      <div class="forwarded-content">
        <div v-if="location.description" class="location-description">
          {{ location.description }}
        </div>
        <div v-if="location.building || location.floor || location.room" class="location-detail">
          <span v-if="location.building">🏢 {{ location.building }}</span>
          <span v-if="location.floor">📍 {{ location.floor }}层</span>
          <span v-if="location.room">🚪 {{ location.room }}室</span>
        </div>
      </div>
      
      <div class="forwarded-footer">
        <span class="forwarded-hint">🗺️ 点击查看地点详情</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { campusAPI } from '@/api/campus';

const props = defineProps({
  forwardedFromLocationId: {
    type: [Number, String],
    required: true
  }
});

const emit = defineEmits(['click']);

const location = ref(null);
const loading = ref(false);

// 加载地点信息
const loadLocation = async () => {
  loading.value = true;
  console.log('🔍 加载被分享地点 ID:', props.forwardedFromLocationId);
  try {
    // 使用 campusAPI 获取地点详情
    const response = await campusAPI.getLocationDetail(props.forwardedFromLocationId);
    
    console.log('📥 被分享地点响应:', response);
    
    if (response && response.id) {
      location.value = {
        id: response.id,
        name: response.name || '未知地点',
        description: response.description || '暂无描述',
        building: response.building,
        floor: response.floor,
        room: response.room
      };
      console.log('✅ 被分享地点加载成功:', location.value);
    } else {
      throw new Error('响应数据缺少 id 字段');
    }
  } catch (error) {
    console.error('❌ 加载被分享地点失败:', error);
    // 如果加载失败，使用默认信息
    location.value = {
      id: props.forwardedFromLocationId,
      name: '⚠️ 该地点信息不可用',
      description: '地点详情无法查看'
    };
  } finally {
    loading.value = false;
  }
};

const handleClick = () => {
  if (location.value && location.value.id) {
    emit('click', location.value.id);
  }
};

onMounted(() => {
  loadLocation();
});
</script>

<style scoped>
.forwarded-location-card {
  background: linear-gradient(135deg, #f0fff4 0%, #e6f7e9 100%);
  border-left: 4px solid #28a745;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
  cursor: pointer;
  transition: all 0.3s;
}

/* 加载状态 */
.loading-location {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  color: #999;
  font-size: 0.9rem;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.forwarded-location-card:hover {
  background: linear-gradient(135deg, #e6f7e9 0%, #d4edda 100%);
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.15);
}

.forwarded-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.forwarded-icon {
  font-size: 1.2rem;
}

.forwarded-label {
  color: #666;
  font-weight: 500;
}

.forwarded-name {
  color: #28a745;
  font-weight: 600;
  font-size: 1rem;
}

.forwarded-content {
  color: #555;
  line-height: 1.6;
  font-size: 0.95rem;
  margin-bottom: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.location-description {
  margin-bottom: 8px;
}

.location-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #666;
  font-size: 0.9rem;
}

.forwarded-footer {
  display: flex;
  justify-content: flex-end;
  color: #28a745;
  font-size: 0.85rem;
  font-weight: 500;
}

.forwarded-hint {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
