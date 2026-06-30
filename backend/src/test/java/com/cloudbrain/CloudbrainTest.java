package com.cloudbrain;

import com.cloudbrain.config.TestDataSourceConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Custom test annotation that works around auto-configuration exclusions
 * affecting DataSource and Security during test execution.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@Import(TestDataSourceConfig.class)
@TestPropertySource(properties = "storage.type=local")
public @interface CloudbrainTest {
}
