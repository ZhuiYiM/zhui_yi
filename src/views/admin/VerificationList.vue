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
        <el-table-column label="用户 ID" width="120" />
        <el-table-column prop="verificationType" label="认证类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.verificationType)">
              {{ getTypeName(row.verificationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认证信息" min-width="200">
          <template #default="{ row }">
            <div class="verification-info">
              <!-- 优先显示 extraInfo 中的信息 -->
              <div v-if="row.extraInfo" style="font-size: 12px;">
                <div v-if="row.verificationType === 'student'">
                  <div>学号：{{ row.extraInfo.studentId || '-' }}</div>
                  <div>手机：{{ row.extraInfo.contactPhone || '-' }}</div>
                </div>
                <div v-else-if="row.verificationType === 'staff'">
                  <div>工号：{{ row.extraInfo.staffId || '-' }}</div>
                  <div>部门：{{ row.extraInfo.department || '-' }}</div>
                </div>
                <div v-else-if="row.verificationType === 'merchant'">
                  <div>店铺：{{ row.extraInfo.shopName || '-' }}</div>
                </div>
                <div v-else-if="row.verificationType === 'organization'">
                  <div>组织：{{ row.extraInfo.organizationName || '-' }}</div>
                  <div>负责人：{{ row.extraInfo.leaderName || '-' }}</div>
                </div>
              </div>
              <!-- 兼容旧数据 -->
              <div v-else>
                <div v-if="row.studentId">学号：{{ row.studentId }}</div>
                <div v-if="row.realName">姓名：{{ row.realName }}</div>
                <div v-if="row.college">学院：{{ row.college }}</div>
                <div v-if="!row.studentId && !row.realName && !row.college">暂无详细信息</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submittedAt) }}
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
        <el-descriptions-item label="用户 ID">{{ currentApplication.userId }}</el-descriptions-item>
        <el-descriptions-item label="认证类型">
          <el-tag :type="getTypeTag(currentApplication.verificationType)">
            {{ getTypeName(currentApplication.verificationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(currentApplication.submittedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusTag(currentApplication.status)">
            {{ getStatusName(currentApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        
        <!-- 显示 extraInfo 中的详细信息 -->
        <template v-if="currentApplication.extraInfo">
          <el-descriptions-item label="学号" v-if="currentApplication.extraInfo.studentId">
            {{ currentApplication.extraInfo.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号" v-if="currentApplication.extraInfo.contactPhone">
            {{ currentApplication.extraInfo.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="学生证照片" v-if="currentApplication.extraInfo.studentCardUrl">
            <el-image 
              :src="getFullImageUrl(currentApplication.extraInfo.studentCardUrl)"
              :preview-src-list="[getFullImageUrl(currentApplication.extraInfo.studentCardUrl)]"
              style="width: 150px; height: 100px;"
              fit="contain"
            >
              <template #placeholder>
                <div style="width: 150px; height: 100px; display: flex; align-items: center; justify-content: center; background: #f5f7fa;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </el-descriptions-item>
          <el-descriptions-item label="工号" v-if="currentApplication.extraInfo.staffId">
            {{ currentApplication.extraInfo.staffId }}
          </el-descriptions-item>
          <el-descriptions-item label="所属部门" v-if="currentApplication.extraInfo.department">
            {{ currentApplication.extraInfo.department }}
          </el-descriptions-item>
          <el-descriptions-item label="工作证照片" v-if="currentApplication.extraInfo.workCardUrl">
            <el-image 
              :src="getFullImageUrl(currentApplication.extraInfo.workCardUrl)"
              :preview-src-list="[getFullImageUrl(currentApplication.extraInfo.workCardUrl)]"
              style="width: 150px; height: 100px;"
              fit="contain"
            >
              <template #placeholder>
                <div style="width: 150px; height: 100px; display: flex; align-items: center; justify-content: center; background: #f5f7fa;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </el-descriptions-item>
          <el-descriptions-item label="店铺名称" v-if="currentApplication.extraInfo.shopName">
            {{ currentApplication.extraInfo.shopName }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照" v-if="currentApplication.extraInfo.businessLicenseUrl">
            <el-image 
              :src="getFullImageUrl(currentApplication.extraInfo.businessLicenseUrl)"
              :preview-src-list="[getFullImageUrl(currentApplication.extraInfo.businessLicenseUrl)]"
              style="width: 150px; height: 100px;"
              fit="contain"
            >
              <template #placeholder>
                <div style="width: 150px; height: 100px; display: flex; align-items: center; justify-content: center; background: #f5f7fa;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </el-descriptions-item>
          <el-descriptions-item label="组织名称" v-if="currentApplication.extraInfo.organizationName">
            {{ currentApplication.extraInfo.organizationName }}
          </el-descriptions-item>
          <el-descriptions-item label="负责人" v-if="currentApplication.extraInfo.leaderName">
            {{ currentApplication.extraInfo.leaderName }}
          </el-descriptions-item>
          <el-descriptions-item label="组织证明文件" v-if="currentApplication.extraInfo.organizationProofUrl">
            <el-image 
              :src="getFullImageUrl(currentApplication.extraInfo.organizationProofUrl)"
              :preview-src-list="[getFullImageUrl(currentApplication.extraInfo.organizationProofUrl)]"
              style="width: 150px; height: 100px;"
              fit="contain"
            >
              <template #placeholder>
                <div style="width: 150px; height: 100px; display: flex; align-items: center; justify-content: center; background: #f5f7fa;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </el-descriptions-item>
          <el-descriptions-item label="备注" v-if="currentApplication.extraInfo.remark">
            {{ currentApplication.extraInfo.remark }}
          </el-descriptions-item>
        </template>
        
        <!-- 兼容旧数据 -->
        <el-descriptions-item label="学号" v-if="currentApplication.studentId && !currentApplication.extraInfo">
          {{ currentApplication.studentId }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名" v-if="currentApplication.realName && !currentApplication.extraInfo">
          {{ currentApplication.realName }}
        </el-descriptions-item>
        <el-descriptions-item label="学院" v-if="currentApplication.college && !currentApplication.extraInfo">
          {{ currentApplication.college }}
        </el-descriptions-item>
        
        <el-descriptions-item label="拒绝原因" :span="2" v-if="currentApplication.rejectionReason">
          {{ currentApplication.rejectionReason }}
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
import { DocumentChecked, Search, Refresh, Picture } from '@element-plus/icons-vue';
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
    
    // 后端直接返回数据，不是 {code, data} 格式
    if (res && res.records) {
      // 解析 extraInfo 字段（JSON 字符串转对象）
      applicationList.value = res.records.map(record => {
        if (record.extraInfo && typeof record.extraInfo === 'string') {
          try {
            return {
              ...record,
              extraInfo: JSON.parse(record.extraInfo)
            };
          } catch (e) {
            console.error('解析 extraInfo 失败:', e);
            return record;
          }
        }
        return record;
      });
      pagination.total = res.total || 0;
    } else {
      ElMessage.error('数据格式异常');
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
  // 确保 extraInfo 已解析
  if (row.extraInfo && typeof row.extraInfo === 'string') {
    try {
      row.extraInfo = JSON.parse(row.extraInfo);
    } catch (e) {
      console.error('解析 extraInfo 失败:', e);
    }
  }
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
    
    // 拦截器已经处理了 code !== 200 的情况，能到这里说明成功
    await adminAPI.approveApplication(row.id, remark || '');
    ElMessage.success('已通过认证申请');
    loadApplicationList();
    dialogVisible.value = false;
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error);
      // 错误消息由拦截器统一显示
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
    
    // 拦截器已经处理了 code !== 200 的情况，能到这里说明成功
    await adminAPI.rejectApplication(row.id, reason);
    ElMessage.success('已拒绝认证申请');
    loadApplicationList();
    dialogVisible.value = false;
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error);
      // 错误消息由拦截器统一显示
    }
  }
};

// 脱敏显示身份证号
const maskIdCard = (idCard) => {
  if (!idCard) return '-';
  return idCard.replace(/^(.{6}).*(.{4})$/, '$1****$2');
};

// 获取类型标签
const getTypeTag = (type) => {
  const typeMap = {
    'student': 'primary',
    'staff': 'success',
    'merchant': 'warning',
    'organization': 'info'
  };
  return typeMap[type] || 'info';
};

// 获取类型名称
const getTypeName = (type) => {
  const nameMap = {
    'student': '学生认证',
    'staff': '教职工认证',
    'merchant': '商户认证',
    'organization': '团体/部门认证'
  };
  return nameMap[type] || '未知类型';
};

// 获取状态标签
const getStatusTag = (status) => {
  const statusMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  };
  return statusMap[status] || '未知状态';
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

// 获取完整的图片 URL
const getFullImageUrl = (url) => {
  if (!url) return ''
  // 如果已经是完整 URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 否则拼接后端服务器地址
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  // 确保 baseUrl 不以 / 结尾
  const base = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  // 确保 url 以 / 开头
  const path = url.startsWith('/') ? url : '/' + url
  return `${base}${path}`
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
