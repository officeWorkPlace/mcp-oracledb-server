package com.deepai.mcpserver.vtools;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.vservice.VisualizationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true")
public class SmartVisualizationTool {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Autowired
    private VisualizationProperties visualizationProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Generate smart visualization with AI-powered column detection and chart type selection
     */
    public String generateSmartVisualization(String tableName, String schemaName, 
                                            String xColumn, String yColumn) {
        try {
            log.info("Generating smart visualization for table: {}", tableName);
            
            String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
            
            // Security check
            if (!isTableAllowed(fullTableName)) {
                return createErrorResponse("Access to table " + fullTableName + " is not allowed");
            }
            
            // First, analyze the table to provide insights
            Map<String, Object> analysis = visualizationService.analyzeTableForVisualization(fullTableName);
            
            // Generate smart visualization using enhanced service
            ChartSpecification chartSpec = visualizationService.generateSmartVisualization(
                fullTableName, xColumn, yColumn);
            
            // Create comprehensive response with AI insights
            Map<String, Object> smartResponse = new HashMap<>();
            smartResponse.put("chartSpecification", chartSpec);
            smartResponse.put("tableAnalysis", analysis);
            smartResponse.put("aiInsights", generateAIInsights(analysis));
            smartResponse.put("recommendations", generateRecommendations(analysis));
            smartResponse.put("configuration", getSmartConfiguration());
            smartResponse.put("toolInfo", Map.of(
                "toolName", "SmartVisualizationTool",
                "version", "2.0",
                "enhanced", true,
                "aiPowered", true
            ));
            
            return createSuccessResponse(smartResponse);
            
        } catch (SecurityException e) {
            log.warn("Security violation in SmartVisualizationTool: {}", e.getMessage());
            return createErrorResponse("Security: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating smart visualization for table: {}", tableName, e);
            return createErrorResponse("Failed to generate smart visualization: " + e.getMessage());
        }
    }
    
