<template>
  <div class="report-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Warning /></el-icon>
          举报管理
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
            <span class="current-page">举报列表</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="table-card">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="举报类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 150px">
            <el-option label="话题" value="topic" />
            <el-option label="商品" value="product" />
            <el-option label="用户" value="user" />
            <el-option label="评论" value="comment" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
            <el-option label="已忽略" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="reportList" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="举报类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标 ID" width="100" />
        <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="详细描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterId" label="举报人 ID" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="举报时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="handleProcess(row)"
              :disabled="row.status !== 0"
            >
              处理
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.limit"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="举报详情" width="600px">
      <el-descriptions :column="1" border v-if="currentReport">
        <el-descriptions-item label="举报 ID">{{ currentReport.id }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">{{ formatType(currentReport.type) }}</el-descriptions-item>
        <el-descriptions-item label="目标 ID">{{ currentReport.targetId }}</el-descriptions-item>
        <el-descriptions-item label="举报原因">{{ currentReport.reason }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">{{ currentReport.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="举报人 ID">{{ currentReport.reporterId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ formatStatus(currentReport.status) }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">
          {{ currentReport.createdAt ? new Date(currentReport.createdAt).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理说明" v-if="currentReport.processNote">
          {{ currentReport.processNote }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理举报" width="500px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理状态">
          <el-select v-model="processForm.status" style="width: 100%">
            <el-option label="已处理" :value="2" />
            <el-option label="已忽略" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input 
            v-model="processForm.processNote" 
            type="textarea" 
            :rows="3"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Warning, Search, Refresh, HomeFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/api/request';

const loading = ref(false);
const submitting = ref(false);
const reportList = ref([]);

const searchForm = reactive({
  type: '',
  status: null
});

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const viewDialogVisible = ref(false);
const currentReport = ref(null);
const processDialogVisible = ref(false);
const processForm = reactive({
  id: null,
  status: 2,
  processNote: ''
});

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

// 加载举报列表
const loadReportList = async () => {
  loading.value = true;
  try {
    const response = await request.get('/admin/reports/list', {
      params: {
        page: pagination.page,
        limit: pagination.limit,
        ...searchForm
      }
    });
    
    console.log('📋 举报列表响应:', response);
    if (response.code === 200 || response.list !== undefined) {
      reportList.value = response.list || [];
      pagination.total = response.total || 0;
      console.log('✅ 举报列表加载成功:', reportList.value.length, '条记录');
    } else {
      ElMessage.error(response.message || '加载失败');
    }
  } catch (error) {
    console.error('❌ 加载举报列表失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('🔍 搜索举报，pagination:', pagination);
  loadReportList();
};

// 重置
const handleReset = () => {
  searchForm.type = '';
  searchForm.status = null;
  pagination.page = 1;
  loadReportList();
};

// 查看详情
const handleView = (row) => {
  currentReport.value = row;
  viewDialogVisible.value = true;
};

// 处理举报
const handleProcess = (row) => {
  processForm.id = row.id;
  processForm.status = 2;
  processForm.processNote = '';
  processDialogVisible.value = true;
};

// 提交处理
const handleSubmitProcess = async () => {
  try {
    submitting.value = true;
    const response = await request.post(`/admin/reports/process/${processForm.id}`, {
      status: processForm.status,
      processNote: processForm.processNote
    });
    
    if (response.code === 200) {
      ElMessage.success('处理成功');
      processDialogVisible.value = false;
      loadReportList();
    } else {
      ElMessage.error(response.message || '处理失败');
    }
  } catch (error) {
    console.error('处理失败:', error);
    ElMessage.error('处理失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 删除举报
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该举报记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await request.delete(`/admin/reports/${row.id}`);
    if (response.code === 200) {
      ElMessage.success('删除成功');
      loadReportList();
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  }
};

onMounted(() => {
  loadReportList();
});
</script>

<style scoped>
.report-management {
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
  margin-bottom: 20px;
}
</style>
