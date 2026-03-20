<template>
  <div class="users-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          用户管理
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">用户管理</span>
            <span class="separator">/</span>
            <span class="current-page">用户列表</span>
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
        <el-form-item label="认证状态">
          <el-select v-model="searchForm.verified" placeholder="全部" clearable>
            <el-option label="已认证" :value="1" />
            <el-option label="未认证" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table 
        :data="userList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatarUrl" :size="50" />
              <div class="info">
                <div class="username">{{ row.username }}</div>
                <div class="realname">{{ row.realName || '未设置' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="college" label="学院" width="150" />
        <el-table-column label="认证状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isVerified === 1 ? 'success' : 'info'">
              {{ row.isVerified === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实名认证" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isRealNameVerified === 1 ? 'success' : 'info'">
              {{ row.isRealNameVerified === 1 ? '已实名' : '未实名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="用户详情"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户 ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentUser.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentUser.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentUser.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentUser.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phoneNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ currentUser.gender === 'male' ? '男' : currentUser.gender === 'female' ? '女' : '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ currentUser.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份认证">
          <el-tag :type="currentUser.isVerified === 1 ? 'success' : 'info'">
            {{ currentUser.isVerified === 1 ? '已认证' : '未认证' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实名认证">
          <el-tag :type="currentUser.isRealNameVerified === 1 ? 'success' : 'info'">
            {{ currentUser.isRealNameVerified === 1 ? '已实名' : '未实名' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ currentUser.createTime ? new Date(currentUser.createTime).toLocaleString() : '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { adminAPI } from '@/api/admin';

const searchForm = reactive({
  keyword: '',
  verified: null,
  status: null
});

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

const userList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const currentUser = ref(null);

// 加载用户列表
const loadUserList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    };
    const res = await adminAPI.getUserList(params);
    console.log('用户列表响应:', res);
    
    // 响应拦截器已经返回了 data，所以直接使用 res
    const data = res;
    console.log('解析后的数据:', data);
    console.log('records:', data?.records);
    console.log('total:', data?.total);
    
    if (res.code === 200 || data) {
      // 处理不同的数据结构
      const records = data?.records || data?.list || data || [];
      console.log('最终使用的数据:', records);
      console.log('是否为数组:', Array.isArray(records));
      userList.value = Array.isArray(records) ? records : [];
      pagination.total = data?.total ?? userList.value.length ?? 0;
      console.log('用户列表数据:', userList.value);
      console.log('分页总数:', pagination.total);
      if (userList.value.length === 0) {
        console.warn('数据为空！完整响应:', res);
      }
    } else {
      ElMessage.error(res.message || '加载用户列表失败');
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('🔍 handleSearch 触发，当前 pagination:', pagination);
  // 注意：不要重置 pagination.page，因为 v-model 会自动更新它
  loadUserList();
};

// 重置
const handleReset = () => {
  searchForm.keyword = '';
  searchForm.verified = null;
  searchForm.status = null;
  handleSearch();
};

// 查看详情
const handleViewDetail = (row) => {
  currentUser.value = row;
  dialogVisible.value = true;
};

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用';
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await adminAPI.updateUserStatus(row.id, row.status === 1 ? 0 : 1);
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

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await adminAPI.deleteUser(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadUserList();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

onMounted(() => {
  loadUserList();
});
</script>

<style scoped>
.users-management {
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
</style>
