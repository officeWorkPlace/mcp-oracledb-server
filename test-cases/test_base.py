#!/usr/bin/env python3
"""
Base Test Infrastructure for MCP Oracle Server
Provides common functionality for all test modules
"""

import requests
import json
import time
from datetime import datetime
import sys
import os

# Configuration
BASE_URL = "http://localhost:8080"
USERNAME = "admin"
PASSWORD = "admin"

class TestBase:
    """Base class for all MCP Oracle Server tests"""
    
    def __init__(self):
        self.base_url = BASE_URL
        self.username = USERNAME
        self.password = PASSWORD
        self.session = requests.Session()
        self.session.auth = (self.username, self.password)
        self.session.headers.update({'Content-Type': 'application/json'})
    
    def make_request(self, endpoint, method="GET", data=None, params=None, headers=None):
        """Make HTTP request with authentication"""
        try:
            # Add any custom headers to the session headers
            request_headers = self.session.headers.copy()
            if headers:
                request_headers.update(headers)
            
            if method.upper() == "GET":
                response = self.session.get(f"{self.base_url}{endpoint}", params=params, headers=request_headers)
            elif method.upper() == "POST":
                response = self.session.post(f"{self.base_url}{endpoint}", json=data, params=params, headers=request_headers)
            elif method.upper() == "PUT":
                response = self.session.put(f"{self.base_url}{endpoint}", json=data, params=params, headers=request_headers)
            elif method.upper() == "DELETE":
                response = self.session.delete(f"{self.base_url}{endpoint}", params=params, headers=request_headers)
            else:
                raise ValueError(f"Unsupported method: {method}")
            
            return response.status_code, response.json() if response.content else {}
        except Exception as e:
            return 500, {"error": str(e)}
    
    def assert_success(self, status_code, response, test_name):
        """Assert that a test was successful"""
        if status_code == 200 and response.get('status') == 'success':
            print(f"✅ {test_name} - PASSED")
            return True
        else:
            print(f"❌ {test_name} - FAILED: Status {status_code}, Response: {response}")
            return False
    
    def print_test_header(self, test_name):
        """Print formatted test header"""
        print(f"\n{'='*60}")
        print(f"🧪 {test_name}")
        print(f"{'='*60}")
    
    def print_results(self, results, title, max_items=5):
        """Print formatted results"""
        print(f"\n📊 {title}")
        print("-" * 40)
        if isinstance(results, list) and results:
            for i, item in enumerate(results[:max_items]):
                print(f"{i+1:2d}. {json.dumps(item, indent=2, default=str)}")
            if len(results) > max_items:
                print(f"... and {len(results) - max_items} more items")
        elif isinstance(results, dict):
            print(json.dumps(results, indent=2, default=str))
        else:
            print(f"Results: {results}")
    
    def wait_for_service(self, max_attempts=30):
        """Wait for the service to be available"""
        for attempt in range(max_attempts):
            try:
                status, response = self.make_request("/actuator/health")
                if status == 200:
                    print("✅ Service is available")
                    return True
            except:
                pass
            
            if attempt < max_attempts - 1:
                print(f"⏳ Waiting for service... (attempt {attempt + 1}/{max_attempts})")
                time.sleep(2)
        
        print("❌ Service is not available after waiting")
        return False
