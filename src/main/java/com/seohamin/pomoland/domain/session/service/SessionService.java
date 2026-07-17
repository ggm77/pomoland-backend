package com.seohamin.pomoland.domain.session.service;

import com.seohamin.pomoland.domain.ranking.repository.StudyDailyStatRepository;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    // heartbeat 시간
    private static final int HEARTBEAT_INTERVAL = 30;
    // 하루 경계 계산 기준 타임존
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StudyDailyStatRepository studyDailyStatRepository;
    private final SessionExpireService sessionExpireService;

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

        // 4) 돌아가고 있는 세션 조회
        final Instant startAt = Instant.now();
        final List<Session> runningSessions =
                sessionRepository.findAllByIsRunningAndMemberId(true, userId);

        // 5) heartbeat이 살아있는 세션이 있으면 새 세션 만들 수 없음
        final boolean hasAliveSession = runningSessions.stream()
                .anyMatch(runningSession -> !isHeartBeatExpired(runningSession, startAt));
        if (hasAliveSession) {
            throw new CustomException(ExceptionCode.ONLINE_SESSION_ALREADY_EXIST);
        }

        // 6) heartbeat이 끊긴 세션은 만료 처리
        runningSessions.forEach(runningSession -> runningSession.updateIsRunning(false));

        // 7) 시간 설정
        final Instant endAt = startAt.plusSeconds(user.getStudyTime() * 60L);
        final Instant lastHeartBeatAt = startAt;

        // 8) 세션 엔티티 제작
        final Session session = Session.builder()
                .sessionUuid(UUID.randomUUID().toString())
                .member(user)
                .startAt(startAt)
                .endAt(endAt)
                .lastHeartBeatAt(lastHeartBeatAt)
                .isRunning(true)
                .isComplete(false)
                .build();

        // 9) 저장
        final Session savedSession = sessionRepository.save(session);

        // 10) 유저에게 시도 횟수 추가해주기
        user.plushPomoTry();

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

        // 4) 자기 세션인지 확인
        if (!session.getMember().getId().equals(userId)) {
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        // 5) 현재 시각 저장
        final Instant now = Instant.now();

        // 6) 이미 끝난 세션인지 확인
        if (!session.getIsRunning()) {
            throw new CustomException(ExceptionCode.SESSION_EXPIRED);
        }

        // 7) 마지막 heartbeat로 부터 특정 시간 이내인지 판단 (유효한 세션인지 판단)
        if (isHeartBeatExpired(session, now)) {
            sessionExpireService.expireSession(sessionUuid);
            throw new CustomException(ExceptionCode.SESSION_EXPIRED);
        }

        // 8) 갱신
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

        // 5) 완료 되어있는지 확인
        if (session.getIsComplete()) {
            throw new CustomException(ExceptionCode.SESSION_ALREADY_COMPLETED);
        }

        // 6) 포기 처리
        session.updateIsRunning(false);
        session.updateIsComplete(false);

        return SessionResponseDto.of(session);
    }

    /**
     * 세션 완료처리하는 메서드
     * @param userIdStr 요청 유저 id
     * @param sessionUuid 완료 처리할 세션 uuid
     * @return 완료된 세션 정보
     */
    @Transactional
    public SessionResponseDto completeSession(
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

        // 4) 유저 조회
        final User user = session.getMember();

        // 5) 자기 세션인지 확인
        if (!user.getId().equals(userId)) {
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        // 6) 이미 완료된 세션인지 확인
        if (session.getIsComplete()) {
            throw new CustomException(ExceptionCode.SESSION_ALREADY_COMPLETED);
        }

        // 7) 현재 시간
        final Instant now = Instant.now();

        // 8) 완료 처리 할 수 있는 세션인지 확인
        if (session.getEndAt().isAfter(now)) {
            throw new CustomException(ExceptionCode.CANNOT_COMPLETE);
        }

        // 9) 이미 끝난 세션인지 확인
        if (!session.getIsRunning()) {
            throw new CustomException(ExceptionCode.SESSION_EXPIRED);
        }

        // 10) 살아있는 세션인지 확인
        if (isHeartBeatExpired(session, now)) {
            sessionExpireService.expireSession(sessionUuid);
            throw new CustomException(ExceptionCode.SESSION_EXPIRED);
        }

        // 11) 완료 처리
        session.updateLastHeartBeatAt(now);
        session.updateIsRunning(false);
        session.updateIsComplete(true);

        // 12) 포인트 지급 및 완료 회수 추가 (5분당 2포인트, 25분당 10포인트)
        final Duration sessionDuration = Duration.between(session.getStartAt(), session.getEndAt());
        final long sessionMinutes = sessionDuration.toMinutes();
        final int earnedPoint = (int) (sessionMinutes / 5) * 2;
        user.updatePoint(user.getPoint() + earnedPoint);
        user.plushPomoComplete();

        // 13) 일별 공부시간 집계 갱신 (랭킹, 일간/주간 공부시간 조회용)
        final LocalDate studyDate = session.getStartAt().atZone(KST).toLocalDate();
        studyDailyStatRepository.upsertStudySeconds(userId, studyDate, sessionDuration.getSeconds());

        return SessionResponseDto.of(session);
    }

    /**
     * heartbeat이 끊긴 세션인지 판단하는 메서드
     * @param session 판단할 세션
     * @param now 기준 시각
     * @return 마지막 heartbeat으로부터 HEARTBEAT_INTERVAL이 지났으면 true
     */
    private boolean isHeartBeatExpired(final Session session, final Instant now) {
        return session.getLastHeartBeatAt().plusSeconds(HEARTBEAT_INTERVAL).isBefore(now);
    }
}
