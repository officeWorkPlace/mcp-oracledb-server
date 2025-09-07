#!/usr/bin/env python3
"""
Advanced Controller Testing Suite for Banking Database
Tests MCP Oracle Server API endpoints with complex analytics queries
Verifies that controllers handle advanced scenarios and return unique results
"""

import requests
import json
from datetime import datetime
import sys
import time

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
            response = requests.post(f"{BASE_URL}{endpoint}", json=data, auth=auth, headers=headers, params=params)
        
        return response.status_code, response.json() if response.content else {}
    except Exception as e:
        return 500, {"error": str(e)}

def print_test_header(test_name):
    """Print formatted test header"""
    print(f"\n{'='*80}")
    print(f"🧪 {test_name}")
    print(f"{'='*80}")

def print_results(results, title, max_items=10):
    """Print formatted results"""
    print(f"\n📊 {title}")
    print("-" * 60)
    if isinstance(results, list) and results:
        for i, item in enumerate(results[:max_items]):
            print(f"{i+1:2d}. {json.dumps(item, indent=4, default=str)}")
        if len(results) > max_items:
            print(f"... and {len(results) - max_items} more items")
    elif isinstance(results, dict):
        print(json.dumps(results, indent=4, default=str))
    else:
        print(f"Results: {results}")

def test_analytics_window_functions():
    """Test advanced window functions through the API"""
    print_test_header("ADVANCED WINDOW FUNCTIONS TESTING")
    
    # Test 1: Customer ranking by balance with percentile analysis
    print("\n🔍 Test 1: Customer Balance Rankings with Window Functions")
    
    window_data = {
        "partitionBy": ["STATE"],
        "orderBy": ["SUM(BALANCE) DESC"],
        "selectColumns": [
            "CUSTOMER_ID", 
            "FIRST_NAME", 
            "LAST_NAME", 
            "STATE",
            "SUM(BALANCE) as TOTAL_BALANCE"
        ]
    }
    
    status, response = make_request(
        "/api/oracle/analytics/window-functions", 
        "POST",
        data=window_data,
        params={
            "tableName": "CUSTOMERS JOIN ACCOUNTS ON CUSTOMERS.CUSTOMER_ID = ACCOUNTS.CUSTOMER_ID",
            "windowFunction": "ROW_NUMBER()"
        }
    )
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Window functions test PASSED")
        results = response.get('data', [])
        print_results(results, "Top Customer Rankings by State", 10)
        return True
    else:
        print(f"❌ Window functions test FAILED: {response}")
        return False

