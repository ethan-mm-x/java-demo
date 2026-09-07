package com.example.demo.sample;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请求体 DTO（Data Transfer Object）—— 专门承接前端 POST/PUT 传来的 JSON。
 *
 * 为什么不直接用 Entity？
 *   1. 不想让客户端随便改 id / createdAt 等字段
 *   2. 校验规则（必填、长度）放在“入口”更清晰
 *
 * 前端类比：zod schema 推断出的入参类型，或 API 的 Request Body 类型。
 *
 * record = Java 16+ 的不可变数据类，自动生成构造器、getter、equals。
 * JSON 字段名与组件名一致：{"code":"U006","name":"Frank",...}
 */
public record DemoSampleRequest(
        @NotBlank(message = "code 不能为空")
        @Size(max = 32, message = "code 最长 32 字符")
        String code,

        @NotBlank(message = "name 不能为空")
        @Size(max = 100, message = "name 最长 100 字符")
        String name,

        @NotNull(message = "status 不能为空")
        @Min(value = 0, message = "status 只能是 0/1/2")
        @Max(value = 2, message = "status 只能是 0/1/2")
        Integer status,

        @NotNull(message = "score 不能为空")
        @DecimalMin(value = "0.00", message = "score 不能为负")
        BigDecimal score,

        @NotNull(message = "vip 不能为空")
        Boolean vip,

        @Size(max = 255, message = "tags 最长 255 字符")
        String tags,

        String profileJson,

        String remark,

        LocalDate birthday,

        LocalDateTime lastLoginAt
) {
}
