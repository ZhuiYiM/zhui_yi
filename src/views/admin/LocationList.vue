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
      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索地点名称或描述"
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
          style="width: 200px"
          @change="handleSearch"
        >
          <el-option label="主校区" :value="1" />
          <el-option label="分校区" :value="2" />
        </el-select>

        <el-select
          v-model="searchForm.category"
          placeholder="选择分类"
          clearable
          style="width: 200px"
          @change="handleSearch"
        >
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加地点
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

      <!-- 地点列表表格 -->
      <el-table
        v-loading="loading"
        :data="locationList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
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
              @click="handleEdit(row)"
            >
              编辑
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="locationFormRef"
        :model="locationForm"
        :rules="locationRules"
        label-width="120px"
      >
        <el-form-item label="地点名称" prop="name">
          <el-input v-model="locationForm.name" placeholder="请输入地点名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-input v-model="locationForm.category" placeholder="例如：教学楼、图书馆、食堂等" />
        </el-form-item>

        <el-form-item label="所属校区" prop="campusId">
          <el-radio-group v-model="locationForm.campusId">
            <el-radio :label="1">主校区</el-radio>
            <el-radio :label="2">分校区</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="地点描述" prop="description">
          <el-input
            v-model="locationForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入地点描述"
          />
        </el-form-item>

        <el-form-item label="经纬度" prop="latitude">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-input
                v-model="locationForm.latitude"
                placeholder="纬度"
                type="number"
                step="0.000001"
              >
                <template #prepend>纬度</template>
              </el-input>
            </el-col>
            <el-col :span="12">
              <el-input
                v-model="locationForm.longitude"
                placeholder="经度"
                type="number"
                step="0.000001"
              >
                <template #prepend>经度</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input v-model="locationForm.icon" placeholder="请输入图标 URL 或图标类名" />
        </el-form-item>

        <el-form-item label="图片" prop="imageUrl">
          <el-input v-model="locationForm.imageUrl" placeholder="请输入图片 URL" />
        </el-form-item>

        <el-form-item label="开放时间" prop="openingHours">
          <el-input v-model="locationForm.openingHours" placeholder="例如：08:00-22:00" />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="locationForm.contactInfo" placeholder="例如：电话、邮箱等" />
        </el-form-item>

        <el-form-item label="设施标签" prop="facilities">
          <el-input
            v-model="locationForm.facilities"
            type="textarea"
            :rows="2"
            placeholder="JSON 格式，例如：[&quot;WiFi&quot;,&quot;空调&quot;]"
          />
        </el-form-item>

        <el-form-item label="是否热门" prop="isPopular">
          <el-switch v-model="locationForm.isPopular" />
        </el-form-item>

        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="locationForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Location, Search, Plus, Delete, HomeFilled } from '@element-plus/icons-vue';
import request from '@/api/request';

const loading = ref(false);
const submitting = ref(false);
const locationList = ref([]);
const categories = ref([]);
const selectedIds = ref([]);

const searchForm = reactive({
  keyword: '',
  campusId: null,
  category: null
});

const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加地点');
const locationFormRef = ref(null);

const locationForm = reactive({
  id: null,
  name: '',
  category: '',
  campusId: 1,
  description: '',
  latitude: null,
  longitude: null,
  icon: '',
  imageUrl: '',
  openingHours: '',
  contactInfo: '',
  facilities: '',
  isPopular: false,
  viewCount: 0,
  sortOrder: 0
});

const locationRules = {
  name: [
    { required: true, message: '请输入地点名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入分类', trigger: 'blur' }
  ],
  campusId: [
    { required: true, message: '请选择校区', trigger: 'change' }
  ]
};

// 加载地点列表
const loadLocations = async () => {
  loading.value = true;
  console.log('📦 准备加载地点列表，pagination:', pagination);
  try {
    const response = await request.get('/admin/locations/list', {
      params: {
        page: pagination.page,
        limit: pagination.limit,
        ...searchForm
      }
    });
    
    console.log('📍 地点列表响应:', response);
    console.log('📍 返回的数据结构:', response);
    console.log('📍 response.list:', response?.list);
    console.log('📍 response.total:', response?.total);
    
    if (response.code === 200 || response.list !== undefined) {
      // 响应拦截器已经返回了 data，直接访问 list 和 total
      locationList.value = response.list || [];
      pagination.total = response.total || 0;
      console.log('✅ 地点列表加载成功:', locationList.value.length, '条记录');
    } else {
      ElMessage.error(response.message || '加载失败');
    }
  } catch (error) {
    console.error('❌ 加载地点列表失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await request.get('/admin/locations/categories');
    if (response.code === 200) {
      categories.value = response.data;
    }
  } catch (error) {
    console.error('加载分类失败:', error);
  }
};

// 搜索
const handleSearch = () => {
  console.log('🔍 handleSearch 触发，当前 pagination:', pagination);
  // 注意：不要重置 pagination.page，因为 v-model 会自动更新它
  loadLocations();
};

// 添加地点
const handleAdd = () => {
  dialogTitle.value = '添加地点';
  dialogVisible.value = true;
};

// 编辑地点
const handleEdit = (row) => {
  dialogTitle.value = '编辑地点';
  Object.assign(locationForm, row);
  dialogVisible.value = true;
};

// 删除地点
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该地点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await request.delete(`/admin/locations/${row.id}`);
      if (response.code === 200) {
        ElMessage.success('删除成功');
        loadLocations();
      } else {
        ElMessage.error(response.message || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  }).catch(() => {});
};

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个地点吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await request.delete('/admin/locations/batch', {
        data: { ids: selectedIds.value }
      });
      if (response.code === 200) {
        ElMessage.success('批量删除成功');
        loadLocations();
      } else {
        ElMessage.error(response.message || '删除失败');
      }
    } catch (error) {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败，请稍后重试');
    }
  }).catch(() => {});
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
};

// 提交表单
const handleSubmit = async () => {
  if (!locationFormRef.value) return;
  
  await locationFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        let response;
        if (locationForm.id) {
          // 更新
          response = await request.put(`/admin/locations/${locationForm.id}`, locationForm);
        } else {
          // 添加
          response = await request.post('/admin/locations', locationForm);
        }
        
        if (response.code === 200) {
          ElMessage.success(locationForm.id ? '更新成功' : '添加成功');
          dialogVisible.value = false;
          loadLocations();
        } else {
          ElMessage.error(response.message || '操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error('操作失败，请稍后重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 关闭对话框
const handleDialogClose = () => {
  locationFormRef.value?.resetFields();
  Object.keys(locationForm).forEach(key => {
    locationForm[key] = key === 'campusId' ? 1 : (key === 'isPopular' ? false : (key === 'viewCount' || key === 'sortOrder' ? 0 : ''));
  });
};

onMounted(() => {
  loadLocations();
  loadCategories();
});
</script>

<style scoped>
.location-management {
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

.location-card {
  border-radius: 12px;
}

.search-bar {
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
