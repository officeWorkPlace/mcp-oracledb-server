#!/usr/bin/env python3
"""
Banking Database Test Script - Using Correct API Endpoints
Tests the banking database with the actual MCP Oracle Server API endpoints
"""

import requests
import json
from datetime import datetime
import sys

# Configuration
BASE_URL = "http://localhost:8080"
USERNAME = "admin"
PASSWORD = "admin"

def make_request(endpoint, method="GET", data=None, params=None):
    """Make HTTP request with basic auth"""
    try:
        auth = (USERNAME, PASSWORD)
        headers = {'Content-Type': 'application/json'} if data else {}
        
        if method == "GET":
            response = requests.get(f"{BASE_URL}{endpoint}", auth=auth, headers=headers, params=params)
        elif method == "POST":
            if data:
                response = requests.post(f"{BASE_URL}{endpoint}", json=data, auth=auth, headers=headers, params=params)
            else:
                response = requests.post(f"{BASE_URL}{endpoint}", auth=auth, headers=headers, params=params)
        
        return response.status_code, response.json() if response.content else {}
    except Exception as e:
        return 500, {"error": str(e)}

def test_service_health():
    """Test service health and connectivity"""
    print("🔍 Testing service health...")
    status, response = make_request("/api/oracle/core/health")
    
    if status == 200:
        print("✅ Service is healthy")
        print(f"   Service: {response.get('service', 'Unknown')}")
        print(f"   Available endpoints: {response.get('availableEndpoints', 'Unknown')}")
        return True
    else:
        print(f"❌ Service health check failed: {response}")
        return False

def test_banking_tables():
    """Test banking table access"""
    print("\n📊 Testing banking table access...")
    
    # Test list tables
    status, response = make_request("/api/oracle/core/tables")
    
    if status == 200 and response.get('status') == 'success':
        tables = response.get('data', [])
        banking_tables = [t for t in tables if t.get('TABLE_NAME') in 
                         ['CUSTOMERS', 'ACCOUNTS', 'LOANS', 'BRANCHES', 'EMPLOYEES']]
        print(f"✅ Found {len(banking_tables)} banking tables")
        for table in banking_tables:
            print(f"   - {table.get('TABLE_NAME')}")
        return len(banking_tables) > 0
    else:
        print(f"❌ Failed to list tables: {response}")
        return False

def test_table_query():
    """Test querying banking data"""
    print("\n📈 Testing table queries...")
    
    # Test querying customers table
    status, response = make_request("/api/oracle/core/tables/CUSTOMERS/query", "POST", 
                                  data=[], params={"limit": 5})
    
    if status == 200 and response.get('status') == 'success':
        customers = response.get('data', [])
        print(f"✅ Retrieved {len(customers)} customer records")
        if customers:
            print("   Sample customer data:")
            for customer in customers[:3]:
                name = f"{customer.get('FIRST_NAME', '')} {customer.get('LAST_NAME', '')}"
                print(f"   - {name}, Credit Score: {customer.get('CREDIT_SCORE', 'N/A')}")
        return True
    else:
        print(f"❌ Failed to query customers: {response}")
        return False

def test_analytics_capabilities():
    """Test analytics capabilities with banking data"""
    print("\n📊 Testing analytics capabilities...")
    
    # Test window functions on CUSTOMERS table
    window_data = {
        "partitionBy": ["STATE"],
        "orderBy": ["CREDIT_SCORE DESC"],
        "selectColumns": ["CUSTOMER_ID", "FIRST_NAME", "LAST_NAME", "STATE", "CREDIT_SCORE"]
    }
    
    status, response = make_request("/api/oracle/analytics/window-functions", "POST",
                                  data=window_data,
                                  params={"tableName": "CUSTOMERS", "windowFunction": "ROW_NUMBER()"})
    
    if status == 200 and response.get('status') == 'success':
        rankings = response.get('data', [])
        print(f"✅ Window functions: {len(rankings)} customer rankings by state")
        
        # Show sample rankings
        if rankings:
            print("   Sample rankings:")
            for rank in rankings[:5]:
                name = f"{rank.get('FIRST_NAME', '')} {rank.get('LAST_NAME', '')}"
                print(f"   - {name} ({rank.get('STATE', '')}) - Score: {rank.get('CREDIT_SCORE', 'N/A')}")
        return True
    else:
        print(f"❌ Window functions test failed: {response}")
        return False

