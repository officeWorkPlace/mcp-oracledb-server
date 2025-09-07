package com.deepai.mcpserver;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for Oracle MCP Server
 * 
 * This suite runs all unit tests, integration tests, and service tests
 * to ensure 100% code coverage across all components.
 * 
 * Test Categories:
 * - Unit Tests: Individual component testing with mocks
 * - Integration Tests: Real database connectivity testing
 * - Controller Tests: REST API endpoint testing
 * - Service Tests: Business logic testing
 * - Configuration Tests: Application configuration validation
 */
@Suite
@SuiteDisplayName("Oracle MCP Server - Complete Test Suite")
@SelectPackages({
    "com.deepai.mcpserver.controller",
    "com.deepai.mcpserver.service", 
    "com.deepai.mcpserver.integration",
    "com.deepai.mcpserver.config"
})
@IncludeClassNamePatterns({
    ".*Test.*",
    ".*Tests.*",
    ".*IT.*"
})
public class TestSuite {
    
    // Test suite configuration class
    // All tests are discovered and executed based on annotations above
    
    /*
     * Coverage Goals:
     * - Unit Tests: 100% line coverage for all service methods
     * - Controller Tests: 100% endpoint coverage with success/error scenarios
     * - Integration Tests: Real Oracle DB operations validation
     * - End-to-End Tests: Full API workflow testing
     * 
     * Test Execution Order:
     * 1. Unit Tests (fastest, no external dependencies)
     * 2. Integration Tests (Oracle DB connectivity required)
     * 3. End-to-End Tests (full stack validation)
     * 
     * Prerequisites for Integration Tests:
     * - Oracle 19c Database running on localhost:1521
     * - Service name: ORCL
     * - User: C##deepai with password: admin
     * - Required privileges: CREATE SESSION, CREATE TABLE, CREATE USER, etc.
     */
}
