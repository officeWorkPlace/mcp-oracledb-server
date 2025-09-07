#!/usr/bin/env python3
"""
Database Access Verification Script
Tests what the API can see with current database connection and user privileges
"""

import requests
import json
import sys
import time

# Configuration
BASE_URL = "http://localhost:8080/api/oracle/core"
TIMEOUT = 30
AUTH = ('admin', 'admin')  # Basic auth credentials from application.properties

def test_connection():
    """Test basic API connectivity"""
    print("=" * 60)
    print("1. TESTING API CONNECTIVITY")
    print("=" * 60)
    
    try:
        response = requests.get(f"{BASE_URL}/databases/stats", auth=AUTH, timeout=TIMEOUT)
        if response.status_code == 200:
            data = response.json()
            print(f"✓ API Connection: SUCCESS")
            print(f"  Status: {data.get('status', 'unknown')}")
            if 'statistics' in data:
                stats = data['statistics']
                print(f"  Current User: {stats.get('current_user', 'unknown')}")
                print(f"  Database Name: {stats.get('database_name', 'unknown')}")
                print(f"  Connection Status: {stats.get('connection_status', 'unknown')}")
                
                # Check privilege level
                privilege_level = data.get('privilegeLevel', 'UNKNOWN')
                print(f"  Privilege Level: {privilege_level}")
                
                if 'table_count' in stats:
                    print(f"  Accessible Tables: {stats['table_count']}")
                if 'user_table_count' in stats:
                    print(f"  User Tables: {stats['user_table_count']}")
                if 'accessible_table_count' in stats:
                    print(f"  Limited Table Access: {stats['accessible_table_count']}")
            return True
        else:
            print(f"✗ API Connection: FAILED - HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ API Connection: ERROR - {str(e)}")
        return False

def check_schemas():
    """Check what schemas are visible"""
    print("\n" + "=" * 60)
    print("2. CHECKING ACCESSIBLE SCHEMAS")
    print("=" * 60)
    
    try:
        response = requests.get(f"{BASE_URL}/schemas", auth=AUTH, timeout=TIMEOUT)
        if response.status_code == 200:
            data = response.json()
            if data.get('status') == 'success':
                schemas = data.get('schemas', [])
                print(f"✓ Found {len(schemas)} schemas:")
                for schema in schemas:
                    schema_name = schema.get('schema_name')
                    account_status = schema.get('account_status', 'unknown')
                    print(f"  - {schema_name} ({account_status})")
                return schemas
            else:
                print(f"✗ Schema Query Failed: {data.get('message', 'unknown error')}")
        else:
            print(f"✗ Schema Query: HTTP {response.status_code}")
    except Exception as e:
        print(f"✗ Schema Query Error: {str(e)}")
    
    return []

def check_all_tables():
    """Check what tables are accessible across all schemas"""
    print("\n" + "=" * 60)
    print("3. CHECKING ALL ACCESSIBLE TABLES")
    print("=" * 60)
    
    try:
        # Get all tables without schema filter
        response = requests.get(f"{BASE_URL}/tables", auth=AUTH, timeout=TIMEOUT)
        if response.status_code == 200:
            data = response.json()
            if data.get('status') == 'success':
                tables = data.get('tables', [])
                print(f"✓ Found {len(tables)} total accessible tables")
                
                # Group by owner/schema
                schema_tables = {}
                banking_tables = []
                
                for table in tables:
                    owner = table.get('owner', 'UNKNOWN')
                    table_name = table.get('table_name')
                    
                    if owner not in schema_tables:
                        schema_tables[owner] = []
                    schema_tables[owner].append(table_name)
                    
                    # Check if this looks like banking data
                    if any(keyword in table_name.upper() for keyword in ['CUSTOMER', 'ACCOUNT', 'LOAN', 'TRANSACTION', 'PAYMENT', 'CARD', 'BRANCH']):
                        banking_tables.append(f"{owner}.{table_name}")
                
                print(f"\nTables by Schema:")
                for schema, table_list in schema_tables.items():
                    print(f"  {schema}: {len(table_list)} tables")
                    if len(table_list) <= 10:
                        for table in table_list[:5]:
                            print(f"    - {table}")
                        if len(table_list) > 5:
                            print(f"    ... and {len(table_list) - 5} more")
                    else:
                        for table in table_list[:3]:
                            print(f"    - {table}")
                        print(f"    ... and {len(table_list) - 3} more")
                
                if banking_tables:
                    print(f"\n🏦 POTENTIAL BANKING TABLES FOUND:")
                    for table in banking_tables:
                        print(f"  ✓ {table}")
                else:
                    print(f"\n⚠️  NO BANKING-RELATED TABLES FOUND")
                
                return tables, banking_tables
            else:
                print(f"✗ Table Query Failed: {data.get('message', 'unknown error')}")
        else:
            print(f"✗ Table Query: HTTP {response.status_code}")
    except Exception as e:
        print(f"✗ Table Query Error: {str(e)}")
    
    return [], []

