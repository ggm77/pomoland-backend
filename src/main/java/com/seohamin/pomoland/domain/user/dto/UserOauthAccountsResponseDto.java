package com.seohamin.pomoland.domain.user.dto;

import com.seohamin.pomoland.domain.user.entity.UserOauth;
import com.seohamin.pomoland.global.constant.Role;

public record UserOauthAccountsResponseDto(
        Long id,
        Long userId,
        Role userRole,
        String provider,
        String providerUserId,
        String email,
        String name,
        String profileImage
) {

    public static UserOauthAccountsResponseDto of(final UserOauth userOauth) {
        return new UserOauthAccountsResponseDto(
                userOauth.getId(),
                userOauth.getMember().getId(),
                userOauth.getMember().getRole(),
                userOauth.getProvider(),
                userOauth.getProviderUserId(),
                userOauth.getEmail(),
                userOauth.getName(),
                userOauth.getProfileImage()
        );
    }
}
