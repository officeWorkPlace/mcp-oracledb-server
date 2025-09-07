# MCP Oracle Server - Postman Collection Guide

This collection provides comprehensive testing for the Oracle Database MCP Server with CSRF protection and basic authentication.

## 🚀 Quick Start

### 1. Import Collection
1. Open Postman
2. Click "Import" → "Upload Files"
3. Select `MCP_Oracle_Server_Complete_API.postman_collection.json`

### 2. Authentication Setup
The collection is pre-configured with:
- **Username**: `admin`
- **Password**: `admin`
- **Base URL**: `http://localhost:8080`

These can be modified in the collection variables.

## 🔐 CSRF Protection

### Important: POST/PUT/DELETE Requests Require CSRF Tokens

This API uses Spring Security CSRF protection. For any state-changing operations, you need to:

1. **First: Get CSRF Token**
   ```
   GET {{baseUrl}}/api/csrf/token
   ```

2. **Then: Use token in subsequent requests**
   - The collection automatically handles this via pre-request scripts
   - CSRF tokens are stored in collection variables
   - Headers are automatically added to POST/PUT/DELETE requests

### Manual CSRF Workflow (if needed)

If automatic handling fails:

1. **Get Token**: Call "Get CSRF Token" endpoint
2. **Copy Token**: From response `token` field  
3. **Add Header**: `X-XSRF-TOKEN: <your-token>` to POST/PUT/DELETE requests

## 📋 Collection Structure

### 🔐 Authentication & CSRF
- **Get CSRF Token** - Retrieves CSRF token for secure requests
- **Get CSRF Info** - Shows CSRF configuration details

### 🤖 Oracle AI Services  
- **Vector Search Operations** - Vector similarity, clustering, indexing
- **AI Content Analysis** - Document analysis, summarization, classification
- **Oracle-AI Integration** - SQL generation, query optimization

### 📊 Oracle Advanced Analytics
- **SQL Analytics & CTEs** - Complex joins, window functions, pivot operations
- **Hierarchical & Model Operations** - Recursive queries, MODEL clause
- **Index & Performance Analysis** - Advanced indexing, performance tuning

### 🚀 Oracle Performance Services
- **Parallel Execution** - Query parallelization
- **Partitioning** - Table and index partitioning
- **Memory Management** - SGA/PGA optimization
- **AWR Reports** - Automatic Workload Repository analysis

### 🔐 Oracle Security Services  
- **Advanced Security Features** - VPD, Data Redaction, Database Vault
- **Audit Management** - Security policies, privilege analysis

### 🔧 Oracle Core Services
- **Database Management** - Database operations, statistics, backups
- **Schema/User Management** - User creation, privileges, security
- **Table Operations** - CRUD operations, table management

## 🔧 Collection Variables

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | Server base URL |
| `username` | `admin` | Basic auth username |
| `password` | `admin` | Basic auth password |
| `csrfToken` | (auto-set) | CSRF token for secure requests |
| `csrfHeader` | `X-XSRF-TOKEN` | CSRF header name |

## 🧪 Testing Workflow

### Recommended Testing Order:

1. **Authentication Setup**
   ```
   1. Get CSRF Token
   2. Get CSRF Info (optional)
   ```

2. **Health Checks**
   ```
   1. Core Health Check
   2. AI Health Check  
   3. Performance Health Check
   4. Security Health Check
   ```

3. **Core Services**
   ```
   1. List Databases
   2. Database Statistics
   3. List Schemas
   4. Core Capabilities
   ```

4. **AI Services**
   ```
   1. AI Capabilities
   2. Vector Similarity Calculation
   3. Vector Search (requires test data)
   ```

5. **Advanced Features** (as needed)
   - Analytics operations
   - Performance tuning
   - Security configuration

## ⚠️ Important Notes

### CSRF Token Management
- Tokens expire after session timeout
- Re-run "Get CSRF Token" if requests start failing with 403
- Collection automatically adds CSRF headers to POST/PUT/DELETE

### Database Requirements  
- Oracle Database 19c+ recommended
- Some features require Oracle 23c (vector operations)
- Ensure proper database connectivity before testing

### Error Handling
- **401 Unauthorized**: Check username/password
- **403 Forbidden**: CSRF token missing/expired - get new token
- **500 Internal Server Error**: Check database connectivity

## 🔄 Auto-Generated Headers

The collection automatically adds:
- `Authorization: Basic <credentials>` (all requests)
- `X-XSRF-TOKEN: <token>` (POST/PUT/DELETE requests)
- `X-Request-Timestamp: <ISO-timestamp>` (all requests)

## 📊 Test Assertions

Each endpoint includes automatic tests for:
- HTTP status codes (200, 201, etc.)
- Response structure validation
- JSON format verification
- Response time validation (< 5000ms)

## 🐛 Troubleshooting

### Common Issues:

1. **Server not running**: Ensure MCP Oracle Server is started on port 8080
2. **Database connection**: Verify Oracle database is accessible
3. **CSRF failures**: Run "Get CSRF Token" first
4. **Authentication**: Verify admin/admin credentials

### Debug Tips:
- Enable Postman Console for detailed logs
- Check response bodies for error details
- Verify collection variables are set correctly

## 📞 Support

For issues or questions:
1. Check server logs for error details
2. Verify database connectivity
3. Ensure all prerequisites are met
4. Review this documentation

---

**Happy Testing! 🚀**
