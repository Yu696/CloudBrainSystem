package com.cloudbrain.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Test DataSource configuration.
 * Ensures Spring Boot auto-configures the DataSource from application.yml during tests.
 */
@Configuration
@EnableAutoConfiguration
public class TestDataSourceConfig {
}
