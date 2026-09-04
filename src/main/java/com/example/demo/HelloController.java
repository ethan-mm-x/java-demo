package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器 —— 处理 HTTP 请求，类似 Express 的 router / NestJS 的 Controller。
 *
 * 前端类比：
 *   app.get('/hello', (req, res) => res.send('Hello World'))
 * 在这里由注解声明路由，方法返回值会直接作为响应体。
 */
@RestController // = @Controller + @ResponseBody：返回值序列化成 HTTP 响应体（不是页面模板）
public class HelloController {

    /**
     * 处理 GET /hello。
     * 返回 String 时，Content-Type 默认是 text/plain，响应体就是这段文字。
     */
    @GetMapping("/hello") // 映射 HTTP GET，路径为 /hello
    public String hello() {
        return "Hello World";
    }
}
