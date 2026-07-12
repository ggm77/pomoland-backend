package com.seohamin.pomoland.domain.map.tile.dto;

import java.util.List;

public record MapResponseDto(
        Integer sizeX,
        Integer sizeY,
        List<TileResponseDto> map
) {
}
