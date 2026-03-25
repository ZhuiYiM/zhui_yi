# 地点分享功能实现说明

## 功能概述

实现了地点详情页的完整分享功能，包括：
1. **复制链接** - 复制地点链接到剪贴板
2. **转发到话题墙** - 跳转到话题墙并发布带地点信息的话题

## 修改的文件

### 1. 新增组件
- `src/components/map/LocationShareModal.vue` - 地点分享弹窗组件

### 2. 修改文件
- `src/components/map/LocationDetail.vue` - 地点详情页
  - 添加分享弹窗引用
  - 修改 `shareLocation()` 函数逻辑
  - 添加 `handleCopyLink()`、`handleRepost()`、`handleShareClose()` 处理函数
  - 添加 `currentLocationUrl` 计算属性

- `src/components/topic/PublishTopicModal.vue` - 发布话题弹窗
  - 支持地点分享类型显示
  - 添加 `getSourceTypeIcon()` 和 `getSourceTypeTitle()` 辅助函数
  - 添加地点分享样式

## 功能流程

### 复制链接流程
```
用户点击"分享地点" 
  → 打开分享弹窗 
  → 点击"复制链接" 
  → 复制地点链接到剪贴板 
  → 显示成功提示 
  → 关闭弹窗
```

### 转发到话题墙流程
```
用户点击"分享地点" 
  → 打开分享弹窗 
  → 点击"转发" 
  → 生成分享数据（包含地点信息） 
  → 存储到 sessionStorage 
  → 跳转到 /topicwall?from=share&sourceType=location&sourceId={locationId} 
  → 自动打开发布弹窗 
  → 显示地点推荐预览 
  → 用户补充内容后发布
```

## 分享数据结构

```javascript
{
  sourceType: 'location',          // 分享来源类型
  sourceId: location.id,           // 地点 ID
  content: '我发现了一个好地方：{地点名称}',  // 默认内容
  author: '用户名',                 // 作者名
  url: '/location/{locationId}',   // 地点链接
  originalLocationId: location.id, // 原始地点 ID
  originalLocationName: location.name // 原始地点名称
}
```

## 样式特点

### 地点分享提示框
- 📍 图标标识
- 绿色渐变背景（区别于商品分享的红色）
- 显示"地点推荐预览"标题
- 提示"转发时会自动添加'地点推荐'标签，无需手动选择"

## 测试步骤

### 1. 测试复制链接
1. 打开地点详情页（如 `/location/1`）
2. 点击"📤 分享地点"按钮
3. 点击"复制链接"选项
4. 检查是否显示"地点链接已复制到剪贴板"提示
5. 粘贴验证是否为正确的地点链接

### 2. 测试转发到话题墙
1. 打开地点详情页
2. 点击"📤 分享地点"按钮
3. 点击"转发"选项
4. 检查是否跳转到话题墙页面，URL 包含正确的 query 参数
5. 检查是否自动打开发布弹窗
6. 检查是否显示地点推荐预览信息（绿色边框）
7. 输入补充内容后点击发布
8. 验证发布成功后话题列表中包含新话题

### 3. 测试不同状态
- ✅ 未登录状态：可以分享，但作者显示为"匿名用户"
- ✅ 已登录状态：显示真实用户名
- ✅ 地点信息不完整：显示"地点信息不完整"提示

## 技术要点

### 1. 路由跳转
使用 `router.push()` 跳转到话题墙，携带 query 参数：
```javascript
router.push({
  path: '/topicwall',
  query: {
    from: 'share',
    sourceType: 'location',
    sourceId: location.value.id
  }
})
```

### 2. 数据持久化
使用 `sessionStorage` 存储分享数据，确保页面刷新后数据仍然存在：
```javascript
sessionStorage.setItem('shareData', JSON.stringify(shareData))
```

### 3. 用户信息获取
从 JWT Token 中解析用户信息：
```javascript
const token = localStorage.getItem('token')
const userPayload = JSON.parse(atob(token.split('.')[1]))
const authorName = userPayload.realName || userPayload.username
```

### 4. 话题墙自动处理
话题墙组件在 `onMounted` 时检查 URL 参数，自动打开分享弹窗：
```javascript
if (routeParams.from === 'share' && routeParams.sourceId) {
  const shareData = sessionStorage.getItem('shareData')
  shareInfo.value = { ...parsedData, sourceType: 'location', sourceId: routeParams.sourceId }
  showPublishModal.value = true
}
```

## 注意事项

1. **地点 ID 必须有效** - 确保地点详情页有有效的 `location.id`
2. **Token 有效性** - 已登录用户需要有效的 JWT Token
3. **sessionStorage 清理** - 分享完成后由话题墙组件负责清理 URL 参数
4. **样式一致性** - 地点分享使用绿色主题，与商品分享（红色）、话题分享（蓝色）区分

## 兼容性

- ✅ 支持话题、商品、地点三种分享类型
- ✅ 支持移动端和桌面端
- ✅ 支持已登录和未登录状态
- ✅ 向后兼容现有的话题分享和商品分享功能

## 未来优化

- [ ] 添加地点图片预览到分享弹窗
- [ ] 支持自定义分享文案
- [ ] 添加分享历史记录
- [ ] 支持分享到站外平台（微信、QQ 等）
