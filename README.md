# java-demo

面向前端同学的 Spring Boot 学习项目：Hello World + MySQL 增删改查（CRUD）。

## 环境要求

- JDK 17+
- Maven 3.6+
- Docker / Docker Compose（本地 MySQL、Redis 用）

## 启动本地 MySQL + Redis

```bash
docker compose up -d
```

查看状态：

```bash
docker compose ps
```

连接信息（仅本地学习用）：

| 服务 | 地址 | 账号 / 说明 |
| --- | --- | --- |
| MySQL | `localhost:3306` | 库名 `java_demo`；用户 `demo` / 密码 `demo`；root 密码 `root` |
| Redis | `localhost:6379` | 无密码 |

常用命令：

```bash
# 看日志
docker compose logs -f mysql
docker compose logs -f redis

# 停容器（数据还在）
docker compose down

# 停容器并删除数据卷（下次 up 会重新执行 init SQL）
docker compose down -v
```

### 学习测试表 `demo_sample`

库 `java_demo` 里有一张字段较全的样例表，方便练增删改查。建表脚本：[`sql/init-demo-sample.sql`](sql/init-demo-sample.sql)。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键，自增 |
| `code` | VARCHAR(32) | 业务编码，唯一 |
| `name` | VARCHAR(100) | 名称 |
| `status` | TINYINT | 0草稿 / 1启用 / 2禁用 |
| `score` | DECIMAL(10,2) | 分数或金额 |
| `is_vip` | TINYINT(1) | 是否 VIP（接口 JSON 字段名是 `vip`） |
| `tags` | VARCHAR(255) | 标签字符串 |
| `profile_json` | JSON | JSON 扩展字段（接口里先当字符串传） |
| `remark` | TEXT | 长文本 |
| `birthday` | DATE | 日期 |
| `last_login_at` | DATETIME | 最近登录 |
| `created_at` / `updated_at` | DATETIME | 创建/更新时间 |

**注意**：挂到 `/docker-entrypoint-initdb.d` 的脚本只在 **数据卷第一次初始化** 时自动执行。容器已经跑过、数据还在时，需要手动执行一次：

```bash
docker compose exec -T mysql mysql -udemo -pdemo java_demo < sql/init-demo-sample.sql
```

脚本可重复执行（`CREATE IF NOT EXISTS` + `INSERT IGNORE`）。

验证：

```bash
docker compose exec -T mysql mysql -udemo -pdemo java_demo -e "SELECT id, code, name, status FROM demo_sample;"
```

## 启动 Spring Boot

先确保 MySQL 已 healthy，再启动：

```bash
mvn spring-boot:run
```

本仓库的 `.mvn/settings.xml` 会覆盖本机全局 Maven 镜像，改走阿里云公共仓库。否则全局配置里的公司 Nexus（`10.126.138.142`）不通时，会报 `Non-resolvable parent POM`。这只影响本项目，不改你机器上的全局 `settings.xml`。

## 验证

### Hello World

```bash
curl http://localhost:8080/hello
```

期望输出：`Hello World`

### 样例 CRUD（`/api/samples`）

```bash
# 列表
curl http://localhost:8080/api/samples

# 按状态过滤
curl 'http://localhost:8080/api/samples?status=1'

# 详情
curl http://localhost:8080/api/samples/1

# 创建
curl -X POST http://localhost:8080/api/samples \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "U006",
    "name": "Frank",
    "status": 1,
    "score": 66.6,
    "vip": false,
    "tags": "learning",
    "profileJson": "{\"city\":\"Guangzhou\",\"level\":1}",
    "remark": "新建一条练手数据",
    "birthday": "2001-02-03",
    "lastLoginAt": null
  }'

# 更新（把上面返回的 id 换进去）
curl -X PUT http://localhost:8080/api/samples/6 \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "U006",
    "name": "Frank Updated",
    "status": 2,
    "score": 70.0,
    "vip": true,
    "tags": "learning,vip",
    "profileJson": "{\"city\":\"Guangzhou\",\"level\":2}",
    "remark": "更新后的备注",
    "birthday": "2001-02-03",
    "lastLoginAt": "2026-09-07T12:00:00"
  }'

# 删除
curl -X DELETE http://localhost:8080/api/samples/6
```

### 国密算法（`/api/gm`）

