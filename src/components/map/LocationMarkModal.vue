<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="标记位置"
    width="800px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form :model="form" label-width="100px" size="default">
      
      <!-- 1. 地图选点 -->
      <el-form-item label="选择位置" required>
        <div class="map-picker-container">
          <div class="map-tips">
            <el-icon><Location /></el-icon>
            <span>拖动地图调整位置</span>
          </div>
          <div class="mini-map" ref="miniMapRef">
            <!-- 中心固定标记点 -->
            <div class="center-marker">
              <span class="marker-pin">📍</span>
            </div>
            <!-- 可拖动的地图容器 -->
            <div 
              class="draggable-map"
              @mousedown="startDrag"
              @mousemove="onDrag"
              @mouseup="endDrag"
              @mouseleave="endDrag"
            >
              <!-- 手绘地图底图 -->
              <img 
                src="@/assets/01.png" 
                alt="校园手绘地图" 
                class="mini-map-image"
                :style="mapImageStyle"
                draggable="false"
              />
            </div>
          </div>
          <div class="location-info">
            <el-input 
              v-model="form.locationName" 
              placeholder="位置名称（如：图书馆门口）"
              clearable
              readonly
            />
            <el-input 
              v-model="form.addressDetail" 
              placeholder="详细地址描述"
              clearable
              style="margin-top: 8px;"
            />
          </div>
        </div>
      </el-form-item>

      <!-- 2. 标记类型 -->
      <el-form-item label="标记类型" required>
        <el-radio-group v-model="form.markType">
          <el-radio label="meeting_point">
            <span class="radio-with-icon">🤝 约见地点</span>
          </el-radio>
          <el-radio label="merchant_shop">
            <span class="radio-with-icon">🏪 店铺位置</span>
          </el-radio>
          <el-radio label="organization_activity">
            <span class="radio-with-icon">👥 活动地点</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 3. 地点分类 -->
      <el-form-item label="地点分类">
        <el-select v-model="form.markCategory" placeholder="请选择地点分类">
          <el-option label="建筑物" value="building" />
          <el-option label="区域" value="area" />
          <el-option label="设施" value="facility" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <!-- 4. 描述信息 -->
      <el-form-item label="位置描述">
        <el-input 
          v-model="form.description" 
          type="textarea"
          :rows="3"
          placeholder="请描述这个位置的特点或用途"
        />
      </el-form-item>

      <!-- 5. 联系方式 -->
      <el-form-item label="联系方式">
        <el-input 
          v-model="form.contactInfo" 
          placeholder="手机号或微信号（可选）"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>

      <!-- 6. 图片上传 -->
      <el-form-item label="位置图片">
        <ImageUploader 
          v-model="form.images"
          :max-count="3"
          tip="最多上传 3 张图片"
        />
      </el-form-item>

      <!-- 7. 时间设置 -->
      <el-form-item label="时间设置">
        <div class="time-settings">
          <el-checkbox v-model="form.isPermanent">永久标记</el-checkbox>
          <el-date-picker
            v-if="!form.isPermanent"
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="margin-top: 8px; width: 100%;"
          />
        </div>
      </el-form-item>

      <!-- 8. 可见性设置 -->
      <el-form-item label="可见性">
        <el-select v-model="form.visibility">
          <el-option label="公开（所有人可见）" value="public" />
          <el-option label="好友可见" value="friends" />
          <el-option label="仅自己可见" value="private" />
        </el-select>
      </el-form-item>

    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        提交标记
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Location } from '@element-plus/icons-vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { campusAPI } from '@/api/campus';
import handdrawnMapLocations from '@/data/handdrawn-map-locations.json';

const props = defineProps({
  modelValue: Boolean,
  defaultCampusId: Number
});

const emit = defineEmits(['update:modelValue', 'success']);

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const miniMapRef = ref(null);
const submitting = ref(false);
const timeRange = ref([]);

// 地图拖拽相关状态
const mapPosition = ref({ x: 0, y: 0 }); // 地图偏移量
const isDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const mapScale = ref(1.5); // 地图放大倍数

// 计算地图图片样式
const mapImageStyle = computed(() => {
  return {
    transform: `translate(${mapPosition.value.x}px, ${mapPosition.value.y}px) scale(${mapScale.value})`,
    transition: isDragging.value ? 'none' : 'transform 0.1s'
  };
});

// 表单数据
const form = ref({
  campusId: props.defaultCampusId || 1,
  locationName: '',
  latitude: null,
  longitude: null,
  addressDetail: '',
  markType: 'meeting_point',
  markCategory: 'building',
  description: '',
  contactInfo: '',
  images: [],
  startTime: null,
  endTime: null,
  isPermanent: false,
  visibility: 'public'
});

// 监听时间范围变化
watch(timeRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    form.value.startTime = newVal[0];
    form.value.endTime = newVal[1];
  } else {
    form.value.startTime = null;
    form.value.endTime = null;
  }
});

// 开始拖拽
const startDrag = (e) => {
  isDragging.value = true;
  dragStart.value = {
    x: e.clientX - mapPosition.value.x,
    y: e.clientY - mapPosition.value.y
  };
};

// 拖拽中
const onDrag = (e) => {
  if (!isDragging.value) return;
  
  mapPosition.value = {
    x: e.clientX - dragStart.value.x,
    y: e.clientY - dragStart.value.y
  };
  
  // 拖拽时实时更新中心点对应的地点
  updateCenterLocation();
};

