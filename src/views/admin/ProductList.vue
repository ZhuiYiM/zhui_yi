<template>
  <div class="product-management">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Shop /></el-icon>
          商品管理
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
            <span class="current-page">商品管理</span>
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
            placeholder="请输入商品标题或描述"
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
          type="warning" 
          size="small" 
          @click="handleBatchRemove" 
          :disabled="selectedIds.length === 0"
        >
          批量下架
        </el-button>
        <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table 
        :data="productList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="商品标题" min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="卖家 ID" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : (row.status === 0 ? 'info' : 'warning')">
              {{ row.status === 1 ? '在售' : (row.status === 0 ? '下架' : '已售') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览数" width="80" />
        <el-table-column prop="favoriteCount" label="收藏数" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
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
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
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

    <!-- 查看商品详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="商品详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentProduct">
        <el-descriptions-item label="商品 ID">{{ currentProduct.id }}</el-descriptions-item>
        <el-descriptions-item label="商品标题">{{ currentProduct.title }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentProduct.price }}</el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ currentProduct.originalPrice }}</el-descriptions-item>
        <el-descriptions-item label="卖家 ID">{{ currentProduct.sellerId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct.status === 1 ? 'success' : (currentProduct.status === 0 ? 'info' : 'warning')">
            {{ currentProduct.status === 1 ? '在售' : (currentProduct.status === 0 ? '下架' : '已售') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览数">{{ currentProduct.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="收藏数">{{ currentProduct.favoriteCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentProduct.createdAt ? new Date(currentProduct.createdAt).toLocaleString() : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="商品描述" :span="2">
          {{ currentProduct.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系方式" :span="2">
          {{ currentProduct.contactInfo || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="交易地点" :span="2">
          {{ currentProduct.location || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Shop, HomeFilled, Search, Refresh } from '@element-plus/icons-vue';
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

const productList = ref([]);
const loading = ref(false);

// 批量操作
const selectAll = ref(false);
const isIndeterminate = ref(false);
const selectedIds = ref([]);
const selectedRows = ref([]);

// 查看商品对话框
const viewDialogVisible = ref(false);
const currentProduct = ref(null);

// 加载商品列表
const loadProductList = async () => {
  loading.value = true;
  console.log('📦 准备加载商品列表，pagination:', pagination);
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword
    };
    const res = await adminAPI.getProductList(params);
    console.log('商品列表响应:', res);
    
    const data = res;
    if (res.code === 200 || data) {
      const records = data?.records || data?.list || data || [];
      productList.value = Array.isArray(records) ? records : [];
      pagination.total = data?.total ?? productList.value.length ?? 0;
      console.log('✅ 商品列表加载成功:', productList.value.length, '条记录');
    } else {
      ElMessage.error(res.message || '加载商品列表失败');
    }
  } catch (error) {
    console.error('加载商品列表失败:', error);
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('🔍 handleSearch 触发，当前 pagination:', pagination);
  // 注意：不要重置 pagination.page，因为 v-model 会自动更新它
  loadProductList();
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
  selectAll.value = selection.length === productList.value.length;
  isIndeterminate.value = selection.length > 0 && selection.length < productList.value.length;
};

// 全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectAll.value = true;
    selectedIds.value = productList.value.map(item => item.id);
    selectedRows.value = productList.value;
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
    ElMessage.warning('请选择要删除的商品');
    return;
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个商品吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const promises = selectedIds.value.map(id => adminAPI.deleteProduct(id));
    await Promise.all(promises);
    
    ElMessage.success('批量删除成功');
    selectedIds.value = [];
    selectedRows.value = [];
    selectAll.value = false;
    loadProductList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败');
    }
  } finally {
    loading.value = false;
  }
};

// 批量下架
const handleBatchRemove = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要下架的商品');
    return;
  }
  
  try {
    await ElMessageBox.confirm(`确定要下架选中的 ${selectedIds.value.length} 个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const promises = selectedRows.value
      .filter(row => row.status === 1)
      .map(row => adminAPI.removeProduct(row.id));
    
    if (promises.length > 0) {
      await Promise.all(promises);
    }
    
    ElMessage.success('批量下架成功');
    selectedIds.value = [];
    selectedRows.value = [];
    selectAll.value = false;
    loadProductList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量下架失败:', error);
      ElMessage.error('批量下架失败');
    }
  } finally {
    loading.value = false;
  }
};

// 查看商品详情
const handleView = (row) => {
  currentProduct.value = row;
  viewDialogVisible.value = true;
};

// 切换商品状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '下架' : '上架';
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const newStatus = row.status === 1 ? 0 : 1;
    const res = await adminAPI.updateProductStatus(row.id, newStatus);
    if (res.code === 200) {
      ElMessage.success(`${action}成功`);
      row.status = newStatus;
    } else {
      ElMessage.error(res.message || `${action}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error);
    }
  }
};

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await adminAPI.deleteProduct(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadProductList();
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
  loadProductList();
});
</script>

<style scoped>
.product-management {
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
  margin-bottom: 12px;
}

.batch-operations {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid #e0e0e0;
}

.selected-count {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
}

.table-card {
  border-radius: 12px;
}

.price-text {
  color: #F56C6C;
  font-weight: bold;
  font-size: 14px;
}
</style>
