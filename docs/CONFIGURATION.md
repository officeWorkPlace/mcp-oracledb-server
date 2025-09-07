# MCP Oracle Database Server - Configuration Guide

> **Comprehensive configuration guide for Oracle MCP Server**  
> **Oracle versions, connection pooling, security, and performance tuning**

[![Oracle](https://img.shields.io/badge/Oracle-11g--23c-red.svg)](https://www.oracle.com/database/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![HikariCP](https://img.shields.io/badge/HikariCP-Oracle_Optimized-blue.svg)](https://github.com/brettwooldridge/HikariCP)

---

## Table of Contents

- [Quick Start Configuration](#quick-start-configuration)
- [Oracle Version-Specific Configuration](#oracle-version-specific-configuration)
- [Connection Pool Configuration](#connection-pool-configuration)
- [Security Configuration](#security-configuration)
- [Performance Tuning](#performance-tuning)
- [MCP-Specific Configuration](#mcp-specific-configuration)
- [Environment-Specific Profiles](#environment-specific-profiles)
- [Monitoring Configuration](#monitoring-configuration)
- [Advanced Oracle Features](#advanced-oracle-features)

---

## Quick Start Configuration

### Basic Oracle Connection

Create `application.properties` in `src/main/resources/`:

```properties
# Basic Oracle Connection
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=hr
spring.datasource.password=password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# MCP Configuration
mcp.tools.exposure=public
mcp.transport=stdio
mcp.edition=enhanced

# Oracle Feature Detection
oracle.features.detection.enabled=true
oracle.features.cache.ttl=3600
```

### Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  oracle-xe:
    image: gvenzl/oracle-xe:21-slim
    environment:
      ORACLE_PASSWORD: oracle
      ORACLE_DATABASE: XEPDB1
    ports:
      - "1521:1521"
    volumes:
      - oracle_data:/opt/oracle/oradata

  mcp-oracle-server:
    build: .
    environment:
      SPRING_DATASOURCE_URL: jdbc:oracle:thin:@oracle-xe:1521/XEPDB1
      SPRING_DATASOURCE_USERNAME: hr
      SPRING_DATASOURCE_PASSWORD: oracle
      SPRING_PROFILES_ACTIVE: enhanced,docker
    ports:
      - "8080:8080"
    depends_on:
      - oracle-xe

volumes:
  oracle_data:
```

---

## Oracle Version-Specific Configuration

### Oracle 23c (Latest with AI/Vector Support)

```yaml
# application-oracle23c.yml
spring:
  profiles:
    active: oracle23c,enhanced

oracle:
  version: "23c"
  features:
    vector:
      enabled: true
      dimension: 1024
    json:
      enabled: true
      native_support: true
    graph:
      enabled: true
    multitenant:
      enabled: true
    ai:
      vector_search: true
      json_search: true

# Oracle 23c specific JDBC properties
spring:
  datasource:
    hikari:
      data-source-properties:
        oracle.jdbc.vectorSupport: true
        oracle.jdbc.jsonSupport: true
        oracle.jdbc.graphSupport: true
```

### Oracle 19c (Enterprise with AWR)

```yaml
# application-oracle19c.yml
spring:
  profiles:
    active: oracle19c,enterprise

oracle:
  version: "19c"
  features:
    awr:
      enabled: true
      retention_days: 30
    partitioning:
      enabled: true
    compression:
      enabled: true
    multitenant:
      enabled: true
    rac:
      enabled: false

# Enterprise features
mcp:
  edition: enterprise
  tools:
    total: 75
```

### Oracle 12c (First PDB Support)

```yaml
# application-oracle12c.yml
spring:
  profiles:
    active: oracle12c,enhanced

oracle:
  version: "12c"
  features:
    multitenant:
      enabled: true
      pdb_auto_detect: true
    json:
      enabled: false
      legacy_support: true
    xml:
      enabled: true

# 12c specific connection
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/pdborcl
```

### Oracle 11g (Legacy Support)

```yaml
# application-oracle11g.yml
spring:
  profiles:
    active: oracle11g,enhanced

oracle:
  version: "11g"
  features:
    multitenant:
      enabled: false
    awr:
      enabled: true
      limited_features: true
    xml:
      enabled: true

# 11g optimizations
spring:
  datasource:
    hikari:
      data-source-properties:
        oracle.jdbc.V8Compatible: true
        oracle.jdbc.mapDateToTimestamp: false
```

---

## Connection Pool Configuration

### Production HikariCP Settings

```properties
# Production Connection Pool Configuration
spring.datasource.type=com.zaxxer.hikari.HikariDataSource

# Pool Sizing (adjust based on load)
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000

# Connection Lifecycle
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.validation-timeout=5000

# Leak Detection (development/testing)
spring.datasource.hikari.leak-detection-threshold=300000

# Pool Name
spring.datasource.hikari.pool-name=OracleCP
```

### Oracle-Specific JDBC Properties

```properties
# Oracle Network Configuration
spring.datasource.hikari.data-source-properties.oracle.net.CONNECT_TIMEOUT=60000
spring.datasource.hikari.data-source-properties.oracle.net.READ_TIMEOUT=60000
spring.datasource.hikari.data-source-properties.oracle.net.keepAlive=true

# Oracle JDBC Optimizations
spring.datasource.hikari.data-source-properties.oracle.jdbc.defaultRowPrefetch=100
spring.datasource.hikari.data-source-properties.oracle.jdbc.useFetchSizeWithLongColumn=true
spring.datasource.hikari.data-source-properties.oracle.jdbc.implicitStatementCacheSize=50
spring.datasource.hikari.data-source-properties.oracle.jdbc.explicitStatementCacheSize=50

# Oracle Performance
spring.datasource.hikari.data-source-properties.oracle.jdbc.fanEnabled=false
spring.datasource.hikari.data-source-properties.oracle.jdbc.processEscapes=false
spring.datasource.hikari.data-source-properties.oracle.jdbc.remarksReporting=false
```

### RAC (Real Application Clusters) Configuration

```properties
# Oracle RAC Connection String
spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=\
  (ADDRESS_LIST=\
    (ADDRESS=(PROTOCOL=TCP)(HOST=rac-node1)(PORT=1521))\
    (ADDRESS=(PROTOCOL=TCP)(HOST=rac-node2)(PORT=1521))\
  )\
  (CONNECT_DATA=\
    (SERVICE_NAME=orcl)\
    (FAILOVER_MODE=(TYPE=SELECT)(METHOD=BASIC))\
  )\
)

# RAC-specific properties
spring.datasource.hikari.data-source-properties.oracle.jdbc.fastConnectionFailover=true
spring.datasource.hikari.data-source-properties.oracle.net.outbound_connect_timeout=5000
```

---

## Security Configuration

### SSL/TLS Configuration

```properties
# Oracle SSL/TLS Configuration
spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=\
  (ADDRESS=(PROTOCOL=tcps)(HOST=secure-oracle.company.com)(PORT=2484))\
  (CONNECT_DATA=(SERVICE_NAME=secure_service))\
  (SECURITY=(SSL_SERVER_CERT_DN="CN=secure-oracle.company.com"))\
)

# SSL Properties
oracle.security.ssl.enabled=true
oracle.security.ssl.truststore.location=/path/to/oracle/truststore.jks
oracle.security.ssl.truststore.password=${SSL_TRUSTSTORE_PASSWORD}
oracle.security.ssl.keystore.location=/path/to/oracle/keystore.jks
oracle.security.ssl.keystore.password=${SSL_KEYSTORE_PASSWORD}

# Certificate Validation
spring.datasource.hikari.data-source-properties.oracle.net.ssl_server_dn_match=true
spring.datasource.hikari.data-source-properties.oracle.net.ssl_version=1.2
```

### Oracle Wallet Configuration

```properties
# Oracle Wallet (Recommended for Production)
spring.datasource.url=jdbc:oracle:thin:/@wallet_service_name
oracle.security.wallet.enabled=true
oracle.security.wallet.location=/path/to/oracle/wallet
oracle.security.wallet.password=${ORACLE_WALLET_PASSWORD}

# Wallet Properties
spring.datasource.hikari.data-source-properties.oracle.net.wallet_location=${oracle.security.wallet.location}
spring.datasource.hikari.data-source-properties.oracle.net.wallet_password=${oracle.security.wallet.password}
```

### Application Security

```properties
# Spring Security Configuration
spring.security.user.name=admin
spring.security.user.password=${ADMIN_PASSWORD:changeme}
spring.security.user.roles=ADMIN,MCP_USER

# Management Endpoints Security
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=when_authorized
management.endpoint.health.roles=ADMIN

# Oracle-specific security
oracle.security.sql_injection_protection=true
oracle.security.system_user_protection=true
oracle.security.ddl_validation=true
```

---

## Performance Tuning

### JVM Configuration

```bash
#!/bin/bash
# production-jvm-settings.sh

export JAVA_OPTS="
  # Garbage Collection
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:G1HeapRegionSize=16m
  -XX:G1NewSizePercent=30
  -XX:G1MaxNewSizePercent=40
  
  # Memory Management
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75.0
  -XX:InitialRAMPercentage=50.0
  
  # Oracle JDBC Optimizations
  -Doracle.jdbc.fanEnabled=false
  -Doracle.net.keepAlive=true
  -Doracle.jdbc.autoCommitSpecCompliant=false
  
  # Monitoring
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp/heapdump.hprof
  -XX:+UseContainerSupport
  
  # JFR (Java Flight Recorder)
  -XX:+FlightRecorder
  -XX:StartFlightRecording=duration=60s,filename=oracle-mcp.jfr
"

java $JAVA_OPTS -jar mcp-oracledb-server-1.0.0-PRODUCTION.jar
```

### Oracle Query Optimization

```properties
# Oracle Query Performance
oracle.query.timeout=300
oracle.query.fetch_size=1000
oracle.query.max_rows=10000

# Oracle Hints Configuration
oracle.hints.parallel.enabled=true
oracle.hints.parallel.default_degree=4
oracle.hints.first_rows.enabled=true
oracle.hints.cost_optimization=true

# Statement Caching
oracle.statement.cache.enabled=true
oracle.statement.cache.size=100
oracle.statement.cache.type=explicit
```

### Spring Boot Performance

```properties
# Spring Boot Optimizations
spring.jpa.open-in-view=false
spring.datasource.hikari.auto-commit=false
spring.transaction.default-timeout=300

# Logging Performance
logging.level.org.springframework.jdbc.core=WARN
logging.level.com.zaxxer.hikari=WARN
logging.level.oracle.jdbc=WARN

# Actuator Performance
management.endpoint.metrics.cache.time-to-live=30s
management.endpoint.health.cache.time-to-live=10s
```

---

## MCP-Specific Configuration

### MCP Protocol Settings

```yaml
# MCP Configuration
mcp:
  # Transport Configuration
  transport: stdio  # stdio, rest, or websocket
  
  # Tool Configuration
  tools:
    exposure: public  # public, private, or selective
    timeout: 300     # seconds
    batch_size: 100  # operations per batch
    
  # Edition Configuration
  edition: enhanced  # enhanced (55+ tools) or enterprise (75+ tools)
  
  # Response Configuration
  responses:
    format: structured  # structured or raw
    include_metadata: true
    include_performance_stats: true

# Oracle-specific MCP settings
oracle:
  mcp:
    feature_detection: true
    version_adaptation: true
    error_translation: true
    performance_monitoring: true
```

### Tool Exposure Configuration

```yaml
# Selective Tool Exposure
mcp:
  tools:
    exposure: selective
    enabled_categories:
      - database_management
      - schema_management
      - table_operations
      - performance_analysis
    disabled_tools:
      - oracle_drop_database
      - oracle_truncate_table
    
    # Enterprise Tools (requires enterprise edition)
    enterprise:
      enabled: true
      security_tools: true
      performance_tools: true
      monitoring_tools: true
```

---

## Environment-Specific Profiles

### Development Profile

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: dev_user
    password: dev_password
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1

# Development-friendly settings
logging:
  level:
    com.deepai.mcpserver: DEBUG
    org.springframework.jdbc.core: DEBUG

oracle:
  features:
    detection:
      enabled: true
    cache:
      ttl: 60  # Short cache for development

mcp:
  tools:
    exposure: public
  responses:
    include_performance_stats: true
```

### Production Profile

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: ${ORACLE_DATABASE_URL}
    username: ${ORACLE_USERNAME}
    password: ${ORACLE_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      leak-detection-threshold: 300000

# Production logging
logging:
  level:
    root: INFO
    com.deepai.mcpserver: INFO
    org.springframework.jdbc.core: WARN

# Production Oracle settings
oracle:
  features:
    cache:
      ttl: 3600
  security:
    ssl:
      enabled: true
    wallet:
      enabled: true

mcp:
  tools:
    exposure: selective
  responses:
    include_performance_stats: false
```

### Testing Profile

```yaml
# application-test.yml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

# Test-specific settings
oracle:
  features:
    detection:
      enabled: false
    mock:
      enabled: true

mcp:
  tools:
    exposure: public
  responses:
    mock_responses: true
```

---

## Monitoring Configuration

### Actuator Configuration

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,metrics,prometheus,info,oracle
management.endpoints.web.base-path=/actuator

# Health Indicators
management.health.oracle.enabled=true
management.health.oracle-feature-detector.enabled=true
management.health.hikaricp.enabled=true

# Custom Metrics
management.metrics.export.prometheus.enabled=true
management.metrics.tags.application=mcp-oracle-server
management.metrics.tags.environment=${ENVIRONMENT:dev}
```

### Custom Oracle Metrics

```yaml
# Custom Oracle Monitoring
oracle:
  monitoring:
    enabled: true
    metrics:
      connection_pool: true
      query_performance: true
      tool_usage: true
      feature_detection: true
    
    alerts:
      slow_query_threshold: 5000  # milliseconds
      connection_pool_threshold: 80  # percentage
      error_rate_threshold: 5  # percentage
```

### Logging Configuration

```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender for Oracle Operations -->
    <appender name="ORACLE_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/oracle-operations.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/oracle-operations.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Oracle-specific logging -->
    <logger name="com.deepai.mcpserver.service" level="INFO" additivity="false">
        <appender-ref ref="ORACLE_FILE"/>
        <appender-ref ref="CONSOLE"/>
    </logger>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

---

## Advanced Oracle Features

### Vector Database Configuration (Oracle 23c)

```yaml
# Oracle 23c Vector Configuration
oracle:
  vector:
    enabled: true
    default_dimension: 1024
    distance_metrics:
      - cosine
      - euclidean
      - manhattan
    index_type: ivf
    
    # Vector search optimization
    search:
      parallel_enabled: true
      cache_enabled: true
      approximate_search: true
```

### JSON Configuration

```properties
# Oracle JSON Configuration
oracle.json.enabled=true
oracle.json.binary_format=oson
oracle.json.validation=strict
oracle.json.pretty_print=false

# JSON search configuration
oracle.json.search.case_sensitive=false
oracle.json.search.whole_words_only=false
oracle.json.index.type=functional
```

### Partitioning Configuration

```yaml
# Oracle Partitioning Configuration
oracle:
  partitioning:
    enabled: true
    auto_detection: true
    types:
      - range
      - hash
      - list
      - composite
    
    optimization:
      partition_pruning: true
      parallel_execution: true
      compression: true
```

---

## Configuration Validation

### Startup Validation

```java
// Custom Configuration Validation
@ConfigurationProperties(prefix = "oracle")
@Validated
public class OracleConfigurationProperties {
    
    @NotNull
    @Pattern(regexp = "^(11g|12c|18c|19c|21c|23c)$")
    private String version;
    
    @NotNull
    private Features features = new Features();
    
    @Valid
    private Security security = new Security();
}
```

### Health Checks

```properties
# Custom Health Checks
management.health.oracle.query=SELECT 1 FROM DUAL
management.health.oracle.timeout=10s
management.health.oracle-feature-detector.cache-validation=true
```

---

## Configuration Examples by Use Case

### AI/ML Workloads (Oracle 23c)

```yaml
# AI/ML optimized configuration
spring:
  profiles:
    active: oracle23c,ai-optimized

oracle:
  version: "23c"
  features:
    vector:
      enabled: true
      dimension: 1536
      index_type: ivf
    json:
      enabled: true
      binary_format: oson
  
  performance:
    parallel_execution: true
    memory_optimization: vector_workload

mcp:
  edition: enterprise
  tools:
    ai_enabled: true
    vector_search: true
```

### Enterprise Production

```yaml
# Enterprise production configuration
spring:
  profiles:
    active: oracle19c,enterprise,prod

oracle:
  version: "19c"
  features:
    awr: 
      enabled: true
      retention_days: 90
    partitioning:
      enabled: true
    compression:
      enabled: true
    encryption:
      enabled: true

  security:
    ssl:
      enabled: true
    wallet:
      enabled: true
    audit:
      enabled: true

mcp:
  edition: enterprise
  tools:
    total: 75
    enterprise_features: true
```

---

## Troubleshooting Configuration Issues

### Common Configuration Problems

1. **Oracle Driver Not Found**
   ```properties
   # Ensure Oracle JDBC driver is included
   spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
   ```

2. **Connection Pool Exhaustion**
   ```properties
   # Increase pool size or reduce connection timeout
   spring.datasource.hikari.maximum-pool-size=50
   spring.datasource.hikari.connection-timeout=30000
   ```

3. **Feature Detection Failures**
   ```properties
   # Enable debug logging
   logging.level.com.deepai.mcpserver.util.OracleFeatureDetector=DEBUG
   oracle.features.detection.retry_count=3
   ```

### Configuration Validation Commands

```bash
# Validate Oracle connection
java -jar mcp-oracle-server.jar --spring.profiles.active=config-test

# Check configuration properties
curl http://localhost:8080/actuator/configprops

# Validate Oracle features
curl http://localhost:8080/actuator/health/oracle
```

---

## Conclusion

This configuration guide provides comprehensive settings for:

- **Multi-version Oracle support** (11g through 23c)
- **Production-ready connection pooling** with HikariCP
- **Enterprise security** with SSL/TLS and Oracle Wallet
- **Performance optimization** for Oracle workloads
- **MCP-specific settings** for AI/LLM integration
- **Environment-specific profiles** for dev/test/prod
- **Advanced Oracle features** like Vector DB and JSON

Use these configurations as templates and adjust based on your specific Oracle environment and requirements.

---

**Configuration Guide v1.0.0-PRODUCTION**  
*Optimized for Oracle 11g-23c environments by officeWorkPlace*
