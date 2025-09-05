package com.deepai.mcpserver.service;

import org.springframework.ai.mcp.server.Tool;
import org.springframework.ai.mcp.server.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.deepai.mcpserver.util.OracleFeatureDetector;
import com.deepai.mcpserver.util.OracleSqlBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Oracle Advanced Analytics Service - 20 Tools
 * Provides SQL Analytics, CTEs, Window Functions, Performance Tools, and PL/SQL Operations
 */
@Service
public class OracleAdvancedAnalyticsService {

    private final JdbcTemplate jdbcTemplate;
    private final OracleFeatureDetector featureDetector;
    private final OracleSqlBuilder sqlBuilder;

    @Autowired
    public OracleAdvancedAnalyticsService(JdbcTemplate jdbcTemplate, 
                                         OracleFeatureDetector featureDetector,
                                         OracleSqlBuilder sqlBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureDetector = featureDetector;
        this.sqlBuilder = sqlBuilder;
    }

    // ========== SQL ANALYTICS & CTEs (8 TOOLS) ==========

    @Tool(name = "oracle_complex_joins",
          description = "Execute multi-table enterprise JOINs with Oracle optimizations")
    public Map<String, Object> executeComplexJoins(
        @ToolParam(name = "tables", required = true) List<String> tables,
        @ToolParam(name = "joinConditions", required = true) List<String> joinConditions,
        @ToolParam(name = "selectColumns", required = false) List<String> selectColumns,
        @ToolParam(name = "whereClause", required = false) String whereClause,
        @ToolParam(name = "optimizerHints", required = false) List<String> optimizerHints) {
        
        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            
            // Add optimizer hints if provided
            if (optimizerHints != null && !optimizerHints.isEmpty()) {
                sql.append("/*+ ").append(String.join(" ", optimizerHints)).append(" */ ");
            }
            
            // Select columns
            if (selectColumns != null && !selectColumns.isEmpty()) {
                sql.append(String.join(", ", selectColumns));
            } else {
                sql.append("*");
            }
            
            // FROM clause with JOINs
            sql.append(" FROM ").append(tables.get(0));
            for (int i = 1; i < tables.size() && i <= joinConditions.size(); i++) {
                sql.append(" JOIN ").append(tables.get(i))
                   .append(" ON ").append(joinConditions.get(i - 1));
            }
            
            // WHERE clause
            if (whereClause != null && !whereClause.isEmpty()) {
                sql.append(" WHERE ").append(whereClause);
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "tables", tables,
                "executionTime", Instant.now(),
                "oracleFeature", "Complex JOIN Operations"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute complex joins: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_cte_queries",
          description = "Execute Common Table Expression (WITH clause) operations")
    public Map<String, Object> executeCteQueries(
        @ToolParam(name = "cteDefinitions", required = true) List<Map<String, Object>> cteDefinitions,
        @ToolParam(name = "mainQuery", required = true) String mainQuery,
        @ToolParam(name = "recursive", required = false) Boolean recursive) {
        
        try {
            StringBuilder sql = new StringBuilder("WITH ");
            
            if (recursive != null && recursive) {
                sql.append("RECURSIVE ");
            }
            
            List<String> cteClause = new ArrayList<>();
            for (Map<String, Object> cte : cteDefinitions) {
                String cteName = (String) cte.get("name");
                String cteQuery = (String) cte.get("query");
                List<String> columns = (List<String>) cte.get("columns");
                
                StringBuilder cteStr = new StringBuilder(cteName);
                if (columns != null && !columns.isEmpty()) {
                    cteStr.append("(").append(String.join(", ", columns)).append(")");
                }
                cteStr.append(" AS (").append(cteQuery).append(")");
                cteClause.add(cteStr.toString());
            }
            
            sql.append(String.join(", ", cteClause));
            sql.append(" ").append(mainQuery);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "cteCount", cteDefinitions.size(),
                "recursive", recursive != null ? recursive : false,
                "oracleFeature", "Common Table Expressions"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute CTE queries: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_window_functions",
          description = "Execute LEAD/LAG/RANK/DENSE_RANK analytics with window functions")
    public Map<String, Object> executeWindowFunctions(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "windowFunction", required = true) String windowFunction,
        @ToolParam(name = "partitionBy", required = false) List<String> partitionBy,
        @ToolParam(name = "orderBy", required = true) List<String> orderBy,
        @ToolParam(name = "selectColumns", required = false) List<String> selectColumns) {
        
        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            
            // Base columns
            if (selectColumns != null && !selectColumns.isEmpty()) {
                sql.append(String.join(", ", selectColumns)).append(", ");
            } else {
                sql.append("*, ");
            }
            
            // Window function
            sql.append(windowFunction).append(" OVER (");
            
            if (partitionBy != null && !partitionBy.isEmpty()) {
                sql.append("PARTITION BY ").append(String.join(", ", partitionBy)).append(" ");
            }
            
            sql.append("ORDER BY ").append(String.join(", ", orderBy));
            sql.append(") as window_result");
            
            sql.append(" FROM ").append(tableName);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "windowFunction", windowFunction,
                "partitionBy", partitionBy != null ? partitionBy : "None",
                "orderBy", orderBy,
                "oracleFeature", "Window Functions"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute window functions: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_pivot_operations",
          description = "Execute PIVOT/UNPIVOT transformations for data analysis")
    public Map<String, Object> executePivotOperations(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "operation", required = true) String operation,
        @ToolParam(name = "aggregateFunction", required = false) String aggregateFunction,
        @ToolParam(name = "aggregateColumn", required = false) String aggregateColumn,
        @ToolParam(name = "pivotColumn", required = true) String pivotColumn,
        @ToolParam(name = "pivotValues", required = true) List<String> pivotValues) {
        
        try {
            StringBuilder sql = new StringBuilder();
            
            if ("PIVOT".equalsIgnoreCase(operation)) {
                String aggFunc = aggregateFunction != null ? aggregateFunction : "SUM";
                String aggCol = aggregateColumn != null ? aggregateColumn : "*";
                
                sql.append("SELECT * FROM (SELECT * FROM ").append(tableName).append(") ");
                sql.append("PIVOT (").append(aggFunc).append("(").append(aggCol).append(") FOR ");
                sql.append(pivotColumn).append(" IN (");
                
                List<String> pivotClause = new ArrayList<>();
                for (String value : pivotValues) {
                    pivotClause.add("'" + value + "'");
                }
                sql.append(String.join(", ", pivotClause));
                sql.append("))");
                
            } else if ("UNPIVOT".equalsIgnoreCase(operation)) {
                sql.append("SELECT * FROM ").append(tableName).append(" ");
                sql.append("UNPIVOT (value FOR ").append(pivotColumn).append(" IN (");
                sql.append(String.join(", ", pivotValues));
                sql.append("))");
            } else {
                return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "operation", operation,
                "pivotColumn", pivotColumn,
                "pivotValues", pivotValues,
                "oracleFeature", "PIVOT/UNPIVOT Transformations"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute pivot operations: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_analytical_functions",
          description = "Execute PERCENTILE, NTILE, CUME_DIST analytics")
    public Map<String, Object> executeAnalyticalFunctions(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "analyticalFunction", required = true) String analyticalFunction,
        @ToolParam(name = "column", required = true) String column,
        @ToolParam(name = "partitionBy", required = false) List<String> partitionBy,
        @ToolParam(name = "orderBy", required = false) List<String> orderBy,
        @ToolParam(name = "parameters", required = false) List<Object> parameters) {
        
        try {
            StringBuilder sql = new StringBuilder("SELECT *, ");
            
            // Build analytical function
            sql.append(analyticalFunction);
            
            if (parameters != null && !parameters.isEmpty()) {
                sql.append("(");
                if (analyticalFunction.toUpperCase().contains("PERCENTILE")) {
                    sql.append(parameters.get(0)).append(") WITHIN GROUP (ORDER BY ").append(column).append(")");
                } else if ("NTILE".equalsIgnoreCase(analyticalFunction)) {
                    sql.append(parameters.get(0)).append(")");
                } else {
                    sql.append(String.join(", ", parameters.stream().map(Object::toString).toArray(String[]::new))).append(")");
                }
            } else {
                sql.append("()");
            }
            
            // Add OVER clause if needed
            if (!analyticalFunction.toUpperCase().contains("PERCENTILE") || 
                (partitionBy != null && !partitionBy.isEmpty()) ||
                (orderBy != null && !orderBy.isEmpty())) {
                
                sql.append(" OVER (");
                if (partitionBy != null && !partitionBy.isEmpty()) {
                    sql.append("PARTITION BY ").append(String.join(", ", partitionBy)).append(" ");
                }
                if (orderBy != null && !orderBy.isEmpty()) {
                    sql.append("ORDER BY ").append(String.join(", ", orderBy));
                }
                sql.append(")");
            }
            
            sql.append(" as analytical_result FROM ").append(tableName);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "analyticalFunction", analyticalFunction,
                "column", column,
                "oracleFeature", "Analytical Functions"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute analytical functions: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_hierarchical_queries",
          description = "Execute CONNECT BY operations for tree-structured data")
    public Map<String, Object> executeHierarchicalQueries(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "startWithCondition", required = true) String startWithCondition,
        @ToolParam(name = "connectByCondition", required = true) String connectByCondition,
        @ToolParam(name = "selectColumns", required = false) List<String> selectColumns,
        @ToolParam(name = "orderSiblings", required = false) String orderSiblings) {
        
        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            
            if (selectColumns != null && !selectColumns.isEmpty()) {
                sql.append(String.join(", ", selectColumns)).append(", ");
            }
            
            // Add hierarchical pseudocolumns
            sql.append("LEVEL, SYS_CONNECT_BY_PATH(");
            String firstCol = selectColumns != null && !selectColumns.isEmpty() ? 
                selectColumns.get(0) : "id";
            sql.append(firstCol).append(", '/') as hierarchy_path");
            
            sql.append(" FROM ").append(tableName);
            sql.append(" START WITH ").append(startWithCondition);
            sql.append(" CONNECT BY ").append(connectByCondition);
            
            if (orderSiblings != null && !orderSiblings.isEmpty()) {
                sql.append(" ORDER SIBLINGS BY ").append(orderSiblings);
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "startWithCondition", startWithCondition,
                "connectByCondition", connectByCondition,
                "oracleFeature", "Hierarchical Queries"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute hierarchical queries: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_recursive_cte",
          description = "Execute recursive WITH queries for complex data traversal")
    public Map<String, Object> executeRecursiveCte(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "anchorQuery", required = true) String anchorQuery,
        @ToolParam(name = "recursiveQuery", required = true) String recursiveQuery,
        @ToolParam(name = "cteName", required = false) String cteName,
        @ToolParam(name = "maxRecursion", required = false) Integer maxRecursion) {
        
        try {
            String cte = cteName != null ? cteName : "recursive_cte";
            
            StringBuilder sql = new StringBuilder("WITH ").append(cte).append(" AS (");
            sql.append(anchorQuery);
            sql.append(" UNION ALL ");
            sql.append(recursiveQuery);
            sql.append(") SELECT * FROM ").append(cte);
            
            if (maxRecursion != null && maxRecursion > 0) {
                sql.append(" WHERE LEVEL <= ").append(maxRecursion);
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "cteName", cte,
                "maxRecursion", maxRecursion != null ? maxRecursion : "Unlimited",
                "oracleFeature", "Recursive CTE"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute recursive CTE: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_model_clause",
          description = "Execute MODEL clause spreadsheet calculations")
    public Map<String, Object> executeModelClause(
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "partitionBy", required = false) List<String> partitionBy,
        @ToolParam(name = "dimensionBy", required = true) List<String> dimensionBy,
        @ToolParam(name = "measuresColumns", required = true) List<String> measuresColumns,
        @ToolParam(name = "modelRules", required = true) List<String> modelRules) {
        
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
            sql.append(" MODEL ");
            
            if (partitionBy != null && !partitionBy.isEmpty()) {
                sql.append("PARTITION BY (").append(String.join(", ", partitionBy)).append(") ");
            }
            
            sql.append("DIMENSION BY (").append(String.join(", ", dimensionBy)).append(") ");
            sql.append("MEASURES (").append(String.join(", ", measuresColumns)).append(") ");
            
            sql.append("RULES (");
            sql.append(String.join(", ", modelRules));
            sql.append(")");
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
            
            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "query", sql.toString(),
                "dimensionBy", dimensionBy,
                "measuresColumns", measuresColumns,
                "modelRules", modelRules,
                "oracleFeature", "MODEL Clause Calculations"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute MODEL clause: " + e.getMessage()
            );
        }
    }

