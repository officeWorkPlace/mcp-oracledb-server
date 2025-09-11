package com.deepai.mcpserver.vtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.model.DataAnalysis;
import com.deepai.mcpserver.vservice.DataAnalysisService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AnalyzeTableTool {
    
    @Autowired
    private DataAnalysisService analysisService;
    
    @Tool(description = "Analyze Oracle table structure and suggest optimal visualization strategies. Returns column types, statistics, and chart recommendations.")
    public Map<String, Object> analyzeTableForVisualization(
            @ToolParam(description = "Oracle table name to analyze", required = true) 
            String tableName,
            
            @ToolParam(description = "Include sample data in analysis", required = false) 
            Boolean includeSampleData) {
        
        try {
            log.info("Analyzing table: {}", tableName);
            
            DataAnalysis analysis = analysisService.analyzeTable(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("tableName", tableName);
            result.put("analysis", Map.of(
                "totalColumns", analysis.getColumnTypes().size(),
                "columnTypes", analysis.getColumnTypes(),
                "numericColumns", analysis.getNumericColumns(),
                "categoricalColumns", analysis.getCategoricalColumns(),
                "dateColumns", analysis.getDateColumns(),
                "statistics", analysis.getStatistics(),
                "suggestedChartTypes", analysis.getSuggestedChartTypes()
            ));
            
            // Add sample data if requested
            if (Boolean.TRUE.equals(includeSampleData) && analysis.getData() != null) {
                result.put("sampleData", analysis.getData().stream().limit(5).toArray());
            }
            
            // Add visualization recommendations
            result.put("recommendations", generateVisualizationRecommendations(analysis));
            
            return result;
            
        } catch (Exception e) {
            log.error("Error analyzing table", e);
            return Map.of(
                "status", "error",
                "message", "Failed to analyze table: " + e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            );
        }
    }
    
    private List<Map<String, Object>> generateVisualizationRecommendations(DataAnalysis analysis) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Bar chart recommendation
        if (!analysis.getCategoricalColumns().isEmpty() && !analysis.getNumericColumns().isEmpty()) {
            recommendations.add(Map.of(
                "chartType", "bar",
                "description", "Bar chart showing " + analysis.getNumericColumns().get(0) + " by " + analysis.getCategoricalColumns().get(0),
                "xColumn", analysis.getCategoricalColumns().get(0),
                "yColumn", analysis.getNumericColumns().get(0),
                "confidence", "high"
            ));
        }
        
        // Line chart recommendation
        if (!analysis.getDateColumns().isEmpty() && !analysis.getNumericColumns().isEmpty()) {
            recommendations.add(Map.of(
                "chartType", "line",
                "description", "Time series showing " + analysis.getNumericColumns().get(0) + " over " + analysis.getDateColumns().get(0),
                "xColumn", analysis.getDateColumns().get(0),
                "yColumn", analysis.getNumericColumns().get(0),
                "confidence", "high"
            ));
        }
        
        // Scatter plot recommendation
        if (analysis.getNumericColumns().size() >= 2) {
            recommendations.add(Map.of(
                "chartType", "scatter",
                "description", "Scatter plot showing correlation between " + analysis.getNumericColumns().get(0) + " and " + analysis.getNumericColumns().get(1),
                "xColumn", analysis.getNumericColumns().get(0),
                "yColumn", analysis.getNumericColumns().get(1),
                "confidence", "medium"
            ));
        }
        
        // Heatmap recommendation
        if (analysis.getCategoricalColumns().size() >= 2) {
            recommendations.add(Map.of(
                "chartType", "heatmap",
                "description", "Heatmap showing relationship between " + analysis.getCategoricalColumns().get(0) + " and " + analysis.getCategoricalColumns().get(1),
                "xColumn", analysis.getCategoricalColumns().get(0),
                "yColumn", analysis.getCategoricalColumns().get(1),
                "confidence", "medium"
            ));
        }
        
        return recommendations;
    }
}
