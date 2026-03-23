# 广告功能快速使用指南

## 一、数据库配置（必须首先执行）

### 1. 执行 SQL 升级脚本

```bash
# 在数据库中执行
mysql -u your_username -p your_password campus_db < database_advertisements_upgrade.sql
```

该脚本会：
- ✅ 添加 `ad_type` 字段（广告类型）
- ✅ 添加 `related_id` 字段（关联 ID）
- ✅ 添加 `filter_tags` 字段（筛选标签）
- ✅ 创建相应索引
- ✅ 插入 5 条测试数据

### 2. 验证数据

```sql
-- 查看广告总数
SELECT COUNT(*) FROM advertisements;

-- 查看各类型广告数量
SELECT ad_type, COUNT(*) as count 
FROM advertisements 
GROUP BY ad_type;

-- 查看所有广告的详细信息
SELECT id, title, ad_type, related_id, position, is_active 
FROM advertisements 
ORDER BY sort_order;
```

## 二、启动项目

### 1. 启动后端

```bash
cd F:\code\CampusInformationPlatform\demo
mvn spring-boot:run
```

### 2. 启动前端

```bash
cd F:\code\CampusInformationPlatform\demo
npm run dev
```

## 三、管理端操作

### 1. 登录管理后台

访问：`http://localhost:5173`（或你的前端端口）
- 使用管理员账号登录
- 进入"系统管理" → "广告管理"

### 2. 创建广告示例

#### 示例 1：商品广告

```
广告标题：学霸笔记合集
广告图片：https://placehold.co/800x300/50C878/FFFFFF?text=学霸笔记
跳转链接：/product/1
广告内容：各科重点整理，考试必备资料
广告位置：交易中心
广告类型：商品广告 ⭐
关联 ID：1（商品 ID）
排序：1
是否启用：是
开始时间：2026-03-23
结束时间：2026-04-23
```

#### 示例 2：商家广告

```
广告标题：校园快递代取服务
广告图片：https://placehold.co/800x300/4A90E2/FFFFFF?text=快递代取
跳转链接：/user/1001
广告内容：快速便捷的快递代取服务
广告位置：交易中心
广告类型：商家页面 ⭐
关联 ID：1001（商家 ID）
排序：2
是否启用：是
开始时间：2026-03-23
结束时间：2026-05-23
```

#### 示例 3：活动标签广告

```
广告标题：毕业季二手交易特卖
广告图片：https://placehold.co/800x300/FF6B6B/FFFFFF?text=毕业季特卖
跳转链接：/mall?tag=secondhand
广告内容：毕业季特价商品，低至 3 折起
广告位置：交易中心
广告类型：活动标签筛选 ⭐
筛选标签：{"tags": ["secondhand", "books", "digital"]}
排序：3
是否启用：是
开始时间：2026-03-23
结束时间：2026-04-23
```

**注意**：筛选标签必须为 JSON 格式，例如：
```json
{"tags": ["secondhand", "books"]}
{"tags": ["food"]}
{"tags": ["digital", "electronics"]}
```

## 四、前端效果验证

### 1. 访问交易中心

访问：`http://localhost:5173/mall`

### 2. 检查广告展示

- ✅ 页面顶部应显示广告轮播区域
- ✅ 广告应该自动轮播（每 5 秒）
- ✅ 点击左右箭头可手动切换
- ✅ 点击指示器可跳转到指定广告

### 3. 测试广告点击

**商品广告**：
- 点击后应跳转到商品详情页
- URL 格式：`/product/{id}`

**商家广告**：
- 点击后应跳转到商家主页
- URL 格式：`/user/{id}`

**活动标签广告**：
- 点击后应跳转到交易中心并自动筛选标签
- URL 格式：`/mall?tags={tag1},{tag2}`

### 4. 检查控制台日志

打开浏览器开发者工具，应看到类似日志：

```
🏪 交易中心页面已加载
📢 广告响应：{...}
✅ 加载广告数据：5 个
🖱️ 广告被点击：{...}
👁️ 广告展示次数 +1
```

## 五、数据统计验证

### 1. 查看点击次数

在管理后台广告列表中，可以看到每个广告的"点击次数"列。

### 2. 查看展示次数

每次广告展示 3 秒以上，展示次数会 +1。

### 3. 验证方法

```sql
-- 查看广告的点击和展示统计
SELECT 
    id,
    title,
    ad_type,
    click_count,
    view_count,
    (click_count * 100.0 / NULLIF(view_count, 0)) as click_rate
FROM advertisements
ORDER BY click_rate DESC;
```

## 六、常见问题排查

### 问题 1：广告不显示

**可能原因**：
- 数据库表结构未更新
- 广告状态为禁用
- 广告不在有效期内

**解决方法**：
```sql
-- 检查广告状态
SELECT id, title, is_active, start_time, end_time 
FROM advertisements 
WHERE position = 'mall';

-- 启用广告
UPDATE advertisements 
SET is_active = 1 
WHERE id = {广告 ID};
```

### 问题 2：点击后跳转错误

**可能原因**：
- 广告类型选择错误
- 关联 ID 填写错误
- 筛选标签 JSON 格式错误

**解决方法**：
1. 检查广告类型是否正确
2. 确认关联 ID 对应的商品/用户存在
3. 验证 JSON 格式：`{"tags": ["tag1", "tag2"]}`

### 问题 3：后端接口 404

**可能原因**：
- 后端未正确启动
- 接口路径错误

**解决方法**：
```bash
# 检查后端日志
# 确认 AdvertisementController 已加载
# 访问 http://localhost:8080/api/advertisements/mall 测试接口
```

### 问题 4：前端请求失败

**可能原因**：
- CORS 跨域问题
- API 路径配置错误

**解决方法**：
检查 `src/config/api.js` 中的 API 基础路径配置。

## 七、调试技巧

### 1. 后端调试

在 `AdvertisementController.java` 中添加日志：

```java
log.info("📢 获取广告列表，position={}", position);
log.info("📦 查询到{}条广告", advertisements.size());
```

### 2. 前端调试

在浏览器控制台中：

```javascript
// 查看广告数据
console.log('广告数据:', this.mallAds);

// 手动调用广告接口
fetch('/api/advertisements/mall')
  .then(res => res.json())
  .then(data => console.log('后端返回:', data));
```

### 3. 数据库调试

```sql
-- 实时监控广告数据变化
SELECT 
    id,
    title,
    ad_type,
    click_count,
    view_count,
    updated_at
FROM advertisements
ORDER BY updated_at DESC
LIMIT 1;
```

## 八、性能优化建议

### 1. 广告缓存

可在后端添加 Redis 缓存：

```java
@Cacheable(value = "ads", key = "#position")
public List<Advertisement> getAdvertisementsByPosition(String position) {
    // ...
}
```

### 2. 图片优化

- 使用 WebP 格式
- 压缩图片大小
- 使用 CDN 加速

### 3. 预加载

在 AdBanner 组件中预加载下一张图片：

```javascript
const preloadImage = (url) => {
  const img = new Image();
  img.src = url;
};
```

## 九、扩展功能建议

1. **广告素材库**：上传图片自动保存到服务器
2. **智能推荐**：根据用户行为推荐相关广告
3. **A/B 测试**：同时展示多个版本测试效果
4. **定时任务**：自动上下线到期广告
5. **数据报表**：生成广告效果分析报表

## 十、技术支持

如遇到问题，请检查：
1. ✅ 数据库表结构是否正确
2. ✅ 后端日志是否有错误
3. ✅ 前端控制台是否有报错
4. ✅ 网络请求是否成功

---

**祝使用愉快！** 🎉
