package com.deepai.mcpserver.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard response wrapper for all Oracle MCP Server API endpoints
 * Provides consistent structure for success, error, warning, info, and other response types
 * 
 * Based on the comprehensive error analysis recommendations from COMPREHENSIVE_ERROR_ANALYSIS.md
 * 
 * @author Oracle MCP Server Team
 * @version 2.0.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse {
    
    /**
     * Response status indicating the type of response
     */
    private ResponseStatus status;
    
    /**
     * User-friendly main message describing what happened
     */
    private String message;
    
    /**
     * Additional details providing context about the response
     */
    private ResponseDetails details;
    
    /**
     * Response timestamp
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Response payload data (for successful operations)
     */
    private Object data;
    
    /**
     * Enum for different response statuses
     */
    public enum ResponseStatus {
        SUCCESS,
        ERROR, 
        WARNING,
        INFO,
        FEATURE_UNAVAILABLE,
        CONFIGURATION_LIMITATION,
        PARTIAL_SUPPORT
    }
    
    /**
     * Additional details for responses providing context and guidance
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseDetails {
        
        /**
         * Why this happened - root cause explanation
         */
        private String reason;
        
        /**
         * What this means for the user - impact description
         */
        private String impact;
        
        /**
         * Current state or configuration
         */
        private String currentState;
        
        /**
         * Required state, version, or license for full functionality
         */
        private String requiredState;
        
        /**
         * List of possible workarounds or alternative approaches
         */
        private List<String> alternatives;
        
        /**
         * Actionable next steps the user can take
         */
        private List<String> suggestions;
        
        /**
         * Technical error details for developers/debugging
         */
        private String technicalError;
        
        /**
         * How to get help or additional information
         */
        private String supportInfo;
        
        /**
         * Security implications of the current state
         */
        private String securityImpact;
        
        /**
         * Performance implications or notes
         */
        private String performanceNote;
        
        /**
         * Configuration instructions or examples
         */
        private Map<String, Object> configurationGuide;
        
        /**
         * Version or license information
         */
        private VersionInfo versionInfo;
        
        /**
         * Feature availability information
         */
        private FeatureInfo featureInfo;
    }
    
    /**
     * Version and license information
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VersionInfo {
        private String currentVersion;
        private String requiredVersion;
        private String currentLicense;
        private String requiredLicense;
        private String upgradeInfo;
    }
    
    /**
     * Feature availability information
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeatureInfo {
        private String featureName;
        private String description;
        private String availability;
        private List<String> availableAlternatives;
        private String enableInstructions;
    }
    
    // ========== FACTORY METHODS FOR COMMON RESPONSE TYPES ==========
    
    /**
     * Create a successful response
     */
    public static StandardResponse success(String message) {
        return StandardResponse.builder()
            .status(ResponseStatus.SUCCESS)
            .message(message)
            .build();
    }
    
    /**
     * Create a successful response with data
     */
    public static StandardResponse success(String message, Object data) {
        return StandardResponse.builder()
            .status(ResponseStatus.SUCCESS)
            .message(message)
            .data(data)
            .build();
    }
    
    /**
     * Create an error response with user-friendly message
     */
    public static StandardResponse error(String message) {
        return StandardResponse.builder()
            .status(ResponseStatus.ERROR)
            .message(message)
            .build();
    }
    
    /**
     * Create an error response with details
     */
    public static StandardResponse error(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.ERROR)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Create a warning response for partial functionality
     */
    public static StandardResponse warning(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.WARNING)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Create an info response for providing context
     */
    public static StandardResponse info(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.INFO)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Create a feature unavailable response
     */
    public static StandardResponse featureUnavailable(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.FEATURE_UNAVAILABLE)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Create a configuration limitation response
     */
    public static StandardResponse configurationLimitation(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.CONFIGURATION_LIMITATION)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Create a partial support response
     */
    public static StandardResponse partialSupport(String message, ResponseDetails details) {
        return StandardResponse.builder()
            .status(ResponseStatus.PARTIAL_SUPPORT)
            .message(message)
            .details(details)
            .build();
    }
}
