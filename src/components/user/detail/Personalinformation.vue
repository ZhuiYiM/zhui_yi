<template>
  <div :class="['personal-info-page', { 'mobile': isMobile }]">
    <!-- 移动端底部导航栏 -->
    <div v-if="isMobile" class="mobile-nav">
      <div class="nav-grid">
        <div class="nav-item" @click="goToPage('map')">
          <div class="nav-icon">🗺️</div>
        </div>
        <div class="nav-item" @click="goToPage('trade')">
          <div class="nav-icon">🛒</div>
        </div>
        <div class="nav-item" @click="goToPage('topic')">
          <div class="nav-icon">💬</div>
        </div>
        <div class="nav-item" @click="goToPage('message')">
          <div class="nav-icon">✉️</div>
        </div>
        <div class="nav-item" @click="goToPage('profile')">
          <div class="nav-icon">👤</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main :class="['main-content', { 'full-width': isMobile }]">
      <!-- 头部 -->
      <header class="header">
        <h1 v-if="isMobile">编辑个人资料</h1>
        <button v-if="!isMobile" class="back-btn" @click="goBack">← 返回个人中心</button>
      </header>

      <!-- 个人信息表单 -->
      <section class="info-form-section">
        <div class="form-container">
          <h2>完善个人信息</h2>
          <p class="form-description">请填写以下信息，让我们更好地为您服务</p>
          
          <!-- 分区说明 -->
          <div class="section-notice">
            <h3>信息分区说明</h3>
            <ul>
              <li><strong>基本信息</strong>：包含ID、头像、昵称、性别、所在学院、个人简介，对他人可见且无法修改隐私设置</li>
              <li><strong>更多信息</strong>：包含联系方式等其他信息，可设置隐私控制</li>
            </ul>
          </div>

          <form @submit.prevent="saveProfile" class="profile-form">
            <!-- 基本信息区域 -->
            <div class="section basic-info-section">
              <h3 class="section-title">基本信息</h3>
              <p class="section-description">这些信息对他人可见，无法修改隐私设置</p>
              
              <!-- 用户ID -->
              <div class="form-group">
                <div class="field-header">
                  <label>ID</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="text" 
                  :value="userId" 
                  readonly
                  class="readonly-input"
                />
              </div>

              <!-- 头像上传 -->
              <div class="form-group">
                <div class="field-header">
                  <label>头像</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <div class="avatar-upload">
                  <img 
                    :src="form.avatar || defaultAvatar" 
                    alt="头像预览" 
                    class="avatar-preview"
                  />
                  <div>
                    <input 
                      type="file" 
                      ref="avatarInput" 
                      @change="handleAvatarUpload" 
                      accept="image/*"
                      style="display: none"
                    />
                    <button type="button" @click="$refs.avatarInput.click()" class="upload-btn">
                      上传头像
                    </button>
                    <p style="font-size: 0.8rem; color: #666; margin-top: 5px;">
                      支持 JPG、PNG 格式，大小不超过 2MB
                    </p>
                  </div>
                </div>
              </div>

              <!-- 昵称 -->
              <div class="form-group">
                <div class="field-header">
                  <label>昵称 *</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="text" 
                  v-model="form.nickname" 
                  placeholder="请输入昵称"
                  required
                />
              </div>

              <!-- 性别 -->
              <div class="form-group">
                <div class="field-header">
                  <label>性别</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <select v-model="form.gender">
                  <option value="">请选择</option>
                  <option value="male">男</option>
                  <option value="female">女</option>
                  <option value="other">其他</option>
                </select>
              </div>

              <!-- 所在学院 -->
              <div class="form-group">
                <div class="field-header">
                  <label>所在学院 *</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <select v-model="form.college" required>
                  <option value="">请选择学院</option>
                  <option value="计算机学院">计算机学院</option>
                  <option value="电子信息学院">电子信息学院</option>
                  <option value="机械工程学院">机械工程学院</option>
                  <option value="经济管理学院">经济管理学院</option>
                  <option value="外国语学院">外国语学院</option>
                  <option value="艺术设计学院">艺术设计学院</option>
                </select>
              </div>

              <!-- 个人简介 -->
              <div class="form-group">
                <div class="field-header">
                  <label>个人简介</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">他人可见</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <textarea 
                  v-model="form.bio" 
                  placeholder="简单介绍一下自己吧..."
                  rows="4"
                ></textarea>
              </div>
            </div>

            <!-- 更多信息区域 -->
            <div class="section more-info-section">
              <h3 class="section-title">更多信息</h3>
              <p class="section-description">这些信息可设置隐私控制，选择是否对他人可见</p>

              <!-- 真实姓名（只读） -->
              <div class="form-group">
                <div class="field-header">
                  <label>真实姓名</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="text" 
                  :value="form.realName" 
                  readonly
                  class="readonly-input"
                  placeholder="真实姓名（只读）"
                />
                <p class="readonly-note">真实姓名为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <!-- 出生日期（只读） -->
              <div class="form-group">
                <div class="field-header">
                  <label>出生日期</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="date" 
                  :value="form.birthDate"
                  readonly
                  class="readonly-input"
                />
                <p class="readonly-note">出生日期为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <!-- 联系信息（只读） -->
              <div class="form-group">
                <div class="field-header">
                  <label>手机号码</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="tel" 
                  :value="form.phone" 
                  readonly
                  class="readonly-input"
                  placeholder="手机号码（只读）"
                />
                <p class="readonly-note">手机号码为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <div class="form-group">
                <div class="field-header">
                  <label>邮箱地址</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="email" 
                  :value="form.email" 
                  readonly
                  class="readonly-input"
                  placeholder="邮箱地址（只读）"
                />
                <p class="readonly-note">邮箱地址为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <!-- 学号（只读） -->
              <div class="form-group">
                <div class="field-header">
                  <label>学号</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="text" 
                  :value="form.studentId" 
                  readonly
                  class="readonly-input"
                  placeholder="学号（只读）"
                />
                <p class="readonly-note">学号为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <!-- 身份证号码（只读） -->
              <div class="form-group">
                <div class="field-header">
                  <label>身份证号码</label>
                  <div class="visibility-fixed">
                    <span class="visibility-label">只读信息</span>
                    <span class="lock-icon">🔒</span>
                  </div>
                </div>
                <input 
                  type="text" 
                  :value="form.idCard || '未设置'" 
                  readonly
                  class="readonly-input"
                  placeholder="身份证号码（只读）"
                />
                <p class="readonly-note">身份证号码为只读信息，请到专门的账号设置页面修改</p>
              </div>

              <!-- 专业班级 -->
              <div class="form-group">
                <div class="field-header">
                  <label>专业班级</label>
                  <div class="visibility-control">
                    <span class="visibility-label">他人可见</span>
                    <label class="switch">
                      <input type="checkbox" v-model="privacy.major">
                      <span class="slider"></span>
                    </label>
                  </div>
                </div>
                <input 
                  type="text" 
                  v-model="form.major" 
                  placeholder="请输入专业班级"
                />
              </div>

              <!-- 兴趣爱好 -->
              <div class="form-group">
                <div class="field-header">
                  <label>兴趣爱好</label>
                  <div class="visibility-control">
                    <span class="visibility-label">他人可见</span>
                    <label class="switch">
                      <input type="checkbox" v-model="privacy.hobbies">
                      <span class="slider"></span>
                    </label>
                  </div>
                </div>
                <div class="tags-input">
                  <div class="tags-container">
                    <span 
                      v-for="(hobby, index) in form.hobbies" 
                      :key="index" 
                      class="tag"
                    >
                      {{ hobby }}
                      <span class="remove-tag" @click="removeHobby(index)">×</span>
                    </span>
                    <input 
                      v-model="newHobby" 
                      @keyup.enter="addHobby" 
                      @blur="addHobby"
                      placeholder="按回车或失去焦点添加标签"
                      class="tag-input"
                    />
                  </div>
                </div>
              </div>

              <!-- 隐私级别设置 -->
              <div class="form-group">
                <label>默认隐私级别</label>
                <div class="privacy-level-selector">
                  <div
                    v-for="level in privacyLevels"
                    :key="level.value"
                    :class="['privacy-option', { active: defaultPrivacyLevel === level.value }]"
                    @click="defaultPrivacyLevel = level.value"
                  >
                    <div class="privacy-icon">{{ level.icon }}</div>
                    <div class="privacy-text">
                      <strong>{{ level.name }}</strong>
                      <p>{{ level.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <button type="button" @click="resetForm" class="cancel-btn">重置</button>
              <button type="submit" class="save-btn">保存信息</button>
            </div>
          </form>
        </div>
      </section>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userAPI } from '@/api/user';

const router = useRouter();

// 设备检测
const isMobile = ref(false);
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
};

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/100x100/4A90E2/FFFFFF?text=头像';

