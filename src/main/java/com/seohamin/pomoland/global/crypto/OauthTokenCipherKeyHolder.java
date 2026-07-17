package com.seohamin.pomoland.global.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * OauthRefreshTokenConverter가 사용하는 AES 키를 보관하는 홀더
 * JPA AttributeConverter는 Hibernate가 자체적으로 인스턴스화할 수 있어
 * Spring 빈 주입을 확실히 보장하기 위해 static 필드로 키를 공유한다.
 */
@Component
public class OauthTokenCipherKeyHolder {

    static final String ALGORITHM = "AES";

    private static volatile SecretKeySpec secretKey;

    @Value("${oauth.token_encryption_key}")
    public void setEncryptionKey(final String base64Key) {
        secretKey = new SecretKeySpec(Base64.getDecoder().decode(base64Key), ALGORITHM);
    }

    static SecretKeySpec getSecretKey() {
        if (secretKey == null) {
            throw new IllegalStateException("OAuth 토큰 암호화 키가 초기화되지 않았습니다.");
        }
        return secretKey;
    }
}