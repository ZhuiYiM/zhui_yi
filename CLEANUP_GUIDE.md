# 项目清理指南

## 📋 文件分类与清理建议

### ✅ 保留文件（重要）

#### 1. 项目配置文件
- `pom.xml` - Maven 配置
- `package.json` - Node.js 配置
- `vite.config.js` - Vite 构建配置
- `.gitignore` - Git 忽略配置
- `jsconfig.json` - JS 配置
- `index.html` - 入口文件

#### 2. 源代码
- `src/` - 所有源代码
- `public/` - 静态资源

#### 3. 核心文档
- `README.md` - 项目主文档
- `docs/` - 文档目录（如果有用）

#### 4. 测试文件
- `tests/` - 前端测试
- `src/test/` - 后端测试

---

### 🗑️ 可删除文件（冗余/临时文件）

#### 1. 构建产物（已生成，可删除）
- `dist/` - 构建输出目录
- `target/` - Maven 构建输出

#### 2. IDE 配置文件（个人配置，不需要提交）
- `.idea/` - IntelliJ IDEA 配置
- `.vscode/` - VSCode 配置（除非有团队统一配置）

#### 3. 临时 SQL 脚本（已执行的修复脚本）
以下是一次性修复脚本，执行后不需要保留：
- `check_and_fix_tags.sql`
- `check_and_fix_user_identity.sql`
- `check_identity_tags_issue.sql`
- `check_tag_query.sql`
- `check_tag_tables_status.sql`
- `check_user_identity_status.sql`
- `cleanup_duplicate_tags.sql`
- `diagnose_tag_system.sql`
- `fix_admin_password.sql`
- `fix_tag_tables.sql`
- `fix_tag_tables_structure.sql`
- `fix_tags_simple.sql`
- `fix_topic_tags.sql`
- `fix_user_identity_data.sql`
- `fix_user_identity_for_organization.sql`
- `remove_product_tags_from_publish.sql`
- `update_product_share_tags.sql`
- `verify_admin_tag_system.sql`
- `verify_identity_display.sql`
- `verify_identity_system.sql`
- `verify_topic_data.sql`

#### 4. 临时测试脚本
- `test_admin_tag_management.sql`
- `test_forward_topic_data.sql`
- `test_map_feature.sql`
- `test_product_forward_feature.sql`
- `test_product_share_favorite.sql`
- `test_products_simple.sql`
- `test_user_identity_update.sql`

#### 5. 重复的 SQL 迁移脚本（保留最终的，删除中间的）
以下脚本可能已被后续脚本替代，需要检查：
- `database_tags_refactor.sql` → 可能被 `database_tags_refactor_simple.sql` 替代
- `database_advertisements.sql` → 可能被 `database_advertisements_upgrade.sql` 替代

#### 6. 过多的功能文档（保留主要的，删除过程性的）

**建议保留的核心文档：**
- `README.md` ✅
- `ADMIN_SYSTEM_README.md` ✅
- `ADVERTISEMENTS_FEATURE_README.md` ✅
- `MAP_FEATURE_README.md` ✅
- `USER_LOCATION_MARK_FEATURE_README.md` ✅

**可以删除的过程性文档：**
- `ADMIN_SYSTEM_TEST_README.md` - 测试过程文档
- `ADMIN_SYSTEM_TEST_REPORT.md` - 测试报告
- `ADMIN_TAG_SYSTEM_COMPLETE_REPORT.md` - 完成报告
- `CODE_QUALITY_ANALYSIS_REPORT.md` - 质量分析报告
- `FUNCTIONAL_ANALYSIS_AND_PREPARATION.md` - 功能分析
- `GIT_UPLOAD_SUMMARY.md` - Git 上传总结
- `IDENTITY_FILES_CHECKLIST.md` - 检查清单
- `MAP_PREVIEW_TODO.md` - TODO 列表
- `PRODUCT_SHARE_QUICK_VERIFICATION.md` - 快速验证
- `PRODUCT_SHARE_TAG_UPDATE_README.md` - 标签更新说明
- `QUICK_CLEANUP_GUIDE.md` - 清理指南（已有本文件）
- `QUICK_VERIFICATION_PRODUCT_TAGS.md` - 验证指南
- `QUICK_VERIFICATION_SCRIPT.md` - 验证脚本
- `REMOVE_PRODUCT_TAGS_SUMMARY.md` - 删除总结
- `SPECIFICATIONS_FEATURE_README.md` - 规格说明
- `TOPIC_WALL_FIX_REPORT.md` - 修复报告
- `USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md` - 实现总结
- `标签体系检查报告.md` - 检查报告
- `标签体系重构方案.md` - 重构方案
- `标签系统修复总结.md` - 修复总结
- `话题标签显示修复报告.md` - 修复报告

