package com.cloudbrain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /** AI 服务商 */
    private String provider = "deepseek";

    /** API Key（从环境变量读取，不硬编码） */
    private String apiKey;

    /** AI API 地址 */
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 读取超时（毫秒） */
    private int timeout = 30000;

    /** 连接超时（毫秒） */
    private int connectTimeout = 3000;

    /** 降级配置 */
    private Fallback fallback = new Fallback();

    /** 重试配置 */
    private Retry retry = new Retry();

    /** 限流配置 */
    private RateLimit rateLimit = new RateLimit();

    /** Mock 模式配置 */
    private Mock mock = new Mock();

    /** 视觉 AI 配置（豆包/Doubao，用于影像分析） */
    private VisionConfig vision = new VisionConfig();

    @Data
    public static class Fallback {
        private boolean enabled = true;
    }

    @Data
    public static class Retry {
        private int maxAttempts = 2;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int maxPerMinute = 10;
        private int maxPerDay = 100;
    }

    @Data
    public static class Mock {
        private boolean enabled = false;
    }

    @Data
    public static class VisionConfig {
        private String apiKey;
        private String apiUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        private String model = "ep-20260629234720-b6dtg";
        private int timeout = 60000;
    }

    /** AI 调用专用 RestTemplate，独立超时配置 */
    @Bean
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    /** 视觉 AI 调用专用 RestTemplate（更长超时） */
    @Bean
    public RestTemplate visionRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(vision.getTimeout());
        return new RestTemplate(factory);
    }

    /** Mock 模式是否生效（配置开启 或 API Key 为空） */
    public boolean isMockEnabled() {
        return mock.isEnabled() || apiKey == null || apiKey.isBlank();
    }

    /** 视觉 AI 是否已配置（配置了 API Key 或改用其他 API 地址） */
    public boolean isVisionEnabled() {
        // 防止 Spring 未解析的占位符被误认为有效 key
        String key = vision.getApiKey();
        boolean hasValidKey = key != null && !key.isBlank() && !key.contains("${");
        return hasValidKey
                || (vision.getApiUrl() != null
                    && !vision.getApiUrl().equals("https://ark.cn-beijing.volces.com/api/v3/chat/completions"));
    }
}
