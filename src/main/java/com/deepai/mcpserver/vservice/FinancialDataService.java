package com.deepai.mcpserver.vservice;

import java.util.List;
import java.util.Map;

public interface FinancialDataService {
    
    /**
     * Get loan product popularity data for specified timeframe
     * @param timeframe Time period (7d, 30d, 90d, 180d, 365d)
     * @return List of loan popularity data with loan_type and application_count
     */
    List<Map<String, Object>> getLoanProductPopularity(String timeframe);
    
    /**
     * Get branch performance data by specified metric
     * @param metricType Metric to analyze (total_amount, approval_rate, application_count, avg_processing_days)
     * @return List of branch performance data
     */
    List<Map<String, Object>> getBranchPerformance(String metricType);
    
    /**
     * Get customer segmentation data
     * @param segmentBy Segmentation basis (credit_score, income, loan_type, risk_category)
     * @return List of customer segmentation data
     */
    List<Map<String, Object>> getCustomerSegmentation(String segmentBy);
    
    /**
     * Get interest rate impact analysis data
     * @param timeframe Analysis period (90d, 180d, 365d, 730d)
     * @return List of interest rate impact data
     */
    List<Map<String, Object>> getInterestRateImpact(String timeframe);
    
    /**
     * Get risk assessment trends data
     * @param timeframe Analysis period (30d, 90d, 180d, 365d)
     * @return List of risk assessment trends data
     */
    List<Map<String, Object>> getRiskAssessmentTrends(String timeframe);
    
    /**
     * Get audit compliance data
     * @param timeframe Analysis period (1d, 7d, 30d, 90d)
     * @param analysisType Analysis focus (activity, security, compliance, performance)
     * @return List of audit compliance data
     */
    List<Map<String, Object>> getAuditCompliance(String timeframe, String analysisType);
    
    /**
     * Get payment behavior data
     * @param analysisType Analysis focus (timeliness, amount, frequency, late_fees)
     * @return List of payment behavior data
     */
    List<Map<String, Object>> getPaymentBehavior(String analysisType);
    
    /**
     * Get portfolio analysis data
     * @param analysisType Analysis type (composition, risk_distribution, performance, concentration)
     * @return List of portfolio analysis data
     */
    List<Map<String, Object>> getPortfolioAnalysis(String analysisType);
    
    /**
     * Get trend analysis data
     * @param metric Metric to analyze (applications, approvals, defaults, revenue, volume)
     * @param period Time period (1m, 3m, 6m, 1y, 2y)
     * @return List of trend analysis data
     */
    List<Map<String, Object>> getTrendAnalysis(String metric, String period);
    
    /**
     * Get correlation data for specified fields
     * @param fields List of fields to correlate
     * @return List of correlation data
     */
    List<Map<String, Object>> getCorrelationData(List<String> fields);
    
    /**
     * Test database connectivity
     * @return true if database is accessible, false otherwise
     */
    default boolean testDatabaseConnection() {
        return true;
    }
    
    /**
     * Enable Oracle database queries (implementation-specific)
     */
    default void enableOracleQueries() {
        // Default implementation does nothing
    }
}
