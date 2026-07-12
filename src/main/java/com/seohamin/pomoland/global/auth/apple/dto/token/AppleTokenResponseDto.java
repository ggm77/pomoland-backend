package com.seohamin.pomoland.global.auth.apple.dto.token;

import lombok.Getter;

@Getter
public class AppleTokenResponseDto {
    private String access_token;
    private String expires_in;
    private String id_token;
    private String refresh_token;
    private String token_type;
    private String error;
}
