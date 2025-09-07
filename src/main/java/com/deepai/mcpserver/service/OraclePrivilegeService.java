package com.deepai.mcpserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * Oracle Privilege Service
 * Checks user privileges and determines available operations
 */
@Service
public class OraclePrivilegeService {

    private static final Logger logger = LoggerFactory.getLogger(OraclePrivilegeService.class);
    
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OraclePrivilegeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get comprehensive privilege information for current user
     */
    public Map<String, Object> getUserPrivileges() {
        logger.info("Starting getUserPrivileges operation");
        
        try {
            Map<String, Object> privilegeInfo = new HashMap<>();
            
            // Get current user info
            String currentUser = jdbcTemplate.queryForObject("SELECT USER FROM dual", String.class);
            logger.info("Checking privileges for user: {}", currentUser);
            
            privilegeInfo.put("currentUser", currentUser);
            privilegeInfo.put("timestamp", Instant.now());
            
            // Check system privileges
            Map<String, Object> systemPrivileges = getSystemPrivileges();
            privilegeInfo.put("systemPrivileges", systemPrivileges);
            
            // Check role privileges  
            Map<String, Object> rolePrivileges = getRolePrivileges();
            privilegeInfo.put("rolePrivileges", rolePrivileges);
            
            // Check object privileges
            Map<String, Object> objectPrivileges = getObjectPrivileges();
            privilegeInfo.put("objectPrivileges", objectPrivileges);
            
            // Determine available operations based on privileges
            Map<String, Object> availableOperations = determineAvailableOperations(
                systemPrivileges, rolePrivileges, objectPrivileges);
            privilegeInfo.put("availableOperations", availableOperations);
            
            // Get privilege level assessment
            String privilegeLevel = assessPrivilegeLevel(systemPrivileges, rolePrivileges);
            privilegeInfo.put("privilegeLevel", privilegeLevel);
            
            privilegeInfo.put("status", "success");
            logger.info("Successfully retrieved privileges for user: {} (Level: {})", currentUser, privilegeLevel);
            
            return privilegeInfo;
            
        } catch (Exception e) {
            logger.error("Failed to get user privileges: {}", e.getMessage(), e);
            return Map.of(
                "status", "error",
                "message", "Failed to get user privileges: " + e.getMessage(),
                "timestamp", Instant.now()
            );
        }
    }

    /**
     * Get system privileges for current user
     */
    private Map<String, Object> getSystemPrivileges() {
        try {
            logger.debug("Checking system privileges");
            List<Map<String, Object>> systemPrivs = jdbcTemplate.queryForList(
                "SELECT privilege, admin_option FROM user_sys_privs ORDER BY privilege");
            
            Map<String, Object> result = new HashMap<>();
            result.put("privileges", systemPrivs);
            result.put("count", systemPrivs.size());
            result.put("hasAdminOptions", systemPrivs.stream()
                .anyMatch(p -> "YES".equals(p.get("ADMIN_OPTION"))));
            
            logger.info("Found {} system privileges", systemPrivs.size());
            return result;
            
        } catch (Exception e) {
            logger.warn("Cannot access system privileges: {}", e.getMessage());
            return Map.of("privileges", Collections.emptyList(), "count", 0, "accessible", false);
        }
    }

    /**
     * Get role privileges for current user  
     */
    private Map<String, Object> getRolePrivileges() {
        try {
            logger.debug("Checking role privileges");
            List<Map<String, Object>> rolePrivs = jdbcTemplate.queryForList(
                "SELECT granted_role, admin_option, default_role FROM user_role_privs ORDER BY granted_role");
            
            Map<String, Object> result = new HashMap<>();
            result.put("roles", rolePrivs);
            result.put("count", rolePrivs.size());
            result.put("defaultRoles", rolePrivs.stream()
                .filter(r -> "YES".equals(r.get("DEFAULT_ROLE")))
                .map(r -> r.get("GRANTED_ROLE"))
                .toList());
            
            logger.info("Found {} role privileges", rolePrivs.size());
            return result;
            
        } catch (Exception e) {
            logger.warn("Cannot access role privileges: {}", e.getMessage());
            return Map.of("roles", Collections.emptyList(), "count", 0, "accessible", false);
        }
    }