// 用户ID计算属性
const userId = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.userId || user.id || user._id || '未设置';
});

// 表单数据
const form = reactive({
  avatar: '',
  nickname: '',
  realName: '',
  gender: '',
  birthDate: '',
  phone: '',
  email: '',
  studentId: '',
  idCard: '',
  college: '',
  major: '',
  bio: '',
  hobbies: []
});

// 隐私控制数据（仅适用于更多信息区域）
const privacy = reactive({
  realName: false,
  birthDate: false,
  major: false,
  hobbies: false
});

// 默认隐私级别
const defaultPrivacyLevel = ref('public');

// 临时变量
const newHobby = ref('');
const avatarInput = ref(null);

// 初始化数据
const initializeData = async () => {
  try {
    // 首先尝试从localStorage获取
    const localUser = JSON.parse(localStorage.getItem('user') || '{}');
    console.log('从localStorage获取的用户数据:', localUser);
    
    // 同时从服务器获取最新的用户信息
    const serverUser = await userAPI.getCurrentUser();
    console.log('从服务器获取的用户数据:', serverUser);
    
    // 合并数据，优先使用服务器数据
    const user = { ...localUser, ...serverUser };
    
    // 更新表单数据
    form.nickname = user.nickname || user.username || localUser.nickname || localUser.username || '';
    form.realName = user.realName || user.name || localUser.realName || localUser.name || '';
    form.gender = user.gender || localUser.gender || '';
    form.birthDate = user.birthDate || localUser.birthDate || '';
    form.phone = user.phone || localUser.phone || '';
    form.email = user.email || localUser.email || '';
    form.studentId = user.studentId || user.student_id || localUser.studentId || localUser.student_id || '';
    form.idCard = user.idCard || user.id_card || localUser.idCard || localUser.id_card || '';
    form.college = user.college || user.department || localUser.college || localUser.department || '';
    form.major = user.major || localUser.major || '';
    form.bio = user.bio || user.introduction || localUser.bio || localUser.introduction || '';
    form.hobbies = user.hobbies || localUser.hobbies || [];
    form.avatar = user.avatar || user.avatarUrl || localUser.avatar || localUser.avatarUrl || '';
    
    console.log('初始化后的表单数据:', form);
  } catch (error) {
    console.error('初始化用户数据失败:', error);
    // 失败时使用localStorage数据
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    form.nickname = user.nickname || user.username || '';
    form.realName = user.realName || user.name || '';
    form.gender = user.gender || '';
    form.birthDate = user.birthDate || '';
    form.phone = user.phone || '';
    form.email = user.email || '';
    form.studentId = user.studentId || user.student_id || '';
    form.idCard = user.idCard || user.id_card || '';
    form.college = user.college || user.department || '';
    form.major = user.major || '';
    form.bio = user.bio || user.introduction || '';
    form.hobbies = user.hobbies || [];
    form.avatar = user.avatar || user.avatarUrl || '';
  }
};

