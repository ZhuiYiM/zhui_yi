<template>
  <div :class="['mall-page', { 'mobile': isMobile }]">
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
      <!-- 头部搜索栏 -->
      <header class="header">
        <h1 v-if="isMobile">交易中心</h1>
        <div class="search-bar">
          <!-- 使用统一搜索组件 -->
          <UnifiedSearch
            v-model="searchQuery"
            :available-tags="productTags"
            :enable-result-page="true"
            :default-search-type="'product'"
            placeholder="搜索商品、服务..."
            tag-selector-title="选择商品类型"
            :show-quick-filters="false"
            :show-clear-button="false"
            @search="handleSearch"
          />
        </div>
      </header>

      <!-- 广告栏 -->
      <AdBanner 
        :is-mobile="isMobile" 
        :custom-ads="mallAds"
        @ad-click="handleAdClick"
      />

      <!-- 分类导航区域 -->
      <section class="categories-section">
        <div class="section-header">
          <h2>交易分类</h2>
        </div>

        <!-- 移动端分类导航 -->
        <div v-if="isMobile" class="category-mobile-grid">
          <div
              v-for="category in categories"
              :key="category.id"
              class="category-mobile-item"
              @click="selectCategory(category.id)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>

        <!-- 桌面端分类导航 -->
        <div v-else class="category-grid">
          <div
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              @click="selectCategory(category.id)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.description }}</p>
          </div>
        </div>
      </section>

      <!-- 商品列表区域 -->
      <section class="products-section">
        <div class="section-header">
          <h2>全部商品</h2>
          <div class="header-actions">
            <!-- 一级标签筛选 -->
            <el-select 
              v-model="filters.level1Tag" 
              placeholder="身份标签" 
              clearable
              @change="applyFilters"
              class="level1-tag-filter"
            >
              <el-option label="全部" value="" />
              <el-option label="学生" value="student" />
              <el-option label="商家" value="merchant" />
              <el-option label="管理员" value="admin" />
              <el-option label="团体" value="organization" />
              <el-option label="社会" value="society" />
            </el-select>
            
            <button 
              v-if="!isMobile && showFilters" 
              class="filter-toggle-btn" 
              @click="toggleFilters"
            >
              {{ showFilters ? '隐藏筛选' : '显示筛选' }}
            </button>
            <button class="publish-btn" @click="createNewTrade">
              ➕ 发布商品
            </button>
          </div>
        </div>

        <!-- 筛选条件 -->
        <div v-if="showFilters" class="filters-bar">
          <el-select v-model="filters.categoryId" placeholder="全部分类" clearable @change="applyFilters">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
          
          <el-select v-model="filters.sort" placeholder="排序方式" @change="applyFilters">
            <el-option label="最新发布" value="newest" />
            <el-option label="最热门" value="hotest" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
          </el-select>
        </div>

        <!-- 商品列表组件 -->
        <ProductList
          :products="displayProducts"
          :is-mobile="isMobile"
          :loading="productsLoading"
          :has-more="hasMore"
          :columns="4"
          :mobile-columns="2"
          :show-load-more="true"
          empty-text="暂无相关商品"
          @product-click="goToProduct"
          @favorite-change="handleFavoriteChange"
          @load-more="loadMore"
        />
      </section>

      <!-- 发布入口（移动端） -->
      <div v-if="isMobile" class="mobile-fab">
        <button @click="createNewTrade">
          ➕ 发布
        </button>
      </div>

      <!-- 桌面端底部版权信息 -->
      <footer v-if="!isMobile" class="desktop-footer">
        <p>© 2023 校园信息平台 | 服务学生，连接校园</p>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import UnifiedNav from '@/components/common/UnifiedNav.vue';
import UnifiedSearch from '@/components/common/UnifiedSearch.vue';
import AdBanner from '../mall/AdBanner.vue';
import ProductList from '../mall/ProductList.vue';
import { productAPI, mallAdAPI } from '@/api/product';

const router = useRouter();
const route = useRoute();

// 用户信息
const currentUser = ref(null);
const defaultAvatar = 'https://placehold.co/24x24/4A90E2/FFFFFF?text=U';