    /**
     * Get object privileges for current user
     */
    private Map<String, Object> getObjectPrivileges() {
        try {
            logger.debug("Checking object privileges");
            List<Map<String, Object>> objPrivs = jdbcTemplate.queryForList(
                "SELECT owner, table_name, privilege, grantable FROM user_tab_privs " +
                "ORDER BY owner, table_name, privilege");
            
            Map<String, Object> result = new HashMap<>();
            result.put("privileges", objPrivs);
            result.put("count", objPrivs.size());
            
            // Group by privilege type
            Map<String, List<String>> privilegeGroups = new HashMap<>();
            objPrivs.forEach(priv -> {
                String privilege = (String) priv.get("PRIVILEGE");
                privilegeGroups.computeIfAbsent(privilege, k -> new ArrayList<>())
                    .add(priv.get("OWNER") + "." + priv.get("TABLE_NAME"));
            });
            result.put("privilegesByType", privilegeGroups);
            
            logger.info("Found {} object privileges", objPrivs.size());
            return result;
            
        } catch (Exception e) {
            logger.warn("Cannot access object privileges: {}", e.getMessage());
            return Map.of("privileges", Collections.emptyList(), "count", 0, "accessible", false);
        }
    }

    /**
     * Determine available operations based on user privileges
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> determineAvailableOperations(
            Map<String, Object> systemPrivs, 
            Map<String, Object> rolePrivs, 
            Map<String, Object> objectPrivs) {
        
        logger.debug("Determining available operations based on privileges");
        
        Map<String, Object> operations = new HashMap<>();
        
        // Extract privilege lists
        List<Map<String, Object>> sysPrivList = (List<Map<String, Object>>) systemPrivs.get("privileges");
        List<Map<String, Object>> rolePrivList = (List<Map<String, Object>>) rolePrivs.get("roles");
        List<Map<String, Object>> objPrivList = (List<Map<String, Object>>) objectPrivs.get("privileges");
        
        // Convert to sets for easier checking
        Set<String> systemPrivileges = sysPrivList.stream()
            .map(p -> (String) p.get("PRIVILEGE"))
            .collect(java.util.stream.Collectors.toSet());
            
        Set<String> roles = rolePrivList.stream()
            .map(r -> (String) r.get("GRANTED_ROLE"))
            .collect(java.util.stream.Collectors.toSet());

        // Database management operations
        Map<String, Boolean> dbOperations = new HashMap<>();
        dbOperations.put("listDatabases", true); // Basic connection ability
        dbOperations.put("getDatabaseStats", systemPrivileges.contains("SELECT ANY TABLE") || 
            hasAnySystemPrivilege(systemPrivileges, "DBA", "SELECT"));
        dbOperations.put("getDatabaseSize", systemPrivileges.contains("SELECT ANY TABLE") ||
            roles.contains("DBA"));
        dbOperations.put("createDatabase", systemPrivileges.contains("CREATE DATABASE"));
        dbOperations.put("dropDatabase", systemPrivileges.contains("DROP DATABASE"));
        dbOperations.put("performBackup", roles.contains("SYSBACKUP") || roles.contains("DBA"));
        operations.put("databaseOperations", dbOperations);

        // Schema/User management operations  
        Map<String, Boolean> schemaOperations = new HashMap<>();
        schemaOperations.put("listSchemas", true); // Can always see some schemas
        schemaOperations.put("createSchema", systemPrivileges.contains("CREATE USER"));
        schemaOperations.put("createUser", systemPrivileges.contains("CREATE USER"));
        schemaOperations.put("grantPrivileges", systemPrivileges.contains("GRANT ANY PRIVILEGE") ||
            systemPrivileges.contains("GRANT ANY ROLE"));
        schemaOperations.put("revokePrivileges", systemPrivileges.contains("GRANT ANY PRIVILEGE"));
        schemaOperations.put("manageUserSessions", systemPrivileges.contains("ALTER SYSTEM"));
        schemaOperations.put("lockAccount", systemPrivileges.contains("ALTER USER"));
        schemaOperations.put("unlockAccount", systemPrivileges.contains("ALTER USER"));
        operations.put("schemaOperations", schemaOperations);

        // Table operations
        Map<String, Boolean> tableOperations = new HashMap<>();
        tableOperations.put("listTables", true); // Can see accessible tables
        tableOperations.put("describeTables", true); // Can describe accessible tables
        tableOperations.put("createTable", systemPrivileges.contains("CREATE ANY TABLE") ||
            roles.contains("RESOURCE"));
        tableOperations.put("alterTable", systemPrivileges.contains("ALTER ANY TABLE") ||
            roles.contains("RESOURCE"));
        tableOperations.put("dropTable", systemPrivileges.contains("DROP ANY TABLE") ||
            roles.contains("RESOURCE"));
        // CRUD operations: Users with RESOURCE can operate on their own tables + granted objects
        tableOperations.put("insertRecords", systemPrivileges.contains("INSERT ANY TABLE") ||
            roles.contains("RESOURCE") || !objPrivList.isEmpty());
        tableOperations.put("updateRecords", systemPrivileges.contains("UPDATE ANY TABLE") ||
            roles.contains("RESOURCE") || !objPrivList.isEmpty());
        tableOperations.put("deleteRecords", systemPrivileges.contains("DELETE ANY TABLE") ||
            roles.contains("RESOURCE") || !objPrivList.isEmpty());
        tableOperations.put("truncateTable", systemPrivileges.contains("DROP ANY TABLE") ||
            roles.contains("RESOURCE"));
        operations.put("tableOperations", tableOperations);

        // Calculate operation summary
        int totalOperations = dbOperations.size() + schemaOperations.size() + tableOperations.size();
        long availableCount = dbOperations.values().stream().mapToLong(b -> b ? 1 : 0).sum() +
                            schemaOperations.values().stream().mapToLong(b -> b ? 1 : 0).sum() +
                            tableOperations.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        
        operations.put("summary", Map.of(
            "totalOperations", totalOperations,
            "availableOperations", availableCount,
            "restrictedOperations", totalOperations - availableCount,
            "accessibilityPercentage", Math.round((double) availableCount / totalOperations * 100)
        ));

        logger.info("Determined {} out of {} operations are available", availableCount, totalOperations);
        return operations;
    }

    /**
     * Assess overall privilege level
     */
    @SuppressWarnings("unchecked")
    private String assessPrivilegeLevel(Map<String, Object> systemPrivs, Map<String, Object> rolePrivs) {
        List<Map<String, Object>> roles = (List<Map<String, Object>>) rolePrivs.get("roles");
        List<Map<String, Object>> sysPrivs = (List<Map<String, Object>>) systemPrivs.get("privileges");
        
        Set<String> roleNames = roles.stream()
            .map(r -> (String) r.get("GRANTED_ROLE"))
            .collect(java.util.stream.Collectors.toSet());
            
        Set<String> privilegeNames = sysPrivs.stream()
            .map(p -> (String) p.get("PRIVILEGE"))
            .collect(java.util.stream.Collectors.toSet());

        // Determine privilege level
        if (roleNames.contains("DBA") || roleNames.contains("SYSDBA")) {
            return "DBA"; // Database Administrator
        } else if (privilegeNames.contains("CREATE ANY TABLE") && privilegeNames.contains("SELECT ANY TABLE")) {
            return "ADVANCED"; // Advanced user with broad privileges
        } else if (roleNames.contains("RESOURCE") && roleNames.contains("CONNECT")) {
            // Check if we have additional privileges that make this ADVANCED
            if (privilegeNames.contains("SELECT ANY TABLE") || privilegeNames.contains("INSERT ANY TABLE")) {
                return "ADVANCED"; // RESOURCE + additional ANY TABLE privileges
            }
            return "DEVELOPER"; // Standard developer privileges with table ownership capabilities
        } else if (roleNames.contains("CONNECT")) {
            return "BASIC"; // Basic connection privileges
        } else {
            return "LIMITED"; // Very limited privileges
        }
    }

