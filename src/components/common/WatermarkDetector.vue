<template>
  <div class="watermark-detector">
    <!-- 检测入口按钮 -->
    <el-button 
      :type="buttonType" 
      :icon="icon"
      @click="openDetector"
    >
      {{ buttonText }}
    </el-button>

    <!-- 检测对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="🔍 图片水印检测"
      width="600px"
      :close-on-click-modal="false"
    >
      <!-- 上传区域 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept="image/*"
          :on-change="handleFileChange"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽图片到此处或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 PNG/JPG 格式，检测隐藏的用户 ID 水印
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 检测结果 -->
      <div v-if="detectionResult" class="result-section">
        <el-alert
          :title="resultTitle"
          :type="resultType"
          :closable="false"
          show-icon
        >
          <template #default>
            <div v-if="detectionResult.hasWatermark" class="watermark-info">
              <p><strong>检测到水印：</strong>是</p>
              <p><strong>用户 ID：</strong>{{ detectionResult.userId }}</p>
              <p><strong>说明：</strong>该图片包含数字水印，可追溯到上传者</p>
            </div>
            <div v-else class="no-watermark-info">
              <p><strong>检测到水印：</strong>否</p>
              <p><strong>说明：</strong>该图片未包含有效水印</p>
            </div>
          </template>
        </el-alert>

        <!-- 技术细节（可选展开） -->
        <el-collapse class="tech-details">
          <el-collapse-item title="查看技术细节" name="1">
            <div class="tech-info">
              <p><strong>水印类型：</strong>LSB 数字水印</p>
              <p><strong>嵌入位置：</strong>RGB 通道最低有效位</p>
              <p><strong>数据容量：</strong>32 位用户 ID</p>
              <p><strong>可见性：</strong>人眼不可见</p>
              <p><strong>鲁棒性：</strong>可承受轻微压缩和格式转换</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 批量检测 -->
      <div v-if="batchMode" class="batch-section">
        <h4>批量检测结果</h4>
        <el-table :data="batchResults" style="width: 100%" max-height="300">
          <el-table-column prop="filename" label="文件名" width="200" />
          <el-table-column label="水印状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.hasWatermark ? 'success' : 'info'">
                {{ row.hasWatermark ? '包含水印' : '无水印' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="用户 ID">
            <template #default="{ row }">
              {{ row.userId || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="detectWatermark" :loading="detecting">
            {{ detecting ? '检测中...' : '开始检测' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import request from '@/api/request';

const props = defineProps({
  // 按钮类型
  buttonType: {
    type: String,
    default: 'info'
  },
  // 按钮图标
  icon: {
    type: String,
    default: 'Search'
  },
  // 按钮文字
  buttonText: {
    type: String,
    default: '水印检测'
  },
  // 是否启用批量模式
  batchMode: {
    type: Boolean,
    default: false
  }
});

const dialogVisible = ref(false);
const detecting = ref(false);
const selectedFile = ref(null);
const detectionResult = ref(null);
const batchResults = ref([]);
const uploadRef = ref(null);

// 打开检测器
const openDetector = () => {
  dialogVisible.value = true;
  resetState();
};

// 重置状态
const resetState = () => {
  selectedFile.value = null;
  detectionResult.value = null;
  batchResults.value = [];
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
};

// 处理文件变化
const handleFileChange = (file) => {
  selectedFile.value = file.raw;
  detectionResult.value = null;
};

// 执行检测
const detectWatermark = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要检测的图片');
    return;
  }

  try {
    detecting.value = true;

    const formData = new FormData();
    formData.append('file', selectedFile.value);

    const response = await request.post('/watermark/extract', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.code === 200 && response.data) {
      detectionResult.value = response.data;
      
      if (response.data.hasWatermark) {
        ElMessage.success(`检测到水印！用户 ID: ${response.data.userId}`);
      } else {
        ElMessage.info('未检测到有效水印');
      }
    } else {
      ElMessage.error(response.message || '检测失败');
    }
  } catch (error) {
    console.error('检测失败:', error);
    ElMessage.error('检测失败，请重试');
  } finally {
    detecting.value = false;
  }
};

// 批量检测
const batchDetect = async (files) => {
  if (!files || files.length === 0) {
    ElMessage.warning('请选择要检测的图片');
    return;
  }

  try {
    detecting.value = true;

    const formData = new FormData();
    files.forEach((file, index) => {
      formData.append('files', file);
    });

    const response = await request.post('/watermark/extract/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.code === 200 && Array.isArray(response.data)) {
      batchResults.value = response.data;
      
      const count = response.data.filter(item => item.hasWatermark).length;
      ElMessage.success(`检测完成！${count}/${files.length} 张图片包含水印`);
    } else {
      ElMessage.error(response.message || '批量检测失败');
    }
  } catch (error) {
    console.error('批量检测失败:', error);
    ElMessage.error('批量检测失败，请重试');
  } finally {
    detecting.value = false;
  }
};

// 计算结果标题
const resultTitle = computed(() => {
  if (!detectionResult.value) return '';
  return detectionResult.value.hasWatermark ? '✅ 检测到水印' : '❌ 未检测到水印';
});

// 计算结果类型
const resultType = computed(() => {
  if (!detectionResult.value) return 'info';
  return detectionResult.value.hasWatermark ? 'success' : 'warning';
});

// 暴露方法给父组件
defineExpose({
  openDetector,
  batchDetect
});
</script>

<style scoped>
.watermark-detector {
  display: inline-block;
}

.upload-section {
  margin-bottom: 20px;
}

.result-section {
  margin-top: 20px;
}

.watermark-info,
.no-watermark-info {
  padding: 10px 0;
}

.watermark-info p,
.no-watermark-info p {
  margin: 8px 0;
  color: #606266;
}

.tech-details {
  margin-top: 15px;
}

.tech-info {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.tech-info p {
  margin: 5px 0;
  font-size: 0.9rem;
  color: #606266;
}

.batch-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.batch-section h4 {
  margin-bottom: 10px;
  color: #333;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
