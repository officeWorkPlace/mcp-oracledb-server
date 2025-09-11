# 🤝 Contributing to MCP Oracle DB Server

Thank you for your interest in contributing to the **MCP Oracle DB Server**! This document provides comprehensive guidelines for contributing to this production-ready Oracle MCP server project.

## 📋 Table of Contents

- [🚀 Quick Start for Contributors](#-quick-start-for-contributors)
- [🌟 Ways to Contribute](#-ways-to-contribute)
- [🛠️ Development Setup](#️-development-setup)
- [📝 Contribution Guidelines](#-contribution-guidelines)
- [🧪 Testing Guidelines](#-testing-guidelines)
- [📊 Code Standards](#-code-standards)
- [🔍 Pull Request Process](#-pull-request-process)
- [🐛 Bug Reports](#-bug-reports)
- [💡 Feature Requests](#-feature-requests)
- [📚 Documentation](#-documentation)
- [🏆 Recognition](#-recognition)

---

## 🚀 Quick Start for Contributors

### Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **Oracle Database** (11g-23c) for testing
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

### Fork & Clone

```bash
# Fork the repository on GitHub
git clone https://github.com/YOUR_USERNAME/mcp-oracledb-server.git
cd mcp-oracledb-server

# Add upstream remote
git remote add upstream https://github.com/officeWorkPlace/mcp-oracledb-server.git
```

### Build & Test

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run integration tests (requires Oracle DB)
mvn test -Pintegration-tests
```

---

## 🌟 Ways to Contribute

### 🔧 Code Contributions

| Area | Examples | Skill Level |
|------|----------|-------------|
| **Core Tools** | New Oracle operations, DDL improvements | Intermediate |
| **Analytics** | Advanced SQL functions, performance tools | Advanced |
| **AI Features** | Vector search, NL to SQL, optimization | Expert |
| **Enterprise** | Security tools, audit features | Expert |
| **Visualization** | New chart types, dashboard features | Intermediate |
| **Performance** | Query optimization, connection tuning | Advanced |

### 📚 Documentation Contributions

- API documentation improvements
- Tutorial and example creation
- Architecture documentation
- Oracle-specific guides
- Troubleshooting guides

### 🧪 Quality Assurance

- Bug identification and reporting
- Test case creation
- Performance testing
- Oracle version compatibility testing
- Security vulnerability assessment

### 🔍 Code Review

- Review pull requests
- Provide constructive feedback
- Verify Oracle compatibility
- Performance impact assessment

---

## 🛠️ Development Setup

### 1. IDE Configuration

#### IntelliJ IDEA (Recommended)

```xml
<!-- Settings for optimal development -->
<component name="JavaCodeStyleSettings">
  <option name="IMPORT_LAYOUT_TABLE">
    <value>
      <package name="java" withSubpackages="true" static="false"/>
      <package name="javax" withSubpackages="true" static="false"/>
      <package name="oracle" withSubpackages="true" static="false"/>
      <package name="org.springframework" withSubpackages="true" static="false"/>
      <package name="" withSubpackages="true" static="false"/>
    </value>
  </option>
</component>
```

#### Maven Configuration

```xml
<!-- Development profile in pom.xml -->
<profile>
    <id>development</id>
    <properties>
        <spring.profiles.active>dev</spring.profiles.active>
        <maven.test.skip>false</maven.test.skip>
    </properties>
</profile>
```

### 2. Oracle Database Setup

#### Using Docker (Recommended for Development)

```bash
# Oracle XE 21c for testing
docker run -d --name oracle-xe-21c \
  -p 1521:1521 \
  -e ORACLE_PWD=oracle \
  gvenzl/oracle-xe:21-slim

# Wait for startup (2-3 minutes)
docker logs -f oracle-xe-21c
```

#### Test Data Setup

```sql
-- Create test schema
CREATE USER mcp_test IDENTIFIED BY mcp_test;
GRANT CONNECT, RESOURCE, DBA TO mcp_test;

-- Create sample tables for testing
CREATE TABLE mcp_test.test_table (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    created_date DATE DEFAULT SYSDATE
);
```

### 3. Application Configuration

```properties
# src/main/resources/application-dev.properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=mcp_test
spring.datasource.password=mcp_test

# Enable debug logging
logging.level.com.deepai.mcpserver=DEBUG
logging.level.org.springframework.jdbc=DEBUG

# Development-specific settings
oracle.features.detection.enabled=true
oracle.features.cache.ttl=60
```

---

## 📝 Contribution Guidelines

### 🎯 Contribution Areas

#### 1. Core Oracle Tools

**Adding New Database Operations:**

```java
@Service
public class OracleServiceClient {
    
    @Tool(name = "newOracleOperation", 
          description = "Description of the new Oracle operation")
    public Map<String, Object> newOracleOperation(
            @JsonSchemaDescription("Parameter description") String param1,
            @JsonSchemaDescription("Parameter description") Integer param2) {
        
        try {
            // 1. Input validation
            validateInput(param1, param2);
            
            // 2. Oracle feature detection
            if (!featureDetector.supportsFeature("REQUIRED_FEATURE")) {
                return StandardResponse.error("Feature not supported");
            }
            
            // 3. SQL generation with safety checks
            String sql = sqlBuilder.buildSafeSql(param1, param2);
            
            // 4. Database operation
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            
            // 5. Result formatting
            return StandardResponse.success(result, buildMetadata());
            
        } catch (Exception e) {
            logger.error("Error in newOracleOperation", e);
            return StandardResponse.error("Operation failed: " + e.getMessage());
        }
    }
}
```

#### 2. Advanced Analytics Tools

**Adding Analytical Functions:**

```java
@Service
public class OracleAdvancedAnalyticsService {
    
    @Tool(name = "executeNewAnalyticalFunction",
          description = "Execute new Oracle analytical function")
    public Map<String, Object> executeNewAnalyticalFunction(
            @JsonSchemaDescription("Table name") String tableName,
            @JsonSchemaDescription("Function parameters") Map<String, Object> params) {
        
        // Dynamic SQL generation for analytics
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        
        // Add analytical function
        sql.append(buildAnalyticalFunction(params));
        sql.append(" FROM ").append(validateTableName(tableName));
        
        // Add partitioning and ordering
        if (params.containsKey("partitionBy")) {
            sql.append(" PARTITION BY ").append(validateColumns(params.get("partitionBy")));
        }
        
        return executeAnalyticalQuery(sql.toString());
    }
}
```

#### 3. AI-Powered Features

**Adding AI Tools:**

```java
@Service
public class OracleAIService {
    
    @Tool(name = "aiPoweredAnalysis",
          description = "AI-powered data analysis")
    public Map<String, Object> aiPoweredAnalysis(
            @JsonSchemaDescription("Analysis type") String analysisType,
            @JsonSchemaDescription("Data parameters") Map<String, Object> dataParams) {
        
        // AI processing pipeline
        AIAnalysisResult result = aiProcessor.analyze(analysisType, dataParams);
        
        return StandardResponse.success(result, createAIMetadata());
    }
}
```

### 🔒 Security Guidelines

#### Input Validation

```java
public class InputValidator {
    
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_$#]*$");
    
    public static void validateTableName(String tableName) {
        if (!SAFE_IDENTIFIER.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        
        // Check against system tables
        if (isSystemTable(tableName)) {
            throw new SecurityException("Access to system table not allowed");
        }
    }
}
```

#### SQL Injection Prevention

```java
public class OracleSqlBuilder {
    
    public String buildSafeQuery(String table, List<String> columns, String whereClause) {
        // Use parameterized queries only
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // Validate and escape identifiers
        columns.stream()
            .map(this::validateAndEscapeColumn)
            .collect(Collectors.joining(", "));
            
        sql.append(" FROM ").append(validateAndEscapeTable(table));
        
        if (whereClause != null) {
            // Use prepared statement parameters
            sql.append(" WHERE ").append(validateWhereClause(whereClause));
        }
        
        return sql.toString();
    }
}
```

---

## 🧪 Testing Guidelines

### Test Structure

```
src/test/java/
├── unit/                           # Unit tests
│   ├── service/                   # Service layer tests
│   ├── util/                      # Utility class tests
│   └── controller/                # Controller tests
├── integration/                   # Integration tests
│   ├── oracle/                   # Oracle-specific integration
│   └── mcp/                      # MCP protocol tests
└── performance/                   # Performance tests
    ├── load/                     # Load testing
    └── benchmark/                # Benchmark tests
```

### Writing Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class OracleServiceClientTest {
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private OracleFeatureDetector featureDetector;
    
    @InjectMocks
    private OracleServiceClient oracleServiceClient;
    
    @Test
    void testCreateDatabase_Success() {
        // Given
        when(featureDetector.supportsFeature("DATABASE_CREATION")).thenReturn(true);
        when(jdbcTemplate.execute(any(String.class))).thenReturn(true);
        
        // When
        Map<String, Object> result = oracleServiceClient.createDatabase(
            "TESTDB", "traditional", "100M");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        verify(jdbcTemplate).execute(contains("CREATE DATABASE"));
    }
}
```

### Integration Testing

```java
@SpringBootTest
@Testcontainers
class OracleIntegrationTest {
    
    @Container
    static GenericContainer<?> oracle = new GenericContainer<>("gvenzl/oracle-xe:21-slim")
        .withEnv("ORACLE_PASSWORD", "oracle")
        .withExposedPorts(1521)
        .withStartupTimeout(Duration.ofMinutes(5));
    
    @Autowired
    private OracleServiceClient oracleServiceClient;
    
    @Test
    void testDatabaseOperations_EndToEnd() {
        // Test complete database operation workflow
        Map<String, Object> databases = oracleServiceClient.listDatabases(true, true);
        assertThat(databases.get("success")).isEqualTo(true);
        
        // Verify Oracle-specific features
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dbList = (List<Map<String, Object>>) databases.get("data");
        assertThat(dbList).isNotEmpty();
    }
}
```

### Performance Testing

```java
@Component
public class PerformanceBenchmark {
    
    @Timed(value = "oracle.query.execution.time", description = "Oracle query execution time")
    public void benchmarkQueryPerformance() {
        // Benchmark Oracle-specific operations
        StopWatch stopWatch = new StopWatch("Oracle Performance Test");
        
        stopWatch.start("Complex Join");
        executeComplexJoin();
        stopWatch.stop();
        
        stopWatch.start("Analytical Function");
        executeAnalyticalFunction();
        stopWatch.stop();
        
        logger.info("Performance results: {}", stopWatch.prettyPrint());
    }
}
```

---

## 📊 Code Standards

### Java Coding Standards

#### Class Organization

```java
@Service
public class OracleServiceClient {
    
    // 1. Logger (first)
    private static final Logger logger = LoggerFactory.getLogger(OracleServiceClient.class);
    
    // 2. Constants
    private static final String DEFAULT_TABLESPACE = "USERS";
    private static final int DEFAULT_TIMEOUT = 300;
    
    // 3. Dependencies (constructor injection)
    private final JdbcTemplate jdbcTemplate;
    private final OracleFeatureDetector featureDetector;
    
    // 4. Constructor
    public OracleServiceClient(JdbcTemplate jdbcTemplate, 
                              OracleFeatureDetector featureDetector) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureDetector = featureDetector;
    }
    
    // 5. Public @Tool methods (alphabetically ordered)
    
    // 6. Private helper methods
}
```

#### Method Structure

```java
@Tool(name = "toolName", description = "Clear description of tool purpose")
public Map<String, Object> toolMethod(
        @JsonSchemaDescription("Parameter description") String param1,
        @JsonSchemaDescription("Parameter description") Integer param2) {
    
    try {
        // 1. Input validation
        validateInputs(param1, param2);
        
        // 2. Feature detection
        checkRequiredFeatures();
        
        // 3. Business logic
        Object result = executeOperation(param1, param2);
        
        // 4. Success response
        return StandardResponse.success(result, createMetadata());
        
    } catch (ValidationException e) {
        logger.warn("Validation error in {}: {}", "toolName", e.getMessage());
        return StandardResponse.error("Invalid input: " + e.getMessage());
        
    } catch (SQLException e) {
        logger.error("Database error in {}", "toolName", e);
        return StandardResponse.error("Database operation failed");
        
    } catch (Exception e) {
        logger.error("Unexpected error in {}", "toolName", e);
        return StandardResponse.error("Operation failed");
    }
}
```

### Documentation Standards

#### JavaDoc Requirements

```java
/**
 * Creates a new Oracle database with specified parameters.
 * 
 * <p>This method supports both traditional Oracle databases and Pluggable Databases (PDBs)
 * for Oracle 12c and later versions. The method automatically detects Oracle version
 * capabilities and adjusts the DDL accordingly.
 * 
 * @param databaseName the name of the database to create (must be valid Oracle identifier)
 * @param createType the type of database: "traditional" or "pdb"  
 * @param datafileSize the size of initial datafiles (e.g., "100M", "1G")
 * @return standardized response map containing success status, result data, and metadata
 * @throws IllegalArgumentException if database name is invalid
 * @throws SecurityException if user lacks required privileges
 * @since 1.0.0
 * @see <a href="https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/CREATE-DATABASE.html">Oracle CREATE DATABASE</a>
 */
@Tool(name = "createDatabase", description = "Create Oracle database or PDB")
public Map<String, Object> createDatabase(String databaseName, String createType, String datafileSize) {
    // Implementation
}
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| **Classes** | PascalCase | `OracleServiceClient` |
| **Methods** | camelCase | `createDatabase` |
| **Variables** | camelCase | `databaseName` |
| **Constants** | UPPER_SNAKE_CASE | `DEFAULT_TABLESPACE` |
| **Tools** | camelCase | `createDatabase` |
| **Parameters** | camelCase | `tableName` |

---

## 🔍 Pull Request Process

### 1. Pre-Submission Checklist

- [ ] Code follows project standards
- [ ] All tests pass (`mvn test`)
- [ ] Integration tests pass (if applicable)
- [ ] Documentation updated
- [ ] No security vulnerabilities introduced
- [ ] Performance impact assessed

### 2. PR Title Format

```
feat(core): add Oracle 23c vector search support

fix(analytics): resolve SQL syntax error in window functions

docs(api): update tool documentation for new parameters

perf(connection): optimize HikariCP configuration for Oracle
```

### 3. PR Description Template

```markdown
## 🎯 Purpose
Brief description of the change and why it's needed.

## 🔧 Changes Made
- List of specific changes
- New features added  
- Bug fixes implemented

## 🧪 Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing performed

## 🔒 Security Impact
- [ ] No new security vulnerabilities
- [ ] Input validation implemented
- [ ] SQL injection prevention verified

## 📊 Performance Impact
- [ ] No performance degradation
- [ ] Benchmarks run (if applicable)
- [ ] Memory usage assessed

## 📚 Documentation
- [ ] API documentation updated
- [ ] Examples provided
- [ ] README updated (if needed)

## 🔗 Related Issues
Closes #issue_number
```

### 4. Review Process

1. **Automated Checks**: GitHub Actions will run tests
2. **Code Review**: Maintainers review for:
   - Code quality and standards
   - Oracle compatibility
   - Security considerations
   - Performance impact
3. **Testing**: Manual testing on different Oracle versions
4. **Approval**: Two maintainer approvals required
5. **Merge**: Squash and merge after approval

---

## 🐛 Bug Reports

### Bug Report Template

```markdown
## 🐛 Bug Report

### 📋 Environment
- **Oracle Version**: 19c Enterprise Edition
- **Java Version**: 17.0.8
- **Server Version**: 1.0.0-PRODUCTION
- **Operating System**: Windows 11 / Ubuntu 20.04

### 🔍 Description
Clear description of the bug and expected vs actual behavior.

### 🔄 Steps to Reproduce
1. Execute tool: `createDatabase`
2. With parameters: `{"databaseName": "TEST", "createType": "pdb"}`
3. Observe error: `ORA-12345: invalid identifier`

### 📊 Expected Behavior
Database should be created successfully.

### 📊 Actual Behavior
Error message appears and operation fails.

### 📝 Logs
```
2024-01-15 10:30:00 ERROR [OracleServiceClient] Database creation failed
Oracle error: ORA-12345: invalid identifier 'TEST'
```

### 💡 Additional Context
- Works on Oracle 21c
- Fails only on Oracle 19c
- Related to PDB naming restrictions
```

---

## 💡 Feature Requests

### Feature Request Template

```markdown
## 💡 Feature Request

### 🎯 Problem Statement
Describe the problem this feature would solve.

### 💡 Proposed Solution
Detailed description of the proposed feature.

### 🔧 Implementation Ideas
- Technical approach suggestions
- Oracle features to leverage
- Integration considerations

### 📊 Use Cases
- Primary use case
- Secondary use cases
- Target user scenarios

### 🚀 Priority
- [ ] Critical
- [ ] High  
- [ ] Medium
- [ ] Low

### 🔗 References
- Oracle documentation links
- Similar implementations
- Related issues
```

---

## 📚 Documentation

### Documentation Standards

#### API Documentation

```java
/**
 * Oracle Database Management Tool
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * Map<String, Object> result = oracleServiceClient.createDatabase(
 *     "MYDB",           // database name
 *     "traditional",    // database type  
 *     "500M"           // datafile size
 * );
 * }</pre>
 * 
 * <h3>Oracle Version Compatibility:</h3>
 * <ul>
 *   <li>Oracle 11g: Traditional databases only</li>
 *   <li>Oracle 12c+: Traditional and PDB support</li>
 *   <li>Oracle 23c: Enhanced PDB features</li>
 * </ul>
 */
```

#### README Contributions

- Keep sections focused and well-organized
- Include practical examples
- Provide Oracle version compatibility information
- Add troubleshooting guidance

### Documentation Review Process

1. **Technical Accuracy**: Verify all technical details
2. **Completeness**: Ensure all aspects are covered
3. **Clarity**: Clear and understandable language
4. **Examples**: Working code examples provided
5. **Oracle Compatibility**: Version compatibility documented

---

## 🏆 Recognition

### Contributor Recognition

We recognize contributors through:

- **GitHub Contributors** page
- **Release notes** mentions  
- **Documentation** acknowledgments
- **Special badges** for significant contributions

### Contribution Categories

| Category | Requirements | Recognition |
|----------|--------------|-------------|
| **Code Contributor** | Merged PR with code changes | GitHub contributors list |
| **Documentation Hero** | Major documentation improvements | Special mention in README |
| **Bug Hunter** | Multiple bug reports/fixes | Bug Hunter badge |
| **Performance Expert** | Performance optimizations | Performance Expert badge |
| **Oracle Guru** | Oracle expertise contributions | Oracle Expert badge |

### Maintainer Path

Contributors who demonstrate consistent quality contributions may be invited to become maintainers with:

- **Commit access** to the repository
- **Review responsibilities** for PRs
- **Release management** participation
- **Community leadership** role

---

## 📞 Getting Help

### Development Support

- **GitHub Discussions**: Technical questions and discussions
- **GitHub Issues**: Bug reports and feature requests
- **Email**: [office.place.work.007@gmail.com](mailto:office.place.work.007@gmail.com)

### Oracle-Specific Questions

- **Oracle Documentation**: Official Oracle documentation
- **Oracle Community**: Oracle developer communities
- **Project Discussions**: Oracle-specific implementation questions

---

**Thank you for contributing to the MCP Oracle DB Server project!** 

Your contributions help make this the most comprehensive Oracle MCP server available, serving the growing AI and LLM community with production-ready Oracle database capabilities.

---

*Built with ❤️ by the MCP Oracle DB Server community*