// 设备检测
const isMobile = ref(false);
const updateDeviceDetection = () => {
  isMobile.value = window.innerWidth < 768;
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

// 广告数据
const mallAds = ref([]);

// 搜索查询（用于 UnifiedSearch 组件）
const searchQuery = ref('');

// 获取广告列表
const fetchMallAds = async () => {
  try {
    const response = await mallAdAPI.getMallAds();
    console.log('📢 广告响应:', response);
    
    // 后端直接返回数组，或者包含 records 属性的对象
    let adList = [];
    if (Array.isArray(response)) {
      adList = response;
    } else if (response && response.records) {
      adList = response.records;
    } else if (response && response.data) {
      // 如果嵌套在 data 中
      adList = Array.isArray(response.data) ? response.data : response.data.records || [];
    }
    
    if (adList && adList.length > 0) {
      // 转换广告数据格式以适配 AdBanner 组件
      mallAds.value = adList.map(ad => {
        // 优先使用生成的链接，如果没有 adType 则使用数据库中的 linkUrl
        const link = ad.adType ? generateAdLink(ad) : (ad.linkUrl || '/mall');
        console.log(' 广告链接生成:', {
          id: ad.id,
          title: ad.title,
          adType: ad.adType,
          relatedId: ad.relatedId,
          linkUrl: ad.linkUrl,
          generatedLink: link
        });
        return {
          id: ad.id,
          title: ad.title,
          description: ad.content || '',
          image: ad.imageUrl || 'https://placehold.co/800x300/CCCCCC/FFFFFF?text=广告',
          actionText: '查看详情',
          link: link,
          adType: ad.adType || 'product',
          relatedId: ad.relatedId,
          filterTags: ad.filterTags ? JSON.parse(ad.filterTags) : null
        };
      });
      console.log('✅ 加载广告数据:', mallAds.value.length, '个');
    } else {
      console.log('⚠️ 广告数据为空，使用默认广告');
    }
  } catch (error) {
    console.error('❌ 获取广告数据失败:', error);
    // 失败时使用默认广告（AdBanner 组件会自动处理）
  }
};

// 处理 URL 中的 tags 参数
const handleUrlTags = () => {
  const tagsParam = route.query.tags || route.query.tag;
  
  if (tagsParam) {
    const tagArray = tagsParam.split(',');
    
    // 清空其他筛选条件
    filters.categoryId = null;
    filters.level1Tag = '';
    filters.sort = 'newest';
    
    // 标签代码映射（英文 -> 中文关键词）
    const tagMapping = {
      'secondhand': '二手',
      'service': '服务',
      'parttime': '兼职',
      'food': '美食',
      'digital': '数码',
      'books': '教材',
      'daily': '生活',
      'electronics': '电子',
      'sports': '体育'
    };
    
    // 将标签代码转换为中文关键词，用空格连接
    const chineseKeywords = tagArray.map(tag => {
      return tagMapping[tag.toLowerCase()] || tag;
    }).join(' ');
    
    filters.keyword = chineseKeywords;
    
    // 刷新商品列表
    fetchProducts();
    
    ElMessage.success(`已筛选标签：${tagsParam}`);
  }
};

// 根据广告类型生成跳转链接
const generateAdLink = (ad) => {
  if (!ad.adType || ad.adType === 'product') {
    return `/product/${ad.relatedId}`;
  } else if (ad.adType === 'merchant') {
    // 商家广告跳转到用户主页
    return `/user/profile/${ad.relatedId}`;
  } else if (ad.adType === 'activity' && ad.filterTags) {
    const tags = JSON.parse(ad.filterTags);
    const tagParam = tags.tags ? tags.tags.join(',') : '';
    return `/mall?tags=${tagParam}`;
  }
  return '/mall';
};

// 处理广告点击
const handleAdClick = (ad) => {
  // 增加点击次数
  if (ad.id) {
    mallAdAPI.incrementClickCount(ad.id).catch(err => {
      console.error('增加点击次数失败:', err);
    });
  }
};

// 商品标签（用于搜索筛选）
const productTags = computed(() => {
  return [
    { code: 'secondhand', name: '二手物品', color: '#4A90E2' },
    { code: 'service', name: '服务需求', color: '#7B68EE' },
    { code: 'parttime', name: '兼职信息', color: '#FFA500' },
    { code: 'food', name: '美食外卖', color: '#FF6347' },
    { code: 'digital', name: '数码配件', color: '#32CD32' },
    { code: 'books', name: '教材出售', color: '#9370DB' },
    { code: 'daily', name: '生活用品', color: '#FF69B4' },
    { code: 'electronics', name: '电子产品', color: '#20B2AA' },
    { code: 'sports', name: '体育用品', color: '#FFD700' }
  ];
});

// 处理搜索
const handleSearch = (searchData) => {
  // 统一搜索组件会自动跳转到搜索结果页
};

// 分类数据
const categories = ref([
  { id: 1, name: '二手物品', icon: '📚', description: '书籍、电子设备等' },
  { id: 2, name: '服务需求', icon: '🚗', description: '代取快递、跑腿' },
  { id: 3, name: '兼职信息', icon: '💼', description: '校内外兼职' },
  { id: 4, name: '拼车出行', icon: '🚙', description: '节假日拼车' },
  { id: 5, name: '美食外卖', icon: '🍔', description: '美食分享' },
  { id: 6, name: '书籍教材', icon: '📖', description: '教材、辅导书等' },
  { id: 7, name: '电子设备', icon: '💻', description: '手机、电脑等' },
  { id: 8, name: '生活用品', icon: '🏠', description: '日用品、家居' },
  { id: 9, name: '代取快递', icon: '📦', description: '快递代取服务' },
  { id: 10, name: '跑腿服务', icon: '🏃', description: '代办事项' },
  { id: 99, name: '其他', icon: '📦', description: '其他类别' }
]);

// 筛选条件
const showFilters = ref(true);
const filters = reactive({
  categoryId: null,
  level1Tag: '', // 一级标签筛选
  sort: 'newest',
  keyword: ''
});

const toggleFilters = () => {
  showFilters.value = !showFilters.value;
};

const applyFilters = () => {
  fetchProducts();
};

// 商品数据
const productsLoading = ref(false);
const allProducts = ref([]);
const displayProducts = ref([]);
const currentPage = ref(1);
const pageSize = 12;
const hasMore = ref(true);

// 获取商品列表
const fetchProducts = async () => {
  productsLoading.value = true;
  try {
    const params = {
      page: 1,
      size: pageSize,
      ...filters
    };
    
    const response = await productAPI.getProducts(params);
    
    if (response && response.records && response.records.length > 0) {
      allProducts.value = response.records.map(item => ({
        ...item,
        isHot: item.viewCount > 100,
        isFavorite: false
      }));
      displayProducts.value = allProducts.value;
      currentPage.value = 1;
      hasMore.value = response.records.length === pageSize;
    } else {
      loadMockProducts();
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败，已切换至测试模式');
    loadMockProducts();
  } finally {
    productsLoading.value = false;
  }
};

// 加载更多
const loadMore = async () => {
  if (!hasMore.value || productsLoading.value) return;
  
  productsLoading.value = true;
  try {
    currentPage.value++;
    const params = {
      page: currentPage.value,
      size: pageSize,
      ...filters
    };
    
    const response = await productAPI.getProducts(params);
    
    if (response && response.records) {
      const newProducts = response.records.map(item => ({
        ...item,
        isHot: item.viewCount > 100,
        isFavorite: false
      }));
      
      allProducts.value = [...allProducts.value, ...newProducts];
      displayProducts.value = allProducts.value;
      hasMore.value = response.records.length === pageSize;
    } else {
      hasMore.value = false;
    }
  } catch (error) {
    console.error('加载更多失败:', error);
    hasMore.value = false;
  } finally {
    productsLoading.value = false;
  }
};

// 加载模拟数据（用于测试）
const loadMockProducts = () => {
  const now = Date.now();
  
  const mockData = [
    // 二手物品
    {
      id: 1,
      title: '二手 MacBook Pro，9 成新',
      description: '2020 款，i5 处理器，16G 内存，512G SSD，电池健康度 90%，无划痕，箱说全',
      price: 4500,
      images: ['https://placehold.co/300x200/4A90E2/FFFFFF?text=MacBook'],
      viewCount: 235,
      likeCount: 18,
      categoryId: 7, // 电子设备
      status: 1,
      createdAt: new Date(now - 2 * 3600 * 1000).toISOString(),
      isHot: true,
      seller: {
        id: 101,
        name: '张同学',
        avatar: 'https://placehold.co/100x100/4A90E2/FFFFFF?text=张',
        level1Tag: 'student' // 学生
      }
    },
    {
      id: 2,
      title: 'iPhone 12 二手手机',
      description: '国行在保，128G 黑色，9 成新，轻微使用痕迹，功能完好',
      price: 2800,
      images: ['https://placehold.co/300x200/FF6B6B/FFFFFF?text=iPhone'],
      viewCount: 189,
      likeCount: 15,
      categoryId: 7,
      status: 1,
      createdAt: new Date(now - 5 * 3600 * 1000).toISOString(),
      isHot: true,
      seller: {
        id: 102,
        name: '李同学',
        avatar: 'https://placehold.co/100x100/50C878/FFFFFF?text=李',
        level1Tag: 'student'
      }
    },
    {
      id: 3,
      title: '高等数学教材上下册',
      description: '同济第七版，几乎全新，有少量笔记，适合考研复习',
      price: 25,
      images: ['https://placehold.co/300x200/50C878/FFFFFF?text=数学教材'],
      viewCount: 156,
      likeCount: 8,
      categoryId: 6, // 书籍教材
      status: 1,
      createdAt: new Date(now - 1.5 * 3600 * 1000).toISOString(),
      isHot: true,
      seller: {
        id: 103,
        name: '王同学',
        avatar: 'https://placehold.co/100x100/FF6B6B/FFFFFF?text=王',
        level1Tag: 'student'
      }
    },
    {
      id: 4,
      title: '小米台灯护眼灯',
      description: '买多了，全新未拆封，智能控制，三档调色',
      price: 45,
      images: ['https://placehold.co/300x200/FFA500/FFFFFF?text=台灯'],
      viewCount: 98,
      likeCount: 5,
      categoryId: 8, // 生活用品
      status: 1,
      createdAt: new Date(now - 0.5 * 3600 * 1000).toISOString(),
      isHot: false,
      seller: {
        id: 104,
        name: '赵同学',
        avatar: 'https://placehold.co/100x100/FFA500/FFFFFF?text=赵',
        level1Tag: 'student'
      }
    },
    {
      id: 5,
      title: '罗技 MX Master 3 鼠标',
      description: '用了半年，功能正常，微磨损，手感依然很好',
      price: 350,
      images: ['https://placehold.co/300x200/9B59B6/FFFFFF?text=鼠标'],
      viewCount: 145,
      likeCount: 12,
      categoryId: 7,
      status: 1,
      createdAt: new Date(now - 8 * 3600 * 1000).toISOString(),
      isHot: true,
      seller: {
        id: 105,
        name: '校园数码店',
        avatar: 'https://placehold.co/100x100/9B59B6/FFFFFF?text=店',
        level1Tag: 'merchant' // 商家
      }
    },
    // 服务需求
    {
      id: 6,
      title: '代取快递，校内配送',
      description: '快速便捷，送货上门，大件小件都可接单',
      price: 5,
      images: ['https://placehold.co/300x200/FF6B6B/FFFFFF?text=快递代取'],
      viewCount: 312,
      likeCount: 25,
      categoryId: 9, // 代取快递
      status: 1,
      createdAt: new Date(now - 3 * 3600 * 1000).toISOString(),
      isHot: true
    },
    {
      id: 7,
      title: '校园跑腿服务',
      description: '代买饭、代排队、代办事项，价格面议',
      price: 10,
      images: ['https://placehold.co/300x200/4A90E2/FFFFFF?text=跑腿服务'],
      viewCount: 267,
      likeCount: 20,
      categoryId: 10, // 跑腿服务
      status: 1,
      createdAt: new Date(now - 4 * 3600 * 1000).toISOString(),
      isHot: true
    },
    {
      id: 8,
      title: '电脑清灰换硅脂',
      description: '专业工具，细心操作，让电脑重获新生',
      price: 30,
      images: ['https://placehold.co/300x200/50C878/FFFFFF?text=电脑维护'],
      viewCount: 178,
      likeCount: 14,
      categoryId: 2, // 服务需求
      status: 1,
      createdAt: new Date(now - 6 * 3600 * 1000).toISOString(),
      isHot: true
    },
    // 兼职信息
    {
      id: 9,
      title: '周末家教兼职',
      description: '初中数理化辅导，有经验，耐心负责，周末有时间',
      price: 100,
      images: ['https://placehold.co/300x200/FF9500/FFFFFF?text=家教'],
      viewCount: 423,
      likeCount: 35,
      categoryId: 3,
      status: 1,
      createdAt: new Date(now - 10 * 3600 * 1000).toISOString(),
      isHot: true
    },
    {
      id: 10,
      title: '图书馆整理员',
      description: '学校图书馆招聘学生助理，工作时间灵活',
      price: 15,
      images: ['https://placehold.co/300x200/4A90E2/FFFFFF?text=图书馆'],
      viewCount: 389,
      likeCount: 28,
      categoryId: 3,
      status: 1,
      createdAt: new Date(now - 12 * 3600 * 1000).toISOString(),
      isHot: true
    },
    // 拼车出行
    {
      id: 11,
      title: '寻找拼车去火车站',
      description: '本周五下午出发，还有 2 个空位',
      price: 30,
      images: ['https://placehold.co/300x200/50C878/FFFFFF?text=拼车'],
      viewCount: 201,
      likeCount: 16,
      categoryId: 4,
      status: 1,
      createdAt: new Date(now - 1 * 3600 * 1000).toISOString(),
      isHot: true
    },
    {
      id: 12,
      title: '清明节回家拼车',
      description: '4 月 3 日上午出发，目的地市区，可送到小区',
      price: 50,
      images: ['https://placehold.co/300x200/FF6B6B/FFFFFF?text=清明拼车'],
      viewCount: 167,
      likeCount: 13,
      categoryId: 4,
      status: 1,
      createdAt: new Date(now - 7 * 3600 * 1000).toISOString(),
      isHot: true
    },
    // 美食外卖
    {
      id: 13,
      title: '自制手工饼干',
      description: '纯手工制作，无添加，多种口味可选，送礼自用两相宜',
      price: 25,
      images: ['https://placehold.co/300x200/FFB6C1/FFFFFF?text=饼干'],
      viewCount: 289,
      likeCount: 22,
      categoryId: 5,
      status: 1,
      createdAt: new Date(now - 9 * 3600 * 1000).toISOString(),
      isHot: true
    },
    {
      id: 14,
      title: '家乡特产腊肠',
      description: '正宗四川风味，麻辣鲜香，真空包装，保证新鲜',
      price: 35,
      images: ['https://placehold.co/300x200/DC143C/FFFFFF?text=腊肠'],
      viewCount: 234,
      likeCount: 19,
      categoryId: 5,
      status: 1,
      createdAt: new Date(now - 11 * 3600 * 1000).toISOString(),
      isHot: true
    },
    // 已售出商品
    {
      id: 15,
      title: 'Switch 游戏机（已售出）',
      description: '国行续航版，已破解，送 10 款热门游戏，成色新',
      price: 1800,
      images: ['https://placehold.co/300x200/E6E6FA/FFFFFF?text=Switch'],
      viewCount: 567,
      likeCount: 45,
      categoryId: 7,
      status: 2, // 已售出
      createdAt: new Date(now - 15 * 3600 * 1000).toISOString(),
      isHot: true
    },
    // 下架商品
    {
      id: 16,
      title: '某考研资料（已下架）',
      description: '因版权问题暂时下架',
      price: 50,
      images: ['https://placehold.co/300x200/CCCCCC/FFFFFF?text=资料'],
      viewCount: 123,
      likeCount: 10,
      categoryId: 6,
      status: 0, // 已下架
      createdAt: new Date(now - 20 * 3600 * 1000).toISOString(),
      isHot: true
    }
  ];
  
  console.log('📦 加载模拟商品数据:', mockData.length, '个');
  allProducts.value = mockData;
  displayProducts.value = mockData;
  hasMore.value = false;
  productsLoading.value = false;
};

// 选择分类
const selectCategory = (categoryId) => {
  filters.categoryId = categoryId === filters.categoryId ? null : categoryId;
  fetchProducts();
};

// 跳转到商品详情
const goToProduct = (product) => {
  console.log('🔍 点击商品:', product);
  console.log('🆔 商品 ID:', product.id);
  
  if (!product.id) {
    console.error('❌ 商品 ID 不存在');
    ElMessage.warning('商品信息不完整');
    return;
  }
  
  const targetPath = `/product/${product.id}`;
  console.log('🚀 准备跳转到:', targetPath);
  
  router.push(targetPath);
};

// 处理收藏变化
const handleFavoriteChange = (data) => {
  console.log('收藏变化:', data);
  // 实际项目中调用 API
};

// 发布商品
const createNewTrade = () => {
  router.push('/publish');
};

// 页面跳转
const goToPage = (page) => {
  switch(page) {
    case 'home':
      router.push('/');
      break;
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
    default:
      console.log(`未知页面：${page}`);
  }
};

onMounted(() => {
  updateDeviceDetection();
  getUserInfo();
  fetchMallAds();
  fetchProducts();
  handleUrlTags();
  window.addEventListener('resize', updateDeviceDetection);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceDetection);
});

// 监听路由参数变化（处理活动广告跳转后的筛选）
watch(() => route.query.tags, (newTags, oldTags) => {
  if (newTags && newTags !== oldTags) {
    handleUrlTags();
  }
}, { immediate: true });
</script>

<style scoped>
.mall-page {
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

.header {
  background-color: #4A90E2;
  color: white;
  padding: 15px 30px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header h1 {
  margin: 0 0 15px 0;
  font-size: 1.8rem;
}

.search-bar {
  display: flex;
  gap: 10px;
  width: 100%;
}

.desktop-search {
  display: flex;
  gap: 10px;
  flex: 1;
}

.desktop-search input {
  flex: 1;
  padding: 10px;
  border: 2px solid #e1e5f2;
  border-radius: 8px;
  font-size: 1rem;
}

.desktop-search button {
  padding: 10px 20px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.desktop-search button:hover {
  background-color: #764ba2;
}

.mobile-search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.user-avatar-mini img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid white;
}

.mobile-search-header input {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #e1e5f2;
  border-radius: 20px;
  font-size: 0.9rem;
}

/* 分类区域 */
.categories-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.categories-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.categories-section h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr); /* 从 5 列改为 6 列，确保两行显示完 11 个分类 */
  gap: 8px; /* 保持紧凑间距 */
}

.category-item {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 3px; /* 进一步减小 padding 到 3px */
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.category-item:hover {
  transform: translateY(-1px); /* 悬停效果更轻微 */
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  background: #e9f7fe;
  border-color: #4A90E2;
}

.category-icon {
  font-size: 1.1rem; /* 图标继续减小 */
  margin-bottom: 2px; /* 间距更小 */
  display: block;
}

.category-item h3 {
  margin: 2px 0 2px; /* 间距进一步减小 */
  font-size: 0.65rem; /* 标题文字更小 */
  color: #333;
}

.category-item p {
  margin: 0;
  font-size: 0.55rem; /* 描述文字极小化 */
  color: #666;
}

.category-mobile-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 5px;
}

.category-mobile-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px 5px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-mobile-item .category-icon {
  font-size: 1.2rem;
  margin-bottom: 3px;
}

.category-mobile-item .category-name {
  font-size: 0.7rem;
  color: #333;
}

/* 商品列表区域 */
.products-section {
  background: white;
  margin: 15px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.products-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.products-section .header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.level1-tag-filter {
  width: 120px;
}

.publish-btn {
  padding: 10px 20px;
  background-color: #4A90E2;
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.3);
}

.publish-btn:hover {
  background-color: #5a9fd6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
}

.products-section h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.filters-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-toggle-btn {
  padding: 8px 15px;
  background-color: white;
  color: #4A90E2;
  border: 2px solid #4A90E2;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.filter-toggle-btn:hover {
  background-color: #4A90E2;
  color: white;
}

/* 移动端悬浮按钮 */
.mobile-fab {
  position: fixed;
  bottom: 90px;
  right: 20px;
  z-index: 1000;
}

.mobile-fab button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #4A90E2;
  color: white;
  border: none;
  font-size: 1.5rem;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;
}

.mobile-fab button:hover {
  transform: scale(1.1);
}

/* 桌面端底部 */
.desktop-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 15px;
  margin: 15px;
  border-radius: 8px;
}

/* 移动端适配 */
.mall-page.mobile .header {
  padding: 15px 20px;
}

.mall-page.mobile .header h1 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.mall-page.mobile .category-grid,
.mall-page.mobile .category-mobile-grid {
  gap: 8px;
}

.mall-page.mobile .filters-bar {
  flex-direction: column;
  gap: 10px;
}
</style>
