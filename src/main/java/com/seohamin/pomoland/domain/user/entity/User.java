package com.seohamin.pomoland.domain.user.entity;

import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.session.entity.Session;
import com.seohamin.pomoland.global.constant.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", indexes = @Index(name = "idx_member_point", columnList = "point"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결된 OAuth2
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOauth> userOauths = new ArrayList<>();

    // 보유 타일
    @OneToMany(mappedBy = "owner",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tile> tiles = new ArrayList<>();

    // 공부 기록
    @OneToMany(mappedBy = "member",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions = new ArrayList<>();

    // 유저 Role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Role role;

    // 유저 명
    @Column(length = 100, nullable = false)
    @NotNull
    @Size(max = 100)
    private String username;

    // 보유 포인트
    @Column(nullable = false)
    @NotNull
    private Integer point;

    // 뽀모도로 시도 횟수
    @Column(nullable = false)
    @NotNull
    private Integer pomoTry;

    // 뽀모도로 성공 횟수
    @Column(nullable = false)
    @NotNull
    private Integer pomoComplete;

    // 공부시간 설정 (분)
    @Column(nullable = false)
    @NotNull
    private Integer studyTime;

    // 쉬는시간 설정 (분)
    @Column(nullable = false)
    @NotNull
    private Integer restTime;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public User(
            final Role role,
            final String username,
            final Integer point,
            final Integer pomoTry,
            final Integer pomoComplete
    ) {
        this.role = role;
        this.username = username;
        this.point = point;
        this.pomoTry = pomoTry;
        this.pomoComplete = pomoComplete;
        this.studyTime = 25;
        this.restTime = 5;
    }

    public void updateUsername(final String username) {
        this.username = username;
    }

    public void updatePoint(final Integer point) {
        this.point = point;
    }

    public void plushPomoTry() {
        this.pomoTry += 1;
    }

    public void plushPomoComplete() {
        this.pomoComplete += 1;
    }

    public void updateStudyTime(final Integer studyTime) {
        this.studyTime = studyTime;
    }

    public void updateRestTime(final Integer restTime) {
        this.restTime = restTime;
    }
}
