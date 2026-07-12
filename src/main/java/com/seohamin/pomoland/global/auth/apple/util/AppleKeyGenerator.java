package com.seohamin.pomoland.global.auth.apple.util;

import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyDto;
import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Jwts;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class AppleKeyGenerator {

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

    /**
     * 애플 client secrete을 생성하는 메서드
     * @return JWT로 된 client secrete
     */
    public String generateClientSecrete(){
        final Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .header()
                .add("kid", kid)
                .add("alg", "ES256")
                .and()
                .issuer(teamId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .audience()
                .add(baseUrl)
                .and()
                .subject(clientId)
                .signWith(getPrivateKey(), Jwts.SIG.ES256)
                .compact();
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
    private PrivateKey getPrivateKey(){
        try (final Reader reader = new InputStreamReader(keyPath.getInputStream())){

            PEMParser pemParser = new PEMParser(reader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo keyInfo = (PrivateKeyInfo) pemParser.readObject();

            return converter.getPrivateKey(keyInfo);

        } catch (IOException ex){
            throw new CustomException(ExceptionCode.INTERNAL_SERVER_ERROR);
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