JDK 不带 SM2/SM3/SM4，本项目用 [BouncyCastle](https://www.bouncycastle.org/) 提供实现。密钥、密文、签名、摘要一律 **hex**；明文按 **UTF-8**。

| 算法 | 角色 | 类比 |
| --- | --- | --- |
| SM2 | 非对称：公钥加密 / 私钥解密，私钥签名 / 公钥验签 | ECDSA + ECIES，曲线 `sm2p256v1` |
| SM4 | 对称：同一把 128-bit 密钥加解密 | AES-128，这里用 CBC + PKCS5Padding |
| SM3 | 摘要：任意输入 → 32 字节哈希 | SHA-256 |

一键跑通（生成密钥、加解密、签名验签）：

```bash
curl http://localhost:8080/api/gm/demo
```

期望：`sm2.decryptOk`、`sm2.verified`、`sm4.decryptOk` 都是 `true`；`sm3.digest` 是 64 个 hex 字符。

分步练习（把 demo 返回的密钥抄进 body）：

```bash
# SM2 密钥对
curl -X POST http://localhost:8080/api/gm/sm2/keypair

# SM2 加密 / 解密
curl -X POST http://localhost:8080/api/gm/sm2/encrypt \
  -H 'Content-Type: application/json' \
  -d '{"publicKey":"<demo 里的 publicKey>","plaintext":"hello-国密"}'

curl -X POST http://localhost:8080/api/gm/sm2/decrypt \
  -H 'Content-Type: application/json' \
  -d '{"privateKey":"<demo 里的 privateKey>","ciphertext":"<上一步的 ciphertext>"}'

# SM2 签名 / 验签
curl -X POST http://localhost:8080/api/gm/sm2/sign \
  -H 'Content-Type: application/json' \
  -d '{"privateKey":"<demo 里的 privateKey>","message":"hello-国密"}'

curl -X POST http://localhost:8080/api/gm/sm2/verify \
  -H 'Content-Type: application/json' \
  -d '{"publicKey":"<demo 里的 publicKey>","message":"hello-国密","signature":"<上一步的 signature>"}'

# SM4 密钥 + 加解密（加密可不传 iv，服务端随机生成并返回）
curl -X POST http://localhost:8080/api/gm/sm4/key

curl -X POST http://localhost:8080/api/gm/sm4/encrypt \
  -H 'Content-Type: application/json' \
  -d '{"key":"<16 字节 hex 密钥>","plaintext":"hello-国密"}'

curl -X POST http://localhost:8080/api/gm/sm4/decrypt \
  -H 'Content-Type: application/json' \
  -d '{"key":"<同一把 key>","ciphertext":"<密文>","iv":"<加密返回的 iv>"}'

# SM3 摘要
curl -X POST http://localhost:8080/api/gm/sm3/digest \
  -H 'Content-Type: application/json' \
  -d '{"message":"hello-国密"}'
```

注意：

- SM2 公钥是未压缩点 `04 || X || Y`（65 字节 hex）；私钥是 32 字节 D 值。
- SM2 密文排列是国标常见的 **C1C3C2**。和只实现 C1C2C3 的库对不上时，先核对这一点。
- SM2 签名默认 userId 为 ASCII `1234567812345678`（GM/T 0009）。
- SM4 解密必须带上加密时的 **同一把 key + 同一个 iv**。

## 项目结构（每个文件做什么）

```text
java-demo/
├── docker-compose.yml
├── sql/init-demo-sample.sql
├── pom.xml
├── README.md
└── src/main/
    ├── resources/
    │   └── application.yml          # 数据源 / JPA 配置（≈ .env）
    └── java/com/example/demo/
        ├── DemoApplication.java     # 启动入口
        ├── HelloController.java     # GET /hello
        ├── GlobalExceptionHandler.java  # 统一错误 JSON
        ├── gm/                      # 国密 SM2 / SM3 / SM4
        │   ├── GmProviders.java          # 注册 BouncyCastle
        │   ├── GmHex.java                # hex 编解码
        │   ├── Sm2Service.java           # 密钥 / 加解密 / 签名验签
        │   ├── Sm3Service.java           # 摘要
        │   ├── Sm4Service.java           # 密钥 / 加解密
        │   └── GmCryptoController.java   # HTTP 接口
        └── sample/                  # demo_sample 的 CRUD
            ├── DemoSample.java           # Entity ↔ 表
            ├── DemoSampleRepository.java # 数据访问
            ├── DemoSampleRequest.java    # 请求体 DTO + 校验
            ├── DemoSampleService.java    # 业务逻辑
            └── DemoSampleController.java # HTTP 接口
```

| 文件 | 作用 | 前端类比 |
| --- | --- | --- |
| [`application.yml`](src/main/resources/application.yml) | 配置端口、MySQL 连接、JPA 行为 | `.env` + 部分 runtime config |
| [`DemoSample.java`](src/main/java/com/example/demo/sample/DemoSample.java) | 表行 ↔ Java 对象 | Prisma model / TypeORM Entity |
| [`DemoSampleRepository.java`](src/main/java/com/example/demo/sample/DemoSampleRepository.java) | 增删改查数据库 | Prisma Client / DAO |
| [`DemoSampleService.java`](src/main/java/com/example/demo/sample/DemoSampleService.java) | 业务规则（唯一性、404） | NestJS Service |
| [`DemoSampleController.java`](src/main/java/com/example/demo/sample/DemoSampleController.java) | 路由与请求/响应 | Express Router / NestJS Controller |
| [`DemoSampleRequest.java`](src/main/java/com/example/demo/sample/DemoSampleRequest.java) | 入参形状 + 校验 | zod schema / Request DTO |
| [`GlobalExceptionHandler.java`](src/main/java/com/example/demo/GlobalExceptionHandler.java) | 把异常转成统一 JSON | Express error middleware |
| [`gm/`](src/main/java/com/example/demo/gm) | 国密 SM2/SM3/SM4 练习（BouncyCastle） | Node `crypto` + 国密实现 |

## 请求怎么走到代码里

```text
浏览器/curl
    │  GET/POST/PUT/DELETE  /api/samples...
    ▼
内嵌 Tomcat
    │
    ▼
DemoSampleController   ← 路由 + @Valid 校验
    │
    ▼
DemoSampleService      ← 业务规则（code 唯一、找不到抛 404）
    │
    ▼
DemoSampleRepository   ← Spring Data JPA 生成 SQL
    │
    ▼
MySQL 表 demo_sample
```

## 和前端习惯的一点差异

- **先编译再运行**：改完 `.java` 要重新编译/重启（`mvn spring-boot:run`），不像 Node 热更新那么自然。
- **注解代替手写注册**：路由、返回格式多靠 `@RestController`、`@GetMapping` 声明，而不是手动 `app.get`。
- **包名即目录**：`package com.example.demo` 必须和文件夹路径 `com/example/demo` 一致。
- **分层**：Controller 不直接操作数据库，先过 Service，再过 Repository（习惯，不是语法强制）。