**建议保留的标签体系文档（整合成一个）：**
- `标签体系快速指南.md` → 保留
- `标签体系总结报告.md` → 保留
- 其他删除

#### 7. Postman 集合文件（可选）
- `admin-system.postman_collection.json` - 可选保留
- `map_api.postman_collection.json` - 可选保留
- `tests/message_center.postman_collection.json` - 可选保留
- `tests/user-location-marks.postman_collection.json` - 可选保留

#### 8. 其他临时文件
- `backend_tables.txt` - 表结构说明（如果已过时）
- `generate_test_data.py` - 测试数据生成脚本（如果不再需要）
- `*.sql` 未列出的其他临时脚本

---

### 📦 需要整合的文件

#### 1. 数据库迁移脚本整合
创建一个统一的迁移脚本：
```
database/
├── V1__initial_schema.sql
├── V2__add_identity_system.sql
├── V3__add_advertisements.sql
├── V4__add_map_locations.sql
└── V5__add_user_verification.sql
```

#### 2. 文档整合
- 将所有功能文档整合到 `docs/` 目录
- 创建统一的开发者指南

---

##  清理步骤

### 第一步：删除构建产物
```bash
# 删除构建输出
rm -rf dist/
rm -rf target/
```

### 第二步：删除 IDE 配置
```bash
# 删除 IDE 配置（谨慎！如果需要保留团队配置）
rm -rf .idea/
rm -rf .vscode/
```

### 第三步：删除临时 SQL 脚本
```bash
# 删除一次性修复脚本
rm check_and_fix_*.sql
rm fix_*.sql (保留必要的)
rm verify_*.sql
rm test_*.sql
rm cleanup_*.sql
rm diagnose_*.sql
```

### 第四步：清理文档
```bash
# 删除过程性文档
rm *_REPORT.md
rm *_SUMMARY.md
rm *_TODO.md
rm *_CHECKLIST.md
rm QUICK_*.md
rm *_SCRIPT.md
```

### 第五步：整理 SQL 脚本
```bash
# 创建数据库脚本目录
mkdir -p database/migration

# 移动重要的迁移脚本
mv database_*.sql database/migration/
mv init_*.sql database/migration/
```

---

## ✅ 清理后检查清单

- [ ] 删除 `dist/` 和 `target/`
- [ ] 删除 `.idea/` 和 `.vscode/`（如不需要）
- [ ] 删除所有一次性 SQL 修复脚本
- [ ] 删除测试 SQL 脚本
- [ ] 删除过程性文档（*_REPORT.md, *_SUMMARY.md 等）
- [ ] 保留核心功能文档
- [ ] 整合数据库迁移脚本
- [ ] 更新 `README.md`
- [ ] 检查 `.gitignore` 是否完整

---

## 📝 最终保留的文件结构

```
demo/
├── .git/
├── .gitignore
├── .mvn/
├── src/                      # ✅ 源代码
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── public/                   # ✅ 静态资源
├── tests/                    # ✅ 测试文件
├── docs/                     # ✅ 文档目录（新建）
│   ├── admin/
│   ├── features/
│   └── database/
├── database/                 # ✅ 数据库脚本（新建）
│   └── migration/
├── pom.xml                   # ✅
├── package.json              # ✅
├── vite.config.js            # ✅
├── README.md                 # ✅
├── ADMIN_SYSTEM_README.md    # ✅
├── ADVERTISEMENTS_FEATURE_README.md  # ✅
└── ... (其他核心文档)
```

---

## ⚠️ 注意事项

1. **删除前备份**：建议先提交到 Git，再删除文件
2. **检查依赖**：确保删除的文件没有被引用
3. **团队沟通**：如果是团队项目，先确认是否需要某些文件
4. **SQL 脚本**：确保所有必要的数据库变更已应用到生产环境

---

## 🚀 清理后上传 GitHub

### 1. 检查 Git 状态
```bash
git status
```

### 2. 添加所有变更
```bash
git add .
```

### 3. 提交清理
```bash
git commit -m "refactor: 清理项目冗余文件，优化项目结构"
```

### 4. 推送到 GitHub
```bash
git push origin main
```

---

生成时间：2026-03-26
