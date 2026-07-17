package com.seohamin.pomoland.domain.ranking.repository;

import com.seohamin.pomoland.domain.ranking.entity.StudyDailyStat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface StudyDailyStatRepository extends JpaRepository<StudyDailyStat, Long> {

    /**
     * 해당 유저의 해당 날짜 공부시간(초)에 원자적으로 더하는 메서드
     * 해당 날짜의 집계 행이 없으면 새로 생성함 (MariaDB upsert)
     * @param memberId 유저 아이디
     * @param studyDate 집계 날짜 (KST 기준)
     * @param seconds 더할 공부시간 (초)
     */
    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO study_daily_stat (member_id, study_date, study_seconds, created_at, updated_at) " +
                    "VALUES (:memberId, :studyDate, :seconds, NOW(), NOW()) " +
                    "ON DUPLICATE KEY UPDATE study_seconds = study_seconds + :seconds, updated_at = NOW()",
            nativeQuery = true
    )
    void upsertStudySeconds(
            @Param("memberId") final Long memberId,
            @Param("studyDate") final LocalDate studyDate,
            @Param("seconds") final Long seconds
    );

    /**
     * 해당 유저의 특정 날짜 공부시간 합(초)을 조회하는 메서드
     * @param memberId 유저 아이디
     * @param studyDate 조회할 날짜
     * @return 공부시간 합 (초)
     */
    @Query("SELECT COALESCE(SUM(s.studySeconds), 0) FROM StudyDailyStat s " +
            "WHERE s.member.id = :memberId AND s.studyDate = :studyDate")
    long sumStudySecondsByMemberIdAndDate(
            @Param("memberId") final Long memberId,
            @Param("studyDate") final LocalDate studyDate
    );

    /**
     * 해당 유저의 특정 날짜 이후 공부시간 합(초)을 조회하는 메서드
     * @param memberId 유저 아이디
     * @param startDate 집계 시작 날짜 (포함)
     * @return 공부시간 합 (초)
     */
    @Query("SELECT COALESCE(SUM(s.studySeconds), 0) FROM StudyDailyStat s " +
            "WHERE s.member.id = :memberId AND s.studyDate >= :startDate")
    long sumStudySecondsByMemberIdSince(
            @Param("memberId") final Long memberId,
            @Param("startDate") final LocalDate startDate
    );

    /**
     * 특정 날짜 이후 유저별 공부시간 합을 내림차순으로 조회하는 메서드 (랭킹)
     * @param startDate 집계 시작 날짜 (포함)
     * @param pageable 랭킹 크기 제한용 페이지 정보
     * @return 유저별 공부시간 합 랭킹 (내림차순)
     */
    @Query("SELECT s.member.id as memberId, SUM(s.studySeconds) as totalStudySeconds " +
            "FROM StudyDailyStat s " +
            "WHERE s.studyDate >= :startDate " +
            "GROUP BY s.member.id " +
            "ORDER BY SUM(s.studySeconds) DESC")
    List<StudyRankingProjection> findRanking(
            @Param("startDate") final LocalDate startDate,
            final Pageable pageable
    );

    /**
     * 주어진 유저들의 특정 날짜 이후 공부시간 합(초)을 각각 조회하는 메서드
     * @param memberIds 조회할 유저 아이디 목록
     * @param startDate 집계 시작 날짜 (포함)
     * @return 유저별 공부시간 합
     */
    @Query("SELECT s.member.id as memberId, SUM(s.studySeconds) as totalStudySeconds " +
            "FROM StudyDailyStat s " +
            "WHERE s.member.id IN :memberIds AND s.studyDate >= :startDate " +
            "GROUP BY s.member.id")
    List<StudyRankingProjection> sumStudySecondsByMemberIdsSince(
            @Param("memberIds") final List<Long> memberIds,
            @Param("startDate") final LocalDate startDate
    );
}