package com.deepai.mcpserver.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * CSRF Token Controller
 * Provides CSRF token for API requests that require CSRF protection
 */
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    /**
     * Get CSRF token for subsequent API requests
     * 
     * @param request HTTP request
     * @return CSRF token information
     */
    @GetMapping("/token")
    public Map<String, Object> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        Map<String, Object> response = new HashMap<>();
        if (csrfToken != null) {
            response.put("status", "success");
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
            response.put("message", "CSRF token generated successfully");
        } else {
            response.put("status", "error");
            response.put("message", "CSRF token not available");
        }
        
        response.put("timestamp", java.time.Instant.now());
        return response;
    }

    /**
     * Get CSRF token information (alternative endpoint)
     * 
     * @param request HTTP request
     * @return CSRF token details
     */
    @GetMapping("/info")
    public Map<String, Object> getCsrfInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        response.put("csrfEnabled", csrfToken != null);
        response.put("timestamp", java.time.Instant.now());
        
        if (csrfToken != null) {
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
            response.put("instructions", Map.of(
                "header", "Include " + csrfToken.getHeaderName() + " header with the token value",
                "parameter", "Include " + csrfToken.getParameterName() + " parameter with the token value",
                "usage", "Use either header or parameter method for CSRF protection"
            ));
        }
        
        return response;
    }
}
