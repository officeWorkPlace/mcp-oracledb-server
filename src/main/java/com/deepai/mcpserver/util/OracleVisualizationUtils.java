package com.deepai.mcpserver.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class OracleVisualizationUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleVisualizationUtils.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

    // Performance Dashboard Helper Methods
    public Map<String, Object> collectPerformanceMetrics(String timeRange, List<String> metrics) {
        Map<String, Object> performanceData = new HashMap<>();
        
        for (String metric : metrics) {
            switch (metric) {
                case "CPU":
                    performanceData.put("cpu", getCpuMetrics(timeRange));
                    break;
                case "MEMORY":
                    performanceData.put("memory", getMemoryMetrics(timeRange));
                    break;
                case "IO":
                    performanceData.put("io", getIoMetrics(timeRange));
                    break;
                case "SESSIONS":
                    performanceData.put("sessions", getSessionMetrics(timeRange));
                    break;
                case "WAITS":
                    performanceData.put("waits", getWaitMetrics(timeRange));
                    break;
            }
        }
        return performanceData;
    }

    private Map<String, Object> getCpuMetrics(String timeRange) {
        try {
            String sql = """
                SELECT 
                    ROUND(AVG(value), 2) as avg_cpu_usage,
                    ROUND(MAX(value), 2) as max_cpu_usage,
                    COUNT(*) as sample_count
                FROM V$SYSMETRIC_HISTORY 
                WHERE metric_name = 'Host CPU Utilization (%)'
                AND begin_time >= SYSTIMESTAMP - INTERVAL '%s' %s
                """.formatted(getTimeRangeValue(timeRange), getTimeRangeUnit(timeRange));
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("avg_cpu_usage", 0, "max_cpu_usage", 0, "sample_count", 0, 
                         "error", "CPU metrics require V$ view access");
        }
    }

    private Map<String, Object> getMemoryMetrics(String timeRange) {
        try {
            String sql = """
                SELECT 
                    ROUND(SUM(bytes)/1024/1024/1024, 2) as total_memory_gb,
                    COUNT(DISTINCT pool) as memory_pools
                FROM V$SGAINFO
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("total_memory_gb", 0, "memory_pools", 0,
                         "error", "Memory metrics require V$ view access");
        }
    }

    private Map<String, Object> getIoMetrics(String timeRange) {
        try {
            String sql = """
                SELECT 
                    SUM(phyrds) as physical_reads,
                    SUM(phywrts) as physical_writes,
                    ROUND(AVG(readtim), 2) as avg_read_time_ms
                FROM V$FILESTAT
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("physical_reads", 0, "physical_writes", 0, "avg_read_time_ms", 0,
                         "error", "I/O metrics require V$ view access");
        }
    }

    private Map<String, Object> getSessionMetrics(String timeRange) {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as current_sessions,
                    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_sessions,
                    COUNT(DISTINCT username) as unique_users
                FROM V$SESSION
                WHERE username IS NOT NULL
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("current_sessions", 0, "active_sessions", 0, "unique_users", 0,
                         "error", "Session metrics require V$ view access");
        }
    }

    private Map<String, Object> getWaitMetrics(String timeRange) {
        try {
            String sql = """
                SELECT 
                    event_name,
                    ROUND(time_waited_micro/1000, 2) as time_waited_ms,
                    total_waits
                FROM (
                    SELECT event_name, time_waited_micro, total_waits,
                           ROW_NUMBER() OVER (ORDER BY time_waited_micro DESC) as rn
                    FROM V$SYSTEM_EVENT
                    WHERE event_name NOT LIKE 'SQL*Net%'
                )
                WHERE rn <= 10
                """;
            
            return Map.of("topWaitEvents", jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            return Map.of("topWaitEvents", new ArrayList<>(),
                         "error", "Wait metrics require V$ view access");
        }
    }

    // Health Report Helper Methods
    public Map<String, Object> getDatabaseInfo() {
        try {
            String sql = """
                SELECT 
                    d.name as database_name,
                    d.database_role,
                    d.open_mode,
                    i.version,
                    i.startup_time,
                    d.created
                FROM V$DATABASE d, V$INSTANCE i
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("database_name", "Unknown", "version", "Unknown",
                         "error", "Database info requires V$ view access");
        }
    }

    public Map<String, Object> analyzeStorageHealth() {
        Map<String, Object> storageHealth = new HashMap<>();
        
        try {
            // Tablespace usage
            String sql = """
                SELECT 
                    tablespace_name,
                    ROUND(used_percent, 2) as used_percent,
                    ROUND(total_mb, 2) as total_mb,
                    ROUND(used_mb, 2) as used_mb,
                    ROUND(free_mb, 2) as free_mb
                FROM (
                    SELECT 
                        ts.tablespace_name,
                        NVL(df.total_mb, 0) as total_mb,
                        NVL(df.total_mb - fs.free_mb, 0) as used_mb,
                        NVL(fs.free_mb, 0) as free_mb,
                        CASE 
                            WHEN df.total_mb > 0 THEN ((df.total_mb - NVL(fs.free_mb, 0)) / df.total_mb) * 100
                            ELSE 0 
                        END as used_percent
                    FROM 
                        dba_tablespaces ts
                        LEFT JOIN (
                            SELECT tablespace_name, SUM(bytes)/1024/1024 as total_mb
                            FROM dba_data_files 
                            GROUP BY tablespace_name
                        ) df ON ts.tablespace_name = df.tablespace_name
                        LEFT JOIN (
                            SELECT tablespace_name, SUM(bytes)/1024/1024 as free_mb
                            FROM dba_free_space 
                            GROUP BY tablespace_name
                        ) fs ON ts.tablespace_name = fs.tablespace_name
                )
                ORDER BY used_percent DESC
                """;
            
            List<Map<String, Object>> tablespaces = jdbcTemplate.queryForList(sql);
            storageHealth.put("tablespaces", tablespaces);
            
            // Calculate overall storage score
            double avgUsage = tablespaces.stream()
                .mapToDouble(ts -> ((Number) ts.get("used_percent")).doubleValue())
                .average().orElse(0.0);
            
            storageHealth.put("overallUsagePercent", avgUsage);
            storageHealth.put("healthScore", calculateStorageHealthScore(avgUsage));
            
        } catch (Exception e) {
            storageHealth.put("error", "Storage analysis requires DBA_TABLESPACES access");
            storageHealth.put("healthScore", 50.0);
        }
        
        return storageHealth;
    }

    public Map<String, Object> analyzePerformanceHealth() {
        Map<String, Object> perfHealth = new HashMap<>();
        
        try {
            // Get recent performance metrics
            String sql = """
                SELECT 
                    metric_name,
                    ROUND(AVG(value), 2) as avg_value,
                    ROUND(MAX(value), 2) as max_value
                FROM V$SYSMETRIC_HISTORY 
                WHERE begin_time >= SYSTIMESTAMP - INTERVAL '1' DAY
                AND metric_name IN (
                    'Database CPU Time Ratio',
                    'Database Wait Time Ratio', 
                    'Buffer Cache Hit Ratio',
                    'Library Cache Hit Ratio'
                )
                GROUP BY metric_name
                """;
            
            List<Map<String, Object>> metrics = jdbcTemplate.queryForList(sql);
            perfHealth.put("keyMetrics", metrics);
            
            // Calculate performance score
            double perfScore = calculatePerformanceScore(metrics);
            perfHealth.put("performanceScore", perfScore);
            
        } catch (Exception e) {
            perfHealth.put("error", "Performance analysis requires V$ view access");
            perfHealth.put("performanceScore", 50.0);
        }
        
        return perfHealth;
    }

    public Map<String, Object> analyzeSecurityHealth() {
        Map<String, Object> securityHealth = new HashMap<>();
        
        try {
            // Password policy analysis
            String sql = """
                SELECT 
                    profile,
                    resource_name,
                    limit
                FROM dba_profiles 
                WHERE resource_type = 'PASSWORD'
                AND profile = 'DEFAULT'
                """;
            
            List<Map<String, Object>> passwordPolicies = jdbcTemplate.queryForList(sql);
            securityHealth.put("passwordPolicies", passwordPolicies);
            
            // User account status
            String userSql = """
                SELECT 
                    account_status,
                    COUNT(*) as user_count
                FROM dba_users 
                WHERE username NOT IN ('SYS', 'SYSTEM', 'SYSMAN')
                GROUP BY account_status
                """;
            
            List<Map<String, Object>> userStatus = jdbcTemplate.queryForList(userSql);
            securityHealth.put("userAccountStatus", userStatus);
            
            // Calculate security score
            double securityScore = calculateSecurityScore(passwordPolicies, userStatus);
            securityHealth.put("securityScore", securityScore);
            
        } catch (Exception e) {
            securityHealth.put("error", "Security analysis requires DBA_PROFILES access");
            securityHealth.put("securityScore", 50.0);
        }
        
        return securityHealth;
    }

    public Map<String, Object> analyzeMaintenanceHealth() {
        Map<String, Object> maintenanceHealth = new HashMap<>();
        
        try {
            // Check for recent statistics gathering
            String sql = """
                SELECT 
                    owner,
                    COUNT(*) as table_count,
                    MIN(last_analyzed) as oldest_stats,
                    MAX(last_analyzed) as newest_stats
                FROM dba_tables 
                WHERE owner NOT IN ('SYS', 'SYSTEM', 'SYSMAN')
                AND owner NOT LIKE 'APEX_%'
                GROUP BY owner
                ORDER BY oldest_stats NULLS FIRST
                """;
            
            List<Map<String, Object>> statsInfo = jdbcTemplate.queryForList(sql);
            maintenanceHealth.put("statisticsInfo", statsInfo);
            
            // Calculate maintenance score
            double maintenanceScore = calculateMaintenanceScore(statsInfo);
            maintenanceHealth.put("maintenanceScore", maintenanceScore);
            
        } catch (Exception e) {
            maintenanceHealth.put("error", "Maintenance analysis requires DBA_TABLES access");
            maintenanceHealth.put("maintenanceScore", 50.0);
        }
        
        return maintenanceHealth;
    }

    // Data Quality Helper Methods
    public List<Map<String, Object>> analyzeTableQuality(String schemaName, String tableName, List<String> checks) {
        List<Map<String, Object>> qualityResults = new ArrayList<>();
        
        for (String check : checks) {
            Map<String, Object> checkResult = new HashMap<>();
            checkResult.put("checkType", check);
            checkResult.put("tableName", tableName);
            
            try {
                switch (check) {
                    case "NULL_CHECK":
                        checkResult.putAll(performNullCheck(schemaName, tableName));
                        break;
                    case "DUPLICATE_CHECK":
                        checkResult.putAll(performDuplicateCheck(schemaName, tableName));
                        break;
                    case "FORMAT_CHECK":
                        checkResult.putAll(performFormatCheck(schemaName, tableName));
                        break;
                    case "CONSTRAINT_CHECK":
                        checkResult.putAll(performConstraintCheck(schemaName, tableName));
                        break;
                }
                checkResult.put("status", "completed");
            } catch (Exception e) {
                checkResult.put("status", "error");
                checkResult.put("error", e.getMessage());
            }
            
            qualityResults.add(checkResult);
        }
        
        return qualityResults;
    }

    private Map<String, Object> performNullCheck(String schemaName, String tableName) {
        try {
            String sql = """
                SELECT 
                    column_name,
                    nullable,
                    CASE WHEN nullable = 'Y' THEN 'ALLOWS_NULL' ELSE 'NOT_NULL' END as null_policy
                FROM all_tab_columns 
                WHERE owner = ? AND table_name = ?
                ORDER BY column_id
                """;
            
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, schemaName, tableName);
            
            // Check for actual null values in nullable columns
            List<Map<String, Object>> nullAnalysis = new ArrayList<>();
            for (Map<String, Object> column : columns) {
                if ("Y".equals(column.get("nullable"))) {
                    String columnName = (String) column.get("column_name");
                    String countSql = String.format(
                        "SELECT COUNT(*) as total_rows, COUNT(%s) as non_null_rows FROM %s.%s",
                        columnName, schemaName, tableName
                    );
                    
                    try {
                        Map<String, Object> counts = jdbcTemplate.queryForMap(countSql);
                        int totalRows = ((Number) counts.get("total_rows")).intValue();
                        int nonNullRows = ((Number) counts.get("non_null_rows")).intValue();
                        double nullPercent = totalRows > 0 ? ((double)(totalRows - nonNullRows) / totalRows) * 100 : 0;
                        
                        nullAnalysis.add(Map.of(
                            "column", columnName,
                            "totalRows", totalRows,
                            "nullRows", totalRows - nonNullRows,
                            "nullPercent", Math.round(nullPercent * 100.0) / 100.0
                        ));
                    } catch (Exception e) {
                        nullAnalysis.add(Map.of(
                            "column", columnName,
                            "error", "Could not analyze null values"
                        ));
                    }
                }
            }
            
            return Map.of(
                "columnDefinitions", columns,
                "nullAnalysis", nullAnalysis,
                "qualityScore", calculateNullQualityScore(nullAnalysis)
            );
        } catch (Exception e) {
            return Map.of("error", "Null check failed: " + e.getMessage(), "qualityScore", 0.0);
        }
    }

    private Map<String, Object> performDuplicateCheck(String schemaName, String tableName) {
        try {
            // Get table row count
            String countSql = String.format("SELECT COUNT(*) as total_rows FROM %s.%s", schemaName, tableName);
            int totalRows = jdbcTemplate.queryForObject(countSql, Integer.class);
            
            // Check for duplicates based on all columns (simplified approach)
            String duplicateSql = String.format("""
                SELECT COUNT(*) as unique_rows FROM (
                    SELECT DISTINCT * FROM %s.%s
                )
                """, schemaName, tableName);
            
            int uniqueRows = jdbcTemplate.queryForObject(duplicateSql, Integer.class);
            int duplicateRows = totalRows - uniqueRows;
            double duplicatePercent = totalRows > 0 ? ((double)duplicateRows / totalRows) * 100 : 0;
            
            return Map.of(
                "totalRows", totalRows,
                "uniqueRows", uniqueRows,
                "duplicateRows", duplicateRows,
                "duplicatePercent", Math.round(duplicatePercent * 100.0) / 100.0,
                "qualityScore", duplicatePercent < 1 ? 100.0 : Math.max(0, 100 - duplicatePercent)
            );
        } catch (Exception e) {
            return Map.of("error", "Duplicate check failed: " + e.getMessage(), "qualityScore", 0.0);
        }
    }

    private Map<String, Object> performFormatCheck(String schemaName, String tableName) {
        try {
            // Get string columns for format validation
            String sql = """
                SELECT column_name, data_type, data_length
                FROM all_tab_columns 
                WHERE owner = ? AND table_name = ?
                AND data_type IN ('VARCHAR2', 'CHAR', 'NVARCHAR2', 'NCHAR')
                """;
            
            List<Map<String, Object>> stringColumns = jdbcTemplate.queryForList(sql, schemaName, tableName);
            List<Map<String, Object>> formatAnalysis = new ArrayList<>();
            
            for (Map<String, Object> column : stringColumns) {
                String columnName = (String) column.get("column_name");
                
                // Check for common format issues
                String formatSql = String.format("""
                    SELECT 
                        COUNT(*) as total_values,
                        COUNT(CASE WHEN %s IS NOT NULL AND TRIM(%s) = '' THEN 1 END) as empty_strings,
                        COUNT(CASE WHEN %s != TRIM(%s) THEN 1 END) as untrimmed_values,
                        COUNT(CASE WHEN REGEXP_LIKE(%s, '[[:cntrl:]]') THEN 1 END) as control_chars
                    FROM %s.%s
                    WHERE %s IS NOT NULL
                    """, columnName, columnName, columnName, columnName, columnName, schemaName, tableName, columnName);
                
                try {
                    Map<String, Object> formatStats = jdbcTemplate.queryForMap(formatSql);
                    formatAnalysis.add(Map.of(
                        "column", columnName,
                        "formatStats", formatStats,
                        "qualityScore", calculateFormatQualityScore(formatStats)
                    ));
                } catch (Exception e) {
                    formatAnalysis.add(Map.of(
                        "column", columnName,
                        "error", "Could not analyze format"
                    ));
                }
            }
            
            double overallScore = formatAnalysis.stream()
                .mapToDouble(fa -> (Double) fa.getOrDefault("qualityScore", 0.0))
                .average().orElse(0.0);
            
            return Map.of(
                "stringColumns", stringColumns.size(),
                "formatAnalysis", formatAnalysis,
                "qualityScore", Math.round(overallScore * 100.0) / 100.0
            );
        } catch (Exception e) {
            return Map.of("error", "Format check failed: " + e.getMessage(), "qualityScore", 0.0);
        }
    }

    private Map<String, Object> performConstraintCheck(String schemaName, String tableName) {
        try {
            // Get constraint information
            String sql = """
                SELECT 
                    constraint_name,
                    constraint_type,
                    status,
                    validated,
                    CASE constraint_type 
                        WHEN 'P' THEN 'PRIMARY KEY'
                        WHEN 'U' THEN 'UNIQUE'
                        WHEN 'R' THEN 'FOREIGN KEY'
                        WHEN 'C' THEN 'CHECK'
                        ELSE constraint_type
                    END as constraint_description
                FROM all_constraints
                WHERE owner = ? AND table_name = ?
                ORDER BY constraint_type
                """;
            
            List<Map<String, Object>> constraints = jdbcTemplate.queryForList(sql, schemaName, tableName);
            
            // Analyze constraint health
            long enabledConstraints = constraints.stream()
                .filter(c -> "ENABLED".equals(c.get("status")))
                .count();
            
            long validatedConstraints = constraints.stream()
                .filter(c -> "VALIDATED".equals(c.get("validated")))
                .count();
            
            double constraintHealthScore = constraints.isEmpty() ? 50.0 :
                ((double)(enabledConstraints + validatedConstraints) / (constraints.size() * 2)) * 100;
            
            return Map.of(
                "totalConstraints", constraints.size(),
                "enabledConstraints", enabledConstraints,
                "validatedConstraints", validatedConstraints,
                "constraints", constraints,
                "qualityScore", Math.round(constraintHealthScore * 100.0) / 100.0
            );
        } catch (Exception e) {
            return Map.of("error", "Constraint check failed: " + e.getMessage(), "qualityScore", 0.0);
        }
    }

    // Capacity Planning Helper Methods
    public Map<String, Object> getCurrentCapacityMetrics(List<String> resources) {
        Map<String, Object> capacityMetrics = new HashMap<>();
        
        for (String resource : resources) {
            try {
                switch (resource) {
                    case "STORAGE":
                        capacityMetrics.put("storage", getCurrentStorageCapacity());
                        break;
                    case "MEMORY":
                        capacityMetrics.put("memory", getCurrentMemoryCapacity());
                        break;
                    case "CPU":
                        capacityMetrics.put("cpu", getCurrentCpuCapacity());
                        break;
                    case "SESSIONS":
                        capacityMetrics.put("sessions", getCurrentSessionCapacity());
                        break;
                    case "CONNECTIONS":
                        capacityMetrics.put("connections", getCurrentConnectionCapacity());
                        break;
                }
            } catch (Exception e) {
                capacityMetrics.put(resource.toLowerCase() + "_error", e.getMessage());
            }
        }
        
        return capacityMetrics;
    }

    private Map<String, Object> getCurrentStorageCapacity() {
        try {
            String sql = """
                SELECT 
                    SUM(total_mb) as total_allocated_mb,
                    SUM(used_mb) as total_used_mb,
                    SUM(free_mb) as total_free_mb,
                    ROUND(AVG(used_percent), 2) as avg_utilization
                FROM (
                    SELECT 
                        ts.tablespace_name,
                        NVL(df.total_mb, 0) as total_mb,
                        NVL(df.total_mb - fs.free_mb, 0) as used_mb,
                        NVL(fs.free_mb, 0) as free_mb,
                        CASE 
                            WHEN df.total_mb > 0 THEN ((df.total_mb - NVL(fs.free_mb, 0)) / df.total_mb) * 100
                            ELSE 0 
                        END as used_percent
                    FROM 
                        dba_tablespaces ts
                        LEFT JOIN (
                            SELECT tablespace_name, SUM(bytes)/1024/1024 as total_mb
                            FROM dba_data_files 
                            GROUP BY tablespace_name
                        ) df ON ts.tablespace_name = df.tablespace_name
                        LEFT JOIN (
                            SELECT tablespace_name, SUM(bytes)/1024/1024 as free_mb
                            FROM dba_free_space 
                            GROUP BY tablespace_name
                        ) fs ON ts.tablespace_name = fs.tablespace_name
                )
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("error", "Storage capacity analysis requires DBA privileges");
        }
    }

    private Map<String, Object> getCurrentMemoryCapacity() {
        try {
            String sql = """
                SELECT 
                    ROUND(SUM(bytes)/1024/1024/1024, 2) as total_sga_gb,
                    COUNT(*) as memory_components
                FROM V$SGAINFO
                """;
            
            Map<String, Object> sgaInfo = jdbcTemplate.queryForMap(sql);
            
            String pgaSql = """
                SELECT 
                    ROUND(value/1024/1024, 2) as pga_target_mb
                FROM V$PARAMETER 
                WHERE name = 'pga_aggregate_target'
                """;
            
            try {
                Map<String, Object> pgaInfo = jdbcTemplate.queryForMap(pgaSql);
                sgaInfo.putAll(pgaInfo);
            } catch (Exception e) {
                sgaInfo.put("pga_info", "Not available");
            }
            
            return sgaInfo;
        } catch (Exception e) {
            return Map.of("error", "Memory capacity analysis requires V$ view access");
        }
    }

    private Map<String, Object> getCurrentCpuCapacity() {
        try {
            String sql = """
                SELECT 
                    value as cpu_count
                FROM V$PARAMETER 
                WHERE name = 'cpu_count'
                """;
            
            Map<String, Object> cpuInfo = jdbcTemplate.queryForMap(sql);
            
            // Get current CPU utilization
            String utilizationSql = """
                SELECT 
                    ROUND(value, 2) as current_cpu_utilization
                FROM V$SYSMETRIC 
                WHERE metric_name = 'Host CPU Utilization (%)'
                AND ROWNUM = 1
                ORDER BY begin_time DESC
                """;
            
            try {
                Map<String, Object> utilizationInfo = jdbcTemplate.queryForMap(utilizationSql);
                cpuInfo.putAll(utilizationInfo);
            } catch (Exception e) {
                cpuInfo.put("utilization_info", "Not available");
            }
            
            return cpuInfo;
        } catch (Exception e) {
            return Map.of("error", "CPU capacity analysis requires V$ view access");
        }
    }

    private Map<String, Object> getCurrentSessionCapacity() {
        try {
            String sql = """
                SELECT 
                    p.value as max_sessions,
                    s.current_sessions,
                    ROUND((s.current_sessions / p.value) * 100, 2) as session_utilization
                FROM 
                    (SELECT value FROM V$PARAMETER WHERE name = 'sessions') p,
                    (SELECT COUNT(*) as current_sessions FROM V$SESSION) s
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("error", "Session capacity analysis requires V$ view access");
        }
    }

    private Map<String, Object> getCurrentConnectionCapacity() {
        try {
            String sql = """
                SELECT 
                    p.value as max_processes,
                    pr.current_processes,
                    ROUND((pr.current_processes / p.value) * 100, 2) as process_utilization
                FROM 
                    (SELECT value FROM V$PARAMETER WHERE name = 'processes') p,
                    (SELECT COUNT(*) as current_processes FROM V$PROCESS) pr
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            return Map.of("error", "Connection capacity analysis requires V$ view access");
        }
    }

    // Security Compliance Helper Methods
    public Map<String, Object> performDetailedSecurityAssessment(List<String> domains) {
        Map<String, Object> assessment = new HashMap<>();
        
        for (String domain : domains) {
            try {
                switch (domain) {
                    case "ACCESS_CONTROL":
                        assessment.put("accessControl", assessAccessControl());
                        break;
                    case "ENCRYPTION":
                        assessment.put("encryption", assessEncryption());
                        break;
                    case "AUDITING":
                        assessment.put("auditing", assessAuditing());
                        break;
                    case "NETWORK":
                        assessment.put("network", assessNetworkSecurity());
                        break;
                }
            } catch (Exception e) {
                assessment.put(domain.toLowerCase() + "_error", e.getMessage());
            }
        }
        
        return assessment;
    }

    private Map<String, Object> assessAccessControl() {
        Map<String, Object> accessControl = new HashMap<>();
        
        try {
            // Analyze user privileges
            String sql = """
                SELECT 
                    grantee,
                    granted_role,
                    admin_option,
                    default_role
                FROM dba_role_privs 
                WHERE grantee NOT IN ('SYS', 'SYSTEM', 'SYSMAN')
                ORDER BY grantee
                """;
            
            List<Map<String, Object>> rolePrivs = jdbcTemplate.queryForList(sql);
            accessControl.put("rolePrivileges", rolePrivs);
            
            // Check for powerful privileges
            String powerfulPrivsSql = """
                SELECT 
                    grantee,
                    privilege,
                    admin_option
                FROM dba_sys_privs 
                WHERE privilege IN ('DBA', 'SYSDBA', 'SYSOPER', 'CREATE ANY TABLE', 'DROP ANY TABLE')
                AND grantee NOT IN ('SYS', 'SYSTEM', 'SYSMAN')
                """;
            
            List<Map<String, Object>> powerfulPrivs = jdbcTemplate.queryForList(powerfulPrivsSql);
            accessControl.put("powerfulPrivileges", powerfulPrivs);
            
            // Calculate access control score
            double score = calculateAccessControlScore(rolePrivs, powerfulPrivs);
            accessControl.put("securityScore", score);
            
            return accessControl;
        } catch (Exception e) {
            return Map.of("error", "Access control assessment requires DBA privileges", "securityScore", 0.0);
        }
    }

    private Map<String, Object> assessEncryption() {
        Map<String, Object> encryption = new HashMap<>();
        
        try {
            // Check for encrypted tablespaces
            String sql = """
                SELECT 
                    tablespace_name,
                    encrypted,
                    CASE WHEN encrypted = 'YES' THEN 1 ELSE 0 END as is_encrypted
                FROM dba_tablespaces
                """;
            
            List<Map<String, Object>> tablespaces = jdbcTemplate.queryForList(sql);
            encryption.put("tablespaces", tablespaces);
            
            long encryptedTablespaces = tablespaces.stream()
                .filter(ts -> "YES".equals(ts.get("encrypted")))
                .count();
            
            double encryptionScore = tablespaces.isEmpty() ? 0.0 :
                ((double) encryptedTablespaces / tablespaces.size()) * 100;
            
            encryption.put("encryptedTablespaces", encryptedTablespaces);
            encryption.put("totalTablespaces", tablespaces.size());
            encryption.put("securityScore", Math.round(encryptionScore * 100.0) / 100.0);
            
            return encryption;
        } catch (Exception e) {
            return Map.of("error", "Encryption assessment requires DBA privileges", "securityScore", 0.0);
        }
    }

    private Map<String, Object> assessAuditing() {
        Map<String, Object> auditing = new HashMap<>();
        
        try {
            // Check audit parameters
            String sql = """
                SELECT name, value 
                FROM V$PARAMETER 
                WHERE name IN ('audit_trail', 'audit_sys_operations')
                """;
            
            List<Map<String, Object>> auditParams = jdbcTemplate.queryForList(sql);
            auditing.put("auditParameters", auditParams);
            
            // Check for audit policies (if available)
            try {
                String policySql = "SELECT policy_name, enabled FROM audit_unified_policies";
                List<Map<String, Object>> auditPolicies = jdbcTemplate.queryForList(policySql);
                auditing.put("auditPolicies", auditPolicies);
            } catch (Exception e) {
                auditing.put("auditPolicies", "Not available or no unified auditing");
            }
            
            double auditScore = calculateAuditScore(auditParams);
            auditing.put("securityScore", auditScore);
            
            return auditing;
        } catch (Exception e) {
            return Map.of("error", "Auditing assessment requires system privileges", "securityScore", 0.0);
        }
    }

    private Map<String, Object> assessNetworkSecurity() {
        Map<String, Object> network = new HashMap<>();
        
        try {
            // Check listener security
            String sql = """
                SELECT name, value 
                FROM V$PARAMETER 
                WHERE name IN ('remote_login_passwordfile', 'sec_case_sensitive_logon')
                """;
            
            List<Map<String, Object>> networkParams = jdbcTemplate.queryForList(sql);
            network.put("networkParameters", networkParams);
            
            double networkScore = calculateNetworkScore(networkParams);
            network.put("securityScore", networkScore);
            
            return network;
        } catch (Exception e) {
            return Map.of("error", "Network security assessment requires system privileges", "securityScore", 0.0);
        }
    }

    // Query Performance Heat Map Helper Methods
    public Map<String, Object> collectQueryPerformanceData(String timeRange, String aggregationLevel, 
                                                          List<String> schemas, String performanceMetric) {
        Map<String, Object> performanceData = new HashMap<>();
        
        try {
            // Build SQL based on available views
            String sql = buildPerformanceQuery(timeRange, aggregationLevel, schemas, performanceMetric);
            List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sql);
            
            performanceData.put("rawData", rawData);
            performanceData.put("dataPoints", rawData.size());
            
            // Aggregate data by time periods
            Map<String, List<Map<String, Object>>> aggregatedData = aggregateByTimePeriod(rawData, aggregationLevel);
            performanceData.put("aggregatedData", aggregatedData);
            
            return performanceData;
        } catch (Exception e) {
            return Map.of("error", "Performance data collection failed: " + e.getMessage());
        }
    }

    private String buildPerformanceQuery(String timeRange, String aggregationLevel, 
                                       List<String> schemas, String performanceMetric) {
        // Fallback to simpler query if V$SQL views are not accessible
        String baseQuery = """
            SELECT 
                SYSDATE as sample_time,
                'SYSTEM' as schema_name,
                COUNT(*) as query_count,
                0 as avg_elapsed_time,
                0 as avg_cpu_time,
                0 as avg_io_time
            FROM dual
            """;
        
        try {
            // Try to use V$SQL for real performance data
            String performanceQuery = """
                SELECT 
                    TRUNC(first_load_time, '%s') as sample_time,
                    parsing_schema_name as schema_name,
                    COUNT(*) as query_count,
                    AVG(elapsed_time/1000000) as avg_elapsed_time,
                    AVG(cpu_time/1000000) as avg_cpu_time,
                    AVG(disk_reads + buffer_gets) as avg_io_operations
                FROM V$SQL 
                WHERE first_load_time >= SYSDATE - %s
                %s
                GROUP BY TRUNC(first_load_time, '%s'), parsing_schema_name
                ORDER BY sample_time DESC, schema_name
                """.formatted(
                    getDateTruncFormat(aggregationLevel),
                    getTimeRangeInterval(timeRange),
                    buildSchemaFilter(schemas),
                    getDateTruncFormat(aggregationLevel)
                );
            
            return performanceQuery;
        } catch (Exception e) {
            return baseQuery;
        }
    }

    // Chart Generation Helper Methods
    public Map<String, Object> generatePerformanceCharts(Map<String, Object> metrics) {
        Map<String, Object> charts = new HashMap<>();
        
        // CPU Usage Chart
        if (metrics.containsKey("cpu")) {
            charts.put("cpuChart", generateCpuChart((Map<String, Object>) metrics.get("cpu")));
        }
        
        // Memory Usage Chart
        if (metrics.containsKey("memory")) {
            charts.put("memoryChart", generateMemoryChart((Map<String, Object>) metrics.get("memory")));
        }
        
        // Wait Events Chart
        if (metrics.containsKey("waits")) {
            charts.put("waitEventsChart", generateWaitEventsChart((Map<String, Object>) metrics.get("waits")));
        }
        
        return charts;
    }

    private Map<String, Object> generateCpuChart(Map<String, Object> cpuMetrics) {
        return Map.of(
            "chartType", "gauge",
            "title", "CPU Utilization",
            "data", Map.of(
                "value", cpuMetrics.getOrDefault("avg_cpu_usage", 0),
                "max", 100,
                "unit", "%"
            ),
            "config", Map.of(
                "colorRanges", List.of(
                    Map.of("from", 0, "to", 70, "color", "green"),
                    Map.of("from", 70, "to", 85, "color", "yellow"),
                    Map.of("from", 85, "to", 100, "color", "red")
                )
            )
        );
    }

    private Map<String, Object> generateMemoryChart(Map<String, Object> memoryMetrics) {
        return Map.of(
            "chartType", "bar",
            "title", "Memory Allocation",
            "data", Map.of(
                "totalMemory", memoryMetrics.getOrDefault("total_memory_gb", 0),
                "unit", "GB"
            )
        );
    }

    private Map<String, Object> generateWaitEventsChart(Map<String, Object> waitMetrics) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> waitEvents = (List<Map<String, Object>>) waitMetrics.get("topWaitEvents");
        
        return Map.of(
            "chartType", "horizontalBar",
            "title", "Top Wait Events",
            "data", waitEvents != null ? waitEvents : new ArrayList<>(),
            "xAxis", "time_waited_ms",
            "yAxis", "event_name"
        );
    }

    // HTML Generation Helper Methods
    public String generateHtmlDashboard(Map<String, Object> dashboardData) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>").append(dashboardData.get("title")).append("</title>");
        html.append("<style>");
        html.append(generateDashboardCSS());
        html.append("</style></head><body>");
        
        html.append("<div class='dashboard-container'>");
        html.append("<h1>").append(dashboardData.get("title")).append("</h1>");
        html.append("<div class='generated-info'>Generated: ").append(dashboardData.get("generatedAt")).append("</div>");
        
        // Add metrics sections
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) dashboardData.get("metrics");
        if (metrics != null) {
            html.append(generateMetricsHtml(metrics));
        }
        
        // Add recommendations if present
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) dashboardData.get("recommendations");
        if (recommendations != null && !recommendations.isEmpty()) {
            html.append("<div class='recommendations'>");
            html.append("<h2>Recommendations</h2><ul>");
            for (String rec : recommendations) {
                html.append("<li>").append(rec).append("</li>");
            }
            html.append("</ul></div>");
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }

    private String generateDashboardCSS() {
        return """
            .dashboard-container { 
                font-family: Arial, sans-serif; 
                margin: 20px; 
                background: #f5f5f5; 
                padding: 20px; 
                border-radius: 8px; 
            }
            .metric-card { 
                background: white; 
                padding: 15px; 
                margin: 10px 0; 
                border-radius: 5px; 
                box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
            }
            .metric-title { 
                font-weight: bold; 
                color: #333; 
                margin-bottom: 10px; 
            }
            .metric-value { 
                font-size: 24px; 
                color: #007acc; 
                font-weight: bold; 
            }
            .recommendations { 
                background: #e8f4f8; 
                padding: 15px; 
                border-left: 4px solid #007acc; 
                margin-top: 20px; 
            }
            """;
    }

    private String generateMetricsHtml(Map<String, Object> metrics) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='metrics-grid'>");
        
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            html.append("<div class='metric-card'>");
            html.append("<div class='metric-title'>").append(formatMetricName(entry.getKey())).append("</div>");
            html.append("<div class='metric-content'>");
            html.append(formatMetricValue(entry.getValue()));
            html.append("</div></div>");
        }
        
        html.append("</div>");
        return html.toString();
    }

    // Utility Helper Methods
    private String getTimeRangeValue(String timeRange) {
        return switch (timeRange) {
            case "LAST_HOUR" -> "1";
            case "LAST_DAY" -> "1";
            case "LAST_WEEK" -> "7";
            case "LAST_MONTH" -> "30";
            default -> "1";
        };
    }

    private String getTimeRangeUnit(String timeRange) {
        return switch (timeRange) {
            case "LAST_HOUR" -> "HOUR";
            case "LAST_DAY" -> "DAY";
            case "LAST_WEEK" -> "DAY";
            case "LAST_MONTH" -> "DAY";
            default -> "DAY";
        };
    }

    private String getTimeRangeInterval(String timeRange) {
        return switch (timeRange) {
            case "LAST_HOUR" -> "1/24";
            case "LAST_DAY" -> "1";
            case "LAST_WEEK" -> "7";
            case "LAST_MONTH" -> "30";
            default -> "1";
        };
    }

    private String getDateTruncFormat(String aggregationLevel) {
        return switch (aggregationLevel) {
            case "HOUR" -> "HH24";
            case "DAY" -> "DD";
            case "WEEK" -> "WW";
            default -> "DD";
        };
    }

    private String buildSchemaFilter(List<String> schemas) {
        if (schemas.isEmpty()) return "";
        
        String schemaList = schemas.stream()
            .map(s -> "'" + s + "'")
            .reduce((a, b) -> a + "," + b)
            .orElse("");
        
        return "AND parsing_schema_name IN (" + schemaList + ")";
    }

    private String formatMetricName(String metricName) {
        return metricName.substring(0, 1).toUpperCase() + 
               metricName.substring(1).toLowerCase().replace("_", " ");
    }

    private String formatMetricValue(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) value;
            StringBuilder formatted = new StringBuilder();
            for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
                formatted.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>");
            }
            return formatted.toString();
        }
        return value != null ? value.toString() : "N/A";
    }

    // Score Calculation Helper Methods
    public double calculateHealthScore(Map<String, Object> healthMetrics) {
        List<Double> scores = new ArrayList<>();
        
        for (Object metric : healthMetrics.values()) {
            if (metric instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metricMap = (Map<String, Object>) metric;
                Object score = metricMap.get("healthScore");
                if (score instanceof Number) {
                    scores.add(((Number) score).doubleValue());
                }
            }
        }
        
        return scores.isEmpty() ? 50.0 : scores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
    }

    public String getHealthGrade(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "GOOD";
        if (score >= 70) return "FAIR";
        if (score >= 60) return "POOR";
        return "CRITICAL";
    }

    public String getQualityGrade(double score) {
        if (score >= 95) return "EXCELLENT";
        if (score >= 85) return "GOOD";
        if (score >= 70) return "ACCEPTABLE";
        if (score >= 50) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }

    public String getComplianceGrade(double score) {
        if (score >= 90) return "FULLY_COMPLIANT";
        if (score >= 75) return "MOSTLY_COMPLIANT";
        if (score >= 60) return "PARTIALLY_COMPLIANT";
        return "NON_COMPLIANT";
    }

    public double calculateTableQualityScore(List<Map<String, Object>> qualityResults) {
        return qualityResults.stream()
            .mapToDouble(result -> {
                Object score = result.get("qualityScore");
                return score instanceof Number ? ((Number) score).doubleValue() : 0.0;
            })
            .average().orElse(0.0);
    }

    private double calculateNullQualityScore(List<Map<String, Object>> nullAnalysis) {
        if (nullAnalysis.isEmpty()) return 100.0;
        
        double avgNullPercent = nullAnalysis.stream()
            .mapToDouble(analysis -> {
                Object percent = analysis.get("nullPercent");
                return percent instanceof Number ? ((Number) percent).doubleValue() : 0.0;
            })
            .average().orElse(0.0);
        
        return Math.max(0, 100 - avgNullPercent);
    }

    private double calculateFormatQualityScore(Map<String, Object> formatStats) {
        int totalValues = ((Number) formatStats.getOrDefault("total_values", 1)).intValue();
        int issues = ((Number) formatStats.getOrDefault("empty_strings", 0)).intValue() +
                    ((Number) formatStats.getOrDefault("untrimmed_values", 0)).intValue() +
                    ((Number) formatStats.getOrDefault("control_chars", 0)).intValue();
        
        if (totalValues == 0) return 100.0;
        double issuePercent = ((double) issues / totalValues) * 100;
        return Math.max(0, 100 - issuePercent);
    }

    private double calculateStorageHealthScore(double avgUsage) {
        if (avgUsage < 70) return 100.0;
        if (avgUsage < 80) return 80.0;
        if (avgUsage < 90) return 60.0;
        if (avgUsage < 95) return 40.0;
        return 20.0;
    }

    private double calculatePerformanceScore(List<Map<String, Object>> metrics) {
        // Simplified performance scoring based on key ratios
        double score = 75.0; // Default baseline
        
        for (Map<String, Object> metric : metrics) {
            String metricName = (String) metric.get("metric_name");
            Number avgValue = (Number) metric.get("avg_value");
            
            if (avgValue != null) {
                double value = avgValue.doubleValue();
                switch (metricName) {
                    case "Buffer Cache Hit Ratio":
                        if (value >= 95) score += 5;
                        else if (value < 85) score -= 10;
                        break;
                    case "Library Cache Hit Ratio":
                        if (value >= 95) score += 5;
                        else if (value < 90) score -= 10;
                        break;
                    case "Database Wait Time Ratio":
                        if (value < 10) score += 5;
                        else if (value > 20) score -= 10;
                        break;
                }
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateSecurityScore(List<Map<String, Object>> passwordPolicies, 
                                        List<Map<String, Object>> userStatus) {
        double score = 50.0; // Baseline
        
        // Check password policies
        for (Map<String, Object> policy : passwordPolicies) {
            String resourceName = (String) policy.get("resource_name");
            String limit = (String) policy.get("limit");
            
            switch (resourceName) {
                case "PASSWORD_LIFE_TIME":
                    if (!"UNLIMITED".equals(limit)) score += 10;
                    break;
                case "PASSWORD_REUSE_MAX":
                    if (!"UNLIMITED".equals(limit)) score += 10;
                    break;
                case "FAILED_LOGIN_ATTEMPTS":
                    if (!"UNLIMITED".equals(limit)) score += 10;
                    break;
            }
        }
        
        // Check account status
        long lockedAccounts = userStatus.stream()
            .filter(status -> "LOCKED".equals(status.get("account_status")) || 
                            "LOCKED(TIMED)".equals(status.get("account_status")))
            .mapToLong(status -> ((Number) status.get("user_count")).longValue())
            .sum();
        
        if (lockedAccounts == 0) score += 10;
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateMaintenanceScore(List<Map<String, Object>> statsInfo) {
        if (statsInfo.isEmpty()) return 50.0;
        
        Date now = new Date();
        long weekAgo = now.getTime() - (7 * 24 * 60 * 60 * 1000);
        
        long recentStats = statsInfo.stream()
            .filter(info -> {
                Object lastAnalyzed = info.get("oldest_stats");
                if (lastAnalyzed instanceof Date) {
                    return ((Date) lastAnalyzed).getTime() > weekAgo;
                }
                return false;
            })
            .count();
        
        double recentPercent = ((double) recentStats / statsInfo.size()) * 100;
        return Math.min(100, recentPercent);
    }

    private double calculateAccessControlScore(List<Map<String, Object>> rolePrivs, 
                                             List<Map<String, Object>> powerfulPrivs) {
        double score = 80.0; // Baseline
        
        // Deduct points for excessive powerful privileges
        if (powerfulPrivs.size() > 5) score -= 20;
        else if (powerfulPrivs.size() > 2) score -= 10;
        
        // Check for admin options
        long adminRoles = rolePrivs.stream()
            .filter(role -> "YES".equals(role.get("admin_option")))
            .count();
        
        if (adminRoles > 3) score -= 15;
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateAuditScore(List<Map<String, Object>> auditParams) {
        double score = 30.0; // Low baseline
        
        for (Map<String, Object> param : auditParams) {
            String name = (String) param.get("name");
            String value = (String) param.get("value");
            
            switch (name) {
                case "audit_trail":
                    if (!"NONE".equals(value)) score += 35;
                    break;
                case "audit_sys_operations":
                    if ("TRUE".equals(value)) score += 35;
                    break;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateNetworkScore(List<Map<String, Object>> networkParams) {
        double score = 50.0; // Baseline
        
        for (Map<String, Object> param : networkParams) {
            String name = (String) param.get("name");
            String value = (String) param.get("value");
            
            switch (name) {
                case "remote_login_passwordfile":
                    if ("EXCLUSIVE".equals(value)) score += 25;
                    break;
                case "sec_case_sensitive_logon":
                    if ("TRUE".equals(value)) score += 25;
                    break;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    public double calculateComplianceScore(Map<String, Object> complianceStatus) {
        // Implementation would depend on specific compliance framework
        // This is a simplified version
        return 75.0; // Placeholder
    }

    // Advanced Helper Methods
    public Map<String, Object> generateQualitySummary(Map<String, List<Map<String, Object>>> qualityResults) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalTables = qualityResults.size();
        int totalChecks = qualityResults.values().stream()
            .mapToInt(List::size)
            .sum();
        
        long passedChecks = qualityResults.values().stream()
            .flatMap(List::stream)
            .filter(check -> {
                Object score = check.get("qualityScore");
                return score instanceof Number && ((Number) score).doubleValue() >= 80.0;
            })
            .count();
        
        summary.put("totalTables", totalTables);
        summary.put("totalChecks", totalChecks);
        summary.put("passedChecks", passedChecks);
        summary.put("passRate", totalChecks > 0 ? 
            Math.round(((double) passedChecks / totalChecks) * 10000.0) / 100.0 : 0.0);
        
        return summary;
    }

    public Map<String, Object> getHistoricalTrends(List<String> resources, String forecastPeriod) {
        Map<String, Object> trends = new HashMap<>();
        
        // This would normally query historical data from AWR or custom monitoring tables
        // For now, we'll simulate trend data
        for (String resource : resources) {
            List<Map<String, Object>> trendData = new ArrayList<>();
            
            // Simulate 30 days of historical data
            for (int i = 30; i >= 0; i--) {
                Date date = new Date(System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L));
                trendData.add(Map.of(
                    "date", date,
                    "value", 50 + (Math.random() * 30), // Simulated growth trend
                    "unit", getResourceUnit(resource)
                ));
            }
            
            trends.put(resource.toLowerCase(), trendData);
        }
        
        return trends;
    }

    public Map<String, Object> generateCapacityForecasts(Map<String, Object> historicalTrends, String forecastPeriod) {
        Map<String, Object> forecasts = new HashMap<>();
        
        int forecastDays = getForecastDays(forecastPeriod);
        
        for (Map.Entry<String, Object> entry : historicalTrends.entrySet()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> trendData = (List<Map<String, Object>>) entry.getValue();
            
            List<Map<String, Object>> forecastData = new ArrayList<>();
            
            // Simple linear forecast based on trend
            if (!trendData.isEmpty()) {
                double lastValue = ((Number) trendData.get(trendData.size() - 1).get("value")).doubleValue();
                double avgGrowth = calculateAverageGrowth(trendData);
                
                for (int i = 1; i <= forecastDays; i++) {
                    Date forecastDate = new Date(System.currentTimeMillis() + (i * 24 * 60 * 60 * 1000L));
                    double forecastValue = lastValue + (avgGrowth * i);
                    
                    forecastData.add(Map.of(
                        "date", forecastDate,
                        "predictedValue", Math.max(0, forecastValue),
                        "confidence", Math.max(0.5, 1.0 - (i * 0.01)) // Decreasing confidence over time
                    ));
                }
            }
            
            forecasts.put(entry.getKey(), forecastData);
        }
        
        return forecasts;
    }

    public List<Map<String, Object>> generateCapacityAlerts(Map<String, Object> currentCapacity, 
                                                           Map<String, Object> forecasts, 
                                                           int alertThreshold) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : forecasts.entrySet()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> forecastData = (List<Map<String, Object>>) entry.getValue();
            
            for (Map<String, Object> forecast : forecastData) {
                double predictedValue = ((Number) forecast.get("predictedValue")).doubleValue();
                
                if (predictedValue > alertThreshold) {
                    alerts.add(Map.of(
                        "resource", entry.getKey(),
                        "alertType", "CAPACITY_WARNING",
                        "forecastDate", forecast.get("date"),
                        "predictedValue", predictedValue,
                        "threshold", alertThreshold,
                        "severity", predictedValue > 90 ? "HIGH" : "MEDIUM",
                        "message", String.format("%s capacity predicted to reach %.1f%% by %s", 
                                 entry.getKey().toUpperCase(), predictedValue, forecast.get("date"))
                    ));
                    break; // Only report first occurrence
                }
            }
        }
        
        return alerts;
    }

    public List<String> generateCapacityRecommendations(Map<String, Object> forecasts, 
                                                        List<Map<String, Object>> alerts) {
        List<String> recommendations = new ArrayList<>();
        
        if (alerts.isEmpty()) {
            recommendations.add("Current capacity planning appears adequate for the forecast period.");
        } else {
            recommendations.add("Consider the following capacity planning actions:");
            
            for (Map<String, Object> alert : alerts) {
                String resource = (String) alert.get("resource");
                String severity = (String) alert.get("severity");
                
                switch (resource.toUpperCase()) {
                    case "STORAGE":
                        if ("HIGH".equals(severity)) {
                            recommendations.add("• URGENT: Plan for additional storage allocation within the next month");
                            recommendations.add("• Consider implementing data archival strategies");
                        } else {
                            recommendations.add("• Monitor storage growth and plan for expansion");
                        }
                        break;
                    case "MEMORY":
                        recommendations.add("• Review SGA/PGA settings and consider memory upgrades");
                        recommendations.add("• Analyze memory-intensive queries for optimization opportunities");
                        break;
                    case "CPU":
                        recommendations.add("• Consider CPU upgrades or adding more cores");
                        recommendations.add("• Review and optimize resource-intensive processes");
                        break;
                    case "SESSIONS":
                        recommendations.add("• Review session parameter settings");
                        recommendations.add("• Implement connection pooling if not already in use");
                        break;
                }
            }
        }
        
        return recommendations;
    }

    public Map<String, Object> generateHeatMapMatrix(Map<String, Object> performanceData, String aggregationLevel) {
        Map<String, Object> matrix = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) performanceData.get("rawData");
        
        if (rawData != null && !rawData.isEmpty()) {
            // Group data by time periods and schemas
            Map<String, Map<String, Double>> timeSchemaMatrix = new HashMap<>();
            Set<String> allSchemas = new HashSet<>();
            Set<String> allTimePeriods = new HashSet<>();
            
            for (Map<String, Object> row : rawData) {
                String timePeriod = formatTimePeriod(row.get("sample_time"), aggregationLevel);
                String schema = (String) row.get("schema_name");
                Double value = ((Number) row.getOrDefault("avg_elapsed_time", 0)).doubleValue();
                
                allTimePeriods.add(timePeriod);
                allSchemas.add(schema);
                
                timeSchemaMatrix.computeIfAbsent(timePeriod, k -> new HashMap<>()).put(schema, value);
            }
            
            matrix.put("timePeriods", new ArrayList<>(allTimePeriods));
            matrix.put("schemas", new ArrayList<>(allSchemas));
            matrix.put("data", timeSchemaMatrix);
            matrix.put("timePeriodsCount", allTimePeriods.size());
            matrix.put("dataPointsCount", rawData.size());
        } else {
            matrix.put("timePeriods", new ArrayList<>());
            matrix.put("schemas", new ArrayList<>());
            matrix.put("data", new HashMap<>());
            matrix.put("timePeriodsCount", 0);
            matrix.put("dataPointsCount", 0);
        }
        
        return matrix;
    }

    public Map<String, Object> generateColorScale(Map<String, Object> performanceData, String performanceMetric) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) performanceData.get("rawData");
        
        if (rawData == null || rawData.isEmpty()) {
            return Map.of("min", 0, "max", 100, "colorStops", List.of());
        }
        
        // Calculate min/max values for color scaling
        double minValue = rawData.stream()
            .mapToDouble(row -> ((Number) row.getOrDefault(getPerformanceColumn(performanceMetric), 0)).doubleValue())
            .min().orElse(0.0);
        
        double maxValue = rawData.stream()
            .mapToDouble(row -> ((Number) row.getOrDefault(getPerformanceColumn(performanceMetric), 0)).doubleValue())
            .max().orElse(100.0);
        
        // Generate color stops
        List<Map<String, Object>> colorStops = List.of(
            Map.of("value", minValue, "color", "#00ff00"), // Green for low values
            Map.of("value", minValue + (maxValue - minValue) * 0.5, "color", "#ffff00"), // Yellow for medium
            Map.of("value", maxValue, "color", "#ff0000") // Red for high values
        );
        
        return Map.of(
            "min", minValue,
            "max", maxValue,
            "colorStops", colorStops,
            "unit", getPerformanceUnit(performanceMetric)
        );
    }

    // Final utility methods
    private String getResourceUnit(String resource) {
        return switch (resource.toUpperCase()) {
            case "STORAGE" -> "GB";
            case "MEMORY" -> "MB";
            case "CPU" -> "%";
            case "SESSIONS", "CONNECTIONS" -> "count";
            default -> "units";
        };
    }

    private int getForecastDays(String forecastPeriod) {
        return switch (forecastPeriod) {
            case "3_MONTHS" -> 90;
            case "6_MONTHS" -> 180;
            case "1_YEAR" -> 365;
            case "2_YEARS" -> 730;
            default -> 90;
        };
    }

    private double calculateAverageGrowth(List<Map<String, Object>> trendData) {
        if (trendData.size() < 2) return 0.0;
        
        double firstValue = ((Number) trendData.get(0).get("value")).doubleValue();
        double lastValue = ((Number) trendData.get(trendData.size() - 1).get("value")).doubleValue();
        
        return (lastValue - firstValue) / trendData.size();
    }

    private String formatTimePeriod(Object timestamp, String aggregationLevel) {
        if (timestamp instanceof Date) {
            Date date = (Date) timestamp;
            return switch (aggregationLevel) {
                case "HOUR" -> new SimpleDateFormat("MM-dd HH:00").format(date);
                case "DAY" -> new SimpleDateFormat("MM-dd").format(date);
                case "WEEK" -> new SimpleDateFormat("yyyy-'W'ww").format(date);
                default -> new SimpleDateFormat("MM-dd").format(date);
            };
        }
        return timestamp.toString();
    }

    private String getPerformanceColumn(String performanceMetric) {
        return switch (performanceMetric) {
            case "ELAPSED_TIME" -> "avg_elapsed_time";
            case "CPU_TIME" -> "avg_cpu_time";
            case "IO_TIME" -> "avg_io_operations";
            case "EXECUTIONS" -> "query_count";
            default -> "avg_elapsed_time";
        };
    }

    private String getPerformanceUnit(String performanceMetric) {
        return switch (performanceMetric) {
            case "ELAPSED_TIME", "CPU_TIME" -> "seconds";
            case "IO_TIME" -> "operations";
            case "EXECUTIONS" -> "count";
            default -> "units";
        };
    }

    public List<String> generatePerformanceRecommendations(Map<String, Object> metrics) {
        List<String> recommendations = new ArrayList<>();
        
        // Analyze metrics and generate recommendations
        if (metrics.containsKey("cpu")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cpuMetrics = (Map<String, Object>) metrics.get("cpu");
            Object avgCpu = cpuMetrics.get("avg_cpu_usage");
            if (avgCpu instanceof Number && ((Number) avgCpu).doubleValue() > 80) {
                recommendations.add("High CPU utilization detected. Consider query optimization or hardware upgrades.");
            }
        }
        
        if (metrics.containsKey("memory")) {
            recommendations.add("Review memory allocation and consider SGA tuning for optimal performance.");
        }
        
        if (metrics.containsKey("waits")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> waitMetrics = (Map<String, Object>) metrics.get("waits");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waitEvents = (List<Map<String, Object>>) waitMetrics.get("topWaitEvents");
            if (waitEvents != null && !waitEvents.isEmpty()) {
                recommendations.add("Review top wait events for performance bottlenecks and optimization opportunities.");
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Database performance metrics are within acceptable ranges.");
        }
        
        return recommendations;
    }

    public Map<String, Object> generateDataQualityCharts(Map<String, List<Map<String, Object>>> qualityResults, 
                                                         Map<String, Object> summary) {
        Map<String, Object> charts = new HashMap<>();
        
        // Overall quality score chart
        charts.put("overallQuality", Map.of(
            "chartType", "donut",
            "title", "Overall Data Quality Score",
            "data", List.of(
                Map.of("label", "Passed", "value", summary.get("passedChecks")),
                Map.of("label", "Failed", "value", 
                    ((Number) summary.get("totalChecks")).intValue() - 
                    ((Number) summary.get("passedChecks")).intValue())
            )
        ));
        
        // Quality by table chart
        List<Map<String, Object>> tableScores = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : qualityResults.entrySet()) {
            double tableScore = calculateTableQualityScore(entry.getValue());
            tableScores.add(Map.of(
                "table", entry.getKey(),
                "score", tableScore,
                "grade", getQualityGrade(tableScore)
            ));
        }
        
        charts.put("qualityByTable", Map.of(
            "chartType", "bar",
            "title", "Data Quality Score by Table",
            "data", tableScores,
            "xAxis", "table",
            "yAxis", "score"
        ));
        
        return charts;
    }

    public Map<String, Object> generateHealthCharts(Map<String, Object> healthMetrics) {
        Map<String, Object> charts = new HashMap<>();
        
        // Health score by category
        List<Map<String, Object>> categoryScores = new ArrayList<>();
        for (Map.Entry<String, Object> entry : healthMetrics.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> categoryData = (Map<String, Object>) entry.getValue();
                Object score = categoryData.get("healthScore");
                if (score instanceof Number) {
                    categoryScores.add(Map.of(
                        "category", entry.getKey(),
                        "score", ((Number) score).doubleValue(),
                        "grade", getHealthGrade(((Number) score).doubleValue())
                    ));
                }
            }
        }
        
        charts.put("healthByCategory", Map.of(
            "chartType", "radar",
            "title", "Database Health by Category",
            "data", categoryScores
        ));
        
        return charts;
    }

    public Map<String, Object> generateCapacityCharts(Map<String, Object> historicalTrends, 
                                                      Map<String, Object> forecasts) {
        Map<String, Object> charts = new HashMap<>();
        
        // Create trend and forecast charts for each resource
        for (String resource : List.of("storage", "memory", "cpu", "sessions")) {
            if (historicalTrends.containsKey(resource)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> historical = (List<Map<String, Object>>) historicalTrends.get(resource);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> forecast = (List<Map<String, Object>>) forecasts.getOrDefault(resource, new ArrayList<>());
                
                charts.put(resource + "Trend", Map.of(
                    "chartType", "line",
                    "title", resource.toUpperCase() + " Capacity Trend and Forecast",
                    "historicalData", historical,
                    "forecastData", forecast,
                    "unit", getResourceUnit(resource)
                ));
            }
        }
        
        return charts;
    }

    public Map<String, Object> generateComplianceCharts(Map<String, Object> complianceStatus, 
                                                        Map<String, Object> securityAssessment) {
        Map<String, Object> charts = new HashMap<>();
        
        // Compliance status overview
        charts.put("complianceOverview", Map.of(
            "chartType", "gauge",
            "title", "Overall Compliance Score",
            "data", Map.of(
                "value", calculateComplianceScore(complianceStatus),
                "max", 100,
                "unit", "%"
            )
        ));
        
        return charts;
    }

    public String generateHealthReportHtml(Map<String, Object> healthReport) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>").append(healthReport.get("reportTitle")).append("</title>");
        html.append("<style>").append(generateDashboardCSS()).append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='dashboard-container'>");
        html.append("<h1>").append(healthReport.get("reportTitle")).append("</h1>");
        html.append("<div class='generated-info'>Generated: ").append(healthReport.get("generatedAt")).append("</div>");
        
        // Overall health score
        html.append("<div class='metric-card'>");
        html.append("<div class='metric-title'>Overall Health Score</div>");
        html.append("<div class='metric-value'>").append(healthReport.get("overallHealthScore")).append("</div>");
        html.append("<div class='metric-grade'>Grade: ").append(healthReport.get("healthGrade")).append("</div>");
        html.append("</div>");
        
        // Add sections
        @SuppressWarnings("unchecked")
        Map<String, Object> sections = (Map<String, Object>) healthReport.get("sections");
        if (sections != null) {
            html.append(generateMetricsHtml(sections));
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }

    public Map<String, Object> mapToComplianceFramework(Map<String, Object> securityAssessment, 
                                                        String complianceFramework) {
        // This would contain specific mapping logic for different compliance frameworks
        // For now, return a simplified mapping
        Map<String, Object> mapping = new HashMap<>();
        
        switch (complianceFramework.toUpperCase()) {
            case "SOX":
                mapping.put("accessControlCompliance", securityAssessment.get("accessControl"));
                mapping.put("auditingCompliance", securityAssessment.get("auditing"));
                break;
            case "GDPR":
                mapping.put("dataProtectionCompliance", securityAssessment.get("encryption"));
                mapping.put("accessControlCompliance", securityAssessment.get("accessControl"));
                break;
            case "HIPAA":
                mapping.put("encryptionCompliance", securityAssessment.get("encryption"));
                mapping.put("accessControlCompliance", securityAssessment.get("accessControl"));
                mapping.put("auditCompliance", securityAssessment.get("auditing"));
                break;
            case "PCI_DSS":
                mapping.put("networkSecurityCompliance", securityAssessment.get("network"));
                mapping.put("encryptionCompliance", securityAssessment.get("encryption"));
                mapping.put("accessControlCompliance", securityAssessment.get("accessControl"));
                break;
            default:
                mapping.put("generalCompliance", securityAssessment);
                break;
        }
        
        return mapping;
    }

    public Map<String, Object> generateSecurityRiskMatrix(Map<String, Object> securityAssessment, String riskTolerance) {
        Map<String, Object> riskMatrix = new HashMap<>();
        List<Map<String, Object>> risks = new ArrayList<>();
        
        // Analyze each security domain for risks
        for (Map.Entry<String, Object> entry : securityAssessment.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> domainData = (Map<String, Object>) entry.getValue();
                Object score = domainData.get("securityScore");
                
                if (score instanceof Number) {
                    double securityScore = ((Number) score).doubleValue();
                    String riskLevel = calculateRiskLevel(securityScore, riskTolerance);
                    
                    risks.add(Map.of(
                        "domain", entry.getKey(),
                        "securityScore", securityScore,
                        "riskLevel", riskLevel,
                        "impact", calculateRiskImpact(entry.getKey(), securityScore),
                        "likelihood", calculateRiskLikelihood(securityScore)
                    ));
                }
            }
        }
        
        riskMatrix.put("risks", risks);
        riskMatrix.put("riskTolerance", riskTolerance);
        riskMatrix.put("highRiskCount", risks.stream().filter(r -> "HIGH".equals(r.get("riskLevel"))).count());
        riskMatrix.put("mediumRiskCount", risks.stream().filter(r -> "MEDIUM".equals(r.get("riskLevel"))).count());
        riskMatrix.put("lowRiskCount", risks.stream().filter(r -> "LOW".equals(r.get("riskLevel"))).count());
        
        return riskMatrix;
    }

    public List<Map<String, Object>> generateRemediationPlan(Map<String, Object> complianceStatus, String riskTolerance) {
        List<Map<String, Object>> remediationPlan = new ArrayList<>();
        
        // Generate remediation items based on compliance gaps
        for (Map.Entry<String, Object> entry : complianceStatus.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> complianceData = (Map<String, Object>) entry.getValue();
                Object score = complianceData.get("securityScore");
                
                if (score instanceof Number && ((Number) score).doubleValue() < 70) {
                    remediationPlan.addAll(generateDomainRemediationItems(entry.getKey(), (Map<String, Object>) entry.getValue()));
                }
            }
        }
        
        // Sort by priority
        remediationPlan.sort((a, b) -> {
            String priorityA = (String) a.get("priority");
            String priorityB = (String) b.get("priority");
            return getPriorityWeight(priorityA) - getPriorityWeight(priorityB);
        });
        
        return remediationPlan;
    }

    public Map<String, Object> identifyTopQueries(Map<String, Object> performanceData, String performanceMetric) {
        Map<String, Object> topQueries = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) performanceData.get("rawData");
        
        if (rawData != null && !rawData.isEmpty()) {
            // Sort by performance metric and get top 10
            List<Map<String, Object>> sortedQueries = rawData.stream()
                .sorted((a, b) -> {
                    double valueA = ((Number) a.getOrDefault(getPerformanceColumn(performanceMetric), 0)).doubleValue();
                    double valueB = ((Number) b.getOrDefault(getPerformanceColumn(performanceMetric), 0)).doubleValue();
                    return Double.compare(valueB, valueA); // Descending order
                })
                .limit(10)
                .toList();
            
            topQueries.put("topPerformers", sortedQueries);
            topQueries.put("metric", performanceMetric);
            topQueries.put("count", sortedQueries.size());
        } else {
            topQueries.put("topPerformers", new ArrayList<>());
            topQueries.put("count", 0);
        }
        
        return topQueries;
    }

    public Map<String, Object> generateHeatMapChartConfig(Map<String, Object> heatMatrix, Map<String, Object> colorScale) {
        Map<String, Object> config = new HashMap<>();
        
        // Chart.js configuration for heat map
        config.put("type", "scatter");
        config.put("options", Map.of(
            "scales", Map.of(
                "x", Map.of(
                    "type", "category",
                    "title", Map.of("display", true, "text", "Time Periods")
                ),
                "y", Map.of(
                    "type", "category", 
                    "title", Map.of("display", true, "text", "Schemas")
                )
            ),
            "plugins", Map.of(
                "legend", Map.of("display", false),
                "title", Map.of(
                    "display", true,
                    "text", "Query Performance Heat Map"
                )
            )
        ));
        
        // Data transformation for heat map visualization
        @SuppressWarnings("unchecked")
        List<String> timePeriods = (List<String>) heatMatrix.get("timePeriods");
        @SuppressWarnings("unchecked")
        List<String> schemas = (List<String>) heatMatrix.get("schemas");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> data = (Map<String, Map<String, Double>>) heatMatrix.get("data");
        
        List<Map<String, Object>> chartData = new ArrayList<>();
        
        for (int timeIndex = 0; timeIndex < timePeriods.size(); timeIndex++) {
            String timePeriod = timePeriods.get(timeIndex);
            for (int schemaIndex = 0; schemaIndex < schemas.size(); schemaIndex++) {
                String schema = schemas.get(schemaIndex);
                Double value = data.getOrDefault(timePeriod, new HashMap<>()).get(schema);
                
                if (value != null) {
                    chartData.add(Map.of(
                        "x", timeIndex,
                        "y", schemaIndex,
                        "v", value,
                        "timePeriod", timePeriod,
                        "schema", schema
                    ));
                }
            }
        }
        
        config.put("data", Map.of(
            "datasets", List.of(Map.of(
                "label", "Performance Data",
                "data", chartData,
                "backgroundColor", generateHeatMapColors(chartData, colorScale)
            ))
        ));
        
        return config;
    }

    private Map<String, List<Map<String, Object>>> aggregateByTimePeriod(List<Map<String, Object>> rawData, String aggregationLevel) {
        Map<String, List<Map<String, Object>>> aggregated = new HashMap<>();
        
        for (Map<String, Object> row : rawData) {
            String timePeriod = formatTimePeriod(row.get("sample_time"), aggregationLevel);
            aggregated.computeIfAbsent(timePeriod, k -> new ArrayList<>()).add(row);
        }
        
        return aggregated;
    }

    // Risk Assessment Helper Methods
    private String calculateRiskLevel(double securityScore, String riskTolerance) {
        double threshold = switch (riskTolerance.toUpperCase()) {
            case "LOW" -> 90.0;    // Low risk tolerance requires high security scores
            case "MEDIUM" -> 75.0; // Medium risk tolerance
            case "HIGH" -> 60.0;   // High risk tolerance allows lower security scores
            default -> 75.0;
        };
        
        if (securityScore >= threshold) return "LOW";
        if (securityScore >= threshold - 20) return "MEDIUM";
        return "HIGH";
    }

    private String calculateRiskImpact(String domain, double securityScore) {
        // Domain-specific impact assessment
        String baseImpact = switch (domain.toLowerCase()) {
            case "accesscontrol" -> "HIGH";      // Access control issues have high impact
            case "encryption" -> "HIGH";         // Encryption issues have high impact  
            case "auditing" -> "MEDIUM";         // Auditing issues have medium impact
            case "network" -> "MEDIUM";          // Network issues have medium impact
            default -> "MEDIUM";
        };
        
        // Adjust based on security score
        if (securityScore < 40) {
            return baseImpact.equals("MEDIUM") ? "HIGH" : "CRITICAL";
        }
        
        return baseImpact;
    }

    private String calculateRiskLikelihood(double securityScore) {
        if (securityScore >= 80) return "LOW";
        if (securityScore >= 60) return "MEDIUM"; 
        if (securityScore >= 40) return "HIGH";
        return "VERY_HIGH";
    }

    private List<Map<String, Object>> generateDomainRemediationItems(String domain, Map<String, Object> domainData) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        switch (domain.toLowerCase()) {
            case "accesscontrol":
                items.add(createRemediationItem(
                    "Review and minimize privileged user accounts",
                    "Conduct audit of users with DBA, SYSDBA, or powerful system privileges",
                    "HIGH", "SECURITY", 2
                ));
                items.add(createRemediationItem(
                    "Implement role-based access control",
                    "Create granular roles instead of granting system privileges directly",
                    "MEDIUM", "SECURITY", 4
                ));
                break;
                
            case "encryption":
                items.add(createRemediationItem(
                    "Enable Transparent Data Encryption (TDE)",
                    "Encrypt sensitive tablespaces and data files",
                    "HIGH", "SECURITY", 3
                ));
                items.add(createRemediationItem(
                    "Implement network encryption",
                    "Configure SSL/TLS for client-server communication",
                    "MEDIUM", "SECURITY", 2
                ));
                break;
                
            case "auditing":
                items.add(createRemediationItem(
                    "Enable database auditing",
                    "Configure audit_trail parameter and enable unified auditing",
                    "MEDIUM", "COMPLIANCE", 1
                ));
                items.add(createRemediationItem(
                    "Implement fine-grained auditing",
                    "Set up auditing for sensitive data access and privilege usage",
                    "LOW", "COMPLIANCE", 6
                ));
                break;
                
            case "network":
                items.add(createRemediationItem(
                    "Secure listener configuration",
                    "Review and harden Oracle Net Listener security settings",
                    "MEDIUM", "SECURITY", 2
                ));
                items.add(createRemediationItem(
                    "Implement connection filtering",
                    "Configure valid node checking and connection rate limiting",
                    "LOW", "SECURITY", 4
                ));
                break;
        }
        
        return items;
    }

    private Map<String, Object> createRemediationItem(String title, String description, 
                                                     String priority, String category, int estimatedWeeks) {
        return Map.of(
            "title", title,
            "description", description,
            "priority", priority,
            "category", category,
            "estimatedWeeks", estimatedWeeks,
            "status", "PENDING",
            "createdDate", new Date()
        );
    }

    private int getPriorityWeight(String priority) {
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            case "LOW" -> 4;
            default -> 5;
        };
    }

    private List<String> generateHeatMapColors(List<Map<String, Object>> chartData, Map<String, Object> colorScale) {
        double minValue = ((Number) colorScale.get("min")).doubleValue();
        double maxValue = ((Number) colorScale.get("max")).doubleValue();
        
        return chartData.stream()
            .map(point -> {
                double value = ((Number) point.get("v")).doubleValue();
                double normalizedValue = maxValue > minValue ? (value - minValue) / (maxValue - minValue) : 0.5;
                
                // Generate color based on normalized value (0.0 to 1.0)
                if (normalizedValue <= 0.33) {
                    return String.format("rgba(0, 255, 0, %.2f)", 0.3 + normalizedValue * 0.4); // Green
                } else if (normalizedValue <= 0.66) {
                    return String.format("rgba(255, 255, 0, %.2f)", 0.3 + normalizedValue * 0.4); // Yellow
                } else {
                    return String.format("rgba(255, 0, 0, %.2f)", 0.3 + normalizedValue * 0.4); // Red
                }
            })
            .toList();
    }

    // Additional utility methods for completeness
    @SuppressWarnings("unused")
    private Map<String, Object> generateExecutiveSummary(Map<String, Object> dashboardData) {
        Map<String, Object> summary = new HashMap<>();
        
        // Extract key metrics for executive summary
        summary.put("reportType", dashboardData.get("title"));
        summary.put("generatedAt", dashboardData.get("generatedAt"));
        
        // Performance summary
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) dashboardData.get("metrics");
        if (metrics != null) {
            summary.put("keyFindings", generateKeyFindings(metrics));
            summary.put("criticalIssues", identifyCriticalIssues(metrics));
            summary.put("overallStatus", determineOverallStatus(metrics));
        }
        
        return summary;
    }

    private List<String> generateKeyFindings(Map<String, Object> metrics) {
        List<String> findings = new ArrayList<>();
        
        // Analyze metrics for key findings
        if (metrics.containsKey("cpu")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cpuData = (Map<String, Object>) metrics.get("cpu");
            Object avgCpu = cpuData.get("avg_cpu_usage");
            if (avgCpu instanceof Number) {
                double cpuValue = ((Number) avgCpu).doubleValue();
                if (cpuValue > 80) {
                    findings.add("High CPU utilization detected (" + cpuValue + "%)");
                } else if (cpuValue < 30) {
                    findings.add("Low CPU utilization indicates potential resource optimization opportunities");
                }
            }
        }
        
        if (metrics.containsKey("memory")) {
            findings.add("Memory allocation within expected parameters");
        }
        
        if (findings.isEmpty()) {
            findings.add("All monitored metrics are within acceptable ranges");
        }
        
        return findings;
    }

    private List<String> identifyCriticalIssues(Map<String, Object> metrics) {
        List<String> issues = new ArrayList<>();
        
        // Check for critical performance issues
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metricData = (Map<String, Object>) entry.getValue();
                
                if (metricData.containsKey("error")) {
                    issues.add("Data collection issue in " + entry.getKey() + ": " + metricData.get("error"));
                }
            }
        }
        
        return issues;
    }

    private String determineOverallStatus(Map<String, Object> metrics) {
        // Simple status determination based on available data
        long errorCount = metrics.values().stream()
            .filter(value -> value instanceof Map)
            .map(value -> (Map<?, ?>) value)
            .mapToLong(map -> map.containsKey("error") ? 1 : 0)
            .sum();
        
        if (errorCount == 0) {
            return "HEALTHY";
        } else if (errorCount <= metrics.size() / 2) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    // JSON export utility
    @Tool(name = "exportVisualizationData", 
          description = "Export visualization data in various formats for external tools")
    public Map<String, Object> exportVisualizationData(
            String reportType,              // PERFORMANCE, HEALTH, QUALITY, CAPACITY, SECURITY
            String exportFormat,            // JSON, CSV, XML
            Map<String, Object> reportData,
            boolean includeChartConfigs) {
        
        try {
            Map<String, Object> exportResult = new HashMap<>();
            exportResult.put("exportFormat", exportFormat);
            exportResult.put("reportType", reportType);
            exportResult.put("exportedAt", new Date());
            
            // Format data based on export type
            String formattedData = switch (exportFormat.toUpperCase()) {
                case "JSON" -> formatAsJson(reportData, includeChartConfigs);
                case "CSV" -> formatAsCsv(reportData);
                case "XML" -> formatAsXml(reportData);
                default -> reportData.toString();
            };
            
            exportResult.put("data", formattedData);
            exportResult.put("size", formattedData.length());
            
            return Map.of(
                "status", "success",
                "export", exportResult
            );
            
        } catch (Exception e) {
            logger.error("Error exporting visualization data", e);
            return Map.of(
                "status", "error",
                "message", "Failed to export data: " + e.getMessage()
            );
        }
    }

    private String formatAsJson(Map<String, Object> data, boolean includeChartConfigs) {
        // In a real implementation, you would use a JSON library like Jackson
        // This is a simplified version for demonstration
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",\n");
            json.append("  \"").append(entry.getKey()).append("\": ");
            
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Number) {
                json.append(entry.getValue());
            } else {
                json.append("\"").append(entry.getValue().toString()).append("\"");
            }
            
            first = false;
        }
        
        json.append("\n}");
        return json.toString();
    }

    private String formatAsCsv(Map<String, Object> data) {
        StringBuilder csv = new StringBuilder();
        csv.append("Key,Value,Type\n");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            csv.append(entry.getKey()).append(",");
            csv.append(entry.getValue()).append(",");
            csv.append(entry.getValue().getClass().getSimpleName()).append("\n");
        }
        
        return csv.toString();
    }

    private String formatAsXml(Map<String, Object> data) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<report>\n");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            xml.append("  <").append(entry.getKey()).append(">");
            xml.append(entry.getValue());
            xml.append("</").append(entry.getKey()).append(">\n");
        }
        
        xml.append("</report>");
        return xml.toString();
    }

}
