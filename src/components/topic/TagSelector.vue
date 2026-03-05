<template>
  <div class="tag-selector-container">
    <!-- 一级标签选择器 - 用户身份 -->
    <div class="tag-section" v-if="showLevel1">
      <label class="tag-label">
        <span class="required">*</span> 身份标签
        <span class="hint" v-if="readOnlyLevel1">（根据用户身份自动确定）</span>
      </label>
      
      <!-- 只读模式：只显示已选中的标签或加载中提示 -->
      <template v-if="readOnlyLevel1">
        <div v-if="selectedTags.level1" class="selected-level1-tag">
          <span class="tag-icon">{{ selectedTags.level1.icon }}</span>
          <span class="tag-name">{{ selectedTags.level1.name }}</span>
        </div>
        <div v-else class="loading-level1">
          <span class="loading-spinner"></span>
          <span>正在加载身份标签...</span>
        </div>
      </template>
      
      <!-- 可编辑模式：显示所有选项 -->
      <div v-else class="tag-options">
        <div
          v-for="tag in level1Tags"
          :key="tag.code"
          @click="selectLevel1(tag)"
          class="tag-option"
          :class="{ 
            'selected': selectedTags.level1?.code === tag.code,
            'disabled': !tag.enabled 
          }"
        >
          <span class="tag-icon">{{ tag.icon }}</span>
          <span class="tag-name">{{ tag.name }}</span>
        </div>
      </div>
    </div>

    <!-- 二级标签选择器 - 话题分类 -->
    <div class="tag-section" v-if="showLevel2">
      <label class="tag-label">
        <span class="required">*</span> 话题分类
        <span class="hint">(最多选 3 个)</span>
      </label>
      <div class="tag-options multi-select">
        <div
          v-for="tag in level2Tags"
          :key="tag.code"
          @click="toggleLevel2(tag)"
          class="tag-option chip"
          :class="{ 
            'selected': isSelectedLevel2(tag),
            'disabled': !tag.enabled || (!hasLevel2Selection() && isMaxLevel2())
          }"
          :style="isSelectedLevel2(tag) ? { backgroundColor: tag.color + '20', borderColor: tag.color } : {}"
        >
          <span class="tag-name">{{ tag.name }}</span>
          <span v-if="isSelectedLevel2(tag)" class="check-mark">✓</span>
        </div>
      </div>
    </div>

    <!-- 三级标签选择器 - 地点实体 -->
    <div class="tag-section" v-if="showLevel3">
      <label class="tag-label">
        <span class="optional">○</span> 地点标签
        <span class="hint">(最多选 2 个)</span>
      </label>
      <div class="tag-options multi-select">
        <div
          v-for="tag in level3Tags"
          :key="tag.code"
          @click="toggleLevel3(tag)"
          class="tag-option chip location"
          :class="{ 
            'selected': isSelectedLevel3(tag),
            'disabled': !tag.enabled || (!hasLevel3Selection() && isMaxLevel3())
          }"
        >
          <span class="tag-name">{{ tag.name }}</span>
          <span v-if="isSelectedLevel3(tag)" class="check-mark">✓</span>
        </div>
      </div>
    </div>

    <!-- 四级标签选择器 - 自定义标签 -->
    <div class="tag-section" v-if="showLevel4">
      <label class="tag-label">
        <span class="optional">○</span> 自定义标签
        <span class="hint">(最多选 5 个，可从标签池选择或创建新标签)</span>
      </label>
      
      <!-- 推荐标签池 -->
      <div class="tag-pool" v-if="recommendedTags.length > 0">
        <div class="pool-title">推荐标签</div>
        <div class="tag-options multi-select">
          <div
            v-for="tag in recommendedTags"
            :key="tag.code"
            @click="toggleLevel4(tag)"
            class="tag-option chip custom"
            :class="{ 
              'selected': isSelectedLevel4(tag),
              'disabled': !tag.enabled || (!hasLevel4Selection() && isMaxLevel4())
            }"
          >
            <span class="tag-name">{{ tag.name }}</span>
            <span v-if="isSelectedLevel4(tag)" class="check-mark">✓</span>
          </div>
        </div>
      </div>

      <!-- 搜索和创建标签 -->
      <div class="tag-search-create">
        <div class="search-box">
          <input
            v-model="tagSearchQuery"
            type="text"
            placeholder="搜索标签池..."
            @keyup.enter="searchTags"
            class="search-input"
          >
          <button @click="searchTags" class="search-btn">🔍</button>
        </div>

        <div class="create-new-tag" v-if="showCreateNewTag">
          <input
            v-model="newTagName"
            type="text"
            placeholder="输入新标签名称"
            class="create-input"
            maxlength="20"
          >
          <select v-model="newTagCategory" class="category-select">
            <option value="">选择分类</option>
            <option value="tech">技术</option>
            <option value="study">学习</option>
            <option value="life">生活</option>
            <option value="hobby">爱好</option>
          </select>
          <button @click="createNewTag" class="create-btn" :disabled="!canCreateNewTag">
            创建
          </button>
        </div>
      </div>

      <!-- 已选择的标签 -->
      <div class="selected-tags" v-if="selectedTags.level4 && selectedTags.level4.length > 0">
        <div
          v-for="(tag, index) in selectedTags.level4"
          :key="index"
          class="selected-tag"
        >
          <span>{{ tag.name }}</span>
          <button @click="removeLevel4(index)" class="remove-btn">×</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { tagAPI } from '@/api/topic';

