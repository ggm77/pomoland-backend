package com.seohamin.pomoland.domain.user.service;

import com.seohamin.pomoland.domain.user.dto.UserOauthAccountsRequestDto;
import com.seohamin.pomoland.domain.user.dto.UserOauthAccountsResponseDto;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.entity.UserOauth;
import com.seohamin.pomoland.domain.user.repository.UserOauthRepository;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.constant.Role;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOauthService {

    private final UserRepository userRepository;
    private final UserOauthRepository userOauthRepository;

    @Transactional
    public UserOauthAccountsResponseDto upsertOAuthUser(final UserOauthAccountsRequestDto userOauthAccountsRequestDto) {
        // 1) null 검사
        if (
                userOauthAccountsRequestDto == null
                || userOauthAccountsRequestDto.provider() == null
                || userOauthAccountsRequestDto.provider().isBlank()
                || userOauthAccountsRequestDto.providerUserId() == null
                || userOauthAccountsRequestDto.providerUserId().isBlank()
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 가입 여부 확인 하기위해 DB조회
        final Optional<UserOauth> userOauth = userOauthRepository.findByProviderAndProviderUserId(
                userOauthAccountsRequestDto.provider(),
                userOauthAccountsRequestDto.providerUserId()
        );

        // 3) 가입 여부에 따라 분기
        if (userOauth.isPresent()) {
            // 4) 리프레시 토큰 있으면 갱신
            if(userOauthAccountsRequestDto.refreshToken() != null && !userOauthAccountsRequestDto.refreshToken().isBlank()){
                userOauth.get().updateRefreshToken(userOauthAccountsRequestDto.refreshToken());
            }

            // 5) 리턴
            return UserOauthAccountsResponseDto.of(userOauth.get());
        }
        else {
            // 4) 유저 엔티티 생성
            final User user = User.builder()
                    .role(Role.USER)
                    .username(userOauthAccountsRequestDto.name())
                    .point(0)
                    .pomoTry(0)
                    .pomoComplete(0)
                    .build();

            // 5) 회원 가입 처리
            final User savedUser = userRepository.save(user);

            // 6) UserOauth 엔티티 생성
            final UserOauth savedUserOauth = userOauthRepository.save(
                    UserOauth.builder()
                            .user(savedUser)
                            .provider(userOauthAccountsRequestDto.provider())
                            .providerUserId(userOauthAccountsRequestDto.providerUserId())
                            .email(userOauthAccountsRequestDto.email())
                            .name(userOauthAccountsRequestDto.name())
                            .profileImage(userOauthAccountsRequestDto.profileImage())
                            .refreshToken(userOauthAccountsRequestDto.refreshToken())
                            .build()
            );

            // 7) 리프레시 토큰 저장 실패 시 로그 찍기
            if(userOauthAccountsRequestDto.refreshToken() == null || userOauthAccountsRequestDto.refreshToken().isEmpty()) {
                log.warn(
                        "OAuth2 refresh token missing. provider={}, sub={}, userId={}",
                        userOauthAccountsRequestDto.provider(),
                        userOauthAccountsRequestDto.providerUserId(),
                        savedUser.getId()
                );
            }

            return UserOauthAccountsResponseDto.of(savedUserOauth);
        }
    }
}
