# Online Judge · 25 天后端实习项目冲刺路线图

> 起点：2026-11-21（周六）｜终点：2026-12-15（周二）
> 前提：Java 零基础、C++ 竞赛背景、可投入 8h+/天、前端自己写

---

## 一、先明确三条战略

**1. 你的优势不在"写出判题逻辑"，而在"把判题做成一个工程系统"。**
算法和判题规则对你不构成难点。真正值钱、也真正消耗时间的是：环境、分层、异常处理、消息可靠性、缓存一致性、部署上线。排期把 60% 时间压在这些"工程肌肉"上。

**2. 目标不是"做一个 Codeforces"，而是"做一个能被面试官深挖 40 分钟的项目"。**
深度 > 广度。四个必须能讲透到实现细节的点：

- Docker 判题沙箱（资源隔离、超时、容器回收、性能）
- RabbitMQ 解耦与可靠性（幂等、ACK、死信、积压）
- Redis（缓存三问题、ZSet 排行榜、Lua 限流）
- 部署与压测数据（真实 QPS / P99 / 优化前后对比）

**3. OJ 是很好的选题，而且和你的背景形成叙事闭环。**
面试官面腻了外卖和商城。OJ 的技术纵深天然存在，而你能说："我打了几年竞赛，所以我从使用者的角度出发做了一套判题系统，并且是用工程的方式实现的。" 这句话本身就值分。

---

## 二、技术选型（照抄即可，不要换）

| 层 | 选型 | 版本 | 备注 |
|---|---|---|---|
| 语言/运行时 | **JDK 17** | Temurin 17 | Spring Boot 3 最低要求 17 |
| 框架 | **Spring Boot** | 3.2.x | 不要用 2.x，现在面试都问 3 |
| 构建 | **Maven** | 3.9 | 配阿里云镜像 |
| ORM | **MyBatis-Plus** | 3.5.7 | 必须用 `mybatis-plus-spring-boot3-starter`，用错 starter 是经典坑 |
| 数据库 | **MySQL** | 8.0 | Docker 起 |
| 缓存 | **Redis** | 7.x | Docker 起 |
| 消息队列 | **RabbitMQ** | 3.13 management | Docker 起，带 Web 控制台方便调试 |
| 沙箱 | **Docker + docker-java** | 3.4.x | 核心亮点 |
| 鉴权 | **JWT + Spring Security 6** | — | 卡住超过 4h 就降级成自定义拦截器，别死磕 |
| 接口文档 | **Knife4j**（springdoc） | 4.x | 调试和演示都靠它 |
| 工具类 | **Hutool** | 5.8 | 省时间，别手写 |
| 前端 | **Vue 3 + Vite + Element Plus + Pinia + Vue Router + Monaco Editor** | — | Monaco 做代码编辑器，C++ 高亮直接有 |
| 部署 | **Docker Compose + Nginx** | — | 一键起全套 |

**明确不做的事**：不做微服务、不做 K8s、不用若依脚手架、不抄黑马/苍穹外卖的代码结构。理由见 day 25 的面试题「为什么不做微服务」。

---

## 三、目标架构

```
                    ┌──────────────┐
   Browser ────────▶│    Nginx     │  静态资源 + /api 反向代理
                    └──────┬───────┘
                           ▼
                    ┌──────────────┐        ┌────────────┐
                    │ Spring Boot  │◀──────▶│   MySQL    │
                    │  (Web/API)   │        └────────────┘
                    └──┬────┬───┬──┘        ┌────────────┐
                       │    │   └──────────▶│   Redis    │
              WebSocket│    │               └────────────┘
                       ▼    │ publish       ┌────────────┐
                    ┌──────┴───────────┐───▶│ RabbitMQ   │
                    │  Judge Worker    │    └────────────┘
                    │ (独立进程/容器)   │◀──────────┐
                    └────────┬─────────┘           │
                             │ docker-java 调用     │
                             ▼                     │
                    ┌──────────────────┐           │
                    │ 判题容器 (gcc/jdk)│  ← 同一份队列可起 N 个 Worker
                    └──────────────────┘
```

