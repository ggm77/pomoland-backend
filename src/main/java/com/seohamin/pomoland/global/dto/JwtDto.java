package com.seohamin.pomoland.global.dto;

public record JwtDto(
        String accessToken,
        String tokenType,
        Long exprTime,
        String refreshToken
) { }
