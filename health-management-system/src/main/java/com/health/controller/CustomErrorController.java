package com.health.controller;

import com.health.common.ErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom error controller to handle all error responses
 * Returns JSON response instead of Whitelabel Error Page
 */
@RestController
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Get status code from request attribute
        Object statusCodeObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = statusCodeObj != null ? Integer.parseInt(statusCodeObj.toString()) : 500;
        
        HttpStatus status = HttpStatus.valueOf(statusCode);
        
        // Get error message from request attribute
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        // Build response based on status code
        switch (status) {
            case NOT_FOUND:
                response.put("code", ErrorCode.NOT_FOUND.getCode());
                response.put("message", errorMessage != null ? errorMessage.toString() : ErrorCode.NOT_FOUND.getMessage());
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                
            case UNAUTHORIZED:
                response.put("code", ErrorCode.UNAUTHORIZED.getCode());
                response.put("message", errorMessage != null ? errorMessage.toString() : ErrorCode.UNAUTHORIZED.getMessage());
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                
            case FORBIDDEN:
                response.put("code", ErrorCode.FORBIDDEN.getCode());
                response.put("message", errorMessage != null ? errorMessage.toString() : ErrorCode.FORBIDDEN.getMessage());
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                
            case BAD_REQUEST:
                response.put("code", ErrorCode.BAD_REQUEST.getCode());
                response.put("message", errorMessage != null ? errorMessage.toString() : ErrorCode.BAD_REQUEST.getMessage());
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                
            default:
                response.put("code", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
                response.put("message", errorMessage != null ? errorMessage.toString() : ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
