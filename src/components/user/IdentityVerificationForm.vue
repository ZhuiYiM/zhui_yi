<template>
  <div class="identity-verification-form">
    <el-dialog
      v-model="dialogVisible"
      title="申请身份认证"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="身份类型" prop="identityType">
          <el-select
            v-model="formData.identityType"
            placeholder="请选择身份类型"
            style="width: 100%"
            @change="onIdentityTypeChange"
          >
            <el-option
              v-for="(label, type) in identityTypes"
              :key="type"
              :label="label"
              :value="type"
            />
          </el-select>
        </el-form-item>
              
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        >
          <p style="margin: 0; font-size: 13px;">
            ℹ️ 一个账户仅可选择一个身份类型进行认证，认证有效期为一年
          </p>
        </el-alert>

        <!-- 学生信息 -->
        <template v-if="formData.identityType === 'student'">
          <el-form-item label="学号" prop="studentId">
            <el-input v-model="formData.studentId" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="手机号码" prop="contactPhone">
            <el-input v-model="formData.contactPhone" placeholder="请输入保持通信的手机号码" maxlength="11" />
          </el-form-item>
          <el-form-item label="学生证照片" prop="studentCardUrl">
            <el-upload
              class="studentcard-uploader"
              :http-request="uploadStudentCard"
              :show-file-list="false"
              :before-upload="beforeStudentCardUpload"
            >
              <div class="uploader-content">
                <template v-if="formData.studentCardUrl">
                  <img :src="getFullImageUrl(formData.studentCardUrl)" class="studentcard-image" />
                  <div class="image-overlay">
                    <el-button type="danger" size="small" @click.stop="removeStudentCard">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                    <el-button type="primary" size="small" @click.stop="reuploadStudentCard">
                      <el-icon><Refresh /></el-icon>
                      重传
                    </el-button>
                  </div>
                </template>
                <div v-else class="uploader-placeholder">
                  <el-icon class="studentcard-uploader-icon"><Plus /></el-icon>
                  <span class="uploader-text">点击上传</span>
                </div>
              </div>
            </el-upload>
            <div class="form-tip">请上传学生证照片，包含个人信息页</div>
          </el-form-item>
        </template>

        <!-- 商户信息 -->
        <template v-if="formData.identityType === 'merchant'">
          <el-form-item label="店铺名称" prop="shopName">
            <el-input v-model="formData.shopName" placeholder="请输入店铺名称" />
          </el-form-item>
          <el-form-item label="营业执照" prop="businessLicenseUrl">
            <el-upload
              class="license-uploader"
              :http-request="uploadLicense"
              :show-file-list="false"
              :before-upload="beforeLicenseUpload"
            >
              <div class="uploader-content">
                <template v-if="formData.businessLicenseUrl">
                  <img :src="getFullImageUrl(formData.businessLicenseUrl)" class="license-image" />
                  <div class="image-overlay">
                    <el-button type="danger" size="small" @click.stop="removeLicense">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                    <el-button type="primary" size="small" @click.stop="reuploadLicense">
                      <el-icon><Refresh /></el-icon>
                      重传
                    </el-button>
                  </div>
                </template>
                <div v-else class="uploader-placeholder">
                  <el-icon class="license-uploader-icon"><Plus /></el-icon>
                  <span class="uploader-text">点击上传</span>
                </div>
              </div>
            </el-upload>
            <div class="form-tip">请上传营业执照照片</div>
          </el-form-item>
        </template>

        <!-- 团体/部门信息 -->
        <template v-if="formData.identityType === 'organization'">
          <el-form-item label="组织名称" prop="organizationName">
            <el-input v-model="formData.organizationName" placeholder="请输入组织/部门名称" />
          </el-form-item>
          <el-form-item label="负责人" prop="leaderName">
            <el-input v-model="formData.leaderName" placeholder="请输入负责人姓名" />
          </el-form-item>
          <el-form-item label="联系方式" prop="orgContactPhone">
            <el-input v-model="formData.orgContactPhone" placeholder="请输入联系方式" maxlength="11" />
          </el-form-item>
          <el-form-item label="组织证明文件" prop="organizationProofUrl">
            <el-upload
              class="organization-proof-uploader"
              :http-request="uploadOrganizationProof"
              :show-file-list="false"
              :before-upload="beforeOrganizationProofUpload"
            >
              <div class="uploader-content">
                <template v-if="formData.organizationProofUrl">
                  <img :src="getFullImageUrl(formData.organizationProofUrl)" class="organization-proof-image" />
                  <div class="image-overlay">
                    <el-button type="danger" size="small" @click.stop="removeOrganizationProof">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                    <el-button type="primary" size="small" @click.stop="reuploadOrganizationProof">
                      <el-icon><Refresh /></el-icon>
                      重传
                    </el-button>
                  </div>
                </template>
                <div v-else class="uploader-placeholder">
                  <el-icon class="organization-proof-uploader-icon"><Plus /></el-icon>
                  <span class="uploader-text">点击上传</span>
                </div>
              </div>
            </el-upload>
            <div class="form-tip">请上传组织成立批文、登记证书或相关证明文件</div>
          </el-form-item>
        </template>

        <!-- 教职工信息 -->
        <template v-if="formData.identityType === 'staff'">
          <el-form-item label="工号" prop="staffId">
            <el-input v-model="formData.staffId" placeholder="请输入工号" />
          </el-form-item>
          <el-form-item label="所属部门" prop="department">
            <el-input v-model="formData.department" placeholder="请输入所属部门" />
          </el-form-item>
          <el-form-item label="工作证照片" prop="workCardUrl">
            <el-upload
              class="workcard-uploader"
              :http-request="uploadWorkCard"
              :show-file-list="false"
              :before-upload="beforeWorkCardUpload"
            >
              <div class="uploader-content">
                <template v-if="formData.workCardUrl">
                  <img :src="getFullImageUrl(formData.workCardUrl)" class="workcard-image" />
                  <div class="image-overlay">
                    <el-button type="danger" size="small" @click.stop="removeWorkCard">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                    <el-button type="primary" size="small" @click.stop="reuploadWorkCard">
                      <el-icon><Refresh /></el-icon>
                      重传
                    </el-button>
                  </div>
                </template>
                <div v-else class="uploader-placeholder">
                  <el-icon class="workcard-uploader-icon"><Plus /></el-icon>
                  <span class="uploader-text">点击上传</span>
                </div>
              </div>
            </el-upload>
            <div class="form-tip">请上传工作证照片</div>
          </el-form-item>
        </template>

        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入其他需要说明的信息（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Plus, Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '@/api/user'
