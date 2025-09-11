package com.deepai.mcpserver.vtools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.util.DeclarativeSpecGenerator;
import com.deepai.mcpserver.vservice.FinancialDataService;
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
    
    /**
     * Tool 1: Generate Loan Product Popularity Chart
     * Returns Vega-Lite or Plotly JSON specification
     */
    @Tool(
        name = "oracle_generate_loan_popularity_chart",
        description = "Generate loan product popularity visualization with Vega-Lite or Plotly JSON specification. Shows which loan types are most frequently used and analyzes trends over time."
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
            mcpResponse.put("toolName", "oracle_generate_loan_popularity_chart");
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
        name = "oracle_generate_branch_performance_chart",
        description = "Generate branch lending performance visualization comparing loan amounts, approval rates, and processing times across branches."
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
            mcpResponse.put("toolName", "oracle_generate_branch_performance_chart");
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
        name = "oracle_generate_customer_segmentation_chart",
        description = "Generate customer segmentation analysis with scatter plots showing credit scores, income, and risk categories for identifying high-value segments."
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
            mcpResponse.put("toolName", "oracle_generate_customer_segmentation_chart");
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
        name = "oracle_generate_interest_rate_impact_chart",
        description = "Analyze how interest rate changes affect loan applications and repayment behavior with time series and correlation analysis."
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
            mcpResponse.put("toolName", "oracle_generate_interest_rate_impact_chart");
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
        name = "oracle_generate_risk_assessment_trends_chart",
        description = "Track risk scores and recommended actions over time, identifying patterns in risk categories and outcomes."
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
            mcpResponse.put("toolName", "oracle_generate_risk_assessment_trends_chart");
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
        name = "oracle_generate_audit_compliance_chart",
        description = "Monitor audit log activity for unusual changes and compliance tracking, analyzing database changes over time for security monitoring."
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
            mcpResponse.put("toolName", "oracle_generate_audit_compliance_chart");
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
        name = "oracle_generate_payment_behavior_chart",
        description = "Analyze payment timeliness, late fees, and repayment patterns to identify customers or loan types with frequent late payments."
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
            mcpResponse.put("toolName", "oracle_generate_payment_behavior_chart");
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
        name = "oracle_generate_portfolio_analysis_chart",
        description = "Analyze loan portfolio composition, risk distribution, and performance metrics across different loan categories and customer segments."
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
            mcpResponse.put("toolName", "oracle_generate_portfolio_analysis_chart");
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
        name = "oracle_generate_trend_analysis_chart",
        description = "Generate time series trend analysis for loan applications, approvals, defaults, and financial metrics over specified time periods."
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
            mcpResponse.put("toolName", "oracle_generate_trend_analysis_chart");
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
        name = "oracle_generate_correlation_analysis_chart",
        description = "Generate correlation matrix and analysis between different financial metrics, customer attributes, and loan performance indicators."
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
            mcpResponse.put("toolName", "oracle_generate_correlation_analysis_chart");
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