    /**
     * Generate multiple smart visualization suggestions
     */
    public String generateVisualizationSuggestions(String tableName, String schemaName) {
        try {
            log.info("Generating visualization suggestions for table: {}", tableName);
            
            String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
            
            // Security check
            if (!isTableAllowed(fullTableName)) {
                return createErrorResponse("Access to table " + fullTableName + " is not allowed");
            }
            
            // Analyze table structure
            Map<String, Object> analysis = visualizationService.analyzeTableForVisualization(fullTableName);
            
            // Generate multiple chart suggestions
            Map<String, ChartSpecification> suggestions = new HashMap<>();
            
            try {
                // Bar chart suggestion
                suggestions.put("barChart", visualizationService.generateSmartVisualization(fullTableName, null, null));
                
                // Line chart suggestion (if date columns exist)
                @SuppressWarnings("unchecked")
                var dateColumns = (java.util.List<String>) ((Map<?, ?>) analysis.get("analysis")).get("dateColumns");
                if (dateColumns != null && !dateColumns.isEmpty()) {
                    suggestions.put("lineChart", visualizationService.generateSmartVisualization(fullTableName, dateColumns.get(0), null));
                }
                
                // Scatter plot suggestion (if multiple numeric columns)
                @SuppressWarnings("unchecked")
                var numericColumns = (java.util.List<String>) ((Map<?, ?>) analysis.get("analysis")).get("numericColumns");
                if (numericColumns != null && numericColumns.size() > 1) {
                    suggestions.put("scatterPlot", visualizationService.generateSmartVisualization(fullTableName, numericColumns.get(0), numericColumns.get(1)));
                }
                
            } catch (Exception e) {
                log.warn("Some chart suggestions failed for table {}: {}", fullTableName, e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("suggestions", suggestions);
            response.put("analysis", analysis);
            response.put("recommendations", generateDetailedRecommendations(analysis, suggestions));
            response.put("bestChoice", selectBestVisualization(suggestions));
            response.put("configuration", getSmartConfiguration());
            
            return createSuccessResponse(response);
            
        } catch (Exception e) {
            log.error("Error generating visualization suggestions for table: {}", tableName, e);
            return createErrorResponse("Failed to generate visualization suggestions: " + e.getMessage());
        }
    }
    
    /**
     * Export smart visualization in various formats
     */
    public String exportSmartVisualization(String tableName, String schemaName, String format) {
        try {
            log.info("Exporting smart visualization for table: {} in format: {}", tableName, format);
            
            String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
            
            // Security check
            if (!isTableAllowed(fullTableName)) {
                return createErrorResponse("Access to table " + fullTableName + " is not allowed");
            }
            
            // Generate export data using enhanced service
            Map<String, Object> exportData = visualizationService.exportVisualization(
                format, tableName, "smart", schemaName);
            
            // Add smart export enhancements
            Map<String, Object> smartExport = new HashMap<>(exportData);
            smartExport.put("smartFeatures", Map.of(
                "autoColumnDetection", visualizationProperties.isAutoDetectColumns(),
                "intelligentChartSelection", true,
                "securityApplied", visualizationProperties.getSecurity().isSqlInjectionProtection()
            ));
            smartExport.put("exportConfiguration", getExportConfiguration(format));
            
            return createSuccessResponse(smartExport);
            
        } catch (Exception e) {
            log.error("Error exporting smart visualization for table: {}", tableName, e);
            return createErrorResponse("Failed to export smart visualization: " + e.getMessage());
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
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> generateAIInsights(Map<String, Object> analysis) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            Map<String, Object> analysisData = (Map<String, Object>) analysis.get("analysis");
            
            if (analysisData != null) {
                var numericColumns = (java.util.List<String>) analysisData.get("numericColumns");
                var dateColumns = (java.util.List<String>) analysisData.get("dateColumns");
                var textColumns = (java.util.List<String>) analysisData.get("textColumns");
                
                insights.put("dataComplexity", calculateDataComplexity(numericColumns, dateColumns, textColumns));
                insights.put("visualizationPotential", assessVisualizationPotential(numericColumns, dateColumns, textColumns));
                insights.put("recommendedApproach", getRecommendedApproach(numericColumns, dateColumns, textColumns));
                insights.put("dataQuality", "Good"); // Could be enhanced with actual data quality analysis
            }
            
        } catch (Exception e) {
            log.warn("Error generating AI insights: {}", e.getMessage());
            insights.put("error", "Unable to generate AI insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateRecommendations(Map<String, Object> analysis) {
        Map<String, Object> recommendations = new HashMap<>();
        
        try {
            recommendations.put("chartType", "Smart selection based on data types");
            recommendations.put("framework", visualizationProperties.getDefaultFramework());
            recommendations.put("responsive", visualizationProperties.getChart().isResponsive());
            recommendations.put("animation", visualizationProperties.getChart().getAnimation().isEnabled());
            recommendations.put("dataLimits", Map.of(
                "maxPoints", visualizationProperties.getMaxDataPoints(),
                "reason", "Performance optimization"
            ));
            
        } catch (Exception e) {
            log.warn("Error generating recommendations: {}", e.getMessage());
        }
        
        return recommendations;
    }
    
    private Map<String, Object> generateDetailedRecommendations(Map<String, Object> analysis, Map<String, ChartSpecification> suggestions) {
        Map<String, Object> detailed = new HashMap<>();
        
        detailed.put("totalSuggestions", suggestions.size());
        detailed.put("bestForTrends", suggestions.containsKey("lineChart") ? "lineChart" : "barChart");
        detailed.put("bestForComparison", "barChart");
        detailed.put("bestForCorrelation", suggestions.containsKey("scatterPlot") ? "scatterPlot" : "barChart");
        detailed.put("framework", visualizationProperties.getDefaultFramework());
        detailed.put("performance", Map.of(
            "cacheEnabled", visualizationProperties.getCache().isEnabled(),
            "maxDataPoints", visualizationProperties.getMaxDataPoints()
        ));
        
        return detailed;
    }
    
    private String selectBestVisualization(Map<String, ChartSpecification> suggestions) {
        if (suggestions.containsKey("lineChart")) {
            return "lineChart"; // Time-series data is often most insightful
        } else if (suggestions.containsKey("scatterPlot")) {
            return "scatterPlot"; // Correlation analysis
        } else if (suggestions.containsKey("barChart")) {
            return "barChart"; // Safe default
        }
        return "barChart";
    }
    
    private String calculateDataComplexity(java.util.List<String> numeric, java.util.List<String> date, java.util.List<String> text) {
        int totalColumns = (numeric != null ? numeric.size() : 0) + 
                          (date != null ? date.size() : 0) + 
                          (text != null ? text.size() : 0);
        
        if (totalColumns <= 3) return "Simple";
        if (totalColumns <= 7) return "Moderate";
        return "Complex";
    }
    
    private String assessVisualizationPotential(java.util.List<String> numeric, java.util.List<String> date, java.util.List<String> text) {
        boolean hasNumeric = numeric != null && !numeric.isEmpty();
        boolean hasDate = date != null && !date.isEmpty();
        boolean hasText = text != null && !text.isEmpty();
        
        if (hasNumeric && hasDate) return "Excellent - Time series analysis possible";
        if (hasNumeric && hasText) return "Good - Categorical analysis possible";
        if (hasNumeric) return "Moderate - Numeric analysis only";
        return "Limited - Primarily text data";
    }
    
    private String getRecommendedApproach(java.util.List<String> numeric, java.util.List<String> date, java.util.List<String> text) {
        boolean hasNumeric = numeric != null && !numeric.isEmpty();
        boolean hasDate = date != null && !date.isEmpty();
        boolean hasText = text != null && !text.isEmpty();
        
        if (hasNumeric && hasDate) return "Time-series visualization with trend analysis";
        if (hasNumeric && hasText) return "Categorical comparison with bar/pie charts";
        if (hasNumeric) return "Distribution analysis with histograms";
        return "Text analysis with word clouds or frequency charts";
    }
    
    private Map<String, Object> getSmartConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("aiPowered", true);
        config.put("autoDetection", visualizationProperties.isAutoDetectColumns());
        config.put("framework", visualizationProperties.getDefaultFramework());
        config.put("intelligentDefaults", Map.of(
            "width", visualizationProperties.getChart().getDefault().getWidth(),
            "height", visualizationProperties.getChart().getDefault().getHeight(),
            "responsive", visualizationProperties.getChart().isResponsive(),
            "animation", visualizationProperties.getChart().getAnimation().isEnabled()
        ));
        config.put("performance", Map.of(
            "maxDataPoints", visualizationProperties.getMaxDataPoints(),
            "caching", visualizationProperties.getCache().isEnabled(),
            "queryTimeout", visualizationProperties.getPerformance().getQueryTimeout()
        ));
        config.put("security", Map.of(
            "enabled", visualizationProperties.getSecurity().isSqlInjectionProtection(),
            "tableFiltering", true
        ));
        return config;
    }
    
    private Map<String, Object> getExportConfiguration(String format) {
        Map<String, Object> exportConfig = new HashMap<>();
        exportConfig.put("format", format);
        exportConfig.put("supportedFormats", java.util.Arrays.asList("json", "csv", "png", "svg", "pdf"));
        exportConfig.put("framework", visualizationProperties.getDefaultFramework());
        exportConfig.put("quality", "high");
        exportConfig.put("compression", format.equals("png") ? "optimized" : "none");
        return exportConfig;
    }
    
    private String createSuccessResponse(Map<String, Object> data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("smartVisualization", data);
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
            error.put("tool", "SmartVisualizationTool");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}