**拆不拆微服务？** 不拆。但把 **判题 Worker 做成独立可水平扩展的进程**（同一队列多个消费者）。这样你既拿到了"分布式"的谈资，又不用付微服务的运维代价——而且这个拆分是**业务天然要求的**，讲出来非常有说服力。

---

## 四、七个里程碑（延期就砍功能，不要砍日期）

| 里程碑 | 日期 | 判定标准 |
|---|---|---|
| M0 环境就绪 | D1 结束 | Spring Boot 能启动，MySQL/Redis/RabbitMQ 三个容器都通 |
| M1 用户+题库 | D5 结束 | 注册登录 + 题目增删改查能在 Knife4j 里跑通 |
| **M2 判题闭环** | **D10 结束** | **提交代码 → 返回 AC/WA/TLE/CE，这是项目的生死线** |
| M3 中间件全接入 | D14 结束 | 缓存、限流、排行榜、实时推送全部生效 |
| M4 比赛模块 | D16 结束 | 能看到 ICPC 赛制的实时排名 |
| M5 前端完成 | D20 结束 | 浏览器里能完整走一遍主流程 |
| M6 上线 | D21 结束 | 公网 IP 可访问 |
| M7 简历级交付 | D25 结束 | 演示视频 + README + 简历条目 + 面试题自测 |

---

## 五、Day 0：今天（11/20）要做的 5 件事

1. **买服务器**：腾讯云/阿里云轻量应用服务器，**2C4G 起步**（2C2G 跑不动 MySQL+Redis+MQ+判题容器，必 OOM）。选**香港/新加坡节点免备案**，直接 IP+HTTP 演示。国内节点绑域名要走备案，2-3 周，来不及。
2. **确认虚拟化已开启**：任务管理器 → 性能 → CPU → "虚拟化：已启用"。没开的话进 BIOS 开，否则 Docker Desktop 装不上。
3. **目录迁一下**：把代码放到 `D:\code\online-judge`。`D:\Online Judge` 带空格，Docker 卷挂载和部分脚本会踩坑。
4. **注册**：GitHub（命名用真名拼音，简历上要写）、LeetCode 已有了、阿里云镜像账号。
5. **装工具**：IDEA（学生认证可白嫖 Ultimate）、DBeaver/Navicat、Postman、Typora/VSCode。

---

## 六、每日安排

> 每天节奏固定：**2h 学 → 5h 写 → 1h 收尾（commit + 3 行日志）**
> 每天结束必须有一个「能在浏览器/命令行里看到」的产物。超过 1 天没有可运行产物 = 路线跑偏。

---

### 第一阶段：地基（D1–D4）

#### Day 1 · 11/21 六 — 环境 + Java 速通（竞赛选手差异版）

**学（2h）**：不要看 Java 基础教程视频。你需要的只是 C++ 到 Java 的差异点：
- 一切皆对象、引用语义、`null` 与 NPE、基本类型包装类与自动装箱
- `String` 不可变、`equals` vs `==`
- 集合：`List/Map/Set`，重点 `HashMap`、`ArrayList`，`Stream` API（对标 C++ 的 range）
- 泛型、接口、抽象类、`@Override`
- 异常体系：checked / unchecked
- **注解**（`@Service` 之类全部来自这里，理解它就理解了一半 Spring）
- 包管理与 Maven 的 `pom.xml` 是什么

**做（5h）**：
- 装 JDK17 + Maven，配阿里云镜像（`~/.m2/settings.xml`）
- 装 Docker Desktop（WSL2 后端）
- 写 `docker-compose.yml` 起 MySQL 8 / Redis 7 / RabbitMQ，三个都用 Navicat 或命令行连上验证
- 用 start.spring.io 生成项目，跑起 Hello World
- `git init` + `.gitignore`（务必排除 `target/`、`.idea/`）+ 推 GitHub
- 手写 3 个小 Java 程序练手：读文件统计词频、用 Stream 处理 List、写一个带接口和实现类的类