def test_complex_joins():
    """Test complex joins across banking tables"""
    print("\n🔗 Testing complex joins...")
    
    join_data = {
        "tables": ["CUSTOMERS", "ACCOUNTS"],
        "joinConditions": ["CUSTOMERS.CUSTOMER_ID = ACCOUNTS.CUSTOMER_ID"],
        "selectColumns": ["CUSTOMERS.FIRST_NAME", "CUSTOMERS.LAST_NAME", "ACCOUNTS.ACCOUNT_TYPE", "ACCOUNTS.BALANCE"]
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=join_data)
    
    if status == 200 and response.get('status') == 'success':
        join_results = response.get('data', [])
        print(f"✅ Complex joins: {len(join_results)} customer-account records")
        
        # Show account type distribution
        if join_results:
            account_types = {}
            for record in join_results:
                acc_type = record.get('ACCOUNT_TYPE', 'Unknown')
                account_types[acc_type] = account_types.get(acc_type, 0) + 1
            
            print("   Account type distribution:")
            for acc_type, count in account_types.items():
                print(f"   - {acc_type}: {count} accounts")
        return True
    else:
        print(f"❌ Complex joins test failed: {response}")
        return False

def test_banking_analytics():
    """Test banking-specific analytics"""
    print("\n🏦 Testing banking analytics...")
    
    # Test hierarchical queries (if we have manager relationships)
    hierarchy_data = {
        "selectColumns": ["EMPLOYEE_ID", "FIRST_NAME", "LAST_NAME", "POSITION", "LEVEL"]
    }
    
    status, response = make_request("/api/oracle/analytics/hierarchical-queries", "POST",
                                  data=hierarchy_data,
                                  params={
                                      "tableName": "EMPLOYEES",
                                      "startWithCondition": "MANAGER_ID IS NULL",
                                      "connectByCondition": "PRIOR EMPLOYEE_ID = MANAGER_ID"
                                  })
    
    if status == 200 and response.get('status') == 'success':
        hierarchy = response.get('data', [])
        print(f"✅ Hierarchical query: {len(hierarchy)} employee records in hierarchy")
        
        # Show management structure
        if hierarchy:
            print("   Management hierarchy sample:")
            for emp in hierarchy[:5]:
                level = "  " * (int(emp.get('LEVEL', 1)) - 1)
                name = f"{emp.get('FIRST_NAME', '')} {emp.get('LAST_NAME', '')}"
                position = emp.get('POSITION', 'Unknown')
                print(f"   {level}- {name} ({position})")
        return True
    else:
        print(f"❌ Banking analytics test failed: {response}")
        return False

def test_data_summary():
    """Get summary of banking data"""
    print("\n📋 Banking data summary...")
    
    tables = ['CUSTOMERS', 'ACCOUNTS', 'LOANS', 'TRANSACTIONS', 'EMPLOYEES', 'BRANCHES']
    summary = {}
    
    for table in tables:
        status, response = make_request(f"/api/oracle/core/tables/{table}/query", "POST",
                                      data=[], params={"limit": 1})
        if status == 200 and response.get('status') == 'success':
            # Get count by querying all and counting (simple approach)
            status2, response2 = make_request(f"/api/oracle/core/tables/{table}/query", "POST", data=[])
            if status2 == 200 and response2.get('status') == 'success':
                count = len(response2.get('data', []))
                summary[table] = count
            else:
                summary[table] = "Unknown"
        else:
            summary[table] = "Error"
    
    print("📊 Banking Database Summary:")
    for table, count in summary.items():
        print(f"   ✓ {table:<20}: {count:>6} records")
    
    return len([c for c in summary.values() if isinstance(c, int)]) > 0

def main():
    """Run comprehensive banking database tests"""
    print("🏦 Banking Database MCP Oracle Server Test Suite")
    print("=" * 65)
    print(f"Testing against: {BASE_URL}")
    print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # Run tests
    tests = [
        ("Service Health", test_service_health),
        ("Banking Tables", test_banking_tables),
        ("Table Queries", test_table_query),
        ("Analytics Capabilities", test_analytics_capabilities),
        ("Complex Joins", test_complex_joins),
        ("Banking Analytics", test_banking_analytics),
        ("Data Summary", test_data_summary)
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        try:
            if test_func():
                passed += 1
            else:
                print(f"\n❌ {test_name} test failed")
        except Exception as e:
            print(f"\n💥 {test_name} test crashed: {str(e)}")
    
    # Summary
    print("\n" + "=" * 65)
    print(f"📋 Test Results: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 All banking database tests passed!")
        return 0
    else:
        print("⚠️  Some tests failed. The banking database is populated but may need adjustments.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
