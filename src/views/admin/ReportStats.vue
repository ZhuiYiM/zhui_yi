<template>
  <div class="report-stats">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          举报统计
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">举报管理</span>
            <span class="separator">/</span>
            <span class="current-page">举报统计</span>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="总举报数" :value="stats.total">
            <template #prefix>
              <el-icon><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="待处理" :value="stats.pending">
            <template #prefix>
              <el-icon style="color: #E6A23C"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="处理中" :value="stats.processing">
            <template #prefix>
              <el-icon style="color: #409EFF"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="已处理" :value="stats.processed">
            <template #prefix>
              <el-icon style="color: #67C23A"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 按类型统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按类型统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-for="item in stats.byType" :key="item.type" class="stat-item">
              <div class="stat-label">{{ formatType(item.type) }}</div>
              <el-progress :percentage="calculatePercentage(item.count)" :format="() => item.count + '件'" />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 按状态统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按状态统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-for="item in stats.byStatus" :key="item.status" class="stat-item">
              <div class="stat-label">{{ formatStatus(item.status) }}</div>
              <el-progress 
                :percentage="calculatePercentage(item.count)" 
                :color="getStatusColor(item.status)"
                :format="() => item.count + '件'" 
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近处理记录 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>最近处理记录</span>
          <el-button type="primary" size="small" @click="loadStats">刷新</el-button>
        </div>
      </template>
      <el-table :data="recentReports" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processedAt" label="处理时间" width="180">
          <template #default="{ row }">
            {{ row.processedAt ? new Date(row.processedAt).toLocaleString() : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Warning, DataAnalysis, HomeFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '@/api/request';

const loading = ref(false);
const stats = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  processed: 0,
  byType: [],
  byStatus: []
});

const recentReports = ref([]);

// 格式化举报类型
const formatType = (type) => {
  const typeMap = {
    topic: '话题',
    product: '商品',
    user: '用户',
    comment: '评论'
  };
  return typeMap[type] || type;
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已处理',
    3: '已忽略'
  };
  return statusMap[status] || '未知';
};

// 获取状态标签类型
const getStatusTag = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  };
  return statusMap[status] || 'info';
};

// 获取类型标签类型
const getTypeTag = (type) => {
  const typeMap = {
    topic: 'success',
    product: 'warning',
    user: 'danger',
    comment: 'info'
  };
  return typeMap[type] || 'info';
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    0: '#E6A23C',
    1: '#409EFF',
    2: '#67C23A',
    3: '#909399'
  };
  return colorMap[status] || '#909399';
};

// 计算百分比
const calculatePercentage = (count) => {
  if (stats.total === 0) return 0;
  return Math.round((count / stats.total) * 100);
};

// 加载统计数据
const loadStats = async () => {
  loading.value = true;
  try {
    const response = await request.get('/admin/reports/stats');
    
    console.log('📊 举报统计响应:', response);
    console.log('response.data:', response.data);
    console.log('response.code:', response.code);
    
    // response 已经是后端返回的 data 部分，不需要再访问 response.data
    if (response && response.total !== undefined) {
      const data = response;
      stats.total = data.total || 0;
      stats.pending = data.pending || 0;
      stats.processing = data.processing || 0;
      stats.processed = data.processed || 0;
      
      // 将对象转换为数组格式
      stats.byType = Object.entries(data.byType || {}).map(([type, count]) => ({
        type,
        count
      }));
      
      stats.byStatus = Object.entries(data.byStatus || {}).map(([status, count]) => ({
        status: parseInt(status),
        count
      }));
      
      recentReports.value = data.recent || [];
      console.log('✅ 举报统计加载成功');
      console.log('转换后的 byType:', stats.byType);
      console.log('转换后的 byStatus:', stats.byStatus);
      console.log('stats.total:', stats.total);
      console.log('stats.pending:', stats.pending);
      console.log('stats.processing:', stats.processing);
      console.log('stats.processed:', stats.processed);
    } else {
      ElMessage.error('数据格式错误');
      console.error('数据格式错误:', response);
    }
  } catch (error) {
    console.error('❌ 加载举报统计失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.report-stats {
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

.stat-card {
  border-radius: 8px;
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  padding: 10px;
}

.stat-item {
  margin-bottom: 20px;
}

.stat-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}
</style>
