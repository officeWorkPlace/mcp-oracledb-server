# 🚀 MCP Oracle DB Server - Production Ready Implementation

> **Oracle MCP Server with 55+ Tools (Enhanced) / 75+ Tools (Enterprise)**  
> **41-92% More Capabilities than MongoDB Baseline**  
> **Spring Boot 3.4.5 + Spring AI 1.0.1 + Oracle 11g-23c**  

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.1-blue.svg)](https://spring.io/projects/spring-ai)
[![Oracle](https://img.shields.io/badge/Oracle-11g--23c-red.svg)](https://www.oracle.com/database/)
[![MCP](https://img.shields.io/badge/MCP-Compatible-purple.svg)](https://modelcontextprotocol.io/)

---

## 📋 Table of Contents

- [🔍 Overview](#-overview)
- [⭐ Features](#-features)
- [📊 Tool Comparison](#-tool-comparison)
- [🚀 Quick Start](#-quick-start)
- [⚙️ Configuration](#️-configuration)
- [🔧 MCP Integration](#-mcp-integration)
- [🧪 Testing](#-testing)
- [🐳 Docker Deployment](#-docker-deployment)
- [📈 Monitoring](#-monitoring)
- [🔒 Security](#-security)
- [📚 Documentation](#-documentation)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [🔗 Related Projects](#-related-projects)
- [🆘 Support](#-support)

---

## 🔍 Overview

The **MCP Oracle DB Server** is a production-ready Model Context Protocol (MCP) server that provides comprehensive Oracle database operations with AI-enhanced capabilities. Built with Spring Boot 3.4.5 and Spring AI 1.0.1, it significantly exceeds the MongoDB MCP server baseline with **55+ tools (Enhanced Edition)** or **75+ tools (Enterprise Edition)**.

### 🏗️ Architecture Highlights

- **Raw JDBC Performance**: Uses Spring JDBC Template for maximum Oracle performance
- **Oracle-Specific Features**: Direct access to Oracle system views and enterprise features
- **Multi-Version Support**: Compatible with Oracle 11g through 23c
- **Dynamic Feature Detection**: Automatically adapts to available Oracle features
- **Enterprise-Grade**: Supports PDBs, AWR, partitioning, and advanced security

### 🔄 Key Differentiators vs MongoDB MCP Server

| Metric | MongoDB Baseline | Oracle Enhanced | Oracle Enterprise | Advantage |
|--------|------------------|-----------------|-------------------|-----------|
| **Total Tools** | 39 tools | 55+ tools | 75+ tools | +41% to +92% |
| **Database Ops** | 4 tools | 7 tools | 7 tools | +75% more |
| **Analytics** | 12 tools | 20 tools | 20 tools | +67% more |
| **AI Features** | 7 tools | 10 tools | 10 tools | +43% more |
| **Enterprise** | 0 tools | 0 tools | 20 tools | Oracle-exclusive |

### 🔎 Why Raw JDBC Instead of JPA/Hibernate?

This project deliberately uses **Spring JDBC Template** instead of JPA/Hibernate for several architectural reasons:

| Aspect | JPA/Hibernate | Raw JDBC (This Project) | Oracle Advantage |
|--------|---------------|-------------------------|------------------|
| **Oracle Features** | Limited ORM abstraction | Full Oracle API access | ✅ V$ views, PDBs, AWR |
| **Performance** | Entity mapping overhead | Direct SQL execution | ✅ Optimized for Oracle |
| **DDL Operations** | Limited schema operations | Full DDL capabilities | ✅ Database/user creation |
| **Enterprise Tools** | Not accessible | Direct Oracle packages | ✅ RMAN, partitioning, TDE |
| **Multi-Version** | Complex version handling | Dynamic feature detection | ✅ Oracle 11g-23c support |
| **AI Integration** | Fixed entity responses | Flexible JSON responses | ✅ Structured metadata |

**🌟 Key Benefits:**
- **Direct Oracle System View Access**: Query `v$database`, `dba_users`, `all_tables` without ORM limitations
- **Oracle-Specific SQL**: Use Oracle hints, connect by, analytical functions, and PL/SQL
- **Enterprise Feature Support**: Access RMAN, AWR, Partitioning, and Database Vault
- **Performance Optimization**: HikariCP with Oracle-specific connection settings
- **Dynamic SQL Generation**: Build Oracle version-specific SQL with safety checks

---

## ⭐ Features

### 💼 **Enhanced Edition (55+ Tools)**
-  **Core Oracle Operations (25 tools):** Database, schema, user, and table management
-  **Advanced Analytics (20 tools):** SQL analytics, CTEs, window functions, PL/SQL
-  **AI-Powered Operations (10 tools):** Vector search, AI SQL generation, query optimization

### 🏢 **Enterprise Edition (75+ Tools)**
-  **Enhanced Edition (55 tools):** All enhanced features included
-  **Enterprise Security (10 tools):** VPD, TDE, Database Vault, audit policies
-  **Enterprise Performance (10 tools):** Partitioning, parallel execution, plan management

### 🛠️ **Technical Features**
-  **Multi-Version Oracle Support:** Compatible with Oracle 11g through 23c
-  **Dynamic @Tool Discovery:** Automatic tool registration with configurable exposure
-  **Stdio MCP Transport:** Default protocol with REST fallback for testing
-  **Spring AI Integration:** Full AI capabilities with vector search and query optimization
-  **Production Security:** Spring Security baseline with Oracle-specific authentication
-  **Comprehensive Monitoring:** Actuator + Prometheus + Grafana integration
-  **Docker Ready:** Multi-stage builds with production optimizations

---

## 📊 Tool Comparison

### 💾 Core Database Operations

| Operation Category | MongoDB Tools | Oracle Enhanced | Oracle Enterprise | Oracle Advantage |
|-------------------|---------------|-----------------|-------------------|------------------|
| **Database Management** | 4 | 7 | 7 | PDB support, RMAN backup |
| **Schema/User Management** | 8 | 10 | 10 | Profiles, tablespaces, privileges |
| **Table Operations** | 8 | 8 | 8 | Constraints, referential integrity |
| **Index Management** | 4 | 7 | 7 | B-tree, bitmap, function-based |
| **Performance Analysis** | 4 | 7 | 17 | AWR, ADDM, SQL tuning advisor |

### 📈 Advanced Analytics

| Analytics Feature | MongoDB | Oracle Enhanced | Oracle Enterprise |
|------------------|---------|-----------------|-------------------|
| **SQL Analytics** | Aggregation | SQL + CTEs + Window Functions | + Enterprise Partitioning |
| **Complex Joins** | Limited | Multi-table JOINs | + Parallel Processing |
| **Hierarchical Data** | Manual | CONNECT BY | + Advanced Optimizations |
| **OLAP Functions** | None | Full OLAP Support | + Materialized Views |

---

## 🚀 Quick Start

### 📋 Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **Oracle Database** (11g, 12c, 18c, 19c, 21c, or 23c)
- **Docker** (optional, for containerized deployment)

### 1️⃣ Clone and Build

\\\ash
git clone https://github.com/officeWorkPlace/mcp-oracledb-server.git
cd mcp-oracledb-server

# Build Enhanced Edition (55+ tools)
mvn clean package -Penhanced

# Build Enterprise Edition (75+ tools)
mvn clean package -Penterprise
\\\

### 2️⃣ Configure Database Connection

\\\properties
# src/main/resources/application.properties

# Oracle Database Configuration
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=hr
spring.datasource.password=password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# MCP Configuration
mcp.tools.exposure=public
mcp.transport=stdio
\\\

### 3️⃣ Run the Server

\\\ash
# Stdio MCP mode (default)
java -jar target/mcp-oracledb-server-1.0.0-PRODUCTION.jar

# REST API mode (for testing)
java -jar target/mcp-oracledb-server-1.0.0-PRODUCTION.jar --spring.profiles.active=rest
\\\

### 4️⃣ Test Oracle Tools

\\\ash
# List available Oracle tools
curl -X POST http://localhost:8080/v1/tools/list

# Test database connectivity
curl -X POST http://localhost:8080/v1/tools/oracle_ping

# List Oracle databases
curl -X POST http://localhost:8080/v1/tools/oracle_list_databases \
  -H "Content-Type: application/json" \
  -d '{"includePdbs": true}'
\\\

---

## ⚙️ Configuration

### 📝 Profile-Based Configuration

#### Enhanced Edition (Default)
\\\yaml
# application-enhanced.yml
oracle:
  features:
    tools:
      core:
        enabled: true
        count: 25
      analytics:
        enabled: true
        count: 20
      ai:
        enabled: true
        count: 10
      enterprise:
        enabled: false
\\\

#### Enterprise Edition
\\\yaml
# application-enterprise.yml
oracle:
  features:
    tools:
      enterprise:
        enabled: true
        security: 10
        performance: 10
        multitenant: 5
\\\

### 🔧 Oracle-Specific Settings

\\\properties
# Oracle feature detection
oracle.features.detection.enabled=true
oracle.features.cache.ttl=3600

# HikariCP optimization for Oracle
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.leak-detection-threshold=60000

# Oracle JDBC properties
spring.datasource.hikari.data-source-properties.oracle.net.CONNECT_TIMEOUT=60000
spring.datasource.hikari.data-source-properties.oracle.jdbc.ReadTimeout=60000
\\\

### 🛠️ Advanced Oracle Configuration

#### Oracle Version-Specific Settings

\\\properties
# Oracle 23c with Vector Support
spring.profiles.active=oracle23c
oracle.features.vector.enabled=true
oracle.features.json.enabled=true

# Oracle 19c Enterprise
spring.profiles.active=oracle19c,enterprise
oracle.features.awr.enabled=true
oracle.features.partitioning.enabled=true

# Oracle 12c with PDB Support
spring.profiles.active=oracle12c
oracle.features.multitenant.enabled=true
oracle.features.pdb.autodetect=true
\\\

#### Connection Pool Optimization

\\\properties
# Production Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=300000

# Oracle-Specific JDBC Properties
spring.datasource.hikari.data-source-properties.oracle.net.CONNECT_TIMEOUT=60000
spring.datasource.hikari.data-source-properties.oracle.net.READ_TIMEOUT=60000
spring.datasource.hikari.data-source-properties.oracle.jdbc.defaultRowPrefetch=100
spring.datasource.hikari.data-source-properties.oracle.jdbc.useFetchSizeWithLongColumn=true
\\\

#### Security Configuration

\\\properties
# SSL/TLS Configuration
spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=hostname)(PORT=2484))(CONNECT_DATA=(SERVICE_NAME=service)))
oracle.security.ssl.enabled=true
oracle.security.ssl.truststore.location=/path/to/truststore.jks
oracle.security.ssl.truststore.password=${SSL_TRUSTSTORE_PASSWORD}

# Oracle Wallet Configuration
oracle.security.wallet.enabled=true
oracle.security.wallet.location=/path/to/wallet
\\\

---

## 🔧 MCP Integration

### 🖥️ Client Configuration

\\\json
{
  "servers": {
    "oracle-mcp-enhanced": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "mcp-oracledb-server-1.0.0-PRODUCTION.jar"
      ],
      "env": {
        "ORACLE_USERNAME": "hr",
        "ORACLE_PASSWORD": "password",
        "SPRING_PROFILES_ACTIVE": "mcp"
      }
    },
    "oracle-mcp-enterprise": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "mcp-oracledb-server-1.0.0-PRODUCTION.jar",
        "--spring.profiles.active=enterprise,mcp"
      ],
      "env": {
        "ORACLE_USERNAME": "system",
        "ORACLE_PASSWORD": "manager"
      }
    }
  }
}
\\\

### 🧰 Available MCP Tools

<details>
<summary><strong>Core Oracle Operations (25 tools)</strong></summary>

**Database Management (7 tools)**
1. oracle_list_databases - List databases and PDBs with metadata
2. oracle_create_database - Create traditional or pluggable databases
3. oracle_drop_database - Safe database deletion with checks
4. oracle_database_stats - AWR and performance statistics
5. oracle_database_size - Storage and tablespace analysis
6. oracle_database_backup - RMAN backup operations
7. oracle_pdb_operations - Pluggable database management (12c+)

**Schema/User Management (10 tools)**
8. oracle_list_schemas - Schema enumeration and metadata
9. oracle_create_schema - Schema creation with privileges
10. oracle_create_user - User creation with tablespaces and profiles
11. oracle_grant_privileges - System and object privilege management
12. oracle_revoke_privileges - Privilege revocation
13. oracle_user_sessions - Session monitoring and management
14. oracle_lock_account - Account security operations
15. oracle_unlock_account - Account management
16. oracle_user_profiles - Profile creation and assignment
17. oracle_password_policies - Security policy configuration

**Table Operations (8 tools)**
18. oracle_list_tables - Table discovery with constraints
19. oracle_create_table - DDL with indexes and constraints
20. oracle_describe_table - Complete metadata retrieval
21. oracle_insert_records - Data insertion with validation
22. oracle_query_records - Advanced querying with hints
23. oracle_update_records - Data modification with constraints
24. oracle_delete_records - Data removal with referential integrity
25. oracle_truncate_table - Fast data clearing

</details>

<details>
<summary><strong>Advanced Analytics (20 tools)</strong></summary>

**SQL Analytics & CTEs (8 tools)**
26. oracle_complex_joins - Multi-table enterprise JOINs
27. oracle_cte_queries - WITH clause operations
28. oracle_window_functions - LEAD/LAG/RANK analytics
29. oracle_pivot_operations - PIVOT/UNPIVOT transformations
30. oracle_analytical_functions - PERCENTILE, NTILE, CUME_DIST
31. oracle_hierarchical_queries - CONNECT BY operations
32. oracle_recursive_cte - Recursive WITH queries
33. oracle_model_clause - MODEL clause calculations

**Performance & Indexing (7 tools)**
34. oracle_create_index - B-tree, bitmap, function-based indexes
35. oracle_analyze_performance - AWR + ADDM integration
36. oracle_optimizer_hints - Cost-based optimizer hints
37. oracle_execution_plans - EXPLAIN PLAN + DBMS_XPLAN
38. oracle_table_statistics - DBMS_STATS operations
39. oracle_sql_tuning - SQL Tuning Advisor integration
40. oracle_memory_advisor - SGA/PGA recommendations

**PL/SQL Operations (5 tools)**
41. oracle_execute_plsql - Anonymous PL/SQL block execution
42. oracle_create_procedure - Stored procedure development
43. oracle_create_function - User-defined function creation
44. oracle_manage_packages - Package creation and management
45. oracle_debug_plsql - PL/SQL debugging and profiling

</details>

<details>
<summary><strong>AI-Powered Operations (10 tools)</strong></summary>

**Oracle Vector Search (4 tools)**
46. oracle_vector_search - Oracle 23c native vector queries
47. oracle_vector_similarity - VECTOR_DISTANCE functions
48. oracle_vector_clustering - Vector grouping and analysis
49. oracle_vector_index - Vector index management

**AI Content Analysis (3 tools)**
50. oracle_ai_analyze_document - Document processing and insights
51. oracle_ai_generate_summary - Content summarization
52. oracle_ai_content_classification - Content categorization

**Oracle-AI Integration (3 tools)**
53. oracle_ai_sql_generation - Natural language to Oracle SQL
54. oracle_ai_query_optimization - AI-powered SQL tuning
55. oracle_ai_schema_design - AI schema recommendations

</details>

---

## 🧪 Testing

### 🔍 Unit and Integration Tests

\\\ash
# Run all tests
mvn test

# Run integration tests with Testcontainers
mvn test -Dtest=OracleIntegrationTest

# Run tool validation tests
mvn test -Dtest=AllOracleToolsValidationTest
\\\

### 🐋 Testcontainers Integration

\\\java
@Testcontainers
@SpringBootTest
class OracleIntegrationTest {

    @Container
    static GenericContainer<?> oracle = new GenericContainer<>("gvenzl/oracle-xe:21-slim")
            .withEnv("ORACLE_PASSWORD", "oracle")
            .withExposedPorts(1521)
            .withStartupTimeout(Duration.ofMinutes(5));

    @Test
    void testAllOracleTools() {
        // Test all 55+ tools against real Oracle database
        // Verify Enhanced Edition tool count
        // Validate Oracle-specific features
    }
}
\\\

---

## 🐳 Docker Deployment

### 🏭 Production Docker Build

\\\dockerfile
# Multi-stage build for production
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -Penhanced -DskipTests

FROM eclipse-temurin:17-jre-alpine AS production
# Optimized JVM settings for Oracle workloads
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
\\\

### 🔄 Docker Compose Stack

\\\yaml
version: '3.8'
services:
  oracle-xe:
    image: gvenzl/oracle-xe:21-slim
    environment:
      ORACLE_PASSWORD: oracle
    ports:
      - "1521:1521"
    volumes:
      - oracle_data:/opt/oracle/oradata

  mcp-oracle-server:
    build: .
    environment:
      ORACLE_USERNAME: hr
      ORACLE_PASSWORD: oracle
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    depends_on:
      - oracle-xe

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./config/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  oracle_data:
  grafana_data:
\\\

---

## 📈 Monitoring

### 🔍 Actuator Endpoints

\\\ash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
\\\

### 📊 Grafana Dashboards

Pre-configured dashboards for:
- Oracle database performance metrics
- MCP tool usage statistics
- JVM and application metrics
- Connection pool monitoring

### 🚀 Performance Monitoring

#### Custom Oracle Metrics

\\\bash
# Oracle-specific performance metrics
curl http://localhost:8080/actuator/metrics/oracle.connection.active
curl http://localhost:8080/actuator/metrics/oracle.query.execution.time
curl http://localhost:8080/actuator/metrics/oracle.tools.usage.count

# Database health indicators
curl http://localhost:8080/actuator/health/oracle
curl http://localhost:8080/actuator/health/oracleFeatureDetector
\\\

#### JVM Optimization for Oracle Workloads

\\\bash
# Production JVM settings
export JAVA_OPTS="
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75.0
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp/heapdump.hprof
  -Doracle.jdbc.fanEnabled=false
  -Doracle.net.keepAlive=true
"

java $JAVA_OPTS -jar mcp-oracledb-server-1.0.0-PRODUCTION.jar
\\\

---

## 🔒 Security

### 🛡️ Oracle Database Security

- **Connection Security:** TLS/SSL encryption support
- **Authentication:** Oracle native authentication
- **Authorization:** Role-based access control
- **Audit Logging:** Oracle audit trail integration

### 🔐 Application Security

- **Spring Security:** Baseline security configuration
- **Input Validation:** SQL injection prevention
- **Error Handling:** Secure error responses
- **Monitoring:** Security event logging

---

## 📚 Documentation

### 📖 API Reference

- **Tool Documentation:** Complete reference for all 55+ tools
- **Configuration Guide:** Environment and profile setup
- **Integration Examples:** MCP client integration patterns
- **Troubleshooting:** Common issues and solutions

### 📔 Oracle-Specific Guides

- **Multi-Version Support:** Oracle 11g-23c compatibility
- **PDB Operations:** Pluggable database management
- **Performance Tuning:** Oracle-specific optimizations
- **Enterprise Features:** VPD, TDE, and advanced security

## 🔧 Troubleshooting

### ❓ Common Issues and Solutions

#### Oracle Connection Issues

**Problem**: `ORA-12541: TNS:no listener`
\\\bash
# Check Oracle listener status
lsnrctl status

# Verify connection string
sqlplus username/password@hostname:port/service_name

# Test with MCP server
curl -X POST http://localhost:8080/v1/tools/oracle_ping
\\\

**Problem**: `ORA-00942: table or view does not exist`
\\\bash
# Grant necessary privileges
GRANT SELECT ON v$database TO mcp_user;
GRANT SELECT ON dba_users TO mcp_user;
GRANT SELECT ON all_tables TO mcp_user;

# For enterprise features
GRANT SELECT ON dba_hist_snapshot TO mcp_user;
GRANT EXECUTE ON dbms_workload_repository TO mcp_user;
\\\

#### Feature Detection Issues

**Problem**: Oracle features not detected correctly
\\\properties
# Enable debug logging
logging.level.com.deepai.mcpserver.util.OracleFeatureDetector=DEBUG

# Force feature detection refresh
oracle.features.detection.enabled=true
oracle.features.cache.ttl=0
\\\

#### Performance Issues

**Problem**: Slow query execution
\\\properties
# Increase fetch size
spring.datasource.hikari.data-source-properties.oracle.jdbc.defaultRowPrefetch=1000

# Enable statement caching
spring.datasource.hikari.data-source-properties.oracle.jdbc.implicitStatementCacheSize=50

# Monitor slow queries
logging.level.org.springframework.jdbc=DEBUG
\\\

#### Memory Issues

**Problem**: OutOfMemoryError with large result sets
\\\bash
# Increase JVM heap
export JAVA_OPTS="-Xms2g -Xmx8g -XX:+UseG1GC"

# Enable result set streaming
oracle.jdbc.streaming.enabled=true
oracle.jdbc.fetchSize=1000
\\\

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### 💻 Development Setup

\\\ash
# Clone repository
git clone https://github.com/officeWorkPlace/mcp-oracledb-server.git

# Install dependencies
mvn clean install

# Run in development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev
\\\

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🔗 Related Projects

- **[MongoDB MCP Server](https://github.com/officeWorkPlace/spring-boot-ai-mongodb-mcp-server)** - Baseline reference (39 tools)
- **[Global MCP Client](https://github.com/officeWorkPlace/global-mcp-client)** - Universal MCP client
- **[Spring AI](https://spring.io/projects/spring-ai)** - Spring AI framework

---

## 🆘 Support

-  **Issues:** [GitHub Issues](https://github.com/officeWorkPlace/mcp-oracledb-server/issues)
-  **Discussions:** [GitHub Discussions](https://github.com/officeWorkPlace/mcp-oracledb-server/discussions)
-  **Email:** [office.place.work.007@gmail.com](mailto:office.place.work.007@gmail.com)

---

**MCP Oracle DB Server v1.0.0-PRODUCTION**  
*Exceeding the MongoDB baseline with 41-92% more Oracle-specific capabilities*  
*Built with ❤️ by officeWorkPlace*