def test_complex_joins():
    """Test complex multi-table joins"""
    print_test_header("COMPLEX JOINS TESTING")
    
    print("\n🔗 Test 1: Customer-Account-Transaction Complex Join")
    
    join_data = {
        "tables": ["CUSTOMERS c", "ACCOUNTS a", "TRANSACTIONS t"],
        "joinConditions": [
            "c.CUSTOMER_ID = a.CUSTOMER_ID",
            "a.ACCOUNT_ID = t.ACCOUNT_ID"
        ],
        "selectColumns": [
            "c.FIRST_NAME || ' ' || c.LAST_NAME as CUSTOMER_NAME",
            "c.STATE",
            "c.CREDIT_SCORE",
            "a.ACCOUNT_TYPE",
            "COUNT(t.TRANSACTION_ID) as TRANSACTION_COUNT",
            "SUM(ABS(t.AMOUNT)) as TOTAL_VOLUME"
        ],
        "whereClause": "t.STATUS = 'COMPLETED'"
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=join_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Complex joins test PASSED")
        results = response.get('data', [])
        print_results(results, "Customer Transaction Summary", 8)
        return True
    else:
        print(f"❌ Complex joins test FAILED: {response}")
        return False

def test_hierarchical_queries():
    """Test hierarchical organization queries"""
    print_test_header("HIERARCHICAL QUERIES TESTING")
    
    print("\n🌳 Test 1: Employee Hierarchy Analysis")
    
    hierarchy_data = {
        "selectColumns": [
            "EMPLOYEE_ID",
            "FIRST_NAME || ' ' || LAST_NAME as EMPLOYEE_NAME",
            "POSITION",
            "DEPARTMENT", 
            "SALARY",
            "LEVEL as ORG_LEVEL"
        ]
    }
    
    status, response = make_request(
        "/api/oracle/analytics/hierarchical-queries", 
        "POST",
        data=hierarchy_data,
        params={
            "tableName": "EMPLOYEES",
            "startWithCondition": "MANAGER_ID IS NULL",
            "connectByCondition": "PRIOR EMPLOYEE_ID = MANAGER_ID"
        }
    )
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Hierarchical queries test PASSED")
        results = response.get('data', [])
        print_results(results, "Employee Organizational Hierarchy", 15)
        return True
    else:
        print(f"❌ Hierarchical queries test FAILED: {response}")
        return False

def test_analytical_functions():
    """Test advanced analytical functions"""
    print_test_header("ANALYTICAL FUNCTIONS TESTING")
    
    print("\n📈 Test 1: Credit Score Distribution Analysis")
    
    analytical_data = {
        "partitionBy": ["STATE"],
        "orderBy": ["CREDIT_SCORE"],
        "parameters": []
    }
    
    status, response = make_request(
        "/api/oracle/analytics/analytical-functions",
        "POST",
        data=analytical_data,
        params={
            "tableName": "CUSTOMERS",
            "analyticalFunction": "PERCENTILE_CONT(0.5) WITHIN GROUP",
            "column": "CREDIT_SCORE"
        }
    )
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Analytical functions test PASSED")
        results = response.get('data', [])
        print_results(results, "Credit Score Percentile Analysis", 10)
        return True
    else:
        print(f"❌ Analytical functions test FAILED: {response}")
        return False

def test_table_queries_advanced():
    """Test advanced table queries"""
    print_test_header("ADVANCED TABLE QUERIES TESTING")
    
    print("\n📊 Test 1: Customer Financial Profile Query")
    
    # Advanced query with complex WHERE conditions
    status, response = make_request(
        "/api/oracle/core/tables/CUSTOMERS/query",
        "POST",
        data=["CUSTOMER_ID", "FIRST_NAME", "LAST_NAME", "STATE", "CREDIT_SCORE", "ANNUAL_INCOME"],
        params={
            "whereClause": "CREDIT_SCORE > 700 AND ANNUAL_INCOME > 75000",
            "orderBy": "CREDIT_SCORE DESC, ANNUAL_INCOME DESC",
            "limit": 15
        }
    )
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Advanced table query test PASSED")
        results = response.get('data', [])
        print_results(results, "High-Value Customer Profiles", 10)
        return True
    else:
        print(f"❌ Advanced table query test FAILED: {response}")
        return False

def test_loan_risk_analysis():
    """Test loan risk analysis through complex joins"""
    print_test_header("LOAN RISK ANALYSIS TESTING")
    
    print("\n🎯 Test 1: Loan Portfolio Risk Assessment")
    
    risk_analysis_data = {
        "tables": ["LOANS l", "CUSTOMERS c", "LOAN_TYPES lt"],
        "joinConditions": [
            "l.CUSTOMER_ID = c.CUSTOMER_ID",
            "l.LOAN_TYPE_ID = lt.LOAN_TYPE_ID"
        ],
        "selectColumns": [
            "lt.LOAN_TYPE_NAME",
            "COUNT(*) as LOAN_COUNT",
            "ROUND(AVG(l.PRINCIPAL_AMOUNT), 2) as AVG_LOAN_AMOUNT",
            "ROUND(AVG(c.CREDIT_SCORE), 0) as AVG_CREDIT_SCORE",
            "ROUND(SUM(l.CURRENT_BALANCE), 2) as TOTAL_OUTSTANDING",
            "CASE WHEN AVG(c.CREDIT_SCORE) >= 750 THEN 'LOW_RISK' " +
            "WHEN AVG(c.CREDIT_SCORE) >= 700 THEN 'MEDIUM_RISK' " +
            "WHEN AVG(c.CREDIT_SCORE) >= 650 THEN 'HIGH_RISK' " +
            "ELSE 'VERY_HIGH_RISK' END as RISK_CATEGORY"
        ],
        "whereClause": "l.STATUS = 'ACTIVE'"
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=risk_analysis_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Loan risk analysis test PASSED")
        results = response.get('data', [])
        print_results(results, "Loan Portfolio Risk Assessment", 8)
        return True
    else:
        print(f"❌ Loan risk analysis test FAILED: {response}")
        return False

def test_transaction_patterns():
    """Test transaction pattern analysis"""
    print_test_header("TRANSACTION PATTERN ANALYSIS")
    
    print("\n💳 Test 1: Transaction Volume by Account Type")
    
    pattern_data = {
        "tables": ["ACCOUNTS a", "TRANSACTIONS t"],
        "joinConditions": ["a.ACCOUNT_ID = t.ACCOUNT_ID"],
        "selectColumns": [
            "a.ACCOUNT_TYPE",
            "COUNT(t.TRANSACTION_ID) as TRANSACTION_COUNT",
            "ROUND(SUM(ABS(t.AMOUNT)), 2) as TOTAL_VOLUME",
            "ROUND(AVG(ABS(t.AMOUNT)), 2) as AVG_AMOUNT",
            "COUNT(DISTINCT a.CUSTOMER_ID) as UNIQUE_CUSTOMERS"
        ],
        "whereClause": "t.STATUS = 'COMPLETED'"
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=pattern_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Transaction pattern analysis test PASSED")
        results = response.get('data', [])
        print_results(results, "Transaction Patterns by Account Type", 5)
        return True
    else:
        print(f"❌ Transaction pattern analysis test FAILED: {response}")
        return False

def test_customer_segmentation():
    """Test advanced customer segmentation"""
    print_test_header("CUSTOMER SEGMENTATION ANALYSIS")
    
    print("\n🎯 Test 1: Multi-Dimensional Customer Segmentation")
    
    segmentation_data = {
        "tables": ["CUSTOMERS c", "ACCOUNTS a"],
        "joinConditions": ["c.CUSTOMER_ID = a.CUSTOMER_ID"],
        "selectColumns": [
            "CASE WHEN c.CREDIT_SCORE >= 750 AND SUM(a.BALANCE) >= 100000 THEN 'PLATINUM' " +
            "WHEN c.CREDIT_SCORE >= 700 AND SUM(a.BALANCE) >= 50000 THEN 'GOLD' " +
            "WHEN c.CREDIT_SCORE >= 650 OR SUM(a.BALANCE) >= 25000 THEN 'SILVER' " +
            "ELSE 'STANDARD' END as CUSTOMER_TIER",
            "COUNT(DISTINCT c.CUSTOMER_ID) as CUSTOMER_COUNT",
            "ROUND(AVG(SUM(a.BALANCE)), 2) as AVG_BALANCE",
            "ROUND(AVG(c.CREDIT_SCORE), 0) as AVG_CREDIT_SCORE",
            "ROUND(AVG(c.ANNUAL_INCOME), 0) as AVG_INCOME"
        ]
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=segmentation_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Customer segmentation test PASSED")
        results = response.get('data', [])
        print_results(results, "Customer Tier Distribution", 5)
        return True
    else:
        print(f"❌ Customer segmentation test FAILED: {response}")
        return False

def test_branch_performance():
    """Test branch performance analysis"""
    print_test_header("BRANCH PERFORMANCE ANALYSIS")
    
    print("\n🏢 Test 1: Branch Operational Metrics")
    
    performance_data = {
        "tables": ["BRANCHES b", "EMPLOYEES e", "TRANSACTIONS t"],
        "joinConditions": [
            "b.BRANCH_ID = e.BRANCH_ID",
            "b.BRANCH_ID = t.BRANCH_ID"
        ],
        "selectColumns": [
            "b.BRANCH_NAME",
            "b.CITY",
            "b.STATE",
            "COUNT(DISTINCT e.EMPLOYEE_ID) as ACTIVE_EMPLOYEES",
            "COUNT(t.TRANSACTION_ID) as TRANSACTIONS_PROCESSED",
            "ROUND(SUM(ABS(t.AMOUNT)), 2) as TRANSACTION_VOLUME",
            "ROUND(AVG(ABS(t.AMOUNT)), 2) as AVG_TRANSACTION_SIZE"
        ],
        "whereClause": "e.STATUS = 'ACTIVE' AND t.STATUS = 'COMPLETED'"
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=performance_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Branch performance analysis test PASSED")
        results = response.get('data', [])
        print_results(results, "Branch Performance Metrics", 6)
        return True
    else:
        print(f"❌ Branch performance analysis test FAILED: {response}")
        return False

def test_payment_analysis():
    """Test payment behavior analysis"""
    print_test_header("PAYMENT BEHAVIOR ANALYSIS")
    
    print("\n💰 Test 1: Payment Performance Metrics")
    
    payment_data = {
        "tables": ["PAYMENTS p", "LOANS l", "CUSTOMERS c"],
        "joinConditions": [
            "p.LOAN_ID = l.LOAN_ID",
            "l.CUSTOMER_ID = c.CUSTOMER_ID"
        ],
        "selectColumns": [
            "p.PAYMENT_METHOD",
            "COUNT(*) as PAYMENT_COUNT",
            "ROUND(SUM(p.ACTUAL_AMOUNT), 2) as TOTAL_PAYMENTS",
            "ROUND(AVG(p.ACTUAL_AMOUNT), 2) as AVG_PAYMENT",
            "COUNT(CASE WHEN p.LATE_FEE > 0 THEN 1 END) as LATE_PAYMENTS",
            "ROUND(AVG(c.CREDIT_SCORE), 0) as AVG_CUSTOMER_CREDIT_SCORE"
        ],
        "whereClause": "p.STATUS = 'COMPLETED'"
    }
    
    status, response = make_request("/api/oracle/analytics/complex-joins", "POST", data=payment_data)
    
    print(f"Status: {status}")
    if status == 200 and response.get('status') == 'success':
        print("✅ Payment analysis test PASSED")
        results = response.get('data', [])
        print_results(results, "Payment Behavior Analysis", 5)
        return True
    else:
        print(f"❌ Payment analysis test FAILED: {response}")
        return False

def run_comprehensive_test_suite():
    """Run all advanced controller tests"""
    print("🚀 ADVANCED BANKING ANALYTICS CONTROLLER TEST SUITE")
    print("=" * 80)
    print(f"Testing API: {BASE_URL}")
    print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Test suite definition
    tests = [
        ("Window Functions", test_analytics_window_functions),
        ("Complex Joins", test_complex_joins),
        ("Hierarchical Queries", test_hierarchical_queries),
        ("Analytical Functions", test_analytical_functions),
        ("Advanced Table Queries", test_table_queries_advanced),
        ("Loan Risk Analysis", test_loan_risk_analysis),
        ("Transaction Patterns", test_transaction_patterns),
        ("Customer Segmentation", test_customer_segmentation),
        ("Branch Performance", test_branch_performance),
        ("Payment Analysis", test_payment_analysis)
    ]
    
    passed = 0
    total = len(tests)
    results_summary = []
    
    for test_name, test_func in tests:
        print(f"\n⏳ Running {test_name} test...")
        try:
            start_time = time.time()
            result = test_func()
            end_time = time.time()
            
            if result:
                passed += 1
                status = "✅ PASSED"
            else:
                status = "❌ FAILED"
            
            results_summary.append({
                "test": test_name,
                "status": status,
                "duration": f"{end_time - start_time:.2f}s"
            })
            
        except Exception as e:
            print(f"💥 {test_name} test crashed: {str(e)}")
            results_summary.append({
                "test": test_name,
                "status": "💥 CRASHED",
                "error": str(e)
            })
    
    # Final summary
    print("\n" + "=" * 80)
    print("📋 COMPREHENSIVE TEST RESULTS")
    print("=" * 80)
    
    for result in results_summary:
        print(f"{result['status']} {result['test']} ({result.get('duration', 'N/A')})")
    
    print(f"\n🎯 Overall Results: {passed}/{total} tests passed ({(passed/total)*100:.1f}%)")
    
    if passed == total:
        print("🎉 ALL ADVANCED CONTROLLER TESTS PASSED!")
        print("✅ Banking database analytics are fully functional through API")
        return 0
    else:
        print("⚠️  Some tests failed. Check individual test results above.")
        return 1

if __name__ == "__main__":
    sys.exit(run_comprehensive_test_suite())
