<template>
  <div class="message-type-tabs">
    <div
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-item', { active: activeTab === tab.id }]"
        @click="$emit('tab-change', tab.id)"
    >
      <span class="tab-icon">{{ tab.icon }}</span>
      <span class="tab-name">{{ tab.name }}</span>
      <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tabs: {
    type: Array,
    required: true
  },
  activeTab: {
    type: String,
    required: true
  }
});

defineEmits(['tab-change']);
</script>

<style scoped>
.message-type-tabs {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
  margin-bottom: 20px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: #f5f7fa;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-item:hover {
  background-color: #e3f2fd;
}

.tab-item.active {
  background-color: #4A90E2;
  color: white;
}

.tab-count {
  background-color: #ff6b6b;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 0.7rem;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
