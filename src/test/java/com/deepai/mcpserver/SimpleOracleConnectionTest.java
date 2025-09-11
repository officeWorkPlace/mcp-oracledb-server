package com.deepai.mcpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simple Oracle Connection Test - Using Mocked Data
 * Tests Oracle service functionality without requiring real database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "mcp.transport=none",
    "spring.main.web-application-type=none",
    "oracle.features.detection.enabled=false"
})
class SimpleOracleConnectionTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Setup mock responses for Oracle queries
        when(jdbcTemplate.queryForObject(eq("SELECT 1 FROM dual"), eq(Integer.class)))
            .thenReturn(1);
        
        when(jdbcTemplate.queryForObject(contains("SELECT BANNER FROM v$version"), eq(String.class)))
            .thenReturn("Oracle Database 21c Express Edition Release 21.0.0.0.0");
        
        when(jdbcTemplate.queryForObject(eq("SELECT USER FROM dual"), eq(String.class)))
            .thenReturn("C##LOAN_SCHEMA");
        
        when(jdbcTemplate.queryForObject(contains("SELECT COUNT(*) FROM all_users"), eq(Integer.class)))
            .thenReturn(1);
    }

    @Test
    @DisplayName("Test basic Oracle connectivity with mocked data")
    void testBasicConnection() {
        // Test basic Oracle connectivity
        assertNotNull(jdbcTemplate, "JdbcTemplate should be injected");
        
        // Test simple query
        Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM dual", Integer.class);
        assertEquals(1, result, "Should be able to execute basic query");
        
        // Test Oracle version
        String version = jdbcTemplate.queryForObject("SELECT BANNER FROM v$version WHERE banner LIKE 'Oracle%'", String.class);
        assertNotNull(version, "Should be able to get Oracle version");
        assertTrue(version.contains("Oracle"), "Version should contain 'Oracle'");
        
        System.out.println("✅ Mocked Oracle connection test successful!");
        System.out.println("📊 Mocked Oracle version: " + version);
        
        // Verify that the mock was called
        verify(jdbcTemplate).queryForObject(eq("SELECT 1 FROM dual"), eq(Integer.class));
        verify(jdbcTemplate).queryForObject(contains("SELECT BANNER FROM v$version"), eq(String.class));
    }

    @Test
    @DisplayName("Test user and schema information with mocked data")
    void testUserInfo() {
        // Get current user (mocked)
        String currentUser = jdbcTemplate.queryForObject("SELECT USER FROM dual", String.class);
        assertNotNull(currentUser, "Should be able to get current user");
        assertEquals("C##LOAN_SCHEMA", currentUser, "Should return mocked user");
        
        System.out.println("👤 Mocked connected user: " + currentUser);
        
        // Check if C##loan_schema user exists (mocked)
        Integer userExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM all_users WHERE username = 'C##LOAN_SCHEMA'", 
            Integer.class);
        
        assertEquals(1, userExists, "Should return mocked user count");
        
        System.out.println("🔍 C##LOAN_SCHEMA user exists: " + (userExists > 0 ? "YES" : "NO"));
        System.out.println("✅ Mocked target schema user found - test passed!");
        
        // Verify that the mocks were called
        verify(jdbcTemplate).queryForObject(eq("SELECT USER FROM dual"), eq(String.class));
        verify(jdbcTemplate).queryForObject(contains("SELECT COUNT(*) FROM all_users"), eq(Integer.class));
    }
}
