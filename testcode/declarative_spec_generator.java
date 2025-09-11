package com.deepai.mcpserver.service.declarative;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Generates declarative visualization specifications (Vega-Lite and Plotly) 
 * for financial data analysis including loans, branches, customers, and risk assessments.
 */
@Service
@Slf4j
public class DeclarativeSpecGenerator {
    
    @Autowired
    private VisualizationProperties properties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String VEGA_LITE_SCHEMA = "https://vega.github.io/schema/vega-lite/v5.json";
    
    /**
     * Generate Vega-Lite specification for any chart type with data
     */
    @Cacheable(value = "vegaLiteSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateVegaLiteSpec(String chartType, List<Map<String, Object>> data, 
                                                     Map<String, String> encoding, Map<String, Object> config) {
        try {
            ObjectNode spec = objectMapper.createObjectNode();
            
            // Schema and metadata
            spec.put("$schema", VEGA_LITE_SCHEMA);
            spec.put("description", config.getOrDefault("description", "Generated chart specification").toString());
            
            // Data - inline values for better performance
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.set("values", objectMapper.valueToTree(data));
            spec.set("data", dataNode);
            
            // Mark (chart type)
            spec.put("mark", getVegaLiteMark(chartType));
            
            // Encoding (axes, color, size, etc.)
            ObjectNode encodingNode = objectMapper.createObjectNode();
            
            if (encoding.containsKey("x")) {
                ObjectNode xNode = objectMapper.createObjectNode();
                xNode.put("field", encoding.get("x"));
                xNode.put("type", getFieldType(data, encoding.get("x")));
                if (encoding.containsKey("xTitle")) {
                    xNode.put("title", encoding.get("xTitle"));
                }
                encodingNode.set("x", xNode);
            }
            
            if (encoding.containsKey("y")) {
                ObjectNode yNode = objectMapper.createObjectNode();
                yNode.put("field", encoding.get("y"));
                yNode.put("type", getFieldType(data, encoding.get("y")));
                if (encoding.containsKey("yTitle")) {
                    yNode.put("title", encoding.get("yTitle"));
                }
                encodingNode.set("y", yNode);
            }
            
            if (encoding.containsKey("color")) {
                ObjectNode colorNode = objectMapper.createObjectNode();
                colorNode.put("field", encoding.get("color"));
                colorNode.put("type", getFieldType(data, encoding.get("color")));
                encodingNode.set("color", colorNode);
            }
            
            if (encoding.containsKey("size")) {
                ObjectNode sizeNode = objectMapper.createObjectNode();
                sizeNode.put("field", encoding.get("size"));
                sizeNode.put("type", "quantitative");
                encodingNode.set("size", sizeNode);
            }
            
            spec.set("encoding", encodingNode);
            
            // Configuration
            ObjectNode configNode = objectMapper.createObjectNode();
            configNode.put("view", objectMapper.createObjectNode()
                .put("strokeWidth", 0));
            
            if (properties.getChart().isResponsive()) {
                configNode.put("autosize", objectMapper.createObjectNode()
                    .put("type", "fit")
                    .put("contains", "padding"));
            }
            
            spec.set("config", configNode);
            
            // Width and height
            if (!properties.getChart().isResponsive()) {
                spec.put("width", properties.getChart().getDefault().getWidth());
                spec.put("height", properties.getChart().getDefault().getHeight());
            }
            
            // Title
            if (config.containsKey("title")) {
                spec.put("title", config.get("title").toString());
            }
            
            return objectMapper.convertValue(spec, Map.class);
            
        } catch (Exception e) {
            log.error("Error generating Vega-Lite spec for chartType: {}", chartType, e);
            throw new RuntimeException("Failed to generate Vega-Lite specification", e);
        }
    }
    
    /**
     * Generate Plotly specification for any chart type with data
     */
    @Cacheable(value = "plotlySpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generatePlotlySpec(String chartType, List<Map<String, Object>> data,
                                                   Map<String, String> encoding, Map<String, Object> config) {
        try {
            Map<String, Object> spec = new HashMap<>();
            
            // Data traces
            List<Map<String, Object>> traces = new ArrayList<>();
            Map<String, Object> trace = new HashMap<>();
            
            trace.put("type", getPlotlyType(chartType));
            
            if (encoding.containsKey("x")) {
                trace.put("x", extractColumn(data, encoding.get("x")));
            }
            
            if (encoding.containsKey("y")) {
                trace.put("y", extractColumn(data, encoding.get("y")));
            }
            
            if (encoding.containsKey("color")) {
                trace.put("marker", Map.of("color", extractColumn(data, encoding.get("color"))));
            }
            
            if (encoding.containsKey("size")) {
                Map<String, Object> marker = (Map<String, Object>) trace.getOrDefault("marker", new HashMap<>());
                marker.put("size", extractColumn(data, encoding.get("size")));
                trace.put("marker", marker);
            }
            
            traces.add(trace);
            spec.put("data", traces);
            
            // Layout
            Map<String, Object> layout = new HashMap<>();
            layout.put("title", config.getOrDefault("title", "Chart"));
            
            if (encoding.containsKey("xTitle")) {
                layout.put("xaxis", Map.of("title", encoding.get("xTitle")));
            }
            
            if (encoding.containsKey("yTitle")) {
                layout.put("yaxis", Map.of("title", encoding.get("yTitle")));
            }
            
            if (!properties.getChart().isResponsive()) {
                layout.put("width", properties.getChart().getDefault().getWidth());
                layout.put("height", properties.getChart().getDefault().getHeight());
            } else {
                layout.put("autosize", true);
            }
            
            spec.put("layout", layout);
            
            // Configuration
            Map<String, Object> plotlyConfig = new HashMap<>();
            plotlyConfig.put("responsive", properties.getChart().isResponsive());
            plotlyConfig.put("displayModeBar", true);
            plotlyConfig.put("displaylogo", false);
            spec.put("config", plotlyConfig);
            
            return spec;
            
        } catch (Exception e) {
            log.error("Error generating Plotly spec for chartType: {}", chartType, e);
            throw new RuntimeException("Failed to generate Plotly specification", e);
        }
    }
    
