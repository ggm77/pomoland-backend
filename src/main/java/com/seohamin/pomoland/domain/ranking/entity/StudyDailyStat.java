package com.seohamin.pomoland.domain.ranking.entity;

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
import java.time.LocalDate;

// 유저별 일자별 공부시간 집계 (랭킹/일간·주간 조회용)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_daily_stat",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_study_date", columnNames = {"member_id", "study_date"}),
        indexes = @Index(name = "idx_study_date_member", columnList = "study_date, member_id")
)
public class StudyDailyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 집계 대상 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    // 집계 기준 날짜 (KST)
    @Column(name = "study_date", nullable = false)
    @NotNull
    private LocalDate studyDate;

    // 해당 날짜에 완료한 세션들의 공부시간 합 (초)
    @Column(name = "study_seconds", nullable = false)
    @NotNull
    private Long studySeconds;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public StudyDailyStat(
            final User member,
            final LocalDate studyDate,
            final Long studySeconds
    ) {
        this.member = member;
        this.studyDate = studyDate;
        this.studySeconds = studySeconds;
    }
}