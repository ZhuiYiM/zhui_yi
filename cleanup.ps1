# 项目清理脚本 - 自动删除冗余文件
# 使用前请确保已备份重要文件

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  项目清理脚本 - 准备上传 GitHub" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 确认提示
Write-Host "⚠️  警告：此操作将删除临时文件和冗余文档！" -ForegroundColor Yellow
Write-Host "请确保：" -ForegroundColor Yellow
Write-Host "  1. 所有代码已提交到 Git" -ForegroundColor Yellow
Write-Host "  2. 已备份重要文件" -ForegroundColor Yellow
Write-Host "  3. uploads 目录的图片不需要提交" -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "是否继续？(y/n)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "❌ 已取消清理操作" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "=== 开始清理 ===" -ForegroundColor Green

# 1. 删除构建产物
Write-Host "1. 删除构建产物..." -ForegroundColor Yellow
if (Test-Path "dist") { 
    Remove-Item -Recurse -Force dist 
    Write-Host "   ✅ 删除 dist/" -ForegroundColor Green
}
if (Test-Path "target") { 
    Remove-Item -Recurse -Force target 
    Write-Host "   ✅ 删除 target/" -ForegroundColor Green
}

# 2. 从 Git 移除 uploads
Write-Host "2. 从 Git 中移除 uploads 目录..." -ForegroundColor Yellow
git rm -r --cached uploads/ 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ 从 Git 移除 uploads/" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  uploads/ 可能不在 Git 中" -ForegroundColor Cyan
}

# 3. 删除临时 SQL 文件
Write-Host "3. 删除临时 SQL 文件..." -ForegroundColor Yellow
$sqlFiles = @(
    "check_and_fix_tags.sql", 
    "check_and_fix_user_identity.sql", 
    "check_identity_tags_issue.sql",
    "check_tag_query.sql", 
    "check_tag_tables_status.sql", 
    "check_user_identity_status.sql",
    "cleanup_duplicate_tags.sql", 
    "diagnose_tag_system.sql", 
    "fix_admin_password.sql",
    "fix_tag_tables.sql", 
    "fix_tag_tables_structure.sql", 
    "fix_tags_simple.sql",
    "fix_topic_tags.sql", 
    "fix_user_identity_data.sql", 
    "fix_user_identity_for_organization.sql",
    "remove_product_tags_from_publish.sql", 
    "update_product_share_tags.sql",
    "verify_admin_tag_system.sql", 
    "verify_identity_display.sql", 
    "verify_identity_system.sql",
    "verify_topic_data.sql", 
    "test_admin_tag_management.sql", 
    "test_forward_topic_data.sql",
    "test_map_feature.sql", 
    "test_product_forward_feature.sql", 
    "test_product_share_favorite.sql",
    "test_products_simple.sql", 
    "test_user_identity_update.sql"
)
$deletedCount = 0
foreach ($file in $sqlFiles) {
    if (Test-Path $file) { 
        Remove-Item $file -Force
        $deletedCount++
    }
}
Write-Host "   ✅ 删除 $deletedCount 个 SQL 文件" -ForegroundColor Green

# 4. 删除冗余文档
Write-Host "4. 删除冗余文档..." -ForegroundColor Yellow
$docFiles = @(
    "ADMIN_SYSTEM_TEST_README.md", 
    "ADMIN_SYSTEM_TEST_REPORT.md", 
    "ADMIN_TAG_SYSTEM_COMPLETE_REPORT.md",
    "CODE_QUALITY_ANALYSIS_REPORT.md", 
    "FUNCTIONAL_ANALYSIS_AND_PREPARATION.md", 
    "GIT_UPLOAD_SUMMARY.md",
    "IDENTITY_FILES_CHECKLIST.md", 
    "MAP_PREVIEW_TODO.md", 
    "PRODUCT_SHARE_QUICK_VERIFICATION.md",
    "PRODUCT_SHARE_TAG_UPDATE_README.md", 
    "QUICK_CLEANUP_GUIDE.md", 
    "QUICK_VERIFICATION_PRODUCT_TAGS.md",
    "QUICK_VERIFICATION_SCRIPT.md", 
    "REMOVE_PRODUCT_TAGS_SUMMARY.md", 
    "SPECIFICATIONS_FEATURE_README.md",
    "TOPIC_WALL_FIX_REPORT.md", 
    "USER_LOCATION_MARK_IMPLEMENTATION_SUMMARY.md",
    "标签体系检查报告.md", 
    "标签体系重构方案.md", 
    "标签系统修复总结.md", 
    "话题标签显示修复报告.md"
)
$deletedDocCount = 0
foreach ($file in $docFiles) {
    if (Test-Path $file) { 
        Remove-Item $file -Force
        $deletedDocCount++
    }
}
Write-Host "   ✅ 删除 $deletedDocCount 个文档文件" -ForegroundColor Green

# 5. 整理数据库脚本
Write-Host "5. 整理数据库脚本..." -ForegroundColor Yellow
if (-not (Test-Path "database\migration")) {
    New-Item -ItemType Directory -Force -Path "database\migration" | Out-Null
    Write-Host "   ✅ 创建 database/migration/ 目录" -ForegroundColor Green
}

$dbMoved = 0
Get-ChildItem -Filter "database_*.sql" -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item $_.FullName "database\migration\" -Force
    $dbMoved++
}

$initMoved = 0
Get-ChildItem -Filter "init_*.sql" -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item $_.FullName "database\migration\" -Force
    $initMoved++
}

if (Test-Path "tag_system_explanation.sql") {
    Move-Item "tag_system_explanation.sql" "database\" -Force
    Write-Host "   ✅ 移动 tag_system_explanation.sql" -ForegroundColor Green
}

Write-Host "   ✅ 移动 $dbMoved 个 database_*.sql 和 $initMoved 个 init_*.sql" -ForegroundColor Green

# 6. Git 状态检查
Write-Host ""
Write-Host "6. 检查 Git 状态..." -ForegroundColor Yellow
git status --short

Write-Host ""
Write-Host "=== 清理完成！===" -ForegroundColor Green
Write-Host ""
Write-Host "删除统计：" -ForegroundColor Cyan
Write-Host "  - 构建产物：2 个目录 (dist/, target/)" -ForegroundColor Cyan
Write-Host "  - SQL 文件：$deletedCount 个" -ForegroundColor Cyan
Write-Host "  - 文档文件：$deletedDocCount 个" -ForegroundColor Cyan
Write-Host "  - 数据库脚本：已整理到 database/migration/" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "  1. 查看变更：git status" -ForegroundColor Cyan
Write-Host "  2. 添加变更：git add ." -ForegroundColor Cyan
Write-Host "  3. 提交变更：git commit -m 'refactor: 清理项目冗余文件'" -ForegroundColor Cyan
Write-Host "  4. 推送到 GitHub：git push origin master" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示：清理指南文件 (CLEANUP_*.md) 可以选择删除或保留" -ForegroundColor Cyan
