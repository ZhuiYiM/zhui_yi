<template>
    <div class="advertisement-management">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span>广告管理</span>
                    <el-button type="primary" @click="handleCreate">新增广告</el-button>
                </div>
            </template>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="广告标题">
                    <el-input v-model="searchForm.title" placeholder="请输入广告标题" clearable />
                </el-form-item>
                <el-form-item label="广告位置">
                    <el-select v-model="searchForm.position" placeholder="请选择广告位置" clearable>
                        <el-option label="首页" value="home" />
                        <el-option label="话题墙" value="topicwall" />
                        <el-option label="交易中心" value="mall" />
                        <el-option label="地图" value="map" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="searchForm.isActive" placeholder="请选择状态" clearable>
                        <el-option label="启用" :value="1" />
                        <el-option label="禁用" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData">查询</el-button>
                    <el-button @click="handleReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 广告列表 -->
            <el-table 
                v-loading="loading"
                :data="tableData" 
                border 
                stripe
                style="width: 100%">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="title" label="广告标题" min-width="200" show-overflow-tooltip />
                <el-table-column prop="imageUrl" label="广告图片" width="150">
                    <template #default="{ row }">
                        <el-image 
                            v-if="row.imageUrl"
                            :src="row.imageUrl" 
                            :preview-src-list="[row.imageUrl]"
                            preview-teleported
                            style="width: 100px; height: 60px; object-fit: cover;"
                            fit="cover" />
                        <span v-else>无图片</span>
                    </template>
                </el-table-column>
                <el-table-column prop="position" label="位置" width="100">
                    <template #default="{ row }">
                        <el-tag v-if="row.position === 'home'">首页</el-tag>
                        <el-tag v-else-if="row.position === 'topicwall'" type="success">话题墙</el-tag>
                        <el-tag v-else-if="row.position === 'mall'" type="warning">交易中心</el-tag>
                        <el-tag v-else-if="row.position === 'map'" type="info">地图</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="adType" label="广告类型" width="120">
                    <template #default="{ row }">
                        <el-tag v-if="row.adType === 'activity'" type="danger">活动标签</el-tag>
                        <el-tag v-else-if="row.adType === 'merchant'" type="primary">商家页面</el-tag>
                        <el-tag v-else-if="row.adType === 'product'" type="success">商品广告</el-tag>
                        <el-tag v-else>未知类型</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" width="80" />
                <el-table-column prop="isActive" label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.isActive === 1 ? 'success' : 'danger'">
                            {{ row.isActive === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="展示次数" width="90" />
                <el-table-column prop="clickCount" label="点击次数" width="90" />
                <el-table-column label="操作" width="250" fixed="right">
                    <template #default="{ row }">
                        <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                        <el-button 
                            size="small" 
                            :type="row.isActive === 1 ? 'warning' : 'success'"
                            @click="handleToggleStatus(row)">
                            {{ row.isActive === 1 ? '禁用' : '启用' }}
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
            :title="isEdit ? '编辑广告' : '新增广告'"
            width="600px"
            @close="handleDialogClose">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px">
                <el-form-item label="广告标题" prop="title">
                    <el-input v-model="formData.title" placeholder="请输入广告标题" maxlength="200" />
                </el-form-item>
                <el-form-item label="广告图片" prop="imageUrl">
                    <el-input v-model="formData.imageUrl" placeholder="请输入图片 URL" />
                    <el-image 
                        v-if="formData.imageUrl"
                        :src="formData.imageUrl"
                        :preview-src-list="[formData.imageUrl]"
                        preview-teleported
                        style="width: 200px; height: 100px; margin-top: 10px; object-fit: cover;"
                        fit="cover" />
                </el-form-item>
                <el-form-item label="跳转链接" prop="linkUrl">
                    <el-input v-model="formData.linkUrl" placeholder="请输入跳转链接" />
                </el-form-item>
                <el-form-item label="广告类型" prop="adType">
                    <el-select v-model="formData.adType" placeholder="请选择广告类型" style="width: 100%;" @change="handleAdTypeChange">
                        <el-option label="商品广告" value="product" />
                        <el-option label="商家页面" value="merchant" />
                        <el-option label="活动标签筛选" value="activity" />
                    </el-select>
                </el-form-item>
                <el-form-item label="关联 ID" prop="relatedId" v-if="formData.adType && formData.adType !== 'activity'">
                    <el-input-number 
                        v-model="formData.relatedId" 
                        :min="1" 
                        :placeholder="formData.adType === 'merchant' ? '请输入商家 ID' : '请输入商品 ID'" 
                        style="width: 100%;" 
                    />
                </el-form-item>
                <el-form-item label="筛选标签" prop="filterTags" v-if="formData.adType === 'activity'">
                    <!-- TODO: 改为多标签按键选择组件，替代当前的 JSON 文本输入方式 -->
                    <!-- TODO: 实现标签选择器，支持从标签库中选择多个标签 -->
                    <!-- TODO: 参考标签管理功能的标签选择器实现 -->
                    <el-input 
                        v-model="formData.filterTags" 
                        type="textarea" 
                        :rows="3"
                        placeholder='请输入 JSON 格式的筛选标签，如：{"tags": ["secondhand", "books"]}' 
                        maxlength="500" 
                    />
                </el-form-item>
                <el-form-item label="广告内容" prop="content">
                    <el-input 
                        v-model="formData.content" 
                        type="textarea" 
                        :rows="4"
                        placeholder="请输入广告内容描述" 
                        maxlength="1000" />
                </el-form-item>
                <el-form-item label="广告位置" prop="position">
                    <el-select v-model="formData.position" placeholder="请选择广告位置" style="width: 100%;">
                        <el-option label="首页" value="home" />
                        <el-option label="话题墙" value="topicwall" />
                        <el-option label="交易中心" value="mall" />
                        <el-option label="地图" value="map" />
                    </el-select>
                </el-form-item>
                <el-form-item label="排序顺序" prop="sortOrder">
                    <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
                </el-form-item>
                <el-form-item label="是否启用" prop="isActive">
                    <el-switch v-model="formData.isActive" :active-value="1" :inactive-value="0" />
                </el-form-item>
                <el-form-item label="开始时间" prop="startTime">
                    <el-date-picker
                        v-model="formData.startTime"
                        type="datetime"
                        placeholder="选择开始时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;" />
                </el-form-item>
                <el-form-item label="结束时间" prop="endTime">
                    <el-date-picker
                        v-model="formData.endTime"
                        type="datetime"
                        placeholder="选择结束时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;" />
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
import { advertisementAPI } from '@/api/admin-advertisement'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
    title: '',
    position: '',
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
    title: '',
    imageUrl: '',
    linkUrl: '',
    content: '',
    position: '',
    sortOrder: 0,
    isActive: 1,
    startTime: null,
    endTime: null,
    adType: 'product', // 默认商品广告
    relatedId: null,
    filterTags: ''
})

