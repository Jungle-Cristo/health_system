package com.health.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "health_data")
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "measurement_value", nullable = false)
    private Double value;

    @Column(name = "unit")
    private String unit;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
