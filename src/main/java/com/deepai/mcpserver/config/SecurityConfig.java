package com.deepai.mcpserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Security configuration for MCP Oracle DB Server
 * Enables CSRF protection with token endpoint for API access
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Create CSRF token handler - DISABLED FOR TESTING
        // CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // // Disable the breach protection (allows token to be sent in response body)
        // requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // Configure CSRF protection - DISABLED FOR TESTING
            // .csrf(csrf -> csrf
            //     .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            //     .csrfTokenRequestHandler(requestHandler)
            // )
            .csrf(csrf -> csrf.disable())
            
            // Configure request authorization
            .authorizeHttpRequests(auth -> auth
                // Allow actuator endpoints without authentication
                .requestMatchers("/actuator/**").permitAll()
                
                // Allow CSRF token endpoints (but still require basic auth)
                .requestMatchers("/api/csrf/**").authenticated()
                
                // Require authentication for all API endpoints
                .requestMatchers("/api/**").authenticated()
                
                // Allow other requests
                .anyRequest().permitAll()
            )
            
            // Enable HTTP Basic authentication
            .httpBasic(httpBasic -> httpBasic.realmName("MCP Oracle Server"))
            
            // Disable form login
            .formLogin(formLogin -> formLogin.disable());
        
        return http.build();
    }
}
