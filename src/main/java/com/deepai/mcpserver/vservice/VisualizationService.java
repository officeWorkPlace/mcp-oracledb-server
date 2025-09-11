package com.deepai.mcpserver.vservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.deepai.mcpserver.config.VisualizationProperties;
import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.DataAnalysis;
import com.deepai.mcpserver.model.VisualizationRequest;
import com.deepai.mcpserver.util.VegaLiteSpecGenerator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VisualizationService {

	@Autowired
	private GenericDataService dataService;

	@Autowired
	private PlotlySpecGenerator plotlyGenerator;

	@Autowired
	private VegaLiteSpecGenerator vegaLiteGenerator;

	@Autowired
	private DataAnalysisService analysisService;
	
	@Autowired
	private VisualizationProperties visualizationProperties;

	/**
	 * Generate visualization from any table/query with enhanced security and caching
	 */
	@Cacheable(value = "chartData", cacheManager = "visualizationCacheManager")
	public ChartSpecification generateVisualization(VisualizationRequest request) {
		try {
			
			if (!isTableAllowed(request.getTableName())) {
		        throw new SecurityException("Access to table " + request.getTableName() + " is not allowed");
		    }
			
			log.info("Generating {} visualization for table {}", request.getChartType(), request.getTableName());

			// Security check
			if (!isTableAllowed(request.getTableName())) {
				throw new SecurityException("Access to table " + request.getTableName() + " is not allowed");
			}

			// Analyze data structure if not provided
			if (request.getColumns() == null || request.getColumns().isEmpty()) {
				DataAnalysis analysis = analysisService.analyzeTable(request.getTableName());
				request = enhanceRequestWithAnalysis(request, analysis);
			}

			// Apply data point limits
			if (request.getLimit() == null || request.getLimit() > visualizationProperties.getMaxDataPoints()) {
				request.setLimit(visualizationProperties.getMaxDataPoints());
				log.warn("Data limited to {} points for table {}", 
						visualizationProperties.getMaxDataPoints(), request.getTableName());
			}

			// Fetch data with performance settings
			List<Map<String, Object>> data = dataService.fetchData(request);

			// Generate specification based on configured framework
			ChartSpecification spec;
			String framework = request.getFramework() != null ? 
				request.getFramework() : visualizationProperties.getDefaultFramework();
				
			if ("plotly".equalsIgnoreCase(framework)) {
				spec = plotlyGenerator.generateSpec(request, data);
			} else {
				spec = vegaLiteGenerator.generateSpec(request, data);
			}

			// Enhanced metadata with configuration
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("rowCount", data.size());
			metadata.put("generatedAt", System.currentTimeMillis());
			metadata.put("source", request.getTableName());
			metadata.put("framework", framework);
			metadata.put("chartType", request.getChartType());
			metadata.put("responsive", visualizationProperties.getChart().isResponsive());
			metadata.put("width", visualizationProperties.getChart().getDefault().getWidth());
			metadata.put("height", visualizationProperties.getChart().getDefault().getHeight());
			metadata.put("animation", visualizationProperties.getChart().getAnimation().isEnabled());
			metadata.put("securityApplied", visualizationProperties.getSecurity().isSqlInjectionProtection());
			metadata.put("cacheEnabled", visualizationProperties.getCache().isEnabled());
			
			spec.setMetadata(metadata);

			return spec;

		} catch (Exception e) {
			log.error("Error generating visualization for table: {}", request.getTableName(), e);
			throw new RuntimeException("Failed to generate visualization: " + e.getMessage(), e);
		}
	}

	/**
	 * Auto-suggest best visualization for data with enhanced intelligence
	 */
	@Cacheable(value = "tableAnalysis", cacheManager = "visualizationCacheManager")
	public ChartSpecification generateSmartVisualization(String tableName, String xColumn, String yColumn) {
		
		// Security check
		if (!isTableAllowed(tableName)) {
			throw new SecurityException("Access to table " + tableName + " is not allowed");
		}
		
		DataAnalysis analysis = analysisService.analyzeTable(tableName);

		VisualizationRequest request = VisualizationRequest.builder()
				.tableName(tableName)
				.framework(visualizationProperties.getDefaultFramework())
				.xColumn(xColumn != null ? xColumn : selectBestXColumn(analysis))
				.yColumn(yColumn != null ? yColumn : selectBestYColumn(analysis))
				.chartType(determineBestChartType(analysis, xColumn, yColumn))
				.limit(visualizationProperties.getMaxDataPoints())
				.build();

		return generateVisualization(request);
	}
	
	/**
	 * New method: Analyze table for visualization capabilities
	 */
	@Cacheable(value = "columnMetadata", cacheManager = "visualizationCacheManager")
	public Map<String, Object> analyzeTableForVisualization(String tableName) {
		if (!isTableAllowed(tableName)) {
			throw new SecurityException("Access to table " + tableName + " is not allowed");
		}
		
		DataAnalysis analysis = analysisService.analyzeTable(tableName);
		
		Map<String, Object> result = new HashMap<>();
		result.put("tableName", tableName);
		result.put("analysis", analysis);
		result.put("suggestions", generateVisualizationSuggestions(analysis));
		result.put("supportedChartTypes", getSupportedChartTypes(analysis));
		result.put("autoDetectedColumns", visualizationProperties.isAutoDetectColumns());
		result.put("maxDataPoints", visualizationProperties.getMaxDataPoints());
		result.put("defaultFramework", visualizationProperties.getDefaultFramework());
		result.put("chartConfig", getChartConfiguration());
		
		return result;
	}
	
	/**
	 * New method: Create dashboard with multiple charts
	 */
	@Cacheable(value = "dashboardData", cacheManager = "visualizationCacheManager")
	public Map<String, Object> createDashboard(String dashboardName, List<String> tableNames, 
											   List<String> chartTypes, String schemaName) {
		Map<String, Object> dashboard = new HashMap<>();
		dashboard.put("name", dashboardName);
		dashboard.put("framework", visualizationProperties.getDefaultFramework());
		dashboard.put("responsive", visualizationProperties.getChart().isResponsive());
		dashboard.put("configuration", getChartConfiguration());
		
		Map<String, ChartSpecification> charts = new HashMap<>();
		
		for (int i = 0; i < tableNames.size() && i < chartTypes.size(); i++) {
			String fullTableName = schemaName != null ? schemaName + "." + tableNames.get(i) : tableNames.get(i);
			
			try {
				if (isTableAllowed(fullTableName)) {
					ChartSpecification chart = generateSmartVisualization(fullTableName, null, null);
					charts.put("chart_" + i, chart);
				} else {
					log.warn("Table {} not allowed for dashboard creation", fullTableName);
				}
			} catch (Exception e) {
				log.error("Failed to generate chart for table {}: {}", fullTableName, e.getMessage());
			}
		}
		
		dashboard.put("charts", charts);
		dashboard.put("chartCount", charts.size());
		dashboard.put("createdAt", System.currentTimeMillis());
		dashboard.put("securityApplied", visualizationProperties.getSecurity().isSqlInjectionProtection());
		
		return dashboard;
	}
	
	/**
	 * New method: Export visualization data
	 */
	public Map<String, Object> exportVisualization(String format, String tableName, 
												   String chartType, String schemaName) {
		String fullTableName = schemaName != null ? schemaName + "." + tableName : tableName;
		
		if (!isTableAllowed(fullTableName)) {
			throw new SecurityException("Access to table " + fullTableName + " is not allowed");
		}
		
		try {
			ChartSpecification chart = generateSmartVisualization(fullTableName, null, null);
			
			Map<String, Object> exportData = new HashMap<>();
			exportData.put("format", format);
			exportData.put("tableName", fullTableName);
			exportData.put("chartType", chartType);
			exportData.put("specification", chart);
			exportData.put("exportedAt", System.currentTimeMillis());
			exportData.put("framework", visualizationProperties.getDefaultFramework());
			exportData.put("config", getChartConfiguration());
			
			return exportData;
			
		} catch (Exception e) {
			log.error("Failed to export visualization for table {}: {}", fullTableName, e.getMessage());
			throw new RuntimeException("Failed to export visualization", e);
		}
	}

	// Security method using configuration properties
	private boolean isTableAllowed(String tableName) {
		if (!visualizationProperties.getSecurity().isSqlInjectionProtection()) {
			return true;
		}
		
		Pattern allowedPattern = Pattern.compile(visualizationProperties.getSecurity().getAllowedTablesPattern());
		Pattern blockedPattern = Pattern.compile(visualizationProperties.getSecurity().getBlockedTablesPattern());
		
		return allowedPattern.matcher(tableName.toUpperCase()).matches() && 
			   !blockedPattern.matcher(tableName.toUpperCase()).matches();
	}

	private VisualizationRequest enhanceRequestWithAnalysis(VisualizationRequest request, DataAnalysis analysis) {
		if (visualizationProperties.isAutoDetectColumns()) {
			if (request.getXColumn() == null && !analysis.getCategoricalColumns().isEmpty()) {
				request.setXColumn(analysis.getCategoricalColumns().get(0));
			}
			if (request.getYColumn() == null && !analysis.getNumericColumns().isEmpty()) {
				request.setYColumn(analysis.getNumericColumns().get(0));
			}
		}
		return request;
	}

	private String selectBestXColumn(DataAnalysis analysis) {
		if (visualizationProperties.isAutoDetectColumns()) {
			if (!analysis.getCategoricalColumns().isEmpty()) {
				return analysis.getCategoricalColumns().get(0);
			}
			if (!analysis.getDateColumns().isEmpty()) {
				return analysis.getDateColumns().get(0);
			}
		}
		if (!analysis.getNumericColumns().isEmpty()) {
			return analysis.getNumericColumns().get(0);
		}
		return analysis.getColumnTypes().keySet().iterator().next();
	}

	private String selectBestYColumn(DataAnalysis analysis) {
		if (!analysis.getNumericColumns().isEmpty()) {
			return analysis.getNumericColumns().get(0);
		}
		if (!analysis.getCategoricalColumns().isEmpty()) {
			return analysis.getCategoricalColumns().get(0);
		}
		return analysis.getColumnTypes().keySet().iterator().next();
	}

	private String determineBestChartType(DataAnalysis analysis, String xColumn, String yColumn) {
		if (xColumn == null) xColumn = selectBestXColumn(analysis);
		if (yColumn == null) yColumn = selectBestYColumn(analysis);

		if (analysis.getCategoricalColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "bar";
		} else if (analysis.getDateColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "line";
		} else if (analysis.getNumericColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "scatter";
		}
		return "bar";
	}
	
	private Map<String, Object> generateVisualizationSuggestions(DataAnalysis analysis) {
		Map<String, Object> suggestions = new HashMap<>();
		
		if (!analysis.getNumericColumns().isEmpty()) {
			if (!analysis.getDateColumns().isEmpty()) {
				suggestions.put("recommendedChartType", "line");
				suggestions.put("xAxis", analysis.getDateColumns().get(0));
				suggestions.put("yAxis", analysis.getNumericColumns().get(0));
			} else if (!analysis.getCategoricalColumns().isEmpty()) {
				suggestions.put("recommendedChartType", "bar");
				suggestions.put("xAxis", analysis.getCategoricalColumns().get(0));
				suggestions.put("yAxis", analysis.getNumericColumns().get(0));
			}
		}
		
		suggestions.put("framework", visualizationProperties.getDefaultFramework());
		suggestions.put("responsive", visualizationProperties.getChart().isResponsive());
		suggestions.put("maxDataPoints", visualizationProperties.getMaxDataPoints());
		
		return suggestions;
	}
	
	private List<String> getSupportedChartTypes(DataAnalysis analysis) {
		List<String> chartTypes = new java.util.ArrayList<>();
		
		if (!analysis.getNumericColumns().isEmpty()) {
			chartTypes.add("bar");
			chartTypes.add("line");
			if (analysis.getNumericColumns().size() > 1) {
				chartTypes.add("scatter");
			}
		}
		
		if (!analysis.getCategoricalColumns().isEmpty()) {
			chartTypes.add("pie");
			chartTypes.add("donut");
		}
		
		if (!analysis.getDateColumns().isEmpty() && !analysis.getNumericColumns().isEmpty()) {
			chartTypes.add("timeseries");
		}
		
		return chartTypes;
	}
	
	private Map<String, Object> getChartConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("width", visualizationProperties.getChart().getDefault().getWidth());
		config.put("height", visualizationProperties.getChart().getDefault().getHeight());
		config.put("responsive", visualizationProperties.getChart().isResponsive());
		config.put("animation", visualizationProperties.getChart().getAnimation().isEnabled());
		config.put("framework", visualizationProperties.getDefaultFramework());
		return config;
	}
}