import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局属性
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$loading = ElLoading

// 添加到window对象以便在其他地方使用
window.$message = ElMessage
window.$loading = ElLoading

app.use(pinia)
app.use(ElementPlus)
app.use(router)

// 在路由守卫中检查用户信息完整性
let hasCheckedProfile = false; // 全局变量避免重复检查

router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('token');
    
    // 如果访问登录页且已登录，重定向到首页
    if (to.path === '/login' && token) {
        next('/');
        return;
    }

    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        if (!token) {
            // 需要认证但没有token，重定向到登录页
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            });
            return;
        }
        
        // 验证token有效性（可选）
        try {
            // 这里可以调用API验证token
            // await userAPI.validateToken();
        } catch (error) {
            // token无效，清除并重定向
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            });
            return;
        }
    }
    
    next();
});

// 路由后置守卫 - 检查用户信息完整性
router.afterEach((to, from) => {
    // 只在需要认证的页面检查，且未检查过
    if (to.meta.requiresAuth && !hasCheckedProfile) {
        setTimeout(() => {
            checkUserInfoCompleteness();
            hasCheckedProfile = true;
        }, 2000); // 延迟2秒执行
    }
});

// 检查用户信息完整性的函数
function checkUserInfoCompleteness() {
    const token = localStorage.getItem('token');
    if (token) {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const requiredFields = ['nickname', 'studentId', 'college'];
        const missingFields = requiredFields.filter(field => !user[field]);
        
        // 检查是否用户选择过"稍后再说"
        const skipCheckTime = localStorage.getItem('skipProfileCheck');
        const oneDay = 24 * 60 * 60 * 1000; // 一天的毫秒数
        
        if (missingFields.length > 0 && router.currentRoute.value.name !== 'PersonalInformation') {
            // 如果24小时内用户选择过"稍后再说"，则不再提示
            if (skipCheckTime && (Date.now() - parseInt(skipCheckTime)) < oneDay) {
                return;
            }
            
            ElMessageBox.confirm(
                '为了更好的使用体验，建议您完善个人信息。是否现在前往完善？',
                '温馨提示',
                {
                    confirmButtonText: '去完善',
                    cancelButtonText: '稍后再说',
                    type: 'info'
                }
            ).then(() => {
                router.push('/personalinformation');
            }).catch(() => {
                // 用户选择稍后再说，记录到本地存储
                localStorage.setItem('skipProfileCheck', Date.now().toString());
            });
        }
    }
}

app.mount('#app')