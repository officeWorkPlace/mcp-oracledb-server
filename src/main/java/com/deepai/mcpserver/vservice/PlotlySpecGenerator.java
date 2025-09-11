package com.deepai.mcpserver.vservice;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.VisualizationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PlotlySpecGenerator {
    
    public ChartSpecification generateSpec(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<String, Object> spec = new HashMap<>();
        
        try {
            switch (request.getChartType().toLowerCase()) {
                case "bar":
                    spec = generateBarChart(request, data);
                    break;
                case "line":
                    spec = generateLineChart(request, data);
                    break;
                case "scatter":
                    spec = generateScatterChart(request, data);
                    break;
                case "pie":
                    spec = generatePieChart(request, data);
                    break;
                case "heatmap":
                    spec = generateHeatmap(request, data);
                    break;
                default:
                    spec = generateBarChart(request, data);
            }
            
        } catch (Exception e) {
            log.error("Error generating Plotly spec", e);
            throw new RuntimeException("Failed to generate chart specification", e);
        }
        
        return ChartSpecification.builder()
            .framework("plotly")
            .chartType(request.getChartType())
            .specification(spec)
            .build();
    }
    
    private Map<String, Object> generateBarChart(VisualizationRequest request, List<Map<String, Object>> data) {
        List<Object> xValues = extractColumnValues(data, request.getXColumn());
        List<Object> yValues = extractColumnValues(data, request.getYColumn());
        
        Map<String, Object> trace = Map.of(
            "x", xValues,
            "y", yValues,
            "type", "bar",
            "name", request.getYColumn() != null ? request.getYColumn() : "Value",
            "marker", Map.of("color", "#3498db")
        );
        
        Map<String, Object> layout = Map.of(
            "title", generateTitle(request),
            "xaxis", Map.of("title", request.getXColumn() != null ? request.getXColumn() : "Category"),
            "yaxis", Map.of("title", request.getYColumn() != null ? request.getYColumn() : "Value"),
            "hovermode", "closest"
        );
        
        return Map.of(
            "data", Arrays.asList(trace),
            "layout", layout
        );
    }
    
    private Map<String, Object> generateLineChart(VisualizationRequest request, List<Map<String, Object>> data) {
        List<Object> xValues = extractColumnValues(data, request.getXColumn());
        List<Object> yValues = extractColumnValues(data, request.getYColumn());
        
        Map<String, Object> trace = Map.of(
            "x", xValues,
            "y", yValues,
            "type", "scatter",
            "mode", "lines+markers",
            "name", request.getYColumn() != null ? request.getYColumn() : "Value",
            "line", Map.of("color", "#e74c3c")
        );
        
        Map<String, Object> layout = Map.of(
            "title", generateTitle(request),
            "xaxis", Map.of("title", request.getXColumn() != null ? request.getXColumn() : "X Axis"),
            "yaxis", Map.of("title", request.getYColumn() != null ? request.getYColumn() : "Y Axis"),
            "hovermode", "x"
        );
        
        return Map.of(
            "data", Arrays.asList(trace),
            "layout", layout
        );
    }
    
    private Map<String, Object> generateScatterChart(VisualizationRequest request, List<Map<String, Object>> data) {
        List<Object> xValues = extractColumnValues(data, request.getXColumn());
        List<Object> yValues = extractColumnValues(data, request.getYColumn());
        
        Map<String, Object> trace = new HashMap<>();
        trace.put("x", xValues);
        trace.put("y", yValues);
        trace.put("type", "scatter");
        trace.put("mode", "markers");
        trace.put("name", "Data Points");
        
        if (request.getColorColumn() != null) {
            List<Object> colorValues = extractColumnValues(data, request.getColorColumn());
            trace.put("marker", Map.of(
                "color", colorValues,
                "colorscale", "Viridis",
                "showscale", true
            ));
        } else {
            trace.put("marker", Map.of("color", "#9b59b6"));
        }
        
        Map<String, Object> layout = Map.of(
            "title", generateTitle(request),
            "xaxis", Map.of("title", request.getXColumn() != null ? request.getXColumn() : "X Axis"),
            "yaxis", Map.of("title", request.getYColumn() != null ? request.getYColumn() : "Y Axis"),
            "hovermode", "closest"
        );
        
        return Map.of(
            "data", Arrays.asList(trace),
            "layout", layout
        );
    }
    
    private Map<String, Object> generatePieChart(VisualizationRequest request, List<Map<String, Object>> data) {
        List<Object> labels = extractColumnValues(data, request.getXColumn());
        List<Object> values = extractColumnValues(data, request.getYColumn());
        
        Map<String, Object> trace = Map.of(
            "labels", labels,
            "values", values,
            "type", "pie",
            "hole", 0.3,
            "textinfo", "label+percent"
        );
        
        Map<String, Object> layout = Map.of(
            "title", generateTitle(request),
            "showlegend", true
        );
        
        return Map.of(
            "data", Arrays.asList(trace),
            "layout", layout
        );
    }
    
    private Map<String, Object> generateHeatmap(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<Object, Map<Object, Number>> pivotData = new HashMap<>();
        
        for (Map<String, Object> row : data) {
            Object x = row.get(request.getXColumn());
            Object y = row.get(request.getYColumn());
            Number z = (Number) row.get(request.getColorColumn() != null ? request.getColorColumn() : "VALUE");
            
            pivotData.computeIfAbsent(y, k -> new HashMap()).put(x, z != null ? z : 0);
        }
        
        List<Object> xLabels = data.stream().map(row -> row.get(request.getXColumn())).distinct().collect(Collectors.toList());
        List<Object> yLabels = data.stream().map(row -> row.get(request.getYColumn())).distinct().collect(Collectors.toList());
        
        List<List<Number>> zValues = new ArrayList<>();
        for (Object y : yLabels) {
            List<Number> row = new ArrayList<>();
            for (Object x : xLabels) {
                Number value = pivotData.getOrDefault(y, new HashMap<>()).getOrDefault(x, 0);
                row.add(value);
            }
            zValues.add(row);
        }
        
        Map<String, Object> trace = Map.of(
            "x", xLabels,
            "y", yLabels,
            "z", zValues,
            "type", "heatmap",
            "colorscale", "RdYlBu",
            "showscale", true
        );
        
        Map<String, Object> layout = Map.of(
            "title", generateTitle(request),
            "xaxis", Map.of("title", request.getXColumn()),
            "yaxis", Map.of("title", request.getYColumn())
        );
        
        return Map.of(
            "data", Arrays.asList(trace),
            "layout", layout
        );
    }
    
    private List<Object> extractColumnValues(List<Map<String, Object>> data, String columnName) {
        if (columnName == null) {
            return data.stream().map(row -> row.values().iterator().next()).collect(Collectors.toList());
        }
        return data.stream()
            .map(row -> row.get(columnName))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private String generateTitle(VisualizationRequest request) {
        StringBuilder title = new StringBuilder();
        if (request.getYColumn() != null) {
            title.append(request.getYColumn());
        }
        if (request.getXColumn() != null) {
            if (title.length() > 0) title.append(" by ");
            title.append(request.getXColumn());
        }
        if (title.length() == 0) {
            title.append("Data from ").append(request.getTableName());
        }
        return title.toString();
    }
}
