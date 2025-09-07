#!/usr/bin/env python3
"""
Quick test summary - Run all controller tests and show results
"""

import subprocess
import sys
from datetime import datetime

def run_test(script_name, controller_name):
    """Run a single test script and capture results"""
    print(f"🔍 Testing {controller_name}...")
    try:
        result = subprocess.run([sys.executable, script_name], 
                              capture_output=True, text=True, timeout=60)
        
        # Extract test results from output
        output = result.stdout
        if "Overall:" in output:
            # Find the overall results line
            for line in output.split('\n'):
                if "Overall:" in line and "tests passed" in line:
                    return line.strip(), result.returncode == 0
        
        return "No results found", result.returncode == 0
        
    except subprocess.TimeoutExpired:
        return "TIMEOUT", False
    except Exception as e:
        return f"ERROR: {str(e)}", False

def main():
    print("🚀 MCP ORACLE SERVER - QUICK CONTROLLER TEST SUMMARY")
    print("=" * 70)
    print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # List of test scripts
    tests = [
        ("test_csrf_controller.py", "CSRF Controller"),
        ("test_oracle_core_controller.py", "Oracle Core Service"),
        ("test_oracle_analytics_controller.py", "Oracle Analytics"),
        ("test_oracle_ai_controller.py", "Oracle AI Services"),
        ("test_oracle_performance_controller.py", "Oracle Performance"),
        ("test_oracle_privilege_controller.py", "Oracle Privilege"),
        ("test_oracle_security_controller.py", "Oracle Security")
    ]
    
    results = []
    total_passed = 0
    
    for script, name in tests:
        result_text, success = run_test(script, name)
        results.append((name, result_text, success))
        if success:
            total_passed += 1
    
    # Print summary
    print("\n📊 COMPREHENSIVE TEST RESULTS")
    print("=" * 70)
    
    for name, result, success in results:
        status = "✅" if success else "❌"
        print(f"{status} {name:30} {result}")
    
    print(f"\n🎯 FINAL SUMMARY: {total_passed}/{len(tests)} controller test suites passed")
    
    if total_passed == len(tests):
        print("🎉 ALL CONTROLLER TESTS PASSED!")
        return True
    else:
        print(f"⚠️  {len(tests) - total_passed} controller test(s) have issues")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
