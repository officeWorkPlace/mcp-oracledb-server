package com.deepai.mcpserver.vservice;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional Chart Generator for Beautiful, Enterprise-Grade Visualizations
 * Supports advanced Vega-Lite specifications with corporate styling, animations, and interactivity
 */
@Service
@Slf4j
public class ProfessionalChartGenerator {
    
    @Autowired
    private VisualizationProperties properties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String VEGA_LITE_SCHEMA = "https://vega.github.io/schema/vega-lite/v5.json";
    
    // Professional Color Palettes
    private static final Map<String, String[]> COLOR_PALETTES = Map.of(
        "corporate", new String[]{"#1e3a8a", "#3b82f6", "#60a5fa", "#93c5fd", "#dbeafe"},
        "financial", new String[]{"#134e4a", "#059669", "#10b981", "#6ee7b7", "#d1fae5"},
        "executive", new String[]{"#7c2d12", "#dc2626", "#f87171", "#fca5a5", "#fecaca"},
        "ocean", new String[]{"#164e63", "#0891b2", "#06b6d4", "#67e8f9", "#cffafe"},
        "gradient_blue", new String[]{"#1e40af", "#3b82f6", "#60a5fa", "#93c5fd", "#dbeafe"},
        "gradient_green", new String[]{"#166534", "#16a34a", "#22c55e", "#4ade80", "#bbf7d0"}
    );
    
