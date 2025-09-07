package com.deepai.mcpserver.service;

// Using Spring Function approach for Spring AI integration
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.deepai.mcpserver.util.OracleFeatureDetector;
import com.deepai.mcpserver.util.OracleSqlBuilder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;
import java.util.*;

/**
 * Oracle AI Service - 10 AI-Powered Tools
 * Provides Vector Search, AI Content Analysis, and Oracle-AI Integration
 */
@Service
public class OracleAIService {

    private final JdbcTemplate jdbcTemplate;
    private final OracleFeatureDetector featureDetector;
    private final OracleSqlBuilder sqlBuilder;

    @Autowired
    public OracleAIService(JdbcTemplate jdbcTemplate, 
                          OracleFeatureDetector featureDetector,
                          OracleSqlBuilder sqlBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureDetector = featureDetector;
        this.sqlBuilder = sqlBuilder;
    }

    // ========== ORACLE VECTOR SEARCH (4 TOOLS) ==========

    // Oracle Vector Search - native similarity search using Oracle 23c vectors
    public Map<String, Object> performVectorSearch(
        String tableName,
        String vectorColumn,
        List<Double> queryVector,
        String distanceMetric,
        Integer topK,
        List<String> additionalColumns) {

        if (!featureDetector.supportsVectorSearch()) {
            return Map.of(
                "status", "error",
                "message", "Vector search requires Oracle 23c or higher"
            );
        }

        try {
            String metric = distanceMetric != null ? distanceMetric.toUpperCase() : "COSINE";
            Integer limit = topK != null ? topK : 10;

            StringBuilder sql = new StringBuilder("SELECT ");

            // Select additional columns if specified
            if (additionalColumns != null && !additionalColumns.isEmpty()) {
                sql.append(String.join(", ", additionalColumns)).append(", ");
            }

            // Vector distance calculation
            String vectorArrayStr = "[" + String.join(",", 
                queryVector.stream().map(Object::toString).toArray(String[]::new)) + "]";

            sql.append("VECTOR_DISTANCE(").append(vectorColumn)
               .append(", TO_VECTOR('").append(vectorArrayStr).append("'), ")
               .append(metric).append(") as distance");

            sql.append(" FROM ").append(tableName);
            sql.append(" ORDER BY VECTOR_DISTANCE(").append(vectorColumn)
               .append(", TO_VECTOR('").append(vectorArrayStr).append("'), ")
               .append(metric).append(")");
            sql.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());

            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "queryVector", queryVector,
                "distanceMetric", metric,
                "topK", limit,
                "query", sql.toString(),
                "oracleFeature", "Oracle 23c Vector Search"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to perform vector search: " + e.getMessage()
            );
        }
    }

    // Oracle Vector Similarity - calculate similarity between vectors
    public Map<String, Object> calculateVectorSimilarity(
        List<Double> vector1,
        List<Double> vector2,
        List<String> distanceMetrics) {

        if (!featureDetector.supportsVectorSearch()) {
            return Map.of(
                "status", "error",
                "message", "Vector operations require Oracle 23c or higher"
            );
        }

        try {
            List<String> metrics = distanceMetrics != null ? distanceMetrics : 
                Arrays.asList("COSINE", "EUCLIDEAN", "DOT", "MANHATTAN");

            String vector1Str = "[" + String.join(",", 
                vector1.stream().map(Object::toString).toArray(String[]::new)) + "]";
            String vector2Str = "[" + String.join(",", 
                vector2.stream().map(Object::toString).toArray(String[]::new)) + "]";

            Map<String, Object> similarities = new HashMap<>();

            for (String metric : metrics) {
                try {
                    String sql = String.format(
                        "SELECT VECTOR_DISTANCE(TO_VECTOR('%s'), TO_VECTOR('%s'), %s) as distance FROM dual",
                        vector1Str, vector2Str, metric.toUpperCase()
                    );

                    Map<String, Object> result = jdbcTemplate.queryForMap(sql);
                    similarities.put(metric.toLowerCase(), result.get("distance"));
                } catch (Exception metricException) {
                    similarities.put(metric.toLowerCase(), "Not supported");
                }
            }

            return Map.of(
                "status", "success",
                "vector1", vector1,
                "vector2", vector2,
                "similarities", similarities,
                "supportedMetrics", metrics,
                "oracleFeature", "Vector Distance Functions"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to calculate vector similarity: " + e.getMessage()
            );
        }
    }

    @Tool(name = "performVectorClustering", description = "Perform vector clustering using Oracle 23c vector operations")
    public Map<String, Object> performVectorClustering(
         @ToolParam(description = "Table containing vectors", required = true) String tableName,
         @ToolParam(description = "Vector column name", required = true) String vectorColumn,
         @ToolParam(description = "Number of clusters", required = false) Integer clusterCount,
         @ToolParam(description = "Distance metric", required = false) String distanceMetric,
         @ToolParam(description = "ID column", required = false) String identifierColumn) {

        if (!featureDetector.supportsVectorSearch()) {
            return Map.of(
                "status", "error",
                "message", "Vector clustering requires Oracle 23c or higher"
            );
        }

        try {
            Integer clusters = clusterCount != null ? clusterCount : 5;
            String metric = distanceMetric != null ? distanceMetric.toUpperCase() : "COSINE";
            String idCol = identifierColumn != null ? identifierColumn : "ROWID";

            // Use Oracle's CLUSTER_DETAILS analytic function for vector clustering
            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(idCol).append(", ");
            sql.append(vectorColumn).append(", ");
            sql.append("CLUSTER_ID(").append(clusters).append(") OVER (");
            sql.append("ORDER BY ").append(vectorColumn).append(") as cluster_id, ");
            sql.append("CLUSTER_DISTANCE(").append(clusters).append(") OVER (");
            sql.append("ORDER BY ").append(vectorColumn).append(") as cluster_distance");
            sql.append(" FROM ").append(tableName);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());

            // Calculate cluster statistics
            Map<String, Object> clusterStats = jdbcTemplate.queryForMap(
                "SELECT COUNT(DISTINCT cluster_id) as actual_clusters, " +
                "COUNT(*) as total_vectors, " +
                "AVG(cluster_distance) as avg_distance " +
                "FROM (" + sql.toString() + ")");

            return Map.of(
                "status", "success",
                "results", results,
                "count", results.size(),
                "requestedClusters", clusters,
                "clusterStatistics", clusterStats,
                "distanceMetric", metric,
                "query", sql.toString(),
                "oracleFeature", "Vector Clustering"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to perform vector clustering: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manageVectorIndex", description = "Manage Oracle 23c vector indexes for similarity search")
    public Map<String, Object> manageVectorIndex(
         String operation,
         String indexName,
         String tableName,
         String vectorColumn,
         String distanceMetric,
         Map<String, Object> indexParameters) {

        if (!featureDetector.supportsVectorSearch()) {
            return Map.of(
                "status", "error",
                "message", "Vector indexes require Oracle 23c or higher"
            );
        }

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE":
                    if (tableName == null || vectorColumn == null) {
                        return Map.of("status", "error", "message", "Table name and vector column required for CREATE");
                    }

                    String metric = distanceMetric != null ? distanceMetric.toUpperCase() : "COSINE";

                    StringBuilder createSql = new StringBuilder("CREATE VECTOR INDEX ");
                    createSql.append(indexName).append(" ON ").append(tableName);
                    createSql.append(" (").append(vectorColumn).append(") ");
                    createSql.append("ORGANIZATION INMEMORY NEIGHBOR GRAPH ");
                    createSql.append("DISTANCE ").append(metric);

                    if (indexParameters != null) {
                        createSql.append(" PARAMETERS ('");
                        List<String> params = new ArrayList<>();
                        indexParameters.forEach((key, value) -> 
                            params.add(key + "=" + value.toString()));
                        createSql.append(String.join(",", params));
                        createSql.append("')");
                    }

                    jdbcTemplate.execute(createSql.toString());
                    result.put("message", "Vector index created successfully");
                    result.put("sql", createSql.toString());
                    break;

                case "DROP":
                    String dropSql = "DROP INDEX " + indexName;
                    jdbcTemplate.execute(dropSql);
                    result.put("message", "Vector index dropped successfully");
                    break;

                case "REBUILD":
                    String rebuildSql = "ALTER INDEX " + indexName + " REBUILD";
                    jdbcTemplate.execute(rebuildSql);
                    result.put("message", "Vector index rebuilt successfully");
                    break;

                case "STATUS":
                    Map<String, Object> indexInfo = jdbcTemplate.queryForMap(
                        "SELECT index_name, index_type, status, tablespace_name " +
                        "FROM user_indexes WHERE index_name = ?", indexName.toUpperCase());
                    result.put("indexInfo", indexInfo);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("indexName", indexName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Vector Index Management"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage vector index: " + e.getMessage()
            );
        }
    }

    // ========== AI CONTENT ANALYSIS (3 TOOLS) ==========

    @Tool(name = "analyzeDocument", description = "AI-powered document analysis with keyword extraction and sentiment")
    public Map<String, Object> analyzeDocument(
         String tableName,
         String documentColumn,
         String documentId,
         String analysisType,
         String aiModel) {

        try {
            String analysis = analysisType != null ? analysisType : "comprehensive";
            String model = aiModel != null ? aiModel : "oracle_ai";

            // Get document content
            Map<String, Object> document = jdbcTemplate.queryForMap(
                String.format("SELECT %s as content FROM %s WHERE id = ?", documentColumn, tableName),
                documentId);

            String content = (String) document.get("content");

            Map<String, Object> analysisResult = new HashMap<>();

            // Basic text analysis
            analysisResult.put("characterCount", content.length());
            analysisResult.put("wordCount", content.split("\\s+").length);
            analysisResult.put("paragraphCount", content.split("\\n\\s*\\n").length);

            // Keyword extraction (simplified)
            String[] words = content.toLowerCase().split("[\\s\\p{Punct}]+");
            Map<String, Integer> wordFreq = new HashMap<>();
            for (String word : words) {
                if (word.length() > 3) {
                    wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                }
            }

            // Get top keywords
            List<Map<String, Object>> topKeywords = wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> keywordMap = new HashMap<>();
                    keywordMap.put("keyword", entry.getKey());
                    keywordMap.put("frequency", entry.getValue());
                    return keywordMap;
                })
                .collect(java.util.stream.Collectors.toList());

            analysisResult.put("topKeywords", topKeywords);

            // Sentiment analysis (basic)
            String[] positiveWords = {"good", "great", "excellent", "amazing", "wonderful", "positive"};
            String[] negativeWords = {"bad", "terrible", "awful", "horrible", "negative", "poor"};

            long positiveCount = Arrays.stream(positiveWords)
                .mapToLong(word -> Arrays.stream(words).filter(w -> w.equals(word)).count())
                .sum();
            long negativeCount = Arrays.stream(negativeWords)
                .mapToLong(word -> Arrays.stream(words).filter(w -> w.equals(word)).count())
                .sum();

            String sentiment = positiveCount > negativeCount ? "positive" : 
                              negativeCount > positiveCount ? "negative" : "neutral";

            analysisResult.put("sentiment", sentiment);
            analysisResult.put("positiveScore", positiveCount);
            analysisResult.put("negativeScore", negativeCount);

            return Map.of(
                "status", "success",
                "documentId", documentId,
                "analysisType", analysis,
                "aiModel", model,
                "analysis", analysisResult,
                "timestamp", Instant.now(),
                "oracleFeature", "AI Document Analysis"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to analyze document: " + e.getMessage()
            );
        }
    }

    @Tool(name = "generateSummary", description = "Generate AI-powered document summaries")
    public Map<String, Object> generateSummary(
         String tableName,
         String documentColumn,
         String documentId,
         String summaryType,
         Integer maxLength) {

        try {
            String type = summaryType != null ? summaryType : "extractive";
            Integer maxLen = maxLength != null ? maxLength : 200;

            // Get document content
            Map<String, Object> document = jdbcTemplate.queryForMap(
                String.format("SELECT %s as content FROM %s WHERE id = ?", documentColumn, tableName),
                documentId);

            String content = (String) document.get("content");

            // Split into sentences
            String[] sentences = content.split("\\. ");

            Map<String, Object> summaryResult = new HashMap<>();

            if ("extractive".equals(type)) {
                // Simple extractive summarization - take first few sentences
                int sentenceCount = Math.min(3, sentences.length);
                List<String> summarySentences = Arrays.asList(sentences).subList(0, sentenceCount);
                String summary = String.join(". ", summarySentences);

                if (summary.length() > maxLen) {
                    summary = summary.substring(0, maxLen) + "...";
                }

                summaryResult.put("summary", summary);
                summaryResult.put("extractedSentences", sentenceCount);

            } else if ("abstractive".equals(type)) {
                // Simple abstractive summary (keyword-based)
                String[] words = content.toLowerCase().split("[\\s\\p{Punct}]+");
                Map<String, Integer> wordFreq = new HashMap<>();
                for (String word : words) {
                    if (word.length() > 3) {
                        wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                    }
                }

                // Get top keywords for summary
                List<String> topWords = wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());

                String summary = "This document primarily discusses: " + String.join(", ", topWords);
                summaryResult.put("summary", summary);
                summaryResult.put("keyTopics", topWords);
            }

            summaryResult.put("originalLength", content.length());
            summaryResult.put("summaryLength", ((String) summaryResult.get("summary")).length());
            summaryResult.put("compressionRatio", 
                String.format("%.2f%%", 
                    (double) ((String) summaryResult.get("summary")).length() / content.length() * 100));

            return Map.of(
                "status", "success",
                "documentId", documentId,
                "summaryType", type,
                "maxLength", maxLen,
                "summaryResult", summaryResult,
                "timestamp", Instant.now(),
                "oracleFeature", "AI Content Summarization"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to generate summary: " + e.getMessage()
            );
        }
    }

    @Tool(name = "classifyContent", description = "AI-powered content classification")
    public Map<String, Object> classifyContent(
         String tableName,
         String contentColumn,
         String recordId,
         List<String> categories,
         Double confidenceThreshold) {

        try {
            List<String> defaultCategories = categories != null ? categories :
                Arrays.asList("technical", "business", "academic", "personal", "news", "entertainment");
            Double threshold = confidenceThreshold != null ? confidenceThreshold : 0.5;

            // Get content
            Map<String, Object> record = jdbcTemplate.queryForMap(
                String.format("SELECT %s as content FROM %s WHERE id = ?", contentColumn, tableName),
                recordId);

            String content = (String) record.get("content");
            String lowerContent = content.toLowerCase();

            Map<String, Object> classificationResult = new HashMap<>();
            Map<String, Double> categoryScores = new HashMap<>();

            // Simple keyword-based classification
            Map<String, String[]> categoryKeywords = Map.of(
                "technical", new String[]{"technology", "system", "software", "code", "programming", "algorithm"},
                "business", new String[]{"company", "profit", "market", "sales", "revenue", "customer"},
                "academic", new String[]{"research", "study", "analysis", "university", "academic", "paper"},
                "personal", new String[]{"personal", "family", "friend", "life", "experience", "feeling"},
                "news", new String[]{"news", "report", "breaking", "update", "announcement", "headline"},
                "entertainment", new String[]{"movie", "music", "game", "fun", "entertainment", "show"}
            );

            for (String category : defaultCategories) {
                String[] keywords = categoryKeywords.getOrDefault(category, new String[0]);
                long keywordCount = Arrays.stream(keywords)
                    .mapToLong(keyword -> Arrays.stream(lowerContent.split("\\s+"))
                        .filter(word -> word.contains(keyword)).count())
                    .sum();

                double score = Math.min(1.0, (double) keywordCount / content.split("\\s+").length * 100);
                categoryScores.put(category, score);
            }

            // Find best match
            String predictedCategory = categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");

            Double confidence = categoryScores.get(predictedCategory);

            classificationResult.put("predictedCategory", predictedCategory);
            classificationResult.put("confidence", confidence);
            classificationResult.put("allScores", categoryScores);
            classificationResult.put("meetsThreshold", confidence >= threshold);

            return Map.of(
                "status", "success",
                "recordId", recordId,
                "categories", defaultCategories,
                "confidenceThreshold", threshold,
                "classification", classificationResult,
                "timestamp", Instant.now(),
                "oracleFeature", "AI Content Classification"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to classify content: " + e.getMessage()
            );
        }
    }

    // ========== ORACLE-AI INTEGRATION (3 TOOLS) ==========

    @Tool(name = "generateSqlFromNaturalLanguage", description = "Convert natural language queries to Oracle SQL")
    public Map<String, Object> generateSqlFromNaturalLanguage(
         String naturalLanguageQuery,
         List<String> tableContext,
         Boolean dialectOptimization) {

        try {
            Map<String, Object> sqlGeneration = new HashMap<>();

            // Simple natural language to SQL conversion
            String query = naturalLanguageQuery.toLowerCase();
            StringBuilder sql = new StringBuilder();

            // Detect query type
            String queryType = "SELECT"; // Default
            if (query.contains("insert") || query.contains("add") || query.contains("create")) {
                queryType = "INSERT";
            } else if (query.contains("update") || query.contains("modify") || query.contains("change")) {
                queryType = "UPDATE";
            } else if (query.contains("delete") || query.contains("remove")) {
                queryType = "DELETE";
            }

            // Parse table names from context or query
            List<String> tables = tableContext != null ? tableContext : new ArrayList<>();
            if (tables.isEmpty()) {
                // Extract potential table names from query
                String[] words = query.split("\\s+");
                for (int i = 0; i < words.length - 1; i++) {
                    if (words[i].equals("from") || words[i].equals("table") || words[i].equals("into")) {
                        tables.add(words[i + 1].replaceAll("[^a-zA-Z0-9_]", ""));
                    }
                }
            }

            // Generate basic SQL structure
            switch (queryType) {
                case "SELECT":
                    sql.append("SELECT ");

                    // Determine columns
                    if (query.contains("all") || query.contains("everything")) {
                        sql.append("*");
                    } else if (query.contains("count")) {
                        sql.append("COUNT(*)");
                    } else {
                        sql.append("*"); // Default
                    }

                    sql.append(" FROM ");
                    sql.append(!tables.isEmpty() ? tables.get(0) : "table_name");

                    // Add WHERE clause if conditions are mentioned
                    if (query.contains("where") || query.contains("with") || query.contains("having")) {
                        sql.append(" WHERE 1=1"); // Placeholder
                    }

                    // Add ORDER BY if mentioned
                    if (query.contains("order") || query.contains("sort")) {
                        sql.append(" ORDER BY column_name");
                    }

                    // Oracle-specific optimizations
                    if (dialectOptimization != null && dialectOptimization) {
                        if (query.contains("recent") || query.contains("latest")) {
                            sql.append(" FETCH FIRST 10 ROWS ONLY");
                        }
                    }
                    break;

                case "INSERT":
                    sql.append("INSERT INTO ");
                    sql.append(!tables.isEmpty() ? tables.get(0) : "table_name");
                    sql.append(" (column1, column2) VALUES (value1, value2)");
                    break;

                case "UPDATE":
                    sql.append("UPDATE ");
                    sql.append(!tables.isEmpty() ? tables.get(0) : "table_name");
                    sql.append(" SET column1 = value1 WHERE condition");
                    break;

                case "DELETE":
                    sql.append("DELETE FROM ");
                    sql.append(!tables.isEmpty() ? tables.get(0) : "table_name");
                    sql.append(" WHERE condition");
                    break;
            }

            sqlGeneration.put("generatedSql", sql.toString());
            sqlGeneration.put("queryType", queryType);
            sqlGeneration.put("detectedTables", tables);
            sqlGeneration.put("naturalLanguage", naturalLanguageQuery);

            // Add Oracle-specific recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Consider using Oracle optimizer hints for performance");
            recommendations.add("Review execution plan with EXPLAIN PLAN");
            if (dialectOptimization != null && dialectOptimization) {
                recommendations.add("Oracle-specific syntax optimizations applied");
            }
            sqlGeneration.put("recommendations", recommendations);

            return Map.of(
                "status", "success",
                "sqlGeneration", sqlGeneration,
                "timestamp", Instant.now(),
                "oracleFeature", "AI SQL Generation"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to generate SQL: " + e.getMessage()
            );
        }
    }

    @Tool(name = "optimizeQuery", description = "AI-powered Oracle SQL query optimization")
    public Map<String, Object> optimizeQuery(
         String sqlQuery,
         List<String> optimizationGoals,
         Boolean includeExplainPlan) {

        try {
            List<String> goals = optimizationGoals != null ? optimizationGoals :
                Arrays.asList("performance", "readability", "maintainability");

            Map<String, Object> optimization = new HashMap<>();

            // Analyze original query
            String originalQuery = sqlQuery.trim();
            optimization.put("originalQuery", originalQuery);

            // Generate optimized query suggestions
            StringBuilder optimizedQuery = new StringBuilder(originalQuery);
            List<String> optimizations = new ArrayList<>();

            // Oracle-specific optimizations
            if (originalQuery.toUpperCase().contains("SELECT *")) {
                optimizations.add("Consider selecting specific columns instead of *");
            }

            if (!originalQuery.toUpperCase().contains("WHERE")) {
                optimizations.add("Add WHERE clause to limit rows");
            }

            if (originalQuery.toUpperCase().contains("ORDER BY") && 
                !originalQuery.toUpperCase().contains("INDEX")) {
                optimizations.add("Consider creating index on ORDER BY columns");
            }

            // Add Oracle hints for performance
            if (goals.contains("performance")) {
                if (originalQuery.toUpperCase().startsWith("SELECT") && 
                    !originalQuery.contains("/*+")) {
                    optimizedQuery = new StringBuilder(originalQuery.replaceFirst("(?i)SELECT", 
                        "SELECT /*+ FIRST_ROWS */"));
                    optimizations.add("Added FIRST_ROWS hint for faster initial response");
                }
            }

            // Check for potential issues
            List<String> issues = new ArrayList<>();
            if (originalQuery.contains("!=")) {
                issues.add("Consider using NOT IN or NOT EXISTS instead of !=");
            }
            if (originalQuery.toUpperCase().contains("OR")) {
                issues.add("OR conditions may prevent index usage");
            }

            optimization.put("optimizedQuery", optimizedQuery.toString());
            optimization.put("optimizations", optimizations);
            optimization.put("potentialIssues", issues);

            // Get execution plan if requested
            if (includeExplainPlan != null && includeExplainPlan) {
                try {
                    jdbcTemplate.execute("EXPLAIN PLAN FOR " + originalQuery);

                    List<Map<String, Object>> plan = jdbcTemplate.queryForList(
                        "SELECT operation, options, object_name, cost FROM plan_table ORDER BY id");
                    optimization.put("executionPlan", plan);

                    Object totalCost = jdbcTemplate.queryForObject(
                        "SELECT SUM(cost) FROM plan_table WHERE cost IS NOT NULL", Object.class);
                    optimization.put("estimatedCost", totalCost);
                } catch (Exception planException) {
                    optimization.put("executionPlan", "Could not generate execution plan");
                }
            }

            return Map.of(
                "status", "success",
                "optimization", optimization,
                "optimizationGoals", goals,
                "timestamp", Instant.now(),
                "oracleFeature", "AI Query Optimization"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to optimize query: " + e.getMessage()
            );
        }
    }

    @Tool(name = "recommendSchemaDesign", description = "AI-powered Oracle schema design recommendations")
    public Map<String, Object> recommendSchemaDesign(
         String businessRequirements,
         String dataVolume,
         List<String> performanceGoals,
         Boolean includePartitioning) {

        try {
            Map<String, Object> schemaDesign = new HashMap<>();

            // Analyze business requirements
            String requirements = businessRequirements.toLowerCase();
            List<String> entities = new ArrayList<>();
            List<String> relationships = new ArrayList<>();

            // Extract potential entities
            String[] words = requirements.split("[\\s\\p{Punct}]+");
            for (String word : words) {
                if (word.length() > 3 && 
                    (requirements.contains(word + "s") || requirements.contains("list of " + word))) {
                    entities.add(word.toUpperCase());
                }
            }

            // Generate table recommendations
            List<Map<String, Object>> recommendedTables = new ArrayList<>();
            for (String entity : entities) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", entity + "_TABLE");

                // Recommend basic columns
                List<String> columns = new ArrayList<>();
                columns.add(entity + "_ID NUMBER PRIMARY KEY");
                columns.add(entity + "_NAME VARCHAR2(100) NOT NULL");
                columns.add("CREATED_DATE DATE DEFAULT SYSDATE");
                columns.add("LAST_MODIFIED DATE DEFAULT SYSDATE");
                table.put("columns", columns);

                // Oracle-specific recommendations
                List<String> oracleFeatures = new ArrayList<>();
                if (dataVolume != null && dataVolume.toLowerCase().contains("large")) {
                    oracleFeatures.add("Consider partitioning by date/range");
                    oracleFeatures.add("Use COMPRESS FOR OLTP storage");
                }
                oracleFeatures.add("Create B-tree index on " + entity + "_NAME");
                oracleFeatures.add("Enable row movement for partitioned tables");
                table.put("oracleFeatures", oracleFeatures);

                recommendedTables.add(table);
            }

            schemaDesign.put("recommendedTables", recommendedTables);
            schemaDesign.put("detectedEntities", entities);

            // Performance recommendations
            List<String> perfRecommendations = new ArrayList<>();
            perfRecommendations.add("Use appropriate data types (NUMBER vs VARCHAR2)");
            perfRecommendations.add("Create foreign key constraints for referential integrity");
            perfRecommendations.add("Consider using Oracle sequences for primary keys");

            if (includePartitioning != null && includePartitioning) {
                perfRecommendations.add("Implement range or hash partitioning for large tables");
                perfRecommendations.add("Use local indexes on partitioned tables");
            }

            schemaDesign.put("performanceRecommendations", perfRecommendations);

            // Oracle-specific design patterns
            List<String> designPatterns = new ArrayList<>();
            designPatterns.add("Use Oracle's built-in audit columns (CREATED_BY, CREATED_DATE)");
            designPatterns.add("Implement soft delete with STATUS column");
            designPatterns.add("Consider using Oracle's flashback features");
            designPatterns.add("Use database triggers for complex business rules");

            schemaDesign.put("oracleDesignPatterns", designPatterns);

            return Map.of(
                "status", "success",
                "schemaDesign", schemaDesign,
                "businessRequirements", businessRequirements,
                "dataVolume", dataVolume != null ? dataVolume : "Not specified",
                "timestamp", Instant.now(),
                "oracleFeature", "AI Schema Design"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to recommend schema design: " + e.getMessage()
            );
        }
    }
}


