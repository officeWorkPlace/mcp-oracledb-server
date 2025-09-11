package com.deepai.mcpserver.vtools;


import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.VisualizationRequest;
import com.deepai.mcpserver.vservice.VisualizationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GenerateVisualizationTool {
    
    @Autowired
    private VisualizationService visualizationService;
            
    @Tool(description = "Generate interactive visualizations from any Oracle table or query data. Supports Plotly and Vega-Lite frameworks with automatic data type detection.")
    public Map<String, Object> generateVisualization(
            @ToolParam(description = "Oracle table name to visualize (e.g., 'C##LOAN_SCHEMA.LOAN_APPLICATIONS')", required = true) 
            String tableName,
            
            @ToolParam(description = "Type of chart to generate (bar, line, scatter, pie, heatmap, combo)", required = false) 
            String chartType,
            
            @ToolParam(description = "Visualization framework to use (plotly, vega-lite)", required = false) 
            String framework,
            
            @ToolParam(description = "Column for X-axis (auto-detected if not provided)", required = false) 
            String xColumn,
            
            @ToolParam(description = "Column for Y-axis (auto-detected if not provided)", required = false) 
            String yColumn,
            
            @ToolParam(description = "Column for color mapping (optional)", required = false) 
            String colorColumn,
            
            @ToolParam(description = "SQL WHERE clause to filter data (optional)", required = false) 
            String whereClause,
            
            @ToolParam(description = "Column to group by for aggregation (optional)", required = false) 
            String groupBy,
            
            @ToolParam(description = "Aggregation function for grouped data (COUNT, SUM, AVG, MIN, MAX)", required = false) 
            String aggregationType,
            
            @ToolParam(description = "Maximum number of records to visualize (default: 1000)", required = false) 
            Integer limit) {
        
        try {
            log.info("Executing generic visualization tool for table: {}", tableName);
            
            VisualizationRequest request = VisualizationRequest.builder()
                .tableName(tableName)
                .chartType(chartType != null ? chartType : "bar")
                .framework(framework != null ? framework : "plotly")
                .xColumn(xColumn)
                .yColumn(yColumn)
                .colorColumn(colorColumn)
                .whereClause(whereClause)
                .groupBy(groupBy)
                .aggregationType(aggregationType)
                .limit(limit != null ? limit : 1000)
                .build();
            
            ChartSpecification chart = visualizationService.generateVisualization(request);
            
            return Map.of(
                "status", "success",
                "visualization", Map.of(
                    "framework", chart.getFramework(),
                    "chartType", chart.getChartType(),
                    "specification", chart.getSpecification(),
                    "metadata", chart.getMetadata()
                ),
                "renderingInstructions", generateRenderingInstructions(chart),
                "dataSource", Map.of(
                    "table", request.getTableName(),
                    "rowCount", chart.getMetadata().get("rowCount")
                )
            );
            
        } catch (Exception e) {
            log.error("Error executing generic visualization tool", e);
            return Map.of(
                "status", "error",
                "message", "Failed to generate visualization: " + e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            );
        }
    }
    
    private Map<String, Object> generateRenderingInstructions(ChartSpecification chart) {
        Map<String, Object> instructions = new HashMap<>();
        
        if ("plotly".equals(chart.getFramework())) {
            instructions.put("library", "Plotly.js");
            instructions.put("cdn", "https://cdn.plot.ly/plotly-latest.min.js");
            instructions.put("htmlTemplate", """
                <div id="chart" style="width:100%;height:400px;"></div>
                <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
                <script>
                  const spec = %s;
                  Plotly.newPlot('chart', spec.data, spec.layout, {responsive: true});
                </script>
                """.formatted("/* INSERT_SPECIFICATION_HERE */"));
            instructions.put("jsCode", "Plotly.newPlot('chart', specification.data, specification.layout, {responsive: true});");
        } else {
            instructions.put("library", "Vega-Lite");
            instructions.put("cdn", "https://cdn.jsdelivr.net/npm/vega-lite@5");
            instructions.put("htmlTemplate", """
                <div id="chart"></div>
                <script src="https://cdn.jsdelivr.net/npm/vega@5"></script>
                <script src="https://cdn.jsdelivr.net/npm/vega-lite@5"></script>
                <script src="https://cdn.jsdelivr.net/npm/vega-embed@6"></script>
                <script>
                  const spec = %s;
                  vegaEmbed('#chart', spec);
                </script>
                """.formatted("/* INSERT_SPECIFICATION_HERE */"));
            instructions.put("jsCode", "vegaEmbed('#chart', specification);");
        }
        
        return instructions;
    }
}