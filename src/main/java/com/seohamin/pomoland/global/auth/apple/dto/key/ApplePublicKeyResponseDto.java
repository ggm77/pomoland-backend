package com.seohamin.pomoland.global.auth.apple.dto.key;

import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.Getter;

import java.util.List;

@Getter
public class ApplePublicKeyResponseDto {

    private final List<ApplePublicKeyDto> keys;

    public ApplePublicKeyResponseDto(List<ApplePublicKeyDto> keys) {
        this.keys = keys;
    }

    public ApplePublicKeyDto getMatchedKey(
            final String kid,
            final String alg
    ) {
        return keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findAny()
                .orElseThrow(() -> new CustomException(ExceptionCode.APPLE_AUTH_ERROR));
    }
}
