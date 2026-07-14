package com.seohamin.pomoland.domain.session.controller;

import com.seohamin.pomoland.domain.session.dto.SessionResponseDto;
import com.seohamin.pomoland.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SessionController {

    private final SessionService sessionService;

    // 타이머 세션 시작하는 API
    @PostMapping("/session")
    public ResponseEntity<SessionResponseDto> createSession(
            @AuthenticationPrincipal final String userIdStr
    ) {

        return ResponseEntity.ok(sessionService.createSession(userIdStr));
    }

    // 타이머 세션 유지하는 API
    @GetMapping("/session/{sessionUuid}/heartbeat")
    public ResponseEntity<SessionResponseDto> heartbeat(
            @AuthenticationPrincipal final String userIdStr,
            @PathVariable final String sessionUuid
    ) {

        return ResponseEntity.ok(sessionService.heartbeat(userIdStr, sessionUuid));
    }

    // 타이머 세션 상태 조회 API
    @GetMapping("/session/{sessionUuid}")
    public ResponseEntity<SessionResponseDto> getSession(
            @AuthenticationPrincipal final String userIdStr,
            @PathVariable final String sessionUuid
    ) {

        return ResponseEntity.ok(sessionService.getSession(userIdStr, sessionUuid));
    }
}
