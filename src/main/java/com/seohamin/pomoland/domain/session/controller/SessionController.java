package com.seohamin.pomoland.domain.session.controller;

import com.seohamin.pomoland.domain.session.dto.SessionResponseDto;
import com.seohamin.pomoland.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
