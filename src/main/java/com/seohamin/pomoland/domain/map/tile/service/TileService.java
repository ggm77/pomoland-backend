package com.seohamin.pomoland.domain.map.tile.service;

import com.seohamin.pomoland.domain.map.tile.dto.MapResponseDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
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

    /**
     * 전체 맵 조회하는 메서드
     * @return 맵에 등록된 타일 리스트
     */
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

    /**
     * 특정 좌표의 타일 정보 조회하는 메서드
     * @param x x좌표
     * @param y y좌표
     * @return 상세 정보
     */
    public TileResponseDto getTile(
            final Integer x,
            final Integer y
    ) {
        // 1) null 판단
        if (x == null || y == null) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }


        // 2) 조회
        final Tile tile = tileRepository.findByXAndY(x, y)
                .orElseThrow(() -> new CustomException(ExceptionCode.TILE_NOT_EXIST));

        return TileResponseDto.of(tile);
    }
}
