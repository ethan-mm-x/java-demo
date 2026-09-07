package com.example.demo.sample;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service（服务层）—— 业务逻辑放这里。
 *
 * 分层习惯（前端可对照 NestJS）：
 *   Controller  → 解析 HTTP、参数校验、返回响应
 *   Service     → 业务规则（唯一性、默认值、组装实体）
 *   Repository  → 读写数据库
 *
 * @Service：注册成 Spring Bean，别处用构造器注入即可拿到实例。
 * 类比：NestJS 的 @Injectable() provider。
 */
@Service
public class DemoSampleService {

    private final DemoSampleRepository repository;

    /** 构造器注入：Spring 自动把 Repository 实现塞进来（类似 DI） */
    public DemoSampleService(DemoSampleRepository repository) {
        this.repository = repository;
    }

    /** 列表：可选按 status 过滤；不传则查全部 */
    @Transactional(readOnly = true)
    public List<DemoSample> list(Integer status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findByStatus(status);
    }

    /** 详情：找不到就抛 404 */
    @Transactional(readOnly = true)
    public DemoSample getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "样例不存在: id=" + id));
    }

    /** 创建 */
    @Transactional
    public DemoSample create(DemoSampleRequest request) {
        if (repository.existsByCode(request.code())) {
            // 409 Conflict：资源冲突（code 唯一键）
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "code 已存在: " + request.code());
        }

        DemoSample entity = new DemoSample();
        applyRequest(entity, request);

        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // save：INSERT；返回的对象带上数据库生成的 id
        return repository.save(entity);
    }

    /** 全量更新（PUT）：用请求体覆盖业务字段 */
    @Transactional
    public DemoSample update(Long id, DemoSampleRequest request) {
        DemoSample entity = getById(id);

        // code 改成别人已占用的值 → 冲突
        repository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "code 已存在: " + request.code());
            }
        });

        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        // 处于 @Transactional 中的托管实体，结束时会自动 UPDATE；
        // 显式 save 更直观，学习阶段推荐写出来。
        return repository.save(entity);
    }

    /** 删除 */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "样例不存在: id=" + id);
        }
        repository.deleteById(id);
    }

    /** 把 DTO 字段抄到 Entity（创建/更新共用，避免两套赋值） */
    private void applyRequest(DemoSample entity, DemoSampleRequest request) {
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setStatus(request.status());
        entity.setScore(request.score());
        entity.setVip(request.vip());
        entity.setTags(request.tags());
        entity.setProfileJson(request.profileJson());
        entity.setRemark(request.remark());
        entity.setBirthday(request.birthday());
        entity.setLastLoginAt(request.lastLoginAt());
    }
}
