package com.seohamin.pomoland.domain.session.repository;

import com.seohamin.pomoland.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionUuid(final String sessionId);
}
