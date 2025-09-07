package com.deepai.mcpserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.deepai.mcpserver.service.OracleAIService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Oracle AI Service Operations
 * Exposes 10 AI-powered Oracle tools via REST API
 * 
 * Categories:
 * - Oracle Vector Search (4 tools)
 * - AI Content Analysis (3 tools) 
 * - Oracle-AI Integration (3 tools)
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@RestController
@RequestMapping("/api/oracle/ai")
@CrossOrigin(origins = "*")
public class OracleAIController {

    private final OracleAIService oracleAIService;

    @Autowired
    public OracleAIController(OracleAIService oracleAIService) {
        this.oracleAIService = oracleAIService;
    }

    // ========== ORACLE VECTOR SEARCH ENDPOINTS (4 tools) ==========

    /**
     * Perform vector similarity search using Oracle 23c Vector Search
     */
    @PostMapping("/vector-search")
    public ResponseEntity<Map<String, Object>> performVectorSearch(
            @RequestParam String tableName,
            @RequestParam String vectorColumn,
            @RequestBody List<Double> queryVector,
            @RequestParam(required = false) String distanceMetric,
            @RequestParam(required = false) Integer topK,
            @RequestBody(required = false) List<String> additionalColumns) {
        
        Map<String, Object> result = oracleAIService.performVectorSearch(
            tableName, vectorColumn, queryVector, distanceMetric, topK, additionalColumns);
        return ResponseEntity.ok(result);
    }

    /**
     * Calculate similarity between two vectors
     */
    @PostMapping("/vector-similarity")
    public ResponseEntity<Map<String, Object>> calculateVectorSimilarity(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Double> vector1 = (List<Double>) request.get("vector1");
        @SuppressWarnings("unchecked")
        List<Double> vector2 = (List<Double>) request.get("vector2");
        @SuppressWarnings("unchecked")
        List<String> distanceMetrics = (List<String>) request.get("distanceMetrics");
        
        Map<String, Object> result = oracleAIService.calculateVectorSimilarity(vector1, vector2, distanceMetrics);
        return ResponseEntity.ok(result);
    }

    /**
     * Perform vector clustering on table data
     */
    @PostMapping("/vector-clustering")
    public ResponseEntity<Map<String, Object>> performVectorClustering(
            @RequestParam String tableName,
            @RequestParam String vectorColumn,
            @RequestParam(required = false) Integer clusterCount,
            @RequestParam(required = false) String distanceMetric,
            @RequestParam(required = false) String identifierColumn) {
        
        Map<String, Object> result = oracleAIService.performVectorClustering(
            tableName, vectorColumn, clusterCount, distanceMetric, identifierColumn);
        return ResponseEntity.ok(result);
    }

    /**
     * Manage vector indexes for improved performance
     */
    @PostMapping("/vector-indexes/{operation}")
    public ResponseEntity<Map<String, Object>> manageVectorIndex(
            @PathVariable String operation,
            @RequestParam String indexName,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String vectorColumn,
            @RequestParam(required = false) String distanceMetric,
            @RequestBody(required = false) Map<String, Object> indexParameters) {
        
        Map<String, Object> result = oracleAIService.manageVectorIndex(
            operation, indexName, tableName, vectorColumn, distanceMetric, indexParameters);
        return ResponseEntity.ok(result);
    }

    // ========== AI CONTENT ANALYSIS ENDPOINTS (3 tools) ==========

    /**
     * Analyze document content using AI techniques
     */
    @PostMapping("/analyze-document")
    public ResponseEntity<Map<String, Object>> analyzeDocument(
            @RequestParam String tableName,
            @RequestParam String documentColumn,
            @RequestParam String documentId,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String aiModel) {
        
        Map<String, Object> result = oracleAIService.analyzeDocument(
            tableName, documentColumn, documentId, analysisType, aiModel);
        return ResponseEntity.ok(result);
    }

