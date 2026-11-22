# SQL 最小知识（写给 C++ 选手）

> **目的**：够你设计 OJ 的表，不是 SQL 语法大全。
> 语法忘了一查就有（`docs/git-cheatsheet.md` 同款思路），但**索引为什么这么建**，查不到，只能想明白。
> 标 ★ 的两节是核心，其余扫一眼。

---

## 目录

- [★ 1. 表和索引：用 C++ 的话说](#★-1-表和索引用-c-的话说)
- [★ 2. 复合索引与最左前缀](#★-2-复合索引与最左前缀)
- [3. 主键 / 外键 / UNIQUE](#3-主键--外键--unique)
- [4. 关系怎么落到表上](#4-关系怎么落到表上)
- [5. 数据类型速查](#5-数据类型速查)
- [6. 你现在只需要会跑的 6 条命令](#6-你现在只需要会跑的-6-条命令)
- [7. 沙箱实验：亲手摸一次索引](#7-沙箱实验亲手摸一次索引)

---

## ★ 1. 表和索引：用 C++ 的话说

| SQL | C++ 里最接近的 | 差别 |
|---|---|---|
| 表 (table) | `struct` 的数组 | 在磁盘上，能装几亿条 |
| 行 (row) | 数组里的一个实例 | 一条记录 |
| 列 (column) | `struct` 的成员 | 有类型约束 |
| 主键 (PRIMARY KEY) | 唯一且自动递增的下标 | 不能空、不能重复 |
| **索引 (INDEX)** | **排序数组 + 二分** | **本质相同** |
| 外键 (FOREIGN KEY) | 指向另一张表的引用 | 数据库帮你校验它有效 |
| UNIQUE | `set` | 同列不能重复 |

**为什么需要索引**

100 万条提交记录，查「张三的所有提交」：

```
无索引：全表扫描，比较 100 万次          O(n)
有索引：预排序结构，二分定位             O(log n)
```

MySQL 的索引底层是 **B+ 树**——一个多叉的有序结构。你不用懂它长什么样，只用记住：**它替你维护了一种"总是有序"的排列。**

**索引不是免费的**

每个索引都是一棵额外的树。每次 `INSERT` / `UPDATE` / `DELETE` 都要同步更新它，还占磁盘。

> **建索引的标准：能写出它加速的那条 SQL。写不出来 = 别建。**

---

## ★ 2. 复合索引与最左前缀

这是唯一会让你真正卡住的地方，也是面试高频。

**建索引 `(problem_id, status)`，等价于：把所有行先按 `problem_id` 排序，`problem_id` 相同的再按 `status` 排序。**

```
(problem_id=1, status=AC)     ← 最前
(problem_id=1, status=WA)
(problem_id=2, status=AC)
(problem_id=2, status=TLE)
(problem_id=3, status=AC)
```

因为有序，才能二分。所以：

| 查询 | 能用这个索引吗 | 原因 |
|---|---|---|
| `WHERE problem_id=1` | ✅ | 按 problem_id 有序，直接二分 |
| `WHERE problem_id=1 AND status=AC` | ✅ | 先定位 problem_id=1，区间内再二分 status |
| `WHERE status=AC` | ❌ | AC 散落在各处，**不连续**，无法二分 |
| `ORDER BY problem_id, status` | ✅ | 索引本身就是这个顺序，不用额外排序 |

**最左前缀原则**：复合索引 `(A, B)` 只能加速 **以 A 开头** 的查询。

推论（重要）：

- `(problem_id, status)` 和 `(status, problem_id)` 是**两个不同的索引**，能加速的查询不一样
- 已经有了 `(A, B)`，再单独建 `(A)` 是浪费（`(A,B)` 已覆盖它的前缀）
- 但已经有 `(A)`，再建 `(A, B)` 是需要的（覆盖不了 B）

### 例外：Skip Scan（MySQL 8.0.13+）

**前导列的基数很低时，最左前缀可以被绕过。**

实验（`a` 只有 1000 个不同值，`b` 只有 7 个）：

```sql
CREATE INDEX idx_ab ON t(a, b);

EXPLAIN SELECT * FROM t WHERE b = 3;
-- type=range, key=idx_ab, Extra: Using index for skip scan
```

**为什么能用了？** MySQL 不会一次二分定位 `b=3`，而是**把 `a` 的 1000 个不同值挨个跳过去**，在每段里找 `b=3`。1000 次小查找 < 10 万行全表扫，所以划算。

**什么时候不管用？** 前导列基数高时。`problem_id` 有上万个不同值，跳扫要分上万组，代价爆炸，优化器直接放弃，回到 `type=ALL`。

> **结论：最左前缀仍然是设计复合索引的第一原则。Skip Scan 是优化器的补救机制，不能作为设计依据。**

想亲眼验证：

```sql
SET SESSION optimizer_switch = 'skip_scan=off';
EXPLAIN SELECT * FROM t WHERE b = 3;
-- type=index（或 ALL）, possible_keys 空, rows≈全表
SET SESSION optimizer_switch = 'skip_scan=on';
```

> **注意 `type=index` 不是"走了索引"。** 它是「全索引扫描」——把整棵索引树扫一遍。
> 因为覆盖索引比表窄，优化器选了它，但 `rows` 依然是全表行数，**没有一丁点定位能力**。
>
> **判断索引有没有用，看 `rows`，不看 `key`。**

### 附：`Using index` = 覆盖索引

如果 `Extra` 里出现 `Using index`，说明**查询要的列全在索引里，不用回表**。

InnoDB 的二级索引会自动带上主键，所以 `idx_ab` 实际存的是 `(a, b, id)`。`SELECT *` 在只有这三列的表上就完全被索引覆盖了。

真实业务里 `SELECT *` 通常要回表，所以跳扫未必划算——**这也是为什么测试表要留意列数。**

---

**怎么判断一条 SQL 用没用到索引**——`EXPLAIN`：

```sql
EXPLAIN SELECT * FROM submission WHERE problem_id = 1 AND status = 2;
```

看 `type` 这一列：

| type | 含义 |
|---|---|
| `ALL` | 全表扫描（**没走索引**） |
| `ref` / `range` / `const` | 走了索引 |
| `index` | 扫了整个索引树（比 ALL 好，但通常不理想） |

---

## 3. 主键 / 外键 / UNIQUE

**主键**

```sql
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
PRIMARY KEY (id)
```

- `AUTO_INCREMENT`：插入时不用给，数据库自动 +1
- 一张表只能有一个主键，不能为空

> **为什么用自增整数做主键，不用用户名？**
> ① 用户名会改，主键不能改；② 整数比变长字符串小得多，索引也更快。

**外键**

```sql
CONSTRAINT fk_testcase_problem
  FOREIGN KEY (problem_id) REFERENCES problem (id) ON DELETE CASCADE
```

两件事：
1. **校验**：`testcase.problem_id` 必须真的存在于 `problem.id`
2. **级联**：`ON DELETE CASCADE` = 删掉 `problem` 里的一行，引用它的 `testcase` 全部自动删除

`ON DELETE` 的几种选择：

| 写法 | 删主表时，从表怎么办 |
|---|---|
| `CASCADE` | 跟着删 |
| `RESTRICT`（默认） | 拒绝删除，报错 |
| `SET NULL` | 把外键置为 NULL |

**没有外键会怎样？** 不校验，可能插入一个指向不存在的题目的测试点。但外键也有代价：插入时要多查一次、大量写入时是瓶颈。**大厂经常故意不用外键**——这个权衡面试会问。

**UNIQUE**

```sql
UNIQUE KEY uk_username (username)
```

同一列（或多列组合）不能重复。

`UNIQUE (contest_id, user_id)` = **这一对组合**唯一 = 一个人不能报名同一场比赛两次。

> 和主键的区别：主键只能有一个且不能为空；`UNIQUE` 可以有多个、可以允许 `NULL`。

---

## 4. 关系怎么落到表上

| 关系 | 例子 | 怎么建 |
|---|---|---|
| **1:1** | 用户 ↔ 用户详情 | 少见，直接合成一张表 |
| **1:N** | 一道题 → 多个测试点 | 外键放在**多**的那一边（`testcase` 里放 `problem_id`） |
| **M:N** | 比赛 ↔ 题目 | **必须建中间表** `contest_problem` |

**M:N 为什么必须建中间表？** 因为一行只能放一个 `problem_id`，而一场比赛有很多题；反过来 `problem` 里也只能放一个 `contest_id`。**没有地方能存"这一对"**，只能单独开一张表，每行存一对。

---

## 5. 数据类型速查

| 类型 | 范围 / 大小 | 什么时候用 |
|---|---|---|
| `BIGINT UNSIGNED` | 0 ~ 1.8×10¹⁹ | 所有 `id` |
| `INT` | -21 亿 ~ 21 亿 | 毫秒、内存 MB、计数 |
| `TINYINT` | -128~127（`UNSIGNED` 则 0~255） | 状态码、难度 1~5、role |
| `VARCHAR(n)` | 变长，n 是**字符数**上限 | 用户名、邮箱、代码 |
| `CHAR(n)` | 定长，n 个字符 | 固定长度（如 `alias CHAR(2)`） |
| `TEXT` | 64 KB | 输入输出说明 |
| `MEDIUMTEXT` | 16 MB | 题面正文、提交的代码 |
| `JSON` | 结构化数据 | 样例 `[{"input":"","output":""}]` |
| `DATETIME` | 日期 + 时间 | `create_time` |

**几个常见判断**：

- **BCrypt 哈希固定 60 字符** → `VARCHAR(100)` 是留余量
- **代码可能很长** → `MEDIUMTEXT`，`VARCHAR` 有长度上限
- **状态用 `TINYINT` 不用字符串** → 省空间，但**必须在代码里用枚举把数字翻译成可读名字**，否则没人看得懂 `status=3` 是什么

---

## 6. 你现在只需要会跑的 6 条命令

> 只需要**读**，今天不写 `CREATE TABLE`

```sql
-- 看有哪些库
SHOW DATABASES;

-- 切到 oj 库
USE oj;

-- 看这个库有哪些表
SHOW TABLES;

-- 看某张表的字段、类型、主键、索引
DESC `user`;

-- 看前 5 行数据
SELECT * FROM `user` LIMIT 5;

-- 看一条查询走没走索引
EXPLAIN SELECT * FROM `user` WHERE username = 'alice';
```

> 表名 `user` 是 MySQL 的关键字，**必须加反引号** `` `user` ``。这是新手第一个坑。

---

## 7. 沙箱实验：亲手摸一次索引

**别跳过。** 20 分钟，把上面两节变成肌肉记忆。

在 Navicat 里新建一个 `sandbox` 库（和 `oj` 完全隔离，随便折腾）：

```sql
CREATE DATABASE sandbox;
USE sandbox;

CREATE TABLE t (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  a INT NOT NULL,
  b INT NOT NULL
);

-- 插 10 万行
INSERT INTO t (a, b)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 100000
)
SELECT n % 1000, n % 7 FROM seq;
```

**实验一：有索引 vs 没索引**

```sql
-- ① 没有索引
EXPLAIN SELECT * FROM t WHERE a = 500;      -- type = ALL（全表扫）

-- ② 建索引
CREATE INDEX idx_a ON t(a);

-- ③ 再看
EXPLAIN SELECT * FROM t WHERE a = 500;      -- type = ref（走索引）
```

**实验二：最左前缀**

```sql
CREATE INDEX idx_ab ON t(a, b);

EXPLAIN SELECT * FROM t WHERE a = 500 AND b = 3;   -- type = ref，走了 idx_ab
EXPLAIN SELECT * FROM t WHERE b = 3;               -- type = ALL，没走
EXPLAIN SELECT * FROM t WHERE a = 500;             -- type = ref，走了（前缀）
```

**做完你应该能回答**：

- 为什么 `WHERE b = 3` 用不上 `(a, b)` 索引？
- 既然有了 `idx_a`，`idx_ab` 还需要吗？反过来呢？
- 如果这张表每秒要插入 1 万行，你会建几个索引？

---

## 一句话总纲

> **表 = struct 数组，索引 = 预排序 + 二分，主键 = 唯一下标。**
> **建索引的唯一理由：能写出它加速的那条 SQL。**
