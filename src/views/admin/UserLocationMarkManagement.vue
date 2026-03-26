<template>
  <div class="user-location-mark-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Location /></el-icon>
          用户地点标记管理
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
            <span class="current-page">用户地点标记管理</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="mark-card">
      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索地点名称、描述或地址"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.campusId"
          placeholder="选择校区"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option label="主校区" :value="1" />
          <el-option label="分校区" :value="2" />
        </el-select>

        <el-select
          v-model="searchForm.markType"
          placeholder="选择标记类型"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option
            v-for="type in markTypes"
            :key="type"
            :label="getMarkTypeName(type)"
            :value="type"
          />
        </el-select>

        <el-select
          v-model="searchForm.verificationStatus"
          placeholder="选择审核状态"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option label="待审核" value="pending" />
          <el-option label="审核通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- 用户地点标记列表表格 -->
      <el-table
        v-loading="loading"
        :data="markList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="locationName" label="地点名称" min-width="180" />
        <el-table-column prop="markType" label="标记类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getMarkTypeColor(row.markType)">
              {{ getMarkTypeName(row.markType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="markCategory" label="分类" width="120" />
        <el-table-column prop="campusId" label="校区" width="100">
          <template #default="{ row }">
            <el-tag :type="row.campusId === 1 ? 'primary' : 'success'">
              {{ row.campusId === 1 ? '主校区' : '分校区' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verificationStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getVerificationStatusColor(row.verificationStatus)">
              {{ getVerificationStatusName(row.verificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visibility" label="可见性" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.visibility === 'public_active'" type="success">公开</el-tag>
            <el-tag v-else-if="row.visibility === 'public_passive'" type="warning">被动公开</el-tag>
            <el-tag v-else-if="row.visibility === 'private'" type="info">私密</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userIdentityType" label="用户身份" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.userIdentityType" type="success">
              {{ getUserIdentityName(row.userIdentityType) }}
            </el-tag>
            <el-tag v-else type="info">未认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.verificationStatus === 'pending'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.verificationStatus === 'pending'"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewTitle"
      width="500px"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="地点标记详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentMark">
        <el-descriptions-item label="地点名称">{{ currentMark.locationName }}</el-descriptions-item>
        <el-descriptions-item label="标记类型">{{ getMarkTypeName(currentMark.markType) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentMark.markCategory }}</el-descriptions-item>
        <el-descriptions-item label="校区">{{ currentMark.campusId === 1 ? '主校区' : '分校区' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getVerificationStatusColor(currentMark.verificationStatus)">
            {{ getVerificationStatusName(currentMark.verificationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="可见性">{{ currentMark.visibility }}</el-descriptions-item>
        <el-descriptions-item label="用户身份">
          <el-tag v-if="currentMark.userIdentityType" type="success">
            {{ getUserIdentityName(currentMark.userIdentityType) }}
          </el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMark.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{ currentMark.addressDetail }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentMark.description }}</el-descriptions-item>
        <el-descriptions-item label="联系方式" :span="2">{{ currentMark.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="图片" :span="2">
          <div v-if="currentMark.images && currentMark.images.length > 0" style="display: flex; gap: 10px;">
            <el-image
              v-for="(img, index) in currentMark.images"
              :key="index"
              :src="img"
              :preview-src-list="currentMark.images"
              :initial-index="index"
              fit="cover"
              style="width: 100px; height: 100px; cursor: pointer;"
            />
          </div>
          <div v-else>无图片</div>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentMark.reviewComment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Location, HomeFilled, Search, Plus, Delete } from '@element-plus/icons-vue';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// 列表数据
const markList = ref([]);
const loading = ref(false);
const selectedIds = ref([]);
const markTypes = ref([]);

// 分页
const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

// 搜索表单
const searchForm = reactive({
  keyword: '',
  campusId: null,
  markType: null,
  verificationStatus: ''
});

// 对话框
const reviewDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const reviewTitle = ref('');
const currentMark = ref(null);
const reviewForm = reactive({
  markId: null,
  status: '',
  reason: ''
});

// 获取标记类型名称
const getMarkTypeName = (type) => {
  const names = {
    meeting_point: '约见地点',
    merchant_shop: '店铺位置',
    organization_activity: '活动地点',
    other: '其他'
  };
  return names[type] || type;
};

// 获取标记类型颜色
const getMarkTypeColor = (type) => {
  const colors = {
    meeting_point: 'primary',
    merchant_shop: 'warning',
    organization_activity: 'success',
    other: 'info'
  };
  return colors[type] || 'info';
};

// 获取审核状态名称
const getVerificationStatusName = (status) => {
  const names = {
    pending: '待审核',
    approved: '审核通过',
    rejected: '已拒绝'
  };
  return names[status] || status;
};

// 获取审核状态颜色
const getVerificationStatusColor = (status) => {
  const colors = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  };
  return colors[status] || 'info';
};

// 获取用户身份名称
const getUserIdentityName = (type) => {
  const names = {
    student: '学生',
    staff: '教职工',
    merchant: '商户',
    organization: '组织'
  };
  return names[type] || type;
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('page', pagination.page);
    params.append('limit', pagination.limit);
    
    if (searchForm.keyword) params.append('keyword', searchForm.keyword);
    if (searchForm.campusId) params.append('campusId', searchForm.campusId);
    if (searchForm.markType) params.append('markType', searchForm.markType);
    if (searchForm.verificationStatus) params.append('verificationStatus', searchForm.verificationStatus);

    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/user-location-marks/list?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      markList.value = response.data.data.list;
      pagination.total = response.data.data.total;
    } else {
      ElMessage.error(response.data.message || '加载失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败：' + (error.message || '网络错误'));
  } finally {
    loading.value = false;
  }
};

// 加载标记类型
const loadMarkTypes = async () => {
  try {
    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/user-location-marks/mark-types`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      markTypes.value = response.data.data;
    }
  } catch (error) {
    console.error('加载标记类型失败:', error);
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(row => row.id);
};

// 通过审核
const handleApprove = (row) => {
  reviewForm.markId = row.id;
  reviewForm.status = 'approved';
  reviewForm.reason = '';
  reviewTitle.value = '通过审核';
  reviewDialogVisible.value = true;
};

// 拒绝审核
const handleReject = (row) => {
  reviewForm.markId = row.id;
  reviewForm.status = 'rejected';
  reviewForm.reason = '';
  reviewTitle.value = '拒绝审核';
  reviewDialogVisible.value = true;
};

// 提交审核
const submitReview = async () => {
  try {
    const token = localStorage.getItem('admin_token');
    const params = new URLSearchParams();
    params.append('status', reviewForm.status);
    if (reviewForm.reason) params.append('reason', reviewForm.reason);

    const response = await axios.post(
      `${API_BASE_URL}/api/admin/user-location-marks/${reviewForm.markId}/review?${params.toString()}`,
      {},
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.data.code === 200) {
      ElMessage.success('审核成功');
      reviewDialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(response.data.message || '审核失败');
    }
  } catch (error) {
    console.error('审核失败:', error);
    ElMessage.error('审核失败：' + (error.response?.data?.message || '网络错误'));
  }
};

// 查看详情
const handleViewDetail = (row) => {
  currentMark.value = row;
  detailDialogVisible.value = true;
};

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除地点标记"${row.locationName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const token = localStorage.getItem('admin_token');
      const response = await axios.delete(
        `${API_BASE_URL}/api/admin/user-location-marks/${row.id}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success('删除成功');
        loadData();
      } else {
        ElMessage.error(response.data.message || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage.error('删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 个标记吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const token = localStorage.getItem('admin_token');
      const response = await axios.delete(
        `${API_BASE_URL}/api/admin/user-location-marks/batch`,
        {
          data: { ids: selectedIds.value },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success('批量删除成功');
        selectedIds.value = [];
        loadData();
      } else {
        ElMessage.error(response.data.message || '批量删除失败');
      }
    } catch (error) {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

onMounted(() => {
  loadData();
  loadMarkTypes();
});
</script>

<style scoped>
.user-location-mark-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-left-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  flex: 1;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #909399;
}

.separator {
  color: #C0C4CC;
}

.back-home-btn {
  padding: 4px 8px;
}

.mark-card {
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
