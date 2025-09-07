package com.deepai.mcpserver.service;

import com.deepai.mcpserver.response.StandardResponse;
import com.deepai.mcpserver.response.StandardResponse.ResponseDetails;
import com.deepai.mcpserver.response.StandardResponse.VersionInfo;
import com.deepai.mcpserver.response.StandardResponse.FeatureInfo;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to translate technical Oracle SQL errors into user-friendly messages
 * Based on comprehensive error analysis from COMPREHENSIVE_ERROR_ANALYSIS.md
 * 
 * Handles common Oracle errors like:
 * - ORA-65096: Invalid common user or role name
 * - ORA-01031: Insufficient privileges  
 * - ORA-20000: Unable to analyze table
 * - ORA-28068: Data redaction policy errors
 * - ORA-65046: PDB operation restrictions
 * 
 * @author Oracle MCP Server Team
 * @version 2.0.0
 */
@Service
public class OracleErrorMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(OracleErrorMessageService.class);
    
    // Pattern to extract Oracle error codes
    private static final Pattern ORA_ERROR_PATTERN = Pattern.compile("ORA-(\\d{5}): (.*)");
    
    /**
     * Convert a technical Oracle exception into a user-friendly StandardResponse
     */
    public StandardResponse translateException(Exception ex, String context) {
        logger.debug("Translating Oracle exception: {} in context: {}", ex.getMessage(), context);
        
        String errorMessage = ex.getMessage();
        Matcher matcher = ORA_ERROR_PATTERN.matcher(errorMessage);
        
        if (matcher.find()) {
            String errorCode = matcher.group(1);
            String errorDescription = matcher.group(2);
            
            return translateOracleError(errorCode, errorDescription, context, errorMessage);
        }
        
        // Handle non-Oracle specific errors
        return handleGenericError(ex, context);
    }
    
    /**
     * Translate specific Oracle error codes to user-friendly messages
     */
    private StandardResponse translateOracleError(String errorCode, String description, String context, String fullError) {
        
        switch (errorCode) {
            case "65096":
                return handleInvalidUserNameError(context, fullError);
            
            case "01031":
                return handleInsufficientPrivilegesError(context, fullError);
                
            case "20000":
                return handleAnalyzeTableError(context, fullError);
                
            case "28068":
                return handleDataRedactionError(context, fullError);
                
            case "65046":
                return handlePDBOperationError(context, fullError);
                
            case "01408":
                return handleIndexExistsError(context, fullError);
                
            case "00932":
                return handleDatatypeMismatchError(context, fullError);
                
            case "00942":
                return handleTableNotFoundError(context, fullError);
                
            case "01017":
                return handleInvalidUsernamePasswordError(context, fullError);
                
            case "28000":
                return handleAccountLockedError(context, fullError);
                
            default:
                return handleUnknownOracleError(errorCode, description, context, fullError);
        }
    }
    
    private StandardResponse handleInvalidUserNameError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Database requires common user names to start with 'C##' prefix")
            .impact("User creation failed due to naming convention requirements")
            .currentState("Pluggable Database (PDB) environment detected")
            .suggestions(List.of(
                "Try using a username like 'C##TEST_USER' instead",
                "Ensure username starts with 'C##' for common users",
                "Use regular usernames only when connected directly to PDB"
            ))
            .technicalError("ORA-65096")
            .supportInfo("Contact database administrator if issue persists")
            .configurationGuide(Map.of(
                "example", "CREATE USER C##MYUSER IDENTIFIED BY password",
                "documentation", "Oracle Database Administrator's Guide - Managing Common Users"
            ))
            .build();
            
        return StandardResponse.error(
            "Unable to create user. The username format is invalid for this database configuration.",
            details
        );
    }
    
    private StandardResponse handleInsufficientPrivilegesError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Current database user lacks required privileges for this operation")
            .impact("Operation cannot be completed with current permissions")
            .currentState("Limited privileges detected")
            .suggestions(List.of(
                "Contact database administrator to grant required privileges",
                "Connect with a user that has DBA or SYSDBA privileges",
                "Use alternative operations that don't require elevated privileges"
            ))
            .technicalError("ORA-01031")
            .supportInfo("Database administrator can grant privileges using: GRANT [privilege] TO [username]")
            .securityImpact("Some database operations require elevated privileges for security")
            .build();
            
        return StandardResponse.featureUnavailable(
            "Operation requires database privileges not available to current user",
            details
        );
    }
    
    private StandardResponse handleAnalyzeTableError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Unable to gather table statistics due to permission restrictions")
            .impact("Query optimization may be affected by missing statistics")
            .alternatives(List.of(
                "Run ANALYZE TABLE with appropriate privileges",
                "Use DBMS_STATS package for comprehensive analysis",
                "Query optimizer will use dynamic sampling"
            ))
            .technicalError("ORA-20000")
            .performanceNote("Missing statistics may impact query performance")
            .build();
            
        return StandardResponse.warning(
            "Table analysis completed with limitations due to insufficient privileges",
            details
        );
    }
    
    private StandardResponse handleDataRedactionError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Oracle Advanced Security Option required for data redaction policies")
            .impact("Data masking features are not available")
            .versionInfo(VersionInfo.builder()
                .currentLicense("Standard Edition or Enterprise without Advanced Security")
                .requiredLicense("Enterprise Edition with Oracle Advanced Security Option")
                .upgradeInfo("Contact Oracle sales for Advanced Security licensing")
                .build())
            .alternatives(List.of(
                "Implement application-level data masking",
                "Use database views to limit sensitive data exposure",
                "Apply column-level security through privileges"
            ))
            .technicalError("ORA-28068")
            .securityImpact("Data protection will need to be implemented at application level")
            .build();
            
        return StandardResponse.featureUnavailable(
            "Data redaction policies are not available in current Oracle configuration",
            details
        );
    }
    
    private StandardResponse handlePDBOperationError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Operation requires connection to Container Database (CDB) root")
            .impact("Some database management operations are restricted from PDB level")
            .currentState("Connected to Pluggable Database (PDB)")
            .requiredState("Connection to Container Database (CDB) root")
            .suggestions(List.of(
                "Connect to CDB root: ALTER SESSION SET CONTAINER = CDB$ROOT",
                "Use PDB-specific alternatives where available",
                "Contact DBA for container-level operations"
            ))
            .technicalError("ORA-65046")
            .build();
            
        return StandardResponse.configurationLimitation(
            "Operation not allowed from Pluggable Database context",
            details
        );
    }
    
    private StandardResponse handleIndexExistsError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("An index with the same column structure already exists")
            .impact("Database optimization may already be in place")
            .currentState("Index already exists on specified columns")
            .alternatives(List.of(
                "Use existing index for query optimization",
                "Create index with different name or column order",
                "Check existing indexes: SELECT * FROM USER_INDEXES"
            ))
            .technicalError("ORA-01408")
            .performanceNote("Existing index provides the same optimization benefits")
            .build();
            
        return StandardResponse.info(
            "Index creation skipped - equivalent index already exists",
            details
        );
    }
    
    private StandardResponse handleDatatypeMismatchError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Data types in query are incompatible for the requested operation")
            .impact("Query cannot be executed due to type conflicts")
            .suggestions(List.of(
                "Check column data types: DESC table_name",
                "Use appropriate data type conversion functions",
                "Verify WHERE clause uses compatible data types"
            ))
            .technicalError("ORA-00932")
            .configurationGuide(Map.of(
                "conversion_functions", List.of("TO_CHAR()", "TO_NUMBER()", "TO_DATE()"),
                "example", "WHERE TO_CHAR(numeric_column) = 'value'"
            ))
            .build();
            
        return StandardResponse.error(
            "Query failed due to incompatible data types",
            details
        );
    }
    
    private StandardResponse handleTableNotFoundError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Specified table or view does not exist or is not accessible")
            .impact("Query cannot be executed against non-existent object")
            .suggestions(List.of(
                "Verify table name spelling and case sensitivity",
                "Check if table exists: SELECT * FROM USER_TABLES WHERE TABLE_NAME = 'name'",
                "Ensure you have access permissions to the table",
                "Check if table is in a different schema"
            ))
            .technicalError("ORA-00942")
            .configurationGuide(Map.of(
                "check_tables", "SELECT table_name FROM all_tables WHERE owner = 'schema'",
                "check_views", "SELECT view_name FROM all_views WHERE owner = 'schema'"
            ))
            .build();
            
        return StandardResponse.error(
            "Table or view not found or not accessible",
            details
        );
    }
    
    private StandardResponse handleInvalidUsernamePasswordError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("Invalid username or password provided")
            .impact("Database connection cannot be established")
            .securityImpact("Multiple failed login attempts may result in account lockout")
            .suggestions(List.of(
                "Verify username and password credentials",
                "Check for case sensitivity in username",
                "Contact database administrator if credentials are correct"
            ))
            .technicalError("ORA-01017")
            .supportInfo("Contact system administrator to verify account status")
            .build();
            
        return StandardResponse.error(
            "Authentication failed - invalid username or password",
            details
        );
    }
    
    private StandardResponse handleAccountLockedError(String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("User account has been locked due to security policy")
            .impact("Cannot connect to database until account is unlocked")
            .securityImpact("Account locked as security measure after failed login attempts")
            .suggestions(List.of(
                "Contact database administrator to unlock account",
                "Wait for automatic unlock if temporary lock policy is in effect"
            ))
            .technicalError("ORA-28000")
            .supportInfo("DBA can unlock with: ALTER USER username ACCOUNT UNLOCK")
            .build();
            
        return StandardResponse.error(
            "Database account is locked",
            details
        );
    }
    
    private StandardResponse handleUnknownOracleError(String errorCode, String description, String context, String fullError) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("An Oracle database error occurred")
            .technicalError("ORA-" + errorCode + ": " + description)
            .supportInfo("Consult Oracle documentation for error code ORA-" + errorCode)
            .configurationGuide(Map.of(
                "documentation_url", "https://docs.oracle.com/error-help/db/ora-" + errorCode + "/",
                "search_tip", "Search Oracle support for ORA-" + errorCode
            ))
            .build();
            
        return StandardResponse.error(
            "Database operation failed due to Oracle error",
            details
        );
    }
    
    private StandardResponse handleGenericError(Exception ex, String context) {
        ResponseDetails details = ResponseDetails.builder()
            .reason("An unexpected error occurred during database operation")
            .technicalError(ex.getClass().getSimpleName() + ": " + ex.getMessage())
            .suggestions(List.of(
                "Check database connectivity",
                "Verify operation parameters",
                "Review application logs for more details"
            ))
            .supportInfo("Contact system administrator if error persists")
            .build();
            
        return StandardResponse.error(
            "Database operation failed",
            details
        );
    }
    
    /**
     * Create version requirement error for Oracle 23c features
     */
    public StandardResponse createVersionRequirementError(String featureName, String currentVersion) {
        VersionInfo versionInfo = VersionInfo.builder()
            .currentVersion(currentVersion)
            .requiredVersion("Oracle 23c or higher")
            .upgradeInfo("Contact your DBA about Oracle 23c upgrade for " + featureName + " capabilities")
            .build();
            
        ResponseDetails details = ResponseDetails.builder()
            .reason("Feature requires newer Oracle database version")
            .impact("Advanced capabilities not available on current version")
            .versionInfo(versionInfo)
            .alternatives(List.of(
                "Use compatible alternative features available in " + currentVersion,
                "Consider upgrading to Oracle 23c for full functionality"
            ))
            .build();
            
        return StandardResponse.partialSupport(
            featureName + " features are not available on your current Oracle version",
            details
        );
    }
    
    /**
     * Create license requirement error for enterprise features
     */
    public StandardResponse createLicenseRequirementError(String featureName, String currentLicense, String requiredLicense) {
        VersionInfo versionInfo = VersionInfo.builder()
            .currentLicense(currentLicense)
            .requiredLicense(requiredLicense)
            .upgradeInfo("Contact Oracle sales for licensing information")
            .build();
            
        ResponseDetails details = ResponseDetails.builder()
            .reason("Feature requires additional Oracle licensing")
            .impact("Enterprise capabilities not available with current license")
            .versionInfo(versionInfo)
            .alternatives(List.of(
                "Use standard edition compatible alternatives",
                "Implement equivalent functionality at application level"
            ))
            .build();
            
        return StandardResponse.featureUnavailable(
            featureName + " is not available with current Oracle license",
            details
        );
    }
}


