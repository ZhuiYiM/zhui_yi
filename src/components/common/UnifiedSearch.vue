<template>
  <div class="unified-search-component">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-row">
        <div class="search-box-wrapper">
          <div class="search-box">
            <!-- 搜索类型选择器（移到搜索框内部左侧） -->
            <el-popover
              placement="bottom-start"
              :width="300"
              trigger="click"
              popper-class="search-type-popover"
            >
              <template #reference>
                <div class="type-selector-btn">
                  <span class="type-icon">📋</span>
                  <span class="type-text">{{ selectedSearchType.name }}</span>
                  <span class="type-arrow">▼</span>
                </div>
              </template>
              
              <template #default>
                <div class="search-type-options">
                  <div
                    v-for="type in searchTypeOptions"
                    :key="type.code"
                    @click="selectSearchType(type)"
                    :class="['type-option', { active: selectedSearchType.code === type.code }]"
                  >
                    <span class="option-icon">{{ type.icon }}</span>
                    <span class="option-name">{{ type.name }}</span>
                    <span v-if="selectedSearchType.code === type.code" class="option-check">✓</span>
                  </div>
                </div>
              </template>
            </el-popover>

            <!-- 搜索输入框 -->
            <input
             v-model="localSearchQuery"
              @keyup.enter="handleSearch"
              @focus="showSearchTagSelector = true"
              @blur="hideSearchTagSelector"
              :placeholder="placeholder"
             class="search-input"
             ref="searchInputRef"
            >
            
            <!-- 搜索按钮 -->
            <button @click="handleSearch" class="search-btn">🔍</button>
          </div>

          <!-- 搜索标签选择器（点击搜索框时显示） -->
          <div 
            v-if="showSearchTagSelector" 
            class="search-tag-selector"
            @mouseenter="cancelHideSearchTagSelector"
            @mouseleave="hideSearchTagSelector"
          >
            <div class="tag-selector-header">
              <span>{{ tagSelectorTitle }}</span>
            </div>
            
            <div class="search-tags-grid">
              <div
               v-for="tag in availableTags"
                :key="tag.code || tag.id"
                @mousedown.prevent="toggleTagSelection(tag)"
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
import { useRouter } from 'vue-router';

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
  },
  // 是否跳转到结果页
  enableResultPage: {
    type: Boolean,
    default: true
  },
  // 默认搜索类型
  defaultSearchType: {
    type: String,
    default: 'all'
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

const router = useRouter();

// 搜索类型选项
const searchTypeOptions = [
  { code: 'all', name: '全部', icon: '🔍' },
  { code: 'location', name: '地点', icon: '📍' },
  { code: 'product', name: '商品', icon: '🛒' },
  { code: 'topic', name: '话题', icon: '💬' },
  { code: 'user', name: '用户', icon: '👤' }
];

const selectedSearchType = ref(searchTypeOptions.find(opt => opt.code === props.defaultSearchType) || searchTypeOptions[0]);

// 监听默认类型变化
watch(() => props.defaultSearchType, (newType) => {
  const foundType = searchTypeOptions.find(opt => opt.code === newType);
  if (foundType) {
    selectedSearchType.value = foundType;
  }
}, { immediate: true });

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
  
  // 如果需要跳转到结果页
  if (props.enableResultPage) {
    router.push({
      path: '/search/results',
      query: {
        q: localSearchQuery.value,
        type: selectedSearchType.value.code,
        tags: selectedTags.value.join(','),
        quickFilters: activeQuickFiltersLocal.value.join(',')
      }
    });
  } else {
    emit('search', {
      query: localSearchQuery.value,
      type: selectedSearchType.value.code,
      tags: [...selectedTags.value],
      quickFilters: [...activeQuickFiltersLocal.value]
    });
  }
  
  showSearchTagSelector.value = false;
};

// 选择搜索类型
const selectSearchType = (type) => {
  selectedSearchType.value = type;
};

// 标签选择器相关
let hideTimeout = null;

const hideSearchTagSelector = () => {
  // 延迟隐藏，给用户操作时间
  hideTimeout = setTimeout(() => {
    showSearchTagSelector.value = false;
  }, 200);
};

const cancelHideSearchTagSelector = () => {
  // 取消隐藏
  if (hideTimeout) {
    clearTimeout(hideTimeout);
  }
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
  
  // 点击标签后重新聚焦输入框
  if (searchInputRef.value) {
    searchInputRef.value.focus();
  }
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

.search-row {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

/* 搜索框容器 */
.search-box-wrapper {
  position: relative;
  width: 100%;
  flex: 1;
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
  gap: 10px;

  &:focus-within {
    box-shadow: 0 4px 15px rgba(76, 110, 245, 0.15);
    border-color: #4c6ef5;
  }
}

/* 搜索类型选择器（在搜索框内部） */
.type-selector-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0f2f5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: #e4e7ed;
  }

  &:active {
    transform: scale(0.98);
  }
}

.type-icon {
  font-size: 18px;
}

.type-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
}

.type-arrow {
  font-size: 10px;
  color: #999;
  transition: transform 0.3s;
}

/* 搜索类型弹窗 */
.search-type-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f8f9fa;

  &:hover {
    background: #e9ecef;
    transform: translateX(5px);
  }

  &.active {
    background: linear-gradient(135deg, #4c6ef5 0%, #3b5bdb 100%);
    color: white;
    box-shadow: 0 4px 10px rgba(76, 110, 245, 0.3);
  }
}

.option-icon {
  font-size: 20px;
}

.option-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.option-check {
  font-size: 18px;
  font-weight: bold;
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
  min-width: 200px;

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
  color: #333;  /* 标签文字设为黑色 */
  font-weight: 500;

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

  .search-type-selector {
    width: 100%;
  }

  .type-selector-btn {
    width: 100%;
    justify-content: center;
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