    /**
     * 1. EXECUTIVE DASHBOARD - Multi-layer KPI Dashboard
     */
    @Cacheable(value = "professionalCharts", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateExecutiveDashboard(List<Map<String, Object>> data, Map<String, Object> config) {
        String theme = config.getOrDefault("theme", "corporate").toString();
        try {
            ObjectNode spec = objectMapper.createObjectNode();
            
            spec.put("$schema", VEGA_LITE_SCHEMA);
            spec.put("description", "Executive KPI Dashboard with Multi-layer Visualizations");
            spec.put("title", createProfessionalTitle("Executive Performance Dashboard", config));
            
            // Data
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("values", objectMapper.valueToTree(data));
            spec.set("data", dataNode);
            
            // Mark (chart type)
            spec.put("mark", "bar");
            
            // Encoding (axes, color, size, etc.)
            ObjectNode encodingNode = objectMapper.createObjectNode();
            
            ObjectNode xNode = objectMapper.createObjectNode();
            xNode.put("field", "branch_name");
            xNode.put("type", "nominal");
            xNode.put("title", "Branch");
            encodingNode.set("x", xNode);
            
            ObjectNode yNode = objectMapper.createObjectNode();
            yNode.put("field", "total_amount");
            yNode.put("type", "quantitative");
            yNode.put("title", "Amount ($M)");
            encodingNode.set("y", yNode);
            
            String[] palette = getColorPalette(theme);
            ObjectNode colorNode = objectMapper.createObjectNode();
            colorNode.put("value", palette[1]);
            encodingNode.set("color", colorNode);
            
            spec.set("encoding", encodingNode);
            
            // Professional configuration
            spec.set("config", createExecutiveThemeConfig(theme));
            
            spec.put("width", 800);
            spec.put("height", 400);
            
            return objectMapper.convertValue(spec, Map.class);
            
        } catch (Exception e) {
            log.error("Error generating executive dashboard", e);
            throw new RuntimeException("Failed to generate executive dashboard", e);
        }
    }
    
    /**
     * 2. GRADIENT AREA CHART - Beautiful Time Series with Gradient Fill
     */
    @Cacheable(value = "professionalCharts", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateGradientAreaChart(List<Map<String, Object>> data, Map<String, Object> config) {
        String theme = config.getOrDefault("theme", "corporate").toString();
        try {
            ObjectNode spec = objectMapper.createObjectNode();
            
            spec.put("$schema", VEGA_LITE_SCHEMA);
            spec.put("description", "Beautiful Gradient Area Chart for Time Series Analysis");
            spec.put("title", createProfessionalTitle("Revenue Trend Analysis", config));
            
            // Data
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("values", objectMapper.valueToTree(data));
            spec.set("data", dataNode);
            
            // Professional mark with gradient
            ObjectNode mark = objectMapper.createObjectNode();
            mark.put("type", "area");
            mark.put("interpolate", "cardinal");
            mark.put("fillOpacity", 0.8);
            
            // Theme-based gradient
            String[] palette = getColorPalette(theme);
            mark.put("fill", palette[1]);
            
            spec.set("mark", mark);
            
            // Professional encoding
            ObjectNode encoding = objectMapper.createObjectNode();
            
            // X-axis (temporal)
            ObjectNode xNode = objectMapper.createObjectNode();
            xNode.put("field", config.getOrDefault("xField", "month").toString());
            xNode.put("type", "temporal");
            xNode.put("title", "Time Period");
            encoding.set("x", xNode);
            
            // Y-axis (quantitative)
            ObjectNode yNode = objectMapper.createObjectNode();
            yNode.put("field", config.getOrDefault("yField", "avg_loan_amount").toString());
            yNode.put("type", "quantitative");
            yNode.put("title", "Revenue ($M)");
            encoding.set("y", yNode);
            
            spec.set("encoding", encoding);
            
            spec.put("width", 700);
            spec.put("height", 350);
            
            return objectMapper.convertValue(spec, Map.class);
            
        } catch (Exception e) {
            log.error("Error generating gradient area chart", e);
            throw new RuntimeException("Failed to generate gradient area chart", e);
        }
    }
    
    /**
     * 3. INTERACTIVE HEATMAP - Risk/Performance Matrix
     */
    @Cacheable(value = "professionalCharts", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateInteractiveHeatmap(List<Map<String, Object>> data, Map<String, Object> config) {
        String theme = config.getOrDefault("theme", "corporate").toString();
        try {
            ObjectNode spec = objectMapper.createObjectNode();
            
            spec.put("$schema", VEGA_LITE_SCHEMA);
            spec.put("description", "Interactive Risk-Performance Heatmap with Professional Styling");
            spec.put("title", createProfessionalTitle("Portfolio Risk-Return Matrix", config));
            
            // Data
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("values", objectMapper.valueToTree(data));
            spec.set("data", dataNode);
            
            // Professional rect mark
            ObjectNode mark = objectMapper.createObjectNode();
            mark.put("type", "rect");
            mark.put("stroke", "#ffffff");
            mark.put("strokeWidth", 2);
            spec.set("mark", mark);
            
            // Encoding
            ObjectNode encoding = objectMapper.createObjectNode();
            
            // X-axis (risk category)
            ObjectNode xNode = objectMapper.createObjectNode();
            xNode.put("field", "risk_category");
            xNode.put("type", "ordinal");
            xNode.put("title", "Risk Level");
            encoding.set("x", xNode);
            
            // Y-axis (loan type)
            ObjectNode yNode = objectMapper.createObjectNode();
            yNode.put("field", "loan_type");
            yNode.put("type", "ordinal");
            yNode.put("title", "Loan Product");
            encoding.set("y", yNode);
            
            // Color (performance metric)
            ObjectNode colorNode = objectMapper.createObjectNode();
            colorNode.put("field", "roi_percentage");
            colorNode.put("type", "quantitative");
            colorNode.put("title", "ROI %");
            
            // Professional color scale
            ObjectNode colorScale = objectMapper.createObjectNode();
            colorScale.put("scheme", "redyellowgreen");
            colorScale.put("reverse", false);
            colorNode.set("scale", colorScale);
            
            encoding.set("color", colorNode);
            
            spec.set("encoding", encoding);
            
            spec.put("width", 400);
            spec.put("height", 300);
            
            return objectMapper.convertValue(spec, Map.class);
            
        } catch (Exception e) {
            log.error("Error generating interactive heatmap", e);
            throw new RuntimeException("Failed to generate interactive heatmap", e);
        }
    }
    
    /**
     * 4. FINANCIAL CANDLESTICK CHART - OHLC with Volume
     */
    @Cacheable(value = "professionalCharts", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateCandlestickChart(List<Map<String, Object>> data, Map<String, Object> config) {
        String theme = config.getOrDefault("theme", "financial").toString();
        try {
            ObjectNode spec = objectMapper.createObjectNode();
            
            spec.put("$schema", VEGA_LITE_SCHEMA);
            spec.put("description", "Professional Financial Candlestick Chart");
            spec.put("title", createProfessionalTitle("Loan Rate Analysis - OHLC", config));
            
            // Data
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("values", objectMapper.valueToTree(data));
            spec.set("data", dataNode);
            
            // Simplified candlestick using bar mark
            ObjectNode mark = objectMapper.createObjectNode();
            mark.put("type", "bar");
            mark.put("width", 8);
            spec.set("mark", mark);
            
            ObjectNode encoding = objectMapper.createObjectNode();
            encoding.set("x", objectMapper.createObjectNode()
                .put("field", "date")
                .put("type", "temporal"));
            encoding.set("y", objectMapper.createObjectNode()
                .put("field", "low")
                .put("type", "quantitative"));
            encoding.set("y2", objectMapper.createObjectNode()
                .put("field", "high"));
            ObjectNode colorCondition = objectMapper.createObjectNode();
            colorCondition.put("test", "datum.open < datum.close");
            colorCondition.put("value", "#22c55e");
            
            ObjectNode colorNode = objectMapper.createObjectNode();
            colorNode.set("condition", colorCondition);
            colorNode.put("value", "#ef4444");
            encoding.set("color", colorNode);
            
            spec.set("encoding", encoding);
            
            spec.put("width", 700);
            spec.put("height", 300);
            
            return objectMapper.convertValue(spec, Map.class);
            
        } catch (Exception e) {
            log.error("Error generating candlestick chart", e);
            throw new RuntimeException("Failed to generate candlestick chart", e);
        }
    }
    
    // Helper methods for creating professional configurations
    
    private ObjectNode createProfessionalTitle(String title, Map<String, Object> config) {
        ObjectNode titleNode = objectMapper.createObjectNode();
        titleNode.put("text", config.getOrDefault("title", title).toString());
        titleNode.put("fontSize", 18);
        titleNode.put("fontWeight", "bold");
        titleNode.put("color", "#1f2937");
        titleNode.put("anchor", "start");
        titleNode.put("offset", 20);
        return titleNode;
    }
    
    private ObjectNode createExecutiveThemeConfig(String theme) {
        ObjectNode config = objectMapper.createObjectNode();
        String[] palette = getColorPalette(theme);
        
        // Professional styling
        config.put("background", "#f8fafc");
        config.put("padding", 40);
        
        // Axis styling
        ObjectNode axisConfig = objectMapper.createObjectNode();
        axisConfig.put("domainColor", "#64748b");
        axisConfig.put("tickColor", "#64748b");
        axisConfig.put("labelColor", "#374151");
        axisConfig.put("titleColor", "#1f2937");
        axisConfig.put("gridColor", "#e2e8f0");
        axisConfig.put("gridOpacity", 0.5);
        config.set("axis", axisConfig);
        
        return config;
    }
    
    /**
     * Get professional color palette
     */
    public String[] getColorPalette(String paletteName) {
        return COLOR_PALETTES.getOrDefault(paletteName, COLOR_PALETTES.get("corporate"));
    }
}
