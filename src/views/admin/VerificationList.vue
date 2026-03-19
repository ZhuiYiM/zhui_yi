<template>
  <div class="verification-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><DocumentChecked /></el-icon>
          身份认证管理
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">认证管理</span>
            <span class="separator">/</span>
            <span class="current-page">认证审核</span>
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
            placeholder="用户名/学号/姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="认证类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable>
            <el-option label="学生证认证" value="student" />
            <el-option label="身份证实名认证" value="idcard" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
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
    </el-card>

    <!-- 认证申请列表 -->
    <el-card class="table-card">
      <el-table 
        :data="applicationList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="申请 ID" width="100" />
        <el-table-column label="申请人" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.user?.avatarUrl" :size="40" />
              <div class="info">
                <div class="username">{{ row.user?.username }}</div>
                <div class="realname">{{ row.user?.realName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="type" label="认证类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'student' ? 'primary' : 'success'">
              {{ row.type === 'student' ? '学生证认证' : '实名认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认证信息" min-width="200">
          <template #default="{ row }">
            <div class="verification-info">
              <div v-if="row.type === 'student'">
                <div>学院：{{ row.college }}</div>
                <div>专业：{{ row.major }}</div>
              </div>
              <div v-else>
                <div>姓名：{{ row.realName }}</div>
                <div>身份证：{{ maskIdCard(row.idCard) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'pending' ? 'warning' : row.status === 'approved' ? 'success' : 'danger'">
              {{ row.status === 'pending' ? '待审核' : row.status === 'approved' ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              size="small" 
              type="success" 
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              size="small" 
              type="danger" 
              @click="handleReject(row)"
            >
              拒绝
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

    <!-- 认证详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="认证申请详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentApplication">
        <el-descriptions-item label="申请 ID">{{ currentApplication.id }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApplication.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentApplication.user?.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentApplication.studentId }}</el-descriptions-item>
        <el-descriptions-item label="认证类型">
          <el-tag :type="currentApplication.type === 'student' ? 'primary' : 'success'">
            {{ currentApplication.type === 'student' ? '学生证认证' : '实名认证' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentApplication.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentApplication.major }}</el-descriptions-item>
        <el-descriptions-item label="身份证号码" v-if="currentApplication.idCard">
          {{ maskIdCard(currentApplication.idCard) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ new Date(currentApplication.createTime).toLocaleString() }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="currentApplication.status === 'pending' ? 'warning' : currentApplication.status === 'approved' ? 'success' : 'danger'">
            {{ currentApplication.status === 'pending' ? '待审核' : currentApplication.status === 'approved' ? '已通过' : '已拒绝' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2" v-if="currentApplication.adminRemark">
          {{ currentApplication.adminRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="currentApplication.rejectReason">
          {{ currentApplication.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer v-if="currentApplication?.status === 'pending'">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject(currentApplication)">拒绝</el-button>
        <el-button type="primary" @click="handleApprove(currentApplication)">通过</el-button>
      </template>
      <template #footer v-else>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { DocumentChecked, Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, ElInput } from 'element-plus';
import { adminAPI } from '@/api/admin';

const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
});

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

const applicationList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const currentApplication = ref(null);

// 加载认证申请列表
const loadApplicationList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    };
    const res = await adminAPI.getVerificationApplications(params);
    if (res.code === 200) {
      applicationList.value = res.data.records || res.data;
      pagination.total = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '加载认证申请列表失败');
    }
  } catch (error) {
    console.error('加载认证申请列表失败:', error);
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadApplicationList();
};

// 重置
const handleReset = () => {
  searchForm.keyword = '';
  searchForm.type = '';
  searchForm.status = '';
  handleSearch();
};

// 查看详情
const handleViewDetail = (row) => {
  currentApplication.value = row;
  dialogVisible.value = true;
};

// 通过审核
const handleApprove = async (row) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入审核备注（可选）', '通过认证', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.{0,200}/,
      inputErrorMessage: '备注长度不能超过 200 字'
    });
    
    const res = await adminAPI.approveApplication(row.id, remark || '');
    if (res.code === 200) {
      ElMessage.success('已通过认证申请');
      loadApplicationList();
      dialogVisible.value = false;
    } else {
      ElMessage.error(res.message || '审核失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error);
    }
  }
};

// 拒绝审核
const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝认证', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.{1,200}/,
      inputErrorMessage: '请输入拒绝原因'
    });
    
    const res = await adminAPI.rejectApplication(row.id, reason);
    if (res.code === 200) {
      ElMessage.success('已拒绝认证申请');
      loadApplicationList();
      dialogVisible.value = false;
    } else {
      ElMessage.error(res.message || '拒绝失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error);
    }
  }
};

// 脱敏显示身份证号
const maskIdCard = (idCard) => {
  if (!idCard) return '-';
  return idCard.replace(/^(.{6}).*(.{4})$/, '$1****$2');
};

onMounted(() => {
  loadApplicationList();
});
</script>

<style scoped>
.verification-management {
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

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.table-card {
  border-radius: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 500;
  color: #303133;
}

.realname {
  font-size: 13px;
  color: #909399;
}

.verification-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
