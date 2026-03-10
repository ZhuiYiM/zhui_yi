<template>
  <div class="unified-search-component">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-row">
        <div class="search-box-wrapper">
          <div class="search-box">
            <input
             v-model="localSearchQuery"
              @keyup.enter="handleSearch"
              @focus="showSearchTagSelector = true"
              @blur="hideSearchTagSelector"
              :placeholder="placeholder"
             class="search-input"
             ref="searchInputRef"
            >
            <button @click="handleSearch" class="search-btn">🔍</button>
          </div>

          <!-- 搜索标签选择器（点击搜索框时显示） -->
          <div v-if="showSearchTagSelector" class="search-tag-selector">
            <div class="tag-selector-header">
              <span>{{ tagSelectorTitle }}</span>
            </div>
            
            <div class="search-tags-grid">
              <div
               v-for="tag in availableTags"
                :key="tag.code || tag.id"
                @click="toggleTagSelection(tag)"
               class="search-tag-item"
                :class="{ active: selectedTags.includes(tag.code || tag.id) }"
                :style="selectedTags.includes(tag.code || tag.id) ? { 
                  backgroundColor: tag.color + '40', 
                  borderColor: tag.color 
                } : {}"
              >
                {{ tag.name || tag.label }}
              </div>
            </div>
            
            <div class="search-tag-actions">
              <button @click="clearSelectedTags" class="clear-tags-btn">清空</button>
              <button @click="applySearchTags" class="apply-tags-btn">应用筛选</button>
            </div>
          </div>
        </div>

        <!-- 快速筛选标签 -->
        <div class="quick-filters" v-if="showQuickFilters && quickFilterOptions.length > 0">
          <span class="filter-label">快速筛选:</span>
          <span
           v-for="filter in quickFilterOptions"
            :key="filter.code || filter.id"
            @click="toggleQuickFilter(filter)"
           class="quick-filter-chip"
            :class="{ active: activeQuickFilters.includes(filter.code || filter.id) }"
          >
            {{ filter.name || filter.label }}
          </span>
        </div>

        <!-- 清除筛选按钮 -->
        <button
         v-if="showClearButton && hasActiveFilters"
          @click="clearAllFilters"
         class="clear-all-btn"
        >
          ✕ 清除所有筛选
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

// Props
const props = defineProps({
  // 搜索词
  modelValue: {
    type: String,
    default: ''
  },
  // 占位符
  placeholder: {
    type: String,
    default: '搜索...'
  },
  // 标签选择器标题
  tagSelectorTitle: {
    type: String,
    default: '选择标签进行搜索'
  },
  // 可用标签列表
  availableTags: {
    type: Array,
    default: () => []
  },
  // 已选中的标签
  selectedTagsModel: {
    type: Array,
    default: () => []
  },
  // 快速筛选选项
  quickFilterOptions: {
    type: Array,
    default: () => []
  },
  // 激活的快速筛选
  activeQuickFilters: {
    type: Array,
    default: () => []
  },
  // 是否显示快速筛选
  showQuickFilters: {
    type: Boolean,
    default: true
  },
  // 是否显示清除按钮
  showClearButton: {
    type: Boolean,
    default: true
  },
  // 是否有激活的筛选
  hasActiveFilters: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits([
  'update:modelValue',
  'update:selectedTagsModel',
  'update:activeQuickFilters',
  'search',
  'applyTags',
  'clearTags',
  'clearFilters'
]);

// 本地状态
const localSearchQuery = ref(props.modelValue);
const selectedTags = ref([...props.selectedTagsModel]);
const activeQuickFiltersLocal = ref([...props.activeQuickFilters]);
const showSearchTagSelector = ref(false);
const searchInputRef = ref(null);

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  localSearchQuery.value = newVal;
});

watch(() => props.selectedTagsModel, (newVal) => {
  selectedTags.value = [...newVal];
}, { deep: true });

