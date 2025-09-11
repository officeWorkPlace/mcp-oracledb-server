package com.deepai.mcpserver.config;

import java.util.concurrent.Executor;

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
            "visualizationCache"
        );
        return cacheManager;
    }
    
    @Bean("visualizationTaskExecutor")
    public Executor visualizationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("viz-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    @Bean
    public RestTemplate visualizationRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Configure timeout settings
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "Oracle-MCP-Visualization-Service");
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}