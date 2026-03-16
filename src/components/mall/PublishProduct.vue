<template>
  <div :class="['publish-page', { 'mobile': isMobile }]">
    <!-- 统一导航组件 -->
    <UnifiedNav 
      :is-mobile="isMobile" 
      current-page="mall"
      :show-sidebar="!isMobile"
      :show-mobile-nav="isMobile"
      :show-user-info="true"
      :user-info="currentUser"
    />

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <div class="publish-container">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          ← 返回交易中心
        </button>

        <h1 class="page-title">📦 发布商品</h1>

        <!-- 发布表单 -->
        <el-form
          ref="formRef"
          :model="productForm"
          :rules="formRules"
          label-position="top"
          class="publish-form"
        >
          <!-- 商品标题 -->
          <el-form-item label="商品标题" prop="title">
            <el-input
              v-model="productForm.title"
              placeholder="请输入商品标题，简明扼要地描述商品"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <!-- 商品分类 -->
          <el-form-item label="商品分类" prop="categoryId">
            <el-select
              v-model="productForm.categoryId"
              placeholder="请选择商品分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <!-- 商品价格 -->
          <el-form-item label="商品价格" prop="price">
            <el-input-number
              v-model="productForm.price"
              :min="0"
              :precision="2"
              placeholder="请输入价格"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 商品描述 -->
          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="productForm.description"
              type="textarea"
              :rows="6"
              placeholder="请详细描述商品的规格、使用情况、新旧程度等"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <!-- 商品图片 -->
          <el-form-item label="商品图片" prop="images">
            <ImageUploader
              v-model="productForm.images"
              :multiple="true"
              :limit="9"
              hint="最多可上传 9 张图片，第一张将作为封面图"
              :auto-upload="false"
              @change="handleImageChange"
            />
          </el-form-item>

          <!-- 联系信息 -->
          <el-form-item label="联系方式（选填）">
            <el-input
              v-model="productForm.contactInfo"
              placeholder="微信/QQ/手机号，不填则默认使用账号信息"
            />
          </el-form-item>

          <!-- 交易方式 -->
          <el-form-item label="交易方式">
            <el-checkbox-group v-model="productForm.tradeMethods">
              <el-checkbox label="face-to-face">当面交易</el-checkbox>
              <el-checkbox label="delivery">快递配送</el-checkbox>
              <el-checkbox label="campus-delivery">校内配送</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
              style="width: 100%; padding: 15px;"
              size="large"
            >
              {{ submitting ? '发布中...' : '立即发布' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElLoading } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { productAPI } from '@/api/product';
import { uploadAPI } from '@/api/upload';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const isMobile = ref(window.innerWidth <= 768);
const currentUser = ref(null);
const categories = ref([]);
const imageFileList = ref([]); // 存储文件对象列表

// 商品表单数据
const productForm = reactive({
  title: '',
  categoryId: null,
  price: 0,
  description: '',
  images: [],
  contactInfo: '',
  tradeMethods: ['face-to-face']
});

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 3, max: 50, message: '标题长度在 3 到 50 个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, message: '描述至少需要 10 个字符', trigger: 'blur' }
  ]
};

// 获取用户信息
const getUserInfo = () => {
  const token = localStorage.getItem('token');
  if (token) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    currentUser.value = {
      name: user.name || user.username || '',
      studentId: user.studentId || user.student_id || '',
      avatar: user.avatar || ''
    };
  } else {
    currentUser.value = null;
  }
};

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await productAPI.getCategories();
    console.log('分类列表:', response); // 调试日志
    
    if (response && Array.isArray(response)) {
      categories.value = response.map(cat => ({
        id: cat.id,
        name: cat.name,
        icon: cat.icon || ''
      }));
      
      // 如果没有分类，添加一些默认选项
      if (categories.value.length === 0) {
        categories.value = [
          { id: 1, name: '二手物品' },
          { id: 2, name: '服务需求' },
          { id: 3, name: '兼职信息' },
          { id: 4, name: '拼车出行' },
          { id: 5, name: '其他' }
        ];
      }
    }
  } catch (error) {
    console.error('获取分类失败:', error);
    // 使用默认分类
    categories.value = [
      { id: 1, name: '二手物品' },
      { id: 2, name: '服务需求' },
      { id: 3, name: '兼职信息' },
      { id: 4, name: '拼车出行' },
      { id: 5, name: '其他' }
    ];
  }
};

