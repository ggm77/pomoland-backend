package com.seohamin.pomoland.domain.map.tile.controller;

import com.seohamin.pomoland.domain.map.tile.dto.MapResponseDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileRequestDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.service.TileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 타일 점령 API
    @PostMapping("/map/tiles/{x}/{y}/occupy")
    public ResponseEntity<Void> occupy(
            @AuthenticationPrincipal final String userIdStr,
            @PathVariable final Integer x,
            @PathVariable final Integer y,
            @RequestBody final TileRequestDto tileRequestDto
    ) {

        tileService.occupy(userIdStr, x, y, tileRequestDto);

        return ResponseEntity.noContent().build();
    }

    // 타일 방어 API
    @PostMapping("/map/tiles/{x}/{y}/defense")
    public ResponseEntity<Void> defense(
            @AuthenticationPrincipal final String userIdStr,
            @PathVariable final Integer x,
            @PathVariable final Integer y,
            @RequestBody final TileRequestDto tileRequestDto
    ) {

        tileService.defense(userIdStr, x, y, tileRequestDto);

        return ResponseEntity.noContent().build();
    }
}
