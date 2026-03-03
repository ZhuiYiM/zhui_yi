import { createRouter, createWebHistory } from 'vue-router';

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Topicwall from "../components/user/Topicwall.vue";
import Mall from "../components/user/Mall.vue";
import Personalcenter from "../components/user/Personalcenter.vue";
import Personalinformation from "../components/user/detail/Personalinformation.vue";
import AccountManagement from "../components/user/AccountManagement.vue";
import AccountVerification from "../components/user/AccountVerification.vue";
import Map from "../components/user/Map.vue";
import Message from "../components/user/Message.vue"; // 添加 Map 组件的导入

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: false }  // 游客可访问
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/topicwall',
        name: 'Topicwall',
        component: Topicwall,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/mall',
        name: 'Mall',
        component: Mall,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/personalcenter',
        name: 'PersonalCenter',
        component: Personalcenter,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/personalinformation',
        name: 'PersonalInformation',
        component: Personalinformation,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/account/settings',
        name: 'AccountManagement',
        component: AccountManagement,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/account/verification',
        name: 'AccountVerification',
        component: AccountVerification,
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/map',
        name: 'MapPage',  // 将名称改为 MapPage 而不是 Map，避免与内置 Map 对象冲突
        component: Map,   // 引用导入的 Map 组件
        meta: { requiresAuth: true }   // 需要认证
    },
    {
        path: '/message',
        name: 'Message',
        component: Message,
        meta: { requiresAuth: true }
    },
    {
        path: '/:pathMatch(.*)*',  // 捕获所有未匹配的路由
        name: 'NotFound',
        component: () => import('../components/NotFound.vue'), // 动态导入
        meta: { requiresAuth: false }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

// 路由守卫 - 统一处理认证检查
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const requiresAuth = to.meta.requiresAuth;
    
    // 如果路由需要认证
    if (requiresAuth) {
        if (!token) {
            // 未登录，重定向到登录页
            next({
                path: '/login',
                query: { redirect: to.fullPath } // 保存原始目标路径
            });
        } else {
            // 已登录，允许访问
            next();
        }
    } else {
        // 不需要认证的路由，直接访问
        next();
    }
});

export default router;