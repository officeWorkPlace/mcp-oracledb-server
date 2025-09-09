package com.deepai.mcpserver.util;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Oracle SQL Builder - Dynamic SQL Generation Utility
 * 
 * Provides Oracle-specific SQL generation for various database operations
 * with support for different Oracle versions and safety checks.
 * 
 * Features:
 * - Multi-version Oracle SQL compatibility
 * - Safety checks for system objects
 * - Dynamic DDL generation
 * - PDB-specific operations (12c+)
 * - Enterprise security integration
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@Component
public class OracleSqlBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OracleSqlBuilder.class);
    
    // System databases that should never be dropped
    private static final Set<String> SYSTEM_DATABASES = Set.of(
        "SYSTEM", "SYSAUX", "TEMP", "USERS", "EXAMPLE", "APEX", "HR", "OE", "PM", "IX", "SH", "BI"
    );
    
    // System users that should be protected
    private static final Set<String> SYSTEM_USERS = Set.of(
        "SYS", "SYSTEM", "SYSAUX", "DBSNMP", "SYSMAN", "OUTLN", "DIP", "ORACLE_OCM", "APPQOSSYS"
    );

    /**
     * Build CREATE USER SQL with Oracle-specific features
     */
    public String buildCreateUserSql(String username, String password, String tablespace, 
                                   String tempTablespace, String profile) {
        
        validateUsername(username);
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE USER ").append(escapeIdentifier(username));
        sql.append(" IDENTIFIED BY ").append(escapePassword(password));
        
        if (tablespace != null && !tablespace.trim().isEmpty()) {
            sql.append(" DEFAULT TABLESPACE ").append(escapeIdentifier(tablespace));
        }
        
        if (tempTablespace != null && !tempTablespace.trim().isEmpty()) {
            sql.append(" TEMPORARY TABLESPACE ").append(escapeIdentifier(tempTablespace));
        }
        
        if (profile != null && !profile.trim().isEmpty()) {
            sql.append(" PROFILE ").append(escapeIdentifier(profile));
        }
        
        // Add default quota
        sql.append(" QUOTA UNLIMITED ON ").append(tablespace != null ? escapeIdentifier(tablespace) : "USERS");
        
        logger.debug("Generated CREATE USER SQL: {}", sql.toString());
        return sql.toString();
    }

    /**
     * Build CREATE DATABASE SQL for traditional Oracle database
     */
    public String buildCreateDatabaseSql(String dbName, String adminUser, String adminPassword) {
        validateDatabaseName(dbName);
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE DATABASE ").append(escapeIdentifier(dbName)).append("\n");
        sql.append("  USER ").append(adminUser != null ? escapeIdentifier(adminUser) : "SYS");
        sql.append(" IDENTIFIED BY ").append(escapePassword(adminPassword != null ? adminPassword : "password"));
        sql.append("\n");
        sql.append("  LOGFILE GROUP 1 ('").append(dbName).append("_redo01.log') SIZE 100M,\n");
        sql.append("          GROUP 2 ('").append(dbName).append("_redo02.log') SIZE 100M\n");
        sql.append("  CHARACTER SET AL32UTF8\n");
        sql.append("  NATIONAL CHARACTER SET AL16UTF16\n");
        sql.append("  DATAFILE '").append(dbName).append("_system01.dbf' SIZE 500M AUTOEXTEND ON\n");
        sql.append("  SYSAUX DATAFILE '").append(dbName).append("_sysaux01.dbf' SIZE 500M AUTOEXTEND ON\n");
        sql.append("  DEFAULT TABLESPACE users\n");
        sql.append("    DATAFILE '").append(dbName).append("_users01.dbf' SIZE 500M AUTOEXTEND ON\n");
        sql.append("  DEFAULT TEMPORARY TABLESPACE temp\n");
        sql.append("    TEMPFILE '").append(dbName).append("_temp01.dbf' SIZE 100M AUTOEXTEND ON\n");
        sql.append("  UNDO TABLESPACE undotbs1\n");
        sql.append("    DATAFILE '").append(dbName).append("_undo01.dbf' SIZE 200M AUTOEXTEND ON");
        
        logger.debug("Generated CREATE DATABASE SQL for: {}", dbName);
        return sql.toString();
    }

    /**
     * Build CREATE PLUGGABLE DATABASE SQL (12c+)
     */
    public String buildCreatePdbSql(String pdbName, String adminUser, String adminPassword) {
        validateDatabaseName(pdbName);
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE PLUGGABLE DATABASE ").append(escapeIdentifier(pdbName)).append("\n");
        
        if (adminUser != null && !adminUser.trim().isEmpty()) {
            sql.append("  ADMIN USER ").append(escapeIdentifier(adminUser));
            sql.append(" IDENTIFIED BY ").append(escapePassword(adminPassword != null ? adminPassword : "password"));
            sql.append("\n");
        }
        
        sql.append("  STORAGE (MAXSIZE 2G)\n");
        sql.append("  DEFAULT TABLESPACE users\n");
        sql.append("    DATAFILE SIZE 100M AUTOEXTEND ON\n");
        sql.append("  FILE_NAME_CONVERT = ('pdbseed', '").append(pdbName.toLowerCase()).append("')");
        
        logger.debug("Generated CREATE PLUGGABLE DATABASE SQL for: {}", pdbName);
        return sql.toString();
    }

    /**
     * Build DROP DATABASE SQL with safety checks
     */
    public String buildDropDatabaseSql(String dbName, Boolean force) {
        validateDatabaseName(dbName);
        
        if (isSystemDatabase(dbName)) {
            throw new IllegalArgumentException("Cannot drop system database: " + dbName);
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("DROP DATABASE ").append(escapeIdentifier(dbName));
        
        if (force != null && force) {
            sql.append(" INCLUDING DATAFILES");
        }
        
        logger.debug("Generated DROP DATABASE SQL for: {}", dbName);
        return sql.toString();
    }

    /**
     * Build DROP PLUGGABLE DATABASE SQL (12c+)
     */
    public String buildDropPdbSql(String pdbName, Boolean force) {
        validateDatabaseName(pdbName);
        
        StringBuilder sql = new StringBuilder();
        sql.append("DROP PLUGGABLE DATABASE ").append(escapeIdentifier(pdbName));
        
        if (force != null && force) {
            sql.append(" INCLUDING DATAFILES");
        }
        
        logger.debug("Generated DROP PLUGGABLE DATABASE SQL for: {}", pdbName);
        return sql.toString();
    }

    /**
     * Build CREATE TABLE SQL with Oracle-specific features
     */
    public String buildCreateTableSql(String tableName, List<Map<String, Object>> columns, 
                                    List<String> primaryKey, String tablespace) {
        
        validateTableName(tableName);
        
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one column");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(escapeIdentifier(tableName)).append(" (\n");
        
        // Add columns
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> column = columns.get(i);
            sql.append("  ").append(buildColumnDefinition(column));
            if (i < columns.size() - 1 || (primaryKey != null && !primaryKey.isEmpty())) {
                sql.append(",");
            }
            sql.append("\n");
        }
        
        // Add primary key constraint if provided
        if (primaryKey != null && !primaryKey.isEmpty()) {
            sql.append("  CONSTRAINT ").append(escapeIdentifier(tableName + "_pk"))
               .append(" PRIMARY KEY (");
            for (int i = 0; i < primaryKey.size(); i++) {
                sql.append(escapeIdentifier(primaryKey.get(i)));
                if (i < primaryKey.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")\n");
        }
        
        sql.append(")");
        
        // Add tablespace if specified
        if (tablespace != null && !tablespace.trim().isEmpty()) {
            sql.append("\nTABLESPACE ").append(escapeIdentifier(tablespace));
        }
        
        logger.debug("Generated CREATE TABLE SQL for: {}", tableName);
        return sql.toString();
    }

    /**
     * Build column definition for CREATE TABLE
     */
    private String buildColumnDefinition(Map<String, Object> column) {
        StringBuilder def = new StringBuilder();
        
        String name = (String) column.get("name");
        String type = (String) column.get("type");
        Integer length = (Integer) column.get("length");
        Integer precision = (Integer) column.get("precision");
        Integer scale = (Integer) column.get("scale");
        Boolean nullable = (Boolean) column.get("nullable");
        Object defaultValue = column.get("defaultValue");
        
        // Validate required fields
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name is required");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Column type is required for column: " + name);
        }
        
        def.append(escapeIdentifier(name)).append(" ").append(type.toUpperCase());
        
        // Add length/precision/scale
        if (Arrays.asList("VARCHAR2", "CHAR", "NVARCHAR2", "NCHAR").contains(type.toUpperCase()) && length != null) {
            def.append("(").append(length).append(")");
        } else if ("NUMBER".equals(type.toUpperCase()) && precision != null) {
            def.append("(").append(precision);
            if (scale != null) {
                def.append(",").append(scale);
            }
            def.append(")");
        }
        
        // Add default value
        if (defaultValue != null) {
            def.append(" DEFAULT ").append(defaultValue);
        }
        
        // Add NOT NULL constraint
        if (nullable != null && !nullable) {
            def.append(" NOT NULL");
        }
        
        return def.toString();
    }

    /**
     * Check if database name is a system database
     */
    public boolean isSystemDatabase(String dbName) {
        return SYSTEM_DATABASES.contains(dbName.toUpperCase());
    }

    /**
     * Check if username is a system user
     */
    public boolean isSystemUser(String username) {
        return SYSTEM_USERS.contains(username.toUpperCase());
    }

    /**
     * Escape Oracle identifiers to prevent SQL injection
     */
    private String escapeIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        
        // Remove dangerous characters and wrap in quotes if needed
        String cleaned = identifier.replaceAll("[^a-zA-Z0-9_$]", "");
        
        // Check if identifier needs quoting (starts with number, contains special chars, or is reserved)
        if (needsQuoting(cleaned)) {
            return "\"" + cleaned + "\"";
        }
        
        return cleaned;
    }

    /**
     * Escape password for SQL generation
     */
    private String escapePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Wrap password in quotes and escape internal quotes
        return "\"" + password.replace("\"", "\"\"") + "\"";
    }

    /**
     * Validate database name
     */
    private void validateDatabaseName(String dbName) {
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        
        if (dbName.length() > 30) {
            throw new IllegalArgumentException("Database name cannot exceed 30 characters");
        }
        
        if (!dbName.matches("^[a-zA-Z][a-zA-Z0-9_$]*$")) {
            throw new IllegalArgumentException("Invalid database name format: " + dbName);
        }
    }

    /**
     * Validate username
     */
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (isSystemUser(username)) {
            throw new IllegalArgumentException("Cannot modify system user: " + username);
        }
        
        if (username.length() > 30) {
            throw new IllegalArgumentException("Username cannot exceed 30 characters");
        }
    }

    /**
     * Validate table name
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        if (tableName.length() > 30) {
            throw new IllegalArgumentException("Table name cannot exceed 30 characters");
        }
    }

    /**
     * Check if identifier needs quoting
     */
    private boolean needsQuoting(String identifier) {
        return !identifier.matches("^[a-zA-Z][a-zA-Z0-9_$]*$") || 
               Character.isDigit(identifier.charAt(0));
    }
    
    /**
     * Build RMAN backup script
     */
    public String buildRmanBackupScript(String backupType, String backupLocation) {
        StringBuilder script = new StringBuilder();
        script.append("RUN {\n");
        
        if ("full".equalsIgnoreCase(backupType)) {
            script.append("  BACKUP DATABASE");
        } else if ("incremental".equalsIgnoreCase(backupType)) {
            script.append("  BACKUP INCREMENTAL LEVEL 1 DATABASE");
        } else {
            script.append("  BACKUP DATABASE");
        }
        
        if (backupLocation != null && !backupLocation.trim().isEmpty()) {
            script.append(" FORMAT '").append(backupLocation).append("/backup_%d_%T_%s_%p.bkp'");
        }
        
        script.append(";\n  SQL 'ALTER SYSTEM ARCHIVE LOG CURRENT';\n}");
        
        logger.debug("Generated RMAN backup script for type: {}", backupType);
        return script.toString();
    }
    
    /**
     * Build CREATE PROFILE SQL
     */
    public String buildCreateProfileSql(String profileName, Map<String, Object> parameters) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE PROFILE ").append(escapeIdentifier(profileName)).append(" LIMIT");
        
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                sql.append("\n  ").append(entry.getKey().toUpperCase()).append(" ").append(entry.getValue());
            }
        } else {
            // Default profile limits
            sql.append("\n  SESSIONS_PER_USER UNLIMITED")
               .append("\n  CPU_PER_SESSION UNLIMITED")
               .append("\n  CPU_PER_CALL UNLIMITED")
               .append("\n  CONNECT_TIME UNLIMITED")
               .append("\n  IDLE_TIME UNLIMITED")
               .append("\n  LOGICAL_READS_PER_SESSION UNLIMITED")
               .append("\n  LOGICAL_READS_PER_CALL UNLIMITED");
        }
        
        logger.debug("Generated CREATE PROFILE SQL for: {}", profileName);
        return sql.toString();
    }
    
    /**
     * Build ALTER PROFILE SQL
     */
    public String buildAlterProfileSql(String profileName, Map<String, Object> parameters) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER PROFILE ").append(escapeIdentifier(profileName)).append(" LIMIT");
        
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                sql.append("\n  ").append(entry.getKey().toUpperCase()).append(" ").append(entry.getValue());
            }
        }
        
        logger.debug("Generated ALTER PROFILE SQL for: {}", profileName);
        return sql.toString();
    }
    
    /**
     * Build INSERT SQL
     */
