# Oracle MCP Server - Claude Desktop Configuration Guide

## 📋 Complete Configuration

### Basic Configuration (Recommended)
```json
{
  "mcpServers": {
    "oracle-mcp-server": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/path/to/your/mcp-oracledb-server/target/mcp-oracledb-server-1.0.0-PRODUCTION.jar",
        "--spring.profiles.active=mcp-run"
      ],
      "env": {
        "ORACLE_DB_URL": "jdbc:oracle:thin:@localhost:1521:XE",
        "ORACLE_DB_USER": "your_oracle_username",
        "ORACLE_DB_PASSWORD": "your_oracle_password",
        "ORACLE_HOST": "localhost",
        "ORACLE_PORT": "1521",
        "ORACLE_SID": "XE",
        "MCP_TOOLS_EXPOSURE": "all",
        "ENTERPRISE_ENABLED": "true"
      },
      "description": "Oracle MCP Server - Production Edition with Professional Visualization & 73+ Tools",
      "features": {
        "toolCount": "73+",
        "transport": "stdio", 
        "oracleVersion": "21c",
        "visualization": "professional",
        "edition": "enterprise"
      }
    }
  }
}
```

### Performance Optimized Configuration (Maximum Features)
```json
{
  "mcpServers": {
    "oracle-mcp-server": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/path/to/your/mcp-oracledb-server/target/mcp-oracledb-server-1.0.0-PRODUCTION.jar",
        "--spring.profiles.active=mcp-run"
      ],
      "env": {
        "ORACLE_DB_URL": "jdbc:oracle:thin:@your_host:your_port:your_sid",
        "ORACLE_DB_USER": "your_oracle_username",
        "ORACLE_DB_PASSWORD": "your_oracle_password",
        "ORACLE_HOST": "your_oracle_host",
        "ORACLE_PORT": "your_oracle_port",
        "ORACLE_SID": "your_oracle_sid",
        "MCP_TOOLS_EXPOSURE": "all",
        "ENTERPRISE_ENABLED": "true",
        "ORACLE_FEATURES_CACHE_TTL": "3600",
        "SPRING_PROFILES_INCLUDE": "performance"
      },
      "description": "Oracle MCP Server - Production Edition with Professional Visualization & 73+ Tools",
      "features": {
        "toolCount": "73+",
        "transport": "stdio",
        "oracleVersion": "21c",
        "visualization": "professional",
        "edition": "enterprise",
        "caching": "optimized",
        "performance": "enhanced"
      }
    }
  }
}
```

## 🔧 Environment Variables Reference

### Required Variables
| Variable | Description | Example |
|----------|-------------|---------|
| `ORACLE_DB_URL` | Full JDBC connection string | `jdbc:oracle:thin:@localhost:1521:XE` |
| `ORACLE_DB_USER` | Oracle database username | `C##your_schema` |
| `ORACLE_DB_PASSWORD` | Oracle database password | `your_password` |
| `ORACLE_HOST` | Oracle database host | `localhost` |
| `ORACLE_PORT` | Oracle database port | `1521` |
| `ORACLE_SID` | Oracle database SID | `XE` |

### Tool Control Variables
| Variable | Values | Description |
|----------|--------|-------------|
| `MCP_TOOLS_EXPOSURE` | `all`, `public`, `core` | Controls which tools are exposed |
| `ENTERPRISE_ENABLED` | `true`, `false` | Enables enterprise security & performance tools |

### Performance Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `ORACLE_FEATURES_CACHE_TTL` | `3600` | Cache TTL in seconds |
| `SPRING_PROFILES_INCLUDE` | - | Additional Spring profiles (e.g., `performance`) |

## 🚀 Feature Unlocking

### Tool Categories by Configuration

#### Basic Configuration (55 tools)
- ✅ Core Operations: 25 tools
- ✅ Advanced Analytics: 20 tools  
- ✅ AI-Powered Tools: 8 tools
- ❌ Enterprise Security: 0 tools
- ❌ Enterprise Performance: 0 tools

