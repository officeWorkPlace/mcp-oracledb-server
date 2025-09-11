package com.deepai.mcpserver.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.DataAnalysis;
import com.deepai.mcpserver.model.VisualizationRequest;
import com.deepai.mcpserver.vservice.DataAnalysisService;
import com.deepai.mcpserver.vservice.GenericDataService;
import com.deepai.mcpserver.vservice.GenericVisualizationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/visualization")
@Slf4j
public class VisualizationController {
    
    @Autowired
    private GenericVisualizationService visualizationService;
    
    @Autowired
    private DataAnalysisService analysisService;
    
    @Autowired
    private GenericDataService dataService;
    
    /**
     * Generate visualization from request
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateVisualization(@RequestBody VisualizationRequest request) {
        try {
            ChartSpecification chart = visualizationService.generateVisualization(request);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "chart", chart
            ));
        } catch (Exception e) {
            log.error("Error generating visualization", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Smart visualization with auto-detection
     */
    @PostMapping("/smart")
    public ResponseEntity<Map<String, Object>> generateSmartVisualization(
            @RequestParam String tableName,
            @RequestParam(required = false) String xColumn,
            @RequestParam(required = false) String yColumn,
            @RequestParam(defaultValue = "plotly") String framework) {
        try {
            ChartSpecification chart = visualizationService.generateSmartVisualization(tableName, xColumn, yColumn);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "chart", chart
            ));
        } catch (Exception e) {
            log.error("Error generating smart visualization", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Analyze table structure
     */
    @GetMapping("/analyze/{tableName}")
    public ResponseEntity<DataAnalysis> analyzeTable(@PathVariable String tableName) {
        try {
            DataAnalysis analysis = analysisService.analyzeTable(tableName);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing table", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get data for visualization
     */
    @PostMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getData(@RequestBody VisualizationRequest request) {
        try {
            List<Map<String, Object>> data = dataService.fetchData(request);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching data", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "visualization",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
