import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

/**
 * 用户信息管理 composable
 * @returns {Object} 用户信息状态和方法
 */
export function useUserInfo() {
  const userInfo = ref({
    name: '',
    studentId: '',
    college: '',
    avatar: 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U',
    isVerified: false,
    isRealNameVerified: false,
    realName: ''
  });

  /**
   * 从 localStorage 获取并更新用户信息
   */
  const fetchUserInfo = () => {
    try {
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      
      // 更新用户信息
      userInfo.value = {
        name: userData.nickname || userData.username || userData.name || '未知用户',
        studentId: userData.studentId || userData.student_id || '未设置',
        college: userData.college || userData.department || '未设置',
        avatar: userData.avatar || userData.avatarUrl || 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U',
        // 身份认证通过判断：有任一已认证身份（学生/商户/团体/教职工）
        isVerified: !!(
          (userData.studentId || userData.student_id) ||  // 学生身份
          (userData.isMerchant === 1) ||  // 商户身份
          (userData.isOrganization === 1) ||  // 团体/部门身份
          (userData.isStaff === 1)  // 教职工身份
        ),
        isRealNameVerified: !!(userData.realName || userData.name),
        realName: userData.realName || userData.name || ''
      };
    } catch (error) {
      console.error('获取用户信息失败:', error);
      userInfo.value = {
        name: '未知用户',
        studentId: '未设置',
        college: '未设置',
        avatar: 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U',
        isVerified: false,
        isRealNameVerified: false,
        realName: ''
      };
    }
  };

  /**
   * 更新认证状态
   * @param {string} type - 认证类型：'identity' 或 'realname'
   * @param {boolean} status - 认证状态
   */
  const updateVerificationStatus = (type, status) => {
    if (type === 'identity') {
      userInfo.value.isVerified = status;
    } else if (type === 'realname') {
      userInfo.value.isRealNameVerified = status;
    }
  };

  onMounted(() => {
    fetchUserInfo();
  });

  return {
    userInfo,
    fetchUserInfo,
    updateVerificationStatus
  };
}