// 表单验证规则
const formRules = {
    title: [
        { required: true, message: '请输入广告标题', trigger: 'blur' },
        { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
    ],
    position: [
        { required: true, message: '请选择广告位置', trigger: 'change' }
    ]
    // TODO: filterTags 字段验证规则：当 adType 为 'activity' 时，需要验证 JSON 格式
    // TODO: 改为多标签选择后，验证选中的标签数组不为空
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
        const res = await advertisementAPI.getAdvertisements(params)
        console.log('📝 广告响应:', res)
        
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

// 重置搜索
const handleReset = () => {
    searchForm.title = ''
    searchForm.position = ''
    searchForm.isActive = null
    pagination.pageNum = 1
    loadData()
}

// 处理广告类型变化
const handleAdTypeChange = (type) => {
    // 清空关联 ID 和筛选标签
    if (type === 'activity') {
        formData.relatedId = null
    } else {
        formData.filterTags = ''
    }
    // TODO: 当 adType 为 'activity' 时，应该打开标签选择器对话框
    // TODO: 标签选择器应支持搜索、多选、已选标签展示等功能
}

// 新增广告
const handleCreate = () => {
    isEdit.value = false
    resetForm()
    dialogVisible.value = true
}

// 编辑广告
const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(formData, {
        id: row.id,
        title: row.title,
        imageUrl: row.imageUrl,
        linkUrl: row.linkUrl,
        content: row.content,
        position: row.position,
        sortOrder: row.sortOrder,
        isActive: row.isActive,
        startTime: row.startTime,
        endTime: row.endTime,
        adType: row.adType || 'product',
        relatedId: row.relatedId,
        filterTags: row.filterTags
    })
    dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
    const newStatus = row.isActive === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'
    
    try {
        await ElMessageBox.confirm(`确定要${action}该广告吗？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        await advertisementAPI.updateAdvertisementStatus(row.id, newStatus)
        // 状态切换成功，不抛出异常就是成功
        ElMessage.success(`${action}成功`)
        loadData()
    } catch (error) {
        // 业务错误已经全局显示了，不需要重复提示
        if (error.message !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

// 删除广告
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该广告吗？', '警告', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        await advertisementAPI.deleteAdvertisement(row.id)
        // 删除成功，不抛出异常就是成功
        ElMessage.success('删除成功')
        loadData()
    } catch (error) {
        // 如果错误消息是"广告不存在"，说明是业务错误，已经全局显示了
        // 如果是取消操作，不显示错误
        if (error.message !== '广告不存在' && error.message !== 'cancel') {
            console.error('删除失败:', error)
            ElMessage.error('删除失败')
        }
    }
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
                res = await advertisementAPI.updateAdvertisement(formData.id, formData)
            } else {
                res = await advertisementAPI.createAdvertisement(formData)
            }
            
            // 响应拦截器已经返回了 data 部分，直接判断是否为空
            if (res !== undefined && res !== null) {
                ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
                dialogVisible.value = false
                loadData()
            } else {
                ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
            }
        } catch (error) {
            // 业务错误已经全局显示了，不需要重复提示
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
    formData.title = ''
    formData.imageUrl = ''
    formData.linkUrl = ''
    formData.content = ''
    formData.position = ''
    formData.sortOrder = 0
    formData.isActive = 1
    formData.startTime = null
    formData.endTime = null
    formData.adType = 'product'
    formData.relatedId = null
    formData.filterTags = ''
    // TODO: 重置表单时，如果需要清空标签选择器的选中状态
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.advertisement-management {
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