// Props
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  showLevel1: { type: Boolean, default: true },
  showLevel2: { type: Boolean, default: true },
  showLevel3: { type: Boolean, default: true },
  showLevel4: { type: Boolean, default: true },
  autoSelectLevel1: { type: Boolean, default: false }, // 是否自动选择一级标签
  readOnlyLevel1: { type: Boolean, default: false }, // 一级标签是否只读
  userId: { type: Number, default: null }
});

// Emit
const emit = defineEmits(['update:modelValue', 'change']);

// 数据状态
const selectedTags = ref({
  level1: null,
  level2: [],
  level3: [],
  level4: []
});

const level1Tags = ref([]);
const level2Tags = ref([]);
const level3Tags = ref([]);
const recommendedTags = ref([]);
const allLevel4Tags = ref([]);

const tagSearchQuery = ref('');
const showCreateNewTag = ref(false);
const newTagName = ref('');
const newTagCategory = ref('');

// 常量
const MAX_LEVEL2 = 3;
const MAX_LEVEL3 = 2;
const MAX_LEVEL4 = 5;

// 计算属性
const canCreateNewTag = computed(() => {
  return newTagName.value.trim().length > 0 && newTagCategory.value.trim();
});

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    selectedTags.value = { ...selectedTags.value, ...newVal };
    // 标签加载完成后，需要将 code 转换为完整的标签对象
    syncSelectedTagsWithLoadedTags();
  }
}, { deep: true });

// 生命周期
onMounted(async () => {
  await loadAllTags();
  
  // 如果需要自动选择一级标签
  if (props.autoSelectLevel1 && props.userId) {
    await autoSelectIdentity();
  }
  
  // 标签加载完成后，同步预设的选中状态
  syncSelectedTagsWithLoadedTags();
});

// 加载所有标签
const loadAllTags = async () => {
  try {
    const promises = [];
    
    if (props.showLevel1) {
      promises.push(loadLevel1Tags());
    }
    if (props.showLevel2) {
      promises.push(loadLevel2Tags());
    }
    if (props.showLevel3) {
      promises.push(loadLevel3Tags());
    }
    if (props.showLevel4) {
      promises.push(loadLevel4Tags());
    }
    
    await Promise.all(promises);
  } catch (error) {
    console.error('加载标签失败:', error);
    ElMessage.error('加载标签失败，请刷新重试');
  }
};

const loadLevel1Tags = async () => {
  const response = await tagAPI.getLevel1Tags();
  console.log('📥 TagSelector - Level1 原始响应:', response);
  
  level1Tags.value = (Array.isArray(response) ? response : []).map(tag => ({
    ...tag,
    enabled: tag.isActive !== undefined ? tag.isActive : true,
    icon: getIconEmoji(tag.code) || tag.icon || '👤'
  }));
  
  console.log('✅ TagSelector - Level1 标签已加载:', level1Tags.value);
};

// 获取标签对应的 emoji 图标
const getIconEmoji = (code) => {
  const iconMap = {
    'student': '👨‍🎓',
    'merchant': '🏪',
    'organization': '👥',
    'individual': '👤',
    'followed': '⭐'
  };
  return iconMap[code] || null;
};

const loadLevel2Tags = async () => {
  const response = await tagAPI.getLevel2Tags();
  console.log('📥 TagSelector - Level2 原始响应:', response);
  
  level2Tags.value = (Array.isArray(response) ? response : []).map(tag => ({
    ...tag,
    enabled: tag.isActive !== undefined ? tag.isActive : true
  }));
  
  console.log('✅ TagSelector - Level2 标签已加载:', level2Tags.value);
};

