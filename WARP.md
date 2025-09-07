# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**MCP Oracle Database Server** - A production-ready Model Context Protocol (MCP) server providing comprehensive Oracle database operations with AI-enhanced capabilities. Built with Spring Boot 3.4.5 and Spring AI 1.0.1, supporting Oracle versions 11g through 23c.

- **Enhanced Edition**: 55+ Oracle-specific tools (41% more than MongoDB baseline)
- **Enterprise Edition**: 75+ tools with advanced Oracle enterprise features (92% more capabilities)
- **Architecture**: Raw JDBC with Spring JDBC Template for maximum Oracle performance
- **Key Features**: Dynamic Oracle version detection, PDB support, AWR integration, Vector search (23c)

## Development Commands

### Building and Running

```bash
# Build Enhanced Edition (default - 55+ tools)
mvn clean package -Penhanced

# Build Enterprise Edition (75+ tools)  
mvn clean package -Penterprise

# Run in development mode
mvn spring-boot:run

# Run with specific Oracle version profile
mvn spring-boot:run -Dspring-boot.run.profiles=oracle19c,enterprise

# Run in MCP stdio mode (production)
java -jar target/mcp-oracledb-server-1.0.0-PRODUCTION.jar

# Run in REST API mode (testing)
java -jar target/mcp-oracledb-server-1.0.0-PRODUCTION.jar --spring.profiles.active=rest
```

### Testing

```bash
# Run all unit tests
mvn test

# Run integration tests with Testcontainers
mvn test -Pintegration-tests

# Run integration tests with real Oracle database
mvn test -Dtest=OracleIntegrationTest

# Run tool validation tests
mvn test -Dtest=AllOracleToolsValidationTest

# Run complete test suite (unit + integration + coverage)
mvn test -Ptest-all

# Run performance tests
mvn test -Pperformance-test

# Run security tests
mvn test -Psecurity-test

# Generate test coverage report (requires 90% minimum)
mvn test -Ptest-coverage
```

### Database Setup and Testing

```bash
# Setup test data (requires Oracle connection)
mvn test -Ptest-data-setup

# Verify Oracle connection
curl -X POST http://localhost:8080/v1/tools/oracle_ping

# List available Oracle tools
curl -X POST http://localhost:8080/v1/tools/list

# Test database listing
curl -X POST "http://localhost:8080/v1/tools/oracle_list_databases" \
  -H "Content-Type: application/json" \
  -d '{"includePdbs": true}'
```

## High-Level Architecture

### Core Design Principle: Oracle-First Architecture

This project deliberately uses **raw JDBC with Spring JDBC Template** instead of JPA/Hibernate to maximize Oracle-specific capabilities:

- **Direct Oracle System View Access**: Query `v$database`, `dba_users`, `all_tables` without ORM limitations
- **Oracle Enterprise Features**: RMAN backup, AWR reports, PDB operations, partitioning
- **Performance Optimization**: 50-80% faster than ORM approach with Oracle-specific hints
- **Multi-Version Support**: Dynamic feature detection across Oracle 11g-23c

### Service Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    MCP Client (AI/LLM)                     │
├─────────────────────────────────────────────────────────────┤
│                   MCP Protocol Layer                       │
├─────────────────────────────────────────────────────────────┤
│              Spring Boot Application Layer                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌──────────────┐ │
│  │   Tool Layer    │ │ Service Layer   │ │ Config Layer │ │
│  │   (75+ Tools)   │ │  (Business)     │ │  (Oracle)    │ │
│  └─────────────────┘ └─────────────────┘ └──────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                Data Access Layer (JDBC)                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌──────────────┐ │
│  │  JdbcTemplate   │ │  SqlBuilder     │ │   Feature    │ │
│  │                 │ │                 │ │  Detector    │ │
│  └─────────────────┘ └─────────────────┘ └──────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                     Oracle Database                        │
│         (11g, 12c, 18c, 19c, 21c, 23c Support)            │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### Core Services
- **OracleServiceClient**: 25 fundamental Oracle operations (database, schema, table management)
- **OracleAIService**: 10 AI-powered tools including Vector search (Oracle 23c) and natural language to SQL
- **OracleAdvancedAnalyticsService**: 20 analytics tools with CTEs, window functions, hierarchical queries
- **OracleEnterprisePerformanceService**: 10 enterprise performance tools (AWR, partitioning, parallel execution)
- **OracleEnterpriseSecurityService**: 10 enterprise security tools (VPD, TDE, Database Vault)

#### Core Utilities
- **OracleFeatureDetector**: Dynamic Oracle version and feature detection (PDBs, AWR, Vector support)
- **OracleSqlBuilder**: Safe, dynamic Oracle SQL generation with injection prevention
- **OracleConfiguration**: Oracle-optimized JDBC settings and HikariCP connection pool

### Tool Organization (75 Total)

**Database Management (7 tools)**
- oracle_list_databases, oracle_create_database, oracle_drop_database
- oracle_database_stats, oracle_database_size, oracle_database_backup, oracle_pdb_operations

**Schema/User Management (10 tools)**  
- oracle_list_schemas, oracle_create_schema, oracle_create_user, oracle_grant_privileges
- oracle_revoke_privileges, oracle_user_sessions, oracle_lock_account, oracle_unlock_account
- oracle_user_profiles, oracle_password_policies

**Table Operations (8 tools)**
- oracle_list_tables, oracle_create_table, oracle_describe_table, oracle_insert_records
- oracle_query_records, oracle_update_records, oracle_delete_records, oracle_truncate_table

