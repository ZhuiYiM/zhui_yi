<template>
  <div class="dashboard-home">
    <h2>数据统计</h2>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
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
      
      <el-col :span="6">
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
      
      <el-col :span="6">
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
      
      <el-col :span="6">
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

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>快捷入口</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-button type="primary" @click="$router.push('/admin/users')">
            <el-icon><User /></el-icon>
            用户管理
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="success" @click="$router.push('/admin/topics')">
            <el-icon><Document /></el-icon>
            话题审核
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="warning" @click="$router.push('/admin/reports')">
            <el-icon><Warning /></el-icon>
            举报处理
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, ChatDotRound, Shop, Warning, Document } from '@element-plus/icons-vue';

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
  padding: 20px;
}

.dashboard-home h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.stat-card {
  text-align: center;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
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
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
}
</style>
