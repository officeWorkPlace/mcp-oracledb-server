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
    private String framework;
    private List<String> columns;
    private String whereClause;
    private String groupBy;
    private String orderBy;
    private Integer limit;
    private Map<String, Object> chartOptions;
    private String aggregationType;
    private String xColumn;
    private String yColumn;
    private String colorColumn;
    private String sizeColumn;
}
