package com.cloudbrain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {

    private String type = "local";

    private String basePath = "./data/cloudbrain/images";

    private Minio minio = new Minio();

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket = "cloudbrain-images";
    }
}
