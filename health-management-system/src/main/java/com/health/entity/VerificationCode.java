package com.health.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes", indexes = {
    @Index(name = "idx_target", columnList = "target"),
    @Index(name = "idx_type_target", columnList = "type,target")
})
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String target; // email or phone

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 20)
    private String type; // REGISTER, LOGIN, FORGET_PASSWORD

    @Column(nullable = false)
    private LocalDateTime expireTime;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
