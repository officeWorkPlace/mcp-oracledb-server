package com.deepai.mcpserver.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalVisualizationExceptionHandler {
    
    @ExceptionHandler(VisualizationException.class)
    public ResponseEntity<Map<String, Object>> handleVisualizationException(VisualizationException e) {
        log.error("Visualization error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "errorCode", e.getErrorCode(),
            "message", e.getMessage(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException e) {
        log.error("SQL error in visualization: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "errorCode", "SQL_ERROR",
            "message", "Database query failed: " + e.getMessage(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument in visualization: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "errorCode", "INVALID_ARGUMENT",
            "message", e.getMessage(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected error in visualization: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "status", "error",
            "errorCode", "INTERNAL_ERROR",
            "message", "An unexpected error occurred",
            "timestamp", System.currentTimeMillis()
        ));
    }
}