package com.seohamin.pomoland.domain.session.dto;

import com.seohamin.pomoland.domain.session.entity.Session;

import java.time.Instant;

public record SessionResponseDto(
        String sessionUuid,
        Instant startAt,
        Instant endAt,
        Boolean isRunning,
        Boolean isComplete
) {
    public static SessionResponseDto of(final Session session) {
        return new SessionResponseDto(
                session.getSessionUuid(),
                session.getStartAt(),
                session.getEndAt(),
                session.getIsRunning(),
                session.getIsComplete()
        );
    }
}
