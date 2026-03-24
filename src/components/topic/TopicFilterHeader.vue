<template>
  <div class="topic-filter-header">
    <!-- 一级标签和排序 -->
    <div class="section-header">
      <div class="level1-tags-container">
        <span
          @click="selectLevel1(null)"
          class="level1-tag-item"
          :class="{ active: !selectedLevel1 }"
        >
          全部
        </span>
        <span
          v-for="tag in displayLevel1Tags"
          :key="tag.code"
          @click="selectLevel1(tag.code)"
          class="level1-tag-item"
          :class="{ active: selectedLevel1 === tag.code }"
        >
          {{ tag.icon }} {{ tag.name }}
        </span>
      </div>
      
      <div class="sort-options">
        <button
          v-for="option in sortOptions"
          :key="option.value"
          @click="changeSort(option.value)"
          class="sort-btn"
          :class="{ active: currentSort === option.value }"
        >
          {{ option.label }}
        </button>
        
        <!-- 设置按钮（TODO: 后续实现默认设置管理功能） -->
        <button class="settings-btn" title="默认设置（待实现）">
          ⚙️
        </button>
      </div>
    </div>
    
    <!-- 标签筛选栏 -->
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
      
      <!-- 清除筛选和自定义标签 -->
      <div class="filter-actions">
        <div class="clear-filters" v-if="hasActiveFilters">
          <button @click="handleClearAll" class="clear-btn">
            ✕ 清除所有筛选
          </button>
        </div>
        
        <!-- 自定义标签按钮 -->
        <div class="custom-tag-action">
          <button @click="handleShowCustomTagModal" class="custom-tag-btn">
            🏷️ 提交自定义标签
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';

// Props
const props = defineProps({
  displayLevel1Tags: {
    type: Array,
    required: true
  },
  displayLevel2Tags: {
    type: Array,
    required: true
  },
  displayLevel3Tags: {
    type: Array,
    required: true
  },
  selectedLevel1: {
    type: String,
    default: null
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
  },
  currentSort: {
    type: String,
    default: 'latest'
  },
  sortOptions: {
    type: Array,
    default: () => [
      { label: '最新', value: 'latest' },
      { label: '最热', value: 'hot' },
      { label: '精华', value: 'essence' }
    ]
  }
});

// Emits
const emit = defineEmits([
  'toggle-level1',
  'toggle-level2',
  'toggle-level3',
  'clear-all',
  'change-sort',
  'show-custom-tag-modal'
]);

// 判断二级标签是否被选中
const isSelectedLevel2 = (tag) => {
  return props.selectedLevel2.includes(tag.code);
};

// 处理一级标签选择
const selectLevel1 = (code) => {
  emit('toggle-level1', code);
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

// 处理排序切换
const changeSort = (sortType) => {
  emit('change-sort', sortType);
};

// 处理自定义标签按钮点击
const handleShowCustomTagModal = () => {
  emit('show-custom-tag-modal');
};
</script>

<style scoped>
.topic-filter-header {
  margin-bottom: 25px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 一级标签和排序样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 0 20px;
  background: #fff;
  border-radius: 8px 8px 0 0;
  box-shadow: none;
  margin-bottom: 0;
}

.level1-tags-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.level1-tag-item {
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  user-select: none;
}

.level1-tag-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.level1-tag-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.sort-options {
  display: flex;
  gap: 8px;
  align-items: center;
}

.sort-btn {
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  transition: all 0.3s;
}

.sort-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.sort-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 设置按钮 */
.settings-btn {
  padding: 8px 12px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.settings-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: rotate(30deg);
}

/* 标签筛选栏样式 */
.tag-selector-section {
  background: #fff;
  padding: 16px 20px 20px 20px;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-top: 0;
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

/* 筛选操作区域 */
.filter-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.clear-filters {
  display: flex;
  gap: 8px;
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
  flex-shrink: 0;
}

.custom-tag-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.custom-tag-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.custom-tag-btn:active {
  transform: translateY(0);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .topic-filter-header {
    padding: 15px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    padding: 12px 15px;
  }
  
  .level1-tags-container {
    width: 100%;
  }
  
  .sort-options {
    width: 100%;
    justify-content: space-between;
  }
  
  .sort-btn {
    flex: 1;
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .settings-btn {
    padding: 6px 10px;
    font-size: 14px;
  }
  
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
  
  .filter-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .clear-filters {
    width: 100%;
  }
  
  .clear-btn {
    width: 100%;
    justify-content: center;
  }
  
  .custom-tag-action {
    width: 100%;
  }
  
  .custom-tag-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
