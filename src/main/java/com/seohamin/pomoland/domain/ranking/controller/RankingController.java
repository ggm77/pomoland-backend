package com.seohamin.pomoland.domain.ranking.controller;

import com.seohamin.pomoland.domain.ranking.dto.RankingResponseDto;
import com.seohamin.pomoland.domain.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    // 오늘 공부시간 랭킹 조회 API
    @GetMapping("/daily")
    public ResponseEntity<List<RankingResponseDto>> getDailyRanking(
            @RequestParam(defaultValue = "20") final Integer limit
    ) {
        return ResponseEntity.ok(rankingService.getDailyRanking(limit));
    }

    // 최근 7일 공부시간 랭킹 조회 API
    @GetMapping("/weekly")
    public ResponseEntity<List<RankingResponseDto>> getWeeklyRanking(
            @RequestParam(defaultValue = "20") final Integer limit
    ) {
        return ResponseEntity.ok(rankingService.getWeeklyRanking(limit));
    }

    // 보유 타일 수 랭킹 조회 API
    @GetMapping("/tiles")
    public ResponseEntity<List<RankingResponseDto>> getTileRanking(
            @RequestParam(defaultValue = "20") final Integer limit
    ) {
        return ResponseEntity.ok(rankingService.getTileRanking(limit));
    }

    // 보유 포인트 랭킹 조회 API
    @GetMapping("/points")
    public ResponseEntity<List<RankingResponseDto>> getPointRanking(
            @RequestParam(defaultValue = "20") final Integer limit
    ) {
        return ResponseEntity.ok(rankingService.getPointRanking(limit));
    }
}