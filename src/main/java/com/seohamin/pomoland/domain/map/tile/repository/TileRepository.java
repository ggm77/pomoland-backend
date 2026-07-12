package com.seohamin.pomoland.domain.map.tile.repository;

import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TileRepository extends JpaRepository<Tile, Integer> {
    Optional<Tile> findByOwnerIdAndIsSpawnPoint(final Long ownerId, final Boolean isSpawnPoint);
    boolean existsByOwnerIdAndIsSpawnPoint(final Long ownerId, final Boolean isSpawnPoint);
    Optional<Tile> findByXAndY(final Integer x, final Integer y);
    boolean existsByXAndY(final Integer x, final Integer y);

    /**
     * 해당 좌표가 점령 가능한 위치인지 판단하는 메서드
     * (상하좌우 4개 타일 중 하나라도 해당 유저의 소유 타일이면 점령 가능)
     * @param x x좌표
     * @param y y좌표
     * @param ownerId 점령을 시도하는 유저 아이디
     * @return 점령 가능 여부
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Tile t " +
            "WHERE t.owner.id = :ownerId " +
            "AND ((t.x = :x - 1 AND t.y = :y) " +
            "OR (t.x = :x + 1 AND t.y = :y) " +
            "OR (t.x = :x AND t.y = :y - 1) " +
            "OR (t.x = :x AND t.y = :y + 1))")
    boolean isOccupiable(@Param("x") final Integer x, @Param("y") final Integer y, @Param("ownerId") final Long ownerId);

}
