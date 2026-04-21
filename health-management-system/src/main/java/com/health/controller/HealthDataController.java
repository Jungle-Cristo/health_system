package com.health.controller;

import com.health.common.Result;
import com.health.dto.HealthDataRequest;
import com.health.dto.HealthDataResponse;
import com.health.service.HealthDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health")
@Tag(name = "健康数据管理", description = "健康数据相关接口")
public class HealthDataController {

    @Autowired
    private HealthDataService healthDataService;

    @Operation(summary = "添加健康数据")
    @PostMapping("/data")
    public Result<HealthDataResponse> addHealthData(@RequestBody HealthDataRequest request) {
        return Result.success(healthDataService.addHealthData(request));
    }

    @Operation(summary = "获取用户健康数据列表")
    @GetMapping("/data")
    public Result<List<HealthDataResponse>> getHealthDataList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(healthDataService.getHealthDataList(type, startDate, endDate));
    }

    @Operation(summary = "获取健康数据趋势")
    @GetMapping("/trend")
    public Result<List<HealthDataResponse>> getHealthDataTrend(
            @RequestParam String type,
            @RequestParam String period) {
        return Result.success(healthDataService.getHealthDataTrend(type, period));
    }

    @Operation(summary = "删除健康数据")
    @DeleteMapping("/data/{id}")
    public Result<Void> deleteHealthData(@PathVariable Long id) {
        healthDataService.deleteHealthData(id);
        return Result.success();
    }
}
