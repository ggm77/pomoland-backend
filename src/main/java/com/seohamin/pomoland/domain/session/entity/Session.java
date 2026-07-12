package com.seohamin.pomoland.domain.session.entity;

import com.seohamin.pomoland.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 세션 uuid
    @Column(nullable = false, unique = true)
    @NotNull
    private String sessionUuid;

    // 세션 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member;

    // 세션 시작 시간
    @Column(nullable = false)
    @NotNull
    private Instant startAt;

    // 세션 끝날 시간
    @Column(nullable = false)
    @NotNull
    private Instant endAt;

    // 마지막 폴링 시간
    @Column(nullable = false)
    @NotNull
    private Instant lastHeartBeatAt;

    // 세션 돌아가고 있는지 여부
    @Column(nullable = false)
    @NotNull
    private Boolean isRunning;

    // 세션 완료 되었는지 여부
    @Column(nullable = false)
    @NotNull
    private Boolean isComplete;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public Session(
            final String sessionUuid,
            final User member,
            final Instant startAt,
            final Instant endAt,
            final Instant lastHeartBeatAt,
            final Boolean isRunning,
            final Boolean isComplete
    ) {
        this.sessionUuid = sessionUuid;
        this.member = member;
        this.startAt = startAt;
        this.endAt = endAt;
        this.lastHeartBeatAt = lastHeartBeatAt;
        this.isRunning = isRunning;
        this.isComplete = isComplete;
    }

    public void updateLastHeartBeatAt(final Instant lastHeartBeatAt) {
        this.lastHeartBeatAt = lastHeartBeatAt;
    }

    public void updateIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void updateIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }
}
