package com.seohamin.pomoland.domain.session.service;

import com.seohamin.pomoland.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 만료 처리 전용 서비스
 * 만료 처리 직후 호출자가 예외를 던져 롤백되기 때문에,
 * 별도 빈으로 분리해 REQUIRES_NEW가 프록시를 통해 적용되도록 함
 */
@Service
@RequiredArgsConstructor
public class SessionExpireService {

    private final SessionRepository sessionRepository;

    /**
     * 세션 만료 처리시키는 메서드
     * 호출자의 트랜잭션과 분리된 새 트랜잭션에서 커밋됨
     * @param sessionUuid 만료시킬 세션의 uuid
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireSession(final String sessionUuid) {
        sessionRepository.findBySessionUuid(sessionUuid)
                .ifPresent(session -> session.updateIsRunning(false));
    }
}