const loadLevel3Tags = async () => {
  const response = await tagAPI.getLevel3Tags();
  console.log('📥 TagSelector - Level3 原始响应:', response);
  
  level3Tags.value = (Array.isArray(response) ? response : []).map(tag => ({
    ...tag,
    enabled: tag.isActive !== undefined ? tag.isActive : true
  }));
  
  console.log('✅ TagSelector - Level3 标签已加载:', level3Tags.value);
};

const loadLevel4Tags = async () => {
  const response = await tagAPI.getLevel4Tags({ recommendedOnly: true });
  if (response.data) {
    const data = response.data.tags || response.data;
    recommendedTags.value = data.filter(tag => tag.isRecommended);
    allLevel4Tags.value = data;
    // 标签加载完成后，同步预设的选中状态
    syncSelectedTagsWithLoadedTags();
  }
};

// 同步选中的标签（将 code 转换为完整标签对象）
const syncSelectedTagsWithLoadedTags = () => {
  // 同步一级标签
  if (selectedTags.value.level1 && typeof selectedTags.value.level1 === 'string') {
    const tag = level1Tags.value.find(t => t.code === selectedTags.value.level1);
    if (tag) {
      selectedTags.value.level1 = tag;
    }
  }
  
  // 同步二级标签
  if (selectedTags.value.level2 && Array.isArray(selectedTags.value.level2)) {
    selectedTags.value.level2 = selectedTags.value.level2.map(code => {
      if (typeof code === 'string') {
        return level2Tags.value.find(t => t.code === code) || { code };
      }
      return code;
    }).filter(Boolean);
  }
  
  // 同步三级标签
  if (selectedTags.value.level3 && Array.isArray(selectedTags.value.level3)) {
    selectedTags.value.level3 = selectedTags.value.level3.map(code => {
      if (typeof code === 'string') {
        return level3Tags.value.find(t => t.code === code) || { code };
      }
      return code;
    }).filter(Boolean);
  }
  
  // 同步四级标签
  if (selectedTags.value.level4 && Array.isArray(selectedTags.value.level4)) {
    selectedTags.value.level4 = selectedTags.value.level4.map(code => {
      if (typeof code === 'string') {
        return allLevel4Tags.value.find(t => t.code === code) || { code };
      }
      return code;
    }).filter(Boolean);
  }
};

// 自动选择身份标签
const autoSelectIdentity = async () => {
  console.log('🔍 开始自动选择身份标签，userId:', props.userId);
  
  // TODO: 调用后端接口获取用户身份
  // const response = await userAPI.getIdentity(props.userId);
  // if (response.data) {
  //   const identityTag = level1Tags.value.find(t => t.code === response.data.identity);
  //   if (identityTag) {
  //     selectLevel1(identityTag);
  //   }
  // }
  
  // 临时方案：根据用户信息自动选择身份
  // 优先级：管理员 > 学生 > 商户 > 团体 > 个人 > 社会
  if (level1Tags.value.length > 0) {
    let selectedTag = null;
    
    // 从 localStorage 获取用户信息
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    console.log('👤 当前用户信息:', user);
    
    // 1. 检查是否为管理员（需要在数据库中手动设置）
    if (user.role === 'admin' || user.isAdmin) {
      selectedTag = level1Tags.value.find(t => t.code === 'admin');
      console.log('✅ 识别为：管理员');
    }
    
    // 2. 检查是否为学生认证（学号不为空）
    if (!selectedTag && user.studentId) {
      selectedTag = level1Tags.value.find(t => t.code === 'student');
      console.log('✅ 识别为：学生（学号：' + user.studentId + '）');
    }
    
    // 3. 检查是否为认证商家（待实现）
    if (!selectedTag && user.isMerchant) {
      selectedTag = level1Tags.value.find(t => t.code === 'merchant');
      console.log('✅ 识别为：商户');
    }
    
    // 4. 检查是否为认证团体（待实现）
    if (!selectedTag && user.isOrganization) {
      selectedTag = level1Tags.value.find(t => t.code === 'organization');
      console.log('✅ 识别为：团体');
    }
    
    // 5. 默认为社会用户
    if (!selectedTag) {
      // 优先显示"社会"标签，如果没有则选第一个
      selectedTag = level1Tags.value.find(t => t.code === 'society') || level1Tags.value[0];
      console.log('✅ 默认为：社会用户');
    }
    
    if (selectedTag && selectedTag.enabled) {
      selectLevel1(selectedTag);
      console.log('✅ 已自动选择身份标签:', selectedTag.name);
    } else {
      console.warn('⚠️ 未找到可用的默认身份标签');
    }
  } else {
    console.warn('⚠️ level1Tags 还未加载');
  }
};

