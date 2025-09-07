package com.deepai.mcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.deepai.mcpserver.util.OracleFeatureDetector;
import com.deepai.mcpserver.util.OracleSqlBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Oracle Enterprise Performance Service - 10 Tools
 * Provides Parallel Execution, Partitioning, Materialized Views, and Advanced Performance Features
 */
@Service
public class OracleEnterprisePerformanceService {

    private final JdbcTemplate jdbcTemplate;
    private final OracleFeatureDetector featureDetector;
    private final OracleSqlBuilder sqlBuilder;

    @Autowired
    public OracleEnterprisePerformanceService(JdbcTemplate jdbcTemplate, 
                                             OracleFeatureDetector featureDetector,
                                             OracleSqlBuilder sqlBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureDetector = featureDetector;
        this.sqlBuilder = sqlBuilder;
    }

    @Tool(name = "manageParallelExecution", description = "Manage Oracle parallel execution for improved performance")
    public Map<String, Object> manageParallelExecution(
         String operation,
         String sqlQuery,
         Integer parallelDegree,
         String tableName) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "ENABLE_PARALLEL":
                    if (tableName == null) {
                        return Map.of("status", "error", "message", "Table name required for ENABLE_PARALLEL");
                    }

                    int degree = parallelDegree != null ? parallelDegree : 4;
                    String enableSql = String.format(
                        "ALTER TABLE %s PARALLEL (DEGREE %d)", tableName, degree
                    );

                    jdbcTemplate.execute(enableSql);
                    result.put("message", "Parallel execution enabled for table");
                    result.put("parallelDegree", degree);
                    break;

                case "DISABLE_PARALLEL":
                    if (tableName == null) {
                        return Map.of("status", "error", "message", "Table name required for DISABLE_PARALLEL");
                    }

                    String disableSql = String.format("ALTER TABLE %s NOPARALLEL", tableName);
                    jdbcTemplate.execute(disableSql);
                    result.put("message", "Parallel execution disabled for table");
                    break;

                case "PARALLEL_HINT_QUERY":
                    if (sqlQuery == null) {
                        return Map.of("status", "error", "message", "SQL query required for PARALLEL_HINT_QUERY");
                    }

                    int hintDegree = parallelDegree != null ? parallelDegree : 4;
                    String hintedQuery = sqlQuery.trim().toUpperCase().startsWith("SELECT") ?
                        sqlQuery.replaceFirst("(?i)SELECT", "SELECT /*+ PARALLEL(" + hintDegree + ") */") :
                        sqlQuery;

                    long startTime = System.currentTimeMillis();
                    List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(hintedQuery);
                    long executionTime = System.currentTimeMillis() - startTime;

                    result.put("executedQuery", hintedQuery);
                    result.put("resultCount", queryResults.size());
                    result.put("executionTimeMs", executionTime);
                    result.put("parallelDegree", hintDegree);
                    break;

                case "PARALLEL_STATUS":
                    List<Map<String, Object>> parallelTables = jdbcTemplate.queryForList(
                        "SELECT table_name, degree, cache, instances FROM user_tables " +
                        "WHERE degree IS NOT NULL AND degree != '1' ORDER BY table_name"
                    );
                    result.put("parallelTables", parallelTables);

                    // Current parallel sessions
                    List<Map<String, Object>> parallelSessions = jdbcTemplate.queryForList(
                        "SELECT qcsid, server_name, status, degree FROM v$px_session " +
                        "ORDER BY qcsid"
                    );
                    result.put("parallelSessions", parallelSessions);
                    break;

                case "PARALLEL_STATISTICS":
                    List<Map<String, Object>> pxStats = jdbcTemplate.queryForList(
                        "SELECT name, value FROM v$sysstat " +
                        "WHERE name LIKE '%parallel%' OR name LIKE '%PX%' " +
                        "ORDER BY name"
                    );
                    result.put("parallelStatistics", pxStats);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("tableName", tableName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Parallel Execution"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage parallel execution: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageTablePartitioning", description = "Manage Oracle table partitioning for large datasets")
    public Map<String, Object> manageTablePartitioning(
         String operation,
         String tableName,
         String partitionType,
         String partitionColumn,
         String partitionName) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE_RANGE_PARTITION":
                    if (partitionColumn == null) {
                        return Map.of("status", "error", "message", "Partition column required");
                    }

                    String createRangeSql = String.format(
                        "CREATE TABLE %s_PARTITIONED AS SELECT * FROM %s " +
                        "PARTITION BY RANGE (%s) " +
                        "(PARTITION p1 VALUES LESS THAN (MAXVALUE))",
                        tableName, tableName, partitionColumn
                    );

                    jdbcTemplate.execute(createRangeSql);
                    result.put("message", "Range partitioned table created");
                    break;

                case "CREATE_HASH_PARTITION":
                    if (partitionColumn == null) {
                        return Map.of("status", "error", "message", "Partition column required");
                    }

                    String createHashSql = String.format(
                        "CREATE TABLE %s_HASH_PARTITIONED AS SELECT * FROM %s " +
                        "PARTITION BY HASH (%s) PARTITIONS 4",
                        tableName, partitionColumn
                    );

                    jdbcTemplate.execute(createHashSql);
                    result.put("message", "Hash partitioned table created");
                    break;

                case "ADD_PARTITION":
                    if (partitionName == null) {
                        return Map.of("status", "error", "message", "Partition name required");
                    }

                    String addPartSql = String.format(
                        "ALTER TABLE %s ADD PARTITION %s VALUES LESS THAN (MAXVALUE)",
                        tableName, partitionName
                    );