**验收**：`curl localhost:8080/hello` 有返回；三个中间件都能连上。

**避坑**：Docker Desktop 折腾超过 2 小时就停手 —— 直接 SSH 到刚买的云服务器上开发（VSCode Remote），或者本地装 MySQL/Redis 原生版过渡，D8 前再回来解决。**不要为环境浪费一天。**

---

#### Day 2 · 11/22 日 — Spring Boot 骨架 + 第一个 CRUD

**学（2h）**：Spring Boot 三件套概念——IoC/DI（`@Component @Service @Autowired`）、AOP、自动装配；`application.yml` 配置；`@RestController @RequestMapping @GetMapping @RequestBody`。

**做（6h）**：
- 设计 **ER 图**（这一步别省，画在纸上/ProcessOn 上）：
  `user` / `problem` / `testcase` / `submission` / `submission_case` / `contest` / `contest_problem` / `contest_register`
- 建表（字符集 utf8mb4，索引先建 `submission(user_id, create_time)`、`submission(problem_id, status)`）
- 接入 MyBatis-Plus：实体类 + Mapper + Service + Controller，**用代码生成器**生成骨架
- 打通 `POST /api/user/register` 一个接口，用 Knife4j 测通

**验收**：Knife4j 页面能注册一个用户，数据库里有数据。

**避坑**：`mybatis-plus-spring-boot3-starter` 而不是 `mybatis-plus-boot-starter`，写错会启动报错。

---

#### Day 3 · 11/23 一 — 工程规范 + 用户模块

**学（2h）**：统一响应体、全局异常处理（`@RestControllerAdvice`）、参数校验（`@Valid` + `jakarta.validation`）、日志（Slf4j + logback）、BCrypt 密码加密。

**做（6h）**：
- `Result<T>` 统一返回 + 错误码枚举
- `GlobalExceptionHandler` 兜住所有异常（业务异常、参数校验异常、系统异常三档）
- `@Valid` 校验注册/登录参数
- 注册（BCrypt 加密）、登录、查询个人信息、修改昵称
- 分层 DTO / VO，**不要直接把 Entity 返回给前端**（面试会问）

**验收**：注册 → 登录 → 拿 token → 查个人信息，全程错误码规范。

---

#### Day 4 · 11/24 二 — JWT + 权限

**学（2h）**：JWT 结构（header/payload/signature）、无状态登录的优缺点、Spring Security 6 过滤链（`SecurityFilterChain`）、RBAC。

**做（6h）**：
- JWT 生成/解析工具类，登录成功后返回 token
- Spring Security 6 + 自定义 `JwtAuthenticationFilter`
- 白名单放行登录/注册/题目列表
- `ThreadLocal` 存当前登录用户（写完记得在 filter 的 finally 里 remove，**内存泄漏坑，面试会问**）
- 角色区分 USER / ADMIN，`@PreAuthorize` 控制管理接口

**验收**：不带 token 访问受保护接口返回 401；普通用户访问管理接口返回 403。

**避坑**：Spring Security 6 的配置写法跟 5 差别很大，网上老教程大量是 5 的。**卡超过 4 小时就降级**成自定义 HandlerInterceptor + JWT，功能一样，面试照样能讲。

---

### 第二阶段：业务主干（D5–D6）

#### Day 5 · 11/25 三 — 题目模块

**学（1h）**：MyBatis-Plus 分页插件、条件构造器 `LambdaQueryWrapper`、文件上传。

**做（7h）**：
- 题目 CRUD（管理员），字段：标题、题面(Markdown)、输入/输出说明、时间/内存限制、难度、样例、通过数/提交数
- 题目列表：分页 + 关键字搜索 + 难度筛选 + 通过率排序
- 题目详情（返回 Markdown 原文，前端渲染）
- 测试点上传：接收 zip → 解析成多个 `.in/.out` → 存入 `testcase` 表
- Knife4j 上全部可调

