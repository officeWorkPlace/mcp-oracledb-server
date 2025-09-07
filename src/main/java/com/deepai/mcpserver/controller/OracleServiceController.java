package com.deepai.mcpserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.deepai.mcpserver.service.OracleServiceClient;
import com.deepai.mcpserver.response.StandardResponse;
import com.deepai.mcpserver.response.ResponseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Oracle Core Service Operations
 * Exposes 25 Oracle database core tools via REST API
 * 
 * Categories:
 * - Database Management (7 tools)
 * - Schema/User Management (10 tools)
 * - Table Operations (8 tools)
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@RestController
@RequestMapping("/api/oracle/core")
@CrossOrigin(origins = "*")
public class OracleServiceController {

    private static final Logger logger = LoggerFactory.getLogger(OracleServiceController.class);
    
    private final OracleServiceClient oracleServiceClient;
    private final ResponseConverter responseConverter;

    @Autowired
    public OracleServiceController(OracleServiceClient oracleServiceClient, ResponseConverter responseConverter) {
        this.oracleServiceClient = oracleServiceClient;
        this.responseConverter = responseConverter;
    }

    // ========== DATABASE MANAGEMENT ENDPOINTS (7 tools) ==========

    /**
     * List databases (CDB and PDBs)
     */
    @GetMapping("/databases")
    public ResponseEntity<Map<String, Object>> listDatabases(
            @RequestParam(required = false) Boolean includePdbs,
            @RequestParam(required = false) Boolean includeStatus) {
        
        Map<String, Object> result = oracleServiceClient.listDatabases(includePdbs, includeStatus);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a new database or PDB
     */
    @PostMapping("/databases")
    public ResponseEntity<Map<String, Object>> createDatabase(
            @RequestParam String databaseName,
            @RequestParam(required = false) String createType,
            @RequestParam(required = false) String datafileSize) {
        
        Map<String, Object> result = oracleServiceClient.createDatabase(databaseName, createType, datafileSize);
        return ResponseEntity.ok(result);
    }

    /**
     * Drop a database
     */
    @DeleteMapping("/databases/{databaseName}")
    public ResponseEntity<Map<String, Object>> dropDatabase(
            @PathVariable String databaseName,
            @RequestParam(required = false) Boolean force) {
        
        Map<String, Object> result = oracleServiceClient.dropDatabase(databaseName, force);
        return ResponseEntity.ok(result);
    }

    /**
     * Get database statistics
     */
    @GetMapping("/databases/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats(
            @RequestParam(required = false) Boolean includeAwrData) {
        
        Map<String, Object> result = oracleServiceClient.getDatabaseStats(includeAwrData);
        return ResponseEntity.ok(result);
    }

    /**
     * Get database size information
     */
    @GetMapping("/databases/size")
    public ResponseEntity<Map<String, Object>> getDatabaseSize(
            @RequestParam(required = false) Boolean includeTablespaces) {
        
        Map<String, Object> result = oracleServiceClient.getDatabaseSize(includeTablespaces);
        return ResponseEntity.ok(result);
    }

    /**
     * Perform database backup
     */
    @PostMapping("/databases/backup")
    public ResponseEntity<Map<String, Object>> performBackup(
            @RequestParam(required = false) String backupType,
            @RequestParam(required = false) String backupLocation) {
        
        Map<String, Object> result = oracleServiceClient.performBackup(backupType, backupLocation);
        return ResponseEntity.ok(result);
    }

    /**
     * Manage PDB operations
     */
    @PostMapping("/databases/pdb/{operation}")
    public ResponseEntity<Map<String, Object>> managePdb(
            @PathVariable String operation,
            @RequestParam String pdbName,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        Map<String, Object> result = oracleServiceClient.managePdb(operation, pdbName, parameters);
        return ResponseEntity.ok(result);
    }

    // ========== SCHEMA/USER MANAGEMENT ENDPOINTS (10 tools) ==========

    /**
     * List schemas/users
     */
    @GetMapping("/schemas")
    public ResponseEntity<Map<String, Object>> listSchemas(
            @RequestParam(required = false) Boolean includeSystemSchemas) {
        
        Map<String, Object> result = oracleServiceClient.listSchemas(includeSystemSchemas);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a new schema
     */
    @PostMapping("/schemas")
    public ResponseEntity<Map<String, Object>> createSchema(
            @RequestParam String schemaName,
            @RequestParam String password,
            @RequestParam(required = false) String tablespace,
            @RequestParam(required = false) String quota) {
        
        Map<String, Object> result = oracleServiceClient.createSchema(schemaName, password, tablespace, quota);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a new user
     * Enhanced with improved error handling and user-friendly messages
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String tablespace,
            @RequestBody(required = false) List<String> privileges) {
        
        logger.info("Creating user request - username: {}, tablespace: {}, privileges: {}", 
            username, tablespace, privileges != null ? privileges.size() + " privileges" : "default privileges");
        
        try {
            Map<String, Object> result = oracleServiceClient.createUser(username, password, tablespace, privileges);
            
            // Log success
            if ("success".equals(result.get("status"))) {
                logger.info("Successfully created user: {}", username);
            } else {
                logger.warn("User creation returned non-success status for {}: {}", username, result.get("message"));
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Unexpected error creating user {}: {}", username, e.getMessage(), e);
            
            // Return generic error response
            Map<String, Object> errorResult = Map.of(
                "status", "error",
                "message", "An unexpected error occurred while creating user",
                "username", username,
                "timestamp", java.time.Instant.now()
            );
            
            return ResponseEntity.status(500).body(errorResult);
        }
    }


    /**
     * Grant privileges to user
     */
    @PostMapping("/users/{username}/privileges/grant")
    public ResponseEntity<Map<String, Object>> grantPrivileges(
            @PathVariable String username,
            @RequestParam String privilegeType,
            @RequestBody List<String> privileges,
            @RequestParam(required = false) String objectName) {
        
        Map<String, Object> result = oracleServiceClient.grantPrivileges(username, privilegeType, privileges, objectName);
        return ResponseEntity.ok(result);
    }

    /**
     * Revoke privileges from user
     */
    @PostMapping("/users/{username}/privileges/revoke")
    public ResponseEntity<Map<String, Object>> revokePrivileges(
            @PathVariable String username,
            @RequestParam String privilegeType,
            @RequestBody List<String> privileges,
            @RequestParam(required = false) String objectName) {
        
        Map<String, Object> result = oracleServiceClient.revokePrivileges(username, privilegeType, privileges, objectName);
        return ResponseEntity.ok(result);
    }

    /**
     * Manage user sessions
     */
    @PostMapping("/users/sessions/{operation}")
    public ResponseEntity<Map<String, Object>> manageUserSessions(
            @PathVariable String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer sessionId) {
        
        Map<String, Object> result = oracleServiceClient.manageUserSessions(operation, username, sessionId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lock user account
     */
    @PostMapping("/users/{username}/lock")
    public ResponseEntity<Map<String, Object>> lockAccount(
            @PathVariable String username,
            @RequestParam(required = false) String reason) {
        
        Map<String, Object> result = oracleServiceClient.lockAccount(username, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * Unlock user account
     */
    @PostMapping("/users/{username}/unlock")
    public ResponseEntity<Map<String, Object>> unlockAccount(
            @PathVariable String username,
            @RequestParam(required = false) Boolean resetPassword,
            @RequestParam(required = false) String newPassword) {
        
        Map<String, Object> result = oracleServiceClient.unlockAccount(username, resetPassword, newPassword);
        return ResponseEntity.ok(result);
    }

    /**
     * Manage user profiles
     */
    @PostMapping("/profiles/{operation}")
    public ResponseEntity<Map<String, Object>> manageUserProfiles(
            @PathVariable String operation,
            @RequestParam String profileName,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        Map<String, Object> result = oracleServiceClient.manageUserProfiles(operation, profileName, parameters);
        return ResponseEntity.ok(result);
    }

    /**
     * Configure password policies
     */
    @PostMapping("/profiles/{profileName}/password-policy")
    public ResponseEntity<Map<String, Object>> configurePasswordPolicies(
            @PathVariable String profileName,
            @RequestParam(required = false) Integer passwordLifeDays,
            @RequestParam(required = false) Integer passwordGraceDays,
            @RequestParam(required = false) Integer passwordReuseMax,
            @RequestParam(required = false) Integer failedLoginAttempts) {
        
        Map<String, Object> result = oracleServiceClient.configurePasswordPolicies(
            profileName, passwordLifeDays, passwordGraceDays, passwordReuseMax, failedLoginAttempts);
        return ResponseEntity.ok(result);
    }

    // ========== TABLE OPERATIONS ENDPOINTS (8 tools) ==========

    /**
     * List tables in schema
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> listTables(
            @RequestParam(required = false) String schemaName,
            @RequestParam(required = false) Boolean includeSystemTables) {
        
        Map<String, Object> result = oracleServiceClient.listTables(schemaName, includeSystemTables);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a new table
     */
    @PostMapping("/tables")
    public ResponseEntity<Map<String, Object>> createTable(
            @RequestParam String tableName,
            @RequestParam(required = false) String tablespace,
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) request.get("columns");
        @SuppressWarnings("unchecked")
        List<String> primaryKey = (List<String>) request.get("primaryKey");
        
        Map<String, Object> result = oracleServiceClient.createTable(tableName, columns, primaryKey, tablespace);
        return ResponseEntity.ok(result);
    }

    /**
     * Describe table structure
     */
    @GetMapping("/tables/{tableName}/describe")
    public ResponseEntity<Map<String, Object>> describeTable(
            @PathVariable String tableName,
            @RequestParam(required = false) String schemaName) {
        
        Map<String, Object> result = oracleServiceClient.describeTable(tableName, schemaName);
        return ResponseEntity.ok(result);
    }

    /**
     * Insert records into table
     */
    @PostMapping("/tables/{tableName}/records")
    public ResponseEntity<Map<String, Object>> insertRecords(
            @PathVariable String tableName,
            @RequestBody List<Map<String, Object>> records) {
        
        Map<String, Object> result = oracleServiceClient.insertRecords(tableName, records);
        return ResponseEntity.ok(result);
    }

    /**
     * Query records from table
     */
    @PostMapping("/tables/{tableName}/query")
    public ResponseEntity<Map<String, Object>> queryRecords(
            @PathVariable String tableName,
            @RequestBody(required = false) List<String> columns,
            @RequestParam(required = false) String whereClause,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) Integer limit) {
        
        Map<String, Object> result = oracleServiceClient.queryRecords(tableName, columns, whereClause, orderBy, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * Update records in table
     */
    @PutMapping("/tables/{tableName}/records")
    public ResponseEntity<Map<String, Object>> updateRecords(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> updateData,
            @RequestParam String whereClause) {
        
        Map<String, Object> result = oracleServiceClient.updateRecords(tableName, updateData, whereClause);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete records from table
     */
    @DeleteMapping("/tables/{tableName}/records")
    public ResponseEntity<Map<String, Object>> deleteRecords(
            @PathVariable String tableName,
            @RequestParam String whereClause,
            @RequestParam(required = false) Boolean cascadeDelete) {
        
        Map<String, Object> result = oracleServiceClient.deleteRecords(tableName, whereClause, cascadeDelete);
        return ResponseEntity.ok(result);
    }

    /**
     * Truncate table (fast delete all records)
     */
    @PostMapping("/tables/{tableName}/truncate")
    public ResponseEntity<Map<String, Object>> truncateTable(
            @PathVariable String tableName,
            @RequestParam(required = false) Boolean reuseStorage) {
        
        Map<String, Object> result = oracleServiceClient.truncateTable(tableName, reuseStorage);
        return ResponseEntity.ok(result);
    }

    // ========== UTILITY ENDPOINTS ==========

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Oracle Core Service",
            "timestamp", java.time.Instant.now(),
            "availableEndpoints", 25
        ));
    }

    /**
     * Get service capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "service", "Oracle Core Service",
            "capabilities", Map.of(
                "databaseManagement", List.of("listDatabases", "createDatabase", "dropDatabase", "getDatabaseStats", "getDatabaseSize", "performBackup", "managePdb"),
                "schemaManagement", List.of("listSchemas", "createSchema", "createUser", "grantPrivileges", "revokePrivileges", "manageUserSessions", "lockAccount", "unlockAccount", "manageUserProfiles", "configurePasswordPolicies"),
                "tableOperations", List.of("listTables", "createTable", "describeTable", "insertRecords", "queryRecords", "updateRecords", "deleteRecords", "truncateTable")
            ),
            "totalEndpoints", 25,
            "timestamp", java.time.Instant.now()
        ));
    }
}
