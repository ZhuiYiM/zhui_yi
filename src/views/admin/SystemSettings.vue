<template>
  <div class="system-settings">
    <div class="page-header">
      <div class="header-left-content">
        <h2 class="page-title">
          <el-icon><Setting /></el-icon>
          系统设置
        </h2>
        <div class="header-actions">
          <div class="breadcrumb-nav">
            <el-button text type="primary" @click="$router.push('/admin/dashboard/home')" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <span class="separator">/</span>
            <span class="current-page">系统管理</span>
            <span class="separator">/</span>
            <span class="current-page">系统设置</span>
          </div>
        </div>
      </div>
    </div>

    <el-card class="table-card">
      <el-form :model="settings" label-width="200px" v-loading="loading">
        <el-form-item label="网站名称">
          <el-input v-model="settings.site_name" placeholder="请输入网站名称" />
        </el-form-item>
        <el-form-item label="网站描述">
          <el-input v-model="settings.site_description" type="textarea" placeholder="请输入网站描述" />
        </el-form-item>
        <el-form-item label="允许注册">
          <el-switch v-model="settings.allow_registration" :active-value="'true'" :inactive-value="'false'" />
        </el-form-item>
        <el-form-item label="最大上传文件大小">
          <el-input v-model="settings.max_upload_size" placeholder="字节数">
            <template #append>bytes</template>
          </el-input>
        </el-form-item>
        <el-form-item label="允许的图片类型">
          <el-input v-model="settings.allowed_image_types" placeholder="例如：jpg,jpeg,png,gif" />
        </el-form-item>
        <el-form-item label="话题需要审核">
          <el-switch v-model="settings.topic_audit_required" :active-value="'true'" :inactive-value="'false'" />
        </el-form-item>
        <el-form-item label="商品需要审核">
          <el-switch v-model="settings.product_audit_required" :active-value="'true'" :inactive-value="'false'" />
        </el-form-item>
        <el-form-item label="维护模式">
          <el-switch v-model="settings.maintenance_mode" :active-value="'true'" :inactive-value="'false'" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Setting, HomeFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { adminAPI } from '@/api/admin';

const loading = ref(false);
const saving = ref(false);
const settings = reactive({
  site_name: '',
  site_description: '',
  allow_registration: 'true',
  max_upload_size: '5242880',
  allowed_image_types: 'jpg,jpeg,png,gif',
  topic_audit_required: 'true',
  product_audit_required: 'false',
  maintenance_mode: 'false'
});

// 加载系统设置
const loadSettings = async () => {
  loading.value = true;
  try {
    const response = await adminAPI.getSystemSettings();
    
    console.log('⚙️ 系统设置响应:', response);
    if (response.code === 200 && response.data) {
      // 将数组转换为对象
      const settingsObj = {};
      response.data.forEach(item => {
        settingsObj[item.settingKey] = item.settingValue;
      });
      
      // 更新设置值
      Object.assign(settings, settingsObj);
      console.log('✅ 系统设置加载成功');
    } else {
      ElMessage.error(response.message || '加载失败');
    }
  } catch (error) {
    console.error('❌ 加载系统设置失败:', error);
    ElMessage.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 保存设置
const handleSave = async () => {
  saving.value = true;
  try {
    const response = await adminAPI.updateSystemSettings(settings);
    if (response.code === 200) {
      ElMessage.success('保存成功');
    } else {
      ElMessage.error(response.message || '保存失败');
    }
  } catch (error) {
    console.error('❌ 保存系统设置失败:', error);
    ElMessage.error('保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped>
.system-settings {
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

.table-card {
  border-radius: 12px;
  max-width: 800px;
}
</style>
