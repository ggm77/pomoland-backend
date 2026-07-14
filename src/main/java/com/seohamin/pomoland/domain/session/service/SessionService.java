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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    // heartbeat 시간
    private static final int HEARTBEAT_INTERVAL = 30;

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

    /**
     * 세션 유지시키는 메서드
     * @param userIdStr 요청 유저 아이디 문자열
     * @param sessionUuid 세션 uuid
     * @return 세션 정보
     */
    @Transactional
    public SessionResponseDto heartbeat(
            final String userIdStr,
            final String sessionUuid
    ) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank() || sessionUuid == null || sessionUuid.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 세션 조회
        final Session session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new CustomException(ExceptionCode.SESSION_NOT_EXIST));

        // 4) 현재 시각 저장
        final Instant now = Instant.now();

        // 5) 마지막 heartbeat로 부터 특정 시간 이내인지 판단 (유효한 세션인지 판단)
        if (session.getLastHeartBeatAt().plusSeconds(HEARTBEAT_INTERVAL).isBefore(now)) {
            expireSession(session);
            throw new CustomException(ExceptionCode.SESSION_EXPIRED);
        }

        // 6) 갱신
        session.updateLastHeartBeatAt(now);

        return SessionResponseDto.of(session);
    }

    /**
     * 세션 정보 조회하는 메서드
     * @param userIdStr 요청 유저 id
     * @param sessionUuid 조회할 세션의 uuid
     * @return 세션 정보
     */
    @Transactional(readOnly = true)
    public SessionResponseDto getSession(
            final String userIdStr,
            final String sessionUuid
    ) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank() || sessionUuid == null || sessionUuid.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 세션 조회
        final Session session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new CustomException(ExceptionCode.SESSION_NOT_EXIST));

        // 4) 자기 세션인지 확인
        if (!session.getMember().getId().equals(userId)) {
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        return SessionResponseDto.of(session);
    }

    /**
     * 세션 포기하는 메서드
     * @param userIdStr 요청 유저 id
     * @param sessionUuid 포기할 세션
     * @return 포기한 세션 정보
     */
    @Transactional
    public SessionResponseDto abandonSession(
            final String userIdStr,
            final String sessionUuid
    ) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank() || sessionUuid == null || sessionUuid.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 세션 조회
        final Session session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new CustomException(ExceptionCode.SESSION_NOT_EXIST));

        // 4) 자기 세션인지 확인
        if (!session.getMember().getId().equals(userId)) {
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        // 5) 포기 처리
        session.updateIsRunning(false);
        session.updateIsComplete(false);

        return SessionResponseDto.of(session);
    }

    /**
     * 세션 만료 처리시키는 메서드
     * 트랜잭션 때문에 별도로 분리
     * @param session 만료시킬 세션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireSession(final Session session) {
        session.updateIsRunning(false);
        sessionRepository.save(session);
    }
}