import { uploadAPI } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref(null)
const submitting = ref(false)
const identityTypes = ref({})
const hasVerifiedIdentity = ref(false) // 标记是否已有已认证的身份

const formData = reactive({
  identityType: '',
  studentId: '',
  contactPhone: '',
  studentCardUrl: '',
  shopName: '',
  businessLicenseUrl: '',
  organizationName: '',
  leaderName: '',
  orgContactPhone: '',
  organizationProofUrl: '',
  staffId: '',
  department: '',
  workCardUrl: '',
  remark: ''
})

const formRules = {
  identityType: [
    { required: true, message: '请选择身份类型', trigger: 'change' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  studentCardUrl: [
    { required: true, message: '请上传学生证照片', trigger: 'change' }
  ],
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  businessLicenseUrl: [
    { required: true, message: '请上传营业执照', trigger: 'change' }
  ],
  organizationName: [
    { required: true, message: '请输入组织名称', trigger: 'blur' }
  ],
  leaderName: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  orgContactPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  organizationProofUrl: [
    { required: true, message: '请上传组织证明文件', trigger: 'change' }
  ],
  staffId: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入所属部门', trigger: 'blur' }
  ]
}

// 上传相关
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

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
}

// 删除图片
const removeStudentCard = () => {
  formData.studentCardUrl = ''
  ElMessage.success('已删除学生证照片')
}

const removeWorkCard = () => {
  formData.workCardUrl = ''
  ElMessage.success('已删除工作证照片')
}

const removeLicense = () => {
  formData.businessLicenseUrl = ''
  ElMessage.success('已删除营业执照')
}

const removeOrganizationProof = () => {
  formData.organizationProofUrl = ''
  ElMessage.success('已删除组织证明文件')
}

// 重新上传（先删除再触发上传）
const reuploadStudentCard = () => {
  formData.studentCardUrl = ''
  // 触发上传组件的点击
  setTimeout(() => {
    const input = document.querySelector('.studentcard-uploader input[type="file"]')
    if (input) input.click()
  }, 100)
}

const reuploadWorkCard = () => {
  formData.workCardUrl = ''
  setTimeout(() => {
    const input = document.querySelector('.workcard-uploader input[type="file"]')
    if (input) input.click()
  }, 100)
}

