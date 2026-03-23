<template>
  <div class="comment-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><ChatLineRound /></el-icon>
          评论管理
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
            <span class="current-page">评论管理</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="table-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索评论内容或用户 ID"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 150px">
          <el-option label="正常" :value="1" />
          <el-option label="待审核" :value="0" />
          <el-option label="已删除" :value="-1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <el-table :data="commentList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="topicId" label="话题 ID" width="100" />
        <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.limit"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ChatLineRound, HomeFilled, Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { adminAPI } from '@/api/admin';

const loading = ref(false);
const commentList = ref([]);
const searchForm = reactive({
  keyword: '',
  status: null
});

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

// 加载评论列表
const loadCommentList = async () => {
  loading.value = true;
  try {
    const response = await adminAPI.getCommentList({
      page: pagination.page,
      limit: pagination.limit,
      ...searchForm
    });
    
    console.log('📝 评论列表响应:', response);
    console.log('当前页码:', pagination.page, '每页数量:', pagination.limit);
    
    // response 已经是后端返回的 data 部分
    if (response && (response.list !== undefined || Array.isArray(response))) {
      commentList.value = response.list || [];
      pagination.total = response.total || 0;
      console.log('✅ 评论列表加载成功:', commentList.value.length, '条记录', '总记录:', pagination.total);
    } else {
      ElMessage.error('数据格式错误');
      console.error('数据格式错误:', response);
    }
  } catch (error) {
    console.error('❌ 加载评论列表失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadCommentList();
};

// 处理页码变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage;
  loadCommentList();
};

// 处理每页数量变化
const handleSizeChange = (newSize) => {
  pagination.limit = newSize;
  pagination.page = 1;
  loadCommentList();
};

// 重置
const handleReset = () => {
  searchForm.keyword = '';
  searchForm.status = null;
  handleSearch();
};

// 删除评论
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminAPI.deleteComment(id);
      // 响应拦截器已经处理了错误情况，能到达这里说明成功
      ElMessage.success('删除成功');
      loadCommentList();
    } catch (error) {
      console.error('❌ 删除评论失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  }).catch(() => {});
};

// 格式化状态
const getStatusTag = (status) => {
  const statusMap = {
    1: 'success',
    0: 'warning',
    '-1': 'danger'
  };
  return statusMap[status] || 'info';
};

const getStatusText = (status) => {
  const statusMap = {
    1: '正常',
    0: '待审核',
    '-1': '已删除'
  };
  return statusMap[status] || '未知';
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
  loadCommentList();
});
</script>

<style scoped>
.comment-management {
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
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
