package com.cloudbrain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cloudbrain.mapper")
public class CloudBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBrainApplication.class, args);
    }
}
