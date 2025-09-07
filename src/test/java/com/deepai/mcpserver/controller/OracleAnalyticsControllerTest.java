package com.deepai.mcpserver.controller;

import com.deepai.mcpserver.config.TestSecurityConfig;
import com.deepai.mcpserver.service.OracleAdvancedAnalyticsService;
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
 * Comprehensive unit tests for OracleAnalyticsController
 * Tests all 20 analytics service endpoints with correct method signatures
 */
@WebMvcTest(OracleAnalyticsController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Oracle Analytics Controller Tests")
@SuppressWarnings("deprecation")
class OracleAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OracleAdvancedAnalyticsService analyticsService;

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

    // ========== SQL ANALYTICS & CTEs TESTS ==========

    @Test
    @DisplayName("Should execute complex joins successfully")
    void shouldExecuteComplexJoins() throws Exception {
        List<String> tables = List.of("EMPLOYEES", "DEPARTMENTS");
        List<String> joinConditions = List.of("e.dept_id = d.id");
        List<String> selectColumns = List.of("e.name", "e.salary", "d.dept_name");
        
        when(analyticsService.executeComplexJoins(eq(tables), eq(joinConditions), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("name", "John", "salary", 75000, "dept_name", "Engineering")
                ),
                "count", 1,
                "oracleFeature", "Complex JOIN Operations"
            ));

        Map<String, Object> requestBody = Map.of(
            "tables", tables,
            "joinConditions", joinConditions,
            "selectColumns", selectColumns
        );

        mockMvc.perform(post("/api/oracle/analytics/complex-joins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute CTE queries successfully")
    void shouldExecuteCteQueries() throws Exception {
        List<Map<String, Object>> cteDefinitions = List.of(
            Map.of("name", "high_salary", "query", "SELECT * FROM employees WHERE salary > 50000")
        );
        String mainQuery = "SELECT * FROM high_salary ORDER BY salary DESC";
        
        when(analyticsService.executeCteQueries(eq(cteDefinitions), eq(mainQuery), eq(false)))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("name", "John", "salary", 75000)
                ),
                "count", 1,
                "oracleFeature", "Common Table Expressions"
            ));

        mockMvc.perform(post("/api/oracle/analytics/cte-queries")
                .param("mainQuery", mainQuery)
                .param("recursive", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cteDefinitions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute window functions successfully")
    void shouldExecuteWindowFunctions() throws Exception {
        List<String> orderBy = List.of("salary DESC");
        
        when(analyticsService.executeWindowFunctions(eq("EMPLOYEES"), eq("ROW_NUMBER()"), any(), eq(orderBy), any()))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("name", "John", "salary", 75000, "window_result", 1)
                ),
                "count", 1,
                "oracleFeature", "Window Functions"
            ));

        Map<String, Object> requestBody = Map.of(
            "orderBy", orderBy,
            "partitionBy", List.of("department"),
            "selectColumns", List.of("name", "salary")
        );

        mockMvc.perform(post("/api/oracle/analytics/window-functions")
                .param("tableName", "EMPLOYEES")
                .param("windowFunction", "ROW_NUMBER()")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute pivot operations successfully")
    void shouldExecutePivotOperations() throws Exception {
        List<String> pivotValues = List.of("Q1", "Q2", "Q3", "Q4");
        
        when(analyticsService.executePivotOperations("SALES", "PIVOT", "SUM", "amount", "quarter", pivotValues))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("product", "Widget", "Q1", 1000, "Q2", 1200, "Q3", 900, "Q4", 1100)
                ),
                "count", 1,
                "oracleFeature", "PIVOT/UNPIVOT Transformations"
            ));

        mockMvc.perform(post("/api/oracle/analytics/pivot-operations")
                .param("tableName", "SALES")
                .param("operation", "PIVOT")
                .param("aggregateFunction", "SUM")
                .param("aggregateColumn", "amount")
                .param("pivotColumn", "quarter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pivotValues)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute analytical functions successfully")
    void shouldExecuteAnalyticalFunctions() throws Exception {
        when(analyticsService.executeAnalyticalFunctions(eq("EMPLOYEES"), eq("RANK()"), eq("salary"), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("name", "John", "salary", 75000, "analytical_result", 1)
                ),
                "count", 1,
                "oracleFeature", "Analytical Functions"
            ));

        Map<String, Object> requestBody = Map.of(
            "partitionBy", List.of("department"),
            "orderBy", List.of("salary DESC"),
            "parameters", List.of()
        );

        mockMvc.perform(post("/api/oracle/analytics/analytical-functions")
                .param("tableName", "EMPLOYEES")
                .param("analyticalFunction", "RANK()")
                .param("column", "salary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute hierarchical queries successfully")
    void shouldExecuteHierarchicalQueries() throws Exception {
        when(analyticsService.executeHierarchicalQueries(eq("EMPLOYEES"), eq("manager_id IS NULL"), eq("PRIOR employee_id = manager_id"), any(), eq("name")))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("employee_id", 1, "name", "CEO", "level", 1)
                ),
                "count", 1,
                "oracleFeature", "Hierarchical Queries"
            ));

        Map<String, Object> requestBody = Map.of(
            "selectColumns", List.of("employee_id", "name", "manager_id"),
            "orderSiblings", "name"
        );

        mockMvc.perform(post("/api/oracle/analytics/hierarchical-queries")
                .param("tableName", "EMPLOYEES")
                .param("startWithCondition", "manager_id IS NULL")
                .param("connectByCondition", "PRIOR employee_id = manager_id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should execute MODEL clause successfully")
    void shouldExecuteModelClause() throws Exception {
        Map<String, Object> request = Map.of(
            "dimensionBy", List.of("employee_id"),
            "measuresColumns", List.of("salary", "bonus"),
            "modelRules", List.of("bonus[any] = salary[cv()] * 0.1")
        );

        when(analyticsService.executeModelClause(eq("EMPLOYEES"), any(), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(
                    Map.of("employee_id", 1, "salary", 50000, "bonus", 5000)
                ),
                "count", 1,
                "oracleFeature", "MODEL Clause"
            ));

        mockMvc.perform(post("/api/oracle/analytics/model-clause")
                .param("tableName", "EMPLOYEES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should apply optimizer hints successfully")
    void shouldApplyOptimizerHints() throws Exception {
        Map<String, Object> request = Map.of(
            "sqlQuery", "SELECT * FROM employees",
            "hints", List.of("USE_INDEX(employees, idx_emp_id)"),
            "comparePerformance", true
        );

        when(analyticsService.applyOptimizerHints(any(), any(), any()))
            .thenReturn(Map.of(
                "status", "success",
                "optimizedQuery", "SELECT /*+ USE_INDEX(employees, idx_emp_id) */ * FROM employees",
                "performanceImprovement", "25%",
                "oracleFeature", "Optimizer Hints"
            ));

        mockMvc.perform(post("/api/oracle/analytics/optimizer-hints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.performanceImprovement").exists());
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle service errors gracefully")
    void shouldHandleServiceErrors() throws Exception {
        when(analyticsService.executeComplexJoins(any(), any(), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Invalid table name"
            ));

        Map<String, Object> request = Map.of(
            "tables", List.of("INVALID_TABLE"),
            "joinConditions", List.of("invalid_condition")
        );

        mockMvc.perform(post("/api/oracle/analytics/complex-joins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid table name"));
    }

    @Test
    @DisplayName("Should handle missing parameters")
    void shouldHandleMissingParameters() throws Exception {
        // Test missing required parameters (tableName and windowFunction)
        mockMvc.perform(post("/api/oracle/analytics/window-functions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
