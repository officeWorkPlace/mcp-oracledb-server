package com.deepai.mcpserver.vtools;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepai.mcpserver.model.ChartSpecification;
import com.deepai.mcpserver.model.DataAnalysis;
import com.deepai.mcpserver.vservice.DataAnalysisService;
import com.deepai.mcpserver.vservice.VisualizationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SmartVisualizationTool {

	@Autowired
	private VisualizationService visualizationService;

	@Autowired
	private DataAnalysisService analysisService;

	@Tool(description = "Automatically analyze table structure and generate the best visualization with AI-powered column selection and chart type recommendation.")
	public Map<String, Object> smartVisualization(
			@ToolParam(description = "Oracle table name to analyze and visualize", required = true) String tableName,

			@ToolParam(description = "Visualization framework (plotly, vega-lite)", required = false) String framework,

			@ToolParam(description = "Preferred X-axis column (optional hint)", required = false) String xColumnHint,

			@ToolParam(description = "Preferred Y-axis column (optional hint)", required = false) String yColumnHint) {

		try {
			log.info("Executing smart visualization for table: {}", tableName);

			// Analyze table structure
			DataAnalysis analysis = analysisService.analyzeTable(tableName);

			// Determine best columns and chart type
			String xColumn = xColumnHint != null ? xColumnHint : selectBestXColumn(analysis);
			String yColumn = yColumnHint != null ? yColumnHint : selectBestYColumn(analysis);
			String chartType = selectBestChartType(analysis, xColumn, yColumn);

			// Generate visualization
			ChartSpecification chart = visualizationService.generateSmartVisualization(tableName, xColumn, yColumn);

			return Map.of("status", "success", "analysis",
					Map.of("tableStructure",
							Map.of("totalColumns", analysis.getColumnTypes().size(), "numericColumns",
									analysis.getNumericColumns(), "categoricalColumns",
									analysis.getCategoricalColumns(), "dateColumns", analysis.getDateColumns()),
							"selectedColumns",
							Map.of("xColumn", xColumn, "yColumn", yColumn, "reasoning",
									generateColumnSelectionReasoning(analysis, xColumn, yColumn)),
							"recommendedChartType", chartType, "alternativeChartTypes",
							analysis.getSuggestedChartTypes()),
					"visualization", Map.of("framework", chart.getFramework(), "chartType", chart.getChartType(),
							"specification", chart.getSpecification(), "metadata", chart.getMetadata()));

		} catch (Exception e) {
			log.error("Error executing smart visualization tool", e);
			return Map.of("status", "error", "message", "Failed to generate smart visualization: " + e.getMessage(),
					"errorType", e.getClass().getSimpleName());
		}
	}

	private String selectBestXColumn(DataAnalysis analysis) {
		if (!analysis.getCategoricalColumns().isEmpty()) {
			return analysis.getCategoricalColumns().get(0);
		}
		if (!analysis.getDateColumns().isEmpty()) {
			return analysis.getDateColumns().get(0);
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

	private String selectBestChartType(DataAnalysis analysis, String xColumn, String yColumn) {
		if (analysis.getCategoricalColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "bar";
		} else if (analysis.getDateColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "line";
		} else if (analysis.getNumericColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "scatter";
		} else if (analysis.getCategoricalColumns().contains(xColumn)
				&& analysis.getCategoricalColumns().contains(yColumn)) {
			return "heatmap";
		}
		return "bar";
	}

	private String generateColumnSelectionReasoning(DataAnalysis analysis, String xColumn, String yColumn) {
		StringBuilder reasoning = new StringBuilder();

		if (analysis.getCategoricalColumns().contains(xColumn)) {
			reasoning.append("Selected '").append(xColumn)
					.append("' for X-axis as it's categorical and good for grouping. ");
		} else if (analysis.getDateColumns().contains(xColumn)) {
			reasoning.append("Selected '").append(xColumn)
					.append("' for X-axis as it's temporal and good for trends. ");
		}

		if (analysis.getNumericColumns().contains(yColumn)) {
			reasoning.append("Selected '").append(yColumn).append("' for Y-axis as it's numeric and measurable.");
		}

		return reasoning.toString();
	}
}