package com.deepai.mcpserver.config;

import com.deepai.mcpserver.service.OracleServiceClient;
import com.deepai.mcpserver.service.OracleVisualizationService;
import com.deepai.mcpserver.service.OracleAdvancedAnalyticsService;
import com.deepai.mcpserver.service.OracleAIService;
import com.deepai.mcpserver.service.OracleEnterpriseSecurityService;
import com.deepai.mcpserver.service.OracleEnterprisePerformanceService;
import com.deepai.mcpserver.vtools.FinancialVisualizationMCPTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle MCP Configuration - Following MongoDB MCP Server Pattern
 * 
 * Registers all Oracle operation tools for AI agent consumption using
 * the exact same pattern as the MongoDB MCP server reference:
 * https://github.com/officeWorkPlace/spring-boot-ai-mongodb-mcp-server
 * 
 * @author Oracle MCP Server Team
 * @version 7.0.0 - MongoDB Pattern Implementation
 */
@Configuration
@Profile({"mcp-run", "default"})
public class SimpleMcpConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMcpConfiguration.class);

    @Value("${mcp.tools.exposure:all}")
    private String toolsExposure;

    /**
     * Register Oracle service tools for AI agent consumption.
     * Following the exact MongoDB MCP server pattern.
     * 
     * Exposure level controlled by mcp.tools.exposure property:
     * - "core": Core Oracle operations only (25 tools)
     * - "all": All Oracle tools including Enterprise features (73+ tools)
     * 
     * Tool count by exposure level:
     * - Core: 25 database, user, and table operations
     * - All: 73+ comprehensive tools across five service categories:
     *   * Core Operations: 25 database, user, table operations
     *   * Advanced Analytics: 20 aggregation, window functions, hierarchical queries
     *   * AI-Powered Tools: 8 vector search, document analysis, SQL generation
     *   * Enterprise Security: 10 VPD, TDE, Database Vault, auditing
     *   * Enterprise Performance: 10 AWR, query optimization, resource management
     */
    @Bean
    public ToolCallbackProvider oracleTools(OracleServiceClient oracleServiceClient,
                                           OracleAdvancedAnalyticsService oracleAdvancedAnalyticsService,
                                           OracleAIService oracleAIService,
                                           OracleEnterpriseSecurityService oracleEnterpriseSecurityService,
                                           OracleEnterprisePerformanceService oracleEnterprisePerformanceService,
                                           OracleVisualizationService oracleVisualizationService,
                                           FinancialVisualizationMCPTools financialVisualizationMCPTools) {
        
        logger.info("🚀 Configuring Oracle MCP tools with exposure level: {}", toolsExposure);
        
        if ("core".equalsIgnoreCase(toolsExposure)) {
            logger.info("📋 Registering CORE Oracle MCP tools (25 core operations)");
            return MethodToolCallbackProvider.builder()
                    .toolObjects(oracleServiceClient)
                    .build();
        } else {
            logger.info("🚀 Registering ALL 73+ Oracle MCP tools (Core + Analytics + AI + Enterprise)");
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🎉 Oracle MCP Server - COMPLETE Tool Registration");
            System.out.println("=".repeat(80));
            System.out.println("📋 Core Operations: 25 tools (OracleServiceClient)");
            System.out.println("📈 Advanced Analytics: 20 tools (OracleAdvancedAnalyticsService)");
            System.out.println("🤖 AI-Powered Tools: 8 tools (OracleAIService)");
            System.out.println("🔒 Enterprise Security: 10 tools (OracleEnterpriseSecurityService)");
            System.out.println("⚡ Enterprise Performance: 10 tools (OracleEnterprisePerformanceService)");
            System.out.println("🏆 TOTAL: 73+ Oracle MCP Tools for Claude Desktop!");
            System.out.println("=".repeat(80) + "\n");
            
            return MethodToolCallbackProvider.builder()
                    .toolObjects(oracleServiceClient, 
                               oracleAdvancedAnalyticsService, 
                               oracleAIService,
                               oracleEnterpriseSecurityService,
                               oracleEnterprisePerformanceService,
                               oracleVisualizationService,
                               financialVisualizationMCPTools)
                    .build();
        }
    }
}