// 添加兴趣爱好
const addHobby = () => {
  if (newHobby.value.trim() && !form.hobbies.includes(newHobby.value.trim())) {
    form.hobbies.push(newHobby.value.trim());
    newHobby.value = '';
  }
};

// 删除兴趣爱好
const removeHobby = (index) => {
  form.hobbies.splice(index, 1);
};

// 处理头像上传
const handleAvatarUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件');
      return;
    }
    
    // 验证文件大小 (2MB)
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过2MB');
      return;
    }

    // 预览图片
    const reader = new FileReader();
    reader.onload = (e) => {
      form.avatar = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

// 保存个人信息
const saveProfile = async () => {
  try {
    // 表单验证
    if (!form.nickname.trim()) {
      ElMessage.error('请输入昵称');
      return;
    }
    
    if (!form.college) {
      ElMessage.error('请选择所在学院');
      return;
    }

    // 验证邮箱格式
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      ElMessage.error('请输入正确的邮箱格式');
      return;
    }

    // 验证手机号格式
    if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
      ElMessage.error('请输入正确的手机号格式');
      return;
    }

    // 构造提交数据
    const profileData = {
      nickname: form.nickname.trim(),
      realName: form.realName.trim(),
      gender: form.gender,
      birthDate: form.birthDate,
      phone: form.phone,
      email: form.email,
      studentId: form.studentId,
      college: form.college,
      major: form.major.trim(),
      bio: form.bio.trim(),
      hobbies: form.hobbies,
      privacySettings: {
        ...privacy,
        defaultLevel: defaultPrivacyLevel.value
      }
    };

    // 如果有新头像，需要上传
    if (form.avatar && form.avatar.startsWith('data:image')) {
      // 这里应该调用上传API
      // profileData.avatar = await uploadAvatar(form.avatar);
    }

    // 调用API保存用户信息
    await userAPI.updateProfile(profileData);
    
    // 更新本地存储
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const updatedUser = { ...currentUser, ...profileData, avatar: form.avatar };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    
    ElMessage.success('个人信息保存成功');
    
    // 延迟返回个人中心
    setTimeout(() => {
      router.push('/personalcenter');
    }, 1000);

  } catch (error) {
    console.error('保存个人信息失败:', error);
    ElMessage.error(error.message || '保存失败，请稍后重试');
  }
};