const reuploadLicense = () => {
  formData.businessLicenseUrl = ''
  setTimeout(() => {
    const input = document.querySelector('.license-uploader input[type="file"]')
    if (input) input.click()
  }, 100)
}

const reuploadOrganizationProof = () => {
  formData.organizationProofUrl = ''
  setTimeout(() => {
    const input = document.querySelector('.organization-proof-uploader input[type="file"]')
    if (input) input.click()
  }, 100)
}

// 自定义上传方法
const uploadStudentCard = async ({ file, onSuccess, onError }) => {
  try {
    const response = await uploadAPI.uploadImage(file)
    console.log('📷 学生证上传 - 原始响应:', response)
    console.log('📷 学生证上传 - 响应类型:', typeof response)
    
    // 响应拦截器已经返回 data 字段，可能是字符串或对象
    let imageUrl
    if (typeof response === 'string') {
      imageUrl = response  // 直接是 URL 字符串
    } else if (response && typeof response === 'object') {
      imageUrl = response.url || response.fileUrl  // 对象形式
    }
    
    console.log('📷 学生证上传 - 提取的 URL:', imageUrl)
    
    if (imageUrl) {
      formData.studentCardUrl = imageUrl
      ElMessage.success('学生证上传成功')
      onSuccess({ url: imageUrl })
    } else {
      throw new Error('未获取到图片 URL')
    }
  } catch (error) {
    console.error('❌ 学生证上传失败:', error)
    ElMessage.error(error.message || '上传失败')
    onError(error)
  }
}

const uploadWorkCard = async ({ file, onSuccess, onError }) => {
  try {
    const response = await uploadAPI.uploadImage(file)
    console.log('📷 工作证上传 - 原始响应:', response)
    console.log('📷 工作证上传 - 响应类型:', typeof response)
    
    let imageUrl
    if (typeof response === 'string') {
      imageUrl = response
    } else if (response && typeof response === 'object') {
      imageUrl = response.url || response.fileUrl
    }
    
    console.log('📷 工作证上传 - 提取的 URL:', imageUrl)
    
    if (imageUrl) {
      formData.workCardUrl = imageUrl
      console.log('✅ formData.workCardUrl 已设置:', formData.workCardUrl)
      console.log('✅ 完整图片 URL:', getFullImageUrl(formData.workCardUrl))
      ElMessage.success('工作证上传成功')
      onSuccess({ url: imageUrl })
    } else {
      throw new Error('未获取到图片 URL')
    }
  } catch (error) {
    console.error('❌ 工作证上传失败:', error)
    ElMessage.error(error.message || '上传失败')
    onError(error)
  }
}

const uploadLicense = async ({ file, onSuccess, onError }) => {
  try {
    const response = await uploadAPI.uploadImage(file)
    console.log('📷 营业执照上传 - 原始响应:', response)
    console.log('📷 营业执照上传 - 响应类型:', typeof response)
    
    let imageUrl
    if (typeof response === 'string') {
      imageUrl = response
    } else if (response && typeof response === 'object') {
      imageUrl = response.url || response.fileUrl
    }
    
    console.log('📷 营业执照上传 - 提取的 URL:', imageUrl)
    
    if (imageUrl) {
      formData.businessLicenseUrl = imageUrl
      ElMessage.success('营业执照上传成功')
      onSuccess({ url: imageUrl })
    } else {
      throw new Error('未获取到图片 URL')
    }
  } catch (error) {
    console.error('❌ 营业执照上传失败:', error)
    ElMessage.error(error.message || '上传失败')
    onError(error)
  }
}

const uploadOrganizationProof = async ({ file, onSuccess, onError }) => {
  try {
    const response = await uploadAPI.uploadImage(file)
    console.log('📷 组织证明文件上传 - 原始响应:', response)
    console.log('📷 组织证明文件上传 - 响应类型:', typeof response)
    
    let imageUrl
    if (typeof response === 'string') {
      imageUrl = response
    } else if (response && typeof response === 'object') {
      imageUrl = response.url || response.fileUrl
    }
    
    console.log('📷 组织证明文件上传 - 提取的 URL:', imageUrl)
    
    if (imageUrl) {
      formData.organizationProofUrl = imageUrl
      ElMessage.success('组织证明文件上传成功')
      onSuccess({ url: imageUrl })
    } else {
      throw new Error('未获取到图片 URL')
    }
  } catch (error) {
    console.error('❌ 组织证明文件上传失败:', error)
    ElMessage.error(error.message || '上传失败')
    onError(error)
  }
}

