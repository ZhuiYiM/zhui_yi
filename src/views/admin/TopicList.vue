<template>
  <div class="topic-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Document /></el-icon>
          话题管理
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">内容管理</span>
            <span class="separator">/</span>
            <span class="current-page">话题管理</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入标题或内容"
            clearable
            @keyup.enter="handleSearch"
          />
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
      
      <!-- 批量操作 -->
      <div class="batch-operations">
        <el-checkbox v-model="selectAll" @change="handleSelectAll" :indeterminate="isIndeterminate">
          全选
        </el-checkbox>
        <el-button 
          type="danger" 
          size="small" 
          @click="handleBatchDelete" 
          :disabled="selectedIds.length === 0"
        >
          批量删除
        </el-button>
        <el-button 
          type="success" 
          size="small" 
          @click="handleBatchApprove" 
          :disabled="selectedIds.length === 0"
        >
          批量通过
        </el-button>
        <el-button 
          type="warning" 
          size="small" 
          @click="handleBatchReject" 
          :disabled="selectedIds.length === 0"
        >
          批量下架
        </el-button>
        <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table 
        :data="topicList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已发布' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleReview(row)"
            >
              {{ row.status === 1 ? '下架' : '通过' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Document, HomeFilled, Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { adminAPI } from '@/api/admin';

const searchForm = reactive({
  keyword: ''
});

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

const topicList = ref([]);
const loading = ref(false);

// 批量操作
const selectAll = ref(false);
const isIndeterminate = ref(false);
const selectedIds = ref([]);
const selectedRows = ref([]);

// 加载话题列表
const loadTopicList = async () => {
  loading.value = true;
  console.log('📦 准备加载话题列表，pagination:', pagination);
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    };
    const res = await adminAPI.getTopicList(params);
    console.log('话题列表响应:', res);
    
    // 响应拦截器已经返回了 data，所以直接使用 res
    const data = res;
    if (res.code === 200 || data) {
      // 处理不同的数据结构
      const records = data?.records || data?.list || data || [];
      topicList.value = Array.isArray(records) ? records : [];
      pagination.total = data?.total ?? topicList.value.length ?? 0;
      console.log('✅ 话题列表加载成功:', topicList.value.length, '条记录');
    } else {
      ElMessage.error(res.message || '加载话题列表失败');
    }
  } catch (error) {
    console.error('加载话题列表失败:', error);
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('🔍 handleSearch 触发，当前 pagination:', pagination);
  // 注意：不要重置 pagination.page，因为 v-model 会自动更新它
  loadTopicList();
};

// 重置
const handleReset = () => {
  searchForm.keyword = '';
  handleSearch();
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
  selectedRows.value = selection;
  selectAll.value = selection.length === topicList.value.length;
  isIndeterminate.value = selection.length > 0 && selection.length < topicList.value.length;
};

// 全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectAll.value = true;
    selectedIds.value = topicList.value.map(item => item.id);
    selectedRows.value = topicList.value;
  } else {
    selectAll.value = false;
    selectedIds.value = [];
    selectedRows.value = [];
  }
  isIndeterminate.value = false;
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的话题');
    return;
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个话题吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const promises = selectedIds.value.map(id => adminAPI.deleteTopic(id));
    await Promise.all(promises);
    
    ElMessage.success('批量删除成功');
    selectedIds.value = [];
    selectedRows.value = [];
    selectAll.value = false;
    loadTopicList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败');
    }
  } finally {
    loading.value = false;
  }
};

// 批量通过
const handleBatchApprove = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审核的话题');
    return;
  }
  
  try {
    await ElMessageBox.confirm(`确定要通过选中的 ${selectedIds.value.length} 个话题吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const promises = selectedRows.value
      .filter(row => row.status !== 1)
      .map(row => adminAPI.reviewTopic(row.id, 1));
    
    if (promises.length > 0) {
      await Promise.all(promises);
    }
    
    ElMessage.success('批量通过成功');
    selectedIds.value = [];
    selectedRows.value = [];
    selectAll.value = false;
    loadTopicList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过失败:', error);
      ElMessage.error('批量通过失败');
    }
  } finally {
    loading.value = false;
  }
};

// 批量下架
const handleBatchReject = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审核的话题');
    return;
  }
  
  try {
    await ElMessageBox.confirm(`确定要下架选中的 ${selectedIds.value.length} 个话题吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const promises = selectedRows.value
      .filter(row => row.status !== 0)
      .map(row => adminAPI.reviewTopic(row.id, 0));
    
    if (promises.length > 0) {
      await Promise.all(promises);
    }
    
    ElMessage.success('批量下架成功');
    selectedIds.value = [];
    selectedRows.value = [];
    selectAll.value = false;
    loadTopicList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量下架失败:', error);
      ElMessage.error('批量下架失败');
    }
  } finally {
    loading.value = false;
  }
};

// 删除话题
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该话题吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await adminAPI.deleteTopic(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadTopicList();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

// 审核话题
const handleReview = async (row) => {
  const action = row.status === 1 ? '下架' : '通过';
  try {
    await ElMessageBox.confirm(`确定要${action}该话题吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await adminAPI.reviewTopic(row.id, row.status === 1 ? 0 : 1);
    if (res.code === 200) {
      ElMessage.success(`${action}成功`);
      row.status = row.status === 1 ? 0 : 1;
    } else {
      ElMessage.error(res.message || `${action}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error);
    }
  }
};

onMounted(() => {
  loadTopicList();
});
</script>

<style scoped>
.topic-management {
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
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.batch-operations {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.selected-count {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
}
</style>
