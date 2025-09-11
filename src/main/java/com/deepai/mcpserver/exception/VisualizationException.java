package com.deepai.mcpserver.exception;

public class VisualizationException extends RuntimeException {
    
    private final String errorCode;
    
    public VisualizationException(String message) {
        super(message);
        this.errorCode = "VISUALIZATION_ERROR";
    }
    
    public VisualizationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public VisualizationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VISUALIZATION_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
