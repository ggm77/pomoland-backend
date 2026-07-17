package com.seohamin.pomoland.domain.auth.oauth2.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.seohamin.pomoland.domain.auth.oauth2.dto.OauthRequestDto;
import com.seohamin.pomoland.domain.user.dto.UserOauthAccountsRequestDto;
import com.seohamin.pomoland.domain.user.dto.UserOauthAccountsResponseDto;
import com.seohamin.pomoland.domain.user.service.UserOauthService;
import com.seohamin.pomoland.global.auth.apple.client.AppleAuthClient;
import com.seohamin.pomoland.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.seohamin.pomoland.global.auth.apple.dto.token.AppleTokenResponseDto;
import com.seohamin.pomoland.global.auth.apple.util.AppleKeyGenerator;
import com.seohamin.pomoland.global.auth.jwt.JwtProvider;
import com.seohamin.pomoland.global.constant.Role;
import com.seohamin.pomoland.global.dto.JwtDto;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {

    // 구글 토큰 엔드포인트 호출 시 커넥션/응답 타임아웃 (기본값에 의존하지 않고 명시)
    private static final int GOOGLE_REQUEST_TIMEOUT_MILLIS = 5000;

    @Value("${oauth2.google.web_client_id}")
    private String GOOGLE_WEB_CLIENT_ID;

    @Value("${oauth2.google.web_client_secret}")
    private String GOOGLE_WEB_CLIENT_SECRET;

    @Value("${oauth2.google.redirect_url}")
    private String GOOGLE_REDIRECT_URI;

    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleAuthClient appleAuthClient;
    private final JwtProvider jwtProvider;
    private final UserOauthService userOauthService;

    // 구글 idToken 서명/발급자/audience를 검증하는 verifier (공개키를 캐싱하므로 인스턴스 재사용)
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @PostConstruct
    private void initGoogleIdTokenVerifier() {
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_WEB_CLIENT_ID))
                .build();
    }

    /**
     * Apple OAuth2 진행하는 메서드
     * @param oauthRequestDto 사용자 이름과 auth code 담긴 DTO
     * @return JWT
     */
    public JwtDto processAppleOauth(final OauthRequestDto oauthRequestDto) {
        // 1) null 검사
        if (
                oauthRequestDto == null || oauthRequestDto.code() == null || oauthRequestDto.code().isBlank()
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) code와 name 추출
        final String code = oauthRequestDto.code();
        final String name = oauthRequestDto.name();

        // 3) idToken과 리프레시 토큰 추출
        final AppleTokenResponseDto appleTokenResponseDto = appleAuthClient.requestToken(code);
        final String idToken = appleTokenResponseDto.getId_token();
        final String appleRefreshToken = appleTokenResponseDto.getRefresh_token();

        // 4) 헤더 추출
        final Map<String, String> headers = jwtProvider.getHeaders(idToken);

        // 5) 애플에 공개키 요청
        final ApplePublicKeyResponseDto applePublicKeyResponseDto = appleAuthClient.requestKeys();

        // 6) 키 조합
        final PublicKey publicKey = appleKeyGenerator.generatePublicKey(
                headers,
                applePublicKeyResponseDto
        );

        // 7) 애플 아이디와 이메일 가져오기
        final Claims claims = jwtProvider.getClaimsFromAppleToken(idToken, publicKey);
        final String accountId = claims.getSubject();
        final String email = claims.get("email", String.class);

        // 8) oauth 정보 저장용 DTO 생성
        final UserOauthAccountsRequestDto userOauthAccountsRequestDto = new UserOauthAccountsRequestDto(
                "apple",
                accountId,
                email,
                name,
                null,
                appleRefreshToken
        );

        // 9) 유저 로그인 또는 회원가입
        return upsertUser(userOauthAccountsRequestDto);
    }

    /**
     * 구글 OAuth2 진행하는 메서드
     * @param oauthRequestDto auth code 담긴 DTO
     * @return JWT
     */
    public JwtDto processGoogleOauth(final OauthRequestDto oauthRequestDto) {
        // 1) null 검사
        if (
                oauthRequestDto == null || oauthRequestDto.code() == null || oauthRequestDto.code().isBlank()
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) code와 name 추출
        final String code = oauthRequestDto.code();

        // 3) code를 통해 구글에서 리프레시 토큰과 유저 정보 조회
        final GoogleTokenResponse response;
        try {
            final GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    "https://oauth2.googleapis.com/token",
                    GOOGLE_WEB_CLIENT_ID,
                    GOOGLE_WEB_CLIENT_SECRET,
                    code,
                    GOOGLE_REDIRECT_URI
            );
            tokenRequest.setRequestInitializer(request -> {
                request.setConnectTimeout(GOOGLE_REQUEST_TIMEOUT_MILLIS);
                request.setReadTimeout(GOOGLE_REQUEST_TIMEOUT_MILLIS);
            });

            response = tokenRequest.execute();
        } catch (IOException ex) {
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR);
        }

        // 4) 리프레시 토큰 변수에 저장
        final String googleRefreshToken = response.getRefreshToken();

        // 5) idToken 서명/발급자/audience 검증 후 payload 추출
        final GoogleIdToken.Payload payload;
        try {
            final GoogleIdToken idToken = googleIdTokenVerifier.verify(response.getIdToken());

            //검증 성공/실패 확인
            if(idToken == null) {
                throw new CustomException(ExceptionCode.INVALID_TOKEN);
            }

            payload = idToken.getPayload();

        } catch (GeneralSecurityException | IOException ex){
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR);
        }

        // 6) payload에서 정보 추출해서 DTO에 정보 주입
        final UserOauthAccountsRequestDto userOauthAccountsRequestDto = new UserOauthAccountsRequestDto(
                "google",
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"),
                (String) payload.get("picture"),
                googleRefreshToken
        );

        // 7) 유저 로그인 또는 회원가입
        return upsertUser(userOauthAccountsRequestDto);
    }

    public JwtDto upsertUser(final UserOauthAccountsRequestDto userOauthAccountsRequestDto) {
        // 1) 유저 로그인 또는 회원가입
        final UserOauthAccountsResponseDto userOauthAccountsResponseDto = userOauthService.upsertOAuthUser(userOauthAccountsRequestDto);

        // 2) JWT 생성
        final Long userId = userOauthAccountsResponseDto.userId();
        final Role userRole = userOauthAccountsResponseDto.userRole();
        final String accessToken = jwtProvider.creatAccessToken(userId, userRole);
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        return new JwtDto(
                accessToken,
                jwtProvider.getTokenType(),
                jwtProvider.getAccessTokenExpirationTime(),
                refreshToken
        );
    }
}
