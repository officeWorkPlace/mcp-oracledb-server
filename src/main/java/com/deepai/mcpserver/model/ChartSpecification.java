package com.deepai.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartSpecification {
    private String framework;
    private String chartType;
    private Map<String, Object> specification;
    private String dataUrl;
    private Map<String, Object> metadata;
}
