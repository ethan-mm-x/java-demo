package com.example.demo.sample;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository（仓库）—— 数据访问层，专门跟数据库打交道。
 *
 * 前端类比：
 *   - Prisma Client：prisma.demoSample.findMany()
 *   - 或 DAO / data-access 模块
 *
 * 关键：只要继承 JpaRepository&lt;实体, 主键类型&gt;，Spring 会在运行时生成实现类。
 * 你几乎不用写 SQL，就能用 save / findById / findAll / deleteById。
 *
 * 方法命名约定（Query Method）：
 *   findByStatus → WHERE status = ?
 *   findByCode   → WHERE code = ?
 * Spring 根据方法名自动拼 SQL，类似 Prisma 的 where 语法糖。
 */
public interface DemoSampleRepository extends JpaRepository<DemoSample, Long> {

    /** 按业务编码查一条；Optional = 可能有也可能没有（避免 null） */
    Optional<DemoSample> findByCode(String code);

    /** 按状态列出多条 */
    List<DemoSample> findByStatus(Integer status);

    /** 判断 code 是否已存在（创建时做唯一性校验） */
    boolean existsByCode(String code);
}
