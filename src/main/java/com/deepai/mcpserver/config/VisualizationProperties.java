package com.deepai.mcpserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "oracle.visualization")
public class VisualizationProperties {

	private boolean enabled = true;
	private Cache cache = new Cache();
	private int maxDataPoints = 10000;
	private String defaultFramework = "plotly";
	private boolean autoDetectColumns = true;
	private Chart chart = new Chart();
	private Performance performance = new Performance();
	private Security security = new Security();

	// Getters and Setters
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public int getMaxDataPoints() {
		return maxDataPoints;
	}

	public void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}

	public String getDefaultFramework() {
		return defaultFramework;
	}

	public void setDefaultFramework(String defaultFramework) {
		this.defaultFramework = defaultFramework;
	}

	public boolean isAutoDetectColumns() {
		return autoDetectColumns;
	}

	public void setAutoDetectColumns(boolean autoDetectColumns) {
		this.autoDetectColumns = autoDetectColumns;
	}

	public Chart getChart() {
		return chart;
	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}

	public Performance getPerformance() {
		return performance;
	}

	public void setPerformance(Performance performance) {
		this.performance = performance;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public static class Cache {
		private boolean enabled = true;
		private int ttl = 300;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getTtl() {
			return ttl;
		}

		public void setTtl(int ttl) {
			this.ttl = ttl;
		}
	}

	public static class Chart {
		private Default defaultSettings = new Default();
		private boolean responsive = true;
		private Animation animation = new Animation();

		public Default getDefault() {
			return defaultSettings;
		}

		public void setDefault(Default defaultSettings) {
			this.defaultSettings = defaultSettings;
		}

		public boolean isResponsive() {
			return responsive;
		}

		public void setResponsive(boolean responsive) {
			this.responsive = responsive;
		}

		public Animation getAnimation() {
			return animation;
		}

		public void setAnimation(Animation animation) {
			this.animation = animation;
		}

		public static class Default {
			private int width = 800;
			private int height = 600;

			public int getWidth() {
				return width;
			}

			public void setWidth(int width) {
				this.width = width;
			}

			public int getHeight() {
				return height;
			}

			public void setHeight(int height) {
				this.height = height;
			}
		}

		public static class Animation {
			private boolean enabled = true;

			public boolean isEnabled() {
				return enabled;
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}
		}
	}

	public static class Performance {
		private int queryTimeout = 30000;
		private int fetchSize = 1000;
		private int maxConcurrentRequests = 10;

		public int getQueryTimeout() {
			return queryTimeout;
		}

		public void setQueryTimeout(int queryTimeout) {
			this.queryTimeout = queryTimeout;
		}

		public int getFetchSize() {
			return fetchSize;
		}

		public void setFetchSize(int fetchSize) {
			this.fetchSize = fetchSize;
		}

		public int getMaxConcurrentRequests() {
			return maxConcurrentRequests;
		}

		public void setMaxConcurrentRequests(int maxConcurrentRequests) {
			this.maxConcurrentRequests = maxConcurrentRequests;
		}
	}

	public static class Security {
		private boolean sqlInjectionProtection = true;
		private String allowedTablesPattern = ".*";
		private String blockedTablesPattern = "^(SYS|SYSTEM|ORACLE).*";

		public boolean isSqlInjectionProtection() {
			return sqlInjectionProtection;
		}

		public void setSqlInjectionProtection(boolean sqlInjectionProtection) {
			this.sqlInjectionProtection = sqlInjectionProtection;
		}

		public String getAllowedTablesPattern() {
			return allowedTablesPattern;
		}

		public void setAllowedTablesPattern(String allowedTablesPattern) {
			this.allowedTablesPattern = allowedTablesPattern;
		}

		public String getBlockedTablesPattern() {
			return blockedTablesPattern;
		}

		public void setBlockedTablesPattern(String blockedTablesPattern) {
			this.blockedTablesPattern = blockedTablesPattern;
		}
	}
}