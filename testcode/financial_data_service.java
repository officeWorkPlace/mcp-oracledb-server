package com.deepai.mcpserver.service.financial;

import com.deepai.mcpserver.config.VisualizationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for executing financial domain-specific queries
 * Supports loan, branch, customer, risk, audit, and payment analysis
 */
@Service
@Slf4j
public class FinancialDataService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private VisualizationProperties properties;
    
    private static final Pattern SAFE_TABLE_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    /**
     * 1. Loan Product Popularity Analysis
     */
    @Cacheable(value = "loanAnalytics", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getLoanProductPopularity(String timeframe) {
        try {
            validateTimeframe(timeframe);
            
            String sql = """
                SELECT 
                    TO_CHAR(ra.assessment_date, 'YYYY-MM-DD') as assessment_date,
                    ra.risk_category,
                    AVG(ra.risk_score) as avg_risk_score,
                    COUNT(*) as assessment_count,
                    COUNT(CASE WHEN ra.recommended_action = 'APPROVE' THEN 1 END) as approve_recommendations,
                    COUNT(CASE WHEN ra.recommended_action = 'REJECT' THEN 1 END) as reject_recommendations,
                    COUNT(CASE WHEN ra.recommended_action = 'REVIEW' THEN 1 END) as review_recommendations,
                    AVG(la.loan_amount) as avg_loan_amount_assessed
                FROM risk_assessments ra
                JOIN loan_applications la ON ra.application_id = la.application_id
                WHERE ra.assessment_date >= SYSDATE - INTERVAL '%s' DAY
                GROUP BY TO_CHAR(ra.assessment_date, 'YYYY-MM-DD'), ra.risk_category
                ORDER BY assessment_date, risk_category
                """.formatted(getTimeframeDays(timeframe));
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            log.info("Retrieved {} risk assessment trend records", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving risk assessment trends: {}", e.getMessage());
            return createErrorData("Failed to retrieve risk assessment trends");
        }
    }
    
    /**
     * 6. Audit and Compliance Analysis
     */
    @Cacheable(value = "auditLogs", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getAuditCompliance(String timeframe, String analysisType) {
        try {
            validateTimeframe(timeframe);
            validateAnalysisType(analysisType);
            
            String sql = """
                SELECT 
                    TO_CHAR(al.audit_timestamp, 'YYYY-MM-DD HH24') as audit_hour,
                    al.table_name,
                    al.operation_type,
                    al.username,
                    COUNT(*) as change_count,
                    COUNT(DISTINCT al.username) as unique_users,
                    COUNT(CASE WHEN al.operation_type = 'UPDATE' THEN 1 END) as update_count,
                    COUNT(CASE WHEN al.operation_type = 'DELETE' THEN 1 END) as delete_count,
                    COUNT(CASE WHEN al.operation_type = 'INSERT' THEN 1 END) as insert_count,
                    CASE 
                        WHEN COUNT(*) > 100 THEN 'HIGH_ACTIVITY'
                        WHEN COUNT(*) > 50 THEN 'MEDIUM_ACTIVITY'
                        ELSE 'NORMAL_ACTIVITY'
                    END as activity_level
                FROM audit_logs al
                WHERE al.audit_timestamp >= SYSDATE - INTERVAL '%s' DAY
                AND al.table_name NOT LIKE 'SYS_%%'
                GROUP BY TO_CHAR(al.audit_timestamp, 'YYYY-MM-DD HH24'), 
                         al.table_name, al.operation_type, al.username
                ORDER BY audit_hour DESC, change_count DESC
                """.formatted(getTimeframeDays(timeframe));
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            // Apply security filtering and limits
            results = results.stream()
                .filter(this::isAuditDataAllowed)
                .limit(properties.getMaxDataPoints())
                .toList();
            
            log.info("Retrieved {} audit compliance records", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving audit compliance data: {}", e.getMessage());
            return createErrorData("Failed to retrieve audit compliance data");
        }
    }
    
    /**
     * 7. Payment Behavior Analysis
     */
    @Cacheable(value = "paymentBehavior", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getPaymentBehavior(String analysisType) {
        try {
            validatePaymentAnalysisType(analysisType);
            
            String sql = """
                SELECT 
                    la.loan_type,
                    CASE 
                        WHEN p.payment_date <= p.due_date THEN 'ON_TIME'
                        WHEN p.payment_date <= p.due_date + INTERVAL '7' DAY THEN 'SLIGHTLY_LATE'
                        WHEN p.payment_date <= p.due_date + INTERVAL '30' DAY THEN 'LATE'
                        ELSE 'VERY_LATE'
                    END as payment_category,
                    COUNT(*) as payment_count,
                    COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(PARTITION BY la.loan_type) as percentage,
                    AVG(p.payment_amount) as avg_payment_amount,
                    SUM(p.late_fee) as total_late_fees,
                    AVG(CASE WHEN p.payment_date > p.due_date 
                        THEN p.payment_date - p.due_date ELSE 0 END) as avg_delay_days,
                    COUNT(DISTINCT p.loan_id) as unique_loans,
                    COUNT(DISTINCT la.customer_id) as unique_customers
                FROM payments p
                JOIN loan_applications la ON p.loan_id = la.loan_id
                WHERE p.payment_date >= SYSDATE - INTERVAL '365' DAY
                GROUP BY la.loan_type, 
                    CASE 
                        WHEN p.payment_date <= p.due_date THEN 'ON_TIME'
                        WHEN p.payment_date <= p.due_date + INTERVAL '7' DAY THEN 'SLIGHTLY_LATE'
                        WHEN p.payment_date <= p.due_date + INTERVAL '30' DAY THEN 'LATE'
                        ELSE 'VERY_LATE'
                    END
                ORDER BY la.loan_type, payment_count DESC
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            log.info("Retrieved {} payment behavior records", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving payment behavior: {}", e.getMessage());
            return createErrorData("Failed to retrieve payment behavior");
        }
    }
    
    /**
     * Generic query method for custom analysis
     */
    public List<Map<String, Object>> executeCustomQuery(String query, Map<String, Object> parameters) {
        try {
            // Security validation
            if (!isQuerySafe(query)) {
                throw new SecurityException("Query contains potentially unsafe operations");
            }
            
            List<Map<String, Object>> results;
            
            if (parameters != null && !parameters.isEmpty()) {
                // Use named parameter query if parameters provided
                results = jdbcTemplate.queryForList(query, parameters);
            } else {
                results = jdbcTemplate.queryForList(query);
            }
            
            // Apply data limits
            if (results.size() > properties.getMaxDataPoints()) {
                results = results.subList(0, properties.getMaxDataPoints());
                log.warn("Query results truncated to {} records", properties.getMaxDataPoints());
            }
            
            log.info("Custom query executed successfully, returned {} records", results.size());
            return results;
            
        } catch (DataAccessException e) {
            log.error("Database error in custom query: {}", e.getMessage());
            return createErrorData("Database query failed: " + e.getMessage());
        } catch (SecurityException e) {
            log.error("Security violation in custom query: {}", e.getMessage());
            return createErrorData("Security violation: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in custom query: {}", e.getMessage());
            return createErrorData("Query execution failed: " + e.getMessage());
        }
    }
    
    // Validation Methods
    private void validateTimeframe(String timeframe) {
        if (timeframe == null || timeframe.trim().isEmpty()) {
            throw new IllegalArgumentException("Timeframe cannot be null or empty");
        }
        
        List<String> validTimeframes = Arrays.asList("7d", "30d", "90d", "180d", "365d", "730d");
        if (!validTimeframes.contains(timeframe.toLowerCase())) {
            throw new IllegalArgumentException("Invalid timeframe. Valid options: " + validTimeframes);
        }
    }
    
    private void validateGroupBy(String groupBy) {
        if (groupBy == null || groupBy.trim().isEmpty()) {
            throw new IllegalArgumentException("GroupBy cannot be null or empty");
        }
        
        List<String> validGroupBy = Arrays.asList("day", "week", "month", "quarter", "year");
        if (!validGroupBy.contains(groupBy.toLowerCase())) {
            throw new IllegalArgumentException("Invalid groupBy. Valid options: " + validGroupBy);
        }
    }
    
    private void validateMetricType(String metricType) {
        List<String> validMetrics = Arrays.asList("total_amount", "approval_rate", "application_count", "avg_processing_days");
        if (!validMetrics.contains(metricType)) {
            throw new IllegalArgumentException("Invalid metric type. Valid options: " + validMetrics);
        }
    }
    
    private void validateSegmentBy(String segmentBy) {
        List<String> validSegments = Arrays.asList("credit_score", "income", "loan_type", "risk_category", "age");
        if (!validSegments.contains(segmentBy)) {
            throw new IllegalArgumentException("Invalid segment type. Valid options: " + validSegments);
        }
    }
    
    private void validateAnalysisType(String analysisType) {
        List<String> validTypes = Arrays.asList("activity", "security", "compliance", "performance");
        if (!validTypes.contains(analysisType)) {
            throw new IllegalArgumentException("Invalid analysis type. Valid options: " + validTypes);
        }
    }
    
    private void validatePaymentAnalysisType(String analysisType) {
        List<String> validTypes = Arrays.asList("timeliness", "amount", "frequency", "late_fees");
        if (!validTypes.contains(analysisType)) {
            throw new IllegalArgumentException("Invalid payment analysis type. Valid options: " + validTypes);
        }
    }
    
    // Helper Methods
    private int getTimeframeDays(String timeframe) {
        return switch (timeframe.toLowerCase()) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            case "180d" -> 180;
            case "365d" -> 365;
            case "730d" -> 730;
            default -> 30;
        };
    }
    
    private String getDateFormat(String groupBy) {
        return switch (groupBy.toLowerCase()) {
            case "day" -> "YYYY-MM-DD";
            case "week" -> "YYYY-IW";
            case "month" -> "YYYY-MM";
            case "quarter" -> "YYYY-Q";
            case "year" -> "YYYY";
            default -> "YYYY-MM-DD";
        };
    }
    
    private String getMetricOrderBy(String metricType) {
        return switch (metricType) {
            case "total_amount" -> "total_amount";
            case "approval_rate" -> "approval_rate";
            case "application_count" -> "total_applications";
            case "avg_processing_days" -> "avg_processing_days";
            default -> "total_amount";
        };
    }
    
    private boolean isQuerySafe(String query) {
        if (query == null) return false;
        
        String upperQuery = query.toUpperCase();
        
        // Check for dangerous operations
        List<String> dangerousOperations = Arrays.asList(
            "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE",
            "GRANT", "REVOKE", "EXEC", "EXECUTE", "CALL"
        );
        
        for (String operation : dangerousOperations) {
            if (upperQuery.contains(operation)) {
                return false;
            }
        }
        
        // Check for system tables
        if (upperQuery.contains("SYS.") || upperQuery.contains("SYSTEM.") || 
            upperQuery.contains("ORACLE_OCM.")) {
            return false;
        }
        
        return true;
    }
    
    private boolean isAuditDataAllowed(Map<String, Object> auditRecord) {
        if (!properties.getSecurity().isSqlInjectionProtection()) {
            return true;
        }
        
        String tableName = (String) auditRecord.get("TABLE_NAME");
        if (tableName == null) return false;
        
        Pattern blockedPattern = Pattern.compile(properties.getSecurity().getBlockedTablesPattern());
        return !blockedPattern.matcher(tableName.toUpperCase()).matches();
    }
    
    private List<Map<String, Object>> createErrorData(String errorMessage) {
        Map<String, Object> errorRecord = new HashMap<>();
        errorRecord.put("error", true);
        errorRecord.put("message", errorMessage);
        errorRecord.put("timestamp", new Date());
        return Arrays.asList(errorRecord);
    }
}
                    loan_type,
                    COUNT(*) as count,
                    COUNT(*) * 100.0 / SUM(COUNT(*)) OVER() as percentage,
                    AVG(loan_amount) as avg_amount,
                    MIN(application_date) as first_application,
                    MAX(application_date) as last_application
                FROM loan_applications 
                WHERE application_date >= SYSDATE - INTERVAL '%s' DAY
                GROUP BY loan_type
                ORDER BY count DESC
                """.formatted(getTimeframeDays(timeframe));
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            // Limit results for performance
            if (results.size() > properties.getMaxDataPoints()) {
                results = results.subList(0, properties.getMaxDataPoints());
            }
            
            log.info("Retrieved {} loan product popularity records for timeframe: {}", results.size(), timeframe);
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving loan product popularity: {}", e.getMessage());
            return createErrorData("Failed to retrieve loan product popularity");
        }
    }
    
    @Cacheable(value = "loanAnalytics", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getLoanTrends(String timeframe, String groupBy) {
        try {
            validateTimeframe(timeframe);
            validateGroupBy(groupBy);
            
            String dateFormat = getDateFormat(groupBy);
            
            String sql = """
                SELECT 
                    TO_CHAR(application_date, '%s') as period,
                    loan_type,
                    COUNT(*) as application_count,
                    SUM(loan_amount) as total_amount,
                    AVG(loan_amount) as avg_amount
                FROM loan_applications 
                WHERE application_date >= SYSDATE - INTERVAL '%s' DAY
                GROUP BY TO_CHAR(application_date, '%s'), loan_type
                ORDER BY period, loan_type
                """.formatted(dateFormat, getTimeframeDays(timeframe), dateFormat);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            log.info("Retrieved {} loan trend records for timeframe: {} grouped by: {}", 
                     results.size(), timeframe, groupBy);
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving loan trends: {}", e.getMessage());
            return createErrorData("Failed to retrieve loan trends");
        }
    }
    
    /**
     * 2. Branch Lending Performance Analysis
     */
    @Cacheable(value = "branchPerformance", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getBranchPerformance(String metricType) {
        try {
            validateMetricType(metricType);
            
            String sql = """
                SELECT 
                    b.branch_name,
                    b.branch_code,
                    b.region,
                    COUNT(la.application_id) as total_applications,
                    COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approved_count,
                    COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / 
                        NULLIF(COUNT(la.application_id), 0) as approval_rate,
                    SUM(CASE WHEN la.status = 'APPROVED' THEN la.loan_amount ELSE 0 END) as total_amount,
                    AVG(CASE WHEN la.status = 'APPROVED' THEN la.loan_amount END) as avg_loan_amount,
                    AVG(CASE WHEN la.processing_end_date IS NOT NULL 
                        THEN la.processing_end_date - la.application_date END) as avg_processing_days
                FROM branches b
                LEFT JOIN loan_applications la ON b.branch_id = la.branch_id
                WHERE la.application_date >= SYSDATE - INTERVAL '365' DAY
                GROUP BY b.branch_name, b.branch_code, b.region
                HAVING COUNT(la.application_id) > 0
                ORDER BY %s DESC
                """.formatted(getMetricOrderBy(metricType));
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            log.info("Retrieved {} branch performance records for metric: {}", results.size(), metricType);
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving branch performance: {}", e.getMessage());
            return createErrorData("Failed to retrieve branch performance");
        }
    }
    
    /**
     * 3. Customer Segmentation Analysis
     */
    @Cacheable(value = "customerSegments", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getCustomerSegmentation(String segmentBy) {
        try {
            validateSegmentBy(segmentBy);
            
            String sql = """
                SELECT 
                    c.customer_id,
                    c.credit_score,
                    c.annual_income,
                    c.employment_status,
                    c.age,
                    COUNT(la.application_id) as loan_count,
                    SUM(CASE WHEN la.status = 'APPROVED' THEN la.loan_amount ELSE 0 END) as total_loan_amount,
                    COUNT(CASE WHEN p.payment_status = 'LATE' THEN 1 END) as late_payments,
                    CASE 
                        WHEN c.credit_score >= 750 THEN 'EXCELLENT'
                        WHEN c.credit_score >= 700 THEN 'GOOD'
                        WHEN c.credit_score >= 650 THEN 'FAIR'
                        ELSE 'POOR'
                    END as credit_category,
                    CASE 
                        WHEN c.annual_income >= 100000 THEN 'HIGH'
                        WHEN c.annual_income >= 50000 THEN 'MEDIUM'
                        ELSE 'LOW'
                    END as income_category,
                    ra.risk_score,
                    ra.risk_category
                FROM customers c
                LEFT JOIN loan_applications la ON c.customer_id = la.customer_id
                LEFT JOIN payments p ON la.loan_id = p.loan_id
                LEFT JOIN risk_assessments ra ON la.application_id = ra.application_id
                WHERE c.created_date >= SYSDATE - INTERVAL '730' DAY
                GROUP BY c.customer_id, c.credit_score, c.annual_income, c.employment_status, 
                         c.age, ra.risk_score, ra.risk_category
                ORDER BY total_loan_amount DESC
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            // Apply data point limit
            if (results.size() > properties.getMaxDataPoints()) {
                results = results.subList(0, properties.getMaxDataPoints());
            }
            
            log.info("Retrieved {} customer segmentation records", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving customer segmentation: {}", e.getMessage());
            return createErrorData("Failed to retrieve customer segmentation");
        }
    }
    
    /**
     * 4. Interest Rate Impact Analysis
     */
    @Cacheable(value = "financialMetrics", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getInterestRateImpact(String timeframe) {
        try {
            validateTimeframe(timeframe);
            
            String sql = """
                SELECT 
                    TO_CHAR(la.application_date, 'YYYY-MM') as month,
                    AVG(la.interest_rate) as avg_interest_rate,
                    COUNT(*) as application_count,
                    COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approved_count,
                    AVG(la.loan_amount) as avg_loan_amount,
                    la.loan_type,
                    CORR(la.interest_rate, la.loan_amount) as rate_amount_correlation
                FROM loan_applications la
                WHERE la.application_date >= SYSDATE - INTERVAL '%s' DAY
                AND la.interest_rate IS NOT NULL
                GROUP BY TO_CHAR(la.application_date, 'YYYY-MM'), la.loan_type
                ORDER BY month, loan_type
                """.formatted(getTimeframeDays(timeframe));
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            log.info("Retrieved {} interest rate impact records", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Error retrieving interest rate impact: {}", e.getMessage());
            return createErrorData("Failed to retrieve interest rate impact");
        }
    }
    
    /**
     * 5. Risk Assessment Trends
     */
    @Cacheable(value = "riskAssessments", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> getRiskAssessmentTrends(String timeframe) {
        try {
            validateTimeframe(timeframe);
            
            String sql = """
                SELECT 
                