<template>
  <div class="system-settings">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Setting /></el-icon>
          系统设置
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">系统管理</span>
            <span class="separator">/</span>
            <span class="current-page">系统设置</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="placeholder-card">
      <el-empty description="系统设置功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
import { Setting } from '@element-plus/icons-vue';
</script>

<style scoped>
.system-settings {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.placeholder-card {
  border-radius: 12px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
