# 地图导引功能 - 快速启动指南

## 🚀 5 分钟快速部署

### 步骤 1: 数据库初始化 (2 分钟)

```bash
# 连接到 MySQL 数据库
mysql -u root -p

# 创建并使用数据库
USE campus_db;

# 执行初始化脚本
source F:/code/CampusInformationPlatform/demo/database_campuses_locations.sql;

# 验证数据（可选）
source F:/code/CampusInformationPlatform/demo/test_map_feature.sql;
```

### 步骤 2: 配置地图 API Key (1 分钟)

编辑 `src/utils/mapHelper.js` 文件，替换以下占位符：

```javascript
// 百度地图 - 访问 https://lbsyun.baidu.com/ 获取 AK
scriptUrl: 'https://api.map.baidu.com/api?v=3.0&ak=YOUR_BAIDU_AK'

// 高德地图 - 访问 https://lbs.amap.com/ 获取 Key
scriptUrl: 'https://webapi.amap.com/maps?v=2.0&key=YOUR_GAODE_KEY'

// 腾讯地图 - 访问 https://lbs.qq.com/ 获取 Key
scriptUrl: 'https://map.qq.com/api/gljs?v=1.exp&key=YOUR_TENCENT_KEY'
```

💡 **提示**: 如果暂时没有 API Key，可以先使用占位值，地图会显示为灰色背景，但不影响其他功能。

### 步骤 3: 启动后端服务 (1 分钟)

```bash
# 进入项目目录
cd F:\code\CampusInformationPlatform\demo

# 启动 Spring Boot 应用
mvn spring-boot:run

# 或者使用 IDEA 直接运行 DemoApplication.java
```

后端服务将在 http://localhost:8080 启动

### 步骤 4: 启动前端服务 (1 分钟)

```bash
# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173/map
```

## ✅ 验证功能

### 基础验证清单

- [ ] 能够看到三个校区选项（本部、平原湖、创新港）
- [ ] 切换校区时地图会更新
- [ ] 能够在下拉框中选择不同的地图服务商
- [ ] 热门地点卡片正常显示
- [ ] 搜索框可以输入地点名称

### API 测试

使用 Postman 导入 `map_api.postman_collection.json` 测试接口：

```bash
# 测试校区列表
GET http://localhost:8080/api/campuses

# 预期响应
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "本部校区",
      "code": "main"
    },
    ...
  ]
}
```

## 🔧 常见问题

### Q1: 地图显示空白
**解决方案**:
1. 检查浏览器控制台是否有错误
2. 验证 API Key 是否正确
3. 确认网络连接正常
4. 尝试切换其他地图服务商

### Q2: 后端启动失败
**解决方案**:
1. 检查端口 8080 是否被占用
2. 确认数据库连接配置正确
3. 查看 application.properties 配置

### Q3: 前端页面无法访问
**解决方案**:
1. 确认 npm install 成功
2. 检查端口 5173 是否被占用
3. 清除浏览器缓存

### Q4: 数据库表已存在
**解决方案**:
```sql
-- 删除旧表（谨慎操作！）
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS campuses;

-- 重新执行初始化脚本
```

## 📊 数据预览

初始化后，数据库将包含：

### 校区数据
- 本部校区 (12 个地点)
- 平原湖校区 (11 个地点)
- 创新港校区 (9 个地点)

### 地点分类
- 🏫 教学楼
- 📚 图书馆
- 🍽️ 食堂
- 🏠 宿舍
- ⚽ 体育设施
- 🏢 行政楼
- 🔬 实验楼
- ☕ 咖啡厅
- 🏥 校医院
- 💡 创新中心

## 🎯 下一步

功能部署完成后，可以：

1. **添加更多地点**: 编辑 `database_campuses_locations.sql`
2. **自定义样式**: 修改 `Map.vue` 组件的 CSS
3. **扩展功能**: 参考 `MAP_FEATURE_README.md` 中的扩展建议
4. **性能优化**: 添加地点搜索索引、缓存等

## 📞 技术支持

如遇到问题，请检查：

1. 浏览器开发者工具的控制台
2. 后端日志输出
3. 数据库连接状态
4. 网络请求响应

---

**祝使用愉快！** 🎉
