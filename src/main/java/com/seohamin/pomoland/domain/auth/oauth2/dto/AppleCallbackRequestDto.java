package com.seohamin.pomoland.domain.auth.oauth2.dto;

public record AppleCallbackRequestDto(
        String code,
        String state,
        String user,
        String error
) {
}