**验收**：能造 5 道题，每道题有 3-5 个测试点。

---

#### Day 6 · 11/26 四 — 提交模块 + 判题状态机

**学（2h）**：状态机设计、异步任务边界、乐观锁/幂等。

**做（6h）**：
- 判题状态枚举：`PENDING / JUDGING / ACCEPTED / WRONG_ANSWER / TIME_LIMIT_EXCEEDED / MEMORY_LIMIT_EXCEEDED / RUNTIME_ERROR / COMPILE_ERROR / SYSTEM_ERROR / OUTPUT_LIMIT_EXCEEDED`
- 提交接口：校验（用户存在、题目存在、语言支持、代码长度）→ 落库 `PENDING` → 返回 submissionId
- 提交记录列表（我的提交 / 某题的所有提交）+ 提交详情
- **先不接 MQ**，用一个 `@Async` 线程池占位，明天换成 MQ

**验收**：提交后数据库有记录，状态停在 PENDING。

---

### 第三阶段：判题引擎（D7–D11）—— 项目灵魂，不可延期

#### Day 7 · 11/27 五 — Docker 沙箱（手工版）

**先别写 Java 代码。先在命令行把"判题"这件事手工跑通**，你会对后面的一切心里有数。

**学（2h）**：Docker 镜像/容器/卷/网络；`--memory` `--cpus` `--pids-limit` `--network=none` 分别限制什么；exit code 137 = 被 OOM Killer 杀掉。

**做（6h）**：
```bash
# 1. 准备
docker pull gcc:13
mkdir -p /tmp/judge/1 && cd /tmp/judge/1
# 写入 main.cpp 和 in.txt / out.txt

# 2. 编译 + 运行（一条命令）
docker run --rm \
  -v /tmp/judge/1:/box -w /box \
  --memory=256m --memory-swap=256m \
  --cpus=1 --pids-limit=64 \
  --network=none \
  gcc:13 \
  sh -c "g++ -O2 -std=c++17 main.cpp -o main && timeout -s KILL 2s ./main < in.txt"
```
逐条验证四种情况：
1. 正确代码 → 输出与 `out.txt` 一致 → AC
2. 错误答案 → 输出不一致 → WA
3. `while(1);` → 触发 timeout → TLE
4. 申请 1G 内存 → exit 137 → MLE
5. 语法错误 → g++ 报错到 stderr → CE

**验收**：五种情况全部手工复现，你能说清每一种是怎么被识别出来的。

**避坑**：注意输出重定向和 stderr 分离，CE 信息要回显给用户。

---

#### Day 8 · 11/28 六 — 沙箱 Java 封装

**学（2h）**：docker-java 的 `DockerClient` / `CreateContainerCmd` / `HostConfig` / `ExecCreateCmd`；`CompletableFuture` 做超时控制。

**做（6h）**：
- `JudgeService`：
  - `createContainer`：镜像、命令、资源限制、卷挂载、工作目录
  - `startContainer` + 带超时的 `waitContainer`（超时则 `killContainer`）
  - `logsContainer` 拿 stdout/stderr（**务必限制读取字节数**，防止死循环输出把内存打爆）
  - `removeContainer` 写进 `finally`，**保证任何分支都回收**
- `OutputComparator`：逐行比对，忽略行尾空格、忽略末尾空行
- 容器命名/打 label 带上 submissionId，方便清理
- 本地单测：拿 D7 的五个 case 跑通

**验收**：调用 `judge(code, testcase)` 能返回 `{status, time, memory, output}`。

**⚠️ 如果 D8 结束还没跑通，D9 一整天继续搞，不要往下推进。** 后面所有东西都建立在这上面。

**性能事实（面试要用）**：容器冷启动 500–800ms，远超 1s 的判题时限。所以**编译和执行要放在同一个容器里一次跑完**，否则光是两个容器的启动就超时了。优化方向放进 README：预热容器池 / 常驻容器。

---

#### Day 9 · 11/29 日 — RabbitMQ 接入