// 选择一级标签
const selectLevel1 = (tag) => {
  if (!tag.enabled) return;
  
  selectedTags.value.level1 = tag;
  emitChange();
};

// 切换二级标签
const toggleLevel2 = (tag) => {
  if (!tag.enabled) return;
  
  const index = selectedTags.value.level2.findIndex(t => t.code === tag.code);
  
  if (index > -1) {
    // 取消选择
    selectedTags.value.level2.splice(index, 1);
  } else {
    // 检查是否超过最大数量
    if (selectedTags.value.level2.length >= MAX_LEVEL2) {
      ElMessage.warning(`最多只能选择${MAX_LEVEL2}个话题分类`);
      return;
    }
    selectedTags.value.level2.push(tag);
  }
  
  emitChange();
};

const isSelectedLevel2 = (tag) => {
  return selectedTags.value.level2.some(t => t.code === tag.code);
};

const hasLevel2Selection = () => {
  return selectedTags.value.level2.length > 0;
};

const isMaxLevel2 = () => {
  return selectedTags.value.level2.length >= MAX_LEVEL2;
};

// 切换三级标签
const toggleLevel3 = (tag) => {
  if (!tag.enabled) return;
  
  const index = selectedTags.value.level3.findIndex(t => t.code === tag.code);
  
  if (index > -1) {
    selectedTags.value.level3.splice(index, 1);
  } else {
    if (selectedTags.value.level3.length >= MAX_LEVEL3) {
      ElMessage.warning(`最多只能选择${MAX_LEVEL3}个地点标签`);
      return;
    }
    selectedTags.value.level3.push(tag);
  }
  
  emitChange();
};

const isSelectedLevel3 = (tag) => {
  return selectedTags.value.level3.some(t => t.code === tag.code);
};

const hasLevel3Selection = () => {
  return selectedTags.value.level3.length > 0;
};

const isMaxLevel3 = () => {
  return selectedTags.value.level3.length >= MAX_LEVEL3;
};

// 切换四级标签
const toggleLevel4 = (tag) => {
  if (!tag.enabled) return;
  
  const index = selectedTags.value.level4.findIndex(t => t.code === tag.code);
  
  if (index > -1) {
    selectedTags.value.level4.splice(index, 1);
  } else {
    if (selectedTags.value.level4.length >= MAX_LEVEL4) {
      ElMessage.warning(`最多只能选择${MAX_LEVEL4}个自定义标签`);
      return;
    }
    selectedTags.value.level4.push(tag);
  }
  
  emitChange();
};

const isSelectedLevel4 = (tag) => {
  return selectedTags.value.level4.some(t => t.code === tag.code);
};

const hasLevel4Selection = () => {
  return selectedTags.value.level4.length > 0;
};

const isMaxLevel4 = () => {
  return selectedTags.value.level4.length >= MAX_LEVEL4;
};

const removeLevel4 = (index) => {
  selectedTags.value.level4.splice(index, 1);
  emitChange();
};

// 搜索标签
const searchTags = async () => {
  if (!tagSearchQuery.value.trim()) {
    recommendedTags.value = allLevel4Tags.value.filter(t => t.isRecommended);
    return;
  }
  
  try {
    const response = await tagAPI.getLevel4Tags({ 
      keyword: tagSearchQuery.value 
    });
    
    if (response.data) {
      const data = response.data.tags || response.data;
      recommendedTags.value = data;
    }
  } catch (error) {
    console.error('搜索标签失败:', error);
  }
};

// 创建新标签
const createNewTag = async () => {
  if (!canCreateNewTag.value) return;
  
  try {
    const response = await tagAPI.createLevel4Tag({
      name: newTagName.value.trim(),
      category: newTagCategory.value
    });
    
    if (response.data) {
      ElMessage.success('标签创建成功');
      
      // 添加到列表并选中
      const newTag = response.data;
      allLevel4Tags.value.push(newTag);
      recommendedTags.value.push(newTag);
      toggleLevel4(newTag);
      
      // 重置输入
      newTagName.value = '';
      newTagCategory.value = '';
      showCreateNewTag.value = false;
    }
  } catch (error) {
    console.error('创建标签失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error('创建标签失败，请重试');
    }
  }
};

