# java-demo

面向前端同学的 Spring Boot Hello World 示例：用最少文件跑通一个 HTTP 接口。

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

库 `java_demo` 里有一张字段较全的样例表，方便后期练增删改查。建表脚本：[`sql/init-demo-sample.sql`](sql/init-demo-sample.sql)。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键，自增 |
| `code` | VARCHAR(32) | 业务编码，唯一 |
| `name` | VARCHAR(100) | 名称 |
| `status` | TINYINT | 0草稿 / 1启用 / 2禁用 |
| `score` | DECIMAL(10,2) | 分数或金额 |
| `is_vip` | TINYINT(1) | 是否 VIP |
| `tags` | VARCHAR(255) | 标签字符串 |
| `profile_json` | JSON | JSON 扩展字段 |
| `remark` | TEXT | 长文本 |
| `birthday` | DATE | 日期 |
| `last_login_at` | DATETIME | 最近登录 |
| `created_at` / `updated_at` | DATETIME | 创建/更新时间 |

**注意**：挂到 `/docker-entrypoint-initdb.d` 的脚本只在 **数据卷第一次初始化** 时自动执行。容器已经跑过、数据还在时，需要手动执行一次：

```bash
docker compose exec -T mysql mysql -udemo -pdemo java_demo < sql/init-demo-sample.sql
```

脚本可重复执行（`CREATE IF NOT EXISTS` + `INSERT IGNORE`）。在 dbx 里刷新后应能看到表和 5 行样例数据。

验证：

```bash
docker compose exec -T mysql mysql -udemo -pdemo java_demo -e "SELECT id, code, name, status FROM demo_sample;"
```

## 启动 Spring Boot

```bash
mvn spring-boot:run
```

## 验证

```bash
curl http://localhost:8080/hello
```

期望输出：

```text
Hello World
```

## 项目结构（每个文件做什么）

```text
java-demo/
├── docker-compose.yml      # 本地 MySQL + Redis
├── sql/
│   └── init-demo-sample.sql # MySQL 学习测试表
├── pom.xml                 # Maven 配置（≈ package.json）
├── .gitignore              # Git 忽略规则
├── README.md               # 本说明文档
└── src/main/java/com/example/demo/
    ├── DemoApplication.java   # 程序入口，启动内嵌服务器
    └── HelloController.java   # 定义 GET /hello 接口
```

| 文件 | 作用 | 前端类比 |
| --- | --- | --- |
| [`docker-compose.yml`](docker-compose.yml) | 一键拉起本地 MySQL、Redis 容器，并映射端口到本机。 | 本地起依赖服务的 compose（类似前端本地起 mock DB） |
| [`sql/init-demo-sample.sql`](sql/init-demo-sample.sql) | 在 `java_demo` 库建 `demo_sample` 表并插入样例数据。 | 本地 seed / migration SQL |
| [`pom.xml`](pom.xml) | 声明项目信息、依赖、Java 版本、打包插件。Maven 根据它下载依赖并构建。 | `package.json` + 部分构建配置 |
| [`.gitignore`](.gitignore) | 告诉 Git 不要提交 `target/`（编译产物）、IDE 配置等。 | 前端项目里的 `.gitignore` |
| [`DemoApplication.java`](src/main/java/com/example/demo/DemoApplication.java) | `main` 方法所在类；运行后启动 Spring Boot 和内嵌 Tomcat（默认 8080）。 | `server.js` / `main.ts` |
| [`HelloController.java`](src/main/java/com/example/demo/HelloController.java) | 用注解声明路由：`GET /hello` 返回字符串 `Hello World`。 | Express 的 `app.get('/hello', ...)` |
| `target/`（本地生成，已忽略） | `mvn` 编译/打包后的 `.class`、`.jar` 等输出目录，不要提交。 | `dist/` / `build/` |

## 请求怎么走到代码里

```text
浏览器/curl
    │  GET http://localhost:8080/hello
    ▼
内嵌 Tomcat（Spring Boot 自带）
    │
    ▼
HelloController.hello()   ← @GetMapping("/hello")
    │
    ▼
返回 "Hello World" 作为响应体
```

## 和前端习惯的一点差异

- **先编译再运行**：改完 `.java` 要重新编译/重启（`mvn spring-boot:run`），不像 Node 热更新那么自然。
- **注解代替手写注册**：路由、返回格式多靠 `@RestController`、`@GetMapping` 声明，而不是手动 `app.get`。
- **包名即目录**：`package com.example.demo` 必须和文件夹路径 `com/example/demo` 一致。
