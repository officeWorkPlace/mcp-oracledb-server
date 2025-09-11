package com.deepai.mcpserver.vservice;

import com.deepai.mcpserver.model.DataAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataAnalysisService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Cacheable(value = "tableAnalysis", key = "#tableName")
    public DataAnalysis analyzeTable(String tableName) {
        try {
            Map<String, String> columnTypes = getColumnTypes(tableName);
            List<Map<String, Object>> sampleData = getSampleData(tableName);
            
            List<String> numericColumns = new ArrayList<>();
            List<String> categoricalColumns = new ArrayList<>();
            List<String> dateColumns = new ArrayList<>();
            
            columnTypes.forEach((column, type) -> {
                if (isNumericType(type)) {
                    numericColumns.add(column);
                } else if (isDateType(type)) {
                    dateColumns.add(column);
                } else {
                    categoricalColumns.add(column);
                }
            });
            
            Map<String, Object> statistics = generateStatistics(sampleData, numericColumns);
            List<String> suggestedChartTypes = suggestChartTypes(numericColumns, categoricalColumns, dateColumns);
            
            return DataAnalysis.builder()
                .data(sampleData)
                .columnTypes(columnTypes)
                .numericColumns(numericColumns)
                .categoricalColumns(categoricalColumns)
                .dateColumns(dateColumns)
                .statistics(statistics)
                .suggestedChartTypes(suggestedChartTypes)
                .build();
                
        } catch (Exception e) {
            log.error("Error analyzing table: {}", tableName, e);
            throw new RuntimeException("Failed to analyze table: " + e.getMessage(), e);
        }
    }
    
    private Map<String, String> getColumnTypes(String tableName) {
        String sql = """
            SELECT COLUMN_NAME, DATA_TYPE 
            FROM ALL_TAB_COLUMNS 
            WHERE TABLE_NAME = ? 
            ORDER BY COLUMN_ID
            """;
        
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName.toUpperCase());
            return columns.stream().collect(
                Collectors.toMap(
                    row -> (String) row.get("COLUMN_NAME"),
                    row -> (String) row.get("DATA_TYPE")
                )
            );
        } catch (Exception e) {
            log.warn("Could not get column metadata for {}, using sample data", tableName);
            return getColumnTypesFromSample(tableName);
        }
    }
    
    private Map<String, String> getColumnTypesFromSample(String tableName) {
        try {
            String sql = "SELECT * FROM " + tableName + " FETCH FIRST 1 ROWS ONLY";
            List<Map<String, Object>> sample = jdbcTemplate.queryForList(sql);
            
            if (sample.isEmpty()) {
                return new HashMap<>();
            }
            
            Map<String, Object> firstRow = sample.get(0);
            Map<String, String> types = new HashMap<>();
            
            firstRow.forEach((column, value) -> {
                if (value != null) {
                    types.put(column, determineTypeFromValue(value));
                } else {
                    types.put(column, "VARCHAR2");
                }
            });
            
            return types;
        } catch (Exception e) {
            log.error("Error getting column types from sample for table: {}", tableName, e);
            return new HashMap<>();
        }
    }
    
    private String determineTypeFromValue(Object value) {
        if (value instanceof Number) {
            return "NUMBER";
        } else if (value instanceof Date || value instanceof Timestamp) {
            return "DATE";
        } else {
            return "VARCHAR2";
        }
    }
    
    private List<Map<String, Object>> getSampleData(String tableName) {
        try {
            String sql = "SELECT * FROM " + tableName + " FETCH FIRST 100 ROWS ONLY";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error getting sample data for table: {}", tableName, e);
            return new ArrayList<>();
        }
    }
    
    private boolean isNumericType(String type) {
        return type.contains("NUMBER") || type.contains("DECIMAL") || 
               type.contains("FLOAT") || type.contains("INTEGER");
    }
    
    private boolean isDateType(String type) {
        return type.contains("DATE") || type.contains("TIMESTAMP");
    }
    
    private Map<String, Object> generateStatistics(List<Map<String, Object>> data, List<String> numericColumns) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rowCount", data.size());
        
        for (String column : numericColumns) {
            List<Number> values = data.stream()
                .map(row -> (Number) row.get(column))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (!values.isEmpty()) {
                OptionalDouble avg = values.stream().mapToDouble(Number::doubleValue).average();
                OptionalDouble min = values.stream().mapToDouble(Number::doubleValue).min();
                OptionalDouble max = values.stream().mapToDouble(Number::doubleValue).max();
                
                Map<String, Object> columnStats = new HashMap<>();
                if (avg.isPresent()) columnStats.put("average", avg.getAsDouble());
                if (min.isPresent()) columnStats.put("min", min.getAsDouble());
                if (max.isPresent()) columnStats.put("max", max.getAsDouble());
                
                stats.put(column, columnStats);
            }
        }
        
        return stats;
    }
    
    private List<String> suggestChartTypes(List<String> numericColumns, List<String> categoricalColumns, List<String> dateColumns) {
        List<String> suggestions = new ArrayList<>();
        
        if (!categoricalColumns.isEmpty() && !numericColumns.isEmpty()) {
            suggestions.add("bar");
            suggestions.add("pie");
        }
        
        if (numericColumns.size() >= 2) {
            suggestions.add("scatter");
        }
        
        if (!dateColumns.isEmpty() && !numericColumns.isEmpty()) {
            suggestions.add("line");
            suggestions.add("area");
        }
        
        if (categoricalColumns.size() >= 2) {
            suggestions.add("heatmap");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("bar");
        }
        
        return suggestions;
    }
}