// 处理图片变化
const handleImageChange = (data) => {
  console.log('🖼️ 商品图片变化:', data);
  if (data && data.files) {
    // 存储文件对象用于后续上传
    imageFileList.value = data.files;
    console.log('💾 保存文件对象:', imageFileList.value.length, '个');
  }
};

// 处理图片移除
const handleImageRemove = () => {
  // 无需特殊处理
};

// 上传图片到服务器
const uploadImages = async () => {
  if (!imageFileList.value || imageFileList.value.length === 0) {
    return [];
  }
  
  const uploadedUrls = [];
  
  try {
    const loading = ElLoading.service({ text: '正在上传图片...' });
    
    for (const file of imageFileList.value) {
      try {
        // 确保 file 和 file.raw 存在
        const fileToUpload = file.raw || file;
        
        if (!(fileToUpload instanceof File) && !(fileToUpload instanceof Blob)) {
          console.warn('⚠️ 文件不是有效的 File/Blob 对象:', fileToUpload);
          continue;
        }
        
        const response = await uploadAPI.uploadImage(fileToUpload);
        
        if (response) {
          // 处理不同的响应格式
          let imageUrl = '';
          if (typeof response === 'string') {
            imageUrl = response;
          } else if (response.data) {
            imageUrl = response.data;
          } else if (response.url) {
            imageUrl = response.url;
          }
          
          if (imageUrl) {
            // 确保图片 URL 是完整的
            let fullUrl = imageUrl;
            if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
              const cleanPath = imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl;
              fullUrl = `http://localhost:8080${cleanPath}`;
            }
            uploadedUrls.push(fullUrl);
          }
        }
      } catch (error) {
        console.error('单张图片上传失败:', error);
        // 继续上传其他图片
      }
    }
    
    loading.close();
  } catch (error) {
    console.error('图片上传异常:', error);
    ElMessage.error('图片上传失败');
    return null;
  }
  
  return uploadedUrls;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写完整信息');
      return;
    }
    
    // 检查图片
    if (productForm.images.length === 0) {
      ElMessage.warning('请至少上传一张商品图片');
      return;
    }
    
    try {
      submitting.value = true;
      
      // 上传图片
      const imageUrls = await uploadImages();
      if (!imageUrls) return;
      
      // 构建提交数据
      const submitData = {
        title: productForm.title,
        description: productForm.description,
        price: productForm.price,
        categoryId: productForm.categoryId,
        images: imageUrls,
        contactInfo: productForm.contactInfo,
        tradeMethods: productForm.tradeMethods
      };
      
      console.log('提交数据:', submitData); // 调试日志
      console.log('图片 URLs:', imageUrls); // 调试图片数据
      
      // 验证 images 是否为有效数组
      if (!Array.isArray(imageUrls) || imageUrls.length === 0) {
        ElMessage.error('图片数据异常');
        return;
      }
      
      // 确保每个 URL 都是有效字符串
      for (let i = 0; i < imageUrls.length; i++) {
        if (typeof imageUrls[i] !== 'string' || !imageUrls[i].trim()) {
          ElMessage.error(`第${i + 1}张图片的 URL 无效`);
          return;
        }
      }
      
      // 调用发布 API
      const response = await productAPI.createProduct(submitData);
      console.log('发布响应:', response); // 调试日志
      
      ElMessage.success('发布成功！');
      
      // 延迟跳转到商品详情页或列表页
      setTimeout(() => {
        router.push('/mall');
      }, 1500);
      
    } catch (error) {
      console.error('发布商品失败:', error);
      ElMessage.error(error.message || '发布失败，请稍后重试');
    } finally {
      submitting.value = false;
    }
  });
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 设备检测
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth <= 768;
};

onMounted(() => {
  getUserInfo();
  fetchCategories();
  window.addEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.main-content {
  padding: 0px;
  margin-left: 210px;
  display: flex;
  flex-direction: column;
}

.main-content.full-width {
  margin-left: 0;
}

.publish-container {
  margin: 20px;
  padding: 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.page-title {
  margin: 0 0 30px;
  font-size: 1.8rem;
  color: #333;
  text-align: center;
}

.back-btn {
  margin-bottom: 20px;
  padding: 10px 20px;
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background-color: #4A90E2;
  color: white;
}

.publish-form {
  max-width: 800px;
  margin: 0 auto;
}

.upload-tip {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #999;
}

/* 移动端适配 */
.publish-page.mobile .publish-container {
  margin: 15px;
  padding: 20px;
}

.publish-page.mobile .page-title {
  font-size: 1.5rem;
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-upload-list__item) {
  border-radius: 8px;
}
</style>
