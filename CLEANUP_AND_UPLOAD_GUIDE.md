# Git 上传前清理脚本

## 🚨 重要提示

此脚本将删除所有临时文件、构建产物和冗余文档。
执行前请确保：
1. 所有代码已提交到 Git
2. 已备份重要文件
3. 团队成员已确认

---

## 📋 清理步骤（手动执行）

### 步骤 1：删除构建产物
```powershell
# 删除前端构建产物
Remove-Item -Recurse -Force dist

# 删除后端构建产物
Remove-Item -Recurse -Force target
```

### 步骤 2：从 Git 中移除 uploads 目录（因为是用户上传的测试图片）
```powershell
# 从 Git 历史中移除 uploads 目录，但保留本地文件
git rm -r --cached uploads/

# 确认移除
git status
```

### 步骤 3：删除临时 SQL 文件
```powershell
# 删除一次性修复脚本
Remove-Item check_and_fix_tags.sql -ErrorAction SilentlyContinue
Remove-Item check_and_fix_user_identity.sql -ErrorAction SilentlyContinue
Remove-Item check_identity_tags_issue.sql -ErrorAction SilentlyContinue
Remove-Item check_tag_query.sql -ErrorAction SilentlyContinue
Remove-Item check_tag_tables_status.sql -ErrorAction SilentlyContinue
Remove-Item check_user_identity_status.sql -ErrorAction SilentlyContinue
Remove-Item cleanup_duplicate_tags.sql -ErrorAction SilentlyContinue
Remove-Item diagnose_tag_system.sql -ErrorAction SilentlyContinue
Remove-Item fix_admin_password.sql -ErrorAction SilentlyContinue
Remove-Item fix_tag_tables.sql -ErrorAction SilentlyContinue
Remove-Item fix_tag_tables_structure.sql -ErrorAction SilentlyContinue
Remove-Item fix_tags_simple.sql -ErrorAction SilentlyContinue
Remove-Item fix_topic_tags.sql -ErrorAction SilentlyContinue
Remove-Item fix_user_identity_data.sql -ErrorAction SilentlyContinue
Remove-Item fix_user_identity_for_organization.sql -ErrorAction SilentlyContinue
Remove-Item remove_product_tags_from_publish.sql -ErrorAction SilentlyContinue
Remove-Item update_product_share_tags.sql -ErrorAction SilentlyContinue
Remove-Item verify_admin_tag_system.sql -ErrorAction SilentlyContinue
Remove-Item verify_identity_display.sql -ErrorAction SilentlyContinue
Remove-Item verify_identity_system.sql -ErrorAction SilentlyContinue
Remove-Item verify_topic_data.sql -ErrorAction SilentlyContinue

# 删除测试 SQL 脚本
Remove-Item test_admin_tag_management.sql -ErrorAction SilentlyContinue
Remove-Item test_forward_topic_data.sql -ErrorAction SilentlyContinue
Remove-Item test_map_feature.sql -ErrorAction SilentlyContinue
Remove-Item test_product_forward_feature.sql -ErrorAction SilentlyContinue
Remove-Item test_product_share_favorite.sql -ErrorAction SilentlyContinue
Remove-Item test_products_simple.sql -ErrorAction SilentlyContinue
Remove-Item test_user_identity_update.sql -ErrorAction SilentlyContinue
```

### 步骤 4：删除冗余文档
```powershell
# 删除过程性文档
Remove-Item ADMIN_SYSTEM_TEST_README.md -ErrorAction SilentlyContinue
Remove-Item ADMIN_SYSTEM_TEST_REPORT.md -ErrorAction SilentlyContinue
Remove-Item ADMIN_TAG_SYSTEM_COMPLETE_REPORT.md -ErrorAction SilentlyContinue
Remove-Item CODE_QUALITY_ANALYSIS_REPORT.md -ErrorAction SilentlyContinue
Remove-Item FUNCTIONAL_ANALYSIS_AND_PREPARATION.md -ErrorAction SilentlyContinue
Remove-Item GIT_UPLOAD_SUMMARY.md -ErrorAction SilentlyContinue
Remove-Item IDENTITY_FILES_CHECKLIST.md -ErrorAction SilentlyContinue
Remove-Item MAP_PREVIEW_TODO.md -ErrorAction SilentlyContinue
Remove-Item PRODUCT_SHARE_QUICK_VERIFICATION.md -ErrorAction SilentlyContinue
Remove-Item PRODUCT_SHARE_TAG_UPDATE_README.md -ErrorAction SilentlyContinue
Remove-Item QUICK_CLEANUP_GUIDE.md -ErrorAction SilentlyContinue
Remove-Item QUICK_VERIFICATION_PRODUCT_TAGS.md -ErrorAction SilentlyContinue
Remove-Item QUICK_VERIFICATION_SCRIPT.md -ErrorAction SilentlyContinue
Remove-Item REMOVE_PRODUCT_TAGS_SUMMARY.md -ErrorAction SilentlyContinue
Remove-Item SPECIFICATIONS_FEATURE_README.md -ErrorAction SilentlyContinue
Remove-Item TOPIC_WALL_FIX_REPORT.md -ErrorAction SilentlyContinue
Remove-Item USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md -ErrorAction SilentlyContinue
Remove-Item 标签体系检查报告.md -ErrorAction SilentlyContinue
Remove-Item 标签体系重构方案.md -ErrorAction SilentlyContinue
Remove-Item 标签系统修复总结.md -ErrorAction SilentlyContinue
Remove-Item 话题标签显示修复报告.md -ErrorAction SilentlyContinue

# 删除本文件（清理完成后）
# Remove-Item CLEANUP_AND_UPLOAD_GUIDE.md -ErrorAction SilentlyContinue
```

