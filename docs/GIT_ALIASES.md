# Git 别名配置

为了提高Git操作效率，建议配置以下别名：

## 配置方法

将以下内容添加到你的全局Git配置文件中：

```bash
# 编辑全局配置文件
git config --global --edit
```

或者直接运行以下命令：

```bash
# 常用别名配置
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.df diff
git config --global alias.dc "diff --cached"
git config --global alias.lg "log --oneline --graph --all"
git config --global alias.last "log -1 HEAD"
git config --global alias.unstage "reset HEAD --"
git config --global alias.undo "checkout --"
git config --global alias.publish "!git push -u origin $(git rev-parse --abbrev-ref HEAD)"
```

## 常用别名说明

| 别名 | 原始命令 | 说明 |
|------|----------|------|
| st | status | 查看状态 |
| co | checkout | 切换分支 |
| br | branch | 查看分支 |
| ci | commit | 提交更改 |
| df | diff | 查看差异 |
| dc | diff --cached | 查看暂存区差异 |
| lg | log --oneline --graph --all | 图形化查看提交历史 |
| last | log -1 HEAD | 查看最后一次提交 |
| unstage | reset HEAD -- | 取消暂存 |
| undo | checkout -- | 撤销工作区修改 |
| publish | !git push -u origin $(git rev-parse --abbrev-ref HEAD) | 推送并设置上游分支 |

## 使用示例

```bash
# 查看状态
git st

# 切换到develop分支
git co develop

# 查看所有分支
git br

# 提交更改
git ci -m "feat: 添加新功能"

# 查看提交历史
git lg

# 推送当前分支并设置上游
git publish
```

## 自定义别名

你也可以根据个人习惯创建自定义别名：

```bash
# 查看最近5次提交
git config --global alias.recent "log --oneline -n 5"

# 查看贡献统计
git config --global alias.stats "shortlog -sn"

# 强制推送（谨慎使用）
git config --global alias.force-push "push --force-with-lease"
```