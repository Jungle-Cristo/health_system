package com.health.repository;

import com.health.entity.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

    List<HealthData> findByUserIdAndTypeOrderByRecordDateDesc(Long userId, String type);

    List<HealthData> findByUserIdAndTypeAndRecordDateBetween(
            Long userId, String type, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT h FROM HealthData h WHERE h.userId = :userId AND h.type = :type ORDER BY h.recordDate DESC LIMIT :limit")
    List<HealthData> findRecentHealthDataByType(
            @Param("userId") Long userId, 
            @Param("type") String type, 
            @Param("limit") int limit);
    
    List<HealthData> findByUserIdOrderByRecordDateDesc(Long userId);
    
    List<HealthData> findByUserIdAndRecordDateBetween(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
