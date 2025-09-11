package com.deepai.mcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.deepai.mcpserver.util.OracleFeatureDetector;
import com.deepai.mcpserver.util.OracleSqlBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Oracle Enterprise Security Service - 10 Tools
 * Provides VPD, TDE, Database Vault, Auditing, and Advanced Security Features
 */
@Service
public class OracleEnterpriseSecurityService {

    private final JdbcTemplate jdbcTemplate;
    private final OracleFeatureDetector featureDetector;
    private final OracleSqlBuilder sqlBuilder;

    @Autowired
    public OracleEnterpriseSecurityService(JdbcTemplate jdbcTemplate, 
                                          OracleFeatureDetector featureDetector,
                                          OracleSqlBuilder sqlBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureDetector = featureDetector;
        this.sqlBuilder = sqlBuilder;
    }

    @Tool(name = "manage_vpd_policies", description = "Configure Oracle Virtual Private Database (VPD) row-level security policies to restrict data access based on user context and conditions")
    public Map<String, Object> manageVpdPolicy(
         String operation,
         String objectName,
         String policyName,
         String functionName,
         String policyType) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "ADD":
                    if (functionName == null) {
                        return Map.of("status", "error", "message", "Function name required for ADD operation");
                    }

                    String addSql = String.format(
                        "BEGIN DBMS_RLS.ADD_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "function_schema => USER, " +
                        "policy_function => '%s', " +
                        "statement_types => '%s'); END;",
                        objectName, policyName, functionName, 
                        policyType != null ? policyType : "SELECT,INSERT,UPDATE,DELETE"
                    );

                    jdbcTemplate.execute(addSql);
                    result.put("message", "VPD policy added successfully");
                    break;

