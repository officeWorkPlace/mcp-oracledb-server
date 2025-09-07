# MCP Oracle Database Server - API Reference

> **Complete API reference for all 75+ Oracle MCP Server tools**  
> **Enhanced (55+ tools) and Enterprise (75+ tools) editions with examples and Oracle feature mappings**

[![Oracle](https://img.shields.io/badge/Oracle-11g--23c-red.svg)](https://www.oracle.com/database/)
[![MCP](https://img.shields.io/badge/MCP-Compatible-purple.svg)](https://modelcontextprotocol.io/)
[![Tools](https://img.shields.io/badge/Tools-75+-green.svg)](#tools)

---

## Table of Contents

- [API Overview](#api-overview)
- [Core Oracle Operations (25 Tools)](#core-oracle-operations-25-tools)
- [Advanced Analytics (20 Tools)](#advanced-analytics-20-tools)
- [AI-Powered Operations (10 Tools)](#ai-powered-operations-10-tools)
- [Enterprise Security (10 Tools)](#enterprise-security-10-tools)
- [Enterprise Performance (10 Tools)](#enterprise-performance-10-tools)
- [Response Formats](#response-formats)
- [Error Handling](#error-handling)
- [Oracle Feature Mapping](#oracle-feature-mapping)

---

## API Overview

### Tool Categories and Counts

| Category | Enhanced Edition | Enterprise Edition | Oracle Features |
|----------|------------------|-------------------|-----------------|
| **Core Oracle Operations** | 25 tools | 25 tools | All Oracle versions |
| **Advanced Analytics** | 20 tools | 20 tools | SQL, PL/SQL, Analytics |
| **AI-Powered Operations** | 10 tools | 10 tools | Oracle 23c Vector DB |
| **Enterprise Security** | ❌ Not included | 10 tools | VPD, TDE, Audit |
| **Enterprise Performance** | ❌ Not included | 10 tools | AWR, Partitioning |
| **Total Tools** | **55 tools** | **75 tools** | **41-92% more than MongoDB** |

### MCP Protocol Integration

All tools are exposed via the Model Context Protocol (MCP) with structured JSON responses optimized for AI/LLM consumption.

```bash
# List all available tools
curl -X POST http://localhost:8080/v1/tools/list

# Execute a tool
curl -X POST http://localhost:8080/v1/tools/{tool_name} \
  -H "Content-Type: application/json" \
  -d '{"parameter": "value"}'
```

---

## Core Oracle Operations (25 Tools)

### Database Management (7 Tools)

#### 1. oracle_list_databases
List databases and PDBs with metadata

**Parameters:**
- `includePdbs` (Boolean, optional): Include Pluggable Databases (12c+)
- `includeStatus` (Boolean, optional): Include database status information

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_list_databases \
  -H "Content-Type: application/json" \
  -d '{"includePdbs": true, "includeStatus": true}'
```

**Response:**
```json
{
  "status": "success",
  "databases": [
    {
      "database_name": "ORCL",
      "created": "2023-01-15T10:30:00Z",
      "log_mode": "ARCHIVELOG",
      "type": "CDB"
    },
    {
      "database_name": "HRPDB",
      "created": "2023-02-01T14:20:00Z",
      "open_mode": "READ WRITE",
      "type": "PDB"
    }
  ],
  "count": 2,
  "oracleVersion": "Oracle Database 19c Enterprise Edition",
  "pdbSupport": true,
  "timestamp": "2024-01-06T16:00:00Z"
}
```

**Oracle Features:** Compatible with Oracle 11g-23c, PDB detection for 12c+

#### 2. oracle_create_database
Create traditional or pluggable databases

**Parameters:**
- `databaseName` (String, required): Name of the database to create
- `createType` (String, optional): "traditional" or "pdb" (default: "traditional")
- `datafileSize` (String, optional): Initial datafile size (default: "100M")

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_create_database \
  -H "Content-Type: application/json" \
  -d '{"databaseName": "TESTPDB", "createType": "pdb", "datafileSize": "500M"}'
```

**Response:**
```json
{
  "status": "success",
  "message": "Database created successfully",
  "databaseName": "TESTPDB",
  "type": "pdb",
  "oracleFeature": "Multitenant Architecture"
}
```

**Oracle Features:** Traditional database creation (11g+), PDB creation (12c+)

#### 3. oracle_drop_database
Safe database deletion with system protection

**Parameters:**
- `databaseName` (String, required): Name of the database to drop
- `force` (Boolean, optional): Force deletion including datafiles

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_drop_database \
  -H "Content-Type: application/json" \
  -d '{"databaseName": "TESTDB", "force": true}'
```

**Oracle Features:** System database protection, datafile management

#### 4. oracle_database_stats
AWR and performance statistics

**Parameters:**
- `includeAwrData` (Boolean, optional): Include AWR statistics (Enterprise Edition)

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_database_stats \
  -H "Content-Type: application/json" \
  -d '{"includeAwrData": true}'
```

**Response:**
```json
{
  "status": "success",
  "statistics": {
    "table_count": 1247,
    "user_count": 45,
    "index_count": 2156,
    "total_size_mb": 15420.75,
    "awr_snapshots": 168
  },
  "awrAvailable": true,
  "timestamp": "2024-01-06T16:00:00Z"
}
```

**Oracle Features:** Basic statistics (all versions), AWR integration (Enterprise)

#### 5. oracle_database_size
Storage and tablespace analysis

**Parameters:**
- `includeTablespaces` (Boolean, optional): Include tablespace breakdown

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_database_size \
  -H "Content-Type: application/json" \
  -d '{"includeTablespaces": true}'
```

**Oracle Features:** Datafile analysis, tablespace management

#### 6. oracle_database_backup
RMAN backup operations

**Parameters:**
- `backupType` (String, optional): "full", "incremental" (default: "full")
- `backupLocation` (String, optional): Backup directory path

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_database_backup \
  -H "Content-Type: application/json" \
  -d '{"backupType": "full", "backupLocation": "/backup/oracle"}'
```

**Oracle Features:** RMAN integration, backup script generation

#### 7. oracle_pdb_operations
Pluggable database management (12c+)

**Parameters:**
- `operation` (String, required): "CREATE", "OPEN", "CLOSE", "DROP"
- `pdbName` (String, required): PDB name
- `parameters` (Object, optional): Additional parameters

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_pdb_operations \
  -H "Content-Type: application/json" \
  -d '{"operation": "OPEN", "pdbName": "HRPDB"}'
```

**Oracle Features:** Oracle 12c+ Multitenant Architecture

---

### Schema/User Management (10 Tools)

#### 8. oracle_list_schemas
Schema enumeration and metadata

**Parameters:**
- `includeSystemSchemas` (Boolean, optional): Include system schemas

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_list_schemas \
  -H "Content-Type: application/json" \
  -d '{"includeSystemSchemas": false}'
```

**Response:**
```json
{
  "status": "success",
  "schemas": [
    {
      "schema_name": "HR",
      "created": "2023-01-15T10:30:00Z",
      "account_status": "OPEN",
      "default_tablespace": "USERS",
      "profile": "DEFAULT"
    }
  ],
  "count": 1,
  "includeSystem": false,
  "timestamp": "2024-01-06T16:00:00Z"
}
```

#### 9. oracle_create_schema
Schema creation with privileges

**Parameters:**
- `schemaName` (String, required): Schema/user name
- `password` (String, required): User password
- `tablespace` (String, optional): Default tablespace
- `quota` (String, optional): Tablespace quota

#### 10. oracle_create_user
User creation with tablespaces and profiles

**Parameters:**
- `username` (String, required): Username
- `password` (String, required): Password  
- `tablespace` (String, optional): Default tablespace
- `privileges` (Array, optional): List of privileges to grant

#### 11. oracle_grant_privileges
System and object privilege management

**Parameters:**
- `username` (String, required): Target username
- `privilegeType` (String, required): "system" or "object"
- `privileges` (Array, required): List of privileges
- `objectName` (String, optional): Object name for object privileges

#### 12. oracle_revoke_privileges
Privilege revocation

**Parameters:** Same as grant_privileges

#### 13. oracle_user_sessions
Session monitoring and management

**Parameters:**
- `operation` (String, required): "LIST" or "KILL"
- `username` (String, optional): Filter by username
- `sessionId` (Integer, optional): Session ID for KILL operation

#### 14. oracle_lock_account
Account security operations

**Parameters:**
- `username` (String, required): Username to lock
- `reason` (String, optional): Reason for locking

#### 15. oracle_unlock_account
Account management

**Parameters:**
- `username` (String, required): Username to unlock
- `resetPassword` (Boolean, optional): Reset password
- `newPassword` (String, optional): New password if resetting

#### 16. oracle_user_profiles
Profile creation and assignment

**Parameters:**
- `operation` (String, required): "CREATE", "ALTER", "DROP", "LIST"
- `profileName` (String, required): Profile name
- `parameters` (Object, optional): Profile parameters

#### 17. oracle_password_policies
Security policy configuration

**Parameters:**
- `profileName` (String, required): Target profile
- `passwordLifeDays` (Integer, optional): Password lifetime
- `passwordGraceDays` (Integer, optional): Grace period
- `passwordReuseMax` (Integer, optional): Reuse count
- `failedLoginAttempts` (Integer, optional): Failed attempt limit

---

### Table Operations (8 Tools)

#### 18. oracle_list_tables
Table discovery with constraints

**Parameters:**
- `schemaName` (String, optional): Filter by schema
- `includeSystemTables` (Boolean, optional): Include system tables

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_list_tables \
  -H "Content-Type: application/json" \
  -d '{"schemaName": "HR", "includeSystemTables": false}'
```

#### 19. oracle_create_table
DDL with indexes and constraints

**Parameters:**
- `tableName` (String, required): Table name
- `columns` (Array, required): Column definitions
- `primaryKey` (Array, optional): Primary key columns
- `tablespace` (String, optional): Tablespace

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_create_table \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "EMPLOYEES",
    "columns": [
      {"name": "EMP_ID", "type": "NUMBER", "precision": 10, "nullable": false},
      {"name": "EMP_NAME", "type": "VARCHAR2", "length": 100, "nullable": false},
      {"name": "SALARY", "type": "NUMBER", "precision": 10, "scale": 2}
    ],
    "primaryKey": ["EMP_ID"]
  }'
```

#### 20. oracle_describe_table
Complete metadata retrieval

**Parameters:**
- `tableName` (String, required): Table name
- `schemaName` (String, optional): Schema name

#### 21. oracle_insert_records
Data insertion with validation

**Parameters:**
- `tableName` (String, required): Target table
- `records` (Array, required): Array of record objects

#### 22. oracle_query_records
Advanced querying with hints

**Parameters:**
- `tableName` (String, required): Table name
- `columns` (Array, optional): Column list
- `whereClause` (String, optional): WHERE condition
- `orderBy` (String, optional): ORDER BY clause
- `limit` (Integer, optional): Row limit

#### 23. oracle_update_records
Data modification with constraints

**Parameters:**
- `tableName` (String, required): Target table
- `updateData` (Object, required): Column-value pairs
- `whereClause` (String, required): WHERE condition

#### 24. oracle_delete_records
Data removal with referential integrity

**Parameters:**
- `tableName` (String, required): Target table
- `whereClause` (String, required): WHERE condition
- `cascadeDelete` (Boolean, optional): Handle foreign keys

#### 25. oracle_truncate_table
Fast data clearing

**Parameters:**
- `tableName` (String, required): Table name
- `reuseStorage` (Boolean, optional): Reuse storage option

---

## Advanced Analytics (20 Tools)

### SQL Analytics & CTEs (8 Tools)

#### 26. oracle_complex_joins
Multi-table enterprise JOINs

**Parameters:**
- `tables` (Array, required): Tables to join
- `joinConditions` (Array, required): Join conditions
- `selectColumns` (Array, optional): Columns to select
- `whereClause` (String, optional): Additional filters

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_complex_joins \
  -H "Content-Type: application/json" \
  -d '{
    "tables": ["employees e", "departments d", "locations l"],
    "joinConditions": [
      "e.department_id = d.department_id",
      "d.location_id = l.location_id"
    ],
    "selectColumns": ["e.employee_name", "d.department_name", "l.city"],
    "whereClause": "e.salary > 50000"
  }'
```

#### 27. oracle_cte_queries
WITH clause operations

**Parameters:**
- `cteDefinitions` (Array, required): CTE definitions
- `mainQuery` (String, required): Main query using CTEs

#### 28. oracle_window_functions
LEAD/LAG/RANK analytics

**Parameters:**
- `tableName` (String, required): Source table
- `windowFunction` (String, required): Window function
- `partitionBy` (Array, optional): Partition columns
- `orderBy` (Array, optional): Order columns

#### 29. oracle_pivot_operations
PIVOT/UNPIVOT transformations

**Parameters:**
- `operation` (String, required): "PIVOT" or "UNPIVOT"
- `sourceQuery` (String, required): Source data query
- `pivotColumn` (String, required): Column to pivot
- `valueColumns` (Array, required): Value columns

#### 30. oracle_analytical_functions
PERCENTILE, NTILE, CUME_DIST

**Parameters:**
- `tableName` (String, required): Source table
- `analyticalFunction` (String, required): Function name
- `parameters` (Object, required): Function parameters

#### 31. oracle_hierarchical_queries
CONNECT BY operations

**Parameters:**
- `tableName` (String, required): Source table
- `startWithCondition` (String, required): START WITH clause
- `connectByCondition` (String, required): CONNECT BY clause
- `selectColumns` (Array, optional): Columns to select

#### 32. oracle_recursive_cte
Recursive WITH queries

**Parameters:**
- `initialQuery` (String, required): Initial/anchor query
- `recursiveQuery` (String, required): Recursive query
- `unionType` (String, optional): "UNION" or "UNION ALL"

#### 33. oracle_model_clause
MODEL clause calculations

**Parameters:**
- `sourceQuery` (String, required): Source data
- `dimensionBy` (Array, required): Dimension columns
- `measuresClause` (String, required): Measures definition
- `rulesClause` (String, required): Calculation rules

---

### Performance & Indexing (7 Tools)

#### 34. oracle_create_index
B-tree, bitmap, function-based indexes

**Parameters:**
- `indexName` (String, required): Index name
- `tableName` (String, required): Target table
- `columns` (Array, required): Indexed columns
- `indexType` (String, optional): "BTREE", "BITMAP", "FUNCTION"
- `unique` (Boolean, optional): Unique constraint

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_create_index \
  -H "Content-Type: application/json" \
  -d '{
    "indexName": "idx_emp_salary",
    "tableName": "employees",
    "columns": ["salary", "department_id"],
    "indexType": "BTREE",
    "unique": false
  }'
```

#### 35. oracle_analyze_performance
AWR + ADDM integration

**Parameters:**
- `analysisType` (String, required): "AWR", "ADDM", "SQL_TRACE"
- `beginSnapId` (Integer, optional): Starting snapshot ID
- `endSnapId` (Integer, optional): Ending snapshot ID
- `sqlId` (String, optional): Specific SQL ID

#### 36. oracle_optimizer_hints
Cost-based optimizer hints

**Parameters:**
- `sqlQuery` (String, required): Base SQL query
- `hints` (Array, required): Optimizer hints
- `analyzePerformance` (Boolean, optional): Generate execution plan

#### 37. oracle_execution_plans
EXPLAIN PLAN + DBMS_XPLAN

**Parameters:**
- `sqlQuery` (String, required): SQL query to analyze
- `planFormat` (String, optional): Plan output format
- `includeStatistics` (Boolean, optional): Include runtime statistics

#### 38. oracle_table_statistics
DBMS_STATS operations

**Parameters:**
- `operation` (String, required): "GATHER", "DELETE", "EXPORT", "IMPORT"
- `tableName` (String, required): Target table
- `schemaName` (String, optional): Schema name
- `estimatePercent` (Integer, optional): Sampling percentage

#### 39. oracle_sql_tuning
SQL Tuning Advisor integration

**Parameters:**
- `sqlText` (String, required): SQL to tune
- `tuningTask` (String, optional): Task name
- `timeLimit` (Integer, optional): Time limit in seconds

#### 40. oracle_memory_advisor
SGA/PGA recommendations

**Parameters:**
- `advisorType` (String, required): "SGA", "PGA", "MEMORY"
- `targetSize` (String, optional): Target memory size
- `generateReport` (Boolean, optional): Generate advisory report

---

### PL/SQL Operations (5 Tools)

#### 41. oracle_execute_plsql
Anonymous PL/SQL block execution

**Parameters:**
- `plsqlBlock` (String, required): PL/SQL code block
- `parameters` (Object, optional): Input parameters
- `outputResults` (Boolean, optional): Capture output

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_execute_plsql \
  -H "Content-Type: application/json" \
  -d '{
    "plsqlBlock": "BEGIN DBMS_OUTPUT.PUT_LINE('"'"'Hello from PL/SQL!'"'"'); END;",
    "outputResults": true
  }'
```

#### 42. oracle_create_procedure
Stored procedure development

**Parameters:**
- `procedureName` (String, required): Procedure name
- `parameters` (Array, optional): Parameter definitions
- `procedureBody` (String, required): PL/SQL body

#### 43. oracle_create_function
User-defined function creation

**Parameters:**
- `functionName` (String, required): Function name
- `parameters` (Array, optional): Parameter definitions
- `returnType` (String, required): Return data type
- `functionBody` (String, required): PL/SQL body

#### 44. oracle_manage_packages
Package creation and management

**Parameters:**
- `operation` (String, required): "CREATE", "DROP", "COMPILE"
- `packageName` (String, required): Package name
- `packageSpec` (String, optional): Package specification
- `packageBody` (String, optional): Package body

#### 45. oracle_debug_plsql
PL/SQL debugging and profiling

**Parameters:**
- `objectName` (String, required): Object to debug
- `objectType` (String, required): "PROCEDURE", "FUNCTION", "PACKAGE"
- `debugLevel` (String, optional): Debug level

---

## AI-Powered Operations (10 Tools)

### Oracle Vector Search (4 Tools) - Oracle 23c

#### 46. oracle_vector_search
Oracle 23c native vector queries

**Parameters:**
- `vectorTable` (String, required): Table with vector data
- `queryVector` (Array, required): Query vector
- `similarityMetric` (String, optional): "COSINE", "EUCLIDEAN", "MANHATTAN"
- `topK` (Integer, optional): Number of results (default: 10)

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_vector_search \
  -H "Content-Type: application/json" \
  -d '{
    "vectorTable": "document_vectors",
    "queryVector": [0.1, 0.2, 0.3, 0.4],
    "similarityMetric": "COSINE",
    "topK": 5
  }'
```

#### 47. oracle_vector_similarity
VECTOR_DISTANCE functions

**Parameters:**
- `vector1` (Array, required): First vector
- `vector2` (Array, required): Second vector
- `distanceMetric` (String, required): Distance function

#### 48. oracle_vector_clustering
Vector grouping and analysis

**Parameters:**
- `vectorTable` (String, required): Source table
- `vectorColumn` (String, required): Vector column
- `clusterCount` (Integer, required): Number of clusters

#### 49. oracle_vector_index
Vector index management

**Parameters:**
- `operation` (String, required): "CREATE", "DROP", "REBUILD"
- `indexName` (String, required): Index name
- `tableName` (String, required): Table name
- `vectorColumn` (String, required): Vector column

---

### AI Content Analysis (3 Tools)

#### 50. oracle_ai_analyze_document
Document processing and insights

**Parameters:**
- `documentContent` (String, required): Document text
- `analysisType` (String, required): "SENTIMENT", "ENTITIES", "SUMMARY"
- `language` (String, optional): Document language

#### 51. oracle_ai_generate_summary
Content summarization

**Parameters:**
- `sourceText` (String, required): Text to summarize
- `maxLength` (Integer, optional): Summary length limit
- `summaryType` (String, optional): "ABSTRACTIVE", "EXTRACTIVE"

#### 52. oracle_ai_content_classification
Content categorization

**Parameters:**
- `content` (String, required): Content to classify
- `categories` (Array, optional): Predefined categories
- `confidenceThreshold` (Float, optional): Minimum confidence

---

### Oracle-AI Integration (3 Tools)

#### 53. oracle_ai_sql_generation
Natural language to Oracle SQL

**Parameters:**
- `naturalLanguageQuery` (String, required): Human readable query
- `schemaContext` (Array, optional): Available tables/columns
- `dialectPreferences` (Object, optional): Oracle SQL preferences

**Example:**
```bash
curl -X POST http://localhost:8080/v1/tools/oracle_ai_sql_generation \
  -H "Content-Type: application/json" \
  -d '{
    "naturalLanguageQuery": "Show me all employees earning more than 50000 in the IT department",
    "schemaContext": ["employees", "departments"]
  }'
```

#### 54. oracle_ai_query_optimization
AI-powered SQL tuning

**Parameters:**
- `sqlQuery` (String, required): SQL to optimize
- `performanceGoals` (Array, optional): Optimization objectives
- `workloadType` (String, optional): "OLTP", "OLAP", "MIXED"

#### 55. oracle_ai_schema_design
AI schema recommendations

**Parameters:**
- `requirements` (String, required): Schema requirements
- `dataVolume` (Object, optional): Expected data volumes
- `accessPatterns` (Array, optional): Expected query patterns

---

## Enterprise Security (10 Tools)

### VPD & Security (4 Tools) - Enterprise Edition Only

#### 56. oracle_create_vpd_policy
Virtual Private Database policies

**Parameters:**
- `policyName` (String, required): Policy name
- `tableName` (String, required): Target table
- `policyFunction` (String, required): Security function
- `statementTypes` (Array, optional): SQL statement types

#### 57. oracle_manage_rls
Row Level Security management

**Parameters:**
- `operation` (String, required): "ENABLE", "DISABLE", "DROP"
- `policyName` (String, required): Policy name
- `tableName` (String, required): Target table

#### 58. oracle_audit_policies
Database audit configuration

**Parameters:**
- `operation` (String, required): "CREATE", "ENABLE", "DISABLE"
- `auditType` (String, required): "STANDARD", "FINE_GRAINED", "UNIFIED"
- `auditCondition` (String, required): Audit condition

#### 59. oracle_security_assessment
Security vulnerability scanning

**Parameters:**
- `assessmentType` (String, required): "COMPREHENSIVE", "QUICK", "CUSTOM"
- `targetObjects` (Array, optional): Objects to assess

---

### TDE & Encryption (3 Tools)

#### 60. oracle_tde_management
Transparent Data Encryption

**Parameters:**
- `operation` (String, required): "ENABLE", "DISABLE", "KEY_ROTATION"
- `targetType` (String, required): "TABLESPACE", "TABLE", "COLUMN"
- `targetName` (String, required): Target object name

#### 61. oracle_wallet_operations
Oracle Wallet management

**Parameters:**
- `operation` (String, required): "CREATE", "OPEN", "CLOSE", "BACKUP"
- `walletLocation` (String, required): Wallet directory
- `walletPassword` (String, optional): Wallet password

#### 62. oracle_key_management
Encryption key operations

**Parameters:**
- `operation` (String, required): "GENERATE", "ROTATE", "BACKUP"
- `keyType` (String, required): "MASTER", "TABLE", "COLUMN"
- `keyId` (String, optional): Specific key identifier

---

### Database Vault (3 Tools)

#### 63. oracle_database_vault
Database Vault configuration

**Parameters:**
- `operation` (String, required): "ENABLE", "DISABLE", "STATUS"
- `vaultOwner` (String, optional): Vault owner account
- `accountManager` (String, optional): Account manager

#### 64. oracle_vault_realms
Security realms management

**Parameters:**
- `operation` (String, required): "CREATE", "DROP", "MODIFY"
- `realmName` (String, required): Realm name
- `realmObjects` (Array, required): Protected objects

#### 65. oracle_command_rules
Command rule enforcement

**Parameters:**
- `operation` (String, required): "CREATE", "ENABLE", "DISABLE"
- `ruleName` (String, required): Rule name
- `commandRule` (String, required): Rule definition

---

## Enterprise Performance (10 Tools)

### Parallel Execution (2 Tools) - Enterprise Edition Only

#### 66. oracle_parallel_execution
Parallel execution management

**Parameters:**
- `operation` (String, required): "ENABLE", "DISABLE", "HINT_QUERY"
- `tableName` (String, optional): Target table
- `parallelDegree` (Integer, optional): Degree of parallelism
- `sqlQuery` (String, optional): SQL query for hinting

#### 67. oracle_parallel_statistics
Parallel execution monitoring

**Parameters:**
- `sessionId` (Integer, optional): Specific session
- `includeHistory` (Boolean, optional): Include historical data

---

### Partitioning (2 Tools)

#### 68. oracle_table_partitioning
Table partitioning operations

**Parameters:**
- `operation` (String, required): "CREATE_RANGE", "ADD_PARTITION", "DROP_PARTITION"
- `tableName` (String, required): Target table
- `partitionType` (String, optional): Partition type
- `partitionColumn` (String, optional): Partition column

#### 69. oracle_partition_maintenance
Partition maintenance operations

**Parameters:**
- `operation` (String, required): "SPLIT", "MERGE", "EXCHANGE", "TRUNCATE"
- `tableName` (String, required): Target table
- `partitionName` (String, required): Partition name

---

### Materialized Views (2 Tools)

#### 70. oracle_materialized_views
Materialized view management

**Parameters:**
- `operation` (String, required): "CREATE", "REFRESH", "DROP", "ENABLE_REWRITE"
- `mvName` (String, required): MV name
- `baseQuery` (String, optional): Base query for CREATE
- `refreshType` (String, optional): "COMPLETE", "FAST", "FORCE"

#### 71. oracle_mv_refresh_groups
MV refresh group management

**Parameters:**
- `operation` (String, required): "CREATE", "ADD", "REMOVE", "REFRESH"
- `groupName` (String, required): Refresh group name
- `mvList` (Array, optional): List of materialized views

---

### AWR & Performance (2 Tools)

#### 72. oracle_awr_management
AWR management operations

**Parameters:**
- `operation` (String, required): "TAKE_SNAPSHOT", "GENERATE_REPORT", "MODIFY_SETTINGS"
- `beginSnapId` (Integer, optional): Start snapshot
- `endSnapId` (Integer, optional): End snapshot
- `reportType` (String, optional): "TEXT", "HTML"

#### 73. oracle_sql_plan_baselines
SQL Plan Baseline management

**Parameters:**
- `operation` (String, required): "CAPTURE", "EVOLVE", "ENABLE", "DISABLE"
- `sqlHandle` (String, optional): SQL handle
- `planName` (String, optional): Plan name

---

### Advanced Features (2 Tools)

#### 74. oracle_compression_management
Table and index compression

**Parameters:**
- `operation` (String, required): "ENABLE", "DISABLE", "ESTIMATE"
- `objectName` (String, required): Table or index name
- `objectType` (String, required): "TABLE", "INDEX"
- `compressionType` (String, optional): "BASIC", "OLTP", "HCC"

#### 75. oracle_resource_manager
Resource Manager configuration

**Parameters:**
- `operation` (String, required): "CREATE_PLAN", "ASSIGN_GROUP", "SET_LIMITS"
- `planName` (String, optional): Resource plan name
- `groupName` (String, optional): Resource group name
- `cpuP1` (Integer, optional): CPU percentage priority 1

---

## Response Formats

### Standard Response Structure

All tools return responses in the following standard format:

```json
{
  "status": "success" | "error" | "warning",
  "message": "Human-readable message",
  "data": {
    // Tool-specific response data
  },
  "metadata": {
    "timestamp": "2024-01-06T16:00:00Z",
    "executionTimeMs": 150,
    "oracleVersion": "Oracle Database 19c Enterprise Edition",
    "oracleFeatures": ["Multitenant", "AWR", "Partitioning"]
  }
}
```

### Error Response Format

```json
{
  "status": "error",
  "message": "Failed to execute operation",
  "error": {
    "code": "ORA-00942",
    "description": "table or view does not exist",
    "sqlState": "42000",
    "suggestion": "Grant SELECT privilege on the required system view"
  },
  "metadata": {
    "timestamp": "2024-01-06T16:00:00Z",
    "executionTimeMs": 50
  }
}
```

---

## Error Handling

### Oracle Error Code Mapping

| Oracle Error | HTTP Status | MCP Response | Suggested Action |
|--------------|-------------|--------------|------------------|
| ORA-00942 | 403 | Privilege Error | Grant required privileges |
| ORA-01017 | 401 | Authentication Error | Check credentials |
| ORA-12541 | 503 | Connection Error | Check Oracle listener |
| ORA-01031 | 403 | Insufficient Privileges | Grant system privileges |
| ORA-00001 | 409 | Constraint Violation | Check unique constraints |

### Retry Logic

Tools implement automatic retry logic for transient errors:

- **Connection timeouts**: 3 retries with exponential backoff
- **Lock conflicts**: 2 retries with 1-second delay  
- **Temporary unavailability**: 3 retries with 2-second delay

---

## Oracle Feature Mapping

### Version Compatibility Matrix

| Tool Category | Oracle 11g | Oracle 12c | Oracle 18c+ | Oracle 23c |
|---------------|------------|------------|-------------|------------|
| **Core Operations** | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| **Schema Management** | ✅ Full | ✅ Full + PDB | ✅ Full + PDB | ✅ Full + PDB |
| **Analytics** | ✅ Most | ✅ Full | ✅ Full | ✅ Full |
| **PL/SQL** | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| **AI Operations** | ❌ None | ❌ None | ⚠️ Limited | ✅ Full Vector |
| **Enterprise Security** | ⚠️ Limited | ✅ Full | ✅ Full | ✅ Full |
| **Enterprise Performance** | ⚠️ Limited | ✅ Full | ✅ Full | ✅ Full |

### License Requirements

| Tool Category | Standard Edition | Enterprise Edition | Cloud Service |
|---------------|------------------|-------------------|---------------|
| **Core Operations (1-25)** | ✅ Included | ✅ Included | ✅ Included |
| **Advanced Analytics (26-45)** | ⚠️ Limited | ✅ Full | ✅ Full |
| **AI Operations (46-55)** | ❌ Not Available | ✅ Full | ✅ Full |
| **Enterprise Security (56-65)** | ❌ Not Available | ✅ Requires License | ✅ Included |
| **Enterprise Performance (66-75)** | ❌ Not Available | ✅ Requires License | ✅ Included |

---

## Conclusion

This API reference provides comprehensive documentation for all **75+ Oracle MCP Server tools**, including:

- **Complete parameter specifications** with data types and constraints
- **Real-world examples** with curl commands and JSON payloads
- **Oracle feature mappings** for version compatibility
- **Error handling** and troubleshooting guidance
- **Response format standards** for consistent AI/LLM integration

The MCP Oracle Database Server delivers **41-92% more capabilities** than the MongoDB baseline through Oracle-specific tools and enterprise features.

---

**API Reference v1.0.0-PRODUCTION**  
*Complete Oracle MCP tool documentation by officeWorkPlace*
