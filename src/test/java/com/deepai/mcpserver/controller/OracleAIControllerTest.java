package com.deepai.mcpserver.controller;

import com.deepai.mcpserver.config.TestSecurityConfig;
import com.deepai.mcpserver.service.OracleAIService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for OracleAIController
 * Tests all 10 AI service endpoints
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(OracleAIController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Oracle AI Controller Tests")
class OracleAIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OracleAIService oracleAIService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== VECTOR SEARCH TESTS (4 tools) ==========

    @Test
    @DisplayName("Should perform vector search successfully")
    void shouldPerformVectorSearch() throws Exception {
        List<Double> queryVector = List.of(0.1, 0.2, 0.3, 0.4, 0.5);
        
        when(oracleAIService.performVectorSearch(eq("TEST_TABLE"), eq("EMBEDDING"), 
            eq(queryVector), eq("COSINE"), eq(10), any()))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(Map.of("ID", 1, "distance", 0.85)),
                "count", 1,
                "oracleFeature", "Oracle 23c Vector Search"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-search")
                .param("tableName", "TEST_TABLE")
                .param("vectorColumn", "EMBEDDING")
                .param("distanceMetric", "COSINE")
                .param("topK", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryVector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("Should calculate vector similarity successfully")
    void shouldCalculateVectorSimilarity() throws Exception {
        List<Double> vector1 = List.of(0.1, 0.2, 0.3);
        List<Double> vector2 = List.of(0.4, 0.5, 0.6);
        List<String> metrics = List.of("COSINE", "EUCLIDEAN");
        
        Map<String, Object> request = Map.of(
            "vector1", vector1,
            "vector2", vector2,
            "distanceMetrics", metrics
        );

        when(oracleAIService.calculateVectorSimilarity(eq(vector1), eq(vector2), eq(metrics)))
            .thenReturn(Map.of(
                "status", "success",
                "similarities", Map.of("cosine", 0.85, "euclidean", 0.52),
                "oracleFeature", "Vector Distance Functions"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-similarity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.similarities").exists());
    }

    @Test
    @DisplayName("Should perform vector clustering successfully")
    void shouldPerformVectorClustering() throws Exception {
        when(oracleAIService.performVectorClustering("TEST_TABLE", "EMBEDDING", 5, "COSINE", "ID"))
            .thenReturn(Map.of(
                "status", "success",
                "results", List.of(Map.of("ID", 1, "cluster_id", 1)),
                "requestedClusters", 5,
                "oracleFeature", "Vector Clustering"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-clustering")
                .param("tableName", "TEST_TABLE")
                .param("vectorColumn", "EMBEDDING")
                .param("clusterCount", "5")
                .param("distanceMetric", "COSINE")
                .param("identifierColumn", "ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.requestedClusters").value(5));
    }

    @Test
    @DisplayName("Should manage vector indexes successfully")
    void shouldManageVectorIndexes() throws Exception {
        Map<String, Object> indexParams = Map.of("neighbors", 50, "efConstruction", 200);

        when(oracleAIService.manageVectorIndex("CREATE", "IDX_VECTOR", "TEST_TABLE", 
            "EMBEDDING", "COSINE", indexParams))
            .thenReturn(Map.of(
                "status", "success",
                "operation", "CREATE",
                "indexName", "IDX_VECTOR",
                "oracleFeature", "Vector Index Management"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-indexes/CREATE")
                .param("indexName", "IDX_VECTOR")
                .param("tableName", "TEST_TABLE")
                .param("vectorColumn", "EMBEDDING")
                .param("distanceMetric", "COSINE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(indexParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.operation").value("CREATE"));
    }

    // ========== AI CONTENT ANALYSIS TESTS (3 tools) ==========

    @Test
    @DisplayName("Should analyze document successfully")
    void shouldAnalyzeDocument() throws Exception {
        when(oracleAIService.analyzeDocument("DOC_TABLE", "CONTENT", "1", "comprehensive", "oracle_ai"))
            .thenReturn(Map.of(
                "status", "success",
                "documentId", "1",
                "analysis", Map.of(
                    "characterCount", 1500,
                    "wordCount", 250,
                    "sentiment", "positive",
                    "topKeywords", List.of("oracle", "database", "performance")
                ),
                "oracleFeature", "AI Document Analysis"
            ));

        mockMvc.perform(post("/api/oracle/ai/analyze-document")
                .param("tableName", "DOC_TABLE")
                .param("documentColumn", "CONTENT")
                .param("documentId", "1")
                .param("analysisType", "comprehensive")
                .param("aiModel", "oracle_ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.analysis.characterCount").value(1500));
    }

    @Test
    @DisplayName("Should generate summary successfully")
    void shouldGenerateSummary() throws Exception {
        when(oracleAIService.generateSummary("DOC_TABLE", "CONTENT", "1", "extractive", 200))
            .thenReturn(Map.of(
                "status", "success",
                "documentId", "1",
                "summaryResult", Map.of(
                    "summary", "This document discusses Oracle database performance optimization techniques.",
                    "originalLength", 1500,
                    "summaryLength", 85,
                    "compressionRatio", "5.67%"
                ),
                "oracleFeature", "AI Content Summarization"
            ));

        mockMvc.perform(post("/api/oracle/ai/generate-summary")
                .param("tableName", "DOC_TABLE")
                .param("documentColumn", "CONTENT")
                .param("documentId", "1")
                .param("summaryType", "extractive")
                .param("maxLength", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.summaryResult.compressionRatio").value("5.67%"));
    }

    @Test
    @DisplayName("Should classify content successfully")
    void shouldClassifyContent() throws Exception {
        List<String> categories = List.of("technical", "business", "academic");

        when(oracleAIService.classifyContent("DOC_TABLE", "CONTENT", "1", categories, 0.8))
            .thenReturn(Map.of(
                "status", "success",
                "recordId", "1",
                "classification", Map.of(
                    "predictedCategory", "technical",
                    "confidence", 0.92,
                    "meetsThreshold", true
                ),
                "oracleFeature", "AI Content Classification"
            ));

        mockMvc.perform(post("/api/oracle/ai/classify-content")
                .param("tableName", "DOC_TABLE")
                .param("contentColumn", "CONTENT")
                .param("recordId", "1")
                .param("confidenceThreshold", "0.8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categories)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.classification.predictedCategory").value("technical"));
    }

    // ========== ORACLE-AI INTEGRATION TESTS (3 tools) ==========

    @Test
    @DisplayName("Should generate SQL from natural language successfully")
    void shouldGenerateSqlFromNaturalLanguage() throws Exception {
        List<String> tableContext = List.of("EMPLOYEES", "DEPARTMENTS");

        when(oracleAIService.generateSqlFromNaturalLanguage(
            "show all employees with their department names", tableContext, true))
            .thenReturn(Map.of(
                "status", "success",
                "sqlGeneration", Map.of(
                    "generatedSql", "SELECT e.*, d.department_name FROM EMPLOYEES e JOIN DEPARTMENTS d ON e.department_id = d.department_id",
                    "queryType", "SELECT",
                    "detectedTables", tableContext,
                    "recommendations", List.of("Consider using Oracle optimizer hints for performance")
                ),
                "oracleFeature", "AI SQL Generation"
            ));

        mockMvc.perform(post("/api/oracle/ai/generate-sql")
                .param("naturalLanguageQuery", "show all employees with their department names")
                .param("dialectOptimization", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tableContext)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.sqlGeneration.queryType").value("SELECT"));
    }

    @Test
    @DisplayName("Should optimize query successfully")
    void shouldOptimizeQuery() throws Exception {
        List<String> goals = List.of("performance", "readability");

        when(oracleAIService.optimizeQuery(
            "SELECT * FROM EMPLOYEES WHERE department_id = 10", goals, true))
            .thenReturn(Map.of(
                "status", "success",
                "optimization", Map.of(
                    "originalQuery", "SELECT * FROM EMPLOYEES WHERE department_id = 10",
                    "optimizedQuery", "SELECT /*+ FIRST_ROWS */ * FROM EMPLOYEES WHERE department_id = 10",
                    "optimizations", List.of("Added FIRST_ROWS hint for faster initial response"),
                    "estimatedCost", 125
                ),
                "oracleFeature", "AI Query Optimization"
            ));

        mockMvc.perform(post("/api/oracle/ai/optimize-query")
                .param("sqlQuery", "SELECT * FROM EMPLOYEES WHERE department_id = 10")
                .param("includeExplainPlan", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(goals)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.optimization.estimatedCost").value(125));
    }

    @Test
    @DisplayName("Should recommend schema design successfully")
    void shouldRecommendSchemaDesign() throws Exception {
        List<String> performanceGoals = List.of("scalability", "performance");

        when(oracleAIService.recommendSchemaDesign(
            "We need to store customer orders with items and track inventory", "large", performanceGoals, true))
            .thenReturn(Map.of(
                "status", "success",
                "schemaDesign", Map.of(
                    "recommendedTables", List.of(
                        Map.of("tableName", "CUSTOMER_TABLE", "columns", List.of("CUSTOMER_ID NUMBER PRIMARY KEY")),
                        Map.of("tableName", "ORDER_TABLE", "columns", List.of("ORDER_ID NUMBER PRIMARY KEY"))
                    ),
                    "detectedEntities", List.of("CUSTOMER", "ORDER"),
                    "performanceRecommendations", List.of("Consider using Oracle sequences for primary keys")
                ),
                "oracleFeature", "AI Schema Design"
            ));

        mockMvc.perform(post("/api/oracle/ai/recommend-schema")
                .param("businessRequirements", "We need to store customer orders with items and track inventory")
                .param("dataVolume", "large")
                .param("includePartitioning", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(performanceGoals)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.schemaDesign.detectedEntities").isArray());
    }

    // ========== UTILITY ENDPOINTS TESTS ==========

    @Test
    @DisplayName("Should get AI capabilities successfully")
    void shouldGetAICapabilities() throws Exception {
        mockMvc.perform(get("/api/oracle/ai/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.capabilities").exists())
                .andExpect(jsonPath("$.capabilities.vectorSearchSupported").value(true));
    }

    @Test
    @DisplayName("Should perform health check successfully")
    void shouldPerformHealthCheck() throws Exception {
        mockMvc.perform(get("/api/oracle/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.aiServicesAvailable").value(true));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle vector search without Oracle 23c")
    void shouldHandleVectorSearchError() throws Exception {
        when(oracleAIService.performVectorSearch(any(), any(), any(), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Vector search requires Oracle 23c or higher"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-search")
                .param("tableName", "TEST_TABLE")
                .param("vectorColumn", "EMBEDDING")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[0.1, 0.2, 0.3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("Should handle missing required parameters")
    void shouldHandleMissingParameters() throws Exception {
        mockMvc.perform(post("/api/oracle/ai/vector-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[0.1, 0.2, 0.3]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exceptions")
    void shouldHandleServiceExceptions() throws Exception {
        when(oracleAIService.performVectorSearch(any(), any(), any(), any(), any(), any()))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Database connection failed"
            ));

        mockMvc.perform(post("/api/oracle/ai/vector-search")
                .param("tableName", "TEST_TABLE")
                .param("vectorColumn", "EMBEDDING")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[0.1, 0.2, 0.3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Database connection failed"));
    }
}
