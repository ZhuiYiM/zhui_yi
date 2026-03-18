<template>
  <div class="message-tabs">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.id"
        :label="`${tab.name} ${tab.count > 0 ? `(${tab.count})` : ''}`"
        :name="tab.id"
      />
    </el-tabs>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: String,
    default: 'all'
  },
  unreadCount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['update:modelValue', 'tab-change']);

const tabs = computed(() => [
  { id: 'all', name: '全部', count: props.unreadCount },
  { id: 'system', name: '系统通知', count: 0 },
  { id: 'personal', name: '私信', count: 0 },
  { id: 'group', name: '群聊', count: 0 }
]);

const activeTab = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const handleTabClick = (tab) => {
  emit('tab-change', tab.props.name);
};
</script>

<style scoped>
.message-tabs {
  background: white;
  padding: 0 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-tabs__item.is-active) {
  font-weight: 600;
  color: #4A90E2;
}

:deep(.el-tabs__active-bar) {
  background-color: #4A90E2;
  height: 3px;
}
</style>