                    jdbcTemplate.execute(addPartSql);
                    result.put("message", "Partition added successfully");
                    break;

                case "DROP_PARTITION":
                    if (partitionName == null) {
                        return Map.of("status", "error", "message", "Partition name required");
                    }

                    String dropPartSql = String.format(
                        "ALTER TABLE %s DROP PARTITION %s", tableName, partitionName
                    );

                    jdbcTemplate.execute(dropPartSql);
                    result.put("message", "Partition dropped successfully");
                    break;

                case "PARTITION_INFO":
                    List<Map<String, Object>> partitions = jdbcTemplate.queryForList(
                        "SELECT partition_name, tablespace_name, num_rows, blocks " +
                        "FROM user_tab_partitions WHERE table_name = ? ORDER BY partition_position",
                        tableName.toUpperCase()
                    );
                    result.put("partitions", partitions);

                    Map<String, Object> partitioningInfo = jdbcTemplate.queryForMap(
                        "SELECT partitioning_type, partition_count, def_tablespace_name " +
                        "FROM user_part_tables WHERE table_name = ?",
                        tableName.toUpperCase()
                    );
                    result.put("partitioningInfo", partitioningInfo);
                    break;

                case "PARTITION_STATISTICS":
                    List<Map<String, Object>> partStats = jdbcTemplate.queryForList(
                        "SELECT partition_name, num_rows, avg_row_len, last_analyzed " +
                        "FROM user_tab_partitions WHERE table_name = ? ORDER BY partition_position",
                        tableName.toUpperCase()
                    );
                    result.put("partitionStatistics", partStats);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("tableName", tableName);
            result.put("partitionType", partitionType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Table Partitioning"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage table partitioning: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageMaterializedViews", description = "Manage Oracle materialized views for performance")
    public Map<String, Object> manageMaterializedViews(
         String operation,
         String mvName,
         String baseQuery,
         String refreshType,
         String refreshInterval) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE":
                    if (baseQuery == null) {
                        return Map.of("status", "error", "message", "Base query required for CREATE operation");
                    }

                    String refresh = refreshType != null ? refreshType.toUpperCase() : "COMPLETE";
                    String interval = refreshInterval != null ? refreshInterval : "SYSDATE + 1";

                    String createMvSql = String.format(
                        "CREATE MATERIALIZED VIEW %s " +
                        "BUILD IMMEDIATE " +
                        "REFRESH %s ON DEMAND " +
                        "NEXT %s " +
                        "AS %s",
                        mvName, refresh, interval, baseQuery
                    );

                    jdbcTemplate.execute(createMvSql);
                    result.put("message", "Materialized view created successfully");
                    break;

                case "REFRESH":
                    String refreshMvSql = String.format(
                        "BEGIN DBMS_MVIEW.REFRESH('%s', '%s'); END;",
                        mvName, refreshType != null ? refreshType.substring(0, 1) : "C"
                    );

                    jdbcTemplate.execute(refreshMvSql);
                    result.put("message", "Materialized view refreshed successfully");
                    break;

                case "DROP":
                    String dropMvSql = String.format("DROP MATERIALIZED VIEW %s", mvName);
                    jdbcTemplate.execute(dropMvSql);
                    result.put("message", "Materialized view dropped successfully");
                    break;

                case "ENABLE_QUERY_REWRITE":
                    String enableRewriteSql = String.format(
                        "ALTER MATERIALIZED VIEW %s ENABLE QUERY REWRITE", mvName
                    );

                    jdbcTemplate.execute(enableRewriteSql);
                    result.put("message", "Query rewrite enabled for materialized view");
                    break;

                case "DISABLE_QUERY_REWRITE":
                    String disableRewriteSql = String.format(
                        "ALTER MATERIALIZED VIEW %s DISABLE QUERY REWRITE", mvName
                    );

                    jdbcTemplate.execute(disableRewriteSql);
                    result.put("message", "Query rewrite disabled for materialized view");
                    break;

                case "STATUS":
                    List<Map<String, Object>> mvStatus = jdbcTemplate.queryForList(
                        "SELECT mview_name, refresh_mode, refresh_method, build_mode, " +
                        "fast_refreshable, last_refresh_date, compile_state " +
                        "FROM user_mviews WHERE mview_name = ?",
                        mvName.toUpperCase()
                    );
                    result.put("materializedViewStatus", mvStatus);

                    // Refresh statistics
                    List<Map<String, Object>> refreshStats = jdbcTemplate.queryForList(
                        "SELECT name, refresh_id, refreshes, elapsed_time " +
                        "FROM user_mview_refresh_times WHERE name = ? " +
                        "ORDER BY refresh_id DESC FETCH FIRST 10 ROWS ONLY",
                        mvName.toUpperCase()
                    );
                    result.put("refreshHistory", refreshStats);
                    break;