**学（2h）**：交换机类型（重点 direct）、队列、手动 ACK、prefetch、死信队列。

**做（6h）**：
- 声明 `judge.exchange`（direct）→ `judge.queue`，绑定 routing key `judge.submit`
- 提交接口改为：落库 → 发消息 → 返回
- 消费者：手动 ACK、`prefetch=1`、遍历测试点逐个判、汇总结果、更新 submission
- 死信队列 `judge.dlx` 接住处理失败的消息
- 用 RabbitMQ 管理台（15672）观察消息进出

**验收**：提交后 2 秒内状态从 PENDING 变成终态。

---

#### Day 10 · 11/30 一 — 判题健壮性

**做（8h）**：
- **幂等**：消费前查状态，已是终态直接 ACK 返回（网络重投递会重复消费）
- **重试**：失败 3 次进死信，状态置 `SYSTEM_ERROR` 并记录原因
- **容器回收**：定时任务扫带 label 且超过 10 分钟的容器并删除（进程崩溃会留僵尸容器）
- **每测试点结果落库**：`submission_case` 表记录每个点的状态/耗时/内存
- **超时保护**：整个判题任务设总超时，防止单个 case 卡死整个 Worker
- **并发控制**：Worker 内并发数可配（比如 2），避免一台机器同时起 20 个容器

**验收**：故意提交一个死循环代码，系统能正确返回 TLE，且 `docker ps` 里没有残留容器。

---

#### Day 11 · 12/1 二 — 多语言 + 比对规则 + 特判（可选）

**做（8h）**：
- 语言适配：C++17（gcc:13）、Java 17（eclipse-temurin:17-jdk）、Python 3.12（python:3.12-slim），每种语言的编译/运行命令做成配置表
- 输出比对升级：忽略行尾空格、忽略末尾空行；浮点数按相对/绝对误差比较
- **Special Judge（可选，加分）**：支持上传一个自定义 checker 程序，用于"输出不唯一"的题。你是竞赛选手，这块讲起来会很自然
- 提交历史里增加语言字段和一键复制

**验收**：同一道题分别用三种语言提交都能 AC。

**时间不够就砍特判**，多语言必须保留（简历上要写）。

---

### 第四阶段：中间件与实时性（D12–D16）

#### Day 12 · 12/2 三 — WebSocket 实时推送

**学（2h）**：WebSocket 握手、STOMP、心跳与断线重连。

**做（6h）**：
- Spring WebSocket + STOMP，连接时用 JWT 鉴权，按 userId 订阅 `/user/queue/judge`
- 判题 Worker 完成时推结果给提交者
- 前端（还没写，先用 HTML 测试页）验证能收到
- **轮询兜底**：前端同时也支持每 2s 查一次，面试可以讲"为什么不能只依赖推送"

**验收**：curl/测试页能实时看到 `PENDING → JUDGING → ACCEPTED`。

---

#### Day 13 · 12/3 四 — Redis 缓存

**学（3h）**：Cache Aside、缓存穿透/击穿/雪崩三兄弟及各自解法、TTL 设计。

**做（5h）**：
- 题目详情缓存：`problem:detail:{id}`，TTL 30min + 随机抖动；更新/删除时删缓存
- 首页热题/题目列表首页缓存
- **缓存穿透**：查不到时缓存空值（短 TTL）
- **缓存击穿**：互斥锁重建（`setnx` + Lua 释放）或逻辑过期
- 写清楚每一条在代码里的位置，面试要指得出来

**验收**：用日志证明第二次查同一道题没走数据库。

---

#### Day 14 · 12/4 五 — 限流 + 排行榜 + 分布式锁

**做（8h）**：
- **提交限流**：Lua 脚本做固定窗口/令牌桶，key = `limit:submit:{userId}`，3 秒 1 次；被限流返回明确的错误码
- **排行榜**：ZSet `rank:ac`，member=userId，score=通过题数（同分按最后 AC 时间）。定时/触发式更新
- **分布式锁**：Redisson 或 `setnx + Lua`，用于"同一题目通过数统计"的并发更新
- 把这三个都写进 README 的「技术亮点」

