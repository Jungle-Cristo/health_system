package com.health.dto;

import lombok.Data;

@Data
public class HealthDataResponse {

    private Long id;
    private Long userId;
    private String type;
    private Double value;
    private String unit;
    private String recordDate;
    private String createdAt;
}
