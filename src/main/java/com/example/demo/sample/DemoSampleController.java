package com.example.demo.sample;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller —— HTTP 接口入口（增删改查）。
 *
 * 前端类比（Express / NestJS）：
 *   GET    /api/samples       → 列表
 *   GET    /api/samples/:id   → 详情
 *   POST   /api/samples       → 创建
 *   PUT    /api/samples/:id   → 更新
 *   DELETE /api/samples/:id   → 删除
 *
 * 请求流：
 *   HTTP → Controller（路由/校验）→ Service（业务）→ Repository（SQL）→ MySQL
 */
@RestController
@RequestMapping("/api/samples") // 本类所有方法的公共前缀
public class DemoSampleController {

    private final DemoSampleService service;

    public DemoSampleController(DemoSampleService service) {
        this.service = service;
    }

    /**
     * 列表。
     * 可选查询参数 status，例如：GET /api/samples?status=1
     * required = false 表示可以不传（类比 query 可选参数）。
     */
    @GetMapping
    public List<DemoSample> list(
            @RequestParam(required = false) Integer status) {
        return service.list(status);
    }

    /**
     * 详情。
     * @PathVariable 把 URL 里的 {id} 绑到方法参数（类比 req.params.id）。
     */
    @GetMapping("/{id}")
    public DemoSample detail(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * 创建。
     * @RequestBody：把 JSON 反序列化成 DemoSampleRequest。
     * @Valid：触发字段上的校验注解；失败会自动返回 400。
     * @ResponseStatus(CREATED)：成功时 HTTP 201（默认 POST 也常用 200，这里更语义化）。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoSample create(@Valid @RequestBody DemoSampleRequest request) {
        return service.create(request);
    }

    /**
     * 更新（全量 PUT）。
     * body 里仍要带齐业务字段；只想改部分字段时，后续可再学 PATCH。
     */
    @PutMapping("/{id}")
    public DemoSample update(
            @PathVariable Long id,
            @Valid @RequestBody DemoSampleRequest request) {
        return service.update(id, request);
    }

    /**
     * 删除。
     * 返回一个简单 JSON，方便前端确认；也可以改成 void + 204 No Content。
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        service.delete(id);
        return Map.of("deleted", true, "id", id);
    }
}
