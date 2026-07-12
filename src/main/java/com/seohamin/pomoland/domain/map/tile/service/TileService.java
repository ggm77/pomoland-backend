package com.seohamin.pomoland.domain.map.tile.service;

import com.seohamin.pomoland.domain.map.tile.dto.MapResponseDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TileService {

    @Value("${map.x_size}")
    private Integer X_SIZE;

    @Value("${map.y_size}")
    private Integer Y_SIZE;

    private final TileRepository tileRepository;

    public MapResponseDto getMap() {

        // 1) 타일 전체 조회
        final List<Tile> tiles = tileRepository.findAll();

        // 2) DTO로 변환
        final List<TileResponseDto> tileResponseDtoList = tiles.stream()
                .map(TileResponseDto::of)
                .toList();

        return new MapResponseDto(
                X_SIZE,
                Y_SIZE,
                tileResponseDtoList
        );
    }
}
