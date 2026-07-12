package com.seohamin.pomoland.global.auth.apple.client;

import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.seohamin.pomoland.global.auth.apple.dto.token.AppleTokenResponseDto;
import com.seohamin.pomoland.global.auth.apple.util.AppleKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleAuthClient {

    @Value("${oauth2.apple.public_key_uri}")
    private String APPLE_PUBLIC_KEY_URI;

    @Value("${oauth2.apple.token_uri}")
    private String APPLE_TOKEN_URI;

    @Value("${oauth2.apple.token_revoke}")
    private String APPLE_TOKEN_REVOKE_URI;

    @Value("${oauth2.apple.client_id}")
    private String APPLE_CLIENT_ID;

    @Value("${oauth2.apple.redirect_url}")
    private String APPLE_REDIRECT_URI;

    private final WebClient appleWebClient;
    private final AppleKeyGenerator appleKeyGenerator;

    // https://appleid.apple.com/auth/keys에 요청을 넣고 keys를 받아온다.
    public ApplePublicKeyResponseDto requestKeys() {
        return appleWebClient.get()
                .uri(APPLE_PUBLIC_KEY_URI)
                .retrieve()
                .bodyToMono(ApplePublicKeyResponseDto.class)
                .block();
    }

    // https://appleid.apple.com/auth/token에 요청을 넣고 토큰을 받아온다.
    public AppleTokenResponseDto requestToken(final String authorizationCode) {
        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", APPLE_CLIENT_ID);
        formData.add("client_secret", appleKeyGenerator.generateClientSecrete());
        formData.add("redirect_uri", APPLE_REDIRECT_URI);
        formData.add("grant_type", "authorization_code");

        return appleWebClient.post()
                .uri(APPLE_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            // Apple이 보내주는 {"error": "...", "error_description": "..."} 확인
                            log.error("Apple Token API Error: ", body);
                            return Mono.error(new RuntimeException("Apple Login Failed: " + body));
                        })
                )
                .bodyToMono(AppleTokenResponseDto.class)
                .block();
    }

    // https://appleid.apple.com/auth/revoke에 회원 탈퇴를 요청한다.
    public void revokeToken(final String appleRefreshToken) {
        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", APPLE_CLIENT_ID);
        formData.add("client_secret", appleKeyGenerator.generateClientSecrete());
        formData.add("token", appleRefreshToken);
        formData.add("token_type_hint", "refresh_token");

        appleWebClient.post()
                .uri(APPLE_TOKEN_REVOKE_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Apple Token Revoke API Error: ", body);
                            return Mono.error(new RuntimeException("Apple Token Revoke Failed: "+body));
                        })
                )
                .toBodilessEntity()
                .block();
    }
}
