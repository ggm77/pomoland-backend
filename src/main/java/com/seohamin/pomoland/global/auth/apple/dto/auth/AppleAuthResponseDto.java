package com.seohamin.pomoland.global.auth.apple.dto.auth;

import com.seohamin.pomoland.global.constant.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AppleAuthResponseDto {
    private final Long userId;
    private final String accessToken;
    private final String refreshToken;
    private final Role role;

    @Builder
    public AppleAuthResponseDto(
            final Long userId,
            final String accessToken,
            final String refreshToken,
            final Role role
    ){
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }
}