    // ========== INDEX & PERFORMANCE (7 TOOLS) ==========

    @Tool(name = "oracle_create_index",
          description = "Create B-tree, bitmap, or function-based indexes")
    public Map<String, Object> createIndex(
        @ToolParam(name = "indexName", required = true) String indexName,
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "columns", required = true) List<String> columns,
        @ToolParam(name = "indexType", required = false) String indexType,
        @ToolParam(name = "unique", required = false) Boolean unique,
        @ToolParam(name = "tablespace", required = false) String tablespace) {
        
        try {
            String type = indexType != null ? indexType.toUpperCase() : "BTREE";
            
            StringBuilder sql = new StringBuilder("CREATE ");
            
            if (unique != null && unique) {
                sql.append("UNIQUE ");
            }
            
            if ("BITMAP".equals(type)) {
                sql.append("BITMAP ");
            }
            
            sql.append("INDEX ").append(indexName);
            sql.append(" ON ").append(tableName);
            sql.append(" (").append(String.join(", ", columns)).append(")");
            
            if (tablespace != null) {
                sql.append(" TABLESPACE ").append(tablespace);
            }
            
            jdbcTemplate.execute(sql.toString());
            
            return Map.of(
                "status", "success",
                "message", "Index created successfully",
                "indexName", indexName,
                "tableName", tableName,
                "columns", columns,
                "indexType", type,
                "unique", unique != null ? unique : false,
                "oracleFeature", "Advanced Indexing"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to create index: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_analyze_performance",
          description = "Analyze query performance with AWR and ADDM integration")
    public Map<String, Object> analyzePerformance(
        @ToolParam(name = "sqlQuery", required = true) String sqlQuery,
        @ToolParam(name = "includeAwrData", required = false) Boolean includeAwrData,
        @ToolParam(name = "generateAddmReport", required = false) Boolean generateAddmReport) {
        
        try {
            Map<String, Object> performance = new HashMap<>();
            
            // Execute EXPLAIN PLAN
            String explainSql = "EXPLAIN PLAN FOR " + sqlQuery;
            jdbcTemplate.execute(explainSql);
            
            // Get execution plan
            List<Map<String, Object>> plan = jdbcTemplate.queryForList(
                "SELECT operation, options, object_name, cost, cardinality " +
                "FROM plan_table ORDER BY id");
            performance.put("executionPlan", plan);
            
            // AWR data if available and requested
            if (includeAwrData != null && includeAwrData && featureDetector.supportsAWR()) {
                List<Map<String, Object>> awrStats = jdbcTemplate.queryForList(
                    "SELECT sql_id, executions, elapsed_time, cpu_time, buffer_gets " +
                    "FROM dba_hist_sqlstat WHERE rownum <= 10 ORDER BY elapsed_time DESC");
                performance.put("awrTopSql", awrStats);
            }
            
            return Map.of(
                "status", "success",
                "sqlQuery", sqlQuery,
                "performance", performance,
                "awrAvailable", featureDetector.supportsAWR(),
                "timestamp", Instant.now(),
                "oracleFeature", "Performance Analysis"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to analyze performance: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_optimizer_hints",
          description = "Apply cost-based optimizer hints for query tuning")
    public Map<String, Object> applyOptimizerHints(
        @ToolParam(name = "sqlQuery", required = true) String sqlQuery,
        @ToolParam(name = "hints", required = true) List<String> hints,
        @ToolParam(name = "comparePerformance", required = false) Boolean comparePerformance) {
        
        try {
            // Build query with hints
            String hintedQuery;
            if (sqlQuery.trim().toUpperCase().startsWith("SELECT")) {
                hintedQuery = sqlQuery.replaceFirst("(?i)SELECT", 
                    "SELECT /*+ " + String.join(" ", hints) + " */");
            } else {
                hintedQuery = "/*+ " + String.join(" ", hints) + " */ " + sqlQuery;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalQuery", sqlQuery);
            result.put("hintedQuery", hintedQuery);
            result.put("hints", hints);
            
            // Execute hinted query
            List<Map<String, Object>> results = jdbcTemplate.queryForList(hintedQuery);
            result.put("results", results);
            result.put("count", results.size());
            
            // Performance comparison if requested
            if (comparePerformance != null && comparePerformance) {
                // Get execution plans for both queries
                jdbcTemplate.execute("EXPLAIN PLAN SET STATEMENT_ID='ORIGINAL' FOR " + sqlQuery);
                jdbcTemplate.execute("EXPLAIN PLAN SET STATEMENT_ID='HINTED' FOR " + hintedQuery);
                
                Map<String, Object> originalPlan = jdbcTemplate.queryForMap(
                    "SELECT SUM(cost) as total_cost FROM plan_table WHERE statement_id='ORIGINAL'");
                Map<String, Object> hintedPlan = jdbcTemplate.queryForMap(
                    "SELECT SUM(cost) as total_cost FROM plan_table WHERE statement_id='HINTED'");
                
                result.put("performanceComparison", Map.of(
                    "originalCost", originalPlan.get("total_cost"),
                    "hintedCost", hintedPlan.get("total_cost")
                ));
            }
            
            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Optimizer Hints"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to apply optimizer hints: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_execution_plans",
          description = "Analyze EXPLAIN PLAN and DBMS_XPLAN output")
    public Map<String, Object> analyzeExecutionPlans(
        @ToolParam(name = "sqlQuery", required = true) String sqlQuery,
        @ToolParam(name = "planFormat", required = false) String planFormat,
        @ToolParam(name = "includePredicates", required = false) Boolean includePredicates) {
        
        try {
            String format = planFormat != null ? planFormat : "BASIC";
            
            // Execute EXPLAIN PLAN
            jdbcTemplate.execute("EXPLAIN PLAN FOR " + sqlQuery);
            
            // Get detailed execution plan
            String xplanSql = "SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY('PLAN_TABLE', NULL, ?))";
            List<Map<String, Object>> xplanOutput = jdbcTemplate.queryForList(xplanSql, format);
            
            // Get structured plan data
            List<Map<String, Object>> planTable = jdbcTemplate.queryForList(
                "SELECT id, operation, options, object_name, cost, cardinality, " +
                "bytes, time, access_predicates, filter_predicates " +
                "FROM plan_table ORDER BY id");
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("xplanOutput", xplanOutput);
            analysis.put("structuredPlan", planTable);
            
            // Calculate total cost
            Object totalCost = jdbcTemplate.queryForObject(
                "SELECT SUM(cost) FROM plan_table WHERE cost IS NOT NULL", Object.class);
            analysis.put("totalCost", totalCost);
            
            // Identify expensive operations
            List<Map<String, Object>> expensiveOps = jdbcTemplate.queryForList(
                "SELECT operation, options, object_name, cost FROM plan_table " +
                "WHERE cost > (SELECT AVG(cost) FROM plan_table WHERE cost IS NOT NULL) " +
                "ORDER BY cost DESC");
            analysis.put("expensiveOperations", expensiveOps);
            
            return Map.of(
                "status", "success",
                "sqlQuery", sqlQuery,
                "planFormat", format,
                "analysis", analysis,
                "timestamp", Instant.now(),
                "oracleFeature", "Execution Plan Analysis"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to analyze execution plans: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_table_statistics",
          description = "Manage DBMS_STATS operations for optimizer statistics")
    public Map<String, Object> manageTableStatistics(
        @ToolParam(name = "operation", required = true) String operation,
        @ToolParam(name = "tableName", required = true) String tableName,
        @ToolParam(name = "schemaName", required = false) String schemaName,
        @ToolParam(name = "estimatePercent", required = false) Integer estimatePercent,
        @ToolParam(name = "cascadeIndexes", required = false) Boolean cascadeIndexes) {
        
        try {
            String schema = schemaName != null ? schemaName.toUpperCase() : null;
            Integer estimate = estimatePercent != null ? estimatePercent : 10;
            Boolean cascade = cascadeIndexes != null ? cascadeIndexes : true;
            
            Map<String, Object> result = new HashMap<>();
            
            switch (operation.toUpperCase()) {
                case "GATHER":
                    String gatherSql = String.format(
                        "BEGIN DBMS_STATS.GATHER_TABLE_STATS('%s', '%s', estimate_percent => %d, cascade => %s); END;",
                        schema != null ? schema : "USER", 
                        tableName.toUpperCase(), 
                        estimate,
                        cascade ? "TRUE" : "FALSE"
                    );
                    jdbcTemplate.execute(gatherSql);
                    result.put("message", "Statistics gathered successfully");
                    break;
                    
                case "DELETE":
                    String deleteSql = String.format(
                        "BEGIN DBMS_STATS.DELETE_TABLE_STATS('%s', '%s', cascade_indexes => %s); END;",
                        schema != null ? schema : "USER",
                        tableName.toUpperCase(),
                        cascade ? "TRUE" : "FALSE"
                    );
                    jdbcTemplate.execute(deleteSql);
                    result.put("message", "Statistics deleted successfully");
                    break;
                    
                case "VIEW":
                    String viewSql = "SELECT num_rows, blocks, avg_row_len, last_analyzed " +
                                   "FROM all_tables WHERE table_name = ?" +
                                   (schema != null ? " AND owner = ?" : "");
                    
                    Map<String, Object> stats = schema != null ?
                        jdbcTemplate.queryForMap(viewSql, tableName.toUpperCase(), schema) :
                        jdbcTemplate.queryForMap(viewSql, tableName.toUpperCase());
                    
                    result.put("statistics", stats);
                    break;
                    
                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }
            
            result.put("operation", operation);
            result.put("tableName", tableName);
            result.put("schema", schema != null ? schema : "Current");
            
            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "DBMS_STATS Operations"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage table statistics: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_sql_tuning",
          description = "Use SQL Tuning Advisor for query optimization")
    public Map<String, Object> runSqlTuning(
        @ToolParam(name = "sqlQuery", required = true) String sqlQuery,
        @ToolParam(name = "taskName", required = false) String taskName,
        @ToolParam(name = "tuningScope", required = false) String tuningScope) {
        
        try {
            String task = taskName != null ? taskName : "TUNING_TASK_" + System.currentTimeMillis();
            String scope = tuningScope != null ? tuningScope : "COMPREHENSIVE";
            
            // Create SQL Tuning Task
            String createTaskSql = String.format(
                "DECLARE task_name VARCHAR2(128); " +
                "BEGIN task_name := DBMS_SQLTUNE.CREATE_TUNING_TASK(" +
                "sql_text => '%s', " +
                "user_name => USER, " +
                "scope => '%s', " +
                "task_name => '%s'); " +
                "DBMS_OUTPUT.PUT_LINE('Task created: ' || task_name); " +
                "END;", 
                sqlQuery.replace("'", "''"), scope, task
            );
            
            jdbcTemplate.execute(createTaskSql);
            
            // Execute the tuning task
            String executeSql = String.format(
                "BEGIN DBMS_SQLTUNE.EXECUTE_TUNING_TASK('%s'); END;", task);
            jdbcTemplate.execute(executeSql);
            
            // Get tuning report
            String reportSql = String.format(
                "SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK('%s') as tuning_report FROM dual", task);
            
            Map<String, Object> report = jdbcTemplate.queryForMap(reportSql);
            
            return Map.of(
                "status", "success",
                "taskName", task,
                "sqlQuery", sqlQuery,
                "tuningScope", scope,
                "report", report.get("tuning_report"),
                "oracleFeature", "SQL Tuning Advisor"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to run SQL tuning: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_memory_advisor",
          description = "Get SGA and PGA memory recommendations")
    public Map<String, Object> getMemoryRecommendations() {
        
        try {
            Map<String, Object> memoryInfo = new HashMap<>();
            
            // Current SGA information
            List<Map<String, Object>> sgaInfo = jdbcTemplate.queryForList(
                "SELECT name, value FROM v\");
            memoryInfo.put("currentSga", sgaInfo);
            
            // PGA information
            List<Map<String, Object>> pgaInfo = jdbcTemplate.queryForList(
                "SELECT name, value FROM v\ WHERE name IN " +
                "('total PGA allocated', 'total PGA used by SQL workareas', 'maximum PGA allocated')");
            memoryInfo.put("currentPga", pgaInfo);
            
            // Memory advisor recommendations if available
            if (featureDetector.supportsAWR()) {
                List<Map<String, Object>> sgaAdvisor = jdbcTemplate.queryForList(
                    "SELECT size_for_estimate, size_factor, estd_db_time_factor " +
                    "FROM v\ ORDER BY size_factor");
                memoryInfo.put("sgaAdvisor", sgaAdvisor);
                
                List<Map<String, Object>> pgaAdvisor = jdbcTemplate.queryForList(
                    "SELECT pga_target_for_estimate, pga_target_factor, estd_time " +
                    "FROM v\ ORDER BY pga_target_factor");
                memoryInfo.put("pgaAdvisor", pgaAdvisor);
            }
            
            return Map.of(
                "status", "success",
                "memoryInfo", memoryInfo,
                "advisorAvailable", featureDetector.supportsAWR(),
                "timestamp", Instant.now(),
                "oracleFeature", "Memory Advisor"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to get memory recommendations: " + e.getMessage()
            );
        }
    }

    // ========== PL/SQL OPERATIONS (5 TOOLS) ==========

    @Tool(name = "oracle_execute_plsql",
          description = "Execute anonymous PL/SQL blocks")
    public Map<String, Object> executePlsqlBlock(
        @ToolParam(name = "plsqlCode", required = true) String plsqlCode,
        @ToolParam(name = "parameters", required = false) Map<String, Object> parameters,
        @ToolParam(name = "captureOutput", required = false) Boolean captureOutput) {
        
        try {
            String plsql = plsqlCode;
            
            // Enable DBMS_OUTPUT if capturing output
            if (captureOutput != null && captureOutput) {
                jdbcTemplate.execute("BEGIN DBMS_OUTPUT.ENABLE(1000000); END;");
            }
            
            // Execute the PL/SQL block
            jdbcTemplate.execute(plsql);
            
            Map<String, Object> result = new HashMap<>();
            result.put("plsqlCode", plsqlCode);
            result.put("status", "executed");
            
            // Capture DBMS_OUTPUT if requested
            if (captureOutput != null && captureOutput) {
                try {
                    List<Map<String, Object>> output = jdbcTemplate.queryForList(
                        "SELECT DBMS_OUTPUT.GET_LINE() as output_line FROM dual " +
                        "CONNECT BY LEVEL <= 100 AND DBMS_OUTPUT.GET_LINE() IS NOT NULL");
                    result.put("output", output);
                } catch (Exception outputException) {
                    result.put("output", "No output captured");
                }
            }
            
            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "PL/SQL Execution"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute PL/SQL: " + e.getMessage(),
                "plsqlCode", plsqlCode
            );
        }
    }

    @Tool(name = "oracle_create_procedure",
          description = "Create Oracle stored procedures")
    public Map<String, Object> createProcedure(
        @ToolParam(name = "procedureName", required = true) String procedureName,
        @ToolParam(name = "parameters", required = false) List<Map<String, Object>> parameters,
        @ToolParam(name = "procedureBody", required = true) String procedureBody,
        @ToolParam(name = "replaceExisting", required = false) Boolean replaceExisting) {
        
        try {
            StringBuilder sql = new StringBuilder("CREATE ");
            
            if (replaceExisting != null && replaceExisting) {
                sql.append("OR REPLACE ");
            }
            
            sql.append("PROCEDURE ").append(procedureName);
            
            // Add parameters
            if (parameters != null && !parameters.isEmpty()) {
                sql.append(" (");
                List<String> paramClause = new ArrayList<>();
                for (Map<String, Object> param : parameters) {
                    String paramName = (String) param.get("name");
                    String paramType = (String) param.get("type");
                    String paramMode = (String) param.getOrDefault("mode", "IN");
                    String defaultValue = (String) param.get("defaultValue");
                    
                    StringBuilder paramStr = new StringBuilder()
                        .append(paramName).append(" ")
                        .append(paramMode).append(" ")
                        .append(paramType);
                    
                    if (defaultValue != null) {
                        paramStr.append(" DEFAULT ").append(defaultValue);
                    }
                    
                    paramClause.add(paramStr.toString());
                }
                sql.append(String.join(", ", paramClause));
                sql.append(")");
            }
            
            sql.append(" AS BEGIN ");
            sql.append(procedureBody);
            sql.append(" END ").append(procedureName).append(";");
            
            jdbcTemplate.execute(sql.toString());
            
            return Map.of(
                "status", "success",
                "message", "Procedure created successfully",
                "procedureName", procedureName,
                "parameterCount", parameters != null ? parameters.size() : 0,
                "replaceExisting", replaceExisting != null ? replaceExisting : false,
                "oracleFeature", "Stored Procedures"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to create procedure: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_create_function",
          description = "Create Oracle user-defined functions")
    public Map<String, Object> createFunction(
        @ToolParam(name = "functionName", required = true) String functionName,
        @ToolParam(name = "parameters", required = false) List<Map<String, Object>> parameters,
        @ToolParam(name = "returnType", required = true) String returnType,
        @ToolParam(name = "functionBody", required = true) String functionBody,
        @ToolParam(name = "replaceExisting", required = false) Boolean replaceExisting) {
        
        try {
            StringBuilder sql = new StringBuilder("CREATE ");
            
            if (replaceExisting != null && replaceExisting) {
                sql.append("OR REPLACE ");
            }
            
            sql.append("FUNCTION ").append(functionName);
            
            // Add parameters
            if (parameters != null && !parameters.isEmpty()) {
                sql.append(" (");
                List<String> paramClause = new ArrayList<>();
                for (Map<String, Object> param : parameters) {
                    String paramName = (String) param.get("name");
                    String paramType = (String) param.get("type");
                    String paramMode = (String) param.getOrDefault("mode", "IN");
                    
                    paramClause.add(paramName + " " + paramMode + " " + paramType);
                }
                sql.append(String.join(", ", paramClause));
                sql.append(")");
            }
            
            sql.append(" RETURN ").append(returnType);
            sql.append(" AS BEGIN ");
            sql.append(functionBody);
            sql.append(" END ").append(functionName).append(";");
            
            jdbcTemplate.execute(sql.toString());
            
            return Map.of(
                "status", "success",
                "message", "Function created successfully",
                "functionName", functionName,
                "returnType", returnType,
                "parameterCount", parameters != null ? parameters.size() : 0,
                "oracleFeature", "User-Defined Functions"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to create function: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_manage_packages",
          description = "Create and manage Oracle packages")
    public Map<String, Object> managePackages(
        @ToolParam(name = "operation", required = true) String operation,
        @ToolParam(name = "packageName", required = true) String packageName,
        @ToolParam(name = "packageSpec", required = false) String packageSpec,
        @ToolParam(name = "packageBody", required = false) String packageBody) {
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            switch (operation.toUpperCase()) {
                case "CREATE_SPEC":
                    if (packageSpec == null) {
                        return Map.of("status", "error", "message", "Package specification required");
                    }
                    String specSql = String.format("CREATE OR REPLACE PACKAGE %s AS %s END %s;",
                        packageName, packageSpec, packageName);
                    jdbcTemplate.execute(specSql);
                    result.put("message", "Package specification created");
                    break;
                    
                case "CREATE_BODY":
                    if (packageBody == null) {
                        return Map.of("status", "error", "message", "Package body required");
                    }
                    String bodySql = String.format("CREATE OR REPLACE PACKAGE BODY %s AS %s END %s;",
                        packageName, packageBody, packageName);
                    jdbcTemplate.execute(bodySql);
                    result.put("message", "Package body created");
                    break;
                    
                case "DROP":
                    String dropSql = String.format("DROP PACKAGE %s", packageName);
                    jdbcTemplate.execute(dropSql);
                    result.put("message", "Package dropped");
                    break;
                    
                case "LIST":
                    List<Map<String, Object>> packages = jdbcTemplate.queryForList(
                        "SELECT object_name, object_type, status, last_ddl_time " +
                        "FROM user_objects WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                        "AND object_name LIKE ? ORDER BY object_name, object_type",
                        packageName.replace("*", "%"));
                    result.put("packages", packages);
                    break;
                    
                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }
            
            result.put("operation", operation);
            result.put("packageName", packageName);
            
            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Package Management"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage packages: " + e.getMessage()
            );
        }
    }

    @Tool(name = "oracle_debug_plsql",
          description = "Debug and profile PL/SQL code execution")
    public Map<String, Object> debugPlsql(
        @ToolParam(name = "objectName", required = true) String objectName,
        @ToolParam(name = "objectType", required = true) String objectType,
        @ToolParam(name = "operation", required = true) String operation,
        @ToolParam(name = "parameters", required = false) Map<String, Object> parameters) {
        
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            switch (operation.toUpperCase()) {
                case "COMPILE_DEBUG":
                    String compileSql = String.format("ALTER %s %s COMPILE DEBUG", 
                        objectType.toUpperCase(), objectName);
                    jdbcTemplate.execute(compileSql);
                    debugInfo.put("message", "Object compiled with debug information");
                    break;
                    
                case "SHOW_ERRORS":
                    List<Map<String, Object>> errors = jdbcTemplate.queryForList(
                        "SELECT line, position, text as error_text FROM user_errors " +
                        "WHERE name = ? AND type = ? ORDER BY sequence",
                        objectName.toUpperCase(), objectType.toUpperCase());
                    debugInfo.put("errors", errors);
                    debugInfo.put("errorCount", errors.size());
                    break;
                    
                case "PROFILE":
                    // Enable DBMS_PROFILER
                    jdbcTemplate.execute("BEGIN DBMS_PROFILER.START_PROFILER('PROFILE_" + 
                        objectName + "_" + System.currentTimeMillis() + "'); END;");
                    debugInfo.put("message", "Profiling started for " + objectName);
                    break;
                    
                case "STOP_PROFILE":
                    jdbcTemplate.execute("BEGIN DBMS_PROFILER.STOP_PROFILER; END;");
                    
                    // Get profiling results
                    List<Map<String, Object>> profileData = jdbcTemplate.queryForList(
                        "SELECT unit_name, line#, total_occur, total_time " +
                        "FROM plsql_profiler_data d, plsql_profiler_units u " +
                        "WHERE d.unit_number = u.unit_number " +
                        "AND u.unit_name = ? ORDER BY total_time DESC",
                        objectName.toUpperCase());
                    debugInfo.put("profileData", profileData);
                    break;
                    
                default:
                    return Map.of("status", "error", "message", "Unsupported debug operation: " + operation);
            }
            
            debugInfo.put("objectName", objectName);
            debugInfo.put("objectType", objectType);
            debugInfo.put("operation", operation);
            
            return Map.of(
                "status", "success",
                "debugInfo", debugInfo,
                "oracleFeature", "PL/SQL Debugging"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to debug PL/SQL: " + e.getMessage()
            );
        }
    }
}
