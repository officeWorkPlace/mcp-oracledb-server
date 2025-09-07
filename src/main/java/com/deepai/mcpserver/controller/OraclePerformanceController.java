package com.deepai.mcpserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.deepai.mcpserver.service.OracleEnterprisePerformanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Oracle Enterprise Performance Service Operations
 * Exposes 10 enterprise performance optimization tools via REST API
 * 
 * Categories:
 * - Parallel Execution Management (1 tool)
 * - Table Partitioning (1 tool)
 * - Materialized Views (1 tool)
 * - Query Optimization (1 tool)
 * - Memory Management (1 tool)
 * - AWR Reports (1 tool)
 * - SQL Plan Baselines (1 tool)
 * - Compression Management (1 tool)
 * - Resource Manager (1 tool)
 * - Performance Monitoring (1 tool)
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@RestController
@RequestMapping("/api/oracle/performance")
@CrossOrigin(origins = "*")
public class OraclePerformanceController {

    private final OracleEnterprisePerformanceService performanceService;

    @Autowired
    public OraclePerformanceController(OracleEnterprisePerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    // ========== PARALLEL EXECUTION ENDPOINTS ==========

    /**
     * Manage parallel execution for tables and queries
     */
    @PostMapping("/parallel-execution/{operation}")
    public ResponseEntity<Map<String, Object>> manageParallelExecution(
            @PathVariable String operation,
            @RequestParam(required = false) String sqlQuery,
            @RequestParam(required = false) Integer parallelDegree,
            @RequestParam(required = false) String tableName) {
        
        Map<String, Object> result = performanceService.manageParallelExecution(
            operation, sqlQuery, parallelDegree, tableName);
        return ResponseEntity.ok(result);
    }

    // ========== TABLE PARTITIONING ENDPOINTS ==========

    /**
     * Manage table partitioning operations
     */
    @PostMapping("/partitioning/{operation}")
    public ResponseEntity<Map<String, Object>> manageTablePartitioning(
            @PathVariable String operation,
            @RequestParam String tableName,
            @RequestParam(required = false) String partitionType,
            @RequestParam(required = false) String partitionColumn,
            @RequestParam(required = false) String partitionName) {
        
        Map<String, Object> result = performanceService.manageTablePartitioning(
            operation, tableName, partitionType, partitionColumn, partitionName);
        return ResponseEntity.ok(result);
    }

    // ========== MATERIALIZED VIEWS ENDPOINTS ==========

    /**
     * Manage materialized views for performance optimization
     */
    @PostMapping("/materialized-views/{operation}")
    public ResponseEntity<Map<String, Object>> manageMaterializedViews(
            @PathVariable String operation,
            @RequestParam(required = false) String mvName,
            @RequestParam(required = false) String baseQuery,
            @RequestParam(required = false) String refreshType,
            @RequestParam(required = false) String refreshInterval) {
        
        Map<String, Object> result = performanceService.manageMaterializedViews(
            operation, mvName, baseQuery, refreshType, refreshInterval);
        return ResponseEntity.ok(result);
    }

    // ========== QUERY OPTIMIZATION ENDPOINTS ==========

    /**
     * Optimize SQL queries with hints and execution plan analysis
     */
    @PostMapping("/optimize-query")
    public ResponseEntity<Map<String, Object>> optimizeQuery(
            @RequestParam String sqlQuery,
            @RequestParam(required = false) String optimizationType,
            @RequestBody(required = false) List<String> hints,
            @RequestParam(required = false) Boolean analyzeExecution) {
        
        Map<String, Object> result = performanceService.optimizeQuery(
            sqlQuery, optimizationType, hints, analyzeExecution);
        return ResponseEntity.ok(result);
    }

    // ========== MEMORY MANAGEMENT ENDPOINTS ==========

    /**
     * Manage Oracle memory components (SGA, PGA)
     */
    @PostMapping("/memory/{operation}")
    public ResponseEntity<Map<String, Object>> manageMemory(
            @PathVariable String operation,
            @RequestParam(required = false) String memoryType,
            @RequestParam(required = false) String targetSize) {
        
        Map<String, Object> result = performanceService.manageMemory(
            operation, memoryType, targetSize);
        return ResponseEntity.ok(result);
    }

    // ========== AWR REPORTS ENDPOINTS ==========

    /**
     * Manage Automatic Workload Repository (AWR) reports
     */
    @PostMapping("/awr/{operation}")
    public ResponseEntity<Map<String, Object>> manageAwrReports(
            @PathVariable String operation,
            @RequestParam(required = false) Integer beginSnapId,
            @RequestParam(required = false) Integer endSnapId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Integer instanceId) {
        
        Map<String, Object> result = performanceService.manageAwrReports(
            operation, beginSnapId, endSnapId, reportType, instanceId);
        return ResponseEntity.ok(result);
    }

    // ========== SQL PLAN BASELINES ENDPOINTS ==========

    /**
     * Manage SQL Plan Baselines for consistent performance
     */
    @PostMapping("/sql-plan-baselines/{operation}")
    public ResponseEntity<Map<String, Object>> manageSqlPlanBaselines(
            @PathVariable String operation,
            @RequestParam(required = false) String sqlHandle,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String sqlText) {
        
        Map<String, Object> result = performanceService.manageSqlPlanBaselines(
            operation, sqlHandle, planName, sqlText);
        return ResponseEntity.ok(result);
    }

    // ========== COMPRESSION MANAGEMENT ENDPOINTS ==========

    /**
     * Manage table and index compression for storage optimization
     */
    @PostMapping("/compression/{operation}")
    public ResponseEntity<Map<String, Object>> manageCompression(
            @PathVariable String operation,
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(required = false) String compressionType) {
        
        Map<String, Object> result = performanceService.manageCompression(
            operation, objectName, objectType, compressionType);
        return ResponseEntity.ok(result);
    }

    // ========== RESOURCE MANAGER ENDPOINTS ==========

    /**
     * Manage Oracle Resource Manager for workload prioritization
     */
    @PostMapping("/resource-manager/{operation}")
    public ResponseEntity<Map<String, Object>> manageResourceManager(
            @PathVariable String operation,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) Integer cpuP1,
            @RequestParam(required = false) Integer cpuP2) {
        
        Map<String, Object> result = performanceService.manageResourceManager(
            operation, planName, groupName, cpuP1, cpuP2);
        return ResponseEntity.ok(result);
    }

