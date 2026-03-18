package com.health.service.impl;

import com.health.dto.HealthDataRequest;
import com.health.dto.HealthDataResponse;
import com.health.entity.HealthData;
import com.health.repository.HealthDataRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.service.HealthDataService;
import com.health.utils.JwtUtils;
import com.health.utils.RedisUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    private static final String HEALTH_DATA_CACHE_PREFIX = "health:data:";
    private static final int CACHE_EXPIRATION = 5; // 5 minutes

    @Override
    public HealthDataResponse addHealthData(HealthDataRequest request) {
        HealthData healthData = new HealthData();
        healthData.setUserId(jwtUtils.getCurrentUserId());
        healthData.setType(request.getType());
        healthData.setValue(request.getValue());
        healthData.setUnit(request.getUnit());
        healthData.setRecordDate(LocalDateTime.parse(request.getRecordDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        healthData = healthDataRepository.save(healthData);
        
        // Clear cache
        clearHealthDataCache(jwtUtils.getCurrentUserId(), request.getType());
        
        return convertToResponse(healthData);
    }

    @Override
    public List<HealthDataResponse> getHealthDataList(String type, String startDate, String endDate) {
        Long userId = jwtUtils.getCurrentUserId();
        String cacheKey = HEALTH_DATA_CACHE_PREFIX + userId + ":" + type + ":" + (startDate != null ? startDate : "all") + ":" + (endDate != null ? endDate : "all");
        
        // Try to get from cache
        String cachedData = redisUtils.get(cacheKey);
        if (cachedData != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cachedData, new TypeReference<List<HealthDataResponse>>() {});
            } catch (Exception e) {
                // Ignore parsing error, fall back to database
            }
        }
        
        List<HealthData> healthDataList;
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            healthDataList = healthDataRepository.findByUserIdAndTypeAndRecordDateBetween(userId, type, start, end);
        } else {
            healthDataList = healthDataRepository.findByUserIdAndTypeOrderByRecordDateDesc(userId, type);
        }
        
        List<HealthDataResponse> responseList = healthDataList.stream().map(this::convertToResponse).collect(Collectors.toList());
        
        // Cache the result
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            redisUtils.set(cacheKey, objectMapper.writeValueAsString(responseList), CACHE_EXPIRATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Ignore caching error
        }
        
        return responseList;
    }

    @Override
    public List<HealthDataResponse> getHealthDataTrend(String type, String period) {
        Long userId = jwtUtils.getCurrentUserId();
        int limit = 7; // 默认最近7天
        if ("month".equals(period)) {
            limit = 30;
        } else if ("year".equals(period)) {
            limit = 365;
        }
        List<HealthData> healthDataList = healthDataRepository.findRecentHealthDataByType(userId, type, limit);
        return healthDataList.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteHealthData(Long id) {
        HealthData healthData = healthDataRepository.findById(id).orElseThrow(() -> new RuntimeException("健康数据不存在"));
        if (!healthData.getUserId().equals(jwtUtils.getCurrentUserId())) {
            throw new RuntimeException("无权删除此数据");
        }
        healthDataRepository.delete(healthData);
        
        // Clear cache
        clearHealthDataCache(healthData.getUserId(), healthData.getType());
    }

    private void clearHealthDataCache(Long userId, String type) {
        // Clear all caches for this user and type
        String pattern = HEALTH_DATA_CACHE_PREFIX + userId + ":" + type + ":*";
        // In a real Redis environment, we would use SCAN to find and delete matching keys
        // For now, we'll just clear the memory cache
        MEMORY.keySet().removeIf(key -> key.startsWith(HEALTH_DATA_CACHE_PREFIX + userId + ":" + type));
    }

    private HealthDataResponse convertToResponse(HealthData healthData) {
        HealthDataResponse response = new HealthDataResponse();
        BeanUtils.copyProperties(healthData, response);
        response.setRecordDate(healthData.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setCreatedAt(healthData.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }

    // Static memory cache for fallback when Redis is not available
    private static final java.util.Map<String, String> MEMORY = new java.util.HashMap<>();
}

