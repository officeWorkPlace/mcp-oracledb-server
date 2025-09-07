package com.deepai.mcpserver.controller;

import com.deepai.mcpserver.config.TestSecurityConfig;
import com.deepai.mcpserver.service.OracleServiceClient;
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
 * Comprehensive unit tests for OracleServiceController
 * Tests all core Oracle database service endpoints with correct method signatures
 */
@WebMvcTest(OracleServiceController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Oracle Core Service Controller Tests")
@SuppressWarnings("deprecation")
class OracleServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OracleServiceClient oracleServiceClient;

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

    // ========== DATABASE MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should list databases successfully")
    void shouldListDatabases() throws Exception {
        when(oracleServiceClient.listDatabases(true, true))
            .thenReturn(Map.of(
                "status", "success",
                "databases", List.of(
                    Map.of("name", "ORCL", "type", "CDB", "status", "OPEN"),
                    Map.of("name", "ORCLPDB1", "type", "PDB", "status", "OPEN")
                ),
                "count", 2
            ));

        mockMvc.perform(get("/api/oracle/core/databases")
                .param("includePdbs", "true")
                .param("includeStatus", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.databases").isArray())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("Should create database successfully")
    void shouldCreateDatabase() throws Exception {
        when(oracleServiceClient.createDatabase("TESTDB", "traditional", "100M"))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Database TESTDB created successfully",
                "databaseName", "TESTDB"
            ));

        mockMvc.perform(post("/api/oracle/core/databases")
                .param("databaseName", "TESTDB")
                .param("createType", "traditional")
                .param("datafileSize", "100M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should drop database successfully")
    void shouldDropDatabase() throws Exception {
        when(oracleServiceClient.dropDatabase("TESTDB", false))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Database TESTDB dropped successfully"
            ));

        mockMvc.perform(delete("/api/oracle/core/databases/TESTDB")
                .param("force", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should get database statistics successfully")
    void shouldGetDatabaseStats() throws Exception {
        when(oracleServiceClient.getDatabaseStats(false))
            .thenReturn(Map.of(
                "status", "success",
                "statistics", Map.of(
                    "table_count", 150,
                    "user_count", 25,
                    "total_size_mb", 2048.5
                )
            ));

        mockMvc.perform(get("/api/oracle/core/databases/stats")
                .param("includeAwrData", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statistics.table_count").value(150));
    }

    @Test
    @DisplayName("Should get database size information successfully")
    void shouldGetDatabaseSize() throws Exception {
        when(oracleServiceClient.getDatabaseSize(true))
            .thenReturn(Map.of(
                "status", "success",
                "sizeInfo", Map.of(
                    "total_size_gb", 10.5,
                    "datafile_count", 8
                )
            ));

        mockMvc.perform(get("/api/oracle/core/databases/size")
                .param("includeTablespaces", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.sizeInfo.total_size_gb").value(10.5));
    }

    @Test
    @DisplayName("Should perform database backup successfully")
    void shouldPerformBackup() throws Exception {
        when(oracleServiceClient.performBackup("full", "/backup"))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Full backup completed successfully",
                "backupLocation", "/backup"
            ));

        mockMvc.perform(post("/api/oracle/core/databases/backup")
                .param("backupType", "full")
                .param("backupLocation", "/backup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should manage PDB operations successfully")
    void shouldManagePdb() throws Exception {
        Map<String, Object> parameters = Map.of("sourceDb", "SEED");
        
        when(oracleServiceClient.managePdb("CREATE", "TESTPDB", parameters))
            .thenReturn(Map.of(
                "status", "success",
                "message", "PDB TESTPDB created successfully",
                "operation", "CREATE",
                "pdbName", "TESTPDB"
            ));

        mockMvc.perform(post("/api/oracle/core/databases/pdb/CREATE")
                .param("pdbName", "TESTPDB")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parameters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== SCHEMA/USER MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should list schemas successfully")
    void shouldListSchemas() throws Exception {
        when(oracleServiceClient.listSchemas(false))
            .thenReturn(Map.of(
                "status", "success",
                "schemas", List.of(
                    Map.of("schema_name", "C##DEEPAI", "account_status", "OPEN"),
                    Map.of("schema_name", "HR", "account_status", "OPEN")
                ),
                "count", 2
            ));

        mockMvc.perform(get("/api/oracle/core/schemas")
                .param("includeSystemSchemas", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("Should create schema successfully")
    void shouldCreateSchema() throws Exception {
        when(oracleServiceClient.createSchema("TESTSCHEMA", "password", "USERS", "100M"))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Schema TESTSCHEMA created successfully",
                "schemaName", "TESTSCHEMA"
            ));

        mockMvc.perform(post("/api/oracle/core/schemas")
                .param("schemaName", "TESTSCHEMA")
                .param("password", "password")
                .param("tablespace", "USERS")
                .param("quota", "100M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() throws Exception {
        List<String> privileges = List.of("CONNECT", "RESOURCE");
        
        when(oracleServiceClient.createUser("TESTUSER", "password", "USERS", privileges))
            .thenReturn(Map.of(
                "status", "success",
                "message", "User TESTUSER created successfully",
                "username", "TESTUSER"
            ));

        mockMvc.perform(post("/api/oracle/core/users")
                .param("username", "TESTUSER")
                .param("password", "password")
                .param("tablespace", "USERS")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privileges)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should grant privileges successfully")
    void shouldGrantPrivileges() throws Exception {
        List<String> privileges = List.of("SELECT", "INSERT", "UPDATE");
        
        when(oracleServiceClient.grantPrivileges("TESTUSER", "OBJECT", privileges, "EMPLOYEES"))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Privileges granted to TESTUSER successfully",
                "privilegesGranted", privileges.size()
            ));

        mockMvc.perform(post("/api/oracle/core/users/TESTUSER/privileges/grant")
                .param("privilegeType", "OBJECT")
                .param("objectName", "EMPLOYEES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privileges)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should revoke privileges successfully")
    void shouldRevokePrivileges() throws Exception {
        List<String> privileges = List.of("DELETE");
        
        when(oracleServiceClient.revokePrivileges("TESTUSER", "OBJECT", privileges, "EMPLOYEES"))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Privileges revoked from TESTUSER successfully",
                "privilegesRevoked", privileges.size()
            ));

        mockMvc.perform(post("/api/oracle/core/users/TESTUSER/privileges/revoke")
                .param("privilegeType", "OBJECT")
                .param("objectName", "EMPLOYEES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privileges)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should manage user sessions successfully")
    void shouldManageUserSessions() throws Exception {
        when(oracleServiceClient.manageUserSessions("LIST", "TESTUSER", null))
            .thenReturn(Map.of(
                "status", "success",
                "sessions", List.of(
                    Map.of("sid", 123, "serial#", 456, "username", "TESTUSER", "status", "ACTIVE")
                ),
                "sessionCount", 1
            ));

        mockMvc.perform(post("/api/oracle/core/users/sessions/LIST")
                .param("username", "TESTUSER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.sessionCount").value(1));
    }

    @Test
    @DisplayName("Should kill user session successfully")
    void shouldKillUserSession() throws Exception {
        when(oracleServiceClient.manageUserSessions("KILL", null, 123))
            .thenReturn(Map.of(
                "status", "success",
                "message", "Session 123 killed successfully"
            ));

        mockMvc.perform(post("/api/oracle/core/users/sessions/KILL")
                .param("sessionId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle database operation errors gracefully")
    void shouldHandleDatabaseErrors() throws Exception {
        when(oracleServiceClient.createDatabase("EXISTING_DB", null, null))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Database EXISTING_DB already exists"
            ));

        mockMvc.perform(post("/api/oracle/core/databases")
                .param("databaseName", "EXISTING_DB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Database EXISTING_DB already exists"));
    }

    @Test
    @DisplayName("Should handle missing required parameters")
    void shouldHandleMissingParameters() throws Exception {
        mockMvc.perform(post("/api/oracle/core/databases"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle schema creation errors")
    void shouldHandleSchemaErrors() throws Exception {
        when(oracleServiceClient.createSchema("INVALID_NAME", "weak", null, null))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Invalid schema name or weak password"
            ));

        mockMvc.perform(post("/api/oracle/core/schemas")
                .param("schemaName", "INVALID_NAME")
                .param("password", "weak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("Should handle privilege management errors")
    void shouldHandlePrivilegeErrors() throws Exception {
        List<String> privileges = List.of("INVALID_PRIVILEGE");
        
        when(oracleServiceClient.grantPrivileges("NONEXISTENT_USER", "SYSTEM", privileges, null))
            .thenReturn(Map.of(
                "status", "error",
                "message", "User NONEXISTENT_USER does not exist"
            ));

        mockMvc.perform(post("/api/oracle/core/users/NONEXISTENT_USER/privileges/grant")
                .param("privilegeType", "SYSTEM")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privileges)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