def test_specific_banking_tables():
    """Test access to specific banking table names"""
    print("\n" + "=" * 60)
    print("4. TESTING SPECIFIC BANKING TABLE ACCESS")
    print("=" * 60)
    
    banking_table_names = ['CUSTOMERS', 'ACCOUNTS', 'LOANS', 'TRANSACTIONS', 'PAYMENTS', 'CARDS', 'BRANCHES']
    
    for table_name in banking_table_names:
        print(f"\nTesting table: {table_name}")
        try:
            # Try to query a few records from the table
            query_params = {
                'limit': 5
            }
            response = requests.post(f"{BASE_URL}/tables/{table_name}/query", json=[], params=query_params, auth=AUTH, timeout=TIMEOUT)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('status') == 'success':
                    results = data.get('results', [])
                    count = data.get('count', 0)
                    print(f"  ✓ {table_name}: Accessible, {count} records found")
                    if count > 0 and results:
                        # Show first record structure
                        first_record = results[0]
                        columns = list(first_record.keys())[:5]  # Show first 5 columns
                        print(f"    Columns (first 5): {', '.join(columns)}")
                else:
                    print(f"  ✗ {table_name}: Query failed - {data.get('message', 'unknown error')}")
            else:
                print(f"  ✗ {table_name}: HTTP {response.status_code}")
        except Exception as e:
            print(f"  ✗ {table_name}: Error - {str(e)}")

def test_with_schema_prefix():
    """Test accessing tables with different schema prefixes"""
    print("\n" + "=" * 60)
    print("5. TESTING WITH SCHEMA PREFIXES")
    print("=" * 60)
    
    # Common Oracle schema names where banking data might be
    schema_prefixes = ['C##DEEPAI', 'DEEPAI', 'BANKING', 'DEMO', 'HR', 'OE', 'PM', 'IX', 'SH', 'BI']
    table_name = 'CUSTOMERS'
    
    for schema in schema_prefixes:
        full_table_name = f"{schema}.{table_name}"
        print(f"\nTesting: {full_table_name}")
        
        try:
            query_params = {
                'limit': 3
            }
            response = requests.post(f"{BASE_URL}/tables/{full_table_name}/query", json=[], params=query_params, auth=AUTH, timeout=TIMEOUT)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('status') == 'success':
                    count = data.get('count', 0)
                    print(f"  ✓ {full_table_name}: Found {count} records")
                    if count > 0:
                        results = data.get('results', [])
                        if results:
                            first_record = results[0]
                            print(f"    Sample columns: {list(first_record.keys())[:3]}")
                else:
                    error_msg = data.get('message', '')
                    if 'table or view does not exist' in error_msg.lower():
                        print(f"  - {full_table_name}: Not found")
                    else:
                        print(f"  ✗ {full_table_name}: {error_msg}")
            else:
                print(f"  ✗ {full_table_name}: HTTP {response.status_code}")
        except Exception as e:
            print(f"  ✗ {full_table_name}: {str(e)}")

def main():
    """Main test execution"""
    print("🔍 ORACLE MCP SERVER - DATABASE ACCESS VERIFICATION")
    print("=" * 60)
    print("Testing database connectivity and data accessibility...")
    
    # Test 1: Basic connectivity
    if not test_connection():
        print("\n❌ Cannot connect to API. Please ensure the MCP Oracle Server is running.")
        return False
    
    # Test 2: Check schemas
    schemas = check_schemas()
    
    # Test 3: Check all accessible tables
    all_tables, banking_tables = check_all_tables()
    
    # Test 4: Test specific banking tables
    test_specific_banking_tables()
    
    # Test 5: Test with schema prefixes
    test_with_schema_prefix()
    
    # Summary
    print("\n" + "=" * 60)
    print("📊 VERIFICATION SUMMARY")
    print("=" * 60)
    
    if banking_tables:
        print(f"✅ SUCCESS: Found {len(banking_tables)} potential banking tables:")
        for table in banking_tables:
            print(f"   - {table}")
    else:
        print("⚠️  NO BANKING TABLES FOUND - Possible causes:")
        print("   1. Banking data is in a different schema not accessible to current user")
        print("   2. Tables have different names than expected")
        print("   3. Database user lacks proper permissions")
        print("   4. Banking data hasn't been loaded yet")
    
    print(f"\n📈 Database Access Statistics:")
    print(f"   - Total schemas visible: {len(schemas)}")
    print(f"   - Total tables accessible: {len(all_tables)}")
    print(f"   - Banking-related tables: {len(banking_tables)}")
    
    print(f"\n🔧 NEXT STEPS:")
    if not banking_tables:
        print("   1. Check if banking data exists in Oracle database")
        print("   2. Verify current user (C##DEEPAI) has SELECT permissions on banking tables")
        print("   3. Confirm the schema where banking data is stored")
        print("   4. Update API configuration if needed to access correct schema")
    else:
        print("   1. Banking tables are accessible - API should return data")
        print("   2. If tests still return empty results, check table contents")
        print("   3. Verify data has been properly loaded into the tables")
    
    return True

if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n⚠️  Test interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n\n❌ Unexpected error: {str(e)}")
        sys.exit(1)
