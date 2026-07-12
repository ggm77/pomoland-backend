package com.seohamin.pomoland.domain.map.tile.dto;

import com.seohamin.pomoland.domain.map.tile.entity.Tile;

public record TileResponseDto(
        Integer x,
        Integer y,
        Long ownerId,
        Integer defensePower,
        Boolean isSpawnPoint
) {
    public static TileResponseDto of(final Tile tile) {
        if (tile == null) {
            return null;
        }

        return new TileResponseDto(
                tile.getX(),
                tile.getY(),
                tile.getOwner().getId(),
                tile.getDefensePower(),
                tile.getIsSpawnPoint()
        );
    }
}
