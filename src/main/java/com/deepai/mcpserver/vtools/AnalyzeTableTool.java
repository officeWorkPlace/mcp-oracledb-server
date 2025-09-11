package com.deepai.mcpserver.vtools;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.deepai.mcpserver.vservice.VisualizationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true")
public class AnalyzeTableTool {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Autowired
    private VisualizationProperties visualizationProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Analyze table structure for visualization with enhanced security
     */
    public String analyzeTable(String tableName, String schemaName) {
        try {
            log.info("Analyzing table for visualization: {}", tableName);
            
            String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
            
            // Security check using properties
            if (!isTableAllowed(fullTableName)) {
                return createErrorResponse("Access to table " + fullTableName + " is not allowed");
            }
            
            // Use enhanced visualization service
            Map<String, Object> analysis = visualizationService.analyzeTableForVisualization(fullTableName);
            
            // Add configuration info to response
            Map<String, Object> enhancedAnalysis = new HashMap<>(analysis);
            enhancedAnalysis.put("configuration", getConfigurationInfo());
            enhancedAnalysis.put("securityApplied", visualizationProperties.getSecurity().isSqlInjectionProtection());
            enhancedAnalysis.put("toolInfo", Map.of(
                "toolName", "AnalyzeTableTool",
                "version", "2.0",
                "enhanced", true
            ));
            
            return createSuccessResponse(enhancedAnalysis);
            
        } catch (SecurityException e) {
            log.warn("Security violation in AnalyzeTableTool: {}", e.getMessage());
            return createErrorResponse("Security: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error analyzing table for visualization: {}", tableName, e);
            return createErrorResponse("Failed to analyze table: " + e.getMessage());
        }
    }
    
    /**
     * Security check using visualization properties
     */
    private boolean isTableAllowed(String tableName) {
        if (!visualizationProperties.getSecurity().isSqlInjectionProtection()) {
            return true;
        }
        
        Pattern allowedPattern = Pattern.compile(visualizationProperties.getSecurity().getAllowedTablesPattern());
        Pattern blockedPattern = Pattern.compile(visualizationProperties.getSecurity().getBlockedTablesPattern());
        
        return allowedPattern.matcher(tableName.toUpperCase()).matches() && 
               !blockedPattern.matcher(tableName.toUpperCase()).matches();
    }
    
    private Map<String, Object> getConfigurationInfo() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxDataPoints", visualizationProperties.getMaxDataPoints());
        config.put("framework", visualizationProperties.getDefaultFramework());
        config.put("autoDetectColumns", visualizationProperties.isAutoDetectColumns());
        config.put("chartConfig", Map.of(
            "width", visualizationProperties.getChart().getDefault().getWidth(),
            "height", visualizationProperties.getChart().getDefault().getHeight(),
            "responsive", visualizationProperties.getChart().isResponsive(),
            "animation", visualizationProperties.getChart().getAnimation().isEnabled()
        ));
        config.put("cacheEnabled", visualizationProperties.getCache().isEnabled());
        config.put("cacheTtl", visualizationProperties.getCache().getTtl());
        return config;
    }
    
    private String createSuccessResponse(Map<String, Object> data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
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
            error.put("tool", "AnalyzeTableTool");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}
