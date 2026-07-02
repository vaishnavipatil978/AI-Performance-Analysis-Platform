package com.platform.analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    @Bean
    public RestClient openRouterRestClient(
            @Value("${spring.ai.openai.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
