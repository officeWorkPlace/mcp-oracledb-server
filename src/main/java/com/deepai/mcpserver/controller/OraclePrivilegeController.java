package com.deepai.mcpserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.deepai.mcpserver.service.OraclePrivilegeService;

import java.time.Instant;
import java.util.Map;

/**
 * REST Controller for Oracle Privilege Management
 * Provides endpoints to check user privileges and determine available operations
 * 
 * @author Oracle MCP Server Team
 * @version 2.0.0-PRIVILEGE-AWARE
 */
@RestController
@RequestMapping("/api/oracle/privileges")
@CrossOrigin(origins = "*")
public class OraclePrivilegeController {

    private final OraclePrivilegeService oraclePrivilegeService;

    @Autowired
    public OraclePrivilegeController(OraclePrivilegeService oraclePrivilegeService) {
        this.oraclePrivilegeService = oraclePrivilegeService;
    }

    /**
     * Get comprehensive privilege information for current user
     * Shows system privileges, roles, object privileges, and available operations
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkUserPrivileges() {
        Map<String, Object> result = oraclePrivilegeService.getUserPrivileges();
        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed system privileges for current user
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemPrivileges() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> systemPrivs = (Map<String, Object>) privilegeInfo.get("systemPrivileges");
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "systemPrivileges", systemPrivs,
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get system privileges: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Get role privileges for current user
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getRolePrivileges() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rolePrivs = (Map<String, Object>) privilegeInfo.get("rolePrivileges");
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "rolePrivileges", rolePrivs,
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get role privileges: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Get object privileges for current user
     */
    @GetMapping("/objects")
    public ResponseEntity<Map<String, Object>> getObjectPrivileges() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> objectPrivs = (Map<String, Object>) privilegeInfo.get("objectPrivileges");
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "objectPrivileges", objectPrivs,
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get object privileges: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Get available operations based on user privileges
     */
    @GetMapping("/operations")
    public ResponseEntity<Map<String, Object>> getAvailableOperations() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> operations = (Map<String, Object>) privilegeInfo.get("availableOperations");
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "privilegeLevel", privilegeInfo.get("privilegeLevel"),
                    "availableOperations", operations,
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get available operations: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Get privilege level assessment for current user
     */
    @GetMapping("/level")
    public ResponseEntity<Map<String, Object>> getPrivilegeLevel() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "privilegeLevel", privilegeInfo.get("privilegeLevel"),
                    "description", getPrivilegeLevelDescription((String) privilegeInfo.get("privilegeLevel")),
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to assess privilege level: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Check if specific Oracle feature is available for current user
     */
    @GetMapping("/features/{featureName}")
    public ResponseEntity<Map<String, Object>> checkFeatureAvailability(@PathVariable String featureName) {
        Map<String, Object> result = oraclePrivilegeService.checkFeatureAvailability(featureName);
        return ResponseEntity.ok(result);
    }

    /**
     * Get comprehensive privilege summary with recommendations
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPrivilegeSummary() {
        try {
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            if ("success".equals(privilegeInfo.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> operations = (Map<String, Object>) privilegeInfo.get("availableOperations");
                @SuppressWarnings("unchecked")
                Map<String, Object> summary = (Map<String, Object>) operations.get("summary");
                
                String privilegeLevel = (String) privilegeInfo.get("privilegeLevel");
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "currentUser", privilegeInfo.get("currentUser"),
                    "privilegeLevel", privilegeLevel,
                    "levelDescription", getPrivilegeLevelDescription(privilegeLevel),
                    "operationSummary", summary,
                    "recommendations", getPrivilegeRecommendations(privilegeLevel, summary),
                    "timestamp", Instant.now()
                ));
            } else {
                return ResponseEntity.ok(privilegeInfo);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get privilege summary: " + e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Health check for privilege service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Quick privilege check to ensure service is working
            Map<String, Object> privilegeInfo = oraclePrivilegeService.getUserPrivileges();
            
            boolean isHealthy = "success".equals(privilegeInfo.get("status"));
            
            return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "service", "Oracle Privilege Service",
                "connected", isHealthy,
                "currentUser", isHealthy ? privilegeInfo.get("currentUser") : "Unknown",
                "timestamp", Instant.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "DOWN",
                "service", "Oracle Privilege Service",
                "error", e.getMessage(),
                "timestamp", Instant.now()
            ));
        }
    }

    /**
     * Get privilege level description
     */
    private String getPrivilegeLevelDescription(String level) {
        return switch (level) {
            case "DBA" -> "Database Administrator - Full system privileges and administrative capabilities";
            case "ADVANCED" -> "Advanced User - Broad privileges including table creation and system access";
            case "DEVELOPER" -> "Developer - Standard development privileges with CONNECT and RESOURCE roles";
            case "BASIC" -> "Basic User - Connection privileges with limited operational capabilities";
            case "LIMITED" -> "Limited User - Very restricted privileges, mostly read-only access";
            default -> "Unknown privilege level";
        };
    }

