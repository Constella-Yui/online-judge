-- Online Judge 初始表结构
-- 由 MySQL 容器首次启动时自动执行（/docker-entrypoint-initdb.d）
-- 只在数据卷为空时运行一次；改表结构请写增量脚本并手动执行

SET NAMES utf8mb4;
USE oj;

-- ============ 用户 ============
CREATE TABLE `user` (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username       VARCHAR(32)     NOT NULL COMMENT '登录名',
  password       VARCHAR(100)    NOT NULL COMMENT 'BCrypt 哈希',
  nickname       VARCHAR(32)     DEFAULT NULL,
  email          VARCHAR(128)    DEFAULT NULL,
  avatar         VARCHAR(255)    DEFAULT NULL,
  role           TINYINT         NOT NULL DEFAULT 0 COMMENT '0 普通 1 管理员',
  submit_count   INT             NOT NULL DEFAULT 0,
  accepted_count INT             NOT NULL DEFAULT 0,
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 0 封禁',
  create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_accepted (accepted_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ============ 题目 ============
CREATE TABLE `problem` (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title          VARCHAR(128)    NOT NULL,
  content        MEDIUMTEXT      COMMENT '题面 Markdown',
  input_desc     TEXT,
  output_desc    TEXT,
  sample_json    JSON            COMMENT '[{"input":"","output":""}]',
  difficulty     TINYINT         NOT NULL DEFAULT 1 COMMENT '1~5',
  time_limit     INT             NOT NULL DEFAULT 1000 COMMENT 'ms',
  memory_limit   INT             NOT NULL DEFAULT 256  COMMENT 'MB',
  submit_count   INT             NOT NULL DEFAULT 0,
  accepted_count INT             NOT NULL DEFAULT 0,
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 公开 0 隐藏',
  create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_difficulty_status (difficulty, status),
  KEY idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

-- ============ 测试点 ============
CREATE TABLE `testcase` (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  problem_id      BIGINT UNSIGNED NOT NULL,
  input           MEDIUMTEXT      NOT NULL,
  expected_output MEDIUMTEXT      NOT NULL,
  sort_order      INT             NOT NULL DEFAULT 0,
  score           INT             NOT NULL DEFAULT 0 COMMENT 'OI 赛制用，ICPC 忽略',
  PRIMARY KEY (id),
  KEY idx_problem (problem_id),
  CONSTRAINT fk_testcase_problem FOREIGN KEY (problem_id) REFERENCES problem (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试点';

-- ============ 提交 ============
CREATE TABLE `submission` (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id       BIGINT UNSIGNED NOT NULL,
  problem_id    BIGINT UNSIGNED NOT NULL,
  contest_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '非比赛提交为 NULL',
  language      VARCHAR(16)     NOT NULL COMMENT 'cpp17 / java17 / python3',
  code          MEDIUMTEXT      NOT NULL,
  status        TINYINT         NOT NULL DEFAULT 0 COMMENT '见 JudgeStatus 枚举',
  time_used     INT             DEFAULT NULL COMMENT '最大测试点耗时 ms',
  memory_used   INT             DEFAULT NULL COMMENT '峰值内存 KB',
  error_message TEXT            COMMENT '编译错误信息',
  create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, create_time),
  KEY idx_problem_status (problem_id, status),
  KEY idx_contest_time (contest_id, create_time),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交记录';

-- ============ 单测试点结果 ============
CREATE TABLE `submission_case` (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  submission_id BIGINT UNSIGNED NOT NULL,
  testcase_id   BIGINT UNSIGNED NOT NULL,
  status        TINYINT         NOT NULL,
  time_used     INT             DEFAULT NULL,
  memory_used   INT             DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_submission (submission_id),
  CONSTRAINT fk_case_submission FOREIGN KEY (submission_id) REFERENCES submission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单测试点判题结果';

-- ============ 比赛 ============
CREATE TABLE `contest` (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title          VARCHAR(128)    NOT NULL,
  description    TEXT,
  start_time     DATETIME        NOT NULL,
  end_time       DATETIME        NOT NULL,
  freeze_minutes INT             NOT NULL DEFAULT 30 COMMENT '封榜时长',
  status         TINYINT         NOT NULL DEFAULT 0 COMMENT '0 未开始 1 进行中 2 已结束',
  create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛';

CREATE TABLE `contest_problem` (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  contest_id BIGINT UNSIGNED NOT NULL,
  problem_id BIGINT UNSIGNED NOT NULL,
  alias      CHAR(2)         NOT NULL COMMENT 'A/B/C...',
  sort_order INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_contest_problem (contest_id, problem_id),
  KEY idx_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛题目关联';

CREATE TABLE `contest_register` (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  contest_id  BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_contest_user (contest_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛报名';

-- 管理员账号：先用注册接口注册，再执行下面这句
-- UPDATE `user` SET role = 1 WHERE username = 'your_name';
