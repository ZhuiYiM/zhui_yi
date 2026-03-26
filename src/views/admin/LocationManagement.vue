<template>
  <div class="location-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Location /></el-icon>
          地点管理
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
            <span class="current-page">地点管理</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="location-card">
      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="系统地点" name="system">
          <!-- 系统地点管理 -->
          <div class="search-bar">
            <el-input
              v-model="systemSearchForm.keyword"
              placeholder="搜索地点名称或描述"
              clearable
              style="width: 300px"
              @clear="handleSystemSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select
              v-model="systemSearchForm.campusId"
              placeholder="选择校区"
              clearable
              style="width: 200px"
              @change="handleSystemSearch"
            >
              <el-option label="主校区" :value="1" />
              <el-option label="分校区" :value="2" />
            </el-select>

            <el-select
              v-model="systemSearchForm.category"
              placeholder="选择分类"
              clearable
              style="width: 200px"
              @change="handleSystemSearch"
            >
              <el-option
                v-for="cat in systemCategories"
                :key="cat"
                :label="cat"
                :value="cat"
              />
            </el-select>

            <el-button type="primary" @click="handleSystemSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>

            <el-button type="success" @click="handleSystemAdd">
              <el-icon><Plus /></el-icon>
              添加地点
            </el-button>

            <el-button
              type="danger"
              :disabled="systemSelectedIds.length === 0"
              @click="handleSystemBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>

          <!-- 系统地点列表表格 -->
          <el-table
            v-loading="systemLoading"
            :data="systemLocationList"
            border
            style="width: 100%"
            @selection-change="handleSystemSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="地点名称" min-width="180" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="campusId" label="校区" width="100">
              <template #default="{ row }">
                <el-tag :type="row.campusId === 1 ? 'primary' : 'success'">
                  {{ row.campusId === 1 ? '主校区' : '分校区' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isPopular" label="热门" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isPopular ? 'warning' : 'info'">
                  {{ row.isPopular ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="100" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleSystemEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleSystemDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="systemPagination.page"
              v-model:page-size="systemPagination.limit"
              :page-sizes="[10, 20, 50, 100]"
              :total="systemPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSystemSearch"
              @current-change="handleSystemPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户地点标记" name="user">
          <!-- 用户地点标记管理 -->
          <div class="search-bar">
            <el-input
              v-model="userSearchForm.keyword"
              placeholder="搜索地点名称、描述或地址"
              clearable
              style="width: 300px"
              @clear="handleUserSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select
              v-model="userSearchForm.campusId"
              placeholder="选择校区"
              clearable
              style="width: 150px"
              @change="handleUserSearch"
            >
              <el-option label="主校区" :value="1" />
              <el-option label="分校区" :value="2" />
            </el-select>

            <el-select
              v-model="userSearchForm.markType"
              placeholder="选择标记类型"
              clearable
              style="width: 150px"
              @change="handleUserSearch"
            >
              <el-option
                v-for="type in userMarkTypes"
                :key="type"
                :label="getUserMarkTypeName(type)"
                :value="type"
              />
            </el-select>

            <el-select
              v-model="userSearchForm.verificationStatus"
              placeholder="选择审核状态"
              clearable
              style="width: 150px"
              @change="handleUserSearch"
            >
              <el-option label="待审核" value="pending" />
              <el-option label="审核通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>

            <el-button type="primary" @click="handleUserSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>

            <el-button
              type="danger"
              :disabled="userSelectedIds.length === 0"
              @click="handleUserBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>

          <!-- 用户地点标记列表表格 -->
          <el-table
            v-loading="userLoading"
            :data="userLocationMarkList"
            border
            style="width: 100%"
            @selection-change="handleUserSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="locationName" label="地点名称" min-width="180" />
            <el-table-column prop="markType" label="标记类型" width="140">
              <template #default="{ row }">
                <el-tag :type="getUserMarkTypeColor(row.markType)">
                  {{ getUserMarkTypeName(row.markType) }}
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
                  @click="handleUserApprove(row)"
                >
                  通过
                </el-button>
                <el-button
                  v-if="row.verificationStatus === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleUserReject(row)"
                >
                  拒绝
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="handleUserViewDetail(row)"
                >
                  详情
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleUserDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="userPagination.page"
              v-model:page-size="userPagination.limit"
              :page-sizes="[10, 20, 50, 100]"
              :total="userPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleUserSearch"
              @current-change="handleUserPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 系统地点：添加/编辑对话框 -->
    <el-dialog
      v-model="systemDialogVisible"
      :title="systemDialogTitle"
      width="800px"
      @close="handleSystemDialogClose"
    >
      <el-form
        ref="systemLocationFormRef"
        :model="systemLocationForm"
        :rules="systemLocationRules"
        label-width="120px"
      >
        <el-form-item label="地点名称" prop="name">
          <el-input v-model="systemLocationForm.name" placeholder="请输入地点名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-input v-model="systemLocationForm.category" placeholder="例如：教学楼、图书馆、食堂等" />
        </el-form-item>

        <el-form-item label="所属校区" prop="campusId">
          <el-radio-group v-model="systemLocationForm.campusId">
            <el-radio :label="1">主校区</el-radio>
            <el-radio :label="2">分校区</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="地点描述" prop="description">
          <el-input
            v-model="systemLocationForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入地点描述"
          />
        </el-form-item>

        <el-form-item label="经纬度">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-input
                v-model="systemLocationForm.latitude"
                placeholder="纬度"
                type="number"
                step="0.000001"
              >
                <template #prepend>纬度</template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <el-input
                v-model="systemLocationForm.longitude"
                placeholder="经度"
                type="number"
                step="0.000001"
              >
                <template #prepend>经度</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="设为热门">
          <el-switch v-model="systemLocationForm.isPopular" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="systemLocationForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="systemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSystemLocation" :loading="systemSubmitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- 用户地点标记：审核对话框 -->
    <el-dialog
      v-model="userReviewDialogVisible"
      :title="userReviewTitle"
      width="500px"
    >
      <el-form :model="userReviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="userReviewForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userReviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserReview">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户地点标记：详情对话框 -->
    <el-dialog
      v-model="userDetailDialogVisible"
      title="地点标记详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentUserMark">
        <el-descriptions-item label="地点名称">{{ currentUserMark.locationName }}</el-descriptions-item>
        <el-descriptions-item label="标记类型">{{ getUserMarkTypeName(currentUserMark.markType) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentUserMark.markCategory }}</el-descriptions-item>
        <el-descriptions-item label="校区">{{ currentUserMark.campusId === 1 ? '主校区' : '分校区' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getVerificationStatusColor(currentUserMark.verificationStatus)">
            {{ getVerificationStatusName(currentUserMark.verificationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="可见性">{{ currentUserMark.visibility }}</el-descriptions-item>
        <el-descriptions-item label="用户身份">
          <el-tag v-if="currentUserMark.userIdentityType" type="success">
            {{ getUserIdentityName(currentUserMark.userIdentityType) }}
          </el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentUserMark.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{ currentUserMark.addressDetail }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentUserMark.description }}</el-descriptions-item>
        <el-descriptions-item label="联系方式" :span="2">{{ currentUserMark.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="图片" :span="2">
          <div v-if="currentUserMark.images && currentUserMark.images.length > 0" style="display: flex; gap: 10px;">
            <el-image
              v-for="(img, index) in currentUserMark.images"
              :key="index"
              :src="img"
              :preview-src-list="currentUserMark.images"
              :initial-index="index"
              fit="cover"
              style="width: 100px; height: 100px; cursor: pointer;"
            />
          </div>
          <div v-else>无图片</div>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentUserMark.reviewComment || '-' }}</el-descriptions-item>
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
const activeTab = ref('system');

// ========== 系统地点相关 ==========
const systemLocationList = ref([]);
const systemLoading = ref(false);
const systemSelectedIds = ref([]);
const systemCategories = ref([]);
const systemDialogVisible = ref(false);
const systemDialogTitle = ref('');
const systemSubmitting = ref(false);
const systemLocationFormRef = ref(null);

const systemPagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const systemSearchForm = reactive({
  keyword: '',
  campusId: null,
  category: null
});

const systemLocationForm = reactive({
  id: null,
  name: '',
  category: '',
  campusId: 1,
  description: '',
  latitude: '',
  longitude: '',
  isPopular: false,
  sortOrder: 0
});

const systemLocationRules = {
  name: [{ required: true, message: '请输入地点名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }]
};

// ========== 用户地点标记相关 ==========
const userLocationMarkList = ref([]);
const userLoading = ref(false);
const userSelectedIds = ref([]);
const userMarkTypes = ref([]);
const userReviewDialogVisible = ref(false);
const userDetailDialogVisible = ref(false);
const userReviewTitle = ref('');
const currentUserMark = ref(null);

const userPagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const userSearchForm = reactive({
  keyword: '',
  campusId: null,
  markType: null,
  verificationStatus: ''
});

const userReviewForm = reactive({
  markId: null,
  status: '',
  reason: ''
});

// ========== 系统地点方法 ==========
const loadSystemData = async () => {
  systemLoading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('page', systemPagination.page);
    params.append('limit', systemPagination.limit);
    
    if (systemSearchForm.keyword) params.append('keyword', systemSearchForm.keyword);
    if (systemSearchForm.campusId) params.append('campusId', systemSearchForm.campusId);
    if (systemSearchForm.category) params.append('category', systemSearchForm.category);

    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/locations/list?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      systemLocationList.value = response.data.data.list;
      systemPagination.total = response.data.data.total;
    } else {
      ElMessage.error(response.data.message || '加载失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败：' + (error.message || '网络错误'));
  } finally {
    systemLoading.value = false;
  }
};

const loadSystemCategories = async () => {
  try {
    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/locations/categories`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      systemCategories.value = response.data.data;
    }
  } catch (error) {
    console.error('加载分类失败:', error);
  }
};

const handleSystemSearch = () => {
  systemPagination.page = 1;
  loadSystemData();
};

const handleSystemPageChange = () => {
  // 分页变化时，直接使用当前页码，不重置
  loadSystemData();
};

const handleSystemSelectionChange = (selection) => {
  systemSelectedIds.value = selection.map(row => row.id);
};

const handleSystemAdd = () => {
  systemDialogTitle.value = '添加地点';
  systemDialogVisible.value = true;
};

const handleSystemEdit = (row) => {
  systemDialogTitle.value = '编辑地点';
  systemLocationForm.id = row.id;
  systemLocationForm.name = row.name;
  systemLocationForm.category = row.category;
  systemLocationForm.campusId = row.campusId;
  systemLocationForm.description = row.description;
  systemLocationForm.latitude = row.latitude?.toString() || '';
  systemLocationForm.longitude = row.longitude?.toString() || '';
  systemLocationForm.isPopular = row.isPopular;
  systemLocationForm.sortOrder = row.sortOrder;
  systemDialogVisible.value = true;
};

const handleSystemDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除地点"${row.name}"吗？`,
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
        `${API_BASE_URL}/api/admin/locations/${row.id}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success('删除成功');
        loadSystemData();
      } else {
        ElMessage.error(response.data.message || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage.error('删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

const handleSystemBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${systemSelectedIds.value.length} 个地点吗？`,
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
        `${API_BASE_URL}/api/admin/locations/batch`,
        {
          data: { ids: systemSelectedIds.value },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success('批量删除成功');
        systemSelectedIds.value = [];
        loadSystemData();
      } else {
        ElMessage.error(response.data.message || '批量删除失败');
      }
    } catch (error) {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

const submitSystemLocation = async () => {
  if (!systemLocationFormRef.value) return;
  
  await systemLocationFormRef.value.validate(async (valid) => {
    if (!valid) return;

    systemSubmitting.value = true;
    try {
      const token = localStorage.getItem('admin_token');
      const url = systemLocationForm.id 
        ? `${API_BASE_URL}/api/admin/locations/${systemLocationForm.id}`
        : `${API_BASE_URL}/api/admin/locations`;
      
      const method = systemLocationForm.id ? 'put' : 'post';
      const response = await axios[method](
        url,
        systemLocationForm,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success(systemLocationForm.id ? '更新成功' : '添加成功');
        systemDialogVisible.value = false;
        loadSystemData();
      } else {
        ElMessage.error(response.data.message || '操作失败');
      }
    } catch (error) {
      console.error('提交失败:', error);
      ElMessage.error('提交失败：' + (error.response?.data?.message || '网络错误'));
    } finally {
      systemSubmitting.value = false;
    }
  });
};

const handleSystemDialogClose = () => {
  systemLocationFormRef.value?.resetFields();
  systemLocationForm.id = null;
  systemLocationForm.name = '';
  systemLocationForm.category = '';
  systemLocationForm.campusId = 1;
  systemLocationForm.description = '';
  systemLocationForm.latitude = '';
  systemLocationForm.longitude = '';
  systemLocationForm.isPopular = false;
  systemLocationForm.sortOrder = 0;
};

// ========== 用户地点标记方法 ==========
const loadUserData = async () => {
  userLoading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('page', userPagination.page);
    params.append('limit', userPagination.limit);
    
    if (userSearchForm.keyword) params.append('keyword', userSearchForm.keyword);
    if (userSearchForm.campusId) params.append('campusId', userSearchForm.campusId);
    if (userSearchForm.markType) params.append('markType', userSearchForm.markType);
    if (userSearchForm.verificationStatus) params.append('verificationStatus', userSearchForm.verificationStatus);

    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/user-location-marks/list?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      userLocationMarkList.value = response.data.data.list;
      userPagination.total = response.data.data.total;
    } else {
      ElMessage.error(response.data.message || '加载失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败：' + (error.message || '网络错误'));
  } finally {
    userLoading.value = false;
  }
};

const loadUserMarkTypes = async () => {
  try {
    const token = localStorage.getItem('admin_token');
    const response = await axios.get(`${API_BASE_URL}/api/admin/user-location-marks/mark-types`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 200) {
      userMarkTypes.value = response.data.data;
    }
  } catch (error) {
    console.error('加载标记类型失败:', error);
  }
};

const handleUserSearch = () => {
  userPagination.page = 1;
  loadUserData();
};

const handleUserPageChange = () => {
  // 分页变化时，直接使用当前页码，不重置
  loadUserData();
};

const handleUserSelectionChange = (selection) => {
  userSelectedIds.value = selection.map(row => row.id);
};

const getUserMarkTypeName = (type) => {
  const names = {
    meeting_point: '约见地点',
    merchant_shop: '店铺位置',
    organization_activity: '活动地点',
    other: '其他'
  };
  return names[type] || type;
};

const getUserMarkTypeColor = (type) => {
  const colors = {
    meeting_point: 'primary',
    merchant_shop: 'warning',
    organization_activity: 'success',
    other: 'info'
  };
  return colors[type] || 'info';
};

const getVerificationStatusName = (status) => {
  const names = {
    pending: '待审核',
    approved: '审核通过',
    rejected: '已拒绝'
  };
  return names[status] || status;
};

const getVerificationStatusColor = (status) => {
  const colors = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  };
  return colors[status] || 'info';
};

const getUserIdentityName = (type) => {
  const names = {
    student: '学生',
    staff: '教职工',
    merchant: '商户',
    organization: '组织'
  };
  return names[type] || type;
};

const handleUserApprove = (row) => {
  userReviewForm.markId = row.id;
  userReviewForm.status = 'approved';
  userReviewForm.reason = '';
  userReviewTitle.value = '通过审核';
  userReviewDialogVisible.value = true;
};

const handleUserReject = (row) => {
  userReviewForm.markId = row.id;
  userReviewForm.status = 'rejected';
  userReviewForm.reason = '';
  userReviewTitle.value = '拒绝审核';
  userReviewDialogVisible.value = true;
};

const submitUserReview = async () => {
  try {
    const token = localStorage.getItem('admin_token');
    const params = new URLSearchParams();
    params.append('status', userReviewForm.status);
    if (userReviewForm.reason) params.append('reason', userReviewForm.reason);

    const response = await axios.post(
      `${API_BASE_URL}/api/admin/user-location-marks/${userReviewForm.markId}/review?${params.toString()}`,
      {},
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.data.code === 200) {
      ElMessage.success('审核成功');
      userReviewDialogVisible.value = false;
      loadUserData();
    } else {
      ElMessage.error(response.data.message || '审核失败');
    }
  } catch (error) {
    console.error('审核失败:', error);
    ElMessage.error('审核失败：' + (error.response?.data?.message || '网络错误'));
  }
};

const handleUserViewDetail = (row) => {
  currentUserMark.value = row;
  userDetailDialogVisible.value = true;
};

const handleUserDelete = (row) => {
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
        loadUserData();
      } else {
        ElMessage.error(response.data.message || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage.error('删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

const handleUserBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${userSelectedIds.value.length} 个标记吗？`,
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
          data: { ids: userSelectedIds.value },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.data.code === 200) {
        ElMessage.success('批量删除成功');
        userSelectedIds.value = [];
        loadUserData();
      } else {
        ElMessage.error(response.data.message || '批量删除失败');
      }
    } catch (error) {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败：' + (error.response?.data?.message || '网络错误'));
    }
  }).catch(() => {});
};

// ========== 标签页切换 ==========
const handleTabChange = (tabName) => {
  if (tabName === 'system') {
    loadSystemData();
  } else if (tabName === 'user') {
    loadUserData();
  }
};

onMounted(() => {
  loadSystemData();
  loadSystemCategories();
  loadUserMarkTypes();
});
</script>

<style scoped>
.location-management {
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

.location-card {
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
