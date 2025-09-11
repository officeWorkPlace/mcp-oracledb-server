package com.deepai.mcpserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "sql.script.execute", havingValue = "true")
public class SqlScriptExecutor implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        String scriptPath = System.getProperty("sql.script.path");
        if (scriptPath != null && !scriptPath.isEmpty()) {
            executeScript(scriptPath);
        }
    }

    public void executeScript(String scriptPath) {
        try {
            log.info("Executing SQL script: {}", scriptPath);
            
            // Read the script file
            String content = new String(Files.readAllBytes(Paths.get(scriptPath)));
            
            // Split by semicolon and slash (Oracle SQL*Plus script delimiters)
            String[] statements = content.split("(?<=;)\\s*\\n|(?<=/)\\s*\\n");
            
            int successCount = 0;
            int errorCount = 0;
            
            for (String statement : statements) {
                statement = statement.trim();
                
                // Skip empty statements, comments, and Oracle-specific commands
                if (statement.isEmpty() || 
                    statement.startsWith("--") || 
                    statement.startsWith("REM") ||
                    statement.startsWith("PROMPT") ||
                    statement.startsWith("SET") ||
                    statement.startsWith("ALTER SESSION") ||
                    statement.equals("/") ||
                    statement.startsWith("BEGIN") ||
                    statement.startsWith("EXCEPTION")) {
                    continue;
                }
                
                // Handle PL/SQL blocks
                if (statement.contains("BEGIN") && statement.contains("END")) {
                    try {
                        jdbcTemplate.execute(statement);
                        successCount++;
                        log.debug("Executed PL/SQL block successfully");
                    } catch (Exception e) {
                        errorCount++;
                        log.warn("Failed to execute PL/SQL block: {}", e.getMessage());
                    }
                    continue;
                }
                
                // Handle regular SQL statements
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1);
                }
                
                try {
                    if (statement.toUpperCase().startsWith("CREATE") ||
                        statement.toUpperCase().startsWith("DROP") ||
                        statement.toUpperCase().startsWith("ALTER")) {
                        jdbcTemplate.execute(statement);
                    } else if (statement.toUpperCase().startsWith("INSERT")) {
                        int rows = jdbcTemplate.update(statement);
                        log.debug("Inserted {} rows", rows);
                    } else {
                        jdbcTemplate.execute(statement);
                    }
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.warn("Failed to execute statement: {} - Error: {}", 
                            statement.substring(0, Math.min(100, statement.length())), 
                            e.getMessage());
                }
            }
            
            log.info("Script execution completed. Success: {}, Errors: {}", successCount, errorCount);
            
        } catch (Exception e) {
            log.error("Error executing SQL script", e);
            throw new RuntimeException("Failed to execute SQL script", e);
        }
    }
}