**验收**：1 秒内连点两次提交，第二次被拦；排行榜能正确排序。

---

### 第五阶段：比赛模块（D15–D16）

#### Day 15 · 12/5 六 — 比赛基础

**做（8h）**：
- `contest` 表：标题、开始/结束时间、封榜时长、赛制
- 报名、比赛题目列表（打乱顺序、用字母 A/B/C 标识）
- 时间窗校验：未开始不能进、进行中才能提交、结束后提交记入练习不记入比赛
- 比赛状态机：未开始 / 进行中 / 已结束（用时间计算，不用定时器改状态）

**验收**：能创建一场 2 小时后开始的比赛并正确拦截。

---

#### Day 16 · 12/6 日 — ICPC 排名 + 封榜

**做（8h）**：这是你最擅长的部分，直接把它做成亮点。
- 排名规则：按通过题数降序，同题数按罚时升序
- 罚时 = 每题首次 AC 的时间（分钟）+ 20 × 该题 AC 前的错误提交次数
- **封榜**：比赛最后 30 分钟内的提交不更新公开榜单（参赛者仍能看到自己的）
- 实时计算 + Redis 缓存 10s，避免每次刷新打穿数据库
- 前端要能显示倒计时

**验收**：构造 3 个用户、若干提交，排名和罚时与手算一致。

---

### 第六阶段：缓冲 + 前端（D17–D20）

#### Day 17 · 12/7 一 — 缓冲 / 还债日

- 补齐前面欠的功能
- 开始写单元测试：`OutputComparator`、状态机、罚时计算（逻辑纯函数最好测）
- **检查点**：此刻如果离 M5（前端）还很远，按下面的裁剪顺序砍功能

**裁剪顺序（从先砍到后砍）**：
1. Special Judge
2. 比赛封榜
3. 比赛报名 / 比赛页前端
4. 多语言（只留 C++）
5. 压测报告（改成 1 页）

---

#### Day 18 · 12/8 二 — 前端 1：框架与登录

**做（8h）**：
- `npm create vite` + Vue 3 + TS（不会 TS 就用 JS，别纠结）+ Element Plus 按需引入 + Pinia + Vue Router
- 布局：直接抄 Element Plus 官方 `Container` 示例（侧边栏 + 顶栏 + 内容区），**不要自己设计**
- axios 封装：请求拦截器带 token、响应拦截器统一弹错误、401 跳登录
- 登录/注册页 + 路由守卫
- 联调 Knife4j 里已有的接口

**验收**：浏览器里能注册、登录、看到自己的昵称。

---

#### Day 19 · 12/9 三 — 前端 2：题库与提交

**做（8h）**：
- 题目列表：表格或卡片 + 分页 + 难度标签（颜色区分）+ 搜索框
- 题目详情：Markdown 渲染（`markdown-it`）+ 样例区块
- 代码编辑器：Monaco Editor（`monaco-editor`，自带 C++/Java/Python 高亮）
- 语言下拉 + 提交按钮 → 跳转提交详情
- 提交详情页：轮询/WebSocket 展示状态变化，AC 绿色、WA 红色

**验收**：能在网页上完整完成一次"看题 → 写代码 → 提交 → AC"。

---

#### Day 20 · 12/10 四 — 前端 3：记录、排行、比赛

**做（8h）**：
- 提交记录列表（我的提交 / 全部提交，状态彩色标签）
- 个人主页：通过题数、提交数、通过率
- 排行榜页
- 比赛页：列表 + 详情 + 倒计时 + 实时排名表格
- 首页：随便做点统计 + 公告，**别花时间**，截图好看就行

**验收**：全站流程可跑通，没有白屏和接口 404。

---

### 第七阶段：上线与包装（D21–D25）

#### Day 21 · 12/11 五 — 部署上线

**学（2h）**：Dockerfile 多阶段构建、Compose 编排、Nginx 反向代理。

