<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
// import { useAuthStore } from '@/stores/auth'

// const authStore = useAuthStore()

// 应用初始化时加载用户信息
onMounted(async () => {
  console.log('🚀 应用启动，初始化用户信息');
  // await authStore.initUserInfo();
})
</script>
