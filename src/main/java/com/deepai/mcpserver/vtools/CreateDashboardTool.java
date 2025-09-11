package com.deepai.mcpserver.vtools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.vservice.VisualizationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CreateDashboardTool {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Tool(description = "Create comprehensive dashboard with multiple visualizations from Oracle table data. Generates executive, operational, or analytical dashboards.")
    public Map<String, Object> createDashboard(
            @ToolParam(description = "Type of dashboard (executive, operational, analytical)", required = true) 
            String dashboardType,
            
            @ToolParam(description = "Primary Oracle table for dashboard data", required = true) 
            String primaryTable,
            
            @ToolParam(description = "Visualization framework (plotly, vega-lite)", required = false) 
            String framework,
            
            @ToolParam(description = "Additional tables to include (comma-separated)", required = false) 
            String additionalTables) {
        
        try {
            log.info("Creating {} dashboard for table: {}", dashboardType, primaryTable);
            
            List<Map<String, Object>> dashboardCharts = generateDashboardCharts(dashboardType, primaryTable, framework);
            
            return Map.of(
                "status", "success",
                "dashboard", Map.of(
                    "type", dashboardType,
                    "framework", framework != null ? framework : "plotly",
                    "primaryTable", primaryTable,
                    "charts", dashboardCharts,
                    "layoutConfig", generateLayoutConfig(dashboardType),
                    "refreshInterval", getDashboardRefreshInterval(dashboardType)
                ),
                "htmlTemplate", generateDashboardHtml(dashboardCharts, dashboardType)
            );
            
        } catch (Exception e) {
            log.error("Error creating dashboard", e);
            return Map.of(
                "status", "error",
                "message", "Failed to create dashboard: " + e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            );
        }
    }
    
    private List<Map<String, Object>> generateDashboardCharts(String dashboardType, String primaryTable, String framework) {
        List<Map<String, Object>> charts = new ArrayList<>();
        String fw = framework != null ? framework : "plotly";
        
        switch (dashboardType.toLowerCase()) {
            case "executive":
                charts.add(createChart(primaryTable, "bar", "Executive Overview", fw));
                charts.add(createChart(primaryTable, "line", "Trend Analysis", fw));
                charts.add(createChart(primaryTable, "pie", "Distribution", fw));
                break;
            case "operational":
                charts.add(createChart(primaryTable, "bar", "Operational Metrics", fw));
                charts.add(createChart(primaryTable, "scatter", "Performance Analysis", fw));
                charts.add(createChart(primaryTable, "heatmap", "Activity Matrix", fw));
                break;
            case "analytical":
                charts.add(createChart(primaryTable, "scatter", "Correlation Analysis", fw));
                charts.add(createChart(primaryTable, "line", "Time Series", fw));
                charts.add(createChart(primaryTable, "heatmap", "Pattern Analysis", fw));
                charts.add(createChart(primaryTable, "bar", "Categorical Breakdown", fw));
                break;
            default:
                charts.add(createChart(primaryTable, "bar", "Data Overview", fw));
        }
        
        return charts;
    }
    
    private Map<String, Object> createChart(String tableName, String chartType, String title, String framework) {
        try {
            ChartSpecification chart = visualizationService.generateSmartVisualization(tableName, null, null);
            return Map.of(
                "id", UUID.randomUUID().toString(),
                "title", title,
                "chartType", chartType,
                "framework", framework,
                "specification", chart.getSpecification(),
                "metadata", chart.getMetadata()
            );
        } catch (Exception e) {
            log.warn("Failed to create chart {} for table {}", chartType, tableName);
            return Map.of(
                "id", UUID.randomUUID().toString(),
                "title", title,
                "error", "Failed to generate chart: " + e.getMessage()
            );
        }
    }
    
    private Map<String, Object> generateLayoutConfig(String dashboardType) {
        switch (dashboardType.toLowerCase()) {
            case "executive":
                return Map.of(
                    "rows", 2,
                    "columns", 2,
                    "responsive", true,
                    "spacing", "medium"
                );
            case "operational":
                return Map.of(
                    "rows", 2,
                    "columns", 3,
                    "responsive", true,
                    "spacing", "small"
                );
            case "analytical":
                return Map.of(
                    "rows", 3,
                    "columns", 2,
                    "responsive", true,
                    "spacing", "large"
                );
            default:
                return Map.of(
                    "rows", 1,
                    "columns", 1,
                    "responsive", true,
                    "spacing", "medium"
                );
        }
    }
    
    private int getDashboardRefreshInterval(String dashboardType) {
        switch (dashboardType.toLowerCase()) {
            case "executive":
                return 300; // 5 minutes
            case "operational":
                return 60;  // 1 minute
            case "analytical":
                return 180; // 3 minutes
            default:
                return 120; // 2 minutes
        }
    }
    
    private String generateDashboardHtml(List<Map<String, Object>> charts, String dashboardType) {
        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>%s Dashboard</title>
                <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }
                    .dashboard { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 20px; }
                    .chart-container { border: 1px solid #ddd; padding: 10px; border-radius: 5px; }
                    .chart-title { font-weight: bold; margin-bottom: 10px; }
                </style>
            </head>
            <body>
                <h1>%s Dashboard</h1>
                <div class="dashboard">
            """.formatted(dashboardType, dashboardType));
        
        for (int i = 0; i < charts.size(); i++) {
            Map<String, Object> chart = charts.get(i);
            html.append("""
                    <div class="chart-container">
                        <div class="chart-title">%s</div>
                        <div id="chart%d" style="height: 400px;"></div>
                    </div>
                """.formatted(chart.get("title"), i));
        }
        
        html.append("""
                </div>
                <script>
                    // Chart rendering code would go here
                    // Each chart specification would be rendered to its corresponding div
                </script>
            </body>
            </html>
            """);
        
        return html.toString();
    }
}