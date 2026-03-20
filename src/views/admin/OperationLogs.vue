<template>
  <div class="operation-logs">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><List /></el-icon>
          操作日志
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
            <span class="current-page">操作日志</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="table-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.module"
          placeholder="操作模块"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="searchForm.operation"
          placeholder="操作类型"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-input-number
          v-model="searchForm.operatorId"
          placeholder="操作人 ID"
          :min="0"
          controls-position="right"
          style="width: 200px"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <el-table :data="logList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="admin_name" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getOperationTag(row.operation)" size="small">
              {{ row.operation }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="150" />
        <el-table-column prop="target_id" label="目标 ID" width="100" />
        <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
        <el-table-column prop="ip_address" label="IP 地址" width="140" />
        <el-table-column prop="created_at" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.limit"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { List, HomeFilled, Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const loading = ref(false);
const logList = ref([]);
const searchForm = reactive({
  module: '',
  operation: '',
  operatorId: null
});

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

// 加载操作日志列表
const loadLogList = async () => {
  loading.value = true;
  try {
    const response = await adminAPI.getOperationLogs({
      page: pagination.page,
      limit: pagination.limit,
      ...searchForm
    });
    
    console.log('📝 操作日志响应:', response);
    if (response.code === 200 || response.list !== undefined) {
      logList.value = response.list || [];
      pagination.total = response.total || 0;
      console.log('✅ 操作日志加载成功:', logList.value.length, '条记录');
    } else {
      ElMessage.error(response.message || '加载失败');
    }
  } catch (error) {
    console.error('❌ 加载操作日志失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadLogList();
};

// 重置
const handleReset = () => {
  searchForm.module = '';
  searchForm.operation = '';
  searchForm.operatorId = null;
  handleSearch();
};

// 获取操作类型标签
const getOperationTag = (operation) => {
  const tagMap = {
    'delete': 'danger',
    'update': 'warning',
    'create': 'success',
    'audit': 'primary'
  };
  return tagMap[operation] || 'info';
};

// 格式化时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-';
  const date = new Date(datetime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadLogList();
});
</script>

<style scoped>
.operation-logs {
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

.table-card {
  border-radius: 12px;
}

.search-form {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
