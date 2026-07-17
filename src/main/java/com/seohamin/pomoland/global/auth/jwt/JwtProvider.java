package com.seohamin.pomoland.global.auth.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohamin.pomoland.global.constant.Role;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.accessToken_exprTime}")
    private Integer ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refreshToken_exprTime}")
    private Integer REFRESH_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.stateToken_exprTime}")
    private Integer STATE_TOKEN_EXPIRATION_TIME;

    public String getTokenType() {
        return "Bearer";
    }

    public Long getAccessTokenExpirationTime() {
        return ACCESS_TOKEN_EXPIRATION_TIME.longValue();
    }

    /**
     * 액세스 토큰을 생성하는 메서드
     * @param userId 유저의 고유 아이디 번호
     * @return JWT
     */
    public String creatAccessToken(final Long userId, final Role role){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_TIME); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userIdStr)
                .claim("authorities", List.of(role.getKey()))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성하는 메서드
     * @param userId 유저의 고유 아이디 번호
     * @return JWT
     */
    public String creatRefreshToken(final Long userId){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(REFRESH_TOKEN_EXPIRATION_TIME); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userIdStr)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * CSRF방지를 위해 state 값이 필요한 경우
     * 사용하는 JWT을 생성하는 메서드
     * @return sub가 UUID인 5분짜리 JWT
     */
    public String createStateToken(){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String sub = UUID.randomUUID().toString();
        final Instant now = Instant.now();
        final Instant exp = now.plusSeconds(STATE_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(sub)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * 리프레시 토큰을 검증하는 메서드
     * 리프레시 토큰이 아니면(액세스 토큰 등) 예외를 던짐
     * 문자열이 된 유저의 고유 아이디 번호를 리턴함
     * @param jwt 리프레시 토큰
     * @return 문자열이 된 유저 아이디
     */
    public String getRefreshTokenSubject(final String jwt){

        final Claims claims = getClaims(jwt);

        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }

        return claims.getSubject();
    }

    /**
     * JWT에서 claims만 얻어오는 메서드
     * @param jwt JWT
     * @return JWT의 Claims
     */
    public Claims getClaims(final String jwt){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (JwtException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }

    /**
     * 애플 idToken에서 Claims를 뽑는 메서드
     * @param token 애플 idToken
     * @param publicKey 애플 공개 키
     * @return Claims
     */
    public Claims getClaimsFromAppleToken(
            final String token,
            final PublicKey publicKey
    ){
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new CustomException(ExceptionCode.APPLE_AUTH_ERROR);
        }
    }

    /**
     * 토큰에서 헤더의 값만 가져오는 메서드
     * @param token 헤더를 가진 토큰
     * @return 맵 형태가 된 헤더
     */
    public Map<String, String> getHeaders(final String token){
        try{
            final String header = token.split("\\.")[0];
            return new ObjectMapper().readValue(decode(header), Map.class);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }

    /**
     * base64url로 인코딩 된 JWT 헤더 부분을 디코딩 하는 메서드
     * (JWT는 표준 Base64가 아닌 Base64URL 인코딩을 사용함)
     * @param base64Url base64url로 인코딩 된 문자열
     * @return 디코딩된 문자열
     */
    public String decode(final String base64Url){
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }

    /**
     * JWT에서 Authorities를 얻는 메서드
     * @param claims JWT의 claims
     * @return JWT의 Authorities
     */
    public List<SimpleGrantedAuthority> getAuthorities(final Claims claims){
        final List<String> roles = claims.get("authorities", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
