package com.deepai.mcpserver.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationRequest {
    
    private String tableName;
    private String chartType;
    private String xColumn;
    private String yColumn;
    private String colorColumn;
    private String framework;
    private String aggregationType;
    private String groupBy;
    private Integer limit;
    private String whereClause;
    private String orderBy;
    private List<String> columns;
    private Map<String, Object> parameters;
    private String schemaName;
    private boolean autoDetect;
    
    // Additional configuration
    private Integer width;
    private Integer height;
    private boolean responsive;
    private boolean animation;
    private String colorScheme;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    
    // Performance settings
    private Integer timeout;
    private Integer fetchSize;
    private boolean useCache;
    
    // Security settings
    private boolean validateTableAccess;
    private String userRole;
}
