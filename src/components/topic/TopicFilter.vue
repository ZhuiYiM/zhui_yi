<template>
  <section class="tag-selector-section">
    <!-- 二级标签 -->
    <div class="tag-level level-2">
      <div class="tags-container multi-select">
        <span
          v-for="tag in displayLevel2Tags"
          :key="tag.code"
          @click="handleToggleLevel2(tag)"
          class="tag-item chip"
          :class="{ active: selectedLevel2.includes(tag.code) }"
          :style="isSelectedLevel2(tag) ? { backgroundColor: tag.color + '20', borderColor: tag.color } : {}"
        >
          {{ tag.name }}
        </span>
      </div>
    </div>
    
    <!-- 三级标签 -->
    <div class="tag-level level-3">
      <div class="tags-container multi-select">
        <span
          v-for="tag in displayLevel3Tags"
          :key="tag.code"
          @click="handleToggleLevel3(tag)"
          class="tag-item chip location"
          :class="{ active: selectedLevel3.includes(tag.code) }"
        >
          📍 {{ tag.name }}
        </span>
      </div>
    </div>
    
    <!-- 清除筛选 -->
    <div class="clear-filters" v-if="hasActiveFilters">
      <button @click="handleClearAll" class="clear-btn">
        ✕ 清除所有筛选
      </button>
    </div>
    
    <!-- 自定义话题标签按钮 -->
    <div class="custom-tag-action">
      <el-button type="primary" size="small" @click="$emit('show-custom-tag-modal')">
        🏷️ 自定义话题标签
      </el-button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';

// Props
const props = defineProps({
  displayLevel2Tags: {
    type: Array,
    required: true
  },
  displayLevel3Tags: {
    type: Array,
    required: true
  },
  selectedLevel2: {
    type: Array,
    default: () => []
  },
  selectedLevel3: {
    type: Array,
    default: () => []
  },
  hasActiveFilters: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['toggle-level2', 'toggle-level3', 'clear-all', 'show-custom-tag-modal']);

// 判断二级标签是否被选中
const isSelectedLevel2 = (tag) => {
 return props.selectedLevel2.includes(tag.code);
};

// 处理二级标签切换
const handleToggleLevel2 = (tag) => {
  emit('toggle-level2', tag);
};

// 处理三级标签切换
const handleToggleLevel3 = (tag) => {
  emit('toggle-level3', tag);
};

// 处理清除所有筛选
const handleClearAll = () => {
  emit('clear-all');
};
</script>

<style scoped>
.tag-selector-section {
  margin-bottom: 25px;
}

.tag-level {
  margin-bottom: 16px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tags-container.multi-select {
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
  font-size: 14px;
  color: #2c3e50;
}

.tag-item:hover:not(.active) {
  background: #ecf5ff;
  border-color: #409eff;
  color: #2c3e50;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.tag-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.tag-item.chip {
  padding: 6px 12px;
  font-size: 13px;
}

.tag-item.chip.location {
  background: #f0f9ff;
  border-color: #00bcd4;
}

.tag-item.chip.location .tag-name {
  color: #2c3e50;
}

.tag-item.chip.location.active {
  background: #e0f7fa;
  border-color: #0097a7;
}

.clear-filters {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.clear-btn {
  padding: 8px 20px;
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.clear-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

/* 自定义标签按钮 */
.custom-tag-action {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
  text-align: right;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .tag-selector-section {
    padding: 15px;
  }

  .tag-item {
    padding: 6px 12px;
    font-size: 13px;
  }

  .tag-item.chip {
    padding: 5px 10px;
    font-size: 12px;
  }
}
</style>
