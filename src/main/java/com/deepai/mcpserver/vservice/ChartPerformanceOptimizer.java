package com.deepai.mcpserver.vservice;

import com.deepai.mcpserver.config.VisualizationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performance Optimization Service for Professional Chart Generation
 * Handles data aggregation, sampling, caching, and rendering optimizations
 */
@Service
@Slf4j
public class ChartPerformanceOptimizer {
    
    @Autowired
    private VisualizationProperties properties;
    
    // Chart-specific optimization thresholds
    private static final Map<String, Integer> CHART_TYPE_THRESHOLDS = Map.of(
        "executive_dashboard", 50,
        "gradient_area", 200,
        "heatmap", 100,
        "candlestick", 500,
        "radar", 20,
        "sunburst", 30
    );
    
    /**
     * INTELLIGENT DATA SAMPLING
     * Reduces data points while preserving visual fidelity
     */
    @Cacheable(value = "optimizedData", cacheManager = "visualizationCacheManager")
    public List<Map<String, Object>> optimizeDataForChart(List<Map<String, Object>> data, 
                                                           String chartType, 
                                                           Map<String, Object> config) {
        try {
            if (data.isEmpty()) return data;
            
            int threshold = CHART_TYPE_THRESHOLDS.getOrDefault(chartType, 100);
            
            if (data.size() <= threshold) {
                return data; // No optimization needed
            }
            
            log.info("Optimizing {} data points for {} chart", data.size(), chartType);
            
            switch (chartType.toLowerCase()) {
                case "gradient_area":
                case "candlestick":
                    return optimizeTimeSeriesData(data, threshold);
                    
                case "heatmap":
                    return optimizeHeatmapData(data, threshold);
                    
                case "executive_dashboard":
                    return optimizeAggregatedData(data, threshold);
                    
                default:
                    return optimizeGenericData(data, threshold);
            }
            
        } catch (Exception e) {
            log.error("Error optimizing data for chart type: {}", chartType, e);
            return data; // Return original data if optimization fails
        }
    }
    
    /**
     * TIME SERIES DATA OPTIMIZATION
     */
    private List<Map<String, Object>> optimizeTimeSeriesData(List<Map<String, Object>> data, int maxPoints) {
        if (data.size() <= maxPoints) return data;
        
        // Simple step-based sampling
        int step = Math.max(1, data.size() / maxPoints);
        List<Map<String, Object>> sampled = new ArrayList<>();
        
        // Always keep first and last points
        sampled.add(data.get(0));
        
        // Sample intermediate points
        for (int i = step; i < data.size() - step; i += step) {
            sampled.add(data.get(i));
        }
        
        // Always keep last point
        if (data.size() > 1) {
            sampled.add(data.get(data.size() - 1));
        }
        
        log.info("Time series sampling: {} -> {} points", data.size(), sampled.size());
        return sampled;
    }
    
    /**
     * HEATMAP DATA OPTIMIZATION
     */
    private List<Map<String, Object>> optimizeHeatmapData(List<Map<String, Object>> data, int maxCells) {
        if (data.size() <= maxCells) return data;
        
        // Simple limit to max cells
        List<Map<String, Object>> optimized = data.stream()
            .limit(maxCells)
            .collect(Collectors.toList());
        
        log.info("Heatmap optimization: {} -> {} cells", data.size(), optimized.size());
        return optimized;
    }
    
    /**
     * EXECUTIVE DASHBOARD OPTIMIZATION
     */
    private List<Map<String, Object>> optimizeAggregatedData(List<Map<String, Object>> data, int maxItems) {
        if (data.size() <= maxItems) return data;
        
        // Sort by total_amount and keep top performers
        return data.stream()
            .sorted((a, b) -> {
                double amountA = ((Number) a.getOrDefault("total_amount", 0)).doubleValue();
                double amountB = ((Number) b.getOrDefault("total_amount", 0)).doubleValue();
                return Double.compare(amountB, amountA); // Descending
            })
            .limit(maxItems)
            .collect(Collectors.toList());
    }
    
    /**
     * GENERIC DATA OPTIMIZATION
     */
    private List<Map<String, Object>> optimizeGenericData(List<Map<String, Object>> data, int maxPoints) {
        if (data.size() <= maxPoints) return data;
        
        // Simple random sampling
        Random random = new Random(42); // Deterministic for caching
        
        return data.stream()
            .filter(d -> random.nextDouble() < (double) maxPoints / data.size())
            .limit(maxPoints)
            .collect(Collectors.toList());
    }
    
    /**
     * Performance monitoring and metrics
     */
    public Map<String, Object> getOptimizationMetrics(String chartType, 
                                                       int originalSize, 
                                                       int optimizedSize, 
                                                       long processingTime) {
        Map<String, Object> metrics = new HashMap<>();
        
        double reductionRatio = (double) (originalSize - optimizedSize) / originalSize;
        double compressionRatio = (double) optimizedSize / originalSize;
        
        metrics.put("chartType", chartType);
        metrics.put("originalDataPoints", originalSize);
        metrics.put("optimizedDataPoints", optimizedSize);
        metrics.put("reductionRatio", Math.round(reductionRatio * 10000.0) / 100.0); // Percentage
        metrics.put("compressionRatio", Math.round(compressionRatio * 10000.0) / 100.0);
        metrics.put("processingTimeMs", processingTime);
        metrics.put("performanceGain", calculatePerformanceGain(reductionRatio));
        
        log.info("Optimization metrics for {}: {}% reduction, {}ms processing", 
                chartType, Math.round(reductionRatio * 100), processingTime);
        
        return metrics;
    }
    
    private String calculatePerformanceGain(double reductionRatio) {
        if (reductionRatio > 0.8) return "Excellent";
        if (reductionRatio > 0.6) return "Very Good";
        if (reductionRatio > 0.4) return "Good";
        if (reductionRatio > 0.2) return "Moderate";
        return "Minimal";
    }
}
