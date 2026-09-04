package com.example.demo; // 包名：类似前端的模块路径，决定类放在哪个“命名空间”

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口类 —— 相当于 Node 里的 server.js / main.ts。
 *
 * 运行 main 方法后，Spring Boot 会：
 * 1. 启动内嵌 Tomcat（自带 HTTP 服务器，不用单独装 Nginx/Express）
 * 2. 扫描本包及子包下的组件（比如带 @RestController 的类）
 * 3. 注册路由，开始监听端口（默认 8080）
 */
@SpringBootApplication // 组合注解：开启自动配置 + 组件扫描 + 标识这是 Spring Boot 应用
public class DemoApplication {

    /**
     * Java 程序的固定入口：JVM 从这里开始执行。
     * 类比：Node 执行入口文件时跑到的顶层代码。
     */
    public static void main(String[] args) {
        // 启动 Spring 容器，并把当前类作为配置源传进去
        SpringApplication.run(DemoApplication.class, args);
    }
}
