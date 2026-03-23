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
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon location-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.locationCount }}</div>
              <div class="stat-label">地点总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon comment-icon">
              <el-icon><Comment /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.commentCount }}</div>
              <div class="stat-label">评论总数</div>
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
            <el-button type="primary" @click="$router.push('/admin/dashboard/users')" class="quick-btn user-btn">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="success" @click="$router.push('/admin/dashboard/verifications')" class="quick-btn verify-btn">
              <el-icon><DocumentChecked /></el-icon>
              <span>认证审核</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="warning" @click="$router.push('/admin/dashboard/reports')" class="quick-btn report-btn">
              <el-icon><Warning /></el-icon>
              <span>举报处理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/topics')" class="quick-btn topic-btn">
              <el-icon><ChatDotRound /></el-icon>
              <span>话题管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/products')" class="quick-btn product-btn">
              <el-icon><Shop /></el-icon>
              <span>商品管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/advertisements')" class="quick-btn ad-btn">
              <el-icon><Picture /></el-icon>
              <span>广告管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/tags')" class="quick-btn tag-btn">
              <el-icon><Collection /></el-icon>
              <span>标签管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/locations')" class="quick-btn location-btn">
              <el-icon><Location /></el-icon>
              <span>地点管理</span>
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="quick-btn-wrapper">
            <el-button type="info" @click="$router.push('/admin/dashboard/comments')" class="quick-btn comment-mgmt-btn">
              <el-icon><Comment /></el-icon>
              <span>评论管理</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, ChatDotRound, Shop, Warning, Document, DataAnalysis, Grid, DocumentChecked, Location, Picture, Collection, Comment } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const stats = reactive({
  userCount: 0,
  topicCount: 0,
  productCount: 0,
  locationCount: 0,
  reportCount: 0,
  commentCount: 0
});

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await adminAPI.getDashboardStats();
    console.log('📊 统计数据响应:', response);
    
    const data = response.data || response.result || response;
    stats.userCount = data.userCount || 0;
    stats.topicCount = data.topicCount || 0;
    stats.productCount = data.productCount || 0;
    stats.locationCount = data.locationCount || 0;
    stats.reportCount = data.pendingReportCount || 0;
    stats.commentCount = data.commentCount || 0;
    
    console.log('✅ 统计数据加载成功');
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error);
    ElMessage.error('加载统计数据失败');
  }
};

onMounted(() => {
  loadStats();
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

.location-icon {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
}

.comment-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
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

/* 用户管理 - 蓝色渐变 */
.user-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 认证审核 - 绿色渐变 */
.verify-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

/* 举报处理 - 橙色渐变 */
.report-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

/* 话题管理 - 紫色渐变 */
.topic-btn {
  background: linear-gradient(135deg, #da22ff 0%, #9733ee 100%);
}

/* 商品管理 - 青色渐变 */
.product-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

/* 广告管理 - 粉红渐变 */
.ad-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

/* 标签管理 - 蓝绿渐变 */
.tag-btn {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
}

/* 地点管理 - 深蓝渐变 */
.location-btn {
  background: linear-gradient(135deg, #2193b0 0%, #6dd5ed 100%);
}

/* 评论管理 - 青绿渐变 */
.comment-mgmt-btn {
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
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
