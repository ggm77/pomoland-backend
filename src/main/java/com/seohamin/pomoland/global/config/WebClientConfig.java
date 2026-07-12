package com.seohamin.pomoland.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${oauth2.apple.auth_base_url}")
    private String APPLE_AUTH_BASE_URL;

    @Bean
    public WebClient appleWebClient() {
        return WebClient.builder()
                .baseUrl(APPLE_AUTH_BASE_URL)
                .build();
    }
}
