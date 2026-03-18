<template>
  <div class="message-search">
    <div class="search-bar">
      <input
          type="text"
          v-model="searchQuery"
          :placeholder="placeholder"
          @keyup.enter="$emit('search', searchQuery)"
      >
      <button @click="$emit('search', searchQuery)">搜索</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索联系人、群聊、消息...'
  }
});

const emit = defineEmits(['search']);

const searchQuery = ref('');
</script>

<style scoped>
.message-search {
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
}

.search-bar input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.search-bar input:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.1);
}

.search-bar button {
  padding: 10px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.search-bar button:hover {
  background-color: #357ABD;
}
</style>