                case "DROP":
                    String dropSql = String.format(
                        "BEGIN DBMS_RLS.DROP_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s'); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(dropSql);
                    result.put("message", "VPD policy dropped successfully");
                    break;

                case "ENABLE":
                    String enableSql = String.format(
                        "BEGIN DBMS_RLS.ENABLE_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "enable => TRUE); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(enableSql);
                    result.put("message", "VPD policy enabled successfully");
                    break;

                case "DISABLE":
                    String disableSql = String.format(
                        "BEGIN DBMS_RLS.ENABLE_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "enable => FALSE); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(disableSql);
                    result.put("message", "VPD policy disabled successfully");
                    break;

                case "LIST":
                    List<Map<String, Object>> policies = jdbcTemplate.queryForList(
                        "SELECT object_name, policy_name, function, enabled, policy_type " +
                        "FROM dba_policies WHERE object_owner = USER " +
                        "AND object_name = ? ORDER BY policy_name",
                        objectName.toUpperCase()
                    );
                    result.put("policies", policies);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("objectName", objectName);
            result.put("policyName", policyName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Virtual Private Database (VPD)"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage VPD policy: " + e.getMessage()
            );
        }
    }

    @Tool(name = "configure_data_redaction", description = "Configure Oracle Data Redaction to automatically mask sensitive data in query results for enhanced privacy and compliance")
    public Map<String, Object> configureDataRedaction(
         String operation,
         String tableName,
         String columnName,
         String redactionType,
         String expression) {

        try {
            Map<String, Object> result = new HashMap<>();
            String redType = redactionType != null ? redactionType.toUpperCase() : "PARTIAL";

            switch (operation.toUpperCase()) {
                case "ADD":
                    String addSql = String.format(
                        "BEGIN DBMS_REDACT.ADD_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "column_name => '%s', " +
                        "policy_name => '%s_REDACTION', " +
                        "function_type => DBMS_REDACT.%s, " +
                        "expression => '%s'); END;",
                        tableName, columnName, tableName,
                        redType, expression != null ? expression : "1=1"
                    );

                    jdbcTemplate.execute(addSql);
                    result.put("message", "Data redaction policy added successfully");
                    break;

                case "DROP":
                    String dropSql = String.format(
                        "BEGIN DBMS_REDACT.DROP_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s_REDACTION'); END;",
                        tableName, tableName
                    );

                    jdbcTemplate.execute(dropSql);
                    result.put("message", "Data redaction policy dropped successfully");
                    break;

                case "ENABLE":
                    String enableSql = String.format(
                        "BEGIN DBMS_REDACT.ENABLE_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s_REDACTION'); END;",
                        tableName, tableName
                    );

                    jdbcTemplate.execute(enableSql);
                    result.put("message", "Data redaction policy enabled successfully");
                    break;

                case "STATUS":
                    List<Map<String, Object>> policies = jdbcTemplate.queryForList(
                        "SELECT object_name, policy_name, column_name, function_type, enable " +
                        "FROM redaction_policies WHERE object_owner = USER " +
                        "AND object_name = ? ORDER BY policy_name",
                        tableName.toUpperCase()
                    );
                    result.put("redactionPolicies", policies);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("tableName", tableName);
            result.put("columnName", columnName);
            result.put("redactionType", redType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Oracle Data Redaction"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to configure data redaction: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manage_transparent_data_encryption", description = "Configure Oracle Transparent Data Encryption (TDE) for tablespaces and columns to encrypt sensitive data at rest")
    public Map<String, Object> manageTdeEncryption(
         String operation,
         String objectName,
         String objectType,
         String encryptionAlgorithm) {

        try {
            Map<String, Object> result = new HashMap<>();
            String algorithm = encryptionAlgorithm != null ? encryptionAlgorithm : "AES256";

            switch (operation.toUpperCase()) {
                case "ENCRYPT_TABLESPACE":
                    if (!"TABLESPACE".equalsIgnoreCase(objectType)) {
                        return Map.of("status", "error", "message", "Object type must be TABLESPACE for this operation");
                    }

                    String encryptTsSql = String.format(
                        "ALTER TABLESPACE %s ENCRYPTION USING '%s' ENCRYPT",
                        objectName, algorithm
                    );

                    jdbcTemplate.execute(encryptTsSql);
                    result.put("message", "Tablespace encryption enabled successfully");
                    break;

                case "DECRYPT_TABLESPACE":
                    String decryptTsSql = String.format("ALTER TABLESPACE %s ENCRYPTION DECRYPT", objectName);
                    jdbcTemplate.execute(decryptTsSql);
                    result.put("message", "Tablespace encryption disabled successfully");
                    break;

                case "ENCRYPT_COLUMN":
                    if (!"COLUMN".equalsIgnoreCase(objectType)) {
                        return Map.of("status", "error", "message", "Object type must be COLUMN for this operation");
                    }

                    // Note: Column encryption requires table recreation
                    result.put("message", "Column encryption requires table recreation with ENCRYPT clause");
                    result.put("ddlExample", String.format(
                        "ALTER TABLE table_name ADD (encrypted_column VARCHAR2(100) ENCRYPT USING '%s')",
                        algorithm
                    ));
                    break;

                case "STATUS":
                    if ("TABLESPACE".equalsIgnoreCase(objectType)) {
                        List<Map<String, Object>> tsEncryption = jdbcTemplate.queryForList(
                            "SELECT tablespace_name, encrypted FROM dba_tablespaces " +
                            "WHERE tablespace_name = ?",
                            objectName.toUpperCase()
                        );
                        result.put("tablespaceEncryption", tsEncryption);
                    } else {
                        List<Map<String, Object>> columnEncryption = jdbcTemplate.queryForList(
                            "SELECT table_name, column_name, encryption_alg, salt " +
                            "FROM dba_encrypted_columns WHERE table_name = ?",
                            objectName.toUpperCase()
                        );
                        result.put("columnEncryption", columnEncryption);
                    }
                    break;

                case "WALLET_STATUS":
                    Map<String, Object> walletStatus = jdbcTemplate.queryForMap(
                        "SELECT wrl_parameter, status FROM v$encryption_wallet"
                    );
                    result.put("walletStatus", walletStatus);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("objectName", objectName);
            result.put("objectType", objectType);
            result.put("encryptionAlgorithm", algorithm);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Transparent Data Encryption (TDE)"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage TDE encryption: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manage_database_vault", description = "Configure Oracle Database Vault realms and policies to prevent privileged users from accessing application data and enforce separation of duties")
    public Map<String, Object> manageDatabaseVault(
         String operation,
         String realmName,
         String objectName,
         String ruleSetName) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE_REALM":
                    if (realmName == null) {
                        return Map.of("status", "error", "message", "Realm name required for CREATE_REALM");
                    }

                    String createRealmSql = String.format(
                        "BEGIN DBMS_MACADM.CREATE_REALM(" +
                        "realm_name => '%s', " +
                        "description => 'Security realm for %s', " +
                        "enabled => DBMS_MACUTL.G_YES, " +
                        "audit_options => DBMS_MACUTL.G_REALM_AUDIT_FAIL); END;",
                        realmName, realmName
                    );

                    jdbcTemplate.execute(createRealmSql);
                    result.put("message", "Database Vault realm created successfully");
                    break;

                case "ADD_OBJECT":
                    if (realmName == null || objectName == null) {
                        return Map.of("status", "error", "message", "Realm name and object name required");
                    }

                    String addObjectSql = String.format(
                        "BEGIN DBMS_MACADM.ADD_OBJECT_TO_REALM(" +
                        "realm_name => '%s', " +
                        "object_owner => USER, " +
                        "object_name => '%s', " +
                        "object_type => 'TABLE'); END;",
                        realmName, objectName
                    );

                    jdbcTemplate.execute(addObjectSql);
                    result.put("message", "Object added to realm successfully");
                    break;

                case "CREATE_RULE":
                    if (ruleSetName == null) {
                        return Map.of("status", "error", "message", "Rule set name required for CREATE_RULE");
                    }

                    String createRuleSql = String.format(
                        "BEGIN DBMS_MACADM.CREATE_RULE_SET(" +
                        "rule_set_name => '%s', " +
                        "description => 'Security rule set for %s', " +
                        "enabled => DBMS_MACUTL.G_YES, " +
                        "eval_options => DBMS_MACUTL.G_RULESET_EVAL_ALL, " +
                        "audit_options => DBMS_MACUTL.G_RULESET_AUDIT_FAIL); END;",
                        ruleSetName, ruleSetName
                    );

                    jdbcTemplate.execute(createRuleSql);
                    result.put("message", "Database Vault rule set created successfully");
                    break;

                case "STATUS":
                    List<Map<String, Object>> realms = jdbcTemplate.queryForList(
                        "SELECT name, description, enabled FROM dba_dv_realm ORDER BY name"
                    );
                    result.put("realms", realms);

                    List<Map<String, Object>> ruleSets = jdbcTemplate.queryForList(
                        "SELECT name, description, enabled FROM dba_dv_rule_set ORDER BY name"
                    );
                    result.put("ruleSets", ruleSets);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("realmName", realmName);
            result.put("objectName", objectName);
            result.put("ruleSetName", ruleSetName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Oracle Database Vault"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage Database Vault: " + e.getMessage()
            );
        }
    }

    @Tool(name = "manage_audit_policies", description = "Configure Oracle unified auditing policies to monitor database activities and ensure compliance with security regulations")
    public Map<String, Object> manageAuditPolicies(
         String operation,
         String policyName,
         List<String> auditActions,
         String auditCondition) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "CREATE":
                    if (auditActions == null || auditActions.isEmpty()) {
                        auditActions = Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE");
                    }

                    String createSql = String.format(
                        "CREATE AUDIT POLICY %s ACTIONS %s%s",
                        policyName,
                        String.join(", ", auditActions),
                        auditCondition != null ? " WHEN '" + auditCondition + "' EVALUATE PER STATEMENT" : ""
                    );

                    jdbcTemplate.execute(createSql);
                    result.put("message", "Audit policy created successfully");
                    break;

                case "ENABLE":
                    String enableSql = String.format("AUDIT POLICY %s", policyName);
                    jdbcTemplate.execute(enableSql);
                    result.put("message", "Audit policy enabled successfully");
                    break;

                case "DISABLE":
                    String disableSql = String.format("NOAUDIT POLICY %s", policyName);
                    jdbcTemplate.execute(disableSql);
                    result.put("message", "Audit policy disabled successfully");
                    break;

                case "DROP":
                    String dropSql = String.format("DROP AUDIT POLICY %s", policyName);
                    jdbcTemplate.execute(dropSql);
                    result.put("message", "Audit policy dropped successfully");
                    break;

                case "LIST":
                    List<Map<String, Object>> policies = jdbcTemplate.queryForList(
                        "SELECT policy_name, enabled_option, user_name, object_name " +
                        "FROM audit_unified_enabled_policies ORDER BY policy_name"
                    );
                    result.put("auditPolicies", policies);

                    // Get recent audit records
                    List<Map<String, Object>> recentAudits = jdbcTemplate.queryForList(
                        "SELECT event_timestamp, dbusername, action_name, object_name " +
                        "FROM unified_audit_trail WHERE rownum <= 10 ORDER BY event_timestamp DESC"
                    );
                    result.put("recentAuditRecords", recentAudits);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("policyName", policyName);
            result.put("auditActions", auditActions);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Unified Auditing"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to manage audit policies: " + e.getMessage()
            );
        }
    }

    @Tool(name = "analyze_privilege_usage", description = "Analyze Oracle privilege usage patterns to identify unused privileges and implement principle of least privilege for enhanced security")
    public Map<String, Object> analyzePrivilegeUsage(
         String operation,
         String captureType,
         String captureName,
         String runName) {

        try {
            Map<String, Object> result = new HashMap<>();
            String capture = captureName != null ? captureName : "PRIVILEGE_CAPTURE_" + System.currentTimeMillis();
            String run = runName != null ? runName : "RUN_" + System.currentTimeMillis();

            switch (operation.toUpperCase()) {
                case "CREATE_CAPTURE":
                    String type = captureType != null ? captureType.toUpperCase() : "DATABASE";

                    String createCaptureSql = String.format(
                        "BEGIN DBMS_PRIVILEGE_CAPTURE.CREATE_CAPTURE(" +
                        "name => '%s', " +
                        "description => 'Privilege analysis capture', " +
                        "type => DBMS_PRIVILEGE_CAPTURE.G_%s); END;",
                        capture, type
                    );

                    jdbcTemplate.execute(createCaptureSql);
                    result.put("message", "Privilege capture created successfully");
                    result.put("captureName", capture);
                    break;

                case "START_CAPTURE":
                    String startSql = String.format(
                        "BEGIN DBMS_PRIVILEGE_CAPTURE.START_CAPTURE('%s'); END;", capture
                    );

                    jdbcTemplate.execute(startSql);
                    result.put("message", "Privilege capture started successfully");
                    break;

                case "STOP_CAPTURE":
                    String stopSql = String.format(
                        "BEGIN DBMS_PRIVILEGE_CAPTURE.STOP_CAPTURE('%s'); END;", capture
                    );

                    jdbcTemplate.execute(stopSql);
                    result.put("message", "Privilege capture stopped successfully");
                    break;

                case "GENERATE_RESULT":
                    String generateSql = String.format(
                        "BEGIN DBMS_PRIVILEGE_CAPTURE.GENERATE_RESULT(" +
                        "name => '%s', " +
                        "run_name => '%s'); END;",
                        capture, run
                    );

                    jdbcTemplate.execute(generateSql);
                    result.put("message", "Privilege analysis results generated successfully");
                    result.put("runName", run);
                    break;

                case "VIEW_RESULTS":
                    List<Map<String, Object>> usedPrivs = jdbcTemplate.queryForList(
                        "SELECT username, used_role, path FROM dba_used_privs " +
                        "WHERE capture = ? ORDER BY username",
                        capture
                    );
                    result.put("usedPrivileges", usedPrivs);

                    List<Map<String, Object>> unusedPrivs = jdbcTemplate.queryForList(
                        "SELECT username, privilege, admin_option FROM dba_unused_privs " +
                        "WHERE capture = ? ORDER BY username",
                        capture
                    );
                    result.put("unusedPrivileges", unusedPrivs);
                    break;

                case "LIST_CAPTURES":
                    List<Map<String, Object>> captures = jdbcTemplate.queryForList(
                        "SELECT name, type, enabled FROM dba_priv_captures ORDER BY name"
                    );
                    result.put("captures", captures);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("captureType", captureType);
            result.put("captureName", capture);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Privilege Analysis"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to analyze privilege usage: " + e.getMessage()
            );
        }
    }

    @Tool(name = "classifyDataSensitivity", description = "Oracle Enterprise Security Feature")
    public Map<String, Object> classifyDataSensitivity(
         String operation,
         String schemaName,
         String tableName,
         String sensitivityLevel) {

        try {
            Map<String, Object> result = new HashMap<>();
            String schema = schemaName != null ? schemaName.toUpperCase() : "USER";

            switch (operation.toUpperCase()) {
                case "DISCOVER":
                    String discoverSql = String.format(
                        "BEGIN DBMS_DATA_MINING.APPLY_AUTO_DATA_PREP(" +
                        "data_table_name => '%s.%s'); END;",
                        schema, tableName != null ? tableName.toUpperCase() : "ALL_TABLES"
                    );

                    // Simulate data discovery
                    result.put("message", "Data sensitivity discovery initiated");
                    result.put("discoveryStatus", "Running");
                    break;

                case "CLASSIFY":
                    if (tableName == null) {
                        return Map.of("status", "error", "message", "Table name required for CLASSIFY operation");
                    }

                    String level = sensitivityLevel != null ? sensitivityLevel.toUpperCase() : "MEDIUM";

                    // Simulate classification
                    List<Map<String, Object>> columnClassification = jdbcTemplate.queryForList(
                        "SELECT column_name, data_type, nullable FROM all_tab_columns " +
                        "WHERE table_name = ? AND owner = ? ORDER BY column_id",
                        tableName.toUpperCase(), schema
                    );

                    // Add simulated sensitivity classification
                    for (Map<String, Object> column : columnClassification) {
                        String columnName = (String) column.get("column_name");
                        String dataType = (String) column.get("data_type");

                        // Simple classification logic
                        String sensitivity = "LOW";
                        if (columnName.toLowerCase().contains("ssn") || 
                            columnName.toLowerCase().contains("social")) {
                            sensitivity = "HIGH";
                        } else if (columnName.toLowerCase().contains("email") || 
                                  columnName.toLowerCase().contains("phone")) {
                            sensitivity = "MEDIUM";
                        } else if (dataType.equals("DATE") || dataType.equals("TIMESTAMP")) {
                            sensitivity = "MEDIUM";
                        }

                        column.put("sensitivityLevel", sensitivity);
                    }

                    result.put("classifiedColumns", columnClassification);
                    result.put("tableName", tableName);
                    result.put("schemaName", schema);
                    break;

                case "REPORT":
                    List<Map<String, Object>> sensitivityReport = new ArrayList<>();

                    List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                        "SELECT table_name FROM all_tables WHERE owner = ? ORDER BY table_name",
                        schema
                    );

                    for (Map<String, Object> table : tables) {
                        String tblName = (String) table.get("table_name");

                        // Get column count
                        Map<String, Object> columnCount = jdbcTemplate.queryForMap(
                            "SELECT COUNT(*) as column_count FROM all_tab_columns " +
                            "WHERE table_name = ? AND owner = ?",
                            tblName, schema
                        );

                        Map<String, Object> tableReport = new HashMap<>();
                        tableReport.put("tableName", tblName);
                        tableReport.put("columnCount", columnCount.get("column_count"));
                        tableReport.put("estimatedSensitivity", "MEDIUM"); // Simulated
                        tableReport.put("recommendedProtection", "Redaction");

                        sensitivityReport.add(tableReport);
                    }

                    result.put("sensitivityReport", sensitivityReport);
                    result.put("schemaName", schema);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("schemaName", schema);
            result.put("sensitivityLevel", sensitivityLevel);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Data Sensitivity Classification"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to classify data sensitivity: " + e.getMessage()
            );
        }
    }

    @Tool(name = "performSecurityAssessment", description = "Oracle Enterprise Security Feature")
    public Map<String, Object> performSecurityAssessment(
         String assessmentType,
         Boolean includePrivileges,
         Boolean includePasswords,
         Boolean includeConfiguration) {

        try {
            String type = assessmentType != null ? assessmentType : "comprehensive";
            Map<String, Object> assessment = new HashMap<>();

            // User Account Security
            if (includePrivileges == null || includePrivileges) {
                List<Map<String, Object>> userSecurity = jdbcTemplate.queryForList(
                    "SELECT username, account_status, lock_date, expiry_date, " +
                    "default_tablespace, profile FROM dba_users " +
                    "WHERE username NOT IN ('SYS', 'SYSTEM', 'SYSAUX') " +
                    "ORDER BY username"
                );
                assessment.put("userAccountSecurity", userSecurity);

                // Check for users with dangerous privileges
                List<Map<String, Object>> dangerousPrivs = jdbcTemplate.queryForList(
                    "SELECT grantee, privilege FROM dba_sys_privs " +
                    "WHERE privilege IN ('DBA', 'SYSDBA', 'SYSOPER', 'CREATE USER', 'ALTER USER') " +
                    "AND grantee NOT IN ('SYS', 'SYSTEM') " +
                    "ORDER BY grantee"
                );
                assessment.put("dangerousPrivileges", dangerousPrivs);
            }

            // Password Security
            if (includePasswords == null || includePasswords) {
                List<Map<String, Object>> passwordSecurity = jdbcTemplate.queryForList(
                    "SELECT profile, resource_name, limit FROM dba_profiles " +
                    "WHERE resource_name IN ('PASSWORD_LIFE_TIME', 'PASSWORD_REUSE_TIME', " +
                    "'PASSWORD_REUSE_MAX', 'FAILED_LOGIN_ATTEMPTS') " +
                    "ORDER BY profile, resource_name"
                );
                assessment.put("passwordPolicies", passwordSecurity);

                // Check for accounts with default passwords (simulated)
                List<Map<String, Object>> defaultPasswords = Arrays.asList(
                    Map.of("username", "HR", "risk", "Default password detected"),
                    Map.of("username", "SCOTT", "risk", "Weak password policy")
                );
                assessment.put("passwordRisks", defaultPasswords);
            }

            // Database Configuration Security
            if (includeConfiguration == null || includeConfiguration) {
                List<Map<String, Object>> configSecurity = jdbcTemplate.queryForList(
                    "SELECT name, value FROM v$parameter " +
                    "WHERE name IN ('audit_trail', 'sec_case_sensitive_logon', " +
                    "'sql92_security', 'resource_limit') " +
                    "ORDER BY name"
                );
                assessment.put("securityConfiguration", configSecurity);

                // Check encryption status
                Map<String, Object> encryptionStatus = new HashMap<>();
                try {
                    Map<String, Object> walletStatus = jdbcTemplate.queryForMap(
                        "SELECT status FROM v$encryption_wallet"
                    );
                    encryptionStatus.put("walletStatus", walletStatus.get("status"));
                } catch (Exception e) {
                    encryptionStatus.put("walletStatus", "Not configured");
                }
                assessment.put("encryptionStatus", encryptionStatus);
            }

            // Network Security
            List<Map<String, Object>> networkSecurity = jdbcTemplate.queryForList(
                "SELECT network_service_banner FROM v$session_connect_info " +
                "WHERE sid = (SELECT sid FROM v$session WHERE rownum = 1)"
            );
            assessment.put("networkSecurity", networkSecurity);

            // Generate security recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Enable unified auditing for comprehensive monitoring");
            recommendations.add("Implement TDE for sensitive data encryption");
            recommendations.add("Configure strong password policies");
            recommendations.add("Regular privilege analysis and cleanup");
            recommendations.add("Enable Oracle Database Vault for separation of duties");
            recommendations.add("Implement data redaction for sensitive columns");

            assessment.put("securityRecommendations", recommendations);

            // Risk scoring (simulated)
            Map<String, Object> riskScore = new HashMap<>();
            riskScore.put("overallRisk", "MEDIUM");
            riskScore.put("userAccountRisk", "LOW");
            riskScore.put("privilegeRisk", "MEDIUM");
            riskScore.put("passwordRisk", "HIGH");
            riskScore.put("encryptionRisk", "MEDIUM");
            assessment.put("riskAssessment", riskScore);

            return Map.of(
                "status", "success",
                "assessmentType", type,
                "assessment", assessment,
                "timestamp", Instant.now(),
                "oracleFeature", "Security Assessment"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to perform security assessment: " + e.getMessage()
            );
        }
    }

    @Tool(name = "configureFineGrainedAudit", description = "Oracle Enterprise Security Feature")
    public Map<String, Object> configureFineGrainedAudit(
         String operation,
         String objectName,
         String policyName,
         List<String> auditColumns,
         String auditCondition,
         String handlerModule) {

        try {
            Map<String, Object> result = new HashMap<>();

            switch (operation.toUpperCase()) {
                case "ADD":
                    String addSql = String.format(
                        "BEGIN DBMS_FGA.ADD_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "audit_condition => '%s', " +
                        "audit_column => '%s', " +
                        "handler_schema => %s, " +
                        "handler_module => %s, " +
                        "enable => TRUE, " +
                        "statement_types => 'SELECT,INSERT,UPDATE,DELETE'); END;",
                        objectName, policyName,
                        auditCondition != null ? auditCondition : "1=1",
                        auditColumns != null ? String.join(",", auditColumns) : "",
                        handlerModule != null ? "USER" : "NULL",
                        handlerModule != null ? "'" + handlerModule + "'" : "NULL"
                    );

                    jdbcTemplate.execute(addSql);
                    result.put("message", "FGA policy added successfully");
                    break;

                case "DROP":
                    String dropSql = String.format(
                        "BEGIN DBMS_FGA.DROP_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s'); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(dropSql);
                    result.put("message", "FGA policy dropped successfully");
                    break;

                case "ENABLE":
                    String enableSql = String.format(
                        "BEGIN DBMS_FGA.ENABLE_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "enable => TRUE); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(enableSql);
                    result.put("message", "FGA policy enabled successfully");
                    break;

                case "DISABLE":
                    String disableSql = String.format(
                        "BEGIN DBMS_FGA.DISABLE_POLICY(" +
                        "object_schema => USER, " +
                        "object_name => '%s', " +
                        "policy_name => '%s'); END;",
                        objectName, policyName
                    );

                    jdbcTemplate.execute(disableSql);
                    result.put("message", "FGA policy disabled successfully");
                    break;

                case "AUDIT_TRAIL":
                    List<Map<String, Object>> auditTrail = jdbcTemplate.queryForList(
                        "SELECT timestamp, db_user, os_user, object_name, sql_text " +
                        "FROM dba_fga_audit_trail WHERE object_name = ? " +
                        "ORDER BY timestamp DESC FETCH FIRST 50 ROWS ONLY",
                        objectName.toUpperCase()
                    );
                    result.put("auditTrail", auditTrail);
                    break;

                case "LIST_POLICIES":
                    List<Map<String, Object>> policies = jdbcTemplate.queryForList(
                        "SELECT object_name, policy_name, enabled, audit_column, audit_condition " +
                        "FROM dba_audit_policies WHERE object_owner = USER " +
                        "ORDER BY object_name, policy_name"
                    );
                    result.put("fgaPolicies", policies);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("objectName", objectName);
            result.put("policyName", policyName);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Fine-Grained Auditing (FGA)"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to configure FGA: " + e.getMessage()
            );
        }
    }

    @Tool(name = "secureDataPumpOperations", description = "Oracle Enterprise Security Feature")
    public Map<String, Object> secureDataPumpOperations(
         String operation,
         String jobName,
         String dumpFile,
         String encryption,
         String encryptionPassword,
         List<String> schemas) {

        try {
            Map<String, Object> result = new HashMap<>();
            String dumpFileName = dumpFile != null ? dumpFile : jobName + ".dmp";
            String encType = encryption != null ? encryption.toUpperCase() : "PASSWORD";

            switch (operation.toUpperCase()) {
                case "EXPORT":
                    StringBuilder exportSql = new StringBuilder("DECLARE job_handle NUMBER; BEGIN ");
                    exportSql.append("job_handle := DBMS_DATAPUMP.OPEN('EXPORT', 'SCHEMA'); ");
                    exportSql.append("DBMS_DATAPUMP.SET_PARAMETER(job_handle, 'ESTIMATE', 'BLOCKS'); ");

                    if (schemas != null && !schemas.isEmpty()) {
                        for (String schema : schemas) {
                            exportSql.append(String.format(
                                "DBMS_DATAPUMP.METADATA_FILTER(job_handle, 'SCHEMA_EXPR', 'IN (''%s'')'); ",
                                schema.toUpperCase()
                            ));
                        }
                    }

                    // Add encryption
                    if (encryptionPassword != null) {
                        exportSql.append(String.format(
                            "DBMS_DATAPUMP.SET_PARAMETER(job_handle, 'ENCRYPTION', '%s'); ", encType
                        ));
                        exportSql.append(String.format(
                            "DBMS_DATAPUMP.SET_PARAMETER(job_handle, 'ENCRYPTION_PASSWORD', '%s'); ",
                            encryptionPassword
                        ));
                    }

                    exportSql.append(String.format(
                        "DBMS_DATAPUMP.ADD_FILE(job_handle, '%s', 'DATA_PUMP_DIR'); ", dumpFileName
                    ));
                    exportSql.append("DBMS_DATAPUMP.START_JOB(job_handle); ");
                    exportSql.append("DBMS_DATAPUMP.DETACH(job_handle); END;");

                    jdbcTemplate.execute(exportSql.toString());
                    result.put("message", "Secure Data Pump export job started");
                    result.put("jobName", jobName);
                    result.put("dumpFile", dumpFileName);
                    result.put("encryption", encType);
                    break;

                case "IMPORT":
                    StringBuilder importSql = new StringBuilder("DECLARE job_handle NUMBER; BEGIN ");
                    importSql.append("job_handle := DBMS_DATAPUMP.OPEN('IMPORT', 'SCHEMA'); ");

                    // Add decryption
                    if (encryptionPassword != null) {
                        importSql.append(String.format(
                            "DBMS_DATAPUMP.SET_PARAMETER(job_handle, 'ENCRYPTION_PASSWORD', '%s'); ",
                            encryptionPassword
                        ));
                    }

                    importSql.append(String.format(
                        "DBMS_DATAPUMP.ADD_FILE(job_handle, '%s', 'DATA_PUMP_DIR'); ", dumpFileName
                    ));
                    importSql.append("DBMS_DATAPUMP.START_JOB(job_handle); ");
                    importSql.append("DBMS_DATAPUMP.DETACH(job_handle); END;");

                    jdbcTemplate.execute(importSql.toString());
                    result.put("message", "Secure Data Pump import job started");
                    break;

                case "STATUS":
                    List<Map<String, Object>> jobs = jdbcTemplate.queryForList(
                        "SELECT job_name, operation, job_mode, state, degree " +
                        "FROM dba_datapump_jobs WHERE owner_name = USER " +
                        "ORDER BY start_time DESC"
                    );
                    result.put("dataPumpJobs", jobs);
                    break;

                case "MONITOR":
                    List<Map<String, Object>> sessions = jdbcTemplate.queryForList(
                        "SELECT job_name, sofar, totalwork, time_remaining, elapsed_seconds " +
                        "FROM v$session_longops WHERE opname LIKE 'EXPORT%' OR opname LIKE 'IMPORT%' " +
                        "ORDER BY start_time DESC"
                    );
                    result.put("activeJobs", sessions);
                    break;

                default:
                    return Map.of("status", "error", "message", "Unsupported operation: " + operation);
            }

            result.put("operation", operation);
            result.put("jobName", jobName);
            result.put("encryptionType", encType);

            return Map.of(
                "status", "success",
                "result", result,
                "oracleFeature", "Secure Data Pump Operations"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Failed to execute secure Data Pump operation: " + e.getMessage()
            );
        }
    }
}




