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
public class DataAnalysis {
    private List<Map<String, Object>> data;
    private Map<String, String> columnTypes;
    private Map<String, Object> statistics;
    private List<String> suggestedChartTypes;
    private String primaryKey;
    private List<String> numericColumns;
    private List<String> categoricalColumns;
    private List<String> dateColumns;
}
