package com.deepai.mcpserver.vservice;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	/**
	 * Generate visualization from any table/query
	 */
	public ChartSpecification generateVisualization(VisualizationRequest request) {
		try {
			log.info("Generating {} visualization for table {}", request.getChartType(), request.getTableName());

			// Analyze data structure if not provided
			if (request.getColumns() == null || request.getColumns().isEmpty()) {
				DataAnalysis analysis = analysisService.analyzeTable(request.getTableName());
				request = enhanceRequestWithAnalysis(request, analysis);
			}

			// Fetch data
			List<Map<String, Object>> data = dataService.fetchData(request);

			// Generate specification
			ChartSpecification spec;
			if ("plotly".equalsIgnoreCase(request.getFramework())) {
				spec = plotlyGenerator.generateSpec(request, data);
			} else {
				spec = vegaLiteGenerator.generateSpec(request, data);
			}

			// Add metadata
			spec.setMetadata(Map.of("rowCount", data.size(), "generatedAt", System.currentTimeMillis(), "source",
					request.getTableName()));

			return spec;

		} catch (Exception e) {
			log.error("Error generating visualization", e);
			throw new RuntimeException("Failed to generate visualization: " + e.getMessage(), e);
		}
	}

	/**
	 * Auto-suggest best visualization for data
	 */
	public ChartSpecification generateSmartVisualization(String tableName, String xColumn, String yColumn) {
		DataAnalysis analysis = analysisService.analyzeTable(tableName);

		VisualizationRequest request = VisualizationRequest.builder().tableName(tableName).framework("plotly")
				.xColumn(xColumn != null ? xColumn : selectBestXColumn(analysis))
				.yColumn(yColumn != null ? yColumn : selectBestYColumn(analysis))
				.chartType(determineBestChartType(analysis, xColumn, yColumn)).build();

		return generateVisualization(request);
	}

	private VisualizationRequest enhanceRequestWithAnalysis(VisualizationRequest request, DataAnalysis analysis) {
		if (request.getXColumn() == null && !analysis.getCategoricalColumns().isEmpty()) {
			request.setXColumn(analysis.getCategoricalColumns().get(0));
		}
		if (request.getYColumn() == null && !analysis.getNumericColumns().isEmpty()) {
			request.setYColumn(analysis.getNumericColumns().get(0));
		}
		return request;
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

	private String determineBestChartType(DataAnalysis analysis, String xColumn, String yColumn) {
		if (xColumn == null)
			xColumn = selectBestXColumn(analysis);
		if (yColumn == null)
			yColumn = selectBestYColumn(analysis);

		if (analysis.getCategoricalColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "bar";
		} else if (analysis.getDateColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "line";
		} else if (analysis.getNumericColumns().contains(xColumn) && analysis.getNumericColumns().contains(yColumn)) {
			return "scatter";
		}
		return "bar";
	}
}