**做（6h）**：
- 后端 `Dockerfile`：`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre` 运行
- `docker-compose.yml`：mysql / redis / rabbitmq / app / nginx 五个服务，网络互通，数据卷持久化
- 前端 `npm run build` → 产物挂给 Nginx；`/api` 反代到后端
- **判题服务需要挂载 `/var/run/docker.sock`** —— 这是判题架构的必然选择，也是绝佳的面试话题（Docker-in-Docker 的替代方案、权限风险、生产上应做的隔离）
- 上传服务器，`docker compose up -d`，安全组开 80 端口
- 记下公网 IP，写进 README

**验收**：手机流量下能打开你的网站并提交一道题 AC。

---

#### Day 22 · 12/12 六 — 压测与优化

**做（8h）**：
- 用 `wrk` 或 JMeter 压两个接口：`/api/problem/list`、`/api/submission`（注意后者受限于判题并行度，压的是入队能力）
- 记录：QPS、平均 RT、P99、错误率；**优化前后各测一次**
- 优化项：
  - `EXPLAIN` 看慢查询，补索引（`submission(user_id, create_time)`、`problem(difficulty, status)`）
  - HikariCP 连接池参数
  - 深分页优化（`WHERE id > ?` 游标分页）
  - Redis 缓存命中率
- **输出一份 `docs/benchmark.md`**：环境、脚本、前后对比表格。这一页纸能让你的项目从"做过"变成"调过"

**验收**：文档里有真实的数字，能说出"我改了什么，QPS 从多少到多少"。

---

#### Day 23 · 12/13 日 — 测试 + 文档

**做（8h）**：
- JUnit5 + Mockito：判题状态机、输出比对、罚时计算、限流逻辑
- 集成测试（可选，加分）：Testcontainers 起一个真 MySQL 跑关键接口
- **README**（面试官真的会看）：
  - 一句话简介 + 在线演示地址 + 截图
  - 技术栈
  - 架构图（draw.io / excalidraw，15 分钟画一个）
  - 核心设计说明：判题沙箱怎么做、MQ 怎么保证不丢、缓存怎么防击穿
  - 部署指南（别人能照着跑起来）
  - 压测数据
- 整理 Git 历史：合并碎提交，确认没有提交 `application.yml` 里的密码

**验收**：一个陌生人照 README 能在 10 分钟内把项目跑起来。

---

#### Day 24 · 12/14 一 — 演示视频 + 简历

**做（8h）**：
- 录 2–3 分钟屏（OBS 或 Windows 自带录制）：注册 → 题库 → 打开一题 → 写代码 → 提交 → 实时看到 Judging → AC → 提交记录（展示每个测试点）→ 排行榜 → 比赛页
- 剪辑掉卡顿，配上字幕说明（B站/YouTube 上传，链接放 README）
- 写简历项目条目（见下方模板），把所有 XX 换成真实数字

**验收**：视频能让一个不懂 OJ 的人 3 分钟内看懂你做了什么。

---

#### Day 25 · 12/15 二 — 面试自测

针对项目，自己出 30 题并写出答案，存到 `docs/INTERVIEW.md`。写不出来 = 这块不能写进简历。

**判题/沙箱**
1. 判题怎么保证安全？容器能逃逸吗？你的方案有什么风险？
2. 内存超限怎么检测的？exit code 137 是什么意思？
3. 死循环程序怎么终止？
4. 恶意程序疯狂输出怎么办？
5. 单次判题耗时多少？瓶颈在哪？
6. 怎么把判题吞吐提高 10 倍？
7. 为什么编译和执行放在同一个容器？

**消息队列**
8. 为什么用 MQ 而不是线程池/异步方法？
9. 消息丢失的三个环节分别怎么处理？
10. 重复消费怎么保证幂等？
11. 队列积压了怎么办？
12. 消费者重启，正在判的任务会怎样？

