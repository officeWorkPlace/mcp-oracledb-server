package com.deepai.mcpserver.response;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility service to convert legacy Map-based responses to StandardResponse format
 * Helps with gradual migration from current response format to improved user-friendly format
 * 
 * @author Oracle MCP Server Team
 * @version 2.0.0
 */
@Component
public class ResponseConverter {
    
    /**
     * Convert a Map response to StandardResponse format
     * Maintains backward compatibility while providing improved structure
     */
    public StandardResponse fromMap(Map<String, Object> mapResponse) {
        if (mapResponse == null) {
            return StandardResponse.error("No response data available");
        }
        
        String status = (String) mapResponse.getOrDefault("status", "unknown");
        String message = (String) mapResponse.getOrDefault("message", "No message provided");
        Object data = mapResponse.get("data");
        
        StandardResponse.StandardResponseBuilder builder = StandardResponse.builder()
            .message(message);
        
        // Determine response status
        switch (status.toLowerCase()) {
            case "success":
                builder.status(StandardResponse.ResponseStatus.SUCCESS);
                if (data != null) {
                    builder.data(data);
                } else {
                    // Include all map data except status and message
                    Map<String, Object> dataMap = new HashMap<>(mapResponse);
                    dataMap.remove("status");
                    dataMap.remove("message");
                    if (!dataMap.isEmpty()) {
                        builder.data(dataMap);
                    }
                }
                break;
            case "error":
                builder.status(StandardResponse.ResponseStatus.ERROR);
                break;
            case "warning":
                builder.status(StandardResponse.ResponseStatus.WARNING);
                break;
            case "info":
                builder.status(StandardResponse.ResponseStatus.INFO);
                break;
            default:
                builder.status(StandardResponse.ResponseStatus.INFO);
                break;
        }
        
        return builder.build();
    }
    
    /**
     * Convert StandardResponse back to Map format for backward compatibility
     */
    public Map<String, Object> toMap(StandardResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatus().name().toLowerCase());
        result.put("message", response.getMessage());
        result.put("timestamp", response.getTimestamp());
        
        if (response.getData() != null) {
            result.put("data", response.getData());
        }
        
        if (response.getDetails() != null) {
            result.put("details", response.getDetails());
        }
        
        return result;
    }
}
