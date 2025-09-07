#!/usr/bin/env python3
"""
CSRF Controller Tests  
Tests CSRF token generation and information endpoints
"""

from test_base import TestBase
from datetime import datetime

class TestCsrfController(TestBase):
    """Test CSRF Controller endpoints"""
    
    def __init__(self):
        super().__init__()
        self.test_results = []
        self.csrf_token = None
    
    def test_csrf_info_endpoint(self):
        """Test CSRF info endpoint"""
        self.print_test_header("CSRF INFO ENDPOINT")
        
        status, response = self.make_request("/api/csrf/info")
        # CSRF endpoints don't follow the standard success format
        if status == 200:
            print("✅ CSRF Info - SUCCESS")
            result = True
        else:
            print(f"❌ CSRF Info - FAILED: Status {status}, Response: {response}")
            result = False
        
        self.test_results.append(("CSRF Info", result))
        
        if result:
            self.print_results(response, "CSRF Service Info")
        
        return result
    
    def test_get_csrf_token(self):
        """Test CSRF token generation"""
        self.print_test_header("GET CSRF TOKEN")
        
        status, response = self.make_request("/api/csrf/token")
        # CSRF endpoints don't follow the standard success format
        if status == 200:
            print("✅ Get CSRF Token - SUCCESS")
            result = True
            
            # Try to extract token if available
            if isinstance(response, dict) and response.get('token'):
                self.csrf_token = response.get('token')
                print(f"🔑 CSRF Token acquired: {self.csrf_token[:20]}...")
        else:
            print(f"❌ Get CSRF Token - FAILED: Status {status}, Response: {response}")
            result = False
        
        self.test_results.append(("Get CSRF Token", result))
        
        if result:
            self.print_results(response, "CSRF Token Information")
        
        return result
    
    def run_all_tests(self):
        """Run all CSRF controller tests"""
        print("🚀 CSRF CONTROLLER TEST SUITE")
        print("=" * 60)
        print(f"Testing API: {self.base_url}")
        print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Wait for service to be available
        if not self.wait_for_service():
            print("❌ Service not available, skipping tests")
            return False
        
        # Run tests - only the actual endpoints that exist
        tests = [
            self.test_csrf_info_endpoint,
            self.test_get_csrf_token
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
        print(f"\n📋 CSRF CONTROLLER TEST RESULTS")
        print("=" * 60)
        for test_name, result in self.test_results:
            status = "✅ PASSED" if result else "❌ FAILED"
            print(f"{status} {test_name}")
        
        print(f"\n🎯 Overall: {passed}/{total} tests passed ({(passed/total)*100:.1f}%)")
        
        return passed == total

if __name__ == "__main__":
    tester = TestCsrfController()
    success = tester.run_all_tests()
    exit(0 if success else 1)
