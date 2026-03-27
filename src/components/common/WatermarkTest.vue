<template>
  <div class="watermark-test-page">
    <h1>🔐 数字水印测试工具</h1>
    
    <div class="test-sections">
      <!-- 单图上传测试 -->
      <div class="test-card">
        <h3>测试 1: 上传图片（自动嵌入水印）</h3>
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="headers"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          drag
          accept="image/png"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽 PNG 图片到此处或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              仅支持 PNG 格式（水印需要无损格式）
            </div>
          </template>
        </el-upload>
        
        <div v-if="uploadedImageUrl" class="upload-result">
          <h4>上传成功！</h4>
          <img :src="uploadedImageUrl" alt="上传的图片" class="preview-image">
          <p><strong>图片 URL:</strong> {{ uploadedImageUrl }}</p>
          <el-button @click="downloadImage" type="primary" size="small">
            下载图片
          </el-button>
        </div>
      </div>

      <!-- 水印检测 -->
      <div class="test-card">
        <h3>测试 2: 检测图片水印</h3>
        <WatermarkDetector 
          button-type="success"
          button-text="开始检测水印"
        />
        
        <div v-if="extractedUserId" class="detection-result">
          <el-alert
            title="✅ 检测到水印！"
            type="success"
            :closable="false"
          >
            <p><strong>提取到的用户 ID:</strong> {{ extractedUserId }}</p>
            <p>这说明该图片是由用户 #{{ extractedUserId }} 上传的</p>
          </el-alert>
        </div>
      </div>

      <!-- 批量检测 -->
      <div class="test-card">
        <h3>测试 3: 批量检测</h3>
        <el-upload
          ref="batchUploadRef"
          :auto-upload="false"
          :limit="9"
          :on-change="handleBatchChange"
          multiple
          accept="image/*"
        >
          <el-button type="primary">选择多张图片</el-button>
          <template #tip>
            <div class="el-upload__tip">
              可多选，批量检测水印
            </div>
          </template>
        </el-upload>
        
        <el-button 
          @click="batchDetect" 
          type="success" 
          :loading="batchDetecting"
          style="margin-top: 15px;"
        >
          {{ batchDetecting ? '检测中...' : '批量检测' }}
        </el-button>

        <div v-if="batchResults.length > 0" class="batch-result">
          <el-table :data="batchResults" style="width: 100%" max-height="300">
            <el-table-column prop="filename" label="文件名" width="250" />
            <el-table-column label="水印状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.hasWatermark ? 'success' : 'info'">
                  {{ row.hasWatermark ? '有水印' : '无水印' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="用户 ID" width="100">
              <template #default="{ row }">
                {{ row.userId || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 技术说明 -->
      <div class="test-card tech-info-card">
        <h3>📖 技术原理</h3>
        <div class="tech-content">
          <h4>1. 水印嵌入流程</h4>
          <ol>
            <li>用户上传 PNG 图片</li>
            <li>后端读取图片字节流</li>
            <li>将用户 ID 转换为 32 位二进制</li>
            <li>添加开始/结束标记（各 8 位）</li>
            <li>使用 LSB 算法修改像素 RGB 通道的最低位</li>
            <li>保存带水印的图片</li>
          </ol>

          <h4>2. 水印提取流程</h4>
          <ol>
            <li>读取图片字节流</li>
            <li>提取每个像素 RGB 通道的最低位</li>
            <li>拼接成二进制字符串</li>
            <li>验证开始/结束标记</li>
            <li>提取中间 32 位作为用户 ID</li>
          </ol>

          <h4>3. 特点</h4>
          <ul>
            <li><strong>不可见性：</strong>修改的是像素最低位，人眼无法察觉</li>
            <li><strong>容量：</strong>可存储 32 位数据（足够存储用户 ID）</li>
            <li><strong>鲁棒性：</strong>PNG 格式可完整保留水印信息</li>
            <li><strong>安全性：</strong>需要知道算法才能提取</li>
          </ul>

          <h4>4. 应用场景</h4>
          <ul>
            <li>追踪图片泄露源头</li>
            <li>版权保护</li>
            <li>用户行为分析</li>
            <li>内容审核辅助</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import WatermarkDetector from '@/components/common/WatermarkDetector.vue';
import request from '@/api/request';

const uploadRef = ref(null);
const batchUploadRef = ref(null);
const uploadedImageUrl = ref('');
const extractedUserId = ref(null);
const batchFiles = ref([]);
const batchResults = ref([]);
const batchDetecting = ref(false);

// 获取 Token
const getToken = () => {
  return localStorage.getItem('token') || '';
};

// 请求头
const headers = computed(() => ({
  'Authorization': `Bearer ${getToken()}`
}));

// 上传地址
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${baseUrl}/api/upload/image`;
});

// 处理上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    // 注意：图片访问不需要 /api 前缀，直接使用后端服务器地址
    const baseUrl = import.meta.env.VITE_API_BASE_URL 
      ? import.meta.env.VITE_API_BASE_URL.replace('/api', '') 
      : window.location.origin;
    uploadedImageUrl.value = `${baseUrl}${response.data}`;
    ElMessage.success('上传成功！图片已嵌入水印');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 处理上传错误
const handleUploadError = () => {
  ElMessage.error('上传失败，请检查网络连接');
};

// 下载图片
const downloadImage = () => {
  const link = document.createElement('a');
  link.href = uploadedImageUrl.value;
  link.download = `watermarked_${Date.now()}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  ElMessage.success('下载已开始');
};

// 处理批量文件变化
const handleBatchChange = (file, fileList) => {
  batchFiles.value = fileList.map(f => f.raw);
};

// 批量检测
const batchDetect = async () => {
  if (batchFiles.value.length === 0) {
    ElMessage.warning('请先选择图片');
    return;
  }

  try {
    batchDetecting.value = true;
    batchResults.value = [];

    const formData = new FormData();
    batchFiles.value.forEach(file => {
      formData.append('files', file);
    });

    const response = await request.post('/watermark/extract/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.code === 200 && Array.isArray(response.data)) {
      // 合并文件名和检测结果
      batchResults.value = response.data.map((result, index) => ({
        filename: batchFiles.value[index]?.name || `图片${index + 1}`,
        ...result
      }));

      const count = response.data.filter(item => item.hasWatermark).length;
      ElMessage.success(`检测完成！${count}/${batchFiles.value.length} 张图片包含水印`);
    } else {
      ElMessage.error(response.message || '检测失败');
    }
  } catch (error) {
    console.error('检测失败:', error);
    ElMessage.error('检测失败，请重试');
  } finally {
    batchDetecting.value = false;
  }
};
</script>

<style scoped>
.watermark-test-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.test-sections {
  display: grid;
  gap: 20px;
}

.test-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.test-card h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 1.2rem;
}

.upload-result,
.detection-result,
.batch-result {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  margin: 10px 0;
  display: block;
}

.tech-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tech-info-card h3 {
  color: white;
}

.tech-content {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 8px;
}

.tech-content h4 {
  margin: 15px 0 10px;
  color: white;
  font-weight: 600;
}

.tech-content ol,
.tech-content ul {
  margin-left: 20px;
  line-height: 1.8;
}

.tech-content li {
  margin: 5px 0;
}

/* Element Plus 样式覆盖 */
:deep(.el-upload-dragger) {
  padding: 40px;
}

:deep(.el-alert__content) {
  text-align: left;
}
</style>
