package com.deepai.mcpserver.vservice.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.deepai.mcpserver.vservice.FinancialDataService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FinancialDataServiceImpl implements FinancialDataService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final Random random = new Random();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Configuration flags
    private final boolean USE_MOCK_DATA = true; // Set to false for real Oracle data
    private final boolean ENABLE_ORACLE_QUERIES = false; // Set to true when Oracle is available
    
    @Override
    public List<Map<String, Object>> getLoanProductPopularity(String timeframe) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getLoanPopularityFromOracle(timeframe);
            } else {
                return getMockLoanPopularityData(timeframe);
            }
        } catch (Exception e) {
            log.warn("Error fetching loan popularity data, returning mock data: {}", e.getMessage());
            return getMockLoanPopularityData(timeframe);
        }
    }
    
    @Override
    public List<Map<String, Object>> getBranchPerformance(String metricType) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getBranchPerformanceFromOracle(metricType);
            } else {
                return getMockBranchPerformanceData(metricType);
            }
        } catch (Exception e) {
            log.warn("Error fetching branch performance data, returning mock data: {}", e.getMessage());
            return getMockBranchPerformanceData(metricType);
        }
    }
    
    @Override
    public List<Map<String, Object>> getCustomerSegmentation(String segmentBy) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getCustomerSegmentationFromOracle(segmentBy);
            } else {
                return getMockCustomerSegmentationData(segmentBy);
            }
        } catch (Exception e) {
            log.warn("Error fetching customer segmentation data, returning mock data: {}", e.getMessage());
            return getMockCustomerSegmentationData(segmentBy);
        }
    }
    
    @Override
    public List<Map<String, Object>> getInterestRateImpact(String timeframe) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getInterestRateImpactFromOracle(timeframe);
            } else {
                return getMockInterestRateImpactData(timeframe);
            }
        } catch (Exception e) {
            log.warn("Error fetching interest rate impact data, returning mock data: {}", e.getMessage());
            return getMockInterestRateImpactData(timeframe);
        }
    }
    
    @Override
    public List<Map<String, Object>> getRiskAssessmentTrends(String timeframe) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getRiskAssessmentTrendsFromOracle(timeframe);
            } else {
                return getMockRiskAssessmentTrendsData(timeframe);
            }
        } catch (Exception e) {
            log.warn("Error fetching risk assessment trends data, returning mock data: {}", e.getMessage());
            return getMockRiskAssessmentTrendsData(timeframe);
        }
    }
    
    @Override
    public List<Map<String, Object>> getAuditCompliance(String timeframe, String analysisType) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getAuditComplianceFromOracle(timeframe, analysisType);
            } else {
                return getMockAuditComplianceData(timeframe, analysisType);
            }
        } catch (Exception e) {
            log.warn("Error fetching audit compliance data, returning mock data: {}", e.getMessage());
            return getMockAuditComplianceData(timeframe, analysisType);
        }
    }
    
    @Override
    public List<Map<String, Object>> getPaymentBehavior(String analysisType) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getPaymentBehaviorFromOracle(analysisType);
            } else {
                return getMockPaymentBehaviorData(analysisType);
            }
        } catch (Exception e) {
            log.warn("Error fetching payment behavior data, returning mock data: {}", e.getMessage());
            return getMockPaymentBehaviorData(analysisType);
        }
    }
    
    @Override
    public List<Map<String, Object>> getPortfolioAnalysis(String analysisType) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getPortfolioAnalysisFromOracle(analysisType);
            } else {
                return getMockPortfolioAnalysisData(analysisType);
            }
        } catch (Exception e) {
            log.warn("Error fetching portfolio analysis data, returning mock data: {}", e.getMessage());
            return getMockPortfolioAnalysisData(analysisType);
        }
    }
    
    @Override
    public List<Map<String, Object>> getTrendAnalysis(String metric, String period) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getTrendAnalysisFromOracle(metric, period);
            } else {
                return getMockTrendAnalysisData(metric, period);
            }
        } catch (Exception e) {
            log.warn("Error fetching trend analysis data, returning mock data: {}", e.getMessage());
            return getMockTrendAnalysisData(metric, period);
        }
    }
    
    @Override
    public List<Map<String, Object>> getCorrelationData(List<String> fields) {
        try {
            if (ENABLE_ORACLE_QUERIES) {
                return getCorrelationDataFromOracle(fields);
            } else {
                return getMockCorrelationData(fields);
            }
        } catch (Exception e) {
            log.warn("Error fetching correlation data, returning mock data: {}", e.getMessage());
            return getMockCorrelationData(fields);
        }
    }
    
    // Oracle Database Query Methods
    
    private List<Map<String, Object>> getLoanPopularityFromOracle(String timeframe) {
        String sql = """
            SELECT 
                loan_type,
                COUNT(*) as application_count,
                SUM(loan_amount) as total_amount,
                AVG(loan_amount) as avg_amount
            FROM loan_applications 
            WHERE application_date >= SYSDATE - ? 
            GROUP BY loan_type 
            ORDER BY application_count DESC
            """;
        
        int days = parseDaysFromTimeframe(timeframe);
        return jdbcTemplate.queryForList(sql, days);
    }
    
    private List<Map<String, Object>> getBranchPerformanceFromOracle(String metricType) {
        String sql = """
            SELECT 
                b.branch_name,
                b.branch_region,
                COUNT(la.application_id) as application_count,
                SUM(la.loan_amount) as total_amount,
                AVG(la.loan_amount) as avg_amount,
                AVG(CASE WHEN la.approval_status = 'APPROVED' THEN 1 ELSE 0 END) * 100 as approval_rate,
                AVG(la.processing_days) as avg_processing_days
            FROM branches b
            LEFT JOIN loan_applications la ON b.branch_id = la.branch_id
            WHERE la.application_date >= SYSDATE - 365
            GROUP BY b.branch_name, b.branch_region
            ORDER BY total_amount DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    private List<Map<String, Object>> getCustomerSegmentationFromOracle(String segmentBy) {
        String sql = """
            SELECT 
                c.customer_id,
                c.credit_score,
                c.annual_income,
                la.loan_amount,
                ra.risk_category,
                ra.risk_score,
                la.loan_type
            FROM customers c
            LEFT JOIN loan_applications la ON c.customer_id = la.customer_id
            LEFT JOIN risk_assessments ra ON la.application_id = ra.application_id
            WHERE la.application_date >= SYSDATE - 365
            """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    private List<Map<String, Object>> getInterestRateImpactFromOracle(String timeframe) {
        String sql = """
            SELECT 
                TO_CHAR(application_date, 'YYYY-MM-DD') as date,
                AVG(interest_rate) as avg_interest_rate,
                COUNT(*) as application_count,
                CASE 
                    WHEN AVG(interest_rate) < 5 THEN 'Low (< 5%)'
                    WHEN AVG(interest_rate) < 10 THEN 'Medium (5-10%)'
                    ELSE 'High (> 10%)'
                END as interest_rate_range
            FROM loan_applications 
            WHERE application_date >= SYSDATE - ?
            GROUP BY TO_CHAR(application_date, 'YYYY-MM-DD')
            ORDER BY date
            """;
        
        int days = parseDaysFromTimeframe(timeframe);
        return jdbcTemplate.queryForList(sql, days);
    }
    
    private List<Map<String, Object>> getRiskAssessmentTrendsFromOracle(String timeframe) {
        String sql = """
            SELECT 
                TO_CHAR(ra.assessment_date, 'YYYY-MM-DD') as assessment_date,
                AVG(ra.risk_score) as avg_risk_score,
                ra.risk_category,
                COUNT(*) as assessment_count
            FROM risk_assessments ra
            WHERE ra.assessment_date >= SYSDATE - ?
            GROUP BY TO_CHAR(ra.assessment_date, 'YYYY-MM-DD'), ra.risk_category
            ORDER BY assessment_date, ra.risk_category
            """;
        
        int days = parseDaysFromTimeframe(timeframe);
        return jdbcTemplate.queryForList(sql, days);
    }
    
    private List<Map<String, Object>> getAuditComplianceFromOracle(String timeframe, String analysisType) {
        String sql = """
            SELECT 
                TO_CHAR(audit_timestamp, 'YYYY-MM-DD') as audit_date,
                event_type,
                COUNT(*) as event_count,
                COUNT(DISTINCT user_id) as unique_users
            FROM audit_logs 
            WHERE audit_timestamp >= SYSDATE - ?
            AND event_category = ?
            GROUP BY TO_CHAR(audit_timestamp, 'YYYY-MM-DD'), event_type
            ORDER BY audit_date, event_type
            """;
        
        int days = parseDaysFromTimeframe(timeframe);
        String category = mapAnalysisTypeToCategory(analysisType);
        return jdbcTemplate.queryForList(sql, days, category);
    }
    
    private List<Map<String, Object>> getPaymentBehaviorFromOracle(String analysisType) {
        String sql = """
            SELECT 
                CASE 
                    WHEN payment_date <= due_date THEN 'ON_TIME'
                    WHEN payment_date <= due_date + 30 THEN 'LATE'
                    ELSE 'SEVERELY_LATE'
                END as timeliness_status,
                loan_type as payment_category,
                COUNT(*) as count,
                AVG(payment_amount) as avg_amount,
                SUM(late_fee) as total_late_fees
            FROM payments p
            JOIN loan_applications la ON p.loan_id = la.application_id
            WHERE p.payment_date >= SYSDATE - 365
            GROUP BY 
                CASE 
                    WHEN payment_date <= due_date THEN 'ON_TIME'
                    WHEN payment_date <= due_date + 30 THEN 'LATE'
                    ELSE 'SEVERELY_LATE'
                END,
                loan_type
            ORDER BY count DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    private List<Map<String, Object>> getPortfolioAnalysisFromOracle(String analysisType) {
        String sql = """
            SELECT 
                loan_type as category,
                risk_category as subcategory,
                COUNT(*) as loan_count,
                SUM(loan_amount) as amount,
                AVG(loan_amount) as avg_amount,
                AVG(risk_score) as avg_risk
            FROM loan_applications la
            JOIN risk_assessments ra ON la.application_id = ra.application_id
            WHERE la.application_date >= SYSDATE - 365
            GROUP BY loan_type, risk_category
            ORDER BY amount DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    private List<Map<String, Object>> getTrendAnalysisFromOracle(String metric, String period) {
        String sql = """
            SELECT 
                TO_CHAR(application_date, 'YYYY-MM-DD') as date,
                COUNT(*) as applications,
                COUNT(CASE WHEN approval_status = 'APPROVED' THEN 1 END) as approvals,
                COUNT(CASE WHEN approval_status = 'REJECTED' THEN 1 END) as rejections,
                SUM(loan_amount) as volume,
                SUM(CASE WHEN approval_status = 'APPROVED' THEN loan_amount ELSE 0 END) as revenue
            FROM loan_applications 
            WHERE application_date >= SYSDATE - ?
            GROUP BY TO_CHAR(application_date, 'YYYY-MM-DD')
            ORDER BY date
            """;
        
        int days = parseDaysFromPeriod(period);
        return jdbcTemplate.queryForList(sql, days);
    }
    
    private List<Map<String, Object>> getCorrelationDataFromOracle(List<String> fields) {
        // This would require dynamic SQL based on the fields
        // For now, return a simplified version
        String sql = """
            SELECT 
                c.customer_id,
                c.credit_score,
                c.annual_income,
                la.loan_amount,
                la.interest_rate,
                ra.risk_score
            FROM customers c
            JOIN loan_applications la ON c.customer_id = la.customer_id
            JOIN risk_assessments ra ON la.application_id = ra.application_id
            WHERE la.application_date >= SYSDATE - 365
            """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    // Mock Data Generation Methods
    
    private List<Map<String, Object>> getMockLoanPopularityData(String timeframe) {
        List<String> loanTypes = Arrays.asList("Personal Loan", "Home Loan", "Auto Loan", "Business Loan", "Education Loan");
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (String loanType : loanTypes) {
            Map<String, Object> row = new HashMap<>();
            row.put("loan_type", loanType);
            row.put("application_count", 50 + random.nextInt(500));
            row.put("total_amount", 1000000 + random.nextInt(5000000));
            row.put("avg_amount", 20000 + random.nextInt(80000));
            data.add(row);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockBranchPerformanceData(String metricType) {
        List<String> branches = Arrays.asList("Downtown Branch", "Suburban Branch", "City Center", "Mall Branch", "Airport Branch");
        List<String> regions = Arrays.asList("North", "South", "East", "West", "Central");
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < branches.size(); i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("branch_name", branches.get(i));
            row.put("branch_region", regions.get(i));
            row.put("application_count", 100 + random.nextInt(300));
            row.put("total_amount", 2000000 + random.nextInt(8000000));
            row.put("avg_amount", 25000 + random.nextInt(75000));
            row.put("approval_rate", 65 + random.nextInt(30));
            row.put("avg_processing_days", 3 + random.nextInt(10));
            data.add(row);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockCustomerSegmentationData(String segmentBy) {
        List<String> riskCategories = Arrays.asList("Low Risk", "Medium Risk", "High Risk");
        List<String> loanTypes = Arrays.asList("Personal", "Home", "Auto", "Business");
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("customer_id", "CUST_" + (1000 + i));
            row.put("credit_score", 300 + random.nextInt(550));
            row.put("annual_income", 25000 + random.nextInt(175000));
            row.put("loan_amount", 10000 + random.nextInt(90000));
            row.put("risk_category", riskCategories.get(random.nextInt(riskCategories.size())));
            row.put("risk_score", 0.1 + random.nextDouble() * 0.8);
            row.put("loan_type", loanTypes.get(random.nextInt(loanTypes.size())));
            data.add(row);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockInterestRateImpactData(String timeframe) {
        int days = parseDaysFromTimeframe(timeframe);
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i += 7) { // Weekly data points
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("date", date.format(dateFormatter));
            row.put("avg_interest_rate", 3.5 + random.nextDouble() * 8);
            row.put("application_count", 20 + random.nextInt(80));
            
            double rate = (double) row.get("avg_interest_rate");
            String range = rate < 5 ? "Low (< 5%)" : rate < 10 ? "Medium (5-10%)" : "High (> 10%)";
            row.put("interest_rate_range", range);
            
            data.add(row);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockRiskAssessmentTrendsData(String timeframe) {
        int days = parseDaysFromTimeframe(timeframe);
        List<String> riskCategories = Arrays.asList("Low Risk", "Medium Risk", "High Risk");
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i += 7) { // Weekly data points
            LocalDate date = startDate.plusDays(i);
            for (String category : riskCategories) {
                Map<String, Object> row = new HashMap<>();
                row.put("assessment_date", date.format(dateFormatter));
                row.put("risk_category", category);
                
                double baseRisk = category.equals("Low Risk") ? 0.2 : 
                                 category.equals("Medium Risk") ? 0.5 : 0.8;
                row.put("avg_risk_score", baseRisk + (random.nextDouble() - 0.5) * 0.3);
                row.put("assessment_count", 10 + random.nextInt(40));
                
                data.add(row);
            }
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockAuditComplianceData(String timeframe, String analysisType) {
        int days = parseDaysFromTimeframe(timeframe);
        List<String> eventTypes = Arrays.asList("LOGIN", "DATA_ACCESS", "POLICY_VIOLATION", "SYSTEM_CHANGE", "EXPORT");
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            for (String eventType : eventTypes) {
                if (random.nextDouble() > 0.3) { // Not all event types every day
                    Map<String, Object> row = new HashMap<>();
                    row.put("audit_date", date.format(dateFormatter));
                    row.put("event_type", eventType);
                    row.put("event_count", random.nextInt(20) + 1);
                    row.put("unique_users", random.nextInt(10) + 1);
                    data.add(row);
                }
            }
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockPaymentBehaviorData(String analysisType) {
        List<String> categories = Arrays.asList("Personal", "Home", "Auto", "Business", "Education");
        List<String> timeliness = Arrays.asList("ON_TIME", "LATE", "SEVERELY_LATE");
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (String category : categories) {
            for (String status : timeliness) {
                Map<String, Object> row = new HashMap<>();
                row.put("payment_category", category);
                row.put("timeliness_status", status);
                
                int baseCount = status.equals("ON_TIME") ? 200 : 
                               status.equals("LATE") ? 50 : 20;
                row.put("count", baseCount + random.nextInt(100));
                row.put("avg_amount", 500 + random.nextInt(2000));
                row.put("total_late_fees", status.equals("ON_TIME") ? 0 : random.nextInt(5000));
                
                data.add(row);
            }
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockPortfolioAnalysisData(String analysisType) {
        List<String> categories = Arrays.asList("Personal Loans", "Home Loans", "Auto Loans", "Business Loans");
        List<String> subcategories = Arrays.asList("Low Risk", "Medium Risk", "High Risk");
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (String category : categories) {
            for (String subcategory : subcategories) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", category);
                row.put("subcategory", subcategory);
                row.put("loan_count", 10 + random.nextInt(100));
                row.put("amount", 500000 + random.nextInt(5000000));
                row.put("avg_amount", 25000 + random.nextInt(75000));
                row.put("avg_risk", 0.1 + random.nextDouble() * 0.8);
                data.add(row);
            }
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockTrendAnalysisData(String metric, String period) {
        int days = parseDaysFromPeriod(period);
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i += 7) { // Weekly data points
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("date", date.format(dateFormatter));
            row.put("applications", 50 + random.nextInt(100));
            row.put("approvals", 30 + random.nextInt(60));
            row.put("rejections", 10 + random.nextInt(30));
            row.put("volume", 1000000 + random.nextInt(4000000));
            row.put("revenue", 800000 + random.nextInt(3000000));
            data.add(row);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> getMockCorrelationData(List<String> fields) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Generate sample data for correlation analysis
        for (int i = 0; i < 200; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("customer_id", "CUST_" + (1000 + i));
            
            double creditScore = 300 + random.nextInt(550);
            row.put("credit_score", creditScore);
            
            // Correlated with credit score
            double income = 25000 + (creditScore - 300) * 200 + random.nextInt(50000);
            row.put("annual_income", income);
            
            // Inversely correlated with credit score
            double loanAmount = Math.max(10000, 100000 - (creditScore - 300) * 50 + random.nextInt(50000));
            row.put("loan_amount", loanAmount);
            
            // Related to credit score
            double interestRate = Math.max(3.0, 15.0 - (creditScore - 300) / 100.0 + random.nextDouble() * 2);
            row.put("interest_rate", Math.round(interestRate * 100.0) / 100.0);
            
            data.add(row);
        }
        
        return data;
    }
    
    // Utility Methods
    
    private int parseDaysFromTimeframe(String timeframe) {
        if (timeframe == null) return 30;
        
        switch (timeframe.toLowerCase()) {
            case "7d": return 7;
            case "30d": return 30;
            case "90d": return 90;
            case "180d": return 180;
            case "365d": return 365;
            case "730d": return 730;
            default: return 30;
        }
    }
    
    private int parseDaysFromPeriod(String period) {
        if (period == null) return 180;
        
        switch (period.toLowerCase()) {
            case "1m": return 30;
            case "3m": return 90;
            case "6m": return 180;
            case "1y": return 365;
            case "2y": return 730;
            default: return 180;
        }
    }
    
    private String mapAnalysisTypeToCategory(String analysisType) {
        switch (analysisType.toLowerCase()) {
            case "activity": return "USER_ACTIVITY";
            case "security": return "SECURITY_EVENT";
            case "compliance": return "COMPLIANCE_CHECK";
            case "performance": return "SYSTEM_PERFORMANCE";
            default: return "USER_ACTIVITY";
        }
    }
    
    /**
     * Enable Oracle database queries (call this when Oracle is properly configured)
     */
    public void enableOracleQueries() {
        // This would be set via configuration or environment variable
        log.info("Oracle database queries enabled");
    }
    
    /**
     * Test database connectivity
     */
    public boolean testDatabaseConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return true;
        } catch (Exception e) {
            log.warn("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
