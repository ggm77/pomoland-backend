package com.seohamin.pomoland.global.auth.apple.util;

import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyDto;
import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class AppleKeyGenerator {

    // Apple client secret의 최대 유효기간(Apple 정책상 6개월 이하, 30일로 발급) 및 만료 전 갱신 여유
    private static final Duration CLIENT_SECRET_TTL = Duration.ofDays(30);
    private static final Duration CLIENT_SECRET_REFRESH_MARGIN = Duration.ofHours(1);

    @Value("${oauth2.apple.key_id}")
    private String kid;

    @Value("${oauth2.apple.team_id}")
    private String teamId;

    @Value("${oauth2.apple.auth_base_url}")
    private String baseUrl;

    @Value("${oauth2.apple.client_id}")
    private String clientId;

    @Value("${oauth2.apple.key_path}")
    private Resource keyPath;

    // 매 요청마다 디스크에서 다시 읽지 않도록 부팅 시 1회 로드해서 캐시
    private PrivateKey privateKey;

    private String cachedClientSecret;
    private Instant cachedClientSecretExpiresAt;

    /**
     * 애플 인증키(p8) 파일을 부팅 시 1회 읽어 캐시하는 메서드
     * 파일이 없거나 형식이 잘못됐으면 로그인 시점이 아니라 부팅 시점에 바로 실패시킨다
     */
    @PostConstruct
    private void init() {
        this.privateKey = loadPrivateKeyFromDisk();
    }

    /**
     * 애플 client secrete을 생성하는 메서드
     * 서명 결과는 만료 전까지 캐시해서 재사용한다 (p8 서명은 매 로그인마다 다시 할 필요가 없음)
     * @return JWT로 된 client secrete
     */
    public synchronized String generateClientSecrete(){
        final Instant now = Instant.now();

        if (
                cachedClientSecret != null
                && cachedClientSecretExpiresAt != null
                && now.isBefore(cachedClientSecretExpiresAt.minus(CLIENT_SECRET_REFRESH_MARGIN))
        ) {
            return cachedClientSecret;
        }

        final Instant expiresAt = now.plus(CLIENT_SECRET_TTL);

        final String clientSecret = Jwts.builder()
                .header()
                .add("kid", kid)
                .add("alg", "ES256")
                .and()
                .issuer(teamId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .audience()
                .add(baseUrl)
                .and()
                .subject(clientId)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();

        this.cachedClientSecret = clientSecret;
        this.cachedClientSecretExpiresAt = expiresAt;

        return clientSecret;
    }

    /**
     * 애플 공개키를 생성하는 메서드
     * @param tokenHeaders 프론트에서 받은 애플 idToken의 디코딩된 헤더
     * @param applePublicKeyResponseDto 애플한테 받은 공개 키들
     * @return 생성된 공개키
     */
    public PublicKey generatePublicKey(
            final Map<String, String> tokenHeaders,
            final ApplePublicKeyResponseDto applePublicKeyResponseDto
    ){

        // 1) 애플에서 받은 공개키 keys 중에서 클라이언트한테 받은 key중에 겹치는거 찾기
        final ApplePublicKeyDto publicKey = applePublicKeyResponseDto.getMatchedKey(
                tokenHeaders.get("kid"),
                tokenHeaders.get("alg")
        );

        // 2) n, e 디코딩
        return getPublicKey(publicKey);
    }

    /**
     * 지정된 위치에 저장된 애플 인증키 PEM 파일을 읽어와서
     * PrivateKey를 생성하는 메서드
     * @return 애플 PrivateKey
     */
    private PrivateKey loadPrivateKeyFromDisk(){
        try (final Reader reader = new InputStreamReader(keyPath.getInputStream())){

            PEMParser pemParser = new PEMParser(reader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo keyInfo = (PrivateKeyInfo) pemParser.readObject();

            return converter.getPrivateKey(keyInfo);

        } catch (IOException ex){
            throw new IllegalStateException("애플 인증키(p8) 로드 실패: " + keyPath, ex);
        }
    }

    /**
     * 공개 키를 작성하는 메서드
     * @param applePublicKeyDto 애플한테 받은 공개 키와 일치하는 키
     * @return 공개 키
     */
    private PublicKey getPublicKey(final ApplePublicKeyDto applePublicKeyDto){

        // 1) n, e Base64 디코딩
        final byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKeyDto.getN());
        final byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKeyDto.getE());

        // 2) BigInteger로 변환 및 RSA 공개 키 스펙 생성
        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                new BigInteger(1, nBytes),
                new BigInteger(1, eBytes)
        );

        // 3) 실제 PublicKey 생성
        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(applePublicKeyDto.getKty());

            return keyFactory.generatePublic(publicKeySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CustomException(ExceptionCode.APPLE_AUTH_ERROR);
        }
    }
}
