package com.example.demo.sample;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity（实体）—— Java 对象 ↔ 数据库表的一行。
 *
 * 前端类比：
 *   - 类似 Prisma 的 model / TypeORM 的 @Entity
 *   - 一个 DemoSample 实例 ≈ SELECT 出来的一行记录
 *
 * 注意：字段名用 camelCase，列名用 snake_case，由 @Column(name = "...") 桥接。
 */
@Entity // 告诉 JPA：这是一张表对应的类
@Table(name = "demo_sample") // 映射到哪张表（不写则默认用类名）
public class DemoSample {

    @Id // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 对应 MySQL AUTO_INCREMENT
    private Long id;

    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 0草稿 / 1启用 / 2禁用；库里是 TINYINT，要用 TINYINT 映射，否则 ddl-auto=validate 会报类型不一致 */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    private BigDecimal score;

    /** MySQL TINYINT(1) ↔ Java Boolean */
    @Column(name = "is_vip", nullable = false)
    private Boolean vip;

    @Column(name = "tags", length = 255)
    private String tags;

    /** JSON 列先当字符串存；需要时再 ObjectMapper 解析成 Map/对象 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_json")
    private String profileJson;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, insertable = true, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- getter / setter ---
    // JPA 和 Jackson（JSON 序列化）都靠它们读写字段。
    // 前端类比：class 上的 getXxx/setXxx，序列化成 JSON 时字段名默认是去掉 get 后的驼峰名。

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Boolean getVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getProfileJson() {
        return profileJson;
    }

    public void setProfileJson(String profileJson) {
        this.profileJson = profileJson;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
