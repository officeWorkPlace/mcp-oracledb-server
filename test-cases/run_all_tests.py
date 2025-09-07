#!/usr/bin/env python3
"""
Master Test Runner
Executes all MCP Oracle Server test suites and provides comprehensive reporting
"""

import sys
import os
from datetime import datetime
import time

# Add current directory to path for imports
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from test_oracle_core_controller import TestOracleCoreController
from test_oracle_analytics_controller import TestOracleAnalyticsController

class MasterTestRunner:
    """Master test runner for all MCP Oracle Server tests"""
    
    def __init__(self):
        self.test_suites = []
        self.overall_results = []
    
    def add_test_suite(self, test_class, name):
        """Add a test suite to be executed"""
        self.test_suites.append((test_class, name))
    
    def run_all_tests(self):
        """Execute all test suites and provide comprehensive reporting"""
        print("🚀 MCP ORACLE SERVER - MASTER TEST SUITE")
        print("=" * 70)
        print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Number of test suites: {len(self.test_suites)}")
        print()
        
        total_passed = 0
        total_suites = len(self.test_suites)
        
        for i, (test_class, suite_name) in enumerate(self.test_suites, 1):
            print(f"📋 [{i}/{total_suites}] Executing {suite_name}")
            print("-" * 70)
            
            start_time = time.time()
            
            try:
                # Create test instance and run tests
                tester = test_class()
                success = tester.run_all_tests()
                
                end_time = time.time()
                duration = end_time - start_time
                
                if success:
                    total_passed += 1
                    status = "✅ PASSED"
                else:
                    status = "❌ FAILED"
                
                self.overall_results.append({
                    "suite": suite_name,
                    "status": status,
                    "success": success,
                    "duration": f"{duration:.2f}s"
                })
                
                print(f"\n{status} {suite_name} (Duration: {duration:.2f}s)")
                
            except Exception as e:
                end_time = time.time()
                duration = end_time - start_time
                
                print(f"💥 {suite_name} CRASHED: {str(e)}")
                self.overall_results.append({
                    "suite": suite_name,
                    "status": "💥 CRASHED",
                    "success": False,
                    "duration": f"{duration:.2f}s",
                    "error": str(e)
                })
            
            print("\n" + "=" * 70)
        
        # Final comprehensive report
        self.generate_final_report(total_passed, total_suites)
        
        return total_passed == total_suites
    
    def generate_final_report(self, passed, total):
        """Generate comprehensive final test report"""
        print("\n" + "🎯 COMPREHENSIVE TEST RESULTS" + "\n")
        print("=" * 70)
        
        # Individual suite results
        for result in self.overall_results:
            print(f"{result['status']} {result['suite']} ({result['duration']})")
            if 'error' in result:
                print(f"   Error: {result['error']}")
        
        print("\n" + "-" * 70)
        
        # Overall statistics
        success_rate = (passed / total) * 100
        print(f"📊 OVERALL STATISTICS:")
        print(f"   Total Test Suites: {total}")
        print(f"   Passed: {passed}")
        print(f"   Failed: {total - passed}")
        print(f"   Success Rate: {success_rate:.1f}%")
        
        # Test environment info
        print(f"\n🔧 TEST ENVIRONMENT:")
        print(f"   Python Version: {sys.version.split()[0]}")
        print(f"   Test Framework: Custom MCP Test Suite")
        print(f"   Completed at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Final verdict
        if passed == total:
            print(f"\n🎉 ALL {total} TEST SUITES PASSED!")
            print("✅ Banking database and MCP Oracle Server are fully functional!")
        else:
            print(f"\n⚠️  {total - passed} out of {total} test suites failed.")
            print("❌ Some functionality may need attention.")
        
        print("=" * 70)

def main():
    """Main test execution function"""
    runner = MasterTestRunner()
    
    # Add all test suites
    runner.add_test_suite(TestOracleCoreController, "Oracle Core Controller Tests")
    runner.add_test_suite(TestOracleAnalyticsController, "Oracle Analytics Controller Tests")
    
    # Execute all tests
    success = runner.run_all_tests()
    
    # Exit with appropriate code
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
