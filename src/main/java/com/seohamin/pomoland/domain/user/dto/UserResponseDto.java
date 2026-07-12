package com.seohamin.pomoland.domain.user.dto;

import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;

public record UserResponseDto(
        Long id,
        String username,
        Integer tileCount,
        Integer dailyStudyTime,
        Integer weeklyStudyTime,
        Integer point,
        Integer pomoTry,
        Integer pomoComplete,
        TileResponseDto spawnPoint
) { }
