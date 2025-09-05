package com.deepai.mcpserver.config;

import com.deepai.mcpserver.util.OracleFeatureDetector;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Oracle Configuration - Database Connection and Feature Setup
 * 
 * Configures Oracle-specific database connections and features:
 * - Multi-version Oracle support (11g-23c)
 * - HikariCP connection pool optimization for Oracle
 * - Feature detection initialization
 * - Oracle-specific JDBC properties
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@Configuration
@ConfigurationProperties(prefix = "oracle")
public class OracleConfiguration {

    @Autowired
    private OracleFeatureDetector featureDetector;

    private Features features = new Features();

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    /**
     * Initialize Oracle feature detection after configuration
     */
    @PostConstruct
    public void initializeOracleFeatures() {
        if (features.detection.enabled) {
            try {
                featureDetector.detectOracleFeatures();
            } catch (Exception e) {
                // Log error but continue - feature detection is optional
                System.err.println("Warning: Oracle feature detection failed: " + e.getMessage());
            }
        }
    }

    /**
     * Configure Oracle-optimized JDBC template
     */
    @Bean
    @Primary
    public JdbcTemplate oracleJdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Oracle-specific optimizations
        jdbcTemplate.setFetchSize(1000);
        jdbcTemplate.setMaxRows(0); // No limit
        jdbcTemplate.setQueryTimeout(300); // 5 minutes
        
        return jdbcTemplate;
    }

    public static class Features {
        private Detection detection = new Detection();
        private Cache cache = new Cache();

        public Detection getDetection() {
            return detection;
        }

        public void setDetection(Detection detection) {
            this.detection = detection;
        }

        public Cache getCache() {
            return cache;
        }

        public void setCache(Cache cache) {
            this.cache = cache;
        }
    }

    public static class Detection {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Cache {
        private int ttl = 3600; // 1 hour

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
    }
}
