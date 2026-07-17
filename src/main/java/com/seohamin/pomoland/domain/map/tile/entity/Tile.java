package com.seohamin.pomoland.domain.map.tile.entity;

import com.seohamin.pomoland.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_tile_x_y",
        columnNames = {"x", "y"}
))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // x좌표
    @Column(nullable = false)
    @NotNull
    private Integer x;

    // y좌표
    @Column(nullable = false)
    @NotNull
    private Integer y;

    // 타일 보유 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // 방어력 포인트
    @Column(nullable = false)
    @NotNull
    private Integer defensePower;

    // 스폰 포인트 여부
    @Column(nullable = false)
    @NotNull
    private Boolean isSpawnPoint;

    @Builder
    public Tile(
            final Integer x,
            final Integer y,
            final User owner,
            final Integer defensePower,
            final Boolean isSpawnPoint
    ) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.defensePower = defensePower;
        this.isSpawnPoint = isSpawnPoint;
    }

    public void updateOwner(User owner) {
        this.owner = owner;
    }

    public void updateDefensePower(Integer defensePower) {
        this.defensePower = defensePower;
    }

    public void updateIsSpawnPoint(Boolean isSpawnPoint) {
        this.isSpawnPoint = isSpawnPoint;
    }
}
