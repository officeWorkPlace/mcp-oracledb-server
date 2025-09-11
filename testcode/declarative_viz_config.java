package com.deepai.mcpserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@EnableCaching
@EnableAsync
@EnableConfigurationProperties(VisualizationProperties.class)
@ConditionalOnProperty(name = "oracle.visualization.enabled", havingValue = "true", matchIfMissing = true)
public class VisualizationConfig {
    
    @Bean("visualizationCacheManager")
    public CacheManager visualizationCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "chartData", 
            "tableAnalysis", 
            "columnMetadata",
            "queryResults",
            "dashboardData",
            "performanceMetrics",
            "visualizationCache",
            "vegaLiteSpecs",
            "plotlySpecs",
            "financialMetrics",
            "loanAnalytics",
            "branchPerformance",
            "customerSegments",
            "riskAssessments",
            "auditLogs",
            "paymentBehavior"
        );
        return cacheManager;
    }
    
    @Bean("visualizationTaskExecutor")
    public Executor visualizationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("viz-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    @Bean("declarativeVizExecutor")
    public Executor declarativeVizExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("declarative-viz-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
    
    @Bean
    public RestTemplate visualizationRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(30))
            .setReadTimeout(Duration.ofSeconds(60))
            .additionalInterceptors((request, body, execution) -> {
                request.getHeaders().add("User-Agent", "Oracle-MCP-Visualization-Service/2.0");
                request.getHeaders().add("Accept", "application/json");
                return execution.execute(request, body);
            })
            .build();
    }
}