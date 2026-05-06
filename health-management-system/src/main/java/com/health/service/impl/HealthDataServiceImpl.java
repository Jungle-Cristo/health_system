package com.health.service.impl;

import com.health.dto.HealthDataRequest;
import com.health.dto.HealthDataResponse;
import com.health.entity.HealthData;
import com.health.repository.HealthDataRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.service.HealthDataService;
import com.health.utils.JwtUtils;
import com.health.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CacheUtils redisUtils;

    private static final String HEALTH_DATA_CACHE_PREFIX = "health:data:";
    private static final int CACHE_EXPIRATION = 5; // 5 minutes

    @Override
    public HealthDataResponse addHealthData(HealthDataRequest request) {
        Long userId = getCurrentUserId();
        log.info("添加健康数据，用户ID: " + userId + "，类型: " + request.getType() + "，值: " + request.getValue());
        HealthData healthData = new HealthData();
        healthData.setUserId(userId);
        healthData.setType(request.getType());
        healthData.setDataValue(request.getValue());
        healthData.setUnit(request.getUnit());
        
        // 处理recordDate字段，支持YYYY-MM-DD格式
        if (request.getRecordDate() != null) {
            String recordDateStr = request.getRecordDate();
            try {
                // 尝试解析YYYY-MM-DD格式
                if (recordDateStr.length() == 10) {
                    healthData.setRecordDate(LocalDateTime.parse(recordDateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    // 尝试解析yyyy-MM-dd HH:mm:ss格式
                    healthData.setRecordDate(LocalDateTime.parse(recordDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            } catch (Exception e) {
                // 如果解析失败，使用当前时间
                healthData.setRecordDate(LocalDateTime.now());
                log.info("日期解析错误: " + e.getMessage());
            }
        } else {
            // 如果recordDate为null，使用当前时间
            healthData.setRecordDate(LocalDateTime.now());
        }
        
        healthData = healthDataRepository.save(healthData);
        log.info("健康数据保存成功，ID: " + healthData.getId());
        
        // Clear cache
        clearHealthDataCache(userId, request.getType());
        
        return convertToResponse(healthData);
    }

    @Override
    public List<HealthDataResponse> getHealthDataList(String type, String startDate, String endDate) {
        Long userId = getCurrentUserId();
        log.info("获取健康数据列表，用户ID: " + userId + "，类型: " + type);
        // 处理type参数，当type为空字符串时，视为null
        if (type != null && type.isEmpty()) {
            type = null;
        }
        String typeKey = (type != null) ? type : "all";
        String cacheKey = HEALTH_DATA_CACHE_PREFIX + userId + ":" + typeKey + ":" + (startDate != null ? startDate : "all") + ":" + (endDate != null ? endDate : "all");
        
        // Try to get from cache
        try {
            String cachedData = redisUtils.get(cacheKey);
            if (cachedData != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<HealthDataResponse> cachedResponse = objectMapper.readValue(cachedData, new TypeReference<List<HealthDataResponse>>() {});
                    log.info("从缓存获取健康数据，数量: " + cachedResponse.size());
                    return cachedResponse;
                } catch (Exception e) {
                    // Ignore parsing error, fall back to database
                    log.info("缓存解析错误: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Ignore cache error, fall back to database
            log.info("缓存访问错误: " + e.getMessage());
        }
        
        List<HealthData> healthDataList = new ArrayList<>();
        try {
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if (type != null) {
                    healthDataList = healthDataRepository.findByUserIdAndTypeAndRecordDateBetween(userId, type, start, end);
                    log.info("从数据库获取指定类型健康数据，数量: " + healthDataList.size());
                } else {
                    healthDataList = healthDataRepository.findByUserIdAndRecordDateBetween(userId, start, end);
                    log.info("从数据库获取所有类型健康数据，数量: " + healthDataList.size());
                }
            } else {
                if (type != null) {
                    healthDataList = healthDataRepository.findByUserIdAndTypeOrderByRecordDateDesc(userId, type);
                    log.info("从数据库获取指定类型健康数据，数量: " + healthDataList.size());
                } else {
                    healthDataList = healthDataRepository.findByUserIdOrderByRecordDateDesc(userId);
                    log.info("从数据库获取所有类型健康数据，数量: " + healthDataList.size());
                }
            }
        } catch (Exception e) {
            // If any error occurs, log it and use the empty list
            log.info("数据库查询错误: " + e.getMessage());
        }
        
        List<HealthDataResponse> responseList = healthDataList.stream().map(this::convertToResponse).collect(Collectors.toList());
        log.info("转换后健康数据数量: " + responseList.size());
        
        // Cache the result
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            redisUtils.set(cacheKey, objectMapper.writeValueAsString(responseList), CACHE_EXPIRATION, TimeUnit.MINUTES);
            log.info("缓存健康数据，键: " + cacheKey);
        } catch (Exception e) {
            // Ignore caching error
            log.info("缓存写入错误: " + e.getMessage());
        }
        
        return responseList;
    }

    @Override
    public List<HealthDataResponse> getHealthDataTrend(String type, String period) {
        Long userId = getCurrentUserId();
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
        Long userId = getCurrentUserId();
        if (!healthData.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此数据");
        }
        healthDataRepository.delete(healthData);
        
        // Clear cache
        clearHealthDataCache(userId, healthData.getType());
    }

    private void clearHealthDataCache(Long userId, String type) {
        // 清除指定类型的缓存
        String typeKey = (type != null) ? type : "all";
        String cacheKey = HEALTH_DATA_CACHE_PREFIX + userId + ":" + typeKey + ":all:all";
        try {
            redisUtils.delete(cacheKey);
            log.info("清除缓存，键: " + cacheKey);
        } catch (Exception e) {
            log.info("清除缓存错误: " + e.getMessage());
        }
        
        // 清除所有类型的缓存
        String allCacheKey = HEALTH_DATA_CACHE_PREFIX + userId + ":all:all:all";
        try {
            redisUtils.delete(allCacheKey);
            log.info("清除缓存，键: " + allCacheKey);
        } catch (Exception e) {
            log.info("清除缓存错误: " + e.getMessage());
        }
    }

    private HealthDataResponse convertToResponse(HealthData healthData) {
        HealthDataResponse response = new HealthDataResponse();
        BeanUtils.copyProperties(healthData, response);
        response.setValue(healthData.getDataValue());
        response.setRecordDate(healthData.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setCreatedAt(healthData.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }

    private Long getCurrentUserId() {
        Long userId = jwtUtils.getCurrentUserId();
        if (userId == null) {
            // 为了测试，返回默认用户ID 1
            return 1L;
        }
        return userId;
    }
}