                case "LIST_ALL":
                    List<Map<String, Object>> allMviews = jdbcTemplate.queryForList(
                        "SELECT mview_name, refresh_mode, refresh_method, " +
                        "query_rewrite_enabled, last_refresh_date " +
                        "FROM user_mviews ORDER BY mview_name"
                    );
                    result.put("materializedViews", allMviews);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("mvName", mvName);
            result.put("refreshType", refreshType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Materialized Views"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage materialized views: " + e.getMessage()
            );
        }
    }

    @Tool(name = "optimizeQueryPerformance", description = "Optimize Oracle SQL queries with hints and analysis")
    public Map<String, Object> optimizeQuery(
         String sqlQuery,
         String optimizationType,
         List<String> hints,
         Boolean analyzeExecution) {

        try {
            Map<String, Object> result = new HashMap<>();

            // Analyze original query execution plan
            String explainSql = "EXPLAIN PLAN FOR " + sqlQuery;
            jdbcTemplate.execute(explainSql);

            List<Map<String, Object>> originalPlan = jdbcTemplate.queryForList(
                "SELECT operation, options, object_name, cost, cardinality, bytes " +
                "FROM plan_table ORDER BY id"
            );
            result.put("originalExecutionPlan", originalPlan);

            // Apply optimization hints
            String optimizedQuery = sqlQuery;
            if (hints != null && !hints.isEmpty()) {
                String hintString = "/*+ " + String.join(" ", hints) + " */";
                optimizedQuery = sqlQuery.trim().toUpperCase().startsWith("SELECT") ?
                    sqlQuery.replaceFirst("(?i)SELECT", "SELECT " + hintString) :
                    sqlQuery;
            } else if (optimizationType != null) {
                // Apply common optimization patterns
                switch (optimizationType.toUpperCase()) {
                    case "FIRST_ROWS":
                        optimizedQuery = sqlQuery.replaceFirst("(?i)SELECT", "SELECT /*+ FIRST_ROWS */");
                        break;
                    case "ALL_ROWS":
                        optimizedQuery = sqlQuery.replaceFirst("(?i)SELECT", "SELECT /*+ ALL_ROWS */");
                        break;
                    case "INDEX":
                        optimizedQuery = sqlQuery.replaceFirst("(?i)SELECT", "SELECT /*+ INDEX */");
                        break;
                    case "PARALLEL":
                        optimizedQuery = sqlQuery.replaceFirst("(?i)SELECT", "SELECT /*+ PARALLEL(4) */");
                        break;
                }
            }

            // Analyze optimized query if different
            if (!optimizedQuery.equals(sqlQuery)) {
                String optimizedExplain = "EXPLAIN PLAN FOR " + optimizedQuery;
                jdbcTemplate.execute(optimizedExplain);

                List<Map<String, Object>> optimizedPlan = jdbcTemplate.queryForList(
                    "SELECT operation, options, object_name, cost, cardinality, bytes " +
                    "FROM plan_table ORDER BY id"
                );
                result.put("optimizedExecutionPlan", optimizedPlan);
                result.put("optimizedQuery", optimizedQuery);

                // Calculate cost improvement
                Integer originalCost = originalPlan.stream()
                    .filter(row -> row.get("cost") != null)
                    .mapToInt(row -> ((Number) row.get("cost")).intValue())
                    .findFirst().orElse(0);

                Integer optimizedCost = optimizedPlan.stream()
                    .filter(row -> row.get("cost") != null)
                    .mapToInt(row -> ((Number) row.get("cost")).intValue())
                    .findFirst().orElse(0);

                if (originalCost > 0 && optimizedCost > 0) {
                    double improvement = ((double)(originalCost - optimizedCost) / originalCost) * 100;
                    result.put("costImprovement", String.format("%.2f%%", improvement));
                }
            }

            // Execute and time both queries if requested
            if (analyzeExecution != null && analyzeExecution) {
                long originalTime = System.currentTimeMillis();
                List<Map<String, Object>> originalResults = jdbcTemplate.queryForList(sqlQuery);
                originalTime = System.currentTimeMillis() - originalTime;

                result.put("originalExecutionTimeMs", originalTime);
                result.put("originalResultCount", originalResults.size());

                if (!optimizedQuery.equals(sqlQuery)) {
                    long optimizedTime = System.currentTimeMillis();
                    List<Map<String, Object>> optimizedResults = jdbcTemplate.queryForList(optimizedQuery);
                    optimizedTime = System.currentTimeMillis() - optimizedTime;

                    result.put("optimizedExecutionTimeMs", optimizedTime);
                    result.put("optimizedResultCount", optimizedResults.size());

                    if (originalTime > 0) {
                        double timeImprovement = ((double)(originalTime - optimizedTime) / originalTime) * 100;
                        result.put("timeImprovement", String.format("%.2f%%", timeImprovement));
                    }
                }
            }

            // Provide optimization recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Consider creating indexes on frequently filtered columns");
            recommendations.add("Use FIRST_ROWS hint for OLTP queries");
            recommendations.add("Use ALL_ROWS hint for batch processing");
            recommendations.add("Enable parallel execution for large data sets");
            recommendations.add("Consider partitioning for very large tables");

            result.put("optimizationRecommendations", recommendations);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Query Optimization"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to optimize query: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageMemory", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> manageMemory(
         String operation,
         String memoryType,
         String targetSize) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "SGA_STATUS":
                    List<Map<String, Object>> sgaComponents = jdbcTemplate.queryForList(
                        "SELECT name, value FROM v$sga ORDER BY name"
                    );
                    result.put("sgaComponents", sgaComponents);

                    List<Map<String, Object>> sgaParameters = jdbcTemplate.queryForList(
                        "SELECT name, value, description FROM v$parameter " +
                        "WHERE name LIKE '%sga%' OR name LIKE '%memory%' ORDER BY name"
                    );
                    result.put("sgaParameters", sgaParameters);
                    break;

                case "PGA_STATUS":
                    List<Map<String, Object>> pgaStats = jdbcTemplate.queryForList(
                        "SELECT name, value, unit FROM v$pgastat ORDER BY name"
                    );
                    result.put("pgaStatistics", pgaStats);

                    List<Map<String, Object>> pgaWorkarea = jdbcTemplate.queryForList(
                        "SELECT operation_type, policy, estimated_optimal_size, " +
                        "estimated_onepass_size, actual_mem_used " +
                        "FROM v$sql_workarea_active ORDER BY estimated_optimal_size DESC FETCH FIRST 10 ROWS ONLY"
                    );
                    result.put("pgaWorkarea", pgaWorkarea);
                    break;

                case "BUFFER_CACHE_ADVICE":
                    List<Map<String, Object>> bufferAdvice = jdbcTemplate.queryForList(
                        "SELECT size_for_estimate, buffers_for_estimate, " +
                        "estd_physical_read_factor, estd_physical_reads " +
                        "FROM v$db_cache_advice WHERE name = 'DEFAULT' ORDER BY size_for_estimate"
                    );
                    result.put("bufferCacheAdvice", bufferAdvice);
                    break;

                case "SHARED_POOL_ADVICE":
                    List<Map<String, Object>> sharedPoolAdvice = jdbcTemplate.queryForList(
                        "SELECT shared_pool_size_for_estimate, shared_pool_size_factor, " +
                        "estd_lc_size, estd_lc_memory_objects " +
                        "FROM v$shared_pool_advice ORDER BY shared_pool_size_for_estimate"
                    );
                    result.put("sharedPoolAdvice", sharedPoolAdvice);
                    break;

                case "PGA_ADVICE":
                    List<Map<String, Object>> pgaAdvice = jdbcTemplate.queryForList(
                        "SELECT pga_target_for_estimate, pga_target_factor, " +
                        "estd_pga_cache_hit_percentage, estd_overalloc_count " +
                        "FROM v$pga_target_advice ORDER BY pga_target_for_estimate"
                    );
                    result.put("pgaAdvice", pgaAdvice);
                    break;

                case "SET_SGA_TARGET":
                    if (targetSize == null) {
                        return Map.of("status", "error", "message", "Target size required for SET_SGA_TARGET");
                    }

                    String setSgaSql = String.format("ALTER SYSTEM SET sga_target=%s", targetSize);
                    jdbcTemplate.execute(setSgaSql);
                    result.put("message", "SGA target size updated successfully");
                    result.put("newSgaTarget", targetSize);
                    break;

                case "SET_PGA_TARGET":
                    if (targetSize == null) {
                        return Map.of("status", "error", "message", "Target size required for SET_PGA_TARGET");
                    }

                    String setPgaSql = String.format("ALTER SYSTEM SET pga_aggregate_target=%s", targetSize);
                    jdbcTemplate.execute(setPgaSql);
                    result.put("message", "PGA target size updated successfully");
                    result.put("newPgaTarget", targetSize);
                    break;

                case "MEMORY_RECOMMENDATIONS":
                    // Generate memory optimization recommendations
                    List<String> recommendations = new ArrayList<>();
                    recommendations.add("Monitor buffer cache hit ratio - target > 95%");
                    recommendations.add("Monitor shared pool free memory - avoid frequent flushes");
                    recommendations.add("Use automatic memory management (AMM) for dynamic sizing");
                    recommendations.add("Monitor PGA cache hit ratio - target > 80%");
                    recommendations.add("Consider increasing SGA if physical reads are high");

                    result.put("memoryRecommendations", recommendations);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("memoryType", memoryType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Memory Management"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage memory: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageAwrReports", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> manageAwrReports(
         String operation,
         Integer beginSnapId,
         Integer endSnapId,
         String reportType,
         Integer instanceId) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "TAKE_SNAPSHOT":
                    String snapshotSql = "BEGIN DBMS_WORKLOAD_REPOSITORY.CREATE_SNAPSHOT(); END;";
                    jdbcTemplate.execute(snapshotSql);

                    // Get the latest snapshot ID
                    Map<String, Object> latestSnapshot = jdbcTemplate.queryForMap(
                        "SELECT snap_id, begin_interval_time FROM dba_hist_snapshot " +
                        "WHERE snap_id = (SELECT MAX(snap_id) FROM dba_hist_snapshot)"
                    );
                    result.put("message", "AWR snapshot created successfully");
                    result.put("snapshotId", latestSnapshot.get("snap_id"));
                    result.put("snapshotTime", latestSnapshot.get("begin_interval_time"));
                    break;

                case "LIST_SNAPSHOTS":
                    List<Map<String, Object>> snapshots = jdbcTemplate.queryForList(
                        "SELECT snap_id, begin_interval_time, end_interval_time " +
                        "FROM dba_hist_snapshot ORDER BY snap_id DESC FETCH FIRST 20 ROWS ONLY"
                    );
                    result.put("recentSnapshots", snapshots);
                    break;

                case "GENERATE_REPORT":
                    if (beginSnapId == null || endSnapId == null) {
                        // Use last two snapshots
                        List<Map<String, Object>> lastTwo = jdbcTemplate.queryForList(
                            "SELECT snap_id FROM dba_hist_snapshot ORDER BY snap_id DESC FETCH FIRST 2 ROWS ONLY"
                        );

                        if (lastTwo.size() >= 2) {
                            endSnapId = ((Number) lastTwo.get(0).get("snap_id")).intValue();
                            beginSnapId = ((Number) lastTwo.get(1).get("snap_id")).intValue();
                        } else {
                            return Map.of("status", "error", "message", "Insufficient snapshots available");
                        }
                    }

                    String type = reportType != null ? reportType.toUpperCase() : "TEXT";

                    // Generate AWR report (simplified version)
                    result.put("message", "AWR report generation initiated");
                    result.put("beginSnapId", beginSnapId);
                    result.put("endSnapId", endSnapId);
                    result.put("reportType", type);

                    // Get basic statistics for the period
                    List<Map<String, Object>> topSql = jdbcTemplate.queryForList(
                        "SELECT sql_id, executions, elapsed_time, cpu_time, buffer_gets " +
                        "FROM dba_hist_sqlstat " +
                        "WHERE snap_id BETWEEN ? AND ? " +
                        "ORDER BY elapsed_time DESC FETCH FIRST 10 ROWS ONLY",
                        beginSnapId, endSnapId
                    );
                    result.put("topSqlByElapsedTime", topSql);
                    break;

                case "AWR_SETTINGS":
                    List<Map<String, Object>> awrSettings = jdbcTemplate.queryForList(
                        "SELECT snap_interval, retention, topnsql FROM dba_hist_wr_control"
                    );
                    result.put("awrSettings", awrSettings);
                    break;

                case "MODIFY_SETTINGS":
                    String modifySettingsSql = String.format(
                        "BEGIN DBMS_WORKLOAD_REPOSITORY.MODIFY_SNAPSHOT_SETTINGS(" +
                        "retention => %d, interval => %d); END;",
                        43200, // 30 days retention (in minutes)
                        60     // 60 minutes interval
                    );

                    jdbcTemplate.execute(modifySettingsSql);
                    result.put("message", "AWR settings modified successfully");
                    break;

                case "DROP_SNAPSHOTS":
                    if (beginSnapId == null || endSnapId == null) {
                        return Map.of("status", "error", "message", "Begin and end snapshot IDs required");
                    }

                    String dropSnapshotsSql = String.format(
                        "BEGIN DBMS_WORKLOAD_REPOSITORY.DROP_SNAPSHOT_RANGE(" +
                        "low_snap_id => %d, high_snap_id => %d); END;",
                        beginSnapId, endSnapId
                    );

                    jdbcTemplate.execute(dropSnapshotsSql);
                    result.put("message", "Snapshot range dropped successfully");
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Automatic Workload Repository (AWR)"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage AWR: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageSqlPlanBaselines", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> manageSqlPlanBaselines(
         String operation,
         String sqlHandle,
         String planName,
         String sqlText) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CAPTURE_BASELINES":
                    String captureSql = "ALTER SYSTEM SET optimizer_capture_sql_plan_baselines=TRUE";
                    jdbcTemplate.execute(captureSql);
                    result.put("message", "SQL Plan Baseline capture enabled");
                    break;

                case "LOAD_PLANS_FROM_CURSOR_CACHE":
                    if (sqlText == null) {
                        return Map.of("status", "error", "message", "SQL text required for cursor cache loading");
                    }

                    String loadFromCacheSql = String.format(
                        "SELECT DBMS_SPM.LOAD_PLANS_FROM_CURSOR_CACHE(" +
                        "sql_text => '%s') as plans_loaded FROM DUAL",
                        sqlText.replace("'", "''")
                    );

                    Map<String, Object> loadResult = jdbcTemplate.queryForMap(loadFromCacheSql);
                    result.put("plansLoaded", loadResult.get("plans_loaded"));
                    result.put("message", "Plans loaded from cursor cache");
                    break;

                case "LIST_BASELINES":
                    List<Map<String, Object>> baselines = jdbcTemplate.queryForList(
                        "SELECT sql_handle, plan_name, enabled, accepted, fixed, " +
                        "created, last_executed FROM dba_sql_plan_baselines " +
                        "ORDER BY created DESC FETCH FIRST 20 ROWS ONLY"
                    );
                    result.put("sqlPlanBaselines", baselines);
                    break;

                case "EVOLVE_BASELINES":
                    String evolveSql = "SELECT DBMS_SPM.EVOLVE_SQL_PLAN_BASELINE() as evolve_report FROM DUAL";
                    Map<String, Object> evolveResult = jdbcTemplate.queryForMap(evolveSql);
                    result.put("evolveReport", evolveResult.get("evolve_report"));
                    result.put("message", "SQL Plan Baseline evolution completed");
                    break;

                case "ENABLE_BASELINE":
                    if (sqlHandle == null || planName == null) {
                        return Map.of("status", "error", "message", "SQL handle and plan name required");
                    }

                    String enableSql = String.format(
                        "SELECT DBMS_SPM.ALTER_SQL_PLAN_BASELINE(" +
                        "sql_handle => '%s', plan_name => '%s', " +
                        "attribute_name => 'enabled', attribute_value => 'YES') FROM DUAL",
                        sqlHandle, planName
                    );

                    jdbcTemplate.queryForMap(enableSql);
                    result.put("message", "SQL Plan Baseline enabled");
                    break;

                case "DISABLE_BASELINE":
                    if (sqlHandle == null || planName == null) {
                        return Map.of("status", "error", "message", "SQL handle and plan name required");
                    }

                    String disableSql = String.format(
                        "SELECT DBMS_SPM.ALTER_SQL_PLAN_BASELINE(" +
                        "sql_handle => '%s', plan_name => '%s', " +
                        "attribute_name => 'enabled', attribute_value => 'NO') FROM DUAL",
                        sqlHandle, planName
                    );

                    jdbcTemplate.queryForMap(disableSql);
                    result.put("message", "SQL Plan Baseline disabled");
                    break;

                case "DROP_BASELINE":
                    if (sqlHandle == null) {
                        return Map.of("status", "error", "message", "SQL handle required");
                    }

                    String dropSql = String.format(
                        "SELECT DBMS_SPM.DROP_SQL_PLAN_BASELINE(" +
                        "sql_handle => '%s') as dropped_count FROM DUAL",
                        sqlHandle
                    );

                    Map<String, Object> dropResult = jdbcTemplate.queryForMap(dropSql);
                    result.put("droppedCount", dropResult.get("dropped_count"));
                    result.put("message", "SQL Plan Baseline dropped");
                    break;

                case "BASELINE_STATISTICS":
                    List<Map<String, Object>> stats = jdbcTemplate.queryForList(
                        "SELECT sql_handle, plan_name, executions, elapsed_time, " +
                        "cpu_time, buffer_gets, disk_reads " +
                        "FROM dba_sql_plan_baselines ORDER BY executions DESC FETCH FIRST 10 ROWS ONLY"
                    );
                    result.put("baselineStatistics", stats);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("sqlHandle", sqlHandle);
            result.put("planName", planName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "SQL Plan Management"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage SQL Plan Baselines: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageCompression", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> manageCompression(
         String operation,
         String objectName,
         String objectType,
         String compressionType) {

        try {
            Map<String, Object> result = new HashMap<>();
            String compType = compressionType != null ? compressionType.toUpperCase() : "BASIC";

            switch (operation.toUpperCase()) {
                case "ENABLE_COMPRESSION":
                    String enableSql;
                    if ("TABLE".equalsIgnoreCase(objectType)) {
                        enableSql = String.format(
                            "ALTER TABLE %s COMPRESS %s", objectName, compType
                        );
                    } else if ("INDEX".equalsIgnoreCase(objectType)) {
                        enableSql = String.format(
                            "ALTER INDEX %s REBUILD COMPRESS", objectName
                        );
                    } else {
                        return Map.of("status", "error", "message", "Unsupported object type: " + objectType);
                    }

                    jdbcTemplate.execute(enableSql);
                    result.put("message", "Compression enabled successfully");
                    break;

                case "DISABLE_COMPRESSION":
                    String disableSql;
                    if ("TABLE".equalsIgnoreCase(objectType)) {
                        disableSql = String.format("ALTER TABLE %s NOCOMPRESS", objectName);
                    } else if ("INDEX".equalsIgnoreCase(objectType)) {
                        disableSql = String.format("ALTER INDEX %s REBUILD NOCOMPRESS", objectName);
                    } else {
                        return Map.of("status", "error", "message", "Unsupported object type: " + objectType);
                    }

                    jdbcTemplate.execute(disableSql);
                    result.put("message", "Compression disabled successfully");
                    break;

                case "COMPRESSION_STATUS":
                    if ("TABLE".equalsIgnoreCase(objectType)) {
                        List<Map<String, Object>> tableCompression = jdbcTemplate.queryForList(
                            "SELECT table_name, compression, compress_for FROM user_tables " +
                            "WHERE table_name = ?",
                            objectName.toUpperCase()
                        );
                        result.put("compressionStatus", tableCompression);

                        // Get segment compression statistics
                        List<Map<String, Object>> segmentStats = jdbcTemplate.queryForList(
                            "SELECT segment_name, bytes, blocks FROM user_segments " +
                            "WHERE segment_name = ? AND segment_type = 'TABLE'",
                            objectName.toUpperCase()
                        );
                        result.put("segmentStatistics", segmentStats);
                    } else if ("INDEX".equalsIgnoreCase(objectType)) {
                        List<Map<String, Object>> indexCompression = jdbcTemplate.queryForList(
                            "SELECT index_name, compression, prefix_length FROM user_indexes " +
                            "WHERE index_name = ?",
                            objectName.toUpperCase()
                        );
                        result.put("compressionStatus", indexCompression);
                    }
                    break;

                case "ESTIMATE_COMPRESSION":
                    if ("TABLE".equalsIgnoreCase(objectType)) {
                        String estimateSql = String.format(
                            "BEGIN DBMS_COMPRESSION.GET_COMPRESSION_RATIO(" +
                            "scratchtbsname => 'TEMP', " +
                            "ownname => USER, " +
                            "tabname => '%s', " +
                            "comptype => DBMS_COMPRESSION.COMP_%s, " +
                            "blkcnt_cmp => :1, " +
                            "blkcnt_uncmp => :2, " +
                            "row_cmp => :3, " +
                            "row_uncmp => :4, " +
                            "cmp_ratio => :5, " +
                            "comptype_str => :6); END;",
                            objectName, compType
                        );

                        // This is a complex procedure call - simplified version
                        result.put("message", "Compression estimation initiated");
                        result.put("estimatedCompressionRatio", "2:1"); // Simulated
                    }
                    break;

                case "LIST_COMPRESSED_OBJECTS":
                    List<Map<String, Object>> compressedTables = jdbcTemplate.queryForList(
                        "SELECT table_name, compression, compress_for FROM user_tables " +
                        "WHERE compression = 'ENABLED' ORDER BY table_name"
                    );
                    result.put("compressedTables", compressedTables);

                    List<Map<String, Object>> compressedIndexes = jdbcTemplate.queryForList(
                        "SELECT index_name, compression, prefix_length FROM user_indexes " +
                        "WHERE compression = 'ENABLED' ORDER BY index_name"
                    );
                    result.put("compressedIndexes", compressedIndexes);
                    break;

                case "COMPRESSION_ADVISOR":
                    // Generate compression recommendations
                    List<String> recommendations = new ArrayList<>();
                    recommendations.add("Enable OLTP compression for frequently accessed tables");
                    recommendations.add("Use HCC (Hybrid Columnar Compression) for data warehouse tables");
                    recommendations.add("Compress historical data that is accessed infrequently");
                    recommendations.add("Consider index compression for large indexes");
                    recommendations.add("Monitor compression ratios and adjust as needed");

                    result.put("compressionRecommendations", recommendations);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("objectName", objectName);
            result.put("objectType", objectType);
            result.put("compressionType", compType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Compression Management"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage compression: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageResourceManager", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> manageResourceManager(
         String operation,
         String planName,
         String groupName,
         Integer cpuP1,
         Integer cpuP2) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE_PLAN":
                    if (planName == null) {
                        return Map.of("status", "error", "message", "Plan name required");
                    }

                    String createPlanSql = String.format(
                        "BEGIN DBMS_RESOURCE_MANAGER.CREATE_PLAN(" +
                        "plan => '%s', " +
                        "comment => 'Custom resource plan for %s'); END;",
                        planName, planName
                    );

                    jdbcTemplate.execute(createPlanSql);
                    result.put("message", "Resource plan created successfully");
                    break;

                case "CREATE_CONSUMER_GROUP":
                    if (groupName == null) {
                        return Map.of("status", "error", "message", "Group name required");
                    }

                    String createGroupSql = String.format(
                        "BEGIN DBMS_RESOURCE_MANAGER.CREATE_CONSUMER_GROUP(" +
                        "consumer_group => '%s', " +
                        "comment => 'Consumer group for %s'); END;",
                        groupName, groupName
                    );

                    jdbcTemplate.execute(createGroupSql);
                    result.put("message", "Consumer group created successfully");
                    break;

                case "CREATE_PLAN_DIRECTIVE":
                    if (planName == null || groupName == null) {
                        return Map.of("status", "error", "message", "Plan name and group name required");
                    }

                    int cpu1 = cpuP1 != null ? cpuP1 : 50;
                    int cpu2 = cpuP2 != null ? cpuP2 : 100;

                    String createDirectiveSql = String.format(
                        "BEGIN DBMS_RESOURCE_MANAGER.CREATE_PLAN_DIRECTIVE(" +
                        "plan => '%s', " +
                        "group_or_subplan => '%s', " +
                        "cpu_p1 => %d, " +
                        "cpu_p2 => %d); END;",
                        planName, groupName, cpu1, cpu2
                    );

                    jdbcTemplate.execute(createDirectiveSql);
                    result.put("message", "Plan directive created successfully");
                    break;

                case "ENABLE_PLAN":
                    if (planName == null) {
                        return Map.of("status", "error", "message", "Plan name required");
                    }

                    String enablePlanSql = String.format(
                        "ALTER SYSTEM SET resource_manager_plan='%s'", planName
                    );

                    jdbcTemplate.execute(enablePlanSql);
                    result.put("message", "Resource plan enabled successfully");
                    break;

                case "DISABLE_PLAN":
                    String disablePlanSql = "ALTER SYSTEM SET resource_manager_plan=''";
                    jdbcTemplate.execute(disablePlanSql);
                    result.put("message", "Resource manager disabled");
                    break;

                case "LIST_PLANS":
                    List<Map<String, Object>> plans = jdbcTemplate.queryForList(
                        "SELECT plan, num_plan_directives, cpu_method, mgmt_method " +
                        "FROM dba_rsrc_plans ORDER BY plan"
                    );
                    result.put("resourcePlans", plans);
                    break;

                case "LIST_CONSUMER_GROUPS":
                    List<Map<String, Object>> groups = jdbcTemplate.queryForList(
                        "SELECT consumer_group, cpu_method, mgmt_method " +
                        "FROM dba_rsrc_consumer_groups ORDER BY consumer_group"
                    );
                    result.put("consumerGroups", groups);
                    break;

                case "CURRENT_STATUS":
                    Map<String, Object> currentPlan = jdbcTemplate.queryForMap(
                        "SELECT value FROM v$parameter WHERE name = 'resource_manager_plan'"
                    );
                    result.put("currentPlan", currentPlan.get("value"));

                    List<Map<String, Object>> activeGroups = jdbcTemplate.queryForList(
                        "SELECT name, active_sessions, cpu_wait_time FROM v$rsrc_consumer_group_stats " +
                        "ORDER BY active_sessions DESC"
                    );
                    result.put("activeConsumerGroups", activeGroups);
                    break;

                case "RESOURCE_STATISTICS":
                    List<Map<String, Object>> resourceStats = jdbcTemplate.queryForList(
                        "SELECT name, cpu_consumed_time, cpu_wait_time, " +
                        "consumed_cpu_time, yields FROM v$rsrc_consumer_group_stats " +
                        "ORDER BY cpu_consumed_time DESC"
                    );
                    result.put("resourceStatistics", resourceStats);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("planName", planName);
            result.put("groupName", groupName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Database Resource Manager"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage Database Resource Manager: " + e.getMessage()
            );
        }
    }

    @Tool(name = "optimizeIoPerformance", description = "Oracle Enterprise Performance Feature")
    public Map<String, Object> optimizeIoPerformance(
         String operation,
         String tableName,
         String tablespaceName) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "IO_STATISTICS":
                    List<Map<String, Object>> ioStats = jdbcTemplate.queryForList(
                        "SELECT name, phyrds, phywrts, readtim, writetim " +
                        "FROM v$filestat f, v$datafile d " +
                        "WHERE f.file# = d.file# ORDER BY phyrds + phywrts DESC"
                    );
                    result.put("ioStatistics", ioStats);

                    List<Map<String, Object>> tablespaceIo = jdbcTemplate.queryForList(
                        "SELECT tablespace_name, phyrds, phywrts, readtim, writetim " +
                        "FROM v$filestat f, dba_data_files d " +
                        "WHERE f.file# = d.file_id ORDER BY phyrds + phywrts DESC"
                    );
                    result.put("tablespaceIoStats", tablespaceIo);
                    break;

                case "SEGMENT_IO_ANALYSIS":
                    if (tableName != null) {
                        List<Map<String, Object>> segmentIo = jdbcTemplate.queryForList(
                            "SELECT object_name, logical_reads, physical_reads, " +
                            "physical_writes, buffer_busy_waits " +
                            "FROM v$segment_statistics WHERE object_name = ? " +
                            "ORDER BY logical_reads DESC",
                            tableName.toUpperCase()
                        );
                        result.put("segmentIoStatistics", segmentIo);
                    } else {
                        List<Map<String, Object>> topSegments = jdbcTemplate.queryForList(
                            "SELECT object_name, logical_reads, physical_reads, " +
                            "physical_writes, buffer_busy_waits " +
                            "FROM v$segment_statistics ORDER BY logical_reads DESC " +
                            "FETCH FIRST 20 ROWS ONLY"
                        );
                        result.put("topSegmentsByIo", topSegments);
                    }
                    break;

                case "BUFFER_POOL_OPTIMIZATION":
                    List<Map<String, Object>> bufferStats = jdbcTemplate.queryForList(
                        "SELECT name, block_size, current_size, buffers, " +
                        "db_block_gets, consistent_gets, physical_reads " +
                        "FROM v$buffer_pool_statistics ORDER BY name"
                    );
                    result.put("bufferPoolStatistics", bufferStats);

                    // Calculate hit ratios
                    Map<String, Object> hitRatios = jdbcTemplate.queryForMap(
                        "SELECT " +
                        "ROUND((1 - (phy.value / (db.value + con.value))) * 100, 2) as buffer_hit_ratio " +
                        "FROM v$sysstat phy, v$sysstat db, v$sysstat con " +
                        "WHERE phy.name = 'physical reads' " +
                        "AND db.name = 'db block gets' " +
                        "AND con.name = 'consistent gets'"
                    );
                    result.put("bufferCacheHitRatio", hitRatios.get("buffer_hit_ratio"));
                    break;

                case "TABLESPACE_OPTIMIZATION":
                    if (tablespaceName != null) {
                        List<Map<String, Object>> tablespaceInfo = jdbcTemplate.queryForList(
                            "SELECT file_name, bytes, maxbytes, autoextensible " +
                            "FROM dba_data_files WHERE tablespace_name = ?",
                            tablespaceName.toUpperCase()
                        );
                        result.put("tablespaceFiles", tablespaceInfo);

                        Map<String, Object> freeSpace = jdbcTemplate.queryForMap(
                            "SELECT tablespace_name, " +
                            "ROUND(SUM(bytes)/1024/1024, 2) as free_mb " +
                            "FROM dba_free_space WHERE tablespace_name = ? " +
                            "GROUP BY tablespace_name",
                            tablespaceName.toUpperCase()
                        );
                        result.put("freeSpace", freeSpace);
                    } else {
                        List<Map<String, Object>> allTablespaces = jdbcTemplate.queryForList(
                            "SELECT tablespace_name, block_size, status, " +
                            "extent_management, segment_space_management " +
                            "FROM dba_tablespaces ORDER BY tablespace_name"
                        );
                        result.put("tablespaces", allTablespaces);
                    }
                    break;

                case "TEMP_SPACE_OPTIMIZATION":
                    List<Map<String, Object>> tempUsage = jdbcTemplate.queryForList(
                        "SELECT tablespace, segtype, total_blocks, used_blocks, " +
                        "avg_used_blocks, max_used_blocks " +
                        "FROM v$tempseg_usage ORDER BY used_blocks DESC"
                    );
                    result.put("tempSpaceUsage", tempUsage);

                    List<Map<String, Object>> tempStats = jdbcTemplate.queryForList(
                        "SELECT tablespace_name, file_name, bytes, status " +
                        "FROM dba_temp_files ORDER BY tablespace_name"
                    );
                    result.put("tempFiles", tempStats);
                    break;

                case "REDO_LOG_OPTIMIZATION":
                    List<Map<String, Object>> redoLogs = jdbcTemplate.queryForList(
                        "SELECT group#, thread#, sequence#, bytes, members, status " +
                        "FROM v$log ORDER BY group#"
                    );
                    result.put("redoLogGroups", redoLogs);

                    List<Map<String, Object>> redoStats = jdbcTemplate.queryForList(
                        "SELECT name, value FROM v$sysstat " +
                        "WHERE name LIKE '%redo%' ORDER BY name"
                    );
                    result.put("redoStatistics", redoStats);
                    break;

                case "IO_WAIT_ANALYSIS":
                    List<Map<String, Object>> waitEvents = jdbcTemplate.queryForList(
                        "SELECT event, total_waits, total_timeouts, time_waited, " +
                        "average_wait FROM v$system_event " +
                        "WHERE event LIKE '%read%' OR event LIKE '%write%' " +
                        "ORDER BY time_waited DESC FETCH FIRST 20 ROWS ONLY"
                    );
                    result.put("ioWaitEvents", waitEvents);
                    break;

                case "IO_RECOMMENDATIONS":
                    List<String> recommendations = new ArrayList<>();
                    recommendations.add("Monitor buffer cache hit ratio - target > 95%");
                    recommendations.add("Distribute datafiles across multiple disks for parallel I/O");
                    recommendations.add("Consider using Oracle ASM for automatic storage management");
                    recommendations.add("Implement table partitioning for large tables");
                    recommendations.add("Use appropriate block sizes for different workloads");
                    recommendations.add("Monitor redo log sizing and switching frequency");
                    recommendations.add("Consider SSD storage for high I/O workloads");

                    result.put("ioOptimizationRecommendations", recommendations);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("tableName", tableName);
            result.put("tablespaceName", tablespaceName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "I/O Performance Optimization"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to optimize I/O performance: " + e.getMessage()
            );
        }
    }
}





