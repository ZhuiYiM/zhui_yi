<template>
  <div class="identity-verification-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>身份认证审核</span>
          <el-input
            v-model="searchText"
            placeholder="搜索用户名或申请类型"
            style="width: 300px"
            clearable
            @input="loadVerifications"
          />
        </div>
      </template>

      <el-table
        :data="verifications"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="identityType" label="申请类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getIdentityTypeTag(row.identityType)">
              {{ getIdentityTypeName(row.identityType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="identityName" label="身份名称" min-width="150" />
        <el-table-column prop="verified" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.verified === 1 ? 'success' : 'warning'">
              {{ row.verified === 1 ? '已通过' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="extraInfo" label="详细信息" min-width="200">
          <template #default="{ row }">
            <el-button type="text" @click="showExtraInfo(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.verified !== 1"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.verified !== 1"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.verified === 1"
              type="warning"
              size="small"
              @click="handleRevoke(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadVerifications"
          @current-change="loadVerifications"
        />
      </div>
    </el-card>

    <!-- 详细信息对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="认证详情"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="currentVerification">
        <el-descriptions-item label="用户名">
          {{ currentVerification.username }}
        </el-descriptions-item>
        <el-descriptions-item label="身份类型">
          {{ getIdentityTypeName(currentVerification.identityType) }}
        </el-descriptions-item>
        <el-descriptions-item label="身份名称">
          {{ currentVerification.identityName }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(currentVerification.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="详细信息">
          <pre>{{ formatExtraInfo(currentVerification.extraInfo) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewAction === 'approve' ? '通过认证' : '拒绝认证'"
      width="500px"
    >
      <el-form ref="reviewFormRef" :model="reviewForm" label-width="80px">
        <el-alert
          :title="reviewAction === 'approve' ? '确认通过该认证申请吗？' : '请输入拒绝理由'"
          :type="reviewAction === 'approve' ? 'success' : 'warning'"
          :closable="false"
          style="margin-bottom: 20px"
        />
        <el-form-item
          v-if="reviewAction === 'reject'"
          label="拒绝理由"
          prop="reason"
        >
          <el-input
            v-model="reviewForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝理由（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmReview"
          :loading="reviewing"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const loading = ref(false)
const reviewing = ref(false)
const verifications = ref([])
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentVerification = ref(null)
const reviewAction = ref('approve') // 'approve' or 'reject'
const reviewForm = reactive({
  reason: ''
})

// 加载认证列表
const loadVerifications = async () => {
  loading.value = true
  try {
    // TODO: 调用后端 API
    // const response = await adminAPI.getIdentityVerifications({
    //   page: currentPage.value,
    //   size: pageSize.value,
    //   search: searchText.value
    // })
    // verifications.value = response.records
    // total.value = response.total
    
    // 模拟数据
    verifications.value = [
      {
        id: 1,
        username: 'zhangsan',
        identityType: 'merchant',
        identityName: '商户',
        verified: 0,
        extraInfo: JSON.stringify({ shopName: '张三小店', businessLicenseUrl: '/uploads/license.jpg' }, null, 2),
        createdAt: '2024-01-15T10:30:00'
      },
      {
        id: 2,
        username: 'lisi',
        identityType: 'staff',
        identityName: '教职工',
        verified: 1,
        extraInfo: JSON.stringify({ staffId: '2024001', department: '计算机学院' }, null, 2),
        createdAt: '2024-01-14T09:20:00'
      }
    ]
    total.value = 2
  } catch (error) {
    console.error('加载认证列表失败:', error)
    ElMessage.error('加载认证列表失败')
  } finally {
    loading.value = false
  }
}

// 显示详细信息
const showExtraInfo = (row) => {
  currentVerification.value = row
  detailDialogVisible.value = true
}

// 格式化额外信息
const formatExtraInfo = (extraInfo) => {
  try {
    if (typeof extraInfo === 'string') {
      return JSON.parse(extraInfo)
    }
    return extraInfo
  } catch (e) {
    return extraInfo || '无'
  }
}

// 处理通过
const handleApprove = (row) => {
  currentVerification.value = row
  reviewAction.value = 'approve'
  reviewDialogVisible.value = true
}

// 处理拒绝
const handleReject = (row) => {
  currentVerification.value = row
  reviewAction.value = 'reject'
  reviewDialogVisible.value = true
}

// 处理撤销
const handleRevoke = (row) => {
  ElMessageBox.confirm(
    '确认撤销该用户的认证吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // TODO: 调用后端 API
      // await adminAPI.revokeIdentity(row.id)
      ElMessage.success('已撤销认证')
      loadVerifications()
    } catch (error) {
      console.error('撤销认证失败:', error)
      ElMessage.error('撤销认证失败')
    }
  }).catch(() => {})
}

// 确认审核
const confirmReview = async () => {
  reviewing.value = true
  try {
    const response = await adminAPI.verifyIdentity(currentVerification.value.id, {
      approved: reviewAction.value === 'approve',
      reason: reviewForm.reason
    })
    
    // 检查是否需要强制重新登录
    if (reviewAction.value === 'approve' && response.data && response.data.force_relogin) {
      ElMessageBox.alert(
        '身份认证已通过，用户需要重新登录才能更新身份信息',
        '审核成功',
        {
          confirmButtonText: '确定',
          type: 'success'
        }
      )
    } else {
      ElMessage.success(reviewAction.value === 'approve' ? '已通过认证' : '已拒绝认证')
    }
    
    reviewDialogVisible.value = false
    loadVerifications()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '审核失败')
  } finally {
    reviewing.value = false
  }
}

// 获取身份类型名称
const getIdentityTypeName = (type) => {
  const nameMap = {
    student: '学生',
    staff: '教职工',
    merchant: '商户',
    organization: '团体/部门'
  }
  return nameMap[type] || type
}

// 获取身份类型标签颜色
const getIdentityTypeTag = (type) => {
  const tagMap = {
    student: '',
    staff: 'success',
    merchant: 'warning',
    organization: 'info'
  }
  return tagMap[type] || ''
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadVerifications()
})
</script>

<style scoped>
.identity-verification-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
