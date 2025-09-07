#!/usr/bin/env python3
"""
Oracle Core Controller Tests
Tests basic database operations, table management, and core functionality
"""

from test_base import TestBase

class TestOracleCoreController(TestBase):
    """Test Oracle Core Controller endpoints"""
    
    def __init__(self):
        super().__init__()
        self.test_results = []
    
    def test_health_check(self):
        """Test core service health check"""
        self.print_test_header("CORE SERVICE HEALTH CHECK")
        
        status, response = self.make_request("/api/oracle/core/health")
        # Health check has different response format
        if status == 200:
            print("✅ Core Health Check - SUCCESS")
            result = True
        else:
            print(f"❌ Core Health Check - FAILED: Status {status}, Response: {response}")
            result = False
            
        self.test_results.append(("Health Check", result))
        
        if result:
            self.print_results(response, "Health Check Response")
        
        return result
    
    def test_list_tables(self):
        """Test listing database tables"""
        self.print_test_header("LIST TABLES")
        
        status, response = self.make_request("/api/oracle/core/tables", params={"schemaName": "C##DEEPAI"})
        if status == 200:
            print("✅ List Tables - SUCCESS")
            result = True
            tables = response.get('tables', [])
            print(f"Found {len(tables)} tables")
            self.print_results(tables, "Database Tables", 10)
        else:
            print(f"❌ List Tables - FAILED: Status {status}, Response: {response}")
            result = False
            
        self.test_results.append(("List Tables", result))
        
        return result
    
    def test_query_customers(self):
        """Test querying customer data"""
        self.print_test_header("QUERY CUSTOMERS TABLE")
        
        status, response = self.make_request(
            "/api/oracle/core/tables/CUSTOMERS/query",
            "POST",
            data=["CUSTOMER_ID", "FIRST_NAME", "LAST_NAME", "STATE", "CREDIT_SCORE"],
            params={"limit": 10, "orderBy": "CREDIT_SCORE DESC"}
        )
        
        result = self.assert_success(status, response, "Query Customers")
        self.test_results.append(("Query Customers", result))
        
        if result:
            customers = response.get('results', [])
            print(f"Retrieved {len(customers)} customer records")
            self.print_results(customers, "Top Customers by Credit Score", 5)
        
        return result
    
    def test_describe_table(self):
        """Test describing table structure"""
        self.print_test_header("DESCRIBE TABLE STRUCTURE")
        
        status, response = self.make_request("/api/oracle/core/tables/CUSTOMERS/describe")
        result = self.assert_success(status, response, "Describe Table")
        self.test_results.append(("Describe Table", result))
        
        if result:
            columns = response.get('columns', [])
            print(f"CUSTOMERS table has {len(columns)} columns")
            self.print_results(columns, "Table Structure", 8)
        
        return result
    
    def test_account_query_with_filter(self):
        """Test querying accounts with filters"""
        self.print_test_header("FILTERED ACCOUNT QUERY")
        
        status, response = self.make_request(
            "/api/oracle/core/tables/ACCOUNTS/query",
            "POST",
            data=["ACCOUNT_ID", "CUSTOMER_ID", "ACCOUNT_TYPE", "BALANCE"],
            params={
                "whereClause": "BALANCE > 50000",
                "orderBy": "BALANCE DESC",
                "limit": 15
            }
        )
        
        result = self.assert_success(status, response, "Filtered Account Query")
        self.test_results.append(("Filtered Account Query", result))
        
        if result:
            accounts = response.get('results', [])
            print(f"Found {len(accounts)} high-balance accounts")
            self.print_results(accounts, "High-Balance Accounts", 5)
        
        return result
    
    def test_loan_data_query(self):
        """Test querying loan data"""
        self.print_test_header("LOAN DATA QUERY")
        
        status, response = self.make_request(
            "/api/oracle/core/tables/LOANS/query",
            "POST",
            data=["LOAN_ID", "CUSTOMER_ID", "PRINCIPAL_AMOUNT", "INTEREST_RATE", "STATUS"],
            params={"whereClause": "STATUS = 'ACTIVE'", "limit": 20}
        )
        
        result = self.assert_success(status, response, "Loan Data Query")
        self.test_results.append(("Loan Data Query", result))
        
        if result:
            loans = response.get('results', [])
            print(f"Found {len(loans)} active loans")
            self.print_results(loans, "Active Loans", 3)
        
        return result
    
    def run_all_tests(self):
        """Run all core controller tests"""
        print("🚀 ORACLE CORE CONTROLLER TEST SUITE")
        print("=" * 60)
        print(f"Testing API: {self.base_url}")
        print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Wait for service to be available
        if not self.wait_for_service():
            print("❌ Service not available, skipping tests")
            return False
        
        # Run tests
        tests = [
            self.test_health_check,
            self.test_list_tables,
            self.test_query_customers,
            self.test_describe_table,
            self.test_account_query_with_filter,
            self.test_loan_data_query
        ]
        
        passed = 0
        for test in tests:
            try:
                if test():
                    passed += 1
            except Exception as e:
                print(f"💥 Test {test.__name__} crashed: {str(e)}")
                self.test_results.append((test.__name__, False))
        
        # Summary
        total = len(tests)
        print(f"\n📋 CORE CONTROLLER TEST RESULTS")
        print("=" * 60)
        for test_name, result in self.test_results:
            status = "✅ PASSED" if result else "❌ FAILED"
            print(f"{status} {test_name}")
        
        print(f"\n🎯 Overall: {passed}/{total} tests passed ({(passed/total)*100:.1f}%)")
        
        return passed == total

if __name__ == "__main__":
    from datetime import datetime
    tester = TestOracleCoreController()
    success = tester.run_all_tests()
    exit(0 if success else 1)
