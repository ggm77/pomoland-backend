package com.seohamin.pomoland.domain.user.controller;

import com.seohamin.pomoland.domain.user.dto.UserResponseDto;
import com.seohamin.pomoland.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