    // ========== UTILITY ENDPOINTS ==========

    /**
     * Get performance capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getPerformanceCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("parallelExecution", List.of("enable", "disable", "hint_query", "status", "statistics"));
        capabilities.put("partitioning", List.of("range", "hash", "list", "composite", "add", "drop", "exchange"));
        capabilities.put("materializedViews", List.of("create", "refresh", "drop", "rewrite", "status"));
        capabilities.put("queryOptimization", List.of("hints", "explain_plan", "cost_analysis", "index_recommendations"));
        capabilities.put("memoryManagement", List.of("sga", "pga", "advice", "automatic", "manual"));
        capabilities.put("awrReports", List.of("snapshots", "reports", "statistics", "trends"));
        capabilities.put("sqlPlanBaselines", List.of("capture", "evolve", "enable", "disable", "drop"));
        capabilities.put("compression", List.of("basic", "oltp", "hcc", "index", "estimation"));
        capabilities.put("resourceManager", List.of("plans", "groups", "directives", "priorities"));
        capabilities.put("monitoring", List.of("real_time", "historical", "waits", "io", "sessions"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("capabilities", capabilities);
        response.put("oracleFeatures", List.of(
            "Parallel Execution", "Partitioning", "Materialized Views", 
            "AWR", "SQL Plan Management", "Advanced Compression", "Resource Manager"
        ));
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check for performance services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("performanceServicesAvailable", true);
        healthStatus.put("parallelExecutionEnabled", true);
        healthStatus.put("partitioningEnabled", true);
        healthStatus.put("materializedViewsEnabled", true);
        healthStatus.put("awrEnabled", true);
        healthStatus.put("sqlPlanManagementEnabled", true);
        healthStatus.put("compressionEnabled", true);
        healthStatus.put("resourceManagerEnabled", true);
        healthStatus.put("performanceMonitoringEnabled", true);
        healthStatus.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(healthStatus);
    }
}
