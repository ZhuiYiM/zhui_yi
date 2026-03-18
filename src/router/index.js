import { createRouter, createWebHistory } from 'vue-router';

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Topicwall from "../components/topic/Topicwall.vue";
import Mall from "../components/mall/Mall.vue";
import Personalcenter from "../components/user/Personalcenter.vue";
import Personalinformation from "../components/user/detail/Personalinformation.vue";
import AccountManagement from "../components/user/AccountManagement.vue";
import AccountVerification from "../components/user/AccountVerification.vue";
import Map from "../components/map/Map.vue";
import Message from "../components/message/Message.vue"; // 消息中心组件
import UserProfile from "../components/user/UserProfile.vue";

// 懒加载组件
const TopicDetail = () => import('../components/topic/TopicDetail.vue');
const ProductDetail = () => import('../components/mall/ProductDetail.vue');
const PublishProduct = () => import('../components/mall/PublishProduct.vue');
const WatermarkTest = () => import('../components/common/WatermarkTest.vue');
const OrderConfirmation = () => import('../components/user/OrderConfirmation.vue');
const OrderPayment = () => import('../components/user/OrderPayment.vue');
const MyOrders = () => import('../components/user/MyOrders.vue');
const MyProducts = () => import('../components/user/MyProducts.vue');
const LocationDetail = () => import('../components/map/LocationDetail.vue');
const SearchResults = () => import('../components/search/SearchResults.vue');

// 管理员页面组件
const AdminLogin = () => import('../views/admin/AdminLogin.vue');
const AdminDashboard = () => import('../views/admin/AdminDashboard.vue');

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
        path: '/topic/:id',
        name: 'TopicDetail',
        component: TopicDetail,
        meta: { requiresAuth: false }  // 话题详情页游客也可访问
    },
    {
        path: '/user/profile/:userId',
        name: 'UserProfile',
        component: UserProfile,
        meta: { requiresAuth: false }  // 用户主页游客也可访问
    },
    {
        path: '/product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
        meta: { requiresAuth: false }  // 商品详情页游客也可访问
    },
    {
        path: '/publish',
        name: 'PublishProduct',
        component: PublishProduct,
        meta: { requiresAuth: true }   // 发布商品需要认证
    },
    {
        path: '/order/confirmation/:id',
        name: 'OrderConfirmation',
        component: OrderConfirmation,
        meta: { requiresAuth: true }   // 订单确认页需要认证
    },
    {
        path: '/order/payment/:id',
        name: 'OrderPayment',
        component: OrderPayment,
        meta: { requiresAuth: true }   // 支付页面需要认证
    },
    {
        path: '/personal/orders',
        name: 'MyOrders',
        component: MyOrders,
        meta: { requiresAuth: true }   // 订单列表需要认证
    },
    {
        path: '/personal/products',
        name: 'MyProducts',
        component: MyProducts,
        meta: { requiresAuth: true }   // 我的商品页面需要认证
    },
    {
        path: '/watermark-test',
        name: 'WatermarkTest',
        component: WatermarkTest,
        meta: { requiresAuth: true }   // 水印测试页面需要认证
    },
    {
        path: '/location/:id',
        name: 'LocationDetail',
        component: LocationDetail,
        meta: { requiresAuth: false }  // 地点详情页游客也可访问
    },
    {
        path: '/search/results',
        name: 'SearchResults',
        component: SearchResults,
        meta: { requiresAuth: false }  // 搜索结果页游客也可访问
    },
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: AdminLogin,
        meta: { requiresAuth: false }  // 管理员登录页无需认证
    },
    {
        path: '/admin/dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requiresAuth: true, role: 'admin' },  // 需要管理员权限
        redirect: '/admin/dashboard/home',
        children: [
            {
                path: 'home',
                name: 'DashboardHome',
                component: () => import('../views/admin/DashboardHome.vue')
            }
        ]
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