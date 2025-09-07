package com.deepai.mcpserver.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Oracle Schema Discovery Utility
 * Provides dynamic schema inspection and column mapping capabilities
 * Makes analytics functions schema-agnostic and truly generic
 */
@Component
public class OracleSchemaDiscovery {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, List<ColumnInfo>> tableSchemaCache = new HashMap<>();

    @Autowired
    public OracleSchemaDiscovery(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get table schema with all column information
     */
    public List<ColumnInfo> getTableSchema(String tableName) {
        String cacheKey = tableName.toUpperCase();
        
        if (tableSchemaCache.containsKey(cacheKey)) {
            return tableSchemaCache.get(cacheKey);
        }

        try {
            String sql = """
                SELECT column_name, data_type, data_length, data_precision, data_scale,
                       nullable, data_default, column_id
                FROM all_tab_columns 
                WHERE table_name = ? 
                AND owner IN (USER, 'C##DEEPAI') 
                ORDER BY column_id
                """;

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName.toUpperCase());
            
            List<ColumnInfo> columnInfos = columns.stream()
                .map(this::mapToColumnInfo)
                .collect(Collectors.toList());

            tableSchemaCache.put(cacheKey, columnInfos);
            return columnInfos;

        } catch (Exception e) {
            System.err.println("Failed to get schema for table " + tableName + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Find columns by data type
     */
    public List<String> getColumnsByType(String tableName, String dataType) {
        List<ColumnInfo> schema = getTableSchema(tableName);
        return schema.stream()
            .filter(col -> col.getDataType().contains(dataType.toUpperCase()))
            .map(ColumnInfo::getColumnName)
            .collect(Collectors.toList());
    }

    /**
     * Get numeric columns for analytics
     */
    public List<String> getNumericColumns(String tableName) {
        return getColumnsByType(tableName, "NUMBER");
    }

    /**
     * Get string columns
     */
    public List<String> getStringColumns(String tableName) {
        List<ColumnInfo> schema = getTableSchema(tableName);
        return schema.stream()
            .filter(col -> col.getDataType().startsWith("VARCHAR") || 
                          col.getDataType().startsWith("CHAR") ||
                          col.getDataType().equals("CLOB"))
            .map(ColumnInfo::getColumnName)
            .collect(Collectors.toList());
    }

    /**
     * Get date/time columns
     */
    public List<String> getDateColumns(String tableName) {
        return getColumnsByType(tableName, "DATE");
    }

    /**
     * Smart column mapping - find best matching column by name pattern
     */
    public String findColumnByPattern(String tableName, List<String> patterns) {
        List<ColumnInfo> schema = getTableSchema(tableName);
        
        for (String pattern : patterns) {
            Optional<ColumnInfo> match = schema.stream()
                .filter(col -> col.getColumnName().toUpperCase().contains(pattern.toUpperCase()))
                .findFirst();
            
            if (match.isPresent()) {
                return match.get().getColumnName();
            }
        }
        
        return null; // No match found
    }

    /**
     * Find ID column (primary key or ID-like column)
     */
    public String findIdColumn(String tableName) {
        // First try to get actual primary key
        try {
            String pkSql = """
                SELECT column_name 
                FROM all_cons_columns acc
                JOIN all_constraints ac ON acc.constraint_name = ac.constraint_name
                WHERE ac.table_name = ? AND ac.constraint_type = 'P'
                AND ac.owner IN (USER, 'C##DEEPAI')
                ORDER BY acc.position
                """;
            
            List<Map<String, Object>> pkColumns = jdbcTemplate.queryForList(pkSql, tableName.toUpperCase());
            if (!pkColumns.isEmpty()) {
                return (String) pkColumns.get(0).get("column_name");
            }
        } catch (Exception e) {
            // Fall back to pattern matching
        }

        // Fall back to pattern-based detection
        return findColumnByPattern(tableName, Arrays.asList("ID", "_ID", "KEY", "PK"));
    }

    /**
     * Find department/category column
     */
    public String findDepartmentColumn(String tableName) {
        return findColumnByPattern(tableName, Arrays.asList("DEPT", "DEPARTMENT", "CATEGORY", "TYPE", "GROUP"));
    }

    /**
     * Find manager/parent column for hierarchical queries
     */
    public String findManagerColumn(String tableName) {
        return findColumnByPattern(tableName, Arrays.asList("MANAGER", "PARENT", "SUPERVISOR", "BOSS"));
    }

    /**
     * Find name column
     */
    public String findNameColumn(String tableName) {
        return findColumnByPattern(tableName, Arrays.asList("NAME", "TITLE", "DESCRIPTION", "LABEL"));
    }

    /**
     * Find email column
     */
    public String findEmailColumn(String tableName) {
        return findColumnByPattern(tableName, Arrays.asList("EMAIL", "MAIL", "E_MAIL"));
    }

    /**
     * Find salary/amount column
     */
    public String findAmountColumn(String tableName) {
        return findColumnByPattern(tableName, Arrays.asList("SALARY", "AMOUNT", "PRICE", "VALUE", "COST"));
    }

    /**
     * Build a safe SELECT query with available columns
     */
    public String buildSafeSelectQuery(String tableName, List<String> requestedColumns, String whereClause, String orderBy, Integer limit) {
        List<ColumnInfo> schema = getTableSchema(tableName);
        Set<String> availableColumns = schema.stream()
            .map(col -> col.getColumnName().toUpperCase())
            .collect(Collectors.toSet());

        // Filter requested columns to only include existing ones
        List<String> validColumns = requestedColumns != null ? 
            requestedColumns.stream()
                .filter(col -> availableColumns.contains(col.toUpperCase()))
                .collect(Collectors.toList())
            : Arrays.asList("*");

        if (validColumns.isEmpty()) {
            validColumns = Arrays.asList("*");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", validColumns));
        sql.append(" FROM ").append(tableName);

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit != null && limit > 0) {
            sql.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
        }

        return sql.toString();
    }

    /**
     * Auto-discover suitable columns for window functions
     */
    public WindowFunctionColumns autoDiscoverWindowColumns(String tableName) {
        List<ColumnInfo> schema = getTableSchema(tableName);
        
        WindowFunctionColumns result = new WindowFunctionColumns();
        
        // Find suitable partition column (department, category, etc.)
        String partitionCol = findDepartmentColumn(tableName);
        if (partitionCol != null) {
            result.setPartitionBy(Arrays.asList(partitionCol));
        }

        // Find suitable order column (salary, amount, date, etc.)
        String amountCol = findAmountColumn(tableName);
        List<String> dateColumns = getDateColumns(tableName);
        
        if (amountCol != null) {
            result.setOrderBy(Arrays.asList(amountCol + " DESC"));
        } else if (!dateColumns.isEmpty()) {
            result.setOrderBy(Arrays.asList(dateColumns.get(0) + " DESC"));
        }

        // Select meaningful columns for display
        List<String> displayColumns = new ArrayList<>();
        String idCol = findIdColumn(tableName);
        String nameCol = findNameColumn(tableName);
        
        if (idCol != null) displayColumns.add(idCol);
        if (nameCol != null) displayColumns.add(nameCol);
        if (amountCol != null) displayColumns.add(amountCol);
        if (partitionCol != null && !displayColumns.contains(partitionCol)) displayColumns.add(partitionCol);

        result.setSelectColumns(displayColumns.isEmpty() ? null : displayColumns);

        return result;
    }

    /**
     * Auto-discover join conditions between tables
     */
    public List<String> autoDiscoverJoinConditions(List<String> tables) {
        if (tables.size() < 2) return Collections.emptyList();

        List<String> joinConditions = new ArrayList<>();

        for (int i = 1; i < tables.size(); i++) {
            String table1 = tables.get(0);
            String table2 = tables.get(i);
            
            // Try to find foreign key relationships
            String joinCondition = findJoinCondition(table1, table2);
            if (joinCondition != null) {
                joinConditions.add(joinCondition);
            } else {
                // Generate alias-based join for common patterns
                String alias1 = table1.substring(0, 1).toLowerCase();
                String alias2 = table2.substring(0, 1).toLowerCase();
                
                // Try common patterns like dept_id, department_id, etc.
                String commonKey = findCommonJoinKey(table1, table2);
                if (commonKey != null) {
                    joinConditions.add(alias1 + "." + commonKey + " = " + alias2 + "." + commonKey);
                }
            }
        }

        return joinConditions;
    }

    private String findJoinCondition(String table1, String table2) {
        try {
            // Look for foreign key relationships
            String fkSql = """
                SELECT 
                    a.column_name as source_column,
                    c.column_name as target_column
                FROM all_cons_columns a
                JOIN all_constraints b ON a.constraint_name = b.constraint_name
                JOIN all_cons_columns c ON b.r_constraint_name = c.constraint_name
                WHERE b.constraint_type = 'R'
                AND ((a.table_name = ? AND c.table_name = ?) OR 
                     (a.table_name = ? AND c.table_name = ?))
                AND b.owner IN (USER, 'C##DEEPAI')
                """;

            List<Map<String, Object>> fks = jdbcTemplate.queryForList(fkSql, 
                table1.toUpperCase(), table2.toUpperCase(),
                table2.toUpperCase(), table1.toUpperCase());

            if (!fks.isEmpty()) {
                Map<String, Object> fk = fks.get(0);
                String sourceCol = (String) fk.get("source_column");
                String targetCol = (String) fk.get("target_column");
                return table1.substring(0,1).toLowerCase() + "." + sourceCol + " = " + 
                       table2.substring(0,1).toLowerCase() + "." + targetCol;
            }
        } catch (Exception e) {
            // Fall back to pattern matching
        }

        return null;
    }

    private String findCommonJoinKey(String table1, String table2) {
        List<ColumnInfo> schema1 = getTableSchema(table1);
        List<ColumnInfo> schema2 = getTableSchema(table2);

        Set<String> columns1 = schema1.stream().map(c -> c.getColumnName().toUpperCase()).collect(Collectors.toSet());
        Set<String> columns2 = schema2.stream().map(c -> c.getColumnName().toUpperCase()).collect(Collectors.toSet());

        // Find common columns that could be join keys
        List<String> commonPatterns = Arrays.asList("ID", "DEPT_ID", "DEPARTMENT_ID", "CATEGORY_ID", "TYPE_ID");
        
        for (String pattern : commonPatterns) {
            if (columns1.contains(pattern) && columns2.contains(pattern)) {
                return pattern;
            }
        }

        return null;
    }

    private ColumnInfo mapToColumnInfo(Map<String, Object> row) {
        ColumnInfo info = new ColumnInfo();
        info.setColumnName((String) row.get("column_name"));
        info.setDataType((String) row.get("data_type"));
        info.setDataLength(((Number) row.get("data_length")).intValue());
        info.setNullable("Y".equals(row.get("nullable")));
        info.setColumnId(((Number) row.get("column_id")).intValue());
        return info;
    }

    /**
     * Clear schema cache (useful for testing or schema changes)
     */
    public void clearCache() {
        tableSchemaCache.clear();
    }

    // Helper classes
    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private int dataLength;
        private boolean nullable;
        private int columnId;

        // Getters and setters
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public int getDataLength() { return dataLength; }
        public void setDataLength(int dataLength) { this.dataLength = dataLength; }
        
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        
        public int getColumnId() { return columnId; }
        public void setColumnId(int columnId) { this.columnId = columnId; }

        @Override
        public String toString() {
            return String.format("%s %s(%d) %s", 
                columnName, dataType, dataLength, nullable ? "NULL" : "NOT NULL");
        }
    }

    public static class WindowFunctionColumns {
        private List<String> partitionBy;
        private List<String> orderBy;
        private List<String> selectColumns;

        public List<String> getPartitionBy() { return partitionBy; }
        public void setPartitionBy(List<String> partitionBy) { this.partitionBy = partitionBy; }
        
        public List<String> getOrderBy() { return orderBy; }
        public void setOrderBy(List<String> orderBy) { this.orderBy = orderBy; }
        
        public List<String> getSelectColumns() { return selectColumns; }
        public void setSelectColumns(List<String> selectColumns) { this.selectColumns = selectColumns; }
    }
}
