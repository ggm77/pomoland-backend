package com.seohamin.pomoland.domain.map.tile.service;

import com.seohamin.pomoland.domain.map.tile.dto.MapResponseDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileOccupyRequestDto;
import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TileService {

    @Value("${map.x_size}")
    private Integer X_SIZE;

    @Value("${map.y_size}")
    private Integer Y_SIZE;

    private final TileRepository tileRepository;
    private final UserRepository userRepository;

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

    /**
     * 타일 점령하는 메서드
     * @param userIdStr 요청 유저 아이디 문자열
     * @param x x좌표
     * @param y y좌표
     */
    @Transactional
    public void occupy(
            final String userIdStr,
            final Integer x,
            final Integer y,
            final TileOccupyRequestDto tileOccupyRequestDto
    ) {
        // 1) null 검사
        if (
                x == null || y == null || userIdStr == null || userIdStr.isBlank()
                || tileOccupyRequestDto == null || tileOccupyRequestDto.point() == null
                || tileOccupyRequestDto.point() <= 0
        ) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 파싱 및 유저 조회
        final Long userId = Long.parseLong(userIdStr);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) 포인트 가지고 있는지 확인
        if (user.getPoint() < tileOccupyRequestDto.point()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 4) 해당 타일 조회
        final Tile tile = tileRepository.findByXAndY(x, y)
                .orElse(null);

        // 타일이 이미 점령 된 경우에만 검사
        if (tile != null) {
            // 5) 스폰 포인트인지 판단
            if (tile.getIsSpawnPoint()) {
                throw new CustomException(ExceptionCode.TILE_IS_SPAWNPOINT);
            }

            // 6) 점령 가능한 가격인지 판단
            if (tile.getDefensePower() > tileOccupyRequestDto.point()) {
                throw new CustomException(ExceptionCode.CANNOT_OCCUPY);
            }
        }

        // 7) 점령 가능한 위치인지 판단
        if (
                !tileRepository.isOccupiable(x, y, userId)
                || x < 0 || y < 0 || x >= X_SIZE || y >= Y_SIZE
        ) {
            throw new CustomException(ExceptionCode.CANNOT_OCCUPY);
        }

        // 8) 점령
        user.updatePoint(user.getPoint() - tileOccupyRequestDto.point()); // 포인트 차감
        // 타일이 이미 점령 되어있던 경우
        if (tile != null) {
            tile.updateOwner(user);
            tile.updateDefensePower(tileOccupyRequestDto.point());
        }
        // 새로 점령하는 경우
        else {
            final Tile newTile = Tile.builder()
                    .x(x)
                    .y(y)
                    .owner(user)
                    .defensePower(tileOccupyRequestDto.point())
                    .isSpawnPoint(false)
                    .build();

            tileRepository.save(newTile);
        }
    }
}
