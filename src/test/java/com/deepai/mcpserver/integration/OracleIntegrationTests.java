package com.deepai.mcpserver.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Oracle MCP Server
 * Tests actual Oracle database connections and operations
 * Requires Oracle 19c database running on localhost:1521/ORCL
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:oracle:thin:@localhost:1521/ORCL",
    "spring.datasource.username=C##deepai",
    "spring.datasource.password=admin",
    "logging.level.com.deepai.mcpserver=DEBUG"
})
@DisplayName("Oracle Database Integration Tests")
class OracleIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/oracle";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    // ========== CORE SERVICE INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should connect to Oracle database and list databases")
    void shouldConnectAndListDatabases() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/core/databases?includePdbs=true&includeStatus=true", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("databases")).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("Should get database statistics")
    void shouldGetDatabaseStatistics() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/core/databases/stats", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("statistics")).isNotNull();
    }

    @Test
    @DisplayName("Should list schemas excluding system schemas")
    void shouldListUserSchemas() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/core/schemas?includeSystemSchemas=false", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("schemas")).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("Should execute a simple query")
    void shouldExecuteSimpleQuery() {
        String queryUrl = baseUrl + "/core/query?sqlQuery=SELECT 1 as TEST_VALUE FROM DUAL&maxRows=1";
        
        ResponseEntity<Map> response = restTemplate.postForEntity(queryUrl, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("results")).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("Should create, use, and drop test schema")
    void shouldManageTestSchema() {
        String testSchema = "C##TESTSCHEMA" + System.currentTimeMillis();
        
        try {
            // Create schema
            String createUrl = baseUrl + "/core/schemas?" +
                "schemaName=" + testSchema + 
                "&password=testpass123&tablespace=USERS&quota=10M";
            
            ResponseEntity<Map> createResponse = restTemplate.postForEntity(createUrl, null, Map.class);
            
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, Object> createBody = createResponse.getBody();
            
            // Only proceed if schema creation was successful
            if ("success".equals(createBody.get("status"))) {
                // Get schema info
                ResponseEntity<Map> infoResponse = restTemplate.getForEntity(
                    baseUrl + "/core/schemas/" + testSchema + "?includeObjects=true", 
                    Map.class
                );
                
                assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(infoResponse.getBody().get("status")).isEqualTo("success");
                
                // Drop schema
                ResponseEntity<Map> dropResponse = restTemplate.exchange(
                    baseUrl + "/core/schemas/" + testSchema + "?cascade=true",
                    HttpMethod.DELETE, null, Map.class
                );
                
                assertThat(dropResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        } catch (Exception e) {
            // Cleanup in case of test failure
            try {
                restTemplate.exchange(
                    baseUrl + "/core/schemas/" + testSchema + "?cascade=true",
                    HttpMethod.DELETE, null, Map.class
                );
            } catch (Exception ignored) {
                // Ignore cleanup errors
            }
        }
    }

    // ========== AI SERVICE INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should check AI service capabilities")
    void shouldCheckAICapabilities() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/ai/capabilities", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("capabilities")).isNotNull();
    }

    @Test
    @DisplayName("Should generate SQL from natural language")
    void shouldGenerateSqlFromNaturalLanguage() {
        String requestUrl = baseUrl + "/ai/generate-sql?" +
            "naturalLanguageQuery=select all employees from HR schema&dialectOptimization=true";
        
        HttpEntity<List<String>> request = new HttpEntity<>(
            List.of("EMPLOYEES", "HR"), 
            headers
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("sqlGeneration")).isNotNull();
    }

    @Test
    @DisplayName("Should optimize a query")
    void shouldOptimizeQuery() {
        String requestUrl = baseUrl + "/ai/optimize-query?" +
            "sqlQuery=SELECT * FROM ALL_TABLES WHERE OWNER = 'HR'&includeExplainPlan=false";
        
        HttpEntity<List<String>> request = new HttpEntity<>(
            List.of("performance", "readability"), 
            headers
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
    }

    // ========== ANALYTICS SERVICE INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should check analytics service capabilities")
    void shouldCheckAnalyticsCapabilities() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/analytics/capabilities", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("capabilities")).isNotNull();
    }

    // ========== PERFORMANCE SERVICE INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should check performance service capabilities")
    void shouldCheckPerformanceCapabilities() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/performance/capabilities", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("capabilities")).isNotNull();
    }

    @Test
    @DisplayName("Should get memory status")
    void shouldGetMemoryStatus() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/performance/memory/SGA_STATUS", 
            null, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
    }

    // ========== SECURITY SERVICE INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should check security service capabilities")
    void shouldCheckSecurityCapabilities() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/security/capabilities", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("capabilities")).isNotNull();
    }

    @Test
    @DisplayName("Should get security status")
    void shouldGetSecurityStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/security/status", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // ========== HEALTH CHECK TESTS ==========

    @Test
    @DisplayName("Should perform health checks for all services")
    void shouldPerformHealthChecks() {
        String[] services = {"core", "ai", "analytics", "performance", "security"};
        
        for (String service : services) {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/" + service + "/health", 
                Map.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("UP");
        }
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle invalid query gracefully")
    void shouldHandleInvalidQuery() {
        String queryUrl = baseUrl + "/core/query?sqlQuery=INVALID SQL SYNTAX HERE&maxRows=1";
        
        ResponseEntity<Map> response = restTemplate.postForEntity(queryUrl, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isNotNull();
    }

    @Test
    @DisplayName("Should handle non-existent endpoints gracefully")
    void shouldHandleNonExistentEndpoints() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/nonexistent/endpoint", 
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== PERFORMANCE TESTS ==========

    @Test
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws InterruptedException {
        int concurrentRequests = 5;
        Thread[] threads = new Thread[concurrentRequests];
        boolean[] results = new boolean[concurrentRequests];

        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    ResponseEntity<Map> response = restTemplate.getForEntity(
                        baseUrl + "/core/databases/stats", 
                        Map.class
                    );
                    results[index] = response.getStatusCode() == HttpStatus.OK;
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout
        }

        // Verify all requests succeeded
        for (boolean result : results) {
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("Should respond within acceptable time limits")
    void shouldRespondWithinTimeLimits() {
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/core/databases", 
            Map.class
        );
        
        long responseTime = System.currentTimeMillis() - startTime;

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseTime).isLessThan(5000); // Response should be under 5 seconds
    }

    // ========== DATA VALIDATION TESTS ==========

    @Test
    @DisplayName("Should validate response structure for database listing")
    void shouldValidateResponseStructure() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/core/databases", 
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        
        // Validate required fields
        assertThat(body).containsKeys("status", "databases");
        assertThat(body.get("status")).isIn("success", "error");
        
        if ("success".equals(body.get("status"))) {
            assertThat(body.get("databases")).isInstanceOf(List.class);
            assertThat(body).containsKey("count");
        }
    }
}
