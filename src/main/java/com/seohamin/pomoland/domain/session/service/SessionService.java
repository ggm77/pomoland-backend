package com.seohamin.pomoland.domain.session.service;

import com.seohamin.pomoland.domain.session.dto.SessionResponseDto;
import com.seohamin.pomoland.domain.session.entity.Session;
import com.seohamin.pomoland.domain.session.repository.SessionRepository;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    /**
     * 타이머 세션 시작하는 메서드
     * @param userIdStr 요청 유저 아이디 문자열
     * @return 세션 정보 DTO
     */
    @Transactional
    public SessionResponseDto createSession(
            final String userIdStr
    ) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 시간 설정
        final Instant startAt = Instant.now();
        final Instant endAt = startAt.plusSeconds(user.getStudyTime() * 60L);
        final Instant lastHeartBeatAt = startAt;

        // 5) 세션 엔티티 제작
        final Session session = Session.builder()
                .sessionUuid(UUID.randomUUID().toString())
                .member(user)
                .startAt(startAt)
                .endAt(endAt)
                .lastHeartBeatAt(lastHeartBeatAt)
                .isRunning(true)
                .isComplete(false)
                .build();

        // 6) 저장
        final Session savedSession = sessionRepository.save(session);

        return SessionResponseDto.of(savedSession);
    }
}
