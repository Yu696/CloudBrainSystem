package com.cloudbrain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("云脑诊疗平台 API")
                        .version("1.0.0")
                        .description("云脑诊疗平台 — 阶段一单体架构接口文档")
                        .contact(new Contact()
                                .name("第31组")
                                .url("https://github.com/Yu696/CloudBrainSystem")));
    }
}
