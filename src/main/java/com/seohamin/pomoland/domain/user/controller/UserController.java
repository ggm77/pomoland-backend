package com.seohamin.pomoland.domain.user.controller;

import com.seohamin.pomoland.domain.user.dto.*;
import com.seohamin.pomoland.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    // 특정 유저 조회 API
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable final String userId
    ) {

        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 내 정보 조회 API
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getUserMe(
            @AuthenticationPrincipal final String userIdStr
    ) {
        return ResponseEntity.ok(userService.getUser(userIdStr));
    }

    // 내 정보 수정 API
    @PatchMapping("/users/me")
    public ResponseEntity<Void> updateUserMe(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final UserRequestDto userRequestDto
    ) {

        userService.updateUser(userIdStr, userRequestDto);

        return ResponseEntity.noContent().build();
    }

    // 회원 탈퇴 API
    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteUserMe(
            @AuthenticationPrincipal final String userIdStr
    ) {

        userService.deleteUser(userIdStr);

        return ResponseEntity.noContent().build();
    }

    // 스폰포인트 생성 API
    @PostMapping("/users/me/spawnpoint")
    public ResponseEntity<Void> createUserSpawnPoint(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final UserSpawnPointRequestDto userSpawnPointRequestDto
    ) {

        userService.createUserSpawnPoint(userIdStr, userSpawnPointRequestDto);

        return ResponseEntity.noContent().build();
    }

    // 유저 설정 PUT API
    @PutMapping("/users/me/settings")
    public ResponseEntity<UserSettingResponseDto> updateUserSettings(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final UserSettingRequestDto userSettingRequestDto
    ) {

        return ResponseEntity.ok(userService.updateUserSetting(userIdStr, userSettingRequestDto));
    }
}
