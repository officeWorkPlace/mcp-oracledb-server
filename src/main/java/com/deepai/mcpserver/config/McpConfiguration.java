package com.deepai.mcpserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * MCP Configuration - Tool Exposure and Transport Settings
 * 
 * Configures MCP (Model Context Protocol) settings including:
 * - Tool exposure modes (public vs all)
 * - Transport protocol (stdio vs REST)
 * - Oracle feature enablement
 * - Profile-based tool activation
 * 
 * Supports:
 * - Enhanced Edition: 55+ tools
 * - Enterprise Edition: 75+ tools
 * - Development mode: All tools exposed
 * - Production mode: Public tools only
 * 
 * @author officeWorkPlace
 * @version 1.0.0-PRODUCTION
 */
@Configuration
@ConfigurationProperties(prefix = "mcp")
public class McpConfiguration {

    private Tools tools = new Tools();
    private String transport = "stdio";

    public Tools getTools() {
        return tools;
    }

    public void setTools(Tools tools) {
        this.tools = tools;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public static class Tools {
        private String exposure = "public"; // public | all
        private Oracle oracle = new Oracle();

        public String getExposure() {
            return exposure;
        }

        public void setExposure(String exposure) {
            this.exposure = exposure;
        }

        public Oracle getOracle() {
            return oracle;
        }

        public void setOracle(Oracle oracle) {
            this.oracle = oracle;
        }
    }

    public static class Oracle {
        private Features features = new Features();

        public Features getFeatures() {
            return features;
        }

        public void setFeatures(Features features) {
            this.features = features;
        }
    }

    public static class Features {
        private Detection detection = new Detection();
        private ToolCategories tools = new ToolCategories();

        public Detection getDetection() {
            return detection;
        }

        public void setDetection(Detection detection) {
            this.detection = detection;
        }

        public ToolCategories getTools() {
            return tools;
        }

        public void setTools(ToolCategories tools) {
            this.tools = tools;
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

    public static class ToolCategories {
        private ToolCategory core = new ToolCategory(true, 25);
        private ToolCategory analytics = new ToolCategory(true, 20);
        private ToolCategory ai = new ToolCategory(true, 10);
        private Enterprise enterprise = new Enterprise();

        public ToolCategory getCore() {
            return core;
        }

        public void setCore(ToolCategory core) {
            this.core = core;
        }

        public ToolCategory getAnalytics() {
            return analytics;
        }

        public void setAnalytics(ToolCategory analytics) {
            this.analytics = analytics;
        }

        public ToolCategory getAi() {
            return ai;
        }

        public void setAi(ToolCategory ai) {
            this.ai = ai;
        }

        public Enterprise getEnterprise() {
            return enterprise;
        }

        public void setEnterprise(Enterprise enterprise) {
            this.enterprise = enterprise;
        }
    }

    public static class ToolCategory {
        private boolean enabled = true;
        private int count;

        public ToolCategory() {}

        public ToolCategory(boolean enabled, int count) {
            this.enabled = enabled;
            this.count = count;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class Enterprise {
        private boolean enabled = false;
        private int security = 10;
        private int performance = 10;
        private int multitenant = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getSecurity() {
            return security;
        }

        public void setSecurity(int security) {
            this.security = security;
        }

        public int getPerformance() {
            return performance;
        }

        public void setPerformance(int performance) {
            this.performance = performance;
        }

        public int getMultitenant() {
            return multitenant;
        }

        public void setMultitenant(int multitenant) {
            this.multitenant = multitenant;
        }
    }
}