// 触发 change 事件
const emitChange = () => {
  // 转换为后端需要的格式
  const tagsData = {
    level1: selectedTags.value.level1?.code || null,
    level2: selectedTags.value.level2.map(t => t.code),
    level3: selectedTags.value.level3.map(t => t.code),
    level4: selectedTags.value.level4.map(t => t.code)
  };
  
  emit('update:modelValue', selectedTags.value);
  emit('change', tagsData);
};

// 验证是否至少选择了必选标签
const validate = () => {
  if (props.showLevel1 && !selectedTags.value.level1) {
    ElMessage.warning('请选择身份标签');
    return false;
  }
  
  if (props.showLevel2 && selectedTags.value.level2.length === 0) {
    ElMessage.warning('请至少选择一个话题分类');
    return false;
  }
  
  return true;
};

// 暴露方法给父组件
defineExpose({
  validate,
  getSelectedTags: () => selectedTags.value
});
</script>

<style scoped>
.tag-selector-container {
  padding: 20px 0;
}

.tag-section {
  margin-bottom: 24px;
}

.tag-section .tag-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.tag-section .tag-label .required {
  color: #ff4d4f;
  margin-right: 4px;
}

.tag-section .tag-label .optional {
  color: #999;
  margin-right: 4px;
}

.tag-section .tag-label .hint {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}

/* 只读模式的一级标签样式 */
.tag-section .selected-level1-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tag-section .selected-level1-tag .tag-icon {
  margin-right: 6px;
  font-size: 16px;
}

.tag-section .selected-level1-tag .tag-name {
  font-size: 14px;
  color: white;
}

/* 只读模式加载状态 */
.tag-section .loading-level1 {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #909399;
  font-size: 14px;
}

.tag-section .loading-level1 .loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #e4e7ed;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-options.multi-select {
  gap: 8px;
}

.tag-options .tag-option {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
  position: relative;
}

.tag-options .tag-option:hover:not(.disabled) {
  background: #ecf5ff;
  border-color: #409eff;
  color: #2c3e50;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.tag-options .tag-option.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.tag-options .tag-option.selected .check-mark {
  display: inline-block;
  margin-left: 4px;
  font-weight: bold;
}

.tag-options .tag-option.selected .tag-name {
  color: white;
}

.tag-options .tag-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f0f0f0;
}

.tag-options .tag-option .tag-icon {
  margin-right: 6px;
  font-size: 16px;
}

.tag-options .tag-option .tag-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.tag-options .tag-option.chip {
  padding: 6px 12px;
  font-size: 13px;
}

.tag-options .tag-option.chip .check-mark {
  display: none;
}

.tag-options .tag-option.chip.location {
  background: #f0f9ff;
  border-color: #00bcd4;
}

.tag-options .tag-option.chip.location.selected {
  background: #e0f7fa;
}

.tag-options .tag-option.chip.custom {
  background: #fff7e6;
  border-color: #ffa726;
}

.tag-options .tag-option.chip.custom.selected {
  background: #ffe0b2;
}

.tag-pool {
  margin-bottom: 16px;
}

.tag-pool .pool-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 600;
}

.tag-search-create {
  margin-top: 12px;
}

.tag-search-create .search-box {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-search-create .search-box .search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.tag-search-create .search-box .search-input:focus {
  border-color: #409eff;
  outline: none;
}

.tag-search-create .search-box .search-btn {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.tag-search-create .search-box .search-btn:hover {
  background: #66b1ff;
}
  
.tag-search-create .create-new-tag {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.tag-search-create .create-new-tag .create-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.tag-search-create .create-new-tag .category-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  background: white;
}

.tag-search-create .create-new-tag .create-btn {
  padding: 8px 20px;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.tag-search-create .create-new-tag .create-btn:hover:not(:disabled) {
  background: #85ce61;
}

.tag-search-create .create-new-tag .create-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.selected-tags .selected-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 16px;
  font-size: 13px;
  color: #409eff;
}

.selected-tags .selected-tag .remove-btn {
  margin-left: 6px;
  width: 18px;
  height: 18px;
  border: none;
  background: transparent;
  color: #409eff;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
}

.selected-tags .selected-tag .remove-btn:hover {
  background: #409eff;
  color: white;
}
</style>