// 加载身份类型
const loadIdentityTypes = async () => {
  try {
    const response = await userAPI.getIdentityTypes()
    identityTypes.value = response
  } catch (error) {
    console.error('加载身份类型失败:', error)
  }
}

// 检查用户已有的身份认证
const checkUserIdentities = async () => {
  try {
    const response = await userAPI.getUserIdentities()
    if (response && response.length > 0) {
      // 检查是否有已认证的
      const verified = response.find(item => item.verified === 1)
      if (verified) {
        hasVerifiedIdentity.value = true
        ElMessage.info(`您当前拥有${verified.identityName}身份，可以申请变更其他身份类型`)
      }
    }
  } catch (error) {
    console.error('检查身份认证状态失败:', error)
  }
}

// 身份类型变更
const onIdentityTypeChange = () => {
  // 清空验证规则
  formRef.value?.clearValidate()
}

// 上传前验证
const beforeLicenseUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const beforeWorkCardUpload = beforeLicenseUpload

const beforeStudentCardUpload = beforeLicenseUpload

const beforeOrganizationProofUpload = beforeLicenseUpload

// 提交申请
const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 检查是否已有已认证的身份
  if (hasVerifiedIdentity.value) {
    ElMessage.info('您可以申请变更其他身份类型，审核通过后将自动切换')
    // 不阻止提交，允许申请新身份
  }
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 准备提交数据
      const submitData = {
        identityType: formData.identityType
      }
      
      // 根据身份类型添加相应字段
      if (formData.identityType === 'student') {
        submitData.studentId = formData.studentId
        submitData.contactPhone = formData.contactPhone
        submitData.studentCardUrl = formData.studentCardUrl
      } else if (formData.identityType === 'merchant') {
        submitData.shopName = formData.shopName
        submitData.businessLicenseUrl = formData.businessLicenseUrl
      } else if (formData.identityType === 'organization') {
        submitData.organizationName = formData.organizationName
        submitData.leaderName = formData.leaderName
        submitData.contactPhone = formData.orgContactPhone
        submitData.organizationProofUrl = formData.organizationProofUrl
      } else if (formData.identityType === 'staff') {
        submitData.staffId = formData.staffId
        submitData.department = formData.department
        submitData.workCardUrl = formData.workCardUrl
      }
      
      if (formData.remark) {
        submitData.remark = formData.remark
      }
      
      await userAPI.applyIdentity(submitData)
      ElMessage.success('申请已提交，请等待审核')
      emit('success')
      dialogVisible.value = false
    } catch (error) {
      console.error('提交申请失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '申请失败')
    } finally {
      submitting.value = false
    }
  })
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  formData.identityType = ''
  formData.studentId = ''
  formData.contactPhone = ''
  formData.studentCardUrl = ''
  formData.shopName = ''
  formData.businessLicenseUrl = ''
  formData.organizationName = ''
  formData.leaderName = ''
  formData.orgContactPhone = ''
  formData.organizationProofUrl = ''
  formData.staffId = ''
  formData.department = ''
  formData.workCardUrl = ''
  formData.remark = ''
  hasVerifiedIdentity.value = false // 重置标记
}

// 初始化
loadIdentityTypes()
checkUserIdentities()
</script>

<style scoped>
.license-uploader,
.workcard-uploader,
.studentcard-uploader,
.organization-proof-uploader {
  width: 100%;
  max-width: 300px;  /* 增大最大宽度 */
  height: 200px;     /* 增大高度 */
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  transition: border-color 0.3s;
}

.license-uploader:hover,
.workcard-uploader:hover,
.studentcard-uploader:hover,
.organization-proof-uploader:hover {
  border-color: #409EFF;
}

.uploader-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 8px;
}

.uploader-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.uploader-text {
  font-size: 12px;
  color: #8c939d;
}

.license-image,
.workcard-image,
.studentcard-image,
.organization-proof-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

/* 图片悬浮操作层 */
.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 6px;
}

.license-uploader:hover .image-overlay,
.workcard-uploader:hover .image-overlay,
.studentcard-uploader:hover .image-overlay,
.organization-proof-uploader:hover .image-overlay {
  opacity: 1;
}

.image-overlay .el-button {
  width: 80px;
  font-size: 12px;
  padding: 8px 12px;
}

.license-uploader-icon,
.workcard-uploader-icon,
.studentcard-uploader-icon,
.organization-proof-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
