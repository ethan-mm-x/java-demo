-- 学习用样例表：覆盖常见字段类型，方便后期练 JDBC / MyBatis / JPA
-- 可重复执行：CREATE IF NOT EXISTS + INSERT IGNORE（按唯一键 code）

USE java_demo;

-- 确保本脚本按 utf8mb4 读写，避免中文备注被二次编码成乱码
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS demo_sample (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    code            VARCHAR(32)     NOT NULL COMMENT '业务编码',
    name            VARCHAR(100)    NOT NULL COMMENT '名称',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1启用 2禁用',
    score           DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '分数/金额',
    is_vip          TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否 VIP：0否 1是',
    tags            VARCHAR(255)    NULL COMMENT '标签，逗号分隔',
    profile_json    JSON            NULL COMMENT '扩展资料 JSON',
    remark          TEXT            NULL COMMENT '备注长文本',
    birthday        DATE            NULL COMMENT '生日',
    last_login_at   DATETIME        NULL COMMENT '最近登录时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_sample_code (code),
    KEY idx_demo_sample_status (status),
    KEY idx_demo_sample_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习测试样例表';

INSERT IGNORE INTO demo_sample
    (code, name, status, score, is_vip, tags, profile_json, remark, birthday, last_login_at)
VALUES
    (
        'U001',
        'Alice',
        1,
        98.50,
        1,
        'frontend,java',
        JSON_OBJECT('city', 'Shanghai', 'level', 3),
        '前端转 Java 的示例用户',
        '1995-03-12',
        '2026-09-01 10:15:00'
    ),
    (
        'U002',
        'Bob',
        1,
        76.00,
        0,
        'backend',
        JSON_OBJECT('city', 'Beijing', 'level', 2),
        NULL,
        '1998-07-21',
        '2026-09-03 09:00:00'
    ),
    (
        'U003',
        'Carol',
        0,
        0.00,
        0,
        'draft',
        JSON_OBJECT('city', 'Hangzhou', 'level', 1),
        '草稿状态，方便练 UPDATE',
        '2000-01-08',
        NULL
    ),
    (
        'U004',
        'Dave',
        2,
        55.25,
        1,
        'vip,disabled',
        JSON_OBJECT('city', 'Shenzhen', 'level', 4, 'note', 'disabled account'),
        '禁用账号，练条件查询',
        '1992-11-30',
        '2026-08-20 18:30:00'
    ),
    (
        'U005',
        'Eve',
        1,
        88.88,
        0,
        'qa,learning',
        JSON_OBJECT('city', 'Chengdu', 'level', 2, 'skills', JSON_ARRAY('sql', 'redis')),
        '带 JSON 数组的样例',
        '1999-05-05',
        '2026-09-04 08:45:00'
    );
