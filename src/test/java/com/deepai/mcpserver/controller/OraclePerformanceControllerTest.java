package com.deepai.mcpserver.controller;

import com.deepai.mcpserver.config.TestSecurityConfig;
import com.deepai.mcpserver.service.OracleEnterprisePerformanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for OraclePerformanceController
 * Tests all 10 performance service endpoints with correct method signatures
 */
@WebMvcTest(OraclePerformanceController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Oracle Performance Controller Tests")
@SuppressWarnings("deprecation")
class OraclePerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OracleEnterprisePerformanceService performanceService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> successResponse;
    private Map<String, Object> errorResponse;

    @BeforeEach
    void setUp() {
        successResponse = Map.of(
            "status", "success",
            "message", "Operation completed successfully",
            "timestamp", Instant.now()
        );

        errorResponse = Map.of(
            "status", "error",
            "message", "Operation failed"
        );
    }

    // ========== PARALLEL EXECUTION TESTS ==========

    @Test
    @DisplayName("Should manage parallel execution successfully")
    void shouldManageParallelExecution() throws Exception {
        when(performanceService.manageParallelExecution("ENABLE_PARALLEL", null, 4, "EMPLOYEES"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ENABLE_PARALLEL",
                    "message", "Parallel execution enabled for table",
                    "parallelDegree", 4,
                    "tableName", "EMPLOYEES"
                ),
                "oracleFeature", "Parallel Execution"
            ));

        mockMvc.perform(post("/api/oracle/performance/parallel-execution/ENABLE_PARALLEL")
                .param("parallelDegree", "4")
                .param("tableName", "EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.parallelDegree").value(4));
    }

    // ========== TABLE PARTITIONING TESTS ==========

    @Test
    @DisplayName("Should manage table partitioning successfully")
    void shouldManageTablePartitioning() throws Exception {
        when(performanceService.manageTablePartitioning("CREATE_RANGE_PARTITION", "ORDERS", "RANGE", "ORDER_DATE", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE_RANGE_PARTITION",
                    "message", "Range partitioned table created",
                    "tableName", "ORDERS",
                    "partitionType", "RANGE"
                ),
                "oracleFeature", "Table Partitioning"
            ));

        mockMvc.perform(post("/api/oracle/performance/partitioning/CREATE_RANGE_PARTITION")
                .param("tableName", "ORDERS")
                .param("partitionType", "RANGE")
                .param("partitionColumn", "ORDER_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("CREATE_RANGE_PARTITION"));
    }

    // ========== MATERIALIZED VIEWS TESTS ==========

    @Test
    @DisplayName("Should manage materialized views successfully")
    void shouldManageMaterializedViews() throws Exception {
        String baseQuery = "SELECT department, SUM(sales) FROM sales_data GROUP BY department";
        
        when(performanceService.manageMaterializedViews("CREATE", "MV_SALES_SUMMARY", baseQuery, "COMPLETE", "SYSDATE + 1"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE",
                    "message", "Materialized view created successfully",
                    "mvName", "MV_SALES_SUMMARY",
                    "refreshType", "COMPLETE"
                ),
                "oracleFeature", "Materialized Views"
            ));

        mockMvc.perform(post("/api/oracle/performance/materialized-views/CREATE")
                .param("mvName", "MV_SALES_SUMMARY")
                .param("baseQuery", baseQuery)
                .param("refreshType", "COMPLETE")
                .param("refreshInterval", "SYSDATE + 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.mvName").value("MV_SALES_SUMMARY"));
    }

    // ========== QUERY OPTIMIZATION TESTS ==========

    @Test
    @DisplayName("Should optimize queries successfully")
    void shouldOptimizeQuery() throws Exception {
        String sqlQuery = "SELECT * FROM employees WHERE department = 'IT'";
        List<String> hints = List.of("INDEX(employees, dept_idx)");
        
        when(performanceService.optimizeQuery(sqlQuery, "COST", hints, true))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "originalExecutionPlan", List.of(
                        Map.of("operation", "TABLE ACCESS FULL", "cost", 1000)
                    ),
                    "optimizedExecutionPlan", List.of(
                        Map.of("operation", "INDEX RANGE SCAN", "cost", 250)
                    ),
                    "costImprovement", "75.00%"
                ),
                "oracleFeature", "Query Optimization"
            ));

        mockMvc.perform(post("/api/oracle/performance/optimize-query")
                .param("sqlQuery", sqlQuery)
                .param("optimizationType", "COST")
                .param("analyzeExecution", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hints)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.costImprovement").value("75.00%"));
    }

    // ========== MEMORY MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should manage memory successfully")
    void shouldManageMemory() throws Exception {
        when(performanceService.manageMemory("SGA_STATUS", null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "SGA_STATUS",
                    "sgaComponents", List.of(
                        Map.of("name", "Fixed Size", "value", 2936320),
                        Map.of("name", "Variable Size", "value", 1073741824),
                        Map.of("name", "Database Buffers", "value", 536870912),
                        Map.of("name", "Redo Buffers", "value", 16777216)
                    )
                ),
                "oracleFeature", "Memory Management"
            ));

        mockMvc.perform(post("/api/oracle/performance/memory/SGA_STATUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("SGA_STATUS"));
    }

    // ========== AWR REPORTS TESTS ==========

    @Test
    @DisplayName("Should take AWR snapshot successfully")
    void shouldTakeAwrSnapshot() throws Exception {
        when(performanceService.manageAwrReports("TAKE_SNAPSHOT", null, null, null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "TAKE_SNAPSHOT",
                    "message", "AWR snapshot created successfully",
                    "snapshotId", 12345,
                    "snapshotTime", "2024-01-01 12:00:00"
                ),
                "oracleFeature", "Automatic Workload Repository (AWR)"
            ));

        mockMvc.perform(post("/api/oracle/performance/awr/TAKE_SNAPSHOT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.snapshotId").value(12345));
    }

    @Test
    @DisplayName("Should generate AWR report successfully")
    void shouldGenerateAwrReport() throws Exception {
        when(performanceService.manageAwrReports("GENERATE_REPORT", 100, 101, "TEXT", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "GENERATE_REPORT",
                    "message", "AWR report generation initiated",
                    "beginSnapId", 100,
                    "endSnapId", 101,
                    "reportType", "TEXT",
                    "topSqlByElapsedTime", List.of(
                        Map.of("sql_id", "abc123", "elapsed_time", 5000000)
                    )
                ),
                "oracleFeature", "Automatic Workload Repository (AWR)"
            ));

        mockMvc.perform(post("/api/oracle/performance/awr/GENERATE_REPORT")
                .param("beginSnapId", "100")
                .param("endSnapId", "101")
                .param("reportType", "TEXT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== SQL PLAN BASELINES TESTS ==========

    @Test
    @DisplayName("Should capture SQL plan baselines successfully")
    void shouldCaptureSqlPlanBaselines() throws Exception {
        when(performanceService.manageSqlPlanBaselines("CAPTURE_BASELINES", null, null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CAPTURE_BASELINES",
                    "message", "SQL Plan Baseline capture enabled"
                ),
                "oracleFeature", "SQL Plan Management"
            ));

        mockMvc.perform(post("/api/oracle/performance/sql-plan-baselines/CAPTURE_BASELINES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should enable SQL plan baseline successfully")
    void shouldEnableSqlPlanBaseline() throws Exception {
        when(performanceService.manageSqlPlanBaselines("ENABLE_BASELINE", "SYS_SQL_12345", "SYS_SQL_PLAN_67890", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ENABLE_BASELINE",
                    "message", "SQL Plan Baseline enabled",
                    "sqlHandle", "SYS_SQL_12345",
                    "planName", "SYS_SQL_PLAN_67890"
                ),
                "oracleFeature", "SQL Plan Management"
            ));

        mockMvc.perform(post("/api/oracle/performance/sql-plan-baselines/ENABLE_BASELINE")
                .param("sqlHandle", "SYS_SQL_12345")
                .param("planName", "SYS_SQL_PLAN_67890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== COMPRESSION MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should manage compression successfully")
    void shouldManageCompression() throws Exception {
        when(performanceService.manageCompression("ENABLE_COMPRESSION", "ORDERS", "TABLE", "BASIC"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ENABLE_COMPRESSION",
                    "objectName", "ORDERS",
                    "objectType", "TABLE",
                    "compressionType", "BASIC",
                    "message", "Compression enabled successfully"
                ),
                "oracleFeature", "Compression Management"
            ));

        mockMvc.perform(post("/api/oracle/performance/compression/ENABLE_COMPRESSION")
                .param("objectName", "ORDERS")
                .param("objectType", "TABLE")
                .param("compressionType", "BASIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== RESOURCE MANAGER TESTS ==========

    @Test
    @DisplayName("Should create resource plan successfully")
    void shouldCreateResourcePlan() throws Exception {
        when(performanceService.manageResourceManager("CREATE_PLAN", "CUSTOM_PLAN", null, null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE_PLAN",
                    "planName", "CUSTOM_PLAN",
                    "message", "Resource plan created successfully"
                ),
                "oracleFeature", "Database Resource Manager"
            ));

        mockMvc.perform(post("/api/oracle/performance/resource-manager/CREATE_PLAN")
                .param("planName", "CUSTOM_PLAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should create plan directive successfully")
    void shouldCreatePlanDirective() throws Exception {
        when(performanceService.manageResourceManager("CREATE_PLAN_DIRECTIVE", "CUSTOM_PLAN", "BATCH_USERS", 30, 50))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE_PLAN_DIRECTIVE",
                    "planName", "CUSTOM_PLAN",
                    "groupName", "BATCH_USERS",
                    "message", "Plan directive created successfully"
                ),
                "oracleFeature", "Database Resource Manager"
            ));

        mockMvc.perform(post("/api/oracle/performance/resource-manager/CREATE_PLAN_DIRECTIVE")
                .param("planName", "CUSTOM_PLAN")
                .param("groupName", "BATCH_USERS")
                .param("cpuP1", "30")
                .param("cpuP2", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle service errors gracefully")
    void shouldHandleServiceErrors() throws Exception {
        when(performanceService.manageParallelExecution("INVALID_OPERATION", null, 4, "EMPLOYEES"))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Unsupported operation: INVALID_OPERATION"
            ));

        mockMvc.perform(post("/api/oracle/performance/parallel-execution/INVALID_OPERATION")
                .param("parallelDegree", "4")
                .param("tableName", "EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Unsupported operation: INVALID_OPERATION"));
    }

    // ========== UTILITY ENDPOINT TESTS ==========

    @Test
    @DisplayName("Should get performance capabilities")
    void shouldGetPerformanceCapabilities() throws Exception {
        mockMvc.perform(get("/api/oracle/performance/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.capabilities").exists())
                .andExpect(jsonPath("$.oracleFeatures").exists());
    }

    @Test
    @DisplayName("Should perform health check")
    void shouldPerformHealthCheck() throws Exception {
        mockMvc.perform(get("/api/oracle/performance/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.performanceServicesAvailable").value(true));
    }
}
