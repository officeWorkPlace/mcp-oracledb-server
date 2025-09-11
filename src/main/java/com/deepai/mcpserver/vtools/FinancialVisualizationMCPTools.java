package com.deepai.mcpserver.vtools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Calendar;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.util.DeclarativeSpecGenerator;
import com.deepai.mcpserver.vservice.FinancialDataService;
import com.deepai.mcpserver.vservice.ProfessionalChartGenerator;
import com.deepai.mcpserver.vservice.ChartPerformanceOptimizer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * MCP Tools for Financial Data Visualization with Declarative Specs
 * These tools are automatically discovered and registered by the MCP framework
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true")
public class FinancialVisualizationMCPTools {
    
    @Autowired
    private FinancialDataService financialDataService;
    
    @Autowired
    private DeclarativeSpecGenerator specGenerator;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private ProfessionalChartGenerator professionalCharts;
    
    @Autowired(required = false) 
    private ChartPerformanceOptimizer optimizer;
    
    /**
     * Tool 1: Generate Loan Product Popularity Chart
     * Returns Vega-Lite or Plotly JSON specification
     */
    @Tool(
        name = "generate_loan_popularity_chart",
        description = "Creates a visual chart showing which loan products (Personal, Home, Auto, Business, Education) are most popular based on application count and loan amounts. Returns interactive Plotly or Vega-Lite chart specification with real Oracle banking data. Perfect for understanding customer preferences and market demand."
    )
    public String generateLoanPopularityChart(
            @ToolParam(description = "Time period for analysis (7d, 30d, 90d, 180d, 365d)", required = false) 
            String timeframe,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'bar', 'pie', 'line' for trends", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateLoanPopularityChart - timeframe={}, framework={}", timeframe, framework);
            
            // Set defaults
            timeframe = timeframe != null ? timeframe : "30d";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "bar";
            
