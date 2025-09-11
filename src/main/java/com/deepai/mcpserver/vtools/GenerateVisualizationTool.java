package com.deepai.mcpserver.vtools;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.VisualizationRequest;
import com.deepai.mcpserver.vservice.VisualizationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true")
public class GenerateVisualizationTool {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Autowired
    private VisualizationProperties visualizationProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Generate visualization with enhanced configuration and security
     */
    public String generateVisualization(String tableName, String chartType, 
                                       String xColumn, String yColumn, 
                                       String schemaName, String framework) {
        try {
            log.info("Generating {} visualization for table: {}", chartType, tableName);
            
            String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
            
            // Security check
            if (!isTableAllowed(fullTableName)) {
                return createErrorResponse("Access to table " + fullTableName + " is not allowed");
            }
            
            // Build visualization request with enhanced configuration
            VisualizationRequest request = VisualizationRequest.builder()
                .tableName(fullTableName)
                .chartType(chartType != null ? chartType : "bar")
                .xColumn(xColumn)
                .yColumn(yColumn)
                .framework(framework != null ? framework : visualizationProperties.getDefaultFramework())
                .limit(visualizationProperties.getMaxDataPoints())
                .build();
            
            // Generate visualization using enhanced service
            ChartSpecification chartSpec = visualizationService.generateVisualization(request);
            
            // Create enhanced response
            Map<String, Object> enhancedResponse = new HashMap<>();
            enhancedResponse.put("specification", chartSpec);
            enhancedResponse.put("configuration", getVisualizationConfiguration());
            enhancedResponse.put("request", Map.of(
                "tableName", fullTableName,
                "chartType", request.getChartType(),
                "framework", request.getFramework(),
                "xColumn", request.getXColumn(),
                "yColumn", request.getYColumn()
            ));
            enhancedResponse.put("toolInfo", Map.of(
                "toolName", "GenerateVisualizationTool",
                "version", "2.0",
                "enhanced", true
            ));
            
            return createSuccessResponse(enhancedResponse);
            
        } catch (SecurityException e) {
            log.warn("Security violation in GenerateVisualizationTool: {}", e.getMessage());
            return createErrorResponse("Security: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating visualization for table: {}", tableName, e);
            return createErrorResponse("Failed to generate visualization: " + e.getMessage());
        }
    }
    
    /**
     * Generate multiple visualizations for comparison
     */
    public String generateComparisonVisualizations(String[] tableNames, String chartType, 
                                                  String schemaName, String framework) {
        try {
            log.info("Generating comparison visualizations for {} tables", tableNames.length);
            
            Map<String, Object> comparisons = new HashMap<>();
            
            for (String tableName : tableNames) {
                String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
                
                if (isTableAllowed(fullTableName)) {
                    try {
                        ChartSpecification chart = visualizationService.generateSmartVisualization(fullTableName, null, null);
                        comparisons.put(tableName, chart);
                    } catch (Exception e) {
                        log.warn("Failed to generate chart for table {}: {}", fullTableName, e.getMessage());
                        comparisons.put(tableName, Map.of("error", e.getMessage()));
                    }
                } else {
                    log.warn("Table {} not allowed for comparison", fullTableName);
                    comparisons.put(tableName, Map.of("error", "Access not allowed"));
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("comparisons", comparisons);
            response.put("configuration", getVisualizationConfiguration());
            response.put("summary", Map.of(
                "totalTables", tableNames.length,
                "successfulCharts", comparisons.size(),
                "chartType", chartType,
                "framework", framework != null ? framework : visualizationProperties.getDefaultFramework()
            ));
            
            return createSuccessResponse(response);
            
        } catch (Exception e) {
            log.error("Error generating comparison visualizations", e);
            return createErrorResponse("Failed to generate comparison visualizations: " + e.getMessage());
        }
    }
    
    private boolean isTableAllowed(String tableName) {
        if (!visualizationProperties.getSecurity().isSqlInjectionProtection()) {
            return true;
        }
        
        Pattern allowedPattern = Pattern.compile(visualizationProperties.getSecurity().getAllowedTablesPattern());
        Pattern blockedPattern = Pattern.compile(visualizationProperties.getSecurity().getBlockedTablesPattern());
        
        return allowedPattern.matcher(tableName.toUpperCase()).matches() && 
               !blockedPattern.matcher(tableName.toUpperCase()).matches();
    }
    
    private Map<String, Object> getVisualizationConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("framework", visualizationProperties.getDefaultFramework());
        config.put("maxDataPoints", visualizationProperties.getMaxDataPoints());
        config.put("autoDetectColumns", visualizationProperties.isAutoDetectColumns());
        config.put("chartSettings", Map.of(
            "width", visualizationProperties.getChart().getDefault().getWidth(),
            "height", visualizationProperties.getChart().getDefault().getHeight(),
            "responsive", visualizationProperties.getChart().isResponsive(),
            "animation", visualizationProperties.getChart().getAnimation().isEnabled()
        ));
        config.put("performance", Map.of(
            "queryTimeout", visualizationProperties.getPerformance().getQueryTimeout(),
            "fetchSize", visualizationProperties.getPerformance().getFetchSize(),
            "maxConcurrentRequests", visualizationProperties.getPerformance().getMaxConcurrentRequests()
        ));
        config.put("caching", Map.of(
            "enabled", visualizationProperties.getCache().isEnabled(),
            "ttl", visualizationProperties.getCache().getTtl()
        ));
        return config;
    }
    
    private String createSuccessResponse(Map<String, Object> data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", data);
            response.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error creating success response", e);
            return createErrorResponse("Failed to create response");
        }
    }
    
    private String createErrorResponse(String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", message);
            error.put("timestamp", System.currentTimeMillis());
            error.put("tool", "GenerateVisualizationTool");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}