package com.deepai.mcpserver.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Generates declarative visualization specifications (Vega-Lite and Plotly) for
 * financial data analysis including loans, branches, customers, and risk
 * assessments.
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
            
            if (encoding.containsKey("name")) {
                trace.put("name", encoding.get("name"));
            }
            
            traces.add(trace);
            spec.put("data", traces);
            
            // Layout
            Map<String, Object> layout = new HashMap<>();
            
            if (config.containsKey("title")) {
                layout.put("title", config.get("title"));
            }
            
            if (encoding.containsKey("xTitle")) {
                layout.put("xaxis", Map.of("title", encoding.get("xTitle")));
            }
            
            if (encoding.containsKey("yTitle")) {
                layout.put("yaxis", Map.of("title", encoding.get("yTitle")));
            }
            
            if (properties.getChart().isResponsive()) {
                layout.put("autosize", true);
            } else {
                layout.put("width", properties.getChart().getDefault().getWidth());
                layout.put("height", properties.getChart().getDefault().getHeight());
            }
            
            spec.put("layout", layout);
            
            // Config
            Map<String, Object> plotlyConfig = new HashMap<>();
            plotlyConfig.put("responsive", properties.getChart().isResponsive());
            plotlyConfig.put("displayModeBar", true);
            spec.put("config", plotlyConfig);
            
            return spec;
            
        } catch (Exception e) {
            log.error("Error generating Plotly spec for chartType: {}", chartType, e);
            throw new RuntimeException("Failed to generate Plotly specification", e);
        }
    }
    
    /**
     * Generate financial dashboard specification with multiple charts
     */
    @Cacheable(value = "dashboardSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateFinancialDashboard(Map<String, List<Map<String, Object>>> datasets,
                                                           Map<String, Object> config) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            List<Map<String, Object>> charts = new ArrayList<>();
            
            // Loan performance chart
            if (datasets.containsKey("loans")) {
                Map<String, Object> loanChart = generateVegaLiteSpec(
                    "bar",
                    datasets.get("loans"),
                    Map.of("x", "loan_type", "y", "amount", "color", "status"),
                    Map.of("title", "Loan Performance by Type")
                );
                charts.add(loanChart);
            }
            
            // Branch performance chart
            if (datasets.containsKey("branches")) {
                Map<String, Object> branchChart = generateVegaLiteSpec(
                    "scatter",
                    datasets.get("branches"),
                    Map.of("x", "total_deposits", "y", "total_loans", "size", "customer_count"),
                    Map.of("title", "Branch Performance Analysis")
                );
                charts.add(branchChart);
            }
            
            // Risk assessment chart
            if (datasets.containsKey("risk")) {
                Map<String, Object> riskChart = generateVegaLiteSpec(
                    "area",
                    datasets.get("risk"),
                    Map.of("x", "date", "y", "risk_score", "color", "risk_category"),
                    Map.of("title", "Risk Assessment Over Time")
                );
                charts.add(riskChart);
            }
            
            dashboard.put("charts", charts);
            dashboard.put("layout", "grid");
            dashboard.put("title", config.getOrDefault("title", "Financial Dashboard"));
            
            return dashboard;
            
        } catch (Exception e) {
            log.error("Error generating financial dashboard", e);
            throw new RuntimeException("Failed to generate financial dashboard", e);
        }
    }
    
    /**
     * Generate time series specification for financial data
     */
    @Cacheable(value = "timeSeriesSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateTimeSeriesSpec(List<Map<String, Object>> data,
                                                       String dateField, String valueField,
                                                       String groupField, Map<String, Object> config) {
        try {
            Map<String, String> encoding = new HashMap<>();
            encoding.put("x", dateField);
            encoding.put("y", valueField);
            
            if (groupField != null) {
                encoding.put("color", groupField);
            }
            
            return generateVegaLiteSpec("line", data, encoding, config);
            
        } catch (Exception e) {
            log.error("Error generating time series spec", e);
            throw new RuntimeException("Failed to generate time series specification", e);
        }
    }
    
    /**
     * Generate correlation matrix specification
     */
    @Cacheable(value = "correlationSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateCorrelationMatrix(List<Map<String, Object>> data,
                                                          List<String> fields, Map<String, Object> config) {
        try {
            // Calculate correlation matrix
            List<Map<String, Object>> correlationData = calculateCorrelationMatrix(data, fields);
            
            Map<String, String> encoding = Map.of(
                "x", "field1",
                "y", "field2",
                "color", "correlation"
            );
            
            return generateVegaLiteSpec("rect", correlationData, encoding, config);
            
        } catch (Exception e) {
            log.error("Error generating correlation matrix", e);
            throw new RuntimeException("Failed to generate correlation matrix", e);
        }
    }
    
    /**
     * Generate hierarchical data specification (treemap, sunburst)
     */
    @Cacheable(value = "hierarchicalSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateHierarchicalSpec(List<Map<String, Object>> data,
                                                         String hierarchyField, String valueField,
                                                         String chartType, Map<String, Object> config) {
        try {
            if ("treemap".equals(chartType)) {
                return generateTreemapSpec(data, hierarchyField, valueField, config);
            } else if ("sunburst".equals(chartType)) {
                return generateSunburstSpec(data, hierarchyField, valueField, config);
            } else {
                throw new IllegalArgumentException("Unsupported hierarchical chart type: " + chartType);
            }
            
        } catch (Exception e) {
            log.error("Error generating hierarchical spec for type: {}", chartType, e);
            throw new RuntimeException("Failed to generate hierarchical specification", e);
        }
    }
    
    // Helper methods
    
    private String getVegaLiteMark(String chartType) {
        switch (chartType.toLowerCase()) {
            case "bar": return "bar";
            case "line": return "line";
            case "scatter": return "point";
            case "area": return "area";
            case "pie": return "arc";
            case "rect": case "heatmap": return "rect";
            case "histogram": return "bar";
            case "boxplot": return "boxplot";
            default: return "point";
        }
    }
    
    private String getPlotlyType(String chartType) {
        switch (chartType.toLowerCase()) {
            case "bar": return "bar";
            case "line": return "scatter";
            case "scatter": return "scatter";
            case "pie": return "pie";
            case "heatmap": return "heatmap";
            case "histogram": return "histogram";
            case "box": return "box";
            case "area": return "scatter";
            case "3d_scatter": return "scatter3d";
            case "surface": return "surface";
            default: return "scatter";
        }
    }
    
    private String getFieldType(List<Map<String, Object>> data, String field) {
        if (data.isEmpty()) return "nominal";
        
        Object value = data.get(0).get(field);
        if (value == null) return "nominal";
        
        if (value instanceof Number) {
            return "quantitative";
        } else if (value instanceof Date || (value instanceof String && isDateString(value.toString()))) {
            return "temporal";
        } else {
            return "nominal";
        }
    }
    
    private boolean isDateString(String value) {
        try {
            // Simple date pattern matching
            return value.matches("\\d{4}-\\d{2}-\\d{2}.*") || 
                   value.matches("\\d{2}/\\d{2}/\\d{4}.*");
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<Object> extractColumn(List<Map<String, Object>> data, String field) {
        return data.stream()
                   .map(row -> row.get(field))
                   .collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> calculateCorrelationMatrix(List<Map<String, Object>> data, List<String> fields) {
        List<Map<String, Object>> correlationData = new ArrayList<>();
        
        for (String field1 : fields) {
            for (String field2 : fields) {
                double correlation = calculateCorrelation(data, field1, field2);
                
                Map<String, Object> row = new HashMap<>();
                row.put("field1", field1);
                row.put("field2", field2);
                row.put("correlation", correlation);
                
                correlationData.add(row);
            }
        }
        
        return correlationData;
    }
    
    private double calculateCorrelation(List<Map<String, Object>> data, String field1, String field2) {
        List<Double> values1 = data.stream()
            .map(row -> convertToDouble(row.get(field1)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        List<Double> values2 = data.stream()
            .map(row -> convertToDouble(row.get(field2)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (values1.size() != values2.size() || values1.isEmpty()) {
            return 0.0;
        }
        
        double mean1 = values1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mean2 = values2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double numerator = 0.0;
        double sumSq1 = 0.0;
        double sumSq2 = 0.0;
        
        for (int i = 0; i < values1.size(); i++) {
            double diff1 = values1.get(i) - mean1;
            double diff2 = values2.get(i) - mean2;
            
            numerator += diff1 * diff2;
            sumSq1 += diff1 * diff1;
            sumSq2 += diff2 * diff2;
        }
        
        double denominator = Math.sqrt(sumSq1 * sumSq2);
        return denominator == 0 ? 0.0 : numerator / denominator;
    }
    
    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Map<String, Object> generateTreemapSpec(List<Map<String, Object>> data,
                                                    String hierarchyField, String valueField,
                                                    Map<String, Object> config) {
        // Treemap implementation using Plotly
        Map<String, Object> spec = new HashMap<>();
        
        List<String> labels = data.stream()
            .map(row -> row.get(hierarchyField).toString())
            .collect(Collectors.toList());
            
        List<Object> values = data.stream()
            .map(row -> row.get(valueField))
            .collect(Collectors.toList());
        
        Map<String, Object> trace = new HashMap<>();
        trace.put("type", "treemap");
        trace.put("labels", labels);
        trace.put("values", values);
        
        spec.put("data", List.of(trace));
        
        Map<String, Object> layout = new HashMap<>();
        layout.put("title", config.getOrDefault("title", "Treemap"));
        spec.put("layout", layout);
        
        return spec;
    }
    
    private Map<String, Object> generateSunburstSpec(List<Map<String, Object>> data,
                                                     String hierarchyField, String valueField,
                                                     Map<String, Object> config) {
        // Sunburst implementation using Plotly
        Map<String, Object> spec = new HashMap<>();
        
        List<String> labels = data.stream()
            .map(row -> row.get(hierarchyField).toString())
            .collect(Collectors.toList());
            
        List<Object> values = data.stream()
            .map(row -> row.get(valueField))
            .collect(Collectors.toList());
        
        Map<String, Object> trace = new HashMap<>();
        trace.put("type", "sunburst");
        trace.put("labels", labels);
        trace.put("values", values);
        
        spec.put("data", List.of(trace));
        
        Map<String, Object> layout = new HashMap<>();
        layout.put("title", config.getOrDefault("title", "Sunburst"));
        spec.put("layout", layout);
        
        return spec;
    }
    
    /**
     * Generate loan popularity specification
     */
    @Cacheable(value = "loanPopularitySpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateLoanPopularitySpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "loan_type",
                "y", "application_count",
                "color", "loan_type"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Loan Product Popularity",
                "description", "Analysis of loan application counts by product type"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("bar", data, encoding, config);
            } else {
                return generatePlotlySpec("bar", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating loan popularity spec", e);
            throw new RuntimeException("Failed to generate loan popularity specification", e);
        }
    }
    
    /**
     * Generate branch performance specification
     */
    @Cacheable(value = "branchPerformanceSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateBranchPerformanceSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "branch_name",
                "y", "total_amount",
                "color", "branch_region"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Branch Performance Analysis",
                "description", "Lending performance comparison across branches"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("bar", data, encoding, config);
            } else {
                return generatePlotlySpec("bar", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating branch performance spec", e);
            throw new RuntimeException("Failed to generate branch performance specification", e);
        }
    }
    
    /**
     * Generate customer segmentation specification
     */
    @Cacheable(value = "customerSegmentationSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateCustomerSegmentationSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "credit_score",
                "y", "annual_income",
                "color", "risk_category",
                "size", "loan_amount"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Customer Segmentation Analysis",
                "description", "Customer distribution by credit score and income"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("scatter", data, encoding, config);
            } else {
                return generatePlotlySpec("scatter", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating customer segmentation spec", e);
            throw new RuntimeException("Failed to generate customer segmentation specification", e);
        }
    }
    
    /**
     * Generate interest rate impact specification
     */
    @Cacheable(value = "interestRateImpactSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateInterestRateImpactSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "date",
                "y", "application_count",
                "color", "interest_rate_range"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Interest Rate Impact Analysis",
                "description", "Impact of interest rate changes on loan applications"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("line", data, encoding, config);
            } else {
                return generatePlotlySpec("line", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating interest rate impact spec", e);
            throw new RuntimeException("Failed to generate interest rate impact specification", e);
        }
    }
    
    /**
     * Generate risk assessment trends specification
     */
    @Cacheable(value = "riskAssessmentTrendsSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateRiskAssessmentTrendsSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "assessment_date",
                "y", "avg_risk_score",
                "color", "risk_category"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Risk Assessment Trends",
                "description", "Risk score trends over time by category"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("area", data, encoding, config);
            } else {
                return generatePlotlySpec("area", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating risk assessment trends spec", e);
            throw new RuntimeException("Failed to generate risk assessment trends specification", e);
        }
    }
    
    /**
     * Generate audit compliance specification
     */
    @Cacheable(value = "auditComplianceSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateAuditComplianceSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "audit_date",
                "y", "event_count",
                "color", "event_type"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Audit & Compliance Analysis",
                "description", "Database audit events and compliance tracking"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("bar", data, encoding, config);
            } else {
                return generatePlotlySpec("bar", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating audit compliance spec", e);
            throw new RuntimeException("Failed to generate audit compliance specification", e);
        }
    }
    
    /**
     * Generate payment behavior specification
     */
    @Cacheable(value = "paymentBehaviorSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generatePaymentBehaviorSpec(List<Map<String, Object>> data, String framework) {
        try {
            Map<String, String> encoding = Map.of(
                "x", "payment_category",
                "y", "count",
                "color", "timeliness_status"
            );
            
            Map<String, Object> config = Map.of(
                "title", "Payment Behavior Analysis",
                "description", "Payment patterns and timeliness analysis"
            );
            
            if ("vega-lite".equals(framework)) {
                return generateVegaLiteSpec("bar", data, encoding, config);
            } else {
                return generatePlotlySpec("bar", data, encoding, config);
            }
        } catch (Exception e) {
            log.error("Error generating payment behavior spec", e);
            throw new RuntimeException("Failed to generate payment behavior specification", e);
        }
    }

    /**
     * Generate advanced financial metrics visualization
     */
    @Cacheable(value = "metricsSpecs", cacheManager = "visualizationCacheManager")
    public Map<String, Object> generateFinancialMetricsSpec(List<Map<String, Object>> data,
                                                            String metricType, Map<String, Object> config) {
        try {
            switch (metricType.toLowerCase()) {
                case "roi":
                    return generateROISpec(data, config);
                case "risk_return":
                    return generateRiskReturnSpec(data, config);
                case "portfolio_allocation":
                    return generatePortfolioAllocationSpec(data, config);
                case "cash_flow":
                    return generateCashFlowSpec(data, config);
                default:
                    throw new IllegalArgumentException("Unsupported metric type: " + metricType);
            }
        } catch (Exception e) {
            log.error("Error generating financial metrics spec for type: {}", metricType, e);
            throw new RuntimeException("Failed to generate financial metrics specification", e);
        }
    }
    
    private Map<String, Object> generateROISpec(List<Map<String, Object>> data, Map<String, Object> config) {
        Map<String, String> encoding = Map.of(
            "x", "investment_period",
            "y", "roi_percentage",
            "color", "investment_type"
        );
        return generateVegaLiteSpec("bar", data, encoding, config);
    }
    
    private Map<String, Object> generateRiskReturnSpec(List<Map<String, Object>> data, Map<String, Object> config) {
        Map<String, String> encoding = Map.of(
            "x", "risk_score",
            "y", "expected_return",
            "size", "investment_amount",
            "color", "asset_class"
        );
        return generateVegaLiteSpec("scatter", data, encoding, config);
    }
    
    private Map<String, Object> generatePortfolioAllocationSpec(List<Map<String, Object>> data, Map<String, Object> config) {
        Map<String, String> encoding = Map.of(
            "theta", "allocation_percentage",
            "color", "asset_class"
        );
        return generateVegaLiteSpec("pie", data, encoding, config);
    }
    
    private Map<String, Object> generateCashFlowSpec(List<Map<String, Object>> data, Map<String, Object> config) {
        Map<String, String> encoding = Map.of(
            "x", "date",
            "y", "cash_flow",
            "color", "flow_type"
        );
        return generateVegaLiteSpec("area", data, encoding, config);
    }
}