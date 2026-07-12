package com.seohamin.pomoland.domain.map.tile.dto;

public record TileResponseDto(
        Integer x,
        Integer y,
        Long ownerId,
        Integer defensePower,
        Boolean isSpawnPoint
) { }
