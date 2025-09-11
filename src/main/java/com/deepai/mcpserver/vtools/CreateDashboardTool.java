package com.deepai.mcpserver.vtools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class CreateDashboardTool {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Autowired
    private VisualizationProperties visualizationProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Create dashboard with multiple charts using enhanced configuration
     */
    public String createDashboard(String dashboardName, String[] tableNames, 
                                 String[] chartTypes, String schemaName) {
        try {
            log.info("Creating dashboard: {} with {} tables", dashboardName, tableNames.length);
            
            // Validate input
            if (tableNames == null || tableNames.length == 0) {
                return createErrorResponse("No tables provided for dashboard creation");
            }
            
            if (chartTypes == null || chartTypes.length == 0) {
                chartTypes = new String[tableNames.length];
                Arrays.fill(chartTypes, "auto"); // Use auto-detection
            }
            
            // Security check for all tables
            List<String> allowedTables = new ArrayList<>();
            for (String tableName : tableNames) {
                String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
                if (isTableAllowed(fullTableName)) {
                    allowedTables.add(fullTableName);
                } else {
                    log.warn("Table {} not allowed for dashboard creation", fullTableName);
                }
            }
            
            if (allowedTables.isEmpty()) {
                return createErrorResponse("No allowed tables found for dashboard creation");
            }
            
            // Create dashboard using enhanced service
            List<String> allowedChartTypes = Arrays.asList(chartTypes).subList(0, Math.min(chartTypes.length, allowedTables.size()));
            Map<String, Object> dashboard = visualizationService.createDashboard(
                dashboardName, allowedTables, allowedChartTypes, schemaName);
            
            // Add enhanced metadata
            Map<String, Object> enhancedDashboard = new HashMap<>(dashboard);
            enhancedDashboard.put("configuration", getDashboardConfiguration());
            enhancedDashboard.put("securityInfo", Map.of(
                "tablesRequested", tableNames.length,
                "tablesAllowed", allowedTables.size(),
                "securityEnabled", visualizationProperties.getSecurity().isSqlInjectionProtection()
            ));
            enhancedDashboard.put("toolInfo", Map.of(
                "toolName", "CreateDashboardTool",
                "version", "2.0",
                "enhanced", true
            ));
            
            return createSuccessResponse(enhancedDashboard);
            
        } catch (SecurityException e) {
            log.warn("Security violation in CreateDashboardTool: {}", e.getMessage());
            return createErrorResponse("Security: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating dashboard: {}", dashboardName, e);
            return createErrorResponse("Failed to create dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Create a performance dashboard with predefined charts
     */
    public String createPerformanceDashboard(String schemaName) {
        try {
            log.info("Creating performance dashboard for schema: {}", schemaName);
            
            // Define performance-related tables and chart types
            String[] performanceTables = {
                "V$SESSION", "V$SQL", "V$SYSSTAT", "V$SYSTEM_EVENT"
            };
            String[] chartTypes = {"bar", "line", "pie", "scatter"};
            
            return createDashboard("Performance Dashboard", performanceTables, chartTypes, schemaName);
            
        } catch (Exception e) {
            log.error("Error creating performance dashboard", e);
            return createErrorResponse("Failed to create performance dashboard: " + e.getMessage());
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
    
    private Map<String, Object> getDashboardConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxChartsPerDashboard", 10); // Configurable limit
        config.put("framework", visualizationProperties.getDefaultFramework());
        config.put("responsive", visualizationProperties.getChart().isResponsive());
        config.put("chartDimensions", Map.of(
            "width", visualizationProperties.getChart().getDefault().getWidth(),
            "height", visualizationProperties.getChart().getDefault().getHeight()
        ));
        config.put("animation", visualizationProperties.getChart().getAnimation().isEnabled());
        config.put("maxDataPointsPerChart", visualizationProperties.getMaxDataPoints());
        config.put("cacheEnabled", visualizationProperties.getCache().isEnabled());
        return config;
    }
    
    private String createSuccessResponse(Map<String, Object> data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dashboard", data);
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
            error.put("tool", "CreateDashboardTool");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}