package com.health.exception;

import com.health.common.Result;
import com.health.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return Result.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation Exception: {}", message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data Integrity Violation: {}", e.getMessage());
        String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        String lower = msg == null ? "" : msg.toLowerCase();
        if (lower.contains("users") && (lower.contains("email") || lower.contains("uk_"))) {
            return Result.error(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (lower.contains("users") && lower.contains("username")) {
            return Result.error(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (lower.contains("users") && lower.contains("phone")) {
            return Result.error(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "Duplicate or invalid data");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
