package com.deepai.mcpserver.tools.declarative;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.deepai.mcpserver.service.declarative.DeclarativeSpecGenerator;
import com.deepai.mcpserver.service.financial.FinancialDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * MCP Tools for generating declarative visualization specifications
 * Supports Vega-Lite and Plotly for financial domain analysis
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true")
public class DeclarativeVisualizationTools {
    
    @Autowired
    private FinancialDataService financialDataService;
    
    @Autowired
    private DeclarativeSpecGenerator specGenerator;
    
    @Autowired
    private VisualizationProperties properties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Generate Loan Product Popularity Visualization
     */
    public String generateLoanPopularityChart(String timeframe, String framework, String chartType) {
        try {
            log.info("Generating loan popularity chart - timeframe: {}, framework: {}", timeframe, framework);
            
            // Validate inputs
            timeframe = timeframe != null ? timeframe : "30d";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "bar";
            
            // Fetch data asynchronously for better performance
            CompletableFuture<List<Map<String, Object>>> dataFuture = 
                CompletableFuture.supplyAsync(() -> financialDataService.getLoanProductPopularity(timeframe));
            
            List<Map<String, Object>> data = dataFuture.get();
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No loan data available for the specified timeframe");
            }
            
            // Generate visualization specification
            Map<String, Object> spec = specGenerator.generateLoanPopularitySpec(data, framework);
            
            // Create comprehensive response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Loan Product Popularity Analysis",
                "timeframe", timeframe,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "loan_applications",
                "description", "Shows loan application counts by product type"
            ));
            response.put("insights", generateLoanInsights(data));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating loan popularity chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate loan popularity chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Branch Performance Visualization
     */
    public String generateBranchPerformanceChart(String metricType, String framework, String chartType) {
        try {
            log.info("Generating branch performance chart - metric: {}, framework: {}", metricType, framework);
            
            metricType = metricType != null ? metricType : "total_amount";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "bar";
            
            List<Map<String, Object>> data = financialDataService.getBranchPerformance(metricType);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No branch performance data available");
            }
            
            Map<String, Object> spec = specGenerator.generateBranchPerformanceSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Branch Lending Performance Analysis",
                "metricType", metricType,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "branches, loan_applications",
                "description", "Compares branch performance across multiple metrics"
            ));
            response.put("insights", generateBranchInsights(data, metricType));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating branch performance chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate branch performance chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Customer Segmentation Visualization
     */
    public String generateCustomerSegmentationChart(String segmentBy, String framework, String chartType) {
        try {
            log.info("Generating customer segmentation chart - segmentBy: {}, framework: {}", segmentBy, framework);
            
            segmentBy = segmentBy != null ? segmentBy : "credit_score";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "scatter";
            
            List<Map<String, Object>> data = financialDataService.getCustomerSegmentation(segmentBy);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No customer segmentation data available");
            }
            
            Map<String, Object> spec = specGenerator.generateCustomerSegmentationSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Customer Segmentation Analysis",
                "segmentBy", segmentBy,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "customers, loan_applications, risk_assessments",
                "description", "Customer distribution analysis for risk and value assessment"
            ));
            response.put("insights", generateCustomerInsights(data));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating customer segmentation chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate customer segmentation chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Interest Rate Impact Visualization
     */
    public String generateInterestRateImpactChart(String timeframe, String framework, String chartType) {
        try {
            log.info("Generating interest rate impact chart - timeframe: {}, framework: {}", timeframe, framework);
            
            timeframe = timeframe != null ? timeframe : "365d";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "line";
            
            List<Map<String, Object>> data = financialDataService.getInterestRateImpact(timeframe);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No interest rate impact data available");
            }
            
            Map<String, Object> spec = specGenerator.generateInterestRateImpactSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Interest Rate Impact Analysis",
                "timeframe", timeframe,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "loan_applications",
                "description", "Analysis of how interest rate changes affect loan applications"
            ));
            response.put("insights", generateInterestRateInsights(data));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating interest rate impact chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate interest rate impact chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Risk Assessment Trends Visualization
     */
    public String generateRiskAssessmentTrendsChart(String timeframe, String framework, String chartType) {
        try {
            log.info("Generating risk assessment trends chart - timeframe: {}, framework: {}", timeframe, framework);
            
            timeframe = timeframe != null ? timeframe : "90d";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "line";
            
            List<Map<String, Object>> data = financialDataService.getRiskAssessmentTrends(timeframe);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No risk assessment trends data available");
            }
            
            Map<String, Object> spec = specGenerator.generateRiskAssessmentTrendsSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Risk Assessment Trends Analysis",
                "timeframe", timeframe,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "risk_assessments, loan_applications",
                "description", "Trends in risk scores and recommendations over time"
            ));
            response.put("insights", generateRiskInsights(data));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating risk assessment trends chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate risk assessment trends chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Audit Compliance Visualization
     */
    public String generateAuditComplianceChart(String timeframe, String analysisType, String framework) {
        try {
            log.info("Generating audit compliance chart - timeframe: {}, analysisType: {}", timeframe, analysisType);
            
            timeframe = timeframe != null ? timeframe : "7d";
            analysisType = analysisType != null ? analysisType : "activity";
            framework = framework != null ? framework : properties.getDefaultFramework();
            
            List<Map<String, Object>> data = financialDataService.getAuditCompliance(timeframe, analysisType);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No audit compliance data available");
            }
            
            Map<String, Object> spec = specGenerator.generateAuditComplianceSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", "line",
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Audit & Compliance Analysis",
                "timeframe", timeframe,
                "analysisType", analysisType,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "audit_logs",
                "description", "Database activity monitoring for compliance and security"
            ));
            response.put("insights", generateAuditInsights(data));
            response.put("securityNote", "Sensitive data filtered according to security policies");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating audit compliance chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate audit compliance chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Payment Behavior Visualization
     */
    public String generatePaymentBehaviorChart(String analysisType, String framework, String chartType) {
        try {
            log.info("Generating payment behavior chart - analysisType: {}, framework: {}", analysisType, framework);
            
            analysisType = analysisType != null ? analysisType : "timeliness";
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "bar";
            
            List<Map<String, Object>> data = financialDataService.getPaymentBehavior(analysisType);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No payment behavior data available");
            }
            
            Map<String, Object> spec = specGenerator.generatePaymentBehaviorSpec(data, framework);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", "Payment Behavior Analysis",
                "analysisType", analysisType,
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "payments, loan_applications",
                "description", "Payment timeliness and behavior patterns by loan type"
            ));
            response.put("insights", generatePaymentInsights(data));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating payment behavior chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate payment behavior chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate Custom Financial Analysis Chart
     */
    public String generateCustomFinancialChart(String query, String chartType, String framework, 
                                              Map<String, String> encoding, Map<String, Object> config) {
        try {
            log.info("Generating custom financial chart - chartType: {}, framework: {}", chartType, framework);
            
            // Security validation
            if (!isQueryAllowed(query)) {
                return createErrorResponse("Query not allowed due to security restrictions");
            }
            
            framework = framework != null ? framework : properties.getDefaultFramework();
            chartType = chartType != null ? chartType : "bar";
            
            List<Map<String, Object>> data = financialDataService.executeCustomQuery(query, null);
            
            if (data.isEmpty() || (data.size() == 1 && data.get(0).containsKey("error"))) {
                return createErrorResponse("No data returned from custom query");
            }
            
            // Generate specification
            Map<String, Object> spec;
            if ("plotly".equalsIgnoreCase(framework)) {
                spec = specGenerator.generatePlotlySpec(chartType, data, encoding, config);
            } else {
                spec = specGenerator.generateVegaLiteSpec(chartType, data, encoding, config);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visualization", Map.of(
                "spec", spec,
                "framework", framework,
                "chartType", chartType,
                "dataPoints", data.size()
            ));
            response.put("data", data);
            response.put("metadata", Map.of(
                "title", config.getOrDefault("title", "Custom Financial Analysis"),
                "generatedAt", System.currentTimeMillis(),
                "dataSource", "custom_query",
                "description", config.getOrDefault("description", "Custom financial data analysis")
            ));
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            log.error("Error generating custom financial chart: {}", e.getMessage(), e);
            return createErrorResponse("Failed to generate custom financial chart: " + e.getMessage());
        }
    }
    
    // Insight Generation Methods
    private Map<String, Object> generateLoanInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Find most popular loan type
            Optional<Map<String, Object>> mostPopular = data.stream()
                .max(Comparator.comparing(row -> ((Number) row.get("COUNT")).intValue()));
            
            if (mostPopular.isPresent()) {
                insights.put("mostPopularLoanType", mostPopular.get().get("LOAN_TYPE"));
                insights.put("mostPopularCount", mostPopular.get().get("COUNT"));
            }
            
            // Calculate total applications
            int totalApplications = data.stream()
                .mapToInt(row -> ((Number) row.get("COUNT")).intValue())
                .sum();
            insights.put("totalApplications", totalApplications);
            
            // Calculate average loan amount
            double avgAmount = data.stream()
                .filter(row -> row.get("AVG_AMOUNT") != null)
                .mapToDouble(row -> ((Number) row.get("AVG_AMOUNT")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("averageLoanAmount", avgAmount);
            
            insights.put("loanTypeCount", data.size());
            
        } catch (Exception e) {
            log.warn("Error generating loan insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateBranchInsights(List<Map<String, Object>> data, String metricType) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Find top performing branch
            Optional<Map<String, Object>> topBranch = data.stream()
                .max(Comparator.comparing(row -> ((Number) row.getOrDefault("TOTAL_AMOUNT", 0)).doubleValue()));
            
            if (topBranch.isPresent()) {
                insights.put("topPerformingBranch", topBranch.get().get("BRANCH_NAME"));
                insights.put("topBranchAmount", topBranch.get().get("TOTAL_AMOUNT"));
            }
            
            // Calculate average approval rate
            double avgApprovalRate = data.stream()
                .filter(row -> row.get("APPROVAL_RATE") != null)
                .mapToDouble(row -> ((Number) row.get("APPROVAL_RATE")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("averageApprovalRate", Math.round(avgApprovalRate * 100.0) / 100.0);
            
            // Find branch with highest approval rate
            Optional<Map<String, Object>> bestApprovalBranch = data.stream()
                .filter(row -> row.get("APPROVAL_RATE") != null)
                .max(Comparator.comparing(row -> ((Number) row.get("APPROVAL_RATE")).doubleValue()));
            
            if (bestApprovalBranch.isPresent()) {
                insights.put("bestApprovalRateBranch", bestApprovalBranch.get().get("BRANCH_NAME"));
                insights.put("bestApprovalRate", bestApprovalBranch.get().get("APPROVAL_RATE"));
            }
            
            insights.put("totalBranches", data.size());
            
        } catch (Exception e) {
            log.warn("Error generating branch insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateCustomerInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Calculate credit score distribution
            Map<String, Long> creditDistribution = data.stream()
                .filter(row -> row.get("CREDIT_CATEGORY") != null)
                .collect(Collectors.groupingBy(
                    row -> (String) row.get("CREDIT_CATEGORY"),
                    Collectors.counting()
                ));
            insights.put("creditScoreDistribution", creditDistribution);
            
            // Calculate average income by credit category
            Map<String, Double> avgIncomeByCredit = data.stream()
                .filter(row -> row.get("CREDIT_CATEGORY") != null && row.get("ANNUAL_INCOME") != null)
                .collect(Collectors.groupingBy(
                    row -> (String) row.get("CREDIT_CATEGORY"),
                    Collectors.averagingDouble(row -> ((Number) row.get("ANNUAL_INCOME")).doubleValue())
                ));
            insights.put("averageIncomeByCredit", avgIncomeByCredit);
            
            // Find high-value customers
            long highValueCustomers = data.stream()
                .filter(row -> row.get("TOTAL_LOAN_AMOUNT") != null)
                .filter(row -> ((Number) row.get("TOTAL_LOAN_AMOUNT")).doubleValue() > 100000)
                .count();
            insights.put("highValueCustomers", highValueCustomers);
            
            insights.put("totalCustomers", data.size());
            
        } catch (Exception e) {
            log.warn("Error generating customer insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateInterestRateInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Calculate average interest rate
            double avgInterestRate = data.stream()
                .filter(row -> row.get("AVG_INTEREST_RATE") != null)
                .mapToDouble(row -> ((Number) row.get("AVG_INTEREST_RATE")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("averageInterestRate", Math.round(avgInterestRate * 10000.0) / 10000.0);
            
            // Find month with highest applications
            Optional<Map<String, Object>> peakMonth = data.stream()
                .max(Comparator.comparing(row -> ((Number) row.get("APPLICATION_COUNT")).intValue()));
            
            if (peakMonth.isPresent()) {
                insights.put("peakApplicationMonth", peakMonth.get().get("MONTH"));
                insights.put("peakApplicationCount", peakMonth.get().get("APPLICATION_COUNT"));
            }
            
            // Calculate correlation strength
            OptionalDouble avgCorrelation = data.stream()
                .filter(row -> row.get("RATE_AMOUNT_CORRELATION") != null)
                .mapToDouble(row -> ((Number) row.get("RATE_AMOUNT_CORRELATION")).doubleValue())
                .average();
            
            if (avgCorrelation.isPresent()) {
                insights.put("rateAmountCorrelation", Math.round(avgCorrelation.getAsDouble() * 1000.0) / 1000.0);
            }
            
        } catch (Exception e) {
            log.warn("Error generating interest rate insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateRiskInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Calculate average risk score
            double avgRiskScore = data.stream()
                .filter(row -> row.get("AVG_RISK_SCORE") != null)
                .mapToDouble(row -> ((Number) row.get("AVG_RISK_SCORE")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("averageRiskScore", Math.round(avgRiskScore * 100.0) / 100.0);
            
            // Calculate recommendation distribution
            Map<String, Integer> recommendationCounts = new HashMap<>();
            for (Map<String, Object> row : data) {
                if (row.get("APPROVE_RECOMMENDATIONS") != null) {
                    recommendationCounts.merge("APPROVE", 
                        ((Number) row.get("APPROVE_RECOMMENDATIONS")).intValue(), Integer::sum);
                }
                if (row.get("REJECT_RECOMMENDATIONS") != null) {
                    recommendationCounts.merge("REJECT", 
                        ((Number) row.get("REJECT_RECOMMENDATIONS")).intValue(), Integer::sum);
                }
                if (row.get("REVIEW_RECOMMENDATIONS") != null) {
                    recommendationCounts.merge("REVIEW", 
                        ((Number) row.get("REVIEW_RECOMMENDATIONS")).intValue(), Integer::sum);
                }
            }
            insights.put("recommendationDistribution", recommendationCounts);
            
            // Find highest risk day
            Optional<Map<String, Object>> highestRiskDay = data.stream()
                .max(Comparator.comparing(row -> ((Number) row.get("AVG_RISK_SCORE")).doubleValue()));
            
            if (highestRiskDay.isPresent()) {
                insights.put("highestRiskDate", highestRiskDay.get().get("ASSESSMENT_DATE"));
                insights.put("highestRiskScore", highestRiskDay.get().get("AVG_RISK_SCORE"));
            }
            
        } catch (Exception e) {
            log.warn("Error generating risk insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generateAuditInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Calculate total changes
            int totalChanges = data.stream()
                .mapToInt(row -> ((Number) row.get("CHANGE_COUNT")).intValue())
                .sum();
            insights.put("totalChanges", totalChanges);
            
            // Find most active table
            Optional<Map<String, Object>> mostActiveTable = data.stream()
                .max(Comparator.comparing(row -> ((Number) row.get("CHANGE_COUNT")).intValue()));
            
            if (mostActiveTable.isPresent()) {
                insights.put("mostActiveTable", mostActiveTable.get().get("TABLE_NAME"));
                insights.put("mostActiveTableChanges", mostActiveTable.get().get("CHANGE_COUNT"));
            }
            
            // Calculate unique users
            Set<String> uniqueUsers = data.stream()
                .map(row -> (String) row.get("USERNAME"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            insights.put("uniqueUsers", uniqueUsers.size());
            
            // Find high activity periods
            long highActivityPeriods = data.stream()
                .filter(row -> "HIGH_ACTIVITY".equals(row.get("ACTIVITY_LEVEL")))
                .count();
            insights.put("highActivityPeriods", highActivityPeriods);
            
        } catch (Exception e) {
            log.warn("Error generating audit insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    private Map<String, Object> generatePaymentInsights(List<Map<String, Object>> data) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            if (data.isEmpty()) return insights;
            
            // Calculate on-time payment percentage
            double onTimePercentage = data.stream()
                .filter(row -> "ON_TIME".equals(row.get("PAYMENT_CATEGORY")))
                .mapToDouble(row -> ((Number) row.get("PERCENTAGE")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("onTimePaymentPercentage", Math.round(onTimePercentage * 100.0) / 100.0);
            
            // Calculate total late fees
            double totalLateFees = data.stream()
                .filter(row -> row.get("TOTAL_LATE_FEES") != null)
                .mapToDouble(row -> ((Number) row.get("TOTAL_LATE_FEES")).doubleValue())
                .sum();
            insights.put("totalLateFees", totalLateFees);
            
            // Find loan type with most late payments
            Optional<Map<String, Object>> worstPaymentType = data.stream()
                .filter(row -> !"ON_TIME".equals(row.get("PAYMENT_CATEGORY")))
                .max(Comparator.comparing(row -> ((Number) row.get("PAYMENT_COUNT")).intValue()));
            
            if (worstPaymentType.isPresent()) {
                insights.put("worstPaymentLoanType", worstPaymentType.get().get("LOAN_TYPE"));
                insights.put("worstPaymentCategory", worstPaymentType.get().get("PAYMENT_CATEGORY"));
            }
            
            // Calculate average delay
            double avgDelay = data.stream()
                .filter(row -> row.get("AVG_DELAY_DAYS") != null)
                .mapToDouble(row -> ((Number) row.get("AVG_DELAY_DAYS")).doubleValue())
                .average()
                .orElse(0.0);
            insights.put("averageDelayDays", Math.round(avgDelay * 100.0) / 100.0);
            
        } catch (Exception e) {
            log.warn("Error generating payment insights: {}", e.getMessage());
            insights.put("error", "Unable to generate insights");
        }
        
        return insights;
    }
    
    // Security validation
    private boolean isQueryAllowed(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        if (!properties.getSecurity().isSqlInjectionProtection()) {
            return true;
        }
        
        String upperQuery = query.toUpperCase();
        
        // Check for dangerous operations
        List<String> dangerousOperations = Arrays.asList(
            "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE",
            "GRANT", "REVOKE", "EXEC", "EXECUTE", "CALL", "MERGE"
        );
        
        for (String operation : dangerousOperations) {
            if (upperQuery.contains(operation)) {
                log.warn("Query blocked due to dangerous operation: {}", operation);
                return false;
            }
        }
        
        // Check for system tables
        Pattern blockedPattern = Pattern.compile(properties.getSecurity().getBlockedTablesPattern());
        if (blockedPattern.matcher(upperQuery).find()) {
            log.warn("Query blocked due to restricted table access");
            return false;
        }
        
        return true;
    }
    
    private String createErrorResponse(String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", message);
            error.put("timestamp", System.currentTimeMillis());
            error.put("tool", "DeclarativeVisualizationTools");
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"success\":false,\"error\":\"Failed to create error response\"}";
        }
    }
}