package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 全局异常处理 —— 把异常统一成 JSON 错误响应。
 *
 * 前端类比：Express 的 error middleware，或 NestJS 的 ExceptionFilter。
 * 没有它时，校验失败可能返回一长串难读的默认错误结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid 校验失败 → 400 + 字段错误列表 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        err -> err.getField(),
                        err -> err.getDefaultMessage() == null ? "invalid" : err.getDefaultMessage(),
                        (a, b) -> a));

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "validation_failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /** 国密 demo 里 hex 格式错误、密钥长度不对、密文无效等 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "bad_request");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /** Service 里抛的 ResponseStatusException（404 / 409 等） */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getReason());
        return ResponseEntity.status(status).body(body);
    }
}
