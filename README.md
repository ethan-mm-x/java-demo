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

# 停容器并删除数据卷
docker compose down -v
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
