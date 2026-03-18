package com.health.repository;

import com.health.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByTargetAndTypeAndUsedFalseAndExpireTimeAfterOrderByCreatedAtDesc(
            String target, String type, LocalDateTime now);
}
