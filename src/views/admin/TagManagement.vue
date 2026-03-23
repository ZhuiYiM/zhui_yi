<template>
    <div class="tag-management">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <div style="display: flex; align-items: center; gap: 12px">
                        <span>标签管理</span>
                        <el-select v-model="currentLevel" placeholder="选择标签级别" size="default" style="width: 150px" @change="handleLevelChange">
                            <el-option label="二级标签 - 分类" value="level2" />
                            <el-option label="三级标签 - 地点" value="level3" />
                            <el-option label="四级标签 - 自定义" value="level4" />
                        </el-select>
                    </div>
                    <el-button type="primary" @click="handleCreate">新增标签</el-button>
                </div>
            </template>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="关键词">
                    <el-input v-model="searchForm.keyword" placeholder="请输入标签名称或代码" clearable />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="searchForm.isActive" placeholder="请选择状态" clearable>
                        <el-option label="启用" :value="true" />
                        <el-option label="禁用" :value="false" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData">查询</el-button>
                    <el-button @click="handleReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 标签列表 -->
            <el-table 
                v-loading="loading"
                :data="tableData" 
                border 
                stripe
                style="width: 100%">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="code" label="标签代码" width="150" />
                <el-table-column prop="name" label="标签名称" min-width="150" show-overflow-tooltip />
                <el-table-column prop="icon" label="图标" width="120">
                    <template #default="{ row }">
                        <span v-if="row.icon">{{ row.icon }}</span>
                        <span v-else>-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="color" label="颜色" width="100">
                    <template #default="{ row }">
                        <div v-if="row.color" :style="{ 
                            width: '30px', 
                            height: '30px', 
                            backgroundColor: row.color, 
                            borderRadius: '3px',
                            border: '1px solid #ddd'
                        }"></div>
                        <span v-else>-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" width="80" />
                <el-table-column prop="isActive" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.isActive ? 'success' : 'danger'">
                            {{ row.isActive ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="300" fixed="right">
                    <template #default="{ row }">
                        <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                        <el-button 
                            size="small" 
                            :type="row.isActive ? 'warning' : 'success'"
                            @click="handleToggleStatus(row)">
                            {{ row.isActive ? '禁用' : '启用' }}
                        </el-button>
                        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
                style="margin-top: 20px; justify-content: flex-end;"
            />
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog
            v-model="dialogVisible"
            :title="isEdit ? '编辑标签' : '新增标签'"
            width="500px"
            @close="handleDialogClose">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px">
                <el-form-item label="标签代码" prop="code">
                    <el-input v-model="formData.code" placeholder="请输入标签代码（如：building）" maxlength="50" :disabled="isEdit" />
                </el-form-item>
                <el-form-item label="标签名称" prop="name">
                    <el-input v-model="formData.name" placeholder="请输入标签名称" maxlength="100" />
                </el-form-item>
                <el-form-item label="图标" prop="icon">
                    <el-input v-model="formData.icon" placeholder="请输入图标（如：el-icon-building）" />
                </el-form-item>
                <el-form-item label="颜色" prop="color">
                    <el-color-picker v-model="formData.color" />
                </el-form-item>
                <el-form-item label="排序顺序" prop="sortOrder">
                    <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api/admin'

// 使用 adminAPI 中的 tag 相关方法
const tagAPI = adminAPI

// 当前标签级别
const currentLevel = ref('level2')

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
    keyword: '',
    isActive: null
})

// 分页
const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0
})

// 表格数据
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
    id: null,
    code: '',
    name: '',
    icon: '',
    color: '#409EFF',
    sortOrder: 0
})

// 表单验证规则
const formRules = {
    code: [
        { required: true, message: '请输入标签代码', trigger: 'blur' },
        { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
    ],
    name: [
        { required: true, message: '请输入标签名称', trigger: 'blur' },
        { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
    ]
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
        
        let res
        if (currentLevel.value === 'level2') {
            res = await tagAPI.getLevel2Tags(params)
            console.log('🏷️ 二级标签响应:', res)
        } else if (currentLevel.value === 'level3') {
            res = await tagAPI.getLevel3Tags(params)
            console.log('🏷️ 三级标签响应:', res)
        } else if (currentLevel.value === 'level4') {
            res = await tagAPI.getLevel4Tags(params)
            console.log('🏷️ 四级标签响应:', res)
        }
        
        // 响应拦截器已经返回了 data 部分，直接解析
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

// 切换标签级别
const handleLevelChange = () => {
    pagination.pageNum = 1
    pagination.total = 0
    tableData.value = []
    loadData()
}

// 重置搜索
const handleReset = () => {
    searchForm.keyword = ''
    searchForm.isActive = null
    pagination.pageNum = 1
    loadData()
}

// 新增标签
const handleCreate = () => {
    isEdit.value = false
    resetForm()
    dialogVisible.value = true
}

// 编辑标签
const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(formData, {
        id: row.id,
        code: row.code,
        name: row.name,
        icon: row.icon,
        color: row.color,
        sortOrder: row.sortOrder
    })
    dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
    const newStatus = !row.isActive
    const action = newStatus ? '启用' : '禁用'
    
    try {
        await ElMessageBox.confirm(`确定要${action}该标签吗？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        const res = await tagAPI.updateLevel2TagStatus(row.id, newStatus)
        if (res.success || res.code === 200) {
            ElMessage.success(`${action}成功`)
            loadData()
        } else {
            ElMessage.error(res.message || `${action}失败`)
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

// 删除标签
const handleDelete = (row) => {
    ElMessageBox.confirm('确定要删除该标签吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const res = await tagAPI.deleteLevel2Tag(row.id)
            if (res.success || res.code === 200) {
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
                res = await tagAPI.updateLevel2Tag(formData.id, formData)
            } else {
                res = await tagAPI.createLevel2Tag(formData)
            }
            
            if (res.success || res.code === 200) {
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

// 关闭对话框
const handleDialogClose = () => {
    formRef.value?.resetFields()
    resetForm()
}

// 重置表单
const resetForm = () => {
    formData.id = null
    formData.code = ''
    formData.name = ''
    formData.icon = ''
    formData.color = '#409EFF'
    formData.sortOrder = 0
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.tag-management {
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
