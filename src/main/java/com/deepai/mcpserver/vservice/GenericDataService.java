package com.deepai.mcpserver.vservice;

import com.deepai.mcpserver.model.VisualizationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenericDataService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Cacheable(value = "chartData", key = "#request.hashCode()")
    public List<Map<String, Object>> fetchData(VisualizationRequest request) {
        String sql = buildQuery(request);
        log.debug("Executing query: {}", sql);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            return sanitizeResults(results);
        } catch (Exception e) {
            log.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to fetch data: " + e.getMessage(), e);
        }
    }
    
    private String buildQuery(VisualizationRequest request) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        if (request.getColumns() != null && !request.getColumns().isEmpty()) {
            if (request.getAggregationType() != null) {
                sql.append(buildAggregatedSelect(request));
            } else {
                sql.append(request.getColumns().stream()
                    .map(this::sanitizeColumnName)
                    .collect(Collectors.joining(", ")));
            }
        } else {
            sql.append("*");
        }
        
        sql.append(" FROM ").append(sanitizeTableName(request.getTableName()));
        
        if (request.getWhereClause() != null && !request.getWhereClause().trim().isEmpty()) {
            sql.append(" WHERE ").append(sanitizeWhereClause(request.getWhereClause()));
        }
        
        if (request.getGroupBy() != null && !request.getGroupBy().trim().isEmpty()) {
            sql.append(" GROUP BY ").append(sanitizeColumnName(request.getGroupBy()));
        } else if (request.getAggregationType() != null && request.getXColumn() != null) {
            sql.append(" GROUP BY ").append(sanitizeColumnName(request.getXColumn()));
        }
        
        if (request.getOrderBy() != null && !request.getOrderBy().trim().isEmpty()) {
            sql.append(" ORDER BY ").append(sanitizeOrderBy(request.getOrderBy()));
        }
        
        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 1000;
        sql.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
        
        return sql.toString();
    }
    
    private String buildAggregatedSelect(VisualizationRequest request) {
        StringBuilder select = new StringBuilder();
        
        if (request.getXColumn() != null) {
            select.append(sanitizeColumnName(request.getXColumn()));
        }
        
        if (request.getYColumn() != null) {
            if (select.length() > 0) select.append(", ");
            String aggType = request.getAggregationType().toUpperCase();
            select.append(aggType).append("(").append(sanitizeColumnName(request.getYColumn())).append(") as ").append(request.getYColumn());
        }
        
        if ("combo".equals(request.getChartType())) {
            if (select.length() > 0) select.append(", ");
            select.append("COUNT(*) as record_count");
        }
        
        return select.toString();
    }
    
    private String sanitizeColumnName(String columnName) {
        if (columnName == null) return "";
        return columnName.replaceAll("[^a-zA-Z0-9_#$]", "").toUpperCase();
    }
    
    private String sanitizeTableName(String tableName) {
        if (tableName == null) return "";
        return tableName.replaceAll("[^a-zA-Z0-9_#$.]", "").toUpperCase();
    }
    
    /**
     * Sanitizes WHERE clause by removing dangerous SQL verbs.
     * 
     * WARNING: This is a basic protection only. For production use:
     * 1. Use prepared statements with parameterized queries
     * 2. Whitelist allowed columns, operators, and functions
     * 3. Consider disabling whereClause entirely unless in trusted context
     * 4. Implement proper SQL injection prevention
     */
    private String sanitizeWhereClause(String whereClause) {
        if (whereClause == null) return "";
        
        // Remove dangerous SQL verbs (basic protection)
        String sanitized = whereClause.replaceAll("(?i)(drop|delete|truncate|insert|update|create|alter|exec|execute|grant|revoke)", "");
        
        // Log potential issues for monitoring
        if (!sanitized.equals(whereClause)) {
            log.warn("WHERE clause was sanitized - removed dangerous SQL verbs");
        }
        
        return sanitized;
    }
    
    private String sanitizeOrderBy(String orderBy) {
        if (orderBy == null) return "";
        return orderBy.replaceAll("[^a-zA-Z0-9_#$, ]", "").toUpperCase();
    }
    
    private List<Map<String, Object>> sanitizeResults(List<Map<String, Object>> results) {
        return results.stream().map(row -> {
            Map<String, Object> sanitizedRow = new HashMap<>();
            row.forEach((key, value) -> {
                if (value != null) {
                    sanitizedRow.put(key, value);
                }
            });
            return sanitizedRow;
        }).collect(Collectors.toList());
    }
}