### 步骤 5：整理数据库脚本（可选）
```powershell
# 创建数据库脚本目录
New-Item -ItemType Directory -Force -Path database\migration

# 移动重要的迁移脚本
Move-Item database_*.sql database\migration\ -ErrorAction SilentlyContinue
Move-Item init_*.sql database\migration\ -ErrorAction SilentlyContinue
Move-Item tag_system_explanation.sql database\ -ErrorAction SilentlyContinue
```

### 步骤 6：提交变更
```powershell
# 查看所有变更
git status

# 添加所有变更
git add .

# 提交清理
git commit -m "refactor: 清理项目冗余文件，优化项目结构

- 删除构建产物 (dist/, target/)
- 从 Git 中移除 uploads 目录（用户上传的图片）
- 删除临时 SQL 修复脚本
- 删除测试 SQL 脚本
- 删除过程性文档
- 整理数据库迁移脚本到 database/migration/
- 更新 .gitignore 配置
"

# 查看提交历史
git log --oneline -5
```

### 步骤 7：推送到 GitHub
```powershell
# 推送到远程仓库
git push origin master

# 或者如果是 main 分支
# git push origin main
```

---

## 🤖 自动化脚本

如果你想一键执行所有清理操作，运行以下 PowerShell 脚本：

```powershell
# 清理脚本
Write-Host "=== 开始清理项目 ===" -ForegroundColor Green

# 1. 删除构建产物
Write-Host "1. 删除构建产物..." -ForegroundColor Yellow
if (Test-Path "dist") { Remove-Item -Recurse -Force dist }
if (Test-Path "target") { Remove-Item -Recurse -Force target }

# 2. 从 Git 移除 uploads
Write-Host "2. 从 Git 中移除 uploads 目录..." -ForegroundColor Yellow
git rm -r --cached uploads/ 2>$null

# 3. 删除临时 SQL 文件
Write-Host "3. 删除临时 SQL 文件..." -ForegroundColor Yellow
$sqlFiles = @(
    "check_and_fix_tags.sql", "check_and_fix_user_identity.sql", "check_identity_tags_issue.sql",
    "check_tag_query.sql", "check_tag_tables_status.sql", "check_user_identity_status.sql",
    "cleanup_duplicate_tags.sql", "diagnose_tag_system.sql", "fix_admin_password.sql",
    "fix_tag_tables.sql", "fix_tag_tables_structure.sql", "fix_tags_simple.sql",
    "fix_topic_tags.sql", "fix_user_identity_data.sql", "fix_user_identity_for_organization.sql",
    "remove_product_tags_from_publish.sql", "update_product_share_tags.sql",
    "verify_admin_tag_system.sql", "verify_identity_display.sql", "verify_identity_system.sql",
    "verify_topic_data.sql", "test_admin_tag_management.sql", "test_forward_topic_data.sql",
    "test_map_feature.sql", "test_product_forward_feature.sql", "test_product_share_favorite.sql",
    "test_products_simple.sql", "test_user_identity_update.sql"
)
foreach ($file in $sqlFiles) {
    if (Test-Path $file) { Remove-Item $file -Force }
}

# 4. 删除冗余文档
Write-Host "4. 删除冗余文档..." -ForegroundColor Yellow
$docFiles = @(
    "ADMIN_SYSTEM_TEST_README.md", "ADMIN_SYSTEM_TEST_REPORT.md", "ADMIN_TAG_SYSTEM_COMPLETE_REPORT.md",
    "CODE_QUALITY_ANALYSIS_REPORT.md", "FUNCTIONAL_ANALYSIS_AND_PREPARATION.md", "GIT_UPLOAD_SUMMARY.md",
    "IDENTITY_FILES_CHECKLIST.md", "MAP_PREVIEW_TODO.md", "PRODUCT_SHARE_QUICK_VERIFICATION.md",
    "PRODUCT_SHARE_TAG_UPDATE_README.md", "QUICK_CLEANUP_GUIDE.md", "QUICK_VERIFICATION_PRODUCT_TAGS.md",
    "QUICK_VERIFICATION_SCRIPT.md", "REMOVE_PRODUCT_TAGS_SUMMARY.md", "SPECIFICATIONS_FEATURE_README.md",
    "TOPIC_WALL_FIX_REPORT.md", "USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md",
    "标签体系检查报告.md", "标签体系重构方案.md", "标签系统修复总结.md", "话题标签显示修复报告.md"
)
foreach ($file in $docFiles) {
    if (Test-Path $file) { Remove-Item $file -Force }
}

# 5. 整理数据库脚本
Write-Host "5. 整理数据库脚本..." -ForegroundColor Yellow
if (-not (Test-Path "database\migration")) {
    New-Item -ItemType Directory -Force -Path "database\migration" | Out-Null
}
Get-ChildItem -Filter "database_*.sql" | ForEach-Object {
    Move-Item $_.FullName "database\migration\" -Force
}
Get-ChildItem -Filter "init_*.sql" | ForEach-Object {
    Move-Item $_.FullName "database\migration\" -Force
}
if (Test-Path "tag_system_explanation.sql") {
    Move-Item "tag_system_explanation.sql" "database\" -Force
}

# 6. Git 操作
Write-Host "6. 提交变更到 Git..." -ForegroundColor Yellow
git add .
git commit -m "refactor: 清理项目冗余文件，优化项目结构"

Write-Host "=== 清理完成！===" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 检查 git status 确认变更" -ForegroundColor Cyan
Write-Host "2. 运行 git push origin master 推送到 GitHub" -ForegroundColor Cyan
Write-Host "3. 删除本清理指南文件（可选）" -ForegroundColor Cyan
```

