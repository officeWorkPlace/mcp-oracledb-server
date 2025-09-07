package com.deepai.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Tool scanning will be configured via Spring configuration

/**
 * MCP Oracle DB Server - Production-Ready Implementation
 * 
 * Features:
 * - 55+ Oracle-specific tools (Enhanced Edition)
 * - 75+ tools (Enterprise Edition)
 * - Spring Boot 3.4.5 + Spring AI 1.0.1
 * - Multi-version Oracle support (11g-23c)
 * - Dynamic @Tool discovery with configurable exposure
 * - Stdio MCP transport as default with REST optional
 * 
 * Target Repository: officeWorkPlace/mcp-oracledb-server
 * Baseline Reference: officeWorkPlace/spring-boot-ai-mongodb-mcp-server (39 tools)
 * Integration Target: officeWorkPlace/global-mcp-client
 * 
 * Tool Architecture:
 * - Core Oracle Operations: 25 tools (+3 vs MongoDB's 22)
 * - Advanced Analytics: 20 tools (+8 vs MongoDB's 12) 
 * - AI-Powered Operations: 10 tools (+3 vs MongoDB's 7)
 * - Enterprise Security: 10 tools (Oracle-exclusive)
 * - Enterprise Performance: 10 tools (Oracle-exclusive)
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@SpringBootApplication
// @ToolScan - Using alternative configuration approach
public class McpOracleDbServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpOracleDbServerApplication.class, args);
    }
}
