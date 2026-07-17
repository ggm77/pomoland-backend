package com.seohamin.pomoland.domain.map.tile.repository;

import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * 주어진 유저들이 보유한 타일 수를 각각 조회하는 메서드
     * @param ownerIds 조회할 유저 아이디 목록
     * @return 유저별 보유 타일 수
     */
    @Query("SELECT t.owner.id as ownerId, COUNT(t) as tileCount " +
            "FROM Tile t " +
            "WHERE t.owner.id IN :ownerIds " +
            "GROUP BY t.owner.id")
    List<TileCountProjection> countTilesByOwnerIds(@Param("ownerIds") final List<Long> ownerIds);

    /**
     * 보유 타일 수 기준 랭킹을 조회하는 메서드
     * @param pageable 랭킹 크기 제한용 페이지 정보
     * @return 유저별 보유 타일 수 랭킹 (내림차순)
     */
    @Query("SELECT t.owner.id as ownerId, COUNT(t) as tileCount " +
            "FROM Tile t " +
            "GROUP BY t.owner.id " +
            "ORDER BY COUNT(t) DESC")
    List<TileCountProjection> findTileCountRanking(final Pageable pageable);

}
