package com.seohamin.pomoland.domain.ranking.dto;

public record RankingResponseDto(
        Integer rank,
        Long userId,
        String username,
        Integer studyTime,
        Integer tileCount,
        Integer point
) {
    public static RankingResponseDto of(
            final int rank,
            final Long userId,
            final String username,
            final Long totalStudySeconds,
            final Integer tileCount,
            final Integer point
    ) {
        return new RankingResponseDto(
                rank,
                userId,
                username,
                (int) (totalStudySeconds / 60),
                tileCount,
                point
        );
    }
}