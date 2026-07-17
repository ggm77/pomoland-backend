package com.seohamin.pomoland.global.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DB에 저장되는 OAuth 리프레시 토큰(UserOauth.refreshToken)을 AES-GCM으로 암복호화하는 컨버터
 * DB 유출 시 서드파티(Apple/Google) 토큰까지 함께 유출되는 것을 막기 위함
 */
@Converter
public class OauthRefreshTokenConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    @Override
    public String convertToDatabaseColumn(final String plainToken) {
        if (plainToken == null) {
            return null;
        }

        try {
            final byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, OauthTokenCipherKeyHolder.getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            final byte[] cipherText = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

            final ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv).put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("OAuth 리프레시 토큰 암호화 실패", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(final String encryptedToken) {
        if (encryptedToken == null) {
            return null;
        }

        try {
            final ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedToken));
            final byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            final byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, OauthTokenCipherKeyHolder.getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("OAuth 리프레시 토큰 복호화 실패", ex);
        }
    }
}