    /**
     * Get recommendations based on privilege level and operation summary
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getPrivilegeRecommendations(String level, Map<String, Object> summary) {
        long availableOps = ((Number) summary.get("availableOperations")).longValue();
        long totalOps = ((Number) summary.get("totalOperations")).longValue();
        long accessibilityPercentage = ((Number) summary.get("accessibilityPercentage")).longValue();
        
        Map<String, Object> recommendations = Map.of(
            "currentAccessLevel", accessibilityPercentage + "% of operations available",
            "recommendations", generateRecommendations(level, accessibilityPercentage),
            "missingPrivileges", generateMissingPrivileges(level),
            "nextSteps", generateNextSteps(level, accessibilityPercentage)
        );
        
        return recommendations;
    }

    private java.util.List<String> generateRecommendations(String level, long accessibilityPercentage) {
        return switch (level) {
            case "LIMITED" -> java.util.List.of(
                "Consider requesting CONNECT role for basic database operations",
                "Request SELECT privileges on specific tables you need to access",
                "Ask DBA for appropriate role assignment based on your job function"
            );
            case "BASIC" -> java.util.List.of(
                "Request RESOURCE role for table creation capabilities",
                "Consider requesting specific object privileges for tables you work with",
                "Explore available system privileges that match your responsibilities"
            );
            case "DEVELOPER" -> java.util.List.of(
                "You have good development privileges",
                "Consider requesting SELECT ANY TABLE for broader data access if needed",
                "Request specific administrative privileges only if required for your role"
            );
            case "ADVANCED" -> java.util.List.of(
                "You have comprehensive privileges for most operations",
                "Consider requesting DBA role only if administrative duties are required",
                "Current privilege level is suitable for advanced development work"
            );
            case "DBA" -> java.util.List.of(
                "You have full administrative privileges",
                "All operations should be available to you",
                "Consider security best practices when using administrative privileges"
            );
            default -> java.util.List.of("Unable to provide recommendations for unknown privilege level");
        };
    }

    private java.util.List<String> generateMissingPrivileges(String level) {
        return switch (level) {
            case "LIMITED" -> java.util.List.of("CONNECT", "CREATE SESSION", "SELECT on specific tables");
            case "BASIC" -> java.util.List.of("RESOURCE", "CREATE TABLE", "INSERT/UPDATE/DELETE privileges");
            case "DEVELOPER" -> java.util.List.of("SELECT ANY TABLE", "CREATE ANY TABLE", "ALTER TABLE privileges");
            case "ADVANCED" -> java.util.List.of("DBA role", "SYSDBA privileges", "Administrative system privileges");
            case "DBA" -> java.util.List.of("No additional privileges needed");
            default -> java.util.List.of("Unknown");
        };
    }

    private java.util.List<String> generateNextSteps(String level, long accessibilityPercentage) {
        if (accessibilityPercentage >= 80) {
            return java.util.List.of(
                "Your current privileges provide good access to most operations",
                "Continue using available features effectively",
                "Monitor for any specific access needs that arise"
            );
        } else if (accessibilityPercentage >= 50) {
            return java.util.List.of(
                "Consider requesting additional privileges for better functionality",
                "Identify specific operations you need access to",
                "Contact your DBA to discuss role enhancements"
            );
        } else {
            return java.util.List.of(
                "Your current access is quite limited",
                "Strongly recommend requesting appropriate roles from your DBA",
                "Identify your specific job requirements and request matching privileges",
                "Consider temporary elevated access for specific tasks if needed"
            );
        }
    }
}
