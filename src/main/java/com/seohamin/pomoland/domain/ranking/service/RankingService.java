package com.seohamin.pomoland.domain.ranking.service;

import com.seohamin.pomoland.domain.map.tile.repository.TileCountProjection;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import com.seohamin.pomoland.domain.ranking.dto.RankingResponseDto;
import com.seohamin.pomoland.domain.ranking.repository.StudyDailyStatRepository;
import com.seohamin.pomoland.domain.ranking.repository.StudyRankingProjection;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RankingService {

    // 하루 경계 계산 기준 타임존
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    // 주간 랭킹 집계 기간 (오늘 포함 최근 7일)
    private static final int WEEKLY_RANKING_RANGE_DAYS = 7;
    // 한 번에 조회 가능한 최대 랭킹 인원
    private static final int MAX_RANKING_SIZE = 100;

    private final StudyDailyStatRepository studyDailyStatRepository;
    private final UserRepository userRepository;
    private final TileRepository tileRepository;

    /**
     * 오늘 공부시간 기준 랭킹을 조회하는 메서드
     * @param limit 조회할 인원 수
     * @return 공부시간 내림차순 랭킹
     */
    @Transactional(readOnly = true)
    public List<RankingResponseDto> getDailyRanking(final Integer limit) {
        return getStudyTimeRanking(LocalDate.now(KST), limit);
    }

    /**
     * 최근 7일 공부시간 기준 랭킹을 조회하는 메서드
     * @param limit 조회할 인원 수
     * @return 공부시간 내림차순 랭킹
     */
    @Transactional(readOnly = true)
    public List<RankingResponseDto> getWeeklyRanking(final Integer limit) {
        return getStudyTimeRanking(weekStart(), limit);
    }

    /**
     * 보유 타일 수 기준 랭킹을 조회하는 메서드
     * @param limit 조회할 인원 수
     * @return 보유 타일 수 내림차순 랭킹
     */
    @Transactional(readOnly = true)
    public List<RankingResponseDto> getTileRanking(final Integer limit) {
        final Pageable pageable = validatePageable(limit);

        // 1) 보유 타일 수 기준 순위 조회 (유저 아이디, 타일 수)
        final List<TileCountProjection> rankings = tileRepository.findTileCountRanking(pageable);
        if (rankings.isEmpty()) {
            return List.of();
        }

        final List<Long> memberIds = rankings.stream()
                .map(TileCountProjection::getOwnerId)
                .toList();
        final Map<Long, Long> tileCountByMemberId = rankings.stream()
                .collect(Collectors.toMap(TileCountProjection::getOwnerId, TileCountProjection::getTileCount));

        return assembleRanking(memberIds, tileCountByMemberId, findWeeklyStudySeconds(memberIds));
    }

    /**
     * 보유 포인트 기준 랭킹을 조회하는 메서드
     * @param limit 조회할 인원 수
     * @return 보유 포인트 내림차순 랭킹
     */
    @Transactional(readOnly = true)
    public List<RankingResponseDto> getPointRanking(final Integer limit) {
        final Pageable pageable = validatePageable(limit);

        // 1) 보유 포인트 기준 순위 조회
        final List<User> rankedUsers = userRepository.findAllByOrderByPointDesc(pageable);
        if (rankedUsers.isEmpty()) {
            return List.of();
        }

        final List<Long> memberIds = rankedUsers.stream().map(User::getId).toList();
        final Map<Long, User> userByMemberId = rankedUsers.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return assembleRanking(
                memberIds,
                userByMemberId,
                tileCountByMemberId(memberIds),
                findWeeklyStudySeconds(memberIds)
        );
    }

    private List<RankingResponseDto> getStudyTimeRanking(final LocalDate startDate, final Integer limit) {
        final Pageable pageable = validatePageable(limit);

        // 1) 공부시간 기준 순위 조회 (유저 아이디, 공부시간 합)
        final List<StudyRankingProjection> rankings = studyDailyStatRepository.findRanking(startDate, pageable);
        if (rankings.isEmpty()) {
            return List.of();
        }

        final List<Long> memberIds = rankings.stream()
                .map(StudyRankingProjection::getMemberId)
                .toList();
        final Map<Long, Long> studySecondsByMemberId = rankings.stream()
                .collect(Collectors.toMap(StudyRankingProjection::getMemberId, StudyRankingProjection::getTotalStudySeconds));

        return assembleRanking(memberIds, tileCountByMemberId(memberIds), studySecondsByMemberId);
    }

    /**
     * 순위에 오른 유저들의 아이디로 유저 정보(이름, 포인트)를 배치 조회한 뒤 랭킹을 조립하는 메서드
     * @param memberIds 순위가 매겨진 유저 아이디 목록 (순서 유지)
     * @param tileCountByMemberId 유저별 보유 타일 수
     * @param studySecondsByMemberId 유저별 공부시간 합 (초)
     * @return 순위가 매겨진 랭킹 목록
     */
    private List<RankingResponseDto> assembleRanking(
            final List<Long> memberIds,
            final Map<Long, Long> tileCountByMemberId,
            final Map<Long, Long> studySecondsByMemberId
    ) {
        final Map<Long, User> userByMemberId = userRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return assembleRanking(memberIds, userByMemberId, tileCountByMemberId, studySecondsByMemberId);
    }

    /**
     * 배치 조회해 둔 유저 정보, 타일 수, 공부시간을 조합해 순위를 매기는 메서드
     * @param memberIds 순위가 매겨진 유저 아이디 목록 (순서 유지)
     * @param userByMemberId 유저별 유저 정보 (이름, 포인트)
     * @param tileCountByMemberId 유저별 보유 타일 수
     * @param studySecondsByMemberId 유저별 공부시간 합 (초)
     * @return 순위가 매겨진 랭킹 목록
     */
    private List<RankingResponseDto> assembleRanking(
            final List<Long> memberIds,
            final Map<Long, User> userByMemberId,
            final Map<Long, Long> tileCountByMemberId,
            final Map<Long, Long> studySecondsByMemberId
    ) {
        return IntStream.range(0, memberIds.size())
                .mapToObj(i -> {
                    final Long memberId = memberIds.get(i);
                    final User user = userByMemberId.get(memberId);

                    return RankingResponseDto.of(
                            i + 1,
                            user.getId(),
                            user.getUsername(),
                            studySecondsByMemberId.getOrDefault(memberId, 0L),
                            tileCountByMemberId.getOrDefault(memberId, 0L).intValue(),
                            user.getPoint()
                    );
                })
                .toList();
    }

    private Map<Long, Long> tileCountByMemberId(final List<Long> memberIds) {
        return tileRepository.countTilesByOwnerIds(memberIds).stream()
                .collect(Collectors.toMap(TileCountProjection::getOwnerId, TileCountProjection::getTileCount));
    }

    private Map<Long, Long> findWeeklyStudySeconds(final List<Long> memberIds) {
        return studyDailyStatRepository.sumStudySecondsByMemberIdsSince(memberIds, weekStart()).stream()
                .collect(Collectors.toMap(StudyRankingProjection::getMemberId, StudyRankingProjection::getTotalStudySeconds));
    }

    private LocalDate weekStart() {
        return LocalDate.now(KST).minusDays(WEEKLY_RANKING_RANGE_DAYS - 1);
    }

    private Pageable validatePageable(final Integer limit) {
        if (limit == null || limit <= 0 || limit > MAX_RANKING_SIZE) {
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        return PageRequest.of(0, limit);
    }
}