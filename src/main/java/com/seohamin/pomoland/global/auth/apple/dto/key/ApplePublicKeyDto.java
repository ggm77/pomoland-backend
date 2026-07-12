package com.seohamin.pomoland.global.auth.apple.dto.key;

import lombok.Getter;

@Getter
public class ApplePublicKeyDto {
    private String kty;
    private String kid;
    private String alg;
    private String n;
    private String e;
}