            // Fetch financial data
            List<Map<String, Object>> data = financialDataService.getLoanProductPopularity(timeframe);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createMCPErrorResponse("No loan data available for the specified timeframe");
            }
            
            // Generate declarative specification
            Map<String, Object> spec = specGenerator.generateLoanPopularitySpec(data, framework);
            
            // Create MCP-compliant response
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_loan_popularity_chart");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Loan Product Popularity Analysis",
                "timeframe", timeframe,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "loan_applications table"
            ));
            mcpResponse.put("insights", generateLoanInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateLoanPopularityChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate loan popularity chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 2: Generate Branch Performance Chart
     */
    @Tool(
        name = "generate_branch_performance_chart",
        description = "Creates a comparative chart showing how different bank branches perform in terms of loan amounts, approval rates, application counts, and processing times. Helps identify top-performing branches and operational inefficiencies. Returns interactive visualization with real branch data."
    )
    public String generateBranchPerformanceChart(
            @ToolParam(description = "Primary metric: 'total_amount', 'approval_rate', 'application_count', 'avg_processing_days'", required = false) 
            String metricType,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'bar', 'scatter', 'grouped'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateBranchPerformanceChart - metric={}, framework={}", metricType, framework);
            
            metricType = metricType != null ? metricType : "total_amount";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "bar";
            
            List<Map<String, Object>> data = financialDataService.getBranchPerformance(metricType);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No branch performance data available");
            }
            
            Map<String, Object> spec = specGenerator.generateBranchPerformanceSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_branch_performance_chart");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Branch Lending Performance Analysis",
                "metricType", metricType,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "branches + loan_applications tables"
            ));
            mcpResponse.put("insights", generateBranchInsights(data, metricType));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateBranchPerformanceChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate branch performance chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 3: Generate Customer Segmentation Chart
     */
    @Tool(
        name = "generate_customer_segmentation_chart",
        description = "Creates a scatter plot or bubble chart showing customer segments based on credit scores, income levels, loan types, or risk categories. Helps identify high-value customer segments and understand customer demographics. Returns interactive visualization for customer analysis."
    )
    public String generateCustomerSegmentationChart(
            @ToolParam(description = "Segmentation basis: 'credit_score', 'income', 'loan_type', 'risk_category'", required = false) 
            String segmentBy,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'scatter', 'bubble', 'heatmap'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateCustomerSegmentationChart - segmentBy={}, framework={}", segmentBy, framework);
            
            segmentBy = segmentBy != null ? segmentBy : "credit_score";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "scatter";
            
            List<Map<String, Object>> data = financialDataService.getCustomerSegmentation(segmentBy);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No customer segmentation data available");
            }
            
            Map<String, Object> spec = specGenerator.generateCustomerSegmentationSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_customer_segmentation_chart");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Customer Segmentation Analysis",
                "segmentBy", segmentBy,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "customers + loan_applications + risk_assessments tables"
            ));
            mcpResponse.put("insights", generateCustomerInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateCustomerSegmentationChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate customer segmentation chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 4: Generate Interest Rate Impact Chart
     */
    @Tool(
        name = "analyze_interest_rate_impact",
        description = "Creates a time series chart showing how interest rate changes affect loan application volumes and customer behavior. Returns interactive visualization with correlation analysis between rates and application patterns. Perfect for monetary policy impact assessment and rate optimization."
    )
    public String generateInterestRateImpactChart(
            @ToolParam(description = "Analysis period: '90d', '180d', '365d', '730d'", required = false) 
            String timeframe,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'line', 'scatter', 'dual-axis'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateInterestRateImpactChart - timeframe={}, framework={}", timeframe, framework);
            
            timeframe = timeframe != null ? timeframe : "365d";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "line";
            
            List<Map<String, Object>> data = financialDataService.getInterestRateImpact(timeframe);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No interest rate impact data available");
            }
            
            Map<String, Object> spec = specGenerator.generateInterestRateImpactSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_interest_rate_impact");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Interest Rate Impact Analysis",
                "timeframe", timeframe,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "loan_applications table with interest rate analysis"
            ));
            mcpResponse.put("insights", generateInterestRateInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateInterestRateImpactChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate interest rate impact chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 5: Generate Risk Assessment Trends Chart
     */
    @Tool(
        name = "analyze_risk_assessment_trends",
        description = "Creates a trend chart showing risk score patterns and risk category distributions over time. Tracks how loan risk profiles change and identifies emerging risk patterns. Returns interactive visualization with risk insights for portfolio management and early warning detection."
    )
    public String generateRiskAssessmentTrendsChart(
            @ToolParam(description = "Analysis period: '30d', '90d', '180d', '365d'", required = false) 
            String timeframe,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'line', 'area', 'stacked'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateRiskAssessmentTrendsChart - timeframe={}, framework={}", timeframe, framework);
            
            timeframe = timeframe != null ? timeframe : "90d";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "line";
            
            List<Map<String, Object>> data = financialDataService.getRiskAssessmentTrends(timeframe);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No risk assessment trends data available");
            }
            
            Map<String, Object> spec = specGenerator.generateRiskAssessmentTrendsSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_risk_assessment_trends");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Risk Assessment Trends Analysis",
                "timeframe", timeframe,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "risk_assessments + loan_applications tables"
            ));
            mcpResponse.put("insights", generateRiskInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateRiskAssessmentTrendsChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate risk assessment trends chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 6: Generate Audit Compliance Chart
     */
    @Tool(
        name = "analyze_audit_compliance",
        description = "Creates a monitoring chart showing audit log activity, database changes, and compliance events over time. Identifies unusual patterns and security anomalies. Returns interactive visualization for compliance reporting and security monitoring. Essential for regulatory oversight."
    )
    public String generateAuditComplianceChart(
            @ToolParam(description = "Analysis period: '1d', '7d', '30d', '90d'", required = false) 
            String timeframe,
            
            @ToolParam(description = "Analysis focus: 'activity', 'security', 'compliance', 'performance'", required = false) 
            String analysisType,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework) {
        
        try {
            log.info("MCP Tool: generateAuditComplianceChart - timeframe={}, analysisType={}", timeframe, analysisType);
            
            timeframe = timeframe != null ? timeframe : "7d";
            analysisType = analysisType != null ? analysisType : "activity";
            framework = framework != null ? framework : "plotly";
            
            List<Map<String, Object>> data = financialDataService.getAuditCompliance(timeframe, analysisType);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No audit compliance data available");
            }
            
            Map<String, Object> spec = specGenerator.generateAuditComplianceSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_audit_compliance");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Audit & Compliance Analysis",
                "timeframe", timeframe,
                "analysisType", analysisType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "audit_logs table",
                "securityNote", "Sensitive data filtered per security policies"
            ));
            mcpResponse.put("insights", generateAuditInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateAuditComplianceChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate audit compliance chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 7: Generate Payment Behavior Chart
     */
    @Tool(
        name = "analyze_payment_behavior",
        description = "Creates charts showing customer payment patterns, timeliness, late fees, and repayment behaviors. Identifies customers or loan types with payment issues. Returns interactive visualization for collections strategy and risk assessment. Helps predict default probability."
    )
    public String generatePaymentBehaviorChart(
            @ToolParam(description = "Analysis focus: 'timeliness', 'amount', 'frequency', 'late_fees'", required = false) 
            String analysisType,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'bar', 'stacked', 'histogram', 'heatmap'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generatePaymentBehaviorChart - analysisType={}, framework={}", analysisType, framework);
            
            analysisType = analysisType != null ? analysisType : "timeliness";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "bar";
            
            List<Map<String, Object>> data = financialDataService.getPaymentBehavior(analysisType);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No payment behavior data available");
            }
            
            Map<String, Object> spec = specGenerator.generatePaymentBehaviorSpec(data, framework);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_payment_behavior");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Payment Behavior Analysis",
                "analysisType", analysisType,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "payments + loan_applications tables"
            ));
            mcpResponse.put("insights", generatePaymentInsights(data));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generatePaymentBehaviorChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate payment behavior chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 8: Generate Portfolio Analysis Chart
     */
    @Tool(
        name = "analyze_portfolio_composition",
        description = "Creates interactive charts showing loan portfolio composition, risk distribution, and performance metrics across loan categories and customer segments. Returns treemap, sunburst, or pie chart visualizations with portfolio insights. Essential for portfolio management and risk diversification analysis."
    )
    public String generatePortfolioAnalysisChart(
            @ToolParam(description = "Analysis type: 'composition', 'risk_distribution', 'performance', 'concentration'", required = false) 
            String analysisType,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'pie', 'treemap', 'sunburst', 'stacked_bar'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generatePortfolioAnalysisChart - analysisType={}, framework={}", analysisType, framework);
            
            analysisType = analysisType != null ? analysisType : "composition";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "pie";
            
            List<Map<String, Object>> data = financialDataService.getPortfolioAnalysis(analysisType);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No portfolio analysis data available");
            }
            
            Map<String, Object> spec;
            if ("treemap".equals(chartType) || "sunburst".equals(chartType)) {
                spec = specGenerator.generateHierarchicalSpec(data, "category", "amount", chartType, 
                    Map.of("title", "Portfolio Analysis - " + analysisType));
            } else {
                Map<String, String> encoding = Map.of(
                    "x", "category",
                    "y", "amount",
                    "color", "subcategory"
                );
                spec = "vega-lite".equals(framework) 
                    ? specGenerator.generateVegaLiteSpec(chartType, data, encoding, Map.of("title", "Portfolio Analysis"))
                    : specGenerator.generatePlotlySpec(chartType, data, encoding, Map.of("title", "Portfolio Analysis"));
            }
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_portfolio_composition");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Portfolio Analysis",
                "analysisType", analysisType,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "loan_applications + customers tables"
            ));
            mcpResponse.put("insights", generatePortfolioInsights(data, analysisType));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generatePortfolioAnalysisChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate portfolio analysis chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 9: Generate Trend Analysis Chart
     */
    @Tool(
        name = "analyze_trends_over_time",
        description = "Creates time series charts showing trends in loan applications, approvals, defaults, revenue, and volume over specified periods. Includes optional forecasting capabilities. Returns interactive line charts with trend analysis and growth patterns. Perfect for strategic planning and performance tracking."
    )
    public String generateTrendAnalysisChart(
            @ToolParam(description = "Metric to analyze: 'applications', 'approvals', 'defaults', 'revenue', 'volume'", required = false) 
            String metric,
            
            @ToolParam(description = "Time period: '1m', '3m', '6m', '1y', '2y'", required = false) 
            String period,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Include forecasting: true/false", required = false) 
            Boolean includeForecast) {
        
        try {
            log.info("MCP Tool: generateTrendAnalysisChart - metric={}, period={}", metric, period);
            
            metric = metric != null ? metric : "applications";
            period = period != null ? period : "6m";
            framework = framework != null ? framework : "plotly";
            includeForecast = includeForecast != null ? includeForecast : false;
            
            List<Map<String, Object>> data = financialDataService.getTrendAnalysis(metric, period);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No trend analysis data available");
            }
            
            Map<String, Object> spec = specGenerator.generateTimeSeriesSpec(data, "date", metric, null, 
                Map.of("title", "Trend Analysis - " + metric, "includeForecast", includeForecast));
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_trends_over_time");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Trend Analysis",
                "metric", metric,
                "period", period,
                "includeForecast", includeForecast,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "time series aggregated data"
            ));
            mcpResponse.put("insights", generateTrendInsights(data, metric));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateTrendAnalysisChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate trend analysis chart: " + e.getMessage());
        }
    }
    
    /**
     * Tool 10: Generate Correlation Analysis Chart
     */
    @Tool(
        name = "analyze_metric_correlations",
        description = "Creates correlation matrix heatmaps showing relationships between financial metrics like credit scores, income, loan amounts, and interest rates. Identifies strong correlations and patterns in customer data. Returns interactive heatmap or scatter matrix with correlation coefficients and insights."
    )
    public String generateCorrelationAnalysisChart(
            @ToolParam(description = "Fields to correlate (comma-separated): 'credit_score,income,loan_amount,interest_rate'", required = false) 
            String fields,
            
            @ToolParam(description = "Visualization framework: 'plotly' or 'vega-lite'", required = false) 
            String framework,
            
            @ToolParam(description = "Chart type: 'heatmap', 'scatter_matrix', 'network'", required = false) 
            String chartType) {
        
        try {
            log.info("MCP Tool: generateCorrelationAnalysisChart - fields={}, framework={}", fields, framework);
            
            fields = fields != null ? fields : "credit_score,annual_income,loan_amount,interest_rate";
            framework = framework != null ? framework : "plotly";
            chartType = chartType != null ? chartType : "heatmap";
            
            List<String> fieldList = Arrays.asList(fields.split(","));
            List<Map<String, Object>> data = financialDataService.getCorrelationData(fieldList);
            
            if (data.isEmpty()) {
                return createMCPErrorResponse("No correlation data available");
            }
            
            Map<String, Object> spec = specGenerator.generateCorrelationMatrix(data, fieldList, 
                Map.of("title", "Correlation Analysis", "chartType", chartType));
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "analyze_metric_correlations");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", framework);
            mcpResponse.put("data", data);
            mcpResponse.put("metadata", Map.of(
                "title", "Correlation Analysis",
                "fields", fieldList,
                "chartType", chartType,
                "dataPoints", data.size(),
                "generatedAt", System.currentTimeMillis(),
                "sqlSource", "multiple tables correlation analysis"
            ));
            mcpResponse.put("insights", generateCorrelationInsights(data, fieldList));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateCorrelationAnalysisChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate correlation analysis chart: " + e.getMessage());
        }
    }
    
    // Helper methods for insights generation
    
    private Map<String, Object> generateLoanInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Find most popular loan type
            String mostPopular = data.stream()
                .max(Comparator.comparing(row -> 
                    Integer.valueOf(row.get("application_count").toString())))
                .map(row -> row.get("loan_type").toString())
                .orElse("N/A");
            
            // Calculate total applications
            int totalApplications = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("application_count").toString()))
                .sum();
            
            // Calculate diversity (number of loan types)
            long loanTypesCount = data.stream()
                .map(row -> row.get("loan_type"))
                .distinct()
                .count();
            
            // Find market share of top loan type
            int topTypeApplications = data.stream()
                .filter(row -> mostPopular.equals(row.get("loan_type").toString()))
                .mapToInt(row -> Integer.valueOf(row.get("application_count").toString()))
                .sum();
            double marketShare = totalApplications > 0 ? (topTypeApplications * 100.0 / totalApplications) : 0;
            
            insights.put("mostPopularLoanType", mostPopular);
            insights.put("totalApplications", totalApplications);
            insights.put("loanTypesOffered", loanTypesCount);
            insights.put("topTypeMarketShare", Math.round(marketShare * 100.0) / 100.0);
            insights.put("diversityIndex", Math.round((loanTypesCount / (double) totalApplications) * 1000.0) / 1000.0);
            insights.put("summary", String.format("Most popular: %s (%,.1f%% market share) with %,d total applications across %d loan types", 
                mostPopular, marketShare, totalApplications, loanTypesCount));
            
        } catch (Exception e) {
            log.warn("Error generating loan insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateBranchInsights(List<Map<String, Object>> data, String metricType) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Find top performing branch
            String topBranch = data.stream()
                .max(Comparator.comparing(row -> 
                    Double.valueOf(row.get("total_amount").toString())))
                .map(row -> row.get("branch_name").toString())
                .orElse("N/A");
            
            // Calculate total across all branches
            double totalAmount = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("total_amount").toString()))
                .sum();
            
            // Find branch with lowest performance
            String lowestBranch = data.stream()
                .min(Comparator.comparing(row -> 
                    Double.valueOf(row.get("total_amount").toString())))
                .map(row -> row.get("branch_name").toString())
                .orElse("N/A");
            
            // Calculate performance metrics
            double avgPerformance = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("total_amount").toString()))
                .average()
                .orElse(0.0);
            
            // Calculate performance variance
            double variance = data.stream()
                .mapToDouble(row -> {
                    double value = Double.valueOf(row.get("total_amount").toString());
                    return Math.pow(value - avgPerformance, 2);
                })
                .average()
                .orElse(0.0);
            double standardDeviation = Math.sqrt(variance);
            
            insights.put("topPerformingBranch", topBranch);
            insights.put("lowestPerformingBranch", lowestBranch);
            insights.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
            insights.put("averagePerformance", Math.round(avgPerformance * 100.0) / 100.0);
            insights.put("performanceVariability", Math.round(standardDeviation * 100.0) / 100.0);
            insights.put("branchCount", data.size());
            insights.put("summary", String.format("Top: %s, Lowest: %s, Total: $%,.2f across %d branches (Avg: $%,.2f)", 
                topBranch, lowestBranch, totalAmount, data.size(), avgPerformance));
            
        } catch (Exception e) {
            log.warn("Error generating branch insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateCustomerInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Calculate average credit score
            double avgCreditScore = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("credit_score").toString()))
                .average()
                .orElse(0.0);
            
            // Find most common risk category
            String commonRiskCategory = data.stream()
                .collect(Collectors.groupingBy(row -> row.get("risk_category").toString(), 
                    Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            // Calculate average income
            double avgIncome = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("annual_income").toString()))
                .average()
                .orElse(0.0);
            
            // Calculate credit score distribution
            long highCreditScore = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("credit_score").toString()))
                .filter(score -> score >= 750)
                .count();
            
            double highCreditPercentage = data.size() > 0 ? (highCreditScore * 100.0 / data.size()) : 0;
            
            insights.put("averageCreditScore", Math.round(avgCreditScore));
            insights.put("mostCommonRiskCategory", commonRiskCategory);
            insights.put("averageAnnualIncome", Math.round(avgIncome));
            insights.put("highCreditScorePercentage", Math.round(highCreditPercentage * 100.0) / 100.0);
            insights.put("totalCustomers", data.size());
            insights.put("summary", String.format("Avg Credit: %d, Common Risk: %s, Avg Income: $%,.0f (%,.1f%% high credit)", 
                Math.round(avgCreditScore), commonRiskCategory, avgIncome, highCreditPercentage));
            
        } catch (Exception e) {
            log.warn("Error generating customer insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateInterestRateInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Find peak application period
            String peakPeriod = data.stream()
                .max(Comparator.comparing(row -> 
                    Integer.valueOf(row.get("application_count").toString())))
                .map(row -> row.get("date").toString())
                .orElse("N/A");
            
            // Calculate total applications
            int totalApplications = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("application_count").toString()))
                .sum();
            
            // Find average interest rate impact
            double avgImpact = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("application_count").toString()))
                .average()
                .orElse(0.0);
            
            // Calculate trend direction
            if (data.size() >= 2) {
                int firstValue = Integer.valueOf(data.get(0).get("application_count").toString());
                int lastValue = Integer.valueOf(data.get(data.size() - 1).get("application_count").toString());
                String trend = lastValue > firstValue ? "Increasing" : lastValue < firstValue ? "Decreasing" : "Stable";
                insights.put("trend", trend);
            }
            
            insights.put("peakApplicationPeriod", peakPeriod);
            insights.put("totalApplications", totalApplications);
            insights.put("averageApplicationsPerPeriod", Math.round(avgImpact));
            insights.put("dataPoints", data.size());
            insights.put("summary", String.format("Peak: %s, Total: %,d applications, Avg: %d per period", 
                peakPeriod, totalApplications, Math.round(avgImpact)));
            
        } catch (Exception e) {
            log.warn("Error generating interest rate insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateRiskInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Calculate average risk score
            double avgRiskScore = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("avg_risk_score").toString()))
                .average()
                .orElse(0.0);
            
            // Find highest risk period
            String highestRiskPeriod = data.stream()
                .max(Comparator.comparing(row -> 
                    Double.valueOf(row.get("avg_risk_score").toString())))
                .map(row -> row.get("assessment_date").toString())
                .orElse("N/A");
            
            // Find most common risk category
            String commonRiskCategory = data.stream()
                .collect(Collectors.groupingBy(row -> row.get("risk_category").toString(), 
                    Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            // Calculate risk trend
            if (data.size() >= 2) {
                double firstRisk = Double.valueOf(data.get(0).get("avg_risk_score").toString());
                double lastRisk = Double.valueOf(data.get(data.size() - 1).get("avg_risk_score").toString());
                String riskTrend = lastRisk > firstRisk ? "Increasing" : lastRisk < firstRisk ? "Decreasing" : "Stable";
                insights.put("riskTrend", riskTrend);
            }
            
            insights.put("averageRiskScore", Math.round(avgRiskScore * 100.0) / 100.0);
            insights.put("highestRiskPeriod", highestRiskPeriod);
            insights.put("mostCommonCategory", commonRiskCategory);
            insights.put("assessmentCount", data.size());
            insights.put("summary", String.format("Avg Risk: %.2f, Peak: %s, Common: %s", 
                avgRiskScore, highestRiskPeriod, commonRiskCategory));
            
        } catch (Exception e) {
            log.warn("Error generating risk insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateAuditInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Calculate total events
            int totalEvents = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("event_count").toString()))
                .sum();
            
            // Find most active audit date
            String mostActiveDate = data.stream()
                .max(Comparator.comparing(row -> 
                    Integer.valueOf(row.get("event_count").toString())))
                .map(row -> row.get("audit_date").toString())
                .orElse("N/A");
            
            // Find most common event type
            String commonEventType = data.stream()
                .collect(Collectors.groupingBy(row -> row.get("event_type").toString(), 
                    Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            // Calculate daily average
            double dailyAverage = data.size() > 0 ? (double) totalEvents / data.size() : 0;
            
            // Identify unusual activity days
            double avgEvents = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("event_count").toString()))
                .average()
                .orElse(0.0);
            
            long unusualDays = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("event_count").toString()))
                .filter(count -> count > avgEvents * 2)
                .count();
            
            insights.put("totalAuditEvents", totalEvents);
            insights.put("mostActiveDate", mostActiveDate);
            insights.put("mostCommonEventType", commonEventType);
            insights.put("dailyAverage", Math.round(dailyAverage * 100.0) / 100.0);
            insights.put("unusualActivityDays", unusualDays);
            insights.put("auditDays", data.size());
            insights.put("summary", String.format("Total: %,d events, Peak: %s, Common: %s (Avg: %.1f/day)", 
                totalEvents, mostActiveDate, commonEventType, dailyAverage));
            
        } catch (Exception e) {
            log.warn("Error generating audit insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generatePaymentInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Calculate total payments
            int totalPayments = data.stream()
                .mapToInt(row -> Integer.valueOf(row.get("count").toString()))
                .sum();
            
            // Find most common payment category
            String commonCategory = data.stream()
                .max(Comparator.comparing(row -> 
                    Integer.valueOf(row.get("count").toString())))
                .map(row -> row.get("payment_category").toString())
                .orElse("N/A");
            
            // Calculate on-time payment ratio
            double onTimePayments = data.stream()
                .filter(row -> "ON_TIME".equals(row.get("timeliness_status")))
                .mapToDouble(row -> Double.valueOf(row.get("count").toString()))
                .sum();
            
            double latePayments = data.stream()
                .filter(row -> "LATE".equals(row.get("timeliness_status")))
                .mapToDouble(row -> Double.valueOf(row.get("count").toString()))
                .sum();
            
            double onTimeRatio = totalPayments > 0 ? (onTimePayments / totalPayments) * 100 : 0;
            double lateRatio = totalPayments > 0 ? (latePayments / totalPayments) * 100 : 0;
            
            insights.put("totalPayments", totalPayments);
            insights.put("mostCommonCategory", commonCategory);
            insights.put("onTimePaymentRatio", Math.round(onTimeRatio * 100.0) / 100.0);
            insights.put("latePaymentRatio", Math.round(lateRatio * 100.0) / 100.0);
            insights.put("paymentCategories", data.size());
            insights.put("summary", String.format("Total: %,d payments, Common: %s, On-time: %.1f%%, Late: %.1f%%", 
                totalPayments, commonCategory, onTimeRatio, lateRatio));
            
        } catch (Exception e) {
            log.warn("Error generating payment insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generatePortfolioInsights(List<Map<String, Object>> data, String analysisType) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Calculate total portfolio value
            double totalValue = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get("amount").toString()))
                .sum();
            
            // Find largest category
            String largestCategory = data.stream()
                .max(Comparator.comparing(row -> 
                    Double.valueOf(row.get("amount").toString())))
                .map(row -> row.get("category").toString())
                .orElse("N/A");
            
            // Calculate concentration ratio (top 3 categories)
            double top3Value = data.stream()
                .sorted((a, b) -> Double.compare(
                    Double.valueOf(b.get("amount").toString()),
                    Double.valueOf(a.get("amount").toString())))
                .limit(3)
                .mapToDouble(row -> Double.valueOf(row.get("amount").toString()))
                .sum();
            
            double concentrationRatio = totalValue > 0 ? (top3Value / totalValue) * 100 : 0;
            
            insights.put("totalPortfolioValue", Math.round(totalValue * 100.0) / 100.0);
            insights.put("largestCategory", largestCategory);
            insights.put("numberOfCategories", data.size());
            insights.put("concentrationRatio", Math.round(concentrationRatio * 100.0) / 100.0);
            insights.put("analysisType", analysisType);
            insights.put("summary", String.format("Portfolio: $%,.2f, Largest: %s, Concentration: %.1f%% (top 3)", 
                totalValue, largestCategory, concentrationRatio));
            
        } catch (Exception e) {
            log.warn("Error generating portfolio insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateTrendInsights(List<Map<String, Object>> data, String metric) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.size() < 2) {
                insights.put("summary", "Insufficient data for trend analysis");
                return insights;
            }
            
            // Calculate trend direction
            double firstValue = Double.valueOf(data.get(0).get(metric).toString());
            double lastValue = Double.valueOf(data.get(data.size() - 1).get(metric).toString());
            double trendChange = ((lastValue - firstValue) / firstValue) * 100;
            
            String trendDirection = trendChange > 5 ? "Strong Upward" : 
                                   trendChange > 0 ? "Upward" :
                                   trendChange < -5 ? "Strong Downward" : 
                                   trendChange < 0 ? "Downward" : "Stable";
            
            // Find peak and valley
            double maxValue = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get(metric).toString()))
                .max()
                .orElse(0.0);
            
            double minValue = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get(metric).toString()))
                .min()
                .orElse(0.0);
            
            // Calculate volatility
            double average = data.stream()
                .mapToDouble(row -> Double.valueOf(row.get(metric).toString()))
                .average()
                .orElse(0.0);
            
            double variance = data.stream()
                .mapToDouble(row -> {
                    double value = Double.valueOf(row.get(metric).toString());
                    return Math.pow(value - average, 2);
                })
                .average()
                .orElse(0.0);
            double volatility = Math.sqrt(variance);
            
            insights.put("trendDirection", trendDirection);
            insights.put("trendChange", Math.round(trendChange * 100.0) / 100.0);
            insights.put("peakValue", Math.round(maxValue * 100.0) / 100.0);
            insights.put("valleyValue", Math.round(minValue * 100.0) / 100.0);
            insights.put("volatility", Math.round(volatility * 100.0) / 100.0);
            insights.put("average", Math.round(average * 100.0) / 100.0);
            insights.put("summary", String.format("%s trend (%.1f%% change), Peak: %.2f, Valley: %.2f", 
                trendDirection, trendChange, maxValue, minValue));
            
        } catch (Exception e) {
            log.warn("Error generating trend insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateCorrelationInsights(List<Map<String, Object>> data, List<String> fields) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Find strongest positive and negative correlations
            double maxCorrelation = data.stream()
                .filter(row -> !row.get("field1").equals(row.get("field2")))
                .mapToDouble(row -> Math.abs(Double.valueOf(row.get("correlation").toString())))
                .max()
                .orElse(0.0);
            
            String strongestCorrelation = data.stream()
                .filter(row -> !row.get("field1").equals(row.get("field2")))
                .max(Comparator.comparing(row -> 
                    Math.abs(Double.valueOf(row.get("correlation").toString()))))
                .map(row -> row.get("field1") + " - " + row.get("field2"))
                .orElse("N/A");
            
            // Count significant correlations (|r| > 0.5)
            long significantCorrelations = data.stream()
                .filter(row -> !row.get("field1").equals(row.get("field2")))
                .mapToDouble(row -> Math.abs(Double.valueOf(row.get("correlation").toString())))
                .filter(corr -> corr > 0.5)
                .count();
            
            insights.put("strongestCorrelation", strongestCorrelation);
            insights.put("maxCorrelationValue", Math.round(maxCorrelation * 1000.0) / 1000.0);
            insights.put("significantCorrelations", significantCorrelations);
            insights.put("totalPairs", fields.size() * (fields.size() - 1) / 2);
            insights.put("fieldsAnalyzed", fields);
            insights.put("summary", String.format("Strongest: %s (%.3f), %d significant correlations out of %d pairs", 
                strongestCorrelation, maxCorrelation, significantCorrelations, fields.size() * (fields.size() - 1) / 2));
            
        } catch (Exception e) {
            log.warn("Error generating correlation insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    /**
     * PROFESSIONAL VISUALIZATION TOOLS - Enhanced Charts with Themes
     */
    
    /**
     * Professional Tool: Executive Dashboard
     */
    @Tool(
        name = "generate_professional_executive_dashboard",
        description = "Creates a professional executive KPI dashboard with multi-layer visualization including bars, trend lines, and data labels. Features corporate themes and optimization. Perfect for executive presentations and board meetings."
    )
    public String generateProfessionalExecutiveDashboard(
            @ToolParam(description = "Visual theme: 'corporate', 'executive', 'financial', 'ocean'", required = false) 
            String theme,
            
            @ToolParam(description = "Primary metric: 'total_amount', 'approval_rate', 'application_count', 'avg_processing_days'", required = false) 
            String metricType) {
        
        if (professionalCharts == null) {
            return createMCPErrorResponse("Professional visualization features are not enabled");
        }
        
        try {
            log.info("MCP Tool: generateProfessionalExecutiveDashboard - theme={}, metricType={}", theme, metricType);
            
            theme = theme != null ? theme : "executive";
            metricType = metricType != null ? metricType : "total_amount";
            
            long startTime = System.currentTimeMillis();
            
            // Fetch and optimize data
            List<Map<String, Object>> rawData = financialDataService.getBranchPerformance(metricType);
            List<Map<String, Object>> optimizedData = optimizer != null ? 
                optimizer.optimizeDataForChart(rawData, "executive_dashboard", Map.of("theme", theme)) : rawData;
            
            // Generate professional specification
            Map<String, Object> config = Map.of("theme", theme, "title", "Executive Performance Dashboard");
            Map<String, Object> spec = professionalCharts.generateExecutiveDashboard(optimizedData, config);
            
            // Create MCP response with optimization metrics
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_professional_executive_dashboard");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", "vega-lite");
            mcpResponse.put("data", optimizedData);
            mcpResponse.put("metadata", Map.of(
                "title", "Professional Executive Dashboard",
                "theme", theme,
                "metricType", metricType,
                "dataPoints", optimizedData.size(),
                "generatedAt", System.currentTimeMillis(),
                "chartType", "executive_dashboard",
                "features", "Multi-layer KPI with bars, lines, and labels"
            ));
            
            if (optimizer != null) {
                long processingTime = System.currentTimeMillis() - startTime;
                mcpResponse.put("optimization", optimizer.getOptimizationMetrics(
                    "executive_dashboard", rawData.size(), optimizedData.size(), processingTime));
            }
            
            mcpResponse.put("insights", generateBranchInsights(optimizedData, metricType));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateProfessionalExecutiveDashboard: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate professional executive dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Professional Tool: Gradient Area Chart
     */
    @Tool(
        name = "generate_professional_gradient_area_chart",
        description = "Creates beautiful gradient area charts for time series analysis with smooth interpolation and professional styling. Features theme-based gradients and performance optimization. Ideal for revenue trends and financial metrics."
    )
    public String generateProfessionalGradientAreaChart(
            @ToolParam(description = "Analysis period: '90d', '180d', '365d', '730d'", required = false) 
            String timeframe,
            
            @ToolParam(description = "Visual theme: 'gradient_blue', 'gradient_green', 'corporate', 'ocean'", required = false) 
            String theme) {
        
        if (professionalCharts == null) {
            return createMCPErrorResponse("Professional visualization features are not enabled");
        }
        
        try {
            log.info("MCP Tool: generateProfessionalGradientAreaChart - timeframe={}, theme={}", timeframe, theme);
            
            timeframe = timeframe != null ? timeframe : "180d";
            theme = theme != null ? theme : "gradient_blue";
            
            long startTime = System.currentTimeMillis();
            
            // Fetch time series data
            List<Map<String, Object>> rawData = financialDataService.getInterestRateImpact(timeframe);
            List<Map<String, Object>> optimizedData = optimizer != null ? 
                optimizer.optimizeDataForChart(rawData, "gradient_area", Map.of("theme", theme)) : rawData;
            
            // Generate gradient area specification
            Map<String, Object> config = Map.of(
                "theme", theme, 
                "title", "Revenue Trend Analysis", 
                "xField", "month", 
                "yField", "avg_loan_amount");
            Map<String, Object> spec = professionalCharts.generateGradientAreaChart(optimizedData, config);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_professional_gradient_area_chart");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", "vega-lite");
            mcpResponse.put("data", optimizedData);
            mcpResponse.put("metadata", Map.of(
                "title", "Professional Gradient Area Chart",
                "theme", theme,
                "timeframe", timeframe,
                "dataPoints", optimizedData.size(),
                "generatedAt", System.currentTimeMillis(),
                "chartType", "gradient_area",
                "features", "Smooth gradients with cardinal interpolation"
            ));
            
            if (optimizer != null) {
                long processingTime = System.currentTimeMillis() - startTime;
                mcpResponse.put("optimization", optimizer.getOptimizationMetrics(
                    "gradient_area", rawData.size(), optimizedData.size(), processingTime));
            }
            
            mcpResponse.put("insights", generateInterestRateInsights(optimizedData));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateProfessionalGradientAreaChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate professional gradient area chart: " + e.getMessage());
        }
    }
    
    /**
     * Professional Tool: Interactive Heatmap
     */
    @Tool(
        name = "generate_professional_interactive_heatmap",
        description = "Creates interactive risk-performance heatmaps with professional color scales and detailed tooltips. Shows ROI by risk level and loan product with advanced interactivity. Perfect for portfolio risk analysis and investment decisions."
    )
    public String generateProfessionalInteractiveHeatmap(
            @ToolParam(description = "Visual theme: 'corporate', 'financial', 'executive'", required = false) 
            String theme) {
        
        if (professionalCharts == null) {
            return createMCPErrorResponse("Professional visualization features are not enabled");
        }
        
        try {
            log.info("MCP Tool: generateProfessionalInteractiveHeatmap - theme={}", theme);
            
            theme = theme != null ? theme : "corporate";
            
            // Generate sample heatmap data (in production, this would come from risk analysis)
            List<Map<String, Object>> heatmapData = generateSampleHeatmapData();
            
            Map<String, Object> config = Map.of(
                "theme", theme, 
                "title", "Portfolio Risk-Return Matrix");
            Map<String, Object> spec = professionalCharts.generateInteractiveHeatmap(heatmapData, config);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_professional_interactive_heatmap");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", "vega-lite");
            mcpResponse.put("data", heatmapData);
            mcpResponse.put("metadata", Map.of(
                "title", "Professional Interactive Heatmap",
                "theme", theme,
                "dataPoints", heatmapData.size(),
                "generatedAt", System.currentTimeMillis(),
                "chartType", "interactive_heatmap",
                "features", "Interactive tooltips with risk-return analysis"
            ));
            
            mcpResponse.put("insights", generateHeatmapInsights(heatmapData));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateProfessionalInteractiveHeatmap: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate professional interactive heatmap: " + e.getMessage());
        }
    }
    
    /**
     * Professional Tool: Financial Candlestick Chart
     */
    @Tool(
        name = "generate_professional_candlestick_chart",
        description = "Creates professional financial candlestick charts with proper wicks, bodies, and volume analysis. Features OHLC data visualization with financial themes. Essential for rate analysis and market trend visualization."
    )
    public String generateProfessionalCandlestickChart(
            @ToolParam(description = "Analysis period: '30d', '90d', '180d', '365d'", required = false) 
            String timeframe,
            
            @ToolParam(description = "Visual theme: 'financial', 'corporate', 'executive'", required = false) 
            String theme) {
        
        if (professionalCharts == null) {
            return createMCPErrorResponse("Professional visualization features are not enabled");
        }
        
        try {
            log.info("MCP Tool: generateProfessionalCandlestickChart - timeframe={}, theme={}", timeframe, theme);
            
            timeframe = timeframe != null ? timeframe : "90d";
            theme = theme != null ? theme : "financial";
            
            // Generate sample OHLC data (in production, this would come from rate data)
            List<Map<String, Object>> candlestickData = generateSampleCandlestickData(timeframe);
            
            Map<String, Object> config = Map.of(
                "theme", theme, 
                "title", "Loan Rate Analysis - OHLC");
            Map<String, Object> spec = professionalCharts.generateCandlestickChart(candlestickData, config);
            
            Map<String, Object> mcpResponse = new HashMap<>();
            mcpResponse.put("success", true);
            mcpResponse.put("toolName", "generate_professional_candlestick_chart");
            mcpResponse.put("specification", spec);
            mcpResponse.put("framework", "vega-lite");
            mcpResponse.put("data", candlestickData);
            mcpResponse.put("metadata", Map.of(
                "title", "Professional Financial Candlestick Chart",
                "theme", theme,
                "timeframe", timeframe,
                "dataPoints", candlestickData.size(),
                "generatedAt", System.currentTimeMillis(),
                "chartType", "candlestick",
                "features", "OHLC with wicks, bodies, and volume"
            ));
            
            mcpResponse.put("insights", generateCandlestickInsights(candlestickData));
            
            return objectMapper.writeValueAsString(mcpResponse);
            
        } catch (Exception e) {
            log.error("MCP Tool Error in generateProfessionalCandlestickChart: {}", e.getMessage(), e);
            return createMCPErrorResponse("Failed to generate professional candlestick chart: " + e.getMessage());
        }
    }
    
    // Helper methods for sample data generation
    
    private List<Map<String, Object>> generateSampleHeatmapData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] riskLevels = {"Low", "Medium", "High"};
        String[] loanTypes = {"Personal", "Home", "Auto", "Business"};
        java.util.Random random = new java.util.Random(42);
        
        for (String risk : riskLevels) {
            for (String loan : loanTypes) {
                Map<String, Object> cell = new HashMap<>();
                cell.put("risk_category", risk);
                cell.put("loan_type", loan);
                cell.put("roi_percentage", -5 + random.nextDouble() * 25); // -5% to 20%
                cell.put("volume", 100 + random.nextInt(900));
                data.add(cell);
            }
        }
        return data;
    }
    
    private List<Map<String, Object>> generateSampleCandlestickData(String timeframe) {
        List<Map<String, Object>> data = new ArrayList<>();
        java.util.Random random = new java.util.Random(42);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -90);
        
        double baseRate = 5.5;
        
        for (int i = 0; i < 90; i++) {
            Map<String, Object> candle = new HashMap<>();
            candle.put("date", cal.getTime());
            
            double open = baseRate + random.nextGaussian() * 0.1;
            double close = open + random.nextGaussian() * 0.2;
            double high = Math.max(open, close) + random.nextDouble() * 0.15;
            double low = Math.min(open, close) - random.nextDouble() * 0.15;
            
            candle.put("open", Math.round(open * 100.0) / 100.0);
            candle.put("close", Math.round(close * 100.0) / 100.0);
            candle.put("high", Math.round(high * 100.0) / 100.0);
            candle.put("low", Math.round(low * 100.0) / 100.0);
            candle.put("volume", 1000 + random.nextInt(5000));
            
            data.add(candle);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            baseRate = close; // Trend continuation
        }
        return data;
    }
    
    private Map<String, Object> generateHeatmapInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            java.util.Optional<Map<String, Object>> bestCell = data.stream()
                .max(java.util.Comparator.comparing(d -> ((Number) d.get("roi_percentage")).doubleValue()));
            
            if (bestCell.isPresent()) {
                insights.put("bestPerformingSegment", bestCell.get().get("loan_type") + " - " + bestCell.get().get("risk_category"));
                insights.put("bestRoi", bestCell.get().get("roi_percentage"));
            }
            
            insights.put("totalSegments", data.size());
            insights.put("summary", "Heatmap shows risk-return relationships across loan products");
            
        } catch (Exception e) {
            log.warn("Error generating heatmap insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateCandlestickInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (!data.isEmpty()) {
                double avgVolume = data.stream()
                    .mapToDouble(d -> ((Number) d.get("volume")).doubleValue())
                    .average()
                    .orElse(0.0);
                
                long bullishCandles = data.stream()
                    .filter(d -> ((Number) d.get("close")).doubleValue() > ((Number) d.get("open")).doubleValue())
                    .count();
                
                insights.put("averageVolume", Math.round(avgVolume));
                insights.put("bullishCandlesPercent", Math.round((double) bullishCandles / data.size() * 100.0));
                insights.put("totalCandles", data.size());
                insights.put("summary", String.format("%.0f%% bullish candles with avg volume of %,.0f", 
                    (double) bullishCandles / data.size() * 100.0, avgVolume));
            }
        } catch (Exception e) {
            log.warn("Error generating candlestick insights", e);
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private String createMCPErrorResponse(String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", message);
            error.put("timestamp", System.currentTimeMillis());
            error.put("toolType", "FinancialVisualizationMCPTools");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating MCP error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}
