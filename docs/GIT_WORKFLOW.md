# Git 工作流规范

## 分支管理策略

### 主要分支

- **main**: 生产环境分支，保持稳定可用状态
- **develop**: 开发主分支，集成所有已完成的功能

### 辅助分支

- **feature/***: 功能开发分支，命名规范为 `feature/功能名称`
- **hotfix/***: 紧急修复分支，命名规范为 `hotfix/问题描述`
- **release/***: 发布准备分支，命名规范为 `release/版本号`

## 工作流程

### 1. 功能开发流程

```bash
# 从 develop 分支创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-login

# 开发过程中定期提交
git add .
git commit -m "feat(user): 实现用户登录功能"

# 功能完成后推送到远程
git push origin feature/user-login

# 创建 Pull Request 进行代码审查
# 审查通过后合并到 develop 分支
```

### 2. 发布流程

```bash
# 从 develop 创建发布分支
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 进行发布前测试和修复
git add .
git commit -m "chore(release): 准备 v1.0.0 版本发布"

# 合并到 main 和 develop
git checkout main
git merge release/v1.0.0
git push origin main

git checkout develop
git merge release/v1.0.0
git push origin develop

# 删除发布分支
git branch -d release/v1.0.0
```

### 3. 热修复流程

```bash
# 从 main 创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug

# 修复问题并测试
git add .
git commit -m "fix(critical): 修复严重bug"

# 合并到 main 和 develop
git checkout main
git merge hotfix/critical-bug
git push origin main

git checkout develop
git merge hotfix/critical-bug
git push origin develop

# 删除热修复分支
git branch -d hotfix/critical-bug
```

## 提交信息规范

使用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### 提交类型

- **feat**: 新功能
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码格式调整（不影响代码运行）
- **refactor**: 代码重构
- **perf**: 性能优化
- **test**: 测试相关
- **build**: 构建系统或外部依赖变更
- **ci**: CI配置文件和脚本
- **chore**: 构建过程或辅助工具变动
- **revert**: 回滚提交

### 示例

```bash
# 新功能
git commit -m "feat(auth): 添加用户注册功能"

# Bug修复
git commit -m "fix(api): 修复API响应超时问题"

# 文档更新
git commit -m "docs(readme): 更新安装说明"

# 代码重构
git commit -m "refactor(components): 重构用户组件结构"

# 测试添加
git commit -m "test(login): 添加登录组件单元测试"
```

## 代码审查清单

Pull Request 合并前需要满足：

- [ ] 代码符合编码规范
- [ ] 所有测试通过
- [ ] 至少一人代码审查通过
- [ ] 提交信息格式正确
- [ ] 功能测试完成
- [ ] 文档已更新（如需要）

## 常用Git命令

```bash
# 查看分支状态
git branch -a

# 查看提交历史
git log --oneline --graph

# 查看文件修改差异
git diff

# 撤销工作区修改
git checkout -- filename

# 撤销暂存区修改
git reset HEAD filename

# 修改最后一次提交
git commit --amend

# 合并分支时使用 no-ff 模式
git merge --no-ff branch-name
```

## 最佳实践

1. **频繁提交**: 小步快跑，及时提交代码变更
2. **有意义的提交信息**: 清晰描述每次提交的内容
3. **及时同步**: 定期从远程仓库拉取最新代码
4. **分支清理**: 及时删除已合并的分支
5. **代码审查**: 所有代码变更都需要经过审查
6. **测试先行**: 功能开发前先编写测试用例