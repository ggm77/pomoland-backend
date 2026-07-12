package com.seohamin.pomoland.domain.user.dto;

public record UserOauthAccountsRequestDto(
        String provider,
        String providerUserId,
        String email,
        String name,
        String profileImage,
        String refreshToken
) { }