    /**
     * Generate summary of document content
     */
    @PostMapping("/generate-summary")
    public ResponseEntity<Map<String, Object>> generateSummary(
            @RequestParam String tableName,
            @RequestParam String documentColumn,
            @RequestParam String documentId,
            @RequestParam(required = false) String summaryType,
            @RequestParam(required = false) Integer maxLength) {
        
        Map<String, Object> result = oracleAIService.generateSummary(
            tableName, documentColumn, documentId, summaryType, maxLength);
        return ResponseEntity.ok(result);
    }

    /**
     * Classify content into predefined categories
     */
    @PostMapping("/classify-content")
    public ResponseEntity<Map<String, Object>> classifyContent(
            @RequestParam String tableName,
            @RequestParam String contentColumn,
            @RequestParam String recordId,
            @RequestBody(required = false) List<String> categories,
            @RequestParam(required = false) Double confidenceThreshold) {
        
        Map<String, Object> result = oracleAIService.classifyContent(
            tableName, contentColumn, recordId, categories, confidenceThreshold);
        return ResponseEntity.ok(result);
    }

    // ========== ORACLE-AI INTEGRATION ENDPOINTS (3 tools) ==========

    /**
     * Generate SQL from natural language description
     */
    @PostMapping("/generate-sql")
    public ResponseEntity<Map<String, Object>> generateSqlFromNaturalLanguage(
            @RequestParam String naturalLanguageQuery,
            @RequestBody(required = false) List<String> tableContext,
            @RequestParam(required = false) Boolean dialectOptimization) {
        
        Map<String, Object> result = oracleAIService.generateSqlFromNaturalLanguage(
            naturalLanguageQuery, tableContext, dialectOptimization);
        return ResponseEntity.ok(result);
    }

    /**
     * Optimize SQL query using AI recommendations
     */
    @PostMapping("/optimize-query")
    public ResponseEntity<Map<String, Object>> optimizeQuery(
            @RequestParam String sqlQuery,
            @RequestBody(required = false) List<String> optimizationGoals,
            @RequestParam(required = false) Boolean includeExplainPlan) {
        
        Map<String, Object> result = oracleAIService.optimizeQuery(sqlQuery, optimizationGoals, includeExplainPlan);
        return ResponseEntity.ok(result);
    }

    /**
     * Recommend schema design based on business requirements
     */
    @PostMapping("/recommend-schema")
    public ResponseEntity<Map<String, Object>> recommendSchemaDesign(
            @RequestParam String businessRequirements,
            @RequestParam(required = false) String dataVolume,
            @RequestBody(required = false) List<String> performanceGoals,
            @RequestParam(required = false) Boolean includePartitioning) {
        
        Map<String, Object> result = oracleAIService.recommendSchemaDesign(
            businessRequirements, dataVolume, performanceGoals, includePartitioning);
        return ResponseEntity.ok(result);
    }

    // ========== ADDITIONAL UTILITY ENDPOINTS ==========

    /**
     * Get AI service capabilities and Oracle feature support
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getAICapabilities() {
        Map<String, Object> capabilities = Map.of(
            "vectorSearchSupported", true,
            "aiAnalysisSupported", true, 
            "naturalLanguageSqlSupported", true,
            "supportedDistanceMetrics", List.of("COSINE", "EUCLIDEAN", "DOT", "MANHATTAN"),
            "supportedAnalysisTypes", List.of("comprehensive", "keyword", "sentiment", "classification"),
            "supportedSummaryTypes", List.of("extractive", "abstractive"),
            "oracleVersionRequired", "19c+",
            "vectorSearchVersionRequired", "23c+"
        );
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "capabilities", capabilities,
            "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * Health check for AI services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "aiServicesAvailable", true,
            "vectorSearchEnabled", true,
            "contentAnalysisEnabled", true,
            "sqlGenerationEnabled", true,
            "timestamp", java.time.Instant.now()
        );
        
        return ResponseEntity.ok(health);
    }
}
