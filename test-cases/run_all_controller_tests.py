#!/usr/bin/env python3
"""
MCP Oracle Server - Comprehensive Controller Test Runner
Runs all controller test suites and provides comprehensive reporting
"""

import subprocess
import sys
import os
from datetime import datetime
import json

class ComprehensiveTestRunner:
    """Runs all controller test suites with comprehensive reporting"""
    
    def __init__(self):
        self.test_scripts = [
            ("CSRF Controller", "test_csrf_controller.py"),
            ("Oracle Service Controller", "test_oracle_core_controller.py"),
            ("Oracle Analytics Controller", "test_oracle_analytics_controller.py"),
            ("Oracle AI Controller", "test_oracle_ai_controller.py"),
            ("Oracle Performance Controller", "test_oracle_performance_controller.py"),
            ("Oracle Privilege Controller", "test_oracle_privilege_controller.py"),
            ("Oracle Security Controller", "test_oracle_security_controller.py")
        ]
        self.results = []
        self.start_time = datetime.now()
    
    def print_header(self):
        """Print test suite header"""
        print("🚀 MCP ORACLE SERVER - COMPREHENSIVE CONTROLLER TESTS")
        print("=" * 70)
        print(f"Started at: {self.start_time.strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Testing {len(self.test_scripts)} controller suites")
        print("=" * 70)
    
    def run_test_script(self, name, script_path):
        """Run a single test script and capture results"""
        print(f"\n🔍 RUNNING: {name}")
        print("-" * 50)
        
        try:
            # Run the test script
            result = subprocess.run([
                sys.executable, script_path
            ], capture_output=True, text=True, timeout=300, encoding='utf-8')
            
            success = result.returncode == 0
            output = result.stdout
            errors = result.stderr
            
            # Parse output for test counts if available
            test_count = 0
            passed_count = 0
            
            for line in output.split('\n'):
                if "tests passed" in line.lower():
                    try:
                        # Extract numbers like "8/10 tests passed"
                        parts = line.split()
                        for i, part in enumerate(parts):
                            if "/" in part:
                                passed_count = int(part.split('/')[0])
                                test_count = int(part.split('/')[1])
                                break
                    except:
                        pass
            
            self.results.append({
                'name': name,
                'script': script_path,
                'success': success,
                'test_count': test_count,
                'passed_count': passed_count,
                'output': output,
                'errors': errors,
                'return_code': result.returncode
            })
            
            # Print summary for this test
            if success:
                if test_count > 0:
                    print(f"✅ {name}: PASSED ({passed_count}/{test_count} tests)")
                else:
                    print(f"✅ {name}: PASSED")
            else:
                if test_count > 0:
                    print(f"❌ {name}: FAILED ({passed_count}/{test_count} tests)")
                else:
                    print(f"❌ {name}: FAILED")
                
                # Show first few lines of error for quick diagnosis
                if errors:
                    print(f"   Error: {errors.split(chr(10))[0]}")
            
        except subprocess.TimeoutExpired:
            print(f"⏱️ {name}: TIMEOUT (>5 minutes)")
            self.results.append({
                'name': name,
                'script': script_path,
                'success': False,
                'test_count': 0,
                'passed_count': 0,
                'output': '',
                'errors': 'Test timed out after 5 minutes',
                'return_code': -1
            })
            
        except Exception as e:
            print(f"💥 {name}: CRASHED - {str(e)}")
            self.results.append({
                'name': name,
                'script': script_path,
                'success': False,
                'test_count': 0,
                'passed_count': 0,
                'output': '',
                'errors': str(e),
                'return_code': -2
            })
    
    def print_detailed_summary(self):
        """Print detailed test results summary"""
        end_time = datetime.now()
        duration = end_time - self.start_time
        
        print(f"\n📊 COMPREHENSIVE TEST RESULTS SUMMARY")
        print("=" * 70)
        print(f"Total Duration: {duration}")
        print(f"Completed at: {end_time.strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Overall statistics
        total_suites = len(self.results)
        passed_suites = sum(1 for r in self.results if r['success'])
        total_tests = sum(r['test_count'] for r in self.results)
        total_passed_tests = sum(r['passed_count'] for r in self.results)
        
        print(f"\n🎯 OVERALL STATISTICS:")
        print(f"   Controller Suites: {passed_suites}/{total_suites} passed ({(passed_suites/total_suites)*100:.1f}%)")
        if total_tests > 0:
            print(f"   Individual Tests: {total_passed_tests}/{total_tests} passed ({(total_passed_tests/total_tests)*100:.1f}%)")
        
        # Detailed results by controller
        print(f"\n📋 DETAILED RESULTS BY CONTROLLER:")
        print("-" * 70)
        
        for result in self.results:
            status = "✅ PASSED" if result['success'] else "❌ FAILED"
            test_info = ""
            if result['test_count'] > 0:
                test_info = f" ({result['passed_count']}/{result['test_count']} tests)"
            
            print(f"{status:12} {result['name']:35} {test_info}")
        
        # Failed tests details
        failed_results = [r for r in self.results if not r['success']]
        if failed_results:
            print(f"\n❌ FAILED CONTROLLER DETAILS:")
            print("-" * 70)
            
            for result in failed_results:
                print(f"\n🔍 {result['name']}:")
                print(f"   Script: {result['script']}")
                print(f"   Return Code: {result['return_code']}")
                if result['errors']:
                    print(f"   Error: {result['errors'][:200]}...")
                if result['test_count'] > 0:
                    failed_tests = result['test_count'] - result['passed_count']
                    print(f"   Failed Tests: {failed_tests}/{result['test_count']}")
        
        # Success summary
        if passed_suites == total_suites:
            print(f"\n🎉 ALL CONTROLLER SUITES PASSED! 🎉")
        else:
            print(f"\n⚠️  {total_suites - passed_suites} controller suite(s) failed")
    
    def save_results_json(self):
        """Save detailed results to JSON file"""
        results_file = "controller_test_results.json"
        
        json_results = {
            'timestamp': self.start_time.isoformat(),
            'duration_seconds': (datetime.now() - self.start_time).total_seconds(),
            'summary': {
                'total_suites': len(self.results),
                'passed_suites': sum(1 for r in self.results if r['success']),
                'total_tests': sum(r['test_count'] for r in self.results),
                'passed_tests': sum(r['passed_count'] for r in self.results)
            },
            'detailed_results': self.results
        }
        
        try:
            with open(results_file, 'w') as f:
                json.dump(json_results, f, indent=2)
            print(f"\n💾 Detailed results saved to: {results_file}")
        except Exception as e:
            print(f"\n⚠️  Could not save results to JSON: {e}")
    
    def run_all_tests(self):
        """Run all controller test suites"""
        self.print_header()
        
        # Check if we're in the right directory
        if not os.path.exists("test_base.py"):
            print("❌ Error: test_base.py not found. Make sure you're in the test-cases directory.")
            return False
        
        # Run each test script
        for name, script in self.test_scripts:
            if os.path.exists(script):
                self.run_test_script(name, script)
            else:
                print(f"⚠️  SKIPPED: {name} - {script} not found")
                self.results.append({
                    'name': name,
                    'script': script,
                    'success': False,
                    'test_count': 0,
                    'passed_count': 0,
                    'output': '',
                    'errors': 'Test script not found',
                    'return_code': -3
                })
        
        # Print comprehensive summary
        self.print_detailed_summary()
        
        # Save results to JSON
        self.save_results_json()
        
        # Return overall success
        return all(r['success'] for r in self.results)

if __name__ == "__main__":
    runner = ComprehensiveTestRunner()
    success = runner.run_all_tests()
    
    if success:
        print("\n🎉 All controller tests completed successfully!")
        sys.exit(0)
    else:
        print("\n❌ Some controller tests failed.")
        sys.exit(1)
