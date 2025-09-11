package com.deepai.mcpserver.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.VisualizationRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VegaLiteSpecGenerator {
    
    public ChartSpecification generateSpec(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<String, Object> spec = new HashMap<>();
        
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
            default:
                spec = generateBarChart(request, data);
        }
        
        return ChartSpecification.builder()
            .framework("vega-lite")
            .chartType(request.getChartType())
            .specification(spec)
            .build();
    }
    
    private Map<String, Object> generateBarChart(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("x", Map.of(
            "field", request.getXColumn(),
            "type", "nominal",
            "title", request.getXColumn()
        ));
        encoding.put("y", Map.of(
            "field", request.getYColumn(),
            "type", "quantitative",
            "title", request.getYColumn()
        ));
        
        return Map.of(
            "$schema", "https://vega.github.io/schema/vega-lite/v5.json",
            "title", generateTitle(request),
            "data", Map.of("values", data),
            "mark", "bar",
            "encoding", encoding
        );
    }
    
    private Map<String, Object> generateLineChart(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("x", Map.of(
            "field", request.getXColumn(),
            "type", isDateColumn(data, request.getXColumn()) ? "temporal" : "ordinal",
            "title", request.getXColumn()
        ));
        encoding.put("y", Map.of(
            "field", request.getYColumn(),
            "type", "quantitative",
            "title", request.getYColumn()
        ));
        
        return Map.of(
            "$schema", "https://vega.github.io/schema/vega-lite/v5.json",
            "title", generateTitle(request),
            "data", Map.of("values", data),
            "mark", Map.of("type", "line", "point", true),
            "encoding", encoding
        );
    }
    
    private Map<String, Object> generateScatterChart(VisualizationRequest request, List<Map<String, Object>> data) {
        Map<String, Object> encoding = new HashMap<>();
        encoding.put("x", Map.of(
            "field", request.getXColumn(),
            "type", "quantitative",
            "title", request.getXColumn()
        ));
        encoding.put("y", Map.of(
            "field", request.getYColumn(),
            "type", "quantitative",
            "title", request.getYColumn()
        ));
        
        if (request.getColorColumn() != null) {
            encoding.put("color", Map.of(
                "field", request.getColorColumn(),
                "type", "nominal"
            ));
        }
        
        return Map.of(
            "$schema", "https://vega.github.io/schema/vega-lite/v5.json",
            "title", generateTitle(request),
            "data", Map.of("values", data),
            "mark", "circle",
            "encoding", encoding
        );
    }
    
    private boolean isDateColumn(List<Map<String, Object>> data, String column) {
        if (data.isEmpty() || column == null) return false;
        Object value = data.get(0).get(column);
        return value instanceof java.util.Date || value instanceof java.sql.Date || value instanceof java.sql.Timestamp;
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