watch(() => props.activeQuickFilters, (newVal) => {
  activeQuickFiltersLocal.value = [...newVal];
}, { deep: true });

// 搜索处理
const handleSearch = () => {
  emit('update:modelValue', localSearchQuery.value);
  emit('search', localSearchQuery.value);
  showSearchTagSelector.value = false;
};

// 标签选择器相关
const hideSearchTagSelector = () => {
  setTimeout(() => {
    showSearchTagSelector.value = false;
  }, 200);
};

const toggleTagSelection = (tag) => {
  const code = tag.code || tag.id;
  const index = selectedTags.value.indexOf(code);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    selectedTags.value.push(code);
  }
  emit('update:selectedTagsModel', selectedTags.value);
};

const clearSelectedTags = () => {
  selectedTags.value = [];
  emit('update:selectedTagsModel', []);
  emit('clearTags');
};

const applySearchTags = () => {
  emit('applyTags', [...selectedTags.value]);
  showSearchTagSelector.value = false;
};

// 快速筛选相关
const toggleQuickFilter = (filter) => {
  const code = filter.code || filter.id;
  const index = activeQuickFiltersLocal.value.indexOf(code);
  if (index > -1) {
    activeQuickFiltersLocal.value.splice(index, 1);
  } else {
    activeQuickFiltersLocal.value.push(code);
  }
  emit('update:activeQuickFilters', activeQuickFiltersLocal.value);
};

// 清除所有筛选
const clearAllFilters = () => {
  localSearchQuery.value = '';
  selectedTags.value = [];
  activeQuickFiltersLocal.value = [];
  
  emit('update:modelValue', '');
  emit('update:selectedTagsModel', []);
  emit('update:activeQuickFilters', []);
  emit('clearFilters');
};
</script>

<style scoped lang="scss">
.unified-search-component {
  width: 100%;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 20px;
}

.search-row {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 搜索框容器 */
.search-box-wrapper {
  position: relative;
  width: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 8px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 2px solid transparent;

  &:focus-within {
    box-shadow: 0 4px 15px rgba(76, 110, 245, 0.15);
    border-color: #4c6ef5;
  }
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 15px;
  font-size: 15px;
  background: transparent;
  color: #333;

  &::placeholder {
   color: #999;
  }
}

.search-btn {
  background: #4c6ef5;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s;

  &:hover {
    background: #3b5bdb;
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}

/* 搜索标签选择器 */
.search-tag-selector {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideDown 0.3s ease-out;

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.tag-selector-header {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.search-tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.search-tag-item {
  padding: 10px 15px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &:hover {
    background: #e9ecef;
    transform: translateY(-2px);
  }

  &.active {
   color: white;
    font-weight: 600;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  }
}

.search-tag-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.clear-tags-btn,
.apply-tags-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.clear-tags-btn {
  background: #f8f9fa;
  color: #666;

  &:hover {
    background: #e9ecef;
  }
}

.apply-tags-btn {
  background: linear-gradient(135deg, #4c6ef5 0%, #3b5bdb 100%);
  color: white;

  &:hover {
    box-shadow: 0 4px 15px rgba(76, 110, 245, 0.4);
    transform: translateY(-2px);
  }
}

/* 快速筛选 */
.quick-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.quick-filter-chip {
  padding: 6px 14px;
  background: #f8f9fa;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;

  &:hover {
    background: #e9ecef;
    transform: translateY(-2px);
  }

  &.active {
    background: #4c6ef5;
   color: white;
    box-shadow: 0 2px 8px rgba(76, 110, 245, 0.3);
  }
}

/* 清除所有筛选 */
.clear-all-btn {
  padding: 8px 16px;
  background: #ffe3e3;
  color: #f56c6c;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;

  &:hover {
    background: #ffd0d0;
    transform: translateY(-2px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-row {
    gap: 10px;
  }

  .search-tags-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .quick-filters {
    .filter-label {
      width: 100%;
    }
  }
}
</style>
