# MCP Oracle Database Server - Troubleshooting Guide

> **Comprehensive troubleshooting guide for Oracle MCP Server**  
> **Common issues, Oracle-specific problems, debugging techniques, and solutions**

[![Oracle](https://img.shields.io/badge/Oracle-11g--23c-red.svg)](https://www.oracle.com/database/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Support](https://img.shields.io/badge/Support-Production_Ready-green.svg)](#support)

---

## Table of Contents

- [Oracle Connection Issues](#oracle-connection-issues)
- [Authentication and Authorization](#authentication-and-authorization)
- [Feature Detection Problems](#feature-detection-problems)
- [Performance Issues](#performance-issues)
- [Memory and Resource Problems](#memory-and-resource-problems)
- [MCP Protocol Issues](#mcp-protocol-issues)
- [Oracle Version-Specific Issues](#oracle-version-specific-issues)
- [Enterprise Features Problems](#enterprise-features-problems)
- [Debugging Techniques](#debugging-techniques)
- [Common Error Codes](#common-error-codes)
- [Production Issues](#production-issues)

---

## Oracle Connection Issues

### ORA-12541: TNS:no listener

**Problem**: Cannot connect to Oracle database - listener not running or unreachable.

**Symptoms**:
```
java.sql.SQLException: ORA-12541: TNS:no listener
```

**Solutions**:

1. **Check Oracle Listener Status**
   ```bash
   # Check listener status
   lsnrctl status
   
   # Start listener if stopped
   lsnrctl start
   
   # Check specific port
   netstat -an | grep 1521
   ```

2. **Verify Connection String**
   ```properties
   # Test with different formats
   spring.datasource.url=jdbc:oracle:thin:@hostname:1521:SID
   spring.datasource.url=jdbc:oracle:thin:@hostname:1521/SERVICE_NAME
   spring.datasource.url=jdbc:oracle:thin:@//hostname:1521/SERVICE_NAME
   ```

3. **Test with SQL*Plus**
   ```bash
   # Test connection directly
   sqlplus username/password@hostname:port/service_name
   
   # Test with tnsping
   tnsping hostname:1521
   ```

4. **Configure Firewall/Network**
   ```bash
   # Check firewall (Linux)
   sudo ufw status
   sudo ufw allow 1521
   
   # Test connectivity
   telnet hostname 1521
   ```

### ORA-12545: Connect failed because target host or object does not exist

**Problem**: DNS resolution or network connectivity issues.

**Solutions**:

1. **Verify Hostname Resolution**
   ```bash
   # Test DNS resolution
   nslookup hostname
   ping hostname
   
   # Use IP address if DNS fails
   spring.datasource.url=jdbc:oracle:thin:@192.168.1.100:1521:XE
   ```

2. **Update Hosts File**
   ```bash
   # Add entry to /etc/hosts (Linux) or C:\Windows\System32\drivers\etc\hosts (Windows)
   192.168.1.100 oracle-server
   ```

### ORA-12514: TNS:listener does not currently know of service requested

**Problem**: Service name not registered with listener.

**Solutions**:

1. **Check Available Services**
   ```sql
   -- Connect as SYSDBA and check services
   SELECT name FROM v$database;
   SELECT value FROM v$parameter WHERE name = 'service_names';
   ```

2. **Update Connection String**
   ```properties
   # Use correct service name
   spring.datasource.url=jdbc:oracle:thin:@hostname:1521/XEPDB1
   
   # Or use SID format for older versions
   spring.datasource.url=jdbc:oracle:thin:@hostname:1521:XE
   ```

3. **Register Service**
   ```sql
   -- Register service manually
   ALTER SYSTEM REGISTER;
   ```

---

## Authentication and Authorization

### ORA-01017: invalid username/password; logon denied

**Problem**: Authentication failure with provided credentials.

**Solutions**:

1. **Verify Credentials**
   ```bash
   # Test credentials directly
   sqlplus username/password@hostname:port/service
   ```

2. **Check Account Status**
   ```sql
   -- Check user account status
   SELECT username, account_status, expiry_date, lock_date 
   FROM dba_users 
   WHERE username = 'USERNAME';
   ```

3. **Unlock Account**
   ```sql
   -- Unlock locked account
   ALTER USER username ACCOUNT UNLOCK;
   
   -- Reset password if expired
   ALTER USER username IDENTIFIED BY new_password;
   ```

4. **Case Sensitivity Issues**
   ```properties
   # Oracle usernames are case-sensitive when quoted
   spring.datasource.username=hr
   # NOT: spring.datasource.username="hr"
   ```

### ORA-00942: table or view does not exist

**Problem**: User lacks privileges to access Oracle system views required by MCP server.

**Solutions**:

1. **Grant Required Privileges**
   ```sql
   -- Basic privileges for MCP server
   GRANT SELECT ON v$database TO mcp_user;
   GRANT SELECT ON v$version TO mcp_user;
   GRANT SELECT ON dba_users TO mcp_user;
   GRANT SELECT ON all_tables TO mcp_user;
   GRANT SELECT ON all_tab_columns TO mcp_user;
   GRANT SELECT ON all_constraints TO mcp_user;
   
   -- For PDB operations (12c+)
   GRANT SELECT ON dba_pdbs TO mcp_user;
   GRANT ALTER SESSION TO mcp_user;
   
   -- For enterprise features
   GRANT SELECT ON dba_hist_snapshot TO mcp_user;
   GRANT EXECUTE ON dbms_workload_repository TO mcp_user;
   ```

2. **Grant System Privileges**
   ```sql
   -- Basic system privileges
   GRANT CREATE SESSION TO mcp_user;
   GRANT SELECT_CATALOG_ROLE TO mcp_user;
   
   -- For administrative operations
   GRANT DBA TO mcp_user; -- Use with caution in production
   ```

3. **Check Current Privileges**
   ```sql
   -- Check user privileges
   SELECT * FROM user_sys_privs WHERE username = 'MCP_USER';
   SELECT * FROM user_tab_privs WHERE grantee = 'MCP_USER';
   ```

### ORA-01031: insufficient privileges

**Problem**: User has connection privileges but lacks specific operation privileges.

**Solutions**:

1. **Grant Specific Privileges**
   ```sql
   -- For database operations
   GRANT CREATE DATABASE TO mcp_user;
   GRANT DROP DATABASE TO mcp_user;
   
   -- For user management
   GRANT CREATE USER TO mcp_user;
   GRANT ALTER USER TO mcp_user;
   GRANT DROP USER TO mcp_user;
   
   -- For table operations
   GRANT CREATE TABLE TO mcp_user;
   GRANT DROP ANY TABLE TO mcp_user;
   ```

2. **Enable Debug Logging**
   ```properties
   # Debug privilege issues
   logging.level.com.deepai.mcpserver.service=DEBUG
   logging.level.org.springframework.jdbc=DEBUG
   ```

---

## Feature Detection Problems

### Oracle Feature Detection Failures

**Problem**: MCP server cannot detect Oracle version or available features.

**Symptoms**:
```
WARN  c.d.m.u.OracleFeatureDetector - Oracle feature detection failed
ERROR c.d.m.u.OracleFeatureDetector - Cannot determine Oracle version
```

**Solutions**:

1. **Enable Debug Logging**
   ```properties
   # Enable feature detection debugging
   logging.level.com.deepai.mcpserver.util.OracleFeatureDetector=DEBUG
   oracle.features.detection.enabled=true
   oracle.features.cache.ttl=0
   ```

2. **Grant Required System View Access**
   ```sql
   -- Grant access to version views
   GRANT SELECT ON v$version TO mcp_user;
   GRANT SELECT ON v$parameter TO mcp_user;
   GRANT SELECT ON v$option TO mcp_user;
   ```

3. **Test Feature Detection Manually**
   ```bash
   # Test Oracle version detection
   curl -X POST http://localhost:8080/actuator/health/oracleFeatureDetector
   
   # Check Oracle health
   curl http://localhost:8080/actuator/health/oracle
   ```

4. **Force Feature Refresh**
   ```properties
   # Force feature detection refresh
   oracle.features.detection.retry_count=3
   oracle.features.detection.retry_delay=5000
   ```

### PDB Detection Issues

**Problem**: Pluggable Database features not detected correctly.

**Solutions**:

1. **Check Oracle Version**
   ```sql
   SELECT * FROM v$version;
   SELECT value FROM v$parameter WHERE name = 'compatible';
   ```

2. **Verify CDB Mode**
   ```sql
   SELECT cdb FROM v$database;
   SELECT con_id, name FROM v$containers;
   ```

3. **Grant PDB Privileges**
   ```sql
   -- Connect to root container
   ALTER SESSION SET container = CDB$ROOT;
   GRANT SELECT ON dba_pdbs TO c##mcp_user;
   GRANT ALTER SESSION TO c##mcp_user;
   ```

---

## Performance Issues

### Slow Query Execution

**Problem**: Oracle queries taking longer than expected.

**Symptoms**:
```
WARN  c.d.m.s.OracleServiceClient - Query execution time exceeded threshold: 15000ms
```

**Solutions**:

1. **Optimize JDBC Settings**
   ```properties
   # Increase fetch size
   spring.datasource.hikari.data-source-properties.oracle.jdbc.defaultRowPrefetch=1000
   
   # Enable statement caching
   spring.datasource.hikari.data-source-properties.oracle.jdbc.implicitStatementCacheSize=100
   
   # Set query timeout
   oracle.query.timeout=300
   ```

2. **Analyze Query Performance**
   ```sql
   -- Enable SQL trace
   ALTER SESSION SET sql_trace = TRUE;
   
   -- Check execution plan
   EXPLAIN PLAN FOR SELECT * FROM large_table;
   SELECT * FROM table(dbms_xplan.display);
   ```

3. **Optimize Oracle Parameters**
   ```sql
   -- Check current parameters
   SELECT name, value FROM v$parameter 
   WHERE name IN ('db_file_multiblock_read_count', 'optimizer_mode');
   
   -- Gather table statistics
   EXEC dbms_stats.gather_table_stats('SCHEMA', 'TABLE_NAME');
   ```

4. **Enable Query Hints**
   ```properties
   # Enable Oracle hints
   oracle.hints.parallel.enabled=true
   oracle.hints.parallel.default_degree=4
   oracle.hints.first_rows.enabled=true
   ```

### Connection Pool Exhaustion

**Problem**: All connections in the pool are in use.

**Symptoms**:
```
java.sql.SQLException: Connection is not available, request timed out after 30000ms
```

**Solutions**:

1. **Increase Pool Size**
   ```properties
   # Increase connection pool size
   spring.datasource.hikari.maximum-pool-size=50
   spring.datasource.hikari.minimum-idle=10
   
   # Adjust timeouts
   spring.datasource.hikari.connection-timeout=60000
   spring.datasource.hikari.idle-timeout=300000
   ```

2. **Monitor Pool Usage**
   ```properties
   # Enable pool monitoring
   spring.datasource.hikari.register-mbeans=true
   management.metrics.export.prometheus.enabled=true
   ```

3. **Check for Connection Leaks**
   ```properties
   # Enable leak detection
   spring.datasource.hikari.leak-detection-threshold=60000
   logging.level.com.zaxxer.hikari=DEBUG
   ```

4. **Optimize Connection Lifecycle**
   ```properties
   # Connection lifecycle settings
   spring.datasource.hikari.max-lifetime=1800000
   spring.datasource.hikari.validation-timeout=5000
   spring.datasource.hikari.idle-timeout=600000
   ```

### High CPU Usage

**Problem**: Oracle MCP server consuming excessive CPU.

**Solutions**:

1. **Profile Application**
   ```bash
   # Use Java Flight Recorder
   java -XX:+FlightRecorder \
        -XX:StartFlightRecording=duration=60s,filename=profile.jfr \
        -jar mcp-oracle-server.jar
   ```

2. **Optimize GC Settings**
   ```bash
   # Use G1GC for better performance
   export JAVA_OPTS="
     -XX:+UseG1GC
     -XX:MaxGCPauseMillis=200
     -XX:G1HeapRegionSize=16m
   "
   ```

3. **Check Oracle AWR Reports**
   ```sql
   -- Generate AWR report for analysis
   SELECT snap_id, begin_interval_time 
   FROM dba_hist_snapshot 
   ORDER BY snap_id DESC 
   FETCH FIRST 2 ROWS ONLY;
   ```

---

## Memory and Resource Problems

### OutOfMemoryError

**Problem**: Java heap space exhausted during Oracle operations.

**Symptoms**:
```
java.lang.OutOfMemoryError: Java heap space
java.lang.OutOfMemoryError: GC overhead limit exceeded
```

**Solutions**:

1. **Increase Heap Size**
   ```bash
   # Set appropriate heap size
   export JAVA_OPTS="
     -Xms2g
     -Xmx8g
     -XX:+UseG1GC
     -XX:MaxGCPauseMillis=200
   "
   ```

2. **Optimize Large Result Sets**
   ```properties
   # Stream large results
   oracle.query.fetch_size=1000
   oracle.query.max_rows=10000
   oracle.query.streaming.enabled=true
   ```

3. **Enable Memory Monitoring**
   ```properties
   # Memory monitoring
   management.endpoint.metrics.enabled=true
   management.metrics.export.prometheus.enabled=true
   ```

4. **Heap Dump Analysis**
   ```bash
   # Configure heap dump on OOM
   -XX:+HeapDumpOnOutOfMemoryError
   -XX:HeapDumpPath=/tmp/heapdump.hprof
   
   # Analyze heap dump with Eclipse MAT or VisualVM
   ```

### Oracle Listener Connection Limit

**Problem**: Oracle listener rejecting connections due to process limit.

**Solutions**:

1. **Check Oracle Parameters**
   ```sql
   SELECT name, value FROM v$parameter 
   WHERE name IN ('processes', 'sessions');
   ```

2. **Increase Oracle Limits**
   ```sql
   -- Increase process limit
   ALTER SYSTEM SET processes=500 SCOPE=SPFILE;
   
   -- Restart database required
   SHUTDOWN IMMEDIATE;
   STARTUP;
   ```

3. **Optimize Connection Usage**
   ```properties
   # Reduce connection pool size
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=5
   
   # Shorter connection lifetime
   spring.datasource.hikari.max-lifetime=900000
   ```

---

## MCP Protocol Issues

### MCP Tool Registration Failures

**Problem**: Oracle tools not registered properly with MCP protocol.

**Solutions**:

1. **Check Tool Registration**
   ```bash
   # List available tools
   curl -X POST http://localhost:8080/v1/tools/list
   
   # Test specific tool
   curl -X POST http://localhost:8080/v1/tools/oracle_ping
   ```

2. **Enable MCP Debug Logging**
   ```properties
   # MCP protocol debugging
   logging.level.org.springframework.ai.mcp=DEBUG
   logging.level.com.deepai.mcpserver.controller=DEBUG
   ```

3. **Verify Tool Configuration**
   ```properties
   # MCP tool settings
   mcp.tools.exposure=public
   mcp.edition=enhanced
   mcp.transport=stdio
   ```

### MCP Response Format Issues

**Problem**: Malformed or unexpected responses from MCP tools.

**Solutions**:

1. **Check Response Format**
   ```properties
   # Configure response format
   mcp.responses.format=structured
   mcp.responses.include_metadata=true
   mcp.responses.include_performance_stats=false
   ```

2. **Validate JSON Responses**
   ```bash
   # Test JSON response format
   curl -X POST http://localhost:8080/v1/tools/oracle_list_databases \
        -H "Content-Type: application/json" \
        -d '{"includePdbs": true}' | jq '.'
   ```

---

## Oracle Version-Specific Issues

### Oracle 23c Issues

**Problem**: Vector database or JSON features not working.

**Solutions**:

1. **Enable 23c Features**
   ```properties
   oracle.version=23c
   oracle.features.vector.enabled=true
   oracle.features.json.enabled=true
   spring.profiles.active=oracle23c,enhanced
   ```

2. **Grant Vector Privileges**
   ```sql
   -- Grant vector-related privileges
   GRANT CREATE ANY INDEX TO mcp_user;
   GRANT EXECUTE ON dbms_vector TO mcp_user;
   ```

### Oracle 12c PDB Issues

**Problem**: PDB operations failing on Oracle 12c.

**Solutions**:

1. **Connect to Correct Container**
   ```sql
   -- Check current container
   SELECT sys_context('USERENV', 'CON_NAME') FROM dual;
   
   -- Connect to root for PDB operations
   ALTER SESSION SET container = CDB$ROOT;
   ```

2. **Use Common Users**
   ```sql
   -- Create common user for PDB operations
   CREATE USER c##mcp_user IDENTIFIED BY password;
   GRANT DBA TO c##mcp_user;
   ALTER USER c##mcp_user SET container_data=all FOR dba_pdbs;
   ```

### Oracle 11g Limitations

**Problem**: Modern features not working on Oracle 11g.

**Solutions**:

1. **Configure 11g Profile**
   ```properties
   spring.profiles.active=oracle11g,enhanced
   oracle.features.multitenant.enabled=false
   oracle.features.json.enabled=false
   ```

2. **Use Compatible SQL**
   ```properties
   # Use 11g compatible settings
   oracle.sql.compatibility=11g
   oracle.features.legacy_support=true
   ```

---

## Enterprise Features Problems

### AWR Access Issues

**Problem**: Cannot access AWR (Automatic Workload Repository) data.

**Solutions**:

1. **Check License and Privileges**
   ```sql
   -- Check if AWR is available
   SELECT * FROM dba_feature_usage_statistics 
   WHERE name = 'Automatic Workload Repository';
   
   -- Grant AWR privileges
   GRANT SELECT ON dba_hist_snapshot TO mcp_user;
   GRANT EXECUTE ON dbms_workload_repository TO mcp_user;
   ```

2. **Verify Enterprise License**
   ```sql
   SELECT * FROM v$option WHERE parameter = 'Diagnostic Pack';
   ```

### Partitioning Issues

**Problem**: Table partitioning operations failing.

**Solutions**:

1. **Check Partitioning Option**
   ```sql
   SELECT * FROM v$option WHERE parameter = 'Partitioning';
   ```

2. **Grant Partitioning Privileges**
   ```sql
   GRANT CREATE ANY TABLE TO mcp_user;
   GRANT ALTER ANY TABLE TO mcp_user;
   GRANT SELECT ON dba_part_tables TO mcp_user;
   ```

---

## Debugging Techniques

### Enable Comprehensive Logging

```properties
# Complete debug configuration
logging.level.root=INFO
logging.level.com.deepai.mcpserver=DEBUG
logging.level.org.springframework.jdbc.core=DEBUG
logging.level.com.zaxxer.hikari=DEBUG
logging.level.oracle.jdbc=INFO

# Oracle-specific debugging
oracle.debug.enabled=true
oracle.debug.sql_trace=true
oracle.debug.performance_monitoring=true
```

### SQL Tracing

```sql
-- Enable SQL trace for session
ALTER SESSION SET sql_trace = TRUE;
ALTER SESSION SET tracefile_identifier = 'MCP_TRACE';

-- Enable 10046 trace (more detailed)
ALTER SESSION SET events '10046 trace name context forever, level 12';

-- Find trace file location
SELECT value FROM v$parameter WHERE name = 'user_dump_dest';
```

### Connection Debugging

```properties
# Connection pool debugging
spring.datasource.hikari.register-mbeans=true
spring.datasource.hikari.leak-detection-threshold=30000

# JDBC debugging
logging.level.oracle.jdbc=DEBUG
logging.level.oracle.net=DEBUG
```

### Performance Monitoring

```bash
# Monitor Oracle processes
ps -ef | grep oracle

# Check Oracle alert log
tail -f $ORACLE_HOME/diag/rdbms/orcl/orcl/trace/alert_orcl.log

# Monitor Java application
jstat -gc -h10 <pid> 5s
jstack <pid>
```

---

## Common Error Codes

### Oracle Error Codes

| Error Code | Description | Common Causes | Solutions |
|------------|-------------|---------------|-----------|
| **ORA-00001** | Unique constraint violated | Duplicate key insertion | Check for existing records, use MERGE |
| **ORA-00054** | Resource busy and acquire with NOWAIT | Lock contention | Wait or use different transaction |
| **ORA-00060** | Deadlock detected | Circular lock dependency | Implement proper lock ordering |
| **ORA-00942** | Table or view does not exist | Missing privileges or object | Grant access or check object name |
| **ORA-01017** | Invalid username/password | Authentication failure | Verify credentials and account status |
| **ORA-01031** | Insufficient privileges | Missing system/object privileges | Grant required privileges |
| **ORA-01555** | Snapshot too old | Long-running transaction | Increase undo tablespace |
| **ORA-12514** | Service not known | Incorrect service name | Verify service registration |
| **ORA-12541** | No listener | Listener not running | Start Oracle listener |
| **ORA-28000** | Account locked | Failed login attempts | Unlock account |

### Java/Spring Error Patterns

```java
// Connection timeout
java.sql.SQLTimeoutException: Connection timed out

// Pool exhaustion  
java.sql.SQLException: Connection is not available

// Authentication
java.sql.SQLException: ORA-01017: invalid username/password

// Memory issues
java.lang.OutOfMemoryError: Java heap space
```

---

## Production Issues

### High Availability Scenarios

**Problem**: Database failover not handled properly.

**Solutions**:

1. **Configure RAC Connection**
   ```properties
   spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=
     (ADDRESS_LIST=
       (ADDRESS=(PROTOCOL=TCP)(HOST=node1)(PORT=1521))
       (ADDRESS=(PROTOCOL=TCP)(HOST=node2)(PORT=1521))
     )
     (CONNECT_DATA=(SERVICE_NAME=orcl))
   )
   
   # Enable Fast Connection Failover
   spring.datasource.hikari.data-source-properties.oracle.jdbc.fastConnectionFailover=true
   ```

2. **Implement Connection Validation**
   ```properties
   spring.datasource.hikari.connection-test-query=SELECT 1 FROM DUAL
   spring.datasource.hikari.validation-timeout=5000
   ```

### Security Audit Issues

**Problem**: Security audit failures or compliance issues.

**Solutions**:

1. **Enable Oracle Audit**
   ```sql
   -- Enable audit for MCP operations
   AUDIT ALL BY mcp_user;
   AUDIT CREATE TABLE, DROP TABLE;
   ```

2. **Implement Application Logging**
   ```properties
   # Security audit logging
   logging.level.com.deepai.mcpserver.service=INFO
   logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{user}] %logger{36} - %msg%n
   ```

### Backup and Recovery Issues

**Problem**: RMAN backup operations failing.

**Solutions**:

1. **Check RMAN Configuration**
   ```sql
   -- Connect to RMAN
   RMAN TARGET /
   
   -- Check configuration
   SHOW ALL;
   
   -- Configure backup location
   CONFIGURE DEFAULT DEVICE TYPE TO DISK;
   CONFIGURE CHANNEL DEVICE TYPE DISK FORMAT '/backup/%d_%T_%s_%p.bkp';
   ```

2. **Grant RMAN Privileges**
   ```sql
   GRANT SYSBACKUP TO mcp_user;
   GRANT EXECUTE ON dbms_backup_restore TO mcp_user;
   ```

---

## Getting Help

### Diagnostic Information Collection

When reporting issues, collect this information:

```bash
# System information
java -version
echo $JAVA_OPTS

# Oracle version
sqlplus -v

# Application logs
tail -100 logs/application.log

# Oracle alert log
tail -100 $ORACLE_HOME/diag/rdbms/orcl/orcl/trace/alert_orcl.log

# Connection test
curl -X POST http://localhost:8080/actuator/health/oracle

# Feature detection
curl -X POST http://localhost:8080/actuator/health/oracleFeatureDetector
```

### Health Check Endpoints

```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Oracle-specific health
curl http://localhost:8080/actuator/health/oracle

# Connection pool status
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Support Resources

- **GitHub Issues**: [Report bugs and feature requests](https://github.com/officeWorkPlace/mcp-oracledb-server/issues)
- **Discussions**: [Community support and questions](https://github.com/officeWorkPlace/mcp-oracledb-server/discussions)
- **Email**: office.place.work.007@gmail.com
- **Oracle Documentation**: [Oracle Database Documentation](https://docs.oracle.com/database/)
- **Spring Boot Reference**: [Spring Boot Documentation](https://spring.io/projects/spring-boot)

---

## Preventive Measures

### Monitoring Setup

```yaml
# Comprehensive monitoring configuration
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info,oracle
  health:
    oracle:
      enabled: true
    hikaricp:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

oracle:
  monitoring:
    enabled: true
    alerts:
      slow_query_threshold: 5000
      connection_pool_threshold: 80
      error_rate_threshold: 5
```

### Regular Maintenance

1. **Daily Checks**
   - Connection pool health
   - Error log review
   - Performance metrics

2. **Weekly Tasks**
   - Oracle statistics gathering
   - Backup verification
   - Security audit review

3. **Monthly Activities**
   - AWR report analysis
   - Capacity planning review
   - Security patch assessment

---

## Conclusion

This troubleshooting guide covers:

- **Oracle connection and authentication issues**
- **Feature detection and version-specific problems**
- **Performance optimization and resource management**
- **MCP protocol debugging techniques**
- **Enterprise features troubleshooting**
- **Production environment considerations**

Use the debugging techniques and solutions provided to resolve issues quickly and maintain optimal Oracle MCP Server performance.

---

**Troubleshooting Guide v1.0.0-PRODUCTION**  
*Comprehensive Oracle issue resolution by officeWorkPlace*
