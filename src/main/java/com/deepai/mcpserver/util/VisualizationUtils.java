package com.deepai.mcpserver.util;

import java.util.*;
import java.util.stream.Collectors;

public class VisualizationUtils {
    
    /**
     * Generate color palette for charts
     */
    public static List<String> generateColorPalette(int count) {
        List<String> colors = Arrays.asList(
            "#3498db", "#e74c3c", "#2ecc71", "#f39c12", "#9b59b6",
            "#1abc9c", "#34495e", "#e67e22", "#95a5a6", "#c0392b"
        );
        
        if (count <= colors.size()) {
            return colors.subList(0, count);
        }
        
        // Generate additional colors if needed
        List<String> extendedColors = new ArrayList<>(colors);
        for (int i = colors.size(); i < count; i++) {
            extendedColors.add(generateRandomColor());
        }
        return extendedColors;
    }
    
    /**
     * Generate random hex color
     */
    private static String generateRandomColor() {
        Random random = new Random();
        return String.format("#%06x", random.nextInt(0xFFFFFF));
    }
    
    /**
     * Sanitize column name for SQL
     */
    public static String sanitizeColumnName(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        return columnName.replaceAll("[^a-zA-Z0-9_#$]", "").toUpperCase();
    }
    
    /**
     * Sanitize table name for SQL
     */
    public static String sanitizeTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        return tableName.replaceAll("[^a-zA-Z0-9_#$.]", "").toUpperCase();
    }
    
    /**
     * Check if column contains numeric data
     */
    public static boolean isNumericColumn(List<Map<String, Object>> data, String columnName) {
        if (data.isEmpty() || columnName == null) return false;
        
        for (Map<String, Object> row : data) {
            Object value = row.get(columnName);
            if (value != null && !(value instanceof Number)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if column contains date data
     */
    public static boolean isDateColumn(List<Map<String, Object>> data, String columnName) {
        if (data.isEmpty() || columnName == null) return false;
        
        Object value = data.get(0).get(columnName);
        return value instanceof java.util.Date || 
               value instanceof java.sql.Date || 
               value instanceof java.sql.Timestamp;
    }
    
    /**
     * Aggregate data by column
     */
    public static List<Map<String, Object>> aggregateData(
            List<Map<String, Object>> data, 
            String groupByColumn, 
            String valueColumn, 
            String aggregationType) {
        
        Map<Object, List<Map<String, Object>>> grouped = data.stream()
            .collect(Collectors.groupingBy(row -> row.get(groupByColumn)));
        
        return grouped.entrySet().stream().map(entry -> {
            Object groupKey = entry.getKey();
            List<Map<String, Object>> groupData = entry.getValue();
            
            Map<String, Object> result = new HashMap<>();
            result.put(groupByColumn, groupKey);
            
            switch (aggregationType.toUpperCase()) {
                case "COUNT":
                    result.put(valueColumn, groupData.size());
                    break;
                case "SUM":
                    double sum = groupData.stream()
                        .mapToDouble(row -> ((Number) row.get(valueColumn)).doubleValue())
                        .sum();
                    result.put(valueColumn, sum);
                    break;
                case "AVG":
                    double avg = groupData.stream()
                        .mapToDouble(row -> ((Number) row.get(valueColumn)).doubleValue())
                        .average().orElse(0.0);
                    result.put(valueColumn, avg);
                    break;
                case "MIN":
                    double min = groupData.stream()
                        .mapToDouble(row -> ((Number) row.get(valueColumn)).doubleValue())
                        .min().orElse(0.0);
                    result.put(valueColumn, min);
                    break;
                case "MAX":
                    double max = groupData.stream()
                        .mapToDouble(row -> ((Number) row.get(valueColumn)).doubleValue())
                        .max().orElse(0.0);
                    result.put(valueColumn, max);
                    break;
                default:
                    result.put(valueColumn, groupData.size());
            }
            
            return result;
        }).collect(Collectors.toList());
    }
}

