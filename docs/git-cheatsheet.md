# Git 常用命令手册

> 格式约定：`<必填>` ／ `[可选]` ／ `...` 表示可以跟多个

---

## 目录

- [一、核心循环：这 6 条占你 95% 的使用](#一核心循环这-6-条占你-95-的使用)
- [二、看：搞清楚现在是什么情况](#二看搞清楚现在是什么情况)
- [三、反悔：出错了怎么办](#三反悔出错了怎么办)
- [四、分支：现在不需要，但得知道有这回事](#四分支现在不需要但得知道有这回事)
- [五、远程：和 GitHub 打交道](#五远程和-github-打交道)
- [六、一次性配置](#六一次性配置)
- [七、一页速查表](#七一页速查表)
- [八、记住三句话](#八记住三句话)

---

## 一、核心循环：这 6 条占你 95% 的使用

| 命令 | 含义 | 什么时候用 |
|---|---|---|
| `git status` | 看当前状态：哪些改了、哪些已暂存 | **每次操作前先跑** |
| `git add <路径>` | 把改动放进暂存区 | 准备提交前 |
| `git commit -m "<消息>"` | 把暂存区存成一个提交 | 一个逻辑单元完成时 |
| `git push` | 推送到 GitHub | 一天结束时 / 想备份时 |
| `git pull` | 把 GitHub 上的新提交拉下来 | 多设备编辑过、或 push 被拒时 |
| `git log --oneline` | 一行一条地看提交历史 | 想确认"我提交了什么" |

### `git add` 的三种写法

```bash
git add docs/env-principles.md   # 单个文件，最精确
git add docs/                    # 整个目录
git add .                        # 当前目录下所有改动（含子目录）
```

`.` 就是"当前目录"。**用 `.` 前先 `git status` 看一眼**，防止把不该提交的东西送上去。

### `git commit` 的常用变体

```bash
git commit -m "feat: 新增 hello 接口"       # 标准写法
git commit -m "标题" -m "正文多行说明"       # 第二段 -m 是正文
```

不加 `-m` 会打开编辑器让你写长消息——**新手容易卡在 vim 里出不来**（按 `Esc` 再输 `:wq` 回车）。**永远带 `-m`。**

---

## 二、看：搞清楚现在是什么情况

| 命令 | 输出什么 |
|---|---|
| `git status` | 完整状态（三块：已暂存 / 未暂存 / 未跟踪） |
| `git status -s` | 精简版，两列字母，如 `M  file` / `?? new.txt` |
| `git diff` | **工作区 vs 暂存区** —— 我改了什么但还没 add |
| `git diff --staged` | **暂存区 vs 上次提交** —— 我这次要提交什么 |
| `git diff HEAD` | 工作区 vs 上次提交（两者合起来看） |
| `git log --oneline` | 提交历史，一行一条 |
| `git log --oneline --graph --all` | 带分支图的历史 |
| `git show <hash>` | 看某次提交具体改了什么 |
| `git remote -v` | 我的远程仓库地址是什么 |

**`git diff` 和 `git diff --staged` 的区别是新手最容易搞混的**：

```
git diff            → 你改了但还没 add 的部分
git diff --staged   → 你已经 add 的部分（即将提交的）
```

`git status -s` 的两列字母：

```
 M file.txt     ← 右边 M：工作区改了，未暂存
M  file.txt     ← 左边 M：已暂存，等待提交
MM file.txt     ← 两边都 M：暂存了一部分，之后又改了
?? new.txt      ← 未跟踪（全新文件）
A  file.txt     ← 新增（已暂存的新文件）
```

---

## 三、反悔：出错了怎么办

按"错误发生在哪一步"选命令：

| 情况 | 命令 | 后果 |
|---|---|---|
| 改了文件，想全部丢弃 | `git restore <file>` | **改动消失，不可恢复** |
| add 错了，想撤出暂存区 | `git restore --staged <file>` | 文件改动**还在**，只是不在暂存区了 |
| 刚 commit，发现漏了文件 | `git add 漏的文件` + `git commit --amend` | 把改动合并进上一次提交 |
| 刚 commit，发现消息写错了 | `git commit --amend -m "新消息"` | 改消息 |
| 想撤销最后一次 commit，但保留改动 | `git reset --soft HEAD~1` | 提交没了，改动回到暂存区 |
| 想撤销最后一次 commit，连改动一起丢 | `git reset --hard HEAD~1` | **全没了，危险** |
| 已经 push 了想撤销 | `git revert <hash>` | 生成一个"反向提交"，安全 |

### `HEAD` 是什么

`HEAD` 是"你现在所在位置"的指针，指向你最后一次提交。

```
HEAD      → 最新一次提交
HEAD~1    → 上一次
HEAD~2    → 上两次
```

### 两条安全线

> 🚫 **已经 push 过的提交，永远不要用 `reset` 或 `amend` 去改。**
> 用 `revert` 生成一个新提交来抵消它。改写已推送的历史需要 `push --force`，而强推可能覆盖别人的东西——个人项目也别养成这个习惯。

> 🚫 **`git reset --hard` 会永久删除未提交的改动。** 敲之前先 `git status` 确认你要丢的到底是什么。

---

## 四、分支：现在不需要，但得知道有这回事

| 命令 | 含义 |
|---|---|
| `git branch` | 列出本地分支，`*` 标的是当前分支 |
| `git branch <名字>` | 新建分支 |
| `git switch <名字>` | 切换分支（Git 2.23+ 新命令） |
| `git switch -c <名字>` | 新建并切换（`-c` = create） |
| `git merge <名字>` | 把某分支合并到当前分支 |
| `git branch -d <名字>` | 删除已合并的分支 |

教程里常见 `git checkout <名字>`——`checkout` 是**老命令**，同时管"切分支"和"恢复文件"两件事，容易搞混。Git 后来拆成了 `switch`（切分支）和 `restore`（恢复文件）。**新代码用新的。**

**这个项目 25 天里不需要开分支。** 一个人开发、没有代码评审，全在 `main` 上提交最省事。

---

## 五、远程：和 GitHub 打交道

| 命令 | 含义 |
|---|---|
| `git clone <url>` | 把远程仓库完整复制到本地（**新项目第一步**） |
| `git remote -v` | 看当前关联了哪些远程仓库 |
| `git remote add origin <url>` | 关联一个远程仓库，起名叫 `origin` |
| `git fetch` | **只下载**远程的新提交，不改你的工作区 |
| `git pull` | `fetch` + **自动合并**到当前分支 |
| `git push` | 把本地提交上传到远程 |
| `git push -u origin main` | 首次推送，`-u` 记住默认，以后直接 `git push` |

### `origin` 和 `main` 是什么

```
git push origin main
         ↑      ↑
         |      └── 分支名（你的主线）
         └───────── 远程仓库的别名（默认就叫 origin，意思是"来源"）
```

`origin` 只是个**名字**，可以改，但 99% 的项目都叫 origin。

### `fetch` vs `pull`

```
git fetch    → 只把远程的新提交下载到本地，你的代码一个字都不变
git pull     → fetch + merge，直接把你本地代码更新到最新
```

日常用 `pull` 就够了。`fetch` 的价值是"先看远程改了什么再决定"，一个人开发用不上。

---

## 六、一次性配置

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"

git config --global --list     # 查看所有配置
```

`--global` 表示对这台电脑上所有仓库生效。

---

## 七、一页速查表

```bash
# ── 每天用 ──────────────────────────────
git status                     # 我现在什么状态
git add <file>                 # 暂存
git commit -m "说明"            # 提交
git push                       # 推到 GitHub
git log --oneline              # 看历史

# ── 出问题 ──────────────────────────────
git diff                       # 我改了啥（未 add）
git diff --staged              # 我要提交啥（已 add）
git restore <file>             # 丢弃改动 ⚠️
git restore --staged <file>    # 撤出暂存区
git commit --amend -m "新消息"  # 修补上次提交（未 push 才行）

# ── 远程 ────────────────────────────────
git pull                       # 拉最新
git remote -v                  # 看远程地址

# ── 红线 ⛔ ─────────────────────────────
git reset --hard               # 永久丢改动，别碰
git push --force               # 覆盖远程历史，别碰
```

---

## 八、记住三句话

1. **`git status` 是最重要的命令** —— 不确定时先跑它，它会告诉你下一步该干什么
2. **commit 是离线的，push 才联网** —— "我提交了怎么网上没有" 一定是忘了 push
3. **已推送的历史不要改写** —— 出错就往前加一个新提交，历史是记录不是画布