// 结束拖拽
const endDrag = () => {
  isDragging.value = false;
  // 拖拽结束后更新一次地点
  updateCenterLocation();
};

// 更新中心点对应的地点信息
const updateCenterLocation = () => {
  if (!miniMapRef.value) return;
  
  const rect = miniMapRef.value.getBoundingClientRect();
  const containerWidth = rect.width;
  const containerHeight = rect.height;
  
  // 计算容器中心点在地图图片上的坐标
  const centerX = containerWidth / 2 - mapPosition.value.x;
  const centerY = containerHeight / 2 - mapPosition.value.y;
  
  // 获取地图图片的实际尺寸
  const imgElement = miniMapRef.value.querySelector('.mini-map-image');
  if (!imgElement) return;
  
  const imgRect = imgElement.getBoundingClientRect();
  const imgWidth = imgRect.width;
  const imgHeight = imgRect.height;
  
  // 转换为百分比坐标
  const percentX = (centerX / imgWidth) * 100;
  const percentY = (centerY / imgHeight) * 100;
  
  console.log('中心点位置:', { percentX, percentY });
  
  // 根据中心点位置查找最近的地点
  const nearestLocation = findNearestLocation(percentX, percentY);
  
  if (nearestLocation) {
    form.value.latitude = nearestLocation.latitude || 35.307736;
    form.value.longitude = nearestLocation.longitude || 113.926765;
    form.value.locationName = nearestLocation.name;
  } else {
    // 如果没有找到匹配的地点，使用百分比计算经纬度
    const centerLat = 35.307736;
    const centerLng = 113.926765;
    const mapLatRange = 0.01;
    const mapLngRange = 0.01;
    
    const latOffset = (50 - percentY) / 100 * mapLatRange;
    const lngOffset = (percentX - 50) / 100 * mapLngRange;
    
    form.value.latitude = centerLat + latOffset;
    form.value.longitude = centerLng + lngOffset;
    form.value.locationName = '自定义位置';
  }
};

// 处理地图点击（基于手绘地图）
const handleMapClick = (event) => {
  // 保留原有逻辑，但现在主要通过拖拽来选点
  console.log('地图点击（建议使用拖拽方式）');
};

// 查找最近的地点
const findNearestLocation = (percentX, percentY) => {
  const threshold = 5; // 阈值，5% 范围内算匹配
  
  for (const loc of handdrawnMapLocations) {
    const dx = Math.abs(loc.x - percentX);
    const dy = Math.abs(loc.y - percentY);
    
    if (dx < threshold && dy < threshold) {
      return loc;
    }
  }
  
  return null;
};

// 表单验证
const validateForm = () => {
  if (!form.value.campusId) {
    ElMessage.error('请选择校区');
    return false;
  }
  
  if (!form.value.locationName || !form.value.locationName.trim()) {
    ElMessage.error('请输入位置名称');
    return false;
  }
  
  if (!form.value.latitude || !form.value.longitude) {
    ElMessage.error('请选择位置坐标');
    return false;
  }
  
  if (!form.value.markType) {
    ElMessage.error('请选择标记类型');
    return false;
  }
  
  return true;
};

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }
  
  submitting.value = true;
  
  try {
    const data = {
      campusId: form.value.campusId,
      locationName: form.value.locationName.trim(),
      latitude: form.value.latitude,
      longitude: form.value.longitude,
      addressDetail: form.value.addressDetail,
      markType: form.value.markType,
      markCategory: form.value.markCategory,
      description: form.value.description,
      contactInfo: form.value.contactInfo,
      images: form.value.images,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      isPermanent: form.value.isPermanent,
      visibility: form.value.visibility
    };
    
    const result = await campusAPI.createLocationMark(data);
    
    if (result.code === 200) {
      ElMessage.success(result.data.message || '标记创建成功');
      dialogVisible.value = false;
      emit('success', result.data);
    } else {
      ElMessage.error(result.message || '创建失败');
    }
  } catch (error) {
    console.error('创建标记失败:', error);
    ElMessage.error('创建失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 对话框关闭后的清理
const handleClosed = () => {
  form.value = {
    campusId: props.defaultCampusId || 1,
    locationName: '',
    latitude: null,
    longitude: null,
    addressDetail: '',
    markType: 'meeting_point',
    markCategory: 'building',
    description: '',
    contactInfo: '',
    images: [],
    startTime: null,
    endTime: null,
    isPermanent: false,
    visibility: 'public'
  };
  timeRange.value = [];
};
</script>

<style scoped>
.map-picker-container {
  width: 100%;
}

.map-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 12px;
  font-size: 14px;
}

.mini-map {
  width: 100%;
  height: 350px; /* 减小容器高度 */
  background: #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

/* 中心固定标记点 */
.center-marker {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 100;
  pointer-events: none;
}

.marker-pin {
  font-size: 1.5rem; /* 减小标记大小 */
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); } /* 减小弹跳幅度 */
}

/* 可拖动的地图容器 */
.draggable-map {
  width: 100%;
  height: 100%;
  cursor: grab;
  user-select: none;
}

.draggable-map:active {
  cursor: grabbing;
}

.mini-map-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 使用 cover 填满容器，显示更多细节 */
  user-select: none;
  will-change: transform;
}

.location-info {
  margin-top: 12px;
}

.time-settings {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
