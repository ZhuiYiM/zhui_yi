<template>
  <div class="dashboard-home">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><DataAnalysis /></el-icon>
        数据统计
      </h2>
      <div class="header-actions">
        <div class="breadcrumb-nav">
          <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
            <el-icon><HomeFilled /></el-icon>
            首页
          </el-button>
          <span class="separator">/</span>
          <span class="current-page">数据统计</span>
        </div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon topic-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.topicCount }}</div>
              <div class="stat-label">话题总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon product-icon">
              <el-icon><Shop /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.productCount }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon report-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.reportCount }}</div>
              <div class="stat-label">待处理举报</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="quick-access-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Grid /></el-icon>
            快捷入口
          </span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="primary" @click="$router.push('/admin/dashboard/users')" class="quick-btn">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="success" @click="$router.push('/admin/dashboard/verifications')" class="quick-btn">
              <el-icon><DocumentChecked /></el-icon>
              <span>认证审核</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="warning" @click="$router.push('/admin/dashboard/reports')" class="quick-btn">
              <el-icon><Warning /></el-icon>
              <span>举报处理</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, ChatDotRound, Shop, Warning, Document, DataAnalysis, Grid, DocumentChecked } from '@element-plus/icons-vue';

const stats = reactive({
  userCount: 0,
  topicCount: 0,
  productCount: 0,
  reportCount: 0
});

// TODO: 从后端获取真实数据
onMounted(() => {
  // 模拟数据
  stats.userCount = 1234;
  stats.topicCount = 5678;
  stats.productCount = 901;
  stats.reportCount = 23;
});
</script>

<style scoped>
.dashboard-home {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.breadcrumb-nav .separator {
  color: #c0c4cc;
  margin: 0 4px;
}

.breadcrumb-nav .current-page {
  color: #303133;
  font-weight: 500;
}

.breadcrumb-nav .back-home-btn {
  color: #409EFF;
  padding: 0;
  font-size: 13px;
}

.breadcrumb-nav .back-home-btn:hover {
  color: #66b1ff;
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

.page-title .el-icon {
  font-size: 28px;
  color: #409EFF;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.topic-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.product-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.report-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  flex: 1;
  text-align: right;
  padding-left: 16px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.quick-access-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-title .el-icon {
  color: #409EFF;
}

.quick-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.quick-btn {
  width: 100%;
  max-width: 280px;
  height: 56px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.quick-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.quick-btn .el-icon {
  font-size: 20px;
  margin-right: 8px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .dashboard-home {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }
  
  .quick-btn {
    max-width: 100%;
    height: 48px;
    font-size: 14px;
  }
}
</style>
