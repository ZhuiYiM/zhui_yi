<template>
    <div class="product-tag-management">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>标签管理（商品标签）</span>
                    <el-button type="primary" @click="handleCreate">新增标签</el-button>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="关键词">
                    <el-input v-model="searchForm.keyword" placeholder="请输入标签名称" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
                        <el-option label="活跃" value="active" />
                        <el-option label="禁用" value="banned" />
                        <el-option label="待审核" value="pending" />
                    </el-select>
                </el-form-item>
                <el-form-item label="分类">
                    <el-input v-model="searchForm.category" placeholder="请输入分类" clearable style="width: 150px" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData">查询</el-button>
                    <el-button @click="handleReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格 -->
            <el-table :data="tableData" v-loading="loading" border>
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="标签名称" width="150" />
                <el-table-column prop="category" label="分类" width="120" />
                <el-table-column prop="usageCount" label="使用次数" width="100" />
                <el-table-column prop="trendScore" label="趋势分数" width="100">
                    <template #default="{ row }">
                        {{ row.trendScore ? row.trendScore.toFixed(2) : '0.00' }}
                    </template>
                </el-table-column>
                <el-table-column prop="lastUsedAt" label="最后使用时间" width="180">
                    <template #default="{ row }">
                        {{ formatDateTime(row.lastUsedAt) }}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag v-if="row.status === 'active'" type="success">活跃</el-tag>
                        <el-tag v-else-if="row.status === 'banned'" type="danger">禁用</el-tag>
                        <el-tag v-else type="warning">待审核</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ formatDateTime(row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                        <el-dropdown size="small">
                            <el-button size="small">
                                更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="handleToggleStatus(row, 'active')">设为活跃</el-dropdown-item>
                                    <el-dropdown-item @click="handleToggleStatus(row, 'banned')">设为禁用</el-dropdown-item>
                                    <el-dropdown-item @click="handleToggleStatus(row, 'pending')">设为待审核</el-dropdown-item>
                                    <el-dropdown-item divided @click="handleDelete(row)" style="color: #f56c6c">删除</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
                v-model:current-page="pagination.pageNum"
                v-model:page-size="pagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="pagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadData"
                @current-change="loadData"
                style="margin-top: 20px; justify-content: flex-end"
            />
        </el-card>

        <!-- 编辑对话框 -->
        <el-dialog v-model="dialogVisible" title="编辑标签" width="500px">
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-form-item label="标签名称" prop="name">
                    <el-input v-model="formData.name" disabled />
                </el-form-item>
                <el-form-item label="分类" prop="category">
                    <el-input v-model="formData.category" placeholder="请输入分类" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
                        <el-option label="活跃" value="active" />
                        <el-option label="禁用" value="banned" />
                        <el-option label="待审核" value="pending" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api/admin'

// 使用 adminAPI 中的商品标签方法
const tagAPI = {
    getProductTags: adminAPI.getProductTags,
    createProductTag: adminAPI.createProductTag,
    updateProductTag: adminAPI.updateProductTag,
    deleteProductTag: adminAPI.deleteProductTag,
    batchDeleteProductTags: adminAPI.batchDeleteProductTags,
    updateProductTagStatus: adminAPI.updateProductTagStatus
}

// 搜索表单
const searchForm = reactive({
    keyword: '',
    status: '',
    category: ''
})

// 分页
const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
    id: null,
    name: '',
    category: '',
    status: ''
})

// 表单验证规则
const formRules = {
    name: [
        { required: true, message: '请输入标签名称', trigger: 'blur' }
    ],
    status: [
        { required: true, message: '请选择状态', trigger: 'change' }
    ]
}

// 时间格式化
const formatDateTime = (time) => {
    if (!time) return '-'
    const date = new Date(time)
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    })
}

// 加载数据
const loadData = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize,
            ...searchForm
        }
        const res = await tagAPI.getProductTags(params)
        console.log('🏷️ 商品标签响应:', res)
        
        const pageData = res.data || res.result || res
        console.log('📦 分页数据:', pageData)
        
        tableData.value = pageData.records || []
        pagination.total = pageData.total || 0
    } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载数据失败')
    } finally {
        loading.value = false
    }
}

// 重置搜索
const handleReset = () => {
    searchForm.keyword = ''
    searchForm.status = ''
    searchForm.category = ''
    pagination.pageNum = 1
    loadData()
}

// 新增标签（四级标签通常由用户创建，管理员主要管理状态）
const handleCreate = () => {
    ElMessage.info('四级标签通常由用户创建，管理员主要负责审核和管理')
}

// 编辑标签
const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(formData, {
        id: row.id,
        name: row.name,
        category: row.category || '',
        status: row.status
    })
    dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row, status) => {
    const statusMap = {
        'active': '活跃',
        'banned': '禁用',
        'pending': '待审核'
    }
    
    try {
        await ElMessageBox.confirm(`确定要将标签状态改为${statusMap[status]}吗？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        const res = await tagAPI.updateProductTagStatus(row.id, status)
        if (res.code === 200 || res.success) {
            ElMessage.success('更新成功')
            loadData()
        } else {
            ElMessage.error(res.message || '更新失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

// 删除标签
const handleDelete = (row) => {
    ElMessageBox.confirm('确定要删除该标签吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const res = await tagAPI.deleteProductTag(row.id)
            if (res.code === 200 || res.success) {
                ElMessage.success('删除成功')
                loadData()
            } else {
                ElMessage.error(res.message || '删除失败')
            }
        } catch (error) {
            console.error('删除失败:', error)
            ElMessage.error('删除失败')
        }
    }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
        if (!valid) return
        
        submitLoading.value = true
        try {
            let res
            if (isEdit.value) {
                res = await tagAPI.updateProductTag(formData.id, formData)
            } else {
                // 四级标签通常由用户创建，管理员不直接创建
                ElMessage.warning('四级标签由用户创建，管理员仅负责审核和管理')
                submitLoading.value = false
                dialogVisible.value = false
                return
            }
            
            if (res.code === 200 || res.success) {
                ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
                dialogVisible.value = false
                loadData()
            } else {
                ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
            }
        } catch (error) {
            console.error('提交失败:', error)
            ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
        } finally {
            submitLoading.value = false
        }
    })
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.product-tag-management {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.search-form {
    margin-bottom: 20px;
}
</style>