    /**
     * Generate Financial Domain Specific Charts
     */
    public Map<String, Object> generateLoanPopularitySpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "loan_type",
            "y", "count",
            "xTitle", "Loan Type",
            "yTitle", "Number of Applications"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Loan Product Popularity",
            "description", "Bar chart showing loan application counts by type"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("bar", data, encoding, config);
        } else {
            return generateVegaLiteSpec("bar", data, encoding, config);
        }
    }
    
    public Map<String, Object> generateBranchPerformanceSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "branch_name",
            "y", "total_amount",
            "color", "approval_rate",
            "xTitle", "Branch",
            "yTitle", "Total Loan Amount"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Branch Lending Performance",
            "description", "Branch performance with total amounts and approval rates"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("bar", data, encoding, config);
        } else {
            return generateVegaLiteSpec("bar", data, encoding, config);
        }
    }
    
    public Map<String, Object> generateCustomerSegmentationSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "credit_score",
            "y", "income",
            "size", "loan_count",
            "color", "risk_category",
            "xTitle", "Credit Score",
            "yTitle", "Annual Income"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Customer Segmentation Analysis",
            "description", "Customer distribution by credit score and income"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("scatter", data, encoding, config);
        } else {
            return generateVegaLiteSpec("point", data, encoding, config);
        }
    }
    
    public Map<String, Object> generateInterestRateImpactSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "date",
            "y", "application_count",
            "color", "interest_rate",
            "xTitle", "Date",
            "yTitle", "Loan Applications"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Interest Rate Impact on Loan Applications",
            "description", "Time series showing loan applications vs interest rates"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("line", data, encoding, config);
        } else {
            return generateVegaLiteSpec("line", data, encoding, config);
        }
    }
    
    public Map<String, Object> generateRiskAssessmentTrendsSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "assessment_date",
            "y", "risk_score",
            "color", "risk_category",
            "xTitle", "Assessment Date",
            "yTitle", "Average Risk Score"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Risk Assessment Trends",
            "description", "Risk score trends over time by category"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("line", data, encoding, config);
        } else {
            return generateVegaLiteSpec("line", data, encoding, config);
        }
    }
    
    public Map<String, Object> generateAuditComplianceSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "audit_date",
            "y", "change_count",
            "color", "table_name",
            "xTitle", "Date",
            "yTitle", "Number of Changes"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Audit Log Activity",
            "description", "Database changes over time for compliance monitoring"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("line", data, encoding, config);
        } else {
            return generateVegaLiteSpec("line", data, encoding, config);
        }
    }
    
    public Map<String, Object> generatePaymentBehaviorSpec(List<Map<String, Object>> data, String framework) {
        Map<String, String> encoding = Map.of(
            "x", "payment_category",
            "y", "percentage",
            "color", "loan_type",
            "xTitle", "Payment Category",
            "yTitle", "Percentage"
        );
        
        Map<String, Object> config = Map.of(
            "title", "Payment Behavior Analysis",
            "description", "Payment timeliness by loan type"
        );
        
        if ("plotly".equalsIgnoreCase(framework)) {
            return generatePlotlySpec("bar", data, encoding, config);
        } else {
            return generateVegaLiteSpec("bar", data, encoding, config);
        }
    }
    
    // Helper methods
    private String getVegaLiteMark(String chartType) {
        switch (chartType.toLowerCase()) {
            case "bar": return "bar";
            case "line": return "line";
            case "point":
            case "scatter": return "point";
            case "area": return "area";
            case "pie": return "arc";
            default: return "bar";
        }
    }
    
    private String getPlotlyType(String chartType) {
        switch (chartType.toLowerCase()) {
            case "bar": return "bar";
            case "line": return "scatter";
            case "scatter": return "scatter";
            case "area": return "scatter";
            case "pie": return "pie";
            default: return "bar";
        }
    }
    
    private String getFieldType(List<Map<String, Object>> data, String field) {
        if (data.isEmpty()) return "nominal";
        
        Object value = data.get(0).get(field);
        if (value instanceof Number) {
            return "quantitative";
        } else if (value instanceof Date || (value instanceof String && 
                   ((String) value).matches("\\d{4}-\\d{2}-\\d{2}.*"))) {
            return "temporal";
        } else {
            return "nominal";
        }
    }
    
    private List<Object> extractColumn(List<Map<String, Object>> data, String column) {
        return data.stream()
                   .map(row -> row.get(column))
                   .filter(Objects::nonNull)
                   .toList();
    }
}