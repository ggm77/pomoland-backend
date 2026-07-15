package com.seohamin.pomoland.domain.session.repository;

import com.seohamin.pomoland.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionUuid(final String sessionId);
    List<Session> findAllByIsRunningAndMemberId(final boolean isRunning, final Long memberId);
    List<Session> findAllByMemberIdAndIsCompleteTrueAndStartAtGreaterThanEqual(final Long memberId, final Instant startAt);
}