    /**
     * Check if user has any system privilege matching pattern
     */
    private boolean hasAnySystemPrivilege(Set<String> privileges, String... patterns) {
        return Arrays.stream(patterns).anyMatch(pattern ->
            privileges.stream().anyMatch(priv -> priv.contains(pattern)));
    }

    /**
     * Check specific Oracle feature availability
     */
    public Map<String, Object> checkFeatureAvailability(String featureName) {
        logger.info("Checking availability of feature: {}", featureName);
        
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("feature", featureName);
            result.put("timestamp", Instant.now());
            
            switch (featureName.toUpperCase()) {
                case "AWR":
                    result.put("available", checkAwrAccess());
                    break;
                case "PDB":
                    result.put("available", checkPdbAccess());
                    break;
                case "VECTOR_SEARCH":
                    result.put("available", checkVectorSearchAccess());
                    break;
                case "ANALYTICS":
                    result.put("available", checkAnalyticsAccess());
                    break;
                default:
                    result.put("available", false);
                    result.put("message", "Unknown feature: " + featureName);
            }
            
            result.put("status", "success");
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to check feature availability for {}: {}", featureName, e.getMessage());
            return Map.of(
                "status", "error",
                "feature", featureName,
                "message", "Failed to check feature: " + e.getMessage(),
                "timestamp", Instant.now()
            );
        }
    }

    private boolean checkAwrAccess() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dba_hist_snapshot WHERE ROWNUM = 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.debug("AWR access not available: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkPdbAccess() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cdb_pdbs WHERE ROWNUM = 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.debug("PDB access not available: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkVectorSearchAccess() {
        try {
            // Check if Oracle 23c vector features are available
            jdbcTemplate.execute("SELECT 1 FROM dual WHERE EXISTS (SELECT 1 FROM all_objects WHERE object_name = 'VECTOR')");
            return true;
        } catch (Exception e) {
            logger.debug("Vector search not available: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkAnalyticsAccess() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables", Integer.class);
            return true;
        } catch (Exception e) {
            logger.debug("Analytics access not available: {}", e.getMessage());
            return false;
        }
    }
}


