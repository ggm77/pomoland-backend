package com.seohamin.pomoland.domain.auth.oauth2.controller;

import com.seohamin.pomoland.domain.auth.oauth2.dto.OauthRequestDto;
import com.seohamin.pomoland.domain.auth.oauth2.service.OauthService;
import com.seohamin.pomoland.global.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class OauthController {

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
}
