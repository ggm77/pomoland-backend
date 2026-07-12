package com.seohamin.pomoland.domain.user.service;

import com.seohamin.pomoland.domain.map.tile.dto.TileResponseDto;
import com.seohamin.pomoland.domain.map.tile.entity.Tile;
import com.seohamin.pomoland.domain.map.tile.repository.TileRepository;
import com.seohamin.pomoland.domain.user.dto.UserResponseDto;
import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