---

## ✅ 清理后检查清单

- [ ] 构建产物已删除（dist/, target/）
- [ ] uploads 目录已从 Git 移除
- [ ] 临时 SQL 文件已删除
- [ ] 测试 SQL 文件已删除
- [ ] 冗余文档已删除
- [ ] 数据库脚本已整理到 database/migration/
- [ ] git status 显示正确的变更
- [ ] 已提交到 Git
- [ ] 已推送到 GitHub

---

## 📝 最终保留的核心文件

### 源代码
- ✅ src/main/java/ - 后端源代码
- ✅ src/main/resources/ - 后端资源文件
- ✅ src/test/ - 后端测试
- ✅ src/ - 前端源代码
- ✅ public/ - 前端静态资源

### 配置文件
- ✅ pom.xml
- ✅ package.json
- ✅ vite.config.js
- ✅ jsconfig.json
- ✅ .gitignore

### 核心文档
- ✅ README.md
- ✅ ADMIN_SYSTEM_README.md
- ✅ ADMIN_TAG_MANAGEMENT_README.md
- ✅ ADMIN_TAG_QUICKSTART.md
- ✅ ADVERTISEMENT_FEATURE_README.md
- ✅ ADVERTISEMENT_QUICKSTART.md
- ✅ IDENTITY_QUICKSTART.md
- ✅ IDENTITY_SYSTEM_COMPLETE.md
- ✅ MAP_FEATURE_README.md
- ✅ MAP_QUICKSTART.md
- ✅ USER_LOCATION_MARK_FEATURE_README.md
- ✅ 标签体系快速指南.md
- ✅ 标签体系总结报告.md
- ✅ CLEANUP_GUIDE.md（本清理指南）

### 测试文件
- ✅ tests/*.test.js
- ✅ tests/*.json（Postman 集合）
- ✅ src/test/java/

### 数据库脚本
- ✅ database/migration/*.sql（迁移脚本）
- ✅ database/tag_system_explanation.sql

---

## ⚠️ 注意事项

1. **uploads 目录**：包含用户上传的图片，不应该提交到 Git
2. **dist 和 target**：构建产物，每次构建会重新生成
3. **.idea 和 .vscode**：IDE 配置，个人化配置不应提交
4. **临时 SQL 脚本**：执行过的一次性修复脚本，不需要保留
5. **过程性文档**：开发过程中的临时文档，完成后应删除

---

生成时间：2026-03-26
