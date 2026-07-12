package com.seohamin.pomoland.domain.map.tile.repository;

import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TileRepository extends JpaRepository<Tile, Integer> {
    Optional<Tile> findByOwnerIdAndIsSpawnPoint(final Long ownerId, final Boolean isSpawnPoint);
}
