package com.deepai.mcpserver.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.deepai.mcpserver.service.OracleAIService;
import com.deepai.mcpserver.service.OracleServiceClient;
import com.deepai.mcpserver.util.OracleFeatureDetector;

@TestConfiguration
@Configuration
public class TestConfig implements WebMvcConfigurer {

    @Bean
    @Primary
    public OracleServiceClient oracleServiceClient() {
        return Mockito.mock(OracleServiceClient.class);
    }

    @Bean
    @Primary
    public OracleAIService oracleAIService() {
        return Mockito.mock(OracleAIService.class);
    }

    @Bean
    @Primary
    public OracleFeatureDetector oracleFeatureDetector() {
        return Mockito.mock(OracleFeatureDetector.class);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }
}