**Redis**
13. 缓存穿透/击穿/雪崩分别怎么解决？你代码里在哪？
14. 缓存和数据库怎么保证一致？为什么用删除而不是更新？
15. 排行榜为什么用 ZSet？
16. 限流为什么要用 Lua？
17. 分布式锁的实现和坑（锁续期、误删）？

**MySQL**
18. 表怎么设计的？为什么这么分表？
19. 索引怎么建的？依据是什么？
20. 慢 SQL 怎么排查？
21. 深分页怎么优化？
22. 事务边界在哪？用 `@Transactional` 有什么坑？

**并发/架构**
23. 同一用户并发提交两次会怎样？
24. Worker 并发数怎么定？线程池参数怎么算？
25. 为什么不做微服务？
26. 服务怎么水平扩展？扩展后 WebSocket 怎么办？
27. JWT 怎么让 token 提前失效？
28. 你的权限模型是什么？
29. 这个项目最大的不足是什么？
30. 流量涨 10 倍怎么办？

---

## 七、简历条目模板（把 XX 换成真实数字）

```
在线判题系统（个人项目）                                    2026.11 – 2026.12
技术栈：Spring Boot 3 / MyBatis-Plus / MySQL / Redis / RabbitMQ / Docker / Vue 3
在线演示：http://XX.XX.XX.XX    源码：github.com/xxx/online-judge

· 独立设计并实现支持 C++/Java/Python 的在线判题系统，覆盖用户、题库、提交、
  比赛（ICPC 赛制）、排行榜等模块，已部署上线并稳定运行。
· 基于 Docker + docker-java 自研判题沙箱，通过 cgroups 限制 CPU/内存/PID 并
  隔离网络，结合超时熔断、输出截断与容器自动回收，单次判题平均耗时 XX ms。
· 使用 RabbitMQ 解耦提交与判题，Worker 可水平扩展，实现手动 ACK、幂等消费与
  死信重试；3 个 Worker 下判题吞吐达 XX 次/秒。
· 基于 Redis 实现题目缓存、ZSet 实时排行榜与 Lua 脚本提交限流，接口 P99 由
  XX ms 优化至 XX ms（wrk 压测数据）。
```

> 铁律：**简历上每个技术名词，都必须是你能讲 5 分钟实现细节的东西。** 讲不清的直接删，比被问穿好一万倍。

---

## 八、风险与纪律

**五条纪律**
1. 每天至少一次 git commit，message 写清楚做了什么（面试官会看提交记录）
2. 每天收工写 3 行日志：今天完成了什么 / 卡在哪 / 明天第一件事
3. **一个问题卡 2 小时就换路**：问 AI、搜 issue、降级方案，不要死磕
4. 用 AI 生成代码可以提高速度，但**留下来的每一行你都要能解释**。看不懂的删掉重写
5. 每个模块只做到"能演示 + 能讲清楚"，不做边缘 case 的完美主义

**五个最可能的失败点**
1. **D10 判题没闭环** —— 这是唯一不可延期的红线，延期就砍 D15–D16 的比赛模块来补
2. **Docker Desktop 装不上** —— 立刻转云端开发，别耗在本地环境
3. **Spring Security 6 卡死** —— 4 小时降级为拦截器
4. **前端 UI 纠结** —— 抄 Element Plus 示例，你不需要设计能力
5. **最后没时间写文档** —— README 从 D1 就开一个空文件，每天随手加两行

---

## 九、25 天之后（重要）

项目只是简历这一关。大厂暑期实习是 **简历 → 笔试 → 面试** 三关：

- **笔试**：你有竞赛底子，刷 100 道 Hot 100 就够，寒假两周解决
- **面试八股**：JavaGuide + 小林 coding，JVM / 并发 / MySQL / Redis / 网络，12 月底到 2 月底刷
- **时间线**：春招提前批约 2027 年 2 月，暑期实习 3–4 月。你有 2 个月缓冲，够

所以这 25 天**不用焦虑地追求完美**——把它做成"能演示、能讲透、有数据"的项目，剩下的交给后面的八股阶段。
