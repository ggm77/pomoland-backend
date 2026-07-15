package com.seohamin.pomoland.domain.auth.token.controller;

import com.seohamin.pomoland.domain.auth.token.dto.TokenRefreshRequestDto;
import com.seohamin.pomoland.domain.auth.token.service.TokenService;
import com.seohamin.pomoland.global.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TokenController {

    private final TokenService tokenService;

    // 리프레시 토큰으로 jwt새로 발급 받는 API
    @PostMapping("/auth/token/refresh")
    public ResponseEntity<JwtDto> refreshToken(
        @RequestBody final TokenRefreshRequestDto tokenRefreshRequestDto
    ) {

        return ResponseEntity.ok(tokenService.refreshToken(tokenRefreshRequestDto));
    }
}