**Advanced Analytics (20 tools)**
- Complex JOINs, CTEs, window functions, pivot operations, hierarchical queries
- Performance analysis with AWR integration, SQL tuning, execution plans
- PL/SQL operations: procedures, functions, packages, debugging

**AI-Powered Operations (10 tools)**
- Vector search (Oracle 23c), vector similarity, vector clustering, vector indexing
- AI document analysis, content summarization, classification
- Natural language to SQL generation, query optimization, schema design

**Enterprise Performance (10 tools)**
- Parallel execution, table partitioning, materialized views, memory management
- AWR reports, SQL plan baselines, compression management, resource manager

**Enterprise Security (10 tools)**
- Virtual Private Database (VPD), Database Vault, Fine-grained access control
- Audit management, encryption (TDE), keystore management, security assessments

## Configuration

### Oracle Database Connection

Required environment variables or application.properties:

```properties
# Oracle Database Configuration
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=hr
spring.datasource.password=password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# MCP Configuration
mcp.tools.exposure=public
mcp.transport=stdio

# Oracle feature detection
oracle.features.detection.enabled=true
oracle.features.cache.ttl=3600

# HikariCP optimization for Oracle
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

### Profile-Based Configuration

- **Enhanced Edition (default)**: `-Penhanced` - 55+ tools
- **Enterprise Edition**: `-Penterprise` - 75+ tools  
- **Oracle Version Profiles**: `-Poracle-12c`, `-Poracle19c`, `-Poracle23c`
- **Testing Profiles**: `-Pintegration-tests`, `-Ptest-coverage`, `-Pperformance-test`

## Development Workflow

### Adding New Oracle Tools

1. Create new method in appropriate service class (OracleServiceClient, OracleAIService, etc.)
2. Annotate with `@Tool(name = "tool_name", description = "description")`
3. Add parameters with `@ToolParam` annotations
4. Implement Oracle-specific logic using JdbcTemplate
5. Add comprehensive unit tests in corresponding test class
6. Update tool count in pom.xml properties

### Oracle Version Compatibility

The codebase uses **OracleFeatureDetector** for dynamic feature detection:

```java
// Example: Check for PDB support (Oracle 12c+)
if (featureDetector.supportsPDBs()) {
    // Use multitenant features
}

// Example: Check for Vector search (Oracle 23c)
if (featureDetector.supportsVectorSearch()) {
    // Use vector operations
}
```

### Database Privileges Required

Minimum Oracle privileges for core functionality:
```sql
GRANT CONNECT, RESOURCE TO mcp_user;
GRANT SELECT ON v$database TO mcp_user;
GRANT SELECT ON dba_users TO mcp_user;
GRANT SELECT ON all_tables TO mcp_user;

-- For enterprise features
GRANT SELECT ON dba_hist_snapshot TO mcp_user;
GRANT EXECUTE ON dbms_workload_repository TO mcp_user;
```

## Testing Strategy

### Test Categories

1. **Unit Tests** (28+ classes): Mock-based testing of individual components
2. **Integration Tests** (25+ tests): Real Oracle database connection tests using Testcontainers
3. **API Tests** (75+ endpoints): Postman collection with automated assertions
4. **Performance Tests**: Load testing with Oracle-specific metrics
5. **Security Tests**: Oracle security feature validation

### Oracle Testcontainers Integration

```java
@Container
static GenericContainer<?> oracle = new GenericContainer<>("gvenzl/oracle-xe:21-slim")
        .withEnv("ORACLE_PASSWORD", "oracle")
        .withExposedPorts(1521)
        .withStartupTimeout(Duration.ofMinutes(5));
```

## Performance Considerations

### Oracle-Specific Optimizations

- **Connection Pool**: HikariCP optimized for Oracle with leak detection
- **Fetch Size**: Set to 1000 for large result sets
- **Query Timeout**: 290 seconds for long-running operations
- **JVM Settings**: G1GC optimized for Oracle workloads
- **Oracle Hints**: Use parallel execution and first rows optimization where appropriate

### Monitoring

```bash
# Health check
curl http://localhost:8080/actuator/health

# Oracle-specific metrics
curl http://localhost:8080/actuator/metrics/oracle.connection.active
curl http://localhost:8080/actuator/metrics/oracle.query.execution.time

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

## Docker Deployment

```bash
# Build production Docker image
docker build -t mcp-oracle-server .

# Run with Docker Compose (includes Oracle XE, Prometheus, Grafana)
docker-compose up -d

# Scale horizontally  
docker-compose up -d --scale mcp-oracle-server=3
```

## Important Notes

- **Oracle Database Required**: This server is specifically designed for Oracle databases only
- **Raw JDBC Performance**: Uses Spring JDBC Template instead of JPA for maximum Oracle performance
- **Enterprise Features**: Full support for Oracle Enterprise Edition features (AWR, Partitioning, Security)
- **Multi-Version Support**: Compatible with Oracle 11g through 23c with dynamic feature detection
- **Production Ready**: Includes comprehensive monitoring, security, and error handling

## Troubleshooting

### Common Issues

**ORA-12541: TNS:no listener**
```bash
# Check Oracle listener status
lsnrctl status
# Test connection
curl -X POST http://localhost:8080/v1/tools/oracle_ping
```

**ORA-00942: table or view does not exist**
```sql
-- Grant necessary privileges
GRANT SELECT ON v$database TO mcp_user;
GRANT SELECT ON dba_users TO mcp_user;
```

**Performance Issues**
```properties
# Increase fetch size
spring.datasource.hikari.data-source-properties.oracle.jdbc.defaultRowPrefetch=1000
# Enable debug logging
logging.level.com.deepai.mcpserver.util.OracleFeatureDetector=DEBUG
```
