package com.seohamin.pomoland.global.auth.apple.dto.token;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AppleTokenRequestDto {
    private final String code;
    private final String client_id;
    private final String client_secret;
    private final String grant_type;

    @Builder
    public AppleTokenRequestDto(
            final String code,
            final String client_id,
            final String client_secret,
            final String grant_type
    ){
        this.code = code;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.grant_type = grant_type;
    }
}