// 重置表单
const resetForm = async () => {
  ElMessageBox.confirm('确定要重置所有已填写的信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await initializeData();
    ElMessage.success('表单已重置');
  }).catch(() => {
    // 用户取消操作
  });
};

// 返回个人中心
const goBack = () => {
  router.push('/personalcenter');
};

// 导航
const goToPage = (page) => {
  switch(page) {
    case 'map':
      router.push('/map');
      break;
    case 'trade':
      router.push('/mall');
      break;
    case 'topic':
      router.push('/topicwall');
      break;
    case 'message':
      router.push('/message');
      break;
    case 'profile':
      router.push('/personalcenter');
      break;
  }
};

// 页面初始化
onMounted(async () => {
  updateDeviceDetection();
  window.addEventListener('resize', updateDeviceDetection);
  await initializeData();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});
</script>

<style scoped>
/* 基础布局 */
.personal-info-page {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.personal-info-page.mobile {
  padding-bottom: 80px;
}

/* 主内容区域 */
.main-content {
  padding: 0;
  overflow-y: auto;
}

.main-content.full-width {
  padding-bottom: 80px;
}

.header {
  background-color: #4A90E2;
  color: white;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.back-btn {
  background: rgba(255,255,255,0.2);
  color: white;
  border: 1px solid rgba(255,255,255,0.3);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(255,255,255,0.3);
}

/* 表单区域 */
.info-form-section {
  padding: 20px;
  flex: 1;
}

.form-container {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  max-width: 800px;
  margin: 0 auto;
}

.form-container h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.5rem;
}

.form-description {
  color: #666;
  margin: 0 0 30px 0;
}

/* 分区说明 */
.section-notice {
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 25px;
}

.section-notice h3 {
  margin: 0 0 10px 0;
  color: #1976d2;
  font-size: 1.1rem;
}

.section-notice ul {
  margin: 0;
  padding-left: 20px;
}

.section-notice li {
  margin: 5px 0;
  color: #333;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.half {
  flex: 1;
}

/* 信息分区 */
.section {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
}

.basic-info-section {
  background: #f8f9fa;
  border: 2px solid #4A90E2;
}

.more-info-section {
  background: #fff;
  border: 1px solid #e1e5f2;
}

.section-title {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.basic-info-section .section-title {
  color: #4A90E2;
}

.section-description {
  color: #666;
  margin: 0 0 20px 0;
  font-size: 0.9rem;
}

/* 字段头部（包含标签和隐私控制） */
.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.field-header label {
  font-weight: 500;
  color: #555;
  font-size: 0.9rem;
  margin: 0;
}

/* 固定可见性（锁定状态） */
.visibility-fixed {
  display: flex;
  align-items: center;
  gap: 8px;
}

.lock-icon {
  font-size: 1rem;
  color: #4A90E2;
}

/* 隐私控制开关 */
.visibility-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.visibility-label {
  font-size: 0.8rem;
  color: #666;
}

/* 开关样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 20px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #4A90E2;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px 15px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

/* 只读输入框样式 */
.readonly-input {
  background-color: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
}

.readonly-input:focus {
  border-color: #e1e5f2;
  box-shadow: none;
}

.readonly-note {
  font-size: 0.8rem;
  color: #6c757d;
  margin: 5px 0 0 0;
  font-style: italic;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

/* 头像上传 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e1e5f2;
}

.upload-btn {
  background: #4A90E2;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-btn:hover {
  background: #3a7bc8;
}

/* 标签输入 */
.tags-input {
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.3s ease;
}

.tags-input:focus-within {
  border-color: #4A90E2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag {
  background: #e3f2fd;
  color: #4A90E2;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 5px;
}

.remove-tag {
  cursor: pointer;
  font-weight: bold;
  color: #ff6b6b;
}

.remove-tag:hover {
  color: #ff5252;
}

.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 150px;
  padding: 5px;
}



/* 操作按钮 */
.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #e0e0e0;
  border-color: #ccc;
}

.save-btn {
  background: #4A90E2;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.save-btn:hover {
  background: #3a7bc8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(74, 144, 226, 0.3);
}

/* 移动端导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 10px 15px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 4px rgba(0,0,0,0.1);
  z-index: 1000;
}

.mobile-nav .nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.mobile-nav .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.mobile-nav .nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin-bottom: 2px;
}

/* 桌面端底部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  margin-top: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin: 20px;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .form-container {
    padding: 20px;
    margin: 10px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .field-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .avatar-upload {
    flex-direction: column;
    text-align: center;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .save-btn, .cancel-btn {
    width: 100%;
  }
  
  .privacy-option {
    flex-direction: column;
    text-align: center;
  }
  
  .privacy-icon {
    width: auto;
  }
  
  .section {
    padding: 15px;
  }
  
  .section-title {
    font-size: 1.1rem;
  }
}
</style>