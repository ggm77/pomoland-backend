package com.seohamin.pomoland.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // 커넥션 연결 타임아웃
    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    // 응답 수신 타임아웃 (Apple 엔드포인트가 응답하지 않을 때 요청 스레드가 무한정 블로킹되는 것을 방지)
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    @Value("${oauth2.apple.auth_base_url}")
    private String APPLE_AUTH_BASE_URL;

    @Bean
    public WebClient appleWebClient() {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(RESPONSE_TIMEOUT)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(RESPONSE_TIMEOUT.toSeconds(), java.util.concurrent.TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(RESPONSE_TIMEOUT.toSeconds(), java.util.concurrent.TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(APPLE_AUTH_BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
