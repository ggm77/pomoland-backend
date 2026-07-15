package com.seohamin.pomoland.domain.auth.oauth2.controller;

import com.seohamin.pomoland.domain.auth.oauth2.dto.AppleCallbackRequestDto;
import com.seohamin.pomoland.domain.auth.oauth2.dto.OauthRequestDto;
import com.seohamin.pomoland.domain.auth.oauth2.service.OauthService;
import com.seohamin.pomoland.global.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class OauthController {

    @Value("${oauth2.apple.front_redirect_url}")
    private String APPLE_FRONT_REDIRECT_URL;

    private final OauthService oauthService;

    // 애플 OAuth API
    @PostMapping("/oauth2/apple")
    public ResponseEntity<JwtDto> appleOauth2(
            @RequestBody final OauthRequestDto oauthRequestDto
    ) {

        return ResponseEntity.ok(oauthService.processAppleOauth(oauthRequestDto));
    }

    // 구글 OAuth API
    @PostMapping("/oauth2/google")
    public ResponseEntity<JwtDto> googleOauth2(
            @RequestBody final OauthRequestDto oauthRequestDto
    ) {

        return ResponseEntity.ok(oauthService.processGoogleOauth(oauthRequestDto));
    }

    // 애플 auth code 파싱용 API
    @PostMapping("/oauth2/callback/apple")
    public ResponseEntity<Void> appleOauth2Callback(
            final AppleCallbackRequestDto appleCallbackRequestDto
    ) {

        final UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(APPLE_FRONT_REDIRECT_URL);

        builder.queryParam("code", Optional.ofNullable(appleCallbackRequestDto.code()).orElse(""));
        builder.queryParam("state", Optional.ofNullable(appleCallbackRequestDto.state()).orElse(""));
        builder.queryParam("user", Optional.ofNullable(appleCallbackRequestDto.user()).orElse(""));
        builder.queryParam("error", Optional.ofNullable(appleCallbackRequestDto.error()).orElse(""));

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(builder.build().toUri())
                .build();
    }
}
