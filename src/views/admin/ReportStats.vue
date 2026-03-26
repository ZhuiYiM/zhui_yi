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
          <el-statistic title="已忽略" :value="stats.ignored">
            <template #prefix>
              <el-icon style="color: #909399"><Warning /></el-icon>
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
              <span>按被举报对象统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-for="item in stats.byType" :key="item.type" class="stat-item">
              <div class="stat-label">
                <el-tag :type="getTypeTag(item.type)" size="small" style="margin-right: 8px">
                  {{ formatType(item.type) }}
                </el-tag>
              </div>
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
              <span>按处理状态统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-for="item in stats.byStatus" :key="item.status" class="stat-item">
              <div class="stat-label">
                <el-tag :type="getStatusTag(item.status)" size="small" style="margin-right: 8px">
                  {{ formatStatus(item.status) }}
                </el-tag>
              </div>
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
          <el-button type="primary" size="small" @click="loadStats" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="recentReports" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="targetType" label="被举报对象" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.targetType)" size="small">{{ formatType(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportType" label="举报原因" width="120">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.reportType)" size="small">{{ formatReportType(row.reportType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报描述" min-width="200" show-overflow-tooltip />
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
  ignored: 0,
  byType: [],
  byStatus: []
});

const recentReports = ref([]);

// 格式化举报类型（被举报对象）
const formatType = (type) => {
  const typeMap = {
    topic: '话题',
    product: '商品',
    user: '用户',
    comment: '评论',
    location: '地点'
  };
  return typeMap[type] || type;
};

// 格式化举报原因类型
const formatReportType = (reportType) => {
  const typeMap = {
    pornography: '色情低俗',
    illegal: '违法犯罪',
    political: '政治敏感',
    spam: '垃圾广告',
    fake_info: '虚假信息',
    copyright: '侵权内容',
    harassment: '人身攻击',
    other: '其他违规'
  };
  return typeMap[reportType] || reportType;
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
    comment: 'info',
    location: 'primary'
  };
  return typeMap[type] || 'info';
};

// 获取举报原因标签类型
const getReportTypeTag = (reportType) => {
  const typeMap = {
    pornography: 'danger',
    illegal: 'danger',
    political: 'danger',
    spam: 'warning',
    fake_info: 'warning',
    copyright: 'info',
    harassment: 'danger',
    other: 'info'
  };
  return typeMap[reportType] || 'info';
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

// 加载统计
const loadStats = async () => {
  loading.value = true;
  try {
    const response = await request.get('/admin/reports/stats');
    
    // 响应数据已在拦截器中处理，直接赋值
    if (response && response.total !== undefined) {
      const data = response;
      stats.total = data.total || 0;
      stats.pending = data.pending || 0;
      stats.processing = data.processing || 0;
      stats.processed = data.processed || 0;
      stats.ignored = data.ignored || 0;
      
      // 数据转换完成
    } else {
      ElMessage.error('数据格式错误');
    }
  } catch (error) {
    // 错误已在拦截器中统一处理
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
