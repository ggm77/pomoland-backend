package com.seohamin.pomoland.domain.auth.oauth2.dto;

public record OauthRequestDto(
        String code,
        String name
) { }
