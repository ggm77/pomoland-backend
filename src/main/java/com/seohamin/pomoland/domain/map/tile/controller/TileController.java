package com.seohamin.pomoland.domain.map.tile.controller;

import com.seohamin.pomoland.domain.map.tile.dto.MapResponseDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.service.TileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TileController {

    private final TileService tileService;

    // 전체 맵 조회하는 API
    @GetMapping("/map/tiles")
    public ResponseEntity<MapResponseDto> getMap() {

        return ResponseEntity.ok(tileService.getMap());
    }

    // 특정 타일 조회 API
    @GetMapping("/map/tiles/{x}/{y}")
    public ResponseEntity<TileResponseDto> getTile(
            @PathVariable final Integer x,
            @PathVariable final Integer y
    ) {

        return ResponseEntity.ok(tileService.getTile(x, y));
    }
}
