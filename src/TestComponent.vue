<template>
  <div class="test-component">
    <h1>测试组件</h1>
    <p>如果能看到这个页面，说明Vue配置正常</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const message = ref('Hello World')
</script>

<style scoped>
.test-component {
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}
</style>