#### With `ENTERPRISE_ENABLED=true` (73+ tools)
- ✅ Core Operations: 25 tools
- ✅ Advanced Analytics: 20 tools
- ✅ AI-Powered Tools: 8 tools
- ✅ Enterprise Security: 10 tools
- ✅ Enterprise Performance: 10+ tools

#### With `MCP_TOOLS_EXPOSURE=all` + Performance Config (75+ tools)
- ✅ All above categories
- ✅ Professional Visualization: 6+ advanced chart generators
- ✅ Performance optimizations
- ✅ Extended caching capabilities

## 📊 Professional Visualization Features

### Available Chart Types
- **Executive Dashboards**: Multi-metric corporate dashboards
- **Gradient Area Charts**: Time-series with gradient fills
- **Interactive Heatmaps**: Correlation and density visualizations
- **Candlestick Charts**: Financial OHLC charts with wicks
- **Radar Charts**: Multi-dimensional analysis
- **Sunburst Charts**: Hierarchical data visualization

### Theme Support
- `corporate`: Professional business theme
- `executive`: High-contrast executive theme  
- `financial`: Financial industry optimized theme

## 🛠️ Installation Steps

### 1. Build the JAR file
```bash
mvn clean package -DskipTests
```

### 2. Locate Configuration File
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

### 3. Update Configuration
Replace the `mcpServers` section with one of the configurations above.

### 4. Customize Paths and Credentials
- Update JAR path to your actual location
- Replace database credentials with your values
- Adjust host/port if not using local Oracle XE

### 5. Restart Claude Desktop
Close and reopen Claude Desktop to apply changes.

## ✅ Verification

### Check Server Status
1. Open Claude Desktop
2. Look for "Oracle MCP Server" in available tools
3. Try a simple query: "Show me all tables in the database"
4. Verify visualization capabilities: "Create a chart of loan amounts by branch"

### Expected Tool Count
- **Basic**: 55+ tools
- **Enterprise**: 73+ tools
- **Performance Optimized**: 75+ tools

## 🔍 Troubleshooting

### Common Issues

#### Server Not Starting
- Check JAR file path exists
- Verify Java is installed and accessible
- Ensure Oracle database is running

#### Connection Failed
- Verify Oracle database credentials
- Check `ORACLE_DB_URL` format
- Ensure Oracle listener is running on specified port

#### Limited Tools Available
- Set `MCP_TOOLS_EXPOSURE=all`
- Enable `ENTERPRISE_ENABLED=true`
- Check Claude Desktop logs for errors

### Performance Issues
- Add `ORACLE_FEATURES_CACHE_TTL=3600`
- Include `SPRING_PROFILES_INCLUDE=performance`
- Increase JVM memory: Add `-Xmx2g` to Java args

## 📈 Advanced Configuration

### Custom JVM Options
```json
"args": [
  "-Xmx2g",
  "-XX:+UseG1GC",
  "-jar",
  "/path/to/mcp-oracledb-server-1.0.0-PRODUCTION.jar",
  "--spring.profiles.active=mcp-run"
]
```

### Multiple Oracle Instances
```json
{
  "mcpServers": {
    "oracle-prod": {
      "env": {
        "ORACLE_DB_URL": "jdbc:oracle:thin:@prod-host:1521:PROD"
      }
    },
    "oracle-dev": {
      "env": {
        "ORACLE_DB_URL": "jdbc:oracle:thin:@dev-host:1521:DEV"
      }
    }
  }
}
```

## 🔐 Security Best Practices

### Environment Variable Security
- Use environment variables for sensitive credentials
- Consider using Oracle Wallet for credential management
- Limit database user permissions to required schemas only

### Network Security
- Use SSL/TLS for production databases
- Configure firewall rules appropriately
- Monitor database connections and access patterns

## 📚 Additional Resources

- [Oracle MCP Server Architecture](./ARCHITECTURE.md)
- [Performance Tuning Guide](./PERFORMANCE_TUNING.md)
- [Visualization Examples](./VISUALIZATION_EXAMPLES.md)
- [Troubleshooting Guide](./TROUBLESHOOTING.md)

---
**Oracle MCP Server v1.0.0-PRODUCTION** - Enhanced Edition with Professional Visualization
