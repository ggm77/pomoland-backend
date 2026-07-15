package com.seohamin.pomoland.domain.auth.token.service;

import com.seohamin.pomoland.domain.auth.token.dto.TokenRefreshRequestDto;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.auth.jwt.JwtProvider;
import com.seohamin.pomoland.global.dto.JwtDto;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    /**
     * 리프레시 토큰으로 JWT 새로 발급받는 메서드
     * @param tokenRefreshRequestDto 리프레시 토큰 담긴 DTO
     * @return JWT
     */
    public JwtDto refreshToken(final TokenRefreshRequestDto tokenRefreshRequestDto) {
        // 1) null 검사
        if (
                tokenRefreshRequestDto == null
                || tokenRefreshRequestDto.refreshToken() == null
                || tokenRefreshRequestDto.refreshToken().isBlank()
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) jwt 검증
        final String userIdStr = jwtProvider.getRefreshTokenSubject(tokenRefreshRequestDto.refreshToken());

        // 3) 아이디 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 4) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 5) jwt 발급
        final String accessToken = jwtProvider.creatAccessToken(userId, user.getRole());
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        return new JwtDto(
                accessToken,
                jwtProvider.getTokenType(),
                jwtProvider.getAccessTokenExpirationTime(),
                refreshToken
        );
    }
}
