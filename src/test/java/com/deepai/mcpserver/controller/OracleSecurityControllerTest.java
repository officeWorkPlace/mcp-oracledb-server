package com.deepai.mcpserver.controller;

import com.deepai.mcpserver.config.TestSecurityConfig;
import com.deepai.mcpserver.service.OracleEnterpriseSecurityService;
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
 * Comprehensive unit tests for OracleSecurityController
 * Tests all 7 security service endpoints with correct method signatures
 */
@WebMvcTest(OracleSecurityController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Oracle Security Controller Tests")
@SuppressWarnings("deprecation")
class OracleSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OracleEnterpriseSecurityService securityService;

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

    // ========== VPD POLICY TESTS ==========

    @Test
    @DisplayName("Should add VPD policy successfully")
    void shouldAddVpdPolicy() throws Exception {
        when(securityService.manageVpdPolicy("ADD", "EMPLOYEES", "HR_POLICY", "hr_security_func", "SELECT,UPDATE"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ADD",
                    "objectName", "EMPLOYEES",
                    "policyName", "HR_POLICY",
                    "message", "VPD policy added successfully"
                ),
                "oracleFeature", "Virtual Private Database (VPD)"
            ));

        mockMvc.perform(post("/api/oracle/security/vpd/ADD")
                .param("objectName", "EMPLOYEES")
                .param("policyName", "HR_POLICY")
                .param("functionName", "hr_security_func")
                .param("policyType", "SELECT,UPDATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.objectName").value("EMPLOYEES"));
    }

    @Test
    @DisplayName("Should list VPD policies successfully")
    void shouldListVpdPolicies() throws Exception {
        when(securityService.manageVpdPolicy("LIST", "EMPLOYEES", "HR_POLICY", null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "LIST",
                    "objectName", "EMPLOYEES",
                    "policies", List.of(
                        Map.of("policy_name", "HR_POLICY", "function", "hr_security_func", "enabled", "YES")
                    )
                ),
                "oracleFeature", "Virtual Private Database (VPD)"
            ));

        mockMvc.perform(post("/api/oracle/security/vpd/LIST")
                .param("objectName", "EMPLOYEES")
                .param("policyName", "HR_POLICY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("LIST"));
    }

    // ========== DATA REDACTION TESTS ==========

    @Test
    @DisplayName("Should add data redaction policy successfully")
    void shouldAddDataRedactionPolicy() throws Exception {
        when(securityService.configureDataRedaction("ADD", "CUSTOMERS", "SSN", "FULL", "1=1"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ADD",
                    "tableName", "CUSTOMERS",
                    "columnName", "SSN",
                    "redactionType", "FULL",
                    "message", "Data redaction policy added successfully"
                ),
                "oracleFeature", "Data Redaction"
            ));

        mockMvc.perform(post("/api/oracle/security/data-redaction/ADD")
                .param("tableName", "CUSTOMERS")
                .param("columnName", "SSN")
                .param("redactionType", "FULL")
                .param("expression", "1=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.tableName").value("CUSTOMERS"));
    }

    @Test
    @DisplayName("Should check data redaction status successfully")
    void shouldCheckDataRedactionStatus() throws Exception {
        when(securityService.configureDataRedaction("STATUS", "CUSTOMERS", "SSN", null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "STATUS",
                    "tableName", "CUSTOMERS",
                    "policies", List.of(
                        Map.of("object_name", "CUSTOMERS", "column_name", "SSN", "function_type", "FULL")
                    )
                ),
                "oracleFeature", "Data Redaction"
            ));

        mockMvc.perform(post("/api/oracle/security/data-redaction/STATUS")
                .param("tableName", "CUSTOMERS")
                .param("columnName", "SSN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("STATUS"));
    }

    // ========== DATABASE VAULT TESTS ==========

    @Test
    @DisplayName("Should create database vault realm successfully")
    void shouldCreateDatabaseVaultRealm() throws Exception {
        when(securityService.manageDatabaseVault("CREATE_REALM", "HR_REALM", "HR.EMPLOYEES", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE_REALM",
                    "realmName", "HR_REALM",
                    "objectName", "HR.EMPLOYEES",
                    "message", "Database Vault realm created successfully"
                ),
                "oracleFeature", "Oracle Database Vault"
            ));

        mockMvc.perform(post("/api/oracle/security/database-vault/CREATE_REALM")
                .param("realmName", "HR_REALM")
                .param("objectName", "HR.EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.realmName").value("HR_REALM"));
    }

    @Test
    @DisplayName("Should list database vault realms successfully")
    void shouldListDatabaseVaultRealms() throws Exception {
        when(securityService.manageDatabaseVault("LIST_REALMS", null, null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "LIST_REALMS",
                    "realms", List.of(
                        Map.of("name", "HR_REALM", "enabled", "Y", "audit_options", "NONE")
                    )
                ),
                "oracleFeature", "Oracle Database Vault"
            ));

        mockMvc.perform(post("/api/oracle/security/database-vault/LIST_REALMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("LIST_REALMS"));
    }

    // ========== TDE ENCRYPTION TESTS ==========

    @Test
    @DisplayName("Should enable TDE encryption successfully")
    void shouldEnableTdeEncryption() throws Exception {
        when(securityService.manageTdeEncryption("ENABLE", "CUSTOMERS", "TABLE", "AES256"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ENABLE",
                    "objectName", "CUSTOMERS",
                    "objectType", "TABLE",
                    "encryptionAlgorithm", "AES256",
                    "message", "TDE encryption enabled successfully"
                ),
                "oracleFeature", "Transparent Data Encryption (TDE)"
            ));

        mockMvc.perform(post("/api/oracle/security/tde-encryption/ENABLE")
                .param("objectName", "CUSTOMERS")
                .param("objectType", "TABLE")
                .param("encryptionAlgorithm", "AES256"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.encryptionAlgorithm").value("AES256"));
    }

    @Test
    @DisplayName("Should check TDE encryption status successfully")
    void shouldCheckTdeEncryptionStatus() throws Exception {
        when(securityService.manageTdeEncryption("STATUS", "CUSTOMERS", "TABLE", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "STATUS",
                    "objectName", "CUSTOMERS",
                    "objectType", "TABLE",
                    "encryptedObjects", List.of(
                        Map.of("owner", "HR", "table_name", "CUSTOMERS", "encrypted", "YES")
                    )
                ),
                "oracleFeature", "Transparent Data Encryption (TDE)"
            ));

        mockMvc.perform(post("/api/oracle/security/tde-encryption/STATUS")
                .param("objectName", "CUSTOMERS")
                .param("objectType", "TABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("STATUS"));
    }

    // ========== AUDIT POLICIES TESTS ==========

    @Test
    @DisplayName("Should create audit policy successfully")
    void shouldCreateAuditPolicy() throws Exception {
        List<String> auditActions = List.of("SELECT", "INSERT", "UPDATE", "DELETE");
        
        when(securityService.manageAuditPolicies("CREATE", "HR_AUDIT_POLICY", auditActions, "DEPARTMENT = 'HR'"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE",
                    "policyName", "HR_AUDIT_POLICY",
                    "auditActions", auditActions,
                    "message", "Audit policy created successfully"
                ),
                "oracleFeature", "Unified Auditing"
            ));

        mockMvc.perform(post("/api/oracle/security/audit-policies/CREATE")
                .param("policyName", "HR_AUDIT_POLICY")
                .param("auditCondition", "DEPARTMENT = 'HR'")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auditActions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.policyName").value("HR_AUDIT_POLICY"));
    }

    @Test
    @DisplayName("Should enable audit policy successfully")
    void shouldEnableAuditPolicy() throws Exception {
        when(securityService.manageAuditPolicies("ENABLE", "HR_AUDIT_POLICY", null, null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "ENABLE",
                    "policyName", "HR_AUDIT_POLICY",
                    "message", "Audit policy enabled successfully"
                ),
                "oracleFeature", "Unified Auditing"
            ));

        mockMvc.perform(post("/api/oracle/security/audit-policies/ENABLE")
                .param("policyName", "HR_AUDIT_POLICY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.operation").value("ENABLE"));
    }

    // ========== PRIVILEGE ANALYSIS TESTS ==========

    @Test
    @DisplayName("Should start privilege analysis successfully")
    void shouldStartPrivilegeAnalysis() throws Exception {
        when(securityService.analyzePrivilegeUsage("CREATE", "CONTEXT", "HR_ANALYSIS", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "CREATE",
                    "captureType", "CONTEXT",
                    "captureName", "HR_ANALYSIS",
                    "message", "Privilege analysis capture created successfully"
                ),
                "oracleFeature", "Privilege Analysis"
            ));

        mockMvc.perform(post("/api/oracle/security/privilege-analysis/CREATE")
                .param("captureType", "CONTEXT")
                .param("captureName", "HR_ANALYSIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.captureName").value("HR_ANALYSIS"));
    }

    @Test
    @DisplayName("Should generate privilege analysis report successfully")
    void shouldGeneratePrivilegeAnalysisReport() throws Exception {
        when(securityService.analyzePrivilegeUsage("GENERATE_RESULT", null, "HR_ANALYSIS", "HR_RUN_001"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "GENERATE_RESULT",
                    "captureName", "HR_ANALYSIS",
                    "runName", "HR_RUN_001",
                    "findings", List.of(
                        Map.of("username", "HR_USER", "privilege", "SELECT_ANY_TABLE", "used", false)
                    )
                ),
                "oracleFeature", "Privilege Analysis"
            ));

        mockMvc.perform(post("/api/oracle/security/privilege-analysis/GENERATE_RESULT")
                .param("captureName", "HR_ANALYSIS")
                .param("runName", "HR_RUN_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.runName").value("HR_RUN_001"));
    }

    // ========== DATA CLASSIFICATION TESTS ==========

    @Test
    @DisplayName("Should classify schema data successfully")
    void shouldClassifySchemaData() throws Exception {
        when(securityService.classifyDataSensitivity("DISCOVER", "HR", null, "HIGH"))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "DISCOVER",
                    "schemaName", "HR",
                    "sensitivityLevel", "HIGH",
                    "classifiedObjects", 5,
                    "message", "Data sensitivity classification completed successfully"
                ),
                "oracleFeature", "Data Classification"
            ));

        mockMvc.perform(post("/api/oracle/security/data-classification/DISCOVER")
                .param("schemaName", "HR")
                .param("sensitivityLevel", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.schemaName").value("HR"));
    }

    @Test
    @DisplayName("Should get classification report successfully")
    void shouldGetClassificationReport() throws Exception {
        when(securityService.classifyDataSensitivity("REPORT", "HR", "EMPLOYEES", null))
            .thenReturn(Map.of(
                "status", "success",
                "result", Map.of(
                    "operation", "REPORT",
                    "schemaName", "HR",
                    "tableName", "EMPLOYEES",
                    "classifications", List.of(
                        Map.of("column_name", "SSN", "sensitivity_level", "SENSITIVE", "data_type", "PII")
                    )
                ),
                "oracleFeature", "Data Classification"
            ));

        mockMvc.perform(post("/api/oracle/security/data-classification/REPORT")
                .param("schemaName", "HR")
                .param("tableName", "EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.tableName").value("EMPLOYEES"));
    }

    // ========== UTILITY ENDPOINT TESTS ==========

    @Test
    @DisplayName("Should get security capabilities")
    void shouldGetSecurityCapabilities() throws Exception {
        mockMvc.perform(get("/api/oracle/security/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.capabilities").exists())
                .andExpect(jsonPath("$.oracleSecurityFeatures").exists());
    }

    @Test
    @DisplayName("Should perform health check")
    void shouldPerformHealthCheck() throws Exception {
        mockMvc.perform(get("/api/oracle/security/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.securityServicesAvailable").value(true));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should handle VPD policy errors gracefully")
    void shouldHandleVpdPolicyErrors() throws Exception {
        when(securityService.manageVpdPolicy("ADD", "EMPLOYEES", "HR_POLICY", null, null))
            .thenReturn(Map.of(
                "status", "error",
                "message", "Function name required for ADD operation"
            ));

        mockMvc.perform(post("/api/oracle/security/vpd/ADD")
                .param("objectName", "EMPLOYEES")
                .param("policyName", "HR_POLICY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Function name required for ADD operation"));
    }

    @Test
    @DisplayName("Should handle missing required parameters")
    void shouldHandleMissingParameters() throws Exception {
        mockMvc.perform(post("/api/oracle/security/vpd/ADD"))
                .andExpect(status().isBadRequest());
    }
}