//    public String buildInsertSql(String tableName, Map<String, Object> data) {
//        validateTableName(tableName);
//        
//        StringBuilder sql = new StringBuilder();
//        sql.append("INSERT INTO ").append(escapeIdentifier(tableName)).append(" (");
//        
//        // Build column list
//        String columns = String.join(", ", 
//            data.keySet().stream().map(this::escapeIdentifier).toArray(String[]::new));
//        sql.append(columns).append(") VALUES (");
//        
//        // Build values list
//        String values = String.join(", ", 
//            data.values().stream().map(v -> v == null ? "NULL" : "'" + v.toString().replace("'", "''") + "'").toArray(String[]::new));
//        sql.append(values).append(")");
//        
//        logger.debug("Generated INSERT SQL for table: {}", tableName);
//        return sql.toString();
//    }
    
    public String buildInsertSql(String tableName, Map<String, Object> data) {
        validateTableName(tableName);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(escapeIdentifier(tableName)).append(" (");

        // Build column list
        String columns = String.join(", ",
            data.keySet().stream().map(this::escapeIdentifier).toArray(String[]::new));
        sql.append(columns).append(") VALUES (");

        // Build values list with proper Oracle type handling
        String values = String.join(", ",
            data.values().stream().map(this::formatValueForOracle).toArray(String[]::new));
        sql.append(values).append(")");

        logger.debug("Generated INSERT SQL for table: {}", tableName);
        return sql.toString();
    }
    
    private String formatValueForOracle(Object value) {
        if (value == null) {
            return "NULL";
        }

        // Handle numeric types (don't quote)
        if (value instanceof Number) {
            return value.toString();
        }

        // Handle boolean (Oracle doesn't have native boolean, convert to number)
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }

        // Handle Java Date/Time objects
        if (value instanceof java.sql.Date) {
            return "DATE '" + value.toString() + "'";
        }

        if (value instanceof java.sql.Timestamp) {
            return "TIMESTAMP '" + value.toString() + "'";
        }

        if (value instanceof java.time.LocalDate) {
            return "DATE '" + value.toString() + "'";
        }

        if (value instanceof java.time.LocalDateTime) {
            // Oracle 12c+ supports ISO 8601 format
            return "TIMESTAMP '" + value.toString().replace('T', ' ') + "'";
        }

        if (value instanceof java.util.Date) {
            java.sql.Timestamp ts = new java.sql.Timestamp(((java.util.Date) value).getTime());
            return "TIMESTAMP '" + ts.toString() + "'";
        }

        // Handle string values
        String stringValue = value.toString();

        // Check if string represents a date (common patterns)
        if (isDateString(stringValue)) {
            return formatDateString(stringValue);
        }

        // Check if string represents a number
        if (isNumericString(stringValue)) {
            return stringValue; // Don't quote numeric strings
        }

        // Check if it's a JSON/XML that should use Oracle 12c+ features
        if (isJsonString(stringValue)) {
            // Oracle 12c+ JSON support - escape and quote
            return "'" + stringValue.replace("'", "''") + "'";
        }

        // Regular string - escape single quotes
        return "'" + stringValue.replace("'", "''") + "'";
    }
    
    private boolean isDateString(String value) {
        // ISO 8601 date patterns
        return value.matches("\\d{4}-\\d{2}-\\d{2}") ||                          // YYYY-MM-DD
               value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}") ||     // YYYY-MM-DDTHH:mm:ss
               value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") ||     // YYYY-MM-DD HH:mm:ss
               value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+") || // with milliseconds
               value.matches("\\d{2}-[A-Z]{3}-\\d{4}") ||                        // DD-MON-YYYY
               value.matches("\\d{2}/\\d{2}/\\d{4}");                           // DD/MM/YYYY or MM/DD/YYYY
    }
    
    private String formatDateString(String dateStr) {
        try {
            // ISO 8601 patterns (Oracle 12c+ supports these directly)
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return "DATE '" + dateStr + "'";
            }

            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                return "TIMESTAMP '" + dateStr.replace('T', ' ') + "'";
            }

            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                return "TIMESTAMP '" + dateStr + "'";
            }

            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")) {
                return "TIMESTAMP '" + dateStr.replace('T', ' ') + "'";
            }

            // Traditional Oracle formats
            if (dateStr.matches("\\d{2}-[A-Z]{3}-\\d{4}")) {
                return "TO_DATE('" + dateStr + "', 'DD-MON-YYYY')";
            }

            if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return "TO_DATE('" + dateStr + "', 'DD/MM/YYYY')";
            }

            // If no specific pattern matches, treat as string
            return "'" + dateStr.replace("'", "''") + "'";

        } catch (Exception e) {
            // If any error, treat as regular string
            return "'" + dateStr.replace("'", "''") + "'";
        }
    }

    /**
    * Check if string represents a number
    */
    private boolean isNumericString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            // Try to parse as different numeric types
            if (value.contains(".")) {
                Double.parseDouble(value);
            } else {
                Long.parseLong(value);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
    * Check if string is JSON (for Oracle 12c+ JSON support)
    */
    private boolean isJsonString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmed = value.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    /**
     * Build UPDATE SQL
     */
//    public String buildUpdateSql(String tableName, Map<String, Object> data, String whereClause) {
//        validateTableName(tableName);
//        
//        StringBuilder sql = new StringBuilder();
//        sql.append("UPDATE ").append(escapeIdentifier(tableName)).append(" SET ");
//        
//        // Build SET clause
//        String[] setParts = data.entrySet().stream()
//            .map(entry -> escapeIdentifier(entry.getKey()) + " = " + 
//                 (entry.getValue() == null ? "NULL" : "'" + entry.getValue().toString().replace("'", "''") + "'"))
//            .toArray(String[]::new);
//        sql.append(String.join(", ", setParts));
//        
//        // Add WHERE clause
//        if (whereClause != null && !whereClause.trim().isEmpty()) {
//            sql.append(" WHERE ").append(whereClause);
//        }
//        
//        logger.debug("Generated UPDATE SQL for table: {}", tableName);
//        return sql.toString();
//    }
    
    public String buildUpdateSql(String tableName, Map<String, Object> data, String whereClause) {
        validateTableName(tableName);

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(escapeIdentifier(tableName)).append(" SET ");

        // Build SET clause with proper Oracle type handling
        String[] setParts = data.entrySet().stream()
            .map(entry -> escapeIdentifier(entry.getKey()) + " = " + formatValueForOracle(entry.getValue()))
            .toArray(String[]::new);
        sql.append(String.join(", ", setParts));

        // Add WHERE clause with validation
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        } else {
            logger.warn("UPDATE operation without WHERE clause on table: {}", tableName);
            // Optionally throw exception for safety
            // throw new IllegalArgumentException("WHERE clause is required for UPDATE operations");
        }

        logger.debug("Generated UPDATE SQL for table: {}", tableName);
        return sql.toString();
    }
}
