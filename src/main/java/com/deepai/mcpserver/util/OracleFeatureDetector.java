package com.deepai.mcpserver.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oracle Feature Detector - Multi-Version Support Utility
 * 
 * Detects Oracle database version and available features to enable
 * version-specific functionality across Oracle 11g-23c.
 * 
 * Supported Features:
 * - Pluggable Databases (12c+)
 * - Vector Search (23c+)
 * - AWR/ADDM (requires Diagnostics Pack)
 * - JSON support (12c+)
 * - Multitenant architecture (12c+)
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@Component
public class OracleFeatureDetector {

    private static final Logger logger = LoggerFactory.getLogger(OracleFeatureDetector.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final Map<String, Object> featureCache = new ConcurrentHashMap<>();
    private String oracleVersion;
    private int majorVersion;
    private int minorVersion;

    /**
     * Initialize Oracle version detection and feature cache
     */
    public void detectOracleFeatures() {
        try {
            // Get Oracle version information
            Map<String, Object> versionInfo = jdbcTemplate.queryForMap(
                "SELECT version, version_legacy FROM v$instance");
            
            this.oracleVersion = (String) versionInfo.get("version");
            parseVersion(this.oracleVersion);
            
            // Cache feature availability
            featureCache.put("version", this.oracleVersion);
            featureCache.put("majorVersion", this.majorVersion);
            featureCache.put("minorVersion", this.minorVersion);
            featureCache.put("pdbSupport", supportsPDBs());
            featureCache.put("vectorSupport", supportsVectorSearch());
            featureCache.put("jsonSupport", supportsJSON());
            featureCache.put("awrSupport", supportsAWR());
            
            logger.info("Oracle version detected: {}, features cached", this.oracleVersion);
            
        } catch (Exception e) {
            logger.error("Failed to detect Oracle features: {}", e.getMessage());
            // Set default values for graceful degradation
            this.oracleVersion = "Unknown";
            this.majorVersion = 11;
            this.minorVersion = 0;
        }
    }

    /**
     * Check if Oracle supports Pluggable Databases (12c+)
     */
    public boolean supportsPDBs() {
        if (featureCache.containsKey("pdbSupport")) {
            return (Boolean) featureCache.get("pdbSupport");
        }
        
        try {
            // Check if PDB views exist and we're in a multitenant environment
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dba_pdbs", Integer.class);
            Map<String, Object> cdbCheck = jdbcTemplate.queryForMap("SELECT cdb FROM v$database");
            boolean isCdb = "YES".equals(cdbCheck.get("cdb"));
            
            featureCache.put("pdbSupport", isCdb && majorVersion >= 12);
            return isCdb && majorVersion >= 12;
        } catch (Exception e) {
            featureCache.put("pdbSupport", false);
            return false;
        }
    }

    /**
     * Check if Oracle supports Vector Search (23c+)
     */
    public boolean supportsVectorSearch() {
        if (featureCache.containsKey("vectorSupport")) {
            return (Boolean) featureCache.get("vectorSupport");
        }
        
        try {
            // Check for vector data type availability
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dba_types WHERE type_name = 'VECTOR'", Integer.class);
            
            boolean vectorSupport = majorVersion >= 23;
            featureCache.put("vectorSupport", vectorSupport);
            return vectorSupport;
        } catch (Exception e) {
            featureCache.put("vectorSupport", false);
            return false;
        }
    }

    /**
     * Check if Oracle supports JSON operations (12c+)
     */
    public boolean supportsJSON() {
        if (featureCache.containsKey("jsonSupport")) {
            return (Boolean) featureCache.get("jsonSupport");
        }
        
        try {
            // Check for JSON functions
            jdbcTemplate.queryForObject("SELECT JSON_VALUE('{}', '$.test') FROM dual", String.class);
            
            boolean jsonSupport = majorVersion >= 12;
            featureCache.put("jsonSupport", jsonSupport);
            return jsonSupport;
        } catch (Exception e) {
            featureCache.put("jsonSupport", false);
            return false;
        }
    }

    /**
     * Check if AWR (Automatic Workload Repository) is available
     * Note: Requires Oracle Diagnostics Pack license
     */
    public boolean supportsAWR() {
        if (featureCache.containsKey("awrSupport")) {
            return (Boolean) featureCache.get("awrSupport");
        }
        
        try {
            // Check if AWR views are accessible
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dba_hist_snapshot WHERE rownum = 1", Integer.class);
            
            boolean awrSupport = majorVersion >= 10; // AWR available since 10g
            featureCache.put("awrSupport", awrSupport);
            return awrSupport;
        } catch (Exception e) {
            featureCache.put("awrSupport", false);
            return false;
        }
    }

    /**
     * Get comprehensive version information
     */
    public Map<String, Object> getVersionInfo() {
        if (oracleVersion == null) {
            detectOracleFeatures();
        }
        
        return Map.of(
            "version", oracleVersion,
            "majorVersion", majorVersion,
            "minorVersion", minorVersion,
            "features", Map.of(
                "pdbSupport", supportsPDBs(),
                "vectorSupport", supportsVectorSearch(),
                "jsonSupport", supportsJSON(),
                "awrSupport", supportsAWR()
            )
        );
    }

    /**
     * Parse Oracle version string to extract major and minor versions
     */
    private void parseVersion(String version) {
        try {
            if (version != null && version.contains(".")) {
                String[] parts = version.split("\\.");
                this.majorVersion = Integer.parseInt(parts[0]);
                if (parts.length > 1) {
                    this.minorVersion = Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse Oracle version: {}", version);
            this.majorVersion = 11; // Default to 11g
            this.minorVersion = 0;
        }
    }

    /**
     * Clear feature cache (useful for testing or configuration changes)
     */
    public void clearCache() {
        featureCache.clear();
        logger.info("Oracle feature cache cleared");
    }
}
