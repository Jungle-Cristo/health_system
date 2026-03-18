package com.health.service;

import com.health.dto.HealthDataRequest;
import com.health.dto.HealthDataResponse;

import java.util.List;

public interface HealthDataService {

    HealthDataResponse addHealthData(HealthDataRequest request);

    List<HealthDataResponse> getHealthDataList(String type, String startDate, String endDate);

    List<HealthDataResponse> getHealthDataTrend(String type, String period);

    void deleteHealthData(Long id);
}
