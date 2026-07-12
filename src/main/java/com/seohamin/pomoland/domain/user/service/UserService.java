package com.seohamin.pomoland.domain.user.service;

import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import com.seohamin.pomoland.domain.user.dto.*;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TileRepository tileRepository;

    /**
     * 유저 아이디로 유저 조회하는 메서드
     * @param userIdStr 조회할 유저의 아이디 문자열
     * @return 유저 DTO
     */
    public UserResponseDto getUser(final String userIdStr) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) Long으로 변환
        final Long userId = Long.parseLong(userIdStr);

        // 3) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 스폰 포인트 조회
        final Tile spawnPoint = tileRepository.findByOwnerIdAndIsSpawnPoint(userId, true)
                .orElse(null);

        // 일부는 mock
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getTiles().size(),
                0,
                0,
                user.getPoint(),
                user.getPomoTry(),
                user.getPomoComplete(),
                TileResponseDto.of(spawnPoint)
        );
    }

    /**
     * 유저 정보 수정하는 메서드
     * @param userIdStr 유저 아이디 문자열
     * @param userRequestDto 수정할 정보 담긴 DTO
     */
    @Transactional
    public void updateUser(
            final String userIdStr,
            final UserRequestDto userRequestDto
    ) {
        // 1) null 검사
        if (
                userIdStr == null || userIdStr.isBlank() || userRequestDto == null
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 유저 아이디 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 유저 조회
        final User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 유저 명 수정
        if (userRequestDto.username() != null && !userRequestDto.username().isBlank()) {
            user.updateUsername(userRequestDto.username());
        }
    }

    /**
     * 유저 삭제 메서드
     * @param userIdStr 삭제할 유저 아이디 문자열
     */
    @Transactional
    public void deleteUser(final String userIdStr) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 삭제
        userRepository.deleteById(userId);
    }

    /**
     * 유저 스폰 포인트 만드는 메서드
     * @param userSpawnPointRequestDto 스폰 포인트 정보 담긴 DTO
     */
    @Transactional
    public void createUserSpawnPoint(
            final String userIdStr,
            final UserSpawnPointRequestDto userSpawnPointRequestDto
    ) {
        // 1) null 검사
        if (userSpawnPointRequestDto == null || userIdStr == null || userIdStr.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);
        final Integer x = userSpawnPointRequestDto.x();
        final Integer y = userSpawnPointRequestDto.y();

        // 3) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 스폰 포인트 이미 설정 했는지 확인
        if (tileRepository.existsByOwnerIdAndIsSpawnPoint(userId, true)){
            throw new CustomException(ExceptionCode.SPAWNPOINT_ALREADY_EXIST);
        }

        // 5) 해당 위치가 이미 점령 되어있는지 확인
        if (tileRepository.existsByXAndY(x, y)) {
            throw new CustomException(ExceptionCode.TILE_ALREADY_OCCUPIED);
        }

        // 6) 타일 엔티티 생성
        final Tile tile = Tile.builder()
                .x(x)
                .y(y)
                .owner(user)
                .defensePower(9999)
                .isSpawnPoint(true)
                .build();

        // 7) 저장
        tileRepository.save(tile);
    }

    /**
     * 유저 설정 PUT하는 메서드
     * @param userIdStr 유저 아이디 문자열
     * @param userSettingRequestDto 변경할 설정들
     * @return 변경된 설정 값
     */
    @Transactional
    public UserSettingResponseDto updateUserSetting(
            final String userIdStr,
            final UserSettingRequestDto userSettingRequestDto
    ) {
        // 1) null 검사
        if (
                userIdStr == null || userIdStr.isBlank() || userSettingRequestDto == null
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 설정 변경
        if (userSettingRequestDto.studyTime() != null) {
            user.updateStudyTime(userSettingRequestDto.studyTime());
        }
        if (userSettingRequestDto.restTime() != null) {
            user.updateRestTime(userSettingRequestDto.restTime());
        }

        return new UserSettingResponseDto(
                userSettingRequestDto.studyTime(),
                userSettingRequestDto.restTime()
        );
    }

    /**
     * 유저 설정 조회하는 메서드
     * @param userIdStr 유저 아이디 문자열
     * @return 유저의 설정
     */
    @Transactional
    public UserSettingResponseDto getUserSetting(
            final String userIdStr
    ) {
        // 1) null 검사
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 3) 유저 조회
        final User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        return new UserSettingResponseDto(
                user.getStudyTime(),
                user.getRestTime()
        );
    }
}
